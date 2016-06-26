/*
 *
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.ui.Rectangle;
import com.dua3.meja.ui.SheetPainterBase;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class SwingSheetPainter extends SheetPainterBase<SwingGraphicsContext> {

    private final float scale = 1f;
    /**
     * Horizontal padding.
     */
    private final int paddingX = 2;

    /**
     * Vertical padding.
     */
    private final int paddingY = 1;

    /**
     * Color used to draw the selection rectangle.
     */
    private Color selectionColor = Color.BLACK;

    private Color gridColor = Color.LIGHT_GRAY;

    /**
     * Width of the selection rectangle borders.
     */
    private final int selectionStrokeWidth = 4;
    private final SwingSheetView sheetView;
    private final CellRenderer renderer;

    SwingSheetPainter(SwingSheetView sheetView, CellRenderer renderer) {
        this.sheetView = sheetView;
        this.renderer = renderer;
    }

    @Override
    protected void beginDraw(SwingGraphicsContext gc) {
        // nop
    }

    @Override
    protected void endDraw(SwingGraphicsContext gc) {
        // nop
    }

    @Override
    protected int getMaxColumnWidth() {
        return 800;
    }

    @Override
    protected void drawBackground(SwingGraphicsContext gc) {
        final Graphics2D g = gc.graphics();
        java.awt.Rectangle r = g.getClipBounds();
        g.clearRect(r.x, r.y, r.width, r.height);
    }

    @Override
    protected double getPaddingX() {
        return paddingX;
    }

    @Override
    protected double getPaddingY() {
        return paddingY;
    }

    @Override
    protected Color getGridColor() {
        return gridColor;
    }

    @Override
    protected Color getSelectionColor() {
        return selectionColor;
    }

    @Override
    protected double getSelectionStrokeWidth() {
        return selectionStrokeWidth;
    }

    @Override
    protected void render(SwingGraphicsContext g, Cell cell, Rectangle rect, Rectangle clipRect) {
        java.awt.Rectangle rectD = sheetView.rectS2D(rect);
        java.awt.Rectangle clipRectD = sheetView.rectS2D(clipRect);

        renderer.render(g.graphics(), cell, rectD, clipRectD, scale);
    }

}
