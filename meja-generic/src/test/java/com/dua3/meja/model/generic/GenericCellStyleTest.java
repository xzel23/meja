package com.dua3.meja.model.generic;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import com.dua3.utility.data.Color;
import com.dua3.utility.text.FontUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class GenericCellStyleTest {

    private GenericWorkbook workbook;
    private GenericCellStyle style;

    @BeforeEach
    void setUp() {
        workbook = GenericWorkbookFactory.instance().create();
        style = new GenericCellStyle(workbook);
    }

    @Test
    void testDefaults() {
        assertEquals(workbook, style.getWorkbook());
        assertEquals(FillPattern.NONE, style.getFillPattern());
        assertEquals(Color.WHITE, style.getFillBgColor());
        assertEquals(Color.WHITE, style.getFillFgColor());
        assertEquals(HAlign.ALIGN_AUTOMATIC, style.getHAlign());
        assertEquals(VAlign.ALIGN_MIDDLE, style.getVAlign());
        assertFalse(style.isWrap());
        assertEquals("", style.getDataFormat());
        assertEquals(0, style.getRotation());
        for (Direction d : Direction.values()) {
            assertEquals(0.0f, style.getBorderStyle(d).width());
        }
    }

    @Test
    void testSettersAndGetters() {
        style.setFont(FontUtil.getInstance().getFont("Courier-12"));
        assertNotNull(style.getFont());

        style.setFillBgColor(Color.BLUE);
        assertEquals(Color.BLUE, style.getFillBgColor());

        style.setFillFgColor(Color.RED);
        assertEquals(Color.RED, style.getFillFgColor());

        style.setFillPattern(FillPattern.SOLID);
        assertEquals(FillPattern.SOLID, style.getFillPattern());

        style.setHAlign(HAlign.ALIGN_CENTER);
        assertEquals(HAlign.ALIGN_CENTER, style.getHAlign());

        style.setVAlign(VAlign.ALIGN_TOP);
        assertEquals(VAlign.ALIGN_TOP, style.getVAlign());

        style.setWrap(true);
        assertTrue(style.isWrap());

        style.setRotation((short) 45);
        assertEquals(45, style.getRotation());

        BorderStyle border = new BorderStyle(2.0f, Color.GREEN);
        style.setBorderStyle(Direction.NORTH, border);
        assertEquals(border, style.getBorderStyle(Direction.NORTH));

        for (Direction d : Direction.values()) {
            style.setBorderStyle(d, border);
            assertEquals(border, style.getBorderStyle(d));
        }
    }

    @Test
    void testDataFormatAndNumberFormatting() {
        style.setDataFormat("#,##0.00");
        assertEquals("#,##0.00", style.getDataFormat());

        String formattedUS = style.format(12345.678, Locale.US);
        assertEquals("12,345.68", formattedUS);

        String formattedDE = style.format(12345.678, Locale.GERMANY);
        assertEquals("12.345,68", formattedDE);
    }

    @Test
    void testDataFormatAndDateFormatting() {
        style.setDataFormat("yyyy-MM-dd");
        LocalDate date = LocalDate.of(2023, 11, 25);
        assertEquals("2023-11-25", style.format(date, Locale.US));

        LocalDateTime dt = LocalDateTime.of(2023, 11, 25, 15, 30, 0);
        style.setDataFormat("yyyy-MM-dd HH:mm");
        assertEquals("2023-11-25 15:30", style.format(dt, Locale.US));
    }

    @Test
    void testCopyStyle() {
        style.setHAlign(HAlign.ALIGN_RIGHT);
        style.setDataFormat("0.0%");
        style.setWrap(true);

        GenericCellStyle copy = new GenericCellStyle(workbook);
        copy.copyStyle(style);

        assertEquals(HAlign.ALIGN_RIGHT, copy.getHAlign());
        assertEquals("0.0%", copy.getDataFormat());
        assertTrue(copy.isWrap());
    }

    @Test
    void testRotationBounds() {
        style.setRotation((short) -90);
        assertEquals((short) -90, style.getRotation());

        style.setRotation((short) 90);
        assertEquals((short) 90, style.getRotation());

        assertThrows(IllegalArgumentException.class, () -> style.setRotation((short) -91));
        assertThrows(IllegalArgumentException.class, () -> style.setRotation((short) 91));
    }

    @Test
    void testInvalidFormatFallbacks() {
        // Invalid date format should fallback gracefully
        style.setDataFormat("invalid [[[]]] pattern");
        LocalDate date = LocalDate.of(2023, 1, 1);
        assertNotNull(style.format(date, Locale.US));

        // Invalid number format should fallback gracefully
        style.setDataFormat("invalid [[[]]] number pattern");
        assertNotNull(style.format(123.45, Locale.US));
    }

    @Test
    void testStyleName() {
        GenericCellStyle registered = workbook.getCellStyle("NamedStyle");
        assertEquals("NamedStyle", registered.getName());
    }
}
