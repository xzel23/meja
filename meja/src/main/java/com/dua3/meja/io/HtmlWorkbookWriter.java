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
package com.dua3.meja.io;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.utility.options.OptionValues;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Axel Howind (axel@dua3.com)
 */
public class HtmlWorkbookWriter implements WorkbookWriter {

    private HtmlWorkbookWriter() {
    }

    /**
     * Create instance.
     *
     * @return instance of {@code HtmlWorkbookWriter}
     */
    public static HtmlWorkbookWriter create() {
        return new HtmlWorkbookWriter();
    }

    private static void writeSheets(Workbook workbook, PrintStream out, Locale locale, String wbId, DoubleConsumer updateProgress) throws IOException {
        long processedRows = 0;
        long totalRows = 0;
        for (Sheet sheet : workbook) {
            totalRows += sheet.getRowCount();
        }

        for (Sheet sheet : workbook) {
            // open DIV for sheet
            String sheetId = (wbId.isEmpty() ? "" : wbId+"-") + sheet.getSheetName().replaceAll("[^a-zA-z0-9]", "_");
            out.format("<div id=\"%s\">%n", sheetId);

            out.format("<table class=\"meja-sheet\">%n", sheetId);

            for (Row row : sheet) {
                out.format("<tr>%n", sheetId);
                
                for (Cell cell : row) {
                    if (cell.getHorizontalSpan() == 0 || cell.getVerticalSpan()==0) {
                        continue;
                    }
                    
                    out.format("  <td");
                    writeAttribute(out, "colspan", cell, Cell::getHorizontalSpan, v -> v>1, Object::toString);
                    writeAttribute(out, "rowspan", cell, Cell::getVerticalSpan, v -> v>1, Object::toString);
                    out.format(">");
                    
                    out.format("%s", cell.getAsText(locale));
                    
                    out.format("</td>%n");
                }
                
                updateProgress.accept((double) processedRows / totalRows);
                
                out.format("</tr>%n", sheetId);
            }

            out.format("</table>%n");
            
            // close DIV for sheet
            out.format("</div>%n");
        }
    }

    private static <T> void writeAttribute(PrintStream out, String attribute, Cell cell, Function<Cell,T> getter, Predicate<T> condition, Function<T,String> formatter) {
        T v = getter.apply(cell);
        if (condition.test(v)) {
            out.format(" %s=\"%s\"", attribute, formatter.apply(v));
        }
    }
    
    @Override
    public void write(Workbook workbook, OutputStream out, DoubleConsumer updateProgress) throws IOException {
        write(workbook, out, Locale.getDefault(), "", updateProgress);
    }

    /**
     * Write to a PrintStream.
     *
     * @param workbook the workbook to write
     * @param out      the write to write the workbook to
     * @param locale   the locale to use (i. e. when formatting cell contents such as numbers)
     * @param wbId     workbook ID to use when generating DIV-IDs
     * @throws IOException if an input/output error occurs
     */
    public void write(Workbook workbook, PrintStream out, Locale locale, String wbId) throws IOException {
        writeSheets(workbook, out, locale, wbId, p -> {});
    }

    /**
     * Write to a PrintStream.
     *
     * @param workbook       the workbook to write
     * @param out            the PrintStream to write the workbook to
     * @param locale         the locale to use (i. e. when formatting cell contents such as numbers)
     * @param wbId           workbook ID to use when generating DIV-IDs
     * @param updateProgress callback for progress updates
     * @throws IOException   if an input/output error occurs
     */
    public void write(Workbook workbook, PrintStream out, Locale locale, String wbId, DoubleConsumer updateProgress) throws IOException {
        writeSheets(workbook, out, locale, wbId, updateProgress);
    }

    /**
     * Write to OutputStream.
     *
     * @param workbook       the workbook to write
     * @param out            the OutputStream to write the workbook to
     * @param locale         the locale to use (i. e. when formatting cell contents such as numbers)
     * @param wbId           workbook ID to use when generating DIV-IDs
     * @param updateProgress callback for progress updates
     * @throws IOException   if an input/output error occurs
     */
    public void write(Workbook workbook, OutputStream out, Locale locale, String wbId, DoubleConsumer updateProgress) throws IOException {
        PrintStream printStream = new PrintStream(out, false, StandardCharsets.UTF_8.name());
        printStream.format("<html>%n" +
                           "<head>%n" +
                           "<meta charset=\"utf-8\">%n" +
                           "</head>%n" +
                           "<body>%n");
        writeSheets(workbook, printStream, locale, wbId, updateProgress);
        printStream.format("</body>%n" +
                           "</html>%n");
    }
}
