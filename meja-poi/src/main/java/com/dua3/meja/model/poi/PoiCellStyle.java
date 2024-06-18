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

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.Font;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
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
                case NORTH -> {
                    color = ((PoiHssfWorkbook) workbook).getColor(poiCellStyle.getTopBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderTop());
                }
                case EAST -> {
                    color = ((PoiHssfWorkbook) workbook).getColor(poiCellStyle.getRightBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderRight());
                }
                case SOUTH -> {
                    color = ((PoiHssfWorkbook) workbook).getColor(poiCellStyle.getBottomBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderBottom());
                }
                case WEST -> {
                    color = ((PoiHssfWorkbook) workbook).getColor(poiCellStyle.getLeftBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderLeft());
                }
                default -> throw new IllegalArgumentException(String.valueOf(d));
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
            short poiColor = ((PoiHssfWorkbook) workbook).getPoiColor(borderStyle.color()).getIndex();
            switch (d) {
                case NORTH -> {
                    poiCellStyle.setTopBorderColor(poiColor);
                    poiCellStyle.setBorderTop(poiBorder);
                }
                case EAST -> {
                    poiCellStyle.setRightBorderColor(poiColor);
                    poiCellStyle.setBorderRight(poiBorder);
                }
                case SOUTH -> {
                    poiCellStyle.setBottomBorderColor(poiColor);
                    poiCellStyle.setBorderBottom(poiBorder);
                }
                case WEST -> {
                    poiCellStyle.setLeftBorderColor(poiColor);
                    poiCellStyle.setBorderLeft(poiBorder);
                }
                default -> throw new IllegalArgumentException(String.valueOf(d));
            }
        }

        @Override
        public void setFillBgColor(Color color) {
            poiCellStyle.setFillBackgroundColor(((PoiHssfWorkbook) workbook).getPoiColor(color).getIndex());
        }

        @Override
        public void setFillFgColor(Color color) {
            poiCellStyle.setFillForegroundColor(((PoiHssfWorkbook) workbook).getPoiColor(color).getIndex());
        }

        @Override
        public short getRotation() {
            short r = poiCellStyle.getRotation();
            return r == 0xff ? 90 : r; // HSSF uses -90 to 90, special value 0xff for vertical
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
                case NORTH -> {
                    width = getBorderWidth(poiCellStyle.getBorderTop());
                    color = workbook.getColor(((XSSFCellStyle) poiCellStyle).getTopBorderXSSFColor(), Color.BLACK);
                }
                case EAST -> {
                    width = getBorderWidth(poiCellStyle.getBorderRight());
                    color = workbook.getColor(((XSSFCellStyle) poiCellStyle).getRightBorderXSSFColor(), Color.BLACK);
                }
                case SOUTH -> {
                    width = getBorderWidth(poiCellStyle.getBorderBottom());
                    color = workbook.getColor(((XSSFCellStyle) poiCellStyle).getBottomBorderXSSFColor(), Color.BLACK);
                }
                case WEST -> {
                    width = getBorderWidth(poiCellStyle.getBorderLeft());
                    color = workbook.getColor(((XSSFCellStyle) poiCellStyle).getLeftBorderXSSFColor(), Color.BLACK);
                }
                default -> throw new IllegalArgumentException(String.valueOf(d));
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
            final Color color = borderStyle.color();
            XSSFColor poiColor = color == null ? null : ((PoiXssfWorkbook) workbook).getPoiColor(color);
            switch (d) {
                case NORTH -> {
                    poiCellStyle.setBorderTop(poiBorder);
                    if (poiBorder != org.apache.poi.ss.usermodel.BorderStyle.NONE) {
                        ((XSSFCellStyle) poiCellStyle).setTopBorderColor(poiColor);
                    }
                }
                case EAST -> {
                    poiCellStyle.setBorderRight(poiBorder);
                    if (poiBorder != org.apache.poi.ss.usermodel.BorderStyle.NONE) {
                        ((XSSFCellStyle) poiCellStyle).setRightBorderColor(poiColor);
                    }
                }
                case SOUTH -> {
                    poiCellStyle.setBorderBottom(poiBorder);
                    if (poiBorder != org.apache.poi.ss.usermodel.BorderStyle.NONE) {
                        ((XSSFCellStyle) poiCellStyle).setBottomBorderColor(poiColor);
                    }
                }
                case WEST -> {
                    poiCellStyle.setBorderLeft(poiBorder);
                    if (poiBorder != org.apache.poi.ss.usermodel.BorderStyle.NONE) {
                        ((XSSFCellStyle) poiCellStyle).setLeftBorderColor(poiColor);
                    }
                }
                default -> throw new IllegalArgumentException(String.valueOf(d));
            }
        }

        @Override
        public void setFillBgColor(Color color) {
            final XSSFColor poiColor = ((PoiXssfWorkbook) workbook).getPoiColor(color);
            ((XSSFCellStyle) poiCellStyle).setFillBackgroundColor(poiColor);
        }

        @Override
        public void setFillFgColor(Color color) {
            final XSSFColor poiColor = ((PoiXssfWorkbook) workbook).getPoiColor(color);
            ((XSSFCellStyle) poiCellStyle).setFillForegroundColor(poiColor);
        }

        @Override
        public short getRotation() {
            short angle = poiCellStyle.getRotation();
            return angle <= 90 ? angle : (short) (angle - 180); // XSSF uses 0 to 180 degrees
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

    /**
     * Get width for a POI defined border.
     *
     * @param poiBorder the POI border value
     * @return the width of the border in points
     */
    protected static float getBorderWidth(org.apache.poi.ss.usermodel.BorderStyle poiBorder) {
        return switch (poiBorder) {
            case NONE -> 0;
            case THIN -> 0.75f;
            case MEDIUM, MEDIUM_DASHED, MEDIUM_DASH_DOT, MEDIUM_DASH_DOT_DOT -> 1.75f;
            case THICK -> 2.25f;
            case DASHED, DOTTED, DOUBLE, HAIR, DASH_DOT, DASH_DOT_DOT, SLANTED_DASH_DOT -> 1;
        };
    }

    @Override
    public String getDataFormat() {
        return switch (poiCellStyle.getDataFormat()) {
            case 0x0e -> StandardDataFormats.MEDIUM.name();
            default -> {
                String fmt = poiCellStyle.getDataFormatString();
                yield fmt.equals("general") ? "0.##########" : fmt;
            }
        };
    }

    @Override
    public FillPattern getFillPattern() {
        return switch (poiCellStyle.getFillPattern()) {
            case SOLID_FOREGROUND -> FillPattern.SOLID;
            default -> FillPattern.NONE;
        };
    }

    @Override
    public Font getFont() {
        return font.getFont();
    }

    /**
     * Get the POI font for this PoiFont object.
     *
     * @return the POI font
     */
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
    protected static org.apache.poi.ss.usermodel.BorderStyle getPoiBorder(BorderStyle borderStyle) {
        float width = borderStyle.width();
        if (width == 0) {
            return org.apache.poi.ss.usermodel.BorderStyle.NONE;
        }
        if (width <= 0.75f) {
            return org.apache.poi.ss.usermodel.BorderStyle.THIN;
        }
        if (width <= 1.75f) {
            return org.apache.poi.ss.usermodel.BorderStyle.MEDIUM;
        }
        // width > 1.75f
        return org.apache.poi.ss.usermodel.BorderStyle.THICK;
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
            case NONE -> poiCellStyle.setFillPattern(FillPatternType.NO_FILL);
            case SOLID -> poiCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            default -> throw new IllegalArgumentException(String.valueOf(pattern));
        }
    }

    @Override
    public void setFont(Font font) {
        poiCellStyle.setFont(workbook.getPoiFont(font).getPoiFont());
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

    @Override
    public void setRotation(short angle) {
        poiCellStyle.setRotation((short) LangUtil.requireInInterval(angle, (short) -90, (short) 90, "angle must be in range [-90, 90]: %d", angle));
    }

    DateTimeFormatter getLocaleAwareDateFormat(Locale locale) {
        return switch (poiCellStyle.getDataFormat()) {
            case 0x0e -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
            case 0xa4 -> DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale);
            default -> null;
        };
    }

    DateTimeFormatter getLocaleAwareDateTimeFormat(Locale locale) {
        return switch (poiCellStyle.getDataFormat()) {
            case 0x0e -> DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale);
            case 0xa4 -> DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL).withLocale(locale);
            default -> null;
        };
    }

}
