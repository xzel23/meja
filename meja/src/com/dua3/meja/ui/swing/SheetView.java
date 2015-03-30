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
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.VAlign;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 *
 * @author axel
 */
public class SheetView extends JComponent implements Scrollable {

    double scale = 1;

    int columnPos[];
    int rowPos[];
    int sheetWidth;
    int sheetHeight;
    int currentColNum;
    int currentRowNum;

    private final Sheet sheet;
    private Color gridColor = Color.LIGHT_GRAY;
    private Color selectionColor = Color.BLACK;
    private Stroke selectionStroke = new BasicStroke(4);

    public SheetView(Sheet sheet) {
        this.sheet = sheet;
        this.currentRowNum = 0;
        this.currentColNum = 0;
        update();
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
        scale = dpi / 72.0;

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
        for (int i = 0; i < rowPos.length; i++) {
            g.drawLine(0, rowPos[i], sheetWidth - 1, rowPos[i]);
        }
        for (int j = 0; j < columnPos.length; j++) {
            g.drawLine(columnPos[j], 0, columnPos[j], sheetHeight - 1);
        }
    }

    Collection<Cell> determineCellsToDraw(Graphics g) {
        // determine visible rows and columns
        Rectangle clipBounds = g.getClipBounds();
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
            final int i = cell.getRowNumber();
            final int j = cell.getColumnNumber();

            final int y = rowPos[i];
            final int h = rowPos[i + cell.getVerticalSpan()] - y;

            final int x = columnPos[j];
            final int w = columnPos[cell.getColumnNumber() + cell.getHorizontalSpan()] - x;

            switch (cellDrawMode) {
                case DRAW_CELL_BACKGROUND:
                    drawCellBackground(g, x, y, w, h, cell);
                    break;
                case DRAW_CELL_BORDER:
                    drawCellBorder(g, x, y, w, h, cell);
                    break;
                case DRAW_CELL_FOREGROUND:
                    drawCellForeground(g, x, y, w, h, cell);
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
    private void drawCellBackground(Graphics2D g, final int x, final int y, final int w, final int h, Cell cell) {
        CellStyle style = cell == null ? sheet.getDefaultCellStyle() : cell.getCellStyle();

        FillPattern pattern = style.getFillPattern();

        if (pattern != FillPattern.SOLID) {
            Color fillBgColor = style.getFillBgColor();
            if (fillBgColor != null) {
                g.setColor(fillBgColor);
                g.fillRect(x, y, w, h);
            }
        }

        if (pattern != FillPattern.NONE) {
            Color fillFgColor = style.getFillFgColor();
            if (fillFgColor != null) {
                g.setColor(fillFgColor);
                g.fillRect(x, y, w, h);
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
    private void drawCellBorder(Graphics2D g, final int x, final int y, final int w, final int h, Cell cell) {
        CellStyle style = cell == null ? sheet.getDefaultCellStyle() : cell.getCellStyle();

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
            final BasicStroke stroke = new BasicStroke((float) (scale * b.getWidth()));
            g.setStroke(stroke);
            switch (d) {
                case NORTH:
                    g.drawLine(x, y, x + w - 1, y);
                    break;
                case EAST:
                    g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
                    break;
                case SOUHT:
                    g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
                    break;
                case WEST:
                    g.drawLine(x, y, x, y + h - 1);
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
    private void drawCellForeground(Graphics2D g, final int x, final int y, final int w, final int h, Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return;
        }

        AttributedString text = cell.getAttributedString();
        if (isEmpty(text)) {
            return;
        }

        CellStyle style = cell.getCellStyle();

        AffineTransform at = g.getTransform();
        g.scale(scale, scale);

        Font font = style.getFont();
        final Color color = font.getColor();
        g.setFont(createAwtFont(style.getFont()));
        g.setColor(color == null ? Color.BLACK : color);

        FontMetrics fontMetrics = g.getFontMetrics();
        final int ascent = fontMetrics.getAscent();
        final int descent = fontMetrics.getDescent();

        TextLayout tl = new TextLayout(text.getIterator(), g.getFontRenderContext());
        Rectangle2D textBounds = tl.getBounds();

        final double xd, yd;
        switch (style.getHAlign()) {
            case ALIGN_LEFT:
            case ALIGN_JUSTIFY:
                xd = 0;
                break;
            case ALIGN_CENTER:
                xd = (w - textBounds.getWidth() * scale) / 2;
                break;
            case ALIGN_RIGHT:
                xd = w - textBounds.getWidth() * scale;
                break;
            default:
                throw new IllegalArgumentException();
        }
        switch (style.getVAlign()) {
            case ALIGN_TOP:
            case ALIGN_JUSTIFY:
                yd = 0 + ascent;
                break;
            case ALIGN_MIDDLE:
                yd = (h - textBounds.getHeight() * scale) / 2 + ascent;
                break;
            case ALIGN_BOTTOM:
                yd = h - descent;
                break;
            default:
                throw new IllegalArgumentException();
        }

        final int xt = (int) Math.round((x + xd) / scale);
        final int yt = (int) Math.round((y + yd) / scale);

        final boolean wrap = style.isWrap() || style.getHAlign() == HAlign.ALIGN_JUSTIFY || style.getVAlign() == VAlign.ALIGN_JUSTIFY;
        if (!wrap) {
            g.drawString(text.getIterator(), xt, yt);
        } else {
            drawAttributedStringWithWrap(g, text, xt, yt, (int) Math.round((w / scale)), (int) Math.round(h / scale));
        }

        g.setTransform(at);
    }

    protected void drawAttributedStringWithWrap(Graphics2D g, AttributedString text, final int x, final int y, int w, int h) {
        AttributedCharacterIterator paragraph = text.getIterator();
        int paragraphStart = paragraph.getBeginIndex();
        int paragraphEnd = paragraph.getEndIndex();
        FontRenderContext frc = g.getFontRenderContext();
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, frc);
        float drawPosY = y;
        // Set position to the index of the first
        // character in the paragraph.
        lineMeasurer.setPosition(paragraphStart);

        // Get lines from until the entire paragraph
        // has been displayed.
        while (lineMeasurer.getPosition() < paragraphEnd) {

            TextLayout layout = lineMeasurer.nextLayout(w);

            // Compute pen x position. If the paragraph
            // is right-to-left we will align the
            // TextLayouts to the right edge of the panel.
            float drawPosX = layout.isLeftToRight()
                    ? x : x + w - layout.getAdvance();

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

    private java.awt.Font createAwtFont(Font font) {
        int style = (font.isBold() ? java.awt.Font.BOLD : 0) | (font.isItalic() ? java.awt.Font.ITALIC : 0);
        return new java.awt.Font(font.getFamily(), style, (int) Math.round(font.getSizeInPoints()));
    }

    private void drawSelection(Graphics2D g2d) {
        if (!isVisible(g2d, currentRowNum, currentColNum)) {
            return;
        }

        int x = columnPos[currentColNum];
        int y = rowPos[currentRowNum];
        int w = columnPos[currentColNum + 1] - x;
        int h = rowPos[currentRowNum + 1] - y;

        g2d.setColor(selectionColor);
        g2d.setStroke(selectionStroke);
        g2d.drawRect(x, y, w, h);
    }

    private boolean isVisible(Graphics2D g, int row, int col) {
        Rectangle clipBounds = g.getClipBounds();
        int startRow = Math.max(0, getRowNumberFromY(clipBounds.y));
        int endRow = Math.min(getNumberOfRows(), 1 + getRowNumberFromY(clipBounds.y + clipBounds.height));
        int startColumn = Math.max(0, getColumnNumberFromX(clipBounds.x));
        int endColumn = Math.min(getNumberOfColumns(), 1 + getColumnNumberFromX(clipBounds.x + clipBounds.width));
        return startRow <= row && row <= endRow && startColumn <= col && col <= endColumn;
    }

    protected static enum CellDrawMode {

        DRAW_CELL_BACKGROUND, DRAW_CELL_BORDER, DRAW_CELL_FOREGROUND
    }

}
