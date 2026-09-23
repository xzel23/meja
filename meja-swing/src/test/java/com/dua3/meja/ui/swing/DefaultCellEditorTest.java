package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import org.junit.jupiter.api.Test;

import javax.swing.Action;
import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCellEditorTest {

    @Test
    void testCellEditorLifecycle() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("EditSheet");
        Cell cell = sheet.getCell(0, 0);
        cell.set("Initial");

        SwingSheetView view = new SwingSheetView(sheet);
        DefaultCellEditor editor = new DefaultCellEditor(view);

        assertFalse(editor.isEditing());

        var component = editor.startEditing(cell);
        assertNotNull(component);
        assertTrue(editor.isEditing());

        // Cancel editing
        editor.stopEditing(false);
        assertFalse(editor.isEditing());
        assertEquals("Initial", cell.getText().toString());

        // Start editing and commit
        editor.startEditing(cell);
        assertTrue(editor.isEditing());

        // Test actions
        Action newlineAction = DefaultCellEditor.Actions.NEWLINE.getAction(editor);
        newlineAction.actionPerformed(new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, "NEWLINE"));

        Action commitAction = DefaultCellEditor.Actions.COMMIT.getAction(editor);
        commitAction.actionPerformed(new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, "COMMIT"));
        assertFalse(editor.isEditing());

        // Abort action test
        editor.startEditing(cell);
        Action abortAction = DefaultCellEditor.Actions.ABORT.getAction(editor);
        abortAction.actionPerformed(new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, "ABORT"));
        assertFalse(editor.isEditing());
    }
}
