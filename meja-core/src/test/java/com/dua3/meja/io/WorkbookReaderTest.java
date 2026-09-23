package com.dua3.meja.io;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WorkbookReaderTest {

    static class MockWorkbookReader extends WorkbookReader {
        URI lastUri;
        Workbook workbookMock;

        @SuppressWarnings("unchecked")
        @Override
        public <W extends Workbook> W read(WorkbookFactory<W> factory, URI uri, InputStream in) throws IOException {
            this.lastUri = uri;
            return (W) workbookMock;
        }
    }

    @Test
    void testDefaultMethods(@TempDir Path tempDir) throws IOException {
        MockWorkbookReader reader = new MockWorkbookReader();
        reader.setOptions(Arguments.of());

        Workbook workbook = (Workbook) Proxy.newProxyInstance(
                Workbook.class.getClassLoader(),
                new Class<?>[]{Workbook.class},
                (p, m, a) -> null
        );
        reader.workbookMock = workbook;

        WorkbookFactory<Workbook> factory = new WorkbookFactory<>() {
            @Override public Workbook create() { return workbook; }

            @Override public Workbook createStreaming() { return workbook; }

            @Override public Workbook open(URI uri, Arguments importSettings, InputStream in) { return workbook; }
        };

        Path file = tempDir.resolve("test.csv");
        Files.writeString(file, "header\nvalue");

        Workbook result = reader.read(factory, file.toUri());
        assertEquals(file.toUri(), reader.lastUri);
        assertSame(workbook, result);
    }
}
