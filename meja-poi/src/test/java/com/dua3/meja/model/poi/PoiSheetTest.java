package com.dua3.meja.model.poi;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PoiSheetTest {

    @Test
    public void testCreateSheet() {
        PoiWorkbook wb = PoiWorkbookFactory.instance().create();

        wb.createSheet("Test");
        wb.createSheet("foo");
        wb.createSheet("bar");

        assertEquals(3, wb.getSheetCount());
        assertEquals("Test", wb.getSheet(0).getSheetName());
        assertEquals("foo", wb.getSheet(1).getSheetName());
        assertEquals("bar", wb.getSheet(2).getSheetName());
        assertNotNull(wb.getSheetByName("Test"));
        assertNotNull(wb.getSheetByName("foo"));
        assertNotNull(wb.getSheetByName("bar"));
        assertEquals("Test", wb.getSheetByName("Test").getSheetName());
        assertEquals("foo", wb.getSheetByName("foo").getSheetName());
        assertEquals("bar", wb.getSheetByName("bar").getSheetName());
    }

    @Test
    public void testCreateRow() {
        PoiWorkbook wb = PoiWorkbookFactory.instance().create();
        Sheet s = wb.createSheet("Test");

        Row r = s.createRow("a", 123.5, null, LocalDate.of(2023, 2, 1), true);

        assertEquals(1, s.getRowCount());

        Cell c0 = r.getCell(0);
        assertEquals(CellType.TEXT, c0.getCellType());
        assertEquals("a", c0.toString());

        Cell c1 = r.getCell(1);
        assertEquals(CellType.NUMERIC, c1.getCellType());
        assertEquals(123.5, c1.getNumber().doubleValue());
        assertEquals("123.5", c1.toString(Locale.US));
        assertEquals("123,5", c1.toString(Locale.GERMANY));
        assertEquals("123,5", c1.toString(Locale.FRANCE));

        Cell c2 = r.getCell(2);
        assertEquals(CellType.BLANK, c2.getCellType());

        Cell c3 = r.getCell(3);
        // Excel does not have a distinct cell type for dates, so dates are detected using value _and_ formatting.
        // Meja automatically sets a date format when a LocalDate is passed. This test makes sure that locale dependent
        // formatting works.
        assertEquals(LocalDate.of(2023, 2,1), c3.getDate());
        assertEquals("2/1/23", c3.toString(Locale.US));
        assertEquals("01.02.23", c3.toString(Locale.GERMANY));
        assertEquals("01/02/2023", c3.toString(Locale.FRANCE));

        Cell c4 = r.getCell(4);
        assertEquals(CellType.BOOLEAN, c4.getCellType());
        assertEquals(Boolean.TRUE, c4.getBoolean());
    }
}
