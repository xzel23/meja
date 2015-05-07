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

import com.dua3.meja.model.Font;
import java.awt.Color;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class GenericFont implements Font {

    private final Color color;
    private final Float size;
    private final String family;
    private final Boolean bold;
    private final Boolean italic;
    private final Boolean underline;
    private final Boolean strikeThrough;

    public GenericFont() {
        this.color = Color.BLACK;
        this.size = 10f;
        this.family = "Helvetica";
        this.bold = false;
        this.italic = false;
        this.underline = false;
        this.strikeThrough = false;
    }

    public GenericFont(Font other) {
        this.color = other.getColor();
        this.size = other.getSizeInPoints();
        this.family = other.getFamily();
        this.bold = other.isBold();
        this.italic = other.isItalic();
        this.underline = other.isUnderlined();
        this.strikeThrough = other.isStrikeThrough();
    }

    public GenericFont(String family, float size, Color color, boolean bold, boolean italic, boolean underlined, boolean strikeThrough) {
        this.color = color;
        this.size = size;
        this.family = family;
        this.bold = bold;
        this.italic = italic;
        this.underline = underlined;
        this.strikeThrough = strikeThrough;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public float getSizeInPoints() {
        return size;
    }

    @Override
    public String getFamily() {
        return family;
    }

    @Override
    public boolean isBold() {
        return bold;
    }

    @Override
    public boolean isItalic() {
        return italic;
    }

    @Override
    public boolean isUnderlined() {
        return underline;
    }

    @Override
    public boolean isStrikeThrough() {
        return strikeThrough;
    }

    @Override
    public GenericFont deriveFont(FontDef fd) {        
        String fontFamily = fd.getFamily() != null ?fd.getFamily(): this.getFamily();
        float fontSize = fd.getSize()!=null?fd.getSize():this.getSizeInPoints();
        Color fontColor = fd.getColor()!=null?fd.getColor():this.getColor(); 
        boolean fontBold = fd.getBold()!=null?fd.getBold():this.isBold();
        boolean fontItalic = fd.getItalic()!=null?fd.getItalic():this.isItalic(); 
        boolean fontUnderlined = fd.getUnderline()!=null?fd.getUnderline():this.isUnderlined(); 
        boolean fontStrikeThrough = fd.getStrikeThrough()!=null?fd.getStrikeThrough():this.isStrikeThrough();
        
        return new GenericFont(fontFamily, fontSize, fontColor, fontBold, fontItalic, fontUnderlined, fontStrikeThrough);
    }
    
}
