package com.dua3.meja.ui.fx;

import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FxRowTest {

    @Test
    void testIndexRecordValid() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");
        com.dua3.meja.model.Row row = sheet.getRow(1);

        FxRow.Index index = new FxRow.Index(0, 1, row);
        assertEquals(0, index.rowIndex());
        assertEquals(1, index.rowNumber());
        assertSame(row, index.row());

        FxRow.Index same = new FxRow.Index(0, 1, row);
        assertEquals(index, same);
        assertEquals(index.hashCode(), same.hashCode());
        assertNotNull(index.toString());

        FxRow.Index nullRow = new FxRow.Index(0, 1, null);
        assertNull(nullRow.row());
    }
}
