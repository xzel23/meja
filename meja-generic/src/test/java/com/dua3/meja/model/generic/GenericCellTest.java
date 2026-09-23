package com.dua3.meja.model.generic;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.Row;
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

class GenericCellTest {

    private GenericWorkbook workbook;
    private GenericSheet sheet;
    private Row row;

    @BeforeEach
    void setUp() {
        workbook = GenericWorkbookFactory.instance().create();
        sheet = workbook.createSheet("TestSheet");
        row = sheet.getRow(0);
    }

    @Test
    void testBlankCell() {
        Cell cell = row.getCell(0);
        assertEquals(CellType.BLANK, cell.getCellType());
        assertTrue(cell.isEmpty());
        assertEquals(Optional.empty(), cell.get());
        assertEquals("", cell.getAsText(Locale.US).toString());
        assertEquals("", cell.toString());
        assertEquals(RichText.emptyText(), cell.getText());
        assertThrows(IllegalStateException.class, cell::getNumber);
        assertThrows(IllegalStateException.class, cell::getBoolean);
        assertThrows(IllegalStateException.class, cell::getDate);
        assertThrows(IllegalStateException.class, cell::getDateTime);
        assertThrows(IllegalStateException.class, cell::getFormula);
    }

    @Test
    void testTextCell() {
        Cell cell = row.getCell(0);
        cell.set("Sample Text");
        assertEquals(CellType.TEXT, cell.getCellType());
        assertFalse(cell.isEmpty());
        assertEquals(Optional.of(RichText.valueOf("Sample Text")), cell.get());
        assertEquals("Sample Text", cell.getText().toString());
        assertEquals("Sample Text", cell.getAsText(Locale.US).toString());
        assertEquals("Sample Text", cell.toString());
    }

    @Test
    void testRichTextCell() {
        Cell cell = row.getCell(0);
        RichText rt = RichText.valueOf("Rich Text Value");
        cell.set(rt);
        assertEquals(CellType.TEXT, cell.getCellType());
        assertEquals(rt, cell.getText());
        assertEquals("Rich Text Value", cell.getText().toString());
        assertEquals("Rich Text Value", cell.getAsText(Locale.US).toString());
    }

    @Test
    void testNumericCell() {
        Cell cell = row.getCell(0);
        cell.set(42.5);
        assertEquals(CellType.NUMERIC, cell.getCellType());
        assertEquals(42.5, cell.getNumber().doubleValue());
        assertEquals("42.5", cell.toString(Locale.US));
        assertEquals("42,5", cell.toString(Locale.GERMANY));
    }

    @Test
    void testBooleanCell() {
        Cell cell = row.getCell(0);
        cell.set(true);
        assertEquals(CellType.BOOLEAN, cell.getCellType());
        assertTrue(cell.getBoolean());
        assertEquals("true", cell.toString(Locale.US));

        cell.set(false);
        assertFalse(cell.getBoolean());
        assertEquals("false", cell.toString(Locale.US));
    }

    @Test
    void testDateCell() {
        Cell cell = row.getCell(0);
        LocalDate date = LocalDate.of(2023, 5, 15);
        cell.set(date);
        assertEquals(CellType.DATE, cell.getCellType());
        assertEquals(date, cell.getDate());
        assertEquals("May 15, 2023", cell.toString(Locale.US));
        assertEquals("15.05.2023", cell.toString(Locale.GERMANY));
    }

    @Test
    void testDateTimeCell() {
        Cell cell = row.getCell(0);
        LocalDateTime dt = LocalDateTime.of(2023, 5, 15, 14, 30, 0);
        cell.set(dt);
        assertEquals(CellType.DATE_TIME, cell.getCellType());
        assertEquals(dt, cell.getDateTime());
        assertNotNull(cell.toString(Locale.US));
        assertNotNull(cell.toString(Locale.GERMANY));
    }

    @Test
    void testFormulaCell() {
        Cell cell = row.getCell(0);
        cell.setFormula("SUM(A1:A5)");
        assertEquals(CellType.FORMULA, cell.getCellType());
        assertEquals("SUM(A1:A5)", cell.getFormula());
        assertEquals(Optional.of("SUM(A1:A5)"), cell.get());
        assertEquals("SUM(A1:A5)", cell.toString());
    }

    @Test
    void testErrorCell() {
        Cell cell = row.getCell(0);
        cell.setError();
        assertEquals(CellType.ERROR, cell.getCellType());
        assertEquals("#ERROR", cell.getAsText(Locale.US).toString());
        assertEquals("#ERROR", cell.toString());
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
        cell.set("test");
        cell.setHyperlink(URI.create("https://example.com"));
        cell.clear();

        assertEquals(CellType.BLANK, cell.getCellType());
        assertTrue(cell.isEmpty());
        assertFalse(cell.getHyperlink().isPresent());
    }

    @Test
    void testCellStyle() {
        Cell cell = row.getCell(0);
        GenericCellStyle defaultStyle = (GenericCellStyle) cell.getCellStyle();
        assertNotNull(defaultStyle);

        GenericCellStyle customStyle = workbook.getCellStyle("CustomStyle");
        customStyle.setHAlign(HAlign.ALIGN_RIGHT);
        cell.setCellStyle(customStyle);
        assertEquals(customStyle, cell.getCellStyle());
        assertEquals(HAlign.ALIGN_RIGHT, cell.getCellStyle().getHAlign());

        cell.setCellStyle("Default");
        assertEquals("Default", cell.getCellStyle().getName());
    }

