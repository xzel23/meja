/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.Cache;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Swing component for displaying sheets.
 *
 * @author axel
 */
public class SheetView extends JPanel implements Scrollable {

    private static final long serialVersionUID = 1L;
    private final CellRenderer renderer = new DefaultCellRenderer();

    Cache<Float, java.awt.Stroke> strokeCache = new Cache<Float, java.awt.Stroke>() {
        @Override
        protected java.awt.Stroke create(Float width) {
            return new BasicStroke(width);
        }
    };

    /**
     * The scale used to calculate screen sizes dependent of display resolution.
     */
    float scale = 1;

    /**
     * Array with column positions (x-axis) in pixels.
     */
    int columnPos[];

    /**
     * Array with column positions (y-axis) in pixels.
     */
    int rowPos[];

    /**
     * Height of the sheet in pixels.
     */
    int sheetWidth;

    /**
     * Width of the sheet in pixels.
     */
    int sheetHeight;

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
     * @param cell
     * @return rectangle
     */
    public Rectangle getCellRect(Cell cell) {
        final int i = cell.getRowNumber();
        final int j = cell.getColumnNumber();

        final int y = rowPos[i];
        final int h = rowPos[i + cell.getVerticalSpan()] - y + 1;
        final int x = columnPos[j];
        final int w = columnPos[cell.getColumnNumber() + cell.getHorizontalSpan()] - x + 1;

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
     */
    public void setCurrent(int rowNum, int colNum) {
        int oldRowNum = currentRowNum;
        int newRowNum = Math.max(sheet.getFirstRowNum(), Math.min(sheet.getLastRowNum(), rowNum));
        int oldColNum = currentColNum;
        int newColNum = Math.max(sheet.getFirstColNum(), Math.min(sheet.getLastColNum(), colNum));
        if (newRowNum != oldRowNum || newColNum != oldColNum) {
            // get old selection for repainting
            Rectangle oldRect = getSelectionRect();
            // update current position
            currentRowNum = newRowNum;
            currentColNum = newColNum;
            // get new selection for repainting
            Rectangle newRect = getSelectionRect();
            repaint(oldRect);
            repaint(newRect);
        }
    }

    /**
     * Actions for key bindings.
     */
    static enum Actions {

        MOVE_UP {
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
                    @Override
                    public Action getAction(final SheetView view) {
                        return new AbstractAction("MOVE_RIGHT") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                view.move(Direction.EAST);
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
    }

    /**
     * Initialization method.
     *
     * <li>initialize the input map
     * <li>set up mouse handling
     * <li>make focusable
     */
    private void init() {
        // setup input map for keyboard navigation
        final InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), Actions.MOVE_UP);
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_UP, 0), Actions.MOVE_UP);
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), Actions.MOVE_DOWN);
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_DOWN, 0), Actions.MOVE_DOWN);
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), Actions.MOVE_LEFT);
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_LEFT, 0), Actions.MOVE_LEFT);
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), Actions.MOVE_RIGHT);
        getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_KP_RIGHT, 0), Actions.MOVE_RIGHT);

        final ActionMap actionMap = getActionMap();
        for (Actions action : Actions.values()) {
            actionMap.put(action, action.getAction(this));
        }

        // listen to mouse events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = getRowNumberFromY(e.getY());
                int col = getColumnNumberFromX(e.getX());
                setCurrent(row, col);
                requestFocusInWindow();
            }
        });

        // make focusable
        setFocusable(true);
        requestFocusInWindow();
    }

    /**
     * Set the grid color.
     *
     * @param gridColor
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
        scale = dpi / 72f;

        // determine sheet dimensions
        if (sheet == null) {
            sheetWidth = 0;
            sheetHeight = 0;
            rowPos = new int[]{0};
            columnPos = new int[]{0};
            return;
        } else {
            sheetHeight = 0;
            rowPos = new int[2 + sheet.getLastRowNum()];
            rowPos[0] = 0;
            for (int i = 1; i < rowPos.length; i++) {
                sheetHeight += Math.round(sheet.getRowHeight(i - 1) * scale);
                rowPos[i] = sheetHeight;
            }

            sheetWidth = 0;
            columnPos = new int[2 + sheet.getLastColNum()];
            columnPos[0] = 0;
            for (int j = 1; j < columnPos.length; j++) {
                sheetWidth += Math.round(sheet.getColumnWidth(j - 1) * scale);
                columnPos[j] = sheetWidth;
            }
        }

        // set headers
        addHeadersAsNeeded();

        // listen to Ancestorevents to add headers when view is added to a JScrollPane
        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                addHeadersAsNeeded();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // nop
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // nop
            }
        });

        // revalidate the layout
        revalidate();
    }

    /**
     * Add custom headers to the JScrollPane the view is displayed in.
     */
    private void addHeadersAsNeeded() {
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
                for (int i = 0; i < rowPos.length; i++) {
                    if (rowPos[i] >= y) {
                        return y - yPrevious;
                    }
                    yPrevious = rowPos[i];
                }
                // should never be reached
                return 0;
            } else {
                // scroll down
                final int y = visibleRect.y + visibleRect.height;
                for (int i = 0; i < rowPos.length; i++) {
                    if (rowPos[i] > y) {
                        return rowPos[i] - y;
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
                    if (columnPos[j] >= x) {
                        return x - xPrevious;
                    }
                    xPrevious = columnPos[j];
                }
                // should never be reached
                return 0;
            } else {
                // scroll down
                final int x = visibleRect.x + visibleRect.width;
                for (int j = 0; j < columnPos.length; j++) {
                    if (columnPos[j] > x) {
                        return columnPos[j] - x;
                    }
                }
                // should never be reached
                return 0;
            }
        }
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
        while (i < rowPos.length && rowPos[i] <= y) {
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
        while (j < columnPos.length && columnPos[j] <= x) {
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
        return new Dimension(sheetWidth + 1, sheetHeight + 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.getClipBounds(clipBounds);

        g2d.clearRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

        // TODO keep?
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        drawGrid(g2d);
        drawCells(g2d, CellDrawMode.DRAW_CELL_BACKGROUND);
        drawCells(g2d, CellDrawMode.DRAW_CELL_BORDER);
        drawCells(g2d, CellDrawMode.DRAW_CELL_FOREGROUND);
        drawSelection(g2d);
    }

    /**
     * Draw the grid.
     *
     * @param g
     */
    private void drawGrid(Graphics2D g) {
        g.setColor(gridColor);

        final int minY = clipBounds.y;
        final int maxY = clipBounds.y + clipBounds.height;
        final int minX = clipBounds.x;
        final int maxX = clipBounds.x + clipBounds.width;

        // draw horizontal grid lines
        for (int gridY : rowPos) {
            if (gridY < minY) {
                // visible region not reached
                continue;
            }
            if (gridY > maxY) {
                // out of visible region
                break;
            }
            g.drawLine(minX, gridY, maxX, gridY);
        }

        // draw vertical grid lines
        for (int gridX : columnPos) {
            if (gridX < minX) {
                // visible region not reached
                continue;
            }
            if (gridX > maxX) {
                // out of visible region
                break;
            }
            g.drawLine(gridX, minY, gridX, maxY);
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

        // Since text can overflow into other cells, add a margin (in pixels)
        // for cells to be drawn that normally aren't visible when drawing
        // foreground.
        int extraX = cellDrawMode == CellDrawMode.DRAW_CELL_FOREGROUND ? 800 : 0;

        // determine visible rows and columns
        int startRow = Math.max(0, getRowNumberFromY(clipBounds.y));
        int endRow = Math.min(getNumberOfRows(), 1 + getRowNumberFromY(clipBounds.y + clipBounds.height));
        int startColumn = Math.max(0, getColumnNumberFromX(clipBounds.x - extraX));
        int endColumn = Math.min(getNumberOfColumns(), 1 + getColumnNumberFromX(clipBounds.x + clipBounds.width + extraX));

        // Collect cells to be drawn
        for (int i = startRow; i < endRow; i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            for (int j = startColumn; j < endColumn; j++) {
                Cell cell = row.getCell(j);

                if (cell != null) {
                    Cell logicalCell = cell.getLogicalCell();

                    final boolean visible;
                    if (cell == logicalCell) {
                        // if cell is not merged or the topleft cell of the
                        // merged region, then it is visible
                        visible = true;
                    } else {
                        // otherwise calculate row and column numbers of the
                        // first visible cell of the merged region
                        int iCell = Math.max(i, logicalCell.getRowNumber());
                        int jCell = Math.max(j, logicalCell.getColumnNumber());
                        visible = i == iCell && j == jCell;
                        // skip the other cells of this row that belong to the same merged region
                        j = logicalCell.getColumnNumber() + logicalCell.getHorizontalSpan() - 1;
                        // filter out cells that cannot overflow into the visible region
                        if (j < startColumn && cell.getCellStyle().isWrap()) {
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
    }

    /**
     * Draw cell background.
     *
     * @param g the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellBackground(Graphics2D g, Cell cell) {
        CellStyle style = cell.getCellStyle();
        FillPattern pattern = style.getFillPattern();

        if (pattern == FillPattern.NONE) {
            return;
        }

        Rectangle cr = getCellRect(cell);

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
        CellStyle style = cell.getCellStyle();
        Rectangle cr = getCellRect(cell);

        // draw border
        for (Direction d : Direction.values()) {
            BorderStyle b = style.getBorderStyle(d);
            if (b.getWidth() == 0) {
                continue;
            }

            Color color = b.getColor();
            if (color == null) {
                color = Color.BLACK;
            }

            g.setColor(color);
            g.setStroke(getStroke(b.getWidth() * scale));
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
        if (cell.getCellType() == CellType.BLANK) {
            return;
        }

        AttributedString text = cell.getAttributedString();
        if (isEmpty(text)) {
            return;
        }

        Rectangle cr = getCellRect(cell);
        cr.x += paddingX;
        cr.width -= 2 * paddingX - 1;
        cr.y += paddingY;
        cr.height -= 2 * paddingY;

        renderer.render(g, cell, cr.x,cr.y,cr.width,cr.height, scale);
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

    private boolean isEmpty(AttributedString text) {
        AttributedCharacterIterator iterator = text.getIterator();
        return iterator.getBeginIndex() == iterator.getEndIndex();
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
        StringBuilder sb = new StringBuilder();
        do {
            sb.append((char) ('A' + (j % 26)));
            j /= 26;
        } while (j > 0);
        return sb.toString();
    }

    /**
     * Get row name.
     *
     * @param i the row number
     * @return name of row
     */
    public String getRowName(int i) {
        return Integer.toString(i);
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

        int x = columnPos[colNum];
        int y = rowPos[rowNum];
        int w = columnPos[colNum + spanX] - x;
        int h = rowPos[rowNum + spanY] - y;

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

        public ColumnHeader() {
            painter = new JLabel("A");
            painter.setHorizontalAlignment(SwingConstants.CENTER);
            painter.setVerticalAlignment(SwingConstants.CENTER);
            painter.setBorder(BorderFactory.createRaisedBevelBorder());
            setPreferredSize(new Dimension(SheetView.this.getPreferredSize().width, painter.getPreferredSize().height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int h = getHeight();

            Rectangle clipBounds = g.getClipBounds();
            int startCol = Math.max(0, getColumnNumberFromX(clipBounds.x));
            int endCol = Math.min(1 + getColumnNumberFromX(clipBounds.x + clipBounds.width), getNumberOfColumns());
            for (int j = startCol; j < endCol; j++) {
                int x = columnPos[j] + 1;
                int w = columnPos[j + 1] - x;
                String text = getColumnName(j);

                painter.setBounds(0, 0, w, h);
                painter.setText(text);
                painter.paint(g.create(x, 0, w, h));
            }
        }

    }

    @SuppressWarnings("serial")
    private class RowHeader extends JComponent {

        private final JLabel painter;

        public RowHeader() {
            // create a string with the maximum number of digits needed to
            // represent the highest row number (use a string only consisting
            // of zeroes instead of the last row number because a proportional
            // font might be used)
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= getNumberOfRows(); i *= 10) {
                sb.append('0');

            }
            painter = new JLabel(sb.toString());
            painter.setHorizontalAlignment(SwingConstants.RIGHT);
            painter.setVerticalAlignment(SwingConstants.CENTER);
            painter.setBorder(BorderFactory.createRaisedBevelBorder());
            setPreferredSize(new Dimension(painter.getPreferredSize().width, SheetView.this.getPreferredSize().height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();

            Rectangle clipBounds = g.getClipBounds();
            int startRow = Math.max(0, getRowNumberFromY(clipBounds.y));
            int endRow = Math.min(1 + getRowNumberFromY(clipBounds.y + clipBounds.height), getNumberOfRows());
            for (int i = startRow; i < endRow; i++) {
                int y = rowPos[i] + 1;
                int h = rowPos[i + 1] - y;
                String text = getRowName(i);

                painter.setBounds(0, 0, w, h);
                painter.setText(text);
                painter.paint(g.create(0, y, w, h));
            }
        }

    }
}
