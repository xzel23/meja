/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.model.generic;

import com.dua3.meja.model.AbstractFont;
import com.dua3.meja.model.Color;
import com.dua3.meja.model.Font;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericFont extends AbstractFont {

    private final Color color;
    private final Float size;
    private final String family;
    private final Boolean bold;
    private final Boolean italic;
    private final Boolean underline;
    private final Boolean strikeThrough;

    public static GenericFont copyOf(Font other) {
        return other instanceof GenericFont ? (GenericFont) other: new GenericFont(other);
    }

    /**
     * Construct a new {@code GenericFont}.
     */
    public GenericFont() {
        this("Helvetica",10f,Color.BLACK,false,false,false,false);
    }

    /**
     * Construct a new {@code GenericFont} as copy of another font.
     * <p>
     * The runtime-type of {@code other} is allowed to be from another
     * implementaion.
     * </p>
     *
     * @param other
     *            the font to copy
     */
    public GenericFont(Font other) {
        this(other.getFamily(), other.getSizeInPoints(), other.getColor(), other.isBold(), other.isItalic(), other.isUnderlined(), other.isStrikeThrough());
    }

    /**
     * Construct a new {@code GenerivFont}.
     *
     * @param family
     *            the font family
     * @param size
     *            the font size in points
     * @param color
     *            the color to use for text
     * @param bold
     *            if text should be displayed in bold lettters
     * @param italic
     *            if text should be displayed in italics
     * @param underlined
     *            if text should be displayed underlined
     * @param strikeThrough
     *            if text should be displayed strike-through
     */
    public GenericFont(String family, float size, Color color, boolean bold, boolean italic, boolean underlined,
            boolean strikeThrough) {
        super(family, size, color, bold, italic, underlined, strikeThrough);
        this.color = color;
        this.size = size;
        this.family = family;
        this.bold = bold;
        this.italic = italic;
        this.underline = underlined;
        this.strikeThrough = strikeThrough;
    }

    @Override
    public GenericFont deriveFont(FontDef fd) {
        String fontFamily = fd.getFamily() != null ? fd.getFamily() : this.getFamily();
        float fontSize = fd.getSize() != null ? fd.getSize() : this.getSizeInPoints();
        Color fontColor = fd.getColor() != null ? fd.getColor() : this.getColor();
        boolean fontBold = fd.getBold() != null ? fd.getBold() : this.isBold();
        boolean fontItalic = fd.getItalic() != null ? fd.getItalic() : this.isItalic();
        boolean fontUnderlined = fd.getUnderline() != null ? fd.getUnderline() : this.isUnderlined();
        boolean fontStrikeThrough = fd.getStrikeThrough() != null ? fd.getStrikeThrough() : this.isStrikeThrough();

        return new GenericFont(fontFamily, fontSize, fontColor, fontBold, fontItalic, fontUnderlined,
                fontStrikeThrough);
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public String getFamily() {
        return family;
    }

    @Override
    public float getSizeInPoints() {
        return size;
    }

    @Override
    public boolean isBold() {
        return bold;
    }

    @Override
    public boolean isItalic() {
        return italic;
    }

    @Override
    public boolean isStrikeThrough() {
        return strikeThrough;
    }

    @Override
    public boolean isUnderlined() {
        return underline;
    }
}