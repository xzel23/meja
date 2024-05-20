/*
 * Copyright 2016 Axel Howind.
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
package com.dua3.meja.ui;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;

import java.util.concurrent.locks.Lock;

/**
 * A helper class that implements the actual drawing algorithm.
 *
 * @param <G> the concrete class implementing GraphicsContext
 */
public abstract class SheetPainterBase<G> {

    /**
     * Horizontal padding.
     */
    protected static final float PADDING_X = 2;

    /**
     * Vertical padding.
     */
    protected static final float PADDING_Y = 1;

    /**
     * Color used to draw the selection rectangle.
     */
    private Color selectionColor = Color.GREEN;

    /**
     * Width of the selection rectangle borders.
     */
    protected static final float SELECTION_STROKE_WIDTH = 2;

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

    protected abstract SheetViewDelegate getDelegate();

    /**
     * Reference to the sheet.
     */
    private Sheet sheet;

    public float getRowLabelWidth() {
        return getDelegate().getRowLabelWidth();
    }

    public float getColumnLabelHeight() {
        return getDelegate().getColumnLabelHeight();
    }

    protected abstract Rectangle2f getClipBounds(G g);
    protected abstract void drawBackground(G g);
    protected abstract void drawLabel(G g, Rectangle2f rect, String text);
    protected abstract void setColor(G g, Color color);
    protected abstract void strokeLine(G g, float v, float v1, float v2, float v3);

    protected abstract void strokeRect(G g, float x, float y, float w, float h);
    protected void strokeRect(G g, Rectangle2f r) {
        strokeRect(g, r.x(), r.y(), r.width(), r.height());
    }

    protected abstract void fillRect(G g, float x, float y, float w, float h);
    protected void fillRect(G g, Rectangle2f r) {
        fillRect(g, r.x(), r.y(), r.width(), r.height());
    }

    protected abstract void setStroke(G g, Color color, float width);
    protected abstract void render(G g, Cell cell, Rectangle2f textRect, Rectangle2f clipRect);

    protected SheetPainterBase() {
    }

    public void drawSheet(G g) {
        if (sheet == null) {
            return;
        }

        Lock readLock = sheet.readLock();
        readLock.lock();
        try {
            beginDraw(g);

            drawBackground(g);

            drawLabels(g);

            drawCells(g);
            drawSelection(g);

            endDraw(g);
        } finally {
            readLock.unlock();
        }
    }


    public void update(@Nullable Sheet sheet) {
        //noinspection ObjectEquality
        if (sheet != this.sheet) {
            this.sheet = sheet;
        }
    }

