package com.dua3.meja.ui;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.VAlign;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.Graphics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The CellRenderer class is responsible for rendering cells in a {@link SheetView}.
 */
public class CellRenderer {
    private static final Logger LOGGER = LogManager.getLogger(CellRenderer.class);

    private final SheetViewDelegate delegate;

    /**
     * Create new CellRenderer instance.
     *
     * @param delegate the delegate object that provides necessary information about the {@link SheetView} for which cells are to be rendered
     */
    public CellRenderer(SheetViewDelegate delegate) {
        this.delegate = delegate;
    }

    private SheetViewDelegate getDelegate() {
        return delegate;
    }

    /**
     * Draws a cell on the graphics object. The cell is composed of three parts:
     * background, border, and foreground.
     *
     * @param g    the graphics object on which to draw the cell
     * @param cell the cell to draw
     */
    public void drawCell(Graphics g, Cell cell) {
        drawCellBackground(g, cell);
        drawCellBorder(g, cell);
        drawCellForeground(g, cell);
    }

    /**
     * Draw the cell background.
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
            g.setFill(style.getFillBgColor());
            g.fillRect(cr);
        }

        g.setFill(style.getFillFgColor());
        g.fillRect(cr);
    }

    /**
     * Draw the cell border.
     *
     * @param cell cell to draw
     */
    private void drawCellBorder(Graphics g, Cell cell) {
        Rectangle2f cr = getCellRect(cell);

        // draw border
        for (Direction d : Direction.values()) {
            BorderStyle b = cell.getEffectiveBorderStyle(d);

            if (b.isNone()) {
                continue;
            }

            Color color = b.color();
            g.setStroke(color, b.width() * delegate.get1PxWidthInPoints());

            switch (d) {
                case NORTH -> {
                    g.setStroke(color, b.width() * delegate.get1PxHeightInPoints());
                    g.strokeLine(cr.xMin(), cr.yMin(), cr.xMax(), cr.yMin());
                }
                case EAST -> {
                    g.setStroke(color, b.width() * delegate.get1PxWidthInPoints());
                    g.strokeLine(cr.xMax(), cr.yMin(), cr.xMax(), cr.yMax());
                }
                case SOUTH -> {
                    g.setStroke(color, b.width() * delegate.get1PxHeightInPoints());
                    g.strokeLine(cr.xMin(), cr.yMax(), cr.xMax(), cr.yMax());
                }
                case WEST -> {
                    g.setStroke(color, b.width() * delegate.get1PxWidthInPoints());
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

        Alignment hAlign;
        Graphics.HAnchor hAnchor;
        HAlign alignment = cs.effectiveHAlign(cell.getCellType());
        switch (alignment) {
            case ALIGN_LEFT -> {
                hAlign = Alignment.LEFT;
                hAnchor = Graphics.HAnchor.LEFT;
            }
            case ALIGN_CENTER -> {
                hAlign = Alignment.CENTER;
                hAnchor = Graphics.HAnchor.CENTER;
            }
            case ALIGN_RIGHT -> {
                hAlign = Alignment.RIGHT;
                hAnchor = Graphics.HAnchor.RIGHT;
            }
            case ALIGN_JUSTIFY -> {
                hAlign = Alignment.JUSTIFY;
                hAnchor = Graphics.HAnchor.LEFT;
            }
            default -> throw new IllegalStateException(alignment.name() + " not allowed here");
        }

        VerticalAlignment vAlign;
        Graphics.VAnchor vAnchor;
        VAlign verticalAlignment = cs.getVAlign();
        switch (verticalAlignment) {
            case ALIGN_TOP -> {
                vAlign = VerticalAlignment.TOP;
                vAnchor = Graphics.VAnchor.TOP;
            }
            case ALIGN_MIDDLE -> {
                vAlign = VerticalAlignment.MIDDLE;
                vAnchor = Graphics.VAnchor.MIDDLE;
            }
            case ALIGN_BOTTOM -> {
                vAlign = VerticalAlignment.BOTTOM;
                vAnchor = Graphics.VAnchor.BOTTOM;
            }
            case ALIGN_DISTRIBUTED, ALIGN_JUSTIFY -> {
                vAlign = VerticalAlignment.DISTRIBUTED;
                vAnchor = Graphics.VAnchor.TOP;
            }
            default -> throw new IllegalStateException("unsupported vertical alignment: " + verticalAlignment.name());
        }

        RichText text = cell.getAsText(delegate.getLocale());
        boolean wrapping = cs.isStyleWrapping();
        g.setFont(cs.getFont());

        short rotation = cs.getRotation();
        double angle = -rotation * Math.PI / 180;

        g.renderText(
                r.min(), text, hAnchor, vAnchor, hAlign, vAlign, r.getDimension(),
                angle, Graphics.TextRotationMode.ROTATE_AND_TRANSLATE_BLOCK, Graphics.AlignmentAxis.AUTOMATIC
        );
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

    /**
     * Draw frame around current selection.
     *
     * @param g    graphics object used for drawing
     * @param cell the cell to render
     */
    public void drawSelection(Graphics g, Cell cell) {
        LOGGER.trace("drawSelection()");

        Cell lc = cell.getLogicalCell();

        Rectangle2f r = getCellRect(lc);

        float strokeWidth = delegate.getSelectionStrokeWidth();

        g.setStroke(delegate.getSelectionColor(), strokeWidth * delegate.get1PxWidthInPoints());
        g.strokeRect(r);

        if (strokeWidth > 1) {
            g.setStroke(delegate.getSelectionColor().brighter(), delegate.get1PxWidthInPoints());
            g.strokeRect(r.addMargin(-delegate.get1PxWidthInPoints() * strokeWidth / 2));
            g.setStroke(delegate.getSelectionColor().darker(), delegate.get1PxWidthInPoints());
            g.strokeRect(r.addMargin(delegate.get1PxWidthInPoints() * strokeWidth / 2));
        }
    }
}
