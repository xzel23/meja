package com.dua3.meja.io;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.model.generic.io.FileTypeCsv;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FileTypeWorkbookTest {

    @Test
    void testFileTypeWorkbookWrite() throws IOException {
        FileTypeWorkbook<Workbook> fileType = FileTypeCsv.instance();
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("FTW");
        sheet.getCell(0, 0).set("FTWContent");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fileType.write(wb, URI.create("test.csv"), baos, ft -> Arguments.empty());

        assertTrue(baos.toString(StandardCharsets.UTF_8).contains("FTWContent"));
    }
}
