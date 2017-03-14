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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
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

import com.dua3.meja.io.FileType;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Color;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiCellStyle.PoiHssfCellStyle;
import com.dua3.meja.model.poi.PoiCellStyle.PoiXssfCellStyle;
import com.dua3.meja.text.Style;
import com.dua3.meja.util.MejaHelper;

/**
 *
 * @author axel
 */
public abstract class PoiWorkbook implements Workbook {

    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

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
     * Return the standard file type for this implementation.
     * @return the file type matching the underlying POI implementation
     */
    protected abstract FileType getStandardFileType();

    /**
     *
     */
    protected final void init() {
        for (int i = 0; i < poiWorkbook.getNumberOfSheets(); i++) {
            createSheet(poiWorkbook.getSheetAt(i));
        }
    }

    org.apache.poi.ss.usermodel.DataFormatter getDataFormatter() {
        return dataFormatter;
    }

    abstract Color getColor(org.apache.poi.ss.usermodel.Color poiColor, Color defaultColor);

    @Override
    public int getSheetCount() {
        assert poiWorkbook.getNumberOfSheets() == sheets.size();
        return sheets.size();
    }

    @Override
    public PoiSheet getSheet(int sheetNr) {
        return sheets.get(sheetNr);
    }

    @Override
    public PoiSheet getSheetByName(String sheetName) {
        for (PoiSheet sheet : sheets) {
            if (sheet.getSheetName().equals(sheetName)) {
                return sheet;
            }
        }
        return null;
    }

    @Override
    public void removeSheet(int sheetNr) {
        poiWorkbook.removeSheetAt(sheetNr);
        sheets.remove(sheetNr);
        pcs.firePropertyChange(PROPERTY_SHEET_REMOVED, sheetNr, null);
    }

