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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;

/**
 * Implementation of {@link WorkbookReader} for Excel files.
 */
public class XlsWorkbookReader extends WorkbookReader {

    private static final XlsWorkbookReader INSTANCE = new XlsWorkbookReader();

    /**
     * Get the singleton instance.
     * @return the singleton instance of {@code XlsWorkbookReader}
     */
    public static XlsWorkbookReader instance() {
        return INSTANCE;
    }

    private XlsWorkbookReader() {
    }

    @Override
    public <WORKBOOK extends Workbook> WORKBOOK read(Class<WORKBOOK> clazz, Locale locale, InputStream in, URI uri) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
