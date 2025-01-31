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

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Sheet;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.ui.Graphics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
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
    private @Nullable Sheet sheet;

    public float getRowLabelWidth() {
        return getDelegate().getRowLabelWidthInPoints();
    }

    public float getColumnLabelHeight() {
        return getDelegate().getColumnLabelHeightInPoints();
    }

    public void drawBackground(Graphics g, Rectangle2f r) {
        g.setFill(getDelegate().getBackground());
        g.fillRect(intersection(r, getDelegate().getTotalArea()));
    }

    static Rectangle2f intersection(Rectangle2f r1, Rectangle2f r2) {
        float x1 = Math.max(r1.x(), r2.x());
        float y1 = Math.max(r1.y(), r2.y());
        float x2 = Math.min(r1.x() + r1.width(), r2.x() + r2.width());
        float y2 = Math.min(r1.y() + r1.height(), r2.y() + r2.height());

        if (x2 > x1 && y2 > y1) {
            return new Rectangle2f(x1, y1, x2 - x1, y2 - y1);
        } else {
            return new Rectangle2f(r1.x(), r1.y(), 0, 0);
        }
    }

    protected SheetPainterBase(CellRenderer cellRenderer) {
        this.cellRenderer = cellRenderer;
    }

    public void drawSheet(Graphics g, Rectangle2f r) {
        LOGGER.trace("drawSheet()");

        if (sheet == null) {
            LOGGER.trace("no sheet");
            return;
        }

        Lock readLock = sheet.readLock();
        readLock.lock();
        try {
            LOGGER.debug("drawSheet - T =\n{}", () -> g.getTransformation().toMatrixString());

            drawBackground(g, r);
            drawLabels(g, r);
            drawGrid(g, r);
            drawCells(g, r);
            cellRenderer.drawSelection(g, sheet.getCurrentCell());
            drawSplitLines(g);
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

    protected void drawLabels(Graphics g, Rectangle2f r) {
        LOGGER.trace("drawLabels()");
        SheetViewDelegate delegate = getDelegate();

        // determine visible rows and columns
        SheetView.SheetArea va = delegate.getSheetArea(r, false);
        LOGGER.trace("draw labels - visible area: {}", va);

        // draw row labels
        LOGGER.trace("draw row labels");
        for (int i = va.startRow(); i < va.endRow(); i++) {
            float x = -getRowLabelWidth();
            float w = getRowLabelWidth();
            float y = getDelegate().getRowPos(i);
            float h = getDelegate().getRowPos(i + 1) - y;
            if (w > 0 && h > 0.0) {
                Rectangle2f lr = new Rectangle2f(x, y, w, h);
                String text = delegate.getRowName(i);
                delegate.drawLabel(g, lr, text);
            }
        }

        // draw column labels
        LOGGER.trace("draw column labels");
        for (int j = va.startColumn(); j < va.endColumn(); j++) {
            float x = getDelegate().getColumnPos(j);
            float y = -getColumnLabelHeight();
            float w = getDelegate().getColumnPos(j + 1) - x;
            if (w > 0) {
                Rectangle2f lr = new Rectangle2f(x, y, w, getColumnLabelHeight());
                String text = delegate.getColumnName(j);
                delegate.drawLabel(g, lr, text);
            }
        }
    }

    protected void drawGrid(Graphics g, Rectangle2f r) {
        LOGGER.trace("drawGrid()");
        SheetViewDelegate delegate = getDelegate();

        // determine visible rows and columns
        SheetView.SheetArea va = delegate.getSheetArea(r, false);
        LOGGER.trace("drawDrid() - visible area: {}", va);

        g.setStroke(delegate.getGridColor(), delegate.get1PxWidthInPoints());

        float w = getDelegate().getSheetWidthInPoints();
        float h = getDelegate().getSheetHeightInPoints();

        // draw horizontal grid lines
        LOGGER.trace("draw horizontal grid lines");
        for (int i = va.startRow(); i <= va.endRow(); i++) {
            float y = getDelegate().getRowPos(i);
            g.strokeLine(0, y, w, y);
        }

        // draw vertical grid lines
        LOGGER.trace("draw vertical grid lines");
        for (int j = va.startColumn(); j <= va.endColumn(); j++) {
            float x = getDelegate().getColumnPos(j);
            g.strokeLine(x, 0, x, h);
        }
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
     * @param g the graphics object to use
     * @param r the rectangle to draw in sheet coordinates
     */
    void drawCells(Graphics g, Rectangle2f r) {
        LOGGER.trace("drawCells()");

        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        double maxWidth = SheetView.MAX_COLUMN_WIDTH;

        // determine visible rows and columns
        AffineTransformation2f t = g.getTransformation();
        Optional<AffineTransformation2f> inverse = t.inverse();

        if (inverse.isEmpty()) {
            return;
        }

        SheetView.SheetArea va = getDelegate().getSheetArea(r, true);
        LOGGER.trace("draw cells - visible area: {}", va);

        // Collect cells to be drawn
        for (int i = va.startRow(); i < va.endRow(); i++) {
            sheet.getRowIfExists(i).ifPresent(row -> {

                // if first/last displayed cell of row is empty, start drawing at
                // the first non-empty cell to the left/right to make sure
                // overflowing text is visible.
                int first = va.startColumn();
                while (first > 0 && getDelegate().getColumnPos(first) + maxWidth > r.xMin() && row.getCell(first).isEmpty()) {
                    first--;
                }

                int end = va.endColumn();
                while (end < getDelegate().getColumnCount() && getDelegate().getColumnPos(end) - maxWidth < r.xMax()
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
                        int iCell = Math.max(va.startRow(), logicalCell.getRowNumber());
                        int jCell = Math.max(first, logicalCell.getColumnNumber());
                        visible = row.getRowNumber() == iCell && j == jCell;
                        // skip the other cells of this row that belong to the same
                        // merged region
                        j = logicalCell.getColumnNumber() + logicalCell.getHorizontalSpan() - 1;
                        // filter out cells that cannot overflow into the visible
                        // region
                        if (j < va.startColumn()) {
                            CellStyle style = cell.getCellStyle();
                            if (style.isStyleWrapping()) {
                                continue;
                            }
                        }
                    }

                    // draw cell
                    if (visible) {
                        cellRenderer.drawCell(g, logicalCell);
                    }
                }
            });
        }
    }

    protected void drawSplitLines(Graphics g) {
        LOGGER.trace("drawSplitLines()");
        SheetViewDelegate delegate = getDelegate();

        int splitColumn = delegate.getSplitColumn();
        if (splitColumn > 0) {
            g.setStroke(Color.BLACK, getDelegate().get1PxWidthInPoints());
            float x = getDelegate().getColumnPos(splitColumn);
            g.strokeLine(x, -getColumnLabelHeight(), x, getColumnLabelHeight() + delegate.getSheetHeightInPoints());
        }

        int splitRow = delegate.getSplitRow();
        if (splitRow > 0) {
            g.setStroke(Color.BLACK, getDelegate().get1PxHeightInPoints());
            float y = getDelegate().getRowPos(splitRow);
            g.strokeLine(-getRowLabelWidth(), y, getRowLabelWidth() + delegate.getSheetWidthInPoints(), y);
        }
    }

}