    @Override
    public boolean removeSheetByName(String sheetName) {
        for ( int i=0; i<sheets.size(); i++) {
            if (sheets.get(i).getSheetName().equals(sheetName)) {
                poiWorkbook.removeSheetAt(i);
                sheets.remove(i);
                pcs.firePropertyChange(PROPERTY_SHEET_REMOVED, i, null);
                return true;
            }
        }
        return false;
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
        if (type == getStandardFileType()) {
            // if the workbook is to be saved in the same format, write it out directly so that
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
    public Sheet getCurrentSheet() {
        return getSheet(getCurrentSheetIndex());
    }

    @Override
    public int getCurrentSheetIndex() {
        return poiWorkbook.getActiveSheetIndex();
    }

    @Override
    public void setCurrentSheet(int idx) {
        int oldIdx = getCurrentSheetIndex();
        if (idx != oldIdx) {
            poiWorkbook.setActiveSheet(idx);
            pcs.firePropertyChange(PROPERTY_ACTIVE_SHEET, oldIdx, idx);
        }
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
        URI oldUri = this.uri;
        this.uri = uri;
        pcs.firePropertyChange(PROPERTY_ACTIVE_SHEET, oldUri, uri);
    }

    /**
     * Get font color.
     * @param poiFont instance of POI font
     * @param dfltColor color return if none is set
     * @return the color for the given font
     */
    public abstract Color getColor(org.apache.poi.ss.usermodel.Font poiFont, Color dfltColor);

    /**
     * Create instance of {@link PoiSheet} for  the given POI sheet.
     * @param poiSheet the POI sheet
     * @return instance of {@link PoiSheet}
     */
    protected PoiSheet createSheet(org.apache.poi.ss.usermodel.Sheet poiSheet) {
        PoiSheet sheet = new PoiSheet(this, poiSheet);
        sheets.add(sheet);
        return sheet;
    }

    @Override
    public Sheet createSheet(String sheetName) {
        org.apache.poi.ss.usermodel.Sheet poiSheet = poiWorkbook.createSheet(sheetName);
        PoiSheet sheet = new PoiSheet(this, poiSheet);
        sheets.add(sheet);
        pcs.firePropertyChange(PROPERTY_SHEET_ADDED, null, sheets.size()-1);
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
        for (int sheetNr = 0; sheetNr < other.getSheetCount(); sheetNr++) {
            Sheet sheet = other.getSheet(sheetNr);
            Sheet newSheet = createSheet(sheet.getSheetName());
            newSheet.copy(sheet);
        }
    }

    /**
     * Get POI color.
     * @param color the color
     * @return POI color
     */
    public abstract org.apache.poi.ss.usermodel.Color getPoiColor(Color color);

    /**
     * Test if formula evaluation is supported.
     * @return true if formula evaluation is supported
     */
    public abstract boolean isFormulaEvaluationSupported();

    /**
     * Convert {@link String} to {@link RichTextString}.
     * @param s the {@link String} to convert
     * @return {@link RichTextString} with the same text as {@code s}
     */
    public abstract RichTextString createRichTextString(String s);

    /**
     * Create a new font.
     * @param fontFamily the font family
     * @param fontSize the font size in points
     * @param fontColor the font color
     * @param fontBold whether font should be bold
     * @param fontItalic whether font should be italic
     * @param fontUnderlined whether font should be underlined
     * @param fontStrikeThrough whether font should be strikethrough
     * @return an instance of {@link PoiFont}
     */
    public abstract PoiFont createFont(String fontFamily, float fontSize, Color fontColor, boolean fontBold, boolean fontItalic, boolean fontUnderlined, boolean fontStrikeThrough);

    /**
     * Get instance of {@link PoiFont}.
     * @param poiFont the POI font instance
     * @return instance of {@link PoiFont} for the given font
     */
    public PoiFont getFont(org.apache.poi.ss.usermodel.Font poiFont) {
        return poiFont == null ? getDefaultCellStyle().getFont() : new PoiFont(this, poiFont);
    }

    PoiFont getPoiFont(com.dua3.meja.model.Font font, Style style) {
        Map<String, String> properties = style.properties();

        if (properties.isEmpty() && font instanceof PoiFont && ((PoiFont)font).workbook==this) {
            return (PoiFont) font;
        }

        String name = properties.getOrDefault(Style.FONT_FAMILY, font.getFamily());

        String sSize = properties.get(Style.FONT_SIZE);
        short height = (short) Math.round(sSize == null ? font.getSizeInPoints() : MejaHelper.decodeFontSize(sSize));

        final String sStyle = properties.get(Style.FONT_STYLE);
        boolean italic = sStyle == null ? font.isItalic() : "italic".equals(sStyle);

        final String sWeight = properties.get(Style.FONT_WEIGHT);
        boolean bold = sWeight == null ? font.isBold() : "bold".equals(sWeight);

        String sDecoration = properties.get(Style.TEXT_DECORATION);
        boolean underline = sDecoration==null ? font.isUnderlined() : "underline".equals(sDecoration);
        boolean strikethrough = sDecoration==null ? font.isStrikeThrough() : "line-through".equals(sDecoration);

        String sColor = properties.get(Style.COLOR);
        Color color = sColor == null ? font.getColor() : Color.valueOf(sColor);

        // try to find existing font
        for (short i=0;i<poiWorkbook.getNumberOfFonts(); i++) {
            Font poiFont = poiWorkbook.getFontAt(i);

            if (poiFont.getFontName().equalsIgnoreCase(name)
                    && poiFont.getFontHeightInPoints()==height
                    && poiFont.getBold() == bold
                    && poiFont.getItalic() == italic
                    && (poiFont.getUnderline()!=Font.U_NONE) == underline
                    && poiFont.getStrikeout() == strikethrough
                    && getColor(poiFont, Color.BLACK).equals(color)
                    && poiFont.getTypeOffset()==Font.SS_NONE) {
                return new PoiFont(this,  poiFont);
            }
        }

        // if not found, create it
        return createFont(name, height, font.getColor(), bold, italic, underline, strikethrough);
    }

    /**
     * Concrete implementation of {@link PoiWorkbook} for HSSF-workbooks
     * (the old Excel format).
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
        public boolean isFormulaEvaluationSupported() {
            return true;
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

        @Override
        public PoiHssfCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle poiStyle) {
            return new PoiHssfCellStyle(this, (HSSFCellStyle) poiStyle);
        }

        public PoiFont getFont(short idx) {
            return getFont(poiWorkbook.getFontAt(idx));
        }

        @Override
        public HSSFColor getPoiColor(Color color) {
            if (color == null) {
                return null;
            }
            HSSFPalette palette = ((HSSFWorkbook) poiWorkbook).getCustomPalette();
            return palette.findSimilarColor(color.r(), color.g(), color.b());
        }

        @Override
        public Color getColor(Font poiFont, Color dfltColor) {
            return getColor(((HSSFFont) poiFont).getHSSFColor(((HSSFWorkbook) getPoiWorkbook())), Color.BLACK);
        }

        @Override
        public HSSFRichTextString createRichTextString(String s) {
            return new HSSFRichTextString(s);
        }

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

        @Override
        protected FileType getStandardFileType() {
          return FileType.XLS;
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
            assert poiWorkbook instanceof XSSFWorkbook || poiWorkbook instanceof SXSSFWorkbook;
            this.defaultCellStyle = new PoiXssfCellStyle(this, (XSSFCellStyle) poiWorkbook.getCellStyleAt((short) 0));
            init();
        }

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
            org.apache.poi.xssf.usermodel.XSSFColor xssfColor = (org.apache.poi.xssf.usermodel.XSSFColor) poiColor;
            if (poiColor == null || xssfColor.isAuto()) {
                return defaultColor;
            }

            // try to get RGB values
            byte[] rgb = xssfColor.hasAlpha() ? xssfColor.getARGB()
                : xssfColor.hasTint() ? xssfColor.getRGBWithTint()
                : xssfColor.getRGB();

            if (rgb != null) {
              if (rgb.length==4) {
                int a = rgb[0] & 0xFF;
                int r = rgb[1] & 0xFF;
                int g = rgb[2] & 0xFF;
                int b = rgb[3] & 0xFF;
                return new Color(r, g, b, a);
              } else {
                int r = rgb[0] & 0xFF;
                int g = rgb[1] & 0xFF;
                int b = rgb[2] & 0xFF;
                return new Color(r, g, b);
              }
            }

            // what should be done now?
            return defaultColor;
        }

        @Override
        public XSSFColor getPoiColor(Color color) {
            return new XSSFColor(color.toByteArray());
        }

        @Override
        public boolean isFormulaEvaluationSupported() {
            return !(poiWorkbook instanceof SXSSFWorkbook);
        }

        @Override
        public Color getColor(Font poiFont, Color dfltColor) {
            return getColor(((XSSFFont) poiFont).getXSSFColor(), Color.BLACK);
        }

        @Override
        public XSSFRichTextString createRichTextString(String s) {
            return new XSSFRichTextString(s);
        }

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

        @Override
        protected FileType getStandardFileType() {
          return FileType.XLSX;
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

}
