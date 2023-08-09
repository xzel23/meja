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

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.utility.data.Color;
import com.dua3.utility.io.IoOptions;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.text.TextUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * HtmlWorkbookWriter is a class that implements the WorkbookWriter interface.
 * It is used to write a workbook as HTML.
 */
public final class HtmlWorkbookWriter implements WorkbookWriter {

    private Arguments options;

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
        out.format("<div class=\"meja-tabbar\">\n");
        for (Sheet sheet : workbook) {
            boolean isActive = sheet == sheet.getWorkbook().getCurrentSheet();

            String cls = isActive ? "meja-tablink active" : "meja-tablink";

            out.format("  <button class=\"%s\" onclick=\"mejaShowTab(event, '%s')\">%s</button>\n",
                    cls,
                    id(sheet),
                    sheet.getSheetName()
            );

            totalRows += sheet.getRowCount();
        }
        out.format("</div>\n");

        long processedRows = 0;
        for (Sheet sheet : workbook) {
            processedRows = writeSheet(sheet, out, locale, totalRows, processedRows, updateProgress);
        }
    }

    private static void writeCellStyle(Formatter out, CellStyle cs) {
        out.format(Locale.ROOT, "    .%s {", id(cs));
        writeCellStyleAttributes(out, cs);
        out.format(Locale.ROOT, " }\n");
    }

    private static void writeCellStyleAttributes(Formatter out, CellStyle cs) {
        out.format(Locale.ROOT, " %s ", cs.getFont().getCssStyle());
        out.format(Locale.ROOT, "%s ", cs.getHAlign().getCssStyle());
        out.format(Locale.ROOT, "%s", cs.getVAlign().getCssStyle());
        short alpha = cs.getRotation();
        if (alpha != 0) {
            String origin = alpha > 0 ? "bottom left" : "top left";
            out.format(Locale.ROOT, " transform-origin: %s; transform: rotate(%ddeg);", origin, -alpha);
        }
        for (Direction d : Direction.values()) {
            BorderStyle bs = cs.getBorderStyle(d);
            Color c = bs.color();
            float w = bs.width();
            if (!c.isTransparent() && w > 0) {
                out.format(Locale.ROOT, " border-%s: %.2fpt solid %s !important;", d.getCssName(), w, c.toCss());
            }
        }
        if (cs.getFillPattern() != FillPattern.NONE) {
            out.format(Locale.ROOT, " background-color: %s;", cs.getFillFgColor().toCss());
        }

        if (cs.isWrap()) {
            out.format(Locale.ROOT, " white-space: pre-wrap; overflow-wrap: break-word;");
        }
    }

    @Override
    public void setOptions(Arguments options) {
        this.options = options;
    }

    /**
     * Write sheet as HTML.
     * <p>
     * <em>NOTE:</em> This method does not add HTML header and body tags.
     *
     * @param sheet  the sheet to write
     * @param out    the PrintStream to write to
     * @param locale the locale to use
     */
    public void writeSheet(Sheet sheet, Formatter out, Locale locale) {
        writeSheet(sheet, out, locale, sheet.getRowCount(), 0L, p -> {});
    }

    private static String id(CellStyle style) {
        return id(style.getWorkbook())
                + "_CS_"
                + HexFormat.of().formatHex(style.getName().getBytes(StandardCharsets.UTF_8));
    }

    private static String id(Sheet sheet) {
        return id(sheet.getWorkbook())
                + "_SHEET_"
                + HexFormat.of().formatHex(sheet.getSheetName().getBytes(StandardCharsets.UTF_8));
    }

    private static String id(Workbook workbook) {
        return "WB_" + TextUtil.getMD5String(workbook.getUri().map(URI::toString).orElse(""));
    }

    /**
     * Write a sheet as HTML.
     * <p>
     * <em>NOTE:</em> This method does not add HTML header and body tags.
     *
     * @param sheet           the sheet to write
     * @param out             the Formatter to write to
     * @param locale          the locale to use
     * @param totalRows       the total number of rows in the sheet
     * @param processedRows   the number of rows already processed
     * @param updateProgress  a DoubleConsumer that updates the progress (value between 0 and 1)
     * @return the number of rows processed after writing the sheet
     */
    private static long writeSheet(Sheet sheet, Formatter out, Locale locale, long totalRows, long processedRows, DoubleConsumer updateProgress) {
        Optional<URI> baseUri = sheet.getWorkbook().getUri().map(uri -> uri.resolve(""));

        boolean isActive = sheet == sheet.getWorkbook().getCurrentSheet();

        // open DIV for sheet
        String sheetId = id(sheet);

        String display = isActive ? "block" : "none";
        String cls = isActive ? "meja-tab active" : "meja-tab";

        out.format(Locale.ROOT, "<div id=\"%s\" class=\"%s\" style=\"display: %s\">\n", sheetId, cls, display);

        out.format(Locale.ROOT, "  <table class=\"meja-sheet\">\n");

        CellStyle defaultCellStyle = sheet.getWorkbook().getDefaultCellStyle();

        // write column widths
        out.format(Locale.ROOT, "    <colgroup>\n");
        for (int j = 0; j <= sheet.getLastColNum(); j++) {
            out.format(Locale.ROOT, "<col style=\"width: %.2fpt;\">\n", sheet.getColumnWidth(j));
        }
        out.format(Locale.ROOT, "    </colgroup>\n");

        out.format(Locale.ROOT, "    <tbody>\n");

        int rownr = 0;
        for (Row row : sheet) {
            // add missing rows
            while (rownr++ < row.getRowNumber()) {
                out.format(Locale.ROOT, "      <tr style=\"height: %.2fpt;\">\n", sheet.getRowHeight(rownr));
                for (int i = 0; i < sheet.getLastColNum(); i++) {
                    out.format(Locale.ROOT, "        <td></td>\n");
                }
                out.format(Locale.ROOT, "      </tr>\n");
            }

            out.format(Locale.ROOT, "      <tr style=\"height: %.2fpt;\">\n", sheet.getRowHeight(row.getRowNumber()));

            int colnr = 0;
            for (Cell cell : row) {
                if (cell.getHorizontalSpan() == 0 || cell.getVerticalSpan() == 0) {
                    continue;
                }

                // add missing cells
                while (colnr < cell.getColumnNumber()) {
                    if (!row.getCell(colnr).isMerged()) {
                        out.format(Locale.ROOT, "        <td></td>\n");
                    }
                    colnr++;
                }

                CellStyle style = cell.getCellStyle();

                out.format(Locale.ROOT, "        <td");
                writeAttribute(out, "colspan", cell, Cell::getHorizontalSpan, v -> v > 1, Object::toString);
                writeAttribute(out, "rowspan", cell, Cell::getVerticalSpan, v -> v > 1, Object::toString);
                if (!style.equals(defaultCellStyle)) {
                    writeAttribute(out, "class", id(style));
                }
                out.format(Locale.ROOT, ">");

                Optional<URI> hyperlink = cell.getHyperlink();
                hyperlink.ifPresent(link -> out.format(Locale.ROOT, "<a href=\"%s\">", baseUri.map(base -> base.relativize(link)).orElse(link)));
                out.format(Locale.ROOT, "%s", cell.getAsText(locale));
                hyperlink.ifPresent(link -> out.format(Locale.ROOT, "</a>"));

                out.format(Locale.ROOT, "</td>\n");
                colnr += cell.getHorizontalSpan();
            }

            updateProgress.accept((double) processedRows / totalRows);

            out.format(Locale.ROOT, "    </tr>\n");

            processedRows++;
        }

        out.format(Locale.ROOT, "    </tbody>\n");
        out.format(Locale.ROOT, "  </table>\n");

        // close DIV for sheet
        out.format(Locale.ROOT, "</div>\n");
        return processedRows;
    }

    private static <T> void writeAttribute(Formatter out, String attribute, Cell cell, Function<Cell, ? extends T> getter, Predicate<? super T> condition, Function<? super T, String> formatter) {
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
        write(workbook, out, IoOptions.getLocale(options), updateProgress);
    }

    /**
     * Write to a Formatter.
     *
     * @param workbook the workbook to write
     * @param out      the writer to write the workbook to
     * @param locale   the locale to use (i.e. when formatting cell contents such as numbers)
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
     * @param locale         the locale to use (i.e. when formatting cell contents such as numbers)
     * @param updateProgress callback for progress updates
     * @throws IOException if an input/output error occurs
     */
    public void write(Workbook workbook, Formatter out, Locale locale, DoubleConsumer updateProgress) throws IOException {
        writeSheets(workbook, out, locale, updateProgress);
    }

    /**
     * Write to OutputStream.
     *
     * @param workbook       the workbook to write
     * @param out            the OutputStream to write the workbook to
     * @param locale         the locale to use (i.e. when formatting cell contents such as numbers)
     * @param updateProgress callback for progress updates
     * @throws IOException if an input/output error occurs
     */
    public void write(Workbook workbook, OutputStream out, Locale locale, DoubleConsumer updateProgress) throws IOException {
        try (Formatter fmt = new Formatter(out, StandardCharsets.UTF_8.name())) {
            writeHtmlHeaderStart(fmt);
            writeCss(fmt, workbook);
            writeHtmlHeaderEnd(fmt);
            writeSheets(workbook, fmt, locale, updateProgress);
            writeHtmlFooter(fmt);
        }
    }

    /**
     * Write the start of the HTML header.
     *
     * @param out the Formatter to write the HTML header to
     */
    public void writeHtmlHeaderStart(Formatter out) {
        out.format(Locale.ROOT, "<html>\n<head>\n  <meta charset=\"utf-8\">\n");
    }

    /**
     * Write the end of the HTML header.
     *
     * @param out the Formatter to write the HTML header to
     */
    public void writeHtmlHeaderEnd(Formatter out) {
        out.format(Locale.ROOT, """
                </head>
                <body>
                  <script>
                  function mejaShowTab(evt, tabName) {
                    let i, tabs, tablinks;
                    tabs = document.getElementsByClassName("meja-tab");
                    for (i = 0; i < tabs.length; i++) {
                      tabs[i].style.display = "none";
                    }
                    tablinks = document.getElementsByClassName("meja-tablink");
                    for (i = 0; i < tablinks.length; i++) {
                      tablinks[i].className = tablinks[i].className.replace(" active", "");
                    }
                    document.getElementById(tabName).style.display = "block";
                    evt.currentTarget.className += " active";
                  }
                  </script>
                """);
    }

    /**
     * Write the end of the HTML footer.
     *
     * @param out the Formatter to write the HTML footer to
     */
    public void writeHtmlFooter(Formatter out) {
        out.format(Locale.ROOT, "</body>\n</html>\n");
    }

    /**
     * Write the CSS styles for the given Workbook to the provided Formatter.
     *
     * @param out the Formatter to write the CSS styles to
     * @param workbook the Workbook containing the cell styles
     */
    public void writeCss(Formatter out, Workbook workbook) {
        out.format(Locale.ROOT, "  <style>\n");
        writeCommonCss(out, workbook.getDefaultCellStyle());
        workbook.cellStyles().forEach(cs -> writeCellStyle(out, cs));
        out.format(Locale.ROOT, "  </style>\n");
    }

    /**
     * Write the common CSS styles for the given CellStyle to the provided Formatter.
     *
     * @param out the Formatter to write the CSS styles to
     * @param defaultCellStyle the default cell style
     */
    private void writeCommonCss(Formatter out, CellStyle defaultCellStyle) {
        out.format(Locale.ROOT, """
                .meja-tabbar {
                  overflow: hidden;
                  border: 1px solid #ccc;
                  background-color: #f1f1f1;
                }
                .meja-tabbar button {
                  background-color: inherit;
                  float: left;
                  border: none;
                  outline: none;
                  cursor: pointer;
                  padding: 14px 16px;
                  transition: 0.3s;
                }
                .meja-tabbar button:hover {
                  background-color: #ddd;
                }
                .meja-tabbar button.active {
                  background-color: #ccc;
                }
                .meja-tab {
                  display: none;
                  padding: 6px 12px;
                  border: 1px solid #ccc;
                  border-top: none;
                }
                table.meja-sheet {
                  border-collapse: collapse;
                  table-layout: fixed;
                  padding: 3px;
                  white-space: pre;
                  overflow: visible;
                """ + " "
        );
        writeCellStyleAttributes(out, defaultCellStyle);
        out.format(Locale.ROOT, """
                                
                }
                table.meja-sheet td,th {
                  border: 1px solid #d4d4d4;
                  max-width: 0;
                  max-height: 0;
                }
                table.meja-sheet a {
                  color: inherit;
                }
                table.meja-sheet td:empty::after{
                  content: "\\00a0";
                }
                """);
    }

    /**
     * Write the CSS styles for a single sheet to the provided Formatter.
     *
     * @param out the Formatter to write the CSS styles to
     * @param sheet the sheet for which to write the CSS styles
     */
    public void writeCssForSingleSheet(Formatter out, Sheet sheet) {
        // determine styles used in this sheet
        out.format(Locale.ROOT, "  <style>\n");

        // write common styles
        writeCommonCss(out, sheet.getWorkbook().getDefaultCellStyle());

        // write user defined styles in sorted order to get reproducible results (i.e. in unit tests)
        SortedMap<String, CellStyle> styles = new TreeMap<>();
        sheet.rows().forEach(row -> row.cells().map(Cell::getCellStyle).forEach(s -> styles.putIfAbsent(s.getName(), s)));
        styles.values().forEach(cs -> writeCellStyle(out, cs));

        out.format(Locale.ROOT, "  </style>\n");
    }

}
