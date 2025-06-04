package com.dua3.meja.model.generic;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenericSheetTest {

    @Test
    void testCreateSheet() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();

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
    void testCreateRow() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        Sheet s = wb.createSheet("Test");

        Row r = s.createRow("a", 123.5, null, LocalDate.of(2023, 1, 1), true);

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
        assertEquals(CellType.DATE, c3.getCellType());
        assertEquals(LocalDate.of(2023, 1,1), c3.getDate());
        assertEquals("Jan 1, 2023", c3.toString(Locale.US));
        assertEquals("01.01.2023", c3.toString(Locale.GERMANY));
        assertEquals("1 janv. 2023", c3.toString(Locale.FRANCE));

        Cell c4 = r.getCell(4);
        assertEquals(CellType.BOOLEAN, c4.getCellType());
        assertEquals(Boolean.TRUE, c4.getBoolean());
    }
}
