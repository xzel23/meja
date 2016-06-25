/*
 *
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.GraphicsContext;
import com.dua3.meja.ui.Rectangle;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public final class SwingGraphicsContext implements GraphicsContext {

    private final Graphics2D g;

    private static int round(double d) {
        return (int) Math.round(d);
    }

    SwingGraphicsContext(Graphics g) {
        this.g = (Graphics2D) g;
    }

    @Override
    public void setColor(Color color) {
        g.setColor(color);
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2) {
        g.drawLine(round(x1), round(y1), round(x2), round(y2));
    }

    @Override
    public void drawRect(double x, double y, double width, double height) {
        g.drawRect(round(x), round(y), round(width), round(height));
    }

    @Override
    public void fillRect(double x, double y, double width, double height) {
        g.fillRect(round(x), round(y), round(width), round(height));
    }

    @Override
    public void setStroke(Color color, double width) {
        g.setColor(color);
        g.setStroke(new BasicStroke((float) width));
    }

    @Override
    public Rectangle getClipBounds() {
        return convertRectangle(g.getClipBounds());
    }

    private Rectangle convertRectangle(java.awt.Rectangle clipBounds) {
        return new Rectangle(clipBounds.x, clipBounds.y, clipBounds.width,clipBounds.height);
    }

}
