package com.dua3.meja.ui;

import com.dua3.meja.model.*;
import com.dua3.meja.model.generic.GenericCellStyle;
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

class CellRendererTest {

    @Test
    void testDrawCellEmpty() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");
        Cell cell = sheet.getCell(0, 0);

        MockDelegate delegate = new MockDelegate(sheet);
        CellRenderer renderer = new CellRenderer(delegate);

        List<String> calls = new ArrayList<>();
        Graphics g = createMockGraphics(calls);

        renderer.drawCell(g, cell);
        // Empty cell without style -> Fill NONE, border NONE, foreground empty
        assertTrue(calls.isEmpty());
    }

    @Test
    void testDrawCellFillsAndBorders() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");
        Cell cell = sheet.getCell(0, 0);
        cell.set("Test");

        // Set style with solid fill, custom borders
        GenericCellStyle style = wb.getCellStyle("CustomStyle");
        style.setFillPattern(FillPattern.SOLID);
        style.setFillFgColor(Color.RED);
        style.setBorderStyle(Direction.NORTH, new BorderStyle(2.0f, Color.BLACK));
        style.setBorderStyle(Direction.EAST, new BorderStyle(1.0f, Color.BLACK));
        style.setBorderStyle(Direction.SOUTH, new BorderStyle(3.0f, Color.BLACK));
        style.setBorderStyle(Direction.WEST, new BorderStyle(1.0f, Color.BLACK));
        cell.setCellStyle(style);

        MockDelegate delegate = new MockDelegate(sheet);
        CellRenderer renderer = new CellRenderer(delegate);

        List<String> calls = new ArrayList<>();
        Graphics g = createMockGraphics(calls);

        renderer.drawCell(g, cell);

        assertTrue(calls.stream().anyMatch(c -> c.startsWith("setFill")));
        assertTrue(calls.stream().anyMatch(c -> c.startsWith("fillRect")));
        assertTrue(calls.stream().anyMatch(c -> c.startsWith("strokeLine")));
        assertTrue(calls.stream().anyMatch(c -> c.startsWith("renderText")));
    }

    @Test
    void testDrawCellAlignments() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");

        HAlign[] hAligns = {HAlign.ALIGN_LEFT, HAlign.ALIGN_CENTER, HAlign.ALIGN_RIGHT, HAlign.ALIGN_JUSTIFY, HAlign.ALIGN_DISTRIBUTED};
        VAlign[] vAligns = {VAlign.ALIGN_TOP, VAlign.ALIGN_MIDDLE, VAlign.ALIGN_BOTTOM, VAlign.ALIGN_JUSTIFY, VAlign.ALIGN_DISTRIBUTED};

        MockDelegate delegate = new MockDelegate(sheet);
        CellRenderer renderer = new CellRenderer(delegate);

        for (int i = 0; i < hAligns.length; i++) {
            Cell cell = sheet.getCell(i, 0);
            cell.set("Align " + i);
            GenericCellStyle style = wb.getCellStyle("AlignStyle" + i);
            style.setHAlign(hAligns[i]);
            style.setVAlign(vAligns[i % vAligns.length]);
            style.setRotation((short) 45);
            style.setWrap(true);
            cell.setCellStyle(style);

            List<String> calls = new ArrayList<>();
            Graphics g = createMockGraphics(calls);
            renderer.drawCell(g, cell);

            assertTrue(calls.stream().anyMatch(c -> c.startsWith("renderText")));
        }
    }

    @Test
    void testDrawCellNonWrappingOverflow() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");
        Cell cell = sheet.getCell(0, 1);
        cell.set("A very long text overflowing left and right");

        GenericCellStyle style = wb.getCellStyle("OverflowStyle");
        style.setWrap(false);
        cell.setCellStyle(style);

        MockDelegate delegate = new MockDelegate(sheet);
        CellRenderer renderer = new CellRenderer(delegate);

        List<String> calls = new ArrayList<>();
        Graphics g = createMockGraphics(calls);

        renderer.drawCell(g, cell);
        assertTrue(calls.stream().anyMatch(c -> c.startsWith("renderText")));
    }

    @Test
    void testDrawSelection() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");
        Cell cell = sheet.getCell(0, 0);

        MockDelegate delegate = new MockDelegate(sheet);
        CellRenderer renderer = new CellRenderer(delegate);

        // stroke width = 1
        delegate.selectionStrokeWidth = 1.0f;
        List<String> calls1 = new ArrayList<>();
        Graphics g1 = createMockGraphics(calls1);
        renderer.drawSelection(g1, cell);
        assertEquals(1, calls1.stream().filter(c -> c.startsWith("strokeRect")).count());

        // stroke width > 1
        delegate.selectionStrokeWidth = 3.0f;
        List<String> calls2 = new ArrayList<>();
        Graphics g2 = createMockGraphics(calls2);
        renderer.drawSelection(g2, cell);
        assertEquals(3, calls2.stream().filter(c -> c.startsWith("strokeRect")).count());
    }

    @Test
    void testGetters() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("S");
        MockDelegate delegate = new MockDelegate(sheet);
        CellRenderer renderer = new CellRenderer(delegate);

        assertEquals(2.0f, renderer.getPaddingX());
        assertEquals(1.0f, renderer.getPaddingY());
        assertEquals(Color.LIGHTGRAY, renderer.getGridColor());
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

    private static class MockDelegate extends SheetViewDelegate {
        float selectionStrokeWidth = 1.0f;

        MockDelegate(Sheet sheet) {
            super(sheet, new DummySheetView());
        }

        @Override
        public float getSelectionStrokeWidth() {
            return selectionStrokeWidth;
        }

        @Override
        public Rectangle2f getCellRect(Cell cell) {
            return Rectangle2f.of(cell.getColumnNumber() * 50, cell.getRowNumber() * 20, 50, 20);
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
        public Locale getLocale() {
            return Locale.US;
        }

        @Override
        public float getColumnPos(int j) {
            return j * 50.0f;
        }

        @Override
        public int getColumnCount() {
            return 10;
        }

        @Override
        public float getPaddingX() {
            return 2.0f;
        }

        @Override
        public float getPaddingY() {
            return 1.0f;
        }

        @Override
        public Color getGridColor() {
            return Color.LIGHTGRAY;
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
