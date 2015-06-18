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
package com.dua3.meja.model.poi;

import com.dua3.meja.io.FileFormatException;
import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author axel
 */
public final class PoiWorkbookFactory extends WorkbookFactory {

    private static final PoiWorkbookFactory INSTANCE = new PoiWorkbookFactory();

    public static PoiWorkbookFactory instance() {
        return INSTANCE;
    }

    private PoiWorkbookFactory() {
    }

    @Override
    public Workbook open(File file) throws IOException {
        Locale locale = Locale.getDefault();
        
        FileType type = FileType.forFile(file);
        
        if (type==FileType.XLS||type==FileType.XLSX) {
            // Read Excel files directly using POI methods
            // Do not use the create(File) method to avoid exception when trying to
            // save the workbook again to the same file.
            try (InputStream in = new FileInputStream(file)) {
                return open(in, Locale.getDefault(), file.toURI());
            }            
        } else if (type==null) {
            // if type could not be determined, try to open as CSV
            type = FileType.CSV;
        }
        
        if (!type.isSupported(OpenMode.READ)) {
            throw new IllegalArgumentException("Reading is not supported for files of type '"+type.getDescription()+"'.");
        }
        
        return type.getReader().read(PoiXssfWorkbook.class, locale, file);
    }

    public Workbook open(InputStream in, URI uri)
            throws IOException {
        return open(in, Locale.getDefault(), uri);
    }

    public Workbook open(InputStream in, Locale locale, URI uri)
            throws IOException {
        try {
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook = org.apache.poi.ss.usermodel.WorkbookFactory
                    .create(in);
            return createWorkbook(poiWorkbook, locale, uri);
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException|org.apache.poi.util.RecordFormatException ex) {
            throw new FileFormatException("Invalid file format or corrupted data", ex);
        }
    }

    public Workbook open(URI uri) throws IOException {
        return open(uri, Locale.getDefault());
    }

    public Workbook open(URI uri, Locale locale)
            throws IOException {
        return open(uri.toURL(), locale);
    }

    public Workbook open(URL url) throws IOException {
        return open(url, Locale.getDefault());
    }

    public Workbook open(URL url, Locale locale)
            throws IOException {
        try (InputStream in = new BufferedInputStream(url.openStream())) {
            URI uri;
            try {
                uri = url.toURI();
            } catch (URISyntaxException ex) {
                Logger.getLogger(PoiWorkbookFactory.class.getName()).log(Level.WARNING, "Could not get URI from URL.", ex);
                uri = null;
            }
            return open(in, locale, uri);
        }
    }

    @Override
    public Workbook create(Locale locale) {
        return createXlsx(locale);
    }

    @Override
    public Workbook createStreaming(Locale locale) {
        return createXlsxStreaming(locale);
    }

    @Override
    public Workbook create() {
        return create(Locale.getDefault());
    }

    @Override
    public Workbook createStreaming() {
        return createStreaming(Locale.getDefault());
    }

    public Workbook createXls() {
        return createXls(Locale.getDefault());
    }

    public Workbook createXls(Locale locale) {
        return new PoiHssfWorkbook(
                new org.apache.poi.hssf.usermodel.HSSFWorkbook(), locale, null);
    }

    public Workbook createXlsx() {
        return createXlsx(Locale.getDefault());
    }

    public Workbook createXlsx(Locale locale) {
        return new PoiXssfWorkbook(new XSSFWorkbook(), locale, null);
    }

    public Workbook createXlsxStreaming(Locale locale) {
        return new PoiXssfWorkbook(new SXSSFWorkbook(), locale, null);
    }

    private Workbook createWorkbook(
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook, Locale locale, URI uri) {
        if (poiWorkbook instanceof HSSFWorkbook) {
            return new PoiHssfWorkbook((HSSFWorkbook) poiWorkbook, locale, uri);
        } else {
            return new PoiXssfWorkbook(poiWorkbook, locale, uri);
        }
    }

}
