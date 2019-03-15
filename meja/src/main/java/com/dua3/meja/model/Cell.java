
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
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.RichText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

/**
 * A single cell of a sheet.
 *
 * @author axel
 */
public interface Cell {

    /**
     * The text to show in error cells.
     */
    public final String ERROR_TEXT = "#ERROR";

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
     * Get cell reference (ie. "Sheet!A1" for the top left cell).
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
            case FIX_COLUMN:
                prefixColumn = "$";
                break;
            case FIX_ROW:
                prefixRow = "$";
                break;
            case WITH_SHEET:
                sheet = "'" + getSheet().getSheetName() + "'!";
                break;
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
     * Get the cell`s column number.
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
     * @throws IllegalStateException if cell is not of date value
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
     *
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
     *
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
     * Get the cell`s row number.
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
     *
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
    Cell set(Boolean b);

    /**
     * Set cell value to date.
     *
     * @param arg date
     * @return this cell
     * @deprecated Use the new Date and Time API introduced in Java 8 and the
     *             corresponding methods {@link #set(LocalDateTime)} and
     *             {@link #set(LocalDate)}.
     */
    @Deprecated
    Cell set(Date arg);

    /**
     * Set cell value to date.
     *
     * @param arg date
     * @return this cell
     */
    Cell set(LocalDateTime arg);

    /**
     * Set cell value to date.
     *
     * @param arg date
     * @return this cell
     */
    default Cell set(LocalDate arg) {
        set(arg == null ? null : arg.atStartOfDay());
        return this;
    }

    /**
     * Set cell value to number.
     *
     * @param arg number
     * @return this cell
     */
    Cell set(Number arg);

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
     * <li>{@link java.lang.Number}
     * <li>{@link java.lang.Boolean}
     * <li>{@link java.time.LocalDateTime}
     * <li>{@link java.time.LocalDate}
     * <li>{@link java.util.Date} (deprecated)
     * <li>{@link java.lang.String}
     * <li>{@link RichText}
     * </ul>
     * 
     * @param arg the value
     * @return this cell
     */
    default Cell set(Object arg) {
        arg = getWorkbook().cache(arg);

        if (arg == null) {
            clear();
        } else if (arg instanceof Number) {
            set((Number) arg);
        } else if (arg instanceof Boolean) {
            set((Boolean) arg);
        } else if (arg instanceof LocalDateTime) {
            set((LocalDateTime) arg);
        } else if (arg instanceof LocalDate) {
            set((LocalDate) arg);
        } else if (arg instanceof RichText) {
            set((RichText) arg);
        } else if (arg instanceof java.util.Date) {
            set((java.util.Date) arg);
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
    Cell set(RichText s);

    /**
     * Set cell value to string.
     *
     * @param s string
     * @return this cell
     */
    Cell set(String s);

    /**
     * Set cell style.
     *
     * @param cellStyle cell style
     */
    void setCellStyle(CellStyle cellStyle);

    /**
     * Sets the cell style registered under name in the workbook.
     *
     * @param cellStyleName cell style name
     */
    default void setCellStyle(String cellStyleName) {
        setCellStyle(getWorkbook().getCellStyle(cellStyleName));
    }

    /**
     * Set formula
     *
     * @param value the formula as a string
     * @return this cell
     */
    public Cell setFormula(String value);

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
     */
    default void merge(int spanX, int spanY) {
        LangUtil.check(!isMerged(), "Cell is already merged.");
        LangUtil.check(spanX >= 1);
        LangUtil.check(spanY >= 1);
        LangUtil.check(spanX > 1 || spanY > 1, "At least 2 cells must be merged.");

        int iMin = getRowNumber();
        int iMax = iMin + spanY - 1;
        int jMin = getColumnNumber();
        int jMax = jMin + spanX - 1;

        RectangularRegion region = new RectangularRegion(iMin, iMax, jMin, jMax);
        getSheet().addMergedRegion(region);
    }
}
