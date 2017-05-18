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

import java.awt.font.FontRenderContext;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import com.dua3.meja.model.Color;
import com.dua3.meja.model.Font;
import com.dua3.meja.util.MejaHelper;

/**
 *
 * @author axel
 */
public class PoiFont
        implements Font {

    /**
     *
     */
    protected final PoiWorkbook workbook;

    /**
     *
     */
    protected final org.apache.poi.ss.usermodel.Font poiFont;

    /**
     * Copy constructor.
     *
     * @param workbook
     *            the workbook the font belongs to
     * @param other
     *            the font to copy
     */
    public PoiFont(PoiWorkbook workbook, Font other) {
        this.workbook = workbook;
        this.poiFont = workbook.getPoiWorkbook().createFont();
        this.poiFont.setFontHeightInPoints((short) Math.round(other.getSizeInPoints()));
        this.poiFont.setFontName(other.getFamily());

        final org.apache.poi.ss.usermodel.Color poiTextColor = workbook.getPoiColor(other.getColor());
        if (poiFont instanceof HSSFFont && poiTextColor instanceof HSSFColor) {
            this.poiFont.setColor(((HSSFColor) poiTextColor).getIndex());
        } else if (poiFont instanceof XSSFFont && poiTextColor instanceof XSSFColor) {
            ((XSSFFont) this.poiFont).setColor((XSSFColor) poiTextColor);
        } else {
            // it should both either be XSSF _or_ HSSF implementations so this
            // line should never be reached.
            throw new IllegalStateException();
        }

        this.poiFont.setBold(other.isBold());
        this.poiFont.setItalic(other.isItalic());
        this.poiFont.setStrikeout(other.isStrikeThrough());
        this.poiFont.setUnderline(other.isUnderlined() ? org.apache.poi.ss.usermodel.Font.U_SINGLE
                : org.apache.poi.ss.usermodel.Font.U_NONE);
    }

    /**
     * Construct instance from POI font.
     *
     * @param workbook
     *            the workbook the font belongs to
     * @param poiFont
     *            the POI font instance
     */
    public PoiFont(PoiWorkbook workbook, org.apache.poi.ss.usermodel.Font poiFont) {
        this.workbook = workbook;
        this.poiFont = poiFont;
    }

    @Override
    public PoiFont deriveFont(FontDef fd) {
        String fontFamily = fd.getFamily() != null ? fd.getFamily() : this.getFamily();
        float fontSize = fd.getSize() != null ? fd.getSize() : this.getSizeInPoints();
        Color fontColor = fd.getColor() != null ? fd.getColor() : this.getColor();
        boolean fontBold = fd.getBold() != null ? fd.getBold() : this.isBold();
        boolean fontItalic = fd.getItalic() != null ? fd.getItalic() : this.isItalic();
        boolean fontUnderlined = fd.getUnderline() != null ? fd.getUnderline() : this.isUnderlined();
        boolean fontStrikeThrough = fd.getStrikeThrough() != null ? fd.getStrikeThrough() : this.isStrikeThrough();

        return workbook.createFont(fontFamily, fontSize, fontColor, fontBold, fontItalic, fontUnderlined,
                fontStrikeThrough);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiFont) {
            return Objects.equals(getPoiFont(), ((PoiFont) obj).getPoiFont());
        } else {
            return false;
        }
    }

    @Override
    public Color getColor() {
        return workbook.getColor(poiFont, Color.BLACK);
    }

    @Override
    public String getFamily() {
        return getPoiFont().getFontName();
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
    public float getSizeInPoints() {
        return getPoiFont().getFontHeightInPoints();
    }

    @Override
    public int hashCode() {
        return getPoiFont().hashCode();
    }

    @Override
    public boolean isBold() {
        return getPoiFont().getBold();
    }

    @Override
    public boolean isItalic() {
        return getPoiFont().getItalic();
    }

    @Override
    public boolean isStrikeThrough() {
        return getPoiFont().getStrikeout();
    }

    @Override
    public boolean isUnderlined() {
        return getPoiFont().getUnderline() != org.apache.poi.ss.usermodel.Font.U_NONE;
    }

    @Override
    public float getTextWidth(String text) {
        java.awt.Font awtFont = MejaHelper.getAwtFont(this);
        FontRenderContext fontRenderContext = new FontRenderContext(awtFont.getTransform(), false, false);
        return (float) awtFont.getStringBounds(text, fontRenderContext).getWidth();
    }


    @Override
    public String toString() {
        return fontspec();
    }
}
