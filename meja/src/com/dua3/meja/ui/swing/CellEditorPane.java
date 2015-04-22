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
import java.text.AttributedString;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultStyledDocument;
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

    static java.awt.Font getAwtFont(Font font, float scale) {
        int style = (font.isBold() ? java.awt.Font.BOLD : 0) | (font.isItalic() ? java.awt.Font.ITALIC : 0);
        return new java.awt.Font(font.getFamily(), style, Math.round(scale * font.getSizeInPoints()));
    }

    /**
     * Set the editor content to the content of given cell.
     *
     * @param cell the cell to display
     * @param scale the scale to apply
     * @param eval set to true to display formula results instead of the formula itself
     */
    public void setContent(Cell cell, float scale, boolean eval) {
        CellStyle cellStyle = cell.getCellStyle();
        final Font font = cellStyle.getFont();
        setFont(getAwtFont(font, scale));
        setBackground(cellStyle.getFillBgColor());
        setForeground(font.getColor());

        final StyledDocument doc;
        if (cell.getCellType() == CellType.FORMULA && !eval) {
            AttributeSet dfltAttr = getCellAttributes(cellStyle, cell);
            doc = new DefaultStyledDocument();
            try {
                doc.insertString(0, "=", dfltAttr);
                doc.insertString(1, cell.getFormula(), dfltAttr);
            } catch (BadLocationException ex) {
                Logger.getLogger(CellEditorPane.class.getName()).log(Level.SEVERE, "Exception", ex);
            }
        } else {
            // get attributed text first because this will update the
            // result type for formulas which is required for HAlign.ALIGN_AUTOMATIC
            final AttributedString text = cell.getAttributedString();
            AttributeSet dfltAttr = getCellAttributes(cellStyle, cell);
            doc = AttributedStringHelper.toStyledDocument(text, dfltAttr, scale);
        }
        setDocument(doc);

        this.vAlign = cellStyle.getVAlign();

        revalidate();
    }

    public SimpleAttributeSet getCellAttributes(final CellStyle cellStyle, Cell cell) throws IllegalStateException {
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

        // these must be set despite already setting the component font
        StyleConstants.setUnderline(dfltAttr, cellStyle.getFont().isUnderlined());
        StyleConstants.setStrikeThrough(dfltAttr, cellStyle.getFont().isStrikeThrough());

        return dfltAttr;
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
            case NUMERIC:
                return HAlign.ALIGN_RIGHT;
            case TEXT:
            case FORMULA:
                return HAlign.ALIGN_LEFT;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * A custom EditorKit to allow vertical alignment of text.
     */
    class CellEditorKit extends StyledEditorKit {

        private static final long serialVersionUID = 1L;

        @Override
        public ViewFactory getViewFactory() {
            return new CellEditorViewFactory();
        }
    }

    /**
     * A ViewFactory for the custom EditorKit.
     */
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

    /**
     * A subclass of BoxView that enables alignment options.
     */
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
