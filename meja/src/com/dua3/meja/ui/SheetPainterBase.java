/*
 * Copyright 2016 Axel Howind <axel@dua3.com>.
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
package com.dua3.meja.ui;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Color;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.MejaHelper;
import java.util.concurrent.locks.Lock;

/**
 *
 * @param <GC>
 */
public abstract class SheetPainterBase<GC extends GraphicsContext> {

    private Sheet sheet = null;

    /**
     * Array with column positions (x-axis) in pixels.
     */
    private double columnPos[];

    /**
     * Array with column positions (y-axis) in pixels.
     */
    private double rowPos[];

    static final int MAX_COLUMN_WIDTH = 800;
    private double sheetHeightInPoints = 0;
    private double sheetWidthInPoints = 0;

    protected abstract void beginDraw(GC gc);

    protected abstract void endDraw(GC gc);

    protected abstract int getMaxColumnWidth();

    protected abstract void drawBackground(GC gc);

    protected abstract void drawLabel(GC gc, Rectangle rect, String text);

    protected abstract double getPaddingX();

    protected abstract double getPaddingY();

    protected abstract Color getGridColor();

    protected abstract Color getSelectionColor();

    protected abstract double getSelectionStrokeWidth();

    protected abstract void render(GC g, Cell cell, Rectangle textRect, Rectangle clipRect);

    protected abstract double getLabelHeight();

    protected abstract double getLabelWidth();
    private String getRowName(int i) {
        return Integer.toString(i+1);
    }

    public double getSheetWidthInPoints() {
        return sheetWidthInPoints;
    }

    public double getSheetHeightInPoints() {
        return sheetHeightInPoints;
    }

    private String getColumnName(int j) {
        return MejaHelper.getColumnName(j);
    }

    protected abstract double getRowLabelWidth();

    protected abstract double getColumnLabelHeight();

    static enum CellDrawMode {
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
     * Test whether style uses text wrapping. While there is a property for text
     * wrapping, the alignment settings have to be taken into account too.
     *
     * @param style style
     * @return true if cell content should be displayed with text wrapping
     */
    static boolean isWrapping(CellStyle style) {
        return style.isWrap() || style.getHAlign().isWrap() || style.getVAlign().isWrap();
    }

