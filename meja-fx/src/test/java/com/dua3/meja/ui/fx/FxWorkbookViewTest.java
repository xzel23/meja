package com.dua3.meja.ui.fx;

import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.fx.PlatformHelper;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FxWorkbookViewTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Already started
        }
    }

    @Test
    void testFxWorkbookViewLifecycle() {
        PlatformHelper.runAndWait(() -> {
            FxWorkbookView view = new FxWorkbookView();
            assertTrue(view.getWorkbook().isEmpty());
            assertTrue(view.getCurrentView().isEmpty());

            GenericWorkbook wb = GenericWorkbookFactory.instance().create();
            GenericSheet s1 = wb.createSheet("S1");
            s1.getCell(0, 0).set("A");
            GenericSheet s2 = wb.createSheet("S2");
            s2.getCell(0, 0).set("B");

            view.setWorkbook(wb);
            assertTrue(view.getWorkbook().isPresent());
            assertSame(wb, view.getWorkbook().get());
            assertSame(wb, view.workbookProperty().get());

            // Editable
            view.setEditable(true);
            assertTrue(view.isEditable());
            view.setEditable(false);
            assertFalse(view.isEditable());

            // Toolbar parent
            Pane pane = new Pane();
            view.setToolbarParent(pane);
            assertSame(pane, view.toolbarParentProperty().get());

            // View for sheet
            Optional<FxSheetView> optV1 = view.getViewForSheet(s1);
            assertTrue(optV1.isPresent());
            assertSame(s1, optV1.get().getSheet());

            Optional<FxSheetView> optV2 = view.getViewForSheet("S2");
            assertTrue(optV2.isPresent());
            assertSame(s2, optV2.get().getSheet());

            assertTrue(view.getViewForSheet("NonExistent").isEmpty());

            // Clear workbook
            view.setWorkbook(null);
            assertTrue(view.getWorkbook().isEmpty());
            assertTrue(view.getCurrentView().isEmpty());
        });
    }
}
