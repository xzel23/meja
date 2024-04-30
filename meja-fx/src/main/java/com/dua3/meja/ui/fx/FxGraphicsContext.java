/*
 *
 */
package com.dua3.meja.ui.fx;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.ui.GraphicsContext;

public final class FxGraphicsContext implements GraphicsContext {

    private final javafx.scene.canvas.GraphicsContext g;
    private final FxSheetView view;

    FxGraphicsContext(javafx.scene.canvas.GraphicsContext g, FxSheetView view) {
        this.g = g;
        this.view = view;
    }

    @Override
    public void drawLine(float x1, float y1, float x2, float y2) {
        g.strokeLine(xS2D(x1), yS2D(y1), wS2D(x2), hS2D(y2));
    }

    @Override
    public void drawRect(float x, float y, float width, float height) {
        final int xd = xS2D(x);
        final int yd = yS2D(y);
        final int wd = xS2D(x + width) - xd;
        final int hd = yS2D(y + height) - yd;
        g.rect(xd, yd, wd, hd);
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

    public javafx.scene.canvas.GraphicsContext graphics() {
        return g;
    }

    private int hS2D(float f) {
        return view.hS2D(f);
    }

    @Override
    public void setColor(Color color) {
        g.setFill(FxUtil.convert(color));
    }

    @Override
    public void setStroke(Color color, float width) {
        g.setFill(FxUtil.convert(color));
        g.setLineWidth(width);
    }

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
