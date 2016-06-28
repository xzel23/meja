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
package com.dua3.meja.util;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.SearchOptions;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.text.RichText;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class MejaHelper {

    private static final Logger LOGGER = Logger.getLogger(MejaHelper.class.getName());

    /**
     * Translate column number to column name.
     *
     * @param j the column number
     * @return the column name
     */
    public static String getColumnName(int j) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) ('A' + j % 26));
        j /= 26;
        while (j > 0) {
            sb.insert(0, (char) ('A' + j % 26 - 1));
            j /= 26;
        }
        return new String(sb);
    }

    /**
     * Create row iterator.
     *
     * @param sheet the sheet for which to create a row iterator
     * @return row iterator for {@code sheet}
     */
    public static Iterator<Row> createRowIterator(final Sheet sheet) {
        return new Iterator<Row>() {

            private int rowNum = sheet.getFirstRowNum();

            @Override
            public boolean hasNext() {
                return rowNum <= sheet.getLastRowNum();
            }

            @Override
            public Row next() {
                return sheet.getRow(rowNum++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing of rows is not supported.");
            }
        };
    }

    /**
     * Create cell iterator.
     *
     * @param row the row for which to create a cell iterator
     * @return cell iterator for {@code row}
     */
    public static Iterator<Cell> createCellIterator(final Row row) {
        return new Iterator<Cell>() {

            private int colNum = row.getFirstCellNum();

            @Override
            public boolean hasNext() {
                return colNum <= row.getLastCellNum();
            }

            @Override
            public Cell next() {
                return row.getCell(colNum++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing of rows is not supported.");
            }
        };
    }

    /**
     * Translate column name to column number.
     *
     * @param colName the name of the column, ie. "A", "B",... , "AA", "AB",...
     * @return the column number
     * @throws IllegalArgumentException if {@code colName} is not a valid column
     * name
     */
    public static int getColumnNumber(String colName) {
        int col = 0;
        for (char c : colName.toLowerCase().toCharArray()) {
            if (c < 'a' || 'z' < c) {
                throw new IllegalArgumentException("'" + colName + "' ist no valid column name.");
            }

            col = col * ('z' - 'a' + 1) + (c - 'a' + 1);
        }
        return col - 1;
    }

    /**
     * Open workbook file.
     * <p>
     * This method inspects the file name extension to determine which factory
     * should be used for loading. If there are multiple factories registered
     * for the extension, the matching factories are tried in sequential order.
     * If loading succeeds, the workbook is returned.
     *
     * @param file the workbook file
     * @return the workbook loaded from file
     * @throws IOException if workbook could not be loaded
     */
    public static Workbook openWorkbook(File file) throws IOException {
        FileType fileType = FileType.forFile(file);

        if (fileType == null) {
            throw new IllegalArgumentException("Could not determine type of file '" + file.getPath() + ".");
        }

        if (!fileType.isSupported(OpenMode.READ)) {
            throw new IllegalArgumentException("Reading is not supported for files of type '" + fileType.getDescription() + ".");
        }

        try {
            return fileType.getFactory().open(file);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            throw new IOException("Could not load '" + file.getPath() + "'.", ex);
        }
    }

    /**
     * Extract file extension.
     * <p>
     * The file extension is the part of the filename from the last dot to the
     * end (including the dot). The extension of the file
     * {@literal /foo/bar/file.txt} is ".txt".
     * </p>
     * <p>
     * If the filename doesn't contain a dot, the extension is the empty string
     * ("").
     * </p>
     *
     * @param file the file
     * @return the file extension including the dot.
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }

    /**
     * Get cell reference as string.
     *
     * @param cell
     * @param includeSheet true, if the sheet name should be part of the cell
     * reference
     * @return the cell in Excel conventions, ie "A1" for the first cell.
     */
    public static String getCellRef(Cell cell, boolean includeSheet) {
        String ref = includeSheet ? cell.getSheet().getSheetName() + "!" : "";
        ref += getColumnName(cell.getColumnNumber()) + (cell.getRowNumber() + 1);
        return ref;
    }

    /**
     * Return copy of workbook in a different implementation.
     *
     * @param <WORKBOOK> the workbook class of the target
     * @param clazz {@code Class} instance for the workbook class of the target
     * @param workbook the source workbook
     * @return workbook instance of type {@code WORKBOOK} with the contents of
     * {@code workbook}
     */
    public static <WORKBOOK extends Workbook> WORKBOOK cloneWorkbookAs(Class<WORKBOOK> clazz, Workbook workbook) {
        try {
            WORKBOOK newWorkbook = clazz.getConstructor(Locale.class).newInstance(workbook.getLocale());
            newWorkbook.setUri(workbook.getUri());

            // copy styles
            for (String styleName : workbook.getCellStyleNames()) {
                CellStyle cellStyle = workbook.getCellStyle(styleName);
                CellStyle newCellStyle = newWorkbook.getCellStyle(styleName);
                newCellStyle.copyStyle(cellStyle);
            }

            // copy sheets
            for (int sheetNr = 0; sheetNr < workbook.getSheetCount(); sheetNr++) {
                Sheet sheet = workbook.getSheet(sheetNr);
                Sheet newSheet = newWorkbook.createSheet(sheet.getSheetName());
                newSheet.copy(sheet);
            }
            return newWorkbook;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException("Error cloning workbook: " + ex.getMessage(), ex);
        }
    }

    public static Writer createWriter(Appendable app) {
        if (app instanceof Writer) {
            return (Writer) app;
        } else {
            return new AppendableWriter(app);
        }
    }

    /**
     * Copy sheet data.
     * <p>
     * Copies all data from one sheet to another. Sheets may be instances of
     * different implementation classes.
     * </p>
     *
     * @param dst the destination sheet
     * @param src the source sheet
     */
    public static void copySheetData(Sheet dst, Sheet src) {
        // copy split
        dst.splitAt(src.getSplitRow(), src.getSplitColumn());
        // set autofilter
        dst.setAutofilterRow(src.getAutoFilterRow());
        // copy column widths
        for (int j = src.getFirstColNum(); j <= src.getLastColNum(); j++) {
            dst.setColumnWidth(j, src.getColumnWidth(j));
        }
        // copy merged regions
        for (RectangularRegion rr : src.getMergedRegions()) {
            dst.addMergedRegion(rr);
        }
        // copy row data
        for (Row row : src) {
            final int i = row.getRowNumber();
            dst.getRow(i).copy(row);
            dst.setRowHeight(i, src.getRowHeight(i));
        }
    }

    /**
     * Find cell containing text in sheet.
     *
     * @param sheet the sheet
     * @param text the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Cell find(Sheet sheet, String text, SearchOptions... options) {
        // EnumSet.of throws IllegalArgumentException if options is empty, so
        // use a standard HashSet instead.
        return find(sheet, text, new HashSet<>(Arrays.asList(options)));
    }

    /**
     * Find cell containing text in sheet.
     *
     * @param sheet the sheet
     * @param text the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Cell find(Sheet sheet, String text, Set<SearchOptions> options) {
        boolean searchFromCurrent = options.contains(SearchOptions.SEARCH_FROM_CURRENT);
        boolean ignoreCase = options.contains(SearchOptions.IGNORE_CASE);
        boolean matchComplete = options.contains(SearchOptions.MATCH_COMPLETE_TEXT);
        boolean updateCurrent = options.contains(SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND);
        boolean searchFormula = options.contains(SearchOptions.SEARCH_FORMLUA_TEXT);

        if (ignoreCase) {
            text = text.toLowerCase();
        }

        Cell cell = searchFromCurrent ? sheet.getCurrentCell() : null;
        int iStart = cell != null ? cell.getRowNumber() : sheet.getLastRowNum();
        int jStart = cell != null ? cell.getColumnNumber() : sheet.getLastColNum();
        int i = iStart;
        int j = jStart;
        do {
            // move to next cell
            if (j < sheet.getRow(i).getLastCellNum()) {
                j++;
            } else {
                j = 0;
                if (i < sheet.getLastRowNum()) {
                    i++;
                } else {
                    i = 0;
                }
            }

            cell = sheet.getCell(i, j);

            // check cell content
            String cellText;
            if (searchFormula && cell.getCellType() == CellType.FORMULA) {
                cellText = cell.getFormula();
            } else {
                cellText = cell.toString();
            }

            if (ignoreCase) {
                cellText = cellText.toLowerCase();
            }

            if (matchComplete && cellText.equals(text)
                    || !matchComplete && cellText.contains(text)) {
                // found!
                if (updateCurrent) {
                    sheet.setCurrentCell(cell);
                }
                return cell;
            }
        } while (i != iStart || j != jStart);
        return null;
    }

    /**
     * Find cell containing text in row.
     *
     * @param row the row
     * @param text the text to search for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Cell find(Row row, String text, SearchOptions... options) {
        // EnumSet.of throws IllegalArgumentException if options is empty, so
        // use a standard HashSet instead.
        return find(row, text, new HashSet<>(Arrays.asList(options)));
    }

    /**
     * Find cell containing text in row.
     *
     * @param row the row
     * @param text the text to search for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Cell find(Row row, String text, Set<SearchOptions> options) {
        boolean searchFromCurrent = options.contains(SearchOptions.SEARCH_FROM_CURRENT);
        boolean ignoreCase = options.contains(SearchOptions.IGNORE_CASE);
        boolean matchComplete = options.contains(SearchOptions.MATCH_COMPLETE_TEXT);
        boolean updateCurrent = options.contains(SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND);
        boolean searchFormula = options.contains(SearchOptions.SEARCH_FORMLUA_TEXT);

        if (ignoreCase) {
            text = text.toLowerCase();
        }

        int jStart = searchFromCurrent ? row.getSheet().getCurrentCell().getColumnNumber() : row.getLastCellNum();
        int j = jStart;
        do {
            // move to next cell
            if (j < row.getLastCellNum()) {
                j++;
            } else {
                j = 0;
            }

            Cell cell = row.getCell(j);

            // check cell content
            String cellText;
            if (searchFormula && cell.getCellType() == CellType.FORMULA) {
                cellText = cell.getFormula();
            } else {
                cellText = cell.toString();
            }

            if (ignoreCase) {
                cellText = cellText.toLowerCase();
            }

            if (matchComplete && cellText.equals(text)
                    || !matchComplete && cellText.contains(text)) {
                // found!
                if (updateCurrent) {
                    row.getSheet().setCurrentCell(cell);
                }
                return cell;
            }
        } while (j != jStart);

        return null;
    }

    public static void set(Cell cell, Object arg) {
        if (arg == null) {
            cell.clear();
        } else if (arg instanceof Number) {
            cell.set((Number) arg);
        } else if (arg instanceof Boolean) {
            cell.set((Boolean) arg);
        } else if (arg instanceof Date) {
            cell.set((Date) arg);
        } else if (arg instanceof RichText) {
            cell.set((RichText) arg);
        } else {
            cell.set(String.valueOf(arg));
        }
    }

    public static String encode(Color color) {
        return "#"+Integer.toHexString(color.getRGB());
    }

    private MejaHelper() {
    }

    private static Color colorDecode(int i) {
        return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
    }

    private static final Map<String, Color> COLORS;

    static {
        COLORS = new HashMap<>();
        COLORS.put("black", colorDecode(0x000000));
        COLORS.put("silver", colorDecode(0xc0c0c0));
        COLORS.put("gray", colorDecode(0x808080));
        COLORS.put("white", colorDecode(0xffffff));
        COLORS.put("maroon", colorDecode(0x800000));
        COLORS.put("red", colorDecode(0xff0000));
        COLORS.put("purple", colorDecode(0x800080));
        COLORS.put("fuchsia", colorDecode(0xff00ff));
        COLORS.put("green", colorDecode(0x008000));
        COLORS.put("lime", colorDecode(0x00ff00));
        COLORS.put("olive", colorDecode(0x808000));
        COLORS.put("yellow", colorDecode(0xffff00));
        COLORS.put("navy", colorDecode(0x000080));
        COLORS.put("blue", colorDecode(0x0000ff));
        COLORS.put("teal", colorDecode(0x008080));
        COLORS.put("aqua", colorDecode(0x00ffff));
        COLORS.put("orange", colorDecode(0xffa500));
        COLORS.put("aliceblue", colorDecode(0xf0f8ff));
        COLORS.put("antiquewhite", colorDecode(0xfaebd7));
        COLORS.put("aquamarine", colorDecode(0x7fffd4));
        COLORS.put("azure", colorDecode(0xf0ffff));
        COLORS.put("beige", colorDecode(0xf5f5dc));
        COLORS.put("bisque", colorDecode(0xffe4c4));
        COLORS.put("blanchedalmond", colorDecode(0xffe4c4));
        COLORS.put("blueviolet", colorDecode(0x8a2be2));
        COLORS.put("brown", colorDecode(0xa52a2a));
        COLORS.put("burlywood", colorDecode(0xdeb887));
        COLORS.put("cadetblue", colorDecode(0x5f9ea0));
        COLORS.put("chartreuse", colorDecode(0x7fff00));
        COLORS.put("chocolate", colorDecode(0xd2691e));
        COLORS.put("coral", colorDecode(0xff7f50));
        COLORS.put("cornflowerblue", colorDecode(0x6495ed));
        COLORS.put("cornsilk", colorDecode(0xfff8dc));
        COLORS.put("crimson", colorDecode(0xdc143c));
        COLORS.put("darkblue", colorDecode(0x00008b));
        COLORS.put("darkcyan", colorDecode(0x008b8b));
        COLORS.put("darkgoldenrod", colorDecode(0xb8860b));
        COLORS.put("darkgray", colorDecode(0xa9a9a9));
        COLORS.put("darkgreen", colorDecode(0x006400));
        COLORS.put("darkgrey", colorDecode(0xa9a9a9));
        COLORS.put("darkkhaki", colorDecode(0xbdb76b));
        COLORS.put("darkmagenta", colorDecode(0x8b008b));
        COLORS.put("darkolivegreen", colorDecode(0x556b2f));
        COLORS.put("darkorange", colorDecode(0xff8c00));
        COLORS.put("darkorchid", colorDecode(0x9932cc));
        COLORS.put("darkred", colorDecode(0x8b0000));
        COLORS.put("darksalmon", colorDecode(0xe9967a));
        COLORS.put("darkseagreen", colorDecode(0x8fbc8f));
        COLORS.put("darkslateblue", colorDecode(0x483d8b));
        COLORS.put("darkslategray", colorDecode(0x2f4f4f));
        COLORS.put("darkslategrey", colorDecode(0x2f4f4f));
        COLORS.put("darkturquoise", colorDecode(0x00ced1));
        COLORS.put("darkviolet", colorDecode(0x9400d3));
        COLORS.put("deeppink", colorDecode(0xff1493));
        COLORS.put("deepskyblue", colorDecode(0x00bfff));
        COLORS.put("dimgray", colorDecode(0x696969));
        COLORS.put("dimgrey", colorDecode(0x696969));
        COLORS.put("dodgerblue", colorDecode(0x1e90ff));
        COLORS.put("firebrick", colorDecode(0xb22222));
        COLORS.put("floralwhite", colorDecode(0xfffaf0));
        COLORS.put("forestgreen", colorDecode(0x228b22));
        COLORS.put("gainsboro", colorDecode(0xdcdcdc));
        COLORS.put("ghostwhite", colorDecode(0xf8f8ff));
        COLORS.put("gold", colorDecode(0xffd700));
        COLORS.put("goldenrod", colorDecode(0xdaa520));
        COLORS.put("greenyellow", colorDecode(0xadff2f));
        COLORS.put("grey", colorDecode(0x808080));
        COLORS.put("honeydew", colorDecode(0xf0fff0));
        COLORS.put("hotpink", colorDecode(0xff69b4));
        COLORS.put("indianred", colorDecode(0xcd5c5c));
        COLORS.put("indigo", colorDecode(0x4b0082));
        COLORS.put("ivory", colorDecode(0xfffff0));
        COLORS.put("khaki", colorDecode(0xf0e68c));
        COLORS.put("lavender", colorDecode(0xe6e6fa));
        COLORS.put("lavenderblush", colorDecode(0xfff0f5));
        COLORS.put("lawngreen", colorDecode(0x7cfc00));
        COLORS.put("lemonchiffon", colorDecode(0xfffacd));
        COLORS.put("lightblue", colorDecode(0xadd8e6));
        COLORS.put("lightcoral", colorDecode(0xf08080));
        COLORS.put("lightcyan", colorDecode(0xe0ffff));
        COLORS.put("lightgoldenrodyellow", colorDecode(0xfafad2));
        COLORS.put("lightgray", colorDecode(0xd3d3d3));
        COLORS.put("lightgreen", colorDecode(0x90ee90));
        COLORS.put("lightgrey", colorDecode(0xd3d3d3));
        COLORS.put("lightpink", colorDecode(0xffb6c1));
        COLORS.put("lightsalmon", colorDecode(0xffa07a));
        COLORS.put("lightseagreen", colorDecode(0x20b2aa));
        COLORS.put("lightskyblue", colorDecode(0x87cefa));
        COLORS.put("lightslategray", colorDecode(0x778899));
        COLORS.put("lightslategrey", colorDecode(0x778899));
        COLORS.put("lightsteelblue", colorDecode(0xb0c4de));
        COLORS.put("lightyellow", colorDecode(0xffffe0));
        COLORS.put("limegreen", colorDecode(0x32cd32));
        COLORS.put("linen", colorDecode(0xfaf0e6));
        COLORS.put("mediumaquamarine", colorDecode(0x66cdaa));
        COLORS.put("mediumblue", colorDecode(0x0000cd));
        COLORS.put("mediumorchid", colorDecode(0xba55d3));
        COLORS.put("mediumpurple", colorDecode(0x9370db));
        COLORS.put("mediumseagreen", colorDecode(0x3cb371));
        COLORS.put("mediumslateblue", colorDecode(0x7b68ee));
        COLORS.put("mediumspringgreen", colorDecode(0x00fa9a));
        COLORS.put("mediumturquoise", colorDecode(0x48d1cc));
        COLORS.put("mediumvioletred", colorDecode(0xc71585));
        COLORS.put("midnightblue", colorDecode(0x191970));
        COLORS.put("mintcream", colorDecode(0xf5fffa));
        COLORS.put("mistyrose", colorDecode(0xffe4e1));
        COLORS.put("moccasin", colorDecode(0xffe4b5));
        COLORS.put("navajowhite", colorDecode(0xffdead));
        COLORS.put("oldlace", colorDecode(0xfdf5e6));
        COLORS.put("olivedrab", colorDecode(0x6b8e23));
        COLORS.put("orangered", colorDecode(0xff4500));
        COLORS.put("orchid", colorDecode(0xda70d6));
        COLORS.put("palegoldenrod", colorDecode(0xeee8aa));
        COLORS.put("palegreen", colorDecode(0x98fb98));
        COLORS.put("paleturquoise", colorDecode(0xafeeee));
        COLORS.put("palevioletred", colorDecode(0xdb7093));
        COLORS.put("papayawhip", colorDecode(0xffefd5));
        COLORS.put("peachpuff", colorDecode(0xffdab9));
        COLORS.put("peru", colorDecode(0xcd853f));
        COLORS.put("pink", colorDecode(0xffc0cb));
        COLORS.put("plum", colorDecode(0xdda0dd));
        COLORS.put("powderblue", colorDecode(0xb0e0e6));
        COLORS.put("rosybrown", colorDecode(0xbc8f8f));
        COLORS.put("royalblue", colorDecode(0x4169e1));
        COLORS.put("saddlebrown", colorDecode(0x8b4513));
        COLORS.put("salmon", colorDecode(0xfa8072));
        COLORS.put("sandybrown", colorDecode(0xf4a460));
        COLORS.put("seagreen", colorDecode(0x2e8b57));
        COLORS.put("seashell", colorDecode(0xfff5ee));
        COLORS.put("sienna", colorDecode(0xa0522d));
        COLORS.put("skyblue", colorDecode(0x87ceeb));
        COLORS.put("slateblue", colorDecode(0x6a5acd));
        COLORS.put("slategray", colorDecode(0x708090));
        COLORS.put("slategrey", colorDecode(0x708090));
        COLORS.put("snow", colorDecode(0xfffafa));
        COLORS.put("springgreen", colorDecode(0x00ff7f));
        COLORS.put("steelblue", colorDecode(0x4682b4));
        COLORS.put("tan", colorDecode(0xd2b48c));
        COLORS.put("thistle", colorDecode(0xd8bfd8));
        COLORS.put("tomato", colorDecode(0xff6347));
        COLORS.put("turquoise", colorDecode(0x40e0d0));
        COLORS.put("violet", colorDecode(0xee82ee));
        COLORS.put("wheat", colorDecode(0xf5deb3));
        COLORS.put("whitesmoke", colorDecode(0xf5f5f5));
        COLORS.put("yellowgreen", colorDecode(0x9acd32));
        COLORS.put("rebeccapurple", colorDecode(0x663399));
    }

    public static Color getColor(String s) {
        // try named colors first
        Color color = COLORS.get(s);
        if (color != null) {
            return color;
        }

        // HEX colors
        if (s.startsWith("#")) {
            // FIXME JDK 8
            // int i = Integer.parseUnsignedInt(s.substring(1), 16);
            int i = (int)Long.parseLong(s.substring(1), 16);
            return colorDecode(i);
        }

        // RGB colors. example: "rgb(255, 0, 0)"
        if (s.startsWith("rgb")) {
            String s1 = s.substring(3).trim();
            if (s1.charAt(0) == '(' && s1.charAt(s.length() - 1) == ')') {
                String[] parts = s1.split(",");
                if (parts.length == 3) {
                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    return new Color(r, g, b);
                }
            }
            throw new IllegalArgumentException("Cannot parse \"" + s + "\" as rgb color.");
        }

        // RGBA colors. example: "rgb(255, 0, 0, 0.3)"
        if (s.startsWith("rgba")) {
            String s1 = s.substring(4).trim();
            if (s1.charAt(0) == '(' && s1.charAt(s.length() - 1) == ')') {
                String[] parts = s1.split(",");
                if (parts.length == 4) {
                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    int a = Math.round(255 * Float.parseFloat(parts[3]));
                    return new Color(r, g, b, a);
                }
            }
            throw new IllegalArgumentException("Cannot parse \"" + s + "\" as rgba color.");
        }

        // no luck so far
        throw new IllegalArgumentException("\"" + s + "\" is no valid color.");
    }

    public static float decodeFontSize(String s) throws NumberFormatException {
        float factor = 1f;
        if (s.endsWith("pt")) {
            s = s.substring(0, s.length()-2);
            factor = 1f;
        } else if (s.endsWith("px")) {
            s = s.substring(0, s.length()-2);
            factor = 96f/72f;
        }
        return factor * Float.parseFloat(s);
    }

}
