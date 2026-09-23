package com.dua3.meja.model.poi;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.FontUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoiFontTest {

    @Test
    void testHssfFont() {
        PoiWorkbook wb = PoiWorkbookFactory.instance().createXls();
        Font baseFont = FontUtil.getInstance().getFont("Arial-12-bold");
        PoiFont poiFont = wb.createFont(baseFont);

        assertNotNull(poiFont);
        assertEquals("Arial", poiFont.getFont().getFamily());
        assertEquals(12.0f, poiFont.getFont().getSizeInPoints());
        assertTrue(poiFont.getFont().isBold());

        FontDef fd = new FontDef();
        fd.setItalic(true);
        PoiFont derived = poiFont.deriveFont(fd);
        assertTrue(derived.getFont().isItalic());

        assertEquals(poiFont, new PoiFont(wb, poiFont.getPoiFont()));
        assertEquals(poiFont.hashCode(), new PoiFont(wb, poiFont.getPoiFont()).hashCode());
        assertNotNull(poiFont.toString());
    }

    @Test
    void testXssfFont() {
        PoiWorkbook wb = PoiWorkbookFactory.instance().createXlsx();
        Font baseFont = FontUtil.getInstance().getFont("Calibri-14-italic");
        PoiFont poiFont = wb.createFont(baseFont);

        assertNotNull(poiFont);
        assertEquals("Calibri", poiFont.getFont().getFamily());
        assertEquals(14.0f, poiFont.getFont().getSizeInPoints());
        assertTrue(poiFont.getFont().isItalic());

        FontDef fd = new FontDef();
        fd.setColor(Color.RED);
        PoiFont derived = poiFont.deriveFont(fd);
        assertEquals(Color.RED, derived.getFont().getColor());
    }
}
