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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An interface representing a single sheet of a workbook.
 *
 * @author axel
 */
public interface Sheet extends Iterable<Row>, ReadWriteLock {

    /**
     * Subscribes a subscriber to receive events of a specified class.
     *
     * @param subscriber   the subscriber to receive the events
     */
    void subscribe(Flow.Subscriber<SheetEvent> subscriber);

    /**
     * Add new merged region.
     *
     * @param cells the region of cells to be merged
     * @throws IllegalStateException if the region already contains merged cells
     */
    void addMergedRegion(RectangularRegion cells);

    /**
     * Adjusts the size of the column to its contents.
     *
     * @param j the column to resize
     */
    void autoSizeColumn(int j);

    /**
     * Adjusts the size of all columns.
     */
    void autoSizeColumns();

    /**
     * Adjusts the size of the row to its contents.
     *
     * @param i the row to resize
     */
    void autoSizeRow(int i);

    /**
     * Remove all content from the sheet.
     */
    void clear();

    /**
     * Get auto filter row number.
     *
     * @return row number for auto filter or -1 if not set
     */
    int getAutoFilterRow();

    /**
     * Get cell at given position, creating a new one if it does not exist yet.
     *
     * @param i the row number
     * @param j the column number
     * @return the cell at row {@code i} and column {@code j}
     */
    Cell getCell(int i, int j);

    /**
     * Get cell if exists.
     *
     * @param i the row number
     * @param j the column number
     * @return Optional holding the cell at row {@code i} and column {@code j}, or empty Optional
     */
    Optional<? extends Cell> getCellIfExists(int i, int j);

    /**
     * Get number of columns.
     *
     * @return number of columns in this sheet
     */
    int getColumnCount();

    /**
     * Get column width.
     *
     * @param j the column number
     * @return width of the column with number {@code j} in points
     */
    float getColumnWidth(int j);

    /**
     * Get the current cell.
     *
     * @return the current cell
     */
    Optional<? extends Cell> getCurrentCell();

    /**
     * Get number of first used column.
     *
     * @return the first used column number
     */
    int getFirstColNum();

    /**
     * Get number of first used row.
     *
     * @return the first used column number, or 0 if sheet is empty
     */
    int getFirstRowNum();

    /**
     * Get number of last used column.
     *
     * @return the last used column number
     */
    int getLastColNum();

    /**
     * Get number of last used row.
     *
     * @return the last used row number
     */
    int getLastRowNum();

    /**
     * Get the list of merged regions for this sheet.
     *
     * @return list of merged regions
     */
    List<RectangularRegion> getMergedRegions();

    /**
     * Get merged region at position.
     *
     * @param rowNum the row number
     * @param colNum the column number
     * @return the merged region or null if the cell does not belong to a merged
     * region
     */
    RectangularRegion getMergedRegion(int rowNum, int colNum);

    /**
     * Get row, creating it if necessary.
     *
     * @param i the row number
     * @return the row with row number {@code i}
     */
    Row getRow(int i);

    /**
     * Get existent row.
     *
     * @param i the row number
     * @return Optional containing the row with row number {@code i}
     */
    Optional<? extends Row> getRowIfExists(int i);

    /**
     * Get number of rows.
     *
     * @return number of rows in this sheet
     */
    int getRowCount();

    /**
     * Get row height.
     *
     * @param i the row number
     * @return height of the row with number {@code i} in points
     */
    float getRowHeight(int i);

    /**
     * Get name of sheet.
     *
     * @return name of sheet
     */
    String getSheetName();

    /**
     * Get the frozen column.
     *
     * @return the number of the first column that will not remain at a fixed
     * position when scrolling
     */
    int getSplitColumn();

    /**
     * Get the split row.
     *
     * @return the number of the first row that will not remain at a fixed position
     * when scrolling
     */
    int getSplitRow();

    /**
     * Get workbook.
     *
     * @return the workbook this sheet belongs to
     */
    Workbook getWorkbook();

    /**
     * Get the zoom factor for this sheet.
     *
     * @return zoom factor, 1.0=100%
     */
    float getZoom();

    /**
     * Create a new row at the bottom of the sheet.
     *
     * @param values the values to insert (optional)
     * @return new row instance
     */
    default Row createRow(Object... values) {
        return createRowWith(Arrays.asList(values));
    }

    /**
     * Create a new row at the bottom of the sheet.
     *
     * @param <T> the generic type of the items to insert into the row
     * @param <C> the generic type of the {@link Iterable} holding the items to be set
     * @param values the values to insert
     * @return new row instance
     */
    default <T, C extends Iterable<T>> Row createRowWith(C values) {
        Row row = getRow(getRowCount());
        values.forEach(row::createCell);
        return row;
    }

    /**
     * Sets an automatic filter on the given row (optional operation).
     *
     * @param i the row number
     */
    void setAutofilterRow(int i);

    /**
     * Set column width.
     *
     * @param j     the column number
     * @param width the width of the column in points
     */
    void setColumnWidth(int j, float width);

    /**
     * Set the current cell.
     *
     * @param cell the cell to set as current
     * @return true, if the current cell changes
     */
    boolean setCurrentCell(Cell cell);

    /**
     * Set the current cell.
     *
     * @param i the row of the cell to set
     * @param j the column of the cell to set
     * @return true, if the current cell changes
     */
    default boolean setCurrentCell(int i, int j) {
        return setCurrentCell(getCell(i, j).getLogicalCell());
    }

