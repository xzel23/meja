package com.dua3.meja.ui;

import com.dua3.meja.model.Sheet;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Scale2f;

import java.util.concurrent.locks.Lock;

public class SegmentViewDelegate {
    private final SegmentView owner;
    private final SheetViewDelegate sheetViewDelegate;
    private final SheetView.Quadrant quadrant;
    private float offsetX;
    private float offsetY;
    private float widthInPoints;
    private float heightInPoints;
    private float widthInPixels;
    private float heightInPixels;

    public SegmentViewDelegate(
            SegmentView owner,
            SheetViewDelegate sheetViewDelegate,
            SheetView.Quadrant quadrant
    ) {
        this.owner = owner;
        this.sheetViewDelegate = sheetViewDelegate;
        this.quadrant = quadrant;
    }

    public SheetViewDelegate getSheetViewDelegate() {
        return sheetViewDelegate;
    }

    public Sheet getSheet() {
        return sheetViewDelegate.getSheet().orElse(null);
    }

    /**
     * Get the Offset by which the x-coordinate is shifted due to row labels.
     *
     * @return the x-offset
     */
    public float getXOffset() {
        return offsetX;
    }

    /**
     * Get the Offset by which the y-coordinate is shifted due to column labels.
     *
     * @return the y-offset
     */
    public float getYOffset() {
        return offsetY;
    }

    public float getXMinInViewCoordinates() {
        float x = sheetViewDelegate.getColumnPos(getStartColumn());
        return getTransformation().transform(x,0).x();
    }

    public float getYMinInViewCoordinates() {
        float y = sheetViewDelegate.getRowPos(getStartRow());
        return getTransformation().transform(0,y).y();
    }

    public float getWidthInPoints() {
        return widthInPoints;
    }

    public float getHeightInPoints() {
        return heightInPoints;
    }

    public float getWidthInPixels() {
        return widthInPixels;
    }

    public float getHeightInPixels() {
        return heightInPixels;
    }

    public boolean isLeftOfSplit() {
        return getEndColumn() <= getSheet().getSplitColumn();
    }

    public boolean isAboveSplit() {
        return getEndRow() <= getSheet().getSplitRow();
    }

    public boolean hasHLine() {
        return isAboveSplit() && getSheet().getSplitRow() > 0;
    }

    public boolean hasVLine() {
        return isLeftOfSplit() && getSheet().getSplitColumn() > 0;
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
            float width = isLeftOfSplit() ? sheetViewDelegate.getRowLabelWidthInPoints() : 0;

            // ... plus 1 Pixel for the splitline if there's a vertical split, and we are left of the split
            width += hasVLine() ? sheetViewDelegate.get1PxWidthInPoints() : 0;

            // ... plus the width of the columns displayed ...
            width += sheetViewDelegate.getColumnPos(getEndColumn()) - sheetViewDelegate.getColumnPos(getStartColumn());

            // the height is the height for the labels showing column names ...
            float height = isAboveSplit() ? sheetViewDelegate.getColumnLabelHeightInPoints() : 0;

            // ... plus 1 Pixel for the splitline if there's a horizontal split and we are above of the split
            height += hasHLine() ? sheetViewDelegate.get1PxHeightInPoints() : 0;

            // ... plus the height of the rows displayed ...
            height += sheetViewDelegate.getRowPos(getEndRow()) - sheetViewDelegate.getRowPos(getStartRow());

            offsetX = (isLeftOfSplit() ? sheetViewDelegate.getRowLabelWidthInPoints() : 0) - sheetViewDelegate.getColumnPos(getStartColumn());
            offsetY = (isAboveSplit() ? sheetViewDelegate.getColumnLabelHeightInPoints() : 0) - sheetViewDelegate.getRowPos(getStartRow());

            this.widthInPoints = width;
            this.heightInPoints = height;

            Scale2f s = getSheetViewDelegate().getScale();
            this.widthInPixels = s.sx() * width;
            this.heightInPixels = s.sy() * height;

            owner.setViewSizeOnDisplay(widthInPixels, heightInPixels);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the index of the first row to display. This might return a row index smaller than the index of the first
     * row contained in the sheet.
     *
     * @return row index
     */
    public int getStartColumn() {
        return quadrant.startColumn(sheetViewDelegate.getColumnCount(), sheetViewDelegate.getSplitColumn());
    }

    public int getStartRow() {
        return quadrant.startRow(sheetViewDelegate.getRowCount(), sheetViewDelegate.getSplitRow());
    }

    public int getEndColumn() {
        return quadrant.endColumn(sheetViewDelegate.getColumnCount(), sheetViewDelegate.getSplitColumn());
    }

    public int getEndRow() {
        return quadrant.endRow(sheetViewDelegate.getRowCount(), sheetViewDelegate.getSplitRow());
    }

    public AffineTransformation2f getTransformation() {
        return AffineTransformation2f.combine(
                AffineTransformation2f.translate(getXOffset(), getYOffset()),
                sheetViewDelegate.getTransformation()
        );
    }

    @Override
    public String toString() {
        return (isAboveSplit() ? "TOP_" : "BOTTOM_")
                + (isLeftOfSplit() ? "LEFT" : "RIGHT");
    }
}
