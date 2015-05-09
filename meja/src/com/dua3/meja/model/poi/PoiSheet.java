/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.model.poi;

import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.MejaHelper;
import com.dua3.meja.util.RectangularRegion;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.table.TableModel;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 * @author axel
 */
public class PoiSheet implements Sheet {

    protected  final PoiWorkbook workbook;
    protected final org.apache.poi.ss.usermodel.Sheet poiSheet;
    private int firstColumn;
    private int lastColumn;
    private List<RectangularRegion> mergedRegions;
    private float zoom = 1.0f;

    protected PoiSheet(PoiWorkbook workbook, org.apache.poi.ss.usermodel.Sheet poiSheet) {
        this.workbook=workbook;
        this.poiSheet = poiSheet;
        update();
    }

    public org.apache.poi.ss.usermodel.Sheet getPoiSheet() {
        return poiSheet;
    }

    @Override
    public PoiWorkbook getWorkbook() {
        return workbook;
    }

    public RectangularRegion getMergedRegion(int rowNum, int colNum) {
        for (RectangularRegion rr : mergedRegions) {
            if (rr.contains(rowNum, colNum)) {
                return rr;
            }
        }
        return null;
    }

    private void update() {
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
        mergedRegions = new ArrayList<>(numMergedRegions);
        for (int i = 0; i < numMergedRegions; i++) {
            CellRangeAddress r = poiSheet.getMergedRegion(i);
            final RectangularRegion rr = new RectangularRegion(r.getFirstRow(), r.getLastRow(), r.getFirstColumn(), r.getLastColumn());
            mergedRegions.add(rr);
        }
    }

    @Override
    public int getNumberOfColumns() {
        return lastColumn - firstColumn+1;
    }

    @Override
    public int getFirstColNum() {
        return firstColumn;
    }

    @Override
    public int getLastColNum() {
        return lastColumn;
    }

    @Override
    public int getFirstRowNum() {
        return poiSheet.getFirstRowNum();
    }

    @Override
    public int getLastRowNum() {
        return poiSheet.getLastRowNum();
    }

    @Override
    public int getNumberOfRows() {
        return 1 + getLastRowNum() - getFirstRowNum();
    }

    @Override
    public String getSheetName() {
        return poiSheet.getSheetName();
    }

    @Override
    public float getColumnWidth(int col) {
        float fontSize = getWorkbook().getDefaultCellStyle().getFont().getSizeInPoints();
        return poiSheet.getColumnWidth(col) * fontSize * 0.6175f / 256;
    }

    @Override
    public float getRowHeight(int rowNum) {
        final org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(rowNum);
        return poiRow == null ? poiSheet.getDefaultRowHeightInPoints() : poiRow.getHeightInPoints();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiSheet) {
            return Objects.equals(poiSheet, ((PoiSheet) obj).poiSheet);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return poiSheet.hashCode();
    }

    @Override
    public TableModel getTableModel() {
        return MejaHelper.getTableModel(this);
    }

    @Override
    public void splitAt(int i, int j) {
        poiSheet.createFreezePane(i + 1, j + 1);
    }

    @Override
    public int getSplitRow() {
        return poiSheet.getPaneInformation().getHorizontalSplitTopRow();
    }

    @Override
    public int getSplitColumn() {
        return poiSheet.getPaneInformation().getVerticalSplitLeftColumn();
    }

    @Override
    public PoiCell getCell(int i, int j) {
        return getRow(i).getCell(j);
    }

    @Override
    public void autoSizeColumn(int j) {
        poiSheet.autoSizeColumn(j);
    }

    @Override
    public void setAutofilterRow(int rowNumber) {
        org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(rowNumber);
        short col1 = poiRow.getFirstCellNum();
        short coln = poiRow.getLastCellNum();
        poiSheet.setAutoFilter(new CellRangeAddress(rowNumber, rowNumber, col1, coln));
    }

    @Override
    public Iterator<Row> iterator() {
        return MejaHelper.createRowIterator(this);
    }

    @Override
    public List<RectangularRegion> getMergedRegions() {
        return Collections.unmodifiableList(mergedRegions);
    }

    @Override
    public void addMergedRegion(RectangularRegion cells) {
        CellRangeAddress cra = new CellRangeAddress(cells.getFirstRow(), cells.getLastRow(), cells.getFirstColumn(), cells.getLastColumn());
        poiSheet.addMergedRegion(cra);
        mergedRegions.add(cells);

        // update cell data
        int spanX = cells.getLastColumn()-cells.getFirstColumn()+1;
        int spanY = cells.getLastRow()-cells.getFirstRow()+1;
        PoiCell topLeftCell = getCell(cells.getFirstRow(), cells.getFirstColumn());
        for (int i=0; i<spanY; i++) {
            for (int j=0; j<spanX; j++) {
                PoiCell cell = getCell(cells.getFirstRow()+i, cells.getFirstColumn()+j);
                cell.addedToMergedRegion(topLeftCell,spanX,spanY);
            }
        }
    }

    @Override
    public NumberFormat getNumberFormat() {
        return getWorkbook().getNumberFormat();
    }

    @Override
    public DateFormat getDateFormat() {
        return getWorkbook().getDateFormat();
    }

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Lock readLock() {
        return lock.readLock();
    }

    @Override
    public Lock writeLock() {
        return lock.writeLock();
    }

    @Override
    public float getZoom() {
        // current POI version has no method for querying the zoom factor
        return zoom;
    }

    @Override
    public void setZoom(float zoom) {
        if (zoom<=0) {
            throw new IllegalArgumentException("Invalid zoom factor: "+zoom);
        }

        this.zoom = zoom;
        // translate zoom factor into fraction (using permille), should be at least 1
        int pmZoom = Math.max(1, Math.round(zoom*1000));
        poiSheet.setZoom(pmZoom, 1000);
    }

    @Override
    public PoiRow getRow(int row) {
        org.apache.poi.ss.usermodel.Row poiRow = poiSheet.getRow(row);
        if (poiRow == null) {
            poiRow = poiSheet.createRow(row);
        }
        return new PoiRow(this, poiRow);
    }

    @Override
    public void copy(Sheet other) {
        for (Row row: other) {
            getRow(row.getRowNumber()).copy(row);
        }
        for (RectangularRegion rr:other.getMergedRegions()) {
            addMergedRegion(rr);
        }
    }

    /**
     * Update first and last column numbers.
     * @param columnNumber
     */
    void setColumnUsed(int columnNumber) {
        firstColumn = Math.min(firstColumn, columnNumber);
        lastColumn = Math.max(lastColumn, columnNumber);
    }

}
