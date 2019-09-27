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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import com.dua3.meja.io.FileFormatException;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.meja.model.poi.io.FileTypeExcel;
import com.dua3.meja.model.poi.io.FileTypeXls;
import com.dua3.meja.model.poi.io.FileTypeXlsx;
import com.dua3.utility.options.Option;
import com.dua3.utility.options.OptionSet;
import com.dua3.utility.options.OptionValues;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.RecordFormatException;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author axel
 */
public class PoiWorkbookFactory extends WorkbookFactory<PoiWorkbook> {

    private static final PoiWorkbookFactory INSTANCE = new PoiWorkbookFactory();

    private static final OptionSet OPTIONS = new OptionSet();

    public static final String OPTION_LOCALE = "Locale";

    private static PoiWorkbook createWorkbook(final org.apache.poi.ss.usermodel.Workbook poiWorkbook, URI uri) {
        if (poiWorkbook instanceof HSSFWorkbook) {
            return new PoiHssfWorkbook((HSSFWorkbook) poiWorkbook, uri);
        } else {
            return new PoiXssfWorkbook(poiWorkbook, uri);
        }
    }

    public static Optional<Option<?>> getOption(String name) {
        return OPTIONS.getOption(name);
    }

    /**
     * @return the singleton instance
     */
    public static PoiWorkbookFactory instance() {
        return INSTANCE;
    }

    /**
     * Open Workbook from InputStream.
     * @param in
     *  the stream to read from
     * @param uri
     *  the URI to set
     * @return the workbook
     * @throws IOException
     *  if an error occurs when reading
     */
    private static PoiWorkbook open(InputStream in, URI uri) throws IOException {
        try {
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook = org.apache.poi.ss.usermodel.WorkbookFactory
                    .create(in);
            return createWorkbook(poiWorkbook, uri);
        } catch (RecordFormatException ex) {
            throw new FileFormatException("Invalid file format or corrupted data", ex);
        }
    }

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
     * Create a new Xls-Workbook.
     *
     * @return the newly created Workbook
     */
    public Workbook createXls() {
        return new PoiHssfWorkbook(new HSSFWorkbook(), null);
    }

    /**
     * Create a new Xlsx-Workbook.
     *
     * @return the newly created Workbook
     */
    public PoiWorkbook createXlsx() {
        return new PoiXssfWorkbook(new XSSFWorkbook(), null);
    }

    /**
     * Create a new Xlsx-Workbook in streaming mode (write only).
     *
     * @return the newly created Workbook
     */
    public PoiWorkbook createXlsxStreaming() {
        return new PoiXssfWorkbook(new SXSSFWorkbook(), null);
    }

    @Override
    public PoiWorkbook open(URI uri, OptionValues importSettings) throws IOException {
        // Read Excel files directly using POI methods
        // Do not use the create(File) method to avoid exception when trying
        // to save the workbook again to the same file.
        try (InputStream in = new BufferedInputStream(uri.toURL().openStream())) {
            return open(in, uri);
        }
    }

    /**
     *
     * @param uri URI of the workbook to open
     * @return the workbook the workbook
     * @throws IOException if an error occurs
     */
    @Override
    public PoiWorkbook open(URI uri) throws IOException {
        try (InputStream in = new BufferedInputStream(uri.toURL().openStream())) {
            return open(in, uri);
        }
    }

}
