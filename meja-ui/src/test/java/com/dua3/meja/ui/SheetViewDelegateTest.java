package com.dua3.meja.ui;

import com.dua3.meja.model.*;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.ui.Graphics;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;

class SheetViewDelegateTest {

    static class ConcreteSheetViewDelegate extends SheetViewDelegate {
        ConcreteSheetViewDelegate(Sheet sheet, SheetView owner) {
            super(sheet, owner);
        }
    }

    static class MockSheetView implements SheetView {
        final List<String> calls = new ArrayList<>();
        boolean editable = true;
        Scale2f displayScale = Scale2f.identity();

        @Override public void scrollToCurrentCell() { calls.add("scrollToCurrentCell"); }
        @Override public void stopEditing(boolean commit) { calls.add("stopEditing:" + commit); }
        @Override public SheetViewDelegate getDelegate() { return null; }
        @Override public void repaintCell(Cell cell) { calls.add("repaintCell:" + cell.getCellRef()); }
        @Override public void updateContent() { calls.add("updateContent"); }
        @Override public void focusView() { calls.add("focusView"); }
        @Override public void setAllowOpenLinks(boolean allowOpenLinks) { calls.add("setAllowOpenLinks:" + allowOpenLinks); }
        @Override public boolean getAllowOpenLinks() { return true; }
        @Override public Locale getLocale() { return Locale.US; }
        @Override public Scale2f getDisplayScale() { return displayScale; }
        @Override public void copyToClipboard() { calls.add("copyToClipboard"); }
        @Override public void showSearchDialog() { calls.add("showSearchDialog"); }
        @Override public boolean isEditable() { return editable; }
        @Override public void startEditing() { calls.add("startEditing"); }
    }

    @Test
    void testBasicPropertiesAndLayout() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Sheet1");
        sheet.getCell(0, 0).set("Hello");
        sheet.getCell(4, 4).set("World");

        MockSheetView view = new MockSheetView();
        ConcreteSheetViewDelegate delegate = new ConcreteSheetViewDelegate(sheet, view);

        assertSame(sheet, delegate.getSheet());
        delegate.requestFocus();
        assertTrue(view.calls.contains("focusView"));

        // Locks
        try (var readLock = delegate.readLock("testRead")) {
            assertNotNull(readLock);
        }
        try (var writeLock = delegate.writeLock("testWrite")) {
            assertNotNull(writeLock);
        }

        // Layout dimensions
        assertTrue(delegate.getSheetWidthInPoints() > 0);
        assertTrue(delegate.getSheetHeightInPoints() > 0);
        assertTrue(delegate.getSheetWidthInPixels() > 0);
        assertTrue(delegate.getSheetHeightInPixels() > 0);
        assertTrue(delegate.getRowLabelWidthInPoints() > 0);
        assertTrue(delegate.getColumnLabelHeightInPoints() > 0);

        assertEquals(5, delegate.getRowCount());
        assertEquals(5, delegate.getColumnCount());

        assertTrue(delegate.getRowPos(0) >= 0);
        assertTrue(delegate.getColumnPos(0) >= 0);

