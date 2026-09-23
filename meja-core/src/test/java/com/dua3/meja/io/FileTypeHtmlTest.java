package com.dua3.meja.io;

import com.dua3.utility.io.OpenMode;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class FileTypeHtmlTest {

    @Test
    void testProperties() {
        FileTypeHtml html = FileTypeHtml.instance();
        assertNotNull(html);
        assertEquals("HTML", html.getName());
        assertTrue(html.isSupported(OpenMode.WRITE));
        assertFalse(html.isSupported(OpenMode.READ));
        assertTrue(html.getExtensions().contains("html"));
        assertTrue(html.getExtensions().contains("htm"));
    }

    @Test
    void testGetWorkbookWriter() {
        assertNotNull(FileTypeHtml.instance().getWorkbookWriter());
    }

    @Test
    @SuppressWarnings("java:S5778")
    void testUnsupportedOperations() {
        FileTypeHtml html = FileTypeHtml.instance();
        assertThrows(UnsupportedOperationException.class, html::getWorkbookFactory);
        assertThrows(UnsupportedOperationException.class, () -> html.read(URI.create("file:///test.html"), new ByteArrayInputStream(new byte[0]), ft -> null));
    }
}
