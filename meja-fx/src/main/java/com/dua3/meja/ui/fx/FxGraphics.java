package com.dua3.meja.ui.fx;

import com.dua3.meja.ui.Graphics;
import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import javafx.scene.canvas.GraphicsContext;

public class FxGraphics implements Graphics {
    public static final FxFontUtil FONT_UTIL = FxFontUtil.getInstance();

    private final GraphicsContext gc;
    private final float w;
    private final float h;

    private float dx;
    private float dy;
    private float s;

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
    public Rectangle2f getBounds() {
        return new Rectangle2f(0, 0, w, h);
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
    public void setColor(Color c) {
        javafx.scene.paint.Color fxColor = FxUtil.convert(c);
        gc.setStroke(fxColor);
        gc.setFill(fxColor);
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
    public void setFont(Font font) {
        gc.setFont(FONT_UTIL.convert(font.withSize(s*font.getSizeInPoints())));
    }

    @Override
    public void drawText(String text, float x, float y) {
        gc.fillText(text, xL2D(x), yL2D(y));
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

}
