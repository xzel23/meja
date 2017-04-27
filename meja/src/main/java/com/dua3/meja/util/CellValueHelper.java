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
package com.dua3.meja.util;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Optional;

import com.dua3.meja.model.Cell;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CellValueHelper {

    private final NumberFormat numberFormat;
    private final DateTimeFormatter dateFormatter;

    /**
     * Construct an instance of {@code CellValueHelper}.
     *
     * @param numberFormat
     *            the number format to use
     * @param dateFormatter
     *            the date format to use
     */
    public CellValueHelper(NumberFormat numberFormat, DateTimeFormatter dateFormatter) {
        this.numberFormat = numberFormat;
        this.dateFormatter = dateFormatter;
    }

    /**
     * Set cell value from {@link String} with automatic conversion.
     *
     * @param cell
     *            the cell whose value is to be set
     * @param value
     *            the value to set
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
        Optional<Boolean> b = parseBoolean(value);
        if (b.isPresent()) {
            cell.set(b.get());
            return;
        }

        // number
        Optional<Number> number = parseNumber(value);
        if (number.isPresent()) {
            cell.set(number.get());
            return;
        }

        // date
        Optional<LocalDateTime> date = parseDate(value);
        if (date.isPresent()) {
            cell.set(date.get());
            return;
        }

        // text
        cell.set(value);
    }

    /**
     * Parse a boolean value.
     *
     * @param text
     *            string representation of a boolean value
     * @return {code Boolean.TRUE} or {Boolean.FALSE} respectively,
     *         if{@code text} is equal to either "true" or "false" (ignoring
     *         case). {@code null} otherwise.
     */
    protected Optional<Boolean> parseBoolean(String text) {
        text = text.trim();
        if (Boolean.FALSE.toString().equalsIgnoreCase(text)) {
            return Optional.of(Boolean.FALSE);
        }
        if (Boolean.TRUE.toString().equalsIgnoreCase(text)) {
            return Optional.of(Boolean.TRUE);
        }
        return Optional.empty();
    }

    /**
     * Parse a date value.
     *
     * @param text
     *            string representaion of a date
     * @return instance of {@link Date} representing {@code value} or
     *         {@code null}, if {@code value} could not be fully parsed
     */
    protected Optional<LocalDateTime> parseDate(String text) {
        text = text.trim();
        ParsePosition pos = new ParsePosition(0);

        // dry run first: try a complete parse first, but do not resolve fields
        // (to avoid exceoptions if this is not a date)
        TemporalAccessor ta = dateFormatter.parseUnresolved(text, pos);
        if (ta == null || pos.getErrorIndex() >= 0 || pos.getIndex() != text.length()) {
            // an error occurred or parsing did not use all available input
            return Optional.empty();
        }

        // everything ok? then get the real data
        ta = dateFormatter.parse(text);
        LocalDateTime dateTime = LocalDateTime.from(ta);
        return Optional.of(dateTime);
    }

    /**
     * Parse a numeric value.
     *
     * @param text
     *            string representaion of a numeric
     * @return instance of {@link Number} representing {@code value} or
     *         {@code null}, if {@code value} could not be fully parsed
     */
    protected Optional<Number> parseNumber(String text) {
        text = text.trim();
        ParsePosition pos = new ParsePosition(0);
        Number number = numberFormat.parse(text, pos);
        return Optional.ofNullable(pos.getIndex() == text.length() ? number : null);
    }

}
