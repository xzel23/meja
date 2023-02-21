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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenericWorkbookTest {

    private static final Path testdataDir = IoUtil.toPath(GenericWorkbookTest.class.getResource("/"))
            .resolve("../../../../../testdata").normalize();

    @Test
    public void testCsv() throws Exception {
        testCountryWorkbook(testdataDir.resolve("population by country_US.csv"), Locale.US);
        testCountryWorkbook(testdataDir.resolve("population by country_DE.csv"), Locale.GERMANY);
    }

    @Test
    public void testSaveAndReloadCsv() throws Exception {
        Workbook original = openWorkbookCsv(testdataDir.resolve("population by country_US.csv"), Locale.US);
        Path tempDir = Files.createTempDirectory("meja-test");
        try {
            Path pathToCopy = tempDir.resolve("population by country (copy).csv");
            original.write(pathToCopy, Arguments.of(
                    Arguments.createEntry(IoOptions.fieldSeparator(), ';'),
                    Arguments.createEntry(IoOptions.locale(), Locale.US)
            ));
            testCountryWorkbook(pathToCopy, Locale.US);
        } finally {
            IoUtil.deleteRecursive(tempDir);
        }
    }

    @Test
    public void testConvertToHtml() throws Exception {
        String[] files = { "population by country_US.csv" };
        Path tempDir = Files.createTempDirectory("meja-test");
        try {
            for (String inFileName: files) {
                Path inFile = testdataDir.resolve(inFileName);
                String outFileName = IoUtil.replaceExtension(inFileName, "html");
                Path refFile = testdataDir.resolve(outFileName);
                Path outFile = tempDir.resolve(outFileName);
                copyToHtml(inFile, outFile, Locale.US);
                assertEquals(Files.readString(refFile), Files.readString(outFile));
            }
        } finally {
            IoUtil.deleteRecursive(tempDir);
        }
    }

    private void copyToHtml(Path inFile, Path outFile, Locale locale) throws IOException {
        Workbook original = openWorkbookCsv(inFile, locale);
        original.write(outFile);
    }

    private void testCountryWorkbook(Path pathToWorkbook, Locale locale) throws IOException {
        Workbook wb = openWorkbookCsv(pathToWorkbook, locale);
        assertEquals(1, wb.getSheetCount());

        Sheet sheet = wb.getSheet(0);
        assertNotNull(sheet);
        
        sheet.splitAt(1, 1); // split position cannot be set in CSV files. set it here so that at least part of the functionlaity can be verified.
        
        assertEquals(0, sheet.getFirstRowNum());
        assertEquals(1, sheet.getSplitRow());
        assertEquals(1, sheet.getSplitRow());

        Cell cChina = sheet.getCell(1,0);
        assertNotNull(cChina);
        assertEquals("China", Objects.toString(cChina.get()));
        assertEquals("China", Objects.toString(cChina.getText()));
        assertEquals("China", cChina.toString());

        // note that percent values are treated as zero in this simple test
        double[] sums = { 0.0, 7_794_798_739.0, 0.0, 81_330_639.0, 111_806.0, 130_094_083.0, 1_263.0, 541.3, 6_152.0, 0.0, 0.0 };

        for (int j=0; j<sums.length; j++) {
            final int jj = j;
            double expected = sums[j];
            double actual = sheet.rows()
                    .map(row -> row.getCell(jj))
                    .mapToDouble(c -> c.getCellType() == CellType.NUMERIC ? c.getNumber().doubleValue() : 0.0)
                    .sum();
            assertEquals(expected, actual);
        }
    }

    private static Workbook openWorkbookCsv(Path pathToWorkbook, Locale locale) throws IOException {
        return FileTypeCsv.instance()
                .read(
                    pathToWorkbook,
                    t -> Arguments.of(
                        Arguments.createEntry(IoOptions.fieldSeparator(), ';'),
                        Arguments.createEntry(IoOptions.locale(), locale)
                    )
                );
    }

    @Test
    public void testRowGetLastColNumError() {
        Workbook wb = GenericWorkbookFactory.instance().create();
        testRowGetLastColNumErrorHelper(wb);
    }

    private void testRowGetLastColNumErrorHelper(Workbook wb) {
        Sheet sheet = wb.createSheet("index");

        assertEquals( 0, sheet.getFirstRowNum());
        assertEquals( 0, sheet.getFirstColNum());
        assertEquals(-1, sheet.getLastRowNum());
        assertEquals(-1, sheet.getLastColNum());

        sheet.createRow(1, 2, 3, 4);
        assertEquals(0, sheet.getFirstRowNum());
        assertEquals(0, sheet.getFirstColNum());
        assertEquals(0, sheet.getLastRowNum());
        assertEquals(3, sheet.getLastColNum());

        sheet.createRow(1, 2, 3, 4);
    }

}
