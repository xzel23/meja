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

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Font;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.meja.text.Style;
import java.awt.Color;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 *
 * @author axel
 */
public abstract class PoiCellStyle implements CellStyle {

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
     *
     * @param workbook
     * @param font
     * @param poiCellStyle
     */
    protected PoiCellStyle(PoiWorkbook workbook, PoiFont font, org.apache.poi.ss.usermodel.CellStyle poiCellStyle) {
        this.workbook = workbook;
        this.font = font;
        this.poiCellStyle = poiCellStyle;
    }

    @Override
    public String getName() {
        return workbook.getCellStyleName(this);
    }

    @Override
    public PoiWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public HAlign getHAlign() {
        return PoiHelper.poiToHAlign(poiCellStyle.getAlignment());
    }

    @Override
    public void setHAlign(HAlign hAlign) {
        poiCellStyle.setAlignment(PoiHelper.hAlignToPoi(hAlign));
    }

    @Override
    public VAlign getVAlign() {
        return PoiHelper.poiToVAlign(poiCellStyle.getVerticalAlignment());
    }

    @Override
    public void setVAlign(VAlign vAlign) {
        poiCellStyle.setVerticalAlignment(PoiHelper.vAlignToPoi(vAlign));
    }

    /**
     * Get width for a POI defined border.
     * @param poiBorder the POI border value
     * @return the width of the border in points
     */
    protected float getBorderWidth(short poiBorder) {
        switch (poiBorder) {
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_NONE:
                return 0;
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_THIN:
                return 0.75f;
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM:
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASHED:
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASH_DOT:
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM_DASH_DOT_DOT:
                return 1.75f;
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_THICK:
                return 2;
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_DASHED:
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_DOTTED:
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_DOUBLE:
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_HAIR:
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_DASH_DOT:
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_DASH_DOT_DOT:
            case org.apache.poi.ss.usermodel.CellStyle.BORDER_SLANTED_DASH_DOT:
            default:
                return 1;
        }
    }

    /**
     * Translate border width to POI border.
     * @param borderStyle the border style
     * @return the POI constant to use
     */
    protected short getPoiBorder(BorderStyle borderStyle) {
        float width = borderStyle.getWidth();
        if (width == 0) {
            return org.apache.poi.ss.usermodel.CellStyle.BORDER_NONE;
        }
        if (width <= 0.75f) {
            return org.apache.poi.ss.usermodel.CellStyle.BORDER_THIN;
        }
        if (width <= 1.75f) {
            return org.apache.poi.ss.usermodel.CellStyle.BORDER_MEDIUM;
        }
        if (width <= 2.0f) {
            return org.apache.poi.ss.usermodel.CellStyle.BORDER_THICK;
        }
        return org.apache.poi.ss.usermodel.CellStyle.BORDER_THIN;
    }

    @Override
    public FillPattern getFillPattern() {
        return poiCellStyle.getFillPattern() == 1 ? FillPattern.SOLID
                : FillPattern.NONE;
    }

