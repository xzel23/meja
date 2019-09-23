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
package com.dua3.meja.model;

import java.io.IOException;
import java.net.URI;

import com.dua3.utility.options.OptionValues;

/**
 * Abstract base class for workbook factories.
 *
 * @param <WORKBOOK> the concrete workbook class
 *
 * @author axel
 */
public abstract class WorkbookFactory<WORKBOOK extends Workbook> {

    /**
     * Return copy of workbook in this factory`s implementation.
     *
     * @param other the source workbook
     * @return workbook instance of type {@code WORKBOOK} with the contents of
     *         {@code workbook}
     */
    public WORKBOOK copyOf(Workbook other) {
        WORKBOOK workbook = create();
        workbook.setUri(other.getUri().orElse(null));

        // copy styles
        for (String styleName : other.getCellStyleNames()) {
            CellStyle cellStyle = other.getCellStyle(styleName);
            CellStyle newCellStyle = workbook.getCellStyle(styleName);
            newCellStyle.copyStyle(cellStyle);
        }

        // copy sheets
        for (int sheetNr = 0; sheetNr < other.getSheetCount(); sheetNr++) {
            Sheet sheet = other.getSheet(sheetNr);
            Sheet newSheet = workbook.createSheet(sheet.getSheetName());
            newSheet.copy(sheet);
        }
        return workbook;
    }

    /**
     * Create a new Workbook instance.
     *
     * @return workbook
     */
    public abstract WORKBOOK create();

    /**
     * Create a new Workbook instance, use streaming if available.
     *
     * @return workbook
     */
    public abstract WORKBOOK createStreaming();

    /**
     * Load workbook from file.
     * <p>
     * The file type is determined automatically based on the extension so that it
     * is possible to open a CSV file as Excel workbook.
     * </p>
     *
     * @param uri the workbook URI
     * @return workbook
     * @throws IOException if an input/output error occurs
     */
    public WORKBOOK open(URI uri) throws IOException {
        return open(uri, OptionValues.empty());
    }

    /**
     * Load workbook from file.
     * <p>
     * The file type is determined automatically based on the extension so that it
     * is possible to open a CSV file as Excel workbook.
     * </p>
     *
     * @param uri           the workbook URI
     * @param importSettings settings to configure the input process
     * @return workbook
     * @throws IOException if an input/output error occurs
     */
    public abstract WORKBOOK open(URI uri, OptionValues importSettings) throws IOException;

}