    @Test
    void testCopyCell() {
        Cell src = row.getCell(0);
        src.set("Copied Content");
        src.setHyperlink(URI.create("https://example.com"));

        Cell dst = row.getCell(1);
        dst.copy(src);

        assertEquals(CellType.TEXT, dst.getCellType());
        assertEquals("Copied Content", dst.getText().toString());
    }

    @Test
    void testMergeAndSpan() {
        Cell c00 = row.getCell(0);
        assertEquals(1, c00.getHorizontalSpan());
        assertEquals(1, c00.getVerticalSpan());
        assertFalse(c00.isMerged());

        sheet.addMergedRegion(new RectangularRegion(0, 1, 0, 1));
        assertTrue(c00.isMerged());
        assertEquals(2, c00.getHorizontalSpan());
        assertEquals(2, c00.getVerticalSpan());
        assertSame(c00, c00.getLogicalCell());

        Cell c01 = row.getCell(1);
        assertTrue(c01.isMerged());
        assertSame(c00, c01.getLogicalCell());

        c00.unMerge();
        assertFalse(c00.isMerged());
        assertEquals(1, c00.getHorizontalSpan());
        assertEquals(1, c00.getVerticalSpan());
        assertFalse(c01.isMerged());
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
        assertEquals(CellType.DATE, cell.getCellType());
        assertEquals(date, cell.getDate());

        LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 12, 0);
        cell.set((Object) dt);
        assertEquals(CellType.DATE_TIME, cell.getCellType());
        assertEquals(dt, cell.getDateTime());

        RichText rt = RichText.valueOf("Rich");
        cell.set((Object) rt);
        assertEquals(CellType.TEXT, cell.getCellType());
        assertEquals(rt, cell.getText());

        cell.set((Object) "Simple String");
        assertEquals(CellType.TEXT, cell.getCellType());
        assertEquals("Simple String", cell.getText().toString());

        // Arbitrary object fallback
        cell.set((Object) URI.create("https://test.com"));
        assertEquals(CellType.TEXT, cell.getCellType());
        assertEquals("https://test.com", cell.getText().toString());

        // Null clears
        cell.set((Object) null);
        assertEquals(CellType.BLANK, cell.getCellType());
        assertTrue(cell.isEmpty());
    }

    @Test
    void testCellRef() {
        Cell c = sheet.getCell(2, 3); // D3
        assertEquals("D3", c.getCellRef());
        assertEquals("'TestSheet'!D3", c.getCellRef(com.dua3.meja.model.RefOption.WITH_SHEET));
        assertEquals("$D3", c.getCellRef(com.dua3.meja.model.RefOption.FIX_COLUMN));
        assertEquals("D$3", c.getCellRef(com.dua3.meja.model.RefOption.FIX_ROW));
        assertEquals("$D$3", c.getCellRef(com.dua3.meja.model.RefOption.FIX_COLUMN, com.dua3.meja.model.RefOption.FIX_ROW));
        assertEquals("'TestSheet'!$D$3", c.getCellRef(com.dua3.meja.model.RefOption.WITH_SHEET, com.dua3.meja.model.RefOption.FIX_COLUMN, com.dua3.meja.model.RefOption.FIX_ROW));
    }

    @Test
    void testResolvedHyperlink() {
        Cell cell = row.getCell(0);
        assertFalse(cell.getResolvedHyperlink().isPresent());

        // Absolute URI
        cell.setHyperlink(URI.create("https://example.com/test"));
        assertEquals(Optional.of(URI.create("https://example.com/test")), cell.getResolvedHyperlink());

        // Relative URI without workbook URI throws IllegalStateException
        cell.setHyperlink(URI.create("relative/path.txt"));
        assertThrows(IllegalStateException.class, cell::getResolvedHyperlink);

        // With workbook URI
        workbook.setUri(URI.create("file:///base/dir/workbook.xlsx"));
        assertEquals(Optional.of(URI.create("file:///base/dir/relative/path.txt")), cell.getResolvedHyperlink());
    }

    @Test
    void testErrorHandling() {
        Cell cell = row.getCell(0);
        assertFalse(cell.isError());

        cell.setError();
        assertTrue(cell.isError());
        assertEquals(CellType.ERROR, cell.getCellType());
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
        GenericCellStyle style = workbook.getCellStyle("Rotated");
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

        GenericCellStyle style = workbook.getCellStyle("Bordered");
        style.setBorderStyle(com.dua3.meja.model.Direction.NORTH, new com.dua3.meja.model.BorderStyle(2.0f, com.dua3.utility.data.Color.RED));
        c11.setCellStyle(style);

        assertEquals(2.0f, c11.getEffectiveBorderStyle(com.dua3.meja.model.Direction.NORTH).width());
        assertEquals(com.dua3.utility.data.Color.RED, c11.getEffectiveBorderStyle(com.dua3.meja.model.Direction.NORTH).color());
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
}
