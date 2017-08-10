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
package com.dua3.meja.model;

import com.dua3.utility.Color;

/**
 * Interface describing fonts used in workbooks.
 *
 * @author axel
 */
public interface Font {

    /**
     * A mutable class holding font attributes to help creating immutable font
     * instances.
     */
    public static class FontDef {
        /**
         * Create FontDef instance with only the color attribute set.
         *
         * @param col
         *            the color
         * @return new FontDef instance
         */
        public static FontDef color(Color col) {
            FontDef fd = new FontDef();
            fd.setColor(col);
            return fd;
        }

        /**
         * Create FontDef instance with only the font family attribute set.
         *
         * @param family
         *            the font family
         * @return new FontDef instance
         */
        public static FontDef family(String family) {
            FontDef fd = new FontDef();
            fd.setFamily(family);
            return fd;
        }

        /**
         * Create FontDef instance with only the font size set.
         *
         * @param size
         *            the font size in points
         * @return new FontDef instance
         */
        public static FontDef size(Float size) {
            FontDef fd = new FontDef();
            fd.setSize(size);
            return fd;
        }

        private Color color;
        private Float size;
        private String family;
        private Boolean bold;
        private Boolean italic;
        private Boolean underline;
        private Boolean strikeThrough;

        public FontDef() {
            // nop - everything being initialized to null is just fine
        }

        /**
         * @return the bold
         */
        public Boolean getBold() {
            return bold;
        }

        /**
         * @return the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * @return the family
         */
        public String getFamily() {
            return family;
        }

        /**
         * @return the italic
         */
        public Boolean getItalic() {
            return italic;
        }

        /**
         * @return the size in points
         */
        public Float getSize() {
            return size;
        }

        /**
         * @return the strikeThrough
         */
        public Boolean getStrikeThrough() {
            return strikeThrough;
        }

        /**
         * @return the underline
         */
        public Boolean getUnderline() {
            return underline;
        }

        /**
         * @param bold
         *            the bold to set
         */
        public void setBold(Boolean bold) {
            this.bold = bold;
        }

        /**
         * @param color
         *            the color to set
         */
        public void setColor(Color color) {
            this.color = color;
        }

        /**
         * @param family
         *            the family to set
         */
        public void setFamily(String family) {
            this.family = family;
        }

        /**
         * @param italic
         *            the italic to set
         */
        public void setItalic(Boolean italic) {
            this.italic = italic;
        }

        /**
         * @param size
         *            the size in points to set
         */
        public void setSize(Float size) {
            this.size = size;
        }

        /**
         * @param strikeThrough
         *            the strikeThrough to set
         */
        public void setStrikeThrough(Boolean strikeThrough) {
            this.strikeThrough = strikeThrough;
        }

        /**
         * @param underline
         *            the underline to set
         */
        public void setUnderline(Boolean underline) {
            this.underline = underline;
        }
    }

    /**
     * Derive font.
     * <p>
     * A new font based on this font is returned. The attributes defined
     * {@code fd} are applied to the new font. If an attribute in {@code fd} is
     * not set, the attribute is copied from this font.
     * </p>
     *
     * @param fd
     *            the {@link FontDef} describing the attributes to set
     * @return new {@link Font} instance
     */
    Font deriveFont(FontDef fd);

    /**
     * Get text color.
     *
     * @return the text color.
     */
    Color getColor();

    /**
     * Get font family.
     *
     * @return the font family as {@code String}.
     */
    String getFamily();

    /**
     * Get font size.
     *
     * @return the font size in points.
     */
    float getSizeInPoints();

    /**
     * Get bold property.
     *
     * @return true if font is bold.
     */
    boolean isBold();

    /**
     * Get italic property.
     *
     * @return true if font is italic.
     */
    boolean isItalic();

    /**
     * Get strike-through property.
     *
     * @return true if font is strike-through.
     */
    boolean isStrikeThrough();

    /**
     * Get underlined property.
     *
     * @return true if font is underlined.
     */
    boolean isUnderlined();

    /**
     * Calculate width of text.
     * @param text the text
     * @return width in points
     */
    float getTextWidth(String text);

    /**
     * Get the corresponding AWT font instance.
     * @return instance of java.awt.Font for this font
     */
    java.awt.Font toAwtFont();

    /**
     * Get a description of the font.
     * @return font description
     */
    default String fontspec() {
        StringBuilder sb = new StringBuilder(32);

        sb.append(getFamily());

        if (isBold()) {
            sb.append('-').append("bold");
        }
        if (isItalic()) {
            sb.append('-').append("italic");
        }
        if (isUnderlined()) {
            sb.append('-').append("underline");
        }
        if (isStrikeThrough()) {
            sb.append('-').append("strikethrough");
        }
        sb.append('-');
        sb.append(getSizeInPoints());
        sb.append('-');
        sb.append(getColor());

        return sb.toString();
    }
}
