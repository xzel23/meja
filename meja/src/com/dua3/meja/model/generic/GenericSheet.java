/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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

import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Helper;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableModel;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class GenericSheet implements Sheet {

    private final GenericWorkbook workbook;
    private String sheetName;
    private final List<GenericRow> rows = new ArrayList<>();

    public GenericSheet(GenericWorkbook workbook, String sheetName) {
        this.workbook = workbook;
        this.sheetName = sheetName;
    }

    @Override
    public TableModel getTableModel() {
        return Helper.getTableModel(this);
    }

    @Override
    public String getSheetName() {
        return sheetName;
    }

    @Override
    public int getFirstColNum() {
        return 0;
    }

    @Override
    public int getFirstRowNum() {
        return 0;
    }

    @Override
    public int getLastColNum() {
        return getNumberOfColumns()-1;
    }

    @Override
    public int getLastRowNum() {
        return getNumberOfRows()-1;
    }

    @Override
    public int getNumberOfColumns() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getNumberOfRows() {
        return rows.size();
    }

    @Override
    public double getColumnWidth(int colNum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getRowHeight(int rowNum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CellStyle getDefaultCellStyle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Row getRow(int row) {
        reserve(row);
        return rows.get(row);
    }

    private void reserve(int row) {
        for (int rowNum=rows.size(); rowNum<=row; rowNum++) {
            rows.add(new GenericRow(this));
        }
    }

    @Override
    public Workbook getWorkbook() {
        return workbook;
    }

}
