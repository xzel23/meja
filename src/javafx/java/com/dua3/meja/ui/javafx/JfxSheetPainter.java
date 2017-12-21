/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.Rectangle;
import com.dua3.meja.ui.SheetPainterBase;
import com.dua3.utility.Color;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;

/**
 *
 * @author Axel Howind
 */
class JfxSheetPainter extends SheetPainterBase<JfxSheetView, JfxGraphicsContext> {

    // private final CellRenderer cellRenderer;
    private final Label labelPainter = new Label();

    private double labelHeight = 0;
    private double labelWidth = 0;

    JfxSheetPainter(JfxSheetView sheetView/* , CellRenderer cellRenderer */) {
        super(sheetView);
        // this.cellRenderer = cellRenderer;

        // setup painter for row and column headers
        labelPainter.setOpacity(1.0);
        labelPainter.setAlignment(Pos.CENTER);
        // labelPainter.setBorder(...);
    }

    @Override
    protected void drawBackground(JfxGraphicsContext gc) {
        Rectangle r = gc.getClipBounds();
        gc.setColor(Color.WHITE);
        gc.fillRect(r.getX(), r.getY(), r.getW(), r.getH());
    }

    @Override
    protected void drawLabel(JfxGraphicsContext gc, Rectangle r, String text) {
        GraphicsContext g = gc.graphics();
        g.setFill(javafx.scene.paint.Color.LIGHTGREY);
        double x = r.getX(), y = r.getY(), w = r.getW(), h = r.getH();
        g.fillRect(x, y, w, h);
        g.setFill(javafx.scene.paint.Color.BLACK);
        g.strokeText(text, x + w / 2, y + h / 2, w);
    }

    @Override
    protected double getColumnLabelHeight() {
        return labelHeight;
    }

    @Override
    protected double getRowLabelWidth() {
        return labelWidth;
    }

    @Override
    protected void render(JfxGraphicsContext g, Cell cell, Rectangle textRect, Rectangle clipRect) {
        // FIXME
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
            for (int i = 1; i <= getRowCount(); i *= 10) {
                sb.append('0');
            }
            labelPainter.setText(new String(sb));
            Bounds labelSize = labelPainter.getBoundsInLocal();
            labelWidth = labelSize.getWidth();
            labelHeight = labelSize.getHeight();
        }
    }
}
