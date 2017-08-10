package com.dua3.meja.model;

import java.awt.font.FontRenderContext;

import com.dua3.utility.Color;

/**
 * Abstract base class for fonts.
 */
public abstract class AbstractFont implements Font {

    private final java.awt.Font awtFont;
    private final java.awt.font.FontRenderContext awtFontRenderContext;

    /**
     * Create a new font.
     * @param family the font family
     * @param size the size in points
     * @param color the color
     * @param bold bold flag
     * @param italic italic flag 
     * @param underlined underline flag
     * @param strikeThrough strike through flag
     */
    public AbstractFont(String family, float size, Color color, boolean bold, boolean italic, boolean underlined,
            boolean strikeThrough) {
        this.awtFont = getAwtFont(family, size, color, bold, italic, underlined, strikeThrough);
        this.awtFontRenderContext = new FontRenderContext(awtFont.getTransform(), false, true);
    }

    private static java.awt.Font getAwtFont(String family, float size, Color color, boolean bold, boolean italic, boolean underlined,
            boolean strikeThrough) {
        int style = (bold ? java.awt.Font.BOLD : 0) | (italic ? java.awt.Font.ITALIC : 0);
        return new java.awt.Font(family, style, Math.round(size));
    }

    @Override
    public java.awt.Font toAwtFont() {
        return awtFont;
    }

    @Override
    public float getTextWidth(String text) {
        return (float) awtFont.getStringBounds(text, awtFontRenderContext).getWidth();
    }

    @Override
    public String toString() {
        return fontspec();
    }

    @Override
    public int hashCode() {
        // java.awt.Font caches its hashcode. no use trying to be smarter than that
        return toAwtFont().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // important: compare the actual classes using getClass()
        return obj!=null
                && obj.getClass()==getClass()
                && toAwtFont().equals(((AbstractFont)obj).toAwtFont())
                && getColor().equals(((AbstractFont)obj).getColor());
    }

}
