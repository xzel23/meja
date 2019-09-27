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

import com.dua3.meja.model.AbstractCell;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.*;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.time.*;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalQueries;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author axel
 */
public final class PoiCell extends AbstractCell {

    private static final Logger LOGGER = Logger.getLogger(PoiCell.class.getName());

    private static final char NON_BREAKING_SPACE = (char) 160;
    private static final char TAB = '\t';

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
            throw new IllegalArgumentException("unknown value for org.apache.poi.ss.usermodel.CellType: "+poiType);
        }
    }

    private static CellType getCellType(org.apache.poi.ss.usermodel.Cell poiCell, org.apache.poi.ss.usermodel.CellType poiType) {
        CellType type = translateCellType(poiType);
        // because excel annoyingly store dates as doubles, we have to check for dates using some tricks
        if (type == CellType.NUMERIC) {
            // since formulas returning dates should return CellType.FORMULA
            // rather than CellType.DATE, only test for dates if cell is numeric.
            if (isCellDateTime(poiCell, poiType)) {
                type = CellType.DATE_TIME;
            }
            if (isCellDateFormatted(poiCell, poiType)) {
                type = CellType.DATE;
            }
        }
        return type;
    }

    private static boolean isCellDateFormatted(org.apache.poi.ss.usermodel.Cell poiCell, org.apache.poi.ss.usermodel.CellType poiType) {
        /*
         * DateUtil.isCellDateFormatted() throws IllegalStateException when cell is not
         * numeric, so we have to work around this. TODO create SCCSE and report bug
         * against POI
         */
        if (poiType == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            poiType = poiCell.getCachedFormulaResultType();
        }
        return poiType == org.apache.poi.ss.usermodel.CellType.NUMERIC && DateUtil.isCellDateFormatted(poiCell);
    }

    private static boolean isCellDateTime(org.apache.poi.ss.usermodel.Cell poiCell, org.apache.poi.ss.usermodel.CellType poiType) {
        // check if date formatted and time is exactly midnight
        if (!isCellDateFormatted(poiCell, poiType)) {
            return false;
        }

        // check time
        Instant instant = poiCell.getDateCellValue().toInstant();
        LocalTime time = instant.query(TemporalQueries.localTime());
        return time !=null;
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
            poiCell.setBlank();
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
            set(other.getDate());
            break;
        case DATE_TIME:
            setCellStyleDateTime(cellStyle);
            set(other.getDateTime());
            break;
        case TEXT:
            set(other.getText());
            break;
        default:
            throw new CellException(other, "Unsupported Cell Type: " + other.getCellType());
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PoiCell && Objects.equals(poiCell, ((PoiCell) obj).poiCell);
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
            return LocalDate.ofInstant(poiCell.getDateCellValue().toInstant(), ZoneId.systemDefault());
        case DATE_TIME:
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
            throw new CellException(this, "unsupported cell type: "+ poiCell.getCellType());
        }
    }

    @Override
    public RichText getAsText(Locale locale) {
        if (getCellType() == CellType.TEXT) {
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
        LangUtil.check(getCellType() == CellType.BOOLEAN, () -> new CellException(this, "Cell does not contain a boolean value."));
        return poiCell.getBooleanCellValue();
    }

    @Override
    public PoiCellStyle getCellStyle() {
        return getWorkbook().getPoiCellStyle(poiCell.getCellStyle());
    }

    @Override
    public CellType getCellType() {
        return getCellType(poiCell, poiCell.getCellType());
    }

    @Override
    public int getColumnNumber() {
        return poiCell.getColumnIndex();
    }

    @Override
    public LocalDate getDate() {
        if (isEmpty()) { // POI will throw for wrong CellType but return null
                         // for empty cells
            throw new CellException(this, "Cell does not contain a date.");
        }
        return poiCell.getDateCellValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @Override
    public LocalDateTime getDateTime() {
        if (isEmpty()) { // POI will throw for wrong CellType but return null
                         // for empty cells
            throw new CellException(this, "Cell does not contain date/time.");
        }
        return LocalDateTime.ofInstant(poiCell.getDateCellValue().toInstant(), ZoneId.systemDefault());
    }

    private Font getFontForFormattingRun(RichTextString richText, int i) {
        if (richText instanceof HSSFRichTextString) {
            HSSFRichTextString hssfRichText = (HSSFRichTextString) richText;
            return ((PoiHssfWorkbook) getWorkbook()).getFont(hssfRichText.getFontOfFormattingRun(i))
                    .getFont();
        } else {
            return getWorkbook().getFont(((XSSFRichTextString) richText).getFontOfFormattingRun(i)).getFont();
        }
    }

    @Override
    public String getFormula() {
        LangUtil.check(getCellType() == CellType.FORMULA, () -> new CellException(this, "Cell does not contain a formula."));
        return poiCell.getCellFormula();
    }

    @Override
    public int getHorizontalSpan() {
        return spanX;
    }

    @Override
    public Number getNumber() {
        switch (getCellType()) {
            case NUMERIC:
                return poiCell.getNumericCellValue();
            case FORMULA:
                LangUtil.check(getResultType()==CellType.NUMERIC, () -> new CellException(this, "formula does not yield a number"));
                return poiCell.getNumericCellValue();
            default:
                throw new CellException(this, "cell does not contain a number.");
        }
    }

    @Override
    public CellType getResultType() {
        org.apache.poi.ss.usermodel.CellType poiType = poiCell.getCellType();
        if (poiType == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            poiType = poiCell.getCachedFormulaResultType();
        }
        return getCellType(poiCell, poiType);
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
        LangUtil.check(getCellType() == CellType.TEXT, () -> new CellException(this, "Cell does not contain a text value."));
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
         * DateUtil.isCellDateFormatted() throws IllegalStateException when cell is not
         * numeric, so we have to work around this. TODO create SCCSE and report bug
         * against POI
         */
        org.apache.poi.ss.usermodel.CellType poiType = poiCell.getCellType();
        if (poiType == org.apache.poi.ss.usermodel.CellType.FORMULA) {
            poiType = poiCell.getCachedFormulaResultType();
        }
        return poiType == org.apache.poi.ss.usermodel.CellType.NUMERIC && DateUtil.isCellDateFormatted(poiCell);
    }

    boolean isDateFormat(PoiCellStyle cellStyle) {
        org.apache.poi.ss.usermodel.CellStyle style = cellStyle.poiCellStyle;
        int i = style.getDataFormat();
        String f = style.getDataFormatString();
        return DateUtil.isADateFormat(i, f);
    }

    boolean isDateTimeFormat(PoiCellStyle cellStyle) {
        org.apache.poi.ss.usermodel.CellStyle style = cellStyle.poiCellStyle;
        int i = style.getDataFormat();
        String f = style.getDataFormatString();
        return DateUtil.isADateFormat(i, f);
    }

    @Override
    public boolean isEmpty() {
        switch (poiCell.getCellType()) {
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
    public PoiCell set(LocalDate arg) {
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            Date d = Date.from(arg.atStartOfDay(ZoneId.systemDefault()).toInstant());
            poiCell.setCellValue(d);
            if (!isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is not date formatted
                LOGGER.log(Level.WARNING, "Cell is not date formatted!");
            }
        }
        updateRow();
        valueChanged(old, arg);
        return this;
    }

    @Override
    public PoiCell set(LocalDateTime arg) {
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            Date d = Date.from(arg.atZone(ZoneId.systemDefault()).toInstant());
            poiCell.setCellValue(d);
            if (!isCellDateFormatted()) {
                // Excel does not have a cell type for dates!
                // Warn if cell is not date formatted
                LOGGER.log(Level.WARNING, "Cell is not date formatted!");
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
                LOGGER.log(Level.WARNING, "Cell is date formatted, but plain number written!");
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
            PoiFont font = getWorkbook().getPoiFont(getCellStyle().getFont(), run.getAttributes());
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

    private void setCellStyleDateTime(PoiCellStyle cellStyle) {
        if (isDateTimeFormat(cellStyle)) {
            // nothing to do
            return;
        }

        // try to get a version adapted to dates
        String dateTimeStyleName = cellStyle.getName() + "#DATETIME#";
        if (!getWorkbook().hasCellStyle(dateTimeStyleName)) {
            // if that doesn't exist, create a new format
            PoiCellStyle dateStyle = getWorkbook().getCellStyle(dateTimeStyleName);
            dateStyle.copyStyle(cellStyle);
            String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM, FormatStyle.SHORT,
                    IsoChronology.INSTANCE, Locale.ROOT);
            dateStyle.setDataFormat(pattern);
        }
        setCellStyle(getWorkbook().getCellStyle(dateTimeStyleName));
    }

    @Override
    public PoiCell setFormula(String arg) {
        Object old = get();
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellFormula(arg);
            final PoiWorkbook wb = getWorkbook();
            if (wb.isFormulaEvaluationSupported()) {
                try {
                    wb.evaluator.evaluateFormulaCell(poiCell);
                } catch (NotImplementedException e) {
                    if (wb.getForceFormulaRecalculation()) {
                        LOGGER.log(Level.INFO,
                                "An unsupported Excel function was used (workbook already flagged as needing recalculation).",
                                e);
                    } else {
                        LOGGER.log(Level.WARNING,
                                "An unsupported Excel function was used. Flagged workbook as needing recalculation.");
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
        text = text.replace(TAB, ' '); // tab
        text = text.replace(NON_BREAKING_SPACE, ' '); // non-breaking space

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
            push(rtb, TextAttributes.STYLE_START_RUN, attr);
            rtb.append(text, start, end);
            push(rtb, TextAttributes.STYLE_END_RUN, attr);
            start = end;
        }
        rtb.append(text, start, text.length());

        return rtb.toRichText();
    }

    private static void push(RichTextBuilder app, String key, Style attr) {
        @SuppressWarnings("unchecked")
        Collection<Style> current = (Collection<Style>) app.get(key);
        if (current == null) {
            current = new LinkedList<>();
            app.put(key, current);
        }
        current.add(attr);
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
     *
     * @return cell content with format applied
     */
    private String getFormattedText(Locale locale) {
        // is there a special date format?
        if (getResultType() == CellType.DATE) {
            DateTimeFormatter df = getCellStyle().getLocaleAwareDateFormat(locale);
            if (df != null) {
                return df.format(getDate());
            }
        }
        if (getResultType() == CellType.DATE_TIME) {
            DateTimeFormatter df = getCellStyle().getLocaleAwareDateTimeFormat(locale);
            if (df != null) {
                return df.format(getDateTime());
            }
        }

        // if not, let POI do the formatting
        FormulaEvaluator evaluator = getWorkbook().evaluator;
        DataFormatter dataFormatter = getWorkbook().getDataFormatter(locale);
        try {
            return dataFormatter.formatCellValue(poiCell, evaluator);
        } catch (@SuppressWarnings("unused") IllegalArgumentException ex) {
            return Cell.ERROR_TEXT;
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
        this.spanY = spanY;
    }

    @Override
    protected void setHorizontalSpan(int spanX) {
        this.spanX = spanX;
    }

}
