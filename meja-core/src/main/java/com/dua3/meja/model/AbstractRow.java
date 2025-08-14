package com.dua3.meja.model;

import org.jspecify.annotations.Nullable;

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

    /**
     * Retrieves the {@link AbstractWorkbook} associated with the current row.
     *
     * @return the {@link AbstractWorkbook} instance to which this row belongs.
     */
    protected final AbstractWorkbook<S, R, C> getAbstractWorkbook() {
        return sheet.getAbstractWorkbook();
    }

    /**
     * Retrieves the {@link AbstractSheet} instance associated with the current row.
     *
     * @return the {@link AbstractSheet} instance to which this row belongs.
     */
    protected final S getAbstractSheet() {
        return sheet;
    }

    /**
     * Retrieves the cell at the specified column index in this row.
     *
     * @param col the column index (0-based) of the cell to retrieve
     * @return the instance of the cell at the given column index
     */
    protected abstract C getAbstractCell(int col);

    /**
     * Retrieves the cell at the specified column index in this row, or {@code null} if the cell does not exist.
     *
     * @param col the column index (0-based) of the cell to retrieve
     * @return the instance of the cell at the given column index, or {@code null} if the cell does not exist
     */
    protected abstract @Nullable C getAbstractCellOrNull(int col);

    @Override
    public final Cell getCell(int colIndex) {
        return getAbstractCell(colIndex);
    }

    @Override
    public final Optional<Cell> getCellIfExists(int colIndex) {
        return Optional.ofNullable(getAbstractCellOrNull(colIndex));
    }

    @Override
    public final int getRowNumber() {
        return rowNumber;
    }

    @Override
    public final Sheet getSheet() {
        return getAbstractSheet();
    }

    @Override
    public final Workbook getWorkbook() {
        return getAbstractWorkbook();
    }
}
