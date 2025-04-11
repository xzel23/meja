/*
 *
 */
package com.dua3.meja.ui;

/**
 * The SegmentView interface defines an interface views that display a segment of a sheet, defined by one of the four
 * {@link com.dua3.meja.ui.SheetView.Quadrant} of the sheet.
 */
@FunctionalInterface
public interface SegmentView {

    /**
     * Sets the size of the view on the display.
     *
     * @param w The width of the view in pixels.
     * @param h The height of the view in pixels.
     */
    void updateViewSize(float w, float h);

}
