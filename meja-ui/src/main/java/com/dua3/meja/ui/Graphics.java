package com.dua3.meja.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;

public interface Graphics {

    record Transformation(float dx, float dy, float s) {}

    /**
     * Get bounds.
     *
     * @return the bounding rectangle
     */
    Rectangle2f getBounds();

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

    void strokeLine(float v, float v1, float v2, float v3);

    void setStroke(Color c, float width);

    void setFgColor(Color c);

    void setBgColor(Color c);

    void setTransformation(Transformation t);

    void setFont(Font f);

    void drawText(String text, float x, float y);

    Transformation getTransformation();

    void translate(float dx, float dy);

    void scale(float s);

    Graphics create(float x, float y, float w, float h);
}