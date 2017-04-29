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

import java.awt.BasicStroke;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Color;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.SearchOptions;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.Rectangle;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.util.MejaHelper;

/**
 * Swing component for displaying instances of {@link Sheet}.
 */
public class SwingSheetView extends JPanel
        implements SheetView, PropertyChangeListener {

    private class SearchDialog extends JDialog {

        private static final long serialVersionUID = 1L;

        private final JTextField jtfText = new JTextField(40);
        private final JCheckBox jcbIgnoreCase = new JCheckBox("ignore case", true);
        private final JCheckBox jcbMatchCompleteText = new JCheckBox("match complete text", false);

        SearchDialog() {
            init();
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);

            if (visible) {
                jtfText.requestFocusInWindow();
                jtfText.selectAll();
            }
        }

        private void init() {
            setTitle("Search");
            setModal(true);
            setResizable(false);

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            getRootPane().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

            // text label
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(new JLabel("Text:"), c);

            // text input
            c.gridx = 2;
            c.gridy = 1;
            c.gridwidth = 4;
            c.gridheight = 1;
            add(jtfText, c);

            // options
            c.gridx = 1;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(new JLabel("Options:"), c);

            c.gridx = 2;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(jcbIgnoreCase, c);

            c.gridx = 3;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(jcbMatchCompleteText, c);

            // submit button
            c.gridx = 4;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            final JButton submitButton = new JButton(new AbstractAction("Search") {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    doSearch();
                }

            });
            add(submitButton, c);

            // close button
            c.gridx = 5;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(new JButton(new AbstractAction("Close") {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            }), c);

            // Enter starts search
            SwingUtilities.getRootPane(submitButton).setDefaultButton(submitButton);

            // Escape closes dialog
            final AbstractAction escapeAction = new AbstractAction() {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent ae) {
                    setVisible(false);
                }
            };

            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE_KEY");
            rootPane.getActionMap().put("ESCAPE_KEY", escapeAction);

            // pack layout
            pack();
        }

        void doSearch() {
            if (sheet == null) {
                return;
            }

            EnumSet<SearchOptions> options = EnumSet.of(SearchOptions.SEARCH_FROM_CURRENT);

            if (jcbIgnoreCase.isSelected()) {
                options.add(SearchOptions.IGNORE_CASE);
            }

            if (jcbMatchCompleteText.isSelected()) {
                options.add(SearchOptions.MATCH_COMPLETE_TEXT);
            }

            Optional<Cell> oc = MejaHelper.find(sheet, getText(), options);
            if (!oc.isPresent()) {
                JOptionPane.showMessageDialog(this, "Text was not found.");
            } else {
                Cell cell = oc.get();
                setCurrentCell(cell.getRowNumber(), cell.getColumnNumber());
            }
        }

        String getText() {
            return jtfText.getText();
        }

    }

    private class SheetPane extends JScrollPane {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        final SwingSegmentView topLeftQuadrant;
        final SwingSegmentView topRightQuadrant;
        final SwingSegmentView bottomLeftQuadrant;
        final SwingSegmentView bottomRightQuadrant;

        SheetPane() {
            // define row and column ranges and set up segments
            final IntSupplier startColumn = () -> 0;
            final IntSupplier splitColumn = () -> getSplitColumn();
            final IntSupplier endColumn = () -> getColumnCount();

            final IntSupplier startRow = () -> 0;
            final IntSupplier splitRow = () -> getSplitRow();
            final IntSupplier endRow = () -> getRowCount();

            topLeftQuadrant = new SwingSegmentView(startRow, splitRow, startColumn, splitColumn);
            topRightQuadrant = new SwingSegmentView(startRow, splitRow, splitColumn, endColumn);
            bottomLeftQuadrant = new SwingSegmentView(splitRow, endRow, startColumn, splitColumn);
            bottomRightQuadrant = new SwingSegmentView(splitRow, endRow, splitColumn, endColumn);

            init();
        }

        /**
         * Scroll cell into view.
         *
         * @param cell
         *            the cell to scroll to
         */
        public void ensureCellIsVisibile(Cell cell) {
            final Rectangle cellRect = sheetPainter.getCellRect(cell);
            boolean aboveSplit = getSplitY() >= cellRect.getBottom();
            boolean toLeftOfSplit = getSplitX() >= cellRect.getRight();

            cellRect.translate(toLeftOfSplit ? 0 : -sheetPainter.getSplitX(),
                    aboveSplit ? 0 : -sheetPainter.getSplitY());

            if (aboveSplit && toLeftOfSplit) {
                // nop: cell is always visible!
            } else if (aboveSplit) {
                // only scroll x
                java.awt.Rectangle r = new java.awt.Rectangle(
                        xS2D(cellRect.getX()),
                        yS2D(cellRect.getY()) - getY(),
                        wS2D(cellRect.getW()),
                        1);
                bottomRightQuadrant.scrollRectToVisible(r);
            } else if (toLeftOfSplit) {
                // only scroll y
                java.awt.Rectangle r = new java.awt.Rectangle(
                        xS2D(cellRect.getX()) - getX(),
                        yS2D(cellRect.getY()),
                        1,
                        hS2D(cellRect.getH()));
                bottomRightQuadrant.scrollRectToVisible(r);
            } else {
                bottomRightQuadrant.scrollRectToVisible(rectS2D(cellRect));
            }
        }

        @Override
        public void validate() {
            if (sheet != null) {
                topLeftQuadrant.validate();
                topRightQuadrant.validate();
                bottomLeftQuadrant.validate();
                bottomRightQuadrant.validate();
            }

            super.validate();
        }

        private Rectangle getCellRectInViewCoordinates(Cell cell) {
            if (sheet == null) {
                return null;
            }

            boolean isTop = cell.getRowNumber() < sheet.getSplitRow();
            boolean isLeft = cell.getColumnNumber() < sheet.getSplitColumn();

            final SwingSegmentView quadrant;
            if (isTop) {
                quadrant = isLeft ? topLeftQuadrant : topRightQuadrant;
            } else {
                quadrant = isLeft ? bottomLeftQuadrant : bottomRightQuadrant;
            }

            boolean insideViewPort = !(isLeft && isTop);

            final Container parent = quadrant.getParent();
            Point pos = insideViewPort ? ((JViewport) parent).getViewPosition() : new Point();

            int i = cell.getRowNumber();
            int j = cell.getColumnNumber();
            double x = sheetPainter.getColumnPos(j);
            double w = sheetPainter.getColumnPos(j + cell.getHorizontalSpan()) - x + 1;
            double y = sheetPainter.getRowPos(i);
            double h = sheetPainter.getRowPos(i + cell.getVerticalSpan()) - y + 1;
            x -= quadrant.getXMinInViewCoordinates();
            x += parent.getX();
            x -= pos.x;
            y -= quadrant.getYMinInViewCoordinates();
            y += parent.getY();
            y -= pos.y;

            return new Rectangle(x, y, w, h);
        }

        private int getColumnCount() {
            return sheet == null ? 0 : 1 + sheet.getLastColNum();
        }

        private int getRowCount() {
            return sheet == null ? 0 : 1 + sheet.getLastRowNum();
        }

        private int getSplitColumn() {
            return sheet == null ? 0 : sheet.getSplitColumn();
        }

        private int getSplitRow() {
            return sheet == null ? 0 : sheet.getSplitRow();
        }

        private void init() {
            setDoubleBuffered(true);

            // set quadrant painters
            setViewportView(bottomRightQuadrant);
            setColumnHeaderView(topRightQuadrant);
            setRowHeaderView(bottomLeftQuadrant);
            setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, topLeftQuadrant);

            setViewportBorder(BorderFactory.createEmptyBorder());
        }

        private void repaintSheet(Rectangle rect) {
            topLeftQuadrant.repaintSheet(rect);
            topRightQuadrant.repaintSheet(rect);
            bottomLeftQuadrant.repaintSheet(rect);
            bottomRightQuadrant.repaintSheet(rect);
        }

        private void setScrollable(boolean b) {
            getHorizontalScrollBar().setEnabled(b);
            getVerticalScrollBar().setEnabled(b);
            getViewport().getView().setEnabled(b);
        }

    }

    /**
     * Actions for key bindings.
     */
    static enum Actions {
        MOVE_UP(view -> view.move(Direction.NORTH)),
        MOVE_DOWN(view -> view.move(Direction.SOUTH)),
        MOVE_LEFT(view -> view.move(Direction.WEST)),
        MOVE_RIGHT(view -> view.move(Direction.EAST)),
        PAGE_UP(view -> view.movePage(Direction.NORTH)),
        PAGE_DOWN(view -> view.movePage(Direction.SOUTH)),
        MOVE_HOME(view -> view.moveHome()),
        MOVE_END(view -> view.moveEnd()),
        START_EDITING(view -> view.startEditing()),
        SHOW_SEARCH_DIALOG(view -> view.showSearchDialog()),
        COPY(view -> view.copyToClipboard());

        private final Consumer<SwingSheetView> action;

        private Actions(Consumer<SwingSheetView> action) {
            this.action = action;
        }

        Action getAction(SwingSheetView view) {
            return new AbstractAction(name()) {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    action.accept(view);
                }
            };
        }
    }

    class SwingSegmentView extends JPanel
            implements Scrollable, SegmentView<SwingSheetView, SwingGraphicsContext> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final IntSupplier startRow;
        private final IntSupplier endRow;
        private final IntSupplier startColumn;
        private final IntSupplier endColumn;

        SwingSegmentView(IntSupplier startRow, IntSupplier endRow, IntSupplier startColumn, IntSupplier endColumn) {
            super(null, false);
            this.startRow = startRow;
            this.endRow = endRow;
            this.startColumn = startColumn;
            this.endColumn = endColumn;
            init();
        }

        @Override
        public int getBeginColumn() {
            return startColumn.getAsInt();
        }

        @Override
        public int getBeginRow() {
            return startRow.getAsInt();
        }

        @Override
        public int getEndColumn() {
            return endColumn.getAsInt();
        }

        @Override
        public int getEndRow() {
            return endRow.getAsInt();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 3 * getScrollableUnitIncrement(visibleRect, orientation, direction);
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            if (orientation == SwingConstants.VERTICAL) {
                // scroll vertical
                if (direction < 0) {
                    // scroll up
                    final double y = yD2S(visibleRect.y);
                    final int yD = yS2D(y);
                    int i = sheetPainter.getRowNumberFromY(y);
                    int posD = yD;
                    while (i >= 0 && yD <= posD) {
                        posD = yS2D(sheetPainter.getRowPos(i--));
                    }
                    return yD - posD;
                } else {
                    // scroll down
                    final double y = yD2S(visibleRect.y + visibleRect.height);
                    final int yD = yS2D(y);
                    int i = sheetPainter.getRowNumberFromY(y);
                    int posD = yD;
                    while (i <= sheetPainter.getRowCount() && posD <= yD) {
                        posD = yS2D(sheetPainter.getRowPos(i++));
                    }
                    return posD - yD;
                }
            } else // scroll horizontal
            {
                if (direction < 0) {
                    // scroll left
                    final double x = xD2S(visibleRect.x);
                    final int xD = xS2D(x);
                    int j = sheetPainter.getColumnNumberFromX(x);
                    int posD = xD;
                    while (j >= 0 && xD <= posD) {
                        posD = xS2D(sheetPainter.getColumnPos(j--));
                    }
                    return xD - posD;
                } else {
                    // scroll right
                    final double x = xD2S(visibleRect.x + visibleRect.width);
                    int xD = xS2D(x);
                    int j = sheetPainter.getColumnNumberFromX(x);
                    int posD = xD;
                    while (j <= sheetPainter.getColumnCount() && posD <= xD) {
                        posD = xS2D(sheetPainter.getColumnPos(j++));
                    }
                    return posD - xD;
                }
            }
        }

        @Override
        public Sheet getSheet() {
            return sheet;
        }

        @Override
        public SwingSheetPainter getSheetPainter() {
            return sheetPainter;
        }

        @Override
        public boolean isOptimizedDrawingEnabled() {
            return true;
        }

        @Override
        public void setViewSize(double wd, double hd) {
            int w = wS2D(wd);
            int h = hS2D(hd);
            Dimension d = new Dimension(w, h);
            setSize(d);
            setPreferredSize(d);
        }

        @Override
        public void validate() {
            updateLayout();
            super.validate();
        }

        private void init() {
            setOpaque(true);
            setDoubleBuffered(false);
            // listen to mouse events
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Point p = e.getPoint();
                    translateMousePosition(p);
                    onMousePressed(p.x, p.y);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            // clear background by calling super method
            super.paintComponent(g2d);

            // set transformation
            final int x = getXMinInViewCoordinates();
            final int y = getYMinInViewCoordinates();
            g2d.translate(-x, -y);

            // get dimensions
            final int width = getWidth();
            final int height = getHeight();

            // draw sheet
            sheetPainter.drawSheet(new SwingGraphicsContext(g2d, SwingSheetView.this));

            // draw split lines
            g2d.setColor(MejaSwingHelper.toAwtColor(Color.BLACK));
            g2d.setStroke(new BasicStroke());
            if (hasHLine()) {
                g2d.drawLine(x, height + y - 1, width + x - 1, height + y - 1);
            }
            if (hasVLine()) {
                g2d.drawLine(width + x - 1, y, width + x - 1, height + y - 1);
            }
        }

        int getXMinInViewCoordinates() {
            double x = sheetPainter.getColumnPos(getBeginColumn());
            if (hasRowHeaders()) {
                x -= sheetPainter.getRowLabelWidth();
            }
            return xS2D(x);
        }

        int getYMinInViewCoordinates() {
            double y = sheetPainter.getRowPos(getBeginRow());
            if (hasColumnHeaders()) {
                y -= sheetPainter.getColumnLabelHeight();
            }
            return yS2D(y);
        }

        void repaintSheet(Rectangle rect) {
            java.awt.Rectangle rect2 = rectS2D(rect);
            rect2.translate(-getXMinInViewCoordinates(), -getYMinInViewCoordinates());
            repaint(rect2);
        }

        void translateMousePosition(Point p) {
            p.translate(getXMinInViewCoordinates(), getYMinInViewCoordinates());
        }
    }

    private static final long serialVersionUID = 1L;

    private final transient SwingSheetPainter sheetPainter;

    private final transient CellEditor editor = new DefaultCellEditor(this);

    private final transient SheetPane sheetPane = new SheetPane();

    private final transient SearchDialog searchDialog = new SearchDialog();

    /**
     * The scale used to calculate screen sizes dependent of display resolution.
     */
    private double scale = 1f;

    /**
     * The sheet displayed.
     */
    private Sheet sheet;

    /**
     * The color to use for the grid lines.
     */
    private Color gridColor = Color.LIGHTGRAY;

    /**
     * Read-only mode.
     */
    private boolean editable = false;

    /**
     * Editing state.
     */
    private boolean editing = false;

    /**
     * Constructor.
     *
     * No sheet is set.
     */
    public SwingSheetView() {
        this(null);
    }

    /**
     * Construct a new SheetView for the given sheet.
     *
     * @param sheet
     *            the sheet to display
     */
    public SwingSheetView(Sheet sheet) {
        super(new GridLayout(1, 1));
        sheetPainter = new SwingSheetPainter(this, new DefaultCellRenderer());
        init(sheet);
    }

    /**
     * Get column name.
     *
     * @param j
     *            the column number
     * @return name of column
     */
    public String getColumnName(int j) {
        return MejaHelper.getColumnName(j);
    }

    /**
     * Get the current column number.
     *
     * @return column number of the selected cell
     */
    public int getCurrentColNum() {
        return sheet == null ? 0 : sheet.getCurrentCell().getColumnNumber();
    }

    /**
     * Get the current row number.
     *
     * @return row number of the selected cell
     */
    public int getCurrentRowNum() {
        return sheet == null ? 0 : sheet.getCurrentCell().getRowNumber();
    }

    @Override
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Get row name.
     *
     * @param i
     *            the row number
     * @return name of row
     */
    public String getRowName(int i) {
        return Integer.toString(i + 1);
    }

    @Override
    public Sheet getSheet() {
        return sheet;
    }

    /**
     * @return the sheetHeight
     */
    public int getSheetHeight() {
        return hS2D(sheetPainter.getSheetHeightInPoints());
    }

    public Dimension getSheetSize() {
        return new Dimension(getSheetWidth() + 1, getSheetHeight() + 1);
    }

    /**
     * @return the sheetWidth
     */
    public int getSheetWidth() {
        return wS2D(sheetPainter.getSheetWidthInPoints());
    }

    /**
     * Check whether editing is enabled.
     *
     * @return true if this SwingSheetView allows editing.
     */
    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Check editing state.
     *
     * @return true, if a cell is being edited.
     */
    @Override
    public boolean isEditing() {
        return editing;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
        case Sheet.PROPERTY_ZOOM:
        case Sheet.PROPERTY_LAYOUT:
            updateContent();
            break;
        case Sheet.PROPERTY_FREEZE:
            updateContent();
            scrollToCurrentCell();
            break;
        case Sheet.PROPERTY_ACTIVE_CELL:
            scrollToCurrentCell();
            repaintCell((Cell) evt.getOldValue());
            repaintCell((Cell) evt.getNewValue());
            break;
        case Sheet.PROPERTY_CELL_CONTENT:
        case Sheet.PROPERTY_CELL_STYLE:
            repaintCell((Cell) evt.getSource());
            break;
        default:
            // nop
            break;
        }
    }

    @Override
    public void removeNotify() {
        searchDialog.dispose();
        super.removeNotify();
    }

    public void repaintCell(Cell cell) {
        sheetPane.repaintSheet(sheetPainter.getSelectionRect(cell));
    }

    /**
     * Scroll the currently selected cell into view.
     */
    @Override
    public void scrollToCurrentCell() {
        sheetPane.ensureCellIsVisibile(getCurrentCell().getLogicalCell());
    }

    /**
     * Set current row and column.
     *
     * @param rowNum
     *            number of row to be set
     * @param colNum
     *            number of column to be set
     * @return true if the current logical cell changed
     */
    @Override
    public boolean setCurrentCell(int rowNum, int colNum) {
        if (sheet == null) {
            return false;
        }

        Cell oldCell = sheet.getCurrentCell();
        int newRowNum = Math.max(sheet.getFirstRowNum(), Math.min(sheet.getLastRowNum(), rowNum));
        int newColNum = Math.max(sheet.getFirstColNum(), Math.min(sheet.getLastColNum(), colNum));
        sheet.setCurrentCell(newRowNum, newColNum);
        return getCurrentCell() != oldCell;
    }

    /**
     * Set the current column number.
     *
     * @param colNum
     *            number of column to be set
     */
    public void setCurrentColNum(int colNum) {
        setCurrentCell(getCurrentRowNum(), colNum);
    }

    /**
     * Set the current row number.
     *
     * @param rowNum
     *            number of row to be set
     */
    public void setCurrentRowNum(int rowNum) {
        setCurrentCell(rowNum, getCurrentColNum());
    }

    /**
     * Enable/disable sheet editing.
     *
     * @param editable
     *            true to allow editing
     */
    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public void setGridColor(Color gridColor) {
        this.gridColor = Objects.requireNonNull(gridColor);
    }

    /**
     * Set sheet to display.
     *
     * @param sheet
     *            the sheet to display
     */
    @Override
    public final void setSheet(Sheet sheet) {
        if (this.sheet != null) {
            this.sheet.removePropertyChangeListener(this);
        }

        if (sheet != this.sheet) {
            Sheet oldSheet = this.sheet;
            this.sheet = sheet;

            if (this.sheet != null) {
                sheet.addPropertyChangeListener(this);
            }

            updateContent();

            firePropertyChange(PROPERTY_SHEET, oldSheet, sheet);
        }
    }

    /**
     * End edit mode for the current cell.
     *
     * @param commit
     *            true if the content of the edited cell is to be updated
     */
    @Override
    public void stopEditing(boolean commit) {
        editor.stopEditing(commit);
    }

    /**
     * Reset editing state when finished editing. This method should only be
     * called from the {@link CellEditor#stopEditing} method of
     * {@link CellEditor} subclasses.
     */
    public void stoppedEditing() {
        editing = false;
        sheetPane.setScrollable(true);
    }

    private void copyToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection text = new StringSelection(getCurrentCell().getAsText().toString());
        clipboard.setContents(text, text);
    }

    private Cell getCurrentCell() {
        return sheet == null ? null : sheet.getCurrentCell();
    }

    private void init(Sheet sheet1) {
        add(sheetPane);
        // setup input map for ...
        final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        // ... keyboard navigation
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_UP, 0), Actions.PAGE_UP);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PAGE_DOWN, 0), Actions.PAGE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, InputEvent.CTRL_DOWN_MASK),
                Actions.MOVE_HOME);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_END, InputEvent.CTRL_DOWN_MASK),
                Actions.MOVE_END);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), Actions.START_EDITING);
        // ... other stuff
        inputMap.put(KeyStroke.getKeyStroke('F', java.awt.event.InputEvent.CTRL_DOWN_MASK), Actions.SHOW_SEARCH_DIALOG);
        inputMap.put(KeyStroke.getKeyStroke('C', java.awt.event.InputEvent.CTRL_DOWN_MASK), Actions.COPY);
        final ActionMap actionMap = getActionMap();
        for (Actions action : Actions.values()) {
            actionMap.put(action, action.getAction(this));
        }
        // listen to mouse events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e.getX() + xS2D(getSplitX()), e.getY() + yS2D(getSplitY()));
            }
        });
        // make focusable
        setFocusable(true);
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
        setSheet(sheet1);
    }

    /**
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d
     *            direction
     */
    private void move(Direction d) {
        Cell cell = getCurrentCell().getLogicalCell();

        switch (d) {
        case NORTH:
            setCurrentRowNum(cell.getRowNumber() - 1);
            break;
        case SOUTH:
            setCurrentRowNum(cell.getRowNumber() + cell.getVerticalSpan());
            break;
        case WEST:
            setCurrentColNum(cell.getColumnNumber() - 1);
            break;
        case EAST:
            setCurrentColNum(cell.getColumnNumber() + cell.getHorizontalSpan());
            break;
        }
    }

    /**
     * Move the selection rectangle to the bottom right cell.
     */
    private void moveEnd() {
        if (sheet == null) {
            return;
        }

        int row = sheet.getLastRowNum();
        int col = sheet.getLastColNum();
        setCurrentCell(row, col);
    }

    /**
     * Move the selection rectangle to the top left cell.
     */
    private void moveHome() {
        if (sheet == null) {
            return;
        }

        int row = sheet.getFirstRowNum();
        int col = sheet.getFirstColNum();
        setCurrentCell(row, col);
    }

    /**
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d
     *            direction
     */
    private void movePage(Direction d) {
        Cell cell = getCurrentCell().getLogicalCell();

        java.awt.Rectangle cellRect = rectS2D(sheetPainter.getCellRect(cell));
        switch (d) {
        case NORTH: {
            int y = Math.max(0, cellRect.y - getVisibleRect().height);
            setCurrentRowNum(sheetPainter.getRowNumberFromY(yD2S(y)));
            break;
        }
        case SOUTH: {
            int y = Math.min(getSheetHeight() - 1, cellRect.y + getVisibleRect().height);
            setCurrentRowNum(sheetPainter.getRowNumberFromY(yD2S(y)));
            break;
        }
        case WEST: {
            int x = Math.max(0, cellRect.x - getVisibleRect().width);
            setCurrentColNum(sheetPainter.getColumnNumberFromX(xD2S(x)));
            break;
        }
        case EAST: {
            int x = Math.min(getSheetWidth() - 1, cellRect.x + getVisibleRect().width);
            setCurrentColNum(sheetPainter.getColumnNumberFromX(xD2S(x)));
            break;
        }
        }
    }

    /**
     * Show the search dialog.
     */
    private void showSearchDialog() {
        searchDialog.setVisible(true);
    }

    /**
     * Enter edit mode for the current cell.
     */
    private void startEditing() {
        if (!isEditable() || isEditing()) {
            return;
        }

        final Cell cell = getCurrentCell().getLogicalCell();

        sheetPane.ensureCellIsVisibile(cell);
        sheetPane.setScrollable(false);

        final JComponent editorComp = editor.startEditing(cell);
        final Rectangle cellRect = sheetPane.getCellRectInViewCoordinates(cell);

        JComponent editorParent;
        editorParent = sheetPane;
        editorComp.setBounds(rectS2D(cellRect));
        editorParent.add(editorComp);
        editorComp.validate();
        editorComp.setVisible(true);
        editorComp.repaint();
        editing = true;
    }

    private void updateContent() {
        if (sheet == null) {
            return;
        }

        // scale according to screen resolution
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        double scaleDpi = dpi / 72.0; // 1 point = 1/72 inch
        scale = sheet.getZoom() * scaleDpi;

        sheetPainter.update(sheet);

        revalidate();
        repaint();
    }

    double getScale() {
        return scale;
    }

    /**
     * Get x-coordinate of split.
     *
     * @return x coordinate of split
     */
    double getSplitX() {
        return sheet == null ? 0 : sheetPainter.getColumnPos(sheet.getSplitColumn());
    }

    /**
     * Get y-coordinate of split.
     *
     * @return y coordinate of split
     */
    double getSplitY() {
        return sheet == null ? 0 : sheetPainter.getRowPos(sheet.getSplitRow());
    }

    double hD2S(int h) {
        return h / scale;
    }

    int hS2D(double h) {
        return (int) Math.round(scale * h);
    }

    void onMousePressed(int x, int y) {
        // make the cell under pointer the current cell
        int row = sheetPainter.getRowNumberFromY(yD2S(y));
        int col = sheetPainter.getColumnNumberFromX(xD2S(x));
        boolean currentCellChanged = setCurrentCell(row, col);
        requestFocusInWindow();

        if (!currentCellChanged) {
            // if it already was the current cell, start cell editing
            if (isEditable()) {
                startEditing();
                editing = true;
            }
        } else // otherwise stop cell editing
        {
            if (editing) {
                stopEditing(true);
                editing = false;
            }
        }
    }

    Rectangle rectD2S(java.awt.Rectangle r) {
        final double x1 = xD2S(r.x);
        final double y1 = yD2S(r.y);
        final double x2 = xD2S(r.x + r.width);
        final double y2 = yD2S(r.y + r.height);
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    java.awt.Rectangle rectS2D(Rectangle r) {
        final int x1 = xS2D(r.getLeft());
        final int y1 = yS2D(r.getTop());
        final int x2 = xS2D(r.getRight());
        final int y2 = yS2D(r.getBottom());
        return new java.awt.Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    double wD2S(int w) {
        return w / scale;
    }

    int wS2D(double w) {
        return (int) Math.round(scale * w);
    }

    double xD2S(int x) {
        return x / scale;
    }

    int xS2D(double x) {
        return (int) Math.round(scale * x);
    }

    double yD2S(int y) {
        return y / scale;
    }

    int yS2D(double y) {
        return (int) Math.round(scale * y);
    }
}
