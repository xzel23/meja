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
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.CellValueHelper;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Default implementation for cell editor.
 */
public class DefaultCellEditor implements CellEditor {

    private final CellEditorPane component;
    private Cell cell;
    private final SheetView sheetView;


    public DefaultCellEditor(SheetView sheetView) {
        this.sheetView = sheetView;
        component = new CellEditorPane();
        component.setOpaque(true);
        component.setBorder(BorderFactory.createEmptyBorder());

        // setup input map for keyboard navigation
        final InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), DefaultCellEditor.Actions.COMMIT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), DefaultCellEditor.Actions.ABORT);

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

        component.setContent(cell, sheetView.getScale(), false);

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

        // update the cell with the new value
        if (commit) {
            updateCellContent();
            sheetView.repaint(sheetView.getCellRect(cell));
        }

        // reset editor state
        this.cell = null;
        component.setText("");
        component.setVisible(false);

        // inform the sheetView
        sheetView.stoppedEditing();

        // give focus back to sheetview
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                sheetView.requestFocusInWindow();
            }
        });
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
        }, ABORT {
                    @SuppressWarnings("serial")
                    @Override
                    public Action getAction(final DefaultCellEditor editor) {
                        return new AbstractAction("ABORT") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                editor.stopEditing(false);
                            }
                        };
                    }
        };
        
        abstract Action getAction(DefaultCellEditor editor);
    }
    
}
