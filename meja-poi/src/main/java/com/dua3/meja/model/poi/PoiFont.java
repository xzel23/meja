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
package com.dua3.meja.model.poi;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.FontUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A class that wraps {@link org.apache.poi.ss.usermodel.Font} instances and provides mappings to the {@link Font}
 * class.
 */
public class PoiFont {

    /**
     * The workbook this font belongs to.
     */
    protected final PoiWorkbook workbook;

    /**
     * The {@link Font} instance that corresponds to this POI font.
     */
    protected final Font font;

    /**
     * The {@link org.apache.poi.ss.usermodel.Font} instance that corresponds to this font.
     */
    protected final org.apache.poi.ss.usermodel.Font poiFont;

    /**
     * Copy constructor.
     *
     * @param workbook the workbook the font belongs to
     * @param other    the font to copy
     */
    public PoiFont(PoiWorkbook workbook, Font other) {
        this(workbook, createPoiFont(workbook, other));
    }

    private static org.apache.poi.ss.usermodel.Font createPoiFont(PoiWorkbook workbook, Font other) {
        org.apache.poi.ss.usermodel.Font poiFont = workbook.getPoiWorkbook().createFont();
        poiFont.setFontHeightInPoints((short) Math.round(other.getSizeInPoints()));
        poiFont.setFontName(other.getFamily());

        final org.apache.poi.ss.usermodel.Color poiTextColor = workbook.getPoiColor(other.getColor());
        switch (poiFont) {
            case HSSFFont hssfFont when poiTextColor instanceof HSSFColor hssfColor ->
                    poiFont.setColor(hssfColor.getIndex());
            case XSSFFont xssfFont when poiTextColor instanceof XSSFColor xssfColor -> xssfFont.setColor(xssfColor);
            default ->
                // it should both either be XSSF _or_ HSSF implementations so this
                // line should never be reached.
                    throw new IllegalStateException("font and color types incompatible: " + poiFont.getClass() + " and " + poiTextColor.getClass());
        }

        poiFont.setBold(other.isBold());
        poiFont.setItalic(other.isItalic());
        poiFont.setStrikeout(other.isStrikeThrough());
        poiFont.setUnderline(other.isUnderline() ? org.apache.poi.ss.usermodel.Font.U_SINGLE
                : org.apache.poi.ss.usermodel.Font.U_NONE);

        return poiFont;
    }

    /**
     * Construct instance from POI font.
     *
     * @param workbook the workbook the font belongs to
     * @param poiFont  the POI font instance
     */
    public PoiFont(PoiWorkbook workbook, org.apache.poi.ss.usermodel.Font poiFont) {
        FontDef fd = new FontDef();
        fd.setFamily(poiFont.getFontName());
        fd.setSize((float) poiFont.getFontHeightInPoints());
        fd.setColor(workbook.getColor(poiFont, Color.BLACK));
        fd.setBold(poiFont.getBold());
        fd.setItalic(poiFont.getItalic());
        fd.setUnderline(poiFont.getUnderline() != org.apache.poi.ss.usermodel.Font.U_NONE);
        fd.setStrikeThrough(poiFont.getStrikeout());
        this.font = FontUtil.getInstance().getFont(fd);

        this.workbook = workbook;
        this.poiFont = poiFont;
    }

    /**
     * Derives a new font from the current font with the specified FontDef.
     *
     * @param fd the FontDef to derive the font from
     * @return a new PoiFont instance with the derived font
     */
    public PoiFont deriveFont(FontDef fd) {
        Font derivedFont = FontUtil.getInstance().deriveFont(font, fd);
        return workbook.createFont(derivedFont);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof PoiFont other && Objects.equals(poiFont, other.poiFont);
    }

    /**
     * Returns the current font.
     *
     * @return the current font stored in the instance of the class
     */
    public Font getFont() {
        return font;
    }

    /**
     * Get the POI font.
     *
     * @return the {link org.apache.poi.ss.usermodel.Font} used
     */
    protected org.apache.poi.ss.usermodel.Font getPoiFont() {
        return poiFont;
    }

    @Override
    public int hashCode() {
        return font.hashCode() + 37 * poiFont.hashCode();
    }

    @Override
    public String toString() {
        return font.fontspec();
    }
}
