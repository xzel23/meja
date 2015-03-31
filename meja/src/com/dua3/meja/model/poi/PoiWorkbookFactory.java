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

import com.dua3.meja.model.InvalidFileFormatException;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Workbook;

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
    public PoiWorkbook open(File file) throws IOException {
        try {
            Locale locale = Locale.getDefault();
            final Workbook poiWorkbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file);
            return createWorkbook(poiWorkbook, locale);
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException ex) {
            throw new InvalidFileFormatException(ex.getMessage());
        }
    }

    private PoiWorkbook createWorkbook(final Workbook poiWorkbook, Locale locale) {
        if (poiWorkbook instanceof org.apache.poi.hssf.usermodel.HSSFWorkbook) {
            return new PoiHssfWorkbook((org.apache.poi.hssf.usermodel.HSSFWorkbook) poiWorkbook, locale);
        } else if (poiWorkbook instanceof org.apache.poi.xssf.usermodel.XSSFWorkbook) {
            return new PoiXssfWorkbook((org.apache.poi.xssf.usermodel.XSSFWorkbook) poiWorkbook, locale);
        } else {
            throw new IllegalStateException();
        }
    }

}
