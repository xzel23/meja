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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.dua3.meja.io.FileFormatException;
import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import com.dua3.meja.util.Option;
import com.dua3.meja.util.OptionSet;
import com.dua3.meja.util.Options;

/**
 *
 * @author axel
 */
public class PoiWorkbookFactory extends WorkbookFactory<PoiWorkbook> {

    private static final PoiWorkbookFactory INSTANCE = new PoiWorkbookFactory();

    private static final OptionSet OPTIONS = new OptionSet();

    public static final String OPTION_LOCALE = "Locale";

    static {
        Locale[] locales = Locale.getAvailableLocales();
        Arrays.sort(locales, (a, b) -> a.toString().compareTo(b.toString()));
        OPTIONS.addOption(OPTION_LOCALE, Locale.class, OptionSet.value("default", Locale.ROOT),
                OptionSet.wrap(locales));
    }

    private static PoiWorkbook createWorkbook(
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook, Locale locale, Path path) {
        if (poiWorkbook instanceof HSSFWorkbook) {
            return new PoiHssfWorkbook((HSSFWorkbook) poiWorkbook, locale, path);
        } else {
            return new PoiXssfWorkbook(poiWorkbook, locale, path);
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
     *
     * @param in
     * @param locale
     * @param path
     * @return the workbook
     * @throws IOException
     */
    private static PoiWorkbook open(InputStream in, Locale locale, Path path)
            throws IOException {
        try {
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook = org.apache.poi.ss.usermodel.WorkbookFactory
                    .create(in);
            return createWorkbook(poiWorkbook, locale, path);
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException
                | org.apache.poi.util.RecordFormatException ex) {
            throw new FileFormatException("Invalid file format or corrupted data", ex);
        }
    }

    private PoiWorkbookFactory() {
    }

    @Override
    public PoiWorkbook create() {
        return create(Locale.getDefault());
    }

    @Override
    public PoiWorkbook create(Locale locale) {
        return createXlsx(locale);
    }

    @Override
    public PoiWorkbook createStreaming() {
        return createStreaming(Locale.getDefault());
    }

    @Override
    public PoiWorkbook createStreaming(Locale locale) {
        return createXlsxStreaming(locale);
    }

    /**
     * Create a new Xls-Workbook.
     *
     * @return the newly created Workbook
     */
    public Workbook createXls() {
        return createXls(Locale.getDefault());
    }

    /**
     * Create a new Xls-Workbook with the given Locale settings.
     *
     * @param locale
     *            the locale to use
     * @return the newly created Workbook
     */
    public Workbook createXls(Locale locale) {
        return new PoiHssfWorkbook(
                new org.apache.poi.hssf.usermodel.HSSFWorkbook(), locale, null);
    }

    /**
     * Create a new Xlsx-Workbook.
     *
     * @return the newly created Workbook
     */
    public PoiWorkbook createXlsx() {
        return createXlsx(Locale.getDefault());
    }

    /**
     * Create a new Xlsx-Workbook in streaming mode (write only) with the given
     * Locale settings.
     *
     * @param locale
     *            the locale to use
     * @return the newly created Workbook
     */
    public PoiWorkbook createXlsx(Locale locale) {
        return new PoiXssfWorkbook(new XSSFWorkbook(), locale, null);
    }

    /**
     * Create a new Xlsx-Workbook in streaming mode (write only).
     *
     * @param locale
     *            the {@link Locale} to use
     * @return the newly created Workbook
     */
    public PoiWorkbook createXlsxStreaming(Locale locale) {
        return new PoiXssfWorkbook(new SXSSFWorkbook(), locale, null);
    }

    @Override
    public PoiWorkbook open(Path path, Options importSettings) throws IOException {
        FileType type = FileType.forPath(path).orElse(FileType.CSV);

        if (type == FileType.XLS || type == FileType.XLSX) {
            // Read Excel files directly using POI methods
            // Do not use the create(File) method to avoid exception when trying
            // to
            // save the workbook again to the same file.
            try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
                Locale locale = (Locale) importSettings.get(OPTIONS.getOption(OPTION_LOCALE).get()).getValue();
                return open(in, locale, path);
            }
        }

        if (!type.isSupported(OpenMode.READ)) {
            throw new IllegalArgumentException(
                    "Reading is not supported for files of type '" + type.getDescription() + "'.");
        }

        return type.getReader().read(PoiWorkbookFactory.instance(), path);
    }

    /**
     *
     * @param path
     *            Path of the workbook to open
     * @return the workbook the workbook
     * @throws IOException
     *             if an error occurs
     */
    @Override
    public PoiWorkbook open(Path path) throws IOException {
        return open(path, Locale.getDefault());
    }

    /**
     * Open workbook from URL.
     *
     * @param path
     *            Path of the workbook to open
     * @param locale
     *            the locale to use
     * @return the workbook the workbook
     * @throws IOException
     *             if an error occurs
     */
    public PoiWorkbook open(Path path, Locale locale)
            throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            return open(in, locale, path);
        }
    }

}
