package com.dua3.meja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    @Test
    void testErrorText() {
        assertEquals("#ERROR", Cell.ERROR_TEXT);
    }
}
