package com.dua3.meja.ui;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.VerticalAlignment;

public class CellRenderer {
    private final SheetViewDelegate delegate;

    public CellRenderer(SheetViewDelegate delegate) {
        this.delegate = delegate;
    }

    private SheetViewDelegate getDelegate() {
        return delegate;
    }

    public void drawCell(Graphics g, Cell cell) {
        drawCellBackground(g, cell);
        drawCellBorder(g, cell);
        drawCellForeground(g, cell);
    }

    /**
     * Draw cell background.
     *
     * @param cell cell to draw
     */
    private void drawCellBackground(Graphics g, Cell cell) {
        Rectangle2f cr = getCellRect(cell);

        CellStyle style = cell.getCellStyle();
        FillPattern pattern = style.getFillPattern();

        if (pattern == FillPattern.NONE) {
            return;
        }

        if (pattern != FillPattern.SOLID) {
            Color fillBgColor = style.getFillBgColor();
            if (fillBgColor != null) {
                g.setFill(fillBgColor);
                g.fillRect(cr);
            }
        }

        Color fillFgColor = style.getFillFgColor();
        if (fillFgColor != null) {
            g.setFill(fillFgColor);
            g.fillRect(cr);
        }
    }

    /**
     * Draw the cell border.
     *
     * @param cell cell to draw
     */
    private void drawCellBorder(Graphics g, Cell cell) {
        CellStyle styleTopLeft = cell.getCellStyle();
        CellStyle styleBottomRight = cell.isMerged()
                ? getDelegate().getSheet().orElseThrow()
                    .getRow(cell.getRowNumber() + cell.getVerticalSpan() - 1)
                    .getCell(cell.getColumnNumber() + cell.getHorizontalSpan() - 1)
                    .getCellStyle()
                : styleTopLeft;

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
            g.setStroke(color, b.width() * delegate.get1PxWidth());

            switch (d) {
                case NORTH -> {
                    g.setStroke(color, b.width() * delegate.get1PxHeight());
                    g.strokeLine(cr.xMin(), cr.yMin(), cr.xMax(), cr.yMin());
                }
                case EAST -> {
                    g.setStroke(color, b.width() * delegate.get1PxWidth());
                    g.strokeLine(cr.xMax(), cr.yMin(), cr.xMax(), cr.yMax());
                }
                case SOUTH -> {
                    g.setStroke(color, b.width() * delegate.get1PxHeight());
                    g.strokeLine(cr.xMin(), cr.yMax(), cr.xMax(), cr.yMax());
                }
                case WEST -> {
                    g.setStroke(color, b.width() * delegate.get1PxWidth());
                    g.strokeLine(cr.xMin(), cr.yMin(), cr.xMin(), cr.yMax());
                }
            }
        }
    }

    /**
     * Draw cell foreground.
     *
     * @param cell cell to draw
     */
    private void drawCellForeground(Graphics g, Cell cell) {
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
        if (style.isStyleWrapping()) {
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

        renderCellContent(g, cell, textRect, clipRect);
    }

    private void renderCellContent(Graphics g, Cell cell, Rectangle2f r, Rectangle2f clipRect) {
        CellStyle cs = cell.getCellStyle();

        Alignment hAlign = switch (cs.effectiveHAlign(cell.getCellType())) {
            case ALIGN_LEFT -> Alignment.LEFT;
            case ALIGN_CENTER -> Alignment.CENTER;
            case ALIGN_RIGHT -> Alignment.RIGHT;
            case ALIGN_JUSTIFY -> Alignment.JUSTIFY;
            case ALIGN_AUTOMATIC -> throw new IllegalStateException("effectiveHAlign() must not return ALIGN_AUTOMATIC");
        };

        VerticalAlignment vAlign = switch (cs.getVAlign()) {
            case ALIGN_TOP -> VerticalAlignment.TOP;
            case ALIGN_MIDDLE -> VerticalAlignment.MIDDLE;
            case ALIGN_BOTTOM -> VerticalAlignment.BOTTOM;
            case ALIGN_DISTRIBUTED, ALIGN_JUSTIFY -> VerticalAlignment.DISTRIBUTED;
        };

        RichText text = cell.getAsText(delegate.getLocale());
        Font font = cs.getFont();
        boolean wrapping = cs.isStyleWrapping();

        g.renderText(r, text, font, hAlign, vAlign, wrapping);
    }

    /**
     * Get the horizontal padding.
     *
     * @return horizontal padding
     */
    public float getPaddingX() {
        return getDelegate().getPaddingX();
    }

    /**
     * Get the vertical padding.
     *
     * @return vertical padding
     */
    public float getPaddingY() {
        return getDelegate().getPaddingY();
    }

    /**
     * Get the grid color.
     *
     * @return the grid color
     */
    public Color getGridColor() {
        return getDelegate().getGridColor();
    }

    /**
     * Get the cell rectangle.
     *
     * @param cell the cell
     * @return the cell rectangle
     */
    private Rectangle2f getCellRect(Cell cell) {
        return getDelegate().getCellRect(cell);
    }
}
