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

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.SearchOptions;
import com.dua3.meja.model.SearchSettings;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.SheetEvent;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * Swing component for displaying instances of {@link Sheet}.
 */
@SuppressWarnings("serial")
public class SwingSheetView extends JPanel implements SheetView, Flow.Subscriber<SheetEvent> {

    private transient Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        if (this.subscription != null) {
            this.subscription.cancel();
        }
        this.subscription = subscription;
        this.subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(SheetEvent item) {
        switch (item.type()) {
            case SheetEvent.ZOOM_CHANGED, SheetEvent.LAYOUT_CHANGED, SheetEvent.ROWS_ADDED -> updateContent();
            case SheetEvent.SPLIT_CHANGED -> {
                updateContent();
                scrollToCurrentCell();
            }
            case SheetEvent.ACTIVE_CELL_CHANGED -> {
                SheetEvent.ActiveCellChanged evt = (SheetEvent.ActiveCellChanged) item;
                scrollToCurrentCell();
                repaintCell(evt.valueOld());
                repaintCell(evt.valueNew());
            }
            case SheetEvent.CELL_VALUE_CHANGED, SheetEvent.CELL_STYLE_CHANGED -> {
                repaintCell(((SheetEvent.CellChanged<?>) item).cell());
            }
            default -> {
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("error with subscription", throwable);
    }

    @Override
    public void onComplete() {
        LOG.debug("subscription completed");
        this.subscription = null;
    }

    private final class SearchDialog extends JDialog {

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
            JButton submitButton = new JButton(SwingUtil.createAction("Search", this::doSearch));
            add(submitButton, c);

            // close button
            c.gridx = 5;
            c.gridy = 2;
            c.gridwidth = 1;
            c.gridheight = 1;
            add(new JButton(SwingUtil.createAction("Close", e -> setVisible(false))), c);

            // Enter starts search
            SwingUtilities.getRootPane(submitButton).setDefaultButton(submitButton);

            // Escape closes dialog
            final Action escapeAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    setVisible(false);
                }
            };

            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    "ESCAPE_KEY");
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

