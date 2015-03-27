/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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

import com.dua3.meja.model.Row;
import com.dua3.meja.model.poi.PoiCell.PoiHssfCell;
import com.dua3.meja.model.poi.PoiCell.PoiXssfCell;
import com.dua3.meja.model.poi.PoiSheet.PoiHssfSheet;
import com.dua3.meja.model.poi.PoiSheet.PoiXssfSheet;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author axel
 * @param <WORKBOOK>
 * @param <SHEET>
 * @param <ROW>
 * @param <CELL>
 * @param <CELLSTYLE>
 * @param <COLOR>
 */
public abstract class PoiRow<WORKBOOK extends org.apache.poi.ss.usermodel.Workbook, SHEET extends org.apache.poi.ss.usermodel.Sheet, ROW extends org.apache.poi.ss.usermodel.Row, CELL extends org.apache.poi.ss.usermodel.Cell, CELLSTYLE extends org.apache.poi.ss.usermodel.CellStyle, COLOR extends org.apache.poi.ss.usermodel.Color>
        implements Row {


    protected final ROW poiRow;

    public PoiRow(ROW row) {
        this.poiRow = row;
    }

    @Override
    public abstract PoiSheet getSheet();

    static class PoiXssfRow extends PoiRow<
            XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell, XSSFCellStyle, XSSFColor> {

        private final PoiXssfSheet sheet;

        public PoiXssfRow(PoiXssfSheet sheet, XSSFRow row) {
            super(row);
            this.sheet = sheet;
        }

        @Override
        public PoiXssfSheet getSheet() {
            return sheet;
        }
        private final PoiCache<XSSFCell, PoiXssfCell> cache = new PoiCache<XSSFCell, PoiXssfCell>() {

            @Override
            protected PoiXssfCell create(XSSFCell poiCell) {
                return new PoiXssfCell(PoiXssfRow.this, poiCell);
            }

        };

        @Override
        public PoiXssfCell getCell(int col) {
            return cache.get(poiRow.getCell(col));
        }
    }

    static class PoiHssfRow extends PoiRow<
            HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell, HSSFCellStyle, HSSFColor> {

        private final PoiHssfSheet sheet;

        public PoiHssfRow(PoiHssfSheet sheet, HSSFRow row) {
            super(row);
            this.sheet = sheet;
        }

        @Override
        public PoiHssfSheet getSheet() {
            return sheet;
        }

        private final PoiCache<HSSFCell, PoiHssfCell> cache = new PoiCache<HSSFCell, PoiHssfCell>() {

            @Override
            protected PoiHssfCell create(HSSFCell poiCell) {
                return new PoiHssfCell(PoiHssfRow.this, poiCell);
            }

        };

        @Override
        public PoiHssfCell getCell(int col) {
            return cache.get(poiRow.getCell(col));
        }
    }

}
