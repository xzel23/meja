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

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.utility.options.Arguments;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Abstract base class for reading workbook data.
 */
public abstract class WorkbookReader {

    /**
     * Protected constructor for the {@code WorkbookReader} class.
     */
    protected WorkbookReader() {
        // nop
    }

    /**
     * Read workbook from stream.
     *
     * @param <W> workbook class
     * @param factory    the WorkbookFactory to use
     * @param uri        the path to set in the workbook
     * @param in         the stream to read from
     * @return the workbook read
     * @throws IOException if the workbook could not be read
     */
    protected abstract <W extends Workbook> W read(WorkbookFactory<W> factory, URI uri, InputStream in) throws IOException;

    /**
     * Read workbook from URI.
     *
     * @param <W> workbook class
     * @param factory    the WorkbookFactory to use
     * @param uri        the path to set in the workbook from
     * @return the workbook read
     * @throws IOException if the workbook could not be read
     */
    public <W extends Workbook> W read(WorkbookFactory<W> factory, URI uri) throws IOException {
        try (InputStream in = new BufferedInputStream(uri.toURL().openStream())) {
            return read(factory, uri, in);
        }
    }

    /**
     * Set the import options for the reader. This method ignores the passed options; it is meant as a default
     * implementation for Reader implementations that don't take options.
     *
     * @param importSettings the import settings to be set
     */
    public void setOptions(Arguments importSettings) {
        // nop: empty default implementation for Readers that don't take options
    }

}
