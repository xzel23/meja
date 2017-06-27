package com.dua3.meja.model;

public abstract class AbstractCell implements Cell {

    private AbstractCell logicalCell;

    public AbstractCell() {
        this.logicalCell = this;
    }

    protected void addedToMergedRegion(AbstractCell topLeftCell, int spanX, int spanY) {
        if (isMerged()) {
            throw new IllegalStateException("Cell is already merged.");
        }

        if (spanX > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Maximum horizontal span number is " + Short.MAX_VALUE + ".");
        }

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

    private boolean isMerged() {
        return getHorizontalSpan() != 1 || getVerticalSpan() != 1;
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
        if (logicalCell != this) {
            // this should never happen because we checked for this cell being
            // the top left cell of the merged region
            throw new IllegalArgumentException("Cell is not top left cell of a merged region");
        }

        getSheet().removeMergedRegion(getRowNumber(), getColumnNumber());
    }

    @Override
    public abstract AbstractSheet getSheet();

}