    /**
     * Set row height.
     *
     * @param i      the row number
     * @param height the height of the row in points
     */
    void setRowHeight(int i, float height);

    /**
     * Set the zoom factor for this sheet.
     *
     * @param zoom zoom factor, 1.0f=100%
     */
    void setZoom(float zoom);

    /**
     * Split (freeze) view.
     * <p>
     * Splits the sheet so that rows <em>above</em> i and columns <em>to the
     * left</em> of j remain in view when scrolling.
     *
     * @param i row number
     * @param j column number
     */
    void splitAt(int i, int j);

    /**
     * Create row iterator.
     *
     * @return row iterator
     */
    @Override
    default Iterator<Row> iterator() {
        return new Iterator<>() {

            private int rowNum = getFirstRowNum();

            @Override
            public boolean hasNext() {
                return rowNum <= getLastRowNum();
            }

            @Override
            public Row next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("no more rows in sheet");
                }

                return getRow(rowNum++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing of rows is not supported.");
            }
        };
    }

    /**
     * Copy sheet data from another sheet.
     *
     * @param other sheet to copy data from
     */
    default void copy(Sheet other) {
        // get split and autofilter position
        int splitRow = other.getSplitRow();
        int splitColumn = other.getSplitColumn();
        int autoFilterRow = other.getAutoFilterRow();

        // copy column widths
        for (int j = other.getFirstColNum(); j <= other.getLastColNum(); j++) {
            setColumnWidth(j, other.getColumnWidth(j));
        }

        // copy merged regions
        for (RectangularRegion rr : other.getMergedRegions()) {
            addMergedRegion(rr);
        }

        // copy row data
        for (Row row : other) {
            final int i = row.getRowNumber();
            getRow(i).copy(row);
            setRowHeight(i, other.getRowHeight(i));

            // apply split and autofilter after row is written (POI restriction)
            if (i == autoFilterRow) {
                setAutofilterRow(autoFilterRow);
            }
            if (i == splitRow) {
                splitAt(splitRow, splitColumn);
            }
        }
    }

    /**
     * Create a stream of the rows in this sheet.
     *
     * @return stream of rows
     */
    default Stream<Row> rows() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    /**
     * Translate column number to column name.
     *
     * @param j the column number
     * @return the column name
     */
    static String getColumnName(int j) {
        StringBuilder sb = new StringBuilder();
        // noinspection CharUsedInArithmeticContext
        sb.append((char) ('A' + j % 26));
        j /= 26;
        while (j > 0) {
            // noinspection CharUsedInArithmeticContext
            sb.insert(0, (char) ('A' + j % 26 - 1));
            j /= 26;
        }
        return new String(sb);
    }

    /**
     * Translate column name to column number.
     *
     * @param colName the name of the column, ie. "A", "B",... , "AA", "AB",...
     * @return the column number
     * @throws IllegalArgumentException if {@code colName} is not a valid column
     *                                  name
     */
    static int getColumnNumber(String colName) {
        final int stride = (int) 'z' - (int) 'a' + 1;
        int col = 0;
        for (char c : colName.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c < 'a' || 'z' < c) {
                throw new IllegalArgumentException("'" + colName + "' ist no valid column name.");
            }

            int d = (int) c - (int) 'a' + 1;
            col = col * stride + d;
        }
        return col - 1;
    }

    /**
     * Get row name as String.
     *
     * @param i the row number
     * @return the row name (in Excel convention, i.e. "1" for row number 0)
     */
    static String getRowName(int i) {
        return Integer.toString(i + 1);
    }

    /**
     * Test if sheet is empty.
     *
     * @return true, if the sheet is empty
     */
    default boolean isEmpty() {
        return getRowCount() == 0;
    }

    /**
     * Find cell containing text in sheet.
     *
     * @param text the text to search for
     * @param ss   the {@link SearchSettings} to use
     * @return {@code Optional} holding the cell found or empty
     */
    default Optional<Cell> find(String text, SearchSettings ss) {
        Lock lock = readLock();
        lock.lock();
        try {
            if (isEmpty()) {
                return Optional.empty();
            }

            if (ss.ignoreCase()) {
                text = text.toLowerCase(Locale.ROOT);
            }

            Cell end = null;
            Cell cell;
            if (ss.searchFromCurrent()) {
                cell = getCurrentCell().map(this::nextCell).orElse(getCell(getFirstRowNum(), getFirstColNum()));
            } else {
                cell = getCell(getFirstRowNum(), getFirstColNum());
            }

            while (end == null || !(cell.getRowNumber() == end.getRowNumber()
                    && cell.getColumnNumber() == end.getColumnNumber())) {
                if (end == null) {
                    // remember the first visited cell
                    end = cell;
                }

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
                        setCurrentCell(cell);
                    }
                    return Optional.of(cell);
                }

                // move to next cell
                cell = nextCell(cell);
            }

            // not found
            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    private Cell nextCell(@Nullable Cell cell) {
        assert cell != null && cell.getSheet() == this;

        // move to next cell
        Row row = cell.getRow();
        int j = cell.getColumnNumber();
        if (j < row.getLastCellNum()) {
            // cell is not the last one in row -> move right
            return row.getCell(j + 1);
        } else {
            // cell is the last one in row...
            int i = row.getRowNumber();
            if (i < getLastRowNum()) {
                // not the last row -> move to next row
                row = getRow(i + 1);
            } else {
                // last row -> move to first row
                row = getRow(getFirstRowNum());
            }
            // return the first cell of the new row
            return row.getCell(row.getFirstCellNum());
        }
    }

}
