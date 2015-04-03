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
import com.dua3.meja.model.Font;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.Cache;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 *
 * @author axel
 */
public class SheetView extends JPanel implements Scrollable {

    Cache<Float, java.awt.Stroke> strokeCache = new Cache<Float, java.awt.Stroke>() {
        @Override
        protected java.awt.Stroke create(Float width) {
            return new BasicStroke(width);
        }
    };

    Cache<Font, java.awt.Font> fontCache = new Cache<Font, java.awt.Font>() {
        @Override
        protected java.awt.Font create(Font font) {
            int style = (font.isBold() ? java.awt.Font.BOLD : 0) | (font.isItalic() ? java.awt.Font.ITALIC : 0);
            return new java.awt.Font(font.getFamily(), style, (int) Math.round(font.getSizeInPoints()));
        }
    };

    double scale = 1;

    int columnPos[];
    int rowPos[];
    int sheetWidth;
    int sheetHeight;
    int currentColNum;
    int currentRowNum;

    private Sheet sheet;
    private Color gridColor = Color.LIGHT_GRAY;
    private Color selectionColor = Color.BLACK;

    private final int selectionStrokeWidth=4;
    private Stroke selectionStroke = getStroke((float)selectionStrokeWidth);

    private final Rectangle clipBounds = new Rectangle();

    private void move(Direction d) {
        Cell cell = getCurrentCell().getLogicalCell();

        switch (d) {
            case NORTH:
                setCurrentRowNum(cell.getRowNumber()-1);
                break;
            case SOUTH:
                setCurrentRowNum(cell.getRowNumber()+cell.getVerticalSpan());
                break;
            case WEST:
                setCurrentColNum(cell.getColumnNumber()-1);
                break;
            case EAST:
                setCurrentColNum(cell.getColumnNumber()+cell.getHorizontalSpan());
                break;
        }

        scrollToCurrentCell();
    }

    private Rectangle getSelectionRect() {
        Rectangle cellRect = getCellRect(getCurrentCell().getLogicalCell());
        int extra = (selectionStrokeWidth+1)/2;
        cellRect.x-=extra;
        cellRect.y-=extra;
        cellRect.width+=2*extra;
        cellRect.height+=2*extra;
        return cellRect;
    }

    public void scrollToCurrentCell() {
        ensureCellIsVisibile(getCurrentCell());
    }

    public void ensureCellIsVisibile(Cell cell) {
        scrollRectToVisible(getCellRect(cell));
    }

    public Rectangle getCellRect(Cell cell) {
        final int i = cell.getRowNumber();
        final int j = cell.getColumnNumber();

        final int y = rowPos[i];
        final int h = rowPos[i + cell.getVerticalSpan()] - y;
        final int x = columnPos[j];
        final int w = columnPos[cell.getColumnNumber() + cell.getHorizontalSpan()] - x;

        return new Rectangle(x, y, w, h);
    }

    public int getCurrentRowNum() {
        return currentRowNum;
    }

    public void setCurrentRowNum(int rowNum) {
        int oldRowNum = currentRowNum;
        int newRowNum = Math.max(sheet.getFirstRowNum(), Math.min(sheet.getLastRowNum(), rowNum));
        if (newRowNum!=oldRowNum) {
            // get old selection for repainting
            Rectangle oldRect = getSelectionRect();
            // update current position
            currentRowNum = newRowNum;
            // get new selection for repainting
            Rectangle newRect = getSelectionRect();
            repaint(oldRect);
            repaint(newRect);
        }
    }

    public int getCurrentColNum() {
        return currentColNum;
    }

    public void setCurrentColNum(int colNum) {
        int oldColNum = currentColNum;
        int newColNum = Math.max(sheet.getFirstColNum(), Math.min(sheet.getLastColNum(), colNum));
        if (newColNum!=oldColNum) {
            // get old selection for repainting
            Rectangle oldRect = getSelectionRect();
            // update current position
            currentColNum = newColNum;
            // get new selection for repainting
            Rectangle newRect = getSelectionRect();
            repaint(oldRect);
            repaint(newRect);
        }
    }

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

    public SheetView() {
        this(null);
    }

    public SheetView(Sheet sheet) {
        init();
        setSheet(sheet);
    }

    public void setSheet(Sheet sheet1) {
        this.sheet = sheet1;
        this.currentRowNum = 0;
        this.currentColNum = 0;
        update();
    }

