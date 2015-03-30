/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import com.dua3.meja.model.poi.PoiFont.PoiHssfFont;
import com.dua3.meja.model.poi.PoiFont.PoiXssfFont;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author axel
 * @param <WORKBOOK>
 * @param <SHEET>
 * @param <ROW>
 * @param <CELL>
 * @param <COLOR>
 * @param <CELLSTYLE>
 */
public abstract class PoiCellStyle<WORKBOOK extends org.apache.poi.ss.usermodel.Workbook, SHEET extends org.apache.poi.ss.usermodel.Sheet, ROW extends org.apache.poi.ss.usermodel.Row, CELL extends org.apache.poi.ss.usermodel.Cell, CELLSTYLE extends org.apache.poi.ss.usermodel.CellStyle, COLOR extends org.apache.poi.ss.usermodel.Color> implements CellStyle {

    protected final CELLSTYLE poiCellStyle;

    protected PoiCellStyle(CELLSTYLE poiCellStyle) {
        this.poiCellStyle = poiCellStyle;
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
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_FILL:
                return HAlign.ALIGN_JUSTIFY;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_GENERAL:
                return HAlign.ALIGN_LEFT;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_JUSTIFY:
                return HAlign.ALIGN_JUSTIFY;
            default:
                Logger.getLogger(PoiCellStyle.class.getName()).log(Level.WARNING, "Unknown value for horizontal algnment: {0}", alignment);
                return HAlign.ALIGN_LEFT;
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
                Logger.getLogger(PoiCellStyle.class.getName()).log(Level.WARNING, "Unknown value for vertical algnment: {0}", alignment);
                return VAlign.ALIGN_MIDDLE;
        }
    }

    @Override
    public abstract PoiFont getFont();

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

    @Override
    public FillPattern getFillPattern() {
        return poiCellStyle.getFillPattern() == 1 ? FillPattern.SOLID : FillPattern.NONE;
    }

    @Override
    public boolean isWrap() {
        return poiCellStyle.getWrapText();
    }

    static class PoiHssfCellStyle extends PoiCellStyle<HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell, HSSFCellStyle, HSSFColor> {

        private final PoiHssfWorkbook workbook;
        private final PoiHssfFont font;

        public PoiHssfCellStyle(PoiHssfWorkbook workbook, HSSFCellStyle poiCellStyle) {
            super(poiCellStyle);
            this.workbook = workbook;
            this.font = workbook.getFont(poiCellStyle.getFont(workbook.getPoiWorkbook()));
        }

        @Override
        public Color getFillBgColor() {
            return workbook.getColor(poiCellStyle.getFillBackgroundColorColor());
        }

        @Override
        public Color getFillFgColor() {
            return workbook.getColor(poiCellStyle.getFillForegroundColorColor());
        }

        @Override
        public PoiHssfFont getFont() {
            return font;
        }

        @Override
        public BorderStyle getBorderStyle(Direction d) {
            final Color color;
            final float width;
            switch (d) {
                case NORTH:
                    color = workbook.getColor(poiCellStyle.getTopBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderTop());
                    break;
                case EAST:
                    color = workbook.getColor(poiCellStyle.getRightBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderRight());
                    break;
                case SOUTH:
                    color = workbook.getColor(poiCellStyle.getBottomBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderBottom());
                    break;
                case WEST:
                    color = workbook.getColor(poiCellStyle.getLeftBorderColor());
                    width = getBorderWidth(poiCellStyle.getBorderLeft());
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            return new BorderStyle(width, color);
        }

    }

    static class PoiXssfCellStyle extends PoiCellStyle<XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell, XSSFCellStyle, XSSFColor> {

        private final PoiXssfWorkbook workbook;
        private final PoiXssfFont font;

        public PoiXssfCellStyle(PoiXssfWorkbook workbook, XSSFCellStyle poiCellStyle) {
            super(poiCellStyle);
            this.workbook = workbook;
            this.font = new PoiXssfFont(workbook, poiCellStyle.getFont());
        }

        @Override
        public Color getFillBgColor() {
            return workbook.getColor(poiCellStyle.getFillBackgroundXSSFColor());
        }

        @Override
        public Color getFillFgColor() {
            return workbook.getColor(poiCellStyle.getFillForegroundXSSFColor());
        }

        @Override
        public PoiXssfFont getFont() {
            return font;
        }

        @Override
        public BorderStyle getBorderStyle(Direction d) {
            final Color color;
            final float width;
            switch (d) {
                case NORTH:
                    color = workbook.getColor(poiCellStyle.getTopBorderXSSFColor());
                    width = getBorderWidth(poiCellStyle.getBorderTop());
                    break;
                case EAST:
                    color = workbook.getColor(poiCellStyle.getRightBorderXSSFColor());
                    width = getBorderWidth(poiCellStyle.getBorderRight());
                    break;
                case SOUTH:
                    color = workbook.getColor(poiCellStyle.getBottomBorderXSSFColor());
                    width = getBorderWidth(poiCellStyle.getBorderBottom());
                    break;
                case WEST:
                    color = workbook.getColor(poiCellStyle.getLeftBorderXSSFColor());
                    width = getBorderWidth(poiCellStyle.getBorderLeft());
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            return new BorderStyle(width, color);
        }
    }
}
