/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Font;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import java.awt.Color;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericCellStyle implements CellStyle {

    private static final BorderStyle defaultBorderStyle = new BorderStyle(0.0f, Color.BLACK);

    private Font font;
    private Color fillBgColor;
    private Color fillFgColor;
    private FillPattern fillPattern;
    private HAlign hAlign;
    private VAlign vAlign;
    private final BorderStyle[] borderStyle = new BorderStyle[Direction.values().length];
    private boolean wrap;

    public GenericCellStyle() {
        this.font = new GenericFont();
        this.fillPattern = FillPattern.NONE;
        this.fillBgColor = Color.WHITE;
        this.fillFgColor = Color.WHITE;
        this.hAlign = HAlign.ALIGN_AUTOMATIC;
        this.vAlign = VAlign.ALIGN_MIDDLE;
        this.wrap = false;

        for (Direction d : Direction.values()) {
            borderStyle[d.ordinal()] = defaultBorderStyle;
        }
    }

    @Override
    public Font getFont() {
        return font;
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
    public HAlign getHAlign() {
        return hAlign;
    }

    @Override
    public VAlign getVAlign() {
        return vAlign;
    }

    @Override
    public BorderStyle getBorderStyle(Direction d) {
        return borderStyle[d.ordinal()];
    }

    @Override
    public boolean isWrap() {
        return wrap;
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
    public void setBorderStyle(Direction d, BorderStyle borderStyle) {
        this.borderStyle[d.ordinal()] = borderStyle;
    }

}
