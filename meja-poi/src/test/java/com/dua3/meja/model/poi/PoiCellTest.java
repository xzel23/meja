package com.dua3.meja.model.poi;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.RectangularRegion;
import com.dua3.utility.text.RichText;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PoiCellTest {

    private PoiWorkbook workbook;
    private Sheet sheet;
    private Row row;

    @BeforeEach
    void setUp() {
        workbook = PoiWorkbookFactory.instance().createXlsx();
        sheet = workbook.createSheet("TestSheet");
        row = sheet.getRow(0);
    }

    @Test
    void testBlankCell() {
        Cell cell = row.getCell(0);
        assertEquals(CellType.BLANK, cell.getCellType());
        assertTrue(cell.isEmpty());
        assertEquals("", cell.getAsText(Locale.US).toString());
        assertEquals("", cell.toString());
    }

    @Test
    void testTextCell() {
        Cell cell = row.getCell(0);
        cell.set("POI Text");
        assertEquals(CellType.TEXT, cell.getCellType());
        assertFalse(cell.isEmpty());
        assertEquals("POI Text", cell.getText().toString());
        assertEquals("POI Text", cell.getAsText(Locale.US).toString());
    }

    @Test
    void testRichTextCell() {
        Cell cell = row.getCell(0);
        RichText rt = RichText.valueOf("Rich POI Text");
        cell.set(rt);
        assertEquals(CellType.TEXT, cell.getCellType());
        assertEquals("Rich POI Text", cell.getText().toString());
    }

    @Test
    void testNumericCell() {
        Cell cell = row.getCell(0);
        cell.set(123.456);
        assertEquals(CellType.NUMERIC, cell.getCellType());
        assertEquals(123.456, cell.getNumber().doubleValue(), 0.0001);
    }

    @Test
    void testBooleanCell() {
        Cell cell = row.getCell(0);
        cell.set(true);
        assertEquals(CellType.BOOLEAN, cell.getCellType());
        assertTrue(cell.getBoolean());

        cell.set(false);
        assertFalse(cell.getBoolean());
    }

    @Test
    void testDateCell() {
        Cell cell = row.getCell(0);
        LocalDate date = LocalDate.of(2023, 12, 1);
        cell.set(date);
        assertEquals(CellType.DATE, cell.getCellType());
        assertEquals(date, cell.getDate());
    }

    @Test
    void testDateTimeCell() {
        Cell cell = row.getCell(0);
        LocalDateTime dt = LocalDateTime.of(2023, 12, 1, 10, 30, 0);
        cell.set(dt);
        assertEquals(CellType.DATE_TIME, cell.getCellType());
        assertEquals(dt, cell.getDateTime());
    }

    @Test
    void testFormulaCell() {
        Cell cell = row.getCell(0);
        cell.setFormula("SUM(A1:B1)");
        assertEquals(CellType.FORMULA, cell.getCellType());
        assertEquals("SUM(A1:B1)", cell.getFormula());
    }

    @Test
    void testErrorCell() {
        Cell cell = row.getCell(0);
        cell.setError();
        assertEquals(CellType.ERROR, cell.getCellType());
        assertEquals("#N/A", cell.getAsText(Locale.US).toString());
    }

    @Test
    void testHyperlink() {
        Cell cell = row.getCell(0);
        assertFalse(cell.getHyperlink().isPresent());

        URI uri = URI.create("https://example.com");
        cell.setHyperlink(uri);
        assertTrue(cell.getHyperlink().isPresent());
        assertEquals(uri, cell.getHyperlink().get());

        cell.clearHyperlink();
        assertFalse(cell.getHyperlink().isPresent());

        cell.setHyperlink(Path.of("subfolder/doc.txt"));
        assertTrue(cell.getHyperlink().isPresent());
    }

    @Test
    void testClear() {
        Cell cell = row.getCell(0);
        cell.set("POI Data");
        cell.setHyperlink(URI.create("https://example.com"));
        cell.clear();

        assertEquals(CellType.BLANK, cell.getCellType());
        assertTrue(cell.isEmpty());
        assertFalse(cell.getHyperlink().isPresent());
    }

    @Test
    void testCopyCell() {
        Cell src = row.getCell(0);
        src.set("Copied Data");

        Cell dst = row.getCell(1);
        dst.copy(src);

        assertEquals(CellType.TEXT, dst.getCellType());
        assertEquals("Copied Data", dst.getText().toString());
    }

    @Test
    void testMergeAndSpan() {
        sheet.addMergedRegion(new RectangularRegion(0, 1, 0, 1));
        Cell c00 = row.getCell(0);
        assertTrue(c00.isMerged());
        assertEquals(2, c00.getHorizontalSpan());
        assertEquals(2, c00.getVerticalSpan());
        assertEquals(c00, c00.getLogicalCell());

        Cell c01 = row.getCell(1);
        assertTrue(c01.isMerged());
        assertEquals(c00, c01.getLogicalCell());

        c00.unMerge();
        assertFalse(row.getCell(0).isMerged());
        assertFalse(row.getCell(1).isMerged());
    }

    @Test
    void testSetObject() {
        Cell cell = row.getCell(0);

        cell.set((Object) Boolean.TRUE);
        assertEquals(CellType.BOOLEAN, cell.getCellType());
        assertTrue(cell.getBoolean());

        cell.set((Object) 123);
        assertEquals(CellType.NUMERIC, cell.getCellType());
        assertEquals(123, cell.getNumber().intValue());

        LocalDate date = LocalDate.of(2024, 1, 1);
        cell.set((Object) date);
        assertEquals(date, cell.getDate());

        LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 12, 0);
        cell.set((Object) dt);
        assertEquals(dt, cell.getDateTime());

        RichText rt = RichText.valueOf("Rich");
        cell.set((Object) rt);
        assertEquals(CellType.TEXT, cell.getCellType());
        assertEquals(rt.toString(), cell.getText().toString());

        cell.set((Object) "Simple String");
        assertEquals(CellType.TEXT, cell.getCellType());
        assertEquals("Simple String", cell.getText().toString());

        cell.set((Object) URI.create("https://test.com"));
        assertEquals(CellType.TEXT, cell.getCellType());
        assertEquals("https://test.com", cell.getText().toString());

        cell.set((Object) null);
        assertEquals(CellType.BLANK, cell.getCellType());
        assertTrue(cell.isEmpty());
    }

    @Test
    void testCellRef() {
        Cell cell = sheet.getCell(2, 3); // D3
        assertEquals("D3", cell.getCellRef());
        assertEquals("'TestSheet'!D3", cell.getCellRef(com.dua3.meja.model.RefOption.WITH_SHEET));
        assertEquals("$D3", cell.getCellRef(com.dua3.meja.model.RefOption.FIX_COLUMN));
        assertEquals("D$3", cell.getCellRef(com.dua3.meja.model.RefOption.FIX_ROW));
        assertEquals("$D$3", cell.getCellRef(com.dua3.meja.model.RefOption.FIX_COLUMN, com.dua3.meja.model.RefOption.FIX_ROW));
        assertEquals("'TestSheet'!$D$3", cell.getCellRef(com.dua3.meja.model.RefOption.WITH_SHEET, com.dua3.meja.model.RefOption.FIX_COLUMN, com.dua3.meja.model.RefOption.FIX_ROW));
    }

    @Test
    void testResolvedHyperlink() {
        Cell cell = row.getCell(0);
        assertFalse(cell.getResolvedHyperlink().isPresent());

        // Absolute URI
        cell.setHyperlink(URI.create("https://example.com/test"));
        assertEquals(Optional.of(URI.create("https://example.com/test")), cell.getResolvedHyperlink());

        // With workbook URI
        workbook.setUri(URI.create("file:///base/dir/workbook.xlsx"));
        cell.setHyperlink(URI.create("relative/path.txt"));
        assertTrue(cell.getResolvedHyperlink().isPresent());
    }

    @Test
    void testCalcCellDimension() {
        Cell cell = row.getCell(0);
        var dimBlank = cell.calcCellDimension();
        assertTrue(dimBlank.width() >= 0);
        assertTrue(dimBlank.height() >= 0);

        cell.set("Hello World");
        var dimText = cell.calcCellDimension();
        assertTrue(dimText.width() > 0);
        assertTrue(dimText.height() > 0);

        cell.set("Line 1\nLine 2\nLine 3");
        var dimMulti = cell.calcCellDimension();
        assertTrue(dimMulti.height() >= dimText.height());

        // Rotated text
        PoiCellStyle style = workbook.getCellStyle("Rotated");
        style.setRotation((short) 45);
        cell.setCellStyle(style);
        var dimRotated = cell.calcCellDimension();
        assertTrue(dimRotated.width() > 0);
        assertTrue(dimRotated.height() > 0);
    }

    @Test
    void testEffectiveBorderStyle() {
        Cell c11 = sheet.getCell(1, 1);
        for (com.dua3.meja.model.Direction dir : com.dua3.meja.model.Direction.values()) {
            assertNotNull(c11.getEffectiveBorderStyle(dir));
        }

        PoiCellStyle style = workbook.getCellStyle("Bordered");
        style.setBorderStyle(com.dua3.meja.model.Direction.NORTH, new com.dua3.meja.model.BorderStyle(2.0f, com.dua3.utility.data.Color.RED));
        c11.setCellStyle(style);

        assertTrue(c11.getEffectiveBorderStyle(com.dua3.meja.model.Direction.NORTH).width() > 0);
    }

    @Test
    void testCellMergeConvenience() {
        Cell cell = sheet.getCell(2, 2);
        cell.merge(2, 3);
        assertTrue(cell.isMerged());
        assertEquals(2, cell.getHorizontalSpan());
        assertEquals(3, cell.getVerticalSpan());
        cell.unMerge();
        assertFalse(cell.isMerged());
    }

    @Test
    void testCellStyle() {
        Cell cell = row.getCell(0);
        PoiCellStyle defaultStyle = (PoiCellStyle) cell.getCellStyle();
        assertNotNull(defaultStyle);

        PoiCellStyle customStyle = workbook.getCellStyle("CustomStyle");
        customStyle.setHAlign(com.dua3.meja.model.HAlign.ALIGN_RIGHT);
        cell.setCellStyle(customStyle);
        assertEquals(com.dua3.meja.model.HAlign.ALIGN_RIGHT, cell.getCellStyle().getHAlign());

        cell.setCellStyle("Default");
        assertNotNull(cell.getCellStyle());
    }
}
