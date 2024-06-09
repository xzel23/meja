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

import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;

/**
 * Definition of a cell style.
 *
 * @author axel
 */
public interface CellStyle {

    /**
     * Enumeration of standard data formats.
     */
    enum StandardDataFormats {
        /**
         * Enum constant for the full date format.
         */
        FULL,
        /**
         * Enum constant for the medium date format.
         */
        MEDIUM
    }

    /**
     * Copy style from another cell style.
     *
     * @param other cell style
     */
    default void copyStyle(CellStyle other) {
        setHAlign(other.getHAlign());
        setVAlign(other.getVAlign());
        for (Direction d : Direction.values()) {
            setBorderStyle(d, other.getBorderStyle(d));
        }
        setDataFormat(other.getDataFormat());
        setFillBgColor(other.getFillBgColor());
        setFillFgColor(other.getFillFgColor());
        setFillPattern(other.getFillPattern());
        setFont(other.getFont());
        setWrap(other.isWrap());
        setRotation(other.getRotation());
    }

    /**
     * Get border style.
     *
     * @param d specifies the edge the border style applies to
     * @return the border style for the edge matching direction {@code d} used in
     * this cell style
     */
    BorderStyle getBorderStyle(Direction d);

    /**
     * Get data format.
     *
     * @return the data format used in this cell style
     */
    String getDataFormat();

    /**
     * Get background fill color.
     *
     * @return the background fill color used in this cell style
     */
    Color getFillBgColor();

    /**
     * Get foreground fill color.
     *
     * @return the foreground color used in this cell style
     */
    Color getFillFgColor();

    /**
     * Get the fill pattern.
     *
     * @return the fill pattern used in this cell style
     */
    FillPattern getFillPattern();

    /**
     * Get font.
     *
     * @return the font used in this cell style
     */
    Font getFont();

    /**
     * Get horizontal alignment.
     *
     * @return the horizontal alignment used in this cell style
     */
    HAlign getHAlign();

    /**
     * Get the effective HAlign for the given cell type.
     *
     * <p>For {@link HAlign#ALIGN_AUTOMATIC}, the default alignment for the cell type is returned. Other values are
     * returned unchanged.
     *
     * @param cellType the cell type
     * @return the default effective alignment
     */
    default HAlign effectiveHAlign(CellType cellType) {
        HAlign hAlign = getHAlign();
        if (hAlign == HAlign.ALIGN_AUTOMATIC) {
            return switch (cellType) {
                case TEXT -> HAlign.ALIGN_LEFT;
                default -> HAlign.ALIGN_RIGHT;
            };
        }
        return hAlign;
    }

    /**
     * Get name of cell style.
     *
     * @return the name of this cell style
     */
    String getName();

    /**
     * Get vertical alignment.
     *
     * @return the vertical alignment used in this cell style
     */
    VAlign getVAlign();

    /**
     * Get workbook this cell style belongs to.
     *
     * @return the workbook
     */
    Workbook getWorkbook();

    /**
     * Get text wrapping.
     *
     * @return true if text should be wrapped
     */
    boolean isWrap();

    /**
     * Set border style
     *
     * @param d           specifies the edge the border style is to be applied to
     * @param borderStyle the border style to set
     */
    void setBorderStyle(Direction d, BorderStyle borderStyle);

    /**
     * Set data format.
     *
     * @param format the data format to set
     */
    void setDataFormat(String format);

    /**
     * Set background fill color.
     *
     * @param color the background fill color to set
     */
    void setFillBgColor(Color color);

    /**
     * Set foreground fill color.
     *
     * @param color the foreground fill-color to set
     */
    void setFillFgColor(Color color);

    /**
     * Set fill pattern.
     *
     * @param pattern the fill pattern to set
     */
    void setFillPattern(FillPattern pattern);

    /**
     * Set font.
     *
     * @param font the font to set
     */
    void setFont(Font font);

    /**
     * Set horizontal alignment.
     *
     * @param hAlign the horizontal alignment to set
     */
    void setHAlign(HAlign hAlign);

    /**
     * Set vertical alignment.
     *
     * @param vAlign the vertical alignment to set
     */
    void setVAlign(VAlign vAlign);

    /**
     * Set text wrapping.
     *
     * @param wrap true if text should be wrapped
     */
    void setWrap(boolean wrap);

    /**
     * Set text rotation.
     *
     * @param angle the angle in degrees; valid range is -90 to 90
     */
    void setRotation(short angle);

    /**
     * Get text rotation.
     *
     * @return rotation angle in degrees (-90 to 90)
     */
    short getRotation();

}
