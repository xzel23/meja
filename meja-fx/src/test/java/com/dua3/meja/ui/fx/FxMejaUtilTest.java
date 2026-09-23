package com.dua3.meja.ui.fx;

import com.dua3.utility.io.OpenMode;
import javafx.stage.FileChooser.ExtensionFilter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FxMejaUtilTest {

    @org.junit.jupiter.api.BeforeAll
    static void initJavaFx() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Platform already started
        }
    }

    @Test
    void testGetExtensionFiltersWithMode() {
        List<ExtensionFilter> readFilters = FxMejaUtil.getExtensionFilters(OpenMode.READ);
        assertNotNull(readFilters);
        assertFalse(readFilters.isEmpty());

        for (ExtensionFilter filter : readFilters) {
            assertNotNull(filter.getDescription());
            assertFalse(filter.getExtensions().isEmpty());
            for (String ext : filter.getExtensions()) {
                assertTrue(ext.startsWith("*."));
            }
        }

        List<ExtensionFilter> writeFilters = FxMejaUtil.getExtensionFilters(OpenMode.WRITE);
        assertNotNull(writeFilters);
        assertFalse(writeFilters.isEmpty());
    }

    @Test
    void testGetExtensionFilters() {
        List<ExtensionFilter> allFilters = FxMejaUtil.getExtensionFilters();
        assertNotNull(allFilters);
        assertFalse(allFilters.isEmpty());
    }

    @Test
    void testCopyToSheet() {
        javafx.scene.control.TableView<List<String>> table = new javafx.scene.control.TableView<>();
        javafx.scene.control.TableColumn<List<String>, String> col1 = new javafx.scene.control.TableColumn<>("Name");
        javafx.scene.control.TableColumn<List<String>, String> col2 = new javafx.scene.control.TableColumn<>("Age");
        table.getColumns().add(col1);
        table.getColumns().add(col2);

        table.getItems().add(List.of("Alice", "30"));
        table.getItems().add(List.of("Bob", "25"));

        com.dua3.meja.model.generic.GenericWorkbook wb = com.dua3.meja.model.generic.GenericWorkbookFactory.instance().create();
        com.dua3.meja.model.generic.GenericSheet sheet = wb.createSheet("CopiedSheet");

        FxMejaUtil.copyToSheet(table, sheet);

        assertEquals("Name", sheet.getCell(0, 0).getText().toString());
        assertEquals("Age", sheet.getCell(0, 1).getText().toString());
        assertEquals("Alice", sheet.getCell(1, 0).getText().toString());
        assertEquals("30", sheet.getCell(1, 1).getText().toString());
        assertEquals("Bob", sheet.getCell(2, 0).getText().toString());
        assertEquals("25", sheet.getCell(2, 1).getText().toString());
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<FxMejaUtil> constructor = FxMejaUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        FxMejaUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
