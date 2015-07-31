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
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.util.AttributedStringHelper;
import com.dua3.meja.util.MejaHelper;
import com.dua3.meja.util.RectangularRegion;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

/**
 *
 * @author axel
 */
public final class PoiCell implements Cell {

    protected final PoiWorkbook workbook;
    protected final PoiRow row;
    protected final org.apache.poi.ss.usermodel.Cell poiCell;
    protected int spanX;
    protected int spanY;
    protected PoiCell logicalCell;

    /**
     *
     * @param row
     * @param cell
     */
    public PoiCell(PoiRow row, org.apache.poi.ss.usermodel.Cell cell) {
        this.workbook = row.getWorkbook();
        this.row = row;
        this.poiCell = cell;

        RectangularRegion mergedRegion = row.getMergedRegion(cell.getColumnIndex());

        if (mergedRegion == null) {
            // cell is not merged
            this.spanX = 1;
            this.spanY = 1;
            this.logicalCell = this;
        } else {
            boolean isTop = getRowNumber() == mergedRegion.getFirstRow();
            boolean isTopLeft = isTop && getColumnNumber() == mergedRegion.getFirstColumn();
            PoiCell topLeftCell;
            if (isTopLeft) {
                topLeftCell = this;
            } else {
                PoiRow topRow = isTop ? row : row.getSheet().getRow(mergedRegion.getFirstRow());
                topLeftCell = topRow.getCell(mergedRegion.getFirstColumn());
            }

            int spanX_ = 1 + mergedRegion.getLastColumn() - mergedRegion.getFirstColumn();
            int spanY_ = 1 + mergedRegion.getLastRow() - mergedRegion.getFirstRow();

            addedToMergedRegion(topLeftCell, spanX_, spanY_);
        }

    }

    @Override
    public PoiWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public PoiSheet getSheet() {
        return row.getSheet();
    }

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
    public PoiRow getRow() {
        return row;
    }

    @Override
    public CellType getCellType() {
        CellType type = translateCellType(poiCell.getCellType());
        // since formulas returning dates should return CellType.FORMULA
        // rather than CellType.DATE, only test for dates if cell is numeric.
        if (type == CellType.NUMERIC && isCellDateFormatted()) {
            type = CellType.DATE;
        }
        return type;
    }

    @Override
    public CellType getResultType() {
        int poiType = poiCell.getCellType();
        if (poiType == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA) {
            poiType = poiCell.getCachedFormulaResultType();
        }
        CellType type = translateCellType(poiType);
        if (type == CellType.NUMERIC && isCellDateFormatted()) {
            type = CellType.DATE;
        }
        return type;
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
    public int getRowNumber() {
        return poiCell.getRowIndex();
    }

    @Override
    public int getColumnNumber() {
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
    public Cell set(AttributedString s) {
        RichTextString richText = workbook.createRichTextString(AttributedStringHelper.toString(s));
        AttributedCharacterIterator iter = s.getIterator();
        int endIndex = iter.getEndIndex();
        while (iter.getIndex() != endIndex) {
            int runStart = iter.getRunStart();
            int runLimit = iter.getRunLimit();

            final Font defaultFont = workbook.getDefaultCellStyle().getFont().poiFont;
            org.apache.poi.ss.usermodel.Font font = workbook.getPoiWorkbook().createFont();
            for (Map.Entry<Attribute, Object> entry : iter.getAttributes().entrySet()) {
                Attribute attribute = entry.getKey();
                Object value = entry.getValue();
                if (attribute == TextAttribute.FAMILY) {
                    font.setFontName(value != null ? value.toString() : defaultFont.getFontName());
                } else if (attribute == TextAttribute.SIZE) {
                    font.setFontHeightInPoints(value != null ? ((Number) value).shortValue() : defaultFont.getFontHeightInPoints());
                } else if (attribute == TextAttribute.FOREGROUND) {
                    org.apache.poi.ss.usermodel.Color poiColor = workbook.getPoiColor((Color) value);
                    if (font instanceof XSSFFont && poiColor instanceof XSSFColor) {
                        ((XSSFFont) font).setColor((XSSFColor) poiColor);
                    } else if (font instanceof HSSFFont && poiColor instanceof HSSFColor) {
                        font.setColor(((HSSFColor) poiColor).getIndex());
                    } else {
                        // this should never happen because font and color
                        // should always both be either XSSF or HSSF instances
                        throw new IllegalStateException();
                    }
                } else if (attribute == TextAttribute.WEIGHT) {
                    font.setBoldweight(((Number) value).shortValue());
                } else if (attribute == TextAttribute.UNDERLINE) {
                    font.setUnderline(TextAttribute.UNDERLINE_ON.equals(value) ? Font.U_SINGLE : Font.U_NONE);
                } else if (attribute == TextAttribute.STRIKETHROUGH) {
                    font.setStrikeout(TextAttribute.STRIKETHROUGH_ON.equals(value));
                }
            }
            richText.applyFont(runStart, runLimit, font);
            iter.setIndex(runLimit);
        }
        poiCell.setCellValue(richText);

        updateRow();

        return this;
    }

    @Override
    public AttributedString getAttributedString() {
        if (getCellType() != CellType.TEXT) {
            return new AttributedString(getAsText());
        }

        RichTextString richText = poiCell.getRichStringCellValue();

        String text = richText.getString();
        //TODO: properly process tabs
        text = text.replace('\t', ' '); // tab
        text = text.replace((char) 160, ' '); // non-breaking space

        AttributedString as = new AttributedString(text);

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

        return as;
    }

    @Override
    public void clear() {
        poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK);
        updateRow();
    }

    @Override
    public PoiCell set(Boolean arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
        }
        updateRow();
        return this;
    }

