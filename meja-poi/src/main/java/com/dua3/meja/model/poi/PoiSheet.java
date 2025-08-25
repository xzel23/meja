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

import com.dua3.meja.model.AbstractSheet;
import com.dua3.meja.model.Cell;
import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * POI implementation of the {@link com.dua3.meja.model.Sheet} interface.
 */
public final class PoiSheet extends AbstractSheet<PoiSheet, PoiRow, PoiCell> {
    private static final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(PoiSheet.class);

    /**
     * Represents the underlying workbook associated with this sheet instance.
     * <p>
     * Note: This member is specific to the {@link PoiSheet} class and its lifecycle
     * is managed in conjunction with the {@link PoiWorkbook} instance provided during
     * the {@link PoiSheet} construction.
     */
    private final PoiWorkbook workbook;
    /**
     * Represents the Apache POI sheet instance encapsulated within the {@link PoiSheet}.
     * <p>
     * This variable holds a reference to the underlying Apache POI-specific {@link Sheet}
     * used to handle low-level operations and interactions with the Excel sheet.
     * <p>
     * It acts as the bridge for executing operations directly on the underlying Excel sheet,
     * such as merging cells, retrieving rows, setting zoom levels, and other sheet-level
     * configurations.
     * <p>
     * The usage of this variable is limited to internal operations within the {@link PoiSheet}
     * class and methods that require access to the raw POI sheet data structure.
     */
    private Sheet poiSheet;
    private int lastColumn;
    private float zoom = 1.0f;
    private int autoFilterRow = -1;

    /**
     * Constructor.
     *
     * @param workbook the {@link PoiWorkbook} the sheet belongs to
     * @param poiSheet the {@link Sheet}
     */
    PoiSheet(PoiWorkbook workbook, Sheet poiSheet) {
        this.workbook = workbook;
        this.poiSheet = poiSheet;
        init();
    }

    @Override
    public void addMergedRegion(RectangularRegion cells) {
        super.addMergedRegion(cells);
        addMergedRegionToPoiSheet(cells);
    }

    /**
     * Updates the underlying POI sheet to reflect a merged region.
     * Converts the Meja RectangularRegion to POI's CellRangeAddress format
     * and adds it to the POI sheet's merged regions.
     *
     * @param cells the rectangular region of cells to merge
     */
    void addMergedRegionToPoiSheet(RectangularRegion cells) {
        CellRangeAddress cra = new CellRangeAddress(cells.firstRow(), cells.lastRow(), cells.firstColumn(),
                cells.lastColumn());
        poiSheet.addMergedRegion(cra);
    }

    @Override
    public void clear() {
        LOG.trace("clearing the sheet");

        // determine sheet number
        int sheetNr = workbook.poiWorkbook.getSheetIndex(poiSheet);

        // This should never happen as this sheet is part of the workbook.
        assert sheetNr < workbook.getSheetCount();

        //
        String sheetName = getSheetName();
        workbook.poiWorkbook.removeSheetAt(sheetNr);
        poiSheet = workbook.poiWorkbook.createSheet(sheetName);
        workbook.poiWorkbook.setSheetOrder(sheetName, sheetNr);

        init();

        layoutChanged();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof PoiSheet other && Objects.equals(poiSheet, other.poiSheet);
    }

    @Override
    public int getAutoFilterRow() {
        return autoFilterRow;
    }

    @Override
    public int getColumnCount() {
        return lastColumn + 1;
    }

    @Override
    public float getColumnWidth(int colIndex) {
        return poiColumnWidthToPoints(poiSheet.getColumnWidth(colIndex));
    }

    @Override
    protected PoiCell getCurrentAbstractCell() {
        CellAddress cellRef = poiSheet.getActiveCell();

        if (cellRef == null) {
            return getAbstractCell(0, 0);
        }

        return getAbstractCell(cellRef.getRow(), cellRef.getColumn());
    }

    /**
     * Get the underlying Apache POI sheet instance.
     * @return the Apache POI sheet used to store sheet data
     */
    public Sheet getPoiSheet() {
        return poiSheet;
    }

    @Override
    protected PoiRow getAbstractRow(int rowIndex) {
        Row poiRow = poiSheet.getRow(rowIndex);
        if (poiRow == null) {
            int oldLast = getRowCount() - 1;
            int added = rowIndex - oldLast;
            poiRow = poiSheet.createRow(rowIndex);
            if (added > 0) {
                rowsAdded(oldLast + 1, oldLast + added + 1);
            }
        }
        return new PoiRow(this, poiRow);
    }

    @Override
    public int getRowCount() {
        return poiSheet.getLastRowNum() + 1;
    }

