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

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
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
        return getCell(getColumnCount());
    }

    /**
     * Create new cell to the right of the existing cells.
     *
     * @param value the value to set, {@code null} for an empty cell
     * @return new Cell instance
     */
    default Cell createCell(@Nullable Object value) {
        Cell cell = getCell(getColumnCount());
        cell.set(value);
        return cell;
    }

    /**
     * Get cell at given column, creating a new one if it does not exist.
     *
     * @param j the column number
     * @return this row's cell for the given column
     */
    Cell getCell(int j);

    /**
     * Retrieves the total number of columns in this row.
     *
     * @return the number of columns in this row
     */
    int getColumnCount();

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
     * <p>
     * <strong>Note:</strong> The returned iterator is read-only, i.e., calling {@link Iterator#remove()} will throw a
     * {@link UnsupportedOperationException}.
     *
     * @return cell iterator
     */
    @Override
    default Iterator<Cell> iterator() {
        return new Iterator<>() {

            private int colNum = getFirstCellNum();

            @Override
            public boolean hasNext() {
                return colNum < getColumnCount();
            }

            @Override
            public Cell next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("no more cells in row");
                }

                return getCell(colNum++);
            }

            /**
             * Removes the current element from the underlying collection.
             * <p>
             * This implementation is not supported and will always throw an
             * {@link UnsupportedOperationException}.
             *
             * @throws UnsupportedOperationException always thrown as this operation is not supported
             */
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

    /**
     * Find cell containing text in row.
     *
     * @param text    the text to search for
     * @param ss      the {@link SearchSettings} to use
     * @return the cell found or {@code null} if nothing found
     */
    default Optional<Cell> find(String text, SearchSettings ss) {
        if (ss.ignoreCase()) {
            text = text.toLowerCase(Locale.ROOT);
        }

        int jStart;
        jStart = ss.searchFromCurrent() ? getSheet().getCurrentCell().getColumnNumber() : getColumnCount() - 1;
        int j = jStart;
        do {
            // move to next cell
            if (j < getColumnCount() - 1) {
                j++;
            } else {
                j = 0;
            }

            Cell cell = getCell(j);

            // check cell content
            String cellText;
            if (ss.searchFormula() && cell.getCellType() == CellType.FORMULA) {
                cellText = cell.getFormula();
            } else {
                cellText = cell.toString();
            }

            if (ss.ignoreCase()) {
                cellText = cellText.toLowerCase(Locale.ROOT);
            }

            if (ss.matchComplete() ? cellText.equals(text) : cellText.contains(text)) {
                // found!
                if (ss.updateCurrent()) {
                    getSheet().setCurrentCell(cell);
                }
                return Optional.of(cell);
            }
        } while (j != jStart);

        return Optional.empty();
    }

    /**
     * Get the height of the row.
     *
     * @return the row height in points
     */
    default float getRowHeight() {
        return getSheet().getRowHeight(getRowNumber());
    }
}
