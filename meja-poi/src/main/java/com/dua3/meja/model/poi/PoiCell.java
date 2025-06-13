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
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Concrete subclass of {@link AbstractCell} for Apache POI based workbooks.
 */
public final class PoiCell extends AbstractCell<PoiSheet, PoiRow, PoiCell> {

    private static final Logger LOGGER = LogManager.getLogger(PoiCell.class);

    private static final char NON_BREAKING_SPACE = 160;
    private static final char TAB = '\t';
    private static final String CELLSTYLE_MARKER_DATETIME = "#DATETIME";

    /**
     * Converts an Apache POI cell type to the corresponding Meja cell type.
     *
     * @param poiType the Apache POI cell type to convert
     * @return the corresponding Meja CellType
     * @throws IllegalArgumentException if the POI cell type is unknown
     */
    private static CellType translateCellType(org.apache.poi.ss.usermodel.CellType poiType) {
        return switch (poiType) {
            case BLANK -> CellType.BLANK;
            case BOOLEAN -> CellType.BOOLEAN;
            case ERROR -> CellType.ERROR;
            case NUMERIC -> CellType.NUMERIC;
            case STRING -> CellType.TEXT;
            case FORMULA -> CellType.FORMULA;
            default ->
                    throw new IllegalArgumentException("unknown value for org.apache.poi.ss.usermodel.CellType: " + poiType);
        };
    }

