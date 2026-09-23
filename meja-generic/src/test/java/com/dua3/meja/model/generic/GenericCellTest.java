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
}
