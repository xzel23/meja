package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SheetPainter;
import com.dua3.meja.ui.SheetViewDelegate;

import java.util.function.Function;

/**
 * The {@code SwingSheetViewDelegate} class is responsible for managing the interaction
 * and rendering logic for a {@code SwingSheetView} in the context of a given {@code Sheet}.
 * It connects the sheet data model with the visual representation using rendering
 * and painting mechanisms.
 * <p>
 * This class utilizes a {@link CellRenderer} to specify how individual cells in the sheet
 * should be rendered, and a {@link SheetPainter} to manage the overall visual rendering
 * of the entire sheet.
 * <p>
 * Inherits from {@link SheetViewDelegate}, providing specific functionality for Swing-based
 * sheet views.
 */
public class SwingSheetViewDelegate extends SheetViewDelegate {

    private final SheetPainter sheetPainter;

    /**
     * Constructs a new {@code SwingSheetViewDelegate} instance, initializing it with the
     * provided {@code Sheet}, {@code SwingSheetView} owner, and cell renderer factory.
     *
     * @param sheet the {@code Sheet} object to be associated with this delegate.
     * @param owner the {@code SwingSheetView} that owns this delegate.
     * @param cellRendererFactory a factory function that provides a {@code CellRenderer}
     *                            instance using this {@code SwingSheetViewDelegate}.
     */
    public SwingSheetViewDelegate(
            Sheet sheet,
            SwingSheetView owner,
            Function<SwingSheetViewDelegate, CellRenderer> cellRendererFactory
    ) {
        super(sheet, owner);
        CellRenderer cellRenderer = cellRendererFactory.apply(this);
        this.sheetPainter = new SheetPainter(this, cellRenderer);
    }

    /**
     * Retrieves the {@link SheetPainter} associated with this delegate.
     *
     * @return the {@link SheetPainter} responsible for rendering the sheet.
     */
    public SheetPainter getSheetPainter() {
        return sheetPainter;
    }

}
