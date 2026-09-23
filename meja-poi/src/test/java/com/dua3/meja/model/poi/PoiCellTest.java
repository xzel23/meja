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
}
