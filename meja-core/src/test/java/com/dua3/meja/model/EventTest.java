package com.dua3.meja.model;

import com.dua3.meja.model.SheetEvent.*;
import com.dua3.meja.model.WorkbookEvent.*;
import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private static Sheet sheetMock;
    private static Workbook workbookMock;
    private static Cell cellMock;

    @BeforeAll
    static void setUp() {
        sheetMock = (Sheet) Proxy.newProxyInstance(
                Sheet.class.getClassLoader(),
                new Class<?>[]{Sheet.class},
                (p, m, a) -> {
                    if ("equals".equals(m.getName())) return a[0] == p;
                    if ("hashCode".equals(m.getName())) return System.identityHashCode(p);
                    if ("toString".equals(m.getName())) return "sheetMock";
                    return null;
                }
        );
        workbookMock = (Workbook) Proxy.newProxyInstance(
                Workbook.class.getClassLoader(),
                new Class<?>[]{Workbook.class},
                (p, m, a) -> {
                    if ("equals".equals(m.getName())) return a[0] == p;
                    if ("hashCode".equals(m.getName())) return System.identityHashCode(p);
                    if ("toString".equals(m.getName())) return "workbookMock";
                    return null;
                }
        );
        cellMock = (Cell) Proxy.newProxyInstance(
                Cell.class.getClassLoader(),
                new Class<?>[]{Cell.class},
                (p, m, a) -> {
                    if ("equals".equals(m.getName())) return a[0] == p;
                    if ("hashCode".equals(m.getName())) return System.identityHashCode(p);
                    if ("toString".equals(m.getName())) return "cellMock";
                    return null;
                }
        );
    }

    @Test
    void testZoomChanged() {
        ZoomChanged event = new ZoomChanged(sheetMock, 1.0f, 1.5f);
        assertEquals(SheetEvent.ZOOM_CHANGED, event.type());
        assertEquals(1.0f, event.oldValue());
        assertEquals(1.5f, event.newValue());
        assertEquals(sheetMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testLayoutChanged() {
        LayoutChanged event = new LayoutChanged(sheetMock);
        assertEquals(SheetEvent.LAYOUT_CHANGED, event.type());
        assertEquals(sheetMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testSplitChanged() {
        SplitChanged event = new SplitChanged(sheetMock, Pair.of(0, 0), Pair.of(2, 4));
        assertEquals(SheetEvent.SPLIT_CHANGED, event.type());
        assertEquals(Pair.of(0, 0), event.oldValue());
        assertEquals(Pair.of(2, 4), event.newValue());
        assertEquals(sheetMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testActiveCellChanged() {
        ActiveCellChanged event = new ActiveCellChanged(sheetMock, cellMock, cellMock);
        assertEquals(SheetEvent.ACTIVE_CELL_CHANGED, event.type());
        assertEquals(cellMock, event.oldValue());
        assertEquals(cellMock, event.newValue());
        assertEquals(sheetMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testCellValueChanged() {
        CellValueChanged event = new CellValueChanged(sheetMock, cellMock, "oldVal", "newVal");
        assertEquals(SheetEvent.CELL_VALUE_CHANGED, event.type());
        assertEquals(cellMock, event.cell());
        assertEquals("oldVal", event.oldValue());
        assertEquals("newVal", event.newValue());
        assertEquals(sheetMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testCellStyleChanged() {
        CellStyleChanged event = new CellStyleChanged(sheetMock, cellMock, "oldStyle", "newStyle");
        assertEquals(SheetEvent.CELL_STYLE_CHANGED, event.type());
        assertEquals(cellMock, event.cell());
        assertEquals("oldStyle", event.oldValue());
        assertEquals("newStyle", event.newValue());
        assertEquals(sheetMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testRowsAdded() {
        RowsAdded event = new RowsAdded(sheetMock, 2, 4);
        assertEquals(SheetEvent.ROWS_ADDED, event.type());
        assertEquals(2, event.first());
        assertEquals(4, event.last());
        assertEquals(sheetMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testColumnsAdded() {
        ColumnsAdded event = new ColumnsAdded(sheetMock, 1, 3);
        assertEquals(SheetEvent.COLUMNS_ADDED, event.type());
        assertEquals(1, event.first());
        assertEquals(3, event.last());
        assertEquals(sheetMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testActiveSheetChanged() {
        ActiveSheetChanged event = new ActiveSheetChanged(workbookMock, 0, 1);
        assertEquals(WorkbookEvent.ACTIVE_SHEET_CHANGED, event.type());
        assertEquals(0, event.oldValue());
        assertEquals(1, event.newValue());
        assertEquals(workbookMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testSheetAdded() {
        SheetAdded event = new SheetAdded(workbookMock, 2);
        assertEquals(WorkbookEvent.SHEET_ADDED, event.type());
        assertEquals(2, event.idx());
        assertEquals(workbookMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testSheetRemoved() {
        SheetRemoved event = new SheetRemoved(workbookMock, 1);
        assertEquals(WorkbookEvent.SHEET_REMOVED, event.type());
        assertEquals(1, event.idx());
        assertEquals(workbookMock, event.source());
        assertNotNull(event.toString());
    }

    @Test
    void testUriChanged() {
        URI oldUri = URI.create("file:///old.xlsx");
        URI newUri = URI.create("file:///new.xlsx");
        UriChanged event = new UriChanged(workbookMock, oldUri, newUri);
        assertEquals(WorkbookEvent.URI_CHANGED, event.type());
        assertEquals(oldUri, event.oldValue());
        assertEquals(newUri, event.newValue());
        assertEquals(workbookMock, event.source());
        assertNotNull(event.toString());
    }
}
