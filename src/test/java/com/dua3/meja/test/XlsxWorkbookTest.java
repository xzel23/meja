package com.dua3.meja;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiWorkbookFactory;

public class XlsxWorkbookTest {

    private Workbook workbook;

    @BeforeEach
    public void initialize() throws IOException {
        workbook = PoiWorkbookFactory.instance().createXlsx();
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
