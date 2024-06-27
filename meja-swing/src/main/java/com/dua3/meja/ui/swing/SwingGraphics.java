package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.Graphics;
import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.lang.Platform;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

public class SwingGraphics implements Graphics {
    private static final AwtFontUtil FONT_UTIL = AwtFontUtil.getInstance();
    public static final java.awt.Font DEFAULT_FONT = FONT_UTIL.convert(new Font());

    private final Graphics2D g2d;
    private final Rectangle parentBounds;
    private final AffineTransformation2f parentTransform;
    private  AffineTransformation2f transform;
    private java.awt.Color strokeColor = java.awt.Color.BLACK;
    private java.awt.Color fillColor = java.awt.Color.BLACK;
    private java.awt.Font font = DEFAULT_FONT;
    private java.awt.Color textColor = java.awt.Color.BLACK;
    private boolean isUnderlined = false;
    private boolean isStrikeThrough = false;
    private final Line2D.Float line = new Line2D.Float();
    private final Rectangle2D.Float rect = new Rectangle2D.Float();
    private final double[] double6 = new double[6];

    public SwingGraphics(Graphics2D g2d, Rectangle bounds) {
        this.g2d = g2d;
        this.parentBounds = bounds;
        this.parentTransform = convert(g2d.getTransform());
        this.transform = AffineTransformation2f.IDENTITY;

        // macOS uses a system-wide setting, and quality seems to be slightly better when antialias is turned off
        Object hintAntiAlias = Platform.isMacOS() ? RenderingHints.VALUE_TEXT_ANTIALIAS_OFF : RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, hintAntiAlias);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
    }

    @Override
    public Rectangle2f getBounds() {
        return convert(LangUtil.orElse(g2d.getClipBounds(), parentBounds));
    }

    /**
     * Convert a java.awt.Rectangle object to a Rectangle2f object.
     *
     * @param r the Rectangle object to convert
     * @return a Rectangle2f object with the same position and size as the input
     */
    public static Rectangle2f convert(Rectangle r) {
        return Rectangle2f.of(r.x, r.y, r.width, r.height);
    }

    /**
     * Converts a {@link Rectangle} object to a {@link Rectangle2f} object.
     *
     * @param r the Rectangle object to convert
     * @return a Rectangle2f object with the same position and size as the input
     */
    public static Rectangle convert(Rectangle2f r) {
        return new Rectangle(
                Math.round(r.x()), Math.round(r.y()),
                Math.round(r.width()), Math.round(r.height())
        );
    }

    /**
     * Converts a {@link Rectangle2f} object to a Rectangle object.
     *
     *
     * <p>The difference to the {@link #convert(Rectangle2f)} is that the returned rectangle will completeyly cover the
     * area of the transformed source rectangle even if the covered area of pixels on the border only is covered
     * to less than 50% by the transformed source rectangle.
     *
     * @param r the Rectangle2f object to convert
     * @return a Rectangle object with the same position and size as the input
     */
    public static Rectangle convertCovering(Rectangle2f r) {
        int xMin = (int) Math.floor(r.xMin());
        int xMax = (int) Math.ceil(r.xMax());
        int yMin = (int) Math.floor(r.yMin());
        int yMax = (int) Math.ceil(r.yMax());
        return new Rectangle(xMin, yMin, xMax-xMin, yMax-yMin);
    }

    /**
     * Convert an AffineTransformation2f object to an AffineTransform object.
     *
     * @param t the AffineTransformation2f object to convert
     * @return an AffineTransform object with the same transformation as the input
     */
    public static AffineTransform convert(AffineTransformation2f t) {
        return new AffineTransform(t.a(), t.d(), t.b(), t.e(), t.c(), t.f());
    }

    /**
     * Converts an instance of {@link AffineTransform} to an instance of {@link AffineTransformation2f}.
     *
     * @param t the affine transform to convert
     * @return the converted affine transformation
     */
    public AffineTransformation2f convert(AffineTransform t) {
        t.getMatrix(double6);
        return new AffineTransformation2f(
                (float) double6[0], (float) double6[2], (float) double6[4], (float) double6[1], (float) double6[3], (float) double6[5]
        );
    }

    @Override
    public void setTransformation(AffineTransformation2f t) {
        this.transform = t;
        g2d.setTransform(convert(t.append(parentTransform)));
    }

    @Override
    public AffineTransformation2f getTransformation() {
        return transform;
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
    public void setFill(Color color) {
        fillColor = SwingUtil.toAwtColor(color);
    }

    @Override
    public void setStroke(Color color, float width) {
        this.strokeColor = SwingUtil.toAwtColor(color);
        g2d.setStroke(new BasicStroke(width));
    }

    @Override
    public void setFont(Font font) {
        textColor = (SwingUtil.toAwtColor(font.getColor()));
        isUnderlined = font.isUnderline();
        isStrikeThrough = font.isStrikeThrough();
        this.font = FONT_UTIL.convert(font);
    }

    @Override
    public void strokeLine(float x1, float y1, float x2, float y2) {
        g2d.setColor(strokeColor);
        line.setLine(x1, y1, x2, y2);
        g2d.draw(line);
    }

    @Override
    public void strokeRect(float x, float y, float width, float height) {
        g2d.setColor(strokeColor);
        rect.setRect(x, y, width, height);
        g2d.draw(rect);
    }

    @Override
    public void fillRect(float x, float y, float width, float height) {
        g2d.setColor(fillColor);
        rect.setRect(x, y, width, height);
        g2d.fill(rect);
    }

    @Override
    public void drawText(CharSequence text, float x, float y) {
        if (text.isEmpty()) {
            return;
        }

        g2d.setColor(textColor);
        AttributedString as = new AttributedString(text.toString());
        as.addAttribute(TextAttribute.FONT, font, 0, text.length());
        as.addAttribute(TextAttribute.UNDERLINE, isUnderlined ? TextAttribute.UNDERLINE_ON : null, 0, text.length());
        as.addAttribute(TextAttribute.STRIKETHROUGH, isStrikeThrough ? TextAttribute.STRIKETHROUGH_ON : null, 0, text.length());

        g2d.drawString(as.getIterator(), x, y);
    }

    @Override
    public Rectangle2f getTextDimension(CharSequence text) {
        return FONT_UTIL.getTextDimension(text, font);
    }

    @Override
    public FontUtil<?> getFontUtil() {
        return FONT_UTIL;
    }
}
