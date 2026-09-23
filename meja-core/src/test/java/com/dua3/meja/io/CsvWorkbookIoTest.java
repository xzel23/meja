package com.dua3.meja.io;

import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.io.IoOptions;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CsvWorkbookIoTest {

    @Test
    void testCsvWriterSingleAndMultiSheet() throws IOException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet s1 = wb.createSheet("SheetA");
        s1.getCell(0, 0).set("A1");
        s1.getCell(0, 1).set(100);

        CsvWorkbookWriter writer = CsvWorkbookWriter.create();
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        writer.write(wb, bw);
        bw.flush();

        String singleSheetCsv = sw.toString();
        assertFalse(singleSheetCsv.contains("!SheetA!"));
        assertTrue(singleSheetCsv.contains("A1"));
        assertTrue(singleSheetCsv.contains("100"));

        GenericSheet s2 = wb.createSheet("SheetB");
        s2.getCell(0, 0).set("B1");

        StringWriter swMulti = new StringWriter();
        BufferedWriter bwMulti = new BufferedWriter(swMulti);
        AtomicBoolean progressReported = new AtomicBoolean(false);
        writer.write(wb, bwMulti, p -> progressReported.set(true));
        bwMulti.flush();

        String multiSheetCsv = swMulti.toString();
        assertTrue(progressReported.get());
        assertTrue(multiSheetCsv.contains("!SheetA!"));
        assertTrue(multiSheetCsv.contains("!SheetB!"));
        assertTrue(multiSheetCsv.contains("B1"));
    }

    @Test
    void testWorkbookWriterDefaultMethods(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws IOException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet s = wb.createSheet("WBSheet");
        s.getCell(0, 0).set("Hello");

        WorkbookWriter writer = CsvWorkbookWriter.create();
        java.nio.file.Path outPath = tempDir.resolve("out_ww.csv");

        writer.write(wb, outPath.toUri());
        assertTrue(java.nio.file.Files.exists(outPath));

        java.nio.file.Path outPathProg = tempDir.resolve("out_ww_prog.csv");
        writer.write(wb, outPathProg.toUri(), p -> {});
        assertTrue(java.nio.file.Files.exists(outPathProg));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.write(wb, baos);
        assertTrue(baos.toString(StandardCharsets.UTF_8).contains("Hello"));

        ByteArrayOutputStream baosProg = new ByteArrayOutputStream();
        writer.write(wb, baosProg, p -> {});
        assertTrue(baosProg.toString(StandardCharsets.UTF_8).contains("Hello"));
    }

    @Test
    void testCsvWriterEmptyWorkbook() throws IOException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        CsvWorkbookWriter writer = CsvWorkbookWriter.create();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AtomicBoolean progressReported = new AtomicBoolean(false);
        writer.write(wb, baos, p -> progressReported.set(true));

        assertTrue(progressReported.get());
    }

    @Test
    void testCsvReaderFromBufferedReaderAndInputStream() throws IOException {
        String csvContent = "Col1,Col2\nVal1,123.45\n";

        CsvWorkbookReader reader = CsvWorkbookReader.create();
        reader.setOptions(Arguments.of(Arguments.createEntry(IoOptions.OPTION_FIELD_SEPARATOR, ',')));

        try (BufferedReader br = new BufferedReader(new StringReader(csvContent));
             GenericWorkbook wb1 = reader.read(GenericWorkbookFactory.instance(), br, URI.create("buffered.csv"))) {
            assertEquals(1, wb1.getSheetCount());
            assertEquals("Val1", wb1.getSheet(0).getRow(1).getCell(0).getText().toString());
            assertEquals(123.45, wb1.getSheet(0).getRow(1).getCell(1).getNumber().doubleValue());
        }

        try (ByteArrayInputStream in = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
             GenericWorkbook wb2 = reader.read(GenericWorkbookFactory.instance(), URI.create("stream.csv"), in)) {
            assertEquals(1, wb2.getSheetCount());
            assertEquals("Val1", wb2.getSheet(0).getRow(1).getCell(0).getText().toString());
            assertEquals(123.45, wb2.getSheet(0).getRow(1).getCell(1).getNumber().doubleValue());
        }
    }
}
