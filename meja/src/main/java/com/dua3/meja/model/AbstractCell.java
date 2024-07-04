package com.dua3.meja.model;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;

import java.util.Locale;

/**
 * Abstract base class for sheet cells.
 */
public abstract class AbstractCell implements Cell {

    private final AbstractRow row;
    private AbstractCell logicalCell;

    /**
     * Create a new Abstract cell that belongs to a row.
     *
     * @param row the row the new cell belongs to
     */
    protected AbstractCell(AbstractRow row) {
        this.row = row;
        this.logicalCell = this;
    }

    /**
     * Adds the cell to a merged region with the specified top-left cell and x- and y-span.
     *
     * @param topLeftCell the top-left cell of the merged region
     * @param spanX the horizontal span of the merged region
     * @param spanY the vertical span of the merged region
     */
    protected void addedToMergedRegion(AbstractCell topLeftCell, int spanX, int spanY) {
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
        getSheet().cellValueChanged(this, old, arg);
    }

    /**
     * This method is called when the value of a cell style has changed and informs the sheet about the change.
     *
     * @param old The old value of the cell.
     * @param arg The new value of the cell.
     */
    protected void styleChanged(Object old, CellStyle arg) {
        getSheet().cellStyleChanged(this, old, arg);
    }

    /**
     * This method is called when a cell is removed from a merged region.
     * It sets the logicalCell property of the cell to itself, and sets the horizontal and vertical spans to 1.
     * This method is typically called internally when a merged region is unmerged or when a cell is updated in a merged region.
     */
    protected void removedFromMergedRegion() {
        this.logicalCell = this;
        setHorizontalSpan(1);
        setVerticalSpan(1);
    }

    @Override
    public Cell getLogicalCell() {
        return logicalCell;
    }

    @Override
    public void unMerge() {
        //noinspection ObjectEquality
        LangUtil.check(logicalCell == this, "Cell is not the top left cell of a merged region");

        getSheet().removeMergedRegion(getRowNumber(), getColumnNumber());

        int originalSpanX = getHorizontalSpan();
        int originalSpanY = getVerticalSpan();
        for (int i = getRowNumber(); i < getRowNumber() + originalSpanY; i++) {
            for (int j = getColumnNumber(); j < getColumnNumber() + originalSpanX; j++) {
                getRow().getCellIfExists(j).ifPresent(AbstractCell::removedFromMergedRegion);
            }
        }
    }

    @Override
    public abstract AbstractSheet getSheet();

    @Override
    public AbstractRow getRow() {
        return row;
    }

    @Override
    public String toString() {
        return toString(Locale.ROOT);
    }
}
