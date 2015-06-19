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

import com.dua3.meja.io.FileType;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiCellStyle.PoiHssfCellStyle;
import com.dua3.meja.model.poi.PoiCellStyle.PoiXssfCellStyle;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author axel
 */
public abstract class PoiWorkbook implements Workbook {

    /**
     *
     */
    protected final org.apache.poi.ss.usermodel.Workbook poiWorkbook;

    /**
     *
     */
    protected final FormulaEvaluator evaluator;

    /**
     *
     */
    protected final List<PoiSheet> sheets = new ArrayList<>();

    /**
     *
     */
    protected final Map<String, Short> cellStyles = new HashMap<>();

    /**
     *
     */
    protected final org.apache.poi.ss.usermodel.DataFormatter dataFormatter;

    /**
     *
     */
    protected Locale locale;

    /**
     *
     */
    protected URI uri;

    /**
     *
     * @param poiWorkbook
     * @param locale
     * @param uri
     */
    protected PoiWorkbook(org.apache.poi.ss.usermodel.Workbook poiWorkbook, Locale locale, URI uri) {
        this.locale = locale;
        this.poiWorkbook = poiWorkbook;
        this.evaluator = poiWorkbook.getCreationHelper().createFormulaEvaluator();
        this.dataFormatter = new org.apache.poi.ss.usermodel.DataFormatter(locale);
        this.uri = uri;

        // init cell style map
        for (short i = 0; i < poiWorkbook.getNumCellStyles(); i++) {
            cellStyles.put("style#" + i, i);
        }
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    protected void init() {
        for (int i = 0; i < poiWorkbook.getNumberOfSheets(); i++) {
            sheets.add(createSheet(poiWorkbook.getSheetAt(i)));
        }
    }

    org.apache.poi.ss.usermodel.DataFormatter getDataFormatter() {
        return dataFormatter;
    }

    abstract Color getColor(org.apache.poi.ss.usermodel.Color poiColor, Color defaultColor);

    @Override
    public int getNumberOfSheets() {
        assert poiWorkbook.getNumberOfSheets() == sheets.size();
        return sheets.size();
    }

    @Override
    public PoiSheet getSheetByNr(int sheetNr) {
        return sheets.get(sheetNr);
    }

    @Override
    public PoiSheet getSheetByName(String sheetName) {
        for (PoiSheet sheet : sheets) {
            if (sheet.getSheetName().equals(sheetName)) {
                return sheet;
            }
        }
        throw new IllegalArgumentException("No sheet '" + sheetName + "'.");
    }

    /**
     * Get {@link PoiCellStyle} from {@link org.apache.poi.ss.usermodel.CellStyle}.
     * @param cellStyle POI cell style
     * @return instance of {@link PoiCellStyle}
     */
    public abstract PoiCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle cellStyle);

    @Override
    public abstract PoiCellStyle getDefaultCellStyle();

    org.apache.poi.ss.usermodel.Workbook getPoiWorkbook() {
        return poiWorkbook;
    }

    @Override
    public void write(FileType type, OutputStream out) throws IOException {
        if ((type == FileType.XLSX && ((poiWorkbook instanceof XSSFWorkbook) || (poiWorkbook instanceof SXSSFWorkbook)))
                || (type == FileType.XLS && poiWorkbook instanceof HSSFWorkbook)) {
            // if Workbook is PoiWorkbook it should be written directly so that
            // features not yet supported by Meja don't get lost in the process
            poiWorkbook.write(out);
        } else {
            type.getWriter().write(this, out);
        }
    }

