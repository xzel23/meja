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

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.util.MejaHelper;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.Cache;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.locks.Lock;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Swing component for displaying instances of class {@link Sheet}.
 *
 * @author axel
 */
public class SheetView extends JPanel implements Scrollable {

    private static final long serialVersionUID = 1L;
    private final CellRenderer renderer = new DefaultCellRenderer();
    private final CellEditor editor = new DefaultCellEditor(this);

    Cache<Float, java.awt.Stroke> strokeCache = new Cache<Float, java.awt.Stroke>() {
        @Override
        protected java.awt.Stroke create(Float width) {
            return new BasicStroke(width);
        }
    };

    static final int MAX_WIDTH = 800;

    /**
     * The scale used to calculate screen sizes dependent of display resolution.
     */
    private float scaleDpi = 1;

    /**
     * Array with column positions (x-axis) in pixels.
     */
    private float columnPos[];

    /**
     * Array with column positions (y-axis) in pixels.
     */
    private float rowPos[];

    /**
     * Height of the sheet in points.
     */
    private float sheetWidthInPoints;

    /**
     * Width of the sheet in points.
     */
    private float sheetHeightInPoints;

    /**
     * The column number of the selected cell.
     */
    int currentColNum;

    /**
     * The row number of the selected cell.
     */
    int currentRowNum;

    /**
     * The sheet displayed.
     */
    private Sheet sheet;

    /**
     * The color to use for the grid lines.
     */
    private Color gridColor = Color.LIGHT_GRAY;

    /**
     * Flag indicating whether column headers should be shown when added to a
     * JScrollPane.
     */
    private boolean showColumnHeader = true;

    /**
     * Flag indicating whether row headers should be shown when added to a
     * JScrollPane.
     */
    private boolean showRowHeader = true;

    /**
     * Horizontal padding.
     */
    private final int paddingX = 2;

    /**
     * Vertical padding.
     */
    private final int paddingY = 1;

    /**
     * Color used to draw the selection rectangle.
     */
    private Color selectionColor = Color.BLACK;

    /**
     * Width of the selection rectangle borders.
     */
    private final int selectionStrokeWidth = 4;

    /**
     * Stroke used to draw the selection rectangle.
     */
    private Stroke selectionStroke = getStroke((float) selectionStrokeWidth);

    /**
     * Active clip bounds when drawing.
     */
    private final Rectangle clipBounds = new Rectangle();

    /**
     * Read-only mode.
     */
    private boolean editable = false;

    /**
     * Check whether editing is enabled.
     *
     * @return true if this SheetView allows editing.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Enable/disable sheet editing.
     *
     * @param editable true to allow editing
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Editing state.
     */
    private boolean editing = false;

    /**
     * Check editing state.
     *
     * @return true, if a cell is being edited.
     */
    public boolean isEditing() {
        return editing;
    }

    /**
     * Move the selection rectangle to an adjacent cell.
     *
     * @param d direction
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

        scrollToCurrentCell();
    }

    /**
     * Get display coordinates of selection rectangle.
     *
     * @return selection rectangle in display coordinates
     */
    private Rectangle getSelectionRect() {
        Rectangle cellRect = getCellRect(getCurrentCell().getLogicalCell());
        int extra = (selectionStrokeWidth + 1) / 2;
        cellRect.x -= extra;
        cellRect.y -= extra;
        cellRect.width += 2 * extra;
        cellRect.height += 2 * extra;
        return cellRect;
    }

    /**
     * Scroll the currently selected cell into view.
     */
    public void scrollToCurrentCell() {
        ensureCellIsVisibile(getCurrentCell().getLogicalCell());
    }

    /**
     * Scroll cell into view.
     *
     * @param cell the cell to scroll to
     */
    public void ensureCellIsVisibile(Cell cell) {
        scrollRectToVisible(getCellRect(cell));
    }

