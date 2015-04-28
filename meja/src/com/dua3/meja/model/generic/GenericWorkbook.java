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

import com.dua3.meja.io.FileType;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericWorkbook implements Workbook {

    final List<GenericSheet> sheets = new ArrayList<>();
    final Map<String, GenericCellStyle> cellStyles = new HashMap<>();
    final Locale locale;
    private GenericCellStyle defaultCellStyle = new GenericCellStyle();
    private URI uri;

    public GenericWorkbook(Locale locale, URI uri) {
        this.locale = locale;
        this.uri = uri;
    }

    @Override
    public int getNumberOfSheets() {
        return sheets.size();
    }

    @Override
    public GenericSheet getSheetByNr(int sheetNr) {
        return sheets.get(sheetNr);
    }

    @Override
    public GenericSheet getSheetByName(String sheetName) {
        for (GenericSheet sheet : sheets) {
            if (sheet.getSheetName().equals(sheetName)) {
                return sheet;
            }
        }
        throw new IllegalArgumentException("No sheet '" + sheetName + "'.");
    }

    @Override
    public void write(FileType type, OutputStream out) throws IOException {
        type.getWriter().write(this, out);
    }

    @Override
    public boolean write(File file, boolean overwriteIfExists) throws IOException {
        boolean exists = file.createNewFile();
        if (!exists || overwriteIfExists) {
            FileType type = FileType.getForFile(file);
            if (type == null) {
                throw new IllegalArgumentException("No matching FileType for file '" + file.getAbsolutePath() + ".");
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                write(type, out);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public GenericSheet createSheet(String sheetName) {
        final GenericSheet sheet = new GenericSheet(this, sheetName, locale);
        sheets.add(sheet);
        return sheet;
    }

    @Override
    public GenericCellStyle getDefaultCellStyle() {
        return defaultCellStyle;
    }

    @Override
    public GenericCellStyle getCellStyle(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GenericCellStyle copyCellStyle(String styleName, CellStyle style) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws IOException {
        // nop
    }

    @Override
    public NumberFormat getNumberFormat() {
        return NumberFormat.getInstance(locale);
    }

    @Override
    public DateFormat getDateFormat() {
        return DateFormat.getDateInstance(DateFormat.SHORT, locale);
    }

    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<Sheet>() {
            Iterator<GenericSheet> iter = sheets.iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Sheet next() {
                return iter.next();
            }

            @Override
            public void remove() {
                iter.remove();
            }
        };
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public void setUri(URI uri) {
        this.uri = uri;
    }

}
