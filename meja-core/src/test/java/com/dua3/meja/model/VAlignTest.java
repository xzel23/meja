package com.dua3.meja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VAlignTest {

    @Test
    void testVAlignProperties() {
        assertEquals("vertical-align: top;", VAlign.ALIGN_TOP.getCssStyle());
        assertEquals("vertical-align: middle;", VAlign.ALIGN_MIDDLE.getCssStyle());
        assertEquals("vertical-align: bottom;", VAlign.ALIGN_BOTTOM.getCssStyle());
        assertEquals("vertical-align: bottom;", VAlign.ALIGN_JUSTIFY.getCssStyle());
        assertEquals("vertical-align: bottom; white-space: pre-wrap !important;", VAlign.ALIGN_DISTRIBUTED.getCssStyle());

        assertTrue(VAlign.ALIGN_DISTRIBUTED.isWrap());
        assertFalse(VAlign.ALIGN_TOP.isWrap());
        assertFalse(VAlign.ALIGN_MIDDLE.isWrap());
        assertFalse(VAlign.ALIGN_BOTTOM.isWrap());
        assertFalse(VAlign.ALIGN_JUSTIFY.isWrap());
    }
}
