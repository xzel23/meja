package com.dua3.meja.ui;

import com.dua3.meja.model.Sheet;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Scale2f;

import java.util.Optional;
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

    /**
     * Retrieves the sheet view delegate associated with this segment view.
     *
     * @return the SheetViewDelegate associated with this segment view
     */
    public SheetViewDelegate getSheetViewDelegate() {
        return sheetViewDelegate;
    }

    /**
     * Retrieves the sheet associated with this segment view delegate.
     *
     * @return an {@code Optional<Sheet>} containing the sheet if present, otherwise an empty {@code Optional}.
     */
    public Optional<Sheet> getSheet() {
        return sheetViewDelegate.getSheet();
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

    /**
     * Calculates the minimum x-coordinate in view coordinates for the segment view.
     *
     * @return the minimum x-coordinate value in view coordinates.
     */
    public float getXMinInViewCoordinates() {
        float x = sheetViewDelegate.getColumnPos(getStartColumn());
        return getTransformation().transform(x, 0).x();
    }

    /**
     * Retrieves the minimum y-coordinate in view coordinates.
     *
     * @return the minimum y-coordinate in view coordinates as a float
     */
    public float getYMinInViewCoordinates() {
        float y = sheetViewDelegate.getRowPos(getStartRow());
        return getTransformation().transform(0, y).y();
    }

    /**
     * Retrieves the width of the segment in points.
     *
     * @return the width of the segment in points
     */
    public float getWidthInPoints() {
        return widthInPoints;
    }

    /**
     * Retrieves the height of the segment view in points.
     *
     * @return the height in points.
     */
    public float getHeightInPoints() {
        return heightInPoints;
    }

    /**
     * Retrieves the width of the segment view in pixels.
     *
     * @return the width of the segment view in pixels
     */
    public float getWidthInPixels() {
        return widthInPixels;
    }

    /**
     * Retrieves the height of the segment view in pixels.
     *
     * @return the height in pixels as a float.
     */
    public float getHeightInPixels() {
        return heightInPixels;
    }

    /**
     * Determines whether the segment view is located to the left of a split column in the associated sheet.
     *
     * @return {@code true} if the segment view is to the left of the split column, or if the sheet is not present and
     * the start column index is less than or equal to 0; otherwise, {@code false}
     */
    public boolean isLeftOfSplit() {
        return getSheet().map(sheet -> getEndColumn() <= sheet.getSplitColumn()).orElse(getStartColumn() <= 0);
    }

    /**
     * Determines if the segment view is positioned above the split row in the sheet.
     *
     * @return {@code true} if the end row of the segment view is less than or equal to
     *         the split row of the sheet; if the sheet is not present, returns {@code true}
     *         if the starting row index is less than or equal to zero.
     */
    public boolean isAboveSplit() {
        return getSheet().map(sheet -> getEndRow() <= sheet.getSplitRow()).orElse(getStartRow() <= 0);
    }

    /**
     * Determines whether the segment view contains a horizontal line.
     *
     * @return {@code true} if the sheet's split row is greater than zero and the end row is
     * less than or equal to the split row, or if the start row is less than or equal to zero
     * when the sheet is not present. Otherwise, returns {@code false}.
     */
    public boolean hasHLine() {
        return getSheet().map(sheet -> sheet.getSplitRow() > 0 && getEndRow() <= sheet.getSplitRow()).orElse(getStartRow() <= 0);
    }

    /**
     * Determines if the segment view intersects with a vertical split line in the sheet.
     *
     * @return {@code true} if there is a vertical split line within the bounds of this segment view, {@code false} otherwise.
     */
    public boolean hasVLine() {
        return getSheet().map(sheet -> sheet.getSplitColumn() > 0 && getEndColumn() <= sheet.getSplitColumn()).orElse(getStartColumn() <= 0);
    }

    /**
     * Updates the layout of the segment view based on the current sheet view settings.
     * This includes recalculating the dimensions of the visible area in points and pixels
     * and adjusting offsets for column and row labels depending on the split configuration.
     *
     * The method first secures a read lock on the sheet to ensure thread safety while it performs
     * the following steps:
     *
     * <ul>
     *   <li>Invokes the {@code updateLayout()} method on the {@code sheetViewDelegate} to ensure the sheet layout is current.</li>
     *   <li>Calculates the total width of the visible area, including optional margins for row labels and split lines.</li>
     *   <li>Calculates the total height of the visible area, similarly including margins for column labels and split lines.</li>
     *   <li>Determines the horizontal and vertical offsets based on the starting positions of columns and rows relative to splits.</li>
     *   <li>Converts the calculated width and height into pixels using the current scaling factors.</li>
     *   <li>Sets the display size in the owner view to the newly calculated pixel dimensions.</li>
     * </ul>
     *
     * The method is designed to handle various display configurations, including different split views
     * where segments might be positioned above, below, left, or right of splits.
     */
    public void updateLayout() {
        getSheet().ifPresent(sheet -> {
            Lock lock = sheet.readLock();
            lock.lock();
            try {
                // update the sheet layout first
                sheetViewDelegate.updateLayout();

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
        });
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

    /**
     * Gets the starting row index for the segment view.
     *
     * @return the starting row index for the quadrant
     */
    public int getStartRow() {
        return quadrant.startRow(sheetViewDelegate.getRowCount(), sheetViewDelegate.getSplitRow());
    }

    /**
     * Retrieves the end column index for the segment view.
     *
     * @return The end column index (exclusive)
     */
    public int getEndColumn() {
        return quadrant.endColumn(sheetViewDelegate.getColumnCount(), sheetViewDelegate.getSplitColumn());
    }

    /**
     * Get the index of the last row to display based on the quadrant's end row logic.
     *
     * @return the index of the last row to display, taking into account the total number
     *         of rows and the split row value.
     */
    public int getEndRow() {
        return quadrant.endRow(sheetViewDelegate.getRowCount(), sheetViewDelegate.getSplitRow());
    }

    /**
     * Combines the translation transformation based on the x and y offsets and the existing transformation
     * provided by the sheet view delegate.
     *
     * @return an {@code AffineTransformation2f} object representing the combined transformation.
     */
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
