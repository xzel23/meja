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
import com.dua3.meja.util.CellValueHelper;
import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import org.jspecify.annotations.Nullable;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Default implementation for cell editor.
 */
public class DefaultCellEditor implements CellEditor {

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
        NEWLINE {
            @Override
            public Action getAction(final DefaultCellEditor editor) {
                return SwingUtil.createAction("NEWLINE", e -> editor.component.replaceSelection("\n"));
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

    private final ScaledTextEditorPane component;
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
        this.component = new ScaledTextEditorPane();
        final JComponent textComponent = component.getTextComponent();

        component.setWrapText(false);
        component.setEditable(true);
        component.setEnterKeyInsertsNewline(false);
        component.setOpaque(true);
        component.setBorder(BorderFactory.createEmptyBorder());

        // setup input map for keyboard navigation
        final InputMap inputMap = textComponent.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), Actions.COMMIT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), Actions.NEWLINE);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Actions.ABORT);

        final ActionMap actionMap = textComponent.getActionMap();
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

        setContent(sheetView.getScale(), false);
        component.setVisible(true);
        component.setEditable(true);

        SwingUtilities.invokeLater(() -> component.getTextComponent().requestFocusInWindow());

        return component;
    }

    @Override
    public void stopEditing(boolean commit) {
        if (!isEditing()) {
            return;
        }

        component.setEditable(false);

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

        // give focus back to sheet view
        SwingUtilities.invokeLater(sheetView::requestFocusInWindow);
    }

    /**
     * Updates the content of the edited cell using the current plain text value.
     */
    protected void updateCellContent() {
        assert cell != null;
        String text = component.getText().toString();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        NumberFormat numberFormat = NumberFormat.getInstance(getLocale());
        CellValueHelper helper = new CellValueHelper(numberFormat, dateFormatter);
        helper.setCellValue(cell, text);
    }

    private Locale getLocale() {
        return component.getLocale();
    }

    /**
     * Set the editor content to the content of the given cell.
     *
     * @param scale the font scaling factor to use
     * @param eval set to true to display formula results instead of the formula
     *             itself
     */
    private void setContent(Scale2f scale, boolean eval) {
        assert cell != null : "cell must not be null";

        CellStyle cellStyle = cell.getCellStyle();

        Color fg = cellStyle.getFillFgColor();
        Color bg = cellStyle.getFillBgColor();
        Font font = cellStyle.getFont();

        component.setBackground(SwingUtil.convert(fg));
        component.setForeground(SwingUtil.convert(bg));
        component.setTextFont(font.scaled(scale.sy()));
        component.setFontScale(scale.sy());

        final RichText text;
        if (!eval && cell.getCellType() == CellType.FORMULA) {
            text = RichText.valueOf("=" + cell.getFormula());
        } else {
            text = cell.getAsText(getLocale());
        }

        component.setCellText(text);

        component.revalidate();
        component.setCaretPosition(component.getText().length());
        component.selectAll();

        component.repaint();
    }

}
