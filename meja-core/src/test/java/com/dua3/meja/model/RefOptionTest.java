package com.dua3.meja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefOptionTest {

    @Test
    void testValues() {
        RefOption[] options = RefOption.values();
        assertEquals(3, options.length);
        assertEquals(RefOption.FIX_ROW, RefOption.valueOf("FIX_ROW"));
        assertEquals(RefOption.FIX_COLUMN, RefOption.valueOf("FIX_COLUMN"));
        assertEquals(RefOption.WITH_SHEET, RefOption.valueOf("WITH_SHEET"));
    }
}
