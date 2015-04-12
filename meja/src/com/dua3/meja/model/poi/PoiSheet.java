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

import com.dua3.meja.model.MejaHelper;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.poi.PoiRow.PoiHssfRow;
import com.dua3.meja.model.poi.PoiRow.PoiXssfRow;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.meja.util.RectangularRegion;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
    private List<RectangularRegion> mergedRegions;

    public PoiSheet(SHEET poiSheet) {
        this.poiSheet = poiSheet;
        update();
    }

    public SHEET getPoiSheet() {
        return poiSheet;
    }

    public RectangularRegion getMergedRegion(int rowNum, int colNum) {
        for (RectangularRegion rr: mergedRegions) {
            if (rr.contains(rowNum, colNum)) {
                return rr;
            }
        }
        return null;
    }

    private void update() {
        // update row and column information
        firstColumn = Integer.MAX_VALUE;
        lastColumn = 0;
        for (int i = poiSheet.getFirstRowNum(); i < poiSheet.getLastRowNum()+1; i++) {
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

        // extract merged regions
        final int numMergedRegions = poiSheet.getNumMergedRegions(); // SLOW in XssfSheet (poi 3.11)
        mergedRegions = new ArrayList<>(numMergedRegions);
        for (int i = 0; i < numMergedRegions; i++) {
            CellRangeAddress r = poiSheet.getMergedRegion(i);
            final RectangularRegion rr = new RectangularRegion(r.getFirstRow(), r.getLastRow(), r.getFirstColumn(), r.getLastColumn());
            mergedRegions.add(rr);
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
    public float getColumnWidth(int col) {
        float fontSize = getWorkbook().getDefaultCellStyle().getFont().getSizeInPoints();
        return poiSheet.getColumnWidth(col) * fontSize * 0.6175f / 256;
    }

    @Override
    public float getRowHeight(int rowNum) {
        final org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(rowNum);
        return poiRow == null ? poiSheet.getDefaultRowHeightInPoints() : poiRow.getHeightInPoints();
    }

    @SuppressWarnings("rawtypes")
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
        return MejaHelper.getTableModel(this);
    }

    @Override
    public void freeze(int i, int j) {
        poiSheet.createFreezePane(i+1, j+1);
    }

    @Override
    public abstract PoiRow<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getRow(int i);

    @Override
    public PoiCell<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getCell(int i, int j) {
    	return getRow(i).getCell(j);
    }

    @Override
    public void autoSizeColumn(int j) {
    	poiSheet.autoSizeColumn(j);
    }

    @Override
    public void setAutofilter(int rowNumber) {
        org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(rowNumber);
		short col1 = poiRow.getFirstCellNum();
        short coln = poiRow.getLastCellNum();
        poiSheet.setAutoFilter(new CellRangeAddress(rowNumber, rowNumber, col1, coln));
    }

	@Override
	public Iterator<Row> iterator() {
		return new Iterator<Row>() {

			private int rowNum=PoiSheet.this.poiSheet.getFirstRowNum();

			@Override
			public boolean hasNext() {
				return rowNum<PoiSheet.this.poiSheet.getLastRowNum();
			}

			@Override
			public Row next() {
				return getRow(rowNum++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removing of rows is not supported.");
			}
		};
	}

	static class PoiHssfSheet extends PoiSheet<
            HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell, HSSFCellStyle, HSSFColor> {

        private final PoiHssfWorkbook workbook;

        public PoiHssfSheet(PoiHssfWorkbook workbook, HSSFSheet poiSheet) {
            super(poiSheet);
            this.workbook = workbook;
        }

        @Override
        public PoiHssfRow getRow(int row) {
            HSSFRow poiRow = poiSheet.getRow(row);
            if(poiRow==null) {
                poiRow=poiSheet.createRow(row);
            }
            return new PoiHssfRow(PoiHssfSheet.this, poiRow);
        }

        @Override
        public PoiHssfWorkbook getWorkbook() {
            return workbook;
        }
    }

    static class PoiXssfSheet extends PoiSheet<
            XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell, XSSFCellStyle, XSSFColor> {

        private final PoiXssfWorkbook workbook;

        public PoiXssfSheet(PoiXssfWorkbook workbook, XSSFSheet poiSheet) {
            super(poiSheet);
            this.workbook = workbook;
        }

        @Override
        public PoiXssfRow getRow(int row) {
            XSSFRow poiRow = poiSheet.getRow(row);
            if(poiRow==null) {
                poiRow=poiSheet.createRow(row);
            }
            return new PoiXssfRow(PoiXssfSheet.this, poiRow);
        }

        @Override
        public PoiXssfWorkbook getWorkbook() {
            return workbook;
        }
    }

}
