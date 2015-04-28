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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public abstract class WorkbookWriter {

    public void write(Workbook workbook, File file) throws IOException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            write(workbook, out);
        }
    }

    public void write(Workbook workbook, URI uri) throws IOException {
        URLConnection connection = uri.toURL().openConnection();
        connection.connect();
        try (OutputStream out = new BufferedOutputStream(connection.getOutputStream())) {
            write(workbook, out);
        }
    }

    public abstract void write(Workbook workbook, OutputStream out) throws IOException;

}
