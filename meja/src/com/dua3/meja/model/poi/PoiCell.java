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
import com.dua3.meja.text.RichText;
import com.dua3.meja.text.RichTextBuilder;
import com.dua3.meja.util.MejaHelper;
import com.dua3.meja.util.RectangularRegion;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

/**
 *
 * @author axel
 */
public final class PoiCell implements Cell {
    private static final Logger LOGGER = Logger.getLogger(PoiCell.class.getName());

    private static CellType translateCellType(int poiType) {
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

    final PoiWorkbook workbook;
    final PoiRow row;
    final org.apache.poi.ss.usermodel.Cell poiCell;
    int spanX;
    int spanY;
    PoiCell logicalCell;

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

    private IllegalStateException newIllegalStateException(Exception e) {
        return new IllegalStateException("["+getCellRef(true)+"]: "+e.getMessage());
    }

    @Override
    public Object get() {
        if (isEmpty()) {
            return null;
        }

        switch (getCellType()) {
            case BLANK:
                return null;
            case DATE:
                return poiCell.getDateCellValue();
            case NUMERIC:
                return poiCell.getNumericCellValue();
            case FORMULA:
                return poiCell.getCellFormula();
            case BOOLEAN:
                return poiCell.getBooleanCellValue();
            case TEXT:
                return getText();
            case ERROR:
                return ERROR_TEXT;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public boolean getBoolean() {
        try {
            return isEmpty() ? null : poiCell.getBooleanCellValue();
        } catch (Exception e) {
            throw newIllegalStateException(e);
        }
    }

    @Override
    public String getFormula() {
        try {
            return isEmpty() ? null : poiCell.getCellFormula();
        } catch (Exception e) {
            throw newIllegalStateException(e);
        }
    }

    @Override
    public Date getDate() {
        try {
            return isEmpty() ? null : poiCell.getDateCellValue();
        } catch (Exception e) {
            throw newIllegalStateException(e);
        }
    }

    @Override
    public Number getNumber() {
        try {
            return isEmpty() ? null : poiCell.getNumericCellValue();
        } catch (Exception e) {
            throw newIllegalStateException(e);
        }
    }

    @Override
    public RichText getText() {
        try {
            return isEmpty() ? RichText.emptyText() : RichText.valueOf(poiCell.getStringCellValue());
        } catch (Exception e) {
            throw newIllegalStateException(e);
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
    public Cell set(RichText s) {
        RichTextString richText = workbook.createRichTextString(s.toString());
        /*
        Iterator<Run> iter = s.iterator();
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
        */
        poiCell.setCellValue(richText);

        updateRow();

        return this;
    }

    @Override
    public RichText getAsText() {
        if (getCellType() == CellType.TEXT) {
            RichTextString richText = poiCell.getRichStringCellValue();

            String text = richText.getString();
            //TODO: properly process tabs
            text = text.replace('\t', ' '); // tab
            text = text.replace((char) 160, ' '); // non-breaking space

            RichTextBuilder rtb = new RichTextBuilder();
            int start = 0;
            for (int i = 0; i < richText.numFormattingRuns(); i++) {
                start = richText.getIndexOfFormattingRun(i);
                int end = i + 1 < richText.numFormattingRuns() ? richText.getIndexOfFormattingRun(i + 1) : richText.length();

                if (start == end) {
                    // skip empty
                    continue;
                }

                // apply font attributes for formatting run
                // FIXME getFontForFormattingRun(richText, i).addAttributes(as, start, end);
                rtb.append(text, start, end);
                start = end;
            }
            rtb.append(text, start, text.length());

            return rtb.toRichText();
        } else {
            if (isEmpty()) {
                return RichText.emptyText();
            }

            // FIXME locale specific grouping separator does not work in POI
            // see https://bz.apache.org/bugzilla/show_bug.cgi?id=59638
            // TODO create and submit patch for POI
            DataFormatter dataFormatter = getWorkbook().getDataFormatter();
            try {
                FormulaEvaluator evaluator = getWorkbook().evaluator;
                return RichText.valueOf(dataFormatter.formatCellValue(poiCell, evaluator));
            } catch (Exception ex) {
                return RichText.valueOf(Cell.ERROR_TEXT);
            }
        }

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
                LOGGER.warning("Cell is not date formatted!");
            }
        }
        updateRow();
        return this;
    }

    @Override
    public PoiCell set(Object arg) {
        MejaHelper.set(this, arg);
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
                LOGGER.warning("Cell is date formatted, but plain number written!");
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
    public void setStyle(String cellStyleName) {
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
        setStyle(other.getCellStyle().getName());

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
                set(other.getText());
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
    public String getCellRef() {
        return MejaHelper.getCellRef(this, false);
    }

    @Override
    public String getCellRef(boolean includeSheet) {
        return MejaHelper.getCellRef(this, includeSheet);
    }
}