    public void drawSheet(GC gc) {
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
    void drawCells(GC g, CellDrawMode cellDrawMode) {
        // no sheet, no drawing
        if (sheet == null) {
            return;
        }

        int maxWidth = getMaxColumnWidth();

        Rectangle clipBounds = g.getClipBounds();

        // determine visible rows and columns
        int startRow = Math.max(0, getRowNumberFromY(clipBounds.getTop()));
        int endRow = Math.min(getRowCount(), 1 + getRowNumberFromY(clipBounds.getBottom()));
        int startColumn = Math.max(0, getColumnNumberFromX(clipBounds.getLeft()));
        int endColumn = Math.min(getColumnCount(), 1 + getColumnNumberFromX(clipBounds.getRight()));

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
            while (first > 0 && getColumnPos(first) + maxWidth > clipBounds.getLeft() && row.getCell(first).isEmpty()) {
                first--;
            }

            int end = endColumn;
            while (end < getColumnCount() && getColumnPos(end) - maxWidth < clipBounds.getRight() && (end <= 0 || row.getCell(end - 1).isEmpty())) {
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
    private void drawCellBackground(GC g, Cell cell) {
        Rectangle cr = getCellRect(cell);

        // draw grid lines
        g.setColor(getGridColor());
        g.drawRect(cr.getX(), cr.getY(), cr.getW(), cr.getH());

        CellStyle style = cell.getCellStyle();
        FillPattern pattern = style.getFillPattern();

        if (pattern == FillPattern.NONE) {
            return;
        }

        if (pattern != FillPattern.SOLID) {
            Color fillBgColor = style.getFillBgColor();
            if (fillBgColor != null) {
                g.setColor(fillBgColor);
                g.fillRect(cr.getX(), cr.getY(), cr.getW(), cr.getH());
            }
        }

        if (pattern != FillPattern.NONE) {
            Color fillFgColor = style.getFillFgColor();
            if (fillFgColor != null) {
                g.setColor(fillFgColor);
                g.fillRect(cr.getX(), cr.getY(), cr.getW(), cr.getH());
            }
        }
    }

    /**
     * Draw cell border.
     *
     * @param g the graphics context to use
     * @param cell cell to draw
     */
    private void drawCellBorder(GC g, Cell cell) {
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
            g.setStroke(color, b.getWidth());

            switch (d) {
                case NORTH:
                    g.drawLine(cr.getLeft(), cr.getTop(), cr.getRight(), cr.getTop());
                    break;
                case EAST:
                    g.drawLine(cr.getRight(), cr.getTop(), cr.getRight(), cr.getBottom());
                    break;
                case SOUTH:
                    g.drawLine(cr.getLeft(), cr.getBottom(), cr.getRight(), cr.getBottom());
                    break;
                case WEST:
                    g.drawLine(cr.getLeft(), cr.getTop(), cr.getLeft(), cr.getBottom());
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
    private void drawCellForeground(GC g, Cell cell) {
        if (cell.isEmpty()) {
            return;
        }

        double paddingX = getPaddingX();
        double paddingY = getPaddingY();

        // the rectangle used for positioning the text
        Rectangle textRect = getCellRect(cell);
        textRect = new Rectangle(
                textRect.getX() + paddingX,
                textRect.getY() + paddingY,
                textRect.getW() - 2 * paddingX,
                textRect.getH() - 2 * paddingY
        );

        // the clipping rectangle
        final Rectangle clipRect;
        final CellStyle style = cell.getCellStyle();
        if (isWrapping(style)) {
            clipRect = textRect;
        } else {
            Row row = cell.getRow();
            double clipXMin = textRect.getX();
            for (int j = cell.getColumnNumber() - 1; j > 0; j--) {
                if (!row.getCell(j).isEmpty()) {
                    break;
                }
                clipXMin = getColumnPos(j) + paddingX;
            }
            double clipXMax = textRect.getRight();
            for (int j = cell.getColumnNumber() + 1; j < getColumnCount(); j++) {
                if (!row.getCell(j).isEmpty()) {
                    break;
                }
                clipXMax = getColumnPos(j + 1) - paddingX;
            }
            clipRect = new Rectangle(clipXMin, textRect.getY(), clipXMax - clipXMin, textRect.getH());
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

        Cell logicalCell = sheet.getCurrentCell().getLogicalCell();
        Rectangle rect = getCellRect(logicalCell);

        gc.setStroke(getSelectionColor(), getSelectionStrokeWidth());
        gc.drawRect(rect.getX(), rect.getY(), rect.getW(), rect.getH());
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

        final double x = getColumnPos(j);
        final double w = getColumnPos(j + cell.getHorizontalSpan()) - x;
        final double y = getRowPos(i);
        final double h = getRowPos(i + cell.getVerticalSpan()) - y;

        return new Rectangle(x, y, w, h);
    }

    /**
     * @param j the column number
     * @return the columnPos
     */
    public double getColumnPos(int j) {
        return columnPos[j];
    }

    /**
     * @param i the row number
     * @return the rowPos
     */
    public double getRowPos(int i) {
        return rowPos[i];
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
    public int getRowNumberFromY(double y) {
        if (rowPos.length==0) {
            return  0;
        }
        
        // guess position 
        int i = (int) (rowPos.length*y/sheetHeightInPoints);
        if (i<0) {
            i=0;
        } else if (i>=rowPos.length) {
            i = rowPos.length-1;
        }

        // linear search from here
        if (getRowPos(i) > y) {
            while (i > 0 && getRowPos(i-1) > y) {
                i--;
            }
        } else {
            while (i < rowPos.length && getRowPos(i) <= y) {
                i++;
            }
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
    public int getColumnNumberFromX(double x) {
        if (columnPos.length==0) {
            return  0;
        }
        
        // guess position 
        int j = (int) (columnPos.length*x/sheetWidthInPoints);
        if (j<0) {
            j=0;
        } else if (j>=columnPos.length) {
            j = columnPos.length-1;
        }

        // linear search from here
        if (getColumnPos(j) > x) {
            while (j > 0 && getColumnPos(j-1) > x) {
                j--;
            }
        } else {
            while (j < columnPos.length && getColumnPos(j) <= x) {
                j++;
            }
        }
        
        return j - 1;
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
     * Get number of rows for the currently loaded sheet.
     *
     * @return number of rows
     */
    public int getRowCount() {
        return rowPos.length - 1;
    }

    public void update(Sheet sheet) {
        if (sheet!=this.sheet) {
            this.sheet = sheet;
        }

        // determine sheet dimensions
        if (sheet == null) {
            sheetWidthInPoints = 0;
            sheetHeightInPoints = 0;
            rowPos = new double[]{0};
            columnPos = new double[]{0};
            return;
        }

        Lock readLock = sheet.readLock();
        readLock.lock();
        try {
            sheetHeightInPoints = 0;
            rowPos = new double[2 + sheet.getLastRowNum()];
            rowPos[0] = 0;
            for (int i = 1; i < rowPos.length; i++) {
                sheetHeightInPoints += sheet.getRowHeight(i - 1);
                rowPos[i] = sheetHeightInPoints;
            }

            sheetWidthInPoints = 0;
            columnPos = new double[2 + sheet.getLastColNum()];
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
     * Get display coordinates of selection rectangle.
     *
     * @param cell
     * @return selection rectangle in display coordinates
     */
    public Rectangle getSelectionRect(Cell cell) {
        Rectangle cellRect = getCellRect(cell.getLogicalCell());
        double extra = (getSelectionStrokeWidth() + 1) / 2;
        return new Rectangle(
                cellRect.getX() - extra,
                cellRect.getY() - extra,
                cellRect.getW() + 2 * extra,
                cellRect.getH() + 2 * extra
        );
    }

    public double getSplitX() {
        return getColumnPos(sheet.getSplitColumn());
    }
    public double getSplitY() {
        return getRowPos(sheet.getSplitRow());
    }

    protected void drawLabels(GC gc) {
        // determine visible rows and columns
        Rectangle clipBounds = gc.getClipBounds();
        int startRow = Math.max(0, getRowNumberFromY(clipBounds.getTop()));
        int endRow = Math.min(getRowCount(), 1 + getRowNumberFromY(clipBounds.getBottom()));
        int startColumn = Math.max(0, getColumnNumberFromX(clipBounds.getLeft()));
        int endColumn = Math.min(getColumnCount(), 1 + getColumnNumberFromX(clipBounds.getRight()));

        // draw row labels
        Rectangle r = new Rectangle(-getRowLabelWidth(), 0, getRowLabelWidth(), 0);
        for (int i = startRow; i < endRow; i++) {
            r.setY(getRowPos(i));
            r.setH(getRowPos(i+1)-r.getY());
            String text = getRowName(i);
            drawLabel(gc, r, text);
        }

        // draw column labels
        r = new Rectangle(0, -getColumnLabelHeight(), 0, getColumnLabelHeight());
        for (int j = startColumn; j < endColumn; j++) {
            r.setX(getColumnPos(j));
            r.setW(getColumnPos(j+1)-r.getX());
            String text = getColumnName(j);
            drawLabel(gc, r, text);
        }
    }
}
