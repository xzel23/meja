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

import com.dua3.meja.model.*;
import com.dua3.utility.data.Color;
import com.dua3.utility.text.TextUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Axel Howind (axel@dua3.com)
 */
public final class HtmlWorkbookWriter implements WorkbookWriter {

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

    private static void writeSheets(Workbook workbook, Formatter out, Locale locale, DoubleConsumer updateProgress) throws IOException {
        long totalRows = 0;
        for (Sheet sheet : workbook) {
            totalRows += sheet.getRowCount();
        }

        long processedRows = 0;
        for (Sheet sheet : workbook) {
            processedRows = writeSheet(sheet, out, locale, totalRows, processedRows, updateProgress);
        }
    }
    
    private static void writeCellStyle(Formatter out, CellStyle cs) {
        out.format("    .%s { ", id(cs));
        out.format("%s ", cs.getFont().getCssStyle());
        out.format("%s ", cs.getHAlign().getCssStyle());
        out.format("%s ", cs.getVAlign().getCssStyle());
        if (cs.getRotation()!=0) {
            out.format("transform: rotate(%ddeg); ", cs.getRotation());
        }
        for (Direction d : Direction.values()) {
            BorderStyle bs = cs.getBorderStyle(d);
            Color c = bs.getColor();
            float w = bs.getWidth();
            if (!c.isTransparent() && w>0) {
                out.format(Locale.ROOT, "border-%s: %.2fpt solid %s; ", d.getCssName(), w, c.toCss());
            }
        }
        if (!cs.getFillBgColor().isTransparent()) {
            out.format("background-color: %s; ", cs.getFillBgColor().toCss());
        }
        
        /* TODO: these are still unsupported:
        cs.getFillFgColor();
        cs.getFillPattern();
        cs.getDataFormat();
        cs.isWrap();        
        */

        out.format("}%n");
    }

    /**
     * Write sheet as HTML.
     * <p>
     * <em>NOTE:</em> This method does not add HTML header and body tags.
     * @param sheet the sheet to write
     * @param out the PrintStream to write to
     * @param locale the locale to use
     */
    public static void writeSheet(Sheet sheet, Formatter out, Locale locale) {
        writeSheet(sheet, out, locale, sheet.getRowCount(), 0L, p -> {});
    }

    private static String id(CellStyle style) {
        return id(style.getWorkbook())
               +"_CS_"
               + TextUtil.byteArrayToHexString(style.getName().getBytes(StandardCharsets.UTF_8));
    }

    private static String id(Sheet sheet) {
        return id(sheet.getWorkbook())
               +"_SHEET_"
               + TextUtil.byteArrayToHexString(sheet.getSheetName().getBytes(StandardCharsets.UTF_8));
    }

    private static String id(Workbook workbook) {
        return "WB_"+TextUtil.getMD5String(workbook.getUri().map(URI::toString).orElse(""));
    }
    
    private static long writeSheet(Sheet sheet, Formatter out, Locale locale, long totalRows, long processedRows, DoubleConsumer updateProgress) {
        Optional<URI> baseUri = sheet.getWorkbook().getUri().map(uri -> uri.resolve(""));

        // open DIV for sheet
        String sheetId = id(sheet);
        
        out.format("<div id=\"%s\">%n", sheetId);

        out.format("<table class=\"meja-sheet\">%n");

        CellStyle defaultCellStyle = sheet.getWorkbook().getDefaultCellStyle();

        for (Row row : sheet) {
            out.format("<tr>%n");
            
            int col=0;
            for (Cell cell : row) {
                if (cell.getHorizontalSpan() == 0 || cell.getVerticalSpan()==0) {
                    continue;
                }
                
                // add missing cells
                while (col++<cell.getColumnNumber()) {
                    out.format("  <td></td>%n");
                }
                
                out.format("  <td");
                writeAttribute(out, "colspan", cell, Cell::getHorizontalSpan, v -> v>1, Object::toString);
                writeAttribute(out, "rowspan", cell, Cell::getVerticalSpan, v -> v>1, Object::toString);
                writeAttribute(out, "class", cell, Cell::getCellStyle, cs -> !Objects.equals(cs, defaultCellStyle), HtmlWorkbookWriter::id);
                out.format(">");

                Optional<URI> hyperlink = cell.getHyperlink();
                hyperlink.ifPresent(link -> out.format("<a href=\"%s\">", baseUri.map(base -> base.relativize(link)).orElse(link)));
                out.format("%s", cell.getAsText(locale));
                hyperlink.ifPresent(link -> out.format("</a>"));
                
                out.format("</td>%n");
            }
            
            updateProgress.accept((double) processedRows / totalRows);
            
            out.format("</tr>%n");
            
            processedRows++;
        }

        out.format("</table>%n");

        // close DIV for sheet
        out.format("</div>%n");
        return processedRows;
    }

