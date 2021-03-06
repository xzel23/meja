/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.Options;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CsvWorkbookWriter extends WorkbookWriter {

    /**
     * The singleton instance.
     *
     * @return the singleton instance of {@code CsvWorkbookWriter}
     */
    public static CsvWorkbookWriter create() {
        return new CsvWorkbookWriter();
    }

    private static void writeSheets(Workbook workbook, final CsvWriter writer) throws IOException {
        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    writer.addField(cell.toString());
                }
                writer.nextRow();
            }
            writer.nextRow();
        }
    }

    private Options options = Options.empty();

    private CsvWorkbookWriter() {
    }

    @Override
    public void setOptions(Options importSettings) {
        this.options = new Options(importSettings);
    }

    /**
     * Write to a BufferedWriter. This is implemented because CSV is a character
     * format. The Encoding must be set correctly in the writer.
     *
     * @param workbook the workbook to write
     * @param out      the write to write the workbook to
     * @throws IOException if an input/output error occurs
     */
    public void write(Workbook workbook, BufferedWriter out) throws IOException {
        // do not close the writer - it is the caller's responsibility
        CsvWriter csvWriter = CsvWriter.create(out, options);
        writeSheets(workbook, csvWriter);
        csvWriter.flush();
    }

    @Override
    public void write(Workbook workbook, OutputStream out) throws IOException {
        CsvWriter csvWriter = CsvWriter.create(out, options);
        writeSheets(workbook, csvWriter);
        csvWriter.flush();
    }
}
