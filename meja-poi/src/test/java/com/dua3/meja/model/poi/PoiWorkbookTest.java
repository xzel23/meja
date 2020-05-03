package com.dua3.meja.model.poi;

import com.dua3.meja.model.*;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.io.IOUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PoiWorkbookTest {

    private static Path testdataDir = IOUtil.toPath(PoiWorkbookTest.class.getResource("/"))
            .resolve("../../../../../testdata").normalize();

    @Test
    public void testXlsx() throws IOException, URISyntaxException {
        testCountryWorkbook(testdataDir.resolve("population by country.xlsx"));
    }

    @Test
    public void testXls() throws IOException, URISyntaxException {
        testCountryWorkbook(testdataDir.resolve("population by country.xls"));
    }

    @Test
    public void testSaveAndReloadXlsx() throws IOException, URISyntaxException {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xlsx"));
        Path pathToCopy = testdataDir.resolve("population by country (copy).xlsx");
        original.write(pathToCopy);
        testCountryWorkbook(pathToCopy);
    }

    @Test
    public void testSaveAndReloadXls() throws IOException, URISyntaxException {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xls"));
        Path pathToCopy = testdataDir.resolve("population by country (copy).xls");
        original.write(pathToCopy);
        testCountryWorkbook(pathToCopy);
    }

    @Test
    public void testConvertXlsxToXls() throws IOException, URISyntaxException {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xlsx"));
        Path pathToCopy = testdataDir.resolve("population by country (copy).xls");
        try {
            original.write(pathToCopy);
            testCountryWorkbook(pathToCopy);
        } finally {
            Files.delete(pathToCopy);
        }
    }

    @Test
    @Disabled("POI bug") // TODO create bug report and fix
    public void testConvertXlsToXlsx() throws IOException, URISyntaxException {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xls"));
        String wbFilename = "population by country (copy).xlsx";
        Path pathToCopy = testdataDir.resolve(wbFilename);
        try {
            original.write(pathToCopy);
            testCountryWorkbook(pathToCopy);
        } finally {
            Files.delete(pathToCopy);
        }
    }

    private void testCountryWorkbook(Path pathToWorkbook) throws IOException, URISyntaxException {
        Workbook wb = MejaHelper.openWorkbook(pathToWorkbook);
        assertEquals(1, wb.getSheetCount());

        Sheet sheet = wb.getSheet(0);
        assertNotNull(sheet);

        assertEquals(0, sheet.getFirstRowNum());
        assertEquals(1, sheet.getSplitRow());
        assertEquals(1, sheet.getSplitRow());

        Cell cChina = sheet.getCell(1,0);
        assertNotNull(cChina);
        assertEquals("China", Objects.toString(cChina.get()));
        assertEquals("China", Objects.toString(cChina.getText()));
        assertEquals("China", cChina.toString());

        Optional<URI> lChina = cChina.getHyperlink();
        assertNotNull(lChina);
        assertEquals(new URI("https://www.worldometers.info/world-population/china-population/"), lChina.get());

        double[] sums = { 0.0, 7_794_798_739.0, 2.5957, 81_330_639.0, 111_806.0, 130_094_083.0, 1_263.0, 541.3, 6_152.0, 131.5, 0.9998 };

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

    @Test
    public void testHyperLink() throws IOException, URISyntaxException {
        Workbook wb = MejaHelper.openWorkbook(testdataDir.resolve("Links.xlsx"));
        Sheet sheet = wb.getSheetByName("Links");

        Cell cCalendar = sheet.getCell(0,1);
        assertEquals("Calendar", cCalendar.toString());
        Optional<URI> lCalendar = cCalendar.getHyperlink();
        assertTrue(lCalendar.isPresent());
        assertEquals(IOUtil.toURI(testdataDir.resolve("Excel 2015 Calendar.xlsx")), lCalendar.get());

        Cell cEmail = sheet.getCell(2,1);
        assertEquals("Email Developer", cEmail.toString());
        Optional<URI> lEmail = cEmail.getHyperlink();
        assertTrue(lEmail.isPresent());
        assertEquals(new URI("mailto:axel@dua3.com?subject=Meja%20Test%20Email"), lEmail.get());

        Workbook wb2 = PoiWorkbookFactory.instance().createXlsx();
        Sheet sheet2 = wb2.createSheet("Links");

        Cell cellLinkToFile = sheet2.getCell(0,0);
        cellLinkToFile.setHyperlink(lCalendar.get());
        assertEquals(lCalendar.get(), cellLinkToFile.getHyperlink().get());

        Cell cellLinkToEmail = sheet2.getCell(1,0);
        cellLinkToEmail.setHyperlink(lEmail.get());
        assertEquals(lEmail.get(), cellLinkToEmail.getHyperlink().get());
    }
}
