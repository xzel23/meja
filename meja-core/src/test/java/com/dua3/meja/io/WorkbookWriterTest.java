package com.dua3.meja.io;

import com.dua3.meja.model.Workbook;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.DoubleConsumer;

import static org.junit.jupiter.api.Assertions.*;

class WorkbookWriterTest {

    static class MockWorkbookWriter implements WorkbookWriter {
        boolean wrote = false;

        @Override
        public void write(Workbook workbook, OutputStream out, DoubleConsumer updateProgress) throws IOException {
            wrote = true;
            out.write("TEST_OUTPUT".getBytes());
            updateProgress.accept(1.0);
        }
    }

    @Test
    void testDefaultMethods(@TempDir Path tempDir) throws IOException {
        MockWorkbookWriter writer = new MockWorkbookWriter();
        writer.setOptions(Arguments.of());

        Workbook workbook = (Workbook) Proxy.newProxyInstance(
                Workbook.class.getClassLoader(),
                new Class<?>[]{Workbook.class},
                (p, m, a) -> null
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writer.write(workbook, baos);
        assertTrue(writer.wrote);
        assertEquals("TEST_OUTPUT", baos.toString());

        Path file = tempDir.resolve("test.out");
        writer.wrote = false;
        writer.write(workbook, file.toUri());
        assertTrue(writer.wrote);
        assertEquals("TEST_OUTPUT", Files.readString(file));
    }
}
