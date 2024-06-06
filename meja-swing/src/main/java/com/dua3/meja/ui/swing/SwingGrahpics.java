package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.Graphics;
import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.text.Font;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class SwingGrahpics implements Graphics {
    private static final AwtFontUtil FONT_UTIL = AwtFontUtil.getInstance();

    private Graphics2D g2d;
    private java.awt.Color textColor = java.awt.Color.BLACK;

    public SwingGrahpics(Graphics2D g2d) {
        this.g2d = g2d;
    }

    @Override
    public Rectangle2f getBounds() {
        return convert(g2d.getClipBounds());
    }

    private Rectangle2f convert(Rectangle r) {
        return Rectangle2f.of(r.x, r.y, r.width, r.height);
    }

    @Override
    public void setTransformation(AffineTransformation2f t) {
        g2d.setTransform(SwingUtil.convert(t));
    }

    @Override
    public AffineTransformation2f getTransformation() {
        return SwingUtil.convert(g2d.getTransform());
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
        g2d.translate(dx, dy);
    }

    @Override
    public void scale(float s) {
        g2d.scale(s, s);
    }

    @Override
    public void strokeLine(float x1, float y1, float x2, float y2) {
        g2d.draw(new Line2D.Float(x1, y1, x2, y2));
    }

    @Override
    public void strokeRect(float x, float y, float width, float height) {
        g2d.draw(new Rectangle2D.Float(x, y, width, height));
    }

    @Override
    public void fillRect(float x, float y, float width, float height) {
        g2d.fill(new Rectangle2D.Float(x, y, width, height));
    }

    @Override
    public void setStroke(Color color, float width) {
        g2d.setColor(SwingUtil.toAwtColor(color));
        g2d.setStroke(new BasicStroke(width));
    }

    @Override
    public void setFont(Font font) {
        textColor = (SwingUtil.toAwtColor(font.getColor()));
        g2d.setFont(FONT_UTIL.convert(font));
    }

    @Override
    public void drawText(String text, float x, float y) {
        java.awt.Color oldColor = g2d.getColor();
        g2d.setColor(textColor);
        g2d.drawString(text, x, y);
        g2d.setColor(oldColor);
    }

    @Override
    public void drawText(String text, float x, float y, HAnchor hAnchor, VAnchor vAnchor) {
        // fastpath
        if (hAnchor == HAnchor.LEFT && vAnchor == VAnchor.TOP) {
            drawText(text, x, y);
        }

        Rectangle2f r = getTextDimension(text);

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
        g2d.drawString(text, tx, ty);
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
