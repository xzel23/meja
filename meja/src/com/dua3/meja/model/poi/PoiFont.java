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

import com.dua3.meja.model.Font;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Objects;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
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
 * @param <CELLSTYLE>
 * @param <COLOR>
 */
public abstract class PoiFont<WORKBOOK extends org.apache.poi.ss.usermodel.Workbook, SHEET extends org.apache.poi.ss.usermodel.Sheet, ROW extends org.apache.poi.ss.usermodel.Row, CELL extends org.apache.poi.ss.usermodel.Cell, CELLSTYLE extends org.apache.poi.ss.usermodel.CellStyle, COLOR extends org.apache.poi.ss.usermodel.Color>
        implements Font {

    protected abstract org.apache.poi.ss.usermodel.Font getPoiFont();

    @Override
    public double getSizeInPoints() {
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

    static class PoiHssfFont extends PoiFont<HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell, HSSFCellStyle, HSSFColor> {

        private final PoiHssfWorkbook workbook;

        private final HSSFFont poiFont;

        PoiHssfFont(PoiHssfWorkbook workbook, HSSFFont poiFont) {
            this.workbook = workbook;
            this.poiFont = poiFont;
        }

        @Override
        public HSSFFont getPoiFont() {
            return poiFont;
        }

        @Override
        public Color getColor() {
            return workbook.getColor(poiFont.getHSSFColor(workbook.getPoiWorkbook()), Color.BLACK);
        }
    }

    static class PoiXssfFont extends PoiFont<XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell, XSSFCellStyle, XSSFColor> {

        private final PoiXssfWorkbook workbook;

        private final XSSFFont poiFont;

        @Override
        public XSSFFont getPoiFont() {
            return poiFont;
        }

        PoiXssfFont(PoiXssfWorkbook workbook, XSSFFont poiFont) {
            this.workbook = workbook;
            this.poiFont = poiFont;
        }

        @Override
        public Color getColor() {
            return workbook.getColor(poiFont.getXSSFColor(), Color.BLACK);
        }
    }

}
