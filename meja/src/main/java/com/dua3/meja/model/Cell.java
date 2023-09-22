
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

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.TextUtil;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * A single cell of a sheet.
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
    void copy(Cell other);

    /**
     * Return raw cell value.
     *
     * @return cell value
     */
    Object get();

    /**
     * Return text representation of value.
     *
     * @param locale the locale to use during formatting
     * @return cell value as R, as it would be displayed
     */
    RichText getAsText(Locale locale);

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
     * <li>{@link Date} (deprecated)
     * <li>{@link String}
     * <li>{@link RichText}
     * </ul>
     *
     * @param arg the value
     * @return this cell
     */
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    default Cell set(@Nullable Object arg) {
        if (arg == null) {
            clear();
            return this;
        }

        arg = getWorkbook().cache(arg);
        if (arg instanceof Number) {
            set((Number) arg);
        } else if (arg instanceof Boolean) {
            set((Boolean) arg);
        } else if (arg instanceof LocalDateTime) {
            set((LocalDateTime) arg);
        } else if (arg instanceof LocalDate) {
            set((LocalDate) arg);
        } else if (arg instanceof RichText) {
            set((RichText) arg);
        } else if (arg instanceof Date) {
            LocalDateTime dt = LocalDateTime.ofInstant(((Date) arg).toInstant(), ZoneId.systemDefault());
            LocalTime t = dt.toLocalTime();
            if (t.toNanoOfDay() == 0) {
                // set to DATE
                set(dt.toLocalDate());
            } else {
                // set to DATE_TIME
                set(dt);
            }
        } else {
            set(String.valueOf(arg));
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
     * Get Hyperlink.
     *
     * @return an Optional with the Hyperlink or empty Optional
     */
    Optional<URI> getHyperlink();

    /**
     * Calculate the dimension of the cell based on the cell's content.
     *
     * @return the calculated dimension of the cell
     */
    default Dimension2f calcCellDimension() {
        // calculate the exact width
        String text = toString();
        CellStyle cellStyle = getCellStyle();
        Font font = cellStyle.getFont();
        Dimension2f dim = TextUtil.getTextDimension(text, font);

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
     * Return string representation of cell content.
     *
     * @param locale locale for formatting
     * @return string representation of cell content
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
     * Test if cell is merged.
     *
     * @return true if cell is merged
     */
    default boolean isMerged() {
        return getHorizontalSpan() != 1 || getVerticalSpan() != 1;
    }

    /**
     * Merge cell with neighbouring cells.
     *
     * @param spanX the horizontal span
     * @param spanY the vertical span
     * @return this cell after merging
     */
    default Cell merge(int spanX, int spanY) {
        LangUtil.check(!isMerged(), "Cell is already merged.");
        LangUtil.check(spanX >= 1);
        LangUtil.check(spanY >= 1);

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
}
