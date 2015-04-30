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
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.MejaHelper;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.table.TableModel;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericSheet implements Sheet {

    private final GenericWorkbook workbook;
    private String sheetName;
    private final Locale locale;
    private final List<GenericRow> rows = new ArrayList<>();
    private int freezeRow;
    private int freezeColumn;
    private int autoFilterRow;
    private int numberOfColumns;
    private float zoom=1.0f;

    public GenericSheet(GenericWorkbook workbook, String sheetName, Locale locale) {
        this.workbook = workbook;
        this.sheetName = sheetName;
        this.locale = locale;
        this.numberOfColumns=0;
    }

    @Override
    public TableModel getTableModel() {
        return MejaHelper.getTableModel(this);
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
        return numberOfColumns;
    }

    @Override
    public int getNumberOfRows() {
        return rows.size();
    }

    @Override
    public float getColumnWidth(int colNum) {
        // TODO
        return 80f;
    }

    @Override
    public float getRowHeight(int rowNum) {
        // TODO
        return 12f;
    }

    @Override
    public Row getRow(int row) {
        reserve(row);
        return rows.get(row);
    }

    private void reserve(int row) {
        for (int rowNum=rows.size(); rowNum<=row; rowNum++) {
            rows.add(new GenericRow(this, rowNum));
        }
    }

    @Override
    public GenericWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public Cell getCell(int i, int j) {
        return getRow(i).getCell(j);
    }

    @Override
    public void splitAt(int i, int j) {
        freezeRow=i;
        freezeColumn=j;
    }

    @Override
    public int getSplitRow() {
		return freezeRow;
	}

    @Override
    public int getSplitColumn() {
		return freezeColumn;
	}

    @Override
    public void autoSizeColumn(int j) {
        // TODO
    }

    @Override
    public void setAutofilterRow(int i) {
        autoFilterRow=i;
    }

    public int getAutoFilterRow() {
		return autoFilterRow;
	}

    @Override
    public Iterator<Row> iterator() {
        return MejaHelper.createRowIterator(this);
    }

    void reserveColumn(int col) {
        numberOfColumns=Math.max(col+1, numberOfColumns);
    }

    @Override
    public NumberFormat getNumberFormat() {
        return NumberFormat.getInstance(locale);
    }

    @Override
    public DateFormat getDateFormat() {
        return DateFormat.getDateInstance(DateFormat.SHORT, locale);
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
        return zoom;
    }

    @Override
    public void setZoom(float zoom) {
        if (zoom<=0) {
            throw new IllegalArgumentException("Invalid zoom factor: "+zoom);
        }

        this.zoom=zoom;
    }

    @Override
    public void copy(Sheet other) {
        for (Row row: other) {
            getRow(row.getRowNumber()).copy(row);
        }
    }

}
