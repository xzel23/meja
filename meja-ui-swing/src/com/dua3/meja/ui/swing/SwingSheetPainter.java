/*
 *
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.ui.GraphicsContext;
import com.dua3.meja.ui.Rectangle;
import com.dua3.meja.ui.SheetPainterBase;
import java.awt.Color;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class SwingSheetPainter extends SheetPainterBase {

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
    private final CellRenderer renderer;

    SwingSheetPainter(CellRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    protected void beginDraw(GraphicsContext gc) {
        // nop
    }

    @Override
    protected void endDraw(GraphicsContext gc) {
        // nop
    }

    @Override
    protected int getMaxColumnWidth() {
        return 800;
    }

    @Override
    protected void drawBackground(GraphicsContext gc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    protected void render(GraphicsContext g, Cell cell, Rectangle textRect, Rectangle clipRect) {
        renderer.render(g, cell, cellRect, clipRect, scale);
    }

}
