package com.dua3.meja.ui;

import com.dua3.meja.model.Direction;
import com.dua3.utility.math.geometry.Rectangle2f;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SheetViewTest {

    @Test
    void testSheetArea() {
        SheetView.SheetArea empty = SheetView.SheetArea.EMPTY;
        assertEquals(0, empty.startRow());
        assertEquals(0, empty.startColumn());
        assertEquals(0, empty.endRow());
        assertEquals(0, empty.endColumn());
        assertEquals(Rectangle2f.of(0, 0, 0, 0), empty.rect());

        Rectangle2f rect = Rectangle2f.of(10, 20, 100, 200);
        SheetView.SheetArea area = new SheetView.SheetArea(rect, 1, 2, 5, 8);
        assertEquals(rect, area.rect());
        assertEquals(1, area.startRow());
        assertEquals(2, area.startColumn());
        assertEquals(5, area.endRow());
        assertEquals(8, area.endColumn());
    }

    @Test
    void testQuadrant() {
        int rowCount = 100;
        int columnCount = 50;
        int splitRow = 10;
        int splitCol = 5;

        // TOP_LEFT
        SheetView.Quadrant tl = SheetView.Quadrant.TOP_LEFT;
        assertTrue(tl.isTop());
        assertTrue(tl.isLeft());
        assertEquals(0, tl.startRow(rowCount, splitRow));
        assertEquals(splitRow, tl.endRow(rowCount, splitRow));
        assertEquals(0, tl.startColumn(columnCount, splitCol));
        assertEquals(splitCol, tl.endColumn(columnCount, splitCol));

        // TOP_RIGHT
        SheetView.Quadrant tr = SheetView.Quadrant.TOP_RIGHT;
        assertTrue(tr.isTop());
        assertFalse(tr.isLeft());
        assertEquals(0, tr.startRow(rowCount, splitRow));
        assertEquals(splitRow, tr.endRow(rowCount, splitRow));
        assertEquals(splitCol, tr.startColumn(columnCount, splitCol));
        assertEquals(columnCount, tr.endColumn(columnCount, splitCol));

        // BOTTOM_LEFT
        SheetView.Quadrant bl = SheetView.Quadrant.BOTTOM_LEFT;
        assertFalse(bl.isTop());
        assertTrue(bl.isLeft());
        assertEquals(splitRow, bl.startRow(rowCount, splitRow));
        assertEquals(rowCount, bl.endRow(rowCount, splitRow));
        assertEquals(0, bl.startColumn(columnCount, splitCol));
        assertEquals(splitCol, bl.endColumn(columnCount, splitCol));

        // BOTTOM_RIGHT
        SheetView.Quadrant br = SheetView.Quadrant.BOTTOM_RIGHT;
        assertFalse(br.isTop());
        assertFalse(br.isLeft());
        assertEquals(splitRow, br.startRow(rowCount, splitRow));
        assertEquals(rowCount, br.endRow(rowCount, splitRow));
        assertEquals(splitCol, br.startColumn(columnCount, splitCol));
        assertEquals(columnCount, br.endColumn(columnCount, splitCol));
    }

    @Test
    void testActions() {
        AtomicReference<String> lastAction = new AtomicReference<>("");

        SheetView view = new TestSheetView() {
            @Override
            public void move(Direction direction) {
                lastAction.set("move:" + direction);
            }

            @Override
            public void movePage(Direction direction) {
                lastAction.set("movePage:" + direction);
            }

            @Override
            public void moveHome() {
                lastAction.set("moveHome");
            }

            @Override
            public void moveEnd() {
                lastAction.set("moveEnd");
            }

            @Override
            public void startEditing() {
                lastAction.set("startEditing");
            }

            @Override
            public void showSearchDialog() {
                lastAction.set("showSearchDialog");
            }

            @Override
            public void copyToClipboard() {
                lastAction.set("copyToClipboard");
            }
        };

        SheetView.Actions.MOVE_UP.action().accept(view);
        assertEquals("move:" + Direction.NORTH, lastAction.get());

        SheetView.Actions.MOVE_DOWN.action().accept(view);
        assertEquals("move:" + Direction.SOUTH, lastAction.get());

        SheetView.Actions.MOVE_LEFT.action().accept(view);
        assertEquals("move:" + Direction.WEST, lastAction.get());

        SheetView.Actions.MOVE_RIGHT.action().accept(view);
        assertEquals("move:" + Direction.EAST, lastAction.get());

        SheetView.Actions.PAGE_UP.action().accept(view);
        assertEquals("movePage:" + Direction.NORTH, lastAction.get());

        SheetView.Actions.PAGE_DOWN.action().accept(view);
        assertEquals("movePage:" + Direction.SOUTH, lastAction.get());

        SheetView.Actions.MOVE_HOME.action().accept(view);
        assertEquals("moveHome", lastAction.get());

        SheetView.Actions.MOVE_END.action().accept(view);
        assertEquals("moveEnd", lastAction.get());

        SheetView.Actions.START_EDITING.action().accept(view);
        assertEquals("startEditing", lastAction.get());

        SheetView.Actions.SHOW_SEARCH_DIALOG.action().accept(view);
        assertEquals("showSearchDialog", lastAction.get());

        SheetView.Actions.COPY.action().accept(view);
        assertEquals("copyToClipboard", lastAction.get());
    }

    @Test
    void testDefaultMethods() {
        com.dua3.meja.model.generic.GenericWorkbook wb = com.dua3.meja.model.generic.GenericWorkbookFactory.instance().create();
        com.dua3.meja.model.generic.GenericSheet sheet = wb.createSheet("TestSheet");
        sheet.getCell(5, 5).set("Cell");

        TestSheetView view = new TestSheetView();
        SheetViewDelegate delegate = new SheetViewDelegate(sheet, view) {};
        view.delegate = delegate;

        assertSame(sheet, view.getSheet());

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
        assertTrue(sheet.getCurrentCell().getRowNumber() >= 4);
    }

    private static class TestSheetView implements SheetView {
        SheetViewDelegate delegate;

        @Override public void scrollToCurrentCell() {/* ignore */}
        @Override public void stopEditing(boolean commit) {/* ignore */}
        @Override public SheetViewDelegate getDelegate() { return delegate; }
        @Override public void repaintCell(com.dua3.meja.model.Cell cell) {/* ignore */}
        @Override public void updateContent() {/* ignore */}
        @Override public void focusView() {/* ignore */}
        @Override public void setAllowOpenLinks(boolean allowOpenLinks) {/* ignore */}
        @Override public boolean getAllowOpenLinks() { return false; }
        @Override public java.util.Locale getLocale() { return java.util.Locale.US; }
        @Override public com.dua3.utility.math.geometry.Scale2f getDisplayScale() { return com.dua3.utility.math.geometry.Scale2f.of(1, 1); }
        @Override public void copyToClipboard() {/* ignore */}
        @Override public void showSearchDialog() {/* ignore */}
        @Override public boolean isEditable() { return true; }
        @Override public void startEditing() {/* ignore */}
    }
}
