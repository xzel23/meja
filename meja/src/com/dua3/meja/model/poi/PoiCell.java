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
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.poi.PoiCellStyle.PoiHssfCellStyle;
import com.dua3.meja.model.poi.PoiCellStyle.PoiXssfCellStyle;
import com.dua3.meja.model.poi.PoiFont.PoiHssfFont;
import com.dua3.meja.model.poi.PoiFont.PoiXssfFont;
import com.dua3.meja.model.poi.PoiRow.PoiHssfRow;
import com.dua3.meja.model.poi.PoiRow.PoiXssfRow;
import com.dua3.meja.model.poi.PoiSheet.PoiHssfSheet;
import com.dua3.meja.model.poi.PoiSheet.PoiXssfSheet;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import java.text.AttributedString;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
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
public abstract class PoiCell<WORKBOOK extends org.apache.poi.ss.usermodel.Workbook, SHEET extends org.apache.poi.ss.usermodel.Sheet, ROW extends org.apache.poi.ss.usermodel.Row, CELL extends org.apache.poi.ss.usermodel.Cell, CELLSTYLE extends org.apache.poi.ss.usermodel.CellStyle, COLOR extends org.apache.poi.ss.usermodel.Color>
        implements Cell {

    protected final CELL poiCell;
    private final CellRangeAddress mergedRegion;
    private final int spanX;
    private final int spanY;
    private final Cell logicalCell;

    protected PoiCell(CELL cell, PoiRow row) {
        this.poiCell = cell;
        this.mergedRegion = row.getSheet().getMergedRegion(cell.getRowIndex(), cell.getColumnIndex());

        if (mergedRegion == null) {
            // cell is not merged
            this.spanX = 1;
            this.spanY = 1;
            this.logicalCell = this;
        } else if (getRowNumber() == mergedRegion.getFirstRow()) {
            // cell part of the top row of a merged region
            if (getColumnNumber() == mergedRegion.getFirstColumn()) {
                // cell is the top left cell of the merged region
                this.spanX = 1 + mergedRegion.getLastColumn() - mergedRegion.getFirstColumn();
                this.spanY = 1 + mergedRegion.getLastRow() - mergedRegion.getFirstRow();
                this.logicalCell = this;
            } else {
                // cell is in top row of merged region, but not the leftmost cell
                this.spanX = 0;
                this.spanY = 0;
                this.logicalCell = row.getCell(mergedRegion.getFirstColumn());                
            }
        } else {
            // cell is merged, but not top row of merged region
            this.spanX = 0;
            this.spanY = 0;
            this.logicalCell = row.getSheet()
                    .getRow(mergedRegion.getFirstRow())
                    .getCell(mergedRegion.getFirstColumn());
        }
    }

    public abstract PoiWorkbook getWorkbook();

    @Override
    public abstract PoiSheet<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getSheet();

    @Override
    public CellType getCellType() {
        switch (poiCell.getCellType()) {
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK:
                return CellType.BLANK;
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
                return CellType.BOOLEAN;
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_ERROR:
                return CellType.ERROR;
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
                return CellType.NUMERIC;
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
                return CellType.TEXT;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean getBoolean() {
        return poiCell.getBooleanCellValue();
    }

    @Override
    public String getFormula() {
        return poiCell.getCellFormula();
    }

    @Override
    public Number getNumber() {
        return poiCell.getNumericCellValue();
    }

    @Override
    public String getText() {
        return poiCell.getStringCellValue();
    }

    @Override
    public String toString() {
        DataFormatter dataFormatter = getWorkbook().getDataFormatter();
        return dataFormatter.formatCellValue(poiCell);
    }

    @Override
    public final int getRowNumber() {
        return poiCell.getRowIndex();
    }

    @Override
    public final int getColumnNumber() {
        return poiCell.getColumnIndex();
    }

    @Override
    public int getHorizontalSpan() {
        return spanX;
    }

    @Override
    public int getVerticalSpan() {
        return spanY;
    }

    @Override
    public Cell getLogicalCell() {
        return logicalCell;
    }

    @Override
    public abstract PoiCellStyle getCellStyle();

    protected abstract PoiFont getFontForFormattingRun(RichTextString richText, int i);

    @Override
    public AttributedString getAttributedString() {
        if (getCellType()!=CellType.TEXT) {
            return new AttributedString(toString());
        }

        RichTextString richText = poiCell.getRichStringCellValue();

        String text = richText.getString();
        //TODO: properly process tabs
        text = text.replace('\t', ' ');
        text = text.replace((char) 160, ' ');

        AttributedString at = new AttributedString(text);
        PoiFont font = getCellStyle().getFont();
        font.addAttributes(at, 0, text.length());

        for (int i = 0; i < richText.numFormattingRuns(); i++) {
            int start = richText.getIndexOfFormattingRun(i);
            int end = i + 1 < richText.numFormattingRuns() ? richText.getIndexOfFormattingRun(i + 1) : richText.length();

            if (start == end) {
                // skip empty
                continue;
            }

            font = getFontForFormattingRun(richText, i);
            font.addAttributes(at, start, end);
        }
        return at;
    }

    static class PoiHssfCell extends PoiCell<
            HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell, HSSFCellStyle, HSSFColor> {

        private final PoiHssfRow row;

        PoiHssfCell(PoiHssfRow row, HSSFCell cell) {
            super(cell, row);
            this.row = row;
        }

        public final PoiHssfRow getRow() {
            return row;
        }

        @Override
        public final PoiHssfWorkbook getWorkbook() {
            return row.getSheet().getWorkbook();
        }

        @Override
        public PoiHssfSheet getSheet() {
            return row.getSheet();
        }

        @Override
        public PoiHssfCellStyle getCellStyle() {
            return getWorkbook().getPoiCellStyle(poiCell.getCellStyle());
        }

        @Override
        protected PoiHssfFont getFontForFormattingRun(RichTextString richText, int i) {
            return getWorkbook().getFont(((HSSFRichTextString) richText).getFontOfFormattingRun(i));
        }

    }

    static class PoiXssfCell extends PoiCell<
            XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell, XSSFCellStyle, XSSFColor> {

        private final PoiXssfRow row;

        PoiXssfCell(PoiXssfRow row, XSSFCell cell) {
            super(cell, row);
            this.row = row;
        }

        public final PoiXssfRow getRow() {
            return row;
        }

        @Override
        public final PoiXssfWorkbook getWorkbook() {
            return row.getSheet().getWorkbook();
        }

        @Override
        public PoiXssfSheet getSheet() {
            return row.getSheet();
        }

        @Override
        public PoiXssfCellStyle getCellStyle() {
            return getWorkbook().getPoiCellStyle(poiCell.getCellStyle());
        }

        @Override
        protected PoiXssfFont getFontForFormattingRun(RichTextString richText, int i) {
            // Catch NPE as Workaround for Apache POI Bug 56511 (will be fixed in 3.12)
            // https://bz.apache.org/bugzilla/show_bug.cgi?id=56511
            try {
                return getWorkbook().getFont(((XSSFRichTextString) richText).getFontOfFormattingRun(i));
            } catch (NullPointerException e) {
                return getCellStyle().getFont();
            }
        }
    }

}
