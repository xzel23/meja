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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public enum FileType {

    /**
     * File type for CSV files.
     */
    CSV("CSV-Data", GenericWorkbookFactory.instance(), CsvWorkbookReader.instance(), CsvWorkbookWriter.instance(), "*.csv", "*.txt"),

    /**
     * File type for the old Excel format that uses the '.xls' extension.
     */
    XLS("Excel 97-2003", PoiWorkbookFactory.instance(), XlsWorkbookReader.instance(), XlsWorkbookWriter.instance(), "*.xls"),

    /**
     * File type for the new XML-based Excel format that uses the '.xlsx' extension.
     */
    XLSX("Excel 2007", PoiWorkbookFactory.instance(), XlsxWorkbookReader.instance(), XlsxWorkbookWriter.instance(), "*.xlsx", "*.xlsm");

    private final String description;
    private final WorkbookFactory factory;
    private final WorkbookWriter writer;
    private final WorkbookReader reader;
    private final String[] extensions;

    private FileType(String description, WorkbookFactory factory, WorkbookReader reader, WorkbookWriter writer, String... extensions) {
        this.description = description;
        this.factory = factory;
        this.writer = writer;
        this.reader = reader;
        this.extensions = extensions;
        assert extensions.length > 0;
    }

    /**
     * Get this format's description
     * @return text description for this format
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get this format's default extension.
     * @return this format's default extension
     */
    public String getDefaultExtension() {
        return extensions[0];
    }

    public String[] getExtensions() {
        return extensions;
    }

    /**
     * Get instance of {@link WorkbookFactory} that matches this format.
     * @return instance of {@link WorkbookFactory}
     */
    public WorkbookFactory getFactory() {
        return factory;
    }

    /**
     * Get instance of {@link WorkbookWriter} that matches this format.
     * @return instance of {@link WorkbookWriter}
     */
    public WorkbookWriter getWriter() {
        return writer;
    }

    /**
     * Get instance of {@link WorkbookReader} that matches this format.
     * @return instance of {@link WorkbookReader}
     */
    public WorkbookReader getReader() {
        return reader;
    }

    /**
     * Tries to determine the FileType instance matching the given file.
     *
     * @param file the file to determine the FileType for
     * @return matching instance of {@link FileType} or {@code null} if none
     * found
     */
    public static FileType forFile(File file) {
        String fileNameLower = file.getName().toLowerCase();
        for (FileType type : values()) {
            for (String ext : type.extensions) {
                if (fileNameLower.endsWith(ext.substring(1).toLowerCase())) {
                    return type;
                }
            }
        }
        return null;
    }

    private OpenMode getOpenMode() {
        if (reader != null && writer != null) {
            return OpenMode.READ_AND_WRITE;
        } else if (reader != null) {
            return OpenMode.READ;
        } else if (writer != null) {
            return OpenMode.WRITE;
        } else {
            return OpenMode.NONE;
        }
    }

    /**
     * Return list of filters supporting the requested operation.
     *
     * @param mode the {@link OpenMode}
     * @return list of filters that support {@code mode}
     */
    public static List<FileType> getFileTypes(OpenMode mode) {
        List<FileType> list = new ArrayList<>();
        for (final FileType type : values()) {
            if (type.isSupported(mode)) {
                list.add(type);
            }
        }
        return list;
    }

    /**
     * Check if the requested operation is supported by this filter.
     *
     * @param modeRequested the {@link OpenMode}
     * @return true if operation is supported
     */
    public boolean isSupported(OpenMode modeRequested) {
        OpenMode mode = getOpenMode();

        switch (modeRequested) {
            case READ:
                return mode == OpenMode.READ || mode == OpenMode.READ_AND_WRITE;
            case WRITE:
                return mode == OpenMode.WRITE || mode == OpenMode.READ_AND_WRITE;
            default:
                throw new IllegalArgumentException(String.valueOf(modeRequested));
        }
    }

}