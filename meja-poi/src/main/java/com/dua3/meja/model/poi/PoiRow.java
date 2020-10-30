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

import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import com.dua3.meja.model.AbstractRow;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;

/**
 *
 * @author axel
 */
public final class PoiRow extends AbstractRow {

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
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass() && poiRow.equals(((PoiRow) obj).poiRow);
    }

    @Override
    public PoiCell getCell(int col) {
        org.apache.poi.ss.usermodel.Cell poiCell = poiRow.getCell(col, MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return new PoiCell(this, poiCell);
    }

    @Override
    public PoiCell getCellIfExists(int col) {
        org.apache.poi.ss.usermodel.Cell poiCell = poiRow.getCell(col);
        return poiCell != null ? new PoiCell(this, poiCell) : null;
    }

    @Override
    public int getFirstCellNum() {
        // POI returns -1 for empty rows
        return Math.max(0, poiRow.getFirstCellNum());
    }

    @Override
    public int getLastCellNum() {
        // POI returns -1 for empty rows
        return Math.max(0, poiRow.getLastCellNum())-1;
    }

    @Override
    public int hashCode() {
        return poiRow.hashCode();
    }

    /**
     * Update first and last column numbers.
     *
     * @param columnNumber
     *  the column number
     */
    void setColumnUsed(int columnNumber) {
        getSheet().setColumnUsed(columnNumber);
    }

    @Override
    public PoiSheet getSheet() {
        return (PoiSheet) (super.getSheet());
    }

    @Override
    public PoiWorkbook getWorkbook() {
        return getSheet().getWorkbook();
    }
}
