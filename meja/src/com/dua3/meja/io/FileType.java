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

import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import com.dua3.meja.util.MejaHelper;
import java.io.File;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public enum FileType {
    CSV("CSV-Data", GenericWorkbookFactory.instance(), CsvWorkbookReader.instance(), CsvWorkbookWriter.instance(),".csv", ".txt"),
    XLS("Excel 97-2003", GenericWorkbookFactory.instance(), XlsWorkbookReader.instance(), XlsWorkbookWriter.instance(), ".xls"),
    XLSX("Excel 2007", PoiWorkbookFactory.instance(), XlsxWorkbookReader.instance(), XlsxWorkbookWriter.instance(), ".xlsx", ".xlsm");

    private final String name;
    private final WorkbookFactory factory;
    private final WorkbookWriter writer;
    private final WorkbookReader reader;
    private final String[] extensions;

    private FileType(String name, WorkbookFactory factory, WorkbookReader reader, WorkbookWriter writer, String... extensions) {
        this.name = name;
        this.factory = factory;
        this.writer = writer;
        this.reader = reader;
        this.extensions = extensions;
        assert extensions.length>0;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extensions[0];
    }

    public WorkbookFactory getFactory() {
        return factory;
    }
    
    public WorkbookWriter getWriter() {
        return writer;
    }

    public WorkbookReader getReader() {
        return reader;
    }

    public static FileType getForFile(File file) {
        String extension = MejaHelper.getFileExtension(file);
        for (FileType type: values()) {
            for (String ext: type.extensions) {
                if (extension.equalsIgnoreCase(ext)) {
                    return type;
                }
            }
        }
        return null;
    }

}
