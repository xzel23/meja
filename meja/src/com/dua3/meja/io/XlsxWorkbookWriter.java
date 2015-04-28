/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
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
package com.dua3.meja.io;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.PoiHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class XlsxWorkbookWriter extends WorkbookWriter {

    private static final XlsxWorkbookWriter INSTANCE = new XlsxWorkbookWriter();

    public static XlsxWorkbookWriter instance() {
        return INSTANCE;
    }

    private XlsxWorkbookWriter() {
    }

    @Override
    public void write(Workbook workbook, OutputStream out) throws IOException {
        try (ExcelWriterImp writer = new ExcelWriterImp(workbook)) {
            writer.write(out);
        }
    }

    private static class ExcelWriterImp implements AutoCloseable {

        private final Workbook workbook;
        private final SXSSFWorkbook poiWorkbook;
        private final Map<String, Short> styles = new HashMap<>();

        public ExcelWriterImp(Workbook workbook) {
            this.workbook = workbook;
            this.poiWorkbook = new SXSSFWorkbook();
        }

        public void write(OutputStream out) throws IOException {
            buildPoiWorkbook();
            poiWorkbook.write(out);
        }

        private void buildPoiWorkbook() {
            // copy styles
            for (String styleName : workbook.getCellStyleNames()) {
                CellStyle cellStyle = workbook.getCellStyle(styleName);
                org.apache.poi.ss.usermodel.CellStyle poiCellStyle = poiWorkbook.createCellStyle();
                styles.put(styleName, poiCellStyle.getIndex());
                poiCellStyle.setAlignment(PoiHelper.hAlignToPoi(cellStyle.getHAlign()));
                poiCellStyle.setVerticalAlignment(PoiHelper.vAlignToPoi(cellStyle.getVAlign()));
                // TODO font etc.
            }

            // copy sheets
            for (Sheet sheet : workbook) {
                org.apache.poi.ss.usermodel.Sheet poiSheet = poiWorkbook.createSheet(sheet.getSheetName());
                // copy rows
                for (Row row : sheet) {
                    org.apache.poi.ss.usermodel.Row poiRow = poiSheet.createRow(row.getRowNumber());
                    // copy cells
                    for (Cell cell : row) {
                        org.apache.poi.ss.usermodel.Cell poiCell = poiRow.createCell(cell.getColumnNumber());
                        copyCellData(poiCell, cell);
                    }
                }
            }
        }

        private void copyCellData(org.apache.poi.ss.usermodel.Cell poiCell, Cell cell) {
            // copy value and type
            switch (cell.getCellType()) {
                case BLANK:
                    poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK);
                    break;
                case BOOLEAN:
                    poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN);
                    poiCell.setCellValue(cell.getBoolean());
                    break;
                case ERROR:
                    poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_ERROR);
                    break;
                case FORMULA:
                    poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA);
                    poiCell.setCellValue(cell.getFormula());
                    break;
                case NUMERIC:
                    poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
                    poiCell.setCellValue(cell.getNumber().doubleValue());
                    break;
                case TEXT:
                    poiCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING);
                    poiCell.setCellValue(cell.getAsText()); // FIXME use RichTextString
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            // copy style
            poiCell.setCellStyle(poiWorkbook.getCellStyleAt(styles.get(cell.getCellStyle().getName())));
        }

        @Override
        public void close() throws IOException {
            poiWorkbook.dispose();
        }

    }
}
