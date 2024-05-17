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
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.IntSupplier;

/**
 * Swing component for displaying instances of {@link Sheet}.
 */
@SuppressWarnings("serial")
public class SwingSheetView extends JPanel implements SheetView {

    private transient final SheetViewDelegate delegate;

    private final class SheetPane extends JScrollPane {
        public static final Rectangle2f EMPTY_RECTANGLE = Rectangle2f.of(0, 0, 0, 0);
        final SwingSegmentView topLeftQuadrant;
        final SwingSegmentView topRightQuadrant;
        final SwingSegmentView bottomLeftQuadrant;
        final SwingSegmentView bottomRightQuadrant;

        SheetPane() {
            // define row and column ranges and set up segments
            final IntSupplier startColumn = () -> 0;
            final IntSupplier splitColumn = () -> getSheet().map(Sheet::getSplitColumn).orElse(0);
            final IntSupplier endColumn = () -> getSheet().map(Sheet::getColumnCount).orElse(0);

            final IntSupplier startRow = () -> 0;
            final IntSupplier splitRow = () -> getSheet().map(Sheet::getSplitRow).orElse(0);
            final IntSupplier endRow = () -> getSheet().map(Sheet::getRowCount).orElse(0);

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
                java.awt.Rectangle r = new java.awt.Rectangle(delegate.xS2D(cellRect.x()), 1, delegate.wS2D(cellRect.width()), 1);
                bottomRightQuadrant.scrollRectToVisible(r);
            } else if (toLeftOfSplit) {
                // only scroll y
                java.awt.Rectangle r = new java.awt.Rectangle(1, delegate.yS2D(cellRect.y()), 1, delegate.hS2D(cellRect.height()));
                bottomRightQuadrant.scrollRectToVisible(r);
            } else {
                bottomRightQuadrant.scrollRectToVisible(rectS2D(cellRect));
            }
        }

        @Override
        public void validate() {
            getSheet().ifPresent( sheet -> {
                topLeftQuadrant.validate();
                topRightQuadrant.validate();
                bottomLeftQuadrant.validate();
                bottomRightQuadrant.validate();
            });

            super.validate();
        }

