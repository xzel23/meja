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
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.text.FontUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.formula.eval.NotImplementedException;
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
import org.jspecify.annotations.Nullable;

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
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoubleConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of the {@link com.dua3.meja.model.Workbook} interface based on the Apache POI implementation.
 */
public abstract class PoiWorkbook extends AbstractWorkbook<PoiSheet, PoiRow, PoiCell> {

    private static final Logger LOGGER = LogManager.getLogger(PoiWorkbook.class);

    private static final boolean IS_HEADLESS = Boolean.getBoolean("java.awt.headless");

    /**
     * The underlying Apache POI {@link Workbook} instance.
     */
    protected final Workbook poiWorkbook;
    /**
     * The Apache POI {@link FormulaEvaluator}.
     */
    protected final FormulaEvaluator evaluator;
    /**
     * The list of sheets contained in the workbook.
     */
    protected final List<PoiSheet> sheets = new ArrayList<>();
    /**
     * Mapping from cellstyle names to internat Apache POI cell style numbers.
     */
    protected final Map<String, Short> cellStyles = new HashMap<>();
    /**
     * Factor to convert between Excel widths (measured in characters) and points.
     */
    private final FactorWidth factorWidth = new FactorWidth();

    /**
     * Construct a new instance.
     *
     * @param poiWorkbook the POI workbook instance
     * @param uri         the URI of this workbook
     */
    protected PoiWorkbook(Workbook poiWorkbook, @Nullable URI uri) {
        super(uri);
        this.poiWorkbook = poiWorkbook;
        this.evaluator = poiWorkbook.getCreationHelper().createFormulaEvaluator();

        // init cell TextAttributes map
        for (short i = 0; i < poiWorkbook.getNumCellStyles(); i++) {
            cellStyles.put("style#" + i, i);
        }
    }

    @Override
    public PoiCellStyle copyCellStyle(String styleName, CellStyle style) {
        PoiCellStyle cellStyle = getCellStyle(styleName);
        cellStyle.poiCellStyle.cloneStyleFrom(((PoiCellStyle) style).poiCellStyle);
        return cellStyle;
    }

    @Override
    public Sheet createSheet(String sheetName) {
        org.apache.poi.ss.usermodel.Sheet poiSheet = poiWorkbook.createSheet(sheetName);
        PoiSheet sheet = new PoiSheet(this, poiSheet);
        sheets.add(sheet);
        sheetAdded(sheets.size() - 1);
        return sheet;
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
    public List<String> getCellStyleNames() {
        return new ArrayList<>(cellStyles.keySet());
    }

    @Override
    public Stream<CellStyle> cellStyles() {
        Iterator<? extends PoiCellStyle> iter = DataUtil.map(cellStyles.keySet().iterator(), this::getCellStyle);
        Spliterator<? extends PoiCellStyle> spliterator = Spliterators.spliterator(iter, cellStyles.size(), 0);
        return StreamSupport.stream(spliterator, false).map(PoiCellStyle.class::cast);
    }

    @Override
    public void setCurrentSheet(int sheetIndex) {
        Objects.checkIndex(sheetIndex, sheets.size());

        int oldIdx = getCurrentSheetIndex();
        if (sheetIndex != oldIdx) {
            poiWorkbook.setActiveSheet(sheetIndex);
            activeSheetChanged(oldIdx, sheetIndex);
        }
    }

    @Override
    public int getCurrentSheetIndex() {
        return poiWorkbook.getActiveSheetIndex();
    }

    @Override
    public abstract PoiCellStyle getDefaultCellStyle();

    @Override
    public PoiSheet getSheet(int sheetIndex) {
        return sheets.get(sheetIndex);
    }

    @Override
    public int getSheetCount() {
        assert poiWorkbook.getNumberOfSheets() == sheets.size();
        return sheets.size();
    }

    @Override
    public boolean hasCellStyle(String name) {
        return cellStyles.containsKey(name);
    }

    @Override
    public void removeSheet(int sheetIndex) {
        sheets.remove(sheetIndex);
        poiWorkbook.removeSheetAt(sheetIndex);
        sheetRemoved(sheetIndex);
    }

    @Override
    public void write(FileType<?> fileType, OutputStream out, Arguments options, DoubleConsumer updateProgress) throws IOException {
        //noinspection ObjectEquality
        if (fileType == getStandardFileType()) {
            // if the workbook is to be saved in the same format, write it out
            // directly so that
            // features not yet supported by Meja don't get lost in the process
            updateProgress.accept(WorkbookWriter.PROGRESS_INDETERMINATE);
            poiWorkbook.write(out);
            updateProgress.accept(1.0);
        } else if (fileType instanceof FileTypeWorkbook<?> fileTypeWorkbook) {
            WorkbookWriter writer = fileTypeWorkbook.getWorkbookWriter();
            writer.setOptions(options);
            writer.write(this, out, updateProgress);
        } else {
            throw new IllegalStateException("could not write workbook");
        }
    }

    /**
     * Return the standard file type for this implementation.
     *
     * @return the file type matching the underlying POI implementation
     */
    protected abstract FileType<PoiWorkbook> getStandardFileType();

    /**
     * Get {@link PoiCellStyle} from {@link org.apache.poi.ss.usermodel.CellStyle}.
     *
     * @param cellStyle POI cell style
     * @return instance of {@link PoiCellStyle}
     */
    public abstract PoiCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle cellStyle);

