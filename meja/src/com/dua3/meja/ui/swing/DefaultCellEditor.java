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
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.AttributedStringHelper;
import com.dua3.meja.util.CellValueHelper;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class DefaultCellEditor implements CellEditor {

    private final JEditorPane component;
    private Cell cell;
    private final SheetView sheetView;

    /**
     * Actions for key bindings.
     */
    static enum Actions {

        COMMIT {
                    @SuppressWarnings("serial")
                    @Override
                    public Action getAction(final DefaultCellEditor editor) {
                        return new AbstractAction("COMMIT") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                editor.stopEditing(true);
                            }
                        };
                    }
                };

        abstract Action getAction(DefaultCellEditor editor);
    }

    public DefaultCellEditor(SheetView sheetView) {
        this.sheetView = sheetView;
        component = new JEditorPane();
        component.setOpaque(true);
        component.setBorder(BorderFactory.createEmptyBorder());
        component.setEditorKit(new CellEditorKit());

        // setup input map for keyboard navigation
        final InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), DefaultCellEditor.Actions.COMMIT);

        final ActionMap actionMap = component.getActionMap();
        for (DefaultCellEditor.Actions action : DefaultCellEditor.Actions.values()) {
            actionMap.put(action, action.getAction(this));
        }
        cell = null;
    }

    @Override
    public boolean isEditing() {
        return cell != null;
    }

    @Override
    public JComponent startEditing(Cell cell) {
        if (isEditing()) {
            throw new IllegalStateException("Already editing.");
        }
        this.cell = cell;

        final CellStyle cellStyle = cell.getCellStyle();
        final Font font = cellStyle.getFont();
        component.setFont(DefaultCellRenderer.getAwtFont(font));
        component.setBackground(cellStyle.getFillBgColor());
        component.setForeground(font.getColor());

        StyledDocument doc = AttributedStringHelper.toStyledDocument(cell.getAttributedString());
        component.setDocument(doc);
        component.revalidate();
        component.setCaretPosition(component.getDocument().getLength());
        component.selectAll();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                component.requestFocusInWindow();
            }
        });

        return component;
    }

    @Override
    public void stopEditing(boolean commit) {
        if (!isEditing()) {
            return;
        }

        if (commit) {
            updateCellContent();
            sheetView.repaint(sheetView.getCellRect(cell));
        }
        this.cell = null;
        component.setText("");
        component.setVisible(false);
        component.transferFocusBackward();
    }

    protected void updateCellContent() {
        String text;
        try {
            Document doc = component.getDocument();
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            text = "#ERROR";
            Logger.getLogger(DefaultCellEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        Sheet sheet = sheetView.getSheet();
        CellValueHelper helper = new CellValueHelper(sheet.getNumberFormat(), sheet.getDateFormat());
        helper.setCellValue(cell, text);
    }

    private class CellView extends BoxView {

        public CellView(Element elem, int axis) {
            super(elem, axis);
        }

        @Override
        public void paint(Graphics g, Shape allocation) {
            Graphics2D g2d = (Graphics2D) g;
            float scale = getScale();
            AffineTransform originalTransform = g2d.getTransform();
            g2d.scale(scale, scale);
            super.paint(g2d, allocation);
            g2d.setTransform(originalTransform);
        }

        protected float getScale() {
            return sheetView.getScale();
        }

        @Override
        public float getMinimumSpan(int axis) {
            return getScale() * super.getMinimumSpan(axis);
        }

        @Override
        public float getMaximumSpan(int axis) {
            return getScale() * super.getMaximumSpan(axis);
        }

        @Override
        public float getPreferredSpan(int axis) {
            return getScale() * super.getPreferredSpan(axis);
        }

        @Override
        protected void layout(int width, int height) {
            final float scale = getScale();
            super.layout(Math.round(width / scale), Math.round(height / scale));
        }

        @Override
        public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
            Rectangle alloc = a.getBounds();
            Shape s = super.modelToView(pos, alloc, b);
            float scale = getScale();
            alloc = s.getBounds();
            alloc.x *= scale;
            alloc.y *= scale;
            alloc.width *= scale;
            alloc.height *= scale;

            return alloc;
        }

        @Override
        public int viewToModel(float x, float y, Shape a,
                Position.Bias[] bias) {
            float scale = getScale();
            Rectangle alloc = a.getBounds();
            alloc.x /= scale;
            alloc.y /= scale;
            alloc.width /= scale;
            alloc.height /= scale;

            return super.viewToModel(x / scale, y / scale, alloc, bias);
        }
    }

    @SuppressWarnings("serial")
    class CellEditorKit extends StyledEditorKit {

        @Override
        public ViewFactory getViewFactory() {
            return new CellViewFactory();
        }

        class CellViewFactory implements ViewFactory {

            @Override
            public View create(Element elem) {
                String kind = elem.getName();
                if (kind != null) {
                    switch (kind) {
                        case AbstractDocument.SectionElementName:
                            return new CellView(elem, View.Y_AXIS);
                        case AbstractDocument.ContentElementName:
                            return new LabelView(elem);
                        case AbstractDocument.ParagraphElementName:
                            return new ParagraphView(elem);
                        case StyleConstants.ComponentElementName:
                            return new ComponentView(elem);
                        case StyleConstants.IconElementName:
                            return new IconView(elem);
                    }
                }

                // default to text display
                return new LabelView(elem);
            }

        }
    }
}
