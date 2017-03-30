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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.Option;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CsvWorkbookWriter extends WorkbookWriter {

    /**
     * The singleton instance.
     * @return the singleton instance of {@code CsvWorkbookWriter}
     */
    public static CsvWorkbookWriter create() {
        return new CsvWorkbookWriter();
    }

    private Map<Option<?>, Object> options = Collections.emptyMap();

    private CsvWorkbookWriter() {
    }

    @Override
    public void write(Workbook workbook, OutputStream out) throws IOException {
      try (CsvWriter writer = CsvWriter.create(out, options)) {
          writeSheets(workbook, writer);
      }
    }

    /**
     * Write to a BufferedWriter.
     * This is implemented because CSV is a character format. The Encoding must be
     * set correctly in the writer.
     * @param workbook
     * @param out
     * @throws IOException
     */
    public void write(Workbook workbook, BufferedWriter out) throws IOException {
      try (CsvWriter writer = CsvWriter.create(out, options)) {
          writeSheets(workbook, writer);
      }
    }

    private static void writeSheets(Workbook workbook, final CsvWriter writer) throws IOException {
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

    @Override
    public void setOptions(Map<Option<?>, Object> importSettings) {
      this.options  = new HashMap<>(importSettings);
    }
}
