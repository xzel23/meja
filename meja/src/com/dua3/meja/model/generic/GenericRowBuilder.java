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

import com.dua3.meja.io.DataException;
import com.dua3.meja.io.RowBuilder;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericRowBuilder implements RowBuilder {

    private final Sheet sheet;
    private Row currentRow;
    private int colNr;

    private final DecimalFormat decimalFormat;
    private final DateFormat dateFormat;
    private final String booleanTrue;
    private final String booleanFalse;

    public GenericRowBuilder(Sheet sheet, Locale locale) {
        this.sheet = sheet;
        this.decimalFormat = new DecimalFormat("#", new DecimalFormatSymbols(locale));
        this.booleanTrue = Boolean.toString(true);
        this.booleanFalse = Boolean.toString(false);
        this.dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
    }

    @Override
    public void startRow() {
        currentRow = sheet.getRow(sheet.getNumberOfRows());
        colNr=0;
    }

    @Override
    public void add(String value) throws DataException {
        Cell cell = currentRow.getCell(colNr++);
        if (value.isEmpty()) {
            // blank
            cell.clear();
        } else if (value.startsWith("=")) {
            // formula
            cell.setFormula(value);
        } else if (value.equalsIgnoreCase(booleanTrue)) {
            // boolean true
            cell.set(true);
        } else if (value.equalsIgnoreCase(booleanFalse)) {
            // boolean false
            cell.set(false);
        } else {
            // number
            Number number = parseNumber(value);
            if (number != null) {
                cell.set(number);
            } else {
                // date
                Date date = parseDate(value);
                if (date != null) {
                    cell.set(date);
                } else {
                    // text
                    cell.set(value);
                }
            }
        }
    }

    private Number parseNumber(String text) {
        ParsePosition pos = new ParsePosition(0);
        Number number = decimalFormat.parse(text, pos);
        return pos.getIndex() == text.length() ? number : null;
    }

    private Date parseDate(String text) {
        ParsePosition pos = new ParsePosition(0);
        Date date = dateFormat.parse(text, pos);
        return pos.getIndex() == text.length() ? date : null;
    }

    @Override
    public void endRow() {
        currentRow = null;
        colNr = 0;
    }

}
