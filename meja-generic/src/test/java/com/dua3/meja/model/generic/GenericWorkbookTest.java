package com.dua3.meja.model.generic;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.io.FileTypeCsv;
import com.dua3.utility.io.IoOptions;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class GenericWorkbookTest {

    private static final Path testdataDir = IoUtil.toPath(GenericWorkbookTest.class.getResource("/"))
            .resolve("../../../../testdata").normalize();

    @Test
    void testCsv() throws Exception {
        testCountryWorkbook(testdataDir.resolve("population by country_US.csv"), Locale.US);
        testCountryWorkbook(testdataDir.resolve("population by country_DE.csv"), Locale.GERMANY);
    }

    @Test
    void testSaveAndReloadCsv() throws Exception {
        try (Workbook original = openWorkbookCsv(testdataDir.resolve("population by country_US.csv"), Locale.US)) {
            Path tempDir = Files.createTempDirectory("meja-test");
            try {
                Path pathToCopy = tempDir.resolve("population by country (copy).csv");
                original.write(pathToCopy, Arguments.of(
                        Arguments.createEntry(IoOptions.OPTION_FIELD_SEPARATOR, ';'),
                        Arguments.createEntry(IoOptions.OPTION_LOCALE, Locale.US)
                ));
                testCountryWorkbook(pathToCopy, Locale.US);
            } finally {
                IoUtil.deleteRecursive(tempDir);
            }
        }
    }

    private static String maskId(String s) {
        return s.replaceAll("W[a-z0-9]{32}_", "WB_********************************_");
    }

    @Test
    void testConvertToHtml() throws Exception {
        String[] files = {"population by country_US.csv"};
        Path tempDir = Files.createTempDirectory("meja-test");
        try {
            for (String inFileName : files) {
                Path inFile = testdataDir.resolve(inFileName);
                String outFileName = IoUtil.replaceExtension(inFileName, "html");
                Path refFile = testdataDir.resolve(outFileName);
                Path outFile = tempDir.resolve(outFileName);
                copyToHtml(inFile, outFile, Locale.US);
                assertLinesMatch(
                        maskId(Files.readString(refFile, StandardCharsets.UTF_8)).lines(),
                        maskId(Files.readString(outFile, StandardCharsets.UTF_8)).lines()
                );
            }
        } finally {
            IoUtil.deleteRecursive(tempDir);
        }
    }

    private static void copyToHtml(Path inFile, Path outFile, Locale locale) throws IOException {
        try (Workbook original = openWorkbookCsv(inFile, locale)) {
            original.write(outFile, Arguments.of(
                    Arguments.createEntry(IoOptions.OPTION_LOCALE, locale)
            ));
        }
    }

    private static void testCountryWorkbook(Path pathToWorkbook, Locale locale) throws IOException {
        try (Workbook wb = openWorkbookCsv(pathToWorkbook, locale)) {
            assertEquals(1, wb.getSheetCount());

            Sheet sheet = wb.getSheet(0);
            assertNotNull(sheet);

            sheet.splitAt(1, 1); // split position cannot be set in CSV files. set it here so that at least part of the functionlaity can be verified.

            assertEquals(1, sheet.getSplitRow());
            assertEquals(1, sheet.getSplitRow());

            Cell cChina = sheet.getCell(1, 0);
            assertNotNull(cChina);
            assertEquals("China", Objects.toString(cChina.getOrDefault(null)));
            assertEquals("China", Objects.toString(cChina.getText()));
            assertEquals("China", cChina.toString());

            // note that percent values are treated as zero in this simple test
            double[] sums = {0.0, 7_794_798_739.0, 0.0, 81_330_639.0, 111_806.0, 130_094_083.0, 1_263.0, 541.3, 6_152.0, 0.0, 0.0};

            for (int j = 0; j < sums.length; j++) {
                final int jj = j;
                double expected = sums[j];
                double actual = sheet.rows()
                        .map(row -> row.getCell(jj))
                        .mapToDouble(c -> c.getCellType() == CellType.NUMERIC ? c.getNumber().doubleValue() : 0.0)
                        .sum();
                assertEquals(expected, actual);
            }
        }
    }

    private static Workbook openWorkbookCsv(Path pathToWorkbook, Locale locale) throws IOException {
        return FileTypeCsv.instance()
                .read(
                        pathToWorkbook,
                        t -> Arguments.of(
                                Arguments.createEntry(IoOptions.OPTION_FIELD_SEPARATOR, ';'),
                                Arguments.createEntry(IoOptions.OPTION_LOCALE, locale)
                        )
                );
    }

    @Test
    void testRowGetLastColNumError() {
        Workbook wb = GenericWorkbookFactory.instance().create();
        testRowGetLastColNumErrorHelper(wb);
    }

    private static void testRowGetLastColNumErrorHelper(Workbook wb) {
        Sheet sheet = wb.createSheet("index");

        assertEquals(0, sheet.getRowCount());
        assertEquals(0, sheet.getColumnCount());

        sheet.createRow(1, 2, 3, 4);
        assertEquals(1, sheet.getRowCount());
        assertEquals(4, sheet.getColumnCount());

        sheet.createRow(1, 2, 3, 4);
    }

    @Test
    void testSheetManagement() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        assertEquals(0, wb.getSheetCount());
        assertFalse(wb.getCurrentSheet().isPresent());
        assertEquals(0, wb.getCurrentSheetIndex());

        GenericSheet s1 = wb.createSheet("Sheet1");
        GenericSheet s2 = wb.createSheet("Sheet2");
        assertEquals(2, wb.getSheetCount());

        // Get by index & name
        assertSame(s1, wb.getSheet(0));
        assertSame(s2, wb.getSheet(1));
        assertSame(s1, wb.getSheetByName("Sheet1"));
        assertSame(s2, wb.getSheetByName("Sheet2"));
        assertThrows(IllegalArgumentException.class, () -> wb.getSheetByName("NonExistent"));

        // Find by name
        assertEquals(java.util.Optional.of(s1), wb.findSheetByName("Sheet1"));
        assertTrue(wb.findSheetByName("NonExistent").isEmpty());

        // Index queries
        assertEquals(0, wb.getSheetIndexByName("Sheet1"));
        assertEquals(1, wb.getSheetIndexByName("Sheet2"));
        assertEquals(-1, wb.getSheetIndexByName("NonExistent"));
        assertEquals(0, wb.getSheetIndex(s1));
        assertEquals(1, wb.getSheetIndex(s2));
        assertEquals(-1, wb.getSheetIndex(null));

        // Get or create
        assertSame(s1, wb.getOrCreateSheet("Sheet1"));
        Sheet s3 = wb.getOrCreateSheet("Sheet3");
        assertEquals(3, wb.getSheetCount());
        assertEquals("Sheet3", s3.getSheetName());

        // Current sheet
        wb.setCurrentSheet(1);
        assertEquals(1, wb.getCurrentSheetIndex());
        assertEquals(java.util.Optional.of(s2), wb.getCurrentSheet());

        wb.setCurrentSheet(s1);
        assertEquals(0, wb.getCurrentSheetIndex());
        assertEquals(java.util.Optional.of(s1), wb.getCurrentSheet());

        // Iterator and streams
        assertEquals(3, wb.sheets().count());
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Sheet s : wb) {
            names.add(s.getSheetName());
        }
        assertEquals(java.util.List.of("Sheet1", "Sheet2", "Sheet3"), names);

        // Remove sheet
        wb.removeSheetByName("Sheet2");
        assertEquals(2, wb.getSheetCount());
        assertEquals(-1, wb.getSheetIndexByName("Sheet2"));
        assertThrows(IllegalArgumentException.class, () -> wb.removeSheetByName("NonExistent"));

        wb.removeSheet(0);
        assertEquals(1, wb.getSheetCount());
        assertEquals("Sheet3", wb.getSheet(0).getSheetName());
    }

    @Test
    void testCopyWorkbook() {
        GenericWorkbook wb1 = GenericWorkbookFactory.instance().create();
        GenericSheet s1 = wb1.createSheet("Data");
        s1.getCell(0, 0).set("Copied Content");

        GenericWorkbook wb2 = GenericWorkbookFactory.instance().create();
        wb2.copy(wb1);

        assertEquals(1, wb2.getSheetCount());
        assertEquals("Data", wb2.getSheet(0).getSheetName());
        assertEquals("Copied Content", wb2.getSheet(0).getCell(0, 0).getText().toString());
    }

    @Test
    void testObjectCaching() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        assertFalse(wb.isObjectCachingEnabled());

        wb.setObjectCaching(true);
        assertTrue(wb.isObjectCachingEnabled());

        String s1 = "CachedValue";
        assertSame(s1, wb.cache(s1));

        wb.setObjectCaching(false);
        assertFalse(wb.isObjectCachingEnabled());
        String s2 = "UncachedValue";
        assertSame(s2, wb.cache(s2));
    }

    @Test
    void testWriteOverloads() throws Exception {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Export");
        sheet.getCell(0, 0).set("Test");

        Path tempDir = Files.createTempDirectory("meja-write-test");
        try {
            Path csvPath = tempDir.resolve("export.csv");
            wb.write(csvPath);
            assertTrue(Files.exists(csvPath));
            assertTrue(Files.readString(csvPath).contains("Test"));

            Path csvUriPath = tempDir.resolve("export_uri.csv");
            wb.write(csvUriPath.toUri());
            assertTrue(Files.exists(csvUriPath));

            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                wb.write(FileTypeCsv.instance(), baos);
                assertTrue(baos.toString(StandardCharsets.UTF_8).contains("Test"));
            }
        } finally {
            IoUtil.deleteRecursive(tempDir);
        }
    }
}
