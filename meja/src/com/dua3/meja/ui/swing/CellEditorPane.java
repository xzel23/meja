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
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Font;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import com.dua3.meja.util.AttributedStringHelper;
import com.dua3.meja.util.Cache;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class CellEditorPane extends JTextPane {

    private static final long serialVersionUID = 1L;
    private VAlign vAlign = VAlign.ALIGN_TOP;

    public CellEditorPane() {
        setEditorKit(new CellEditorKit());
    }

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
                return scale == other.scale && font.equals(other.font);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Float.floatToRawIntBits(scale) ^ font.hashCode();
        }
    }

    static final Cache<ScaledFont, java.awt.Font> fontCache = new Cache<ScaledFont, java.awt.Font>() {
        @Override
        protected java.awt.Font create(ScaledFont sf) {
            Font font = sf.font;
            float scale = sf.scale;
            int style = (font.isBold() ? java.awt.Font.BOLD : 0) | (font.isItalic() ? java.awt.Font.ITALIC : 0);
            return new java.awt.Font(font.getFamily(), style, Math.round(scale * font.getSizeInPoints()));
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

        SimpleAttributeSet dfltAttr = new SimpleAttributeSet();
        switch (getHAlign(cellStyle.getHAlign(), cell.getResultType())) {
            case ALIGN_LEFT:
                StyleConstants.setAlignment(dfltAttr, StyleConstants.ALIGN_LEFT);
                break;
            case ALIGN_CENTER:
                StyleConstants.setAlignment(dfltAttr, StyleConstants.ALIGN_CENTER);
                break;
            case ALIGN_RIGHT:
                StyleConstants.setAlignment(dfltAttr, StyleConstants.ALIGN_RIGHT);
                break;
            case ALIGN_JUSTIFY:
                StyleConstants.setAlignment(dfltAttr, StyleConstants.ALIGN_JUSTIFIED);
                break;
            case ALIGN_AUTOMATIC: // ALIGN_AUTOMATIC should already be resolved
            default:
                throw new IllegalStateException();
        }

        StyledDocument doc = AttributedStringHelper.toStyledDocument(cell.getAttributedString(), dfltAttr, scale);
        setDocument(doc);

        this.vAlign=cellStyle.getVAlign();

        revalidate();
    }

    /**
     * Translate {@code HALign.ALIGN_AUTOMATIC} to the actual value for the cell
     * type.
     *
     * @param hAlign the horizontal alignment
     * @param type the cell type
     * @return
     * <ul>
     * <li>{@code hAlign}, if {@code hAlign!=HAlign.ALIGN_AUTOMATIC}</li>
     * <li>otherwise the horizontal alignment to apply to cells of the given
     * type</li>
     * </ul>
     */
    public static HAlign getHAlign(HAlign hAlign, CellType type) {
        if (hAlign != HAlign.ALIGN_AUTOMATIC) {
            return hAlign;
        }

        switch (type) {
            case BLANK:
            case BOOLEAN:
            case ERROR:
                return HAlign.ALIGN_RIGHT;
            case TEXT:
            case FORMULA:
                return HAlign.ALIGN_LEFT;
            default:
                throw new IllegalStateException();
        }
    }

    class CellEditorKit extends StyledEditorKit {

        private static final long serialVersionUID = 1L;

        @Override
        public ViewFactory getViewFactory() {
            return new CellEditorViewFactory();
        }
    }

    class CellEditorViewFactory implements ViewFactory {

        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new LabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new ParagraphView(elem);
                    case AbstractDocument.SectionElementName:
                        return new AlignedBoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                }
            }

            return new LabelView(elem);
        }

    }

    class AlignedBoxView extends BoxView {

        public AlignedBoxView(Element elem, int axis) {
            super(elem, axis);
        }

        @Override
        protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {

            super.layoutMajorAxis(targetSpan, axis, offsets, spans);

            int textBlockHeight = 0;
            for (int i = 0; i < spans.length; i++) {
                textBlockHeight = spans[i];
            }

            final int available = targetSpan - textBlockHeight;
            float offset;
            float increase;
            switch (vAlign) {
                case ALIGN_TOP:
                    offset = 0;
                    increase = 0;
                    break;
                case ALIGN_BOTTOM:
                    offset = available;
                    increase = 0;
                    break;
                case ALIGN_MIDDLE:
                    offset = available / 2;
                    increase = 0;
                    break;
                case ALIGN_JUSTIFY:
                    offset = (float) available / spans.length;
                    increase = offset;
                    break;
                default:
                    throw new IllegalStateException();
            }

            for (int i = 0; i < offsets.length; i++) {
                offsets[i] += Math.round(offset + i * increase);
            }

        }
    }
}
