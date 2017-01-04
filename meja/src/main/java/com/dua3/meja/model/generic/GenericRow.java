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
package com.dua3.meja.model.generic;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.util.IteratorAdapter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericRow implements Row {

    private final GenericSheet sheet;
    private final ArrayList<GenericCell> cells;
    private final int rowNumber;

    /**
     * Construct a new {@code GenericRow}.
     * @param sheet the sheet the row belongs to
     * @param rowNumber the row number
     */
    public GenericRow(GenericSheet sheet, int rowNumber) {
        this.sheet = sheet;
        this.rowNumber = rowNumber;
        this.cells = new ArrayList<>(sheet.getColumnCount());
    }

    @Override
    public GenericWorkbook getWorkbook() {
        return sheet.getWorkbook();
    }

    @Override
    public GenericSheet getSheet() {
        return sheet;
    }

    @Override
    public GenericCell getCell(int col) {
        reserve(col);
        return cells.get(col);
    }

    @Override
    public GenericCell getCellIfExists(int col) {
        return col < cells.size() ? cells.get(col) : null;
    }
    
    private void reserve(int col) {
        if (col >= cells.size()) {
            GenericCellStyle cellStyle = getSheet().getWorkbook().getDefaultCellStyle();
            cells.ensureCapacity(col+1);
            for (int colNum = cells.size(); colNum <= col; colNum++) {
                cells.add(new GenericCell(this, colNum, cellStyle));
            }
            sheet.reserveColumn(col);
        }
    }

    @Override
    public Iterator<Cell> iterator() {
        return new IteratorAdapter<>(cells.iterator());
    }

    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    @Override
    public int getFirstCellNum() {
        return 0;
    }

    @Override
    public int getLastCellNum() {
        return cells.size()-1;
    }

    @Override
    public void copy(Row other) {
        for (Cell cell: other) {
            getCell(cell.getColumnNumber()).copy(cell);
        }
    }

}