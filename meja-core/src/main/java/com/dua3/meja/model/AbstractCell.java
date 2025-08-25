package com.dua3.meja.model;

import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Abstract base class for sheet cells.
 *
 * @param <S> the concrete type of Sheet that contains this cell, extending AbstractSheet
 * @param <R> the concrete type of Row that contains this cell, extending AbstractRow
 * @param <C> the concrete type of Cell (self-referential type parameter), extending AbstractCell
 */
public abstract class AbstractCell<S extends AbstractSheet<S, R, C>, R extends AbstractRow<S, R, C>, C extends AbstractCell<S, R, C>> implements Cell {

    private final R row;
    private C logicalCell;

    /**
     * Create a new Abstract cell that belongs to a row.
     *
     * @param row the row the new cell belongs to
     */
    @SuppressWarnings("unchecked")
    protected AbstractCell(R row) {
        this.row = row;
        this.logicalCell = (C) this;
    }

    /**
     * Adds the cell to a merged region with the specified top-left cell and x- and y-span.
     *
     * @param topLeftCell the top-left cell of the merged region
     * @param spanX the horizontal span of the merged region
     * @param spanY the vertical span of the merged region
     */
    protected void addedToMergedRegion(C topLeftCell, int spanX, int spanY) {
        LangUtil.check(!isMerged(), () -> new CellException(this, "Cell is already merged."));
        LangUtil.check(spanX <= Short.MAX_VALUE, () -> new CellException(this, "Maximum horizontal span number is " + Short.MAX_VALUE));

        if (getRowNumber() == topLeftCell.getRowNumber() && getColumnNumber() == topLeftCell.getColumnNumber()) {
            setHorizontalSpan(spanX);
            setVerticalSpan(spanY);
        } else {
            clear();
            setHorizontalSpan(0);
            setVerticalSpan(0);
        }

        this.logicalCell = topLeftCell;
    }

    /**
     * Sets the vertical span for the cell.
     *
     * @param spanY the vertical span of the merged region
     */
    protected abstract void setVerticalSpan(int spanY);

    /**
     * Sets the horizontal span for the cell.
     *
     * @param spanX the horizontal span of the merged region
     */
    protected abstract void setHorizontalSpan(int spanX);

    /**
     * This method is called when the value of a cell has changed and informs the sheet about the change.
     *
     * @param old The old value of the cell.
     * @param arg The new value of the cell.
     */
    protected void valueChanged(@Nullable Object old, @Nullable Object arg) {
        getAbstractSheet().cellValueChanged(this, old, arg);
    }

    /**
     * This method is called when the value of a cell style has changed and informs the sheet about the change.
     *
     * @param old The old value of the cell.
     * @param arg The new value of the cell.
     */
    protected void styleChanged(Object old, CellStyle arg) {
        getAbstractSheet().cellStyleChanged(this, old, arg);
    }

    /**
     * This method is called when a cell is removed from a merged region.
     * It sets the logicalCell property of the cell to itself, and sets the horizontal and vertical spans to 1.
     * This method is typically called internally when a merged region is unmerged or when a cell is updated in a merged region.
     */
    @SuppressWarnings("unchecked")
    protected void removedFromMergedRegion() {
        //noinspection unchecked
        this.logicalCell = (C) this;
        setHorizontalSpan(1);
        setVerticalSpan(1);
    }

    /**
     * Retrieves the logical abstract cell associated with this cell.
     * This method returns the logical representation of the cell that may
     * be part of a merged region or share properties with other cells.
     *
     * @return the logical abstract cell of type {@code C} associated with this cell
     */
    protected final C getLogicalAbstractCell() {
        return logicalCell;
    }

    /**
     * Retrieves the abstract workbook associated with the current cell.
     * The abstract workbook encapsulates the structural representation
     * of sheets, rows, and cells within the workbook.
     *
     * @return the associated abstract workbook of type {@code AbstractWorkbook<S, R, C>}
     *         providing access to the workbook operations and metadata
     */
    protected abstract AbstractWorkbook<S, R, C> getAbstractWorkbook();

    @Override
    public final Workbook getWorkbook() {
        return getAbstractWorkbook();
    }

    @Override
    public final Cell getLogicalCell() {
        return getLogicalAbstractCell();
    }

    @Override
    public void unMerge() {
        //noinspection ObjectEquality
        LangUtil.check(logicalCell == this, "Cell is not the top left cell of a merged region");

        getAbstractSheet().removeMergedRegion(getRowNumber(), getColumnNumber());

        int originalSpanX = getHorizontalSpan();
        int originalSpanY = getVerticalSpan();
        for (int i = getRowNumber(); i < getRowNumber() + originalSpanY; i++) {
            for (int j = getColumnNumber(); j < getColumnNumber() + originalSpanX; j++) {
                C mergedCell = row.getAbstractCellOrNull(j);
                if (mergedCell != null) {
                    mergedCell.removedFromMergedRegion();
                }
            }
        }
    }

    /**
     * Retrieves the abstract sheet object associated with this cell.
     *
     * @return the abstract sheet object of type {@code S} to which this cell belongs.
     */
    protected S getAbstractSheet() {
        return getAbstractRow().getAbstractSheet();
    }

    /**
     * Retrieves the abstract row object associated with this cell.
     *
     * @return the abstract row object of type {@code R} associated with this cell
     */
    protected R getAbstractRow() {
        return row;
    }

    @Override
    public final Sheet getSheet() {
        return getRow().getSheet();
    }

    @Override
    public final Row getRow() {
        return getAbstractRow();
    }

    @Override
    public String toString() {
        return toString(Locale.ROOT);
    }
}
