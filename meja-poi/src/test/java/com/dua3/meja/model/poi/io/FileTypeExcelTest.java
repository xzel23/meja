package com.dua3.meja.model.poi.io;

import com.dua3.meja.model.poi.PoiWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class FileTypeExcelTest {

    private static final Path TESTDATA = Path.of("../testdata").toAbsolutePath().normalize();

    @Test
    void readXlsFromInputStream() throws Exception {
        FileTypeExcel fileType = FileTypeExcel.instance();
        PoiWorkbook workbook = read(fileType, "population by country.xls", FileTypeXls.instance());
        try (workbook) {
            assertInstanceOf(PoiHssfWorkbook.class, workbook);
        }
    }

    @Test
    void readXlsxFromInputStream() throws Exception {
        FileTypeExcel fileType = FileTypeExcel.instance();
        PoiWorkbook workbook = read(fileType, "population by country.xlsx", FileTypeXlsx.instance());
        try (workbook) {
            assertInstanceOf(PoiXssfWorkbook.class, workbook);
        }
    }

    private static PoiWorkbook read(FileTypeExcel fileType, String filename, Object delegatedFileType) throws Exception {
        Path input = TESTDATA.resolve(filename);
        try (InputStream in = Files.newInputStream(input)) {
            return fileType.read(input.toUri(), in, type -> {
                assertSame(delegatedFileType, type);
                return Arguments.empty();
            });
        }
    }
}