    /**
     * Convert {@link String} to {@link RichTextString}.
     *
     * @param s the {@link String} to convert
     * @return {@link RichTextString} with the same text as {@code s}
     */
    public abstract RichTextString createRichTextString(String s);

    String getCellStyleName(PoiCellStyle cellStyle) {
        final short styleIndex = cellStyle.poiCellStyle.getIndex();
        for (Entry<String, Short> entry : cellStyles.entrySet()) {
            if (entry.getValue() == styleIndex) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("CellStyle is not from this workbook.");
    }

    /**
     * Get the color from the given POI color. If the POI color is null, return the default color.
     *
     * @param poiColor     the POI color
     * @param defaultColor the default color
     * @return the color
     */
    public abstract Color getColor(org.apache.poi.ss.usermodel.Color poiColor, Color defaultColor);

    @Override
    public @Nullable PoiSheet getCurrentAbstractSheetOrNull() {
        int currentSheetIdx = getCurrentSheetIndex();
        if (currentSheetIdx < sheets.size()) {
            return getSheet(currentSheetIdx);
        } else {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        poiWorkbook.close();
    }

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

    /**
     * Get font color.
     *
     * @param poiFont   instance of POI font
     * @param dfltColor color return if none is set
     * @return the color for the given font
     */
    public abstract Color getColor(Font poiFont, Color dfltColor);

    /**
     * Create a new font.
     *
     * @param font the font to create a POI version of
     * @return an instance of {@link PoiFont}
     */
    protected abstract PoiFont createFont(com.dua3.utility.text.Font font);

    Workbook getPoiWorkbook() {
        return poiWorkbook;
    }

    @Override
    public int hashCode() {
        return poiWorkbook.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof PoiWorkbook other && poiWorkbook.equals(other.poiWorkbook);
    }

    /**
     * Initialise the workbook by creating {@link PoiSheet} instances for the sheets contained in the workbook.
     */
    protected final void init() {
        for (int i = 0; i < poiWorkbook.getNumberOfSheets(); i++) {
            createSheet(poiWorkbook.getSheetAt(i));
        }
    }

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

    /**
     * Test if formula evaluation is supported.
     *
     * @return true if formula evaluation is supported
     */
    public abstract boolean isFormulaEvaluationSupported();

    /**
     * Returns the flag indicating whether force formula recalculation is enabled.
     *
     * @return true if force formula recalculation is enabled, false otherwise.
     */
    public boolean getForceFormulaRecalculation() {
        return poiWorkbook.getForceFormulaRecalculation();
    }

    /**
     * Sets the flag indicating whether force formula recalculation is enabled.
     *
     * @param flag the flag to set. Pass true to enable force formula recalculation, false to disable it.
     */
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

    /**
     * Creates a hyperlink based on the target URI.
     *
     * @param target the target URI for the hyperlink.
     * @return a Hyperlink object representing the created hyperlink.
     * @throws IllegalArgumentException if the target URI's protocol is not supported.
     */
    public Hyperlink createHyperLink(URI target) {
        HyperlinkType type;
        String address = target.toString();
        type = switch (Optional.ofNullable(target.getScheme()).map(s -> s.toLowerCase(Locale.ROOT)).orElse("")) {
            case "http", "https" -> HyperlinkType.URL;
            case "file" -> HyperlinkType.FILE;
            case "mailto" -> HyperlinkType.EMAIL;
            case "" -> HyperlinkType.FILE; // workbook-relative link
            default -> throw new IllegalArgumentException("unsupported protocol: " + target.getScheme());
        };
        Hyperlink link = poiWorkbook.getCreationHelper().createHyperlink(type);
        link.setAddress(address);
        return link;
    }

    /**
     * Calculates and retrieves the width factor for the default cell style's font.
     * This method updates the factor based on the font characteristics, ensuring proper rendering.
     *
     * @return the calculated width factor as a {@code float}.
     */
    public float getFactorWidth() {
        return factorWidth.updateAndGet(getDefaultCellStyle().getFont());
    }

    /**
     * The {@code FactorWidth} class is a utility class responsible for calculating
     * and maintaining the width factor for a workbook. The width factor is used
     * to determine the cell dimensions. The calculation uses Excel's weird units,
     * specifically 1/256ths of the default workbbok font's width of the '0' character.
     *
     * <p>This class is designed to handle both graphical and headless environments,
     * ensuring consistent behavior for text dimension calculations. In headless mode,
     * default width values are used for the two known default fonts Excel used in
     * different versions.
     */
    private static class FactorWidth {
        /**
         * Width of "0" in the Arial 10 pt font.
         */
        private static final float WIDTH_OF_ZERO_ARIAL_10 = 5.5615234375f;
        /**
         * Width of "0" in the Calibri 10 pt font.
         */
        private static final float WIDTH_OF_ZERO_CALIBRI = 5.068359375f;
        /**
         * A generic value used for all other fonts.
         */
        private static final float WIDTH_OF_ZERO_GENERIC = 6.0f;

        private com.dua3.utility.text.@Nullable Font font;
        private float factor;

        FactorWidth() {
            this.font = null;
            this.factor = 0.0f;
        }

        float updateAndGet(com.dua3.utility.text.Font font) {
            if (font == this.font) {
                return factor;
            }

            float charZeroWidth;
            if (IS_HEADLESS) {
                // use a constant value in headless mode; this also ensures stable unit tests
                if (font.getFamily().startsWith("Arial")) {
                    charZeroWidth = WIDTH_OF_ZERO_ARIAL_10 * font.getSizeInPoints() / 10.0f;
                } else if (font.getFamily().startsWith("Calibri")) {
                    charZeroWidth = WIDTH_OF_ZERO_CALIBRI * font.getSizeInPoints() / 10.0f;
                } else {
                    charZeroWidth = WIDTH_OF_ZERO_GENERIC;
                }
            } else {
                charZeroWidth = FontUtil.getInstance().getTextDimension("0", font).width();
            }
            this.font = font;
            this.factor = charZeroWidth / 256.0f;

            return factor;
        }
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
        public PoiCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        @Override
        protected FileType<PoiWorkbook> getStandardFileType() {
            return FileTypeXls.instance();
        }

        @Override
        public PoiCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle cellStyle) {
            return new PoiHssfCellStyle(this, (HSSFCellStyle) cellStyle);
        }

        @Override
        public HSSFRichTextString createRichTextString(String s) {
            return new HSSFRichTextString(s);
        }

        @Override
        public Color getColor(org.apache.poi.ss.usermodel.@Nullable Color poiColor, Color defaultColor) {
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
            return Color.rgba(r, g, b, a);
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
        public Color getColor(Font poiFont, Color dfltColor) {
            return getColor(((HSSFFont) poiFont).getHSSFColor(((HSSFWorkbook) getPoiWorkbook())), Color.BLACK);
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
        public boolean isFormulaEvaluationSupported() {
            return true;
        }

        Color getColor(short idx) {
            return getColor(((HSSFWorkbook) poiWorkbook).getCustomPalette().getColor(idx), Color.BLACK);
        }

        /**
         * Retrieves the font at the given index.
         *
         * @param idx the index of the font to retrieve
         * @return the PoiFont instance representing the font at the given index
         */
        public PoiFont getFont(int idx) {
            return getFont(poiWorkbook.getFontAt(idx));
        }
    }

    /**
     * Implementation of {@code PoiWorkbook} for workbooks using the XSSF format.
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
            LangUtil.check(
                    poiWorkbook instanceof XSSFWorkbook || poiWorkbook instanceof SXSSFWorkbook,
                    () -> {
                        throw new IllegalArgumentException("poiWorkbook must be of type XSSFWorkbook or SXSSFWorkbook but is: " + poiWorkbook.getClass().getName());
                    }
            );
            this.defaultCellStyle = new PoiXssfCellStyle(this, (XSSFCellStyle) poiWorkbook.getCellStyleAt(0));
            init();
        }

        @Override
        public PoiCellStyle getDefaultCellStyle() {
            return defaultCellStyle;
        }

        @Override
        protected FileType<PoiWorkbook> getStandardFileType() {
            return FileTypeXlsx.instance();
        }

        @Override
        public PoiCellStyle getPoiCellStyle(org.apache.poi.ss.usermodel.CellStyle cellStyle) {
            return new PoiXssfCellStyle(this, (XSSFCellStyle) cellStyle);
        }

        @Override
        public XSSFRichTextString createRichTextString(String s) {
            return new XSSFRichTextString(s);
        }

        @Override
        public Color getColor(org.apache.poi.ss.usermodel.@Nullable Color poiColor, Color defaultColor) {
            XSSFColor xssfColor = (XSSFColor) poiColor;
            if (poiColor == null || xssfColor.isAuto()) {
                return defaultColor;
            }

            // try to get RGB values
            byte[] rgb = getRgbBytes(xssfColor);

            if (rgb == null) {
                return defaultColor;
            }

            if (rgb.length == 4) {
                int a = rgb[0] & 0xFF;
                int r = rgb[1] & 0xFF;
                int g = rgb[2] & 0xFF;
                int b = rgb[3] & 0xFF;
                return Color.rgba(r, g, b, a);
            } else {
                int r = rgb[0] & 0xFF;
                int g = rgb[1] & 0xFF;
                int b = rgb[2] & 0xFF;
                return Color.rgb(r, g, b);
            }
        }

        @Override
        public XSSFColor getPoiColor(Color color) {
            return new XSSFColor(color.toByteArrayRGB());
        }

        @Override
        public Color getColor(Font poiFont, Color dfltColor) {
            return getColor(((XSSFFont) poiFont).getXSSFColor(), Color.BLACK);
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
        public boolean isFormulaEvaluationSupported() {
            return !(poiWorkbook instanceof SXSSFWorkbook);
        }

        /**
         * Retrieves the RGB bytes of the given {@link XSSFColor} instance.
         * Depending on the properties of the color (e.g., alpha channel, tint),
         * the method returns the appropriate representation of the color.
         *
         * @param xssfColor the {@link XSSFColor} instance from which to extract the RGB bytes.
         *                  Must not be null.
         * @return a byte array representing the RGB (or ARGB) bytes of the provided color,
         * or {@code null} if POI does not return a byte array
         */
        private static byte @Nullable [] getRgbBytes(XSSFColor xssfColor) {
            if (xssfColor.hasAlpha()) {
                return xssfColor.getARGB();
            } else if (xssfColor.hasTint()) {
                return xssfColor.getRGBWithTint();
            } else {
                return xssfColor.getRGB();
            }
        }
    }

}
