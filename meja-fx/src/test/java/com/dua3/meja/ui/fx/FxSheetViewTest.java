package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Direction;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.fx.PlatformHelper;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FxSheetViewTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Already started
        }
    }

    @Test
    void testFxSheetViewLifecycle() {
        PlatformHelper.runAndWait(() -> {
            GenericWorkbook wb = GenericWorkbookFactory.instance().create();
            GenericSheet sheet = wb.createSheet("FxTestSheet");
            sheet.getCell(0, 0).set("Start");
            sheet.getCell(5, 5).set("End");

            FxSheetView view = new FxSheetView(sheet);

            assertSame(sheet, view.getSheet());
            assertNotNull(view.getDelegate());
            assertNotNull(view.getObservableSheet());
            assertNotNull(view.getDisplayScale());
            assertNotNull(view.getLocale());
            assertNotNull(view.sheetScaleXProperty());
            assertNotNull(view.sheetScaleYProperty());

            // Editable
            view.setEditable(true);
            assertTrue(view.isEditable());
            assertTrue(view.editableProperty().get());
            view.setEditable(false);
            assertFalse(view.isEditable());

            // Allow open links
            view.setAllowOpenLinks(true);
            assertTrue(view.getAllowOpenLinks());
            view.setAllowOpenLinks(false);
            assertFalse(view.getAllowOpenLinks());

            // Toolbar parent
            Pane toolbarPane = new Pane();
            view.toolbarParentProperty().set(toolbarPane);
            assertSame(toolbarPane, view.toolbarParentProperty().get());

            // Navigation
            assertTrue(view.setCurrentCell(2, 3));
            assertEquals(2, sheet.getCurrentCell().getRowNumber());
            assertEquals(3, sheet.getCurrentCell().getColumnNumber());

            view.moveHome();
            assertEquals(0, sheet.getCurrentCell().getRowNumber());
            assertEquals(0, sheet.getCurrentCell().getColumnNumber());

            view.moveEnd();
            assertEquals(5, sheet.getCurrentCell().getRowNumber());
            assertEquals(5, sheet.getCurrentCell().getColumnNumber());

            view.move(Direction.NORTH);
            assertEquals(4, sheet.getCurrentCell().getRowNumber());

            view.movePage(Direction.SOUTH);

            // Repaint, focus, scroll
            assertDoesNotThrow(() -> view.repaintCell(sheet.getCell(0, 0)));
            assertDoesNotThrow(view::scrollToCurrentCell);
            assertDoesNotThrow(view::focusView);
            assertDoesNotThrow(view::copyToClipboard);
            assertDoesNotThrow(view::updateContent);

            // Editing lifecycle
            view.setEditable(true);
            view.startEditing();
            view.stopEditing(false);
            view.startEditing();
            view.stopEditing(true);
        });
    }
}
