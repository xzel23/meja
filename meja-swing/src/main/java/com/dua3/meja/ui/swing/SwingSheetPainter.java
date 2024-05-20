/*
 *
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.ui.SheetPainterBase;
import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class SwingSheetPainter extends SheetPainterBase<Graphics2D> {

    private final CellRenderer cellRenderer;
    private final JLabel labelPainter = new JLabel();
    private final SwingSheetViewDelegate delegate;

    SwingSheetPainter(SwingSheetViewDelegate svDelegate, CellRenderer cellRenderer) {
        this.delegate = svDelegate;
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
        final java.awt.Rectangle rd = delegate.rectS2D(r);
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
        g.drawLine(
                delegate.xS2Di(x1),
                delegate.yS2Di(y1),
                delegate.wS2Di(x2),
                delegate.hS2Di(y2)
        );
    }

    @Override
    protected void strokeRect(Graphics2D g, float x, float y, float width, float height) {
        final int xd = delegate.xS2Di(x);
        final int yd = delegate.yS2Di(y);
        final int wd = delegate.xS2Di(x + width) - xd;
        final int hd = delegate.yS2Di(y + height) - yd;
        g.drawRect(xd, yd, wd, hd);
    }

    @Override
    protected void fillRect(Graphics2D g, float x, float y, float width, float height) {
        final int xd = delegate.xS2Di(x);
        final int yd = delegate.yS2Di(y);
        final int wd = delegate.xS2Di(x + width) - xd;
        final int hd = delegate.yS2Di(y + height) - yd;
        g.fillRect(xd, yd, wd, hd);
    }

    @Override
    protected void setStroke(Graphics2D g, Color color, float width) {
        g.setColor(SwingUtil.toAwtColor(color));
        g.setStroke(new BasicStroke(width));
    }

    @Override
    protected Rectangle2f getClipBounds(Graphics2D g) {
        return delegate.rectD2S(g.getClipBounds());
    }

    @Override
    protected SheetViewDelegate getDelegate() {
        return delegate;
    }

    @Override
    protected void render(Graphics2D g, Cell cell, Rectangle2f rect, Rectangle2f clipRect) {
        java.awt.Rectangle rectD = delegate.rectS2D(rect);
        java.awt.Rectangle clipRectD = delegate.rectS2D(clipRect);

        cellRenderer.render(g, cell, rectD, clipRectD, delegate.getScale());
    }

}
