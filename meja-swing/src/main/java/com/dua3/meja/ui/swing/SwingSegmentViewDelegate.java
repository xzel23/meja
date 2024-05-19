package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.meja.ui.SheetViewDelegate;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.function.IntSupplier;

public class SwingSegmentViewDelegate extends SegmentViewDelegate<SwingSheetView, Graphics2D, Rectangle> {
    public SwingSegmentViewDelegate(SegmentView<SwingSheetView, Graphics2D, Rectangle> owner, SheetViewDelegate<Graphics2D, Rectangle> sheetViewDelegate, IntSupplier startRow, IntSupplier endRow, IntSupplier startColumn, IntSupplier endColumn) {
        super(owner, sheetViewDelegate, startRow, endRow, startColumn, endColumn);
    }
}
