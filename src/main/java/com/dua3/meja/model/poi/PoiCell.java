/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.model.poi;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.meja.model.AbstractCell;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextAttributes;

/**
 *
 * @author axel
 */
public final class PoiCell
        extends AbstractCell {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoiCell.class);

    private static CellType translateCellType(org.apache.poi.ss.usermodel.CellType poiType) {
        switch (poiType) {
        case BLANK:
            return CellType.BLANK;
        case BOOLEAN:
            return CellType.BOOLEAN;
        case ERROR:
            return CellType.ERROR;
        case NUMERIC:
            return CellType.NUMERIC;
        case STRING:
            return CellType.TEXT;
        case FORMULA:
            return CellType.FORMULA;
        default:
            throw new IllegalArgumentException();
        }
    }

    final org.apache.poi.ss.usermodel.Cell poiCell;
    int spanX;
    int spanY;

    public PoiCell(PoiRow row, org.apache.poi.ss.usermodel.Cell cell) {
        super(row);
        this.poiCell = cell;
        this.spanX = 1;
        this.spanY = 1;

        RectangularRegion mergedRegion = row.getSheet().getMergedRegion(cell.getRowIndex(), cell.getColumnIndex());

        if (mergedRegion != null) {
            // cell is merged
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
    public void clear() {
        if (!isEmpty()) {
            Object old = get();
            poiCell.setCellType(org.apache.poi.ss.usermodel.CellType.BLANK);
            updateRow();
            valueChanged(old, null);
        }
    }

    @Override
    public void copy(Cell other) {
        PoiCellStyle cellStyle = getWorkbook().getCellStyle(other.getCellStyle().getName());
        setCellStyle(cellStyle);

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
        case FORMULA:
            setFormula(other.getFormula());
            break;
        case NUMERIC:
            set(other.getNumber());
            break;
        case DATE:
            setCellStyleDate(cellStyle);
            set(other.getDateTime());
            break;
        case TEXT:
            set(other.getText());
            break;
        default:
            throw new UnsupportedOperationException("Unsupported Cell Type: " + other.getCellType());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiCell) {
            return Objects.equals(poiCell, ((PoiCell) obj).poiCell);
        } else {
            return false;
        }
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
            return LocalDateTime.ofInstant(poiCell.getDateCellValue().toInstant(), ZoneId.systemDefault());
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
    public RichText getAsText(Locale locale) {
        if (getResultType() == CellType.TEXT) {
            return getText();
        } else {
            if (isEmpty()) {
                return RichText.emptyText();
            }

            return RichText.valueOf(getFormattedText(locale));
        }
    }

    @Override
    public boolean getBoolean() {
        LangUtil.check(getCellType() == CellType.BOOLEAN, "Cell does not contain a boolean value.");
        return poiCell.getBooleanCellValue();
    }

    @Override
    public PoiCellStyle getCellStyle() {
        return getWorkbook().getPoiCellStyle(poiCell.getCellStyle());
    }

    @Override
    public CellType getCellType() {
        CellType type = translateCellType(poiCell.getCellTypeEnum());
        // since formulas returning dates should return CellType.FORMULA
        // rather than CellType.DATE, only test for dates if cell is numeric.
        if (type == CellType.NUMERIC && isCellDateFormatted()) {
            type = CellType.DATE;
        }
        return type;
    }

    @Override
    public int getColumnNumber() {
        return poiCell.getColumnIndex();
    }

    @Override
    @Deprecated
    public Date getDate() {
        if (isEmpty()) { // POI will throw for wrong CellType but return null
                         // for empty cells
            throw new IllegalStateException("Cell does not contain a date.");
        }
        return poiCell.getDateCellValue();
    }

    @Override
    public LocalDateTime getDateTime() {
        if (isEmpty()) { // POI will throw for wrong CellType but return null
                         // for empty cells
            throw new IllegalStateException("Cell does not contain date/time.");
        }
        return LocalDateTime.ofInstant(poiCell.getDateCellValue().toInstant(), ZoneId.systemDefault());
    }

    private Font getFontForFormattingRun(RichTextString richText, int i) {
        if (richText instanceof HSSFRichTextString) {
            HSSFRichTextString hssfRichText = (HSSFRichTextString) richText;
            return ((PoiWorkbook.PoiHssfWorkbook) getWorkbook()).getFont(hssfRichText.getFontOfFormattingRun(i)).getFont();
        } else {
            return getWorkbook().getFont(((XSSFRichTextString) richText).getFontOfFormattingRun(i)).getFont();
        }
    }

    @Override
    public String getFormula() {
        LangUtil.check(getCellType() == CellType.FORMULA, "Cell does not contain a formula.");
        return poiCell.getCellFormula();
    }

    @Override
    public int getHorizontalSpan() {
        return spanX;
    }

    @Override
    public Number getNumber() {
        LangUtil.check(getCellType() == CellType.NUMERIC, "Cell does not contain a number.");
        return poiCell.getNumericCellValue();
    }

    @Override
    public CellType getResultType() {
        org.apache.poi.ss.usermodel.CellType poiType = poiCell.getCellTypeEnum();
        if (poiType == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            poiType = poiCell.getCachedFormulaResultTypeEnum();
        }
        CellType type = translateCellType(poiType);
        if (type == CellType.NUMERIC && isCellDateFormatted()) {
            type = CellType.DATE;
        }
        return type;
    }

    @Override
    public PoiRow getRow() {
        return (PoiRow) super.getRow();
    }

    @Override
    public int getRowNumber() {
        return getRow().getRowNumber();
    }

    @Override
    public PoiSheet getSheet() {
        return getRow().getSheet();
    }

    @Override
    public RichText getText() {
        LangUtil.check(getCellType() == CellType.TEXT, "Cell does not contain a text value.");
        return toRichText(poiCell.getRichStringCellValue());
    }

    @Override
    public int getVerticalSpan() {
        return spanY;
    }

    @Override
    public PoiWorkbook getWorkbook() {
        return getRow().getWorkbook();
    }

    @Override
    public int hashCode() {
        return poiCell.hashCode();
    }

    private boolean isCellDateFormatted() {
        /*
         * DateUtil.isCellDateFormatted() throws IllegalStateException when cell
         * is not numeric, so we have to work around this. TODO create SCCSE and
         * report bug against POI
         */
        org.apache.poi.ss.usermodel.CellType poiType = poiCell.getCellTypeEnum();
        if (poiType == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            poiType = poiCell.getCachedFormulaResultTypeEnum();
        }
        return poiType == org.apache.poi.ss.usermodel.CellType.NUMERIC
                && DateUtil.isCellDateFormatted(poiCell);
    }

    boolean isDateFormat(PoiCellStyle cellStyle) {
        org.apache.poi.ss.usermodel.CellStyle style = cellStyle.poiCellStyle;
        int i = style.getDataFormat();
        String f = style.getDataFormatString();
        return DateUtil.isADateFormat(i, f);
    }

    @Override
    public boolean isEmpty() {
        switch (poiCell.getCellTypeEnum()) {
        case BLANK:
            return true;
        case STRING:
            return poiCell.getStringCellValue().isEmpty();
        default:
            return false;
        }
    }

    @Override
    public PoiCell set(Boolean arg) {
        arg = getWorkbook().cache(arg);
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
        }
        updateRow();
        valueChanged(old, arg);
        return this;
    }

    @Override
    @Deprecated
    public PoiCell set(Date arg) {
        arg = getWorkbook().cache(arg);
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
            if (!isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is not date formatted
                LOGGER.warn("Cell is not date formatted!");
            }
        }
        updateRow();
        valueChanged(old, arg);
        return this;
    }

    @Override
    public PoiCell set(LocalDateTime arg) {
        arg = getWorkbook().cache(arg);
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            Date d = Date.from(arg.atZone(ZoneId.systemDefault()).toInstant());
            poiCell.setCellValue(d);
            if (!isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is not date formatted
                LOGGER.warn("Cell is not date formatted!");
            }
        }
        updateRow();
        valueChanged(old, arg);
        return this;
    }

    @Override
    public PoiCell set(Number arg) {
        arg = getWorkbook().cache(arg);
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg.doubleValue());
            if (isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is date formatted, but a plain number is stored
                LOGGER.warn("Cell is date formatted, but plain number written!");
            }
        }
        updateRow();
        valueChanged(old, null);
        return this;
    }

    @Override
    public Cell set(RichText s) {
        s = getWorkbook().cache(s);
        Object old = get();

        RichTextString richText = getWorkbook().createRichTextString(s.toString());
        for (Run run : s) {
            PoiFont font = getWorkbook().getPoiFont(getCellStyle().getFont(), run.getStyle());
            richText.applyFont(run.getStart(), run.getEnd(), font.getPoiFont());
        }
        poiCell.setCellValue(richText);

        updateRow();

        valueChanged(old, s);

        return this;
    }

    @Override
    public PoiCell set(String arg) {
        arg = getWorkbook().cache(arg);
        Object old = get();
        poiCell.setCellValue(arg);
        updateRow();
        valueChanged(old, null);
        return this;
    }

    @Override
    public void setCellStyle(CellStyle cellStyle) {
        LangUtil.check((cellStyle instanceof PoiCellStyle), "Incompatible implementation.");

        Object old = getCellStyle();
        poiCell.setCellStyle(((PoiCellStyle) cellStyle).poiCellStyle);
        styleChanged(old, cellStyle);
    }

    private void setCellStyleDate(PoiCellStyle cellStyle) {
        if (isDateFormat(cellStyle)) {
            // nothing to do
            return;
        }

        // try to get a version adapted to dates
        String dateStyleName = cellStyle.getName() + "#DATE#";
        if (!getWorkbook().hasCellStyle(dateStyleName)) {
            // if that doesn't exist, create a new format
            PoiCellStyle dateStyle = getWorkbook().getCellStyle(dateStyleName);
            dateStyle.copyStyle(cellStyle);
            String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM, null,
                    IsoChronology.INSTANCE, Locale.ROOT);
            dateStyle.setDataFormat(pattern);
        }
        setCellStyle(getWorkbook().getCellStyle(dateStyleName));
    }

    @SuppressWarnings("deprecation")
    @Override
    public PoiCell setFormula(String arg) {
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellFormula(arg);
            poiCell.setCellType(org.apache.poi.ss.usermodel.CellType.FORMULA);
            final PoiWorkbook wb = getWorkbook();
            if (wb.isFormulaEvaluationSupported()) {
                try {
                    wb.evaluator.evaluateFormulaCell(poiCell);
                } catch (NotImplementedException e) {
                    if (wb.getForceFormulaRecalculation()) {
                        LOGGER.info("An unsupported Excel function was used (workbook already flagged as needing recalculation).", e);
                    } else {
                        LOGGER.warn("An unsupported Excel function was used. Flagged workbook as needing recalculation.");
                        wb.setForceFormulaRecalculation(true);
                    }
                }
            }
        }
        updateRow();
        valueChanged(old, null);
        return this;
    }

    public RichText toRichText(RichTextString rts) {
        String text = rts.getString();
        // TODO: properly process tabs
        text = text.replace('\t', ' '); // tab
        text = text.replace((char) 160, ' '); // non-breaking space

        RichTextBuilder rtb = new RichTextBuilder();
        int start = 0;
        for (int i = 0; i < rts.numFormattingRuns(); i++) {
            start = rts.getIndexOfFormattingRun(i);
            int end = i + 1 < rts.numFormattingRuns() ? rts.getIndexOfFormattingRun(i + 1) : rts.length();

            if (start == end) {
                // skip empty
                continue;
            }

            Map<String, Object> properties = new HashMap<>();
            // apply font attributes for formatting run
            Font runFont = getFontForFormattingRun(rts, i);
            properties.put(TextAttributes.FONT_FAMILY, runFont.getFamily());
            properties.put(TextAttributes.FONT_SIZE, runFont.getSizeInPoints() + "pt");
            properties.put(TextAttributes.COLOR, runFont.getColor().toString());
            if (runFont.isBold()) {
                properties.put(TextAttributes.FONT_WEIGHT, "bold");
            }
            if (runFont.isItalic()) {
                properties.put(TextAttributes.FONT_STYLE, "italic");
            }
            if (runFont.isUnderlined()) {
                properties.put(TextAttributes.TEXT_DECORATION, "underline");
            }
            if (runFont.isStrikeThrough()) {
                properties.put(TextAttributes.TEXT_DECORATION, "line-through");
            }
            properties.put(TextAttributes.COLOR, runFont.getColor());

            Style attr = Style.create("style", properties);
            push(rtb, TextAttributes.STYLE_START_RUN, attr );
            rtb.append(text, start, end);
            push(rtb, TextAttributes.STYLE_END_RUN, attr );
            start = end;
        }
        rtb.append(text, start, text.length());

        return rtb.toRichText();
    }

    private void push(RichTextBuilder app, String key, Style attr) {
        @SuppressWarnings("unchecked")
        List<Style> current = (List<Style>) app.pop(key);
        if (current == null) {
            current = new LinkedList<>();
        }
        current.add(attr);
        app.push(key, current);
    }

    @Override
    public String toString(Locale locale) {
        if (getCellType() == CellType.TEXT) {
            return poiCell.getStringCellValue();
        } else {
            if (isEmpty()) {
                return "";
            }

            return getFormattedText(locale);
        }
    }

    /**
     * Format the cell content as a String, with number and date format applied.
     * @return cell content with format applied
     */
    private String getFormattedText(Locale locale) {
        DateTimeFormatter df = getCellStyle().getLocaleAwareDateFormat(locale);
        if (df != null) {
            return df.format(getDateTime());
        } else {
            // let POI do the formatting
            FormulaEvaluator evaluator = getWorkbook().evaluator;
            DataFormatter dataFormatter = getWorkbook().getDataFormatter(locale);
            try {
                return dataFormatter.formatCellValue(poiCell, evaluator);
            } catch (Exception ex) {
                return Cell.ERROR_TEXT;
            }
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

    @Override
    protected void setVerticalSpan(int spanY) {
        this.spanY=spanY;
    }

    @Override
    protected void setHorizontalSpan(int spanX) {
        this.spanX=spanX;
    }

}
