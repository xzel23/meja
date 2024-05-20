package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.meja.ui.SheetViewDelegate;

import java.util.function.IntSupplier;

public class SwingSegmentViewDelegate extends SegmentViewDelegate {
    public SwingSegmentViewDelegate(SegmentView owner, SheetViewDelegate sheetViewDelegate, IntSupplier startRow, IntSupplier endRow, IntSupplier startColumn, IntSupplier endColumn) {
        super(owner, sheetViewDelegate, startRow, endRow, startColumn, endColumn);
    }
}