    private static <T> void writeAttribute(Formatter out, String attribute, Cell cell, Function<Cell,T> getter, Predicate<T> condition, Function<T,String> formatter) {
        T v = getter.apply(cell);
        if (condition.test(v)) {
            out.format(" %s=\"%s\"", attribute, formatter.apply(v));
        }
    }
    
    @Override
    public void write(Workbook workbook, OutputStream out, DoubleConsumer updateProgress) throws IOException {
        write(workbook, out, Locale.getDefault(), updateProgress);
    }

    /**
     * Write to a Formatter.
     *
     * @param workbook the workbook to write
     * @param out      the write to write the workbook to
     * @param locale   the locale to use (i. e. when formatting cell contents such as numbers)
     * @throws IOException if an input/output error occurs
     */
    public void write(Workbook workbook, Formatter out, Locale locale) throws IOException {
        writeSheets(workbook, out, locale, p -> {});
    }

    /**
     * Write to a Formatter.
     *
     * @param workbook       the workbook to write
     * @param out            the Formatter to write the workbook to
     * @param locale         the locale to use (i. e. when formatting cell contents such as numbers)
     * @param updateProgress callback for progress updates
     * @throws IOException   if an input/output error occurs
     */
    public void write(Workbook workbook, Formatter out, Locale locale, DoubleConsumer updateProgress) throws IOException {
        writeSheets(workbook, out, locale, updateProgress);
    }

    /**
     * Write to OutputStream.
     *
     * @param workbook       the workbook to write
     * @param out            the OutputStream to write the workbook to
     * @param locale         the locale to use (i. e. when formatting cell contents such as numbers)
     * @param updateProgress callback for progress updates
     * @throws IOException   if an input/output error occurs
     */
    public void write(Workbook workbook, OutputStream out, Locale locale, DoubleConsumer updateProgress) throws IOException {
        Formatter fmt = new Formatter(out, StandardCharsets.UTF_8.name());
        writeHtmlHeaderStart(fmt);
        writeCss(fmt, workbook);
        writeHtmlHeaderEnd(fmt);
        writeSheets(workbook, fmt, locale, updateProgress);
        writeHtmlFooter(fmt);
    }

    public void writeHtmlHeaderStart(Formatter out) {
        out.format("<html>%n<head>%n  <meta charset=\"utf-8\">%n");
    }

    public void writeHtmlHeaderEnd(Formatter out) {
        out.format("</head>%n<body>%n");
    }
    public void writeHtmlFooter(Formatter out) {
        out.format("</body>%n</html>%n");
    }

    public void writeCss(Formatter out, Workbook... workbooks) {
        out.format("  <style>%n" +
               "    table.meja-sheet { border-collapse: collapse; }%n" +
               "    table.meja-sheet td,th { border: 1px solid darkgray; padding: 3px; }%n");
        for (Workbook workbook : workbooks) {
            workbook.cellStyles().forEach(cs -> writeCellStyle(out, cs));
        }
        out.format("  </style>%n");
    }
    
}
