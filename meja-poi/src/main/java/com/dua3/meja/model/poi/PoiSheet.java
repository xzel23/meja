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

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.AbstractSheet;
import com.dua3.meja.model.Cell;
import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;

import java.util.Objects;
import java.util.Optional;

/**
 * POI implementation of the {@link com.dua3.meja.model.Sheet} interface.
 */
public class PoiSheet extends AbstractSheet {

    protected final PoiWorkbook workbook;
    protected Sheet poiSheet;
    private int firstColumn;
    private int lastColumn;
    private float zoom = 1.0f;
    private int autoFilterRow = -1;
    private float factorWidth = 1;

    protected PoiSheet(PoiWorkbook workbook, Sheet poiSheet) {
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
        CellRangeAddress cra = new CellRangeAddress(cells.firstRow(), cells.lastRow(), cells.firstColumn(),
                cells.lastColumn());
        poiSheet.addMergedRegion(cra);
    }

    @Override
    public void clear() {
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
    public PoiCell getCell(int i, int j) {
        return getRow(i).getCell(j);
    }

    @Override
    public Optional<PoiCell> getCellIfExists(int i, int j) {
        return getRowIfExists(i).flatMap(row -> row.getCellIfExists(j));
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
    public Optional<PoiCell> getCurrentCell() {
        final int currentRow, currentColumn;
        CellAddress cellRef = poiSheet.getActiveCell();
        if (cellRef != null) {
            currentRow = Math.max(getFirstRowNum(), Math.min(getLastRowNum(), cellRef.getRow()));
            currentColumn = Math.max(firstColumn, Math.min(lastColumn, cellRef.getColumn()));
        } else {
            return Optional.empty();
        }
        return Optional.of(getCell(currentRow, currentColumn));
    }

    @Override
    public int getFirstColNum() {
        return firstColumn;
    }

    @Override
    public int getFirstRowNum() {
        return Math.max(0, poiSheet.getFirstRowNum());
    }

    @Override
    public int getLastColNum() {
        return lastColumn;
    }

    @Override
    public int getLastRowNum() {
        return poiSheet.getLastRowNum();
    }

    /**
     * Get the underlying Apache POI sheet instance.
     * @return the Apache POI sheet used ti store sheet data
     */
    public Sheet getPoiSheet() {
        return poiSheet;
    }

    @Override
    public PoiRow getRow(int i) {
        Row poiRow = poiSheet.getRow(i);
        if (poiRow == null) {
            poiRow = poiSheet.createRow(i);
            rowsAdded(i, i+1);
        }
        return new PoiRow(this, poiRow);
    }

    @Override
    public Optional<PoiRow> getRowIfExists(int i) {
        return Optional.ofNullable(0 <= i && i <= getLastRowNum() ? getRow(i) : null);
    }

    @Override
    public int getRowCount() {
        return 1 + getLastRowNum() - getFirstRowNum();
    }

    @Override
    public float getRowHeight(int rowNum) {
        final Row poiRow = poiSheet.getRow(rowNum);
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

    private void setAutoFilterForPoiRow(@Nullable Row poiRow) {
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
            Row poiRow = poiSheet.getRow(rowNumber);
            setAutoFilterForPoiRow(poiRow);
        }
        autoFilterRow = rowNumber;
    }

    /**
     * Update first and last column numbers.
     *
     * @param columnNumber the column number
     */
    void setColumnUsed(int columnNumber) {
        int first = lastColumn + 1;

        firstColumn = Math.min(firstColumn, columnNumber);
        lastColumn = Math.max(lastColumn, columnNumber);

        int last = lastColumn + 1;
        if (first != last) {
            columnsAdded(first, last);
        }
    }

    @Override
    public void setColumnWidth(int j, float width) {
        poiSheet.setColumnWidth(j, pointsToPoiColumnWidth(width));
        layoutChanged();
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

        ((PoiCell) cell).poiCell.setAsActiveCell();
        activeCellChanged(old, cell);

        return true;
    }

    @Override
    public void setRowHeight(int i, float height) {
        Row poiRow = poiSheet.getRow(i);
        if (poiRow == null) {
            poiRow = poiSheet.createRow(i);
        }
        poiRow.setHeightInPoints(height);
        layoutChanged();
    }

    @Override
    public void setZoom(float zoom) {
        LangUtil.check(zoom > 0, "Invalid zoom factor: %f", zoom);

        if (zoom != this.zoom) {
            float oldZoom = this.zoom;
            this.zoom = zoom;
            // translate zoom factor into percent
            int pmZoom = Math.max(1, Math.round(zoom * 100));
            poiSheet.setZoom(pmZoom);
            zoomChanged(oldZoom, zoom);
        }
    }

    @Override
    public void splitAt(int i, int j) {
        Pair<Integer, Integer> old = Pair.of(getSplitRow(), getSplitColumn());
        Pair<Integer, Integer> newSplit = Pair.of(i,j);
        poiSheet.createFreezePane(j, i);
        splitChanged(old, newSplit);
    }

    private void init() {
        // update row and column information
        firstColumn = Integer.MAX_VALUE;
        lastColumn = -1;
        for (int i = poiSheet.getFirstRowNum(); i < poiSheet.getLastRowNum() + 1; i++) {
            final Row poiRow = poiSheet.getRow(i);
            if (poiRow != null) {
                final short firstCellNum = poiRow.getFirstCellNum();
                if (firstCellNum >= 0) {
                    firstColumn = Math.min(firstColumn, firstCellNum);
                }
                final short lastCellNum = (short) (poiRow.getLastCellNum() - 1);
                if (lastCellNum > lastColumn) {
                    lastColumn = lastCellNum;
                }
            }
        }

        if (firstColumn == Integer.MAX_VALUE) {
            firstColumn = 0;
            lastColumn = -1;
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
        Font font = workbook.getFont(workbook.poiWorkbook.getFontAt(0)).font;
        factorWidth = (float) FontUtil.getInstance().getTextWidth("0", font)/256;
    }

}
