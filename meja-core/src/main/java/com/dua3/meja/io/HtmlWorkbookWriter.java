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
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * HtmlWorkbookWriter is a class that implements the WorkbookWriter interface.
 * It is used to write a workbook as HTML.
 */
public final class HtmlWorkbookWriter implements WorkbookWriter {

    private final Object lock = new Object();

    private String workbookId = "";
    private Arguments options = Arguments.empty();

    private void generateNewWorkbookId() {
        // generate UUID
        UUID uuid = UUID.randomUUID();

        // convert to BASE64 encoded String
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        // remove padding
        workbookId = "W" + HexFormat.of().formatHex(buffer.array());
    }

    private HtmlWorkbookWriter() {
    }

    private enum Display {
        BLOCK,
        NONE;

        private final String value;

        Display() {
            this.value = name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Create instance.
     *
     * @return instance of {@code HtmlWorkbookWriter}
     */
    public static HtmlWorkbookWriter create() {
        return new HtmlWorkbookWriter();
    }

    private void writeSheets(Workbook workbook, Formatter out, Locale locale, DoubleConsumer updateProgress) {
        long totalRows = 0;
        out.format("<div class=\"meja-tabbar\">\n");
        for (Sheet sheet : workbook) {
            boolean isActive = sheet == sheet.getWorkbook().getCurrentSheet().orElse(null);

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
            Display display = (workbook.getSheetCount() < 2) || (sheet == workbook.getCurrentSheet().orElse(null))
                    ? Display.BLOCK : Display.NONE;
            processedRows = writeSheet(sheet, out, locale, totalRows, processedRows, updateProgress, display);
        }
    }

    private void writeCellStyle(Formatter out, CellStyle cs) {
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
     * Write a single sheet as HTML.
     * <p>
     * <em>NOTE:</em> This method does not add HTML header and body tags.
     *
     * @param sheet  the sheet to write
     * @param out    the Formatter to write to
     * @param locale the locale to use
     */
    private void writeSheet(Sheet sheet, Formatter out, Locale locale) {
        writeSheet(sheet, out, locale, sheet.getRowCount(), 0L, p -> {}, Display.BLOCK);
    }

    private String id(CellStyle style) {
        return workbookId
                + "_CS"
                + HexFormat.of().formatHex(style.getName().getBytes(StandardCharsets.UTF_8));
    }

    private String id(Sheet sheet) {
        return workbookId
                + "_S"
                + HexFormat.of().formatHex(sheet.getSheetName().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Write a sheet as HTML.
     * <p>
     * <em>NOTE:</em> This method does not add HTML header and body tags.
     *
     * @param sheet          the sheet to write
     * @param out            the Formatter to write to
     * @param locale         the locale to use
     * @param totalRows      the total number of rows in the sheet
     * @param processedRows  the number of rows already processed
     * @param updateProgress a DoubleConsumer that updates the progress (value between 0 and 1)
     * @param display        the {@link Display} mode to use
     * @return the number of rows processed after writing the sheet
     */
    private long writeSheet(Sheet sheet, Formatter out, Locale locale, long totalRows, long processedRows, DoubleConsumer updateProgress, Display display) {
        Optional<URI> baseUri = sheet.getWorkbook().getUri().map(uri -> uri.resolve(""));

        // open DIV for sheet
        String sheetId = id(sheet);

        String cls = display == Display.BLOCK ? "meja-tab active" : "meja-tab";

        out.format(Locale.ROOT, "<div id=\"%s\" class=\"%s\" style=\"display: %s\">\n", sheetId, cls, display);

        out.format(Locale.ROOT, "  <table class=\"meja-sheet\">\n");

        CellStyle defaultCellStyle = sheet.getWorkbook().getDefaultCellStyle();

        // write column widths
        writeColumnWidths(sheet, out);

        out.format(Locale.ROOT, "    <tbody>\n");

        int lastRownr = 0;
        for (Row row : sheet) {
            int nextRowNr = row.getRowNumber();
            addMissingRows(sheet, out, lastRownr, nextRowNr);
            writeRow(sheet, out, locale, row, defaultCellStyle, baseUri.orElse(null));

            processedRows += nextRowNr - lastRownr;
            lastRownr = nextRowNr;

            updateProgress.accept((double) processedRows / totalRows);
        }

        out.format(Locale.ROOT, "    </tbody>\n");
        out.format(Locale.ROOT, "  </table>\n");

        // close DIV for sheet
        out.format(Locale.ROOT, "</div>\n");
        return processedRows;
    }

    /**
     * Adds missing rows to the HTML representation of a sheet. This method generates
     * HTML rows for the range of row numbers between {@code lastRowNr} and {@code nextRowNr}
     * (both exclusive) and appends them to the provided {@code Formatter}.
     *
     * @param sheet     the {@link Sheet} from which row height and column count are derived
     * @param out       the {@link Formatter} to which the HTML representation of missing rows is written
     * @param lastRowNr the number of the last row that has been processed
     * @param nextRowNr the number of the next row that needs to be processed
     */
    private static void addMissingRows(Sheet sheet, Formatter out, int lastRowNr, int nextRowNr) {
        while (++lastRowNr < nextRowNr) {
            out.format(Locale.ROOT, "      <tr style=\"height: %.2fpt;\">\n", sheet.getRowHeight(lastRowNr));
            for (int i = 0; i < sheet.getColumnCount(); i++) {
                out.format(Locale.ROOT, "        <td></td>\n");
            }
            out.format(Locale.ROOT, "      </tr>\n");
        }
    }

    /**
     * Writes a row from a spreadsheet into an output stream in an HTML table row format.
     *
     * @param sheet The sheet containing the row to be written. Used for retrieving row-specific properties.
     * @param out The {@link Formatter} used to write formatted output data.
     * @param locale The {@link Locale} used for formatting cell content and any locale-sensitive features.
     * @param row The specific row from the sheet to be converted to an HTML representation.
     * @param defaultCellStyle The default cell style to compare against when setting custom styles for table cells.
     * @param baseUri An {@link Optional} containing the base URI used to resolve relative hyperlinks for the row's cells.
     */
    private void writeRow(Sheet sheet, Formatter out, Locale locale, Row row, CellStyle defaultCellStyle, @Nullable URI baseUri) {
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
            hyperlink.ifPresent(link -> out.format(Locale.ROOT, "<a href=\"%s\">", LangUtil.mapNonNullOrElse(baseUri, base -> base.relativize(link), link)));
            out.format(Locale.ROOT, "%s", cell.getAsText(locale));
            hyperlink.ifPresent(link -> out.format(Locale.ROOT, "</a>"));

            out.format(Locale.ROOT, "</td>\n");
            colnr += cell.getHorizontalSpan();
        }

        out.format(Locale.ROOT, "    </tr>\n");
    }

    private static void writeColumnWidths(Sheet sheet, Formatter out) {
        out.format(Locale.ROOT, "    <colgroup>\n");
        for (int j = 0; j < sheet.getColumnCount(); j++) {
            out.format(Locale.ROOT, "<col style=\"width: %.2fpt;\">\n", sheet.getColumnWidth(j));
        }
        out.format(Locale.ROOT, "    </colgroup>\n");
    }

    private static <T> void writeAttribute(Formatter out, String attribute, Cell cell, Function<? super Cell, ? extends T> getter, Predicate<? super T> condition, Function<? super T, String> formatter) {
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
        write(workbook, out, options.getOrThrow(IoOptions.OPTION_LOCALE), updateProgress);
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
        try {
            writeSheets(workbook, out, locale, p -> {});
        } catch (UncheckedIOException e) {
            //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
            throw e.getCause();
        }
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
        synchronized (lock) {
            try {
                writeSheets(workbook, out, locale, updateProgress);
            } catch (UncheckedIOException e) {
                //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
                throw e.getCause();
            }
        }
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
        synchronized (lock) {
            generateNewWorkbookId();
            try (Formatter fmt = new Formatter(out, StandardCharsets.UTF_8.name())) {
                writeHtmlHeaderStart(fmt);
                writeCss(fmt, workbook);
                writeHtmlHeaderEnd(fmt);
                writeSheets(workbook, fmt, locale, updateProgress);
                writeHtmlFooter(fmt);
            } finally {
                workbookId = "";
            }
        }
    }

    /**
     * Exports a single sheet as HTML.
     * The exported content includes the sheet's HTML representation, CSS styles,
     * and footer, ensuring proper formatting of the sheet in a web-compatible format.
     *
     * @param fmt   the {@code Formatter} to write the HTML output to
     * @param sheet the sheet to be exported as HTML
     */
    public void exportSingleSheet(Formatter fmt, Sheet sheet) {
        doExportSheet(fmt, sheet, true);
    }

    /**
     * Exports a single sheet as HTML without including the HTML header and body tags.
     * This method is designed for scenarios where the HTML header and body structure
     * are managed externally or not required.
     *
     * @param fmt   the {@code Formatter} used to write the HTML output
     * @param sheet the sheet to export as HTML
     */
    public void exportSingleSheetWithoutHtmlHeader(Formatter fmt, Sheet sheet) {
        doExportSheet(fmt, sheet, false);
    }

    /**
     * Exports selected sheets from a workbook to an HTML format. The exported content includes
     * the sheets' HTML representation, CSS styles, and footer, ensuring proper formatting
     * for web display. Only sheets accepted by the given predicate will be exported.
     *
     * @param fmt       the {@code Formatter} to write the HTML output to
     * @param workbook  the workbook containing the sheets to export
     * @param predicate a {@code Predicate} used to filter which sheets to export
     */
    public void exportSheets(Formatter fmt, Workbook workbook, Predicate<Sheet> predicate) {
        synchronized (lock) {
            generateNewWorkbookId();
            try {
                List<Sheet> sheets = workbook.sheets()
                        .filter(predicate)
                        .toList();

                writeHtmlHeaderStart(fmt);
                AtomicBoolean first = new AtomicBoolean(true);
                sheets.forEach(sheet -> writeCssForSingleSheet(fmt, sheet, first.getAndSet(false)));
                writeHtmlHeaderEnd(fmt);
                sheets.forEach(sheet -> writeSheet(sheet, fmt, Locale.ROOT));
                writeHtmlFooter(fmt);
            } finally {
                workbookId = "";
            }
        }
    }

    private void doExportSheet(Formatter fmt, Sheet sheet, boolean writeHtmlHeader) {
        synchronized (lock) {
            generateNewWorkbookId();
            try {
                if (writeHtmlHeader) {
                    writeHtmlHeaderStart(fmt);
                    writeCssForSingleSheet(fmt, sheet, true);
                    writeHtmlHeaderEnd(fmt);
                }
                writeSheet(sheet, fmt, Locale.ROOT);
                if (writeHtmlHeader) {
                    writeHtmlFooter(fmt);
                }
            } finally {
                workbookId = "";
            }
        }
    }

    /**
     * Write the start of the HTML header.
     *
     * @param out the Formatter to write the HTML header to
     */
    private static void writeHtmlHeaderStart(Formatter out) {
        out.format(Locale.ROOT, "<html>\n<head>\n  <meta charset=\"utf-8\">\n");
    }

    /**
     * Write the end of the HTML header.
     *
     * @param out the Formatter to write the HTML header to
     */
    private static void writeHtmlHeaderEnd(Formatter out) {
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
    private static void writeHtmlFooter(Formatter out) {
        out.format(Locale.ROOT, "</body>\n</html>\n");
    }

    /**
     * Write the CSS styles for the given Workbook to the provided Formatter.
     *
     * @param out the Formatter to write the CSS styles to
     * @param workbook the Workbook containing the cell styles
     */
    private void writeCss(Formatter out, Workbook workbook) {
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
    private static void writeCommonCss(Formatter out, CellStyle defaultCellStyle) {
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
    private void writeCssForSingleSheet(Formatter out, Sheet sheet, boolean writeCommonStyles) {
        // determine styles used in this sheet
        out.format(Locale.ROOT, "  <style>\n");

        // write common styles
        if (writeCommonStyles) {
            writeCommonCss(out, sheet.getWorkbook().getDefaultCellStyle());
        }

        // write user defined styles in sorted order to get reproducible results (i.e. in unit tests)
        SortedMap<String, CellStyle> styles = new TreeMap<>();
        sheet.rows().forEach(row -> row.cells().map(Cell::getCellStyle).forEach(s -> styles.putIfAbsent(s.getName(), s)));
        styles.values().forEach(cs -> writeCellStyle(out, cs));

        out.format(Locale.ROOT, "  </style>\n");
    }

}
