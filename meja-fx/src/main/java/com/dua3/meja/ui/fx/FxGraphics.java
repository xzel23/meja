package com.dua3.meja.ui.fx;

import com.dua3.meja.ui.Graphics;
import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

public class FxGraphics implements Graphics {
    public static final FxFontUtil FONT_UTIL = FxFontUtil.getInstance();

    private final GraphicsContext gc;
    private final float w;
    private final float h;

    private float dx;
    private float dy;
    private float s;
    private javafx.scene.paint.Color textColor = javafx.scene.paint.Color.BLACK;

    public FxGraphics(GraphicsContext gc, float w, float h) {
        this.gc = gc;
        this.w = w;
        this.h = h;
        this.s = 1f;
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
    public Rectangle2f getTextDimension(String text, float s) {
        Rectangle2f dimension = FONT_UTIL.getTextDimension(text, gc.getFont());
        return Rectangle2f.of(dimension.x() * s, dimension.y() * s, dimension.width() * s, dimension.height() * s);
    }

    @Override
    public Rectangle2f getBounds() {
        return new Rectangle2f(0, 0, w, h);
    }

    @Override
    public Rectangle2f getTextDimension(String text) {
        return null;
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
    public void strokeRect(float x, float y, float w, float h) {
        gc.strokeRect(xL2D(x), yL2D(y), wL2D(w), hL2D(h));
    }

    @Override
    public void fillRect(float x, float y, float w, float h) {
        gc.fillRect(xL2D(x), yL2D(y), wL2D(w), hL2D(h));
    }

    @Override
    public void strokeLine(float x1, float y1, float x2, float y2) {
        gc.strokeLine(xL2D(x1), yL2D(y1), xL2D(x2), yL2D(y2));
    }

    @Override
    public void setStroke(Color c, float width) {
        gc.setStroke(FxUtil.convert(c));
        gc.setLineWidth(width);
    }

    @Override
    public void setFill(Color c) {
        javafx.scene.paint.Color fxColor = FxUtil.convert(c);
        gc.setStroke(fxColor);
        gc.setFill(fxColor);
    }

    @Override
    public void setTransformation(AffineTransformation2f t) {
        gc.setTransform(FxUtil.convert(t));
    }

    @Override
    public AffineTransformation2f getTransformation() {
        return FxUtil.convert(gc.getTransform());
    }

    @Override
    public void setFont(Font font) {
        textColor = FxUtil.convert(font.getColor());
        gc.setFont(FONT_UTIL.convert(font.scaled(s)));
    }

    @Override
    public void drawText(String text, float x, float y) {
        Paint oldPaint = gc.getFill();
        gc.setFill(textColor);
        gc.fillText(text, xL2D(x), yL2D(y));
        gc.setFill(oldPaint);
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
            case TOP -> y - r.height() - r.yMax();
            case BOTTOM -> y - r.yMax();
            case BASELINE -> y;
            case MIDDLE -> y + r.height() / 2 - r.yMax();
        };

        Paint oldPaint = gc.getFill();
        gc.setFill(textColor);
        gc.strokeText(text, xL2D(tx), yL2D(ty));
        gc.setFill(oldPaint);
    }

}
