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

import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiCellStyle.PoiHssfCellStyle;
import com.dua3.meja.model.poi.PoiCellStyle.PoiXssfCellStyle;
import com.dua3.meja.model.poi.PoiFont.PoiHssfFont;
import com.dua3.meja.model.poi.PoiFont.PoiXssfFont;
import com.dua3.meja.model.poi.PoiSheet.PoiHssfSheet;
import com.dua3.meja.model.poi.PoiSheet.PoiXssfSheet;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
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
public abstract class PoiWorkbook<WORKBOOK extends org.apache.poi.ss.usermodel.Workbook, SHEET extends org.apache.poi.ss.usermodel.Sheet, ROW extends org.apache.poi.ss.usermodel.Row, CELL extends org.apache.poi.ss.usermodel.Cell, CELLSTYLE extends org.apache.poi.ss.usermodel.CellStyle, COLOR extends org.apache.poi.ss.usermodel.Color>
        implements Workbook {

    protected final WORKBOOK poiWorkbook;
    protected final FormulaEvaluator evaluator;
    protected final List<PoiSheet<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR>> sheets = new ArrayList<>();
    protected final Map<String, Short> cellStyles = new HashMap<>();
    protected final org.apache.poi.ss.usermodel.DataFormatter dataFormatter;

    protected PoiWorkbook(WORKBOOK poiWorkbook, Locale locale) {
        this.poiWorkbook = poiWorkbook;
        this.evaluator = poiWorkbook.getCreationHelper().createFormulaEvaluator();
        this.dataFormatter = new org.apache.poi.ss.usermodel.DataFormatter(locale);
    }

    protected abstract PoiSheet<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> createSheet(SHEET poiSheet);

    @SuppressWarnings("unchecked")
    protected void init() {
        for (int i = 0; i < poiWorkbook.getNumberOfSheets(); i++) {
            sheets.add(createSheet((SHEET) poiWorkbook.getSheetAt(i)));
        }
    }

    org.apache.poi.ss.usermodel.DataFormatter getDataFormatter() {
        return dataFormatter;
    }

    abstract Color getColor(COLOR poiColor, Color defaultColor);

    @Override
    public int getNumberOfSheets() {
        assert poiWorkbook.getNumberOfSheets() == sheets.size();
        return sheets.size();
    }

    @Override
    public PoiSheet<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getSheetByNr(int sheetNr) {
        return sheets.get(sheetNr);
    }

    @Override
    public PoiSheet<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getSheetByName(String sheetName) {
        for (PoiSheet<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> sheet : sheets) {
            if (sheet.getSheetName().equals(sheetName)) {
                return sheet;
            }
        }
        throw new IllegalArgumentException("No sheet '" + sheetName + "'.");
    }

    public abstract PoiCellStyle<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getPoiCellStyle(CELLSTYLE cellStyle);

    public abstract PoiCellStyle<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getDefaultCellStyle();

    WORKBOOK getPoiWorkbook() {
        return poiWorkbook;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        poiWorkbook.write(out);
    }

    @Override
    public void write(File file, boolean overwriteIfExists) throws IOException {
        boolean exists = file.createNewFile();
        if (!exists || overwriteIfExists) {
            try (FileOutputStream out = new FileOutputStream(file)) {
                poiWorkbook.write(out);
            }
        } else {
            throw new IOException("File '" + file.getAbsolutePath() + "' already present and overwriteIfExists is false.");
        }
    }

    @Override
    public void close() throws IOException {
        poiWorkbook.close();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiWorkbook) {
            return Objects.equals(poiWorkbook, ((PoiWorkbook) obj).poiWorkbook);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return poiWorkbook.hashCode();
    }

    @SuppressWarnings("rawtypes")
	@Override
    public PoiCellStyle<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> copyCellStyle(String styleName, CellStyle style) {
        PoiCellStyle<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> cellStyle = getCellStyle(styleName);
        cellStyle.poiCellStyle.cloneStyleFrom(((PoiCellStyle) style).poiCellStyle);
        return cellStyle;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PoiCellStyle<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getCellStyle(String name) {
        Short index = cellStyles.get(name);
        CELLSTYLE poiCellStyle;
        if (index == 0) {
            poiCellStyle = (CELLSTYLE) poiWorkbook.createCellStyle();
            index = poiCellStyle.getIndex();
            cellStyles.put(name, index);
        } else {
            poiCellStyle = (CELLSTYLE) poiWorkbook.getCellStyleAt(index);
        }
        return getPoiCellStyle(poiCellStyle);
    }

    static class PoiHssfWorkbook
            extends PoiWorkbook<HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell, HSSFCellStyle, HSSFColor> {

        private final PoiHssfCellStyle defaultCellStyle;

        public PoiHssfWorkbook(HSSFWorkbook poiWorkbook, Locale locale) {
            super(poiWorkbook, locale);
            this.defaultCellStyle = new PoiHssfCellStyle(this, poiWorkbook.getCellStyleAt((short) 0));
            init();
        }

        @Override
        public PoiHssfCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        Color getColor(short idx) {
            return getColor(poiWorkbook.getCustomPalette().getColor(idx), null);
        }

        @Override
        Color getColor(org.apache.poi.hssf.util.HSSFColor poiColor, Color defaultColor) {
            if (poiColor == null || poiColor == HSSFColor.AUTOMATIC.getInstance()) {
                return defaultColor;
            }

            short[] triplet = poiColor.getTriplet();

            if (triplet == null) {
                return defaultColor;
            }

            int a = 0xff;
            int r = triplet[0];
            int g = triplet[1];
            int b = triplet[2];
            return new Color(r, g, b, a);
        }

        @Override
        public PoiHssfSheet getSheetByNr(int sheetNr) {
            return (PoiSheet.PoiHssfSheet) super.getSheetByNr(sheetNr);
        }

        @Override
        public PoiHssfSheet getSheetByName(String sheetName) {
            return (PoiSheet.PoiHssfSheet) super.getSheetByName(sheetName);
        }

        @Override
        protected PoiHssfSheet createSheet(HSSFSheet poiSheet) {
            return new PoiHssfSheet(this, poiSheet);
        }

        @Override
        public PoiHssfCellStyle getPoiCellStyle(HSSFCellStyle poiStyle) {
            return new PoiHssfCellStyle(PoiHssfWorkbook.this, poiStyle);
        }

        public PoiHssfFont getFont(HSSFFont poiFont) {
            return new PoiHssfFont(PoiHssfWorkbook.this, poiFont);
        }

        public PoiHssfFont getFont(short idx) {
            return getFont(poiWorkbook.getFontAt(idx));
        }

        @Override
        public Sheet createSheet(String sheetName) {
            HSSFSheet poiSheet = poiWorkbook.createSheet(sheetName);
            PoiSheet<HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell, HSSFCellStyle, HSSFColor> sheet = new PoiHssfSheet(this, poiSheet);
            sheets.add(sheet);
            return sheet;
        }

        public HSSFColor getPoiColor(Color color) {
            HSSFPalette palette = poiWorkbook.getCustomPalette();
            return palette.findSimilarColor(color.getRed(), color.getGreen(), color.getBlue());
        }

    }

    static class PoiXssfWorkbook extends PoiWorkbook<
            XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell, XSSFCellStyle, XSSFColor> {

        private final PoiXssfCellStyle defaultCellStyle;

        public PoiXssfWorkbook(XSSFWorkbook poiWorkbook, Locale locale) {
            super(poiWorkbook, locale);
            this.defaultCellStyle = new PoiXssfCellStyle(this, poiWorkbook.getCellStyleAt((short) 0));
            init();
        }

        @Override
        public PoiXssfCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        @Override
        Color getColor(org.apache.poi.xssf.usermodel.XSSFColor poiColor, Color defaultColor) {
            if (poiColor == null || poiColor.isAuto()) {
                return defaultColor;
            }

            byte[] rgb = poiColor.getARgb();

            if (rgb == null) {
                return defaultColor;
            }

            int a = rgb[0] & 0xFF;
            int r = rgb[1] & 0xFF;
            int g = rgb[2] & 0xFF;
            int b = rgb[3] & 0xFF;

            return new Color(r, g, b, a);
        }

        @Override
        public PoiXssfSheet getSheetByNr(int sheetNr) {
            return (PoiXssfSheet) super.getSheetByNr(sheetNr);
        }

        @Override
        public PoiXssfSheet getSheetByName(String sheetName) {
            return (PoiXssfSheet) super.getSheetByName(sheetName);
        }

        @Override
        protected PoiXssfSheet createSheet(XSSFSheet poiSheet) {
            return new PoiXssfSheet(this, poiSheet);
        }

        @Override
        public PoiXssfCellStyle getPoiCellStyle(XSSFCellStyle poiStyle) {
            return new PoiXssfCellStyle(PoiXssfWorkbook.this, poiStyle);
        }

        public PoiXssfFont getFont(XSSFFont poiFont) {
            return new PoiXssfFont(PoiXssfWorkbook.this, poiFont);
        }

        @Override
        public Sheet createSheet(String sheetName) {
            XSSFSheet poiSheet = poiWorkbook.createSheet(sheetName);
            PoiSheet<XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell, XSSFCellStyle, XSSFColor> sheet = new PoiXssfSheet(this, poiSheet);
            sheets.add(sheet);
            return sheet;
        }

        public XSSFColor getPoiColor(Color color) {
            return new XSSFColor(color);
        }

    }

}
