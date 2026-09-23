package com.dua3.meja.model.generic;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class GenericSheetTest {

    @Test
    void testCreateSheet() throws IOException {
        try (GenericWorkbook wb = GenericWorkbookFactory.instance().create()) {
            wb.createSheet("Test");
            wb.createSheet("foo");
            wb.createSheet("bar");

            assertEquals(3, wb.getSheetCount());
            assertEquals("Test", wb.getSheet(0).getSheetName());
            assertEquals("foo", wb.getSheet(1).getSheetName());
            assertEquals("bar", wb.getSheet(2).getSheetName());
            assertNotNull(wb.getSheetByName("Test"));
            assertNotNull(wb.getSheetByName("foo"));
            assertNotNull(wb.getSheetByName("bar"));
            assertEquals("Test", wb.getSheetByName("Test").getSheetName());
            assertEquals("foo", wb.getSheetByName("foo").getSheetName());
            assertEquals("bar", wb.getSheetByName("bar").getSheetName());
        }
    }

    @Test
    void testCreateRow() throws IOException {
        try (GenericWorkbook wb = GenericWorkbookFactory.instance().create()) {
            Sheet s = wb.createSheet("Test");

            Row r = s.createRow("a", 123.5, null, LocalDate.of(2023, 1, 1), true);

            assertEquals(1, s.getRowCount());

            Cell c0 = r.getCell(0);
            assertEquals(CellType.TEXT, c0.getCellType());
            assertEquals("a", c0.toString());

            Cell c1 = r.getCell(1);
            assertEquals(CellType.NUMERIC, c1.getCellType());
            assertEquals(123.5, c1.getNumber().doubleValue());
            assertEquals("123.5", c1.toString(Locale.US));
            assertEquals("123,5", c1.toString(Locale.GERMANY));
            assertEquals("123,5", c1.toString(Locale.FRANCE));

            Cell c2 = r.getCell(2);
            assertEquals(CellType.BLANK, c2.getCellType());

            Cell c3 = r.getCell(3);
            assertEquals(CellType.DATE, c3.getCellType());
            assertEquals(LocalDate.of(2023, 1, 1), c3.getDate());
            assertEquals("Jan 1, 2023", c3.toString(Locale.US));
            assertEquals("01.01.2023", c3.toString(Locale.GERMANY));
            assertEquals("1 janv. 2023", c3.toString(Locale.FRANCE));

            Cell c4 = r.getCell(4);
            assertEquals(CellType.BOOLEAN, c4.getCellType());
            assertEquals(Boolean.TRUE, c4.getBoolean());
        }
    }

    @Test
    void testLocks() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Locks");

        try (var readLock = sheet.readLock("testRead")) {
            assertNotNull(readLock);
        }

        try (var writeLock = sheet.writeLock("testWrite")) {
            assertNotNull(writeLock);
        }
    }

    @Test
    void testMergedRegions() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Merged");

        sheet.getCell(0, 0).set("TopLeft");
        com.dua3.meja.util.RectangularRegion region = new com.dua3.meja.util.RectangularRegion(0, 1, 0, 2);
        sheet.addMergedRegion(region);

        assertEquals(1, sheet.getMergedRegions().size());
        assertEquals(region, sheet.getMergedRegions().get(0));
        assertTrue(sheet.getMergedRegion(0, 0).isPresent());
        assertTrue(sheet.getMergedRegion(1, 2).isPresent());
        assertFalse(sheet.getMergedRegion(2, 2).isPresent());

        // Overlapping should fail
        com.dua3.meja.util.RectangularRegion overlap = new com.dua3.meja.util.RectangularRegion(1, 2, 1, 3);
        assertThrows(IllegalStateException.class, () -> sheet.addMergedRegion(overlap));

        // Check cell properties in merged region
        Cell tl = sheet.getCell(0, 0);
        assertTrue(tl.isMerged());
        assertEquals(3, tl.getHorizontalSpan());
        assertEquals(2, tl.getVerticalSpan());
        assertEquals(tl, tl.getLogicalCell());

        Cell child = sheet.getCell(1, 2);
        assertTrue(child.isMerged());
        assertEquals(0, child.getHorizontalSpan());
        assertEquals(0, child.getVerticalSpan());
        assertEquals(tl, child.getLogicalCell());

        // Unmerge
        tl.unMerge();
        assertEquals(0, sheet.getMergedRegions().size());
        assertFalse(tl.isMerged());
        assertFalse(child.isMerged());
    }

    @Test
    void testSplitAndZoom() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("SplitZoom");

        assertEquals(0, sheet.getSplitRow());
        assertEquals(0, sheet.getSplitColumn());
        sheet.splitAt(3, 2);
        assertEquals(3, sheet.getSplitRow());
        assertEquals(2, sheet.getSplitColumn());

        assertEquals(1.0f, sheet.getZoom());
        sheet.setZoom(1.5f);
        assertEquals(1.5f, sheet.getZoom());

        assertThrows(IllegalStateException.class, () -> sheet.setZoom(0.0f));
        assertThrows(IllegalStateException.class, () -> sheet.setZoom(-1.0f));
        assertThrows(IllegalStateException.class, () -> sheet.splitAt(-1, 0));
        assertThrows(IllegalStateException.class, () -> sheet.splitAt(0, -1));
    }

    @Test
    void testAutofilter() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Autofilter");

        assertEquals(-1, sheet.getAutoFilterRow());
        sheet.setAutofilterRow(2);
        assertEquals(2, sheet.getAutoFilterRow());

        assertThrows(IllegalStateException.class, () -> sheet.setAutofilterRow(-1));
    }

    @Test
    void testColumnWidthAndRowHeight() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Dimensions");

        sheet.setColumnWidth(1, 45.0f);
        assertEquals(45.0f, sheet.getColumnWidth(1));

        sheet.setRowHeight(2, 30.0f);
        assertEquals(30.0f, sheet.getRowHeight(2));

        assertTrue(sheet.getDefaultColumnWidth() > 0);
        assertTrue(sheet.getDefaultRowHeight() > 0);

        assertThrows(IllegalStateException.class, () -> sheet.setColumnWidth(0, -5.0f));
        assertThrows(IllegalStateException.class, () -> sheet.setRowHeight(0, -5.0f));
    }

    @Test
    void testAutoSize() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("AutoSize");

        sheet.getCell(0, 0).set("Short");
        sheet.getCell(1, 0).set("A much longer text inside column 0");
        sheet.getCell(0, 1).set("Line 1\nLine 2\nLine 3");

        sheet.autoSizeColumn(0);
        assertTrue(sheet.getColumnWidth(0) > 50.0f);

        sheet.autoSizeColumns();
        assertTrue(sheet.getColumnWidth(1) > 0.0f);

        sheet.autoSizeRow(0);
        assertTrue(sheet.getRowHeight(0) > 0.0f);
    }

    @Test
    void testCurrentCell() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("CurrentCell");

        Cell initial = sheet.getCurrentCell();
        assertNotNull(initial);
        assertEquals(0, initial.getRowNumber());
        assertEquals(0, initial.getColumnNumber());

        Cell target = sheet.getCell(3, 4);
        assertTrue(sheet.setCurrentCell(target));
        assertEquals(target, sheet.getCurrentCell());

        // Same cell again returns false
        assertFalse(sheet.setCurrentCell(target));

        // Setting cell from another sheet should throw
        GenericSheet otherSheet = wb.createSheet("Other");
        Cell otherCell = otherSheet.getCell(1, 1);
        assertThrows(IllegalArgumentException.class, () -> sheet.setCurrentCell(otherCell));
    }

    @Test
    void testFind() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("FindSheet");

        // Empty sheet find
        assertTrue(sheet.find("nothing", com.dua3.meja.model.SearchSettings.of()).isEmpty());

        sheet.getCell(0, 0).set("Hello World");
        sheet.getCell(1, 1).set("Foo Bar");
        sheet.getCell(2, 2).setFormula("SUM(A1:A2)");

        // Basic match
        var found = sheet.find("World", com.dua3.meja.model.SearchSettings.of());
        assertTrue(found.isPresent());
        assertEquals(0, found.get().getRowNumber());
        assertEquals(0, found.get().getColumnNumber());

        // Ignore case
        var foundCase = sheet.find("world", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.IGNORE_CASE));
        assertTrue(foundCase.isPresent());

        // Match complete
        var notMatchComplete = sheet.find("World", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.MATCH_COMPLETE_TEXT));
        assertTrue(notMatchComplete.isEmpty());

        var matchComplete = sheet.find("Hello World", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.MATCH_COMPLETE_TEXT));
        assertTrue(matchComplete.isPresent());

        // Search formula
        var foundFormula = sheet.find("SUM", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.SEARCH_FORMULA_TEXT));
        assertTrue(foundFormula.isPresent());
        assertEquals(2, foundFormula.get().getRowNumber());

        // Update current
        var updateCurrent = sheet.find("Foo", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND));
        assertTrue(updateCurrent.isPresent());
        assertEquals(sheet.getCell(1, 1), sheet.getCurrentCell());

        // Search from current
        var fromCurrent = sheet.find("Hello", com.dua3.meja.model.SearchSettings.of(com.dua3.meja.model.SearchOptions.SEARCH_FROM_CURRENT));
        assertTrue(fromCurrent.isPresent());
        assertEquals(sheet.getCell(0, 0), fromCurrent.get());

        // Not found
        assertTrue(sheet.find("NonExistentText", com.dua3.meja.model.SearchSettings.of()).isEmpty());
    }

    @Test
    void testIteratorAndStream() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Iter");

        sheet.getCell(0, 0).set("R0");
        sheet.getCell(2, 0).set("R2");

        assertEquals(3, sheet.getRowCount());
        assertEquals(3, sheet.rows().count());

        java.util.Iterator<Row> it = sheet.iterator();
        assertTrue(it.hasNext());
        assertEquals(0, it.next().getRowNumber());
        assertTrue(it.hasNext());
        assertEquals(1, it.next().getRowNumber());
        assertTrue(it.hasNext());
        assertEquals(2, it.next().getRowNumber());
        assertFalse(it.hasNext());
        assertThrows(java.util.NoSuchElementException.class, it::next);
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    @Test
    void testClearAndCopy() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet s1 = wb.createSheet("S1");
        s1.getCell(0, 0).set("Data");
        s1.splitAt(1, 1);
        s1.setAutofilterRow(0);
        s1.setColumnWidth(0, 50.0f);
        s1.setRowHeight(0, 35.0f);
        s1.addMergedRegion(new com.dua3.meja.util.RectangularRegion(1, 2, 1, 2));

        GenericSheet s2 = wb.createSheet("S2");
        s2.copy(s1);

        assertEquals("Data", s2.getCell(0, 0).toString());
        assertEquals(1, s2.getSplitRow());
        assertEquals(1, s2.getSplitColumn());
        assertEquals(0, s2.getAutoFilterRow());
        assertEquals(50.0f, s2.getColumnWidth(0));
        assertEquals(35.0f, s2.getRowHeight(0));
        assertEquals(1, s2.getMergedRegions().size());

        s2.clear();
        assertEquals(0, s2.getRowCount());
        assertEquals(0, s2.getMergedRegions().size());
        assertTrue(s2.isEmpty());
    }

    @Test
    void testEvents() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Events");

        java.util.List<com.dua3.meja.model.SheetEvent> received = new java.util.concurrent.CopyOnWriteArrayList<>();
        java.util.concurrent.Flow.Subscriber<com.dua3.meja.model.SheetEvent> subscriber = new java.util.concurrent.Flow.Subscriber<>() {

            @Override
            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(com.dua3.meja.model.SheetEvent item) {
                received.add(item);
            }

            @Override
            public void onError(Throwable throwable) {
                // ignore
            }

            @Override
            public void onComplete() {
                // ignore
            }
        };

        sheet.subscribe(subscriber);

        sheet.getCell(0, 0).set("Value");
        sheet.setZoom(1.2f);
        sheet.splitAt(1, 1);
        sheet.setCurrentCell(sheet.getCell(2, 2));

        // Sleep briefly to allow async Flow delivery
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            // do nothing
        }

        assertFalse(received.isEmpty());
        sheet.unsubscribe(subscriber);
    }

    @Test
    void testToString() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("StringRep");
        assertTrue(sheet.toString().contains("StringRep"));
    }
}
