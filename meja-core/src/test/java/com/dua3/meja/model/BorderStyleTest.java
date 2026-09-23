package com.dua3.meja.model;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BorderStyleTest {

    @Test
    void testPredefinedEmptyBorder() {
        BorderStyle empty = BorderStyle.EMPTY_BORDER;
        assertNotNull(empty);
        assertEquals(0.0f, empty.width());
        assertEquals(Color.BLACK, empty.color());
        assertTrue(empty.isNone());
    }

    @Test
    void testIsNone() {
        assertTrue(BorderStyle.EMPTY_BORDER.isNone());
        assertTrue(new BorderStyle(0.0f, Color.RED).isNone());
        assertFalse(new BorderStyle(1.0f, Color.BLACK).isNone());
    }

    @Test
    void testRecordProperties() {
        BorderStyle border = new BorderStyle(1.5f, Color.BLUE);
        assertEquals(1.5f, border.width());
        assertEquals(Color.BLUE, border.color());
    }

    @Test
    void testEqualsAndHashCode() {
        BorderStyle b1 = new BorderStyle(1.0f, Color.BLACK);
        BorderStyle b2 = new BorderStyle(1.0f, Color.BLACK);
        BorderStyle b3 = new BorderStyle(2.0f, Color.BLACK);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
        assertNotEquals(b1, b3);
    }
}
