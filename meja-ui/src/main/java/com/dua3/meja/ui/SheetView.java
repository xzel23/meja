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
import com.dua3.utility.math.geometry.Scale2f;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author axel
 */
public interface SheetView {

    double MAX_COLUMN_WIDTH = 800;

    /**
     * Get the sheet for this view.
     *
     * @return the sheet
     */
    default Optional<Sheet> getSheet() {
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

    SheetViewDelegate getDelegate();

    void repaintCell(Cell cell);

    void updateContent();

    void focusView();

    Locale getLocale();

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

    enum Quadrant {
        TOP_LEFT(true, true),
        TOP_RIGHT(true, false),
        BOTTOM_LEFT(false, true),
        BOTTOM_RIGHT(false, false);

        private final boolean isTop;
        private final boolean isLeft;

        Quadrant(boolean isTop, boolean isLeft) {
            this.isTop = isTop;
            this.isLeft = isLeft;
        }

        public boolean isTop() {
            return isTop;
        }

        public boolean isLeft() {
            return isLeft;
        }

        public int startColumn(int columnCount, int splitColumn) {
            return isLeft ? 0 : splitColumn;
        }

        public int endColumn(int columnCount, int splitColumn) {
            return isLeft ? splitColumn : columnCount;
        }

        public int startRow(int rowCount, int splitRow) {
            return isTop ? 0 : splitRow;
        }

        public int endRow(int rowCount, int splitRow) {
            return isTop ? splitRow : rowCount;
        }

     }
}
