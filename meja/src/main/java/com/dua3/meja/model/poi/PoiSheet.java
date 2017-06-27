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
package com.dua3.meja.model.poi;

import java.util.Objects;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import com.dua3.meja.model.AbstractSheet;
import com.dua3.meja.model.Cell;
import com.dua3.meja.util.RectangularRegion;

/**
 *
 * @author axel
 */
public class PoiSheet extends AbstractSheet {

    protected final PoiWorkbook workbook;
    protected org.apache.poi.ss.usermodel.Sheet poiSheet;
    private int firstColumn;
    private int lastColumn;
    private float zoom = 1.0f;
    private int autoFilterRow = -1;
    private float factorWidth = 1;

    protected PoiSheet(PoiWorkbook workbook, org.apache.poi.ss.usermodel.Sheet poiSheet) {
        this.workbook = workbook;
        this.poiSheet = poiSheet;
        init();
    }

    @Override
    public void addMergedRegion(RectangularRegion cells) {
        super.addMergedRegion(cells);
        addMergedRegionToPoiSheet(cells);
    }

    void addMergedRegionToPoiSheet(RectangularRegion cells) {
        CellRangeAddress cra = new CellRangeAddress(cells.getFirstRow(), cells.getLastRow(), cells.getFirstColumn(),
                cells.getLastColumn());
        poiSheet.addMergedRegion(cra);
    }

    @Override
    public void autoSizeColumn(int j) {
        // for streaming implementation, only tracked columns can be autosized!
        if (poiSheet instanceof SXSSFSheet) {
            SXSSFSheet sxssfSheet = (SXSSFSheet) poiSheet;
            if (!sxssfSheet.isColumnTrackedForAutoSizing(j)) {
                return;
            }
        }

        poiSheet.autoSizeColumn(j);
        firePropertyChange(PROPERTY_LAYOUT_CHANGED, null, null);
    }

    @Override
    public void autoSizeColumns() {
        boolean layoutChanged = false;

        // for streaming implementation, only tracked columns can be autosized!
        if (poiSheet instanceof SXSSFSheet) {
            SXSSFSheet sxssfSheet = (SXSSFSheet) poiSheet;
            for (int j = 0; j < getColumnCount(); j++) {
                if (sxssfSheet.isColumnTrackedForAutoSizing(j)) {
                    poiSheet.autoSizeColumn(j);
                    layoutChanged = true;
                }
            }
        } else {
            for (int j = 0; j < getColumnCount(); j++) {
                poiSheet.autoSizeColumn(j);
                layoutChanged = true;
            }
        }

        // inform listeners
        if (layoutChanged) {
            firePropertyChange(PROPERTY_LAYOUT_CHANGED, null, null);
        }
    }