            Optional<Cell> oc = sheet.find(getText(), SearchSettings.of(options));
            if (oc.isEmpty()) {
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

    private final class SheetPane extends JScrollPane {
        final SwingSegmentView topLeftQuadrant;
        final SwingSegmentView topRightQuadrant;
        final SwingSegmentView bottomLeftQuadrant;
        final SwingSegmentView bottomRightQuadrant;

        SheetPane() {
            // define row and column ranges and set up segments
            final IntSupplier startColumn = () -> 0;
            final IntSupplier splitColumn = this::getSplitColumn;
            final IntSupplier endColumn = this::getColumnCount;

            final IntSupplier startRow = () -> 0;
            final IntSupplier splitRow = this::getSplitRow;
            final IntSupplier endRow = this::getRowCount;

            topLeftQuadrant = new SwingSegmentView(startRow, splitRow, startColumn, splitColumn);
            topRightQuadrant = new SwingSegmentView(startRow, splitRow, splitColumn, endColumn);
            bottomLeftQuadrant = new SwingSegmentView(splitRow, endRow, startColumn, splitColumn);
            bottomRightQuadrant = new SwingSegmentView(splitRow, endRow, splitColumn, endColumn);

            init();
        }

        /**
         * Scroll cell into view.
         *
         * @param cell the cell to scroll to
         */
        public void ensureCellIsVisible(@Nullable Cell cell) {
            if (cell == null) {
                return;
            }

            Rectangle2f cellRect = sheetPainter.getCellRect(cell);
            boolean aboveSplit = getSplitY() >= cellRect.xMax();
            boolean toLeftOfSplit = getSplitX() >= cellRect.xMax();

            cellRect = cellRect.translate(
                    toLeftOfSplit ? 0 : -sheetPainter.getSplitX(),
                    aboveSplit ? 0 : -sheetPainter.getSplitY()
            );

            //noinspection StatementWithEmptyBody
            if (aboveSplit && toLeftOfSplit) {
                // nop: cell is always visible!
            } else if (aboveSplit) {
                // only scroll x
                java.awt.Rectangle r = new java.awt.Rectangle(xS2D(cellRect.x()), 1, wS2D(cellRect.width()), 1);
                bottomRightQuadrant.scrollRectToVisible(r);
            } else if (toLeftOfSplit) {
                // only scroll y
                java.awt.Rectangle r = new java.awt.Rectangle(1, yS2D(cellRect.y()), 1, hS2D(cellRect.height()));
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

        private Rectangle2f getCellRectInViewCoordinates(Cell cell) {
            if (sheet == null) {
                return new Rectangle2f(0, 0, 0, 0);
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
            float x = sheetPainter.getColumnPos(j);
            float w = sheetPainter.getColumnPos(j + cell.getHorizontalSpan()) - x + 1;
            float y = sheetPainter.getRowPos(i);
            float h = sheetPainter.getRowPos(i + cell.getVerticalSpan()) - y + 1;
            x -= quadrant.getXMinInViewCoordinates();
            x += parent.getX();
            x -= pos.x;
            y -= quadrant.getYMinInViewCoordinates();
            y += parent.getY();
            y -= pos.y;

            return new Rectangle2f(x, y, w, h);
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
            setCorner(ScrollPaneConstants.UPPER_LEADING_CORNER, topLeftQuadrant);

            setViewportBorder(BorderFactory.createEmptyBorder());
        }

        private void repaintSheet(Rectangle2f rect) {
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
    enum Actions {
        MOVE_UP(view -> view.move(Direction.NORTH)), MOVE_DOWN(view -> view.move(Direction.SOUTH)),
        MOVE_LEFT(view -> view.move(Direction.WEST)), MOVE_RIGHT(view -> view.move(Direction.EAST)),
        PAGE_UP(view -> view.movePage(Direction.NORTH)), PAGE_DOWN(view -> view.movePage(Direction.SOUTH)),
        MOVE_HOME(SwingSheetView::moveHome), MOVE_END(SwingSheetView::moveEnd),
        START_EDITING(SwingSheetView::startEditing), SHOW_SEARCH_DIALOG(SwingSheetView::showSearchDialog),
        COPY(SwingSheetView::copyToClipboard);

        private final Consumer<? super SwingSheetView> action;

        Actions(Consumer<? super SwingSheetView> action) {
            this.action = action;
        }

        Action getAction(SwingSheetView view) {
            return SwingUtil.createAction(name(), e -> action.accept(view));
        }
    }

    final class SwingSegmentView extends JPanel implements Scrollable, SegmentView<SwingSheetView, Graphics2D> {
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
                    final float y = yD2S(visibleRect.y);
                    final int yD = yS2D(y);
                    int i = sheetPainter.getRowNumberFromY(y);
                    int posD = yD;
                    while (i >= 0 && yD <= posD) {
                        posD = yS2D(sheetPainter.getRowPos(i--));
                    }
                    return yD - posD;
                } else {
                    // scroll down
                    final float y = yD2S(visibleRect.y + visibleRect.height);
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
                    final float x = xD2S(visibleRect.x);
                    final int xD = xS2D(x);
                    int j = sheetPainter.getColumnNumberFromX(x);
                    int posD = xD;
                    while (j >= 0 && xD <= posD) {
                        posD = xS2D(sheetPainter.getColumnPos(j--));
                    }
                    return xD - posD;
                } else {
                    // scroll right
                    final float x = xD2S(visibleRect.x + visibleRect.width);
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
        public void setViewSize(float wd, float hd) {
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

            if (sheet == null) {
                return;
            }

            // set transformation
            final int x = getXMinInViewCoordinates();
            final int y = getYMinInViewCoordinates();
            g2d.translate(-x, -y);

            // get dimensions
            final int width = getWidth();
            final int height = getHeight();

            // draw sheet
            sheetPainter.drawSheet(g2d);

            // draw split lines
            g2d.setColor(SwingUtil.toAwtColor(Color.BLACK));
            g2d.setStroke(new BasicStroke());
            if (hasHLine()) {
                g2d.drawLine(x, height + y - 1, width + x - 1, height + y - 1);
            }
            if (hasVLine()) {
                g2d.drawLine(width + x - 1, y, width + x - 1, height + y - 1);
            }
        }

        int getXMinInViewCoordinates() {
            float x = sheetPainter.getColumnPos(getBeginColumn());
            if (hasRowHeaders()) {
                x -= sheetPainter.getRowLabelWidth();
            }
            return xS2D(x);
        }

        int getYMinInViewCoordinates() {
            float y = sheetPainter.getRowPos(getBeginRow());
            if (hasColumnHeaders()) {
                y -= sheetPainter.getColumnLabelHeight();
            }
            return yS2D(y);
        }

        void repaintSheet(Rectangle2f rect) {
            java.awt.Rectangle rect2 = rectS2D(rect);
            rect2.translate(-getXMinInViewCoordinates(), -getYMinInViewCoordinates());
            repaint(rect2);
        }

        void translateMousePosition(Point p) {
            p.translate(getXMinInViewCoordinates(), getYMinInViewCoordinates());
        }
    }

    private static final Logger LOG = LogManager.getLogger(SwingSheetView.class);

    private transient IntFunction<String> columnNames = Sheet::getColumnName;

    private transient IntFunction<String> rowNames = Sheet::getRowName;

    private final transient SwingSheetPainter sheetPainter;

    private final transient CellEditor editor = new DefaultCellEditor(this);

    private final transient SheetPane sheetPane = new SheetPane();

    private final transient SearchDialog searchDialog = new SearchDialog();

    /**
     * The scale used to calculate screen sizes dependent of display resolution.
     */
    private float scale = 1.0f;

    /**
     * The sheet displayed.
     */
    private transient Sheet sheet;

    /**
     * The color to use for the grid lines.
     */
    private transient Color gridColor = Color.LIGHTGRAY;

    /**
     * Read-only mode.
     */
    private boolean editable;

    /**
     * Editing state.
     */
    private boolean editing;

    /**
     * Constructor.
     * <p>
     * No sheet is set.
     */
    public SwingSheetView() {
        this(null);
    }

    /**
     * Construct a new SheetView for the given sheet.
     *
     * @param sheet the sheet to display
     */
    public SwingSheetView(Sheet sheet) {
        super(new GridLayout(1, 1));
        sheetPainter = new SwingSheetPainter(this, new DefaultCellRenderer());
        init(sheet);
    }

    @Override
    public String getColumnName(int j) {
        return columnNames.apply(j);
    }

    /**
     * Get the current column number.
     *
     * @return column number of the selected cell
     */
    public int getCurrentColNum() {
        return getSheet().flatMap(Sheet::getCurrentCell).map(Cell::getColumnNumber).orElse(0);
    }

    /**
     * Get the current row number.
     *
     * @return row number of the selected cell
     */
    public int getCurrentRowNum() {
        return getSheet().flatMap(Sheet::getCurrentCell).map(Cell::getRowNumber).orElse(0);
    }

    @Override
    public Color getGridColor() {
        return gridColor;
    }

    @Override
    public String getRowName(int i) {
        return rowNames.apply(i);
    }

    @Override
    public Optional<Sheet> getSheet() {
        return Optional.ofNullable(sheet);
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
    public void removeNotify() {
        searchDialog.dispose();
        super.removeNotify();
    }

    public void repaintCell(@Nullable Cell cell) {
        if (cell != null) {
            sheetPane.repaintSheet(sheetPainter.getSelectionRect(cell));
        }
    }

    /**
     * Scroll the currently selected cell into view.
     */
    @Override
    public void scrollToCurrentCell() {
        getCurrentLogicalCell().ifPresent(sheetPane::ensureCellIsVisible);
    }

    @Override
    public void setColumnNames(IntFunction<String> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * Set current row and column.
     *
     * @param rowNum number of row to be set
     * @param colNum number of column to be set
     * @return true if the current logical cell changed
     */
    @Override
    public boolean setCurrentCell(int rowNum, int colNum) {
        if (sheet == null) {
            return false;
        }

        Cell oldCell = sheet.getCurrentCell().orElse(null);
        int newRowNum = Math.max(sheet.getFirstRowNum(), Math.min(sheet.getLastRowNum(), rowNum));
        int newColNum = Math.max(sheet.getFirstColNum(), Math.min(sheet.getLastColNum(), colNum));
        sheet.setCurrentCell(newRowNum, newColNum);
        //noinspection ObjectEquality
        return getCurrentCell().orElse(null) != oldCell;
    }

    /**
     * Set the current column number.
     *
     * @param colNum number of column to be set
     */
    public void setCurrentColNum(int colNum) {
        setCurrentCell(getCurrentRowNum(), colNum);
    }

    /**
     * Set the current row number.
     *
     * @param rowNum number of row to be set
     */
    public void setCurrentRowNum(int rowNum) {
        setCurrentCell(rowNum, getCurrentColNum());
    }

    /**
     * Enable/disable sheet editing.
     *
     * @param editable true to allow editing
     */
    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    @Override
    public void setRowNames(IntFunction<String> rowNames) {
        this.rowNames = rowNames;
    }

    /**
     * Set sheet to display.
     *
     * @param sheet the sheet to display
     */
    @Override
    public final void setSheet(@Nullable Sheet sheet) {
        if (this.subscription != null) {
            this.subscription.cancel();
            this.subscription = null;
        }

        //noinspection ObjectEquality
        if (sheet != this.sheet) {
            Sheet oldSheet = this.sheet;
            this.sheet = sheet;

            LOG.debug("sheet changed");

            if (this.sheet != null) {
                this.sheet.subscribe(this);
            }

            updateContent();

            firePropertyChange(PROPERTY_SHEET, oldSheet, sheet);
        }
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
        editing = false;
        sheetPane.setScrollable(true);
    }

    private void copyToClipboard() {
        getCurrentCell().ifPresent(cell -> SwingUtil.setClipboardText(cell.getAsText(getLocale()).toString()));
    }

    private Optional<Cell> getCurrentCell() {
        return getSheet().flatMap(Sheet::getCurrentCell);
    }

    private Optional<Cell> getCurrentLogicalCell() {
        return getSheet().flatMap(Sheet::getCurrentCell).map(Cell::getLogicalCell);
    }

    private void init(Sheet sheet1) {
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
        SwingUtilities.invokeLater(this::requestFocusInWindow);
        setSheet(sheet1);
    }

    /**
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d direction
     */
    private void move(Direction d) {
        getCurrentLogicalCell().ifPresent(cell -> {
            switch (d) {
                case NORTH -> setCurrentRowNum(cell.getRowNumber() - 1);
                case SOUTH -> setCurrentRowNum(cell.getRowNumber() + cell.getVerticalSpan());
                case WEST -> setCurrentColNum(cell.getColumnNumber() - 1);
                case EAST -> setCurrentColNum(cell.getColumnNumber() + cell.getHorizontalSpan());
            }
        });
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
     * @param d direction
     */
    private void movePage(Direction d) {
        getCurrentLogicalCell().ifPresent(cell -> {
            java.awt.Rectangle cellRect = rectS2D(sheetPainter.getCellRect(cell));
            switch (d) {
                case NORTH -> {
                    int y = Math.max(0, cellRect.y - getVisibleRect().height);
                    setCurrentRowNum(sheetPainter.getRowNumberFromY(yD2S(y)));
                }
                case SOUTH -> {
                    int y = Math.min(getSheetHeight() - 1, cellRect.y + getVisibleRect().height);
                    setCurrentRowNum(sheetPainter.getRowNumberFromY(yD2S(y)));
                }
                case WEST -> {
                    int x = Math.max(0, cellRect.x - getVisibleRect().width);
                    setCurrentColNum(sheetPainter.getColumnNumberFromX(xD2S(x)));
                }
                case EAST -> {
                    int x = Math.min(getSheetWidth() - 1, cellRect.x + getVisibleRect().width);
                    setCurrentColNum(sheetPainter.getColumnNumberFromX(xD2S(x)));
                }
            }
        });
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
        if (!editable || editing) {
            return;
        }

        getCurrentLogicalCell().ifPresent(cell -> {
            sheetPane.ensureCellIsVisible(cell);
            sheetPane.setScrollable(false);

            final JComponent editorComp = editor.startEditing(cell);

            final Rectangle2f cellRect = sheetPane.getCellRectInViewCoordinates(cell);
            editorComp.setBounds(rectS2D(cellRect));

            sheetPane.add(editorComp);
            editorComp.validate();
            editorComp.setVisible(true);
            editorComp.repaint();
            editing = true;
        });
    }

    private void updateContent() {
        LOG.debug("updating content");

        if (sheet == null) {
            return;
        }

        // scale according to screen resolution
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        float scaleDpi = dpi / 72.0f; // 1 point = 1/72 inch
        scale = sheet.getZoom() * scaleDpi;

        sheetPainter.update(sheet);

        revalidate();
        repaint();
    }

    float getScale() {
        return scale;
    }

    /**
     * Get x-coordinate of split.
     *
     * @return x coordinate of split
     */
    float getSplitX() {
        return sheet == null ? 0 : sheetPainter.getColumnPos(sheet.getSplitColumn());
    }

    /**
     * Get y-coordinate of split.
     *
     * @return y coordinate of split
     */
    float getSplitY() {
        return sheet == null ? 0 : sheetPainter.getRowPos(sheet.getSplitRow());
    }

    float hD2S(int h) {
        return h / scale;
    }

    int hS2D(float h) {
        return Math.round(scale * h);
    }

    void onMousePressed(int x, int y) {
        // make the cell under pointer the current cell
        int row = sheetPainter.getRowNumberFromY(yD2S(y));
        int col = sheetPainter.getColumnNumberFromX(xD2S(x));
        boolean currentCellChanged = setCurrentCell(row, col);
        requestFocusInWindow();

        if (currentCellChanged) {
            // if cell changed, stop cell editing
            if (editing) {
                stopEditing(true);
                editing = false;
            }
        } else {
            // otherwise start cell editing
            if (editable) {
                startEditing();
                editing = true;
            }
        }
    }

    Rectangle2f rectD2S(java.awt.Rectangle r) {
        final float x1 = xD2S(r.x);
        final float y1 = yD2S(r.y);
        final float x2 = xD2S(r.x + r.width);
        final float y2 = yD2S(r.y + r.height);
        return new Rectangle2f(x1, y1, x2 - x1, y2 - y1);
    }

    java.awt.Rectangle rectS2D(Rectangle2f r) {
        final int x1 = xS2D(r.xMin());
        final int y1 = yS2D(r.yMin());
        final int x2 = xS2D(r.xMax());
        final int y2 = yS2D(r.yMax());
        return new java.awt.Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    float wD2S(int w) {
        return w / scale;
    }

    int wS2D(float w) {
        return Math.round(scale * w);
    }

    float xD2S(int x) {
        return x / scale;
    }

    int xS2D(float x) {
        return Math.round(scale * x);
    }

    float yD2S(int y) {
        return y / scale;
    }

    int yS2D(float y) {
        return Math.round(scale * y);
    }
}
