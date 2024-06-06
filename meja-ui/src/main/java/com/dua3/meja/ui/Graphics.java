package com.dua3.meja.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;

public interface Graphics {

    Rectangle2f getTextDimension(String text, float s);

    /**
     * Get bounds.
     *
     * @return the bounding rectangle
     */
    Rectangle2f getBounds();

    /**
     * Get text dimensions using the current font.
     *
     * @return the text dimensions
     */
    Rectangle2f getTextDimension(String text);

    /**
     * Start drawing.
     */
    void beginDraw();

    /**
     * End drawing and release ressources.
     */
    void endDraw();

    /**
     * Stroke rectangle.
     * @param r the recatngle
     */
    default void strokeRect(Rectangle2f r) {
        strokeRect(r.xMin(), r.yMin(), r.width(), r.height());
    }

    void strokeRect(float x, float y, float w, float h);

    /**
     * Fill rectangle.
     * @param r the recatngle
     */
    default void fillRect(Rectangle2f r) {
        fillRect(r.xMin(), r.yMin(), r.width(), r.height());
    }

    void fillRect(float x, float y, float w, float h);

    void strokeLine(float x1, float y1, float x2, float y2);

    void setStroke(Color c, float width);

    void setColor(Color c);

    void setTransformation(AffineTransformation2f t);

    void setFont(Font f);

    void drawText(String text, float x, float y);

    enum HAnchor {
        LEFT, RIGHT, CENTER
    }

    enum VAnchor {
        TOP, BOTTOM, BASELINE, MIDDLE
    }

    void drawText(String text, float x, float y, HAnchor hAnchor, VAnchor vAnchor);

    AffineTransformation2f getTransformation();

    void translate(float dx, float dy);

    void scale(float s);
}