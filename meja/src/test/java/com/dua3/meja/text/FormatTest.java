package com.dua3.meja.text;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.BiFunction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.RefOption;
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
            System.out.format("--- WORKAROUND --- caught IOException, trying to fix path%n");
            // WORKAROUND - If anyone knows a less hackish solution for this, please send a pull request!

            // When tests are run from within gradle, resources are placed in another location (outside of classpath).
            // In that case, we try to guess the correct location of the resource files to be able to perform the tests.
            System.err.println("Resource not found! "+e.getMessage());
            String pathStr = clazz.getResource(".").getPath();

            System.out.format("--- WORKAROUND --- current path is %s%n", pathStr);

            // When started from within Bash on windows, a slash is prepended to the path returned by getResource.
            // We have to remove it again.
            if (pathStr.matches("^/[a-zA-Z]:/.*")) {
                System.out.format("FIXING PATH%n");
                pathStr = pathStr.replaceFirst("^/", "");
                System.out.format("--- WORKAROUND --- fix windows bash path to %s%n", pathStr);
            }

            // Change the path so that it points to the probable resource dir.
            String s = pathStr.replaceAll("/build/classes/java/test/", "/build/resources/test/");
            System.out.format("--- WORKAROUND --- path to resources is now %s%n", pathStr);

            // Then try to load the workbook from there.
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
    public void testFormat_getAsText() {
        testHelper((cell,locale) -> cell.getAsText(locale).toString());
    }

    @Test
    public void testFormat_toString() {
        testHelper((cell,locale) -> cell.toString(locale));
    }

    /**
     * Test formatting.
     * <p>
     * The workboook 'FormatTest.xlsx' is read from the classpath. Each sheet contains for columns used for testing:
     * <ul>
     * <li> A Flag to indicate ignored test cases
     * <li> description of what is being tested in the current row
     * <li> the locale to use
     * <li> value with an applied format to be tested
     * <li> the expected result as a {@code String}
     * <li> an optional remark
     * </ul>
     * </p>
     * @param extract a lambda expression maps (Cell, Locale) -> (formatted cell content)
     */
    public void testHelper(BiFunction<Cell,Locale,String> extract) {
        workbook.sheets()
            .peek(s -> System.out.format("Processing sheet '%s'%n", s.getSheetName()))
            .forEach(s -> {
                s.rows()
                    .skip(1)
                    .forEach(r -> {
                        boolean ignored = r.getCell(0).toString().equalsIgnoreCase("x");
                        if (ignored) {
                            System.out.format("line %d ignored%n", r.getRowNumber()+1);
                        } else {
                            String description = r.getCell(1).toString();

                            Cell languageCell = r.getCell(2);
                            String languageTag = languageCell.toString();
                            Locale locale = Locale.forLanguageTag(languageTag);
                            if (!languageTag.equals(locale.toLanguageTag())) {
                                throw new IllegalStateException("Check language tag in cell "+languageCell.getCellRef(RefOption.WITH_SHEET));
                            }

                            String actual = extract.apply(r.getCell(3), locale);
                            String expected = r.getCell(4).toString();
                            Assert.assertEquals(String.format("in line %d: %s - expected '%s', actual '%s'",
                                    r.getRowNumber()+1, description,
                                    expected, actual),
                                    expected, actual);
                        }
                    });
            });
    }
}
