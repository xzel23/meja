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

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.util.Cache;
import com.dua3.meja.util.MejaHelper;
import com.dua3.meja.util.RectangularRegion;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author axel
 */
public class PoiRow implements Row {

    protected final PoiSheet sheet;
    protected final org.apache.poi.ss.usermodel.Row poiRow;
    protected final int rowNumber;
    protected final List<RectangularRegion> mergedRegions = new ArrayList<>();

    public PoiRow(PoiSheet sheet, org.apache.poi.ss.usermodel.Row row) {
        this.sheet = sheet;
        this.poiRow = row;
        this.rowNumber = poiRow.getRowNum();
        update();
    }

    private void update() {
        this.mergedRegions.clear();
        for (RectangularRegion r: getSheet().getMergedRegions()) {
            if (r.getFirstRow()<=rowNumber&&rowNumber<=r.getLastRow()) {
                this.mergedRegions.add(r);
            }
        }
    }

    @Override
    public PoiSheet getSheet() {
        return sheet;
    }

    @Override
    public PoiWorkbook getWorkbook() {
        return sheet.getWorkbook();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiRow) {
            return Objects.equals(poiRow, ((PoiRow) obj).poiRow);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return poiRow.hashCode();
    }

    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    @Override
    public Iterator<Cell> iterator() {
        return MejaHelper.createCellIterator(this);
    }

    @Override
    public int getFirstCellNum() {
        return poiRow.getFirstCellNum();
    }

    @Override
    public int getLastCellNum() {
        return poiRow.getLastCellNum()-1;
    }

    RectangularRegion getMergedRegion(int columnIndex) {
        for (RectangularRegion r: mergedRegions) {
            if (r.getFirstColumn()<=columnIndex&&columnIndex<=r.getLastColumn()) {
                return r;
            }
        }
        return null;
    }

    private final Cache<org.apache.poi.ss.usermodel.Cell, PoiCell> cache = new Cache<org.apache.poi.ss.usermodel.Cell, PoiCell>(Cache.Type.WEAK_KEYS) {
        @Override
        protected PoiCell create(org.apache.poi.ss.usermodel.Cell poiCell) {
            return new PoiCell(PoiRow.this, poiCell);
        }
    };


    @Override
    public PoiCell getCell(int col) {
        org.apache.poi.ss.usermodel.Cell poiCell = poiRow.getCell(col);
        if (poiCell == null) {
            poiCell = poiRow.createCell(col);
        }
        return cache.get(poiCell);
    }

    @Override
    public void copy(Row other) {
        for (Cell cell: other) {
            getCell(cell.getColumnNumber()).copy(cell);
        }
    }

    /**
     * Update first and last column numbers.
     * @param columnNumber 
     */
    void setColumnUsed(int columnNumber) {
        sheet.setColumnUsed(columnNumber);
    }

}
