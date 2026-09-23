package com.dua3.meja.util;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class IteratorAdapterTest {

    @Test
    void testIteratorAdapter() {
        List<String> list = List.of("A", "B", "C");
        Iterator<String> it = list.iterator();
        IteratorAdapter<CharSequence, String> adapter = new IteratorAdapter<>(it);

        assertTrue(adapter.hasNext());
        assertEquals("A", adapter.next());
        assertTrue(adapter.hasNext());
        assertEquals("B", adapter.next());
        assertTrue(adapter.hasNext());
        assertEquals("C", adapter.next());
        assertFalse(adapter.hasNext());
        assertThrows(NoSuchElementException.class, adapter::next);
    }
}
