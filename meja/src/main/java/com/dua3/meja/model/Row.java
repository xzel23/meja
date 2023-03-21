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

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A single row of a sheet.
 *
 * @author axel
 */
public interface Row extends Iterable<Cell> {

    /**
     * Copy row data.
     *
     * @param other row to copy data from
     */
    void copy(Row other);

    /**
     * Create new cell to the right of the existing cells.
     *
     * @return new Cell instance
     */
    default Cell createCell() {
        return getCell(getLastCellNum() + 1);
    }

    /**
     * Create new cell to the right of the existing cells.
     *
     * @return new Cell instance
     */
    default Cell createCell(Object value) {
        Cell cell = getCell(getLastCellNum() + 1);
        cell.set(value);
        return cell;
    }

    /**
     * Get cell.
     *
     * @param j the column number
     * @return this row's cell for the given column, missing cells are created on
     * the fly
     */
    Cell getCell(int j);

    /**
     * Get column number of last cell.
     * <p>
     * Workbooks have an area of used cells. All cells outside that area are
     * blank. If the value returned by this method is less than the number of
     * columns-1 that means that all the cells to the right of the column returned
     * are blank. The opposite is not necessarily true, since the area of used cells
     * might not be updated when a cell is cleared (because that would require a
     * full sweep of all rows).
     *
     * @return number of last cell that potentially contains a value.
     */
    int getLastCellNum();

    /**
     * Get cell.
     *
     * @param j the column number
     * @return this row's cell for the given column or null if cell doesn't exist
     */
    Optional<? extends Cell> getCellIfExists(int j);

    /**
     * Get row number.
     *
     * @return row number in sheet
     */
    int getRowNumber();

    /**
     * Get the sheet this row belongs to.
     *
     * @return the sheet
     */
    Sheet getSheet();

    /**
     * Get the workbook this row belongs to.
     *
     * @return the workbook
     */
    Workbook getWorkbook();

    /**
     * Create a stream of the cells in this row.
     *
     * @return stream of cells
     */
    default Stream<Cell> cells() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    /**
     * Create cell iterator.
     *
     * @return cell iterator
     */
    @Override
    default Iterator<Cell> iterator() {
        return new Iterator<>() {

            private int colNum = getFirstCellNum();

            @Override
            public boolean hasNext() {
                return colNum <= getLastCellNum();
            }

            @Override
            public Cell next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("no more cells in row");
                }

                return getCell(colNum++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing of rows is not supported.");
            }
        };
    }

    /**
     * Get column number of first cell.
     * <p>
     * Workbooks have an area of used cells. All cells outside that area are
     * blank. If the value returned by this method is greater than 0 that means that
     * all the cells to the left of the column returned are blank. The opposite is
     * not necessarily true, since the area of used cells might not be updated when
     * a cell is cleared (because that would require a full sweep of all rows).
     *
     * @return number of first cell that potentially contains a value.
     */
    int getFirstCellNum();
}
