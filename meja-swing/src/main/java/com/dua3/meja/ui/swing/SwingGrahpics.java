package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.Graphics;
import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.text.Font;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class SwingGrahpics implements Graphics {
    private static final AwtFontUtil FONT_UTIL = AwtFontUtil.getInstance();

    private Graphics2D g2d;
    private float dx = 0f;
    private float dy = 0f;
    private float s = 1f;
    private java.awt.Color textColor = java.awt.Color.BLACK;

    public SwingGrahpics(Graphics2D g2d) {
        this.g2d = g2d;
    }

    public int xL2D(float x) {
        return Math.round(s*(x+dx));
    }

    public int yL2D(float y) {
        return Math.round(s*(y+dy));
    }

    public int wL2D(float w) {
        return Math.round(s*w);
    }

    public int hL2D(float h) {
        return Math.round(s*h);
    }

    @Override
    public Rectangle2f getBounds() {
        return convert(g2d.getClipBounds());
    }

    private Rectangle2f convert(Rectangle r) {
        return Rectangle2f.of(r.x, r.y, r.width, r.height);
    }

    @Override
    public void setTransformation(Transformation t) {
        this.dx = t.dx();
        this.dy = t.dy();
        this.s = t.s();
    }

    @Override
    public Transformation getTransformation() {
        return new Transformation(dx, dy, s);
    }

    @Override
    public void beginDraw() {
        // nop
    }

    @Override
    public void endDraw() {
        // nop
    }

    @Override
    public void setColor(Color color) {
        g2d.setColor(SwingUtil.toAwtColor(color));
    }

    @Override
    public void translate(float dx, float dy) {
        this.dx += dx;
        this.dy += dy;
    }

    @Override
    public void setTranslate(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void scale(float s) {
        this.s = s;
    }

    @Override
    public void strokeLine(float x1, float y1, float x2, float y2) {
        g2d.drawLine(
                xL2D(x1),
                yL2D(y1),
                wL2D(x2),
                hL2D(y2)
        );
    }

    @Override
    public void strokeRect(float x, float y, float width, float height) {
        final int xd = xL2D(x);
        final int yd = yL2D(y);
        final int wd = xL2D(x + width) - xd;
        final int hd = yL2D(y + height) - yd;
        g2d.drawRect(xd, yd, wd, hd);
    }

    @Override
    public void fillRect(float x, float y, float width, float height) {
        final int xd = xL2D(x);
        final int yd = yL2D(y);
        final int wd = xL2D(x + width) - xd;
        final int hd = yL2D(y + height) - yd;
        g2d.fillRect(xd, yd, wd, hd);
    }

    @Override
    public void setStroke(Color color, float width) {
        g2d.setColor(SwingUtil.toAwtColor(color));
        g2d.setStroke(new BasicStroke(width));
    }

    @Override
    public void setFont(Font font) {
        textColor = (SwingUtil.toAwtColor(font.getColor()));
        g2d.setFont(FONT_UTIL.convert(font.scaledBy(s)));
    }

    @Override
    public void drawText(String text, float x, float y) {
        java.awt.Color oldColor = g2d.getColor();
        g2d.setColor(textColor);
        g2d.drawString(text, xL2D(x), yL2D(y));
        g2d.setColor(oldColor);
    }

    @Override
    public void drawText(String text, float x, float y, HAnchor hAnchor, VAnchor vAnchor) {
        // fastpath
        if (hAnchor == HAnchor.LEFT && vAnchor == VAnchor.TOP) {
            drawText(text, x, y);
        }

        Rectangle2f r = getTextDimension(text, 1/s);

        float tx = 0;
        float ty = 0;

        tx = switch (hAnchor) {
            case LEFT -> x;
            case RIGHT -> x - r.width();
            case CENTER -> x - r.width() / 2;
        };

        ty = switch (vAnchor) {
            case TOP -> y;
            case BOTTOM -> y + r.height();
            case BASELINE -> y + r.height() - r.y();
            case MIDDLE -> y + r.height() / 2 - r.yMax();
        };

        java.awt.Color oldColor = g2d.getColor();
        g2d.setColor(textColor);
        g2d.drawString(text, xL2D(tx), yL2D(ty));
        g2d.setColor(oldColor);
    }

    @Override
    public Rectangle2f getTextDimension(String text) {
        return FONT_UTIL.getTextDimension(text, g2d.getFont());
    }

    @Override
    public Rectangle2f getTextDimension(String text, float s) {
        Rectangle2f dimension = FONT_UTIL.getTextDimension(text, g2d.getFont());
        return Rectangle2f.of(dimension.x() * s, dimension.y() * s, dimension.width() * s, dimension.height() * s);
    }
}