    @Override
    public PoiCell set(Date arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
            if (!isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is not date formatted
                Logger.getLogger(PoiCell.class.getName()).warning("Cell is not date formatted!");
            }
        }
        updateRow();
        return this;
    }

    private boolean isCellDateFormatted() {
        /*
         * DateUtil.isCellDateFormatted() throws IllegalStateException
         * when cell is not numeric, so we have to work around this.
         * TODO create SCCSE and report bug against POI
         */
        int poiType = poiCell.getCellType();
        if (poiType==org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA) {
            poiType = poiCell.getCachedFormulaResultType();
        }
        return (poiType == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC) 
                && DateUtil.isCellDateFormatted(poiCell);
    }

    @Override
    public PoiCell set(Number arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg.doubleValue());
            if (isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is date formatted, but a plain number is stored
                Logger.getLogger(PoiCell.class.getName()).warning("Cell is date formatted, but plain number written!");
            }
        }
        updateRow();
        return this;
    }

    @Override
    public PoiCell set(String arg) {
        poiCell.setCellValue(arg);
        updateRow();
        return this;
    }

    @Override
    public PoiCell setFormula(String arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellFormula(arg);
            poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA);
            getWorkbook().evaluator.evaluateFormulaCell(poiCell);
        }
        updateRow();
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
        } else {
            throw new IllegalArgumentException("Incompatible implementation.");
        }
    }

    @Override
    public void setCellStyle(String cellStyleName) {
        setCellStyle(getWorkbook().getCellStyle(cellStyleName));
    }

    @Override
    public PoiCellStyle getCellStyle() {
        return workbook.getPoiCellStyle(poiCell.getCellStyle());
    }

    private PoiFont getFontForFormattingRun(RichTextString richText, int i) {
        if (richText instanceof HSSFRichTextString) {
            HSSFRichTextString hssfRichText = (HSSFRichTextString) richText;
            return ((PoiHssfWorkbook) workbook).getFont(hssfRichText.getFontOfFormattingRun(i));
        } else {
            return workbook.getFont(((XSSFRichTextString) richText).getFontOfFormattingRun(i));
        }
    }

    @Override
    public void copy(Cell other) {
        setCellStyle(other.getCellStyle().getName());

        switch (other.getCellType()) {
            case BLANK:
                clear();
                break;
            case BOOLEAN:
                set(other.getBoolean());
                break;
            case ERROR:
                // FIXME
                setFormula("1/0");
                break;
            case NUMERIC:
                set(other.getNumber());
                break;
            case DATE:
                set(other.getDate());
                break;
            case TEXT:
                if (other.isRichText()) {
                    set(other.getAttributedString());
                } else {
                    set(other.getText());
                }
                break;
        }
    }

    /**
     * Update sheet data, ie. first and last cell numbers.
     */
    private void updateRow() {
        if (getCellType() != CellType.BLANK) {
            getRow().setColumnUsed(getColumnNumber());
        }
    }

    void addedToMergedRegion(PoiCell topLeftCell, int spanX, int spanY) {
        if (this.getRowNumber() == topLeftCell.getRowNumber()
                && this.getColumnNumber() == topLeftCell.getColumnNumber()) {
            this.logicalCell = topLeftCell;
            this.spanX = spanX;
            this.spanY = spanY;
        } else {
            clear();
            this.logicalCell = topLeftCell;
            this.spanX = 0;
            this.spanY = 0;
        }
    }

    void removedFromMergedRegion() {
        this.logicalCell = this;
        this.spanX = 1;
        this.spanY = 1;
    }

    @Override
    public void unMerge() {
        if (logicalCell != this) {
            // this should never happen because we checked for this cell being
            // the top left cell of the merged region
            throw new IllegalArgumentException("Cell is not top left cell of a merged region");
        }

        getSheet().removeMergedRegion(getRowNumber(), getColumnNumber());
    }

    @Override
    public boolean isRichText() {
        return getResultType()==CellType.TEXT && poiCell.getRichStringCellValue().numFormattingRuns()!=0;
    }

    @Override
    public String getCellRef() {
        return MejaHelper.getCellRef(this, false);
    }

    @Override
    public String getCellRef(boolean includeSheet) {
        return MejaHelper.getCellRef(this, includeSheet);
    }
}
