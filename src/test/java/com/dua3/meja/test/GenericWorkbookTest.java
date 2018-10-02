package com.dua3.meja.test;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;

public class GenericWorkbookTest {

    private Workbook workbook;

    @BeforeEach
    public void initialize() throws IOException {
        workbook = PoiWorkbookFactory.instance().createXls();
        workbook.copy(WorkbookTestHelper.loadWorkbook());
    }

    @AfterEach
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

    /**
     * @see WorkbookTestHelper#testMergeUnmerge(Workbook)
     */
    @Test
    public void testMergeUnmerge() {
        WorkbookTestHelper.testMergeUnmerge(workbook);
    }

}
