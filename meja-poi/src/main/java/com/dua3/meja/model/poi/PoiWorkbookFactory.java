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
package com.dua3.meja.model.poi;

import com.dua3.meja.io.FileFormatException;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.meja.model.poi.io.FileTypeExcel;
import com.dua3.meja.model.poi.io.FileTypeXls;
import com.dua3.meja.model.poi.io.FileTypeXlsx;
import com.dua3.utility.options.Arguments;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * A {@link WorkbookFactory} implementation for creating workbooks based on the Apache POI implementation.
 */
public class PoiWorkbookFactory extends WorkbookFactory<PoiWorkbook> {

    private static final PoiWorkbookFactory INSTANCE = new PoiWorkbookFactory();

    private static PoiWorkbook createWorkbook(final org.apache.poi.ss.usermodel.Workbook poiWorkbook, @Nullable URI uri) {
        if (poiWorkbook instanceof HSSFWorkbook hssfWorkbook) {
            return new PoiHssfWorkbook(hssfWorkbook, uri);
        } else {
            return new PoiXssfWorkbook(poiWorkbook, uri);
        }
    }

    /**
     * Get the singleton instance for this factory.
     *
     * @return the singleton instance
     */
    public static PoiWorkbookFactory instance() {
        return INSTANCE;
    }

    /**
     * Open Workbook from InputStream.
     *
     * @param in  the stream to read from
     * @param uri the URI to set
     * @return the workbook
     * @throws IOException if an error occurs when reading
     */
    private static PoiWorkbook open(InputStream in, @Nullable URI uri) throws IOException {
        try {
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook = org.apache.poi.ss.usermodel.WorkbookFactory
                    .create(in);
            return createWorkbook(poiWorkbook, uri);
        } catch (RecordFormatException ex) {
            throw new FileFormatException("Invalid file format or corrupted data", ex);
        }
    }

    /**
     * PoiWorkbookFactory constructor.
     * Initializes the FileType instances for Excel, XLSX, and XLS.
     *   It suppresses the warning for ignoring the result of the method call.
     *   Use this constructor to create an instance of PoiWorkbookFactory.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public PoiWorkbookFactory() {
        // force initialization of FileType instances
        FileTypeExcel.instance();
        FileTypeXlsx.instance();
        FileTypeXls.instance();
    }

    @Override
    public PoiWorkbook create() {
        return createXlsx();
    }

    @Override
    public PoiWorkbook createStreaming() {
        return createXlsxStreaming();
    }

    /**
     * Creates a new empty workbook in XLS format (Excel 97-2003).
     * This format has limitations on the number of rows and columns compared to XLSX.
     *
     * @return a new empty workbook in XLS format
     * @see #createXlsx()
     * @see #createXlsxStreaming()
     */
    public Workbook createXls() {
        return new PoiHssfWorkbook(new HSSFWorkbook(), null);
    }

    /**
     * Creates a new empty workbook in XLSX format (Excel 2007+).
     * This is the recommended format for new workbooks as it supports larger datasets
     * and more features than XLS format.
     *
     * @return a new empty workbook in XLSX format
     * @see #createXls()
     * @see #createXlsxStreaming()
     */
    public PoiWorkbook createXlsx() {
        return new PoiXssfWorkbook(new XSSFWorkbook(), null);
    }

    /**
     * Creates a new empty workbook in XLSX format optimized for streaming operations.
     * This mode is recommended for handling large datasets as it uses minimal memory,
     * but note that it's write-only and some features may be limited.
     *
     * @return a new empty workbook in XLSX format configured for streaming
     * @see #createXlsx()
     * @see #createXls()
     */
    public PoiWorkbook createXlsxStreaming() {
        return new PoiXssfWorkbook(new SXSSFWorkbook(), null);
    }

    /**
     * Opens an existing workbook from the specified URI with custom import settings.
     * The workbook format (XLS or XLSX) is automatically detected. This method provides
     * additional control over the import process through the importSettings parameter.
     *
     * @param uri the URI of the workbook to open
     * @param importSettings configuration options for the import process, such as
     *                      character encoding for CSV files or other format-specific settings
     * @return the opened workbook
     * @throws IOException if an I/O error occurs while reading the file
     * @throws FileFormatException if the file format is invalid or the file is corrupted
     * @see #open(URI)
     */
    @Override
    public PoiWorkbook open(URI uri, Arguments importSettings) throws IOException {
        // Read Excel files directly using POI methods
        // Do not use the `create(File)` method to avoid exception when trying
        // to save the workbook again to the same file.
        try (InputStream in = new BufferedInputStream(uri.toURL().openStream())) {
            return open(in, uri);
        }
    }

    /**
     * Opens an existing workbook from the specified URI. The workbook format (XLS or XLSX)
     * is automatically detected.
     *
     * @param uri URI of the workbook to open
     * @return the opened workbook
     * @throws IOException if an I/O error occurs while reading the file
     * @throws FileFormatException if the file format is invalid or the file is corrupted
     * @see #open(URI, Arguments)
     */
    @Override
    public PoiWorkbook open(URI uri) throws IOException {
        try (InputStream in = new BufferedInputStream(uri.toURL().openStream())) {
            return open(in, uri);
        }
    }

}
