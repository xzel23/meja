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

import com.dua3.meja.util.RectangularRegion;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * An interface representing a single sheet of a workbook.
 *
 * @author axel
 */
public interface Sheet extends Iterable<Row>, ReadWriteLock {

    static final String PROPERTY_ZOOM = "zoom";
    static final String PROPERTY_LAYOUT = "layout";
    static final String PROPERTY_FREEZE = "freeze";
    static final String PROPERTY_ACTIVE_CELL = "active cell";
    static final String PROPERTY_CELL_CONTENT = "cell content";
    static final String PROPERTY_CELL_STYLE = "cell style";

    /**
     * Get name of sheet.
     *
     * @return name of sheet
     */
    String getSheetName();

    /**
     * Get number of first used column.
     * @return the first used column number
     */
    int getFirstColNum();

    /**
     * Get number of first used row.
     * @return the first used column number
     */
    int getFirstRowNum();

    /**
     * Get number of last used column.
     * @return the last used column number
     */
    int getLastColNum();

    /**
     * Get number of last used row.
     * @return the last used row number
     */
    int getLastRowNum();

    /**
     * Get number of columns.
     * @return number of columns in this sheet
     */
    int getColumnCount();

    /**
     * Get number of rows.
     * @return number of rows in this sheet
     */
    int getRowCount();

    /**
     * Get column width.
     * @param j the column number
     * @return width of the column with number {@code j} in points
     */
    float getColumnWidth(int j);

    /**
     * Set column width.
     * @param j the column number
     * @param width the width of the column in points
     */
    void setColumnWidth(int j, float width);

    /**
     * Get row height.
     * @param i the row number
     * @return height of the row with number {@code i} in points
     */
    float getRowHeight(int i);

    /**
     * Set row height.
     * @param i the row number
     * @param height the height of the row in points
     */
    void setRowHeight(int i, float height);

    /**
     * Get row.
     * @param i the row number
     * @return the row with row number {@code i}
     */
    Row getRow(int i);

    /**
     * Get workbook.
     * @return the workbook this sheet belongs to
     */
    Workbook getWorkbook();

    /**
     * Get cell.
     * @param i the row number
     * @param j the column number
     * @return the cell at row {@code i} and column {@code j}
     */
    Cell getCell(int i, int j);

    /**
     * Split (freeze) view.
     *
     * Splits the sheet so that rows <em>above</em> i and columns
     * <em>to the left</em> of j remain in view when scrolling.
     *
     * @param i row number
     * @param j column number
     */
    void splitAt(int i, int j);

    /**
     * Get the split row.
     * @return the number of the first row that will not remain at a fixed position when scrolling
     */
    int getSplitRow();

    /**
     * Get the frozen column.
     * @return the number of the first column that will not remain at a fixed position when scrolling
     */
    int getSplitColumn();

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
     * Sets an automatic filter on the given row (optional operation).
     *
     * @param i the row number
     */
    void setAutofilterRow(int i);

    /**
     * Get auto filter row number.
     * @return row number for auto filter or -1 if not set
     */
    int getAutoFilterRow();

    /**
     * Get number format for this sheet.
     * <p>
     * The number format is used when parsing user input.
     * </p>
     * @return the number format for this sheet
     */
    NumberFormat getNumberFormat();

    /**
     * Get date format for this sheet.
     * <p>
     * The date format is used when parsing user input.
     * </p>
     * @return the date format for this sheet
     */
    DateFormat getDateFormat();

    /**
     * Set the zoom factor for this sheet.
     * @param zoom zoom factor, 1.0f=100%
     */
    void setZoom(float zoom);

    /**
     * Get the zoom factor for this sheet.
     * @return zoom factor, 1.0=100%
     */
    float getZoom();

    /**
     * Copy sheet data from another sheet.
     * @param other sheet to copy data from
     */
    void copy(Sheet other);

    /**
     * Get the list of merged regions for this sheet.
     * @return list of merged regions
     */
    List<RectangularRegion> getMergedRegions();

    /**
     * Add new merged region.
     * @param cells the region of cells to be merged
     * @throws IllegalStateException if the region already contains merged cells
     */
    void addMergedRegion(RectangularRegion cells);

    /**
     * Get the current cell.
     * @return the current cell
     */
    Cell getCurrentCell();

    /**
     * Set the current cell.
     * @param cell the cell to set  as current
     */
    void setCurrentCell(Cell cell);

    /**
     * Set the current cell.
     * @param i the row of the cell to set
     * @param j the column of the cell to set
     */
    void setCurrentCell(int i, int j);

    /**
     * Remove all content from sheet.
     */
    void clear();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

}