    private void init() {
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
        for (Actions action: Actions.values()) {
            actionMap.put(action, action.getAction(this));
        }

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
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        //scale = dpi / 72.0;

        if (sheet==null) {
            sheetWidth = 0;
            sheetHeight = 0;
            rowPos=new int[] { 0 };
            columnPos=new int[] { 0 };
            return;
        }

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
        g.getClipBounds(clipBounds);

        g.clearRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        drawGrid(g2d);

        Collection<Cell> cells = determineCellsToDraw(g);
        drawCells(g2d, cells, CellDrawMode.DRAW_CELL_BACKGROUND);
        drawCells(g2d, cells, CellDrawMode.DRAW_CELL_BORDER);
        drawCells(g2d, cells, CellDrawMode.DRAW_CELL_FOREGROUND);
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
        final int maxY = clipBounds.y+clipBounds.height;
        final int minX = clipBounds.x;
        final int maxX = clipBounds.x+clipBounds.width;

        // draw horizontal grid lines
        for (int gridY: rowPos) {
            if (gridY<minY) {
                // visible region not reached
                continue;
            }
            if (gridY>maxY) {
                // out of visible region
                break;
            }
            g.drawLine(minX, gridY, maxX, gridY);
        }

        // draw vertical grid lines
        for (int gridX: columnPos) {
            if (gridX<minX) {
                // visible region not reached
                continue;
            }
            if (gridX>maxX) {
                // out of visible region
                break;
            }
            g.drawLine(gridX, minY, gridX, maxY);
        }
    }

    Collection<Cell> determineCellsToDraw(Graphics g) {
        // no sheet, no drawing
        if (sheet==null) {
            return Collections.emptyList();
        }

        // determine visible rows and columns
        int startRow = Math.max(0, getRowNumberFromY(clipBounds.y));
        int endRow = Math.min(getNumberOfRows(), 1 + getRowNumberFromY(clipBounds.y + clipBounds.height));
        int startColumn = Math.max(0, getColumnNumberFromX(clipBounds.x));
        int endColumn = Math.min(getNumberOfColumns(), 1 + getColumnNumberFromX(clipBounds.x + clipBounds.width));

        Set<Cell> cells = new HashSet<>();

        // Collect cells to be drawn
        for (int i = startRow; i < endRow; i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            for (int j = startColumn; j < endColumn; j++) {
                Cell cell = row.getCell(j);

                if (cell != null) {
                    cells.add(cell.getLogicalCell());
                }
            }
        }

        return cells;
    }

    /**
     * Draw cells.
     *
     * Depending on {@code cellDrawMode}, either background, border or
     * foreground is drawn.
     */
    private void drawCells(Graphics2D g, Collection<Cell> cells, CellDrawMode cellDrawMode) {
        for (Cell cell : cells) {
            switch (cellDrawMode) {
                case DRAW_CELL_BACKGROUND:
                    drawCellBackground(g, cell);
                    break;
                case DRAW_CELL_BORDER:
                    drawCellBorder(g, cell);
                    break;
                case DRAW_CELL_FOREGROUND:
                    drawCellForeground(g, cell);
                    break;
            }
        }
    }

