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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.util.Options;

/**
 * Abstract base class for reading workbook data.
 */
public abstract class WorkbookReader {

    /**
     * Read workbook from stream.
     *
     * @param         <WORKBOOK> workbook class
     * @param factory the WorkbookFactory to use
     * @param in      the stream to read from
     * @param path    the path to set in the workbook
     * @return the workbook read
     * @throws IOException if the workbook could not be read
     */
    protected abstract <WORKBOOK extends Workbook> WORKBOOK read(WorkbookFactory<WORKBOOK> factory, InputStream in,
            Path path) throws IOException;

    /**
     * Read workbook from URI.
     *
     * @param         <WORKBOOK> workbook class
     * @param factory the WorkbookFactory to use
     * @param path    the path to set in the workbook from
     * @return the workbook read
     * @throws IOException if the workbook could not be read
     */
    public <WORKBOOK extends Workbook> WORKBOOK read(WorkbookFactory<WORKBOOK> factory, Path path) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            return read(factory, in, path);
        }
    }

    public void setOptions(Options importSettings) {
        // nop: empty default implementation for Readers that don't take options
    }

}
