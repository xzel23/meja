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

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Sheet;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * The SheetView interface represents a user interface view of a spreadsheet.
 * Implementing classes define the behaviors and functionality for displaying and interacting with the sheet.
 */
public interface SheetView {

    /**
     * The maximum allowable width for a column in the sheet view, expressed in pixels.
     * This value serves as an upper limit to ensure consistent rendering and layout alignment
     * in the visual representation of the sheet.
     */
    double MAX_COLUMN_WIDTH = 800;

    /**
     * Get the sheet for this view.
     *
     * @return the sheet
     */
    default Sheet getSheet() {
        return getDelegate().getSheet();
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
     * End edit mode for the current cell.
     *
     * @param commit true if the content of the edited cell is to be updated
     */
    void stopEditing(boolean commit);

    /**
     * Retrieves the delegate responsible for handling the sheet view's operations and interactions.
     *
     * @return the SheetViewDelegate that manages various operations for the sheet view
     */
    SheetViewDelegate getDelegate();

    /**
     * Repaints the specified cell in the sheet view.
     *
     * @param cell the cell to repaint.
     */
    void repaintCell(Cell cell);

    /**
     * Refreshes the content of the sheet view. This method is responsible for updating the display
     * to reflect any changes made to the underlying data or structure of the sheet.
     * It ensures that the current state of the sheet is accurately represented in the view.
     */
    void updateContent();

    /**
     * Requests focus for the current view, bringing it to the foreground and
     * ensuring that it is ready to receive user input.
     */
    void focusView();

    /**
     * Retrieve the current locale settings for this sheet view.
     *
     * @return the locale associated with this sheet view
     */
    Locale getLocale();

    /**
     * Retrieves the scale factor for displaying content in the sheet view.
     *
     * @return the display scale as a Scale2f object which represents the scale factors for both X and Y axes.
     */
    Scale2f getDisplayScale();

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

        /**
         * Returns the action associated with this {@code Actions} enum constant.
         * The action is a {@link Consumer} that accepts a {@link SheetView} instance
         * and performs the corresponding operation.
         *
         * @return a {@link Consumer} representing the action for this enum constant
         */
        public Consumer<? super SheetView> action() {
            return action;
        }
    }

    /**
     * Copies the currently selected cell's content to the system clipboard.
     */
    void copyToClipboard();

    /**
     * Displays a search dialog that allows users to search for specific content within the sheet.
     */
    void showSearchDialog();

    /**
     * Initiates the editing mode for the currently selected cell in the sheet.
     * This method prepares the cell for input, allowing the user to modify
     * its content. If the sheet or the current cell is not editable,
     * this method may have no effect.
     */
    void startEditing();

    /**
     * Moves the selection rectangle to the top-left cell in the sheet.
     * This action typically aligns the view to the beginning of the data set.
     */
    default void moveHome() {
        getDelegate().moveHome();
    }

    /**
     * Move the selection rectangle to the bottom right cell of the sheet.
     */
    default void moveEnd() {
        getDelegate().moveEnd();
    }

    /**
     * Moves the view in the specified direction.
     *
     * @param direction the direction to move the view, which can be NORTH, EAST, SOUTH, or WEST
     */
    default void move(Direction direction) {
        getDelegate().move(direction);
    }

    /**
     * Moves the current page in the specified direction.
     *
     * @param direction the direction to move the page, must be one of the following: NORTH, EAST, SOUTH, or WEST.
     */
    default void movePage(Direction direction) {
        getDelegate().movePage(direction);
    }

    /**
     * Defines a rectangular area of cells in a sheet.
     *
     * @param rect        the area in the sheet
     * @param startRow    the start row
     * @param startColumn the start column
     * @param endRow      the end row (exclusive)
     * @param endColumn   the end column (exclusive)
     */
    record SheetArea(com.dua3.utility.math.geometry.Rectangle2f rect, int startRow, int startColumn, int endRow, int endColumn) {
        public static final SheetArea EMPTY = new SheetArea(Rectangle2f.of(0, 0, 0, 0), 0, 0, 0, 0);

        public SheetArea {
            assert startRow <= endRow && startColumn <= endColumn;
        }
    }

    /**
     * Represents the quadrant of a sheet view.
     * A quadrant can be one of the four sections of a sheet view: top-left, top-right, bottom-left, and bottom-right.
     * <p>
     * The quadrant is defined by two boolean properties:
     * <ul>
     * <li>isTop: indicates whether the quadrant is in the top half of the sheet view
     * <li>isLeft: indicates whether the quadrant is in the left half of the sheet view
     * </ul>
     * <p>
     * Each quadrant provides methods to get the start and end positions for rows and columns within the quadrant.
     */
    enum Quadrant {
        TOP_LEFT(true, true),
        TOP_RIGHT(true, false),
        BOTTOM_LEFT(false, true),
        BOTTOM_RIGHT(false, false);

        private final boolean isTop;
        private final boolean isLeft;

        /**
         * Construct a new instance.
         *
         * @param isLeft whether the quadrant is on the left of the split column
         * @param isTop  whether the quadrant is above the split row
         */
        Quadrant(boolean isTop, boolean isLeft) {
            this.isTop = isTop;
            this.isLeft = isLeft;
        }

        /**
         * Returns whether the current quadrant is above the split.
         *
         * @return true if the quadrant is in the top half of the sheet view, false otherwise
         */
        public boolean isTop() {
            return isTop;
        }

        /**
         * Returns whether the quadrant is located left of the split column.
         *
         * @return {@code true} if the quadrant is in the left half of the sheet view, {@code false} otherwise.
         */
        public boolean isLeft() {
            return isLeft;
        }

        /**
         * Returns the start column index (inclusive) based on the given column count and split column.
         *
         * @param columnCount the number of total columns
         * @param splitColumn the index of the split column
         * @return the index of the starting column
         */
        public int startColumn(int columnCount, int splitColumn) {
            return isLeft ? 0 : splitColumn;
        }

        /**
         * Returns the end column index (exclusive) based on the given column count and split column.
         *
         * @param columnCount the total number of columns in the sheet
         * @param splitColumn the column position where the sheet is split
         * @return the end column position
         */
        public int endColumn(int columnCount, int splitColumn) {
            return isLeft ? splitColumn : columnCount;
        }

        /**
         * Returns the start row (inclusive) based on the given row count and split row.
         *
         * @param rowCount the number of rows in the sheet view
         * @param splitRow the split row index of the sheet view
         * @return the starting row position for the quadrant
         */
        public int startRow(int rowCount, int splitRow) {
            return isTop ? 0 : splitRow;
        }

        /**
         * Returns the end row index (exclusive) based on the given row count and split row.
         *
         * @param rowCount the number of rows in the sheet
         * @param splitRow the split row value
         * @return the end row index
         */
        public int endRow(int rowCount, int splitRow) {
            return isTop ? splitRow : rowCount;
        }
    }
}
