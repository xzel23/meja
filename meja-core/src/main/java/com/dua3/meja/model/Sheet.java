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
import com.dua3.utility.concurrent.AutoLock;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Flow;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An interface representing a single sheet of a workbook. A Sheet contains rows and cells organized in a tabular structure.
 * It provides methods for accessing and manipulating cell data, managing merged regions, auto-sizing rows and columns,
 * and handling sheet-level operations.
 * <p>
 * The Sheet interface implements the Iterable&lt;Row&gt; interface, allowing iteration over all rows in the sheet.
 * It also supports an event system through which subscribers can receive notifications about changes to the sheet.
 * <p>
 * <strong>Units:</strong>
 * <ul>
 *   <li>All physical lengths exposed by the model API (column widths, row heights, default sizes, and cell content dimensions)
 *       are measured in typographical points (1/72 inch).</li>
 *   <li>Zoom is a unitless factor where {@code 1.0} corresponds to 100%.</li>
 *   <li>Row and column indices are 0-based.</li>
 * </ul>
 *
 * @author axel
 * @see Row
 * @see Cell
 * @see Workbook
 */
public interface Sheet extends Iterable<Row> {

    /**
     * Subscribe to sheet events.
     *
     * @param subscriber the subscriber to receive events
     */
    void subscribe(Flow.Subscriber<SheetEvent> subscriber);

    /**
     * Creates a new merged region in the sheet. A merged region is a rectangular area of cells that acts
     * as a single cell. The content and style of the top-left cell in the region becomes the content
     * and style for the entire merged region.
     *
     * @param cells the rectangular region of cells to be merged
     * @throws IllegalStateException if any cell in the region is already part of another merged region
     * @throws IllegalArgumentException if the region is invalid (empty or outside sheet bounds)
     * @see #getMergedRegions()
     * @see #getMergedRegion(int, int)
     */
    void addMergedRegion(RectangularRegion cells);

    /**
     * Adjusts the size of the column to its contents.
     * <p>
     * The computed width is measured in typographical points (1/72 inch) and applied via
     * {@link #setColumnWidth(int, float)}.
     *
     * @param j the column to resize (0-based index)
     */
    void autoSizeColumn(int j);

    /**
     * Adjusts the size of all columns.
     * <p>
     * The computed widths are measured in typographical points (1/72 inch) and applied via
     * {@link #setColumnWidth(int, float)}.
     */
    void autoSizeColumns();

    /**
     * Adjusts the size of the row to its contents.
     * <p>
     * The computed height is measured in typographical points (1/72 inch) and applied via
     * {@link #setRowHeight(int, float)}.
     *
     * @param i the row to resize (0-based index)
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
     * Returns the cell at the specified position, creating a new empty cell if one doesn't exist.
     * This method ensures a cell is always returned, creating new rows or cells as needed.
     *
     * @param i the row number (0-based)
     * @param j the column number (0-based)
     * @return the cell at the specified position, never null
     * @throws IllegalArgumentException if row or column number is negative
     * @see #getCellIfExists(int, int)
     * @see #getRow(int)
     */
    Cell getCell(int i, int j);

    /**
     * Returns the cell at the specified position if it exists, without creating new cells.
     * Unlike {@link #getCell(int, int)}, this method will not create new cells or rows
     * if they don't exist.
     *
     * @param i the row number (0-based)
     * @param j the column number (0-based)
     * @return an Optional containing the cell if it exists, or an empty Optional if the cell
     *         or its containing row doesn't exist
     * @throws IllegalArgumentException if row or column number is negative
     * @see #getCell(int, int)
     * @see #getRowIfExists(int)
     */
    Optional<? extends Cell> getCellIfExists(int i, int j);

    /**
     * Returns the total number of columns in this sheet. This count includes all columns
     * between 0 and the last used column, inclusive, regardless of whether individual
     * columns contain data.
     *
     * @return the total number of columns in this sheet
     */
    int getColumnCount();

    /**
     * Get column width.
     *
     * @param j the column index (0-based)
     * @return width of the column with number {@code j} in points
     */
    float getColumnWidth(int j);

