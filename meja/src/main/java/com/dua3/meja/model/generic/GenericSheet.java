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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Font;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.RectangularRegion;

/**
 * A generic implementation of {@link Sheet}.
 */
public class GenericSheet
        implements Sheet {

    private static final float DEFAULT_COLUMN_WIDTH = 80f;
    private static final float DEFAULT_ROW_HEIGHT = 12f;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final GenericWorkbook workbook;
    private final String sheetName;
    private final List<GenericRow> rows = new ArrayList<>(4_000);
    private final List<RectangularRegion> mergedRegions = new ArrayList<>();
    private final ArrayList<Float> columnWidth = new ArrayList<>(200);
    private final ArrayList<Float> rowHeight = new ArrayList<>(4_000);
    private int freezeRow;
    private int freezeColumn;
    private int autoFilterRow = -1;
    private int numberOfColumns;
    private int currentRow = 0;
    private int currentColumn = 0;
    private float zoom = 1.0f;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public GenericSheet(GenericWorkbook workbook, String sheetName) {
        this.workbook = workbook;
        this.sheetName = sheetName;
        this.numberOfColumns = 0;
    }

    @Override
    public void addMergedRegion(RectangularRegion cells) {
        // check that all cells are unmerged
        for (RectangularRegion rr : mergedRegions) {
            if (rr.intersects(cells)) {
                throw new IllegalStateException("New merged region overlaps with an existing one.");
            }
        }

        // add to list
        mergedRegions.add(cells);

        // update cell data
        int spanX = cells.getLastColumn() - cells.getFirstColumn() + 1;
        int spanY = cells.getLastRow() - cells.getFirstRow() + 1;
        GenericCell topLeftCell = getCell(cells.getFirstRow(), cells.getFirstColumn());
        for (int i = 0; i < spanY; i++) {
            for (int j = 0; j < spanX; j++) {
                GenericCell cell = getCell(cells.getFirstRow() + i, cells.getFirstColumn() + j);
                cell.addedToMergedRegion(topLeftCell, spanX, spanY);
            }
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
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
        final int n = getColumnCount();

        float[] colWidth = new float[n];
        Arrays.fill(colWidth, 0f);

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
    private float calcCellWidth(Cell cell) {
        // calculate the exact width
        String text = cell.toString();
        Font font = cell.getCellStyle().getFont();
        float width = font.getTextWidth(text);

        // add half the font size as spacing on the sides
        width += font.getSizeInPoints()/2;

        return width;
    }

    void cellStyleChanged(GenericCell cell, Object old, Object arg) {
        PropertyChangeEvent evt = new PropertyChangeEvent(cell, Property.CELL_STYLE.name(), old, arg);
        pcs.firePropertyChange(evt);
    }

    void cellValueChanged(GenericCell cell, Object old, Object arg) {
        PropertyChangeEvent evt = new PropertyChangeEvent(cell, Property.CELL_CONTENT.name(), old, arg);
        pcs.firePropertyChange(evt);
    }

    @Override
    public void clear() {
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
        return getColumnCount() - 1;
    }

    @Override
    public int getLastRowNum() {
        return getRowCount() - 1;
    }

    @Override
    public List<RectangularRegion> getMergedRegions() {
        return Collections.unmodifiableList(mergedRegions);
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

    @Override
    public Lock readLock() {
        return lock.readLock();
    }

    void removeMergedRegion(int rowNumber, int columnNumber) {
        for (int idx = 0; idx < mergedRegions.size(); idx++) {
            RectangularRegion rr = mergedRegions.get(idx);
            if (rr.getFirstRow() == rowNumber && rr.getFirstColumn() == columnNumber) {
                mergedRegions.remove(idx);
                for (int i = rr.getFirstRow(); i <= rr.getLastRow(); i++) {
                    GenericRow row = getRow(i);
                    for (int j = rr.getFirstColumn(); j <= rr.getLastColumn(); j++) {
                        GenericCell cell = row.getCellIfExists(j);
                        if (cell != null) {
                            cell.removedFromMergedRegion();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    private void reserve(int row) {
        for (int rowNum = rows.size(); rowNum <= row; rowNum++) {
            rows.add(new GenericRow(this, rowNum));
        }
    }

    void reserveColumn(int col) {
        numberOfColumns = Math.max(col + 1, numberOfColumns);
    }

    @Override
    public void setAutofilterRow(int i) {
        autoFilterRow = i;
    }

    @Override
    public void setColumnWidth(int j, float width) {
        if (j < columnWidth.size()) {
            if (columnWidth.set(j, width) != width) {
                firePropertyChange(Property.LAYOUT_CHANGED, null, null);
            }
        } else {
            columnWidth.ensureCapacity(j + 1);
            while (columnWidth.size() < j) {
                columnWidth.add(null); // use default width
            }
            columnWidth.add(width);
            firePropertyChange(Property.LAYOUT_CHANGED, null, null);
        }
    }

    @Override
    public void setCurrentCell(Cell cell) {
        if (cell.getSheet() != this) {
            throw new IllegalArgumentException("Cannot set cell from another sheet as current cell.");
        }

        Cell old = getCurrentCell();

        currentRow = cell.getRowNumber();
        currentColumn = cell.getColumnNumber();

        firePropertyChange(Property.ACTIVE_CELL, old, cell);
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
        firePropertyChange(Property.LAYOUT_CHANGED, null, null);
    }

    @Override
    public void setZoom(float zoom) {
        if (zoom <= 0) {
            throw new IllegalArgumentException("Invalid zoom factor: " + zoom);
        }

        if (zoom != this.zoom) {
            float oldZoom = this.zoom;
            this.zoom = zoom;
            firePropertyChange(Property.ZOOM, oldZoom, zoom);
        }
    }

    @Override
    public void splitAt(int i, int j) {
        freezeRow = i;
        freezeColumn = j;
        firePropertyChange(Property.SPLIT, null, null);
    }

    @Override
    public Lock writeLock() {
        return lock.writeLock();
    }

    private <T> void firePropertyChange(Property property, T oldValue, T newValue) {
        pcs.firePropertyChange(property.name(), oldValue, newValue);
    }

}
