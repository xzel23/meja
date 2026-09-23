package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Direction;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SwingSheetViewTest {

    @Test
    void testSwingSheetViewBasic() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("TestSheet");
        sheet.getCell(0, 0).set("Start");
        sheet.getCell(5, 5).set("End");

        SwingSheetView view = new SwingSheetView(sheet);

        assertSame(sheet, view.getSheet());
        assertNotNull(view.getDelegate());
        assertNotNull(view.getLocale());
        assertNotNull(view.getDisplayScale());

        // Editable
        view.setEditable(false);
        assertFalse(view.isEditable());
        view.setEditable(true);
        assertTrue(view.isEditable());

        // Allow open links
        view.setAllowOpenLinks(true);
        assertTrue(view.getAllowOpenLinks());
        view.setAllowOpenLinks(false);
        assertFalse(view.getAllowOpenLinks());

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

        // Repaint and update
        assertDoesNotThrow(() -> view.repaintCell(sheet.getCell(0, 0)));
        assertDoesNotThrow(view::updateContent);
        assertDoesNotThrow(view::focusView);
        assertDoesNotThrow(view::scrollToCurrentCell);
        assertDoesNotThrow(view::copyToClipboard);

        // Editing lifecycle
        view.startEditing();
        view.stopEditing(false);
        view.startEditing();
        view.stopEditing(true);
    }
}
