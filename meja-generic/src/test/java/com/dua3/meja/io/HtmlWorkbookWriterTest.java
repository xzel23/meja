package com.dua3.meja.io;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.generic.GenericCellStyle;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.data.Color;
import com.dua3.utility.io.IoOptions;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.text.FontUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class HtmlWorkbookWriterTest {

    @Test
    void testExportMultiSheetWithTabs() throws IOException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        wb.setUri(URI.create("file:///test/workbook.html"));

        GenericSheet s1 = wb.createSheet("FirstSheet");
        s1.getCell(0, 0).set("Sheet 1 Data");

        GenericSheet s2 = wb.createSheet("SecondSheet");
        s2.getCell(0, 0).set("Sheet 2 Data");

        wb.setCurrentSheet(s1);

        HtmlWorkbookWriter writer = HtmlWorkbookWriter.create();
        writer.setOptions(Arguments.of(Arguments.createEntry(IoOptions.OPTION_LOCALE, Locale.GERMANY)));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AtomicBoolean progressCalled = new AtomicBoolean(false);
        writer.write(wb, baos, p -> progressCalled.set(true));

        String html = baos.toString(StandardCharsets.UTF_8);
        assertTrue(progressCalled.get());
        assertTrue(html.contains("<html lang=\"de\">"));
        assertTrue(html.contains("meja-tabbar"));
        assertTrue(html.contains("FirstSheet"));
        assertTrue(html.contains("SecondSheet"));
        assertTrue(html.contains("Sheet 1 Data"));
        assertTrue(html.contains("Sheet 2 Data"));
    }

    @Test
    void testExportSingleSheet() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Solo");
        sheet.getCell(0, 0).set("Solo Content");

        HtmlWorkbookWriter writer = HtmlWorkbookWriter.create();
        StringBuilder sb = new StringBuilder();
        try (Formatter formatter = new Formatter(sb, Locale.US)) {
            writer.exportSingleSheet(formatter, sheet);
        }

        String html = sb.toString();
        assertTrue(html.contains("Solo Content"));
    }

    @Test
    void testCellStylesAndFormatting() throws IOException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Styles");

        // Cell 0: Rotated text with positive angle
        Cell c0 = sheet.getCell(0, 0);
        c0.set("Rotated 45");
        GenericCellStyle style0 = wb.getCellStyle("RotatedPos");
        style0.setRotation((short) 45);
        style0.setFont(FontUtil.getInstance().getFont("Arial-12-bold"));
        style0.setFillPattern(FillPattern.SOLID);
        style0.setFillFgColor(Color.YELLOW);
        style0.setWrap(true);
        c0.setCellStyle(style0);

        // Cell 1: Rotated text with negative angle
        Cell c1 = sheet.getCell(0, 1);
        c1.set("Rotated -30");
        GenericCellStyle style1 = wb.getCellStyle("RotatedNeg");
        style1.setRotation((short) -30);
        c1.setCellStyle(style1);

        // Cell 2: All borders
        Cell c2 = sheet.getCell(0, 2);
        c2.set("Borders");
        GenericCellStyle style2 = wb.getCellStyle("Bordered");
        for (Direction d : Direction.values()) {
            style2.setBorderStyle(d, new BorderStyle(1.5f, Color.BLUE));
        }
        c2.setCellStyle(style2);

        // Cell 3: Number (right aligned)
        Cell c3 = sheet.getCell(0, 3);
        c3.set(12345.67);

        // Cell 4: Date
        Cell c4 = sheet.getCell(0, 4);
        c4.set(LocalDate.of(2024, 5, 20));

        // Cell 5: DateTime
        Cell c5 = sheet.getCell(0, 5);
        c5.set(LocalDateTime.of(2024, 5, 20, 10, 30));

        // Cell 6: Hyperlink
        Cell c6 = sheet.getCell(0, 6);
        c6.set("Click Here");
        c6.setHyperlink(URI.create("https://example.com"));

        // Cell 7: Multiline
        Cell c7 = sheet.getCell(0, 7);
        c7.set("Line 1\nLine 2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlWorkbookWriter.create().write(wb, baos, Locale.US, p -> {});

        String html = baos.toString(StandardCharsets.UTF_8);
        assertTrue(html.contains("rotate(-45deg)"));
        assertTrue(html.contains("transform-origin: bottom left;"));
        assertTrue(html.contains("rotate(30deg)"));
        assertTrue(html.contains("transform-origin: top left;"));
        assertTrue(html.contains("border-top: 1.50pt solid"));
        assertTrue(html.contains("background-color:"));
        assertTrue(html.contains("white-space: pre-wrap;"));
        assertTrue(html.contains("12345.67"));
        assertTrue(html.contains("https://example.com"));
        assertTrue(html.contains("Line 1\nLine 2"));
    }

    @Test
    void testMergedCellsHtmlExport() throws IOException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("MergedSheet");

        Cell tl = sheet.getCell(1, 1);
        tl.set("Merged Area");
        tl.merge(2, 3); // 2 columns wide, 3 rows high

        GenericCellStyle edgeStyle = wb.getCellStyle("EdgeBorder");
        edgeStyle.setBorderStyle(Direction.SOUTH, new BorderStyle(2.0f, Color.RED));
        sheet.getCell(3, 1).setCellStyle(edgeStyle);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlWorkbookWriter.create().write(wb, baos);

        String html = baos.toString(StandardCharsets.UTF_8);
        assertTrue(html.contains("colspan=\"2\""));
        assertTrue(html.contains("rowspan=\"3\""));
        assertTrue(html.contains("Merged Area"));
    }
}
