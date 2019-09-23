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
package com.dua3.meja.util;

import com.dua3.meja.model.*;
import com.dua3.utility.io.FileType;
import com.dua3.utility.text.TextUtil;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class MejaHelper {

    /**
     * Find cell containing text in row.
     *
     * @param row     the row
     * @param text    the text to search for
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
     * @param row     the row
     * @param text    the text to search for
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
            text = text.toLowerCase(Locale.ROOT);
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
                cellText = cellText.toLowerCase(Locale.ROOT);
            }

            if (matchComplete && cellText.equals(text) || !matchComplete && cellText.contains(text)) {
                // found!
                if (updateCurrent) {
                    row.getSheet().setCurrentCell(cell);
                }
                return cell;
            }
        } while (j != jStart);

        return null;
    }

    /**
     * Find cell containing text in sheet.
     *
     * @param sheet   the sheet
     * @param text    the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Optional<Cell> find(Sheet sheet, String text, SearchOptions... options) {
        // EnumSet.of throws IllegalArgumentException if options is empty, so
        // use a standard HashSet instead.
        return find(sheet, text, new HashSet<>(Arrays.asList(options)));
    }

    /**
     * Find cell containing text in sheet.
     *
     * @param sheet   the sheet
     * @param text    the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return {@code Optional} holding the cell found or empty
     */
    public static Optional<Cell> find(Sheet sheet, String text, Set<SearchOptions> options) {
        Lock lock = sheet.readLock();
        lock.lock();
        try {
            boolean searchFromCurrent = options.contains(SearchOptions.SEARCH_FROM_CURRENT);
            boolean ignoreCase = options.contains(SearchOptions.IGNORE_CASE);
            boolean matchComplete = options.contains(SearchOptions.MATCH_COMPLETE_TEXT);
            boolean updateCurrent = options.contains(SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND);
            boolean searchFormula = options.contains(SearchOptions.SEARCH_FORMLUA_TEXT);

            if (isEmpty(sheet)) {
                return Optional.empty();
            }

            if (ignoreCase) {
                text = text.toLowerCase(Locale.ROOT);
            }

            Cell end = null;
            Cell cell;
            if (searchFromCurrent) {
                cell = nextCell(sheet.getCurrentCell());
            } else {
                cell = sheet.getCell(sheet.getFirstRowNum(), sheet.getFirstColNum());
            }

            while (end == null || !(cell.getRowNumber() == end.getRowNumber()
                    && cell.getColumnNumber() == end.getColumnNumber())) {
                if (end == null) {
                    // remember the first visited cell
                    end = cell;
                }

                // check cell content
                String cellText;
                if (searchFormula && cell.getCellType() == CellType.FORMULA) {
                    cellText = cell.getFormula();
                } else {
                    cellText = cell.toString();
                }

                if (ignoreCase) {
                    cellText = cellText.toLowerCase(Locale.ROOT);
                }

                if (matchComplete && cellText.equals(text) || !matchComplete && cellText.contains(text)) {
                    // found!
                    if (updateCurrent) {
                        sheet.setCurrentCell(cell);
                    }
                    return Optional.of(cell);
                }

                // move to next cell
                cell = nextCell(cell);
            }

            // not found
            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Test if sheet is empty.
     *
     * @param sheet the sheet to test
     * @return true, if the sheet is empty
     */
    public static boolean isEmpty(Sheet sheet) {
        return sheet.getRowCount() == 0;
    }

    private static Cell nextCell(Cell cell) {
        // move to next cell
        Row row = cell.getRow();
        int j = cell.getColumnNumber();
        if (j < row.getLastCellNum()) {
            // cell is not the last one in row -> move right
            return row.getCell(j + 1);
        } else {
            // cell is the last one in row...
            Sheet sheet = row.getSheet();
            int i = row.getRowNumber();
            if (i < sheet.getLastRowNum()) {
                // not the last row -> move to next row
                row = sheet.getRow(i + 1);
            } else {
                // last row -> move to first row
                row = sheet.getRow(sheet.getFirstRowNum());
            }
            // return the first cell of the new row
            return row.getCell(row.getFirstCellNum());
        }
    }

    /**
     * Open workbook file.
     * <p>
     * This method inspects the file name extension to determine which factory
     * should be used for loading.
     * </p>
     *
     * @param uri the workbook URI
     * @return the workbook loaded from file
     * @throws IOException if workbook could not be loaded
     */
    public static Workbook openWorkbook(URI uri) throws IOException {
        return FileType.read(uri, Workbook.class).orElseThrow(() -> new IOException("could not read workbook: " + uri));
    }

    /**
     * Options controlling print output.
     */
    public enum PrintOptions {
        DRAW_LINES,
        PREPEND_SHEET_NAME,
        LINE_ABOVE,
        LINE_BELOW,
        FIRST_LINE_IS_HEADER
    }

    /**
     * Print Sheet contesnts as a text table (for console output).
     * <p>
     * Example output:
     * <pre>
     *     ----------  (PrintOptions.LINE_ABOVE)
     *     |A|  B| C|
     *     |-+---+--|  (PrintOptions.LINE_BELOW_HEADER)
     *     |1|123|xx|
     *     ----------  (PrintOptions.LINE_BELOW)
     * </pre>
     *
     * @param <A>
     *      type parameter for the Appendable used
     * @param app
     *  the Appendable used for output
     * @param sheet
     *  the sheet to print
     * @param locale
     *  the locale to use (i. e. for number formatting)
     * @param printOptions
     *  the {@link PrintOptions} to use
     * @return
     *  the Appanedable
     */
    public static <A extends Appendable> A printTable(A app, Sheet sheet, Locale locale, PrintOptions... printOptions) {
        EnumSet<PrintOptions> options = printOptions.length==0
                ? EnumSet.noneOf(PrintOptions.class)
                : EnumSet.copyOf(Arrays.asList(printOptions));

        // setup the symbols used to drawing lines
        final String pipe, dash, cross;
        if (options.contains(PrintOptions.DRAW_LINES)) {
            pipe = "|";
            dash = "-";
            cross = "+";
        } else {
            pipe = dash = cross = " ";
        }

        // determine column dimensions
        int[] columnLength = new int[sheet.getColumnCount()];
        for (Row row : sheet) {
            for (int j = 0; j < sheet.getColumnCount(); j++) {
                for (String s : row.getCell(j).toString(locale).split("\n")) {
                    columnLength[j] = Math.max(columnLength[j], s.length());
                }
            }
        }

        int overallLength = 1;
        for (int len : columnLength) {
            overallLength += len + 1;
        }

        // output data
        Formatter fmt = new Formatter(app);

        if (options.contains(PrintOptions.LINE_ABOVE)) {
            fmt.format("%s%n", dash.repeat(overallLength));
        }

        if (options.contains(PrintOptions.PREPEND_SHEET_NAME)) {
            String title = sheet.getSheetName();
            fmt.format("%2$s%1$s%2$s%n", TextUtil.align(title, overallLength-2, TextUtil.Alignment.CENTER), pipe);

            if (options.contains(PrintOptions.LINE_ABOVE)) {
                fmt.format("%s%n", "-".repeat(overallLength));
            }
        }

        boolean isHeadRow = true;
        for (Row row : sheet) {
            // collect data and determine row height
            int lines = 0;
            String[][] data = new String[sheet.getColumnCount()][];
            int[] align = new int[sheet.getColumnCount()];
            for (int j = 0; j < sheet.getColumnCount(); j++) {
                data[j] = row.getCell(j).toString(locale).split("\n");
                align[j] = row.getCell(j).get() instanceof Number ? 1 : -1;
                lines = Math.max(lines, data[j].length);
            }

            // print row data
            for (int k = 0; k < lines; k++) {
                fmt.format("%s", pipe /* '|' */);
                for (int j = 0; j < sheet.getColumnCount(); j++) {
                    int w = align[j]*columnLength[j];
                    String[] colulmnData = data[j];
                    String s = k < colulmnData.length ? colulmnData[k] : "";
                    fmt.format("%1$" + w + "s%2$s", s, pipe);
                }
                fmt.format("%n");
            }

            // print horizontal line
            if (isHeadRow && options.contains(PrintOptions.FIRST_LINE_IS_HEADER) && sheet.getRowCount() > 1) {
                fmt.format("%s", pipe);
                for (int j = 0; j < sheet.getColumnCount(); j++) {
                    String endSymbol = j + 1 < sheet.getColumnCount() ? cross : pipe;
                    fmt.format("%s%s", "-".repeat(columnLength[j]), endSymbol);
                }
                fmt.format("%n");
            }

            isHeadRow = false;
        }

        if (options.contains(PrintOptions.LINE_BELOW)) {
            fmt.format("%s%n", dash.repeat(overallLength));
        }

        return app;
    }

    private MejaHelper() {
    }

}