    /**
     * Draw cell background.
     *
     * @param g    the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellBackground(G g, Cell cell) {
        Rectangle2f cr = getCellRect(cell);

        // draw grid lines
        setColor(g, getGridColor());
        strokeRect(g, cr);

        CellStyle style = cell.getCellStyle();
        FillPattern pattern = style.getFillPattern();

        if (pattern == FillPattern.NONE) {
            return;
        }

        if (pattern != FillPattern.SOLID) {
            Color fillBgColor = style.getFillBgColor();
            if (fillBgColor != null) {
                setColor(g, fillBgColor);
                fillRect(g, cr);
            }
        }

        Color fillFgColor = style.getFillFgColor();
        if (fillFgColor != null) {
            setColor(g, fillFgColor);
            fillRect(g, cr);
        }
    }

    private Rectangle2f getCellRect(Cell cell) {
        return getDelegate().getCellRect(cell);
    }

    /**
     * Draw cell border.
     *
     * @param g    the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellBorder(G g, Cell cell) {
        CellStyle styleTopLeft = cell.getCellStyle();

        Cell cellBottomRight = sheet.getRow(cell.getRowNumber() + cell.getVerticalSpan() - 1)
                .getCell(cell.getColumnNumber() + cell.getHorizontalSpan() - 1);
        CellStyle styleBottomRight = cellBottomRight.getCellStyle();

        Rectangle2f cr = getCellRect(cell);

        // draw border
        for (Direction d : Direction.values()) {
            boolean isTopLeft = d == Direction.NORTH || d == Direction.WEST;
            CellStyle style = isTopLeft ? styleTopLeft : styleBottomRight;

            BorderStyle b = style.getBorderStyle(d);
            if (b.width() == 0) {
                continue;
            }

            Color color = b.color();
            if (color == null) {
                color = Color.BLACK;
            }
            setStroke(g, color, b.width());

            switch (d) {
                case NORTH -> strokeLine(g, cr.xMin(), cr.yMin(), cr.xMax(), cr.yMin());
                case EAST -> strokeLine(g, cr.xMax(), cr.yMin(), cr.xMax(), cr.yMax());
                case SOUTH -> strokeLine(g, cr.xMin(), cr.yMax(), cr.xMax(), cr.yMax());
                case WEST -> strokeLine(g, cr.xMin(), cr.yMin(), cr.xMin(), cr.yMax());
            }
        }
    }

    /**
     * Draw cell foreground.
     *
     * @param g    the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellForeground(G g, Cell cell) {
        if (cell.isEmpty()) {
            return;
        }

        float paddingX = getPaddingX();
        float paddingY = getPaddingY();

        // the rectangle used for positioning the text
        Rectangle2f textRect = getCellRect(cell).addMargin(-paddingX, -paddingY);

        // the clipping rectangle
        final Rectangle2f clipRect;
        final CellStyle style = cell.getCellStyle();
        if (isWrapping(style)) {
            clipRect = textRect;
        } else {
            Row row = cell.getRow();
            float clipXMin = textRect.xMin();
            for (int j = cell.getColumnNumber() - 1; j > 0; j--) {
                if (!row.getCell(j).isEmpty()) {
                    break;
                }
                clipXMin = getDelegate().getColumnPos(j) + paddingX;
            }
            float clipXMax = textRect.xMax();
            for (int j = cell.getColumnNumber() + 1; j < getDelegate().getColumnCount(); j++) {
                if (!row.getCell(j).isEmpty()) {
                    break;
                }
                clipXMax = getDelegate().getColumnPos(j + 1) - paddingX;
            }
            clipRect = new Rectangle2f(clipXMin, textRect.yMin(), clipXMax - clipXMin, textRect.height());
        }

        render(g, cell, textRect, clipRect);
    }

    /**
     * Draw frame around current selection.
     *
     * @param g graphics object used for drawing
     */
    private void drawSelection(G g) {
        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        sheet.getCurrentCell().map(Cell::getLogicalCell)
                .ifPresent(lc -> {
                    Rectangle2f rect = getCellRect(lc);
                    setStroke(g, getSelectionColor(), getSelectionStrokeWidth());
                    strokeRect(g, rect);
                });
    }
    /**
     * Get display coordinates of selection rectangle.
     *
     * @param cell the selected cell
     * @return selection rectangle in display coordinates
     */
    public Rectangle2f getSelectionRect(Cell cell) {
        Rectangle2f cellRect = getCellRect(cell.getLogicalCell());
        float extra = (getSelectionStrokeWidth() + 1) / 2;
        return cellRect.addMargin(extra);
    }


    protected void beginDraw(G g) {
        // nop
    }

    protected final class VisibleArea {
        public final int startRow;
        public final int endRow;
        public final int startColumn;
        public final int endColumn;

        public VisibleArea(Rectangle2f clipBounds) {
            this.startRow = Math.max(0, getDelegate().getRowNumberFromY(clipBounds.yMin()));
            this.endRow = Math.min(getDelegate().getRowCount(), 1 + getDelegate().getRowNumberFromY(clipBounds.yMax()));
            this.startColumn = Math.max(0, getDelegate().getColumnNumberFromX(clipBounds.xMin()));
            this.endColumn = Math.min(getDelegate().getColumnCount(), 1 + getDelegate().getColumnNumberFromX(clipBounds.xMax()));
        }
    }