    @Override
    public boolean write(File file, boolean overwriteIfExists) throws IOException {
        boolean exists = file.createNewFile();
        if (!exists || overwriteIfExists) {
            FileType type = FileType.forFile(file);
            if (type == null) {
                throw new IllegalArgumentException("No matching FileType for file '" + file.getAbsolutePath() + ".");
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                write(type, out);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        poiWorkbook.close();
        if (poiWorkbook instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) poiWorkbook).dispose();
        }
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

    @Override
    public PoiCellStyle copyCellStyle(String styleName, CellStyle style) {
        PoiCellStyle cellStyle = getCellStyle(styleName);
        cellStyle.poiCellStyle.cloneStyleFrom(((PoiCellStyle) style).poiCellStyle);
        return cellStyle;
    }

    @Override
    public PoiCellStyle getCellStyle(String name) {
        Short index = cellStyles.get(name);
        org.apache.poi.ss.usermodel.CellStyle poiCellStyle;
        if (index == null) {
            poiCellStyle = poiWorkbook.createCellStyle();
            index = poiCellStyle.getIndex();
            cellStyles.put(name, index);
        } else {
            poiCellStyle = poiWorkbook.getCellStyleAt(index);
        }
        return getPoiCellStyle(poiCellStyle);
    }

    @Override
    public NumberFormat getNumberFormat() {
        return NumberFormat.getInstance(Locale.getDefault());
    }

    @Override
    public DateFormat getDateFormat() {
        return DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
    }

    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<Sheet>() {
            Iterator<PoiSheet> iter = sheets.iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Sheet next() {
                return iter.next();
            }

            @Override
            public void remove() {
                iter.remove();
            }
        };
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     *
     * @param poiFont
     * @return
     */
    public PoiFont getFont(org.apache.poi.ss.usermodel.Font poiFont) {
        return poiFont == null ? getDefaultCellStyle().getFont() : new PoiFont(this, poiFont);
    }

    /**
     *
     * @param poiFont
     * @param dfltColor
     * @return
     */
    public abstract Color getColor(org.apache.poi.ss.usermodel.Font poiFont, Color dfltColor);

    /**
     *
     * @param poiSheet
     * @return
     */
    protected PoiSheet createSheet(org.apache.poi.ss.usermodel.Sheet poiSheet) {
        return new PoiSheet(this, poiSheet);
    }

    @Override
    public Sheet createSheet(String sheetName) {
        org.apache.poi.ss.usermodel.Sheet poiSheet = poiWorkbook.createSheet(sheetName);
        PoiSheet sheet = new PoiSheet(this, poiSheet);
        sheets.add(sheet);
        return sheet;
    }

    @Override
    public List<String> getCellStyleNames() {
        return new ArrayList<>(cellStyles.keySet());
    }

    String getCellStyleName(PoiCellStyle cellStyle) {
        final short styleIndex = cellStyle.poiCellStyle.getIndex();
        for (Map.Entry<String, Short> entry : cellStyles.entrySet()) {
            if (entry.getValue() == styleIndex) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("CellStyle is not from this workbook.");
    }

    @Override
    public void copy(Workbook other) {
        // copy styles
        for (String styleName : other.getCellStyleNames()) {
            CellStyle cellStyle = other.getCellStyle(styleName);
            CellStyle newCellStyle = getCellStyle(styleName);
            newCellStyle.copyStyle(cellStyle);
        }

        // copy sheets
        for (int sheetNr = 0; sheetNr < other.getNumberOfSheets(); sheetNr++) {
            Sheet sheet = other.getSheetByNr(sheetNr);
            Sheet newSheet = createSheet(sheet.getSheetName());
            newSheet.copy(sheet);
        }
    }

    /**
     *
     * @param color
     * @return
     */
    public abstract org.apache.poi.ss.usermodel.Color getPoiColor(Color color);

    /**
     *
     * @param s
     * @return
     */
    public abstract RichTextString createRichTextString(String s);

    /**
     *
     * @param fontFamily
     * @param fontSize
     * @param fontColor
     * @param fontBold
     * @param fontItalic
     * @param fontUnderlined
     * @param fontStrikeThrough
     * @return
     */
    public abstract PoiFont createFont(String fontFamily, float fontSize, Color fontColor, boolean fontBold, boolean fontItalic, boolean fontUnderlined, boolean fontStrikeThrough);

    /**
     *
     */
    public static class PoiHssfWorkbook extends PoiWorkbook {

        private final PoiHssfCellStyle defaultCellStyle;

        /**
         *
         * @param poiWorkbook
         * @param locale
         * @param uri
         */
        public PoiHssfWorkbook(HSSFWorkbook poiWorkbook, Locale locale, URI uri) {
            super(poiWorkbook, locale, uri);
            this.defaultCellStyle = new PoiHssfCellStyle(this, poiWorkbook.getCellStyleAt((short) 0));
            cellStyles.put("", (short) 0);
            init();
        }

        @Override
        public PoiHssfCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        Color getColor(short idx) {
            return getColor(((HSSFWorkbook) poiWorkbook).getCustomPalette().getColor(idx), null);
        }

        @Override
        Color getColor(org.apache.poi.ss.usermodel.Color poiColor, Color defaultColor) {
            if (poiColor == null || poiColor == HSSFColor.AUTOMATIC.getInstance()) {
                return defaultColor;
            }

            short[] triplet = ((HSSFColor) poiColor).getTriplet();

            if (triplet == null) {
                return defaultColor;
            }

            int a = 0xff;
            int r = triplet[0];
            int g = triplet[1];
            int b = triplet[2];
            return new Color(r, g, b, a);
        }

        /**
         *
         * @param poiStyle
         * @return
         */
        @Override
        public PoiHssfCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle poiStyle) {
            return new PoiHssfCellStyle(this, (HSSFCellStyle) poiStyle);
        }

        /**
         *
         * @param idx
         * @return
         */
        public PoiFont getFont(short idx) {
            return getFont(((HSSFWorkbook) poiWorkbook).getFontAt(idx));
        }

        /**
         *
         * @param color
         * @return
         */
        @Override
        public HSSFColor getPoiColor(Color color) {
            if (color == null) {
                return null;
            }
            HSSFPalette palette = ((HSSFWorkbook) poiWorkbook).getCustomPalette();
            return palette.findSimilarColor(color.getRed(), color.getGreen(), color.getBlue());
        }

        /**
         *
         * @param poiFont
         * @param dfltColor
         * @return
         */
        @Override
        public Color getColor(Font poiFont, Color dfltColor) {
            return getColor(((HSSFFont) poiFont).getHSSFColor(((HSSFWorkbook) getPoiWorkbook())), Color.BLACK);
        }

        /**
         *
         * @param s
         * @return
         */
        @Override
        public HSSFRichTextString createRichTextString(String s) {
            return new HSSFRichTextString(s);
        }

        /**
         *
         * @param fontFamily
         * @param fontSize
         * @param fontColor
         * @param fontBold
         * @param fontItalic
         * @param fontUnderlined
         * @param fontStrikeThrough
         * @return
         */
        @Override
        public PoiFont createFont(String fontFamily, float fontSize, Color fontColor, boolean fontBold, boolean fontItalic, boolean fontUnderlined, boolean fontStrikeThrough) {
            Font poiFont = poiWorkbook.createFont();
            poiFont.setFontName(fontFamily);
            poiFont.setFontHeightInPoints((short) Math.round(fontSize));
            poiFont.setColor(getPoiColor(fontColor).getIndex());
            poiFont.setBold(fontBold);
            poiFont.setItalic(fontItalic);
            poiFont.setUnderline(fontUnderlined ? org.apache.poi.ss.usermodel.Font.U_SINGLE : org.apache.poi.ss.usermodel.Font.U_NONE);
            poiFont.setStrikeout(fontStrikeThrough);
            return new PoiFont(this, poiFont);
        }

    }

    /**
     *
     */
    public static class PoiXssfWorkbook
            extends PoiWorkbook {

        private final PoiXssfCellStyle defaultCellStyle;

        /**
         *
         * @param poiWorkbook
         * @param locale
         * @param uri
         */
        public PoiXssfWorkbook(org.apache.poi.ss.usermodel.Workbook poiWorkbook, Locale locale, URI uri) {
            super(poiWorkbook, locale, uri);
            assert (poiWorkbook instanceof XSSFWorkbook) || (poiWorkbook instanceof SXSSFWorkbook);
            this.defaultCellStyle = new PoiXssfCellStyle(this, (XSSFCellStyle) poiWorkbook.getCellStyleAt((short) 0));
            init();
        }

        /**
         *
         * @param poiStyle
         * @return
         */
        @Override
        public PoiXssfCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle poiStyle) {
            return new PoiXssfCellStyle(this, (XSSFCellStyle) poiStyle);
        }

        @Override
        public PoiXssfCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        @Override
        Color getColor(org.apache.poi.ss.usermodel.Color poiColor, Color defaultColor) {
            if (poiColor == null || ((org.apache.poi.xssf.usermodel.XSSFColor) poiColor).isAuto()) {
                return defaultColor;
            }

            byte[] rgb = ((org.apache.poi.xssf.usermodel.XSSFColor) poiColor).getARgb();

            if (rgb == null) {
                return defaultColor;
            }

            int a = rgb[0] & 0xFF;
            int r = rgb[1] & 0xFF;
            int g = rgb[2] & 0xFF;
            int b = rgb[3] & 0xFF;

            return new Color(r, g, b, a);
        }

        /**
         *
         * @param color
         * @return
         */
        @Override
        public XSSFColor getPoiColor(Color color) {
            return new XSSFColor(color);
        }

        /**
         *
         * @param poiFont
         * @param dfltColor
         * @return
         */
        @Override
        public Color getColor(Font poiFont, Color dfltColor) {
            return getColor(((XSSFFont) poiFont).getXSSFColor(), Color.BLACK);
        }

        /**
         *
         * @param s
         * @return
         */
        @Override
        public XSSFRichTextString createRichTextString(String s) {
            return new XSSFRichTextString(s);
        }

        /**
         *
         * @param fontFamily
         * @param fontSize
         * @param fontColor
         * @param fontBold
         * @param fontItalic
         * @param fontUnderlined
         * @param fontStrikeThrough
         * @return
         */
        @Override
        public PoiFont createFont(String fontFamily, float fontSize, Color fontColor, boolean fontBold, boolean fontItalic, boolean fontUnderlined, boolean fontStrikeThrough) {
            XSSFFont poiFont = (XSSFFont) poiWorkbook.createFont();
            poiFont.setFontName(fontFamily);
            poiFont.setFontHeightInPoints((short) Math.round(fontSize));
            poiFont.setColor(getPoiColor(fontColor));
            poiFont.setBold(fontBold);
            poiFont.setItalic(fontItalic);
            poiFont.setUnderline(fontUnderlined ? org.apache.poi.ss.usermodel.Font.U_SINGLE : org.apache.poi.ss.usermodel.Font.U_NONE);
            poiFont.setStrikeout(fontStrikeThrough);
            return new PoiFont(this, poiFont);
        }
    }

}