    /**
     * Calculate the rectangle the cell occupies on screen.
     *
     * @param cell the cell whose area is requested
     * @return rectangle the rectangle the cell takes up in screen coordinates
     */
    public Rectangle getCellRect(Cell cell) {
        final int i = cell.getRowNumber();
        final int j = cell.getColumnNumber();

        final int y = getRowPos(i);
        final int h = getRowPos(i + cell.getVerticalSpan()) - y + 1;
        final int x = getColumnPos(j);
        final int w = getColumnPos(cell.getColumnNumber() + cell.getHorizontalSpan()) - x + 1;

        return new Rectangle(x, y, w, h);
    }

    /**
     * Get the current row number.
     *
     * @return row number of the selected cell
     */
    public int getCurrentRowNum() {
        return currentRowNum;
    }

    /**
     * Set the current row number.
     *
     * @param rowNum number of row to be set
     */
    public void setCurrentRowNum(int rowNum) {
        setCurrent(rowNum, currentColNum);
    }

    /**
     * Get the current column number.
     *
     * @return column number of the selected cell
     */
    public int getCurrentColNum() {
        return currentColNum;
    }

    /**
     * Set the current column number.
     *
     * @param colNum number of column to be set
     */
    public void setCurrentColNum(int colNum) {
        setCurrent(currentRowNum, colNum);
    }

