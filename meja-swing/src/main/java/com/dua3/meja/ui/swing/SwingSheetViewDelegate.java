package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SheetPainter;
import com.dua3.meja.ui.SheetViewDelegate;

import java.util.function.Function;

public class SwingSheetViewDelegate extends SheetViewDelegate {

    private final CellRenderer cellRenderer;
    private final SheetPainter sheetPainter;

    public SwingSheetViewDelegate(
            Sheet sheet,
            SwingSheetView owner,
            Function<SwingSheetViewDelegate, CellRenderer> cellRendererFactory
    ) {
        super(sheet, owner);
        this.cellRenderer = cellRendererFactory.apply(this);
        this.sheetPainter = new SheetPainter(this, cellRenderer);
    }

    public SheetPainter getSheetPainter() {
        return sheetPainter;
    }

}