    /**
     * Draw cell background.
     *
     * @param g the graphics context to use
     * @param x x-coordinate of the cells top-left corner
     * @param y y-coordinate of the cells top-left corner
     * @param w width of the cell in pixels
     * @param h height of the cell in pixels
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
     * @param x x-coordinate of the cells top-left corner
     * @param y y-coordinate of the cells top-left corner
     * @param w width of the cell in pixels
     * @param h height of the cell in pixels
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
            g.setStroke(getStroke(b.getWidth()));
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
     * @param x x-coordinate of the cells top-left corner
     * @param y y-coordinate of the cells top-left corner
     * @param w width of the cell in pixels
     * @param h height of the cell in pixels
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
        int paddingX=2;
        int paddingY=2;
        float width = cr.width-2*paddingX-1;

        CellStyle style = cell.getCellStyle();
        Font font = style.getFont();
        final Color color = font.getColor();

        g.setFont(getAwtFont(font));
        g.setColor(color == null ? Color.BLACK : color);

        // layout text
        float wrapWidth = style.isWrap() ? width : 0;
        FontRenderContext frc = new FontRenderContext(g.getTransform(), true, true);
        List<TextLayout> layouts = prepareText(g, frc, text.getIterator(), wrapWidth);

        // determine size of text
        float textWidth = 0;
        float textHeight = 0;
        for (TextLayout layout: layouts) {
            textWidth=Math.max(textWidth, layout.getVisibleAdvance());
            textHeight += layout.getAscent()+layout.getDescent() + layout.getLeading();
        }

        // calculate text position
        final float xd, yd;
        switch (style.getHAlign()) {
            case ALIGN_LEFT:
            case ALIGN_JUSTIFY:
                xd = cr.x+paddingX;
                break;
            case ALIGN_CENTER:
                xd = (float) (cr.x+(cr.width - textWidth) / 2.0);
                break;
            case ALIGN_RIGHT:
                xd = cr.x+cr.width - textWidth-paddingX;
                break;
            default:
                throw new IllegalArgumentException();
        }
        switch (style.getVAlign()) {
            case ALIGN_TOP:
            case ALIGN_JUSTIFY:
                yd = cr.y+paddingY;
                break;
            case ALIGN_MIDDLE:
                yd = (float) (cr.y+(cr.height - textHeight-layouts.get(layouts.size()-1).getLeading()) / 2.0);
                break;
            case ALIGN_BOTTOM:
                final TextLayout lastLayout = layouts.get(layouts.size()-1);
                yd = cr.y+cr.height - lastLayout.getDescent()-lastLayout.getAscent()-paddingY;
                break;
            default:
                throw new IllegalArgumentException();
        }

        // draw text
        float drawPosY = yd;
        for (TextLayout layout: layouts) {
            // Compute pen x position. If the paragraph
            // is right-to-left we will align the
            // TextLayouts to the right edge of the panel.
            float drawPosX = layout.isLeftToRight() ? xd : xd + width - layout.getAdvance();

            // Move y-coordinate by the ascent of the
            // layout.
            drawPosY += layout.getAscent();

            // Draw the TextLayout at (drawPosX,drawPosY).
            layout.draw(g, drawPosX, drawPosY);

            // Move y-coordinate in preparation for next
            // layout.
            drawPosY += layout.getDescent() + layout.getLeading();
        }
    }

    private List<TextLayout> prepareText(Graphics2D g, FontRenderContext frc, AttributedCharacterIterator text, float width) {

        if (width<=0) {
            // no width is given, so no wrapping will be applied.
            return Collections.singletonList(new TextLayout(text,frc));
        }

        AttributedCharacterIterator paragraph = text;
        int paragraphStart = paragraph.getBeginIndex();
        int paragraphEnd = paragraph.getEndIndex();
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);
        float drawPosY = 0;
        List<TextLayout> tls = new ArrayList<>();
        // Set position to the index of the first
        // character in the paragraph.
        lineMeasurer.setPosition(paragraphStart);

        // Get lines from until the entire paragraph
        // has been displayed.
        while (lineMeasurer.getPosition() < paragraphEnd) {

            TextLayout layout = lineMeasurer.nextLayout(width);

            // Compute pen x position. If the paragraph
            // is right-to-left we will align the
            // TextLayouts to the right edge of the panel.

            // Move y-coordinate by the ascent of the
            // layout.
            drawPosY += layout.getAscent();

            // Draw the TextLayout at (drawPosX,drawPosY).
            tls.add(layout);

            // Move y-coordinate in preparation for next
            // layout.
            drawPosY += layout.getDescent() + layout.getLeading();
        }
        return tls;
    }

    /**
     * Get number of columns for the currently loaded sheet.
     *
     * @return
     */
    private int getNumberOfColumns() {
        return columnPos.length - 1;
    }

    /**
     * Get number of rows for the currently loaded sheet.
     *
     * @return
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

    private java.awt.Font getAwtFont(Font font) {
        return fontCache.get(font);
    }

    /**
     * Return the current cell.
     * @return current cell
     */
    private Cell getCurrentCell() {
        return sheet.getRow(currentRowNum).getCell(currentColNum);
    }

    /**
     * Draw frame around current selection.
     * @param g2d graphics used for drawing
     */
    private void drawSelection(Graphics2D g2d) {
        // no sheet, no drawing
        if (sheet==null) {
            return;
        }

        Cell logicalCell = getCurrentCell().getLogicalCell();

        int rowNum =logicalCell.getRowNumber();
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

}