    /**
     * Get the current cell in the sheet. The current cell represents the active or focused cell
     * in the sheet, which is typically used for navigation and editing operations.
     * <p>
     * If no active cell has been set, the cell at position "A1" is returned
     *
     * @return the current cell
     * @see #setCurrentCell(Cell)
     * @see #setCurrentCell(int, int)
     */
    Cell getCurrentCell();

    /**
     * Returns a list of all merged regions in this sheet. A merged region is a rectangular area
     * of cells that acts as a single cell. The list is unmodifiable and represents a snapshot
     * of the current state.
     *
     * @return an unmodifiable list of all merged regions in this sheet
     * @see #addMergedRegion(RectangularRegion)
     * @see #getMergedRegion(int, int)
     */
    List<RectangularRegion> getMergedRegions();

    /**
     * Returns the merged region that contains the cell at the specified position.
     *
     * @param rowNum the row index (0-based)
     * @param colNum the column index (0-based)
     * @return an Optional containing the merged region, or empty if the cell is not part of any merged region
     * @see #getMergedRegions()
     * @see #addMergedRegion(RectangularRegion)
     */
    Optional<RectangularRegion> getMergedRegion(int rowNum, int colNum);

    /**
     * Returns the row at the specified index, creating it if it doesn't exist.
     * This method ensures a row is always returned, creating new rows as needed.
     *
     * @param i the row number (0-based)
     * @return the row at the specified position, never null
     * @throws IllegalArgumentException if the row number is negative
     * @see #getRowIfExists(int)
     */
    Row getRow(int i);

    /**
     * Returns the row at the specified index if it exists, without creating new rows.
     * Unlike {@link #getRow(int)}, this method will not create new rows if they don't exist.
     *
     * @param i the row number (0-based)
     * @return an Optional containing the row if it exists, or an empty Optional if the row doesn't exist
     * @throws IllegalArgumentException if the row number is negative
     * @see #getRow(int)
     */
    Optional<? extends Row> getRowIfExists(int i);

    /**
     * Returns the total number of rows in this sheet. This count includes all rows
     * between 0 and the last used row, inclusive, regardless of whether individual
     * rows contain data. Empty rows within this range are included in the count.
     *
     * @return the total number of rows in this sheet
     * @see #isEmpty()
     */
    int getRowCount();

    /**
     * Returns the height of the specified row in points. The height affects the visual
     * presentation of the row in the sheet.
     *
     * @param i the row number (0-based)
     * @return height of the specified row in points
     * @throws IllegalArgumentException if the row number is negative
     * @see #setRowHeight(int, float)
     * @see #getDefaultRowHeight()
     */
    float getRowHeight(int i);

    /**
     * Returns the name of this sheet. The sheet name is a unique identifier within its workbook
     * and is used to reference the sheet in formulas and programmatic access.
     *
     * @return the name of this sheet
     * @see Workbook
     */
    String getSheetName();

    /**
     * Returns the frozen column split point. Columns before this point remain visible
     * while scrolling horizontally. This is commonly used to keep headers visible.
     *
     * @return the number of the first unfrozen column (0-based)
     * @see #splitAt(int, int)
     */
    int getSplitColumn();

    /**
     * Returns the frozen row split point. Rows before this point remain visible
     * while scrolling vertically. This is commonly used to keep headers visible.
     *
     * @return the number of the first unfrozen row (0-based)
     * @see #splitAt(int, int)
     */
    int getSplitRow();

    /**
     * Returns the workbook that contains this sheet. Each sheet belongs to exactly one workbook
     * and maintains this association throughout its lifetime.
     *
     * @return the workbook that contains this sheet, never null
     */
    Workbook getWorkbook();

    /**
     * Get the zoom factor for this sheet.
     *
     * @return zoom factor, 1.0=100%
     */
    float getZoom();

