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

/**
 * A helper class that implements the actual drawing algorithm.
 */
public class SheetPainter {

    private static final Logger LOGGER = LogManager.getLogger(SheetPainter.class);

    private final SheetViewDelegate delegate;
    private final CellRenderer cellRenderer;

    /**
     * Reference to the sheet.
     */
    private @Nullable Sheet sheet;

    /**
     * Gets the width of the row labels in points.
     *
     * @return the width of the row labels area in points
     */
    public float getRowLabelWidth() {
        return delegate.getRowLabelWidthInPoints();
    }

    /**
     * Gets the height of the column labels in points.
     *
     * @return the height of the column labels area in points
     */
    public float getColumnLabelHeight() {
        return delegate.getColumnLabelHeightInPoints();
    }

    /**
     * Draws the background of the sheet in the specified area.
     *
     * @param g the graphics context to draw on
     * @param va the visible area of the sheet to be drawn
     */
    public void drawBackground(Graphics g, SheetView.SheetArea va) {
        Rectangle2f sheetArea = delegate.getTotalArea();
        Rectangle2f r = Rectangle2f.of(
                Math.max(va.rect().xMin(), sheetArea.xMin()),
                Math.max(va.rect().yMin(), sheetArea.yMin()),
                Math.max(va.rect().xMax(), sheetArea.xMax()),
                Math.max(va.rect().yMax(), sheetArea.yMax())
        );
        g.setFill(delegate.getBackground());
        g.fillRect(r);
    }

    /**
     * Calculates the intersection of two rectangles.
     *
     * @param r1 the first rectangle
     * @param r2 the second rectangle
     * @return the intersection rectangle, or a zero-size rectangle at r1's position if the rectangles don't intersect
     */
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

    public SheetPainter(SheetViewDelegate delegate, CellRenderer cellRenderer) {
        this.delegate = delegate;
        this.cellRenderer = cellRenderer;
    }

    /**
     * Draws the complete sheet including all components (background, labels, grid, cells, selection, and split lines).
     * This method orchestrates the entire drawing process for the sheet view.
     *
     * @param g the graphics context to draw on
     * @param r the rectangle defining the area where the sheet should be drawn
     */
    public void drawSheet(Graphics g, Rectangle2f r) {
        LOGGER.trace("drawSheet()");

        if (sheet == null) {
            return;
        }

        try (var __ = sheet.readLock("SheetPainter.drawSheet()")) {
            SheetView.SheetArea va = delegate.getSheetArea(r, false);

            drawBackground(g, va);
            drawLabels(g, va);
            drawGrid(g, va);
            drawCells(g, va);
            cellRenderer.drawSelection(g, sheet.getCurrentCell());
            drawSplitLines(g, va);
        }
    }


    /**
     * Updates the sheet reference for this painter.
     * Only updates if the provided sheet is different from the current one.
     *
     * @param sheet the new sheet to paint, can be null to clear the current sheet
     */
    public void update(@Nullable Sheet sheet) {
        //noinspection ObjectEquality
        if (sheet != this.sheet) {
            this.sheet = sheet;
        }
    }

    /**
     * Draws the row and column labels in the specified sheet area.
     * This method handles the rendering of both row numbers and column letters.
     *
     * @param g the graphics context to draw on
     * @param va the visible area of the sheet where labels should be drawn
     */
    protected void drawLabels(Graphics g, SheetView.SheetArea va) {
       // draw row labels
        for (int i = va.startRow(); i < va.endRow(); i++) {
            float x = -getRowLabelWidth();
            float w = getRowLabelWidth();
            float y = delegate.getRowPos(i);
            float h = delegate.getRowPos(i + 1) - y;
            if (w > 0 && h > 0.0) {
                Rectangle2f lr = new Rectangle2f(x, y, w, h);
                String text = delegate.getRowName(i);
                delegate.drawLabel(g, lr, text);
            }
        }

        // draw column labels
        for (int j = va.startColumn(); j < va.endColumn(); j++) {
            float x = delegate.getColumnPos(j);
            float y = -getColumnLabelHeight();
            float w = delegate.getColumnPos(j + 1) - x;
            if (w > 0) {
                Rectangle2f lr = new Rectangle2f(x, y, w, getColumnLabelHeight());
                String text = delegate.getColumnName(j);
                delegate.drawLabel(g, lr, text);
            }
        }
    }

