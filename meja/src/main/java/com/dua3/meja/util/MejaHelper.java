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

import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.utility.io.FileType;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.text.TextUtil.Alignment;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public final class MejaHelper {

    private static final Pattern PATTERN_NEWLINE = Pattern.compile("\\R");

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
     * Open workbook file.
     * <p>
     * This method inspects the file name extension to determine which factory
     * should be used for loading.
     * </p>
     *
     * @param path the workbook Path
     * @return the workbook loaded from file
     * @throws IOException if workbook could not be loaded
     */
    public static Workbook openWorkbook(Path path) throws IOException {
        return FileType.read(path, Workbook.class).orElseThrow(() -> new IOException("could not read workbook: " + path));
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
     * Print Sheet contents as a text table (for console output).
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
     * @param <A>          type parameter for the Appendable used
     * @param app          the Appendable used for output
     * @param sheet        the sheet to print
     * @param locale       the locale to use (i.e. for number formatting)
     * @param printOptions the {@link PrintOptions} to use
     * @return the Appendable
     */
    public static <A extends Appendable> A printTable(A app, Sheet sheet, Locale locale, PrintOptions... printOptions) {
        EnumSet<PrintOptions> options = LangUtil.enumSet(PrintOptions.class, printOptions);

        // set up the symbols used to drawing lines
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
                for (String s : PATTERN_NEWLINE.split(row.getCell(j).toString(locale))) {
                    columnLength[j] = Math.max(columnLength[j], s.length());
                }
            }
        }

        int overallLength = 1;
        for (int len : columnLength) {
            overallLength += len + 1;
        }

        // output data
        try (Formatter fmt = new Formatter(app)) {

            if (options.contains(PrintOptions.LINE_ABOVE)) {
                fmt.format("%s%n", dash.repeat(overallLength));
            }

            if (options.contains(PrintOptions.PREPEND_SHEET_NAME)) {
                String title = sheet.getSheetName();
                fmt.format("%2$s%1$s%2$s%n", TextUtil.align(title, overallLength - 2, Alignment.CENTER), pipe);

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
                    data[j] = PATTERN_NEWLINE.split(row.getCell(j).toString(locale));
                    align[j] = row.getCell(j).get() instanceof Number ? 1 : -1;
                    lines = Math.max(lines, data[j].length);
                }

                // print row data
                for (int k = 0; k < lines; k++) {
                    fmt.format("%s", pipe /* '|' */);
                    for (int j = 0; j < sheet.getColumnCount(); j++) {
                        int w = align[j] * columnLength[j];
                        String[] columnData = data[j];
                        String s = k < columnData.length ? columnData[k] : "";
                        //noinspection StringConcatenationInFormatCall
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
        }

        return app;
    }

    private MejaHelper() {
    }

}
