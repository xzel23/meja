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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Lock;

/**
 * A helper class that implements the actual drawing algorithm.
 */
public abstract class SheetPainterBase {

    private static final Logger LOGGER = LogManager.getLogger(SheetPainterBase.class);

    private final CellRenderer cellRenderer;

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
        g.setColor(getDelegate().getLabelBackgroundColor());
        g.fillRect(r);

        g.setColor(getDelegate().getLabelBorderColor());
        g.strokeRect(r);

        g.setFont(getDelegate().getLabelFont());
        g.drawText(text, r.xCenter(), r.yCenter(), Graphics.HAnchor.CENTER, Graphics.VAnchor.MIDDLE);
    }

    protected SheetPainterBase(CellRenderer cellRenderer) {
        this.cellRenderer = cellRenderer;
    }

    public void drawSheet(Graphics g) {
        LOGGER.trace("drawSheet()");

        if (sheet == null) {
            LOGGER.trace("no sheet");
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
        LOGGER.trace("drawSelection()");

        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        sheet.getCurrentCell().map(Cell::getLogicalCell)
                .ifPresent(lc -> {
                    Rectangle2f rect = getDelegate().getCellRect(lc);
                    LOGGER.trace("drawing selection rectangle: {}", rect);
                    g.setStroke(getDelegate().getSelectionColor(), getDelegate().getSelectionStrokeWidth());
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
        float extra = getDelegate().getSelectionStrokeWidth() / 2;
        return cellRect.addMargin(extra);
    }

    protected record VisibleArea(int startRow, int endRow, int startColumn, int endColumn) {
        public VisibleArea(SheetViewDelegate delegate, Rectangle2f boundsInSheet) {
            this(
                    Math.max(0, delegate.getRowNumberFromY(boundsInSheet.yMin())),
                    Math.min(delegate.getRowCount(), 1 + delegate.getRowNumberFromY(boundsInSheet.yMax())),
                    Math.max(0, delegate.getColumnNumberFromX(boundsInSheet.xMin())),
                    Math.min(delegate.getColumnCount(), 1 + delegate.getColumnNumberFromX(boundsInSheet.xMax()))
            );
        }
    }

    protected void drawLabels(Graphics g) {
        LOGGER.trace("drawLabels()");
        SheetViewDelegate delegate = getDelegate();

        // determine visible rows and columns
        float s = delegate.getScale();
        Graphics.Transformation t = g.getTransformation();
        Rectangle2f boundsInSheet = g.getBounds().translate(-t.dx() * s, -t.dy() * s);
        VisibleArea va = new VisibleArea(getDelegate(), boundsInSheet);
        LOGGER.trace("draw labels - visible area: {}", va);

        // draw row labels
        LOGGER.trace("draw row labels");
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
        LOGGER.trace("draw column labels");
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
        LOGGER.trace("drawCells()");

        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        double maxWidth = SheetView.MAX_COLUMN_WIDTH;

        // determine visible rows and columns
        float s = getDelegate().getScale();
        Graphics.Transformation t = g.getTransformation();
        Rectangle2f boundsInSheet = g.getBounds().translate(-t.dx() * s, -t.dy() * s);
        VisibleArea va = new VisibleArea(getDelegate(), boundsInSheet);
        LOGGER.trace("draw cells - visible area: {}", va);

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
            while (first > 0 && getDelegate().getColumnPos(first) + maxWidth > boundsInSheet.xMin() && row.getCell(first).isEmpty()) {
                first--;
            }

            int end = va.endColumn;
            while (end < getDelegate().getColumnCount() && getDelegate().getColumnPos(end) - maxWidth < boundsInSheet.xMax()
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
