/*
 *
 */
package com.dua3.meja.ui.javafx;

import java.util.function.IntSupplier;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class SegmentView extends SheetControl {

    private final IntSupplier startRow;
    private final IntSupplier endRow;
    private final IntSupplier startColumn;
    private final IntSupplier endColumn;

    public SegmentView(JfxSheetView sheetView, IntSupplier startRow, IntSupplier endRow, IntSupplier startColumn, IntSupplier endColumn) {
        super(sheetView);
        this.startRow = startRow;
        this.endRow = endRow;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
    }

    public int getBeginRow() {
        return startRow.getAsInt();
    }

    public int getEndRow() {
        return endRow.getAsInt();
    }

    public int getStartColumn() {
        return startColumn.getAsInt();
    }

    public int getEndColumn() {
        return endColumn.getAsInt();
    }
}
