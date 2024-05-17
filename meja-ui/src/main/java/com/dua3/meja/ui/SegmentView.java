/*
 *
 */
package com.dua3.meja.ui;

import com.dua3.meja.model.Sheet;

import java.util.concurrent.locks.Lock;

/**
 * @param <SV> the concrete class implementing SheetView
 * @param <G> the concrete class implementing GraphicsContext
 * @author Axel Howind
 */
public interface SegmentView<SV extends SheetView, G> {

    int getBeginColumn();

    int getBeginRow();

    int getEndColumn();

    int getEndRow();

    Sheet getSheet();

    SheetPainterBase<G> getSheetPainter();

    default boolean hasColumnHeaders() {
        return getEndRow() <= getSheet().getSplitRow();
    }

    default boolean hasHLine() {
        return getEndRow() > 0 && getEndRow() <= getSheet().getLastRowNum();
    }

    default boolean hasRowHeaders() {
        return getEndColumn() <= getSheet().getSplitColumn();
    }

    default boolean hasVLine() {
        return getEndColumn() > 0 && getEndColumn() <= getSheet().getLastColNum();
    }

    void setViewSize(float width, float height);

    default void updateLayout() {
        Sheet sheet = getSheet();
        if (sheet == null) {
            return;
        }

        Lock lock = sheet.readLock();
        lock.lock();
        try {

            SheetPainterBase<G> sheetPainter = getSheetPainter();

            // the width is the width for the labels showing row names ...
            float width = hasRowHeaders() ? sheetPainter.getRowLabelWidth() : 1;

            // ... plus the width of the columns displayed ...
            width += sheetPainter.getColumnPos(getEndColumn()) - sheetPainter.getColumnPos(getBeginColumn());

            // ... plus 1 pixel for drawing a line at the split position.
            if (hasVLine()) {
                width += 1;
            }

            // the height is the height for the labels showing column names ...
            float height = hasColumnHeaders() ? sheetPainter.getColumnLabelHeight() : 1;

            // ... plus the height of the rows displayed ...
            height += sheetPainter.getRowPos(getEndRow()) - sheetPainter.getRowPos(getBeginRow());

            // ... plus 1 pixel for drawing a line below the lines above the
            // split.
            if (hasHLine()) {
                height += 1;
            }

            setViewSize(width, height);
        } finally {
            lock.unlock();
        }
    }

}
