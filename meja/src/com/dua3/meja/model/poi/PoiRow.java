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

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.poi.PoiCell.PoiHssfCell;
import com.dua3.meja.model.poi.PoiCell.PoiXssfCell;
import com.dua3.meja.model.poi.PoiSheet.PoiHssfSheet;
import com.dua3.meja.model.poi.PoiSheet.PoiXssfSheet;
import com.dua3.meja.util.Cache;

import java.util.Iterator;
import java.util.Objects;

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
    protected final int rowNumber;

    public PoiRow(ROW row) {
        this.poiRow = row;
        this.rowNumber = poiRow.getRowNum();
    }

    @SuppressWarnings("rawtypes")
	@Override
    public boolean equals(Object obj) {
        if (obj instanceof PoiRow) {
            return Objects.equals(poiRow, ((PoiRow)obj).poiRow);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return poiRow.hashCode();
    }

    @Override
    public int getRowNumber() {
    	return rowNumber;
    }
    
    @Override
    public abstract PoiSheet<WORKBOOK, SHEET, ROW, CELL, CELLSTYLE, COLOR> getSheet();

	@Override
	public Iterator<Cell> iterator() {
		return new Iterator<Cell>() {

			private int colNum=PoiRow.this.poiRow.getFirstCellNum();
			
			@Override
			public boolean hasNext() {
				return colNum<PoiRow.this.poiRow.getLastCellNum();
			}

			@Override
			public Cell next() {
				return getCell(colNum++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removing of cells is not supported.");
			}
		};
	}
    
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
        private final Cache<XSSFCell, PoiXssfCell> cache = new Cache<XSSFCell, PoiXssfCell>(Cache.Type.WEAK_KEYS) {

            @Override
            protected PoiXssfCell create(XSSFCell poiCell) {
                return new PoiXssfCell(PoiXssfRow.this, poiCell);
            }

        };

        @Override
        public PoiXssfCell getCell(int col) {
            XSSFCell poiCell = poiRow.getCell(col);
            if(poiCell==null) {
                poiCell=poiRow.createCell(col);
            }
            return cache.get(poiCell);
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

        private final Cache<HSSFCell, PoiHssfCell> cache = new Cache<HSSFCell, PoiHssfCell>(Cache.Type.WEAK_KEYS) {

            @Override
            protected PoiHssfCell create(HSSFCell poiCell) {
                return new PoiHssfCell(PoiHssfRow.this, poiCell);
            }

        };

        @Override
        public PoiHssfCell getCell(int col) {
            HSSFCell poiCell = poiRow.getCell(col);
            if(poiCell==null) {
                poiCell=poiRow.createCell(col);
            }
            return cache.get(poiCell);
        }
    }

}
