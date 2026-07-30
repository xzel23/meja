package com.dua3.meja.model.generic.io;

import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.utility.io.IoOptions;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FileTypeCsvTest {

    @Test
    void readFromInputStream() throws Exception {
        Path input = Path.of("../testdata/population by country_US.csv").toAbsolutePath().normalize();
        FileTypeCsv fileType = FileTypeCsv.instance();

        try (InputStream in = Files.newInputStream(input);
             GenericWorkbook workbook = fileType.read(input.toUri(), in, type -> {
                 assertSame(fileType, type);
                 return Arguments.of(
                         Arguments.createEntry(IoOptions.OPTION_FIELD_SEPARATOR, ';'),
                         Arguments.createEntry(IoOptions.OPTION_LOCALE, Locale.US)
                 );
             })) {
            assertEquals(1, workbook.getSheetCount());
            assertEquals("Country (or dependency)", Objects.toString(workbook.getSheet(0).getCell(0, 0).getText()));
        }
    }
}
