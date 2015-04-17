/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.model;

import java.awt.Color;

/**
 *
 * @author axel
 */
public interface  CellStyle {

    /**
     * Get font.
     * @return the font used in this cell style
     */
    Font getFont();

    /**
     * Get background fill color.
     * @return the background fill color used in this cell style
     */
    Color getFillBgColor();

    /**
     * Get foreground fill color.
     * @return the foreground color used in this cell style
     */
    Color getFillFgColor();

    /**
     * Get fill pattern.
     * @return the fill pattern used in this cell style
     */
    FillPattern getFillPattern();

    /**
     * Get horizontal alignment.
     * @return the horizontal alignment used in this cell style
     */
    HAlign getHAlign();

    /**
     * Get vertical alignment.
     * @return the vertical alignment used in this cell style
     */
    VAlign getVAlign();

    /**
     * Get border style.
     * @param d specifies the edge the border style applies to
     * @return the border style for the edge matching direction {@code d}
     * used in this cell style
     */
    BorderStyle getBorderStyle(Direction d);
    
    /**
     * Get text wrapping.
     * @return true if text should be wrapped
     */
    boolean isWrap();
    
    /**
     * Get data format.
     * @return the data format used in this cell style
     */
    String getDataFormat();

    /**
     * Set font.
     * @param font the font to set
     */
    void setFont(Font font);
    
    /**
     * Set background fill color.
     * @param color the background fill color to set
     */
    void setFillBgColor(Color color);
    
    /**
     * Set foreground fill color.
     * @param color the foreground fill color to set
     */
    void setFillFgColor(Color color);
    
    /**
     * Set fill pattern.
     * @param pattern the fill pattern to set
     */
    void setFillPattern(FillPattern pattern);
    
    /**
     * Set horizontal alignment.
     * @param hAlign the horizontal alignmet to set
     */
    void setHAlign(HAlign hAlign);
    
    /**
     * Set vertical alignment.
     * @param vAlign the vertical alignmet to set
     */
    void setVAlign(VAlign vAlign);
    
    /**
     * Set border style
     * @param d specifies the adge the border style is to be applied to
     * @param borderStyle the border style to set
     */
    void setBorderStyle(Direction d, BorderStyle borderStyle);
    
    /**
     * Set text wrapping.
     * @param wrap true if text should be wrapped
     */
    void setWrap(boolean wrap);
    
    /**
     * Set data format.
     * @param format the data format to set
     */
    void setDataFormat(String format);

}
