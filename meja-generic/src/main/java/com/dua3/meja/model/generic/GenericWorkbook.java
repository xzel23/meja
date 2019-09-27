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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.DoubleConsumer;

import com.dua3.meja.io.FileTypeWorkbook;
import com.dua3.meja.io.WorkbookWriter;
import com.dua3.meja.model.AbstractWorkbook;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.utility.io.FileType;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.OptionValues;

/**
 * Generic implementation of {@link Workbook}.
 */
public class GenericWorkbook extends AbstractWorkbook {

    private final List<GenericSheet> sheets = new ArrayList<>();
    private final Map<String, GenericCellStyle> cellStyles = new HashMap<>();
    private final GenericCellStyle defaultCellStyle;
    private int currentSheetIdx = 0;

    /**
     * Construct a new {@code GenericWorkbook}.
     *
     * @param uri the URI to set
     */
    public GenericWorkbook(URI uri) {
        super(uri);
        this.defaultCellStyle = new GenericCellStyle(this);
        this.cellStyles.put("", defaultCellStyle);
    }

    @Override
    public void close() {
        // nop
    }

    @Override
    public GenericCellStyle copyCellStyle(String styleName, CellStyle style) {
        GenericCellStyle cellStyle = getCellStyle(styleName);
        cellStyle.copyStyle(style);
        return cellStyle;
    }

    @Override
    public GenericSheet createSheet(String sheetName) {
        final GenericSheet sheet = new GenericSheet(this, sheetName);
        sheets.add(sheet);
        firePropertyChange(PROPERTY_SHEET_ADDED, null, sheets.size() - 1);
        return sheet;
    }

    @Override
    public GenericCellStyle getCellStyle(String name) {
        return cellStyles.computeIfAbsent(name, n -> new GenericCellStyle(this));
    }

    String getCellStyleName(GenericCellStyle cellStyle) {
        for (Entry<String, GenericCellStyle> entry : cellStyles.entrySet()) {
            if (entry.getValue() == cellStyle) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("CellStyle is not from this workbook.");
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
    public GenericSheet getSheet(int sheetNr) {
        return sheets.get(sheetNr);
    }

    @Override
    public int getSheetCount() {
        return sheets.size();
    }

    @Override
    public boolean hasCellStyle(String name) {
        return cellStyles.containsKey(name);
    }

    @Override
    public Iterator<Sheet> iterator() {
        return new Iterator<>() {
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
    public void removeSheet(int sheetNr) {
        sheets.remove(sheetNr);
        firePropertyChange(PROPERTY_SHEET_REMOVED, sheetNr, null);
    }

    @Override
    public void setCurrentSheet(int idx) {
        LangUtil.checkIndex(idx, sheets.size());

        int oldIdx = getCurrentSheetIndex();
        if (idx != oldIdx) {
            currentSheetIdx = idx;
            firePropertyChange(PROPERTY_ACTIVE_SHEET, oldIdx, idx);
        }
    }

    @Override
    public void write(FileType type, OutputStream out, OptionValues options, DoubleConsumer updateProgress) throws IOException {
        WorkbookWriter writer = ((FileTypeWorkbook) type).getWorkbookWriter();
        writer.setOptions(options);
        writer.write(this, out, updateProgress);
    }
}