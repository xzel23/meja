package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import javafx.collections.ListChangeListener;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObservableSheetTest {

    @Test
    void testBasicPropertiesAndListOperations() throws IOException {
        try (GenericWorkbook wb = GenericWorkbookFactory.instance().create()) {
            GenericSheet sheet = wb.createSheet("ObservableSheet");
            sheet.getCell(0, 0).set("R0C0");
            sheet.getCell(2, 2).set("R2C2");

            ObservableSheet observableSheet = new ObservableSheet(sheet);

            assertEquals(3, observableSheet.size());
            assertEquals(2, observableSheet.get(2).getRowNumber());
            assertEquals(sheet.getRow(0), observableSheet.get(0));

            assertEquals(sheet.getZoom(), observableSheet.zoomProperty().get());
            assertEquals(sheet.getColumnCount(), observableSheet.columnCountProperty().get());
            assertEquals(sheet.getSplitRow(), observableSheet.splitRowProperty().get());
            assertEquals(sheet.getSplitColumn(), observableSheet.splitColumnProperty().get());
            assertEquals(sheet.getCurrentCell(), observableSheet.currentCellProperty().get());
        }
    }

    @Test
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    void testEventsAndPropertiesUpdate() throws Exception {
        try (GenericWorkbook wb = GenericWorkbookFactory.instance().create()) {
            GenericSheet sheet = wb.createSheet("Events");
            sheet.getCell(0, 0).set("Init");

            ObservableSheet observableSheet = new ObservableSheet(sheet);

            AtomicInteger rowsAddedCount = new AtomicInteger(0);
            observableSheet.addListener((ListChangeListener<com.dua3.meja.model.Row>) c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        rowsAddedCount.addAndGet(c.getAddedSize());
                    }
                }
            });

            // Add rows to sheet
            sheet.getCell(3, 0).set("Row3");
            long deadline = System.currentTimeMillis() + 3000;
            while (rowsAddedCount.get() < 3 && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertEquals(3, rowsAddedCount.get());

            // Zoom change
            sheet.setZoom(1.5f);
            deadline = System.currentTimeMillis() + 3000;
            while (observableSheet.zoomProperty().get() != 1.5f && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertEquals(1.5f, observableSheet.zoomProperty().get());

            // Split change
            sheet.splitAt(2, 4);
            deadline = System.currentTimeMillis() + 3000;
            while ((observableSheet.splitRowProperty().get() != 2 || observableSheet.splitColumnProperty().get() != 4) && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertEquals(2, observableSheet.splitRowProperty().get());
            assertEquals(4, observableSheet.splitColumnProperty().get());

            // Current cell change via sheet
            Cell targetCell = sheet.getCell(2, 2);
            sheet.setCurrentCell(targetCell);
            deadline = System.currentTimeMillis() + 3000;
            while (observableSheet.currentCellProperty().get() != targetCell && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertEquals(targetCell, observableSheet.currentCellProperty().get());

            // Current cell change via property
            Cell anotherCell = sheet.getCell(1, 1);
            observableSheet.currentCellProperty().set(anotherCell);
            assertEquals(anotherCell, sheet.getCurrentCell());

            // Layout listener
            AtomicBoolean layoutNotified = new AtomicBoolean(false);
            var listener = (java.util.function.Consumer<com.dua3.meja.model.Sheet>) s -> layoutNotified.set(true);
            observableSheet.addLayoutListener(listener);

            sheet.setColumnWidth(0, 50.0f);
            deadline = System.currentTimeMillis() + 3000;
            while (!layoutNotified.get() && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            assertTrue(layoutNotified.get());

            assertTrue(observableSheet.removeLayoutListener(listener));
            assertFalse(observableSheet.removeLayoutListener(listener));
        }
    }

    @Test
    void testEqualsAndHashCode() throws IOException {
        try (GenericWorkbook wb = GenericWorkbookFactory.instance().create()) {
            GenericSheet sheet1 = wb.createSheet("S1");
            GenericSheet sheet2 = wb.createSheet("S2");

            ObservableSheet os1 = new ObservableSheet(sheet1);
            ObservableSheet os2 = new ObservableSheet(sheet1);
            ObservableSheet os3 = new ObservableSheet(sheet2);

            assertEquals(os1, os2);
            assertEquals(os1.hashCode(), os2.hashCode());

            assertNotEquals(os1, os3);
            assertNotEquals(null, os1);
            assertNotEquals("String", os1);
        }
    }
}
