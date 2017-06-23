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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Color;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Font;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericCellStyle
        implements CellStyle {

    private static final BorderStyle defaultBorderStyle = new BorderStyle(0.0f, Color.BLACK);

    private static final Logger LOGGER = Logger.getLogger(GenericCellStyle.class.getName());

    private final GenericWorkbook workbook;

    private GenericFont font;
    private Color fillBgColor;
    private Color fillFgColor;
    private FillPattern fillPattern;
    private HAlign hAlign;
    private VAlign vAlign;
    private final BorderStyle[] borderStyle = new BorderStyle[Direction.values().length];
    private boolean wrap;
    private String dataFormat = "";

    // formatting helper
    private DateTimeFormatter dateFormatter = null;
    private NumberFormat numberFormatter = null;

    /**
     * Construct a new {@code GenericCellStyle}.
     *
     * @param workbook
     *            the workbook the style is defined in
     */
    public GenericCellStyle(GenericWorkbook workbook) {
        this.workbook = workbook;
        this.font = new GenericFont();
        this.fillPattern = FillPattern.NONE;
        this.fillBgColor = Color.WHITE;
        this.fillFgColor = Color.WHITE;
        this.hAlign = HAlign.ALIGN_AUTOMATIC;
        this.vAlign = VAlign.ALIGN_MIDDLE;
        this.wrap = false;
        this.dataFormat = "";

        for (Direction d : Direction.values()) {
            borderStyle[d.ordinal()] = defaultBorderStyle;
        }
    }

    @Override
    public void copyStyle(CellStyle other) {
        setHAlign(other.getHAlign());
        setVAlign(other.getVAlign());
        for (Direction d : Direction.values()) {
            setBorderStyle(d, other.getBorderStyle(d));
        }
        setDataFormat(other.getDataFormat());
        setFillBgColor(other.getFillBgColor());
        setFillFgColor(other.getFillFgColor());
        setFillPattern(other.getFillPattern());
        setFont(other.getFont());
        setWrap(other.isWrap());
    }

    /**
     * Format date for output.
     *
     * @param date
     *            the date to format
     * @return text representation of {@code date}
     */
    public String format(LocalDateTime date, Locale locale) {
        if (dateFormatter == null) {
            try {
                if (dataFormat == null || dataFormat.isEmpty()) {
                    dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            .withLocale(locale);
                } else {
                    dateFormatter = DateTimeFormatter.ofPattern(dataFormat, locale);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Not a date pattern: ''{0}''", dataFormat);
                dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
            }
        }

        return dateFormatter.format(date);
    }

    /**
     * Format number for output.
     *
     * @param n
     *            the number to format
     * @return text representation of {@code n}
     */
    public String format(Number n, Locale locale) {
        if (numberFormatter == null) {
            try {
                String fmt = dataFormat == null || dataFormat.isEmpty() ? "0.##########" : dataFormat;
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
                numberFormatter = new DecimalFormat(fmt, symbols);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Not a number pattern: ''{0}''", dataFormat);
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
    public GenericFont getFont() {
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
        this.font = GenericFont.copyOf(font);
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

}
