package com.dua3.meja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FillPatternTest {

    @Test
    void testValues() {
        FillPattern[] patterns = FillPattern.values();
        assertTrue(patterns.length > 0);
        for (FillPattern pattern : patterns) {
            assertEquals(pattern, FillPattern.valueOf(pattern.name()));
        }
    }
}
