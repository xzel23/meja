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

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericRowBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Locale;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CsvWorkbookReader extends WorkbookReader {

    private static final CsvWorkbookReader INSTANCE = new CsvWorkbookReader();

    /**
     * Get instance of {@code CsvWorkbookReader}.
     * @return the singleton instance of {@code CsvWorkbookReader}.
     */
    public static CsvWorkbookReader instance() {
        return INSTANCE;
    }

    private CsvWorkbookReader() {
    }

    @Override
    public <WORKBOOK extends Workbook> WORKBOOK read(Class<WORKBOOK> clazz, Locale locale, InputStream in, URI uri) throws IOException {
        try {
            WORKBOOK workbook = clazz.getConstructor(Locale.class).newInstance(locale);
            workbook.setUri(uri);
            GenericRowBuilder builder = new GenericRowBuilder(workbook.createSheet(uri.getPath()), locale);
            try (CsvReader reader = CsvReader.createReader(builder, in)) {
                reader.readAll();
            }
            return workbook;
        } catch (DataException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException ex) {
            throw new IOException("Error reading workbook: "+ex.getMessage(), ex);
        }
    }

}
