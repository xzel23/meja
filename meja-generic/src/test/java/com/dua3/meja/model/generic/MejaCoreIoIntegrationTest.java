package com.dua3.meja.model.generic;

import com.dua3.meja.io.CsvWorkbookReader;
import com.dua3.meja.io.CsvWorkbookWriter;
import com.dua3.meja.io.HtmlWorkbookWriter;
import com.dua3.meja.io.SheetRowBuilder;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.util.CellValueHelper;
import com.dua3.utility.io.IoOptions;
import com.dua3.utility.io.PredefinedDateTimeFormat;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MejaCoreIoIntegrationTest {

    @Test
    void testCellValueHelper() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S1");
        Row row = sheet.getRow(0);

        CellValueHelper helper = new CellValueHelper(NumberFormat.getNumberInstance(Locale.US), DateTimeFormatter.ISO_LOCAL_DATE);

        Cell c0 = row.getCell(0);
        helper.setCellValue(c0, "123.45");
        assertEquals(CellType.NUMERIC, c0.getCellType());
        assertEquals(123.45, c0.getNumber().doubleValue(), 0.001);

        Cell c1 = row.getCell(1);
        helper.setCellValue(c1, "2023-11-25");
        assertEquals(CellType.DATE_TIME, c1.getCellType());

        Cell c2 = row.getCell(2);
        helper.setCellValue(c2, "true");
        assertEquals(CellType.BOOLEAN, c2.getCellType());
        assertTrue(c2.getBoolean());

        Cell c3 = row.getCell(3);
        helper.setCellValue(c3, "=SUM(A1:B1)");
        assertEquals(CellType.FORMULA, c3.getCellType());
        assertEquals("SUM(A1:B1)", c3.getFormula());

        Cell c4 = row.getCell(4);
        helper.setCellValue(c4, "");
        assertEquals(CellType.BLANK, c4.getCellType());
    }

    @Test
    void testSheetRowBuilder() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S1");

        Arguments options = Arguments.of(
                Arguments.createEntry(IoOptions.OPTION_LOCALE, Locale.US),
                Arguments.createEntry(IoOptions.OPTION_DATE_TIME_FORMAT, PredefinedDateTimeFormat.ISO_DATE_TIME)
        );

        SheetRowBuilder builder = new SheetRowBuilder(sheet, options);
        builder.startRow();
        builder.add("100");
        builder.add("Text");
        builder.endRow();

        assertEquals(1, sheet.getRowCount());
        assertEquals(100.0, sheet.getRow(0).getCell(0).getNumber().doubleValue());
        assertEquals("Text", sheet.getRow(0).getCell(1).getText().toString());
    }

    @Test
    void testCsvWorkbookReaderAndWriter(@TempDir Path tempDir) throws IOException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet s1 = wb.createSheet("Sheet1");
        s1.getRow(0).getCell(0).set("Header1");
        s1.getRow(0).getCell(1).set("Header2");
        s1.getRow(1).getCell(0).set(100);
        s1.getRow(1).getCell(1).set("Value2");

        CsvWorkbookWriter writer = CsvWorkbookWriter.create();
        Path csvFile = tempDir.resolve("out.csv");
        writer.write(wb, csvFile.toUri());

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertTrue(content.contains("Header1"));
        assertTrue(content.contains("100"));

        CsvWorkbookReader reader = CsvWorkbookReader.create();
        GenericWorkbook loaded = reader.read(GenericWorkbookFactory.instance(), csvFile.toUri());
        assertNotNull(loaded);
        assertEquals(1, loaded.getSheetCount());
        assertEquals("Header1", loaded.getSheet(0).getRow(0).getCell(0).getText().toString());
    }

    @Test
    void testHtmlWorkbookWriter() throws IOException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet s1 = wb.createSheet("Sheet1");
        s1.getRow(0).getCell(0).set("Hello World");

        HtmlWorkbookWriter writer = HtmlWorkbookWriter.create();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.write(wb, baos);

        String html = baos.toString(StandardCharsets.UTF_8);
        assertTrue(html.contains("Hello World"));
        assertTrue(html.contains("<html"));
        assertTrue(html.contains("</html>"));
    }
}
