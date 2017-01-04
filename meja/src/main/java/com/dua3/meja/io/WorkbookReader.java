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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;

/**
 * Abstract base class for reading workbook data.
 */
public abstract class WorkbookReader {

    /**
     * Read workbook from file.
     * @param <WORKBOOK> workbook class
     * @param clazz the {@code Class} instance of the workbook class to instantiate
     * @param locale the locale to use when creating the workbook
     * @param file the file to read from
     * @return the workbook read
     * @throws IOException if the workbook could not be read
     */
    public <WORKBOOK extends Workbook>
    WORKBOOK read(Class<WORKBOOK> clazz, Locale locale, File file) throws IOException {
        return read(clazz, locale, file.toURI());
    }

    /**
     * Read workbook from URI.
     * @param <WORKBOOK> workbook class
     * @param clazz the {@code Class} instance of the workbook class to instantiate
     * @param locale the locale to use when creating the workbook
     * @param uri the uri to set in the workbook
     * @return the workbook read
     * @throws IOException if the workbook could not be read
     */
    public <WORKBOOK extends Workbook>
    WORKBOOK read(Class<WORKBOOK> clazz, Locale locale, URI uri) throws IOException {
        try (InputStream in = uri.toURL().openStream()) {
            return read(clazz, locale, in, uri);
        }
    }

    /**
     * Read workbook from stream.
     * @param <WORKBOOK> workbook class
     * @param clazz the {@code Class} instance of the workbook class to instantiate
     * @param locale the locale to use when creating the workbook
     * @param in the stream to read from
     * @param uri the uri to set in the workbook
     * @return the workbook read
     * @throws IOException if the workbook could not be read
     */
    public abstract  <WORKBOOK extends Workbook>
    WORKBOOK read(Class<WORKBOOK> clazz, Locale locale, InputStream in, URI uri) throws IOException;

}