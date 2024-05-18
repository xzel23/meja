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

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Sheet;
import com.dua3.utility.data.Color;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * @author axel
 */
public interface SheetView {

    double MAX_COLUMN_WIDTH = 800;

    /**
     * property "sheet".
     */
    String PROPERTY_SHEET = "sheet";

    /**
     * Get the grid color.
     *
     * @return color of grid
     */
    default Color getGridColor() {
        return getDelegate().getGridColor();
    }

    /**
     * Get the sheet for this view.
     *
     * @return the sheet
     */
    default Optional<Sheet> getSheet() {
        return getDelegate().getSheet();
    }

    /**
     * Check whether editing is enabled.
     *
     * @return true if this SheetView allows editing.
     */
    default boolean isEditable() {
        return getDelegate().isEditable();
    }

    /**
     * Check editing state.
     *
     * @return true, if a cell is being edited.
     */
    default boolean isEditing() {
        return getDelegate().isEditing();
    }

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
    default boolean setCurrentCell(int rowNum, int colNum) {
        return getDelegate().setCurrentCell(rowNum, colNum);
    }

    /**
     * Enable/disable sheet editing.
     *
     * @param editable true to allow editing
     */
    default void setEditable(boolean editable) {
        getDelegate().setEditable(editable);
    }

    /**
     * Set the grid color.
     *
     * @param gridColor the color for th grid
     */
    default void setGridColor(Color gridColor) {
        getDelegate().setGridColor(gridColor);
    }

    /**
     * Set sheet to display.
     *
     * @param sheet the sheet to display
     */
    default void setSheet(Sheet sheet) {
        getDelegate().setSheet(sheet);
    }

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
    default String getColumnName(int j) {
        return getDelegate().getColumnName(j);
    }

    /**
     * Get name for row.
     *
     * @param i the row number
     * @return the label text
     */
    default String getRowName(int i) {
        return getDelegate().getRowName(i);
    }

    /**
     * Set the column name provider.
     *
     * @param columnNames a function that maps column numbers to column names
     */
    default void setColumnNames(IntFunction<String> columnNames) {
        getDelegate().setColumnNames(columnNames);
    }

    /**
     * Set the row name provider.
     *
     * @param rowNames a function that maps row numbers to row names
     */
    default void setRowNames(IntFunction<String> rowNames) {
        getDelegate().setRowNames(rowNames);
    }

    /**
     * Set the current column number.
     *
     * @param colNum number of column to be set
     */
    default void setCurrentColNum(int colNum) {
        getSheet().ifPresent(sheet -> {
            int rowNum = sheet.getCurrentCell().map(Cell::getRowNumber).orElse(0);
            setCurrentCell(rowNum, colNum);
        });
    }

    /**
     * Set the current row number.
     *
     * @param rowNum number of row to be set
     */
    default void setCurrentRowNum(int rowNum) {
        getSheet().ifPresent(sheet -> {
            int colNum = sheet.getCurrentCell().map(Cell::getColumnNumber).orElse(0);
            setCurrentCell(rowNum, colNum);
        });
    }


    SheetViewDelegate getDelegate();

    void repaintCell(@Nullable Cell cell);

    void updateContent();

    boolean requestFocusInWindow();

    /**
     * Actions for key bindings.
     */
    enum Actions {
        MOVE_UP(view -> view.move(Direction.NORTH)), MOVE_DOWN(view -> view.move(Direction.SOUTH)),
        MOVE_LEFT(view -> view.move(Direction.WEST)), MOVE_RIGHT(view -> view.move(Direction.EAST)),
        PAGE_UP(view -> view.movePage(Direction.NORTH)), PAGE_DOWN(view -> view.movePage(Direction.SOUTH)),
        MOVE_HOME(SheetView::moveHome), MOVE_END(SheetView::moveEnd),
        START_EDITING(SheetView::startEditing), SHOW_SEARCH_DIALOG(SheetView::showSearchDialog),
        COPY(SheetView::copyToClipboard);

        private final Consumer<? super SheetView> action;

        Actions(Consumer<? super SheetView> action) {
            this.action = action;
        }

        public Consumer<? super SheetView> action() {
            return action;
        }
    }

    void copyToClipboard();

    void showSearchDialog();

    void startEditing();

    default void moveHome() {
        getDelegate().moveHome();
    }

    default void moveEnd() {
        getDelegate().moveEnd();
    }

    default void move(Direction direction) {
        getDelegate().move(direction);
    }

    default void movePage(Direction direction) {
        getDelegate().movePage(direction);
    }
}
