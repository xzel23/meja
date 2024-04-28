/*
 *
 */
package com.dua3.meja.ui.swing;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetPainterBase;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class SwingSheetPainter extends SheetPainterBase<SwingSheetView, SwingGraphicsContext> {

    private final CellRenderer cellRenderer;
    private final JLabel labelPainter = new JLabel();

    private float labelHeight;
    private float labelWidth;

    SwingSheetPainter(SwingSheetView sheetView, CellRenderer cellRenderer) {
        super(sheetView);

        this.cellRenderer = cellRenderer;

        // setup painter for row and column headers
        labelPainter.setOpaque(true);
        labelPainter.setHorizontalAlignment(SwingConstants.CENTER);
        labelPainter.setVerticalAlignment(SwingConstants.CENTER);
        labelPainter.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, SwingUtil.toAwtColor(getGridColor())));
    }

    @Override
    protected void drawBackground(SwingGraphicsContext gc) {
        final Graphics2D g = gc.graphics();
        Rectangle r = g.getClipBounds();
        g.setColor(sheetView.getBackground().brighter());
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    @Override
    protected void drawLabel(SwingGraphicsContext gc, Rectangle2f r, String text) {
        final java.awt.Rectangle rd = sheetView.rectS2D(r);
        labelPainter.setBounds(0, 0, rd.width, rd.height);
        labelPainter.setText(text);
        labelPainter.paint(gc.graphics().create(rd.x, rd.y, rd.width, rd.height));
    }

    @Override
    protected float getColumnLabelHeight() {
        return labelHeight;
    }

    @Override
    protected float getRowLabelWidth() {
        return labelWidth;
    }

    @Override
    protected void render(SwingGraphicsContext g, Cell cell, Rectangle2f rect, Rectangle2f clipRect) {
        java.awt.Rectangle rectD = sheetView.rectS2D(rect);
        java.awt.Rectangle clipRectD = sheetView.rectS2D(clipRect);

        cellRenderer.render(g.graphics(), cell, rectD, clipRectD, sheetView.getScale());
    }

    @Override
    public void update(@Nullable Sheet sheet) {
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
            final Dimension labelSize = labelPainter.getPreferredSize();
            labelWidth = sheetView.wD2S(labelSize.width);
            labelHeight = sheetView.hD2S(labelSize.height);
        }
    }

}
