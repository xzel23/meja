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
package com.dua3.meja.model.poi;

import com.dua3.meja.model.Font;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Objects;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

/**
 *
 * @author axel
 */
public class PoiFont implements Font {

    protected final PoiWorkbook workbook;
    protected final org.apache.poi.ss.usermodel.Font poiFont;

    public PoiFont(PoiWorkbook workbook, org.apache.poi.ss.usermodel.Font poiFont) {
        this.workbook = workbook;
        this.poiFont = poiFont;
    }

    public PoiFont(PoiWorkbook workbook, Font other) {
        this.workbook = workbook;
        this.poiFont = workbook.getPoiWorkbook().createFont();
        this.poiFont.setFontHeightInPoints((short)Math.round(other.getSizeInPoints()));
        this.poiFont.setFontName(other.getFamily());

        final org.apache.poi.ss.usermodel.Color poiTextColor = workbook.getPoiColor(other.getColor());
        if (poiTextColor instanceof HSSFColor) {
            this.poiFont.setColor(((HSSFColor)poiTextColor).getIndex());
        } else if (poiFont instanceof XSSFFont) {
            ((XSSFFont)this.poiFont).setColor((XSSFColor) poiTextColor);
        } else {
            // it should both either be XSSF _or_ HSSF implementations so this
            // line should never be reached.
            throw new IllegalStateException();
        }

        this.poiFont.setBold(other.isBold());
        this.poiFont.setItalic(other.isItalic());
        this.poiFont.setStrikeout(other.isStrikeThrough());
        this.poiFont.setUnderline(other.isUnderlined()? XSSFFont.U_SINGLE: XSSFFont.U_NONE);
    }

    protected org.apache.poi.ss.usermodel.Font getPoiFont() {
        return poiFont;
    }

    @Override
    public float getSizeInPoints() {
        return getPoiFont().getFontHeightInPoints();
    }

    @Override
    public String getFamily() {
        return getPoiFont().getFontName();
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
    public boolean isUnderlined() {
        return getPoiFont().getUnderline() != HSSFFont.U_NONE;
    }

    @Override
    public boolean isStrikeThrough() {
        return getPoiFont().getStrikeout();
    }

    public void addAttributes(AttributedString at, int start, int end) {
        at.addAttribute(TextAttribute.FAMILY, getFamily(), start, end);
        at.addAttribute(TextAttribute.SIZE, getSizeInPoints(), start, end);
        at.addAttribute(TextAttribute.FOREGROUND, getColor(), start, end);
        if (isBold()) {
            at.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, start, end);
        }
        if (isItalic()) {
            at.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, start, end);
        }
        if (isUnderlined()) {
            at.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, start, end);
            at.addAttribute(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL, start, end);
        }
        if (isStrikeThrough()) {
            at.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, start, end);
        }
    }

    @SuppressWarnings("rawtypes")
	@Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiFont) {
            return Objects.equals(getPoiFont(), ((PoiFont)obj).getPoiFont());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getPoiFont().hashCode();
    }

    @Override
    public Color getColor() {
        return workbook.getColor(poiFont, Color.BLACK);
    }

}
