package com.dua3.meja.model.generic;

import com.dua3.meja.model.Workbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GenericWorkbookFactoryTest {

    @Test
    void testCreateAndStreaming() {
        GenericWorkbookFactory factory = GenericWorkbookFactory.instance();
        assertNotNull(factory);

        GenericWorkbook wb = factory.create();
        assertNotNull(wb);
        assertEquals(0, wb.getSheetCount());

        GenericWorkbook streamingWb = factory.createStreaming();
        assertNotNull(streamingWb);
    }

    @Test
    void testCopyOf() {
        GenericWorkbookFactory factory = GenericWorkbookFactory.instance();
        GenericWorkbook original = factory.create();
        original.createSheet("Sheet1").getRow(0).getCell(0).set("Data");

        GenericWorkbook copy = factory.copyOf(original);
        assertEquals(1, copy.getSheetCount());
        assertEquals("Sheet1", copy.getSheet(0).getSheetName());
        assertEquals("Data", copy.getSheet(0).getRow(0).getCell(0).getText().toString());
    }

    @Test
    void testOpen(@TempDir Path tempDir) throws IOException {
        GenericWorkbookFactory factory = GenericWorkbookFactory.instance();
        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, "col1,col2\nval1,val2\n", StandardCharsets.UTF_8);

        Workbook wb = factory.open(csvFile.toUri());
        assertNotNull(wb);
        assertEquals(1, wb.getSheetCount());
        assertEquals("val1", wb.getSheet(0).getRow(1).getCell(0).getText().toString());

        ByteArrayInputStream in = new ByteArrayInputStream("colA,colB\n1,2\n".getBytes(StandardCharsets.UTF_8));
        Workbook streamWb = factory.open(csvFile.toUri(), com.dua3.utility.options.Arguments.empty(), in);
        assertNotNull(streamWb);
    }
}
