/*
 * Copyright 2016 axel.
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
package com.dua3.meja.ui;

import com.dua3.meja.model.Sheet;
import com.dua3.utility.data.Color;

import java.beans.PropertyChangeListener;
import java.util.function.IntFunction;

/**
 *
 * @author axel
 */
public interface SheetView {

    double MAX_COLUMN_WIDTH = 800;

    /**
     * property "sheet".
     */
    String PROPERTY_SHEET = "sheet";

    /**
     * Add a new listener.
     *
     * @param listener listener to be added
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Add a new listener for the given property.
     *
     * @param propertyName the property
     * @param listener     the listener
     */
    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Get the grid color.
     *
     * @return color of grid
     */
    Color getGridColor();

    /**
     * Get the sheet for this view.
     *
     * @return the sheet
     */
    Sheet getSheet();

    /**
     * Check whether editing is enabled.
     *
     * @return true if this SheetView allows editing.
     */
    boolean isEditable();

    /**
     * Check editing state.
     *
     * @return true, if a cell is being edited.
     */
    boolean isEditing();

    /**
     * Remove a listener.
     *
     * @param listener the listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a listener.
     *
     * @param propertyName the property
     * @param listener     the listener
     */
    void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Scroll the currently selected cell into view.
     */
    void scrollToCurrentCell();

    /**
     * Set current row and column.
     *
     * @param rowNum number of row to be set
     * @param colNum number of column to be set
     * @return true if the current logical cell changed
     */
    boolean setCurrentCell(int rowNum, int colNum);

    /**
     * Enable/disable sheet editing.
     *
     * @param editable true to allow editing
     */
    void setEditable(boolean editable);

    /**
     * Set the grid color.
     *
     * @param gridColor the color for th grid
     */
    void setGridColor(Color gridColor);

    /**
     * Set sheet to display.
     *
     * @param sheet the sheet to display
     */
    void setSheet(Sheet sheet);

    /**
     * End edit mode for the current cell.
     *
     * @param commit true if the content of the edited cell is to be updated
     */
    void stopEditing(boolean commit);

    /**
     * Get name for column.
     * 
     * @param j the column number
     * @return the label text
     */
    String getColumnName(int j);

    /**
     * Get name for row.
     * 
     * @param i the row number
     * @return the label text
     */
    String getRowName(int i);

    /**
     * Set the column name provider.
     * 
     * @param columnNames a function that maps column numbers to column names
     */
    void setColumnNames(IntFunction<String> columnNames);

    /**
     * Set the row name provider.
     * 
     * @param rowNames a function that maps row numbers to row names
     */
    void setRowNames(IntFunction<String> rowNames);
}
