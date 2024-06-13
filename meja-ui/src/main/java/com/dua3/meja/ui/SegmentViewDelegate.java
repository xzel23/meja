package com.dua3.meja.ui;

import com.dua3.meja.model.Sheet;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Scale2f;

import java.util.concurrent.locks.Lock;
import java.util.function.IntSupplier;

public class SegmentViewDelegate<SVD extends SheetViewDelegate> {
    private final SegmentView owner;
    private final SVD svDelegate;
    private final IntSupplier startRow;
    private final IntSupplier endRow;
    private final IntSupplier startColumn;
    private final IntSupplier endColumn;

    public SegmentViewDelegate(
            SegmentView owner,
            SVD sheetViewDelegate,
            IntSupplier startRow,
            IntSupplier endRow,
            IntSupplier startColumn,
            IntSupplier endColumn
    ) {
        this.owner = owner;
        this.svDelegate = sheetViewDelegate;
        this.startRow = startRow;
        this.endRow = endRow;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
    }

    public SVD getSvDelegate() {
        return svDelegate;
    }

    public int getBeginColumn() {
        return getStartColumn();
    }

    public int getBeginRow() {
        return getStartRow();
    }

    public int getEndColumn() {
        return endColumn.getAsInt();
    }

    public int getEndRow() {
        return endRow.getAsInt();
    }

    public Sheet getSheet() {
        return svDelegate.getSheet().orElse(null);
    }

    public void setViewSize(float wd, float hd) {
        Scale2f s = getSvDelegate().getScale();
        float w = s.sx() * wd;
        float h = s.sy() * hd;
        owner.setViewSizeOnDisplay(w,h);
    }

    /**
     * Get the Offset by which the x-coordinate is shifted due to row labels.
     *
     * @return the x-offset
     */
    public float getXOffset() {
        return (isLeftOfSplit() ? svDelegate.getRowLabelWidth() : 0) - svDelegate.getColumnPos(getBeginColumn());
    }

    /**
     * Get the Offset by which the y-coordinate is shifted due to column labels.
     *
     * @return the y-offset
     */
    public float getYOffset() {
        return (isAboveSplit() ? svDelegate.getColumnLabelHeight() : 0) - svDelegate.getRowPos(getBeginRow());
    }

    public float getXMinInViewCoordinates() {
        float x = svDelegate.getColumnPos(getBeginColumn());
        return getTransformation().transform(x,0).x();
    }

    public float getYMinInViewCoordinates() {
        float y = svDelegate.getRowPos(getBeginRow());
        return getTransformation().transform(0,y).y();
    }

    public boolean isLeftOfSplit() {
        return getEndColumn() <= getSheet().getSplitColumn();
    }

    public boolean isAboveSplit() {
        return getEndRow() <= getSheet().getSplitRow();
    }

    public boolean hasHLine() {
        return getEndRow() > 0 && getEndRow() < getSheet().getLastRowNum();
    }

    public boolean hasVLine() {
        return getEndColumn() > 0 && getEndColumn() < getSheet().getLastColNum();
    }

    public void updateLayout() {
        Sheet sheet = getSheet();
        if (sheet == null) {
            return;
        }

        Lock lock = sheet.readLock();
        lock.lock();
        try {
            // the width is the width for the labels showing row names ...
            float width = isLeftOfSplit() ? svDelegate.getRowLabelWidth() : svDelegate.get1PxWidth();

            // ... plus the width of the columns displayed ...
            width += svDelegate.getColumnPos(getMaxColumn()+1) - svDelegate.getColumnPos(getMinColumn());

            // ... plus 1 pixel for drawing a line at the split position.
            if (hasVLine()) {
                width += svDelegate.get1PxWidth();
            }

            // the height is the height for the labels showing column names ...
            float height = isAboveSplit() ? svDelegate.getColumnLabelHeight() : 1;

            // ... plus the height of the rows displayed ...
            height += svDelegate.getRowPos(getMaxRow()+1) - svDelegate.getRowPos(getMinRow());

            // ... plus 1 pixel for drawing a line below the lines above the
            // split.
            if (hasHLine()) {
                height += svDelegate.get1PxWidth();
            }

            setViewSize(width, height);
        } finally {
            lock.unlock();
        }
    }

    private int getStartRow() {
        return startRow.getAsInt();
    }

    /**
     * Get the index of the first row to display. This might return a row index smaller than the index of the first
     * row contained in the sheet.
     *
     * @return row index
     */
    private int getMinRow() {
        return isAboveSplit() ? 0 : getSheet().getSplitRow();
    }

    private int getMaxRow() {
        return isAboveSplit() ? getSheet().getSplitRow()-1 : getSheet().getLastRowNum();
    }

    private int getStartColumn() {
        return startColumn.getAsInt();
    }

    private int getMinColumn() {
        return isLeftOfSplit() ? 0 : getSheet().getSplitColumn();
    }

    private int getMaxColumn() {
        return isLeftOfSplit() ? getSheet().getSplitColumn()-1 : getSheet().getLastColNum();
    }

    public AffineTransformation2f getTransformation() {
        return AffineTransformation2f.combine(
                AffineTransformation2f.translate(getXOffset(), getYOffset()),
                svDelegate.getTransformation()
        );
    }

    @Override
    public String toString() {
        return (isAboveSplit() ? "TOP_" : "BOTTOM_")
                + (isLeftOfSplit() ? "LEFT" : "RIGHT");
    }
}
