package com.dua3.meja.model;

import java.util.Optional;

/**
 * Abstract base class for implementations of the {@link Row} interface.
 */
public abstract class AbstractRow implements Row {

    private final AbstractSheet sheet;
    private final int rowNumber;

    /**
     * AbstractRow is an abstract base class for implementations of the Row interface.
     *
     * @param sheet The AbstractSheet to which this row belongs.
     * @param rowNumber The row number of this row.
     */
    protected AbstractRow(AbstractSheet sheet, int rowNumber) {
        this.sheet = sheet;
        this.rowNumber = rowNumber;
    }

    @Override
    public abstract AbstractCell getCell(int col);

    @Override
    public abstract Optional<? extends AbstractCell> getCellIfExists(int j);

    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    @Override
    public AbstractSheet getSheet() {
        return sheet;
    }

    @Override
    public abstract AbstractWorkbook getWorkbook();
}