    /**
     * Determines the actual Meja cell type, including special handling for date and datetime values.
     *
     * @param poiCell the Apache POI cell
     * @param poiType the basic POI cell type
     * @return the actual Meja CellType, with date detection
     */
    private static CellType getCellType(org.apache.poi.ss.usermodel.Cell poiCell, org.apache.poi.ss.usermodel.CellType poiType) {
        CellType type = translateCellType(poiType);
        // because Excel annoyingly store dates as doubles, we have to check for dates using some tricks
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

    /**
     * Checks if a cell contains a date value.
     * Works around POI's limitation where DateUtil.isCellDateFormatted() throws IllegalStateException for non-numeric cells.
     *
     * @param poiCell the Apache POI cell to check
     * @param poiType the basic POI cell type
     * @return true if the cell contains a date value
     */
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

    /**
     * Checks if a cell contains a date and time value.
     * First checks if the cell is date-formatted, then verifies if it has a time component.
     *
     * @param poiCell the Apache POI cell to check
     * @param poiType the basic POI cell type
     * @return true if the cell contains both date and time values
     */
    private static boolean isCellDateTime(org.apache.poi.ss.usermodel.Cell poiCell, org.apache.poi.ss.usermodel.CellType poiType) {
        // check if date formatted and time is exactly midnight
        if (!isCellDateFormatted(poiCell, poiType)) {
            return false;
        }

        // check time
        Instant instant = poiCell.getDateCellValue().toInstant();
        LocalTime time = instant.query(TemporalQueries.localTime());
        return time != null;
    }

    final org.apache.poi.ss.usermodel.Cell poiCell;
    int spanX;
    int spanY;

    /**
     * Initializes a new instance of the PoiCell class.
     *
     * @param row The PoiRow object to which the cell belongs.
     * @param cell The org.apache.poi.ss.usermodel.Cell object representing the cell.
     */
    public PoiCell(PoiRow row, org.apache.poi.ss.usermodel.Cell cell) {
        super(row);
        this.poiCell = cell;
        this.spanX = 1;
        this.spanY = 1;

        row.getSheet().getMergedRegion(cell.getRowIndex(), cell.getColumnIndex())
                .ifPresent(mergedRegion -> {
                    // cell is merged
                    boolean isTop = getRowNumber() == mergedRegion.firstRow();
                    boolean isTopLeft = isTop && getColumnNumber() == mergedRegion.firstColumn();
                    PoiCell topLeftCell;
                    if (isTopLeft) {
                        topLeftCell = this;
                    } else {
                        PoiRow topRow = isTop ? row : row.getSheet().getRow(mergedRegion.firstRow());
                        topLeftCell = topRow.getCell(mergedRegion.firstColumn());
                    }

                    int mergedSpanX = 1 + mergedRegion.lastColumn() - mergedRegion.firstColumn();
                    int mergedSpanY = 1 + mergedRegion.lastRow() - mergedRegion.firstRow();

                    addedToMergedRegion(topLeftCell, mergedSpanX, mergedSpanY);
                });
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            Object old = getOrDefault(null);
            poiCell.setBlank();
            updateRow();
            valueChanged(old, null);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof PoiCell other && Objects.equals(poiCell, other.poiCell);
    }

    @Override
    public @Nullable Object getOrDefault(@Nullable Object defaultValue) {
        return switch (getCellType()) {
            case BLANK -> defaultValue;
            case DATE -> poiCell.getLocalDateTimeCellValue().toLocalDate();
            case DATE_TIME -> poiCell.getLocalDateTimeCellValue();
            case NUMERIC -> poiCell.getNumericCellValue();
            case FORMULA -> poiCell.getCellFormula();
            case BOOLEAN -> poiCell.getBooleanCellValue();
            case TEXT -> getText();
            case ERROR -> ERROR_TEXT;
        };
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
        if (isEmpty()) { // POI will throw an exception for the wrong CellType but return null for empty cells
            throw new CellException(this, "the cell does not contain a date.");
        }
        return poiCell.getLocalDateTimeCellValue().toLocalDate();
    }

    @Override
    public LocalDateTime getDateTime() {
        if (isEmpty()) { // POI will throw an exception for the wrong CellType but return null for empty cells
            throw new CellException(this, "Cell does not contain datetime.");
        }
        return poiCell.getLocalDateTimeCellValue();
    }

    /**
     * Gets the font used for a specific formatting run in rich text.
     * Handles both HSSF (old .xls) and XSSF (.xlsx) formats.
     *
     * @param richText the POI rich text string
     * @param i the index of the formatting run
     * @return the Font instance used for the specified formatting run
     */
    private Font getFontForFormattingRun(RichTextString richText, int i) {
        if (richText instanceof HSSFRichTextString hssfRichText) {
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
        return switch (getCellType()) {
            case NUMERIC -> poiCell.getNumericCellValue();
            case FORMULA -> {
                LangUtil.check(getResultType() == CellType.NUMERIC, () -> new CellException(this, "formula does not yield a number"));
                yield poiCell.getNumericCellValue();
            }
            default -> throw new CellException(this, "the cell does not contain a number.");
        };
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
    public int getRowNumber() {
        return getRow().getRowNumber();
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

    /**
     * Checks if a cell style is formatted for date/time values.
     * This is determined either by the style name ending with a specific marker
     * or by checking the POI date format settings.
     *
     * @param cellStyle the cell style to check
     * @return true if the style is formatted for date/time values
     */
    static boolean isDateTimeFormat(PoiCellStyle cellStyle) {
        if (cellStyle.getName().endsWith(CELLSTYLE_MARKER_DATETIME)) {
            return true;
        }

        org.apache.poi.ss.usermodel.CellStyle style = cellStyle.poiCellStyle;
        int i = style.getDataFormat();
        String f = style.getDataFormatString();
        return DateUtil.isADateFormat(i, f);
    }

    @Override
    public boolean isEmpty() {
        return switch (poiCell.getCellType()) {
            case BLANK -> true;
            case STRING -> poiCell.getStringCellValue().isEmpty();
            default -> false;
        };
    }

    @Override
    public PoiCell set(@Nullable Boolean arg) {
        if (arg == null) {
            clear();
            return this;
        }

        Object old = getOrDefault(null);
        arg = getWorkbook().cache(arg);
        poiCell.setCellValue(arg);
        setCellStylePlain();
        updateRow();
        valueChanged(old, arg);
        return this;
    }

    @Override
    public PoiCell set(@Nullable LocalDate arg) {
        Object old = getOrDefault(null);
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
            setCellStyleDateTime();
        }
        updateRow();
        valueChanged(old, arg);
        return this;
    }

    @Override
    public PoiCell set(@Nullable LocalDateTime arg) {
        Object old = getOrDefault(null);
        if (arg == null) {
            clear();
        } else {
            poiCell.setCellValue(arg);
            setCellStyleDateTime();
        }
        updateRow();
        valueChanged(old, arg);
        return this;
    }

    @Override
    public PoiCell set(@Nullable Number arg) {
        if (arg == null) {
            clear();
            return this;
        }

        arg = getWorkbook().cache(arg);
        Object old = getOrDefault(null);
        poiCell.setCellValue(arg.doubleValue());
        setCellStylePlain();
        updateRow();
        valueChanged(old, null);
        return this;
    }

    @Override
    public Cell set(@Nullable RichText arg) {
        if (arg == null) {
            clear();
            return this;
        }

        arg = getWorkbook().cache(arg);
        Object old = getOrDefault(null);
        RichTextString richText = getWorkbook().createRichTextString(arg.toString());
        for (Run run : arg) {
            PoiFont font = getWorkbook().getPoiFont(FontUtil.getInstance().deriveFont(getCellStyle().getFont(), run.getFontDef()));
            richText.applyFont(run.getStart(), run.getEnd(), font.getPoiFont());
        }
        poiCell.setCellValue(richText);
        setCellStylePlain();
        updateRow();
        valueChanged(old, arg);

        return this;
    }

    @Override
    public PoiCell set(@Nullable String arg) {
        if (arg == null) {
            clear();
            return this;
        }

        arg = getWorkbook().cache(arg);
        Object old = getOrDefault(null);
        poiCell.setCellValue(arg);
        setCellStylePlain();
        updateRow();
        valueChanged(old, arg);
        return this;
    }

    @Override
    public PoiCell setCellStyle(CellStyle cellStyle) {
        if (!(cellStyle instanceof PoiCellStyle poiCellStyle)) {
            throw new IllegalStateException("Incompatible implementation: " + cellStyle.getClass());
        }
        Object old = getCellStyle();
        poiCell.setCellStyle(poiCellStyle.poiCellStyle);
        styleChanged(old, cellStyle);

        return this;
    }

    private void setCellStyleDateTime() {
        PoiCellStyle cellStyle = getCellStyle();

        if (isDateTimeFormat(cellStyle)) {
            // nothing to do
            return;
        }

        // try to get a version adapted to dates
        String dateStyleName = cellStyle.getName() + CELLSTYLE_MARKER_DATETIME;
        if (!getWorkbook().hasCellStyle(dateStyleName)) {
            // if that doesn't exist, create a new format
            LOGGER.debug("setting cell style to a date/time compatible format");
            PoiCellStyle dateStyle = getWorkbook().getCellStyle(dateStyleName);
            dateStyle.copyStyle(cellStyle);
            dateStyle.setDataFormat(CellStyle.StandardDataFormats.MEDIUM.name());
        }
        setCellStyle(getWorkbook().getCellStyle(dateStyleName));
    }

    private void setCellStylePlain() {
        PoiCellStyle style = getCellStyle();

        String styleName = style.getName();
        if (!styleName.endsWith(CELLSTYLE_MARKER_DATETIME)) {
            // nothing to do
            return;
        }

        styleName = styleName.substring(0, styleName.length() - CELLSTYLE_MARKER_DATETIME.length());

        // try to get a version adapted to dates
        if (!getWorkbook().hasCellStyle(styleName)) {
            // if that doesn't exist, create a new format
            LOGGER.debug("setting cell style to a standard format");
            CellStyle newStyle = getWorkbook().getCellStyle(styleName);
            newStyle.copyStyle(style);
            newStyle.setDataFormat("GENERAL");
        }
        setCellStyle(getWorkbook().getCellStyle(styleName));
    }

    @Override
    public PoiCell setFormula(@Nullable String arg) {
        Object old = getOrDefault(null);
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
                        LOGGER.debug("an unsupported Excel function was used (workbook already flagged as needing recalculation)", e);
                    } else {
                        LOGGER.warn("an unsupported Excel function was used; flagged workbook as needing recalculation", e);
                        wb.setForceFormulaRecalculation(true);
                    }
                }
            }
        }
        updateRow();
        valueChanged(old, null);
        return this;
    }

    @Override
    public PoiCell setHyperlink(URI target) {
        Hyperlink link = getWorkbook().createHyperLink(target);
        poiCell.setHyperlink(link);
        return this;
    }

    @Override
    public PoiCell clearHyperlink() {
        poiCell.removeHyperlink();
        return this;
    }

    @Override
    public Optional<URI> getHyperlink() {
        Hyperlink link = poiCell.getHyperlink();

        if (link == null) {
            return Optional.empty();
        }
        try {
            return switch (link.getType()) {
                case URL, EMAIL -> Optional.of(new URI(link.getAddress()));
                case FILE -> Optional.of(URI.create(link.getAddress()));
                case NONE -> Optional.empty();
                case DOCUMENT -> throw new UnsupportedOperationException("Unsupported link type: " + link.getType());
            };
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Cell setError() {
        poiCell.setCellErrorValue(FormulaError.NA.getCode());
        return this;
    }

    /**
     * Converts a RichTextString to a RichText object.
     *
     * @param rts The RichTextString to convert.
     * @return The converted RichText object.
     */
    public RichText toRichText(RichTextString rts) {
        String text = rts.getString();
        // TODO: properly process tabs
        text = text.replace(TAB, ' '); // tab
        text = text.replace(NON_BREAKING_SPACE, ' '); // non-breaking space

        RichTextBuilder rtb = new RichTextBuilder();
        for (int i = 0; i < rts.numFormattingRuns(); i++) {
            int start = rts.getIndexOfFormattingRun(i);
            int end = i + 1 < rts.numFormattingRuns() ? rts.getIndexOfFormattingRun(i + 1) : rts.length();

            if (start == end) {
                // skip empty
                continue;
            }

            Map<String, Object> properties = new HashMap<>();
            // apply font attributes for formatting run
            Font runFont = getFontForFormattingRun(rts, i);
            properties.put(Style.FONT, runFont);

            Style style = Style.create("style", properties);
            rtb.push(style);
            rtb.append(text, start, end);
            rtb.pop(style);
        }
        // append the remainder
        rtb.append(text, rtb.length(), text.length());

        return rtb.toRichText();
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
     * Formats the cell content as a String, applying appropriate number and date formats.
     * This method handles different cell types differently:
     * - For dates, uses the cell style's locale-aware date format
     * - For other types, uses POI's DataFormatter for consistent formatting
     *
     * @param locale the locale to use for formatting
     * @return the formatted cell content as a string
     */
    private String getFormattedText(Locale locale) {
        // is there a special date format?
        switch (getResultType()) {
            case DATE -> {
                DateTimeFormatter df = getCellStyle().getLocaleAwareDateFormat(locale);
                if (df != null) {
                    return df.format(getDate());
                }
            }
            case DATE_TIME -> {
                DateTimeFormatter df = getCellStyle().getLocaleAwareDateTimeFormat(locale);
                if (df != null) {
                    return df.format(getDateTime());
                }
            }
            default -> { /* do nothing */ }
        }

        // if not, let POI do the formatting
        FormulaEvaluator evaluator = getWorkbook().evaluator;
        DataFormatter dataFormatter = PoiWorkbook.getDataFormatter(locale);
        try {
            return dataFormatter.formatCellValue(poiCell, evaluator);
        } catch (@SuppressWarnings("unused") IllegalArgumentException ex) {
            return Cell.ERROR_TEXT;
        }
    }

    /**
     * Update sheet data i.e., first and last cell numbers.
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
