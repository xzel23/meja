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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.Options;

/**
 * Abstract base class for writing workbook data.
 */
public abstract class WorkbookWriter {

    public void setOptions(Options exportSettings) {
        // empty implementation for writers not taking export options
    }

    /**
     * Write workbook to file.
     *
     * @param workbook
     *            the workbook to write
     * @param path
     *            the path to write to
     * @throws IOException
     *             if an error occurs when writing out the workbook
     */
    public void write(Workbook workbook, Path path) throws IOException {
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
            write(workbook, out);
        }
    }

    /**
     * Write workbook to a stream.
     *
     * @param workbook
     *            the workbook to write
     * @param out
     *            the stream to write to
     * @throws IOException
     *             if an error occurs when writing out the workbook
     */
    public abstract void write(Workbook workbook, OutputStream out) throws IOException;

}
