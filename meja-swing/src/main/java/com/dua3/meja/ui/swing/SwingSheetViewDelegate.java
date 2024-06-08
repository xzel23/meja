package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.math.geometry.Scale2f;

import java.util.function.Function;

public class SwingSheetViewDelegate extends SheetViewDelegate {

    private final CellRenderer cellRenderer;
    private final SwingSheetPainter sheetPainter;
    private Scale2f displayScale = Scale2f.identity();

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

    public void setDisplayScale(Scale2f displayScale) {
        this.displayScale = displayScale;
    }

    public Scale2f getDisplayScale() {
        return displayScale;
    }

}
