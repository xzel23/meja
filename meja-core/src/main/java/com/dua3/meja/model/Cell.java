
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
package com.dua3.meja.model;

import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextUtil;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * A single cell of a sheet.
 * <p>
 * <strong>Units:</strong> Any size-related values derived from this cell (e.g., values returned by
 * {@link #calcCellDimension()}) are measured in typographical points (1/72 inch), independent of device pixels.
 * </p>
 *
 * @author axel
 */
public interface Cell {

    /**
     * The CellException class is an exception that extends the IllegalStateException class. It is used to indicate
     * exceptional conditions related to a cell in a spreadsheet.
     */
    class CellException extends IllegalStateException {
        /**
         * Constructs a new CellException with the given Cell, message, and cause.
         *
         * @param cell   The Cell associated with the exception.
         * @param message   The detail message.
         * @param cause   The cause of the exception.
         */
        public CellException(Cell cell, String message, @Nullable Throwable cause) {
            super(messagePrefix(cell) + message, cause);
        }

        /**
         * Constructs a new CellException with the given Cell and message.
         *
         * @param cell   The Cell associated with the exception.
         * @param message   The detail message.
         */
        public CellException(Cell cell, String message) {
            super(messagePrefix(cell) + message);
        }

        /**
         * Returns the message prefix for the given Cell. The message prefix is the cell reference in brackets.
         *
         * @param cell   The Cell for which the message prefix is required.
         * @return The message prefix.
         */
        private static String messagePrefix(Cell cell) {
            return "[" + cell.getCellRef(RefOption.WITH_SHEET) + "] ";
        }
    }

    /**
     * The text to show in error cells.
     */
    String ERROR_TEXT = "#ERROR";

    /**
     * Clear the cell`s content.
     */
    void clear();

    /**
     * Copy cell data.
     *
     * @param other cell to copy data from
     */
    default void copy(Cell other) {
        setCellStyle(other.getCellStyle().getName());
        switch (other.getCellType()) {
            case BLANK -> clear();
            case BOOLEAN -> set(other.getBoolean());
            case ERROR -> setError();
            case FORMULA -> set(other.getFormula());
            case NUMERIC -> set(other.getNumber());
            case DATE -> set(other.getDate());
            case DATE_TIME -> set(other.getDateTime());
            case TEXT -> set(other.getText());
            default -> throw new CellException(other, "unsupported cell type: " + other.getCellType());
        }
        other.getHyperlink().ifPresent(this::setHyperlink);
    }

    /**
     * Return raw cell value.
     *
     * @return cell value
     */
    default Optional<Object> get() {
        return Optional.ofNullable(getOrDefault(null));
    }

    /**
     * Return raw cell value.
     *
     * @param defaultValue the default value to return for empty cells
     * @return cell value if cell is not empty, {@code defaultValue otherwise}
     */
    @Nullable
    Object getOrDefault(@Nullable Object defaultValue);

    /**
     * Return the textual representation of the cell's value as RichText, without applying the cell style.
     * <p>
     * This method converts the underlying value to text using the given locale, but does not apply the
     * cell's visual style (font, fill colors, etc.). Use {@link #getAsFormattedText(Locale)} when you
     * need the value as it would appear to the user, including cell styling.
     * </p>
     *
     * <p>Examples (assuming a cell with numeric value 1234.5 and a German locale):
     * <ul>
     *   <li>{@code cell.getAsText(Locale.GERMANY)} &rarr; RichText("1234.5") (no thousand separator, no style)</li>
     *   <li>{@code cell.getAsFormattedText(Locale.GERMANY)} &rarr; RichText("1.234,5") (formatted, styled)</li>
     *   <li>{@code cell.toString(Locale.GERMANY)} &rarr; "1.234,5" (plain String, formatted)</li>
     * </ul>
     *
     * @param locale the locale to use during formatting
     * @return the cell value as a RichText instance without applying cell style
     * @see #getAsFormattedText(Locale)
     * @see #toString(Locale)
     */
    RichText getAsText(Locale locale);

    /**
     * Return the textual representation of the cell's value as it would be displayed, with the cell style applied.
     * <p>
     * This method applies the cell's visual style (font, fills) to the textual value generated for the given locale.
     * It is suitable for UI display when you want to preserve styling information in the returned {@link RichText}.
     * </p>
     *
     * <p>Examples (assuming a date value 2025-01-02 and the cell style uses a short date pattern):
     * <ul>
     *   <li>{@code cell.getAsText(Locale.US)} &rarr; RichText("2025-01-02") (raw text, no style)</li>
     *   <li>{@code cell.getAsFormattedText(Locale.US)} &rarr; RichText("1/2/25") (formatted and styled)</li>
     *   <li>{@code cell.toString(Locale.US)} &rarr; "1/2/25" (plain String, formatted)</li>
     * </ul>
     *
     * @param locale the locale to use during formatting
     * @return the value as RichText, including cell style
     * @see #getAsText(Locale)
     * @see #toString(Locale)
     */
    default RichText getAsFormattedText(Locale locale) {
        CellStyle cs = getCellStyle();
        return getAsText(locale).wrap(Style.create(cs.getFont(), cs.getFillFgColor(), cs.getFillBgColor()));
    }

    /**
     * Return boolean cell value.
     *
     * @return boolean cell value
     * @throws IllegalStateException if cell is not of boolean type
     */
    boolean getBoolean();

    /**
     * Get cell reference (i.e. "Sheet!A1" for the top left cell).
     *
     * @param options options to be used
     * @return cell reference as String
     */
    default String getCellRef(RefOption... options) {
        String prefixRow = "";
        String prefixColumn = "";
        String sheet = "";

        for (RefOption o : options) {
            switch (o) {
                case FIX_COLUMN -> prefixColumn = "$";
                case FIX_ROW -> prefixRow = "$";
                case WITH_SHEET -> sheet = "'" + getSheet().getSheetName() + "'!";
            }
        }

        return sheet + prefixColumn + Sheet.getColumnName(getColumnNumber()) + prefixRow + (getRowNumber() + 1);
    }

    /**
     * Return the cell style.
     *
     * @return cell style
     */
    CellStyle getCellStyle();

    /**
     * Return the cell type.
     *
     * @return cell type
     */
    CellType getCellType();

    /**
     * Get the cell's column number.
     *
     * @return column number of this cell
     */
    int getColumnNumber();

    /**
     * Return date value.
     *
     * @return date cell value
     * @throws IllegalStateException if cell is not of date value
     */
    LocalDate getDate();

    /**
     * Return datetime value.
     *
     * @return datetime cell value
     * @throws IllegalStateException if cell is not of datetime value
     */
    LocalDateTime getDateTime();

    /**
     * Return formula.
     *
     * @return the cell`s formula
     * @throws IllegalStateException if no formula is set
     */
    String getFormula();

    /**
     * Get the horizontal span.
     * <p>
     * The horizontal span of a merged cells is the horizontal number of merged
     * cells for the top left cell of the merged cells and 0 for the other merged
     * cells. For cells that are not merged, the span is 1.
     *
     * @return horizontal span for this cell
     */
    int getHorizontalSpan();

    /**
     * Get the logical cell. The logical cell for merged cells is the top left cell
     * of the group of merged cells. For cells that are not merged, the logical cell
     * is the cell itself.
     *
     * @return the logical cell
     */
    Cell getLogicalCell();

    /**
     * Return numeric value.
     *
     * @return numeric cell value
     * @throws IllegalStateException if cell is not of numeric type
     */
    Number getNumber();

    /**
     * Return the result type.
     * <p>
     * For non-formula cells, this is the same as the value returned by
     * {@link #getCellType()}. For formula cells, the result type of the last
     * evaluation is returned.
     *
     * <p>
     * <em>Note: Since excel doesn't know about dates internally and date cells are
     * determined by looking at both cell type and format, the cell style has to be
     * set before calling this method to make sure {@code CellType.DATE} is returned
     * for formulas that return a date.</em>
     * </p>
     *
     * @return cell type
     */
    CellType getResultType();

    /**
     * Get the row this cell belongs to.
     *
     * @return the row for this cell
     */
    Row getRow();

    /**
     * Get the cell's row number.
     *
     * @return row number of this cell
     */
    int getRowNumber();

    /**
     * Get the cell`s sheet.
     *
     * @return sheet this cell belongs to
     */
    Sheet getSheet();

    /**
     * Return string value.
     *
     * @return text cell value
     * @throws IllegalStateException if cell is not of text type
     */
    RichText getText();

    /**
     * Get the vertical span.
     * <p>
     * The vertical span of a merged cells is the vertical number of merged cells
     * for the top left cell of the merged cells and 0 for the other merged cells.
     * For cells that are not merged, the span is 1.
     *
     * @return vertical span for this cell
     */
    int getVerticalSpan();

    /**
     * Get the workbook this cell belongs to.
     *
     * @return the workbook
     */
    Workbook getWorkbook();

    /**
     * Test for empty cell.
     *
     * @return true if cell has cell type BLANK or contains the empty string.
     */
    boolean isEmpty();

    /**
     * Set cell value to boolean value.
     *
     * @param b boolean value
     * @return this cell
     */
    Cell set(@Nullable Boolean b);

    /**
     * Set cell value to date.
     *
     * @param arg date
     * @return this cell
     */
    Cell set(@Nullable LocalDate arg);

    /**
     * Set cell value to date.
     *
     * @param arg date
     * @return this cell
     */
    Cell set(@Nullable LocalDateTime arg);

    /**
     * Set cell value to number.
     *
     * @param arg number
     * @return this cell
     */
    Cell set(@Nullable Number arg);

    /**
     * Set cell value.
     * <p>
     * Use this method when the exact type of the value object is not known at
     * compile time. Depending on the runtime-type, the corresponding overload of
     * {@code Cell.set(...)} is called. if {@code value} is {@code null}, the cell
     * is cleared. If no overload of {@code Cell.set(...)} matches the runtime type
     * of {@code value}, the cell value is set to {@code String.valueOf(value)}.
     * </p>
     * The following types are supported:
     * <ul>
     * <li>{@link Number}
     * <li>{@link Boolean}
     * <li>{@link LocalDateTime}
     * <li>{@link LocalDate}
     * <li>{@link String}
     * <li>{@link RichText}
     * </ul>
     *
     * @param arg the value
     * @return this cell
     */
    default Cell set(@Nullable Object arg) {
        if (arg == null) {
            clear();
            return this;
        }

        arg = getWorkbook().cache(arg);
        switch (arg) {
            case Number n -> set(n);
            case Boolean b -> set(b);
            case LocalDateTime ldt -> set(ldt);
            case LocalDate ld -> set(ld);
            case RichText rt -> set(rt);
            default -> set(String.valueOf(arg));
        }
        return this;
    }

    /**
     * Set cell value to string with markup.
     *
     * @param s rich text string
     * @return this cell
     */
    Cell set(@Nullable RichText s);

    /**
     * Set cell value to string.
     *
     * @param s string
     * @return this cell
     */
    Cell set(@Nullable String s);

    /**
     * Set cell style.
     *
     * @param cellStyle cell style
     * @return this cell
     */
    Cell setCellStyle(CellStyle cellStyle);

    /**
     * Sets the cell style registered under name in the workbook.
     *
     * @param cellStyleName cell style name
     * @return this cell
     */
    default Cell setCellStyle(String cellStyleName) {
        return setCellStyle(getWorkbook().getCellStyle(cellStyleName));
    }

    /**
     * Set formula
     *
     * @param value the formula as a string
     * @return this cell
     */
    Cell setFormula(@Nullable String value);

    /**
     * Set Hyperlink.
     *
     * @param target the link target
     * @return this cell
     */
    Cell setHyperlink(URI target);

    /**
     * Set Hyperlink.
     *
     * @param target the link target
     * @return this cell
     */
    default Cell setHyperlink(Path target) {
        return setHyperlink(IoUtil.toURI(target));
    }

    /**
     * Remove Hyperlink.
     *
     * @return this cell
     */
    Cell clearHyperlink();

    /**
     * Get Hyperlink.
     *
     * @return an Optional with the Hyperlink or empty Optional
     */
    Optional<URI> getHyperlink();

    /**
     * Get resolved Hyperlink.
     * <p>
     * <strong>Note:</strong> Absolute URIs are returned unchanged; relative URIs are resolved against the workbook URI.
     *
     * @return an Optional with the resolved Hyperlink or an empty Optional
     * @throws IllegalStateException if a relative link exists but the workbook URI is not set
     */
    default Optional<URI> getResolvedHyperlink() {
        return getHyperlink().map(link -> {
            if (link.getScheme() != null) {
                // relative URIs have scheme set to null, absolute URIs require a valid scheme
                return link;
            }

            return getWorkbook()
                    .getUri()
                    .orElseThrow(() -> new IllegalStateException("cannot resolve the relative link because the workbook URI is not set"))
                    .resolve(link);
        });
    }

    /**
     * Set error.
     *
     * @return this cell
     */
    Cell setError();

    /**
     * Check for error.
     * @return true, if the cell type is ERROR
     */
    default boolean isError() {
        return getCellType() == CellType.ERROR;
    }

    /**
     * Calculate the dimension of the cell based on the cell's content.
     * <p>
     * The returned {@link com.dua3.utility.math.geometry.Dimension2f} is measured in typographical points (1/72 inch)
     * and reflects the content as it would be displayed using the cell's font and rotation. A small padding is applied
     * to width and height to account for spacing.
     * </p>
     *
     * @return the calculated dimension of the cell in typographical points (1/72 inch)
     */
    default Dimension2f calcCellDimension() {
        // calculate the exact width
        String text = toString();
        CellStyle cellStyle = getCellStyle();
        Font font = cellStyle.getFont();
        Rectangle2f dim = TextUtil.getTextDimension(text, font);

        // add some space on the sides
        float w = dim.width() + font.getSizeInPoints();
        float h = (float) (dim.height() + font.getSpaceWidth());

        // take rotation into account
        short rotation = cellStyle.getRotation();
        if (rotation == 0) {
            return new Dimension2f(w, h);
        }

        double alpha = rotation / 180.0 * Math.PI;
        double cosAbs = Math.abs(Math.cos(alpha));
        double sinAbs = Math.abs(Math.sin(alpha));
        return new Dimension2f(
                (float) (cosAbs * w + sinAbs * h),
                (float) (sinAbs * w + cosAbs * h)
        );
    }

    /**
     * Return string representation of cell content.
     *
     * @return string representation of cell content
     */
    @Override
    String toString();

    /**
     * Return the localized string representation of the cell's value.
     * <p>
     * This method returns a plain {@link String} suitable for display or export (e.g., CSV). It formats
     * the value using the provided locale and the cell's data format, but does not carry styling information.
     * For a styled representation, use {@link #getAsFormattedText(Locale)}. For a non-styled RichText, use
     * {@link #getAsText(Locale)}.
     * </p>
     *
     * <p>Examples:
     * <ul>
     *   <li>Number 1234.5 with German locale: {@code cell.toString(Locale.GERMANY)} &rarr; "1.234,5"</li>
     *   <li>Date 2025-01-02 with US locale (short date style): {@code cell.toString(Locale.US)} &rarr; "1/2/25"</li>
     * </ul>
     *
     * @param locale locale for formatting
     * @return string representation of the cell content for the given locale
     * @see #getAsText(Locale)
     * @see #getAsFormattedText(Locale)
     */
    String toString(Locale locale);

    /**
     * Unmerge cell.
     *
     * @throws IllegalStateException if this cell is not the top left cell of a
     *                               merged region
     */
    void unMerge();

    /**
     * Test if the cell is merged.
     *
     * @return true if cell is merged
     */
    default boolean isMerged() {
        return getHorizontalSpan() != 1 || getVerticalSpan() != 1;
    }

    /**
     * Merge cell with neighboring cells.
     *
     * @param spanX the horizontal span
     * @param spanY the vertical span
     * @return this cell after merging
     */
    default Cell merge(int spanX, int spanY) {
        LangUtil.check(!isMerged(), "Cell is already merged.");
        LangUtil.check(spanX >= 1, () -> new IllegalArgumentException("spanX must be positive: " + spanX));
        LangUtil.check(spanY >= 1, () -> new IllegalArgumentException("spanY must be positive: " + spanY));

        if (spanX == 1 && spanY == 1) {
            // ignore
            return this;
        }

        int iMin = getRowNumber();
        int iMax = iMin + spanY - 1;
        int jMin = getColumnNumber();
        int jMax = jMin + spanX - 1;

        RectangularRegion region = new RectangularRegion(iMin, iMax, jMin, jMax);
        getSheet().addMergedRegion(region);

        return this;
    }

    /**
     * Returns the effective border style for the specified direction.
     * The effective border style is determined by the cell's border style and the border style of its neighboring cell.
     *
     * @param direction the direction of the border
     * @return the effective border style for the specified direction
     */
    default BorderStyle getEffectiveBorderStyle(Direction direction) {
        BorderStyle style = getLogicalCell().getCellStyle().getBorderStyle(direction);
        // When drawing the right or lower edge of a cell, the border style of the current cell is used
        if (!style.isNone() && direction == Direction.EAST || direction == Direction.SOUTH) {
            return style;
        }

        // else it is determined by the neighboring cell's style
        return getNeighboringCell(direction)
                .map(Cell::getCellStyle)
                .map(cs -> cs.getBorderStyle(direction.inverse()))
                .filter(bs -> !bs.isNone())
                .orElse(style);
    }

    /**
     * Returns an optional neighboring cell based on the given direction.
     *
     * @param direction the direction in which to find the neighboring cell
     * @return an optional containing the neighboring cell if it exists, otherwise an empty optional
     */
    private Optional<? extends Cell> getNeighboringCell(Direction direction) {
        return switch (direction) {
            case EAST -> getRow().getCellIfExists(getColumnNumber() + 1);
            case WEST -> getRow().getCellIfExists(getColumnNumber() - 1);
            case NORTH -> getSheet().getCellIfExists(getRowNumber() - 1, getColumnNumber());
            case SOUTH -> getSheet().getCellIfExists(getRowNumber() + 1, getColumnNumber());
        };
    }
}
