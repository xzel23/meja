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
import java.util.*;
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
        out.format(Locale.ROOT, "    .%s { ", id(cs));
        out.format(Locale.ROOT, "%s ", cs.getFont().getCssStyle());
        out.format(Locale.ROOT, "%s ", cs.getHAlign().getCssStyle());
        out.format(Locale.ROOT, "%s ", cs.getVAlign().getCssStyle());
        if (cs.getRotation()!=0) {
            out.format(Locale.ROOT, "transform: rotate(%ddeg); ", cs.getRotation());
        }
        for (Direction d : Direction.values()) {
            BorderStyle bs = cs.getBorderStyle(d);
            Color c = bs.getColor();
            float w = bs.getWidth();
            if (!c.isTransparent() && w>0) {
                out.format(Locale.ROOT, "border-%s: %.2fpt solid %s !important; ", d.getCssName(), w, c.toCss());
            }
        }
        if (cs.getFillPattern() != FillPattern.NONE) {
            out.format(Locale.ROOT, "background-color: %s; ", cs.getFillFgColor().toCss());
        }
        
        /* TODO: these are still unsupported:
        cs.getFillFgColor();
        cs.getFillPattern();
        cs.getDataFormat();
        cs.isWrap();        
        */

        out.format(Locale.ROOT, "}%n");
    }

    /**
     * Write sheet as HTML.
     * <p>
     * <em>NOTE:</em> This method does not add HTML header and body tags.
     * @param sheet the sheet to write
     * @param out the PrintStream to write to
     * @param locale the locale to use
     */
    public void writeSheet(Sheet sheet, Formatter out, Locale locale) {
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
        
        out.format(Locale.ROOT, "<div id=\"%s\">%n", sheetId);

        out.format(Locale.ROOT, "  <table class=\"meja-sheet\">%n");

        CellStyle defaultCellStyle = sheet.getWorkbook().getDefaultCellStyle();

        // write column widths
        out.format(Locale.ROOT, "    <colgroup>%n");
        for (int j=0; j<=sheet.getLastColNum(); j++) {
            out.format(Locale.ROOT, "<col style=\"width: %.2fpt;\">%n", sheet.getColumnWidth(j));
        }
        out.format(Locale.ROOT, "    </colgroup>%n");

        out.format(Locale.ROOT, "    <tbody>%n");
        
        int rownr=0;
        for (Row row : sheet) {
            // add missing rows
            while (rownr++<row.getRowNumber()) {
                out.format(Locale.ROOT, "      <tr style=\"height: %.2fpt;\">%n", sheet.getRowHeight(rownr));
                for (int i=0; i<sheet.getLastColNum(); i++) {
                    out.format(Locale.ROOT, "        <td></td>%n");
                }
                out.format(Locale.ROOT, "      </tr>%n");
            }

            out.format(Locale.ROOT, "      <tr style=\"height: %.2fpt;\">%n", sheet.getRowHeight(row.getRowNumber()));
            
            int colnr=0;
            for (Cell cell : row) {
                if (cell.getHorizontalSpan() == 0 || cell.getVerticalSpan()==0) {
                    continue;
                }
                
                // add missing cells
                while (colnr<cell.getColumnNumber()) {
                    if (!row.getCell(colnr).isMerged()) {
                        out.format(Locale.ROOT, "        <td></td>%n");
                    }
                    colnr++;
                }

                CellStyle style = cell.getCellStyle();

                out.format(Locale.ROOT, "        <td");
                writeAttribute(out, "colspan", cell, Cell::getHorizontalSpan, v -> v>1, Object::toString);
                writeAttribute(out, "rowspan", cell, Cell::getVerticalSpan, v -> v>1, Object::toString);
                if (!style.equals(defaultCellStyle)) {
                    writeAttribute(out, "class", HtmlWorkbookWriter.id(style));
                }
                out.format(Locale.ROOT, ">");

                Optional<URI> hyperlink = cell.getHyperlink();
                hyperlink.ifPresent(link -> out.format(Locale.ROOT, "<a href=\"%s\">", baseUri.map(base -> base.relativize(link)).orElse(link)));
                out.format(Locale.ROOT, "%s", cell.getAsText(locale));
                hyperlink.ifPresent(link -> out.format(Locale.ROOT, "</a>"));

                out.format(Locale.ROOT, "</td>%n");
                colnr += cell.getHorizontalSpan();
            }
            
            updateProgress.accept((double) processedRows / totalRows);
            
            out.format(Locale.ROOT, "    </tr>%n");
            
            processedRows++;
        }
        
        out.format(Locale.ROOT, "    </tbody>%n");
        out.format(Locale.ROOT, "  </table>%n");

        // close DIV for sheet
        out.format(Locale.ROOT, "</div>%n");
        return processedRows;
    }

    private static <T> void writeAttribute(Formatter out, String attribute, Cell cell, Function<Cell,T> getter, Predicate<T> condition, Function<T,String> formatter) {
        T v = getter.apply(cell);
        if (condition.test(v)) {
            writeAttribute(out, attribute, formatter.apply(v));
        }
    }
    
    private static void writeAttribute(Formatter out, String attribute, String formattedValue) {
        out.format(Locale.ROOT, " %s=\"%s\"", attribute, formattedValue);
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
        out.format(Locale.ROOT, "<html>%n<head>%n  <meta charset=\"utf-8\">%n");
    }

    public void writeHtmlHeaderEnd(Formatter out) {
        out.format(Locale.ROOT, "</head>%n<body>%n");
    }
    public void writeHtmlFooter(Formatter out) {
        out.format(Locale.ROOT, "</body>%n</html>%n");
    }

    public void writeCss(Formatter out, Workbook... workbooks) {
        out.format(Locale.ROOT, "  <style>%n");
        writeCommonCss(out);
        for (Workbook workbook : workbooks) {
            workbook.cellStyles().forEach(cs -> writeCellStyle(out, cs));
        }
        out.format(Locale.ROOT, "  </style>%n");
    }

    private void writeCommonCss(Formatter out) {
        out.format(Locale.ROOT, "    table.meja-sheet { border-collapse: collapse; table-layout: fixed; }%n" +
                                "    table.meja-sheet td,th { border: 1px solid #d4d4d4; padding: 3px; white-space: pre; overflow: visible; max-width: 0; max-height: 0; }%n" +
                                "    table.meja-sheet td:empty::after{ content: \"\\00a0\"; }%n");
    }

    public void writeCssForSingleSheet(Formatter out, Sheet sheet) {
        // determine styles used in this sheet
        Set<CellStyle> styles = new HashSet<>();
        sheet.rows().forEach(row -> row.cells().map(Cell::getCellStyle).forEach(styles::add));

        out.format(Locale.ROOT, "  <style>%n");
        writeCommonCss(out);
        // sort styles to get reproducible results (i. e. in unit tests)
        styles.stream().sorted((a,b) -> a.getName().compareTo(b.getName())).forEach(cs -> writeCellStyle(out, cs));
        out.format(Locale.ROOT, "  </style>%n");
    }

}
