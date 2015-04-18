/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
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
import com.dua3.meja.model.CellStyle;
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
import com.dua3.meja.util.RectangularRegion;
import java.lang.ref.SoftReference;
import java.text.AttributedString;
import java.util.Date;
import java.util.Objects;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
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
 * @param <WORKBOOK> POI workbook class
 * @param <SHEET> POI sheet class
 * @param <ROW> POI row class
 * @param <CELL> POI cell class
 * @param <CELLSTYLE> POI cell style class
 * @param <COLOR> POI color class
 */
public abstract class PoiCell<WORKBOOK extends org.apache.poi.ss.usermodel.Workbook, SHEET extends org.apache.poi.ss.usermodel.Sheet, ROW extends org.apache.poi.ss.usermodel.Row, CELL extends org.apache.poi.ss.usermodel.Cell, CELLSTYLE extends org.apache.poi.ss.usermodel.CellStyle, COLOR extends org.apache.poi.ss.usermodel.Color>
        implements Cell {

    protected final CELL poiCell;
    protected final RectangularRegion mergedRegion;
    protected final int spanX;
    protected final int spanY;
    protected final Cell logicalCell;
    protected SoftReference<AttributedString> attributedString = new SoftReference<>(null);

    protected PoiCell(CELL cell, PoiRow<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> row) {
        this.poiCell = cell;
        this.mergedRegion = row.getMergedRegion(cell.getColumnIndex());

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

    public abstract PoiWorkbook<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getWorkbook();

    @Override
    public abstract PoiSheet<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getSheet();

    private CellType translateCellType(int poiType) {
        switch (poiType) {
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
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA:
                return CellType.FORMULA;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public CellType getCellType() {
        return translateCellType(poiCell.getCellType());
    }

    @Override
    public CellType getResultType() {
        int poiType = poiCell.getCellType();
        if (poiType == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA) {
            poiType = poiCell.getCachedFormulaResultType();
        }
        return translateCellType(poiType);
    }

    @Override
    public boolean getBoolean() {
        return isEmpty() ? null : poiCell.getBooleanCellValue();
    }

    @Override
    public String getFormula() {
        return isEmpty() ? null : poiCell.getCellFormula();
    }

    @Override
    public Date getDate() {
        return isEmpty() ? null : poiCell.getDateCellValue();
    }

    @Override
    public Number getNumber() {
        return isEmpty() ? null : poiCell.getNumericCellValue();
    }

    @Override
    public String getText() {
        return isEmpty() ? "" : poiCell.getStringCellValue();
    }

    @Override
    public String getAsText() {
        if (isEmpty()) {
            return "";
        }

        DataFormatter dataFormatter = getWorkbook().getDataFormatter();
        try {
            FormulaEvaluator evaluator = getWorkbook().evaluator;
            return dataFormatter.formatCellValue(poiCell, evaluator);
        } catch (Exception ex) {
            return Cell.ERROR_TEXT;
        }
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
    public abstract PoiCellStyle<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getCellStyle();

    protected abstract PoiFont<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getFontForFormattingRun(RichTextString richText, int i);

    @Override
    public AttributedString getAttributedString() {
        if (isEmpty()) {
            return new AttributedString("");
        }

        final CellType cellType = getCellType();
        if (cellType == CellType.FORMULA) {
            return new AttributedString(getAsText());
        }

        AttributedString as = attributedString.get();
        if (as == null) {
            if (cellType != CellType.TEXT) {
                as = new AttributedString(getAsText());
            } else {
                RichTextString richText = poiCell.getRichStringCellValue();

                String text = richText.getString();
                //TODO: properly process tabs
                text = text.replace('\t', ' '); // tab
                text = text.replace((char) 160, ' '); // non-breaking space

                as = new AttributedString(text);

                // apply cell style font attributes first
                getCellStyle().getFont().addAttributes(as, 0, text.length());

                for (int i = 0; i < richText.numFormattingRuns(); i++) {
                    int start = richText.getIndexOfFormattingRun(i);
                    int end = i + 1 < richText.numFormattingRuns() ? richText.getIndexOfFormattingRun(i + 1) : richText.length();

                    if (start == end) {
                        // skip empty
                        continue;
                    }

                    // apply font attributes for formatting run
                    getFontForFormattingRun(richText, i).addAttributes(as, start, end);
                }
            }
            attributedString = new SoftReference<>(as);
        }
        return as;
    }

    @Override
    public void clear() {
        poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK);
        attributedString.clear();
    }

    @Override
    public PoiCell<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> set(Boolean arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
            poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN);
            attributedString.clear();
        }
        return this;
    }

    @Override
    public PoiCell<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> set(Date arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
            poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
            attributedString.clear();
        }
        return this;
    }

    @Override
    public PoiCell<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> set(Number arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg.doubleValue());
            poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
            attributedString.clear();
        }
        return this;
    }

    @Override
    public PoiCell<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> set(String arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
            poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING);
            attributedString.clear();
        }
        return this;
    }

    @Override
    public PoiCell<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> setFormula(String arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
            poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA);
            attributedString.clear();
        }
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiCell) {
            return Objects.equals(poiCell, ((PoiCell) obj).poiCell);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return poiCell.hashCode();
    }

    @Override
    public boolean isEmpty() {
        switch (poiCell.getCellType()) {
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK:
                return true;
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
                return poiCell.getStringCellValue().isEmpty();
            default:
                return false;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setCellStyle(CellStyle cellStyle) {
        if (cellStyle instanceof PoiCellStyle) {
            poiCell.setCellStyle(((PoiCellStyle) cellStyle).poiCellStyle);
            attributedString.clear();
        } else {
            throw new IllegalArgumentException("Incompatible implementation.");
        }
    }

    @Override
    public void setCellStyle(String cellStyleName) {
        setCellStyle(getWorkbook().getCellStyle(cellStyleName));
    }

    static class PoiHssfCell extends PoiCell<
            HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell, HSSFCellStyle, HSSFColor> {

        private final PoiHssfWorkbook workbook;
        private final PoiHssfRow row;

        PoiHssfCell(PoiHssfRow row, HSSFCell cell) {
            super(cell, row);
            this.workbook = row.getWorkbook();
            this.row = row;
        }

        @Override
        public final PoiHssfRow getRow() {
            return row;
        }

        @Override
        public final PoiHssfWorkbook getWorkbook() {
            return workbook;
        }

        @Override
        public PoiHssfSheet getSheet() {
            return row.getSheet();
        }

        @Override
        public PoiHssfCellStyle getCellStyle() {
            return workbook.getPoiCellStyle(poiCell.getCellStyle());
        }

        @Override
        protected PoiHssfFont getFontForFormattingRun(RichTextString richText, int i) {
            return workbook.getFont(((HSSFRichTextString) richText).getFontOfFormattingRun(i));
        }

        @Override
        public void setCellStyle(CellStyle cellStyle) {
            if (cellStyle instanceof PoiHssfCellStyle) {
                poiCell.setCellStyle(((PoiHssfCellStyle) cellStyle).poiCellStyle);
                attributedString.clear();
            } else {
                throw new IllegalArgumentException("Incompatible implementation.");
            }
        }

    }

    static class PoiXssfCell extends PoiCell<
            XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell, XSSFCellStyle, XSSFColor> {

        private final PoiXssfWorkbook workbook;
        private final PoiXssfRow row;

        PoiXssfCell(PoiXssfRow row, XSSFCell cell) {
            super(cell, row);
            this.workbook = row.getWorkbook();
            this.row = row;
        }

        @Override
        public final PoiXssfRow getRow() {
            return row;
        }

        @Override
        public final PoiXssfWorkbook getWorkbook() {
            return workbook;
        }

        @Override
        public PoiXssfSheet getSheet() {
            return row.getSheet();
        }

        @Override
        public PoiXssfCellStyle getCellStyle() {
            return workbook.getPoiCellStyle(poiCell.getCellStyle());
        }

        @Override
        protected PoiXssfFont getFontForFormattingRun(RichTextString richText, int i) {
            // Catch NPE as Workaround for Apache POI Bug 56511 (will be fixed in 3.12)
            // https://bz.apache.org/bugzilla/show_bug.cgi?id=56511
            try {
                return workbook.getFont(((XSSFRichTextString) richText).getFontOfFormattingRun(i));
            } catch (NullPointerException e) {
                return getCellStyle().getFont();
            }
        }

    }

}
