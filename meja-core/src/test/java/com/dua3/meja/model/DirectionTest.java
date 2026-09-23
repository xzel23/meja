package com.dua3.meja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

    @Test
    void testGetCssName() {
        assertEquals("top", Direction.NORTH.getCssName());
        assertEquals("right", Direction.EAST.getCssName());
        assertEquals("bottom", Direction.SOUTH.getCssName());
        assertEquals("left", Direction.WEST.getCssName());
    }

    @Test
    void testInverse() {
        assertEquals(Direction.SOUTH, Direction.NORTH.inverse());
        assertEquals(Direction.NORTH, Direction.SOUTH.inverse());
        assertEquals(Direction.WEST, Direction.EAST.inverse());
        assertEquals(Direction.EAST, Direction.WEST.inverse());
    }
}
