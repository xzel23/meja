package com.dua3.meja;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;

public class XlsWorkbookTest {

    private Workbook workbook;

    @Before
    public void initialize() throws Exception {
        workbook = GenericWorkbookFactory.instance().create();
        workbook.copy(WorkbookTestHelper.loadWorkbook());
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
    @Test @Ignore // FIXME remove once tests pass
    public void testFormat_getAsText() {
        WorkbookTestHelper.testFormat_getAsText(workbook);
    }

    @Test @Ignore // FIXME remove once tests pass
    public void testFormat_toString() {
        WorkbookTestHelper.testFormat_toString(workbook);
    }

}
