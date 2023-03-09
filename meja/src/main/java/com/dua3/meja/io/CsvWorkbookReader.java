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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.dua3.utility.io.CsvReader;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.utility.io.CsvReader.RowBuilder;
import com.dua3.utility.options.Arguments;

/**
 * @author Axel Howind (axel@dua3.com)
 */
public final class CsvWorkbookReader extends WorkbookReader {

    /**
     * Create a new instance of {@code CsvWorkbookReader}.
     *
     * @return the singleton instance of {@code CsvWorkbookReader}.
     */
    public static CsvWorkbookReader create() {
        return new CsvWorkbookReader();
    }

    private Arguments options = Arguments.empty();

    private CsvWorkbookReader() {
    }

    /**
     * Read from a BufferedReader. This is implemented because CSV is a character
     * format. The Encoding must be set correctly in the reader.
     *
     * @param <W>     the Workbook implementation class to use
     * @param factory the WorkbookFactory to use
     * @param in      the reader to read from
     * @param uri     the URI of the source (for creating meaningful error messages)
     * @return the workbook read
     * @throws IOException if an io-error occurs during reading
     */
    public <W extends Workbook> W read(WorkbookFactory<W> factory, BufferedReader in, URI uri) throws IOException {
        W workbook = factory.create();
        workbook.setUri(uri);
        RowBuilder builder = new SheetRowBuilder(workbook.createSheet(uri.toString()), options);
        workbook.setObjectCaching(true);
        try (CsvReader reader = CsvReader.create(builder, in, options)) {
            reader.readAll();
        } finally {
            workbook.setObjectCaching(false);
        }
        return workbook;
    }

    @Override
    protected <W extends Workbook> W read(WorkbookFactory<W> factory, InputStream in, URI uri) throws IOException {
        W workbook = factory.create();
        workbook.setUri(uri);
        RowBuilder builder = new SheetRowBuilder(workbook.createSheet("Sheet 1"), options);
        workbook.setObjectCaching(true);
        try (CsvReader reader = CsvReader.create(builder, in, options)) {
            reader.readAll();
        } finally {
            workbook.setObjectCaching(false);
        }
        return workbook;
    }

    @Override
    public void setOptions(Arguments importSettings) {
        this.options = importSettings;
    }
}
