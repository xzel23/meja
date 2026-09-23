package com.dua3.meja.model.poi;

import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoiHelperTest {

    @Test
    void testHAlignConversions() {
        assertEquals(HorizontalAlignment.LEFT, PoiHelper.hAlignToPoi(HAlign.ALIGN_LEFT));
        assertEquals(HorizontalAlignment.RIGHT, PoiHelper.hAlignToPoi(HAlign.ALIGN_RIGHT));
        assertEquals(HorizontalAlignment.CENTER, PoiHelper.hAlignToPoi(HAlign.ALIGN_CENTER));
        assertEquals(HorizontalAlignment.JUSTIFY, PoiHelper.hAlignToPoi(HAlign.ALIGN_JUSTIFY));
        assertEquals(HorizontalAlignment.DISTRIBUTED, PoiHelper.hAlignToPoi(HAlign.ALIGN_DISTRIBUTED));
        assertEquals(HorizontalAlignment.GENERAL, PoiHelper.hAlignToPoi(HAlign.ALIGN_AUTOMATIC));

        assertEquals(HAlign.ALIGN_LEFT, PoiHelper.poiToHAlign(HorizontalAlignment.LEFT));
        assertEquals(HAlign.ALIGN_RIGHT, PoiHelper.poiToHAlign(HorizontalAlignment.RIGHT));
        assertEquals(HAlign.ALIGN_CENTER, PoiHelper.poiToHAlign(HorizontalAlignment.CENTER));
        assertEquals(HAlign.ALIGN_CENTER, PoiHelper.poiToHAlign(HorizontalAlignment.CENTER_SELECTION));
        assertEquals(HAlign.ALIGN_JUSTIFY, PoiHelper.poiToHAlign(HorizontalAlignment.JUSTIFY));
        assertEquals(HAlign.ALIGN_JUSTIFY, PoiHelper.poiToHAlign(HorizontalAlignment.FILL));
        assertEquals(HAlign.ALIGN_DISTRIBUTED, PoiHelper.poiToHAlign(HorizontalAlignment.DISTRIBUTED));
        assertEquals(HAlign.ALIGN_AUTOMATIC, PoiHelper.poiToHAlign(HorizontalAlignment.GENERAL));
    }

    @Test
    void testVAlignConversions() {
        assertEquals(VerticalAlignment.TOP, PoiHelper.vAlignToPoi(VAlign.ALIGN_TOP));
        assertEquals(VerticalAlignment.CENTER, PoiHelper.vAlignToPoi(VAlign.ALIGN_MIDDLE));
        assertEquals(VerticalAlignment.BOTTOM, PoiHelper.vAlignToPoi(VAlign.ALIGN_BOTTOM));
        assertEquals(VerticalAlignment.JUSTIFY, PoiHelper.vAlignToPoi(VAlign.ALIGN_JUSTIFY));
        assertEquals(VerticalAlignment.DISTRIBUTED, PoiHelper.vAlignToPoi(VAlign.ALIGN_DISTRIBUTED));

        assertEquals(VAlign.ALIGN_TOP, PoiHelper.poiToVAlign(VerticalAlignment.TOP));
        assertEquals(VAlign.ALIGN_MIDDLE, PoiHelper.poiToVAlign(VerticalAlignment.CENTER));
        assertEquals(VAlign.ALIGN_BOTTOM, PoiHelper.poiToVAlign(VerticalAlignment.BOTTOM));
        assertEquals(VAlign.ALIGN_JUSTIFY, PoiHelper.poiToVAlign(VerticalAlignment.JUSTIFY));
        assertEquals(VAlign.ALIGN_DISTRIBUTED, PoiHelper.poiToVAlign(VerticalAlignment.DISTRIBUTED));
    }
}
