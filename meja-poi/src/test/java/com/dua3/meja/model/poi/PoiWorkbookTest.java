package com.dua3.meja.model.poi;

import com.dua3.meja.io.HtmlWorkbookWriter;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.io.IoOptions;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class PoiWorkbookTest {
    private static Path testdataDir;

    private static Path tempDir;

    @BeforeAll
    public static void setup() throws IOException {
        testdataDir = IoUtil.toPath(PoiWorkbookTest.class.getResource("/"))
                .resolve("../../../../../testdata").normalize();
        tempDir = Files.createTempDirectory("meja-test");
    }

    @AfterAll
    public static void teardown() throws IOException {
        IoUtil.deleteRecursive(tempDir);
    }

    @ParameterizedTest
    @ValueSource(strings = {"population by country.xlsx", "population by country.xls"})
    public void testOpenWorkbook(String filename) throws Exception {
        testCountryWorkbook(testdataDir.resolve(filename));
    }

    @ParameterizedTest
    @ValueSource(strings = {"population by country.xlsx", "population by country.xls"})
    public void testSaveAndReloadWorkbook(String filename) throws Exception {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve(filename));
        Path pathToCopy = tempDir.resolve(filename);
        original.write(pathToCopy);
        testCountryWorkbook(pathToCopy);
    }

    @Test
    public void testConvertXlsxToXls() throws Exception {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xlsx"));
        Path pathToCopy = tempDir.resolve("population by country.xls");
        original.write(pathToCopy);
        testCountryWorkbook(pathToCopy);
    }

    @Test
    public void testConvertXlsToXlsx() throws Exception {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xls"));
        Path pathToCopy = tempDir.resolve("population by country.xlsx");
        original.write(pathToCopy);
        testCountryWorkbook(pathToCopy);
    }

    private static String maskUriHash(String s) {
        return s.replaceAll("WB_[a-z0-9]{32}_", "WB_********************************_");
    }

    @ParameterizedTest
    @ValueSource(strings = {"population by country.xlsx", "Excel 2015 Calendar.xlsx"})
    public void testConvertToHtml(String fileName) throws Exception {
        Path inFile = testdataDir.resolve(fileName);
        String outFileName = IoUtil.replaceExtension(fileName, "html");
        Path refFile = testdataDir.resolve(outFileName);
        Path outFile = tempDir.resolve(outFileName);
        copyToHtml(inFile, outFile, Locale.US);
        assertLinesMatch(
                maskUriHash(Files.readString(refFile, StandardCharsets.UTF_8)).lines(),
                maskUriHash(Files.readString(outFile, StandardCharsets.UTF_8)).lines()
        );
    }

    private void copyToHtml(Path inFile, Path outFile, Locale locale) throws IOException {
        Workbook original = MejaHelper.openWorkbook(inFile);
        original.write(outFile, Arguments.of(Arguments.createEntry(IoOptions.locale(), locale)));
    }

    private void testCountryWorkbook(Path pathToWorkbook) throws IOException, URISyntaxException {
        Workbook wb = MejaHelper.openWorkbook(pathToWorkbook);
        assertEquals(1, wb.getSheetCount());

        Sheet sheet = wb.getSheet(0);
        assertNotNull(sheet);

        assertEquals(0, sheet.getFirstRowNum());
        assertEquals(1, sheet.getSplitRow());
        assertEquals(1, sheet.getSplitRow());

        Cell cChina = sheet.getCell(1, 0);
        assertNotNull(cChina);
        assertEquals("China", Objects.toString(cChina.getOrDefault(null)));
        assertEquals("China", Objects.toString(cChina.getText()));
        assertEquals("China", cChina.toString());

        Optional<URI> lChina = cChina.getHyperlink();
        assertNotNull(lChina);
        assertEquals(new URI("https://www.worldometers.info/world-population/china-population/"), lChina.get());

        double[] sums = {0.0, 7_794_798_739.0, 2.5957, 81_330_639.0, 111_806.0, 130_094_083.0, 1_263.0, 541.3, 6_152.0, 131.5, 0.9998};

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

    @Test
    public void testHyperLink() throws Exception {
        Workbook wb = MejaHelper.openWorkbook(testdataDir.resolve("Links.xlsx"));
        Sheet sheet = wb.getSheetByName("Links");

        Cell cCalendar = sheet.getCell(0, 1);
        assertEquals("Calendar", cCalendar.toString());

        // test relative hyperlink
        Optional<URI> lCalendar = cCalendar.getHyperlink();
        assertTrue(lCalendar.isPresent());
        assertEquals(URI.create("Excel%202015%20Calendar.xlsx"), lCalendar.get());

        // test resolving relative hyperlink
        lCalendar = cCalendar.getResolvedHyperlink();
        assertTrue(lCalendar.isPresent());
        assertEquals(wb.getUri().get().resolve("Excel%202015%20Calendar.xlsx"), lCalendar.get());

        Cell cEmail = sheet.getCell(2, 1);
        assertEquals("Email Developer", cEmail.toString());
        Optional<URI> lEmail = cEmail.getHyperlink();
        assertTrue(lEmail.isPresent());
        assertEquals(new URI("mailto:axel@dua3.com?subject=Meja%20Test%20Email"), lEmail.get());

        Workbook wb2 = PoiWorkbookFactory.instance().createXlsx();
        Sheet sheet2 = wb2.createSheet("Links");

        Cell cellLinkToFile = sheet2.getCell(0, 0);
        cellLinkToFile.setHyperlink(lCalendar.get());
        assertEquals(lCalendar.get(), cellLinkToFile.getHyperlink().get());

        Cell cellLinkToEmail = sheet2.getCell(1, 0);
        cellLinkToEmail.setHyperlink(lEmail.get());
        assertEquals(lEmail.get(), cellLinkToEmail.getHyperlink().get());
    }

    @Test
    public void testHtmlExport() throws IOException {
        Workbook wb = MejaHelper.openWorkbook(testdataDir.resolve("test.xlsx"));

        // make sure workbook URI is the same independent of test environment
        wb.setUri(URI.create(""));

        for (Sheet sheet : wb) {
            HtmlWorkbookWriter writer = HtmlWorkbookWriter.create();

            String refHtml = Files.readString(testdataDir.resolve(sheet.getSheetName() + ".html"), StandardCharsets.UTF_8);

            try (Formatter out = new Formatter()) {
                writer.writeHtmlHeaderStart(out);
                writer.writeCssForSingleSheet(out, sheet);
                writer.writeHtmlHeaderEnd(out);
                writer.writeSheet(sheet, out, Locale.ROOT);
                writer.writeHtmlFooter(out);
                String actHtml = out.toString();

                boolean updateResult = false; // set to true to update reference files
                if (updateResult) {
                    Files.writeString(testdataDir.resolve(sheet.getSheetName() + ".html"), actHtml, StandardCharsets.UTF_8);
                } else {
                    assertEquals(refHtml.replaceAll("\r\n", "\n"), actHtml.replaceAll("\r\n", "\n"));
                }
            }
        }
    }

    @Test
    public void testRowGetLastColNumErrorXlsx() {
        Workbook wb = PoiWorkbookFactory.instance().createXlsx();
        testRowGetLastColNumErrorHelper(wb);
    }

    @Test
    public void testRowGetLastColNumErrorXls() {
        Workbook wb = PoiWorkbookFactory.instance().createXls();
        testRowGetLastColNumErrorHelper(wb);
    }

    private void testRowGetLastColNumErrorHelper(Workbook wb) {
        Sheet sheet = wb.createSheet("index");

        assertEquals(0, sheet.getFirstRowNum());
        assertEquals(0, sheet.getFirstColNum());
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
