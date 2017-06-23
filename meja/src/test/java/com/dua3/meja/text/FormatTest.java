package com.dua3.meja.text;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.io.FileSystemView;

public class FormatTest {

    private Workbook workbook;

    @Before
    public void initialize() throws Exception {
        Class<? extends FormatTest> clazz = getClass();
        String fileName = clazz.getSimpleName()+".xlsx";

        // the FileSystemView is needed in case the test is run from a jar file
        try (FileSystemView fsv = FileSystemView.create(clazz)) {
            Path wbPath = fsv.resolve(fileName);
            workbook = MejaHelper.openWorkbook(wbPath);
        } catch (IOException e) {
            // XXX when run from within gradle, resources are placed in another location
            System.err.println("Resource not found! "+e.getMessage());
            String s = clazz.getResource(".").getPath().replaceFirst("^/", "").replaceAll("/build/classes/java/test/", "/build/resources/test/");
            Path path = Paths.get(s);
            try (FileSystemView fsv = FileSystemView.create(path)) {
                Path wbPath = fsv.resolve(fileName);
                workbook = MejaHelper.openWorkbook(wbPath);
            }
        }
    }

    @After
    public void cleanup() throws IOException {
        workbook.close();
        workbook = null;
    }

    /**
     * Test formatting applied when calling Cell.toString().
     * <p>
     * The workboook 'FormatTest.xlsx' is read from the classpath. Each sheet contains for columns used for testing:
     * <ul>
     * <li> description of what is being tested in the current row
     * <li> value with an applied format to be tested
     * <li> the expected result as a {@code String}
     * <li> an optional remark - if it contains the text {@literal #IGNORE#}, the row is skipped
     * </ul>
     * </p>
     */
    @Test
    public void testFormat_toString() {
        workbook.sheets()
            .peek(s -> System.out.format("Processing sheet '%s'%n", s.getSheetName()))
            .forEach(s -> {
                s.rows()
                    .skip(1)
                    .forEach(r -> {
                        if (r.getCell(3).toString().contains("#IGNORE#")) {
                            System.out.format("line %d ignored%n", r.getRowNumber()+1);
                        } else {
                            String description = r.getCell(0).toString();
                            String actual = r.getCell(1).toString();
                            String expected = r.getCell(2).toString();
                            Assert.assertEquals(String.format("in line %d: %s - expected '%s', actual '%s'",
                                    r.getRowNumber()+1, description,
                                    expected, actual),
                                    expected, actual);
                        }
                    });
            });
    }

    /**
     * Test formatting applied when calling Cell.toString().
     * <p>
     * The workboook 'FormatTest.xlsx' is read from the classpath. Each sheet contains for columns used for testing:
     * <ul>
     * <li> description of what is being tested in the current row
     * <li> value with an applied format to be tested
     * <li> the expected result as a {@code String}
     * <li> an optional remark - if it contains the text {@literal #IGNORE#}, the row is skipped
     * </ul>
     * </p>
     */
    @Test
    public void testFormat_asText() {
        Locale locale = Locale.GERMAN;

        workbook.sheets()
            .peek(s -> System.out.format("Processing sheet '%s'%n", s.getSheetName()))
            .forEach(s -> {
                s.rows()
                    .skip(1)
                    .forEach(r -> {
                        if (r.getCell(3).toString().contains("#IGNORE#")) {
                            System.out.format("line %d ignored%n", r.getRowNumber()+1);
                        } else {
                            String description = r.getCell(0).toString();
                            String actual = r.getCell(1).getAsText(locale).toString();
                            String expected = r.getCell(2).toString();
                            Assert.assertEquals(String.format("in line %d: %s - expected '%s', actual '%s'",
                                    r.getRowNumber()+1, description,
                                    expected, actual),
                                    expected, actual);
                        }
                    });
            });
    }
}
