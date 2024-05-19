package com.dua3.meja.ui;

import com.dua3.meja.model.Sheet;

import java.util.concurrent.locks.Lock;
import java.util.function.IntSupplier;

public class SegmentViewDelegate<SV extends SheetView, GC, R> {
    private final SegmentView<SV, GC, R> owner;
    private final SheetViewDelegate<GC, R> svDelegate;
    private final IntSupplier startRow;
    private final IntSupplier endRow;
    private final IntSupplier startColumn;
    private final IntSupplier endColumn;

    public SegmentViewDelegate(
            SegmentView<SV, GC, R> owner,
            SheetViewDelegate<GC, R> sheetViewDelegate,
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

    public int getBeginColumn() {
        return startColumn.getAsInt();
    }

    public int getBeginRow() {
        return startRow.getAsInt();
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

    public SheetPainterBase<GC, R> getSheetPainter() {
        return svDelegate.getSheetPainter();
    }

    public void setViewSize(float wd, float hd) {
        float w = svDelegate.wS2D(wd);
        float h = svDelegate.hS2D(hd);
        owner.setViewSizeOnDisplay(w,h);
    }

    public float getXMinInViewCoordinates() {
        float x = svDelegate.getSheetPainter().getColumnPos(getBeginColumn());
        if (hasRowHeaders()) {
            x -= svDelegate.getSheetPainter().getRowLabelWidth();
        }
        return svDelegate.xS2D(x);
    }

    public float getYMinInViewCoordinates() {
        float y = svDelegate.getSheetPainter().getRowPos(getBeginRow());
        if (hasColumnHeaders()) {
            y -= svDelegate.getSheetPainter().getColumnLabelHeight();
        }
        return svDelegate.yS2D(y);
    }

    public boolean hasRowHeaders() {
        return getEndColumn() <= getSheet().getSplitColumn();
    }

    public boolean hasColumnHeaders() {
        return getEndRow() <= getSheet().getSplitRow();
    }

    public boolean hasHLine() {
        return getEndRow() > 0 && getEndRow() <= getSheet().getLastRowNum();
    }

    public boolean hasVLine() {
        return getEndColumn() > 0 && getEndColumn() <= getSheet().getLastColNum();
    }

    public void updateLayout() {
        Sheet sheet = getSheet();
        if (sheet == null) {
            return;
        }

        Lock lock = sheet.readLock();
        lock.lock();
        try {

            SheetPainterBase<GC, R> sheetPainter = getSheetPainter();

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
