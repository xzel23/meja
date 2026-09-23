package com.dua3.meja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HAlignTest {

    @Test
    void testHAlignProperties() {
        assertEquals("text-align: left;", HAlign.ALIGN_LEFT.getCssStyle());
        assertEquals("text-align: center;", HAlign.ALIGN_CENTER.getCssStyle());
        assertEquals("text-align: right;", HAlign.ALIGN_RIGHT.getCssStyle());
        assertEquals("text-align: left; white-space: pre-wrap !important;", HAlign.ALIGN_JUSTIFY.getCssStyle());
        assertEquals("text-align: left; white-space: pre-wrap !important;", HAlign.ALIGN_DISTRIBUTED.getCssStyle());
        assertEquals("text-align: left;", HAlign.ALIGN_AUTOMATIC.getCssStyle());

        assertTrue(HAlign.ALIGN_JUSTIFY.isWrap());
        assertTrue(HAlign.ALIGN_DISTRIBUTED.isWrap());
        assertFalse(HAlign.ALIGN_LEFT.isWrap());
        assertFalse(HAlign.ALIGN_CENTER.isWrap());
        assertFalse(HAlign.ALIGN_RIGHT.isWrap());
        assertFalse(HAlign.ALIGN_AUTOMATIC.isWrap());
    }
}
