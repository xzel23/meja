/*
 *
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SheetPainterBase;
import com.dua3.meja.ui.SheetViewDelegate;

public class SwingSheetPainter extends SheetPainterBase {

    private final SwingSheetViewDelegate delegate;

    SwingSheetPainter(SwingSheetViewDelegate svDelegate, CellRenderer cellRenderer) {
        super(new CellRenderer(svDelegate));
        this.delegate = svDelegate;
    }

    @Override
    protected SheetViewDelegate getDelegate() {
        return delegate;
    }

}
