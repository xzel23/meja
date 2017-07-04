package com.dua3.meja;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;

public class GenericWorkbookTest {

    private Workbook workbook;

    @Before
    public void initialize() throws Exception {
        workbook = PoiWorkbookFactory.instance().createXls();
        workbook.copy(WorkbookTestHelper.loadWorkbook());
    }

    @After
    public void cleanup() throws IOException {
        workbook.close();
        workbook = null;
    }

    /**
     * @see WorkbookTestHelper#testFormat_getAsText(Workbook)
     */
    @Test
    public void testFormat_getAsText() {
        WorkbookTestHelper.testFormat_getAsText(workbook);
    }

    /**
     * @see WorkbookTestHelper#testFormat_toString(Workbook)
     */
    @Test
    public void testFormat_toString() {
        WorkbookTestHelper.testFormat_toString(workbook);
    }

}