    protected void drawLabels(G g) {
        SheetViewDelegate delegate = getDelegate();

        // determine visible rows and columns
        VisibleArea va = new VisibleArea(getClipBounds(g));

        // draw row labels
        for (int i = va.startRow; i < va.endRow; i++) {
            float x = -getRowLabelWidth();
            float w = getRowLabelWidth();
            float y = getDelegate().getRowPos(i);
            float h = getDelegate().getRowPos(i + 1) - y;
            Rectangle2f r = new Rectangle2f(x, y, w, h);
            String text = delegate.getRowName(i);
            drawLabel(g, r, text);
        }

        // draw column labels
        for (int j = va.startColumn; j < va.endColumn; j++) {
            float x = getDelegate().getColumnPos(j);
            float y = -getColumnLabelHeight();
            float w = getDelegate().getColumnPos(j + 1) - x;
            Rectangle2f r = new Rectangle2f(x, y, w, getColumnLabelHeight());
            String text = delegate.getColumnName(j);
            drawLabel(g, r, text);
        }
    }

    protected void endDraw(G g) {
        // nop
    }

    protected Color getGridColor() {
        return getDelegate().getGridColor();
    }

    protected float getPaddingX() {
        return PADDING_X;
    }

    protected float getPaddingY() {
        return PADDING_Y;
    }

    protected Color getSelectionColor() {
        return selectionColor;
    }

    protected void setSelectionColor(Color selectionColor) {
        this.selectionColor = selectionColor;
        update(sheet);
    }

    protected float getSelectionStrokeWidth() {
        return SELECTION_STROKE_WIDTH;
    }

    /**
     * Draw cells.
     * <p>
     * Since borders can be draw over by the background of adjacent cells and text
     * can overlap, drawing is done in three steps:
     * <ul>
     * <li>draw background for <em>all</em> cells
     * <li>draw borders for <em>all</em> cells
     * <li>draw foreground <em>all</em> cells
     * </ul>
     * This is controlled by {@code cellDrawMode}.
     *
     * @param g            the graphics object to use
     */
    void drawCells(G g) {
        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        double maxWidth = SheetView.MAX_COLUMN_WIDTH;

        // determine visible rows and columns
        Rectangle2f clipBounds = getClipBounds(g);
        VisibleArea va = new VisibleArea(clipBounds);

        // Collect cells to be drawn
        for (int i = va.startRow; i < va.endRow; i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            // if first/last displayed cell of row is empty, start drawing at
            // the first non-empty cell to the left/right to make sure
            // overflowing text is visible.
            int first = va.startColumn;
            while (first > 0 && getDelegate().getColumnPos(first) + maxWidth > clipBounds.xMin() && row.getCell(first).isEmpty()) {
                first--;
            }

            int end = va.endColumn;
            while (end < getDelegate().getColumnCount() && getDelegate().getColumnPos(end) - maxWidth < clipBounds.xMax()
                    && (end <= 0 || row.getCell(end - 1).isEmpty())) {
                end++;
            }

            for (int j = first; j < end; j++) {
                Cell cell = row.getCell(j);
                Cell logicalCell = cell.getLogicalCell();

                final boolean visible;
                //noinspection ObjectEquality
                if (cell == logicalCell) {
                    // if cell is not merged or the top left cell of the
                    // merged region, then it is visible
                    visible = true;
                } else {
                    // otherwise, calculate row and column numbers of the
                    // first visible cell of the merged region
                    int iCell = Math.max(va.startRow, logicalCell.getRowNumber());
                    int jCell = Math.max(first, logicalCell.getColumnNumber());
                    visible = i == iCell && j == jCell;
                    // skip the other cells of this row that belong to the same
                    // merged region
                    j = logicalCell.getColumnNumber() + logicalCell.getHorizontalSpan() - 1;
                    // filter out cells that cannot overflow into the visible
                    // region
                    if (j < va.startColumn && isWrapping(cell.getCellStyle())) {
                        continue;
                    }
                }

                // draw cell
                if (visible) {
                    drawCellBackground(g, logicalCell);
                    drawCellBorder(g, logicalCell);
                    drawCellForeground(g, logicalCell);
                }
            }
        }
    }
}
