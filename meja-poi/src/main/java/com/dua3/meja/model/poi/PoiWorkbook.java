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

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.io.FileTypeWorkbook;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.AbstractWorkbook;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.poi.PoiCellStyle.PoiHssfCellStyle;
import com.dua3.meja.model.poi.PoiCellStyle.PoiXssfCellStyle;
import com.dua3.meja.model.poi.io.FileTypeXls;
import com.dua3.meja.model.poi.io.FileTypeXlsx;
import com.dua3.utility.data.Color;
import com.dua3.utility.data.DataUtil;
import com.dua3.utility.io.FileType;
import com.dua3.utility.options.Arguments;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoubleConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author axel
 */
public abstract class PoiWorkbook extends AbstractWorkbook {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoiWorkbook.class);
    /**
     *
     */
    protected final Workbook poiWorkbook;
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
     * Construct a new instance.
     *
     * @param poiWorkbook the POI workbook instance
     * @param uri         the URI of this workbook
     */
    protected PoiWorkbook(@Nullable Workbook poiWorkbook, @Nullable URI uri) {
        super(uri);
        this.poiWorkbook = poiWorkbook;
        this.evaluator = poiWorkbook.getCreationHelper().createFormulaEvaluator();

        // init cell TextAttributes map
        for (short i = 0; i < poiWorkbook.getNumCellStyles(); i++) {
            cellStyles.put("style#" + i, i);
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
    public PoiCellStyle copyCellStyle(String styleName, CellStyle style) {
        PoiCellStyle cellStyle = getCellStyle(styleName);
        cellStyle.poiCellStyle.cloneStyleFrom(((PoiCellStyle) style).poiCellStyle);
        return cellStyle;
    }

    /**
     * Create a new font.
     *
     * @param font the font to create a POI version of
     * @return an instance of {@link PoiFont}
     */
    protected abstract PoiFont createFont(com.dua3.utility.text.Font font);

    /**
     * Convert {@link String} to {@link RichTextString}.
     *
     * @param s the {@link String} to convert
     * @return {@link RichTextString} with the same text as {@code s}
     */
    public abstract RichTextString createRichTextString(String s);

    /**
     * Create instance of {@link PoiSheet} for the given POI sheet.
     *
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
        firePropertyChange(PROPERTY_SHEET_ADDED, null, sheets.size() - 1);
        return sheet;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof PoiWorkbook && Objects.equals(poiWorkbook, ((PoiWorkbook) obj).poiWorkbook);
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

    String getCellStyleName(PoiCellStyle cellStyle) {
        final short styleIndex = cellStyle.poiCellStyle.getIndex();
        for (Entry<String, Short> entry : cellStyles.entrySet()) {
            if (entry.getValue() == styleIndex) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("CellStyle is not from this workbook.");
    }

    @Override
    public List<String> getCellStyleNames() {
        return new ArrayList<>(cellStyles.keySet());
    }

    public abstract Color getColor(org.apache.poi.ss.usermodel.Color poiColor, Color defaultColor);

    /**
     * Get font color.
     *
     * @param poiFont   instance of POI font
     * @param dfltColor color return if none is set
     * @return the color for the given font
     */
    public abstract Color getColor(Font poiFont, Color dfltColor);

    @Override
    public Sheet getCurrentSheet() {
        return getSheet(getCurrentSheetIndex());
    }

    @Override
    public void setCurrentSheet(int idx) {
        Objects.checkIndex(idx, sheets.size());

        int oldIdx = getCurrentSheetIndex();
        if (idx != oldIdx) {
            poiWorkbook.setActiveSheet(idx);
            firePropertyChange(PROPERTY_ACTIVE_SHEET, oldIdx, idx);
        }
    }

    @Override
    public int getCurrentSheetIndex() {
        return poiWorkbook.getActiveSheetIndex();
    }

    static DataFormatter getDataFormatter(Locale locale) {
        return new DataFormatter(locale);
    }

    @Override
    public abstract PoiCellStyle getDefaultCellStyle();

    /**
     * Get instance of {@link PoiFont}.
     *
     * @param poiFont the POI font instance
     * @return instance of {@link PoiFont} for the given font
     */
    public PoiFont getFont(@Nullable Font poiFont) {
        return poiFont == null ? getDefaultCellStyle().getPoiFont() : new PoiFont(this, poiFont);
    }

    /**
     * Get {@link PoiCellStyle} from {@link org.apache.poi.ss.usermodel.CellStyle}.
     *
     * @param cellStyle POI cell style
     * @return instance of {@link PoiCellStyle}
     */
    public abstract PoiCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle cellStyle);

    /**
     * Get POI color.
     *
     * @param color the color
     * @return POI color
     */
    public abstract org.apache.poi.ss.usermodel.Color getPoiColor(Color color);

    PoiFont getPoiFont(com.dua3.utility.text.Font font) {
        // try to find existing font
        for (int i = 0; i < poiWorkbook.getNumberOfFonts(); i++) {
            Font poiFont = poiWorkbook.getFontAt(i);

            if (poiFont.getFontName().equalsIgnoreCase(font.getFamily())
                    && poiFont.getFontHeightInPoints() == font.getSizeInPoints()
                    && poiFont.getBold() == font.isBold()
                    && poiFont.getItalic() == font.isItalic()
                    && (poiFont.getUnderline() != Font.U_NONE) == font.isUnderline()
                    && poiFont.getStrikeout() == font.isStrikeThrough()
                    && getColor(poiFont, Color.BLACK).equals(font.getColor())
                    && poiFont.getTypeOffset() == Font.SS_NONE) {
                return new PoiFont(this, poiFont);
            }
        }

        // if not found, create it
        return createFont(font);
    }

    Workbook getPoiWorkbook() {
        return poiWorkbook;
    }

    @Override
    public PoiSheet getSheet(int sheetNr) {
        return sheets.get(sheetNr);
    }

    @Override
    public int getSheetCount() {
        assert poiWorkbook.getNumberOfSheets() == sheets.size();
        return sheets.size();
    }

    /**
     * Return the standard file type for this implementation.
     *
     * @return the file type matching the underlying POI implementation
     */
    protected abstract FileType<?> getStandardFileType();

    @Override
    public boolean hasCellStyle(String name) {
        return cellStyles.containsKey(name);
    }

    @Override
    public int hashCode() {
        return poiWorkbook.hashCode();
    }

    /**
     *
     */
    protected final void init() {
        for (int i = 0; i < poiWorkbook.getNumberOfSheets(); i++) {
            createSheet(poiWorkbook.getSheetAt(i));
        }
    }

    /**
     * Test if formula evaluation is supported.
     *
     * @return true if formula evaluation is supported
     */
    public abstract boolean isFormulaEvaluationSupported();

    /**
     * Try to evaluate all formula cells.
     */
    public void evaluateAllFormulaCells() {
        if (isFormulaEvaluationSupported()) {
            try {
                evaluator.evaluateAll();
            } catch (NotImplementedException e) {
                LOGGER.warn("unsupported function in formula; flagging workbook as needing recalculation", e);
                setForceFormulaRecalculation(true);
            }
        }
    }

    public boolean getForceFormulaRecalculation() {
        return poiWorkbook.getForceFormulaRecalculation();
    }

    public void setForceFormulaRecalculation(boolean flag) {
        poiWorkbook.setForceFormulaRecalculation(flag);
        LOGGER.debug("setForceFormulaRecalculation({})", flag);
    }

    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<>() {
            private final Iterator<PoiSheet> iter = sheets.iterator();

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
    public void removeSheet(int sheetNr) {
        sheets.remove(sheetNr);
        poiWorkbook.removeSheetAt(sheetNr);
        firePropertyChange(PROPERTY_SHEET_REMOVED, sheetNr, null);
    }

    @Override
    public void write(FileType<?> type, OutputStream out, Arguments options, DoubleConsumer updateProgress) throws IOException {
        //noinspection ObjectEquality
        if (type == getStandardFileType()) {
            // if the workbook is to be saved in the same format, write it out
            // directly so that
            // features not yet supported by Meja don't get lost in the process
            updateProgress.accept(WorkbookWriter.PROGRESS_INDETERMINATE);
            poiWorkbook.write(out);
            updateProgress.accept(1.0);
        } else if (type instanceof FileTypeWorkbook) {
            WorkbookWriter writer = ((FileTypeWorkbook<?>) type).getWorkbookWriter();
            writer.setOptions(options);
            writer.write(this, out, updateProgress);
        } else {
            throw new IllegalStateException("could not write workbook");
        }
    }

    public Hyperlink createHyperLink(URI target) {
        HyperlinkType type;
        String address = target.toString();
        type = switch (target.getScheme().toLowerCase(Locale.ROOT)) {
            case "http", "https" -> HyperlinkType.URL;
            case "file" -> HyperlinkType.FILE;
            case "mailto" -> HyperlinkType.EMAIL;
            default -> throw new IllegalArgumentException("unsupported protocol: " + target.getScheme());
        };
        Hyperlink link = poiWorkbook.getCreationHelper().createHyperlink(type);
        link.setAddress(address);
        return link;
    }

    @Override
    public Stream<? extends PoiCellStyle> cellStyles() {
        Iterator<? extends PoiCellStyle> iter = DataUtil.map(cellStyles.keySet().iterator(), this::getCellStyle);
        Spliterator<? extends PoiCellStyle> spliterator = Spliterators.spliterator(iter, cellStyles.size(), 0);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Concrete implementation of {@link PoiWorkbook} for HSSF-workbooks (the old
     * Excel format).
     */
    public static class PoiHssfWorkbook extends PoiWorkbook {

        private final PoiHssfCellStyle defaultCellStyle;

        /**
         * Construct instance from existing POI workbook.
         *
         * @param poiWorkbook the POI workbook instance
         * @param uri         the URI of the workbook
         */
        public PoiHssfWorkbook(HSSFWorkbook poiWorkbook, @Nullable URI uri) {
            super(poiWorkbook, uri);
            this.defaultCellStyle = new PoiHssfCellStyle(this, poiWorkbook.getCellStyleAt(0));
            cellStyles.put("", (short) 0);
            init();
        }

        @Override
        public PoiFont createFont(com.dua3.utility.text.Font font) {
            Font poiFont = poiWorkbook.createFont();
            poiFont.setFontName(font.getFamily());
            poiFont.setFontHeight(((short) Math.round(20 * font.getSizeInPoints())));
            poiFont.setColor(getPoiColor(font.getColor()).getIndex());
            poiFont.setBold(font.isBold());
            poiFont.setItalic(font.isItalic());
            poiFont.setUnderline(font.isUnderline() ? Font.U_SINGLE
                    : Font.U_NONE);
            poiFont.setStrikeout(font.isStrikeThrough());
            return new PoiFont(this, poiFont);
        }

        @Override
        public HSSFRichTextString createRichTextString(String s) {
            return new HSSFRichTextString(s);
        }

        @Override
        public Color getColor(Font poiFont, Color dfltColor) {
            return getColor(((HSSFFont) poiFont).getHSSFColor(((HSSFWorkbook) getPoiWorkbook())), Color.BLACK);
        }

        @Override
        public Color getColor(@Nullable org.apache.poi.ss.usermodel.Color poiColor, Color defaultColor) {
            if (poiColor == null || poiColor.equals(HSSFColorPredefined.AUTOMATIC.getColor())) {
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
            return Color.rgb(r, g, b, a);
        }

        Color getColor(short idx) {
            return getColor(((HSSFWorkbook) poiWorkbook).getCustomPalette().getColor(idx), Color.BLACK);
        }

        @Override
        public PoiCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        public PoiFont getFont(int idx) {
            return getFont(poiWorkbook.getFontAt(idx));
        }

        @Override
        public PoiCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle poiStyle) {
            return new PoiHssfCellStyle(this, (HSSFCellStyle) poiStyle);
        }

        @Override
        public HSSFColor getPoiColor(Color color) {
            HSSFPalette palette = ((HSSFWorkbook) poiWorkbook).getCustomPalette();
            int argb = color.argb();
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = argb & 0xff;
            return palette.findSimilarColor(r, g, b);
        }

        @Override
        protected FileType<PoiWorkbook> getStandardFileType() {
            return FileTypeXls.instance();
        }

        @Override
        public boolean isFormulaEvaluationSupported() {
            return true;
        }
    }

    /**
     *
     */
    public static class PoiXssfWorkbook extends PoiWorkbook {

        private final PoiXssfCellStyle defaultCellStyle;

        /**
         * Construct instance from existing POI workbook.
         *
         * @param poiWorkbook the POI workbook instance
         * @param uri         the URI of the workbook
         */
        public PoiXssfWorkbook(Workbook poiWorkbook, @Nullable URI uri) {
            super(poiWorkbook, uri);
            assert poiWorkbook instanceof XSSFWorkbook || poiWorkbook instanceof SXSSFWorkbook;
            this.defaultCellStyle = new PoiXssfCellStyle(this, (XSSFCellStyle) poiWorkbook.getCellStyleAt(0));
            init();
        }

        @Override
        public PoiFont createFont(com.dua3.utility.text.Font font) {
            XSSFFont poiFont = (XSSFFont) poiWorkbook.createFont();
            poiFont.setFontName(font.getFamily());
            poiFont.setFontHeight(((short) Math.round(20 * font.getSizeInPoints())));
            poiFont.setColor(getPoiColor(font.getColor()));
            poiFont.setBold(font.isBold());
            poiFont.setItalic(font.isItalic());
            poiFont.setUnderline(font.isUnderline() ? Font.U_SINGLE
                    : Font.U_NONE);
            poiFont.setStrikeout(font.isStrikeThrough());
            return new PoiFont(this, poiFont);
        }

        @Override
        public XSSFRichTextString createRichTextString(String s) {
            return new XSSFRichTextString(s);
        }

        @Override
        public Color getColor(Font poiFont, Color dfltColor) {
            return getColor(((XSSFFont) poiFont).getXSSFColor(), Color.BLACK);
        }

        @Override
        public Color getColor(@Nullable org.apache.poi.ss.usermodel.Color poiColor, Color defaultColor) {
            XSSFColor xssfColor = (XSSFColor) poiColor;
            if (poiColor == null || xssfColor.isAuto()) {
                return defaultColor;
            }

            // try to get RGB values
            byte[] rgb = xssfColor.hasAlpha() ? xssfColor.getARGB()
                    : xssfColor.hasTint() ? xssfColor.getRGBWithTint() : xssfColor.getRGB();

            if (rgb != null) {
                if (rgb.length == 4) {
                    int a = rgb[0] & 0xFF;
                    int r = rgb[1] & 0xFF;
                    int g = rgb[2] & 0xFF;
                    int b = rgb[3] & 0xFF;
                    return Color.rgb(r, g, b, a);
                } else {
                    int r = rgb[0] & 0xFF;
                    int g = rgb[1] & 0xFF;
                    int b = rgb[2] & 0xFF;
                    return Color.rgb(r, g, b);
                }
            }

            // what should be done now?
            return defaultColor;
        }

        @Override
        public PoiCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        @Override
        public PoiCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle poiStyle) {
            return new PoiXssfCellStyle(this, (XSSFCellStyle) poiStyle);
        }

        @Override
        public XSSFColor getPoiColor(Color color) {
            return new XSSFColor(color.toByteArrayRGB());
        }

        @Override
        protected FileType<PoiWorkbook> getStandardFileType() {
            return FileTypeXlsx.instance();
        }

        @Override
        public boolean isFormulaEvaluationSupported() {
            return !(poiWorkbook instanceof SXSSFWorkbook);
        }
    }

}
