package com.dua3.meja.model;

import java.util.Locale;

import com.dua3.utility.lang.LangUtil;

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

    protected void addedToMergedRegion(AbstractCell topLeftCell, int spanX, int spanY) {
        LangUtil.check(!isMerged(), () -> new CellException(this, "Cell is already merged."));
        LangUtil.check(spanX <= Short.MAX_VALUE, () -> new CellException(this, "Maximum horizontal span number is " + Short.MAX_VALUE));

        if (this.getRowNumber() == topLeftCell.getRowNumber()
                && this.getColumnNumber() == topLeftCell.getColumnNumber()) {
            setHorizontalSpan(spanX);
            setVerticalSpan(spanY);
        } else {
            clear();
            setHorizontalSpan(0);
            setVerticalSpan(0);
        }

        this.logicalCell = topLeftCell;
    }

    protected abstract void setVerticalSpan(int spanY);

    protected abstract void setHorizontalSpan(int spanX);

    protected void valueChanged(Object old, Object arg) {
        getSheet().cellValueChanged(this, old, arg);
    }

    protected void styleChanged(Object old, CellStyle cellStyle) {
        getSheet().cellStyleChanged(this, old, cellStyle);
    }

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
        LangUtil.check(logicalCell == this, "Cell is not top left cell of a merged region");

        getSheet().removeMergedRegion(getRowNumber(), getColumnNumber());

        int originalSpanX = getHorizontalSpan();
        int originalSpanY = getVerticalSpan();
        for (int i = getRowNumber(); i < getRowNumber() + originalSpanY; i++) {
            for (int j = getColumnNumber(); j < getColumnNumber() + originalSpanX; j++) {
                AbstractCell cell = getRow().getCellIfExists(j);
                if (cell != null) {
                    cell.removedFromMergedRegion();
                }
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
