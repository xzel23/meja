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
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.util.AttributedStringHelper;
import java.text.AttributedString;
import java.util.Date;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericCell implements Cell {

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
        if (type == CellType.DATE) {
            return (Date) value;
        }
        throw new IllegalStateException("Cannot get date value from cell of type " + type.name() + ".");
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
            if (value instanceof AttributedString) {
                return AttributedStringHelper.toString((AttributedString) value);
            } else if (value instanceof String) {
                return (String) value;
            } else {
                throw new IllegalStateException();
            }
        }
        throw new IllegalStateException("Cannot get text value from cell of type " + type.name() + ".");
    }

    @Override
    public String getAsText() {
        switch (getCellType()) {
            case BLANK:
                return "";
            case TEXT:
                return getText();
            case NUMERIC:
                return getCellStyle().format(getNumber());
            case DATE:
                return getCellStyle().format(getDate());
            default:
                return String.valueOf(value);
        }
    }

    @Override
    public GenericCellStyle getCellStyle() {
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
        if (getCellType() == CellType.TEXT) {
            if (value instanceof AttributedString) {
                return (AttributedString) value;
            } else if (value instanceof String) {
                // convert to AttributedString on first access
                final AttributedString as = new AttributedString((String) value);
                value = as;
                return as;
            } else {
                throw new IllegalStateException();
            }
        } else {
            return new AttributedString(getAsText());
        }
    }

    @Override
    public GenericCell set(Date arg) {
        if (arg == null) {
            clear();
        } else {
            this.type = CellType.DATE;
            this.value = arg;
        }
        return this;
    }

    @Override
    public GenericCell set(Number arg) {
        if (arg == null) {
            clear();
        } else {
            this.type = CellType.NUMERIC;
            this.value = arg;
        }
        return this;
    }

    @Override
    public GenericCell set(String arg) {
        if (arg == null || arg.isEmpty()) {
            clear();
        } else {
            this.type = CellType.TEXT;
            this.value = arg;
        }
        return this;
    }

    @Override
    public GenericCell set(AttributedString arg) {
        if (arg == null || AttributedStringHelper.isEmpty(arg)) {
            clear();
        } else {
            this.type = CellType.TEXT;
            this.value = arg;
        }
        return this;
    }

    @Override
    public GenericCell set(Boolean arg) {
        if (arg == null) {
            clear();
        } else {
            this.type = CellType.BOOLEAN;
            this.value = arg;
        }
        return this;
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
        if (cellStyle.getWorkbook() != getWorkbook()) {
            throw new IllegalArgumentException("Cell style does not belong to this workbook.");
        }
        this.cellStyle = (GenericCellStyle) cellStyle;
    }

    @Override
    public CellType getResultType() {
        return getCellType();
    }

    @Override
    public boolean isEmpty() {
        return type == CellType.BLANK;
    }

    @Override
    public void setCellStyle(String cellStyleName) {
        this.cellStyle = getSheet().getWorkbook().getCellStyle(cellStyleName);
    }

    @Override
    public GenericCell setFormula(String value) {
        this.type = CellType.FORMULA;
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        if (type == CellType.BLANK) {
            return "";
        } else {
            return getAsText();
        }
    }

    @Override
    public void copy(Cell other) {
        setCellStyle(other.getCellStyle().getName());
        switch (other.getCellType()) {
            case BLANK:
                clear();
                break;
            case BOOLEAN:
                set(other.getBoolean());
                break;
            case ERROR:
                set(Double.NaN);
                break;
            case FORMULA:
                set(other.getFormula());
                break;
            case NUMERIC:
                set(other.getNumber());
                break;
            case DATE:
                set(other.getDate());
                break;
            case TEXT:
                set(other.getText());
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public GenericWorkbook getWorkbook() {
        return row.getWorkbook();
    }

    void addedToMergedRegion(GenericCell logicalCell, int spanX, int spanY) {
        if (this.spanX != 1 || this.spanY != 1) {
            throw new IllegalStateException("Cell is already merged.");
        }

        if (this == logicalCell) {
            this.logicalCell = logicalCell;
            this.spanX = spanX;
            this.spanY = spanY;
        } else {
            clear();
            this.logicalCell = logicalCell;
            this.spanX = 0;
            this.spanY = 0;
        }
    }

    void removedFromMergedRegion() {
        this.logicalCell = this;
        this.spanX = 1;
        this.spanY = 1;
    }

    @Override
    public void unMerge() {
        if (logicalCell != this) {
            // this should never happen because we checked for this cell being
            // the top left cell of the merged region
            throw new IllegalArgumentException("Cell is not top left cell of a merged region");
        }

        getSheet().removeMergedRegion(getRowNumber(), getColumnNumber());
        int originalSpanX = spanX;
        int originalSpanY = spanY;
        for (int i = getRowNumber(); i < getRowNumber() + originalSpanY; i++) {
            for (int j = getColumnNumber(); j < getColumnNumber() + originalSpanX; j++) {
                GenericCell cell = row.getCell(j);
                cell.removedFromMergedRegion();
            }
        }
    }
}