    /**
     * Set current row and column.
     *
     * @param rowNum number of row to be set
     * @param colNum number of column to be set
     * @return true if the current logical cell changed
     */
    public boolean setCurrent(int rowNum, int colNum) {
        Cell oldCell = getCurrentCell().getLogicalCell();
        Rectangle oldRect = getSelectionRect();

        currentRowNum = Math.max(sheet.getFirstRowNum(), Math.min(sheet.getLastRowNum(), rowNum));
        currentColNum = Math.max(sheet.getFirstColNum(), Math.min(sheet.getLastColNum(), colNum));

        Cell newCell = getCurrentCell().getLogicalCell();
        if (newCell.getRowNumber() != oldCell.getRowNumber()
                || newCell.getColumnNumber() != oldCell.getColumnNumber()) {
            // get new selection for repainting
            Rectangle newRect = getSelectionRect();
            repaint(oldRect);
            repaint(newRect);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Enter edit mode for the current cell.
     */
    private void startEditing() {
        if (!isEditable() || isEditing()) {
            return;
        }

        Cell cell = getCurrentCell().getLogicalCell();
        final JComponent editorComp = editor.startEditing(cell);
        editorComp.setBounds(getCellRect(cell));
        add(editorComp);
        editorComp.validate();
        editorComp.setVisible(true);
        editorComp.repaint();
        editing = true;
    }

    /**
     * End edit mode for the current cell.
     *
     * @param commit true if the content of the edited cell is to be updated
     */
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
    }

    float getScale() {
        return sheet == null ? 1.0f : sheet.getZoom() * scaleDpi;
    }

    /**
     * @return the columnPos
     */
    public int getColumnPos(int j) {
        return Math.round(columnPos[j] * getScale());
    }

    /**
     * @return the rowPos
     */
    public int getRowPos(int i) {
        return Math.round(rowPos[i] * getScale());
    }

    /**
     * @return the sheetWidth
     */
    public int getSheetWidth() {
        return Math.round(sheetWidthInPoints * getScale());
    }

    /**
     * @return the sheetHeight
     */
    public int getSheetHeight() {
        return Math.round(sheetHeightInPoints * getScale());
    }

    /**
     * Actions for key bindings.
     */
    static enum Actions {

        MOVE_UP {
                    @SuppressWarnings("serial")
                    @Override
                    public Action getAction(final SheetView view) {
                        return new AbstractAction("MOVE_UP") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                view.move(Direction.NORTH);
                            }
                        };
                    }
                },
        MOVE_DOWN {
                    @SuppressWarnings("serial")
                    @Override
                    public Action getAction(final SheetView view) {
                        return new AbstractAction("MOVE_DOWN") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                view.move(Direction.SOUTH);
                            }
                        };
                    }
                },
        MOVE_LEFT {
                    @SuppressWarnings("serial")
                    @Override
                    public Action getAction(final SheetView view) {
                        return new AbstractAction("MOVE_LEFT") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                view.move(Direction.WEST);
                            }
                        };
                    }
                },
        MOVE_RIGHT {
                    @SuppressWarnings("serial")
                    @Override
                    public Action getAction(final SheetView view) {
                        return new AbstractAction("MOVE_RIGHT") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                view.move(Direction.EAST);
                            }
                        };
                    }
                },
        START_EDITING {
                    @SuppressWarnings("serial")
                    @Override
                    public Action getAction(final SheetView view) {
                        return new AbstractAction("MOVE_RIGHT") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                view.startEditing();
                            }
                        };
                    }
                };

        abstract Action getAction(SheetView view);
    }

    /**
     * Constructor.
     *
     * No sheet is set.
     */
    public SheetView() {
        this(null);
    }

    /**
     * Construct a new SheetView for the given sheet.
     *
     * @param sheet the sheet to display
     */
    public SheetView(Sheet sheet) {
        // explicitly set a null layout since that is needed for absolute
        // positioning of the in-place cell editor
        super((LayoutManager) null);
        init();
        setSheet(sheet);
    }

    /**
     * Set sheet to display.
     *
     * @param sheet the sheet to display
     */
    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
        this.currentRowNum = 0;
        this.currentColNum = 0;
        update();
        revalidate();
    }

    public Sheet getSheet() {
        return sheet;
    }

    /**
     * Initialization method.
     *
     * <li>initialize the input map
     * <li>set up mouse handling
     * <li>make focusable
     */
    private void init() {
        setOpaque(true);

        // setup input map for keyboard navigation
        final InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_UP, 0), Actions.MOVE_UP);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_DOWN, 0), Actions.MOVE_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_LEFT, 0), Actions.MOVE_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_RIGHT, 0), Actions.MOVE_RIGHT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), Actions.START_EDITING);

        final ActionMap actionMap = getActionMap();
        for (Actions action : Actions.values()) {
            actionMap.put(action, action.getAction(this));
        }

        // listen to mouse events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e.getX(), e.getY());
            }
        });

        // make focusable
        setFocusable(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                requestFocusInWindow();
            }
        });
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return true;
    }

    void onMousePressed(int x, int y) {
        // make the cell under pointer the current cell
        int row = getRowNumberFromY(y);
        int col = getColumnNumberFromX(x);
        boolean currentCellChanged = setCurrent(row, col);
        requestFocusInWindow();

        if (!currentCellChanged) {
            // if it already was the current cell, start cell editing
            if (isEditable()) {
                startEditing();
                editing = true;
            }
        } else {
            // otherwise stop cell editing
            if (editing) {
                stopEditing(true);
                editing = false;
            }
        }
    }

    /**
     * Set the grid color.
     *
     * @param gridColor the color for th grid
     */
    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    /**
     * Get the grid color.
     *
     * @return color of grid
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Update sheet layout data.
     */
    private void update() {
        // scale according to screen resolution
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        scaleDpi = dpi / 72f;

        // determine sheet dimensions
        if (sheet == null) {
            sheetWidthInPoints = 0;
            sheetHeightInPoints = 0;
            rowPos = new float[]{0};
            columnPos = new float[]{0};
            // revalidate the layout
            revalidate();
            return;
        }

        Lock readLock = sheet.readLock();
        readLock.lock();
        try {
            sheetHeightInPoints = 0;
            rowPos = new float[2 + sheet.getLastRowNum()];
            rowPos[0] = 0;
            for (int i = 1; i < rowPos.length; i++) {
                sheetHeightInPoints += sheet.getRowHeight(i - 1);
                rowPos[i] = sheetHeightInPoints;
            }

            sheetWidthInPoints = 0;
            columnPos = new float[2 + sheet.getLastColNum()];
            columnPos[0] = 0;
            for (int j = 1; j < columnPos.length; j++) {
                sheetWidthInPoints += sheet.getColumnWidth(j - 1);
                columnPos[j] = sheetWidthInPoints;
            }

            // revalidate the layout
            revalidate();
        } finally {
            readLock.unlock();
        }

    }

    @Override
    public void addNotify() {
        super.addNotify();

        // Add custom headers to the JScrollPane the view is displayed in.
        Container parent = getParent();
        if (parent instanceof JViewport) {
            parent = parent.getParent();
            if (parent instanceof JScrollPane) {
                JScrollPane jsp = (JScrollPane) parent;
                if (showColumnHeader) {
                    jsp.setColumnHeaderView(new ColumnHeader());
                } else {
                    jsp.setColumnHeaderView(null);
                }
                if (showRowHeader) {
                    jsp.setRowHeaderView(new RowHeader());
                } else {
                    jsp.setRowHeaderView(null);
                }
            }
        }
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            // scroll vertical
            if (direction < 0) {
                //scroll up
                final int y = visibleRect.y;
                int yPrevious = 0;
                for (int i = sheet.getSplitRow(); i < rowPos.length; i++) {
                    if (getRowPos(i) >= y) {
                        return y - yPrevious;
                    }
                    yPrevious = getRowPos(i);
                }
                // should never be reached
                return 0;
            } else {
                // scroll down
                final int y = visibleRect.y + visibleRect.height;
                for (int i = 0; i < rowPos.length; i++) {
                    if (getRowPos(i) > y) {
                        return getRowPos(i) - y;
                    }
                }
                // should never be reached
                return 0;
            }
        } else {
            // scroll horizontal
            if (direction < 0) {
                //scroll left
                final int x = visibleRect.x;
                int xPrevious = 0;
                for (int j = 0; j < columnPos.length; j++) {
                    if (getColumnPos(j) >= x) {
                        return x - xPrevious;
                    }
                    xPrevious = getColumnPos(j);
                }
                // should never be reached
                return 0;
            } else {
                // scroll down
                final int x = visibleRect.x + visibleRect.width;
                for (int j = 0; j < columnPos.length; j++) {
                    if (getColumnPos(j) > x) {
                        return getColumnPos(j) - x;
                    }
                }
                // should never be reached
                return 0;
            }
        }
    }

    /**
     * Get y-coordinate of split.
     *
     * @return y coordinate of split
     */
    int getSplitY() {
        return getRowPos(sheet.getSplitRow());
    }

    /**
     * Get the row number that the given y-coordinate belongs to.
     *
     * @param y y-coordinate
     *
     * @return
     * <ul>
     * <li> -1, if the first row is displayed below the given coordinate
     * <li> number of rows, if the lower edge of the last row is displayed above
     * the given coordinate
     * <li> the number of the row that belongs to the given coordinate
     * </ul>
     */
    public int getRowNumberFromY(int y) {
        int i = 0;
        while (i < rowPos.length && getRowPos(i) <= y) {
            i++;
        }
        return i - 1;
    }

    /**
     * Get the column number that the given x-coordinate belongs to.
     *
     * @param x x-coordinate
     *
     * @return
     * <ul>
     * <li> -1, if the first column is displayed to the right of the given
     * coordinate
     * <li> number of columns, if the right edge of the last column is displayed
     * to the left of the given coordinate
     * <li> the number of the column that belongs to the given coordinate
     * </ul>
     */
    public int getColumnNumberFromX(int x) {
        int j = 0;
        while (j < columnPos.length && getColumnPos(j) <= x) {
            j++;
        }
        return j - 1;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 3 * getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getSheetWidth() + 1, getSheetHeight() - getSplitY() + 1);
    }

    public Dimension getSheetSize() {
        return new Dimension(getSheetWidth() + 1, getSheetHeight() + 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (sheet == null) {
            return;
        }

        Dimension sheetSize = getSheetSize();
        drawSheet(g.create(0, -getSplitY(), sheetSize.width, sheetSize.height));
    }

    private void drawSheet(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        Lock readLock = sheet.readLock();
        readLock.lock();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g2d.getClipBounds(clipBounds);

            g2d.setBackground(sheet.getWorkbook().getDefaultCellStyle().getFillBgColor());
            g2d.clearRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

            drawCells(g2d, CellDrawMode.DRAW_CELL_BACKGROUND);
            drawCells(g2d, CellDrawMode.DRAW_CELL_BORDER);
            drawCells(g2d, CellDrawMode.DRAW_CELL_FOREGROUND);
            drawSelection(g2d);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Draw cells.
     *
     * Since borders can be draw over by the background of adjacent cells and
     * text can overlap, drawing is done in three steps:
     * <ul>
     * <li> draw background for <em>all</em> cells
     * <li> draw borders for <em>all</em> cells
     * <li> draw foreground <em>all</em> cells
     * </ul>
     * This is controlled by {@code cellDrawMode}.
     *
     * @param g the graphics object to use
     * @param cellDrawMode the draw mode to use
     */
    void drawCells(Graphics2D g, CellDrawMode cellDrawMode) {
        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        int maxWidthScaled = (int) (MAX_WIDTH * getScale());

        // determine visible rows and columns
        int startRow = Math.max(0, getRowNumberFromY(clipBounds.y));
        int endRow = Math.min(getNumberOfRows(), 1 + getRowNumberFromY(clipBounds.y + clipBounds.height));
        int startColumn = Math.max(0, getColumnNumberFromX(clipBounds.x));
        int endColumn = Math.min(getNumberOfColumns(), 1 + getColumnNumberFromX(clipBounds.x + clipBounds.width));

        // Collect cells to be drawn
        for (int i = startRow; i < endRow; i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            // if first/last displayed cell of row is empty, start drawing at
            // the first non-empty cell to the left/right to make sure
            // overflowing text is visible.
            int first = startColumn;
            while (first > 0 && getColumnPos(first) + maxWidthScaled > clipBounds.x && row.getCell(first).isEmpty()) {
                first--;
            }

            int end = endColumn;
            while (end < getNumberOfColumns() && getColumnPos(end) - maxWidthScaled < clipBounds.x + clipBounds.width && row.getCell(end - 1).isEmpty()) {
                end++;
            }

            for (int j = first; j < end; j++) {
                Cell cell = row.getCell(j);
                Cell logicalCell = cell.getLogicalCell();

                final boolean visible;
                if (cell == logicalCell) {
                    // if cell is not merged or the topleft cell of the
                    // merged region, then it is visible
                    visible = true;
                } else {
                    // otherwise calculate row and column numbers of the
                    // first visible cell of the merged region
                    int iCell = Math.max(startRow, logicalCell.getRowNumber());
                    int jCell = Math.max(first, logicalCell.getColumnNumber());
                    visible = i == iCell && j == jCell;
                    // skip the other cells of this row that belong to the same merged region
                    j = logicalCell.getColumnNumber() + logicalCell.getHorizontalSpan() - 1;
                    // filter out cells that cannot overflow into the visible region
                    if (j < startColumn && isWrapping(cell.getCellStyle())) {
                        continue;
                    }
                }

                // draw cell
                if (visible) {
                    switch (cellDrawMode) {
                        case DRAW_CELL_BACKGROUND:
                            drawCellBackground(g, logicalCell);
                            break;
                        case DRAW_CELL_BORDER:
                            drawCellBorder(g, logicalCell);
                            break;
                        case DRAW_CELL_FOREGROUND:
                            drawCellForeground(g, logicalCell);
                            break;
                    }
                }

            }
        }
    }

    /**
     * Draw cell background.
     *
     * @param g the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellBackground(Graphics2D g, Cell cell) {
        Rectangle cr = getCellRect(cell);

        // draw grid lines
        g.setColor(gridColor);
        g.drawRect(cr.x, cr.y, cr.width - 1, cr.height - 1);

        CellStyle style = cell.getCellStyle();
        FillPattern pattern = style.getFillPattern();

        if (pattern == FillPattern.NONE) {
            return;
        }

        if (pattern != FillPattern.SOLID) {
            Color fillBgColor = style.getFillBgColor();
            if (fillBgColor != null) {
                g.setColor(fillBgColor);
                g.fillRect(cr.x, cr.y, cr.width, cr.height);
            }
        }

        if (pattern != FillPattern.NONE) {
            Color fillFgColor = style.getFillFgColor();
            if (fillFgColor != null) {
                g.setColor(fillFgColor);
                g.fillRect(cr.x, cr.y, cr.width, cr.height);
            }
        }
    }

    /**
     * Draw cell border.
     *
     * @param g the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellBorder(Graphics2D g, Cell cell) {
        CellStyle styleTopLeft = cell.getCellStyle();

        Cell cellBottomRight = sheet.getRow(cell.getRowNumber() + cell.getVerticalSpan() - 1).getCell(cell.getColumnNumber() + cell.getHorizontalSpan() - 1);
        CellStyle styleBottomRight = cellBottomRight.getCellStyle();

        Rectangle cr = getCellRect(cell);

        // draw border
        for (Direction d : Direction.values()) {
            boolean isTopLeft = d == Direction.NORTH || d == Direction.WEST;
            CellStyle style = isTopLeft ? styleTopLeft : styleBottomRight;

            BorderStyle b = style.getBorderStyle(d);
            if (b.getWidth() == 0) {
                continue;
            }

            Color color = b.getColor();
            if (color == null) {
                color = Color.BLACK;
            }

            g.setColor(color);
            g.setStroke(getStroke(b.getWidth() * getScale()));
            switch (d) {
                case NORTH:
                    g.drawLine(cr.x, cr.y, cr.x + cr.width - 1, cr.y);
                    break;
                case EAST:
                    g.drawLine(cr.x + cr.width - 1, cr.y, cr.x + cr.width - 1, cr.y + cr.height - 1);
                    break;
                case SOUTH:
                    g.drawLine(cr.x, cr.y + cr.height - 1, cr.x + cr.width - 1, cr.y + cr.height - 1);
                    break;
                case WEST:
                    g.drawLine(cr.x, cr.y, cr.x, cr.y + cr.height - 1);
                    break;
            }
        }
    }

    /**
     * Draw cell foreground.
     *
     * @param g the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellForeground(Graphics2D g, Cell cell) {
        if (cell.isEmpty()) {
            return;
        }

        // the cell rectangle, used for positioning the text
        Rectangle cellRect = getCellRect(cell);
        cellRect.x += paddingX;
        cellRect.width -= 2 * paddingX;
        cellRect.y += paddingY;
        cellRect.height -= 2 * paddingY;

        // the clipping rectangle
        final Rectangle clipRect;
        final CellStyle style = cell.getCellStyle();
        if (isWrapping(style)) {
            clipRect = cellRect;
        } else {
            Row row = cell.getRow();
            int clipXMin = cellRect.x;
            for (int j = cell.getColumnNumber() - 1; j > 0; j--) {
                if (!row.getCell(j).isEmpty()) {
                    break;
                }
                clipXMin = getColumnPos(j) + paddingX;
            }
            int clipXMax = cellRect.x + cellRect.width;
            for (int j = cell.getColumnNumber() + 1; j < getNumberOfColumns(); j++) {
                if (!row.getCell(j).isEmpty()) {
                    break;
                }
                clipXMax = getColumnPos(j + 1) - paddingX;
            }
            clipRect = new Rectangle(clipXMin, cellRect.y, clipXMax - clipXMin, cellRect.height);
        }

        renderer.render(g, cell, cellRect, clipRect, getScale());
    }

    /**
     * Test whether style uses text wrapping. While there is a property for text
     * wrapping, the alignment settings have to be taken into account too.
     *
     * @param style style
     * @return true if cell content should be displayed with text wrapping
     */
    static boolean isWrapping(CellStyle style) {
        return style.isWrap() || style.getHAlign().isWrap() || style.getVAlign().isWrap();
    }

    /**
     * Get number of columns for the currently loaded sheet.
     *
     * @return number of columns
     */
    private int getNumberOfColumns() {
        return columnPos.length - 1;
    }

    /**
     * Get number of rows for the currently loaded sheet.
     *
     * @return number of rows
     */
    private int getNumberOfRows() {
        return rowPos.length - 1;
    }

    private java.awt.Stroke getStroke(Float width) {
        return strokeCache.get(width);
    }

    /**
     * Get column name.
     *
     * @param j the column number
     * @return name of column
     */
    public String getColumnName(int j) {
        return MejaHelper.getColumnName(j);
    }

    /**
     * Get row name.
     *
     * @param i the row number
     * @return name of row
     */
    public String getRowName(int i) {
        return Integer.toString(i + 1);
    }

    /**
     * Return the current cell.
     *
     * @return current cell
     */
    private Cell getCurrentCell() {
        return sheet.getRow(currentRowNum).getCell(currentColNum);
    }

    /**
     * Draw frame around current selection.
     *
     * @param g2d graphics object used for drawing
     */
    private void drawSelection(Graphics2D g2d) {
        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        Cell logicalCell = getCurrentCell().getLogicalCell();

        int rowNum = logicalCell.getRowNumber();
        int colNum = logicalCell.getColumnNumber();
        int spanX = logicalCell.getHorizontalSpan();
        int spanY = logicalCell.getVerticalSpan();

        int x = getColumnPos(colNum);
        int y = getRowPos(rowNum);
        int w = getColumnPos(colNum + spanX) - x;
        int h = getRowPos(rowNum + spanY) - y;

        g2d.setColor(selectionColor);
        g2d.setStroke(selectionStroke);
        g2d.drawRect(x, y, w, h);
    }

    protected static enum CellDrawMode {

        DRAW_CELL_BACKGROUND, DRAW_CELL_BORDER, DRAW_CELL_FOREGROUND
    }

    @SuppressWarnings("serial")
    private class ColumnHeader extends JComponent {

        private final JLabel painter;
        private int labelHeight = 0;

        public ColumnHeader() {
            painter = new JLabel();
            painter.setHorizontalAlignment(SwingConstants.CENTER);
            painter.setVerticalAlignment(SwingConstants.CENTER);
            painter.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, gridColor));

            validate();
        }

        @Override
        public void validate() {
            // determine height of labels (assuming noe letter is higher than 'A')
            painter.setText("A");
            labelHeight = painter.getPreferredSize().height;

            // width is the width of the worksheet in pixels
            int width = SheetView.this.getPreferredSize().width;

            // the height is the height for the labels showing column names ...
            int height = labelHeight;

            // ... plus the height of the rows above the split line ...
            height += getRowPos(sheet.getSplitRow());

            // ... plus 1 pixel for drawing a line below the lines above the split.
            height += 1;

            setPreferredSize(new Dimension(width, height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            // draw column labels
            Rectangle clipBounds = g.getClipBounds();
            int startCol = Math.max(0, getColumnNumberFromX(clipBounds.x));
            int endCol = Math.min(1 + getColumnNumberFromX(clipBounds.x + clipBounds.width), getNumberOfColumns());
            for (int j = startCol; j < endCol; j++) {
                int x = getColumnPos(j) + 1;
                int w = getColumnPos(j + 1) - x;
                String text = getColumnName(j);

                painter.setBounds(0, 0, w, labelHeight);
                painter.setText(text);
                painter.paint(g.create(x, 0, w, labelHeight));
            }

            // draw rows above split
            drawSheet(g.create(0, labelHeight, getWidth(), getHeight() - labelHeight));
        }

    }

    @SuppressWarnings("serial")
    private class RowHeader extends JComponent {

        private final JLabel painter;

        public RowHeader() {
            painter = new JLabel();
            painter.setHorizontalAlignment(SwingConstants.RIGHT);
            painter.setVerticalAlignment(SwingConstants.CENTER);
            painter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, gridColor));

            validate();
        }

        @Override
        public void validate() {
            // create a string with the maximum number of digits needed to
            // represent the highest row number (use a string only consisting
            // of zeroes instead of the last row number because a proportional
            // font might be used)
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= getNumberOfRows(); i *= 10) {
                sb.append('0');
            }
            painter.setText(sb.toString());
            setPreferredSize(new Dimension(painter.getPreferredSize().width, SheetView.this.getPreferredSize().height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();

            Rectangle clipBounds = g.getClipBounds();
            int startRow = Math.max(0, getRowNumberFromY(clipBounds.y));
            int endRow = Math.min(1 + getRowNumberFromY(clipBounds.y + clipBounds.height), getNumberOfRows());
            for (int i = startRow; i < endRow; i++) {
                int y = getRowPos(i) + 1;
                int h = getRowPos(i + 1) - y;
                String text = getRowName(i);

                painter.setBounds(0, 0, w, h);
                painter.setText(text);
                painter.paint(g.create(0, y, w, h));
            }
        }
    }

    public void updateContent() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                update();
                repaint();
            }
        });
    }
}
