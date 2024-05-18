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
 * @param <GC> the concrete class implementing GraphicsContext
 */
public abstract class SheetPainterBase<GC, R> {

    enum CellDrawMode {
        /**
         *
         */
        DRAW_CELL_BACKGROUND,
        /**
         *
         */
        DRAW_CELL_BORDER,
        /**
         *
         */
        DRAW_CELL_FOREGROUND
    }

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
    private Color selectionColor = Color.LIGHTBLUE;

    /**
     * Width of the selection rectangle borders.
     */
    protected static final float SELECTION_STROKE_WIDTH = 4;

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

    protected final SheetViewDelegate<GC, R> delegate;

    /**
     * Reference to the sheet.
     */
    private Sheet sheet;

    /**
     * Array with column positions (x-axis) in pixels.
     */
    private float[] columnPos = {0};

    /**
     * Array with column positions (y-axis) in pixels.
     */
    private float[] rowPos = {0};
    private float sheetHeightInPoints;

    private float sheetWidthInPoints;

    public abstract float getRowLabelWidth();
    public abstract float getColumnLabelHeight();
    protected abstract Rectangle2f getClipBounds(GC g);
    protected abstract void drawBackground(GC g);
    protected abstract void drawLabel(GC g, Rectangle2f rect, String text);
    protected abstract void setColor(GC g, Color color);
    protected abstract void strokeLine(GC g, float v, float v1, float v2, float v3);

    protected abstract void strokeRect(GC g, float x, float y, float w, float h);
    protected void strokeRect(GC g, Rectangle2f r) {
        strokeRect(g, r.x(), r.y(), r.width(), r.height());
    }

    protected abstract void fillRect(GC g, float x, float y, float w, float h);
    protected void fillRect(GC g, Rectangle2f r) {
        fillRect(g, r.x(), r.y(), r.width(), r.height());
    }

    protected abstract void setStroke(GC g, Color color, float width);
    protected abstract void render(GC g, Cell cell, Rectangle2f textRect, Rectangle2f clipRect);

    protected SheetPainterBase(SheetViewDelegate delegate) {
        this.delegate = delegate;
    }

