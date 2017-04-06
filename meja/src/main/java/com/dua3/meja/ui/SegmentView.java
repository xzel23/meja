/*
 *
 */
package com.dua3.meja.ui;

import java.util.concurrent.locks.Lock;

import com.dua3.meja.model.Sheet;

/**
 *
 * @author Axel Howind
 * @param <SV>
 * @param <GC>
 */
public interface SegmentView<SV extends SheetView, GC extends GraphicsContext> {

    Sheet getSheet();

    SheetPainterBase<SV, GC> getSheetPainter();

    default boolean hasColumnHeaders() {
        return getEndRow() <= getSheet().getSplitRow();
    }

    default boolean hasRowHeaders() {
        return getEndColumn() <= getSheet().getSplitColumn();
    }

    default boolean hasHLine() {
        return getEndRow() > 0 && getEndRow() <= getSheet().getLastRowNum();
    }

    default boolean hasVLine() {
        return getEndColumn() > 0 && getEndColumn() <= getSheet().getLastColNum();
    }

    default void updateLayout() {
        Sheet sheet = getSheet();
        if (sheet == null) {
          return;
        }

        Lock lock = sheet.readLock();
        try {
          lock.lock();

          SheetPainterBase<SV, GC> sheetPainter = getSheetPainter();

          // the width is the width for the labels showing row names ...
          double width = hasRowHeaders() ? sheetPainter.getRowLabelWidth() : 1;

          // ... plus the width of the columns displayed ...
          width += sheetPainter.getColumnPos(getEndColumn()) - sheetPainter.getColumnPos(getBeginColumn());

          // ... plus 1 pixel for drawing a line at the split position.
          if (hasVLine()) {
              width += 1;
          }

          // the height is the height for the labels showing column names ...
          double height = hasColumnHeaders() ? sheetPainter.getColumnLabelHeight() : 1;

          // ... plus the height of the rows displayed ...
          height += sheetPainter.getRowPos(getEndRow()) - sheetPainter.getRowPos(getBeginRow());

          // ... plus 1 pixel for drawing a line below the lines above the split.
          if (hasHLine()) {
              height += 1;
          }

          setViewSize(width, height);
        } finally {
          lock.unlock();
        }
    }

    int getBeginColumn();

    int getEndColumn();

    int getBeginRow();

    int getEndRow();

    void setViewSize(double width, double height);

}
