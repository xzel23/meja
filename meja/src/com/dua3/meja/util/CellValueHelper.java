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
package com.dua3.meja.util;

import com.dua3.meja.model.Cell;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CellValueHelper {

    private final NumberFormat numberFormat;
    private final DateFormat dateFormat;

    public CellValueHelper(Locale locale) {
        this(NumberFormat.getInstance(locale), DateFormat.getDateInstance(DateFormat.SHORT, locale));
    }

    public CellValueHelper(NumberFormat numberFormat, DateFormat dateFormat) {
        this.numberFormat = numberFormat;
        this.dateFormat = dateFormat;
    }

    public boolean setCellValue(Cell cell, String value) {
        // blank
        if (value.isEmpty()) {
            cell.clear();
            return true;
        }

        // formula
        if (value.startsWith("=")) {
            cell.setFormula(value);
            return true;
        }

        // boolean
        Boolean b = parseBoolean(value);
        if (b != null) {
            cell.set(b);
            return true;
        }

        // number
        Number number = parseNumber(value);
        if (number != null) {
            cell.set(number);
            return true;
        }

        // date
        Date date = parseDate(value);
        if (date != null) {
            cell.set(date);
            return true;
        }

        // text
        cell.set(value);
        return true;
    }

    protected Boolean parseBoolean(String value) {
        if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        return null;
    }

    protected Number parseNumber(String text) {
        ParsePosition pos = new ParsePosition(0);
        Number number = numberFormat.parse(text, pos);
        return pos.getIndex() == text.length() ? number : null;
    }

    protected Date parseDate(String text) {
        ParsePosition pos = new ParsePosition(0);
        Date date = dateFormat.parse(text, pos);
        return pos.getIndex() == text.length() ? date : null;
    }

}
