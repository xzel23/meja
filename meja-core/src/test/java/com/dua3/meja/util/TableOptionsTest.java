package com.dua3.meja.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableOptionsTest {

    @Test
    void testValues() {
        TableOptions[] options = TableOptions.values();
        assertTrue(options.length > 0);
        for (TableOptions option : options) {
            assertEquals(option, TableOptions.valueOf(option.name()));
        }
    }
}
