/*
 *
 */
package com.dua3.meja.ui.swing;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetPainterBase;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class SwingSheetPainter extends SheetPainterBase<Graphics2D> {

    private final CellRenderer cellRenderer;
    private final JLabel labelPainter = new JLabel();

    private float labelHeight;
    private float labelWidth;

    SwingSheetPainter(SwingSheetView sheetView, CellRenderer cellRenderer) {
        super(sheetView.getDelegate());

        this.cellRenderer = cellRenderer;

        // setup painter for row and column headers
        labelPainter.setOpaque(true);
        labelPainter.setHorizontalAlignment(SwingConstants.CENTER);
        labelPainter.setVerticalAlignment(SwingConstants.CENTER);
        labelPainter.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, SwingUtil.toAwtColor(getGridColor())));
    }

    @Override
    protected void drawBackground(Graphics2D g) {
        Rectangle r = g.getClipBounds();
        g.setColor(SwingUtil.toAwtColor(delegate.getBackground().brighter()));
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    @Override
    protected void drawLabel(Graphics2D g, Rectangle2f r, String text) {
        final java.awt.Rectangle rd = rectS2D(r);
        labelPainter.setBounds(0, 0, rd.width, rd.height);
        labelPainter.setText(text);
        labelPainter.paint(g.create(rd.x, rd.y, rd.width, rd.height));
    }

    @Override
    protected void setColor(Graphics2D g, Color color) {
        g.setColor(SwingUtil.toAwtColor(color));
    }

    @Override
    protected void strokeLine(Graphics2D g, float x1, float y1, float x2, float y2) {
        g.drawLine(delegate.xS2D(x1), delegate.yS2D(y1), delegate.wS2D(x2), delegate.hS2D(y2));
    }

    @Override
    protected void strokeRect(Graphics2D g, float x, float y, float width, float height) {
        final int xd = delegate.xS2D(x);
        final int yd = delegate.yS2D(y);
        final int wd = delegate.xS2D(x + width) - xd;
        final int hd = delegate.yS2D(y + height) - yd;
        g.drawRect(xd, yd, wd, hd);
    }

    @Override
    protected void fillRect(Graphics2D g, float x, float y, float width, float height) {
        final int xd = delegate.xS2D(x);
        final int yd = delegate.yS2D(y);
        final int wd = delegate.xS2D(x + width) - xd;
        final int hd = delegate.yS2D(y + height) - yd;
        g.fillRect(xd, yd, wd, hd);
    }

    @Override
    protected void setStroke(Graphics2D g, Color color, float width) {
        g.setColor(SwingUtil.toAwtColor(color));
        g.setStroke(new BasicStroke(width));
    }

    @Override
    protected Rectangle2f getClipBounds(Graphics2D g) {
        return rectD2S(g.getClipBounds());
    }

    private Rectangle2f rectD2S(Rectangle r) {
        return Rectangle2f.of(
                delegate.xD2S(r.x),
                delegate.yD2S(r.y),
                delegate.wD2S(r.width),
                delegate.hD2S(r.height)
        );
    }

    private Rectangle rectS2D(Rectangle2f r) {
        return new Rectangle(
                delegate.xS2D(r.x()),
                delegate.yS2D(r.y()),
                delegate.wS2D(r.width()),
                delegate.hS2D(r.height())
        );
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
    protected void render(Graphics2D g, Cell cell, Rectangle2f rect, Rectangle2f clipRect) {
        java.awt.Rectangle rectD = rectS2D(rect);
        java.awt.Rectangle clipRectD = rectS2D(clipRect);

        cellRenderer.render(g, cell, rectD, clipRectD, delegate.getScale());
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
            labelWidth = delegate.wD2S(labelSize.width);
            labelHeight = delegate.hD2S(labelSize.height);
        }
    }

}
