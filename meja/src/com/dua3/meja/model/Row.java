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

/**
 * A single row of a sheet.
 *
 * @author axel
 */
public interface Row extends Iterable<Cell> {

    /**
     * Get the sheet this row belongs to.
     * @return the sheet
     */
    Sheet getSheet();

    /**
     * Get cell.
     * @param col the column number
     * @return this row`s cell for the given column
     */
    Cell getCell(int col);

    /**
     * Get row number.
     * @return row number in sheet
     */
    int getRowNumber();

    /**
     * Get column number of first cell.
     *
     * Workbooks have an area of used cells. All cells outside of that area
     * are blank. If the value returned by this method is greater than 0 that
     * means that all the cells to the left of the column returned are blank.
     * The opposite is not necessarily true, since the area of used cells
     * might not be updated when a cell is cleared (because that would require
     * a full sweep of all rows).
     * @return number of first cell that potentially contains a value.
     */
    public int getFirstCellNum();

    /**
     * Get column number of last cell.
     *
     * Workbooks have an area of used cells. All cells outside of that area
     * are blank. If the value returned by this method is less than
     * the number of columns-1 that means that all the cells to the right of
     * the column returned are blank.
     * The opposite is not necessarily true, since the area of used cells
     * might not be updated when a cell is cleared (because that would require
     * a full sweep of all rows).
     * @return number of last cell that potentially contains a value.
     */
    public int getLastCellNum();

}
