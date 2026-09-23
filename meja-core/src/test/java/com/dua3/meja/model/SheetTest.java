package com.dua3.meja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SheetTest {

    @Test
    void testGetColumnName() {
        assertEquals("A", Sheet.getColumnName(0));
        assertEquals("Z", Sheet.getColumnName(25));
        assertEquals("AA", Sheet.getColumnName(26));
        assertEquals("AB", Sheet.getColumnName(27));
        assertEquals("AZ", Sheet.getColumnName(51));
        assertEquals("BA", Sheet.getColumnName(52));
        assertEquals("ZZ", Sheet.getColumnName(701));
        assertEquals("AAA", Sheet.getColumnName(702));
        assertEquals("XFD", Sheet.getColumnName(16383));

        assertThrows(IllegalArgumentException.class, () -> Sheet.getColumnName(-1));
    }

    @Test
    void testGetColumnNumber() {
        assertEquals(0, Sheet.getColumnNumber("A"));
        assertEquals(25, Sheet.getColumnNumber("Z"));
        assertEquals(26, Sheet.getColumnNumber("AA"));
        assertEquals(27, Sheet.getColumnNumber("AB"));
        assertEquals(51, Sheet.getColumnNumber("AZ"));
        assertEquals(52, Sheet.getColumnNumber("BA"));
        assertEquals(701, Sheet.getColumnNumber("ZZ"));
        assertEquals(702, Sheet.getColumnNumber("AAA"));
        assertEquals(16383, Sheet.getColumnNumber("XFD"));

        // Case insensitivity
        assertEquals(0, Sheet.getColumnNumber("a"));
        assertEquals(26, Sheet.getColumnNumber("Aa"));
        assertEquals(701, Sheet.getColumnNumber("zZ"));

        assertThrows(IllegalArgumentException.class, () -> Sheet.getColumnNumber("123"));
        assertThrows(IllegalArgumentException.class, () -> Sheet.getColumnNumber("A-1"));
        assertThrows(IllegalArgumentException.class, () -> Sheet.getColumnNumber(""));
    }

    @Test
    void testColumnNameNumberRoundTrip() {
        for (int i = 0; i < 2000; i++) {
            String name = Sheet.getColumnName(i);
            int number = Sheet.getColumnNumber(name);
            assertEquals(i, number, "Mismatch for index " + i + " -> " + name);
        }
    }

    @Test
    void testGetRowName() {
        assertEquals("1", Sheet.getRowName(0));
        assertEquals("100", Sheet.getRowName(99));
        assertThrows(IllegalArgumentException.class, () -> Sheet.getRowName(-1));
    }
}