    public void drawSheet(GC gc) {
        if (sheet == null) {
            return;
        }

        Lock readLock = sheet.readLock();
        readLock.lock();
        try {
            beginDraw(gc);

            drawBackground(gc);

            drawLabels(gc);

            drawCells(gc, CellDrawMode.DRAW_CELL_BACKGROUND);
            drawCells(gc, CellDrawMode.DRAW_CELL_BORDER);
            drawCells(gc, CellDrawMode.DRAW_CELL_FOREGROUND);
            drawSelection(gc);

            endDraw(gc);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Calculate the rectangle the cell occupies on screen.
     *
     * @param cell the cell whose area is requested
     * @return the rectangle the cell takes up in screen coordinates
     */
    public Rectangle2f getCellRect(Cell cell) {
        final int i = cell.getRowNumber();
        final int j = cell.getColumnNumber();

        final float x = getColumnPos(j);
        final float w = getColumnPos(j + cell.getHorizontalSpan()) - x;
        final float y = getRowPos(i);
        final float h = getRowPos(i + cell.getVerticalSpan()) - y;

        return new Rectangle2f(x, y, w, h);
    }

    /**
     * Get number of columns for the currently loaded sheet.
     *
     * @return number of columns
     */
    public int getColumnCount() {
        return columnPos.length - 1;
    }

    /**
     * Get the column number that the given x-coordinate belongs to.
     *
     * @param x x-coordinate
     * @return <ul>
     *         <li>-1, if the first column is displayed to the right of the given
     *         coordinate
     *         <li>number of columns, if the right edge of the last column is
     *         displayed to the left of the given coordinate
     *         <li>the number of the column that belongs to the given coordinate
     *         </ul>
     */
    public int getColumnNumberFromX(double x) {
        return getPositionIndexFromCoordinste(columnPos, x, sheetWidthInPoints);
    }

    private int getPositionIndexFromCoordinste(float[] positions, double coord, float sizeInPoints) {
        if (positions.length == 0) {
            return 0;
        }

        // guess position
        int j = (int) (positions.length * coord / sizeInPoints);
        if (j < 0) {
            j = 0;
        } else if (j >= positions.length) {
            j = positions.length - 1;
        }

        // linear search from here
        if (positions[Math.min(positions.length - 1, j)] > coord) {
            while (j > 0 && getColumnPos(j - 1) > coord) {
                j--;
            }
        } else {
            while (j < positions.length && positions[Math.min(positions.length - 1, j)] <= coord) {
                j++;
            }
        }

        return j - 1;
    }

    /**
     * @param j the column number
     * @return the columnPos
     */
    public float getColumnPos(int j) {
        return columnPos[Math.min(columnPos.length - 1, j)];
    }

    /**
     * Get number of rows for the currently loaded sheet.
     *
     * @return number of rows
     */
    public int getRowCount() {
        return rowPos.length - 1;
    }

    /**
     * Get the row number that the given y-coordinate belongs to.
     *
     * @param y y-coordinate
     * @return <ul>
     *         <li>-1, if the first row is displayed below the given coordinate
     *         <li>number of rows, if the lower edge of the last row is displayed
     *         above the given coordinate
     *         <li>the number of the row that belongs to the given coordinate
     *         </ul>
     */
    public int getRowNumberFromY(double y) {
        return getPositionIndexFromCoordinste(rowPos, y, sheetHeightInPoints);
    }

    /**
     * @param i the row number
     * @return the rowPos
     */
    public float getRowPos(int i) {
        return rowPos[Math.min(rowPos.length - 1, i)];
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

    public float getSheetHeightInPoints() {
        return sheetHeightInPoints;
    }

    public float getSheetWidthInPoints() {
        return sheetWidthInPoints;
    }

    public float getSplitX() {
        return getColumnPos(sheet.getSplitColumn());
    }

    public float getSplitY() {
        return getRowPos(sheet.getSplitRow());
    }

    public void update(@Nullable Sheet sheet) {
        //noinspection ObjectEquality
        if (sheet != this.sheet) {
            this.sheet = sheet;
        }

        // determine sheet dimensions
        if (sheet == null) {
            sheetWidthInPoints = 0;
            sheetHeightInPoints = 0;
            rowPos = new float[]{0};
            columnPos = new float[]{0};
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
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Draw cell background.
     *
     * @param g    the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellBackground(GC g, Cell cell) {
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

    /**
     * Draw cell border.
     *
     * @param g    the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellBorder(GC g, Cell cell) {
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
    private void drawCellForeground(GC g, Cell cell) {
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
                clipXMin = getColumnPos(j) + paddingX;
            }
            float clipXMax = textRect.xMax();
            for (int j = cell.getColumnNumber() + 1; j < getColumnCount(); j++) {
                if (!row.getCell(j).isEmpty()) {
                    break;
                }
                clipXMax = getColumnPos(j + 1) - paddingX;
            }
            clipRect = new Rectangle2f(clipXMin, textRect.yMin(), clipXMax - clipXMin, textRect.height());
        }

        render(g, cell, textRect, clipRect);
    }

    /**
     * Draw frame around current selection.
     *
     * @param gc graphics object used for drawing
     */
    private void drawSelection(GC gc) {
        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        sheet.getCurrentCell().map(Cell::getLogicalCell)
                .ifPresent(lc -> {
                    Rectangle2f rect = getCellRect(lc);
                    setStroke(gc, getSelectionColor(), getSelectionStrokeWidth());
                    strokeRect(gc, rect);
                });
    }

    private String getColumnName(int j) {
        return delegate.getColumnName(j);
    }

    private String getRowName(int i) {
        return delegate.getRowName(i);
    }

    protected void beginDraw(GC gc) {
        // nop
    }

    protected final class VisibleArea {
        public final int startRow;
        public final int endRow;
        public final int startColumn;
        public final int endColumn;

        public VisibleArea(Rectangle2f clipBounds) {
            this.startRow = Math.max(0, getRowNumberFromY(clipBounds.yMin()));
            this.endRow = Math.min(getRowCount(), 1 + getRowNumberFromY(clipBounds.yMax()));
            this.startColumn = Math.max(0, getColumnNumberFromX(clipBounds.xMin()));
            this.endColumn = Math.min(getColumnCount(), 1 + getColumnNumberFromX(clipBounds.xMax()));
        }
    }

    protected void drawLabels(GC gc) {
        // determine visible rows and columns
        VisibleArea va = new VisibleArea(getClipBounds(gc));

        // draw row labels
        for (int i = va.startRow; i < va.endRow; i++) {
            float x = -getRowLabelWidth();
            float w = getRowLabelWidth();
            float y = getRowPos(i);
            float h = getRowPos(i + 1) - y;
            Rectangle2f r = new Rectangle2f(x, y, w, h);
            String text = getRowName(i);
            drawLabel(gc, r, text);
        }

        // draw column labels
        for (int j = va.startColumn; j < va.endColumn; j++) {
            float x = getColumnPos(j);
            float y = -getColumnLabelHeight();
            float w = getColumnPos(j + 1) - x;
            Rectangle2f r = new Rectangle2f(x, y, w, getColumnLabelHeight());
            String text = getColumnName(j);
            drawLabel(gc, r, text);
        }
    }

    protected void endDraw(GC gc) {
        // nop
    }

    protected Color getGridColor() {
        return delegate.getGridColor();
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
     * @param cellDrawMode the draw mode to use
     */
    void drawCells(GC g, CellDrawMode cellDrawMode) {
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
            while (first > 0 && getColumnPos(first) + maxWidth > clipBounds.xMin() && row.getCell(first).isEmpty()) {
                first--;
            }

            int end = va.endColumn;
            while (end < getColumnCount() && getColumnPos(end) - maxWidth < clipBounds.xMax()
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
                    switch (cellDrawMode) {
                        case DRAW_CELL_BACKGROUND -> drawCellBackground(g, logicalCell);
                        case DRAW_CELL_BORDER -> drawCellBorder(g, logicalCell);
                        case DRAW_CELL_FOREGROUND -> drawCellForeground(g, logicalCell);
                    }
                }

            }
        }
    }
}
