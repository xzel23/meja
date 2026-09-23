package com.dua3.meja.model.poi.io;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class XlsxWorkbookWriterTest {

    @Test
    void testWriteToStream() throws IOException {
        PoiWorkbook wb = PoiWorkbookFactory.instance().createXlsx();
        wb.createSheet("Sheet1").getRow(0).getCell(0).set("XLSX Writer Test");

        XlsxWorkbookWriter writer = XlsxWorkbookWriter.instance();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.write(wb, baos);

        assertTrue(baos.size() > 0);
    }

    @Test
    void testWriteToFile(@TempDir Path tempDir) throws IOException {
        PoiWorkbook wb = PoiWorkbookFactory.instance().createXlsx();
        wb.createSheet("Sheet1").getRow(0).getCell(0).set("XLSX File Test");

        XlsxWorkbookWriter writer = XlsxWorkbookWriter.instance();
        Path file = tempDir.resolve("test_out.xlsx");
        writer.write(wb, file.toUri());

        assertTrue(Files.exists(file));
        assertTrue(Files.size(file) > 0);

        Workbook opened = PoiWorkbookFactory.instance().open(file.toUri());
        assertEquals("XLSX File Test", opened.getSheet(0).getRow(0).getCell(0).getText().toString());
    }
}
