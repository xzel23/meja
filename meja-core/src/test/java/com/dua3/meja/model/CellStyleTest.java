package com.dua3.meja.model;

import com.dua3.meja.model.CellStyle.StandardDataFormats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellStyleTest {

    @Test
    void testStandardDataFormats() {
        StandardDataFormats[] formats = StandardDataFormats.values();
        assertEquals(2, formats.length);
        assertEquals(StandardDataFormats.FULL, StandardDataFormats.valueOf("FULL"));
        assertEquals(StandardDataFormats.MEDIUM, StandardDataFormats.valueOf("MEDIUM"));
    }
}
