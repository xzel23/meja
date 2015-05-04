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
import com.dua3.meja.util.RectangularRegion;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.lang.ref.SoftReference;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataFormatter;
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
public class PoiCell implements Cell {

    protected final PoiWorkbook workbook;
    protected final PoiRow row;
    protected final org.apache.poi.ss.usermodel.Cell poiCell;
    protected final RectangularRegion mergedRegion;
    protected final int spanX;
    protected final int spanY;
    protected final PoiCell logicalCell;
    protected SoftReference<AttributedString> attributedString = new SoftReference<>(null);

    public PoiCell(PoiRow row, org.apache.poi.ss.usermodel.Cell cell) {
        this.workbook = row.getWorkbook();
        this.row = row;
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
    public Cell set(AttributedString s) {
        RichTextString richText = workbook.createRichTextString(AttributedStringHelper.toString(s));
        AttributedCharacterIterator iter = s.getIterator();
        int endIndex = iter.getEndIndex();
        while (iter.getIndex()!=iter.getEndIndex()) {
            int runStart = iter.getRunStart();
            int runLimit = iter.getRunLimit();

            final Font defaultFont = workbook.getDefaultCellStyle().getFont().poiFont;
            org.apache.poi.ss.usermodel.Font font = workbook.getPoiWorkbook().createFont();
            for (Map.Entry<Attribute, Object> entry: iter.getAttributes().entrySet()) {
                Attribute attribute = entry.getKey();
                Object value = entry.getValue();
                if (attribute==TextAttribute.FAMILY) {
                    font.setFontName(value!=null?value.toString():defaultFont.getFontName());
                } else if (attribute==TextAttribute.SIZE) {
                    font.setFontHeightInPoints(value!=null?((Number)value).shortValue():defaultFont.getFontHeightInPoints());
                } else if (attribute==TextAttribute.FOREGROUND) {
                    org.apache.poi.ss.usermodel.Color poiColor = workbook.getPoiColor((Color)value);
                    if (font instanceof XSSFFont && poiColor instanceof XSSFColor) {
                        ((XSSFFont)font).setColor((XSSFColor)poiColor);
                    } if (font instanceof HSSFFont && poiColor instanceof HSSFColor) {
                        font.setColor(((HSSFColor)poiColor).getIndex());
                    } else {
                        // this should never happen because font and color
                        // should always both be either XSSF or HSSF instances
                        throw new IllegalStateException();
                    }
                } else if (attribute==TextAttribute.WEIGHT) {
                    font.setBoldweight(((Number)value).shortValue());
                } else if (attribute==TextAttribute.UNDERLINE) {
                    font.setUnderline(TextAttribute.UNDERLINE_ON.equals(value)?Font.U_SINGLE:Font.U_NONE);
                } else if (attribute==TextAttribute.STRIKETHROUGH) {
                    font.setStrikeout(TextAttribute.STRIKETHROUGH_ON.equals(value));
                }

            }
            richText.applyFont(runStart, runLimit, font);
        }
        poiCell.setCellValue(richText);
        return this;
    }

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
    public PoiCell set(Boolean arg) {
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
    public PoiCell set(Date arg) {
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
    public PoiCell set(Number arg) {
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
    public PoiCell set(String arg) {
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
    public PoiCell setFormula(String arg) {
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellFormula(arg);
            poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA);
            attributedString.clear();
            getWorkbook().evaluator.evaluateFormulaCell(poiCell);
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

    @Override
    public PoiCellStyle getCellStyle() {
        return workbook.getPoiCellStyle(poiCell.getCellStyle());
    }

    private PoiFont getFontForFormattingRun(RichTextString richText, int i) {
        if (richText instanceof HSSFRichTextString) {
            HSSFRichTextString hssfRichText = (HSSFRichTextString) richText;
            return ((PoiHssfWorkbook)workbook).getFont(hssfRichText.getFontOfFormattingRun(i));
        } else {
            // Catch NPE as Workaround for Apache POI Bug 56511 (will be fixed in 3.12)
            // https://bz.apache.org/bugzilla/show_bug.cgi?id=56511
            try {
                return workbook.getFont(((XSSFRichTextString) richText).getFontOfFormattingRun(i));
            } catch (NullPointerException e) {
                return getCellStyle().getFont();
            }
        }
    }

    @Override
    public void copy(Cell other) {
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
            case TEXT:
                // FIXME AttributedString
                set(other.getText());
                break;
        }
        setCellStyle(other.getCellStyle().getName());
    }

}
