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

import com.dua3.meja.model.AbstractRow;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.util.IteratorAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

/**
 * Implementation of the {@link Row} interface for {@link GenericSheet}.
 */
public class GenericRow extends AbstractRow<GenericSheet, GenericRow, GenericCell> {

    private final ArrayList<GenericCell> cells;

    /**
     * Construct a new {@code GenericRow}.
     *
     * @param sheet     the sheet the row belongs to
     * @param rowNumber the row number
     */
    public GenericRow(GenericSheet sheet, int rowNumber) {
        super(sheet, rowNumber);
        this.cells = new ArrayList<>(sheet.getColumnCount());
    }

    @Override
    public void copy(Row other) {
        for (Cell cell : other) {
            getCell(cell.getColumnNumber()).copy(cell);
        }
    }

    @Override
    public GenericCell getCell(int col) {
        int added = reserve(col);
        GenericCell cell = cells.get(col);
        if (added > 0) {
            getSheet().setColumnUsed(col);
        }
        return cell;
    }

    @Override
    public Optional<GenericCell> getCellIfExists(int col) {
        return Optional.ofNullable(0 <= col && col < cells.size() ? cells.get(col) : null);
    }

    @Override
    public int getFirstCellNum() {
        return 0;
    }

    @Override
    public int getLastCellNum() {
        return cells.size() - 1;
    }

    @Override
    public Iterator<Cell> iterator() {
        return new IteratorAdapter<>(cells.iterator());
    }

    private int reserve(int col) {
        int n = Math.max (0, col - cells.size() + 1);
        if (n > 0) {
            GenericCellStyle cellStyle = getSheet().getWorkbook().getDefaultCellStyle();
            cells.ensureCapacity(col + 1);
            for (int colNum = cells.size(); colNum <= col; colNum++) {
                cells.add(new GenericCell(this, colNum, cellStyle));
            }
            getSheet().reserveColumn(col);
        }
        return n;
    }

    @Override
    public GenericWorkbook getWorkbook() {
        return getSheet().getWorkbook();
    }
}
