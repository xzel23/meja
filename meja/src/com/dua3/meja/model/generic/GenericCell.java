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
import com.dua3.meja.util.MejaHelper;
import java.text.AttributedString;
import java.util.Date;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericCell implements Cell {

    private final GenericRow row;
    private Object value;
    private GenericCellStyle cellStyle;
    private GenericCell logicalCell;

    /**
     * A single long storing meta information.
     *
     * CCXXYYYT
     *
     * C - column number (2 bytes) X - horizontal span (2 bytes) Y - vertical
     * span (3 bytes) T - cell type (1 byte)
     */
    private long data;

    public static final int MAX_HORIZONTAL_SPAN = 0xefff;
    public static final int MAX_VERTICAL_SPAN = 0xefffff;
    public static final int MAX_COLUMN_NUMBER = 0xefffff;

    private void setCellType(CellType type) {
        data = (data & 0xffffffffffffff00L) | type.ordinal();
    }

    @Override
    public CellType getCellType() {
        return CellType.values()[(int) (data & 0xffL)];
    }

    private void setVerticalSpan(int spanY) {
        if (spanY < 0 || spanY > MAX_VERTICAL_SPAN) {
            throw new IllegalArgumentException();
        }

        data = (data & 0xffffffff000000ffL) | (((long) spanY) << 8);
    }

    @Override
    public int getVerticalSpan() {
        return (int) ((data & 0x00000000ffffff00L) >> 8);
    }

    private void setHorizontalSpan(int spanX) {
        if (spanX < 0 || spanX > MAX_HORIZONTAL_SPAN) {
            throw new IllegalArgumentException();
        }

        data = (data & 0xffff0000ffffffffL) | (((long) spanX) << 32);
    }

    @Override
    public int getHorizontalSpan() {
        return (int) ((data & 0x0000ffff00000000L) >> 32);
    }

    private void setColumnNr(int colNr) {
        if (colNr < 0 || colNr > MAX_COLUMN_NUMBER) {
            throw new IllegalArgumentException();
        }

        data = (data & 0x0000ffffffffffffL) | (((long) colNr) << 48);
    }

    @Override
    public int getColumnNumber() {
        return (int) ((data & 0xffff000000000000L) >> 48);
    }

    /**
     * Construct a new {@code GenericCell}.
     *
     * @param row the row this cell belongs to
     * @param colNumber the column number
     * @param cellStyle the cell style to use
     */
    public GenericCell(GenericRow row, int colNumber, GenericCellStyle cellStyle) {
        if (colNumber > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Maximum column number is " + Short.MAX_VALUE + ".");
        }

        this.row = row;
        this.logicalCell = this;
        this.cellStyle = cellStyle;
        this.value = null;

        setColumnNr(colNumber);
        setHorizontalSpan(1);
        setVerticalSpan(1);
        setCellType(CellType.BLANK);
    }

    @Override
    public boolean getBoolean() {
        if (getCellType() == CellType.BOOLEAN) {
            return (boolean) value;
        }
        throw new IllegalStateException("Cannot get boolean value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public String getFormula() {
        if (getCellType() == CellType.FORMULA) {
            return (String) value;
        }
        throw new IllegalStateException("Cannot get formula from cell of type " + getCellType().name() + ".");
    }

    @Override
    public Date getDate() {
        if (getCellType() == CellType.DATE) {
            return (Date) value;
        }
        throw new IllegalStateException("Cannot get date value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public Number getNumber() {
        if (getCellType() == CellType.NUMERIC) {
            return (Number) value;
        }
        throw new IllegalStateException("Cannot get numeric value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public String getText() {
        if (getCellType() == CellType.TEXT) {
            if (value instanceof String) {
                return (String) value;
            } else if (value instanceof AttributedString) {
                return AttributedStringHelper.toString((AttributedString) value);
            } else {
                throw new IllegalStateException();
            }
        }
        throw new IllegalStateException("Cannot get text value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public String getAsText() {
        switch (getCellType()) {
            case BLANK:
                return "";
            case TEXT:
                return getText();
            case NUMERIC:
                return getCellStyle().format((Number) value);
            case DATE:
                return getCellStyle().format((Date) value);
            default:
                return String.valueOf(value);
        }
    }

    @Override
    public GenericCellStyle getCellStyle() {
        return cellStyle;
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
            setCellType(CellType.DATE);
            this.value = arg;
        }
        return this;
    }

    @Override
    public GenericCell set(Number arg) {
        if (arg == null) {
            clear();
        } else {
            setCellType(CellType.NUMERIC);
            this.value = arg;
        }
        return this;
    }

    @Override
    public GenericCell set(String arg) {
        if (arg == null || arg.isEmpty()) {
            clear();
        } else {
            setCellType(CellType.TEXT);
            this.value = arg;
        }
        return this;
    }

    @Override
    public GenericCell set(AttributedString arg) {
        if (arg == null || AttributedStringHelper.isEmpty(arg)) {
            clear();
        } else {
            setCellType(CellType.TEXT);
            this.value = arg;
        }
        return this;
    }

    @Override
    public GenericCell set(Boolean arg) {
        if (arg == null) {
            clear();
        } else {
            setCellType(CellType.BOOLEAN);
            this.value = arg;
        }
        return this;
    }

    @Override
    public GenericCell set(Object arg) {
        MejaHelper.set(this, arg);
        return this;
    }

    @Override
    public Row getRow() {
        return row;
    }

    @Override
    public void clear() {
        setCellType(CellType.BLANK);
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
        return getCellType() == CellType.BLANK;
    }

    @Override
    public void setCellStyle(String cellStyleName) {
        this.cellStyle = getSheet().getWorkbook().getCellStyle(cellStyleName);
    }

    @Override
    public GenericCell setFormula(String value) {
        setCellType(CellType.FORMULA);
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
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
                if (other.isRichText()) {
                    set(other.getAttributedString());
                } else {
                    set(other.getText());
                }
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
        if (getHorizontalSpan() != 1 || getVerticalSpan() != 1) {
            throw new IllegalStateException("Cell is already merged.");
        }

        if (spanX > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Maximum horizontal span number is " + Short.MAX_VALUE + ".");
        }

        if (this == logicalCell) {
            this.logicalCell = logicalCell;
            setHorizontalSpan(spanX);
            setVerticalSpan(spanY);
        } else {
            clear();
            this.logicalCell = logicalCell;
            setHorizontalSpan(0);
            setVerticalSpan(0);
        }
    }

    void removedFromMergedRegion() {
        this.logicalCell = this;
        setHorizontalSpan(1);
        setVerticalSpan(1);
    }

    @Override
    public void unMerge() {
        if (logicalCell != this) {
            // this should never happen because we checked for this cell being
            // the top left cell of the merged region
            throw new IllegalArgumentException("Cell is not top left cell of a merged region");
        }

        getSheet().removeMergedRegion(getRowNumber(), getColumnNumber());
        int originalSpanX = getHorizontalSpan();
        int originalSpanY = getVerticalSpan();
        for (int i = getRowNumber(); i < getRowNumber() + originalSpanY; i++) {
            for (int j = getColumnNumber(); j < getColumnNumber() + originalSpanX; j++) {
                GenericCell cell = row.getCellIfExists(j);
                if (cell != null) {
                    cell.removedFromMergedRegion();
                }
            }
        }
    }

    @Override
    public boolean isRichText() {
        // no need to check the cell type!
        return value instanceof AttributedString;
    }

    @Override
    public String getCellRef() {
        return MejaHelper.getCellRef(this, false);
    }

    @Override
    public String getCellRef(boolean includeSheet) {
        return MejaHelper.getCellRef(this, includeSheet);
    }
}
