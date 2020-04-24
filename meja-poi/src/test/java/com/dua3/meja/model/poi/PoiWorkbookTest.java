package com.dua3.meja.model.poi;

import com.dua3.meja.model.*;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.io.IOUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PoiWorkbookTest {

    private static Path testdataDir = IOUtil.toPath(PoiWorkbookTest.class.getResource("/"))
            .resolve("../../../../../testdata").normalize();

    @Test
    public void testXlsx() throws IOException {
        testCountryWorkbook("population by country.xlsx");
    }

    @Test
    public void testXls() throws IOException {
        testCountryWorkbook("population by country.xls");
    }

    @Test
    public void testSaveAndReloadXlsx() throws IOException {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xlsx"));
        Path pathToCopy = testdataDir.resolve("population by country (copy).xlsx");
        original.write(pathToCopy);
        testCountryWorkbook(pathToCopy.getFileName().toString());
    }

    @Test
    public void testSaveAndReloadXls() throws IOException {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xls"));
        Path pathToCopy = testdataDir.resolve("population by country (copy).xls");
        original.write(pathToCopy);
        testCountryWorkbook(pathToCopy.getFileName().toString());
    }

    @Test
    public void testConvertXlsxToXls() throws IOException {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xlsx"));
        Path pathToCopy = testdataDir.resolve("population by country (copy).xls");
        original.write(pathToCopy);
        testCountryWorkbook(pathToCopy.getFileName().toString());
    }

    @Test
    @Disabled("POI bug")
    public void testConvertXlsToXlsx() throws IOException {
        Workbook original = MejaHelper.openWorkbook(testdataDir.resolve("population by country.xls"));
        Path pathToCopy = testdataDir.resolve("population by country (copy).xlsx");
        original.write(pathToCopy);
        testCountryWorkbook(pathToCopy.getFileName().toString());
    }

    private void testCountryWorkbook(String filename) throws IOException {
        Workbook wb = MejaHelper.openWorkbook(testdataDir.resolve(filename));
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

        Optional<URL> lChina = cChina.getHyperlink();
        assertNotNull(lChina);
        assertEquals(new URL("https://www.worldometers.info/world-population/china-population/"), lChina.get());

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
}
