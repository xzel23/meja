package com.dua3.meja.model.generic;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericRowTest {

    private GenericWorkbook workbook;
    private GenericSheet sheet;
    private Row row;

    @BeforeEach
    void setUp() {
        workbook = GenericWorkbookFactory.instance().create();
        sheet = workbook.createSheet("Test");
        row = sheet.getRow(0);
    }

    @Test
    void testGetCellAndGetCellIfExists() {
        assertEquals(0, row.getColumnCount());
        assertFalse(row.getCellIfExists(0).isPresent());

        Cell c0 = row.getCell(0);
        assertNotNull(c0);
        assertEquals(1, row.getColumnCount());
        assertTrue(row.getCellIfExists(0).isPresent());
        assertSame(c0, row.getCellIfExists(0).get());

        Cell c5 = row.getCell(5);
        assertNotNull(c5);
        assertTrue(c5.isEmpty());
        assertEquals(6, row.getColumnCount());
        assertTrue(row.getCellIfExists(5).isPresent());
        assertTrue(row.getCellIfExists(3).isPresent());
    }

    @Test
    void testCreateCellWithValue() {
        Cell c0 = row.createCell("Hello");
        assertEquals(CellType.TEXT, c0.getCellType());
        assertEquals("Hello", c0.getText().toString());

        Cell c1 = row.createCell(100);
        assertEquals(CellType.NUMERIC, c1.getCellType());
        assertEquals(100, c1.getNumber().intValue());
    }

    @Test
    void testIteratorAndStream() {
        row.getCell(0).set("A");
        row.getCell(1).set("B");
        row.getCell(2).set("C");

        List<String> values = new ArrayList<>();
        for (Cell cell : row) {
            values.add(cell.getText().toString());
        }
        assertEquals(List.of("A", "B", "C"), values);

        long count = row.cells().count();
        assertEquals(3, count);
    }

    @Test
    void testCopyRow() {
        row.getCell(0).set("Value 0");
        row.getCell(1).set(123.4);

        Row otherRow = sheet.getRow(1);
        otherRow.copy(row);

        assertEquals(2, otherRow.getColumnCount());
        assertEquals("Value 0", otherRow.getCell(0).getText().toString());
        assertEquals(123.4, otherRow.getCell(1).getNumber().doubleValue());
    }
}
