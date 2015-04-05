/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.model.poi;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.poi.PoiCellStyle.PoiHssfCellStyle;
import com.dua3.meja.model.poi.PoiCellStyle.PoiXssfCellStyle;
import com.dua3.meja.model.poi.PoiRow.PoiHssfRow;
import com.dua3.meja.model.poi.PoiRow.PoiXssfRow;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.meja.util.Cache;
import java.util.Objects;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author axel
 * @param <WORKBOOK>
 * @param <SHEET>
 * @param <ROW>
 * @param <CELL>
 * @param <CELLSTYLE>
 * @param <COLOR>
 */
public abstract class PoiSheet<WORKBOOK extends org.apache.poi.ss.usermodel.Workbook, SHEET extends org.apache.poi.ss.usermodel.Sheet, ROW extends org.apache.poi.ss.usermodel.Row, CELL extends org.apache.poi.ss.usermodel.Cell, CELLSTYLE extends org.apache.poi.ss.usermodel.CellStyle, COLOR extends org.apache.poi.ss.usermodel.Color>
        implements Sheet {


    protected final SHEET poiSheet;
    private int firstColumn;
    private int lastColumn;

    public PoiSheet(SHEET poiSheet) {
        this.poiSheet = poiSheet;
        update();
    }

    public SHEET getPoiSheet() {
        return poiSheet;
    }

    public CellRangeAddress getMergedRegion(int rowNum, int colNum) {
        for (int i = 0; i < poiSheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = poiSheet.getMergedRegion(i);
            if (mergedRegion.isInRange(rowNum, colNum)) {
                return mergedRegion;
            }
        }
        return null;
    }

    private void update() {
        firstColumn = Integer.MAX_VALUE;
        lastColumn = 0;
        for (int i = poiSheet.getFirstRowNum(); i < poiSheet.getLastRowNum(); i++) {
            final org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(i);
            if (poiRow != null) {
                final short firstCellNum = poiRow.getFirstCellNum();
                if (firstCellNum >= 0) {
                    firstColumn = Math.min(firstColumn, firstCellNum);
                }
                final short lastCellNum = (short) (poiRow.getLastCellNum() - 1);
                if (lastCellNum >= 0) {
                    lastColumn = Math.max(lastColumn, lastCellNum);
                }
            }
        }

        if (firstColumn == Integer.MAX_VALUE) {
            firstColumn = 0;
            lastColumn = 0;
        }
    }

    @Override
    public int getNumberOfColumns() {
        return lastColumn - firstColumn;
    }

    @Override
    public int getFirstColNum() {
        return firstColumn;
    }

    @Override
    public int getLastColNum() {
        return lastColumn;
    }

    @Override
    public int getFirstRowNum() {
        return poiSheet.getFirstRowNum();
    }

    @Override
    public int getLastRowNum() {
        return poiSheet.getLastRowNum();
    }

    @Override
    public int getNumberOfRows() {
        return 1+getLastRowNum() - getFirstRowNum();
    }

    @Override
    public String getSheetName() {
        return poiSheet.getSheetName();
    }

    @Override
    public abstract PoiWorkbook<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getWorkbook();

    @Override
    public double getColumnWidth(int col) {
        double fontSize = getWorkbook().getDefaultCellStyle().getFont().getSizeInPoints();
        return poiSheet.getColumnWidth(col) * fontSize * 0.6175 / 256.0;
    }

    @Override
    public double getRowHeight(int rowNum) {
        final org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(rowNum);
        return poiRow == null ? poiSheet.getDefaultRowHeightInPoints() : poiRow.getHeightInPoints();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiSheet) {
            return Objects.equals(poiSheet, ((PoiSheet)obj).poiSheet);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return poiSheet.hashCode();
    }

    @Override
    public TableModel getTableModel() {
        return new AbstractTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public int getRowCount() {
                return getNumberOfRows();
            }

            @Override
            public int getColumnCount() {
                return getNumberOfColumns();
            }

            @Override
            public String getColumnName(int columnIndex) {
                int col = getSheetCol(columnIndex);
                StringBuilder sb = new StringBuilder();
                do {
                    sb.append((char) ('A' + col % 26));
                    col /= 26;
                } while (col > 0);
                return sb.toString();
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Cell.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                int rowNum = getSheetRow(rowIndex);
                int colNum = getSheetCol(columnIndex);
                Row row = getRow(rowNum);
                Cell cell = row == null ? null : row.getCell(colNum);
                return cell;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            private int getSheetCol(int columnIndex) {
                return columnIndex + getFirstColNum();
            }

            private int getSheetRow(int rowIndex) {
                return rowIndex + getFirstRowNum();
            }
        };

    }

    static class PoiHssfSheet extends PoiSheet<
            HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell, HSSFCellStyle, HSSFColor> {

        private final PoiHssfWorkbook workbook;
        private final PoiHssfCellStyle defaultCellStyle;
        private final Cache<HSSFRow, PoiHssfRow> cache = new Cache<HSSFRow, PoiHssfRow>(Cache.Type.WEAK_KEYS){

            @Override
            protected PoiHssfRow create(HSSFRow poiRow) {
                return new PoiHssfRow(PoiHssfSheet.this, poiRow);
            }

        };

        public PoiHssfSheet(PoiHssfWorkbook workbook, HSSFSheet poiSheet) {
            super(poiSheet);
            this.workbook = workbook;
            this.defaultCellStyle = workbook.getDefaultCellStyle();
        }

        @Override
        public PoiHssfRow getRow(int row) {
            HSSFRow poiRow = poiSheet.getRow(row);
            if(poiRow==null) {
                poiRow=poiSheet.createRow(row);
            }
            return cache.get(poiRow);
        }

        @Override
        public PoiHssfCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        @Override
        public PoiHssfWorkbook getWorkbook() {
            return workbook;
        }
    }

    static class PoiXssfSheet extends PoiSheet<
            XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell, XSSFCellStyle, XSSFColor> {

        private final PoiXssfWorkbook workbook;
        private final PoiXssfCellStyle defaultCellStyle;
        private final Cache<XSSFRow, PoiXssfRow> cache = new Cache<XSSFRow, PoiXssfRow>(Cache.Type.WEAK_KEYS) {

            @Override
            protected PoiXssfRow create(XSSFRow poiRow) {
                return new PoiXssfRow(PoiXssfSheet.this, poiRow);
            }

        };

        public PoiXssfSheet(PoiXssfWorkbook workbook, XSSFSheet poiSheet) {
            super(poiSheet);
            this.workbook = workbook;
            this.defaultCellStyle = workbook.getDefaultCellStyle();
        }

        @Override
        public PoiXssfRow getRow(int row) {
            XSSFRow poiRow = poiSheet.getRow(row);
            if(poiRow==null) {
                poiRow=poiSheet.createRow(row);
            }
            return cache.get(poiRow);
        }

        @Override
        public PoiXssfCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        @Override
        public PoiXssfWorkbook getWorkbook() {
            return workbook;
        }
    }

}