    /**
     * Draws the grid lines that separate cells in the sheet.
     * This includes both horizontal and vertical lines that form the cell boundaries.
     *
     * @param g the graphics context to draw on
     * @param va the visible area of the sheet where the grid should be drawn
     */
    protected void drawGrid(Graphics g, SheetView.SheetArea va) {
        Rectangle2f r = va.rect();

        g.setStroke(delegate.getGridColor(), delegate.get1PxWidthInPoints());

        // draw horizontal grid lines
        for (int i = va.startRow(); i <= va.endRow(); i++) {
            float y = delegate.getRowPos(i);
            g.strokeLine(Math.max(0, r.xMin()), y, r.xMax(), y);
        }

        // draw vertical grid lines
        for (int j = va.startColumn(); j <= va.endColumn(); j++) {
            float x = delegate.getColumnPos(j);
            g.strokeLine(x, r.yMin(), x, r.yMax());
        }
    }

    /**
     * Draws all visible cells in the specified area using a three-step process.
     * This method handles the complex task of rendering cells with proper layering
     * to ensure correct display of backgrounds, borders, and text.
     * <p>
     * The drawing process consists of three steps to handle overlapping elements:
     * <ul>
     * <li>draw background for <em>all</em> cells - ensures proper background layering</li>
     * <li>draw borders for <em>all</em> cells - prevents borders from being covered</li>
     * <li>draw foreground for <em>all</em> cells - allows text to overlap if needed</li>
     * </ul>
     *
     * @param g  the graphics context to draw on
     * @param va the visible area of the sheet containing the cells to be drawn
     */
    void drawCells(Graphics g, SheetView.SheetArea va) {
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

        // Collect cells to be drawn
        Rectangle2f r = va.rect();
        for (int i = va.startRow(); i < va.endRow(); i++) {
            sheet.getRowIfExists(i).ifPresent(row -> {

                // if first/last displayed cell of row is empty, start drawing at
                // the first non-empty cell to the left/right to make sure
                // overflowing text is visible.
                int first = va.startColumn();
                while (first > 0 && delegate.getColumnPos(first) + maxWidth > r.xMin() && row.getCell(first).isEmpty()) {
                    first--;
                }

                int end = va.endColumn();
                while (end < delegate.getColumnCount() && delegate.getColumnPos(end) - maxWidth < r.xMax()
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

    /**
     * Draws split lines that indicate frozen panes in the sheet view.
     * These lines appear as black lines at the split positions for both frozen rows and columns.
     *
     * @param g the graphics context to draw on
     * @param va the visible area of the sheet where split lines should be drawn
     */
    protected void drawSplitLines(Graphics g, SheetView.SheetArea va) {
        Rectangle2f sheetArea = delegate.getTotalArea();
        Rectangle2f r = Rectangle2f.of(
                Math.max(va.rect().xMin(), sheetArea.xMin()),
                Math.max(va.rect().yMin(), sheetArea.yMin()),
                Math.max(va.rect().xMax(), sheetArea.xMax()),
                Math.max(va.rect().yMax(), sheetArea.yMax())
        );

        int splitColumn = delegate.getSplitColumn();
        if (splitColumn > 0) {
            g.setStroke(Color.BLACK, delegate.get1PxWidthInPoints());
            float x = delegate.getColumnPos(splitColumn);
            g.strokeLine(x, r.yMin(), x, r.yMax());
        }

        int splitRow = delegate.getSplitRow();
        if (splitRow > 0) {
            g.setStroke(Color.BLACK, delegate.get1PxHeightInPoints());
            float y = delegate.getRowPos(splitRow);
            g.strokeLine(r.xMin(), y, r.xMax(), y);
        }
    }

}
