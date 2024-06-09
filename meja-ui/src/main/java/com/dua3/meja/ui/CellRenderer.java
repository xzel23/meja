package com.dua3.meja.ui;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.Row;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CellRenderer {
    private static final Logger LOG = LogManager.getLogger(CellRenderer.class);

    private static final FontUtil<?> FONT_UTIL = FontUtil.getInstance();

    private final SheetViewDelegate delegate;

    public CellRenderer(SheetViewDelegate delegate) {
        this.delegate = delegate;
    }

    private SheetViewDelegate getDelegate() {
        return delegate;
    }

    /**
     * Draw cell background.
     *
     * @param cell cell to draw
     */
    public void drawCellBackground(Graphics g, Cell cell) {
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
    public void drawCellBorder(Graphics g, Cell cell) {
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
                // draw grid line instead of border
                g.setStroke(getGridColor(), delegate.get1Px());
            } else {
                Color color = b.color();
                if (color == null) {
                    color = Color.BLACK;
                }
                g.setStroke(color, b.width() * delegate.get1Px());
            }

            switch (d) {
                case NORTH -> g.strokeLine(cr.xMin(), cr.yMin(), cr.xMax(), cr.yMin());
                case EAST -> g.strokeLine(cr.xMax(), cr.yMin(), cr.xMax(), cr.yMax());
                case SOUTH -> g.strokeLine(cr.xMin(), cr.yMax(), cr.xMax(), cr.yMax());
                case WEST -> g.strokeLine(cr.xMin(), cr.yMin(), cr.xMin(), cr.yMax());
            }
        }
    }

    /**
     * Draw cell foreground.
     *
     * @param cell cell to draw
     */
    public void drawCellForeground(Graphics g, Cell cell) {
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

    private void render(Graphics g, Cell cell, Rectangle2f r, Rectangle2f clipRect) {
        CellStyle cs = cell.getCellStyle();

        RichText text = cell.getAsText(delegate.getLocale());
        Font font = cs.getFont();

        float x, y;
        Graphics.HAnchor hAnchor;
        Graphics.VAnchor vAnchor;

        switch (effectiveHAlign(cs.getHAlign(), cell.getCellType())) {
            default -> { x = r.xMin(); hAnchor = Graphics.HAnchor.LEFT; }
            case ALIGN_CENTER -> { x = r.xCenter(); hAnchor = Graphics.HAnchor.CENTER; }
            case ALIGN_RIGHT -> { x = r.xMax(); hAnchor = Graphics.HAnchor.RIGHT; }
        }

        switch (cs.getVAlign()) {
            default -> { y = r.yMax(); vAnchor = Graphics.VAnchor.BOTTOM; }
            case ALIGN_TOP, ALIGN_DISTRIBUTED -> { y = r.yMin(); vAnchor = Graphics.VAnchor.TOP; }
            case ALIGN_MIDDLE, ALIGN_JUSTIFY -> { y = r.yCenter(); vAnchor = Graphics.VAnchor.MIDDLE; }
        }

        g.setFont(font);
        g.drawText(text.toString(), x, y, hAnchor, vAnchor);
    }

    private static HAlign effectiveHAlign(HAlign hAlign, CellType cellType) {
        if (hAlign == HAlign.ALIGN_AUTOMATIC) {
            return switch (cellType) {
                case TEXT -> HAlign.ALIGN_LEFT;
                default -> HAlign.ALIGN_RIGHT;
            };
        }

        return hAlign;
    }

    /**
     * Test whether style uses text wrapping. While there is a property for text
     * wrapping, the alignment settings have to be taken into account too.
     *
     * @param style style
     * @return true if cell content should be displayed with text wrapping
     */
    public static boolean isWrapping(CellStyle style) {
        return style.isWrap() || style.getHAlign().isWrap() || style.getVAlign().isWrap();
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
