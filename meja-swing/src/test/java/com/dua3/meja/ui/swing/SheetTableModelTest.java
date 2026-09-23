package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.util.TableOptions;
import org.junit.jupiter.api.Test;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SheetTableModelTest {

    @Test
    void testTableModelWithoutHeader() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Sheet1");
        sheet.getCell(0, 0).set("A1");
        sheet.getCell(0, 1).set("B1");
        sheet.getCell(1, 0).set("A2");
        sheet.getCell(1, 1).set("B2");

        SheetTableModel model = new SheetTableModel(sheet, TableOptions.EDITABLE);

        assertEquals(2, model.getRowCount());
        assertEquals(2, model.getColumnCount());
        assertEquals("A", model.getColumnName(0));
        assertEquals("B", model.getColumnName(1));

        assertEquals("A1", String.valueOf(model.getValueAt(0, 0)));
        assertEquals("B1", String.valueOf(model.getValueAt(0, 1)));
        assertEquals("A2", String.valueOf(model.getValueAt(1, 0)));
        assertEquals("B2", String.valueOf(model.getValueAt(1, 1)));

        assertTrue(model.isCellEditable(0, 0));

        model.setValueAt("NewA1", 0, 0);
        assertEquals("NewA1", String.valueOf(model.getValueAt(0, 0)));
        assertEquals("NewA1", sheet.getCell(0, 0).getText().toString());
    }

    @Test
    void testTableModelWithHeader() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("SheetWithHeader");
        // Row 0 is header
        sheet.getCell(0, 0).set("Header1");
        sheet.getCell(0, 1).set("Header2");
        // Row 1 is data
        sheet.getCell(1, 0).set("Val1");
        sheet.getCell(1, 1).set("Val2");

        SheetTableModel model = new SheetTableModel(sheet, TableOptions.FIRST_ROW_IS_HEADER, TableOptions.EDITABLE);

        assertEquals(1, model.getRowCount());
        assertEquals(2, model.getColumnCount());
        assertEquals("Header1", model.getColumnName(0));
        assertEquals("Header2", model.getColumnName(1));

        // JTable row 0 maps to sheet row 1
        assertEquals("Val1", String.valueOf(model.getValueAt(0, 0)));
        assertEquals("Val2", String.valueOf(model.getValueAt(0, 1)));

        // Test setValueAt with header
        model.setValueAt("UpdatedVal1", 0, 0);
        assertEquals("UpdatedVal1", String.valueOf(model.getValueAt(0, 0)));
        assertEquals("UpdatedVal1", sheet.getCell(1, 0).getText().toString());
        assertEquals("Header1", sheet.getCell(0, 0).getText().toString()); // header unchanged
    }

    @Test
    void testNotEditable() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("ReadOnly");
        SheetTableModel model = new SheetTableModel(sheet); // default is not editable
        assertFalse(model.isCellEditable(0, 0));
    }

    @Test
    void testListenerAndEvents() throws Exception {
        try (GenericWorkbook wb = GenericWorkbookFactory.instance().create()) {
            GenericSheet sheet = wb.createSheet("EventsSheet");
            sheet.getCell(0, 0).set("H1");
            sheet.getCell(1, 0).set("D1");

            SheetTableModel model = new SheetTableModel(sheet, TableOptions.FIRST_ROW_IS_HEADER);

            List<TableModelEvent> events = new java.util.concurrent.CopyOnWriteArrayList<>();
            TableModelListener listener = events::add;

            model.addTableModelListener(listener);

            // 1. Cell value changed on data row (row 1 in sheet -> row 0 in table)
            Cell dataCell = sheet.getCell(1, 0);
            dataCell.set("D1_Modified");
            awaitEvents(events, 1);
            assertEquals(1, events.size());
            TableModelEvent e1 = events.get(0);
            assertEquals(0, e1.getFirstRow());
            assertEquals(0, e1.getLastRow());
            assertEquals(0, e1.getColumn());
            assertEquals(TableModelEvent.UPDATE, e1.getType());

            // 2. Cell value changed on header row (row 0 in sheet -> header row in table)
            Cell headerCell = sheet.getCell(0, 0);
            headerCell.set("H1_Modified");
            awaitEvents(events, 2);
            assertTrue(events.size() >= 2);
            TableModelEvent e2 = events.get(1);
            assertEquals(TableModelEvent.HEADER_ROW, e2.getFirstRow());
            assertEquals(TableModelEvent.ALL_COLUMNS, e2.getColumn());

            // 3. Rows added
            int countBeforeRow = events.size();
            sheet.getCell(2, 0).set("NewRow");
            awaitEvents(events, countBeforeRow + 1);
            assertTrue(events.size() > countBeforeRow);

            // 4. Columns added
            int countBeforeCol = events.size();
            sheet.getCell(0, 2).set("NewCol");
            awaitEvents(events, countBeforeCol + 1);
            assertTrue(events.size() > countBeforeCol);

            model.removeTableModelListener(listener);

            // After removing listener, further changes should not produce new events
            int finalCount = events.size();
            sheet.getCell(1, 0).set("AnotherChange");
            Thread.sleep(100);
            assertEquals(finalCount, events.size());
        }
    }

    private static void awaitEvents(List<TableModelEvent> events, int expectedCount) {
        long deadline = System.currentTimeMillis() + 3000;
        while (events.size() < expectedCount && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
