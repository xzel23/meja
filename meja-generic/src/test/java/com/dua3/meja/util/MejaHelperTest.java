package com.dua3.meja.util;

import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.io.ReadableObjectStore;
import com.dua3.utility.io.imp.FileObjectStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MejaHelperTest {

    @Test
    void testPrintTableBasic() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("TestSheet");
        Row r0 = sheet.getRow(0);
        r0.getCell(0).set("Name");
        r0.getCell(1).set("Score");

        Row r1 = sheet.getRow(1);
        r1.getCell(0).set("Alice");
        r1.getCell(1).set(100);

        StringWriter sw = new StringWriter();
        MejaHelper.printTable(sw, sheet, Locale.US,
                MejaHelper.PrintOptions.DRAW_LINES,
                MejaHelper.PrintOptions.LINE_ABOVE,
                MejaHelper.PrintOptions.FIRST_LINE_IS_HEADER,
                MejaHelper.PrintOptions.LINE_BELOW,
                MejaHelper.PrintOptions.PREPEND_SHEET_NAME);

        String result = sw.toString();
        assertTrue(result.contains("TestSheet"));
        assertTrue(result.contains("Name"));
        assertTrue(result.contains("Score"));
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("|"));
        assertTrue(result.contains("-"));
        assertTrue(result.contains("+"));
    }

    @Test
    void testPrintTableNoLines() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("NoLines");
        Row r0 = sheet.getRow(0);
        r0.getCell(0).set("Item");
        r0.getCell(1).set("Qty");

        Row r1 = sheet.getRow(1);
        r1.getCell(0).set("Apple");
        r1.getCell(1).set(5);

        StringWriter sw = new StringWriter();
        MejaHelper.printTable(sw, sheet, Locale.US); // No options -> spaces instead of pipe/dash

        String result = sw.toString();
        assertFalse(result.contains("|"));
        assertTrue(result.contains("Item"));
        assertTrue(result.contains("Qty"));
        assertTrue(result.contains("Apple"));
        assertTrue(result.contains("5"));
    }

    @Test
    void testPrintTableMultilineCells() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("MultiLine");
        Row r0 = sheet.getRow(0);
        r0.getCell(0).set("Multi\nLine\nText");
        r0.getCell(1).set(42);

        StringWriter sw = new StringWriter();
        MejaHelper.printTable(sw, sheet, Locale.US, MejaHelper.PrintOptions.DRAW_LINES);

        String result = sw.toString();
        assertTrue(result.contains("Multi"));
        assertTrue(result.contains("Line"));
        assertTrue(result.contains("Text"));
        assertTrue(result.contains("42"));
    }

    @Test
    void testPrintTableEmptySheet() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Empty");

        StringWriter sw = new StringWriter();
        MejaHelper.printTable(sw, sheet, Locale.US,
                MejaHelper.PrintOptions.DRAW_LINES,
                MejaHelper.PrintOptions.LINE_ABOVE,
                MejaHelper.PrintOptions.LINE_BELOW,
                MejaHelper.PrintOptions.PREPEND_SHEET_NAME);

        String result = sw.toString();
        assertTrue(result.contains("Empty"));
    }

    @Test
    void testOpenWorkbookFromPathAndUri(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, "A,B\n1,2\n");

        // Test openWorkbook(Path)
        try (Workbook wbFromPath = MejaHelper.openWorkbook(csvFile)) {
            assertNotNull(wbFromPath);
            assertEquals(1, wbFromPath.getSheetCount());
            Sheet s = wbFromPath.getSheet(0);
            assertEquals("A", s.getRow(0).getCell(0).getText().toString());
            assertEquals(1.0, s.getRow(1).getCell(0).getNumber().doubleValue());
        }

        // Test openWorkbook(URI)
        URI uri = csvFile.toUri();
        try (Workbook wbFromUri = MejaHelper.openWorkbook(uri)) {
            assertNotNull(wbFromUri);
            assertEquals(1, wbFromUri.getSheetCount());
            Sheet s = wbFromUri.getSheet(0);
            assertEquals("B", s.getRow(0).getCell(1).getText().toString());
            assertEquals(2.0, s.getRow(1).getCell(1).getNumber().doubleValue());
        }
    }

    @Test
    void testOpenWorkbookFromObjectStore(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("stored.csv");
        Files.writeString(csvFile, "Col1,Col2\nHello,World\n");

        try (ReadableObjectStore store = FileObjectStore.newReadableObjectStore(tempDir);
             Workbook wb = MejaHelper.openWorkbook(store, URI.create("stored.csv"))) {
            assertNotNull(wb);
            assertEquals(1, wb.getSheetCount());
            assertEquals("Hello", wb.getSheet(0).getRow(1).getCell(0).getText().toString());
        }
    }

    @Test
    void testOpenWorkbookExceptions(@TempDir Path tempDir) throws IOException {
        Path nonExistent = tempDir.resolve("non_existent.csv");
        assertThrows(IOException.class, () -> MejaHelper.openWorkbook(nonExistent));
        assertThrows(IOException.class, () -> MejaHelper.openWorkbook(nonExistent.toUri()));

        try (ReadableObjectStore store = FileObjectStore.newReadableObjectStore(tempDir)) {
            assertThrows(IOException.class, () -> MejaHelper.openWorkbook(store, URI.create("non_existent.csv")));
        }
    }
}
