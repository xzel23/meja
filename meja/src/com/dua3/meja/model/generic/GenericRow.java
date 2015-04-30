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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericRow implements Row {

    private final GenericSheet sheet;
    private final List<Cell> cells = new ArrayList<>();
    private final int rowNumber;

    public GenericRow(GenericSheet sheet, int rowNumber) {
        this.sheet = sheet;
        this.rowNumber = rowNumber;
        reserve(sheet.getNumberOfColumns()-1);
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
    public Cell getCell(int col) {
        reserve(col);
        return cells.get(col);
    }

    private void reserve(int col) {
        if (col >= cells.size()) {
            GenericCellStyle cellStyle = getSheet().getWorkbook().getDefaultCellStyle();
            for (int colNum = cells.size(); colNum <= col; colNum++) {
                cells.add(new GenericCell(this, colNum, cellStyle));
            }
            sheet.reserveColumn(col);
        }
    }

    @Override
    public Iterator<Cell> iterator() {
        return cells.iterator();
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
        return cells.size();
    }

    @Override
    public void copy(Row other) {
        for (Cell cell: other) {
            getCell(cell.getColumnNumber()).copy(cell);
        }
    }

}
