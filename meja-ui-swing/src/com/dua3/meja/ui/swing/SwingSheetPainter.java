/*
 *
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Color;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.Rectangle;
import com.dua3.meja.ui.SheetPainterBase;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class SwingSheetPainter extends SheetPainterBase<SwingGraphicsContext> {

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
    private static final Color SELECTION_COLOR = Color.BLACK;

    private static final Color GRID_COLOR = Color.GRAY;

    /**
     * Width of the selection rectangle borders.
     */
    private final int selectionStrokeWidth = 4;
    private final SwingSheetView sheetView;
    private final CellRenderer renderer;

    SwingSheetPainter(SwingSheetView sheetView, CellRenderer renderer) {
        this.sheetView = sheetView;
        this.renderer = renderer;

        // setup painter for row and column headers
        labelPainter.setOpaque(true);
        labelPainter.setHorizontalAlignment(SwingConstants.CENTER);
        labelPainter.setVerticalAlignment(SwingConstants.CENTER);
        labelPainter.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, MejaSwingHelper.toAwtColor(GRID_COLOR)));
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
        g.setColor(g.getBackground().brighter());
        g.fillRect(r.x, r.y, r.width, r.height);
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
        return GRID_COLOR;
    }

    @Override
    protected Color getSelectionColor() {
        return SELECTION_COLOR;
    }

    @Override
    protected double getSelectionStrokeWidth() {
        return selectionStrokeWidth;
    }

    @Override
    protected void render(SwingGraphicsContext g, Cell cell, Rectangle rect, Rectangle clipRect) {
        java.awt.Rectangle rectD = sheetView.rectS2D(rect);
        java.awt.Rectangle clipRectD = sheetView.rectS2D(clipRect);

        renderer.render(g.graphics(), cell, rectD, clipRectD, sheetView.getScale());
    }

    private final JLabel labelPainter = new JLabel();
    private double labelHeight = 0;
    private double labelWidth = 0;

    @Override
    protected void drawLabel(SwingGraphicsContext gc, Rectangle r, String text) {
        final java.awt.Rectangle rd = sheetView.rectS2D(r);
        labelPainter.setBounds(0,0,rd.width,rd.height);
        labelPainter.setText(text);
        labelPainter.paint(gc.graphics().create(rd.x, rd.y, rd.width,rd.height));
    }

    @Override
    protected double getRowLabelWidth() {
        return labelWidth;
    }

    @Override
    protected double getColumnLabelHeight() {
        return labelHeight;
    }

    @Override
    public void update(Sheet sheet) {
        super.update(sheet);

        if (sheet != null) {
            // create a string with the maximum number of digits needed to
            // represent the highest row number (use a string only consisting
            // of zeroes instead of the last row number because a proportional
            // font might be used)
            StringBuilder sb = new StringBuilder("0");
            for (int i = 1; i <= getNumberOfRows(); i *= 10) {
                sb.append('0');
            }
            labelPainter.setText(new String(sb));
            final Dimension labelSize = labelPainter.getPreferredSize();
            labelWidth = sheetView.wD2S(labelSize.width);
            labelHeight = sheetView.hD2S(labelSize.height);
        }
    }

    @Override
    protected double getLabelHeight() {
        return labelHeight;
    }

    @Override
    protected double getLabelWidth() {
        return labelWidth;
    }

}
