package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SheetViewDelegate;

import java.util.function.Function;

public class SwingSheetViewDelegate extends SheetViewDelegate {

    private final CellRenderer cellRenderer;
    private final SwingSheetPainter sheetPainter;

    public SwingSheetViewDelegate(
            SwingSheetView owner,
            Function<SwingSheetViewDelegate, CellRenderer> cellRendererFactory
    ) {
        super(owner);
        this.cellRenderer = cellRendererFactory.apply(this);
        this.sheetPainter = new SwingSheetPainter(this, cellRenderer);
    }

    public SwingSheetPainter getSheetPainter() {
        return sheetPainter;
    }

}
