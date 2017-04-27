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
package com.dua3.meja.model.generic;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.Options;

/**
 * Generic implementation of {@link Workbook}.
 */
public class GenericWorkbook
        implements Workbook {
    private static final URI DEFAULT_URI = URI.create("");

    final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    final List<GenericSheet> sheets = new ArrayList<>();
    final Map<String, GenericCellStyle> cellStyles = new HashMap<>();
    final Locale locale;
    private final GenericCellStyle defaultCellStyle;
    private URI uri;
    private int currentSheetIdx = 0;

    public GenericWorkbook(Locale locale) {
        this(locale, DEFAULT_URI);
    }

    /**
     * Construct a new {@code GenericWorkbook}.
     *
     * @param locale
     *            the locale to use
     * @param uri
     *            the URI to set
     */
    public GenericWorkbook(Locale locale, URI uri) {
        this.locale = locale;
        this.uri = uri;
        this.defaultCellStyle = new GenericCellStyle(this);
        this.cellStyles.put("", defaultCellStyle);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void close() throws IOException {
        // nop
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
        for (int sheetNr = 0; sheetNr < other.getSheetCount(); sheetNr++) {
            Sheet sheet = other.getSheet(sheetNr);
            Sheet newSheet = createSheet(sheet.getSheetName());
            newSheet.copy(sheet);
        }
    }

    @Override
    public GenericCellStyle copyCellStyle(String styleName, CellStyle style) {
        GenericCellStyle cellStyle = getCellStyle(styleName);
        cellStyle.copyStyle(style);
        return cellStyle;
    }

    @Override
    public GenericSheet createSheet(String sheetName) {
        final GenericSheet sheet = new GenericSheet(this, sheetName, locale);
        sheets.add(sheet);
        pcs.firePropertyChange(PROPERTY_SHEET_ADDED, null, sheets.size() - 1);
        return sheet;
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
    public List<String> getCellStyleNames() {
        return new ArrayList<>(cellStyles.keySet());
    }

    @Override
    public Sheet getCurrentSheet() {
        return getSheet(getCurrentSheetIndex());
    }

    @Override
    public int getCurrentSheetIndex() {
        return currentSheetIdx;
    }

    @Override
    public GenericCellStyle getDefaultCellStyle() {
        return defaultCellStyle;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public GenericSheet getSheet(int sheetNr) {
        return sheets.get(sheetNr);
    }

    @Override
    public int getSheetCount() {
        return sheets.size();
    }

    @Override
    public Optional<URI> getUri() {
        return Optional.ofNullable(uri);
    }

    @Override
    public boolean hasCellStyle(java.lang.String name) {
        return cellStyles.keySet().contains(name);
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
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removeSheet(int sheetNr) {
        sheets.remove(sheetNr);
        pcs.firePropertyChange(PROPERTY_SHEET_REMOVED, sheetNr, null);
    }

    @Override
    public void setCurrentSheet(int idx) {
        if (idx < 0 || idx > sheets.size()) {
            throw new IndexOutOfBoundsException("Sheet index out of range: " + idx);
        }

        int oldIdx = getCurrentSheetIndex();
        if (idx != oldIdx) {
            currentSheetIdx = idx;
            pcs.firePropertyChange(PROPERTY_ACTIVE_SHEET, oldIdx, idx);
        }
    }

    @Override
    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public boolean write(File file, boolean overwriteIfExists, Options options) throws IOException {
        boolean exists = file.createNewFile();
        if (!exists || overwriteIfExists) {
            FileType type = FileType.forFile(file);
            if (type == null) {
                throw new IllegalArgumentException("No matching FileType for file '" + file.getAbsolutePath() + ".");
            }
            try (OutputStream out = Files.newOutputStream(file.toPath())) {
                write(type, out, options);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void write(FileType type, OutputStream out, Options options) throws IOException {
        WorkbookWriter writer = type.getWriter();
        writer.setOptions(options);
        writer.write(this, out);
    }

    String getCellStyleName(GenericCellStyle cellStyle) {
        for (Map.Entry<String, GenericCellStyle> entry : cellStyles.entrySet()) {
            if (entry.getValue() == cellStyle) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("CellStyle is not from this workbook.");
    }

}
