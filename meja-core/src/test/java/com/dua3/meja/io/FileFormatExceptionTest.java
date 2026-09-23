package com.dua3.meja.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileFormatExceptionTest {

    @Test
    void testConstructors() {
        FileFormatException ex1 = new FileFormatException("Error message");
        assertEquals("Error message", ex1.getMessage());

        Throwable cause = new IllegalArgumentException("cause");
        FileFormatException ex2 = new FileFormatException("Error message with cause", cause);
        assertEquals("Error message with cause", ex2.getMessage());
        assertEquals(cause, ex2.getCause());
    }
}