        Rectangle2f totalArea = delegate.getTotalArea();
        assertNotNull(totalArea);
        assertTrue(totalArea.width() > 0);
        assertTrue(totalArea.height() > 0);
    }

    @Test
    void testColorsAndFonts() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Colors");
        MockSheetView view = new MockSheetView();
        ConcreteSheetViewDelegate delegate = new ConcreteSheetViewDelegate(sheet, view);

        // Font
        Font font = FontUtil.getInstance().getDefaultFont().withSize(12);
        delegate.setLabelFont(font);
        assertEquals(font, delegate.getLabelFont());

        // Colors
        delegate.setSelectionColor(Color.RED);
        assertEquals(Color.RED, delegate.getSelectionColor());

        delegate.setGridColor(Color.BLUE);
        assertEquals(Color.BLUE, delegate.getGridColor());

        delegate.setBackground(Color.YELLOW);
        assertEquals(Color.YELLOW, delegate.getBackground());

        delegate.setLabelBackgroundColor(Color.valueOf("#00FFFF"));
        assertEquals(Color.valueOf("#00FFFF"), delegate.getLabelBackgroundColor());

        delegate.setLabelBorderColor(Color.valueOf("#FF00FF"));
        assertEquals(Color.valueOf("#FF00FF"), delegate.getLabelBorderColor());

        delegate.setLabelBorderWidthInPixels(2.5f);
        assertEquals(2.5f, delegate.getLabelBorderWidthInPixels());

        delegate.setSelectionStrokeWidth(3.0f);
        assertEquals(3.0f, delegate.getSelectionStrokeWidth());
    }

    @Test
    void testNavigationAndCurrentCell() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Nav");
        sheet.getCell(0, 0).set("Start");
        sheet.getCell(9, 9).set("End");

        MockSheetView view = new MockSheetView();
        ConcreteSheetViewDelegate delegate = new ConcreteSheetViewDelegate(sheet, view);

        assertEquals(0, sheet.getCurrentCell().getRowNumber());
        assertEquals(0, sheet.getCurrentCell().getColumnNumber());

        assertTrue(delegate.setCurrentCell(2, 3));
        assertEquals(2, sheet.getCurrentCell().getRowNumber());
        assertEquals(3, sheet.getCurrentCell().getColumnNumber());

        // Move directions
        delegate.move(Direction.NORTH);
        assertEquals(1, sheet.getCurrentCell().getRowNumber());
        delegate.move(Direction.SOUTH);
        assertEquals(2, sheet.getCurrentCell().getRowNumber());
        delegate.move(Direction.WEST);
        assertEquals(2, sheet.getCurrentCell().getColumnNumber());
        delegate.move(Direction.EAST);
        assertEquals(3, sheet.getCurrentCell().getColumnNumber());

        // Move home and end
        delegate.moveEnd();
        assertEquals(9, sheet.getCurrentCell().getRowNumber());
        assertEquals(9, sheet.getCurrentCell().getColumnNumber());

        delegate.moveHome();
        assertEquals(0, sheet.getCurrentCell().getRowNumber());
        assertEquals(0, sheet.getCurrentCell().getColumnNumber());
    }

    @Test
    void testEditingLifecycle() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Edit");
        sheet.getCell(0, 0).set("Init");

        MockSheetView view = new MockSheetView();
        ConcreteSheetViewDelegate delegate = new ConcreteSheetViewDelegate(sheet, view);

        assertFalse(delegate.isEditing());
        assertTrue(delegate.getEditingCell().isEmpty());

        delegate.startEditing();
        assertTrue(delegate.isEditing());
        assertTrue(delegate.getEditingCell().isPresent());

        delegate.stopEditing();
        assertFalse(delegate.isEditing());
        assertTrue(delegate.getEditingCell().isEmpty());
    }

    @Test
    void testSplitAndNames() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Split");
        sheet.getCell(5, 5).set("Test");

        MockSheetView view = new MockSheetView();
        ConcreteSheetViewDelegate delegate = new ConcreteSheetViewDelegate(sheet, view);

        delegate.setSplitRow(2);
        delegate.setSplitColumn(2);
        assertEquals(2, delegate.getSplitRow());
        assertEquals(2, delegate.getSplitColumn());

        // Custom names
        delegate.setRowNames(r -> "Row#" + r);
        delegate.setColumnNames(c -> "Col#" + c);
        assertEquals("Row#3", delegate.getRowName(3));
        assertEquals("Col#3", delegate.getColumnName(3));
    }

    @Test
    void testDrawLabel() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Labels");
        MockSheetView view = new MockSheetView();
        ConcreteSheetViewDelegate delegate = new ConcreteSheetViewDelegate(sheet, view);

        List<String> calls = new ArrayList<>();
        Graphics g = (Graphics) Proxy.newProxyInstance(
                Graphics.class.getClassLoader(),
                new Class<?>[]{Graphics.class},
                (proxy, method, args) -> {
                    calls.add(method.getName());
                    return null;
                }
        );

        delegate.drawLabel(g, Rectangle2f.of(0, 0, 50, 20), "TestLabel");
        assertTrue(calls.contains("setFill"));
        assertTrue(calls.contains("fillRect"));
        assertTrue(calls.contains("setStroke"));
        assertTrue(calls.contains("strokeRect"));
        assertTrue(calls.contains("drawText"));
    }

    @Test
    void testEvents() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Events");
        MockSheetView view = new MockSheetView();
        ConcreteSheetViewDelegate delegate = new ConcreteSheetViewDelegate(sheet, view);

        // Subscription lifecycle
        Flow.Subscription sub = (Flow.Subscription) Proxy.newProxyInstance(
                Flow.Subscription.class.getClassLoader(),
                new Class<?>[]{Flow.Subscription.class},
                (proxy, method, args) -> null
        );
        delegate.onSubscribe(sub);

        // Event dispatches
        Cell cell = sheet.getCell(0, 0);
        delegate.onNext(new SheetEvent.CellValueChanged(sheet, cell, "old", "new"));
        delegate.onNext(new SheetEvent.CellStyleChanged(sheet, cell, cell.getCellStyle(), cell.getCellStyle()));
        delegate.onNext(new SheetEvent.ActiveCellChanged(sheet, cell, sheet.getCell(1, 1)));
        delegate.onNext(new SheetEvent.LayoutChanged(sheet));
        delegate.onNext(new SheetEvent.RowsAdded(sheet, 0, 1));
        delegate.onNext(new SheetEvent.ColumnsAdded(sheet, 0, 1));
        delegate.onNext(new SheetEvent.ZoomChanged(sheet, 1.0f, 1.5f));
        delegate.onNext(new SheetEvent.SplitChanged(sheet, com.dua3.utility.data.Pair.of(0, 0), com.dua3.utility.data.Pair.of(1, 1)));

        assertTrue(view.calls.stream().anyMatch(c -> c.startsWith("repaintCell")));
        assertTrue(view.calls.contains("updateContent"));

        // Error & Complete
        delegate.onError(new RuntimeException("Test"));
        delegate.onComplete();
    }

    @Test
    void testCoordinateAndLayoutCalculations() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Calculations");
        sheet.getCell(10, 10).set("Test");
        sheet.splitAt(2, 2);

        MockSheetView view = new MockSheetView();
        ConcreteSheetViewDelegate delegate = new ConcreteSheetViewDelegate(sheet, view);

        // Pixel and Point dimensions
        assertTrue(delegate.getColumnWidthInPoints(0) > 0);
        assertTrue(delegate.getRowHeightInPoints(0) > 0);
        assertTrue(delegate.getRowHeightInPixels(0) > 0);
        assertTrue(delegate.getDefaultRowHeightInPixels() > 0);
        assertTrue(delegate.getDefaultColumnWidthInPixels() > 0);
        assertTrue(delegate.getRowLabelWidthInPixels() > 0);
        assertTrue(delegate.getColumnLabelHeightInPixels() > 0);

        assertTrue(delegate.getSplitLineWidth() > 0);
        assertTrue(delegate.getSplitLineHeight() > 0);
        assertTrue(delegate.getSplitLineWidthInPoints() > 0);
        assertTrue(delegate.getSplitLineHeightInPoints() > 0);
        assertTrue(delegate.getSplitXInPixels() > 0);
        assertTrue(delegate.getSplitYInPixels() > 0);

        // Coordinate to index lookups
        assertEquals(0, delegate.getRowNumberFromY(5.0f, true));
        assertEquals(0, delegate.getColumnNumberFromX(5.0f, true));

        // Scale and DPI
        delegate.setScale(Scale2f.of(2.0f, 2.0f));
        assertEquals(2.0f, delegate.getScale().sx());
        delegate.setDisplayScale(Scale2f.of(1.5f, 1.5f));
        assertEquals(1.5f, delegate.getDisplayScale().sx());
        delegate.update(96);

        // Move page
        delegate.movePage(Direction.SOUTH);
        delegate.movePage(Direction.NORTH);
        delegate.movePage(Direction.EAST);
        delegate.movePage(Direction.WEST);
    }
}
