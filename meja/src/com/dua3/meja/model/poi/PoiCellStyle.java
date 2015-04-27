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
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 *
 * @author axel
 */
public abstract class PoiCellStyle implements CellStyle {

    protected final PoiWorkbook workbook;
    protected final PoiFont font;
    protected final org.apache.poi.ss.usermodel.CellStyle poiCellStyle;

    protected PoiCellStyle(PoiWorkbook workbook, PoiFont font, org.apache.poi.ss.usermodel.CellStyle poiCellStyle) {
        this.workbook = workbook;
        this.font = font;
        this.poiCellStyle = poiCellStyle;
    }
    
    public PoiWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public HAlign getHAlign() {
        final short alignment = poiCellStyle.getAlignment();
        switch (alignment) {
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_LEFT:
                return HAlign.ALIGN_LEFT;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER:
                return HAlign.ALIGN_CENTER;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_RIGHT:
                return HAlign.ALIGN_RIGHT;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER_SELECTION:
                return HAlign.ALIGN_CENTER;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_GENERAL:
                return HAlign.ALIGN_AUTOMATIC;
            default:
                return HAlign.ALIGN_JUSTIFY;
        }
    }

    @Override
    public void setHAlign(HAlign hAlign) {
        switch (hAlign) {
            case ALIGN_LEFT:
                poiCellStyle
                        .setAlignment(org.apache.poi.ss.usermodel.CellStyle.ALIGN_LEFT);
                break;
            case ALIGN_RIGHT:
                poiCellStyle
                        .setAlignment(org.apache.poi.ss.usermodel.CellStyle.ALIGN_RIGHT);
                break;
            case ALIGN_CENTER:
                poiCellStyle
                        .setAlignment(org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER);
                break;
            case ALIGN_JUSTIFY:
                poiCellStyle
                        .setAlignment(org.apache.poi.ss.usermodel.CellStyle.ALIGN_JUSTIFY);
                break;
            case ALIGN_AUTOMATIC:
                poiCellStyle
                        .setAlignment(org.apache.poi.ss.usermodel.CellStyle.ALIGN_GENERAL);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public VAlign getVAlign() {
        final short alignment = poiCellStyle.getVerticalAlignment();
        switch (alignment) {
            case org.apache.poi.ss.usermodel.CellStyle.VERTICAL_TOP:
                return VAlign.ALIGN_TOP;
            case org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER:
                return VAlign.ALIGN_MIDDLE;
            case org.apache.poi.ss.usermodel.CellStyle.VERTICAL_BOTTOM:
                return VAlign.ALIGN_BOTTOM;
            case org.apache.poi.ss.usermodel.CellStyle.VERTICAL_JUSTIFY:
                return VAlign.ALIGN_JUSTIFY;
            default:
                Logger.getLogger(PoiCellStyle.class.getName()).log(Level.WARNING,
                        "Unknown value for vertical algnment: {0}", alignment);
                return VAlign.ALIGN_MIDDLE;
        }
    }

    @Override
    public void setVAlign(VAlign vAlign) {
        switch (vAlign) {
            case ALIGN_TOP:
                poiCellStyle
                        .setVerticalAlignment(org.apache.poi.ss.usermodel.CellStyle.VERTICAL_TOP);
                break;
            case ALIGN_MIDDLE:
                poiCellStyle
                        .setVerticalAlignment(org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER);
                break;
            case ALIGN_BOTTOM:
                poiCellStyle
                        .setVerticalAlignment(org.apache.poi.ss.usermodel.CellStyle.VERTICAL_BOTTOM);
                break;
            case ALIGN_JUSTIFY:
                poiCellStyle
                        .setVerticalAlignment(org.apache.poi.ss.usermodel.CellStyle.VERTICAL_JUSTIFY);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

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
        if (font instanceof PoiFont) {
            poiCellStyle.setFont(((PoiFont) font).getPoiFont());
        } else {
            throw new IllegalArgumentException("Incompatible implementation class.");
        }
    }
        
    static class PoiHssfCellStyle
    extends PoiCellStyle {


        public PoiHssfCellStyle(PoiHssfWorkbook workbook,
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
            poiCellStyle.setFillBackgroundColor(((PoiHssfWorkbook)workbook).getPoiColor(color).getIndex());
        }

        @Override
        public Color getFillFgColor() {
            return workbook.getColor(
                    poiCellStyle.getFillForegroundColorColor(), null);
        }

        @Override
        public void setFillFgColor(Color color) {
            poiCellStyle.setFillForegroundColor(((PoiHssfWorkbook)workbook).getPoiColor(color).getIndex());
        }

        @Override
        public BorderStyle getBorderStyle(Direction d) {
            final Color color;
            final float width;
            switch (d) {
                case NORTH:
                    color = ((PoiHssfWorkbook)workbook).getColor(poiCellStyle.getTopBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderTop());
                    break;
                case EAST:
                    color = ((PoiHssfWorkbook)workbook).getColor(poiCellStyle.getRightBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderRight());
                    break;
                case SOUTH:
                    color = ((PoiHssfWorkbook)workbook).getColor(poiCellStyle.getBottomBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderBottom());
                    break;
                case WEST:
                    color = ((PoiHssfWorkbook)workbook).getColor(poiCellStyle.getLeftBorderColor());
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
            short poiColor = ((PoiHssfWorkbook)workbook).getPoiColor(borderStyle.getColor()).getIndex();
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
    public PoiXssfCellStyle(PoiXssfWorkbook workbook,
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
            ((XSSFCellStyle)poiCellStyle).setFillBackgroundColor(((PoiXssfWorkbook)workbook).getPoiColor(color));
        }

        @Override
        public Color getFillFgColor() {
            return workbook.getColor(
                    poiCellStyle.getFillForegroundColorColor(), null);
        }

        @Override
        public void setFillFgColor(Color color) {
            ((XSSFCellStyle)poiCellStyle).setFillForegroundColor(((PoiXssfWorkbook)workbook).getPoiColor(color));
        }

        @Override
        public BorderStyle getBorderStyle(Direction d) {
            final Color color;
            final float width;
            switch (d) {
                case NORTH:
                    color = workbook.getColor(((XSSFCellStyle)poiCellStyle).getTopBorderXSSFColor(),
                            Color.BLACK);
                    width = getBorderWidth(poiCellStyle.getBorderTop());
                    break;
                case EAST:
                    color = workbook.getColor(
                            ((XSSFCellStyle)poiCellStyle).getRightBorderXSSFColor(), Color.BLACK);
                    width = getBorderWidth(poiCellStyle.getBorderRight());
                    break;
                case SOUTH:
                    color = workbook.getColor(
                            ((XSSFCellStyle)poiCellStyle).getBottomBorderXSSFColor(), Color.BLACK);
                    width = getBorderWidth(poiCellStyle.getBorderBottom());
                    break;
                case WEST:
                    color = workbook.getColor(
                            ((XSSFCellStyle)poiCellStyle).getLeftBorderXSSFColor(), Color.BLACK);
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
            XSSFColor poiColor = ((PoiXssfWorkbook)workbook).getPoiColor(borderStyle.getColor());
            switch (d) {
                case NORTH:
                    ((XSSFCellStyle)poiCellStyle).setTopBorderColor(poiColor);
                    poiCellStyle.setBorderTop(poiBorder);
                    break;
                case EAST:
                    ((XSSFCellStyle)poiCellStyle).setRightBorderColor(poiColor);
                    poiCellStyle.setBorderRight(poiBorder);
                    break;
                case SOUTH:
                    ((XSSFCellStyle)poiCellStyle).setBottomBorderColor(poiColor);
                    poiCellStyle.setBorderBottom(poiBorder);
                    break;
                case WEST:
                    ((XSSFCellStyle)poiCellStyle).setLeftBorderColor(poiColor);
                    poiCellStyle.setBorderLeft(poiBorder);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

}
