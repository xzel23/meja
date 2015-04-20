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
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Font;
import com.dua3.meja.util.AttributedStringHelper;
import com.dua3.meja.util.Cache;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CellEditorPane extends JEditorPane {
    private static final long serialVersionUID = 1L;

    static class ScaledFont {
        final Font font;
        final float scale;

        public ScaledFont(Font font, float scale) {
            this.font = font;
            this.scale = scale;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ScaledFont) {
                ScaledFont other = (ScaledFont) obj;
                return scale==other.scale && font.equals(other.font);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Float.floatToRawIntBits(scale)^font.hashCode();
        }
    }

    static final Cache<ScaledFont, java.awt.Font> fontCache = new Cache<ScaledFont, java.awt.Font>() {
        @Override
        protected java.awt.Font create(ScaledFont sf) {
            Font font = sf.font;
            float scale = sf.scale;
            int style = (font.isBold() ? java.awt.Font.BOLD : 0) | (font.isItalic() ? java.awt.Font.ITALIC : 0);
            return new java.awt.Font(font.getFamily(), style, Math.round(scale*font.getSizeInPoints()));
        }
    };

    static java.awt.Font getAwtFont(Font font, float scale) {
        return fontCache.get(new ScaledFont(font, scale));
    }

    public void setContent(Cell cell, float scale) {
        final CellStyle cellStyle = cell.getCellStyle();
        final Font font = cellStyle.getFont();
        setFont(getAwtFont(font, scale));
        setBackground(cellStyle.getFillBgColor());
        setForeground(font.getColor());
        StyledDocument doc = AttributedStringHelper.toStyledDocument(cell.getAttributedString(), scale);
        setDocument(doc);
    }

}
