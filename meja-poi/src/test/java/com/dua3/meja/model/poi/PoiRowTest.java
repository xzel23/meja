package com.dua3.meja.model.poi;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PoiRowTest {

    private PoiWorkbook workbook;
    private Sheet sheet;
    private Row row;

    @BeforeEach
    void setUp() {
        workbook = PoiWorkbookFactory.instance().createXlsx();
        sheet = workbook.createSheet("Test");
        row = sheet.getRow(0);
    }

    @Test
    void testGetCellAndGetCellIfExists() {
        assertFalse(row.getCellIfExists(0).isPresent());

        Cell c0 = row.getCell(0);
        assertNotNull(c0);
        assertTrue(row.getCellIfExists(0).isPresent());
        assertEquals(c0, row.getCellIfExists(0).get());

        Cell c5 = row.getCell(5);
        assertNotNull(c5);
        assertTrue(c5.isEmpty());
        assertTrue(row.getCellIfExists(5).isPresent());
    }

    @Test
    void testIteratorAndStream() {
        row.getCell(0).set("X");
        row.getCell(1).set("Y");
        row.getCell(2).set("Z");

        List<String> values = new ArrayList<>();
        for (Cell cell : row) {
            values.add(cell.getText().toString());
        }
        assertEquals(List.of("X", "Y", "Z"), values);

        long count = row.cells().count();
        assertEquals(3, count);
    }

    @Test
    void testCopyRow() {
        row.getCell(0).set("Hello POI");
        row.getCell(1).set(99.9);

        Row otherRow = sheet.getRow(1);
        otherRow.copy(row);

        assertEquals("Hello POI", otherRow.getCell(0).getText().toString());
        assertEquals(99.9, otherRow.getCell(1).getNumber().doubleValue(), 0.001);
    }

    @Test
    void testCreateCellWithoutArgs() {
        Cell c0 = row.createCell();
        assertNotNull(c0);
        assertTrue(c0.isEmpty());
        assertEquals(1, row.getColumnCount());
    }

    @Test
    void testCreateCellWithValue() {
        Cell c0 = row.createCell("Hello");
        assertEquals("Hello", c0.getText().toString());

        Cell c1 = row.createCell(100);
        assertEquals(100, c1.getNumber().intValue());
    }

    @Test
    void testRowHeightAndProperties() {
        sheet.setRowHeight(0, 42.0f);
        assertEquals(42.0f, row.getRowHeight(), 0.1);
        assertSame(sheet, row.getSheet());
        assertSame(workbook, row.getWorkbook());
        assertEquals(0, row.getRowNumber());
    }

    @Test
    void testFind() {
        row.getCell(0).set("First");
        row.getCell(1).set("Second");
        row.getCell(2).setFormula("SUM(A1:B1)");

        // Ignore case
        assertTrue(row.find("second", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.IGNORE_CASE)).isPresent());
        assertFalse(row.find("second", com.dua3.meja.model.SearchSettings.of()).isPresent());

        // Match complete
        assertFalse(row.find("Sec", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.MATCH_COMPLETE_TEXT)).isPresent());
        assertTrue(row.find("Second", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.MATCH_COMPLETE_TEXT)).isPresent());

        // Formula search
        assertTrue(row.find("SUM", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.SEARCH_FORMULA_TEXT)).isPresent());

        // Update current
        assertTrue(row.find("Second", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND)).isPresent());
        assertEquals(row.getCell(1), sheet.getCurrentCell());

        // Search from current
        assertTrue(row.find("First", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.SEARCH_FROM_CURRENT)).isPresent());

        // Not found
        assertFalse(row.find("NonExistent", com.dua3.meja.model.SearchSettings.of()).isPresent());
    }
}
