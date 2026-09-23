package com.dua3.meja.ui;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.ui.Graphics;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class SheetPainterTest {

    @Test
    void testIntersection() {
        Rectangle2f r1 = Rectangle2f.of(0, 0, 100, 100);
        Rectangle2f r2 = Rectangle2f.of(50, 50, 100, 100);
        Rectangle2f isect = SheetPainter.intersection(r1, r2);
        assertEquals(50, isect.x());
        assertEquals(50, isect.y());
        assertEquals(50, isect.width());
        assertEquals(50, isect.height());

        // Disjoint
        Rectangle2f r3 = Rectangle2f.of(200, 200, 50, 50);
        Rectangle2f disjoint = SheetPainter.intersection(r1, r3);
        assertEquals(0, disjoint.width());
        assertEquals(0, disjoint.height());
    }

    @Test
    void testSheetPainterWithNullSheet() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");
        MockPainterDelegate delegate = new MockPainterDelegate(sheet);
        CellRenderer renderer = new CellRenderer(delegate);
        SheetPainter painter = new SheetPainter(delegate, renderer);

        List<String> calls = new ArrayList<>();
        Graphics g = createMockGraphics(calls);

        painter.drawSheet(g, Rectangle2f.of(0, 0, 200, 200));
        // sheet is null by default in painter, drawSheet does nothing
        assertTrue(calls.isEmpty());
    }

    @Test
    void testSheetPainterCompleteFlow() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");
        sheet.getCell(0, 0).set("A1");
        sheet.getCell(0, 1).set("B1");
        sheet.getCell(1, 0).set("A2");
        sheet.getCell(1, 1).set("B2");

        // Merge cells (2, 2) to (3, 3)
        sheet.getCell(2, 2).set("Merged");
        sheet.addMergedRegion(new com.dua3.meja.util.RectangularRegion(2, 3, 2, 3));

        MockPainterDelegate delegate = new MockPainterDelegate(sheet);
        delegate.splitRow = 1;
        delegate.splitColumn = 1;

        CellRenderer renderer = new CellRenderer(delegate);
        SheetPainter painter = new SheetPainter(delegate, renderer);
        painter.update(sheet);

        assertEquals(40.0f, painter.getRowLabelWidth());
        assertEquals(20.0f, painter.getColumnLabelHeight());

        List<String> calls = new ArrayList<>();
        Graphics g = createMockGraphics(calls);

        painter.drawSheet(g, Rectangle2f.of(0, 0, 400, 400));

        // Verifications:
        // Background drawn
        assertTrue(calls.stream().anyMatch(c -> c.startsWith("setFill")));
        assertTrue(calls.stream().anyMatch(c -> c.startsWith("fillRect")));

        // Labels drawn
        assertTrue(delegate.drawnLabels.contains("1"));
        assertTrue(delegate.drawnLabels.contains("A"));

        // Grid lines stroked
        assertTrue(calls.stream().anyMatch(c -> c.startsWith("strokeLine")));

        // Cells rendered
        assertTrue(calls.stream().anyMatch(c -> c.startsWith("renderText")));

        // Selection stroked
        assertTrue(calls.stream().anyMatch(c -> c.startsWith("strokeRect")));
    }

    private static Graphics createMockGraphics(List<String> calls) {
        return (Graphics) Proxy.newProxyInstance(
                Graphics.class.getClassLoader(),
                new Class<?>[]{Graphics.class},
                (proxy, method, args) -> {
                    calls.add(method.getName() + (args != null && args.length > 0 ? ":" + args[0] : ""));
                    if (method.getName().equals("getTransformation")) {
                        return com.dua3.utility.math.geometry.AffineTransformation2f.identity();
                    }
                    return null;
                }
        );
    }

    private static class MockPainterDelegate extends SheetViewDelegate {
        int splitRow = 0;
        int splitColumn = 0;
        final List<String> drawnLabels = new ArrayList<>();

        MockPainterDelegate(Sheet sheet) {
            super(sheet, new DummySheetView());
        }

        @Override
        public Rectangle2f getTotalArea() {
            return Rectangle2f.of(0, 0, 500, 500);
        }

        @Override
        public Color getBackground() {
            return Color.WHITE;
        }

        @Override
        public float getRowLabelWidthInPoints() {
            return 40.0f;
        }

        @Override
        public float getColumnLabelHeightInPoints() {
            return 20.0f;
        }

        @Override
        public SheetView.SheetArea getSheetArea(Rectangle2f r, boolean b) {
            return new SheetView.SheetArea(r, 0, 0, 4, 4);
        }

        @Override
        public float getRowPos(int i) {
            return i * 25.0f;
        }

        @Override
        public float getColumnPos(int j) {
            return j * 60.0f;
        }

        @Override
        public int getRowCount() {
            return 4;
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getRowName(int i) {
            return String.valueOf(i + 1);
        }

        @Override
        public String getColumnName(int j) {
            return String.valueOf((char) ('A' + j));
        }

        @Override
        public void drawLabel(Graphics g, Rectangle2f r, CharSequence text) {
            drawnLabels.add(text.toString());
        }

        @Override
        public Color getGridColor() {
            return Color.LIGHTGRAY;
        }

        @Override
        public float get1PxWidthInPoints() {
            return 1.0f;
        }

        @Override
        public float get1PxHeightInPoints() {
            return 1.0f;
        }

        @Override
        public int getSplitRow() {
            return splitRow;
        }

        @Override
        public int getSplitColumn() {
            return splitColumn;
        }

        @Override
        public Rectangle2f getCellRect(Cell cell) {
            Cell lc = cell.getLogicalCell();
            return Rectangle2f.of(lc.getColumnNumber() * 60.0f, lc.getRowNumber() * 25.0f,
                    lc.getHorizontalSpan() * 60.0f, lc.getVerticalSpan() * 25.0f);
        }

        @Override
        public float getSelectionStrokeWidth() {
            return 2.0f;
        }

        @Override
        public Color getSelectionColor() {
            return Color.BLUE;
        }

        @Override
        public Locale getLocale() {
            return Locale.US;
        }
    }

    private static class DummySheetView implements SheetView {
        @Override public void scrollToCurrentCell() {/* ignore */}
        @Override public void stopEditing(boolean commit) {/* ignore */}
        @Override public SheetViewDelegate getDelegate() { return null; }
        @Override public void repaintCell(Cell cell) {/* ignore */}
        @Override public void updateContent() {/* ignore */}
        @Override public void focusView() {/* ignore */}
        @Override public void setAllowOpenLinks(boolean allowOpenLinks) {/* ignore */}
        @Override public boolean getAllowOpenLinks() { return false; }
        @Override public Locale getLocale() { return Locale.US; }
        @Override public Scale2f getDisplayScale() { return Scale2f.identity(); }
        @Override public void copyToClipboard() {/* ignore */}
        @Override public void showSearchDialog() {/* ignore */}
        @Override public boolean isEditable() { return false; }
        @Override public void startEditing() {/* ignore */}
    }
}
