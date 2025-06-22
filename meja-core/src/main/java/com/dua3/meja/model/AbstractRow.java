package com.dua3.meja.model;

import java.util.Optional;

/**
 * Abstract base class for implementations of the {@link Row} interface.
 *
 * @param <S> the concrete type of Sheet that contains this row, extending AbstractSheet
 * @param <R> the concrete type of Row (self-referential type parameter), extending AbstractRow
 * @param <C> the concrete type of Cell that this row contains, extending AbstractCell
 */
public abstract class AbstractRow<S extends AbstractSheet<S, R, C>, R extends AbstractRow<S, R, C>, C extends AbstractCell<S, R, C>> implements Row {

    private final S sheet;
    private final int rowNumber;

    /**
     * AbstractRow is an abstract base class for implementations of the Row interface.
     *
     * @param sheet The AbstractSheet to which this row belongs.
     * @param rowNumber The row number of this row.
     */
    protected AbstractRow(S sheet, int rowNumber) {
        this.sheet = sheet;
        this.rowNumber = rowNumber;
    }

    @Override
    public abstract C getCell(int col);

    @Override
    public abstract Optional<C> getCellIfExists(int j);

    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    @Override
    public S getSheet() {
        return sheet;
    }

    @Override
    public abstract AbstractWorkbook getWorkbook();
}
