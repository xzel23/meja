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
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;

import java.util.concurrent.locks.Lock;

/**
 * A helper class that implements the actual drawing algorithm.
 */
public abstract class SheetPainterBase {

    private final CellRenderer cellRenderer;

    /**
     * Color used to draw the selection rectangle.
     */
    private Color selectionColor = Color.GREEN;

    /**
     * Width of the selection rectangle borders.
     */
    protected static final float SELECTION_STROKE_WIDTH = 2;

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

    public void drawBackground(Graphics g) {
        Rectangle2f r = g.getBounds();
        g.setColor(getDelegate().getBackground().brighter());
        g.fillRect(r.x(), r.y(), r.width(), r.height());
    }

    public void drawLabel(Graphics g, Rectangle2f r, String text) {
        g.setColor(Color.BLACK);
        g.strokeRect(r);
        g.drawText(text, r.x(), r.y());
    }

    protected SheetPainterBase(CellRenderer cellRenderer) {
        this.cellRenderer = cellRenderer;
    }

    public void drawSheet(Graphics g) {
        if (sheet == null) {
            return;
        }

        Lock readLock = sheet.readLock();
        readLock.lock();
        try {
            g.beginDraw();

            drawBackground(g);
            drawLabels(g);
            drawCells(g);
            drawSelection(g);

            g.endDraw();
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
     * Draw frame around current selection.
     *
     * @param g graphics object used for drawing
     */
    private void drawSelection(Graphics g) {
        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        sheet.getCurrentCell().map(Cell::getLogicalCell)
                .ifPresent(lc -> {
                    Rectangle2f rect = getDelegate().getCellRect(lc);
                    g.setStroke(getSelectionColor(), getSelectionStrokeWidth());
                    g.strokeRect(rect);
                });
    }
    /**
     * Get display coordinates of selection rectangle.
     *
     * @param cell the selected cell
     * @return selection rectangle in display coordinates
     */
    public Rectangle2f getSelectionRect(Cell cell) {
        Rectangle2f cellRect = getDelegate().getCellRect(cell.getLogicalCell());
        float extra = (getSelectionStrokeWidth() + 1) / 2;
        return cellRect.addMargin(extra);
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

    protected void drawLabels(Graphics g) {
        SheetViewDelegate delegate = getDelegate();

        // determine visible rows and columns
        VisibleArea va = new VisibleArea(g.getBounds());

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

    protected Color getGridColor() {
        return getDelegate().getGridColor();
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
    void drawCells(Graphics g) {
        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        double maxWidth = SheetView.MAX_COLUMN_WIDTH;

        // determine visible rows and columns
        Rectangle2f clipBounds = g.getBounds();
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
                    if (j < va.startColumn && CellRenderer.isWrapping(cell.getCellStyle())) {
                        continue;
                    }
                }

                // draw cell
                if (visible) {
                    cellRenderer.drawCellBackground(g, logicalCell);
                    cellRenderer.drawCellBorder(g, logicalCell);
                    cellRenderer.drawCellForeground(g, logicalCell);
                }
            }
        }
    }
}