    @Override
    public float getRowHeight(int rowIndex) {
        final Row poiRow = poiSheet.getRow(rowIndex);
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
    protected PoiWorkbook getAbstractWorkbook() {
        return workbook;
    }

    @Override
    public float getZoom() {
        // current POI version has no method for querying the zoom factor
        return zoom;
    }

    @Override
    public int hashCode() {
        //noinspection NonFinalFieldReferencedInHashCode
        return poiSheet.hashCode();
    }

    private float poiColumnWidthToPoints(int poiWidth) {
        return poiWidth * workbook.getFactorWidth();
    }

    private int pointsToPoiColumnWidth(float width) {
        return Math.round(width / workbook.getFactorWidth());
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

    @Override
    public void setAutofilterRow(int rowIndex) {
        LOG.trace("setting auto filter row {}", rowIndex);
        LangUtil.check(rowIndex >= 0, "Invalid row number: %d", rowIndex);

        int rowNumber1 = getAbstractRow(rowIndex).poiRow.getRowNum();
        short col1 = getAbstractRow(rowIndex).poiRow.getFirstCellNum();
        short coln = getAbstractRow(rowIndex).poiRow.getLastCellNum();
        poiSheet.setAutoFilter(new CellRangeAddress(rowNumber1, rowNumber1, col1, coln));

        autoFilterRow = rowIndex;
    }

    /**
     * Update first and last column numbers.
     *
     * @param columnNumber the column number
     */
    void setColumnUsed(int columnNumber) {
        int first = lastColumn + 1;

        lastColumn = Math.max(lastColumn, columnNumber);

        int last = lastColumn + 1;
        if (first != last) {
            columnsAdded(first, last);
            for (int j = first; j < last; j++) {
                setColumnWidth(j, getDefaultColumnWidth());
            }
        }
    }

    @Override
    public void setColumnWidth(int colIndex, float width) {
        LOG.trace("setting column width of column {} to {}", colIndex, width);
        LangUtil.check(width >= 0, "Invalid column width: %f", width);

        poiSheet.setColumnWidth(colIndex, pointsToPoiColumnWidth(width));
        layoutChanged();
    }

    @Override
    public boolean setCurrentCell(Cell cell) {
        LOG.trace("setting current cell to {}", cell::getCellRef);

        LangUtil.checkArg(cell.getSheet() == this, "cell  belongs to another sheet");

        Cell old = getCurrentCell();
        if (cell == old) {
            return false;
        }

        if (cell instanceof PoiCell poiCell) {
            poiCell.poiCell.setAsActiveCell();
            activeCellChanged(old, cell.getLogicalCell());
        } else {
            poiSheet.setActiveCell(null);
            activeCellChanged(old, null);
        }

        return true;
    }

    @Override
    public void setRowHeight(int rowIndex, float height) {
        LOG.trace("setting row height of row {} to {}", rowIndex, height);
        LangUtil.check(height >= 0, "Invalid row height: %f", height);

        Row poiRow = poiSheet.getRow(rowIndex);
        if (poiRow == null) {
            poiRow = poiSheet.createRow(rowIndex);
        }
        poiRow.setHeightInPoints(height);
        layoutChanged();
    }

    @Override
    public void setZoom(float zoom) {
        LOG.trace("setting zoom to {}", zoom);
        LangUtil.check(zoom > 0, "Invalid zoom factor: %f", zoom);

        if (zoom == this.zoom) {
            LOG.trace("zoom does not change");
        } else {
            float oldZoom = this.zoom;
            this.zoom = zoom;

            LOG.trace("changing zoom from {} to {}", oldZoom, zoom);

            // translate zoom factor into percent
            int pmZoom = Math.max(1, Math.round(zoom * 100));
            poiSheet.setZoom(pmZoom);
            zoomChanged(oldZoom, zoom);
        }
    }

    @Override
    public void splitAt(int rowIndex, int colIndex) {
        LOG.trace("setting split to ({}, {})", rowIndex, colIndex);
        LangUtil.check(rowIndex >= 0 && colIndex >= 0, "Invalid split position: (%d, %d)", rowIndex, colIndex);

        Pair<Integer, Integer> old = Pair.of(getSplitRow(), getSplitColumn());
        Pair<Integer, Integer> newSplit = Pair.of(rowIndex, colIndex);
        poiSheet.createFreezePane(colIndex, rowIndex);
        splitChanged(old, newSplit);
    }

    @Override
    public float getDefaultRowHeight() {
        return poiSheet.getDefaultRowHeightInPoints();
    }

    @Override
    public float getDefaultColumnWidth() {
        // unit is number of characters and 7 is a common character width in points
        return poiSheet.getDefaultColumnWidth() * 7.0f;
    }

    private void init() {
        // update row and column information
        lastColumn = -1;
        for (int i = poiSheet.getFirstRowNum(); i < poiSheet.getLastRowNum() + 1; i++) {
            final Row poiRow = poiSheet.getRow(i);
            if (poiRow != null) {
                lastColumn = Math.max(lastColumn, poiRow.getLastCellNum() - 1);
            }
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
    }

}
