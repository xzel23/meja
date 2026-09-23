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

class XlsWorkbookWriterTest {

    @Test
    void testWriteToStream() throws IOException {
        PoiWorkbook wb = PoiWorkbookFactory.instance().createXls();
        wb.createSheet("Sheet1").getRow(0).getCell(0).set("XLS Writer Test");

        XlsWorkbookWriter writer = XlsWorkbookWriter.instance();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.write(wb, baos);

        assertTrue(baos.size() > 0);
    }

    @Test
    void testWriteToFile(@TempDir Path tempDir) throws IOException {
        PoiWorkbook wb = PoiWorkbookFactory.instance().createXls();
        wb.createSheet("Sheet1").getRow(0).getCell(0).set("XLS File Test");

        XlsWorkbookWriter writer = XlsWorkbookWriter.instance();
        Path file = tempDir.resolve("test_out.xls");
        writer.write(wb, file.toUri());

        assertTrue(Files.exists(file));
        assertTrue(Files.size(file) > 0);

        Workbook opened = PoiWorkbookFactory.instance().open(file.toUri());
        assertEquals("XLS File Test", opened.getSheet(0).getRow(0).getCell(0).getText().toString());
    }
}
