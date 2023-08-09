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

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericCellStyle implements CellStyle {

    private static final BorderStyle defaultBorderStyle = new BorderStyle(0.0f, Color.BLACK);

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericCellStyle.class);

    private final GenericWorkbook workbook;

    private Font font;
    private Color fillBgColor;
    private Color fillFgColor;
    private FillPattern fillPattern;
    private HAlign hAlign;
    private VAlign vAlign;
    private final BorderStyle[] borderStyle = new BorderStyle[Direction.values().length];
    private boolean wrap;
    private String dataFormat;
    private short rotation;

    // formatting helper
    private DateTimeFormatter dateFormatter;
    private NumberFormat numberFormatter;

    /**
     * Construct a new {@code GenericCellStyle}.
     *
     * @param workbook the workbook the style is defined in
     */
    public GenericCellStyle(GenericWorkbook workbook) {
        this.workbook = workbook;
        this.font = new Font();
        this.fillPattern = FillPattern.NONE;
        this.fillBgColor = Color.WHITE;
        this.fillFgColor = Color.WHITE;
        this.hAlign = HAlign.ALIGN_AUTOMATIC;
        this.vAlign = VAlign.ALIGN_MIDDLE;
        this.wrap = false;
        this.dataFormat = "";
        this.rotation = 0;

        for (Direction d : Direction.values()) {
            borderStyle[d.ordinal()] = defaultBorderStyle;
        }
    }

    /**
     * Format datetime for output.
     *
     * @param locale the locale to use during formatting
     * @param arg    the datetime to format
     * @return text representation of {@code date}
     */
    public String format(LocalDateTime arg, Locale locale) {
        if (dateFormatter == null) {
            try {
                if (dataFormat == null || dataFormat.isEmpty()) {
                    dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale);
                } else {
                    dateFormatter = DateTimeFormatter.ofPattern(dataFormat, locale);
                }
            } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
                LOGGER.warn("not a date pattern: '{}'", dataFormat);
                dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale);
            }
        }

        return dateFormatter.format(arg);
    }

    /**
     * Format date for output.
     *
     * @param locale the locale to use during formatting
     * @param arg    the date to format
     * @return text representation of {@code date}
     */
    public String format(TemporalAccessor arg, Locale locale) {
        if (dateFormatter == null) {
            try {
                if (dataFormat == null || dataFormat.isEmpty()) {
                    dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
                } else {
                    dateFormatter = DateTimeFormatter.ofPattern(dataFormat, locale);
                }
            } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
                LOGGER.warn("not a date pattern: '{}'", dataFormat);
                dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
            }
        }

        return dateFormatter.format(arg);
    }

    /**
     * Format number for output.
     *
     * @param n      the number to format
     * @param locale the locale to use during formatting
     * @return text representation of {@code n}
     */
    public String format(Number n, Locale locale) {
        if (numberFormatter == null) {
            try {
                String fmt = dataFormat == null || dataFormat.isEmpty() ? "0.##########" : dataFormat;
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
                numberFormatter = new DecimalFormat(fmt, symbols);
            } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
                LOGGER.warn("not a number pattern: '{}'", dataFormat);
                numberFormatter = NumberFormat.getInstance(locale);
                numberFormatter.setGroupingUsed(false);
            }
        }

        return numberFormatter.format(n);
    }

    @Override
    public BorderStyle getBorderStyle(Direction d) {
        return borderStyle[d.ordinal()];
    }

    @Override
    public String getDataFormat() {
        return dataFormat;
    }

    @Override
    public Color getFillBgColor() {
        return fillBgColor;
    }

    @Override
    public Color getFillFgColor() {
        return fillFgColor;
    }

    @Override
    public FillPattern getFillPattern() {
        return fillPattern;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public HAlign getHAlign() {
        return hAlign;
    }

    @Override
    public String getName() {
        return workbook.getCellStyleName(this);
    }

    @Override
    public VAlign getVAlign() {
        return vAlign;
    }

    @Override
    public GenericWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public boolean isWrap() {
        return wrap;
    }

    @Override
    public void setBorderStyle(Direction d, BorderStyle borderStyle) {
        this.borderStyle[d.ordinal()] = borderStyle;
    }

    @Override
    public void setDataFormat(String format) {
        this.dataFormat = format;
        this.dateFormatter = null;
        this.numberFormatter = null;
    }

    @Override
    public void setFillBgColor(Color fillBgColor) {
        this.fillBgColor = fillBgColor;
    }

    @Override
    public void setFillFgColor(Color fillFgColor) {
        this.fillFgColor = fillFgColor;
    }

    @Override
    public void setFillPattern(FillPattern fillPattern) {
        this.fillPattern = fillPattern;
    }

    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public void setHAlign(HAlign hAlign) {
        this.hAlign = hAlign;
    }

    @Override
    public void setVAlign(VAlign vAlign) {
        this.vAlign = vAlign;
    }

    @Override
    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    @Override
    public void setRotation(short angle) {
        LangUtil.check(angle >= -90 && angle <= 90, "angle must be in range [-90, 90]: %d", angle);
        this.rotation = angle;
    }

    @Override
    public short getRotation() {
        return rotation;
    }
}
