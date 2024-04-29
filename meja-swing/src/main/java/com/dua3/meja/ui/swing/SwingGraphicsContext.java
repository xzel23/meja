/*
 *
 */
package com.dua3.meja.ui.swing;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.ui.GraphicsContext;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;

public final class SwingGraphicsContext implements GraphicsContext {

    private final Graphics2D g;
    private final SwingSheetView view;

    SwingGraphicsContext(Graphics g, SwingSheetView view) {
        this.g = (Graphics2D) g;
        this.view = view;
    }

    @Override
    public void drawLine(float x1, float y1, float x2, float y2) {
        g.drawLine(xS2D(x1), yS2D(y1), wS2D(x2), hS2D(y2));
    }

    @Override
    public void drawRect(float x, float y, float width, float height) {
        final int xd = xS2D(x);
        final int yd = yS2D(y);
        final int wd = xS2D(x + width) - xd;
        final int hd = yS2D(y + height) - yd;
        g.drawRect(xd, yd, wd, hd);
    }

    @Override
    public void fillRect(float x, float y, float width, float height) {
        final int xd = xS2D(x);
        final int yd = yS2D(y);
        final int wd = xS2D(x + width) - xd;
        final int hd = yS2D(y + height) - yd;
        g.fillRect(xd, yd, wd, hd);
    }

    @Override
    public Rectangle2f getClipBounds() {
        return view.rectD2S(g.getClipBounds());
    }

    public Graphics2D graphics() {
        return g;
    }

    private int hS2D(float f) {
        return view.hS2D(f);
    }

    @Override
    public void setColor(Color color) {
        g.setColor(SwingUtil.toAwtColor(color));
    }

    @Override
    public void setStroke(Color color, float width) {
        g.setColor(SwingUtil.toAwtColor(color));
        g.setStroke(new BasicStroke(width));
    }

    /*
    @Override
    public void setXOR(boolean on) {
        if (on) {
            g.setXORMode(SwingUtil.toAwtColor(Color.WHITE));
        } else {
            g.setPaintMode();
        }
    }
     */

    private int wS2D(float f) {
        return view.wS2D(f);
    }

    private int xS2D(float f) {
        return view.xS2D(f);
    }

    private int yS2D(float f) {
        return view.yS2D(f);
    }

}
