package com.dua3.meja.model.poi.io;

import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class FileTypeXlsxTest {

    @Test
    void readFromInputStream() throws Exception {
        Path input = Path.of("../testdata/population by country.xlsx").toAbsolutePath().normalize();
        FileTypeXlsx fileType = FileTypeXlsx.instance();

        try (InputStream in = Files.newInputStream(input);
             var workbook = fileType.read(input.toUri(), in, type -> {
                 assertSame(fileType, type);
                 return Arguments.empty();
             })) {
            assertInstanceOf(PoiXssfWorkbook.class, workbook);
        }
    }
}
