/*
 *
 */
package com.dua3.meja.ui.swing;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.dua3.meja.ui.GraphicsContext;
import com.dua3.meja.ui.Rectangle;
import com.dua3.utility.Color;
import com.dua3.utility.swing.SwingUtil;

public final class SwingGraphicsContext implements GraphicsContext {

    private final Graphics2D g;
    private final SwingSheetView view;

    SwingGraphicsContext(Graphics g, SwingSheetView view) {
        this.g = (Graphics2D) g;
        this.view = view;
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        g.drawLine(xS2D(x1), yS2D(y1), wS2D(x2), hS2D(y2));
    }

    @Override
    public void drawRect(double x, double y, double width, double height) {
        final int xd = xS2D(x);
        final int yd = yS2D(y);
        final int wd = xS2D(x + width) - xd;
        final int hd = yS2D(y + height) - yd;
        g.drawRect(xd, yd, wd, hd);
    }

    @Override
    public void fillRect(double x, double y, double width, double height) {
        final int xd = xS2D(x);
        final int yd = yS2D(y);
        final int wd = xS2D(x + width) - xd;
        final int hd = yS2D(y + height) - yd;
        g.fillRect(xd, yd, wd, hd);
    }

    @Override
    public Rectangle getClipBounds() {
        return view.rectD2S(g.getClipBounds());
    }

    public Graphics2D graphics() {
        return g;
    }

    private int hS2D(double d) {
        return view.hS2D(d);
    }

    @Override
    public void setColor(Color color) {
        g.setColor(SwingUtil.toAwtColor(color));
    }

    @Override
    public void setStroke(Color color, double width) {
        g.setColor(SwingUtil.toAwtColor(color));
        g.setStroke(new BasicStroke((float) width));
    }

    @Override
    public void setXOR(boolean on) {
        if (on) {
            g.setXORMode(SwingUtil.toAwtColor(Color.WHITE));
        } else {
            g.setPaintMode();
        }
    }

    private int wS2D(double d) {
        return view.wS2D(d);
    }

    private int xS2D(double d) {
        return view.xS2D(d);
    }

    private int yS2D(double d) {
        return view.yS2D(d);
    }

}
