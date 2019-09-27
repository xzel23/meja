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

import com.dua3.meja.model.*;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.TextAttributes;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author axel
 */
public abstract class PoiCellStyle implements CellStyle {

    static class PoiHssfCellStyle extends PoiCellStyle {

        PoiHssfCellStyle(PoiHssfWorkbook workbook, HSSFCellStyle poiCellStyle) {
            super(workbook, workbook.getFont(poiCellStyle.getFont(workbook.poiWorkbook)), poiCellStyle);
        }

        @Override
        public BorderStyle getBorderStyle(Direction d) {
            final Color color;
            final float width;
            switch (d) {
            case NORTH:
                color = ((PoiHssfWorkbook) workbook).getColor(poiCellStyle.getTopBorderColor());
                width = getBorderWidth(poiCellStyle.getBorderTop());
                break;
            case EAST:
                color = ((PoiHssfWorkbook) workbook).getColor(poiCellStyle.getRightBorderColor());
                width = getBorderWidth(poiCellStyle.getBorderRight());
                break;
            case SOUTH:
                color = ((PoiHssfWorkbook) workbook).getColor(poiCellStyle.getBottomBorderColor());
                width = getBorderWidth(poiCellStyle.getBorderBottom());
                break;
            case WEST:
                color = ((PoiHssfWorkbook) workbook).getColor(poiCellStyle.getLeftBorderColor());
                width = getBorderWidth(poiCellStyle.getBorderLeft());
                break;
            default:
                throw new IllegalArgumentException();
            }
            return new BorderStyle(width, color);
        }

        @Override
        public Color getFillBgColor() {
            return workbook.getColor(poiCellStyle.getFillBackgroundColorColor(), Color.TRANSPARENT_WHITE);
        }

        @Override
        public Color getFillFgColor() {
            return workbook.getColor(poiCellStyle.getFillForegroundColorColor(), Color.TRANSPARENT_WHITE);
        }

