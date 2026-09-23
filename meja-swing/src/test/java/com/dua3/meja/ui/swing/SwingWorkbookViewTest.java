package com.dua3.meja.ui.swing;

import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SwingWorkbookViewTest {

    @Test
    void testWorkbookViewLifecycle() throws Exception {
        SwingWorkbookView view = new SwingWorkbookView();
        assertTrue(view.getWorkbook().isEmpty());
        assertTrue(view.getCurrentView().isEmpty());

        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet s1 = wb.createSheet("FirstSheet");
        s1.getCell(0, 0).set("S1");
        GenericSheet s2 = wb.createSheet("SecondSheet");
        s2.getCell(0, 0).set("S2");

        view.setWorkbook(wb);
        assertTrue(view.getWorkbook().isPresent());
        assertSame(wb, view.getWorkbook().get());

        // Check editable
        assertFalse(view.isEditable());
        view.setEditable(true);
        assertTrue(view.isEditable());

        // Check view for sheet
        Optional<SwingSheetView> v1 = view.getViewForSheet(s1);
        assertTrue(v1.isPresent());
        assertSame(s1, v1.get().getSheet());

        Optional<SwingSheetView> v2 = view.getViewForSheet("SecondSheet");
        assertTrue(v2.isPresent());
        assertSame(s2, v2.get().getSheet());

        assertTrue(view.getViewForSheet("NonExistent").isEmpty());

        // Toolbar parent
        JPanel toolbarParent = new JPanel();
        view.setToolbarParent(toolbarParent);
        assertSame(toolbarParent, view.getToolbarParent());

        // Add third sheet to workbook to trigger WorkbookEvent
        GenericSheet s3 = wb.createSheet("ThirdSheet");
        assertNotNull(s3);
        long deadline = System.currentTimeMillis() + 3000;
        while (view.getViewForSheet("ThirdSheet").isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
            javax.swing.SwingUtilities.invokeAndWait(() -> {});
        }
        Optional<SwingSheetView> v3 = view.getViewForSheet("ThirdSheet");
        assertTrue(v3.isPresent());

        // Remove sheet to trigger WorkbookEvent
        wb.removeSheet(2);
        deadline = System.currentTimeMillis() + 3000;
        while (view.getViewForSheet("ThirdSheet").isPresent() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
            javax.swing.SwingUtilities.invokeAndWait(() -> {});
        }
        assertTrue(view.getViewForSheet("ThirdSheet").isEmpty());

        // Clear workbook
        view.setWorkbook(null);
        assertTrue(view.getWorkbook().isEmpty());
        assertTrue(view.getCurrentView().isEmpty());
    }
}
