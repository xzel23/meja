package com.dua3.meja.model.poi;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import com.dua3.utility.data.Color;
import com.dua3.utility.text.FontUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class PoiCellStyleTest {

    private PoiWorkbook workbook;

    @BeforeEach
    void setUp() {
        workbook = PoiWorkbookFactory.instance().createXlsx();
    }

    @Test
    void testPropertiesAndSetters() {
        testCellStyleProperties(workbook);
    }

    @Test
    void testHssfPropertiesAndSetters() {
        PoiWorkbook hssfWb = PoiWorkbookFactory.instance().createXls();
        testCellStyleProperties(hssfWb);
    }

    @SuppressWarnings("java:S5863")
    private void testCellStyleProperties(PoiWorkbook wb) {
        PoiCellStyle custom = wb.getCellStyle("CustomStyle");
        custom.setFont(FontUtil.getInstance().getFont("Arial-12-bold"));
        assertNotNull(custom.getFont());

        custom.setHAlign(HAlign.ALIGN_RIGHT);
        assertEquals(HAlign.ALIGN_RIGHT, custom.getHAlign());

        custom.setVAlign(VAlign.ALIGN_TOP);
        assertEquals(VAlign.ALIGN_TOP, custom.getVAlign());

        custom.setWrap(true);
        assertTrue(custom.isWrap());

        custom.setDataFormat("#,##0.00");
        assertEquals("#,##0.00", custom.getDataFormat());

        custom.setRotation((short) 45);
        assertEquals((short) 45, custom.getRotation());

        custom.setFillPattern(FillPattern.SOLID);
        assertEquals(FillPattern.SOLID, custom.getFillPattern());

        custom.setFillFgColor(Color.YELLOW);
        assertNotNull(custom.getFillFgColor());

        custom.setFillBgColor(Color.BLUE);
        assertNotNull(custom.getFillBgColor());

        for (Direction direction : Direction.values()) {
            BorderStyle border = new BorderStyle(1.0f, Color.RED);
            custom.setBorderStyle(direction, border);
            assertTrue(custom.getBorderStyle(direction).width() > 0);
        }

        assertNotNull(custom.toString());
        assertEquals(custom, custom);
        assertNotEquals(null, custom);
    }

    @Test
    void testCopyStyle() {
        PoiCellStyle source = workbook.getCellStyle("SourceStyle");
        source.setHAlign(HAlign.ALIGN_CENTER);
        source.setWrap(true);
        source.setDataFormat("yyyy-MM-dd");
        source.setRotation((short) 30);
        source.setFillPattern(FillPattern.SOLID);

        PoiCellStyle target = workbook.getCellStyle("TargetStyle");
        target.copyStyle(source);

        assertEquals(HAlign.ALIGN_CENTER, target.getHAlign());
        assertTrue(target.isWrap());
        assertEquals("yyyy-MM-dd", target.getDataFormat());
        assertEquals((short) 30, target.getRotation());
        assertEquals(FillPattern.SOLID, target.getFillPattern());
    }

    @Test
    void testDateFormatVariants() {
        PoiCellStyle style = workbook.getCellStyle("DateFormatTest");

        String[] formats = {
                "yyyy\\-mm\\-dd",
                "d/m",
                "dddd",
                "m/d/yy",
                "d/m/yy",
                "dd/mm/yy",
                "m/d/yyyy",
                "d/m/yyyy",
                "[$-F800]dddd\\,\\ mmm\\ dd\\,\\ yyyy;@"
        };

        for (String fmt : formats) {
            style.setDataFormat(fmt);
            // Verify method executes without error for both matched and unmatched format strings
            style.getLocaleAwareDateFormat(Locale.US);
            style.getLocaleAwareDateFormat(Locale.GERMANY);
        }

        // Specific match checks
        style.setDataFormat("yyyy\\-mm\\-dd");
        assertNotNull(style.getLocaleAwareDateFormat(Locale.US));

        style.setDataFormat("dddd");
        assertNotNull(style.getLocaleAwareDateFormat(Locale.US));

        // Default/fallback returns null
        style.setDataFormat("unrecognized_date_format");
        assertNull(style.getLocaleAwareDateFormat(Locale.US));

        style.setDataFormat("StandardDataFormats.MEDIUM");
        assertNotNull(style.getDataFormat());

        style.setDataFormat("StandardDataFormats.FULL");
        assertNotNull(style.getDataFormat());
    }

    @Test
    void testBorderWidthCalculations() {
        // Test different border widths
        float[] widths = {0.0f, 0.5f, 0.75f, 1.0f, 1.75f, 2.25f, 3.0f};
        PoiCellStyle style = workbook.getCellStyle("BorderWidths");
        for (float w : widths) {
            style.setBorderStyle(Direction.NORTH, new BorderStyle(w, Color.BLUE));
            assertTrue(style.getBorderStyle(Direction.NORTH).width() >= 0);
        }
    }
}
