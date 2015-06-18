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
package com.dua3.meja.model.generic;

import com.dua3.meja.io.CsvReader;
import com.dua3.meja.io.DataException;
import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.WorkbookFactory;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericWorkbookFactory extends WorkbookFactory {

    private static final GenericWorkbookFactory INSTANCE = new GenericWorkbookFactory();

    public static GenericWorkbookFactory instance() {
        return INSTANCE;
    }

    private GenericWorkbookFactory() {
    }

    @Override
    public GenericWorkbook open(File file) throws IOException {
        Locale locale = Locale.getDefault();
        
        FileType type = FileType.forFile(file);
        
        if (type==null) {
            // if type could not be determined, try to open as CSV
            type = FileType.CSV;
        }
        
        if (!type.isSupported(OpenMode.READ)) {
            throw new IllegalArgumentException("Reading is not supported for files of type '"+type.getDescription()+"'.");
        }
        
        return type.getReader().read(GenericWorkbook.class, locale, file);
    }

    @Override
    public GenericWorkbook create(Locale locale) {
        return new GenericWorkbook(locale, null);
    }

    @Override
    public GenericWorkbook createStreaming(Locale locale) {
        return create(locale);
    }

    @Override
    public GenericWorkbook create() {
        return create(Locale.getDefault());
    }

    @Override
    public GenericWorkbook createStreaming() {
        return create();
    }

}
