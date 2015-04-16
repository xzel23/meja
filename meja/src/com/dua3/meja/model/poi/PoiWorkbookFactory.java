/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import com.dua3.meja.io.FileFormatException;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import java.io.FileInputStream;

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
        // Do not use the create(File) method to avoid exception when trying to 
        // save the workbook again. 
        try (InputStream in = new FileInputStream(file)) {            
            return open(in, Locale.getDefault());
        }
    }

    public Workbook open(InputStream in)
            throws IOException {
        return open(in, Locale.getDefault());
    }

    public Workbook open(InputStream in, Locale locale)
            throws IOException {
        try {
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook = org.apache.poi.ss.usermodel.WorkbookFactory
                    .create(in);
            return createWorkbook(poiWorkbook, locale);
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException ex) {
            throw new FileFormatException(ex.getMessage());
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
            return open(in, locale);
        }
    }

    @Override
    public Workbook create() {
        return createXls(Locale.getDefault());
    }

    public Workbook createXls() {
        return createXls(Locale.getDefault());
    }

    public Workbook createXls(Locale locale) {
        return new PoiHssfWorkbook(
                new org.apache.poi.hssf.usermodel.HSSFWorkbook(), locale);
    }

    public Workbook createXlsx() {
        return createXlsx(Locale.getDefault());
    }

    public Workbook createXlsx(Locale locale) {
        return new PoiXssfWorkbook(
                new org.apache.poi.xssf.usermodel.XSSFWorkbook(), locale);
    }

    private Workbook createWorkbook(
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook, Locale locale) {
        if (poiWorkbook instanceof org.apache.poi.hssf.usermodel.HSSFWorkbook) {
            return new PoiHssfWorkbook(
                    (org.apache.poi.hssf.usermodel.HSSFWorkbook) poiWorkbook,
                    locale);
        } else if (poiWorkbook instanceof org.apache.poi.xssf.usermodel.XSSFWorkbook) {
            return new PoiXssfWorkbook(
                    (org.apache.poi.xssf.usermodel.XSSFWorkbook) poiWorkbook,
                    locale);
        } else {
            throw new IllegalStateException();
        }
    }

}
