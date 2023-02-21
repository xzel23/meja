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
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import com.dua3.utility.data.Color;
import com.dua3.utility.swing.StyledDocumentConverter;
import com.dua3.utility.swing.SwingFontUtil;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;

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
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import java.util.Arrays;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
@SuppressWarnings("serial")
public class CellEditorPane extends JTextPane {

    /**
     * A subclass of BoxView that enables alignment options.
     */
    final class AlignedBoxView extends BoxView {

        AlignedBoxView(Element elem, int axis) {
            super(elem, axis);
        }

        @Override
        protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {

            super.layoutMajorAxis(targetSpan, axis, offsets, spans);

            int textBlockHeight = Arrays.stream(spans).sum();

            final int available = targetSpan - textBlockHeight;
            float offset;
            float increase;
            switch (vAlign) {
                case ALIGN_TOP -> {
                    offset = 0;
                    increase = 0;
                }
                case ALIGN_BOTTOM -> {
                    offset = available;
                    increase = 0;
                }
                case ALIGN_MIDDLE -> {
                    offset = (float) available / 2;
                    increase = 0;
                }
                case ALIGN_JUSTIFY -> {
                    offset = (float) available / spans.length;
                    increase = offset;
                }
                case ALIGN_DISTRIBUTED -> {
                    offset = available;
                    increase = 0;
                }
                default -> throw new IllegalStateException("unexpected value: "+vAlign);
            }

            for (int i = 0; i < offsets.length; i++) {
                offsets[i] += Math.round(offset + i * increase);
            }

        }
    }

    /**
     * A custom EditorKit to allow vertical alignment of text.
     */
    final class CellEditorKit extends StyledEditorKit {
        @Override
        public ViewFactory getViewFactory() {
            return new CellEditorViewFactory();
        }
    }

    /**
     * A ViewFactory for the custom EditorKit.
     */
    final class CellEditorViewFactory implements ViewFactory {

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
                default:
                }
            }

            return new LabelView(elem);
        }

    }

    /**
     * Translate {@code HAlign.ALIGN_AUTOMATIC} to the actual value for the cell
     * type.
     *
     * @param hAlign the horizontal alignment
     * @param type   the cell type
     * @return
     *         <ul>
     *         <li>{@code hAlign}, if {@code hAlign!=HAlign.ALIGN_AUTOMATIC}</li>
     *         <li>otherwise the horizontal alignment to apply to cells of the given
     *         type</li>
     *         </ul>
     */
    public static HAlign getHAlign(HAlign hAlign, CellType type) {
        if (hAlign != HAlign.ALIGN_AUTOMATIC) {
            return hAlign;
        }

        return switch (type) {
            case BLANK, BOOLEAN, ERROR, NUMERIC, DATE, DATE_TIME -> HAlign.ALIGN_RIGHT;
            case TEXT, FORMULA -> HAlign.ALIGN_LEFT;
            default -> throw new IllegalStateException("unexpected value: type");
        };
    }

    private VAlign vAlign = VAlign.ALIGN_TOP;

    /**
     * Construct a new CellEditorPane.
     */
    public CellEditorPane() {
        setEditorKit(new CellEditorKit());
    }

    /**
     * Get a {@link SimpleAttributeSet} corresponding to the cellstyle.
     *
     * @param cellStyle the cell style
     * @param cell      the cell (because cell tye influences the style attributes)
     * @return a SimpleAttributeSet
     */
    public SimpleAttributeSet getCellAttributes(final CellStyle cellStyle, Cell cell) {
        SimpleAttributeSet dfltAttr = new SimpleAttributeSet();
        HAlign hAlign = getHAlign(cellStyle.getHAlign(), cell.getResultType());
        switch (hAlign) {
            case ALIGN_LEFT -> StyleConstants.setAlignment(dfltAttr, StyleConstants.ALIGN_LEFT);
            case ALIGN_CENTER -> StyleConstants.setAlignment(dfltAttr, StyleConstants.ALIGN_CENTER);
            case ALIGN_RIGHT -> StyleConstants.setAlignment(dfltAttr, StyleConstants.ALIGN_RIGHT);
            case ALIGN_JUSTIFY -> StyleConstants.setAlignment(dfltAttr, StyleConstants.ALIGN_JUSTIFIED);
            // ALIGN_AUTOMATIC should already be resolved
            default -> throw new IllegalStateException("unexpected value: "+hAlign);
        }

        return dfltAttr;
    }

    private static final FontUtil<java.awt.Font> fontUtil = new SwingFontUtil();

    /**
     * Set the editor content to the content of the given cell.
     *
     * @param cell  the cell to display
     * @param eval  set to true to display formula results instead of the formula
     *              itself
     */
    public void setContent(Cell cell, double scale, boolean eval) {
        CellStyle cellStyle = cell.getCellStyle();

        Color fg = cellStyle.getFillFgColor();
        Color bg = cellStyle.getFillBgColor();
        Font font = cellStyle.getFont();

        setBackground(SwingUtil.toAwtColor(fg));
        setForeground(SwingUtil.toAwtColor(bg));
        
        final RichText text;
        if (!eval && cell.getCellType() == CellType.FORMULA) {
            text = RichText.valueOf("=" + cell.getFormula());
        } else {
            text = cell.getAsText(getLocale());
        }
        
        setDocument(
                StyledDocumentConverter.create(
                        StyledDocumentConverter.addStyledAttributes(getCellAttributes(cellStyle, cell)),
                        StyledDocumentConverter.scale(scale),
                        StyledDocumentConverter.defaultFont(font)
                ).convert(text)
        );

        this.vAlign = cellStyle.getVAlign();

        revalidate();
        repaint();
    }
    
}
