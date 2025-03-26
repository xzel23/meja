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
import com.dua3.meja.util.CellValueHelper;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.swing.SwingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Default implementation for cell editor.
 */
public class DefaultCellEditor implements CellEditor {

    private static final Logger LOGGER = LogManager.getLogger(DefaultCellEditor.class);

    /**
     * Actions for key bindings.
     */
    enum Actions {

        COMMIT {
            @Override
            public Action getAction(final DefaultCellEditor editor) {
                return SwingUtil.createAction("COMMIT", e -> editor.stopEditing(true));
            }
        },
        ABORT {
            @Override
            public Action getAction(final DefaultCellEditor editor) {
                return SwingUtil.createAction("ABORT", e -> editor.stopEditing(false));
            }
        };

        abstract Action getAction(DefaultCellEditor editor);
    }

    private final CellEditorPane component;
    private @Nullable Cell cell;

    private final SwingSheetView sheetView;

    /**
     * Constructs a {@code DefaultCellEditor} instance for the specified {@link SwingSheetView}.
     * This editor is responsible for managing cell editing functionality within the sheet view.
     * It initializes the underlying editor pane, binds keyboard actions for editing,
     * and sets default properties for the editor component.
     *
     * @param sheetView the {@link SwingSheetView} associated with this cell editor
     */
    public DefaultCellEditor(SwingSheetView sheetView) {
        this.sheetView = sheetView;
        component = new CellEditorPane();
        component.setOpaque(true);
        component.setBorder(BorderFactory.createEmptyBorder());

        // setup input map for keyboard navigation
        final InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), Actions.COMMIT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Actions.ABORT);

        final ActionMap actionMap = component.getActionMap();
        for (Actions action : Actions.values()) {
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
        LangUtil.check(!isEditing(), "Already editing.");

        this.cell = cell;

        component.setContent(cell, sheetView.getScale(), false);

        component.revalidate();
        component.setCaretPosition(component.getDocument().getLength());
        component.selectAll();
        SwingUtilities.invokeLater(component::requestFocusInWindow);

        return component;
    }

    @Override
    public void stopEditing(boolean commit) {
        if (!isEditing()) {
            return;
        }

        // update the cell with the new value
        if (commit) {
            assert cell != null;
            updateCellContent();
            sheetView.repaintCell(cell);
        }

        // reset editor state
        this.cell = null;
        component.setText("");
        component.setVisible(false);

        // inform the sheetView
        sheetView.stoppedEditing();

        // give focus back to sheetview
        SwingUtilities.invokeLater(sheetView::requestFocusInWindow);
    }

    /**
     * Updates the content of the cell being edited by fetching the text from the
     * associated component's document, formatting it, and applying it to the cell.
     * <p>
     * This method retrieves the text from the document linked to the editor
     * component. If an error occurs during retrieval, it assigns a default error
     * value ("#ERROR") to the text. The retrieved text is then processed and set
     * to the cell using a {@code CellValueHelper}, which handles parsing and
     * formatting of the value based on locale-specific number and date formats.
     * <p>
     * The {@code CellValueHelper} ensures the proper conversion and application
     * of the text to the cell, accommodating various types of values, such as
     * numbers, dates, booleans, formulas, or plain text.
     * <p>
     * If assertions are enabled, this method will throw an {@code AssertionError}
     * if the cell is {@code null}.
     */
    protected void updateCellContent() {
        assert cell != null;
        String text;
        try {
            Document doc = component.getDocument();
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
            text = "#ERROR";
            LOGGER.warn("could not get text from document", ex);
        }
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        NumberFormat numberFormat = NumberFormat.getInstance(getLocale());
        CellValueHelper helper = new CellValueHelper(numberFormat, dateFormatter);
        helper.setCellValue(cell, text);
    }

    private Locale getLocale() {
        return component.getLocale();
    }

}
