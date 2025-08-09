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
package com.dua3.meja.model.generic;

import com.dua3.meja.model.AbstractSheet;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A generic implementation of {@link Sheet}.
 */
public class GenericSheet extends AbstractSheet<GenericSheet, GenericRow, GenericCell> {
    private static final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(GenericSheet.class);

    private static final float DEFAULT_COLUMN_WIDTH = 80.0f;
    private static final float DEFAULT_ROW_HEIGHT = 12.0f;

    private final GenericWorkbook workbook;
    private final String sheetName;
    private final List<GenericRow> rows = new ArrayList<>(4_000);
    private final ArrayList<@Nullable Float> columnWidth = new ArrayList<>(200);
    private final ArrayList<@Nullable Float> rowHeight = new ArrayList<>(4_000);
    private int splitRow;
    private int splitColumn;
    private int autoFilterRow = -1;
    private int numberOfColumns;
    private int currentRow;
    private int currentColumn;
    private float zoom = 1.0f;

    /**
     * Constructor
     *
     * @param workbook  the workbook to add this sheet to
     * @param sheetName the sheet name
     */
    public GenericSheet(GenericWorkbook workbook, String sheetName) {
        this.workbook = workbook;
        this.sheetName = sheetName;
        this.numberOfColumns = 0;
    }

    @Override
    public void clear() {
        LOG.trace("clearing the sheet");

        rows.clear();
        copy(new GenericSheet(workbook, sheetName));
    }

    @Override
    public int getAutoFilterRow() {
        return autoFilterRow;
    }

    @Override
    public int getColumnCount() {
        return numberOfColumns;
    }

    @Override
    public float getColumnWidth(int j) {
        Float width = j < columnWidth.size() ? columnWidth.get(j) : null;
        return width != null ? width : DEFAULT_COLUMN_WIDTH;
    }

    @Override
    public GenericCell getCurrentCell() {
        return getCell(currentRow, currentColumn);
    }

    @Override
    public GenericRow getRow(int row) {
        reserve(row);
        return rows.get(row);
    }

    public Optional<GenericRow> getRowIfExists(int i) {
        return Optional.ofNullable(0 <= i && i < getRowCount() ? getRow(i) : null);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public float getRowHeight(int i) {
        Float height = i < rowHeight.size() ? rowHeight.get(i) : null;
        return height != null ? height : DEFAULT_ROW_HEIGHT;
    }

    @Override
    public String getSheetName() {
        return sheetName;
    }

    @Override
    public int getSplitColumn() {
        return splitColumn;
    }

    @Override
    public int getSplitRow() {
        return splitRow;
    }

    @Override
    public GenericWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public float getZoom() {
        return zoom;
    }

    private void reserve(int row) {
        int first = rows.size();
        for (int rowNum = rows.size(); rowNum <= row; rowNum++) {
            rows.add(new GenericRow(this, rowNum));
        }
        int last = rows.size();

        if (last > first) {
            rowsAdded(first, last);
        }
    }

    /**
     * Ensures the sheet has enough columns allocated to accommodate the specified column number.
     * If necessary, expands the sheet and notifies listeners about the added columns.
     *
     * @param col the column number to reserve space for
     */
    void reserveColumn(int col) {
        int oldValue = numberOfColumns;
        numberOfColumns = Math.max(col + 1, numberOfColumns);
        if (numberOfColumns != oldValue) {
            columnsAdded(oldValue, numberOfColumns);
        }
    }

    @Override
    public void setAutofilterRow(int i) {
        LOG.trace("setting auto filter row {}", i);
        LangUtil.check(i >= 0, "Invalid row number: %d", i);
        autoFilterRow = i;
    }

    @Override
    public void setColumnWidth(int j, float width) {
        LOG.trace("setting column width of column {} to {}", j, width);
        LangUtil.check(width >= 0, "Invalid column width: %f", width);

        if (j < columnWidth.size()) {
            if (!Objects.equals(columnWidth.set(j, width), width)) { // use Objects.equals to avoid NPE!
                layoutChanged();
            }
        } else {
            columnWidth.ensureCapacity(j + 1);
            while (columnWidth.size() < j) {
                columnWidth.add(null); // use default width
            }
            columnWidth.add(width);
            layoutChanged();
        }
    }

    @Override
    public boolean setCurrentCell(Cell cell) {
        LOG.trace("setting current cell to {}", cell::getCellRef);

        LangUtil.checkArg(cell.getSheet() == this, "cell  belongs to another sheet");

        Cell old = getCurrentCell();
        if (cell == old) {
            return false;
        }

        currentRow = cell.getRowNumber();
        currentColumn = cell.getColumnNumber();

        activeCellChanged(old, cell.getLogicalCell());

        return true;
    }

    @Override
    public void setRowHeight(int i, float height) {
        LOG.trace("setting row height of row {} to {}", i, height);
        LangUtil.check(height >= 0, "Invalid row height: %f", height);

        if (i < rowHeight.size()) {
            rowHeight.set(i, height);
        } else {
            rowHeight.ensureCapacity(i + 1);
            while (rowHeight.size() < i) {
                rowHeight.add(null); // use default width
            }
            rowHeight.add(height);
        }
        layoutChanged();
    }

    @Override
    public void setZoom(float zoom) {
        LOG.trace("setting zoom to {}", zoom);
        LangUtil.check(zoom > 0, "Invalid zoom factor: %f", zoom);

        if (zoom != this.zoom) {
            float oldZoom = this.zoom;
            this.zoom = zoom;
            zoomChanged(oldZoom, zoom);
        }
    }

    @Override
    public void splitAt(int i, int j) {
        LOG.trace("setting split to ({}, {})", i, j);
        LangUtil.check(i >= 0 && j >= 0, "Invalid split position: (%d, %d)", i, j);

        Pair<Integer, Integer> oldSplit = Pair.of(getSplitRow(), getSplitColumn());
        splitRow = i;
        splitColumn = j;
        splitChanged(oldSplit, Pair.of(i, j));
    }

    @Override
    public float getDefaultRowHeight() {
        return DEFAULT_ROW_HEIGHT;
    }

    @Override
    public float getDefaultColumnWidth() {
        return DEFAULT_COLUMN_WIDTH;
    }

    /**
     * Marks a column as being in use and expands the sheet if necessary.
     * If the column number is beyond the current sheet bounds, the sheet is expanded
     * and listeners are notified about the added columns.
     *
     * @param columnNumber the column number to mark as used
     */
    void setColumnUsed(int columnNumber) {
        if (columnNumber > numberOfColumns - 1) {
            int oldLast = getColumnCount() - 1;
            numberOfColumns = columnNumber + 1;
            columnsAdded(oldLast, numberOfColumns);
        }
    }
}
