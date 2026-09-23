package com.dua3.meja.util;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CellValueHelperTest {

    private GenericSheet sheet;
    private CellValueHelper helperDateTime;
    private CellValueHelper helperDateOnly;

    @BeforeEach
    void setUp() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        sheet = wb.createSheet("Test");

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DateTimeFormatter dtfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        helperDateTime = new CellValueHelper(nf, dtfDateTime);

        DateTimeFormatter dtfDateOnly = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        helperDateOnly = new CellValueHelper(nf, dtfDateOnly);
    }

    @Test
    void testBlankAndFormula() {
        Cell c0 = sheet.getCell(0, 0);
        c0.set("Initial");
        helperDateTime.setCellValue(c0, "");
        assertTrue(c0.isEmpty());
        assertEquals(CellType.BLANK, c0.getCellType());

        Cell c1 = sheet.getCell(0, 1);
        helperDateTime.setCellValue(c1, "=SUM(A1:B1)");
        assertEquals(CellType.FORMULA, c1.getCellType());
        assertEquals("SUM(A1:B1)", c1.getFormula());
    }

    @Test
    void testBooleans() {
        Cell c0 = sheet.getCell(0, 0);
        helperDateTime.setCellValue(c0, "TRUE");
        assertEquals(CellType.BOOLEAN, c0.getCellType());
        assertTrue(c0.getBoolean());

        Cell c1 = sheet.getCell(0, 1);
        helperDateTime.setCellValue(c1, "  false  ");
        assertEquals(CellType.BOOLEAN, c1.getCellType());
        assertFalse(c1.getBoolean());
    }

    @Test
    void testNumbers() {
        Cell c0 = sheet.getCell(0, 0);
        helperDateTime.setCellValue(c0, "1234.56");
        assertEquals(CellType.NUMERIC, c0.getCellType());
        assertEquals(1234.56, c0.getNumber().doubleValue());

        // Partial number with invalid suffix falls back to text
        Cell c1 = sheet.getCell(0, 1);
        helperDateTime.setCellValue(c1, "1234abc");
        assertEquals(CellType.TEXT, c1.getCellType());
        assertEquals("1234abc", c1.getText().toString());
    }

    @Test
    void testDatesWithHourOfDay() {
        Cell c0 = sheet.getCell(0, 0);
        helperDateTime.setCellValue(c0, "2024-06-15 14:30:00");
        assertEquals(CellType.DATE_TIME, c0.getCellType());
        assertEquals(LocalDateTime.of(2024, 6, 15, 14, 30, 0), c0.getDateTime());
    }

    @Test
    void testDatesWithDateOnly() {
        Cell c0 = sheet.getCell(0, 0);
        helperDateOnly.setCellValue(c0, "2024-06-15");
        assertEquals(CellType.DATE_TIME, c0.getCellType());
        assertEquals(LocalDate.of(2024, 6, 15), c0.getDateTime().toLocalDate());
    }

    @Test
    void testInvalidDatesFallbackToText() {
        Cell c0 = sheet.getCell(0, 0);
        helperDateOnly.setCellValue(c0, "2024-99-99");
        assertEquals(CellType.TEXT, c0.getCellType());
        assertEquals("2024-99-99", c0.getText().toString());

        Cell c1 = sheet.getCell(0, 1);
        helperDateOnly.setCellValue(c1, "NotADate");
        assertEquals(CellType.TEXT, c1.getCellType());
        assertEquals("NotADate", c1.getText().toString());
    }
}