        private Rectangle2f getCellRectInViewCoordinates(Cell cell) {
            return getSheet().map( sheet -> {
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
            }).orElse(EMPTY_RECTANGLE);
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
                    final float y = delegate.yD2S(visibleRect.y);
                    final int yD = delegate.yS2D(y);
                    int i = sheetPainter.getRowNumberFromY(y);
                    int posD = yD;
                    while (i >= 0 && yD <= posD) {
                        posD = delegate.yS2D(sheetPainter.getRowPos(i--));
                    }
                    return yD - posD;
                } else {
                    // scroll down
                    final float y = delegate.yD2S(visibleRect.y + visibleRect.height);
                    final int yD = delegate.yS2D(y);
                    int i = sheetPainter.getRowNumberFromY(y);
                    int posD = yD;
                    while (i <= sheetPainter.getRowCount() && posD <= yD) {
                        posD = delegate.yS2D(sheetPainter.getRowPos(i++));
                    }
                    return posD - yD;
                }
            } else // scroll horizontal
            {
                if (direction < 0) {
                    // scroll left
                    final float x = delegate.xD2S(visibleRect.x);
                    final int xD = delegate.xS2D(x);
                    int j = sheetPainter.getColumnNumberFromX(x);
                    int posD = xD;
                    while (j >= 0 && xD <= posD) {
                        posD = delegate.xS2D(sheetPainter.getColumnPos(j--));
                    }
                    return xD - posD;
                } else {
                    // scroll right
                    final float x = delegate.xD2S(visibleRect.x + visibleRect.width);
                    int xD = delegate.xS2D(x);
                    int j = sheetPainter.getColumnNumberFromX(x);
                    int posD = xD;
                    while (j <= sheetPainter.getColumnCount() && posD <= xD) {
                        posD = delegate.xS2D(sheetPainter.getColumnPos(j++));
                    }
                    return posD - xD;
                }
            }
        }

        @Override
        public Sheet getSheet() {
            return SwingSheetView.this.getSheet().orElse(null);
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
            int w = delegate.wS2D(wd);
            int h = delegate.hS2D(hd);
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

            delegate.getSheet().ifPresent(sheet -> {
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
            });
        }

        int getXMinInViewCoordinates() {
            float x = sheetPainter.getColumnPos(getBeginColumn());
            if (hasRowHeaders()) {
                x -= sheetPainter.getRowLabelWidth();
            }
            return delegate.xS2D(x);
        }

        int getYMinInViewCoordinates() {
            float y = sheetPainter.getRowPos(getBeginRow());
            if (hasColumnHeaders()) {
                y -= sheetPainter.getColumnLabelHeight();
            }
            return delegate.yS2D(y);
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

    private final transient SwingSheetPainter sheetPainter;

    private final transient CellEditor editor = new DefaultCellEditor(this);

    private final transient SheetPane sheetPane = new SheetPane();

    private final transient SwingSearchDialog searchDialog = new SwingSearchDialog(this);

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
        this.delegate = new SheetViewDelegate(this);
        this.sheetPainter = new SwingSheetPainter(this, new DefaultCellRenderer());
        init(sheet);
    }

    /**
     * @return the sheetHeight
     */
    public int getSheetHeight() {
        return delegate.hS2D(sheetPainter.getSheetHeightInPoints());
    }

    public Dimension getSheetSize() {
        return new Dimension(getSheetWidth() + 1, getSheetHeight() + 1);
    }

    /**
     * @return the sheetWidth
     */
    public int getSheetWidth() {
        return delegate.wS2D(sheetPainter.getSheetWidthInPoints());
    }

    @Override
    public void removeNotify() {
        searchDialog.dispose();
        super.removeNotify();
    }

    @Override
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
        SwingUtilities.invokeLater(() -> getCurrentLogicalCell().ifPresent(sheetPane::ensureCellIsVisible));
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
        getCurrentCell().ifPresent(cell -> SwingUtil.setClipboardText(cell.getAsText(getLocale()).toString()));
    }

    private Optional<Cell> getCurrentCell() {
        return getSheet().flatMap(Sheet::getCurrentCell);
    }

    private Optional<Cell> getCurrentLogicalCell() {
        return getSheet().flatMap(Sheet::getCurrentCell).map(Cell::getLogicalCell);
    }

    private void init(Sheet sheet) {
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
        // listen to mouse events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e.getX() + delegate.xS2D(getSplitX()), e.getY() + delegate.yS2D(getSplitY()));
            }
        });
        // make focusable
        setFocusable(true);
        SwingUtilities.invokeLater(this::requestFocusInWindow);
        setSheet(sheet);
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

            delegate.setEditing(true);
        });
    }

    public void updateContent() {
        LOG.debug("updating content");

        getSheet().ifPresent(sheet -> {
            // scale according to screen resolution
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            float scaleDpi = dpi / 72.0f; // 1 point = 1/72 inch
            delegate.setScale(sheet.getZoom() * scaleDpi);

            SwingUtilities.invokeLater(() -> {
                sheetPainter.update(sheet);
                revalidate();
                repaint();
            });
        });
    }

    float getScale() {
        return delegate.getScale();
    }

    /**
     * Get x-coordinate of split.
     *
     * @return x coordinate of split
     */
    float getSplitX() {
        return getSheet()
                .map(sheet -> sheetPainter.getColumnPos(sheet.getSplitColumn()))
                .orElse(0f);
    }

    /**
     * Get y-coordinate of split.
     *
     * @return y coordinate of split
     */
    float getSplitY() {
        return getSheet()
                .map(sheet -> sheetPainter.getRowPos(sheet.getSplitRow()))
                .orElse(0f);
    }

    void onMousePressed(int x, int y) {
        // make the cell under pointer the current cell
        int row = sheetPainter.getRowNumberFromY(delegate.yD2S(y));
        int col = sheetPainter.getColumnNumberFromX(delegate.xD2S(x));
        boolean currentCellChanged = setCurrentCell(row, col);
        requestFocusInWindow();

        if (currentCellChanged) {
            // if cell changed, stop cell editing
            if (delegate.isEditing()) {
                stopEditing(true);
                delegate.setEditing(false);
            }
        } else {
            // otherwise start cell editing
            if (delegate.isEditable()) {
                startEditing();
                delegate.setEditing(true);
            }
        }
    }

    Rectangle2f rectD2S(java.awt.Rectangle r) {
        final float x1 = delegate.xD2S(r.x);
        final float y1 = delegate.yD2S(r.y);
        final float x2 = delegate.xD2S(r.x + r.width);
        final float y2 = delegate.yD2S(r.y + r.height);
        return new Rectangle2f(x1, y1, x2 - x1, y2 - y1);
    }

    java.awt.Rectangle rectS2D(Rectangle2f r) {
        final int x1 = delegate.xS2D(r.xMin());
        final int y1 = delegate.yS2D(r.yMin());
        final int x2 = delegate.xS2D(r.xMax());
        final int y2 = delegate.yS2D(r.yMax());
        return new java.awt.Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public SheetViewDelegate getDelegate() {
        return delegate;
    }
}
