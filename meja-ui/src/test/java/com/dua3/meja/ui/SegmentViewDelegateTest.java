package com.dua3.meja.ui;

import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SegmentViewDelegateTest {

    static class DummyOwner implements SegmentView {
        float lastW;
        float lastH;

        @Override
        public void updateViewSize(float w, float h) {
            this.lastW = w;
            this.lastH = h;
        }
    }

    static class DummySheetView implements SheetView {
        @Override public void scrollToCurrentCell() {/* ignore */}

        @Override public void stopEditing(boolean commit) {/* ignore */}

        @Override public SheetViewDelegate getDelegate() { return null; }

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

    @Test
    void testAllQuadrantsWithSplits() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Splits");
        sheet.splitAt(2, 3); // splitRow=2, splitColumn=3
        sheet.getCell(10, 10).set("Data");

        DummySheetView view = new DummySheetView();
        SheetViewDelegate sheetDelegate = new SheetViewDelegate(sheet, view) {/* instantiate abstract class for test */};

        for (SheetView.Quadrant quadrant : SheetView.Quadrant.values()) {
            DummyOwner owner = new DummyOwner();
            SegmentViewDelegate seg = new SegmentViewDelegate(owner, sheetDelegate, quadrant);

            assertSame(sheetDelegate, seg.getSheetViewDelegate());
            assertSame(sheet, seg.getSheet());
            assertEquals(quadrant, seg.getQuadrant());

            seg.updateLayout();

            assertTrue(seg.getWidthInPoints() > 0);
            assertTrue(seg.getHeightInPoints() > 0);
            assertTrue(seg.getWidthInPixels() > 0);
            assertTrue(seg.getHeightInPixels() > 0);
            assertEquals(owner.lastW, seg.getWidthInPixels());
            assertEquals(owner.lastH, seg.getHeightInPixels());

            assertNotNull(seg.getTransformation());
            assertNotNull(seg.toString());

            if (quadrant.isLeft()) {
                assertTrue(seg.isLeftOfSplit());
                assertTrue(seg.hasVLine());
                assertEquals(0, seg.getStartColumn());
                assertEquals(3, seg.getEndColumn());
            } else {
                assertFalse(seg.isLeftOfSplit());
                assertFalse(seg.hasVLine());
                assertEquals(3, seg.getStartColumn());
                assertEquals(11, seg.getEndColumn());
            }

            if (quadrant.isTop()) {
                assertTrue(seg.isAboveSplit());
                assertTrue(seg.hasHLine());
                assertEquals(0, seg.getStartRow());
                assertEquals(2, seg.getEndRow());
            } else {
                assertFalse(seg.isAboveSplit());
                assertFalse(seg.hasHLine());
                assertEquals(2, seg.getStartRow());
                assertEquals(11, seg.getEndRow());
            }

            // View coordinate checks
            assertDoesNotThrow(seg::getXMinInViewCoordinates);
            assertDoesNotThrow(seg::getYMinInViewCoordinates);
        }
    }

    @Test
    void testQuadrantsWithoutSplits() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("NoSplits");
        sheet.getCell(5, 5).set("Test");

        DummySheetView view = new DummySheetView();
        SheetViewDelegate sheetDelegate = new SheetViewDelegate(sheet, view) {/* instantiate abstract class for test */};

        DummyOwner owner = new DummyOwner();
        SegmentViewDelegate seg = new SegmentViewDelegate(owner, sheetDelegate, SheetView.Quadrant.TOP_LEFT);

        seg.updateLayout();

        assertFalse(seg.hasHLine());
        assertFalse(seg.hasVLine());
    }
}
