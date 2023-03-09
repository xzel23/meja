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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.dua3.meja.model.AbstractSheet;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.TextUtil;

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
    private int freezeRow = 0;
    private int freezeColumn = 0;
    private int autoFilterRow = -1;
    private int numberOfColumns;
    private int currentRow = 0;
    private int currentColumn = 0;
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
    public void autoSizeColumn(int j) {
        float colWidth = 0;
        for (Row row : this) {
            Cell cell = row.getCellIfExists(j);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                float width = calcCellWidth(cell);
                colWidth = Math.max(colWidth, width);
            }
        }
        setColumnWidth(j, colWidth);
    }

    @Override
    public void autoSizeColumns() {
        final int n = numberOfColumns;

        float[] colWidth = new float[n];
        Arrays.fill(colWidth, 0.0f);

        for (Row row : this) {
            for (int j = 0; j < n; j++) {
                Cell cell = row.getCellIfExists(j);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    float width = calcCellWidth(cell);
                    colWidth[j] = Math.max(colWidth[j], width);
                }
            }
        }

        for (int j = 0; j < n; j++) {
            setColumnWidth(j, colWidth[j]);
        }
    }

    // helper method used to calculate a cell's width
    private static float calcCellWidth(Cell cell) {
        // calculate the exact width
        String text = cell.toString();
        Font font = cell.getCellStyle().getFont();
        float width = (float) TextUtil.getTextWidth(text, font);

        // add half the font size as spacing on the sides
        width += font.getSizeInPoints() / 2;

        return width;
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
        int firstAddedRow = rows.size();
        int lastAddedRow = -1;
        for (int rowNum = rows.size(); rowNum <= row; rowNum++) {
            rows.add(new GenericRow(this, rowNum));
            lastAddedRow = rowNum;
        }

        if (lastAddedRow >= firstAddedRow) {
            firePropertyChange(PROPERTY_ROWS_ADDED, RowInfo.none(), new RowInfo(firstAddedRow, lastAddedRow));
        }
    }

    void reserveColumn(int col) {
        int oldValue = numberOfColumns;
        numberOfColumns = Math.max(col + 1, numberOfColumns);
        if (numberOfColumns != oldValue) {
            firePropertyChange(PROPERTY_COLUMNS_ADDED, oldValue, numberOfColumns);
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
                firePropertyChange(PROPERTY_LAYOUT_CHANGED, null, null);
            }
        } else {
            columnWidth.ensureCapacity(j + 1);
            while (columnWidth.size() < j) {
                columnWidth.add(null); // use default width
            }
            columnWidth.add(width);
            firePropertyChange(PROPERTY_LAYOUT_CHANGED, null, null);
        }
    }

    @Override
    public void setCurrentCell(Cell cell) {
        //noinspection ObjectEquality
        LangUtil.check(cell.getSheet() == this, "Cannot set cell from another sheet as current cell.");

        Cell old = getCurrentCell();

        currentRow = cell.getRowNumber();
        currentColumn = cell.getColumnNumber();

        firePropertyChange(PROPERTY_ACTIVE_CELL, old, cell);
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
        firePropertyChange(PROPERTY_LAYOUT_CHANGED, null, null);
    }

    @Override
    public void setZoom(float zoom) {
        LangUtil.check(zoom > 0, "Invalid zoom factor: %f", zoom);

        if (zoom != this.zoom) {
            float oldZoom = this.zoom;
            this.zoom = zoom;
            firePropertyChange(PROPERTY_ZOOM, oldZoom, zoom);
        }
    }

    @Override
    public void splitAt(int i, int j) {
        freezeRow = i;
        freezeColumn = j;
        firePropertyChange(PROPERTY_SPLIT, null, null);
    }

}
