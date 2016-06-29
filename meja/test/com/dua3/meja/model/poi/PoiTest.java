/*
 * Copyright 2016 a5xysq1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.model.poi;

import com.dua3.meja.model.Color;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author a5xysq1
 */
public class PoiTest {

    public PoiTest() {
    }

    /**
     * Test converting color from Meja to POI color and back again does
     * not change the color value.
     * 
     * This test checks the implementation for XSSF.
     */
    @Test
    public void testColorConversionXssf() {
        PoiWorkbook.PoiXssfWorkbook wb = (PoiWorkbook.PoiXssfWorkbook) PoiWorkbookFactory.instance().createXlsx();
        for (Color col: Color.values()) {
            XSSFColor poiColor = wb.getPoiColor(col);
            Color expected = col;
            Color actual = wb.getColor(poiColor, Color.BLACK);
            assertEquals(expected, actual);
        }
    }
    
    /**
     * Test converting color from Meja to POI color and back again does
     * not change the color value.
     * 
     * This test checks the implementation for HSSF.
     */
    @Test
    public void testColorConversionHssf() {
        PoiWorkbook.PoiHssfWorkbook wb = (PoiWorkbook.PoiHssfWorkbook) PoiWorkbookFactory.instance().createXls();
        // HSSF seems to map colors to more or less matching nearest colors.
        // The first 16 entries seem to be exact.
        int n=0;
        for (Color col: Color.values()) {
            if (++n > 16) {
                break;
            }
            
            HSSFColor poiColor = wb.getPoiColor(col);
            Color expected = col;
            Color actual = wb.getColor(poiColor, Color.BLACK);
            assertEquals(expected, actual);
        }
    }
    
}
