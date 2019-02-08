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
package com.dua3.meja.io;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.CellValueHelper;
import com.dua3.meja.util.Options;

/**
 * Implementation of the {@link RowBuilder} interface that appends new rows for an existing sheet.
 */
public class RowBuilder implements IRowBuilder {

    private final Sheet sheet;
    private final CellValueHelper helper;
    private Row currentRow;
    private int colNr;

    /**
     * Construct a new {@code RowBuilder}.
     *
     * @param sheet   the sheet to build rows for
     * @param options the locale to use
     */
    public RowBuilder(Sheet sheet, Options options) {
        this.sheet = sheet;

        Locale locale = Csv.getLocale(options);
        Csv.PredefinedDateFormat dateFormat = Csv.getDateFormat(options);

        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        DateTimeFormatter dateFormatter = dateFormat.getFormatter(locale);

        this.helper = new CellValueHelper(numberFormat, dateFormatter);
    }

    @Override
    public void add(String value) {
        Cell cell = currentRow.getCell(colNr++);
        helper.setCellValue(cell, value);
    }

    @Override
    public void endRow() {
        currentRow = null;
        colNr = 0;
    }

    @Override
    public void startRow() {
        currentRow = sheet.getRow(sheet.getRowCount());
        colNr = 0;
    }

}
