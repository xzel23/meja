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

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.util.AttributedStringHelper;
import java.text.AttributedString;
import java.util.Date;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
class GenericCell implements Cell {

    private final GenericRow row;
    private CellType type;
    private Object value;
    private GenericCellStyle cellStyle;
    private int spanX;
    private int spanY;
    private GenericCell logicalCell;
    private final int columnNumber;

    public GenericCell(GenericRow row, int colNumber, GenericCellStyle cellStyle) {
        this.row = row;
        this.logicalCell = this;
        this.cellStyle = cellStyle;
        this.columnNumber = colNumber;
        this.spanX = 1;
        this.spanY = 1;
        this.type = CellType.BLANK;
        this.value = null;
    }

    @Override
    public CellType getCellType() {
        return type;
    }

    @Override
    public boolean getBoolean() {
        if (type == CellType.BOOLEAN) {
            return (boolean) value;
        }
        throw new IllegalStateException("Cannot get boolean value from cell of type " + type.name() + ".");
    }

    @Override
    public String getFormula() {
        if (type == CellType.FORMULA) {
            return (String) value;
        }
        throw new IllegalStateException("Cannot get formula from cell of type " + type.name() + ".");
    }

    @Override
    public Date getDate() {
        throw new IllegalStateException("Cannot get boolean value from cell of type " + type.name() + ".");
    }

    @Override
    public Number getNumber() {
        if (type == CellType.NUMERIC) {
            return (Number) value;
        }
        throw new IllegalStateException("Cannot get numeric value from cell of type " + type.name() + ".");
    }

    @Override
    public String getText() {
        if (type == CellType.TEXT) {
            return AttributedStringHelper.toString((AttributedString) value);
        }
        throw new IllegalStateException("Cannot get text value from cell of type " + type.name() + ".");
    }

    @Override
    public String getAsText() {
        switch (getCellType()) {
            case BLANK:
                return "";
            case TEXT:
                return AttributedStringHelper.toString((AttributedString) value);
            default:
                return String.valueOf(value);
        }
    }

    @Override
    public CellStyle getCellStyle() {
        return cellStyle;
    }

    @Override
    public int getHorizontalSpan() {
        return spanX;
    }

    @Override
    public int getVerticalSpan() {
        return spanY;
    }

    @Override
    public Cell getLogicalCell() {
        return logicalCell;
    }

    @Override
    public int getRowNumber() {
        return row.getRowNumber();
    }

    @Override
    public int getColumnNumber() {
        return columnNumber;
    }

    @Override
    public GenericSheet getSheet() {
        return row.getSheet();
    }

    @Override
    public AttributedString getAttributedString() {
        if (getCellType()==CellType.TEXT) {
            return (AttributedString) value;
        } else {
            return new AttributedString(getAsText());
        }
    }

    @Override
    public void set(Date arg) {
        this.type = CellType.NUMERIC;
        this.value = arg;
    }

    @Override
    public void set(Number arg) {
        this.type = CellType.NUMERIC;
        this.value = arg;
    }

    @Override
    public void set(String arg) {
        this.type = CellType.TEXT;
        this.value = new AttributedString(arg);
    }

    @Override
    public void set(Boolean arg) {
        this.type = CellType.BOOLEAN;
        this.value = arg;
    }

    @Override
    public Row getRow() {
        return row;
    }

    @Override
    public void clear() {
        this.type = CellType.BLANK;
        this.value = null;
    }

    @Override
    public void setCellStyle(CellStyle cellStyle) {
        this.cellStyle = (GenericCellStyle) cellStyle;
    }

    @Override
    public CellType getResultType() {
        return getCellType();
    }

    @Override
    public boolean isEmpty() {
        return type == CellType.BLANK
                || type == CellType.TEXT && AttributedStringHelper.isEmpty((AttributedString) value);
    }

    @Override
    public void setCellStyle(String cellStyleName) {
        this.cellStyle = getSheet().getWorkbook().getCellStyle(cellStyleName);
    }

    @Override
    public void setFormula(String value) {
        this.type = CellType.FORMULA;
        this.value = value;
    }

}