    @Override
    public void clear() {
        // determine sheet number
        int sheetNr = workbook.poiWorkbook.getSheetIndex(poiSheet);
        if (sheetNr >= workbook.getSheetCount()) {
            /*
             * This should never happen as this sheet is part of the workbook.
             */
            throw new IllegalStateException();
        }

        //
        String sheetName = getSheetName();
        workbook.poiWorkbook.removeSheetAt(sheetNr);
        poiSheet = workbook.poiWorkbook.createSheet(sheetName);
        workbook.poiWorkbook.setSheetOrder(sheetName, sheetNr);

        init();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiSheet) {
            return Objects.equals(poiSheet, ((PoiSheet) obj).poiSheet);
        } else {
            return false;
        }
    }

    @Override
    public int getAutoFilterRow() {
        return autoFilterRow;
    }

    @Override
    public PoiCell getCell(int i, int j) {
        return getRow(i).getCell(j);
    }

    @Override
    public int getColumnCount() {
        return lastColumn - firstColumn + 1;
    }

    @Override
    public float getColumnWidth(int col) {
        return poiColumnWidthToPoints(poiSheet.getColumnWidth(col));
    }

    @Override
    public PoiCell getCurrentCell() {
        final int currentRow, currentColumn;
        CellAddress cellRef = poiSheet.getActiveCell();
        if (cellRef != null) {
            currentRow = Math.max(getFirstRowNum(), Math.min(getLastRowNum(), cellRef.getRow()));
            currentColumn = Math.max(getFirstColNum(), Math.min(getLastColNum(), cellRef.getColumn()));
        } else {
            currentRow = poiSheet.getTopRow();
            currentColumn = poiSheet.getLeftCol();
        }
        return getCell(currentRow, currentColumn);
    }

    @Override
    public int getFirstColNum() {
        return firstColumn;
    }

    @Override
    public int getFirstRowNum() {
        return poiSheet.getFirstRowNum();
    }

    @Override
    public int getLastColNum() {
        return lastColumn;
    }

    @Override
    public int getLastRowNum() {
        return poiSheet.getLastRowNum();
    }

    public org.apache.poi.ss.usermodel.Sheet getPoiSheet() {
        return poiSheet;
    }

    @Override
    public PoiRow getRow(int i) {
        org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(i);
        if (poiRow == null) {
            poiRow = poiSheet.createRow(i);
        }
        return new PoiRow(this, poiRow);
    }

    @Override
    public int getRowCount() {
        return 1 + getLastRowNum() - getFirstRowNum();
    }

    @Override
    public float getRowHeight(int rowNum) {
        final org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(rowNum);
        return poiRow == null ? poiSheet.getDefaultRowHeightInPoints() : poiRow.getHeightInPoints();
    }

    @Override
    public String getSheetName() {
        return poiSheet.getSheetName();
    }

    @Override
    public int getSplitColumn() {
        final PaneInformation pi = poiSheet.getPaneInformation();
        return pi == null ? 0 : pi.getVerticalSplitPosition();
    }

    @Override
    public int getSplitRow() {
        final PaneInformation pi = poiSheet.getPaneInformation();
        return pi == null ? 0 : pi.getHorizontalSplitPosition();
    }

    @Override
    public PoiWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public float getZoom() {
        // current POI version has no method for querying the zoom factor
        return zoom;
    }

    @Override
    public int hashCode() {
        return poiSheet.hashCode();
    }

    private float poiColumnWidthToPoints(int poiWidth) {
        return poiWidth * factorWidth;
    }

    private int pointsToPoiColumnWidth(float width) {
        return Math.round(width / factorWidth);
    }

    @Override
    protected void removeMergedRegion(int rowNumber, int columnNumber) {
        super.removeMergedRegion(rowNumber, columnNumber);

        for (int idx = 0; idx < poiSheet.getNumMergedRegions(); idx++) {
            CellRangeAddress cra = poiSheet.getMergedRegion(idx);
            if (cra.isInRange(rowNumber, columnNumber)) {
                poiSheet.removeMergedRegion(idx);
            }
        }
    }

    private void setAutoFilterForPoiRow(org.apache.poi.ss.usermodel.Row poiRow) {
        if (poiRow != null) {
            int rowNumber = poiRow.getRowNum();
            short col1 = poiRow.getFirstCellNum();
            short coln = poiRow.getLastCellNum();
            poiSheet.setAutoFilter(new CellRangeAddress(rowNumber, rowNumber, col1, coln));
        }
    }

    @Override
    public void setAutofilterRow(int rowNumber) {
        if (rowNumber >= 0) {
            org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(rowNumber);
            setAutoFilterForPoiRow(poiRow);
        }
        autoFilterRow = rowNumber;
    }

    /**
     * Update first and last column numbers.
     *
     * @param columnNumber
     */
    void setColumnUsed(int columnNumber) {
        firstColumn = Math.min(firstColumn, columnNumber);
        lastColumn = Math.max(lastColumn, columnNumber);
    }

    @Override
    public void setColumnWidth(int j, float width) {
        poiSheet.setColumnWidth(j, pointsToPoiColumnWidth(width));
        firePropertyChange(PROPERTY_LAYOUT_CHANGED, null, null);
    }

    @Override
    public void setCurrentCell(Cell cell) {
        if (cell.getSheet() != this) {
            throw new IllegalArgumentException("Cannot set cell from another sheet as current cell.");
        }

        Cell old = getCurrentCell();

        ((PoiCell) cell).poiCell.setAsActiveCell();

        firePropertyChange(PROPERTY_ACTIVE_CELL, old, cell);
    }

    @Override
    public void setRowHeight(int i, float height) {
        org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(i);
        if (poiRow == null) {
            poiRow = poiSheet.createRow(i);
        }
        poiRow.setHeightInPoints(height);
        firePropertyChange(PROPERTY_LAYOUT_CHANGED, null, null);
    }

    @Override
    public void setZoom(float zoom) {
        if (zoom <= 0) {
            throw new IllegalArgumentException("Invalid zoom factor: " + zoom);
        }

        if (zoom != this.zoom) {
            float oldZoom = this.zoom;
            this.zoom = zoom;
            // translate zoom factor into percent
            int pmZoom = Math.max(1, Math.round(zoom * 100));
            poiSheet.setZoom(pmZoom);
            firePropertyChange(PROPERTY_ZOOM, oldZoom, zoom);
        }
    }

    @Override
    public void splitAt(int i, int j) {
        poiSheet.createFreezePane(j, i);
        firePropertyChange(PROPERTY_SPLIT, null, null);
    }

    private void init() {
        // update row and column information
        firstColumn = Integer.MAX_VALUE;
        lastColumn = 0;
        for (int i = poiSheet.getFirstRowNum(); i < poiSheet.getLastRowNum() + 1; i++) {
            final org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(i);
            if (poiRow != null) {
                final short firstCellNum = poiRow.getFirstCellNum();
                if (firstCellNum >= 0) {
                    firstColumn = Math.min(firstColumn, firstCellNum);
                }
                final short lastCellNum = (short) (poiRow.getLastCellNum() - 1);
                if (lastCellNum >= 0) {
                    lastColumn = Math.max(lastColumn, lastCellNum);
                }
            }
        }

        if (firstColumn == Integer.MAX_VALUE) {
            firstColumn = 0;
            lastColumn = 0;
        }

        // extract merged regions
        final int numMergedRegions = poiSheet.getNumMergedRegions(); // SLOW in XssfSheet (poi 3.11)
        for (int i = 0; i < numMergedRegions; i++) {
            CellRangeAddress r = poiSheet.getMergedRegion(i);
            final RectangularRegion rr = new RectangularRegion(r.getFirstRow(), r.getLastRow(), r.getFirstColumn(),
                    r.getLastColumn());
            // the merged region is already present in the POI file
            super.addMergedRegion(rr);
        }

        // determine default font size
        short fontSize = getWorkbook().poiWorkbook.getFontAt((short) 0).getFontHeightInPoints();
        factorWidth = fontSize * 0.525f / 256;
    }

}
