package com.dua3.meja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CellTypeTest {

    @Test
    void testCellTypeValues() {
        // Test that all expected enum values exist
        assertEquals(8, CellType.values().length);
    }

    @Test
    void testCellTypeOrdinals() {
        // Test the ordinal values of the enum constants
        assertEquals(0, CellType.BLANK.ordinal());
        assertEquals(1, CellType.BOOLEAN.ordinal());
        assertEquals(2, CellType.ERROR.ordinal());
        assertEquals(3, CellType.FORMULA.ordinal());
        assertEquals(4, CellType.NUMERIC.ordinal());
        assertEquals(5, CellType.TEXT.ordinal());
        assertEquals(6, CellType.DATE.ordinal());
        assertEquals(7, CellType.DATE_TIME.ordinal());
    }
}