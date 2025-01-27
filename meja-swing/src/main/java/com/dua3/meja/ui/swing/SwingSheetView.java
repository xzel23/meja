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
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.swing.SwingGraphics;
import com.dua3.utility.swing.SwingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;

/**
 * Swing component for displaying instances of {@link Sheet}.
 */
public class SwingSheetView extends JPanel implements SheetView {

    private final transient SwingSheetViewDelegate delegate;

    private static final Logger LOG = LogManager.getLogger(SwingSheetView.class);

    private final transient CellEditor editor = new DefaultCellEditor(this);

    private final transient SwingSheetPane sheetPane;

    private final transient SwingSearchDialog searchDialog = new SwingSearchDialog(this);

    /**
     * Constructor.
     *
     * @param sheet the {@link Sheet}
     */
    public SwingSheetView(Sheet sheet) {
        super(new GridLayout(1, 1));
        this.delegate = new SwingSheetViewDelegate(sheet, this, CellRenderer::new);
        this.sheetPane = new SwingSheetPane(delegate);
        init();
    }

    @Override
    public void removeNotify() {
        searchDialog.dispose();
        super.removeNotify();
    }

    @Override
    public void repaintCell(Cell cell) {
        cell = cell.getLogicalCell();
        Rectangle2f r = delegate.getCellRect(cell);
        float m = getDelegate().getSelectionStrokeWidth() / 2.0f;
        CellStyle cs = cell.getCellStyle();
        r = r.addMargin(
                Math.max(m, cs.getBorderStyle(Direction.WEST).width()),
                Math.max(m, cs.getBorderStyle(Direction.NORTH).width()),
                Math.max(m, cs.getBorderStyle(Direction.EAST).width()),
                Math.max(m, cs.getBorderStyle(Direction.SOUTH).width())
        );
        sheetPane.repaintSheet(r);
    }

    /**
     * Scroll the currently selected cell into view.
     */
    @Override
    public void scrollToCurrentCell() {
        SwingUtilities.invokeLater(() -> delegate.getCurrentLogicalCell().ifPresent(sheetPane::ensureCellIsVisible));
    }

    /**
     * Set the current row and column.
     *
     * @param rowNum number of row to be set
     * @param colNum number of column to be set
     * @return true if the current logical cell changed
     */
    @Override
    public boolean setCurrentCell(int rowNum, int colNum) {
        return delegate.setCurrentCell(rowNum, colNum);
    }

    /**
     * End edit mode for the current cell.
     *
     * @param commit true if the content of the edited cell is to be updated
     */
    @Override
    public void stopEditing(boolean commit) {
        editor.stopEditing(commit);
    }

    /**
     * Reset editing state when finished editing. This method should only be called
     * from the {@link CellEditor#stopEditing} method of {@link CellEditor}
     * subclasses.
     */
    public void stoppedEditing() {
        delegate.setEditing(false);
        sheetPane.setScrollable(true);
    }

    public void copyToClipboard() {
        delegate.getSheet().getCurrentCell()
                .map(cell -> cell.getAsText(getLocale()))
                .ifPresent(t -> SwingUtil.setClipboardText(String.valueOf(t)));
    }

    private void init() {
        add(sheetPane);
        // setup input map for ...
        final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        // ... keyboard navigation
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), Actions.PAGE_UP);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), Actions.PAGE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_DOWN_MASK),
                Actions.MOVE_HOME);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_DOWN_MASK),
                Actions.MOVE_END);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), Actions.START_EDITING);
        // ... other stuff
        inputMap.put(KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK), Actions.SHOW_SEARCH_DIALOG);
        inputMap.put(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK), Actions.COPY);
        final ActionMap actionMap = getActionMap();
        for (Actions action : Actions.values()) {
            actionMap.put(action, SwingUtil.createAction(action.name(), () -> action.action().accept(this)));
        }
        // make focusable
        setFocusable(true);
        SwingUtilities.invokeLater(this::focusView);
    }

    @Override
    public void focusView() {
        requestFocusInWindow();
    }

    /**
     * Show the search dialog.
     */
    public void showSearchDialog() {
        searchDialog.setVisible(true);
    }

    /**
     * Enter edit mode for the current cell.
     */
    public void startEditing() {
        if (!delegate.isEditable() || delegate.isEditing()) {
            return;
        }

        delegate.getCurrentLogicalCell().ifPresent(cell -> {
            sheetPane.ensureCellIsVisible(cell);
            sheetPane.setScrollable(false);

            final JComponent editorComp = editor.startEditing(cell);

            final Rectangle2f cellRect = sheetPane.getCellRectInViewCoordinates(cell);
            SwingSegmentView sv = getViewContainingCell(cell);
            SegmentViewDelegate svDelegate = sv.getSvDelegate();
            AffineTransformation2f t = svDelegate.getTransformation();
            editorComp.setBounds(SwingGraphics.convert(Rectangle2f.withCorners(
                    t.transform(cellRect.min()),
                    t.transform(cellRect.max())
            )));

            sheetPane.add(editorComp);
            editorComp.validate();
            editorComp.setVisible(true);
            editorComp.repaint();

            delegate.setEditing(true);
        });
    }

    private SwingSegmentView getViewContainingCell(Cell cell) {
        int idx = (cell.getRowNumber() < getDelegate().getSplitRow() ? 0 : 2)
                + (cell.getColumnNumber() < getDelegate().getSplitColumn() ? 0 : 1);

        return switch (idx) {
            case 0 -> sheetPane.topLeftQuadrant;
            case 1 -> sheetPane.topRightQuadrant;
            case 2 -> sheetPane.bottomLeftQuadrant;
            case 3 -> sheetPane.bottomRightQuadrant;
            default -> throw new IllegalStateException("Unexpected value: " + idx);
        };
    }

    @Override
    public void updateContent() {
        LOG.trace("updateContent()");

        Sheet sheet = getSheet();
        try (var __ = delegate.automaticWriteLock()) {
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            delegate.setDisplayScale(getDisplayScale());
            delegate.setScale(new Scale2f(sheet.getZoom() * dpi / 72.0f));
            delegate.updateLayout();
        }
        SwingUtilities.invokeLater(() -> {
            delegate.getSheetPainter().update(sheet);
            revalidate();
            repaint();
        });
    }

    @Override
    public Scale2f getDisplayScale() {
        return SwingUtil.getDisplayScale(this);
    }

    Scale2f getScale() {
        return delegate.getScale();
    }

    @Override
    public SwingSheetViewDelegate getDelegate() {
        return delegate;
    }

    @Override
    public Locale getLocale() {
        Locale locale = super.getLocale();
        assert locale != null;
        return locale;
    }
}
