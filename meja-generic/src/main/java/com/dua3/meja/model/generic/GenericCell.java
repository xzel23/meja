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

import com.dua3.meja.model.AbstractCell;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.RichText;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the {@link Cell} interface for {@link GenericSheet}.
 */
public class GenericCell extends AbstractCell<GenericSheet, GenericRow, GenericCell> {
    private static final int MAX_HORIZONTAL_SPAN = 0xefff;
    private static final int MAX_VERTICAL_SPAN = 0xef_ffff;
    private static final int MAX_COLUMN_NUMBER = 0xef_ffff;

    /**
     * The precalculated initial value for the data field with rowspan=colspan=1 and
     * a cell type of blank.
     */
    private static final long INITIAL_DATA = ((/* spanX */ 1L) << 32) | ((/* spanY */ 1L) << 8)
            | CellType.BLANK.ordinal();
    private @Nullable Object value;
    private GenericCellStyle cellStyle;
    private @Nullable Map<Attribute, Object> attributes;

    enum Attribute {
        LINK_URI,
        LABEL
    }

    private Optional<Object> getAttribute(Attribute name) {
        if (attributes == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(attributes.get(name));
    }

    private void setAttribute(Attribute name, @Nullable Object value) {
        if (value == null) {
            if (attributes != null) {
                attributes.remove(name);
                if (attributes.isEmpty()) {
                    attributes = null;
                }
            }
            return;
        }

        if (attributes == null) {
            attributes = new EnumMap<>(GenericCell.Attribute.class);
        }

        attributes.put(name, value);
    }

    /**
     * A single long storing meta-information.
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
     * @param row       the row this cell belongs to
     * @param colNr     the column number
     * @param cellStyle the cell style to use
     */
    public GenericCell(GenericRow row, int colNr, GenericCellStyle cellStyle) {
        super(row);

        LangUtil.check(colNr >= 0 && colNr <= MAX_COLUMN_NUMBER, () -> new CellException(this, "column number out of range: " + colNr));

        this.cellStyle = cellStyle;
        this.value = null;

        initData(colNr);
    }

    @Override
    public void clear() {
        Object old = value;
        setCellType(CellType.BLANK);
        this.value = null;
        valueChanged(old, null);
    }

    @Override
    public @Nullable Object getOrDefault(@Nullable Object defaultValue) {
        return value != null ? value : defaultValue;
    }

    @Override
    public RichText getAsText(Locale locale) {
        assert value != null || getCellType() == CellType.BLANK;

        return switch (getCellType()) {
            case BLANK -> RichText.emptyText();
            case TEXT -> getText();
            case NUMERIC -> RichText.valueOf(cellStyle.format((Number) value, locale));
            case DATE -> RichText.valueOf(cellStyle.format((LocalDate) value, locale));
            case DATE_TIME -> RichText.valueOf(cellStyle.format((LocalDateTime) value, locale));
            default -> RichText.valueOf(value);
        };
    }

    @Override
    public boolean getBoolean() {
        if (getCellType() == CellType.BOOLEAN) {
            assert value != null;
            return (boolean) value;
        }
        throw new CellException(this, "Cannot get boolean value from cell of type " + getCellType().name() + ".");
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
    public LocalDate getDate() {
        if (getCellType() == CellType.DATE) {
            assert value != null;
            return (LocalDate) value;
        }
        throw new CellException(this, "Cannot get date value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public LocalDateTime getDateTime() {
        if (getCellType() == CellType.DATE_TIME) {
            assert value != null;
            return (LocalDateTime) value;
        }
        throw new CellException(this, "Cannot get date value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public String getFormula() {
        if (getCellType() == CellType.FORMULA) {
            assert value != null;
            return (String) value;
        }
        throw new CellException(this, "Cannot get formula from cell of type " + getCellType().name() + ".");
    }

    @Override
    public int getHorizontalSpan() {
        return (int) ((data & 0x0000_ffff_0000_0000L) >> 32);
    }

    @Override
    public Number getNumber() {
        if (getCellType() == CellType.NUMERIC) {
            assert value != null;
            return (Number) value;
        }
        throw new CellException(this, "Cannot get numeric value from cell of type " + getCellType().name() + ".");
    }

    @Override
    public CellType getResultType() {
        return getCellType();
    }

    @Override
    public int getRowNumber() {
        return getRow().getRowNumber();
    }

    @Override
    public RichText getText() {
        assert value != null || getCellType() == CellType.BLANK;

        return switch (getCellType()) {
            case BLANK -> RichText.emptyText();
            case TEXT -> (RichText) value;
            default ->
                    throw new CellException(this, "Cannot get text value from cell of type " + getCellType().name() + ".");
        };
    }

    @Override
    public int getVerticalSpan() {
        return (int) ((data & 0x0000_0000_ffff_ff00L) >> 8);
    }

    private void initData(int colNr) {
        LangUtil.check(colNr >= 0 && colNr <= MAX_COLUMN_NUMBER, () -> new CellException(this, "column number out of range: " + colNr));
        data = (((long) colNr) << 48) | INITIAL_DATA;
    }

    @Override
    public boolean isEmpty() {
        return getCellType() == CellType.BLANK;
    }

    @Override
    public GenericCell set(@Nullable Boolean b) {
        return set(b, CellType.BOOLEAN);
    }

    @Override
    public GenericCell set(@Nullable LocalDate arg) {
        return set(arg, CellType.DATE);
    }

    @Override
    public GenericCell set(@Nullable LocalDateTime arg) {
        return set(arg, CellType.DATE_TIME);
    }

    @Override
    public GenericCell set(@Nullable Number arg) {
        return set(arg, CellType.NUMERIC);
    }

    private GenericCell set(@Nullable Object arg, CellType type) {
        if (arg == null) {
            clear();
        } else {
            GenericSheet sheet = getAbstractSheet();
            arg = sheet.getWorkbook().cache(arg);
            if (arg != value || type != getCellType()) {
                Object old = value;
                setCellType(type);
                value = arg;
                valueChanged(old, arg);
            }
        }
        return this;
    }

    @Override
    public GenericCell set(@Nullable RichText s) {
        if (s == null || s.isEmpty()) {
            clear();
            return this;
        } else {
            return set(s, CellType.TEXT);
        }
    }

    @Override
    public GenericCell set(@Nullable String s) {
        if (s == null || s.isEmpty()) {
            clear();
            return this;
        }
        return set(RichText.valueOf(s), CellType.TEXT);
    }

    @Override
    public GenericCell setCellStyle(CellStyle cellStyle) {
        //noinspection ObjectEquality
        LangUtil.check(cellStyle.getWorkbook() == getWorkbook(),
                () -> new CellException(this, "Cell style does not belong to this workbook."));

        //noinspection ObjectEquality
        if (cellStyle != this.cellStyle) {
            GenericCellStyle old = this.cellStyle;
            this.cellStyle = (GenericCellStyle) cellStyle;
            styleChanged(old, this.cellStyle);
        }

        return this;
    }

    private void setCellType(CellType type) {
        data = (data & 0xffff_ffff_ffff_ff00L) | type.ordinal();
    }

    @Override
    public GenericCell setFormula(@Nullable String value) {
        set(value, CellType.FORMULA);
        return this;
    }

    @Override
    public Cell setHyperlink(URI target) {
        setAttribute(Attribute.LINK_URI, target);
        return this;
    }

    @Override
    public Cell clearHyperlink() {
        setAttribute(Attribute.LINK_URI, null);
        return this;
    }

    @Override
    public Cell setError() {
        setCellType(CellType.ERROR);
        value = null;
        return this;
    }

    @Override
    public Optional<URI> getHyperlink() {
        return getAttribute(Attribute.LINK_URI).map(obj -> {
            if (obj instanceof URI uri) {
                return uri;
            } else {
                throw new IllegalStateException("attribute value should be of type URI, but is " + obj.getClass());
            }
        });
    }

    @Override
    protected void setHorizontalSpan(int spanX) {
        LangUtil.check(spanX >= 0 && spanX <= MAX_HORIZONTAL_SPAN,
                () -> new CellException(this, "invalid value for horizontal span: " + spanX));

        data = (data & 0xffff_0000_ffff_ffffL) | (((long) spanX) << 32);
    }

    @Override
    protected GenericWorkbook getAbstractWorkbook() {
        return getAbstractSheet().getAbstractWorkbook();
    }

    @Override
    protected void setVerticalSpan(int spanY) {
        LangUtil.check(spanY >= 0 && spanY <= MAX_VERTICAL_SPAN,
                () -> new CellException(this, "invalid value for vertical span: " + spanY));

        data = (data & 0xffff_ffff_0000_00ffL) | (((long) spanY) << 8);
    }

    @Override
    public String toString(Locale locale) {
        assert value != null || getCellType() == CellType.BLANK;

        return switch (getCellType()) {
            case BLANK -> "";
            case NUMERIC -> cellStyle.format((Number) value, locale);
            case DATE -> cellStyle.format((LocalDate) value, locale);
            case DATE_TIME -> cellStyle.format((LocalDateTime) value, locale);
            default -> String.valueOf(value);
        };
    }

}
