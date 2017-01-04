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
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CellValueHelper {

    private final NumberFormat numberFormat;
    private final DateTimeFormatter dateFormatter;

    /**
     * Construct an instance of {@code CellValueHelper} for a specific locale.
     * @param locale the locale to use
     */
    public CellValueHelper(Locale locale) {
        this(NumberFormat.getInstance(locale), DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale));
    }

    /**
     * Construct an instance of {@code CellValueHelper} with specific data formats.
     * @param numberFormat the {@link NumberFormat} to use
     * @param dateFormatter the {@code DateFormat} to use
     */
    public CellValueHelper(NumberFormat numberFormat, DateTimeFormatter dateFormatter) {
        this.numberFormat = numberFormat;
        this.dateFormatter = dateFormatter;
    }

    /**
     * Set cell value from {@link String} with automatic conversion.
     * @param cell the cell whose value is to be set
     * @param value the value to set
     */
    public void setCellValue(Cell cell, String value) {
        // blank
        if (value.isEmpty()) {
            cell.clear();
            return;
        }

        // formula
        if (value.startsWith("=")) {
            cell.setFormula(value.substring(1));
            return;
        }

        // boolean
        Boolean b = parseBoolean(value);
        if (b != null) {
            cell.set(b);
            return;
        }

        // number
        Number number = parseNumber(value);
        if (number != null) {
            cell.set(number);
            return;
        }

        // date
        LocalDateTime date = parseDate(value);
        if (date != null) {
            cell.set(date);
            return;
        }

        // text
        cell.set(value);
    }

    /**
     * Parse a boolean value.
     * @param value string representation of a boolean value
     * @return {code Boolean.TRUE} or {Boolean.FALSE} respectively,
     * if{@code value} is equal to either "true" or "false" (ignoring case).
     * {@code null} otherwise.
     */
    protected Boolean parseBoolean(String value) {
        if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Parse a numeric value.
     * @param text string representaion of a numeric
     * @return instance of {@link Number} representing {@code value} or
     * {@code null}, if {@code value} could not be fully parsed
     */
    protected Number parseNumber(String text) {
        ParsePosition pos = new ParsePosition(0);
        Number number = numberFormat.parse(text, pos);
        return pos.getIndex() == text.length() ? number : null;
    }

    /**
     * Parse a date value.
     * @param text string representaion of a date
     * @return instance of {@link Date} representing {@code value} or
     * {@code null}, if {@code value} could not be fully parsed
     */
    protected LocalDateTime parseDate(String text) {
        ParsePosition pos = new ParsePosition(0);
        TemporalAccessor ta = dateFormatter.parseUnresolved(text, pos);        
        return pos.getIndex() == text.length() ? LocalDateTime.from(ta) : null;
    }

}
