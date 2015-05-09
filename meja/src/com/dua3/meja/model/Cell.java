
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
package com.dua3.meja.model;

import java.text.AttributedString;
import java.util.Date;

/**
 * A single cell of a sheet.
 *
 * @author axel
 */
public interface Cell {

    public final String ERROR_TEXT = "#ERROR";

    /**
     * Return the cell type.
     *
     * @return cell type
     */
    CellType getCellType();

    /**
     * Return the result type.
     *
     * For non-formula cells, this is the same as the value returned by
     * {@link #getCellType()}. For formula cells, the result type of the last
     * evaluation is returned.
     *
     * @return cell type
     */
    CellType getResultType();

    /**
     * Return boolean cell value.
     *
     * @return boolean cell value
     * @throws IllegalArgumentException if cell is not of boolean type
     */
    boolean getBoolean();

    /**
     * Return formula.
     *
     * @return the cell`s formula
     * @throws IllegalArgumentException if no formula is set
     */
    String getFormula();

    /**
     * Return date value.
     *
     * @return date cell value
     * @throws IllegalArgumentException if cell is not of date value
     */
    Date getDate();

    /**
     * Return numeric value.
     *
     * @return numeric cell value
     * @throws IllegalArgumentException if cell is not of numeric type
     */
    Number getNumber();

    /**
     * Return string value.
     *
     * @return text cell value
     * @throws IllegalArgumentException if cell is not of text type
     */
    String getText();

    /**
     * Return text representation of value.
     *
     * @return cell value as String, as it would be displayed
     */
    String getAsText();

    /**
     * Test for empty cell.
     *
     * @return true if cell has cell type BLANK or contains the empty string.
     */
    boolean isEmpty();

    /**
     * Return the cell style.
     *
     * @return cell style
     */
    CellStyle getCellStyle();

    /**
     * Get the horizontal span.
     *
     * The horizontal span of a merged cells is the horizontal number of merged
     * cells for the top left cell of the merged cells and 0 for the other
     * merged cells. For cells that are not merged, the span is 1.
     *
     * @return horizontal span for this cell
     */
    int getHorizontalSpan();

    /**
     * Get the vertical span.
     *
     * The vertical span of a merged cells is the vertical number of merged
     * cells for the top left cell of the merged cells and 0 for the other
     * merged cells. For cells that are not merged, the span is 1.
     *
     * @return vertical span for this cell
     */
    int getVerticalSpan();

    /**
     * Get the logical cell. The logical cell for merged cells is the top left
     * cell of the group of merged cells. For cells that are not merged, the
     * logical cell is the cell itself.
     *
     * @return the logical cell
     */
    Cell getLogicalCell();

    /**
     * Get the cell`s row number.
     *
     * @return row number of this cell
     */
    int getRowNumber();

    /**
     * Get the cell`s column number.
     *
     * @return column number of this cell
     */
    int getColumnNumber();

    /**
     * Get the cell`s sheet.
     *
     * @return sheet this cell belongs to
     */
    Sheet getSheet();

    /**
     * Get the cell`s content as AttributedString for display.
     *
     * If the cell is of formula type, the result of evaluation is returned.
     *
     * @return cell content as AttributedString
     */
    AttributedString getAttributedString();

    /**
     * Set cell value to date.
     *
     * @param arg date
     * @return this cell
     */
    Cell set(Date arg);

    /**
     * Set cell value to number.
     *
     * @param arg number
     * @return this cell
     */
    Cell set(Number arg);

    /**
     * Set cell value to string.
     *
     * @param s string
     * @return this cell
     */
    Cell set(String s);

    /**
     * Set cell value to string with markup.
     *
     * @param s attributed string
     * @return this cell
     */
    Cell set(AttributedString s);

    /**
     * Set cell value to boolean value.
     *
     * @param b boolean value
     * @return this cell
     */
    Cell set(Boolean b);

    /**
     * Get the row this cell belongs to.
     *
     * @return the row for this cell
     */
    Row getRow();

    /**
     * Clear the cell`s content.
     */
    void clear();

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
    void setCellStyle(String cellStyleName);

    /**
     * Set formula
     * @param value the formula as a string
     * @return this cell
     */
    public Cell setFormula(String value);

    /**
     * Copy cell data.
     * @param other cell to copy data from
     */
    void copy(Cell other);

    /**
     * Get the workbook this cell belongs to.
     * @return the workbook
     */
    Workbook getWorkbook();

    /**
     * Unmerge cell.
     * @throws IllegalArgumentException if this cell is not the top left cell
     * of a merged region
     */
    void unMerge();
}
