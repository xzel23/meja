package com.dua3.meja.model;

import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CoreSheetAndCellTest {

    @Test
    void testAbstractSheetAndCellOperations() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("CoreTest");

        // Cell setting & basic types
        Cell c00 = sheet.getCell(0, 0);
        c00.set("Text");
        assertEquals("Text", c00.getText().toString());
        assertEquals("Text", c00.toString());
        assertEquals("Text", c00.getAsText(Locale.US).toString());
        assertEquals(CellType.TEXT, c00.getCellType());
        assertFalse(c00.isEmpty());

        Cell c01 = sheet.getCell(0, 1);
        c01.set(123.45);
        assertEquals(123.45, c01.getNumber().doubleValue());
        assertEquals(CellType.NUMERIC, c01.getCellType());

        Cell c02 = sheet.getCell(0, 2);
        c02.set(true);
        assertTrue(c02.getBoolean());
        assertEquals(CellType.BOOLEAN, c02.getCellType());

        Cell c03 = sheet.getCell(0, 3);
        LocalDate d = LocalDate.of(2023, 5, 10);
        c03.set(d);
        assertEquals(d, c03.getDate());
        assertEquals(CellType.DATE, c03.getCellType());

        Cell c04 = sheet.getCell(0, 4);
        LocalDateTime dt = LocalDateTime.of(2023, 5, 10, 14, 30);
        c04.set(dt);
        assertEquals(dt, c04.getDateTime());
        assertEquals(CellType.DATE_TIME, c04.getCellType());

        Cell c05 = sheet.getCell(0, 5);
        c05.setFormula("SUM(A1:B1)");
        assertEquals("SUM(A1:B1)", c05.getFormula());
        assertEquals(CellType.FORMULA, c05.getCellType());

        // Cell dimensions and borders
        var dim = c00.calcCellDimension();
        assertTrue(dim.width() > 0);
        assertTrue(dim.height() > 0);

        for (Direction dir : Direction.values()) {
            assertNotNull(c00.getEffectiveBorderStyle(dir));
        }

        // Cell references
        assertEquals("A1", c00.getCellRef());
        assertEquals("'CoreTest'!A1", c00.getCellRef(RefOption.WITH_SHEET));
        assertEquals("$A$1", c00.getCellRef(RefOption.FIX_COLUMN, RefOption.FIX_ROW));

        // Merging
        Cell mergeCell = sheet.getCell(2, 2);
        mergeCell.merge(2, 3);
        assertTrue(mergeCell.isMerged());
        assertEquals(2, mergeCell.getHorizontalSpan());
        assertEquals(3, mergeCell.getVerticalSpan());
        assertEquals(mergeCell, mergeCell.getLogicalCell());

        mergeCell.unMerge();
        assertFalse(mergeCell.isMerged());

        // Clear cell
        c00.clear();
        assertTrue(c00.isEmpty());
        assertEquals(CellType.BLANK, c00.getCellType());
    }

    @Test
    void testSheetSearchAndNavigation() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("SearchNav");

        sheet.getCell(0, 0).set("Alpha");
        sheet.getCell(0, 1).set("Beta");
        sheet.getCell(1, 0).set("Gamma");
        sheet.getCell(1, 1).setFormula("A1+B1");

        // Sheet find
        Optional<Cell> found = sheet.find("Beta", SearchSettings.of());
        assertTrue(found.isPresent());
        assertEquals(0, found.get().getRowNumber());
        assertEquals(1, found.get().getColumnNumber());

        assertTrue(sheet.find("gamma", SearchSettings.of(SearchOptions.IGNORE_CASE)).isPresent());
        assertTrue(sheet.find("Gamma", SearchSettings.of(SearchOptions.MATCH_COMPLETE_TEXT)).isPresent());
        assertTrue(sheet.find("A1", SearchSettings.of(SearchOptions.SEARCH_FORMULA_TEXT)).isPresent());

        // Current cell
        Cell cur = sheet.getCell(1, 0);
        sheet.setCurrentCell(cur);
        assertEquals(cur, sheet.getCurrentCell());

        // Row find
        Row row = sheet.getRow(0);
        assertTrue(row.find("Alpha", SearchSettings.of()).isPresent());
        assertTrue(row.find("alpha", SearchSettings.of(SearchOptions.IGNORE_CASE)).isPresent());
        assertFalse(row.find("NonExistent", SearchSettings.of()).isPresent());

        // Row cells
        assertEquals(2, row.getColumnCount());
        assertEquals(2, row.cells().count());
        Iterator<Cell> it = row.iterator();
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void testSheetDimensionsAndLayout() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("Layout");

        sheet.setColumnWidth(0, 60.0f);
        assertEquals(60.0f, sheet.getColumnWidth(0));

        sheet.setRowHeight(0, 40.0f);
        assertEquals(40.0f, sheet.getRowHeight(0));

        sheet.splitAt(2, 1);
        assertEquals(2, sheet.getSplitRow());
        assertEquals(1, sheet.getSplitColumn());

        sheet.setZoom(1.25f);
        assertEquals(1.25f, sheet.getZoom());

        sheet.setAutofilterRow(1);
        assertEquals(1, sheet.getAutoFilterRow());

        sheet.addMergedRegion(new RectangularRegion(3, 4, 3, 5));
        assertEquals(1, sheet.getMergedRegions().size());
        assertTrue(sheet.getMergedRegion(3, 4).isPresent());

        // Autosizing
        sheet.getCell(0, 0).set("A somewhat longer text string for sizing");
        sheet.getCell(1, 0).set("Short");
        sheet.autoSizeColumn(0);
        assertTrue(sheet.getColumnWidth(0) > 0);
        sheet.autoSizeColumns();
        sheet.autoSizeRow(0);
        assertTrue(sheet.getRowHeight(0) > 0);

        // Lock & toString
        try (var wLock = sheet.writeLock("CoreTestWrite")) {
            assertNotNull(wLock);
        }
        assertNotNull(sheet.toString());

        sheet.clear();
        assertEquals(0, sheet.getRowCount());
        assertEquals(0, sheet.getMergedRegions().size());
    }

    @Test
    void testWorkbookDefaultMethods() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        Sheet s1 = wb.createSheet("SheetA");
        Sheet s2 = wb.createSheet("SheetB");

        assertEquals(2, wb.getSheetCount());
        assertEquals(s1, wb.getSheetByName("SheetA"));
        assertEquals(s2, wb.getSheetByName("SheetB"));
        assertEquals(0, wb.getSheetIndexByName("SheetA"));
        assertEquals(1, wb.getSheetIndexByName("SheetB"));
        assertEquals(0, wb.getSheetIndex(s1));
        assertEquals(1, wb.getSheetIndex(s2));

        assertEquals(s1, wb.getOrCreateSheet("SheetA"));
        Sheet s3 = wb.getOrCreateSheet("SheetC");
        assertEquals(3, wb.getSheetCount());
        assertEquals("SheetC", s3.getSheetName());

        wb.setCurrentSheet(s2);
        assertEquals(1, wb.getCurrentSheetIndex());
        assertEquals(Optional.of(s2), wb.getCurrentSheet());

        assertEquals(3, wb.sheets().count());

        wb.removeSheetByName("SheetB");
        assertEquals(2, wb.getSheetCount());
        assertEquals(-1, wb.getSheetIndexByName("SheetB"));

        GenericWorkbook copy = GenericWorkbookFactory.instance().create();
        copy.copy(wb);
        assertEquals(2, copy.getSheetCount());
        assertEquals("SheetA", copy.getSheet(0).getSheetName());
        assertEquals("SheetC", copy.getSheet(1).getSheetName());

        // Styles and caching on workbook
        wb.setObjectCaching(true);
        assertTrue(wb.isObjectCachingEnabled());
        wb.cache("SampleText");
        wb.setObjectCaching(false);
        assertFalse(wb.isObjectCachingEnabled());

        assertNotNull(wb.getCellStyle("Default"));
        assertNotNull(wb.getDefaultCellStyle());
        CellStyle copied1 = wb.copyCellStyle("MyCopiedStyle", wb.getDefaultCellStyle());
        assertNotNull(copied1);
    }

    @Test
    void testWorkbookFactoryMethods(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws IOException {
        WorkbookFactory<GenericWorkbook> factory = GenericWorkbookFactory.instance();

        // copyOf
        GenericWorkbook original = factory.create();
        original.setUri(URI.create("file:///test/original.csv"));
        GenericSheet s = original.createSheet("Source");
        s.getCell(0, 0).set("CopyMe");

        GenericWorkbook copy = factory.copyOf(original);
        assertEquals(original.getUri(), copy.getUri());
        assertEquals(1, copy.getSheetCount());
        assertEquals("CopyMe", copy.getSheet(0).getCell(0, 0).getText().toString());

        // open(URI) and open(URI, Arguments)
        java.nio.file.Path csvFile = tempDir.resolve("test_factory.csv");
        java.nio.file.Files.writeString(csvFile, "H1,H2\nV1,V2\n");
        URI fileUri = csvFile.toUri();

        try (GenericWorkbook opened1 = factory.open(fileUri)) {
            assertEquals(1, opened1.getSheetCount());
            assertEquals("V1", opened1.getSheet(0).getRow(1).getCell(0).getText().toString());
        }

        try (GenericWorkbook opened2 = factory.open(fileUri, com.dua3.utility.options.Arguments.empty())) {
            assertEquals(1, opened2.getSheetCount());
            assertEquals("V2", opened2.getSheet(0).getRow(1).getCell(1).getText().toString());
        }
    }

    @Test
    void testWorkbookEvents() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();

        java.util.List<WorkbookEvent> events = new java.util.concurrent.CopyOnWriteArrayList<>();
        java.util.concurrent.Flow.Subscriber<WorkbookEvent> subscriber = new java.util.concurrent.Flow.Subscriber<>() {
            @Override
            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(WorkbookEvent item) {
                events.add(item);
            }

            @Override
            public void onError(Throwable throwable) {
                // do nothing
            }

            @Override
            public void onComplete() {
                // do nothing
            }
        };

        wb.subscribe(subscriber);

        wb.createSheet("S1");
        wb.createSheet("S2");
        wb.setCurrentSheet(1);
        wb.setUri(URI.create("file:///new_uri.csv"));
        wb.removeSheet(0);

        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            // do nothing
        }

        assertFalse(events.isEmpty());
        wb.unsubscribe(subscriber);
    }

    @Test
    void testSheetEvents() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("EventSheet");

        java.util.List<SheetEvent> events = new java.util.concurrent.CopyOnWriteArrayList<>();
        java.util.concurrent.Flow.Subscriber<SheetEvent> subscriber = new java.util.concurrent.Flow.Subscriber<>() {
            @Override
            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(SheetEvent item) {
                events.add(item);
            }

            @Override
            public void onError(Throwable throwable) {
                // do nothing
            }

            @Override
            public void onComplete() {
                // do nothing
            }
        };

        sheet.subscribe(subscriber);

        sheet.getCell(0, 0).set("ValueChange");
        sheet.setZoom(1.5f);
        sheet.splitAt(1, 1);
        sheet.setCurrentCell(sheet.getCell(2, 2));

        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            // ignored
        }

        assertFalse(events.isEmpty());
        sheet.unsubscribe(subscriber);
    }

    @Test
    void testCellAndSheetDeepMethods(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws IOException {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        wb.setUri(URI.create("file:///test/core_deep.csv"));
        GenericSheet sheet = wb.createSheet("DeepSheet");

        // Cell.get()
        Cell c0 = sheet.getCell(0, 0);
        assertFalse(c0.get().isPresent());
        c0.set("Val");
        assertEquals(Optional.of(com.dua3.utility.text.RichText.valueOf("Val")), c0.get());

        // Cell.copy() for all types
        Cell target = sheet.getCell(5, 5);

        // Copy BLANK
        Cell srcBlank = sheet.getCell(10, 10);
        target.copy(srcBlank);
        assertEquals(CellType.BLANK, target.getCellType());

        // Copy BOOLEAN
        Cell srcBool = sheet.getCell(10, 11);
        srcBool.set(true);
        target.copy(srcBool);
        assertEquals(CellType.BOOLEAN, target.getCellType());
        assertTrue(target.getBoolean());

        // Copy NUMERIC
        Cell srcNum = sheet.getCell(10, 12);
        srcNum.set(42.5);
        target.copy(srcNum);
        assertEquals(CellType.NUMERIC, target.getCellType());
        assertEquals(42.5, target.getNumber().doubleValue());

        // Copy DATE
        Cell srcDate = sheet.getCell(10, 13);
        srcDate.set(LocalDate.of(2025, 1, 1));
        target.copy(srcDate);
        assertEquals(CellType.DATE, target.getCellType());
        assertEquals(LocalDate.of(2025, 1, 1), target.getDate());

        // Copy DATE_TIME
        Cell srcDateTime = sheet.getCell(10, 14);
        srcDateTime.set(LocalDateTime.of(2025, 1, 1, 12, 0));
        target.copy(srcDateTime);
        assertEquals(CellType.DATE_TIME, target.getCellType());
        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0), target.getDateTime());

        // Copy FORMULA
        Cell srcFormula = sheet.getCell(10, 15);
        srcFormula.setFormula("SUM(A1:B1)");
        target.copy(srcFormula);
        assertEquals(CellType.FORMULA, target.getCellType());
        assertEquals("SUM(A1:B1)", target.getFormula());

        // Copy ERROR
        Cell srcError = sheet.getCell(10, 16);
        srcError.setError();
        target.copy(srcError);
        assertTrue(target.isError());

        // Copy TEXT and Hyperlink
        Cell srcText = sheet.getCell(10, 17);
        srcText.set("LinkText");
        srcText.setHyperlink(URI.create("https://example.com/link"));
        target.copy(srcText);
        assertEquals(CellType.TEXT, target.getCellType());
        assertEquals(Optional.of(URI.create("https://example.com/link")), target.getHyperlink());

        // Cell.set(Object) with all branches
        Cell setCell = sheet.getCell(6, 6);
        setCell.set((Object) null);
        assertEquals(CellType.BLANK, setCell.getCellType());

        setCell.set((Object) 99);
        assertEquals(99, setCell.getNumber().intValue());

        setCell.set((Object) false);
        assertFalse(setCell.getBoolean());

        setCell.set((Object) LocalDate.of(2024, 2, 2));
        assertEquals(LocalDate.of(2024, 2, 2), setCell.getDate());

        setCell.set((Object) LocalDateTime.of(2024, 2, 2, 8, 0));
        assertEquals(LocalDateTime.of(2024, 2, 2, 8, 0), setCell.getDateTime());

        setCell.set((Object) com.dua3.utility.text.RichText.valueOf("RichContent"));
        assertEquals("RichContent", setCell.getText().toString());

        setCell.set((Object) "StringContent");
        assertEquals("StringContent", setCell.getText().toString());

        setCell.set((Object) URI.create("https://custom.com"));
        assertEquals("https://custom.com", setCell.getText().toString());

        // Cell.setCellStyle(String)
        setCell.setCellStyle("Default");
        assertNotNull(setCell.getCellStyle());

        // Cell.setHyperlink(Path) & getResolvedHyperlink()
        java.nio.file.Path targetPath = tempDir.resolve("target.txt");
        setCell.setHyperlink(targetPath);
        assertTrue(setCell.getResolvedHyperlink().isPresent());

        // Cell.getAsFormattedText(Locale)
        assertNotNull(setCell.getAsFormattedText(Locale.US));

        // Cell.merge(1, 1) should be ignored
        Cell noMerge = sheet.getCell(8, 8);
        assertSame(noMerge, noMerge.merge(1, 1));

        // Effective border style with neighboring cell
        Cell topCell = sheet.getCell(1, 1);
        Cell bottomCell = sheet.getCell(2, 1);
        CellStyle bStyle = wb.getCellStyle("SouthBorder");
        bStyle.setBorderStyle(Direction.SOUTH, new BorderStyle(2.0f, Color.RED));
        topCell.setCellStyle(bStyle);

        assertEquals(2.0f, topCell.getEffectiveBorderStyle(Direction.SOUTH).width());
        assertEquals(2.0f, bottomCell.getEffectiveBorderStyle(Direction.NORTH).width());

        // Sheet createRow and createRowWith
        Row r1 = sheet.createRow("ColA", 1, true);
        assertEquals(3, r1.getColumnCount());

        Row r2 = sheet.createRowWith(List.of("X", "Y"));
        assertEquals(2, r2.getColumnCount());

        // Sheet.setCurrentCell(row, col)
        sheet.setCurrentCell(1, 1);
        assertEquals(topCell, sheet.getCurrentCell());

        // Sheet.getRowIfExists and getCellIfExists
        assertTrue(sheet.getRowIfExists(0).isPresent());
        assertFalse(sheet.getRowIfExists(1000).isPresent());
        assertFalse(sheet.getRowIfExists(-1).isPresent());

        assertTrue(sheet.getCellIfExists(0, 0).isPresent());
        assertFalse(sheet.getCellIfExists(1000, 1000).isPresent());

        // Sheet find looping around (searchFromCurrent)
        sheet.setCurrentCell(sheet.getRowCount() - 1, 0);
        Optional<Cell> foundWrapped = sheet.find("Val", SearchSettings.of(SearchOptions.SEARCH_FROM_CURRENT));
        assertTrue(foundWrapped.isPresent());

        // Workbook write methods
        java.nio.file.Path outPath = tempDir.resolve("out_wb.csv");
        wb.write(outPath);
        assertTrue(java.nio.file.Files.exists(outPath));

        wb.write(outPath, com.dua3.utility.options.Arguments.empty());
        assertTrue(java.nio.file.Files.exists(outPath));

        java.nio.file.Path outPathProgress = tempDir.resolve("out_wb_prog.csv");
        wb.write(outPathProgress, com.dua3.utility.options.Arguments.empty(), p -> {});
        assertTrue(java.nio.file.Files.exists(outPathProgress));

        URI outUri = tempDir.resolve("out_uri.csv").toUri();
        wb.write(outUri);
        assertTrue(java.nio.file.Files.exists(java.nio.file.Path.of(outUri)));

        wb.write(outUri, com.dua3.utility.options.Arguments.empty());
        assertTrue(java.nio.file.Files.exists(java.nio.file.Path.of(outUri)));

        wb.write(outUri, com.dua3.utility.options.Arguments.empty(), p -> {});
        assertTrue(java.nio.file.Files.exists(java.nio.file.Path.of(outUri)));

        wb.write(com.dua3.meja.model.generic.io.FileTypeCsv.instance(), new java.io.ByteArrayOutputStream());
        wb.write(com.dua3.meja.model.generic.io.FileTypeCsv.instance(), new java.io.ByteArrayOutputStream(), com.dua3.utility.options.Arguments.empty());
        wb.write(com.dua3.meja.model.generic.io.FileTypeCsv.instance(), new java.io.ByteArrayOutputStream(), com.dua3.utility.options.Arguments.empty(), p -> {});
    }
}
