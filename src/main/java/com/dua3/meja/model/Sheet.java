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

import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.dua3.meja.util.RectangularRegion;

/**
 * An interface representing a single sheet of a workbook.
 *
 * @author axel
 */
public interface Sheet
        extends Iterable<Row>, ReadWriteLock {

    public final String PROPERTY_ZOOM = "ZOOM";
    public final String PROPERTY_LAYOUT_CHANGED = "LAYOUT_CHANGED";
    public final String PROPERTY_SPLIT = "SPLIT";
    public final String PROPERTY_ACTIVE_CELL = "ACTIVE_CELL";
    public final String PROPERTY_CELL_CONTENT = "CELL_CONTENT";
    public final String PROPERTY_CELL_STYLE = "CELL_STYLE";

    /**
     * Add new merged region.
     *
     * @param cells
     *            the region of cells to be merged
     * @throws IllegalStateException
     *             if the region already contains merged cells
     */
    void addMergedRegion(RectangularRegion cells);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Adjusts the size of the column to its contents.
     *
     * @param j
     *            the column to resize
     */
    void autoSizeColumn(int j);

    /**
     * Adjusts the size of all columns.
     */
    void autoSizeColumns();

    /**
     * Remove all content from sheet.
     */
    void clear();

    /**
     * Get auto filter row number.
     *
     * @return row number for auto filter or -1 if not set
     */
    int getAutoFilterRow();

    /**
     * Get cell.
     *
     * @param i
     *            the row number
     * @param j
     *            the column number
     * @return the cell at row {@code i} and column {@code j}
     */
    Cell getCell(int i, int j);

    /**
     * Get number of columns.
     *
     * @return number of columns in this sheet
     */
    int getColumnCount();

    /**
     * Get column width.
     *
     * @param j
     *            the column number
     * @return width of the column with number {@code j} in points
     */
    float getColumnWidth(int j);

    /**
     * Get the current cell.
     *
     * @return the current cell
     */
    Cell getCurrentCell();

    /**
     * Get number of first used column.
     *
     * @return the first used column number
     */
    int getFirstColNum();

    /**
     * Get number of first used row.
     *
     * @return the first used column number
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
     * @param rowNum the row number
     * @param colNum the column number
     * @return the merged region or null if the cell does not belong to a merged region
     */
    RectangularRegion getMergedRegion(int rowNum, int colNum);

    /**
     * Get row.
     *
     * @param i
     *            the row number
     * @return the row with row number {@code i}
     */
    Row getRow(int i);

    /**
     * Get number of rows.
     *
     * @return number of rows in this sheet
     */
    int getRowCount();

    /**
     * Get row height.
     *
     * @param i
     *            the row number
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
     *         position when scrolling
     */
    int getSplitColumn();

    /**
     * Get the split row.
     *
     * @return the number of the first row that will not remain at a fixed
     *         position when scrolling
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

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Sets an automatic filter on the given row (optional operation).
     *
     * @param i
     *            the row number
     */
    void setAutofilterRow(int i);

    /**
     * Set column width.
     *
     * @param j
     *            the column number
     * @param width
     *            the width of the column in points
     */
    void setColumnWidth(int j, float width);

    /**
     * Set the current cell.
     *
     * @param cell
     *            the cell to set as current
     */
    void setCurrentCell(Cell cell);

    /**
     * Set the current cell.
     *
     * @param i
     *            the row of the cell to set
     * @param j
     *            the column of the cell to set
     */
    default void setCurrentCell(int i, int j) {
        setCurrentCell(getCell(i, j));
    }

    /**
     * Set row height.
     *
     * @param i
     *            the row number
     * @param height
     *            the height of the row in points
     */
    void setRowHeight(int i, float height);

    /**
     * Set the zoom factor for this sheet.
     *
     * @param zoom
     *            zoom factor, 1.0f=100%
     */
    void setZoom(float zoom);

    /**
     * Split (freeze) view.
     *
     * Splits the sheet so that rows <em>above</em> i and columns <em>to the
     * left</em> of j remain in view when scrolling.
     *
     * @param i
     *            row number
     * @param j
     *            column number
     */
    void splitAt(int i, int j);

    /**
     * Create row iterator.
     * @return row iterator
     */
    @Override
    default Iterator<Row> iterator() {
        return new Iterator<Row>() {

            private int rowNum = getFirstRowNum();

            @Override
            public boolean hasNext() {
                return rowNum <= getLastRowNum();
            }

            @Override
            public Row next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
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
     * @param other
     *            sheet to copy data from
     */
    default void copy(Sheet other) {
        // copy split
        splitAt(other.getSplitRow(), other.getSplitColumn());
        // set autofilter
        setAutofilterRow(other.getAutoFilterRow());
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
        }
    }

    /**
     * Create a stream of the rows in this sheet.
     * @return stream of rows
     */
    default Stream<Row> rows() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    /**
     * Translate column number to column name.
     *
     * @param j
     *            the column number
     * @return the column name
     */
    static String getColumnName(int j) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) ('A' + j % 26));
        j /= 26;
        while (j > 0) {
            sb.insert(0, (char) ('A' + j % 26 - 1));
            j /= 26;
        }
        return new String(sb);
    }

    /**
     * Translate column name to column number.
     *
     * @param colName
     *            the name of the column, ie. "A", "B",... , "AA", "AB",...
     * @return the column number
     * @throws IllegalArgumentException
     *             if {@code colName} is not a valid column name
     */
    static int getColumnNumber(String colName) {
        final int stride = 'z' - 'a' + 1;
        int col = 0;
        for (char c : colName.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c < 'a' || 'z' < c) {
                throw new IllegalArgumentException("'" + colName + "' ist no valid column name.");
            }

            int d = c - 'a' + 1;
            col = col * stride + d;
        }
        return col - 1;
    }

    /**
     * Get row name as String.
     *
     * @param i
     *            the row number as used in Excel spreadsheets
     * @return the row name ("1" for row number 0)
     */
    static String getRowName(int i) {
        return Integer.toString(i + 1);
    }

}
