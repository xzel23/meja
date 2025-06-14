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

import com.dua3.meja.model.AbstractRow;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Concrete Implementation of {@link AbstractRow} for Apache POI based workbooks.
 */
public final class PoiRow extends AbstractRow<PoiSheet, PoiRow, PoiCell> {

    /**
     *
     */
    final org.apache.poi.ss.usermodel.Row poiRow;

    /**
     * Construct row from existing POI row instance.
     *
     * @param sheet the sheet
     * @param row   the row
     */
    public PoiRow(PoiSheet sheet, org.apache.poi.ss.usermodel.Row row) {
        super(sheet, row.getRowNum());
        this.poiRow = row;
    }

    @Override
    public void copy(Row other) {
        for (Cell cell : other) {
            getCell(cell.getColumnNumber()).copy(cell);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj != null && getClass() == obj.getClass() && poiRow.equals(((PoiRow) obj).poiRow);
    }

    @Override
    public PoiCell getCell(int j) {
        org.apache.poi.ss.usermodel.Cell poiCell;
        int oldLast = getLastCellNum();
        if (j > oldLast) {
            int jj = oldLast;
            do {
                poiCell = poiRow.createCell(++jj);
            } while (jj < j);
            getSheet().setColumnUsed(j);
        } else {
            poiCell = poiRow.getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
        }

        return new PoiCell(this, poiCell);
    }

    @Override
    public Optional<PoiCell> getCellIfExists(int col) {
        org.apache.poi.ss.usermodel.Cell poiCell = col < 0 ? null : poiRow.getCell(col);
        return Optional.ofNullable(poiCell != null ? new PoiCell(this, poiCell) : null);
    }

    @Override
    public int getFirstCellNum() {
        // POI returns -1 for empty rows
        return Math.max(0, poiRow.getFirstCellNum());
    }

    @Override
    public int getLastCellNum() {
        // POI returns -1 for empty rows
        return Math.max(0, poiRow.getLastCellNum()) - 1;
    }

    @Override
    public int hashCode() {
        return poiRow.hashCode();
    }

    /**
     * Update first and last column numbers.
     *
     * @param columnNumber the column number
     */
    void setColumnUsed(int columnNumber) {
        getSheet().setColumnUsed(columnNumber);
    }

    /**
     * Broadcast event: columns added.
     *
     * @param first the index (inclusive) of the first added column
     * @param last the index (exclusive) of the last added column
     */
    private void columnsAdded(int first, int last) {
        getSheet().setColumnUsed(last);
    }

    @Override
    public PoiWorkbook getWorkbook() {
        return getSheet().getWorkbook();
    }
}
