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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.RefOption;
import com.dua3.meja.model.Row;
import com.dua3.meja.text.RichText;
import com.dua3.meja.util.MejaHelper;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericCell
        implements Cell {
    public static final int MAX_HORIZONTAL_SPAN = 0xefff;
    public static final int MAX_VERTICAL_SPAN = 0xef_ffff;
    public static final int MAX_COLUMN_NUMBER = 0xef_ffff;

    /**
     * The precalculated initial value for the data field with rowspan=colspan=1
     * and a cell type of blank.
     */
    private static final long INITIAL_DATA = ((/* spanX */ 1L) << 32)
            | ((/* spanY */ 1L) << 8)
            | CellType.BLANK.ordinal();
    private final GenericRow row;
    private Object value;
    private GenericCellStyle cellStyle;

    private GenericCell logicalCell;

    /**
     * A single long storing meta information.
     *
     * <pre>
     * data format (bytes): CCXXYYYT
     *
     * C - column number   (2 bytes)
     * X - horizontal span (2 bytes)
     * Y - vertical span   (3 bytes)
     * T - cell type       (1 byte)
     * </pre>
     */
    private long data;

    /**
     * Construct a new {@code GenericCell}.
     *
     * @param row
     *            the row this cell belongs to
     * @param colNumber
     *            the column number
     * @param cellStyle
     *            the cell style to use
     */
    public GenericCell(GenericRow row, int colNumber, GenericCellStyle cellStyle) {
        if (colNumber > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Maximum column number is " + Short.MAX_VALUE + ".");
        }

        this.row = row;
        this.logicalCell = this;
        this.cellStyle = cellStyle;
        this.value = null;

        initData(colNumber);
    }

    @Override
    public void clear() {
        Object old = value;
        setCellType(CellType.BLANK);
        this.value = null;
        getSheet().cellValueChanged(this, old, null);
    }

    @Override
    public void copy(Cell other) {
        setStyle(other.getCellStyle().getName());
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
            set(other.getDateTime());
            break;
        case TEXT:
            set(other.getText());
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Object get() {
        return value;
    }

    @Override
    public RichText getAsText() {
        switch (getCellType()) {
        case BLANK:
            return RichText.emptyText();
        case TEXT:
            return getText();
        case NUMERIC:
            return RichText.valueOf(getCellStyle().format((Number) value));
        case DATE:
            return RichText.valueOf(getCellStyle().format((LocalDateTime) value));
        default:
            return RichText.valueOf(value);
        }
    }

    @Override
    public boolean getBoolean() {
        if (getCellType() == CellType.BOOLEAN) {
            return (boolean) value;
        }
        throw new IllegalStateException("Cannot get boolean value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public String getCellRef(RefOption... options) {
        return MejaHelper.getCellRef(this, options);
    }

    @Override
    public GenericCellStyle getCellStyle() {
        return cellStyle;
    }

    @Override
    public CellType getCellType() {
        return CellType.values()[(int) (data & 0xffL)];
    }

    @Override
    public int getColumnNumber() {
        return (int) ((data & 0xffff_0000_0000_0000L) >> 48);
    }

    @Override
    @Deprecated
    public Date getDate() {
        return Date.from(getDateTime().atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public LocalDateTime getDateTime() {
        if (getCellType() == CellType.DATE) {
            return (LocalDateTime) value;
        }
        throw new IllegalStateException("Cannot get date value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public String getFormula() {
        if (getCellType() == CellType.FORMULA) {
            return (String) value;
        }
        throw new IllegalStateException("Cannot get formula from cell of type " + getCellType().name() + ".");
    }

    @Override
    public int getHorizontalSpan() {
        return (int) ((data & 0x0000_ffff_0000_0000L) >> 32);
    }

    @Override
    public Cell getLogicalCell() {
        return logicalCell;
    }

    @Override
    public Number getNumber() {
        if (getCellType() == CellType.NUMERIC) {
            return (Number) value;
        }
        throw new IllegalStateException("Cannot get numeric value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public CellType getResultType() {
        return getCellType();
    }

    @Override
    public Row getRow() {
        return row;
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
    public RichText getText() {
        switch (getCellType()) {
        case BLANK:
            return RichText.emptyText();
        case TEXT:
            return (RichText) value;
        default:
            throw new IllegalStateException("Cannot get text value from cell of type " + getCellType().name() + ".");
        }
    }

    @Override
    public int getVerticalSpan() {
        return (int) ((data & 0x0000_0000_ffff_ff00L) >> 8);
    }

    @Override
    public GenericWorkbook getWorkbook() {
        return row.getWorkbook();
    }

    @Override
    public boolean isEmpty() {
        return getCellType() == CellType.BLANK;
    }

    @Override
    public GenericCell set(Boolean arg) {
        return set(arg, CellType.BOOLEAN);
    }

    @Override
    @Deprecated
    public GenericCell set(Date arg) {
        LocalDateTime ldt = LocalDateTime.ofInstant(arg.toInstant(), ZoneId.systemDefault());
        return set(ldt, CellType.DATE);
    }

    @Override
    public GenericCell set(LocalDateTime arg) {
        return set(arg, CellType.DATE);
    }

    @Override
    public GenericCell set(Number arg) {
        return set(arg, CellType.NUMERIC);
    }

    @Override
    public GenericCell set(Object arg) {
        MejaHelper.set(this, arg);
        return this;
    }

    @Override
    public GenericCell set(RichText arg) {
        if (arg == null || arg.isEmpty()) {
            clear();
            return this;
        } else {
            return set(arg, CellType.TEXT);
        }
    }

    @Override
    public GenericCell set(String arg) {
        if (arg == null || arg.isEmpty()) {
            clear();
            return this;
        }
        return set(RichText.valueOf(arg), CellType.TEXT);
    }

    @Override
    public void setCellStyle(CellStyle cellStyle) {
        if (cellStyle.getWorkbook() != getWorkbook()) {
            throw new IllegalArgumentException("Cell style does not belong to this workbook.");
        }
        if (cellStyle != this.cellStyle) {
            GenericCellStyle old = this.cellStyle;
            this.cellStyle = (GenericCellStyle) cellStyle;
            getSheet().cellStyleChanged(this, old, cellStyle);
        }
    }

    @Override
    public GenericCell setFormula(String value) {
        set(value, CellType.FORMULA);
        return this;
    }

    @Override
    public void setStyle(String cellStyleName) {
        this.cellStyle = getSheet().getWorkbook().getCellStyle(cellStyleName);
    }

    @Override
    public String toString() {
        switch (getCellType()) {
        case BLANK:
            return "";
        case NUMERIC:
            return getCellStyle().format((Number) value);
        case DATE:
            return getCellStyle().format((LocalDateTime) value);
        default:
            return String.valueOf(value);
        }
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

    private void initData(int colNr) {
        if (colNr < 0 || colNr > MAX_COLUMN_NUMBER) {
            throw new IllegalArgumentException();
        }

        data = (((long) colNr) << 48) | INITIAL_DATA;
    }

    private GenericCell set(Object arg, CellType type) {
        GenericSheet sheet = getSheet();
        arg = sheet.getWorkbook().cache(arg);
        if (arg != value || type != getCellType()) {
            Object old = value;
            if (arg == null) {
                clear();
            } else {
                setCellType(type);
                value = arg;
            }
            sheet.cellValueChanged(this, old, arg);
        }
        return this;
    }

    private void setCellType(CellType type) {
        data = (data & 0xffff_ffff_ffff_ff00L) | type.ordinal();
    }

    private void setHorizontalSpan(int spanX) {
        if (spanX < 0 || spanX > MAX_HORIZONTAL_SPAN) {
            throw new IllegalArgumentException();
        }

        data = (data & 0xffff_0000_ffff_ffffL) | (((long) spanX) << 32);
    }

    private void setVerticalSpan(int spanY) {
        if (spanY < 0 || spanY > MAX_VERTICAL_SPAN) {
            throw new IllegalArgumentException();
        }

        data = (data & 0xffff_ffff_0000_00ffL) | (((long) spanY) << 8);
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

}
