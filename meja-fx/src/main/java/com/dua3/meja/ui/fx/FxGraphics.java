package com.dua3.meja.ui.fx;

import com.dua3.meja.ui.Graphics;
import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;

public class FxGraphics implements Graphics {
    public static final FxFontUtil FONT_UTIL = FxFontUtil.getInstance();
    public static final Font DEFAULT_FONT = new Font();

    private final GraphicsContext gc;
    private final float w;
    private final float h;
    private final AffineTransformation2f parentTransform;

    private AffineTransformation2f transform;

    private float s;
    private javafx.scene.paint.Color textColor = javafx.scene.paint.Color.BLACK;
    private javafx.scene.text.Font font = FONT_UTIL.convert(DEFAULT_FONT);
    private boolean isStrikeThrough = false;
    private boolean isUnderline = false;
    private javafx.scene.paint.Paint strokeColor = javafx.scene.paint.Color.BLACK;
    private double width = 1.0;
    private javafx.scene.paint.Color fillColor = javafx.scene.paint.Color.BLACK;

    public FxGraphics(GraphicsContext gc, float w, float h) {
        this.gc = gc;
        this.w = w;
        this.h = h;
        this.s = 1f;
        this.parentTransform = FxUtil.convert(gc.getTransform());
    }

    @Override
    public FontUtil<?> getFontUtil() {
        return FONT_UTIL;
    }

    @Override
    public Rectangle2f getBounds() {
        return new Rectangle2f(0, 0, w, h);
    }

    @Override
    public Rectangle2f getTextDimension(CharSequence text) {
        return FONT_UTIL.getTextDimension(text, font);
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
        gc.setStroke(strokeColor);
        gc.setLineWidth(width);
        gc.strokeRect(x, y, w, h);
    }

    @Override
    public void fillRect(float x, float y, float w, float h) {
        gc.setFill(fillColor);
        gc.fillRect(x, y, w, h);
    }

    @Override
    public void strokeLine(float x1, float y1, float x2, float y2) {
        gc.setStroke(strokeColor);
        gc.setLineWidth(width);
        gc.strokeLine(x1, y1, x2, y2);
    }

    @Override
    public void setStroke(Color c, float width) {
        this.strokeColor = FxUtil.convert(c);
        this.width = width;
    }

    @Override
    public void setFill(Color c) {
        this.fillColor = FxUtil.convert(c);
    }

    @Override
    public void setTransformation(AffineTransformation2f t) {
        this.transform = t;
        gc.setTransform(FxUtil.convert(t.append(parentTransform)));
    }

    @Override
    public AffineTransformation2f getTransformation() {
        return transform;
    }

    @Override
    public void setFont(Font font) {
        this.textColor = FxUtil.convert(font.getColor());
        this.font = FONT_UTIL.convert(font.scaled(s));
        this.isStrikeThrough = font.isStrikeThrough();
        this.isUnderline = font.isUnderline();
    }

    @Override
    public void drawText(CharSequence text, float x, float y) {
        gc.setFont(font);
        gc.setFill(textColor);
        gc.fillText(text.toString(), x, y);

        if (isStrikeThrough || isUnderline) {
            double strokeWidth = font.getSize() / 15f;

            Text t = new Text(text.toString());
            t.setFont(font);
            Bounds r = t.getBoundsInLocal();
            double xStroke = x;
            double wStroke = r.getWidth();

            gc.setStroke(textColor);
            gc.setLineWidth(strokeWidth);

            if (isUnderline) {
                double yStroke = y + r.getMaxY()/2f;
                gc.strokeLine(xStroke, yStroke, xStroke+wStroke, yStroke);
            }
            if (isStrikeThrough) {
                double yStroke = y + r.getMinY()/2f + r.getMaxY();
                gc.strokeLine(xStroke, yStroke, xStroke+wStroke, yStroke);
            }
        }
    }

}
