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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A generic implementation of {@link Sheet}.
 */
public class GenericSheet extends AbstractSheet {

    private static final float DEFAULT_COLUMN_WIDTH = 80.0f;
    private static final float DEFAULT_ROW_HEIGHT = 12.0f;

    private final GenericWorkbook workbook;
    private final String sheetName;
    private final List<GenericRow> rows = new ArrayList<>(4_000);
    private final ArrayList<Float> columnWidth = new ArrayList<>(200);
    private final ArrayList<Float> rowHeight = new ArrayList<>(4_000);
    private int freezeRow;
    private int freezeColumn;
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
        rows.clear();
        copy(new GenericSheet(workbook, sheetName));
    }

    @Override
    public int getAutoFilterRow() {
        return autoFilterRow;
    }

    @Override
    public GenericCell getCell(int i, int j) {
        return getRow(i).getCell(j);
    }

    @Override
    public Optional<GenericCell> getCellIfExists(int i, int j) {
        return getRowIfExists(i).flatMap(row -> row.getCellIfExists(j));
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
    public Optional<GenericCell> getCurrentCell() {
        return getCellIfExists(currentRow, currentColumn);
    }

    @Override
    public int getFirstColNum() {
        return 0;
    }

    @Override
    public int getFirstRowNum() {
        return 0;
    }

    @Override
    public int getLastColNum() {
        return numberOfColumns - 1;
    }

    @Override
    public int getLastRowNum() {
        return getRowCount() - 1;
    }

    @Override
    public GenericRow getRow(int row) {
        reserve(row);
        return rows.get(row);
    }

    public Optional<GenericRow> getRowIfExists(int i) {
        return Optional.ofNullable(i <= getLastRowNum() ? getRow(i) : null);
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
        return freezeColumn;
    }

    @Override
    public int getSplitRow() {
        return freezeRow;
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

    void reserveColumn(int col) {
        int oldValue = numberOfColumns;
        numberOfColumns = Math.max(col + 1, numberOfColumns);
        if (numberOfColumns != oldValue) {
            columnsAdded(oldValue, numberOfColumns);
        }
    }

    @Override
    public void setAutofilterRow(int i) {
        autoFilterRow = i;
    }

    @Override
    public void setColumnWidth(int j, float width) {
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
        //noinspection ObjectEquality
        LangUtil.check(cell.getSheet() == this, "Cannot set cell from another sheet as current cell.");

        Cell old = getCurrentCell().orElse(null);

        cell = cell.getLogicalCell();
        if (cell == old) {
            return false;
        }

        currentRow = cell.getRowNumber();
        currentColumn = cell.getColumnNumber();

        activeCellChanged(old, cell);
        return true;
    }

    @Override
    public void setRowHeight(int i, float height) {
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
        LangUtil.check(zoom > 0, "Invalid zoom factor: %f", zoom);

        if (zoom != this.zoom) {
            float oldZoom = this.zoom;
            this.zoom = zoom;
            zoomChanged(oldZoom, zoom);
        }
    }

    @Override
    public void splitAt(int i, int j) {
        Pair<Integer, Integer> oldSplit = Pair.of(getSplitRow(),  getSplitColumn());
        freezeRow = i;
        freezeColumn = j;
        splitChanged(oldSplit, Pair.of(i, j));
    }

}
