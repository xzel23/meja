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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger LOGGER = Logger.getLogger(PoiWorkbookFactory.class.getName());

    private static final OptionSet OPTIONS = new OptionSet();

    public static final String OPTION_LOCALE = "Locale";

    static {
        Locale[] locales = Locale.getAvailableLocales();
        Arrays.sort(locales, (a, b) -> a.toString().compareTo(b.toString()));
        OPTIONS.addOption(OPTION_LOCALE, Locale.class, OptionSet.value("default", Locale.ROOT),
                OptionSet.wrap(locales));
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

    private static PoiWorkbook createWorkbook(
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook, Locale locale, URI uri) {
        if (poiWorkbook instanceof HSSFWorkbook) {
            return new PoiHssfWorkbook((HSSFWorkbook) poiWorkbook, locale, uri);
        } else {
            return new PoiXssfWorkbook(poiWorkbook, locale, uri);
        }
    }

    /**
     *
     * @param in
     * @param locale
     * @param uri
     * @return the workbook
     * @throws IOException
     */
    private static PoiWorkbook open(InputStream in, Locale locale, URI uri)
            throws IOException {
        try {
            final org.apache.poi.ss.usermodel.Workbook poiWorkbook = org.apache.poi.ss.usermodel.WorkbookFactory
                    .create(in);
            return createWorkbook(poiWorkbook, locale, uri);
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
    public PoiWorkbook open(File file, Options importSettings) throws IOException {
        FileType type = FileType.forFile(file);

        if (type == FileType.XLS || type == FileType.XLSX) {
            // Read Excel files directly using POI methods
            // Do not use the create(File) method to avoid exception when trying
            // to
            // save the workbook again to the same file.
            try (InputStream in = Files.newInputStream(file.toPath())) {
                Locale locale = (Locale) importSettings.get(OPTIONS.getOption(OPTION_LOCALE).get()).getValue();
                return open(in, locale, file.toURI());
            }
        } else if (type == null) {
            // if type could not be determined, try to open as CSV
            type = FileType.CSV;
        }

        if (!type.isSupported(OpenMode.READ)) {
            throw new IllegalArgumentException(
                    "Reading is not supported for files of type '" + type.getDescription() + "'.");
        }

        return type.getReader().read(PoiXssfWorkbook.class, file);
    }

    /**
     *
     * @param uri
     *            URI of the workbook to open
     * @return the workbook the workbook
     * @throws IOException
     *             if an error occurs
     */
    public Workbook open(URI uri) throws IOException {
        return open(uri, Locale.getDefault());
    }

    /**
     *
     * @param uri
     *            URI of the workbook to open
     * @param locale
     *            locale to use
     * @return the workbook the workbook
     * @throws IOException
     *             if an error occurs
     */
    public Workbook open(URI uri, Locale locale)
            throws IOException {
        return open(uri.toURL(), locale);
    }

    /**
     *
     * @param url
     *            URL of the workbook to open
     * @return the workbook the workbook
     * @throws IOException
     *             if an error occurs
     */
    public Workbook open(URL url) throws IOException {
        return open(url, Locale.getDefault());
    }

    /**
     * Open workbook from URL.
     *
     * @param url
     *            URL of the workbook to open
     * @param locale
     *            the locale to use
     * @return the workbook the workbook
     * @throws IOException
     *             if an error occurs
     */
    public Workbook open(URL url, Locale locale)
            throws IOException {
        try (InputStream in = new BufferedInputStream(url.openStream())) {
            URI uri;
            try {
                uri = url.toURI();
            } catch (URISyntaxException ex) {
                LOGGER.log(Level.WARNING, "Could not get URI from URL.", ex);
                uri = null;
            }
            return open(in, locale, uri);
        }
    }

}
