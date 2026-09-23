package com.dua3.meja.model.poi;

import com.dua3.meja.model.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PoiWorkbookFactoryTest {

    @Test
    void testCreateVariants() {
        PoiWorkbookFactory factory = PoiWorkbookFactory.instance();

        PoiWorkbook defaultWb = factory.create();
        assertNotNull(defaultWb);
        assertTrue(defaultWb instanceof PoiWorkbook.PoiXssfWorkbook);

        PoiWorkbook streamingWb = factory.createStreaming();
        assertNotNull(streamingWb);
        assertTrue(streamingWb instanceof PoiWorkbook.PoiXssfWorkbook);

        PoiWorkbook xlsWb = factory.createXls();
        assertNotNull(xlsWb);
        assertTrue(xlsWb instanceof PoiWorkbook.PoiHssfWorkbook);

        PoiWorkbook xlsxWb = factory.createXlsx();
        assertNotNull(xlsxWb);
        assertTrue(xlsxWb instanceof PoiWorkbook.PoiXssfWorkbook);

        PoiWorkbook xlsxStreamingWb = factory.createXlsxStreaming();
        assertNotNull(xlsxStreamingWb);
        assertTrue(xlsxStreamingWb instanceof PoiWorkbook.PoiXssfWorkbook);
    }

    @Test
    void testCopyOf() {
        PoiWorkbookFactory factory = PoiWorkbookFactory.instance();
        PoiWorkbook original = factory.createXls();
        original.createSheet("S1").getRow(0).getCell(0).set("POI Copy");

        PoiWorkbook copy = factory.copyOf(original);
        assertEquals(1, copy.getSheetCount());
        assertEquals("S1", copy.getSheet(0).getSheetName());
        assertEquals("POI Copy", copy.getSheet(0).getRow(0).getCell(0).getText().toString());
    }

    @Test
    void testOpenXlsAndXlsx(@TempDir Path tempDir) throws IOException {
        PoiWorkbookFactory factory = PoiWorkbookFactory.instance();

        // Save and reopen XLSX
        PoiWorkbook xlsxWb = factory.createXlsx();
        xlsxWb.createSheet("SheetX").getRow(0).getCell(0).set("XLSX Test");
        Path xlsxFile = tempDir.resolve("test.xlsx");
        try (var out = Files.newOutputStream(xlsxFile)) {
            xlsxWb.getPoiWorkbook().write(out);
        }

        Workbook openedXlsx = factory.open(xlsxFile.toUri());
        assertNotNull(openedXlsx);
        assertEquals("XLSX Test", openedXlsx.getSheet(0).getRow(0).getCell(0).getText().toString());

        // Save and reopen XLS
        PoiWorkbook xlsWb = factory.createXls();
        xlsWb.createSheet("SheetH").getRow(0).getCell(0).set("XLS Test");
        Path xlsFile = tempDir.resolve("test.xls");
        try (var out = Files.newOutputStream(xlsFile)) {
            xlsWb.getPoiWorkbook().write(out);
        }

        Workbook openedXls = factory.open(xlsFile.toUri());
        assertNotNull(openedXls);
        assertEquals("XLS Test", openedXls.getSheet(0).getRow(0).getCell(0).getText().toString());
    }
}