    @Override
    public void setFillPattern(FillPattern pattern) {
        switch (pattern) {
            case NONE:
                poiCellStyle.setFillPattern(org.apache.poi.ss.usermodel.CellStyle.NO_FILL);
                break;
            case SOLID:
                poiCellStyle.setFillPattern(org.apache.poi.ss.usermodel.CellStyle.SOLID_FOREGROUND);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isWrap() {
        return poiCellStyle.getWrapText();
    }

    @Override
    public void setWrap(boolean wrap) {
        poiCellStyle.setWrapText(wrap);
    }

    @Override
    public void setDataFormat(String format) {
        poiCellStyle.setDataFormat(getWorkbook().poiWorkbook.createDataFormat().getFormat(format));
    }

    @Override
    public String getDataFormat() {
        return poiCellStyle.getDataFormatString();
    }

    @Override
    public PoiFont getFont() {
        return font;
    }

    @Override
    public void setFont(Font font) {
        poiCellStyle.setFont(workbook.getPoiFont(font, Style.none()).getPoiFont());
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

    static class PoiHssfCellStyle extends PoiCellStyle {

        PoiHssfCellStyle(PoiHssfWorkbook workbook,
                HSSFCellStyle poiCellStyle) {
            super(workbook, workbook.getFont(poiCellStyle.getFont(workbook.poiWorkbook)), poiCellStyle);
        }

        @Override
        public Color getFillBgColor() {
            return workbook.getColor(
                    poiCellStyle.getFillBackgroundColorColor(), Color.WHITE);
        }

        @Override
        public void setFillBgColor(Color color) {
            poiCellStyle.setFillBackgroundColor(((PoiHssfWorkbook) workbook).getPoiColor(color).getIndex());
        }

        @Override
        public Color getFillFgColor() {
            return workbook.getColor(
                    poiCellStyle.getFillForegroundColorColor(), null);
        }

        @Override
        public void setFillFgColor(Color color) {
            if (color!=null) {
                poiCellStyle.setFillForegroundColor(((PoiHssfWorkbook) workbook).getPoiColor(color).getIndex());
            }
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
        public void setBorderStyle(Direction d, BorderStyle borderStyle) {
            short poiBorder = getPoiBorder(borderStyle);
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

    }

    static class PoiXssfCellStyle
            extends PoiCellStyle {

        PoiXssfCellStyle(PoiXssfWorkbook workbook,
                XSSFCellStyle poiCellStyle) {
            super(workbook, new PoiFont(workbook, poiCellStyle.getFont()), poiCellStyle);
        }

        @Override
        public Color getFillBgColor() {
            return workbook.getColor(
                    poiCellStyle.getFillBackgroundColorColor(), Color.WHITE);
        }

        @Override
        public void setFillBgColor(Color color) {
            final XSSFColor poiColor = color==null?null:((PoiXssfWorkbook) workbook).getPoiColor(color);
            ((XSSFCellStyle) poiCellStyle).setFillBackgroundColor(poiColor);
        }

        @Override
        public Color getFillFgColor() {
            return workbook.getColor(poiCellStyle.getFillForegroundColorColor(), null);
        }

        @Override
        public void setFillFgColor(Color color) {
            final XSSFColor poiColor = color==null?null:((PoiXssfWorkbook) workbook).getPoiColor(color);
            ((XSSFCellStyle) poiCellStyle).setFillForegroundColor(poiColor);
        }

        @Override
        public BorderStyle getBorderStyle(Direction d) {
            final Color color;
            final float width;
            switch (d) {
                case NORTH:
                    width = getBorderWidth(poiCellStyle.getBorderTop());
                    color = workbook.getColor(((XSSFCellStyle) poiCellStyle).getTopBorderXSSFColor(),
                            Color.BLACK);
                    break;
                case EAST:
                    width = getBorderWidth(poiCellStyle.getBorderRight());
                    color = workbook.getColor(
                            ((XSSFCellStyle) poiCellStyle).getRightBorderXSSFColor(), Color.BLACK);
                    break;
                case SOUTH:
                    width = getBorderWidth(poiCellStyle.getBorderBottom());
                    color = workbook.getColor(
                            ((XSSFCellStyle) poiCellStyle).getBottomBorderXSSFColor(), Color.BLACK);
                    break;
                case WEST:
                    width = getBorderWidth(poiCellStyle.getBorderLeft());
                    color = workbook.getColor(
                            ((XSSFCellStyle) poiCellStyle).getLeftBorderXSSFColor(), Color.BLACK);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            return new BorderStyle(width, color);
        }

        @Override
        public void setBorderStyle(Direction d, BorderStyle borderStyle) {
            short poiBorder = getPoiBorder(borderStyle);
            final Color color = borderStyle.getColor();
            XSSFColor poiColor = color==null?null:((PoiXssfWorkbook) workbook).getPoiColor(color);
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
    }

}
