package com.dua3.meja.text;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.MejaHelper;

public class FormatTest {

    private Workbook workbook;

    @Before
    public void initialize() throws Exception {
        String fileName = getClass().getSimpleName()+".xlsx";
        Path path = Paths.get(getClass().getResource(fileName).toURI());
        workbook = MejaHelper.openWorkbook(path);
    }

    @After
    public void cleanup() throws IOException {
        workbook.close();
        workbook = null;
    }

    /**
     * Test formatting.
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
    public void testFormat() {
        System.out.format("Testing Workbook %s (workbook locale is %s)%n", workbook.getPath(), workbook.getLocale());

        workbook.sheets()
            .peek(s -> System.out.format("Processing sheet '%s'%n", s.getSheetName()))
            .forEach(s -> {
                s.rows()
                    .skip(1)
                    .forEach(r -> {
                        if (r.getCell(3).toString().contains("#IGNORE#")) {
                            System.out.format("line %d ignored%n", r.getRowNumber()+1);
                        } else {
                            String actual = r.getCell(1).toString();
                            String expected = r.getCell(2).toString();
                            Assert.assertEquals(String.format("in line %d: %s - expected '%s', actual '%s'",
                                    r.getRowNumber()+1, r.getCell(0).get(),
                                    expected, actual),
                                    expected, actual);
                        }
                    });
            });
    }
}
