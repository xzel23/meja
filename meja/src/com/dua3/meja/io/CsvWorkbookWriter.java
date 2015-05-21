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
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.MejaHelper;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CsvWorkbookWriter extends WorkbookWriter {

    private static final CsvWorkbookWriter INSTANCE = new CsvWorkbookWriter();

    public static CsvWorkbookWriter instance() {
        return INSTANCE;
    }

    private CsvWorkbookWriter() {
    }

    /**
     * Append workbook data as CSV to the given Appendable.
     * @param workbook the workbook to write
     * @param app Appendable to write to
     * @throws IOException 
     */
    public void write(Workbook workbook, Appendable app) throws IOException {
        try (CsvWriter writer = new CsvWriter(MejaHelper.createWriter(app))) {
            writeSheets(workbook, writer);
        }
    }
    
    @Override
    public void write(Workbook workbook, OutputStream out) throws IOException {
        try (CsvWriter writer = new CsvWriter(out)) {
            writeSheets(workbook, writer);
        }
    }

    private void writeSheets(Workbook workbook, final CsvWriter writer) {
        for (Sheet sheet: workbook) {
            for (Row row:sheet) {
                for (Cell cell: row) {
                    writer.addField(cell.toString());
                }
                writer.nextRow();
            }
            writer.nextRow();
        }
    }

}
