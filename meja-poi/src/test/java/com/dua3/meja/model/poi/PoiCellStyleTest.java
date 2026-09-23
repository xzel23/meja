package com.dua3.meja.model.poi;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import com.dua3.utility.data.Color;
import com.dua3.utility.text.FontUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoiCellStyleTest {

    private PoiWorkbook workbook;

    @BeforeEach
    void setUp() {
        workbook = PoiWorkbookFactory.instance().createXlsx();
    }

    @Test
    void testPropertiesAndSetters() {
        PoiCellStyle custom = workbook.getCellStyle("CustomStyle");
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

        BorderStyle border = new BorderStyle(1.0f, Color.RED);
        custom.setBorderStyle(Direction.NORTH, border);
        assertTrue(custom.getBorderStyle(Direction.NORTH).width() > 0);
    }

    @Test
    void testCopyStyle() {
        PoiCellStyle source = workbook.getCellStyle("SourceStyle");
        source.setHAlign(HAlign.ALIGN_CENTER);
        source.setWrap(true);
        source.setDataFormat("yyyy-MM-dd");

        PoiCellStyle target = workbook.getCellStyle("TargetStyle");
        target.copyStyle(source);

        assertEquals(HAlign.ALIGN_CENTER, target.getHAlign());
        assertTrue(target.isWrap());
        assertEquals("yyyy-MM-dd", target.getDataFormat());
    }
}
