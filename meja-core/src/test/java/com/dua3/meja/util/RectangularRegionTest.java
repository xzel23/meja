package com.dua3.meja.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RectangularRegionTest {

    @Test
    void testConstructorsAndProperties() {
        RectangularRegion region = new RectangularRegion(1, 4, 2, 6);
        assertEquals(1, region.firstRow());
        assertEquals(4, region.lastRow());
        assertEquals(2, region.firstColumn());
        assertEquals(6, region.lastColumn());
    }

    @Test
    void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new RectangularRegion(4, 1, 2, 6));
        assertThrows(IllegalArgumentException.class, () -> new RectangularRegion(1, 4, 6, 2));
    }

    @Test
    void testContains() {
        RectangularRegion region = new RectangularRegion(1, 3, 2, 5);
        assertTrue(region.contains(1, 2));
        assertTrue(region.contains(3, 5));
        assertTrue(region.contains(2, 3));

        assertFalse(region.contains(0, 2));
        assertFalse(region.contains(4, 2));
        assertFalse(region.contains(1, 1));
        assertFalse(region.contains(1, 6));
    }

    @Test
    void testIntersects() {
        RectangularRegion r1 = new RectangularRegion(1, 3, 1, 3);
        RectangularRegion r2 = new RectangularRegion(2, 4, 2, 4);
        RectangularRegion r3 = new RectangularRegion(4, 5, 4, 5);

        assertTrue(r1.intersects(r2));
        assertTrue(r2.intersects(r1));
        assertFalse(r1.intersects(r3));
        assertFalse(r3.intersects(r1));
    }

    @Test
    void testEqualsAndHashCode() {
        RectangularRegion r1 = new RectangularRegion(1, 3, 2, 4);
        RectangularRegion r2 = new RectangularRegion(1, 3, 2, 4);
        RectangularRegion r3 = new RectangularRegion(1, 4, 2, 4);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }
}