    /**
     * Creates a new row at the end of the sheet and populates it with the provided values.
     * This is a convenience method that converts the varargs parameter to a list and delegates
     * to {@link #createRowWith(Iterable)}.
     *
     * <p>Example usage:
     * {@code Row row = sheet.createRow("Name", 42, true);}</p>
     *
     * @param values the values to insert into the cells of the new row. Each value will be converted
     *              to its string representation if necessary. Null values create empty cells.
     * @return the newly created row
     * @see #createRowWith(Iterable)
     * @see #getRowCount()
     */
    default Row createRow(@Nullable Object... values) {
        return createRowWith(Arrays.asList(values));
    }

    /**
     * Creates a new row at the end of the sheet and populates it with values from the provided Iterable.
     * This is the primary implementation for row creation with multiple values. It supports any Iterable
     * source of values, making it flexible for different data structures.
     *
     * <p>Examples:
     * <pre>{@code
     * // Using a List
     * List<String> data = Arrays.asList("Name", "Age", "City");
     * Row row1 = sheet.createRowWith(data);
     *
     * // Using a Set
     * Set<Integer> numbers = new HashSet<>(Arrays.asList(1, 2, 3));
     * Row row2 = sheet.createRowWith(numbers);
     * }</pre>
     *
     * @param <T> the type of elements in the Iterable
     * @param <C> the type of the Iterable collection
     * @param values the Iterable containing values to insert into the new row's cells.
     *              Each value will be converted to its string representation if necessary.
     *              Null values create empty cells.
     * @return the newly created row
     * @see #createRow(Object...)
     * @see #getRowCount()
     * @see Row#createCell(Object)
     */
    default <T, C extends Iterable<@Nullable T>> Row createRowWith(C values) {
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
     * Sets the current (active) cell in the sheet. The current cell is used for navigation
     * and editing operations. If the specified cell belongs to a merged region, the top-left
     * cell of that region becomes the current cell.
     *
     * @param cell the cell to set as current. If null, the current cell selection is cleared.
     * @return true if the current cell changed, false if the specified cell was already the current cell
     * @throws IllegalArgumentException if the cell belongs to a different sheet
     * @see #getCurrentCell()
     * @see #setCurrentCell(int, int)
     */
    boolean setCurrentCell(Cell cell);

    /**
     * Convenience method to set the current cell by its row and column coordinates.
     * This method creates or retrieves the cell at the specified position and sets it
     * as the current cell.
     *
     * @param i the row number of the cell to set as current
     * @param j the column number of the cell to set as current
     * @return true if the current cell changed, false if the specified cell was already the current cell
     * @see #setCurrentCell(Cell)
     * @see #getCurrentCell()
     * @see #getCell(int, int)
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
     * Splits the sheet so that rows above {@code i} and columns to the left of {@code j} remain visible when scrolling.
     *
     * @param i row index (0-based)
     * @param j column index (0-based)
     */
    void splitAt(int i, int j);

    /**
     * Returns an iterator over the rows in this sheet.
     * Empty rows within this range are included in the iteration.
     *
     * <p>This method is part of the {@link Iterable} interface implementation, allowing
     * the sheet to be used in for-each loops: {@code for (Row row : sheet) {...}}</p>
     *
     * <p>The iterator's {@code remove()} operation is not supported.</p>
     *
     * @return an iterator over the rows in ascending order
     * @see #rows() for a stream-based alternative
     * @throws UnsupportedOperationException if attempting to remove rows through the iterator
     */
    @Override
    default Iterator<Row> iterator() {
        return new Iterator<>() {

            private int rowNum = 0;

            @Override
            public boolean hasNext() {
                return rowNum <= getRowCount() - 1;
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

        // copy column widths (must be done after copying rows because POI SXSSF overwrites widths when adding the rows)
        for (int j = 0; j < other.getColumnCount(); j++) {
            setColumnWidth(j, other.getColumnWidth(j));
        }
    }

    /**
     * Returns a sequential {@link Stream} of all rows in this sheet. This method provides
     * a stream-based alternative to the {@link #iterator()} method, enabling functional-style
     * operations on the rows.
     *
     * <p>The stream traverses rows in ascending order from 0 to
     * {@link #getColumnCount()}, exclusive. Empty rows within this range are included in
     * the stream. The stream is ordered and non-parallel.</p>
     *
     * <p>Example usage:
     * {@code sheet.rows().filter(row -> !row.isEmpty()).forEach(row -> processRow(row));}</p>
     *
     * @return a sequential Stream of rows in ascending order
     * @see #iterator()
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
     * @param colName the name of the column, i.e. "A", "B",... , "AA", "AB",...
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
     * Test if the sheet is empty.
     *
     * @return true, if the sheet is empty
     */
    default boolean isEmpty() {
        return getRowCount() == 0;
    }

    /**
     * Acquires a read lock for thread-safe access to the sheet. Multiple threads can hold read locks
     * simultaneously, but a read lock cannot be acquired while a write lock is held. The lock is
     * automatically released when the returned AutoLock is closed.
     *
     * <p>Example usage with try-with-resources:
     * <pre>{@code
     * try (var lock = sheet.readLock("MyOperation")) {
     *     // Read operations here...
     *     Cell cell = sheet.getCell(0, 0);
     * }
     * }</pre>
     *
     * @param name a descriptive name for the lock, used for debugging and monitoring.
     *            Should indicate the operation or component acquiring the lock.
     * @return an AutoLock that will automatically release the read lock when closed
     * @throws IllegalArgumentException if name is null
     * @see #writeLock(String)
     * @see AutoLock
     */
    AutoLock readLock(String name);

    /**
     * Acquires an exclusive write lock for thread-safe modifications to the sheet. Only one thread
     * can hold a write lock at a time, and no read locks can be held while a write lock is active.
     * The lock is automatically released when the returned AutoLock is closed.
     *
     * <p>Example usage with try-with-resources:
     * <pre>{@code
     * try (var lock = sheet.writeLock("UpdateOperation")) {
     *     // Modification operations here...
     *     sheet.getCell(0, 0).setValue("New Value");
     *     sheet.addMergedRegion(region);
     * }
     * }</pre>
     *
     * <p><strong>Note:</strong> Write locks should be held for the shortest time possible
     * as they block all other threads from reading or writing.</p>
     *
     * @param name a descriptive name for the lock, used for debugging and monitoring.
     *            Should indicate the operation or component acquiring the lock.
     * @return an AutoLock that will automatically release the write lock when closed
     * @throws IllegalArgumentException if name is null
     * @see #readLock(String)
     * @see AutoLock
     */
    AutoLock writeLock(String name);

    /**
     * Find cell containing text in sheet.
     *
     * @param text the text to search for
     * @param ss   the {@link SearchSettings} to use
     * @return {@code Optional} holding the cell found or empty
     */
    default Optional<Cell> find(String text, SearchSettings ss) {
        try (var __ = readLock("AbstractSheet.find()")) {
            if (isEmpty()) {
                return Optional.empty();
            }

            if (ss.ignoreCase()) {
                text = text.toLowerCase(Locale.ROOT);
            }

            Cell end = null;
            Cell cell;
            if (ss.searchFromCurrent()) {
                cell = nextCell(getCurrentCell());
            } else {
                cell = getCell(0, 0);
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
        }
    }

    private Cell nextCell(@Nullable Cell cell) {
        assert cell != null && cell.getSheet() == this;

        // move to next cell
        Row row = cell.getRow();
        int j = cell.getColumnNumber();
        if (j < row.getColumnCount() - 1) {
            // cell is not the last one in row -> move right
            return row.getCell(j + 1);
        } else {
            // cell is the last one in row...
            int i = row.getRowNumber();
            if (i < getRowCount() - 1) {
                // not the last row -> move to next row
                row = getRow(i + 1);
            } else {
                // last row -> move to first row
                row = getRow(0);
            }
            // return the first cell of the new row
            return row.getCell(row.getFirstCellNum());
        }
    }

    /**
     * Get the default row height for this sheet, i.e., the height used when creating new rows.
     *
     * @return the default row height in typographical points (1/72 inch)
     */
    float getDefaultRowHeight();

    /**
     * Get the default column width for this sheet, i.e., the width used when creating new columns.
     *
     * @return the default column width in typographical points (1/72 inch)
     */
    float getDefaultColumnWidth();
}
