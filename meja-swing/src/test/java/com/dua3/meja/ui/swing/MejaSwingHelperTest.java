package com.dua3.meja.ui.swing;

import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.util.TableOptions;
import org.junit.jupiter.api.Test;

import javax.swing.table.TableModel;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class MejaSwingHelperTest {

    @Test
    void testGetTableModel() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");
        sheet.getCell(0, 0).set("X");

        TableModel model = MejaSwingHelper.getTableModel(sheet, TableOptions.EDITABLE);
        assertNotNull(model);
        assertEquals(1, model.getRowCount());
        assertEquals(1, model.getColumnCount());
        assertEquals("X", String.valueOf(model.getValueAt(0, 0)));
    }

    @Test
    void testOpenWorkbook(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws java.io.IOException {
        java.nio.file.Path csvFile = tempDir.resolve("test.csv");
        java.nio.file.Files.writeString(csvFile, "A,B\n1,2\n");

        javax.swing.JPanel parent = new javax.swing.JPanel();
        var optWb = MejaSwingHelper.openWorkbook(parent, csvFile.toUri());
        assertTrue(optWb.isPresent());
        assertEquals(1, optWb.get().getSheetCount());

        java.net.URI unknownUri = java.net.URI.create("file:///unknown.xyz");
        assertThrows(IllegalArgumentException.class, () -> MejaSwingHelper.openWorkbook(parent, unknownUri));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<MejaSwingHelper> constructor = MejaSwingHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        MejaSwingHelper instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
