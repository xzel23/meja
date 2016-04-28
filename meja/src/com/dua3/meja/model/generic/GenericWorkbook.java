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
 * Generic implementation of {@link Workbook}.
 */
public class GenericWorkbook implements Workbook {

    final List<GenericSheet> sheets = new ArrayList<>();
    final Map<String, GenericCellStyle> cellStyles = new HashMap<>();
    final Locale locale;
    private final GenericCellStyle defaultCellStyle;
    private URI uri;
    
    private static final URI DEFAULT_URI = URI.create("");

    public GenericWorkbook(Locale locale) {
        this(locale, DEFAULT_URI);
    }
    
    /**
     * Construct a new {@code GenericWorkbook}.
     * @param locale the locale to use
     * @param uri the URI to set
     */
    public GenericWorkbook(Locale locale, URI uri) {
        this.locale = locale;
        this.uri = uri;
        this.defaultCellStyle = new GenericCellStyle(this);
        this.cellStyles.put("", defaultCellStyle);
    }

    @Override
    public Locale getLocale() {
        return locale;
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
        return null;
    }

    @Override
    public void write(FileType type, OutputStream out) throws IOException {
        type.getWriter().write(this, out);
    }

    @Override
    public boolean write(File file, boolean overwriteIfExists) throws IOException {
        boolean exists = file.createNewFile();
        if (!exists || overwriteIfExists) {
            FileType type = FileType.forFile(file);
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
        GenericCellStyle cellStyle = cellStyles.get(name);
        if (cellStyle == null) {
            cellStyle = new GenericCellStyle(this);
            cellStyles.put(name, cellStyle);
        }
        return cellStyle;
    }

    @Override
    public GenericCellStyle copyCellStyle(String styleName, CellStyle style) {
        GenericCellStyle cellStyle = getCellStyle(styleName);
        cellStyle.copyStyle(style);
        return cellStyle;
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

    @Override
    public List<String> getCellStyleNames() {
        return new ArrayList<>(cellStyles.keySet());
    }

    String getCellStyleName(GenericCellStyle cellStyle) {
        for (Map.Entry<String, GenericCellStyle> entry : cellStyles.entrySet()) {
            if (entry.getValue() == cellStyle) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("CellStyle is not from this workbook.");
    }

    @Override
    public void copy(Workbook other) {
        // copy styles
        for (String styleName : other.getCellStyleNames()) {
            CellStyle cellStyle = other.getCellStyle(styleName);
            CellStyle newCellStyle = getCellStyle(styleName);
            newCellStyle.copyStyle(cellStyle);
        }

        // copy sheets
        for (int sheetNr = 0; sheetNr < other.getNumberOfSheets(); sheetNr++) {
            Sheet sheet = other.getSheetByNr(sheetNr);
            Sheet newSheet = createSheet(sheet.getSheetName());
            newSheet.copy(sheet);
        }
    }

}