        @Override
        public void setBorderStyle(Direction d, BorderStyle borderStyle) {
            org.apache.poi.ss.usermodel.BorderStyle poiBorder = getPoiBorder(borderStyle);
            short poiColor = ((PoiHssfWorkbook) workbook).getPoiColor(borderStyle.getColor()).getIndex();
            switch (d) {
            case NORTH:
                poiCellStyle.setTopBorderColor(poiColor);
                poiCellStyle.setBorderTop(poiBorder);
                break;
            case EAST:
                poiCellStyle.setRightBorderColor(poiColor);
                poiCellStyle.setBorderRight(poiBorder);
                break;
            case SOUTH:
                poiCellStyle.setBottomBorderColor(poiColor);
                poiCellStyle.setBorderBottom(poiBorder);
                break;
            case WEST:
                poiCellStyle.setLeftBorderColor(poiColor);
                poiCellStyle.setBorderLeft(poiBorder);
                break;
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void setFillBgColor(Color color) {
            Objects.requireNonNull(color);
            poiCellStyle.setFillBackgroundColor(((PoiHssfWorkbook) workbook).getPoiColor(color).getIndex());
        }

        @Override
        public void setFillFgColor(Color color) {
            Objects.requireNonNull(color);
            poiCellStyle.setFillForegroundColor(((PoiHssfWorkbook) workbook).getPoiColor(color).getIndex());
        }

    }

    static class PoiXssfCellStyle extends PoiCellStyle {

        PoiXssfCellStyle(PoiXssfWorkbook workbook, XSSFCellStyle poiCellStyle) {
            super(workbook, new PoiFont(workbook, poiCellStyle.getFont()), poiCellStyle);
        }

        @Override
        public BorderStyle getBorderStyle(Direction d) {
            final Color color;
            final float width;
            switch (d) {
            case NORTH:
                width = getBorderWidth(poiCellStyle.getBorderTop());
                color = workbook.getColor(((XSSFCellStyle) poiCellStyle).getTopBorderXSSFColor(), Color.BLACK);
                break;
            case EAST:
                width = getBorderWidth(poiCellStyle.getBorderRight());
                color = workbook.getColor(((XSSFCellStyle) poiCellStyle).getRightBorderXSSFColor(), Color.BLACK);
                break;
            case SOUTH:
                width = getBorderWidth(poiCellStyle.getBorderBottom());
                color = workbook.getColor(((XSSFCellStyle) poiCellStyle).getBottomBorderXSSFColor(), Color.BLACK);
                break;
            case WEST:
                width = getBorderWidth(poiCellStyle.getBorderLeft());
                color = workbook.getColor(((XSSFCellStyle) poiCellStyle).getLeftBorderXSSFColor(), Color.BLACK);
                break;
            default:
                throw new IllegalArgumentException();
            }
            return new BorderStyle(width, color);
        }

        @Override
        public Color getFillBgColor() {
            return workbook.getColor(poiCellStyle.getFillBackgroundColorColor(), Color.TRANSPARENT_WHITE);
        }

        @Override
        public Color getFillFgColor() {
            return workbook.getColor(poiCellStyle.getFillForegroundColorColor(), Color.TRANSPARENT_WHITE);
        }

        @Override
        public void setBorderStyle(Direction d, BorderStyle borderStyle) {
            org.apache.poi.ss.usermodel.BorderStyle poiBorder = getPoiBorder(borderStyle);
            final Color color = borderStyle.getColor();
            XSSFColor poiColor = color == null ? null : ((PoiXssfWorkbook) workbook).getPoiColor(color);
            switch (d) {
            case NORTH:
                ((XSSFCellStyle) poiCellStyle).setTopBorderColor(poiColor);
                poiCellStyle.setBorderTop(poiBorder);
                break;
            case EAST:
                ((XSSFCellStyle) poiCellStyle).setRightBorderColor(poiColor);
                poiCellStyle.setBorderRight(poiBorder);
                break;
            case SOUTH:
                ((XSSFCellStyle) poiCellStyle).setBottomBorderColor(poiColor);
                poiCellStyle.setBorderBottom(poiBorder);
                break;
            case WEST:
                ((XSSFCellStyle) poiCellStyle).setLeftBorderColor(poiColor);
                poiCellStyle.setBorderLeft(poiBorder);
                break;
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void setFillBgColor(Color color) {
            final XSSFColor poiColor = color == null ? null : ((PoiXssfWorkbook) workbook).getPoiColor(color);
            ((XSSFCellStyle) poiCellStyle).setFillBackgroundColor(poiColor);
        }

        @Override
        public void setFillFgColor(Color color) {
            final XSSFColor poiColor = color == null ? null : ((PoiXssfWorkbook) workbook).getPoiColor(color);
            ((XSSFCellStyle) poiCellStyle).setFillForegroundColor(poiColor);
        }
    }

    /**
     *
     */
    protected final PoiWorkbook workbook;

    /**
     *
     */
    protected final PoiFont font;

    /**
     *
     */
    protected final org.apache.poi.ss.usermodel.CellStyle poiCellStyle;

    /**
     * Construct new instance.
     *
     * @param workbook     the workbook this style belongs to
     * @param font         the font
     * @param poiCellStyle the POI cell style
     */
    protected PoiCellStyle(PoiWorkbook workbook, PoiFont font, org.apache.poi.ss.usermodel.CellStyle poiCellStyle) {
        this.workbook = workbook;
        this.font = font;
        this.poiCellStyle = poiCellStyle;
    }

    @Override
    public void copyStyle(CellStyle other) {
        setHAlign(other.getHAlign());
        setVAlign(other.getVAlign());
        for (Direction d : Direction.values()) {
            setBorderStyle(d, other.getBorderStyle(d));
        }
        setDataFormat(other.getDataFormat());
        setFillFgColor(other.getFillFgColor());
        setFillBgColor(other.getFillBgColor());
        setFillPattern(other.getFillPattern());
        setFont(other.getFont());
        setWrap(other.isWrap());
    }

    /**
     * Get width for a POI defined border.
     *
     * @param poiBorder the POI border value
     * @return the width of the border in points
     */
    protected float getBorderWidth(org.apache.poi.ss.usermodel.BorderStyle poiBorder) {
        switch (poiBorder) {
        case NONE:
            return 0;
        case THIN:
            return 0.75f;
        case MEDIUM:
        case MEDIUM_DASHED:
        case MEDIUM_DASH_DOT:
        case MEDIUM_DASH_DOT_DOT:
            return 1.75f;
        case THICK:
            return 2;
        case DASHED:
        case DOTTED:
        case DOUBLE:
        case HAIR:
        case DASH_DOT:
        case DASH_DOT_DOT:
        case SLANTED_DASH_DOT:
        default:
            return 1;
        }
    }

    @Override
    public String getDataFormat() {
        switch (poiCellStyle.getDataFormat()) {
        case 0x0e:
            return StandardDataFormats.MEDIUM.name();
        default:
            String fmt = poiCellStyle.getDataFormatString();
            return fmt.equals("general") ? "0.##########" : fmt;
        }
    }

    @Override
    public FillPattern getFillPattern() {
        switch (poiCellStyle.getFillPattern()) {
        case SOLID_FOREGROUND:
            return FillPattern.SOLID;
        default:
            return FillPattern.NONE;
        }
    }

    @Override
    public Font getFont() {
        return font.getFont();
    }

    public PoiFont getPoiFont() {
        return font;
    }

    @Override
    public HAlign getHAlign() {
        return PoiHelper.poiToHAlign(poiCellStyle.getAlignment());
    }

    @Override
    public String getName() {
        return workbook.getCellStyleName(this);
    }

    /**
     * Translate border width to POI border.
     *
     * @param borderStyle the border style
     * @return the POI constant to use
     */
    protected org.apache.poi.ss.usermodel.BorderStyle getPoiBorder(BorderStyle borderStyle) {
        float width = borderStyle.getWidth();
        if (width == 0) {
            return org.apache.poi.ss.usermodel.BorderStyle.NONE;
        }
        if (width <= 0.75f) {
            return org.apache.poi.ss.usermodel.BorderStyle.THIN;
        }
        if (width <= 1.75f) {
            return org.apache.poi.ss.usermodel.BorderStyle.MEDIUM;
        }
        if (width <= 2.0f) {
            return org.apache.poi.ss.usermodel.BorderStyle.THICK;
        }
        return org.apache.poi.ss.usermodel.BorderStyle.THIN;
    }

    @Override
    public VAlign getVAlign() {
        return PoiHelper.poiToVAlign(poiCellStyle.getVerticalAlignment());
    }

    @Override
    public PoiWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public boolean isWrap() {
        return poiCellStyle.getWrapText();
    }

    @Override
    public void setDataFormat(String format) {
        Objects.requireNonNull(format);
        if (StandardDataFormats.MEDIUM.name().equals(format)) {
            poiCellStyle.setDataFormat((short) 0x0e);
        } else if (StandardDataFormats.FULL.name().equals(format)) {
            poiCellStyle.setDataFormat((short) 0xa4);
        } else {
            poiCellStyle.setDataFormat(getWorkbook().poiWorkbook.createDataFormat().getFormat(format));
        }
    }

    @Override
    public void setFillPattern(FillPattern pattern) {
        switch (pattern) {
        case NONE:
            poiCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.NO_FILL);
            break;
        case SOLID:
            poiCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setFont(Font font) {
        poiCellStyle.setFont(workbook.getPoiFont(font, TextAttributes.none()).getPoiFont());
    }

    @Override
    public void setHAlign(HAlign hAlign) {
        poiCellStyle.setAlignment(PoiHelper.hAlignToPoi(hAlign));
    }

    @Override
    public void setVAlign(VAlign vAlign) {
        poiCellStyle.setVerticalAlignment(PoiHelper.vAlignToPoi(vAlign));
    }

    @Override
    public void setWrap(boolean wrap) {
        poiCellStyle.setWrapText(wrap);
    }

    DateTimeFormatter getLocaleAwareDateFormat(Locale locale) {
        switch (poiCellStyle.getDataFormat()) {
            case 0x0e:
                return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
            case 0xa4:
                return DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale);
            default:
                return null;
        }
    }

    DateTimeFormatter getLocaleAwareDateTimeFormat(Locale locale) {
        switch (poiCellStyle.getDataFormat()) {
            case 0x0e:
                return DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale);
            case 0xa4:
                return DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL).withLocale(locale);
            default:
                return null;
        }
    }

}
