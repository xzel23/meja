package com.dua3.meja.ui;

import com.dua3.meja.model.BorderStyle;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Direction;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.TextUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

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

    /**
     * A text fragment that can't be split (i.e., contains no whitespace) and has a uniform font so that it can be
     * drawn in a single operation.
     *
     * @param x the x-position
     * @param y the y-position
     * @param w the width
     * @param h the height
     * @param baseLine the basline value (of the line the fragment belongs to
     * @param font the font
     * @param text the text
     */
    private record Fragment (float x, float y, float w, float h, float baseLine, Font font, CharSequence text) {}

    private void render(Graphics g, Cell cell, Rectangle2f r, Rectangle2f clipRect) {
        CellStyle cs = cell.getCellStyle();
        boolean wrapping = cs.isWrap() || cs.getVAlign().isWrap() || cs.getHAlign().isWrap();
        float wrapWidth = wrapping ? r.width() : Float.MAX_VALUE;

        RichText text = cell.getAsText(delegate.getLocale());
        Font font = cs.getFont();

        Function<RichText, RichText> trimLine = switch (cs.effectiveHAlign(cell.getCellType())) {
            case ALIGN_LEFT -> RichText::stripTrailing;
            case ALIGN_RIGHT -> RichText::stripLeading;
            case ALIGN_CENTER, ALIGN_JUSTIFY, ALIGN_AUTOMATIC -> RichText::strip;
        };

        // generate lists of chunks for each line
        List<List<Fragment>> fragmentLines = new ArrayList<>();
        float textWidth = 0f;
        float textHeight = 0f;
        float baseLine = 0f;
        for (RichText line: text.split("\n")) {
            line = trimLine.apply(line);

            List<Fragment> fragments = new ArrayList<>();
            fragmentLines.add(fragments);

            float xAct = 0f;
            float lineHeight = 0f;
            float lineWidth = 0f;
            float lineBaseLine = 0f;
            boolean wrapAllowed = false;
            boolean lineWrapped = false;
            for (var run: splitLine(line, wrapping)) {
                Font f = font.deriveFont(run.getFontDef());
                Rectangle2f tr = FONT_UTIL.getTextDimension(run, f);
                if (wrapAllowed && xAct + tr.width() > wrapWidth) {
                    if (!fragments.isEmpty() && TextUtil.isBlank(fragments.get(fragments.size() - 1).text())) {
                        // remove trailing whitespace
                        fragments.remove(fragments.size() - 1);
                    } else if (TextUtil.isBlank(run)) {
                        // skip leading whitespace after wrapped line
                        continue;
                    }
                    fragments = new ArrayList<>();
                    fragmentLines.add(fragments);
                    xAct = 0f;
                    textHeight += lineHeight;
                    fragments.add(new Fragment(xAct, textHeight, tr.width(), tr.height(), lineBaseLine, f, run));
                    xAct += tr.width();
                    lineWidth = tr.width();
                    lineHeight = tr.height();
                    wrapAllowed = false;
                    lineWrapped = true;
                    lineBaseLine = -tr.yMin();
                } else {
                    wrapAllowed = wrapping;
                    lineWrapped = false;
                    fragments.add(new Fragment(xAct, textHeight, tr.width(), tr.height(), lineBaseLine, f, run));
                    xAct += tr.width();
                    lineWidth += tr.width();
                    lineHeight = Math.max(lineHeight, tr.height());
                    lineBaseLine = Math.max(lineBaseLine, -tr.yMin());
                }
            }
            textWidth = Math.max(textWidth, lineWidth);
            textHeight += lineHeight;
            baseLine = lineBaseLine;
        }

        renderFragments(g, cell, r, cs, textWidth, textHeight, baseLine, fragmentLines);
    }

    private static void renderFragments(Graphics g, Cell cell, Rectangle2f cr, CellStyle cs, float textWidth, float textHeight, float baseLine, List<List<Fragment>> fragmentLines) {
        float y, fillerHeight = 0f;
        switch (cs.getVAlign()) {
            default -> { y = cr.yMax() - textHeight; }
            case ALIGN_TOP -> { y = cr.yMin(); }
            case ALIGN_DISTRIBUTED -> { y = cr.yMin(); fillerHeight =(cr.height()- textHeight)/Math.max(1, fragmentLines.size()-1); }
            case ALIGN_MIDDLE, ALIGN_JUSTIFY -> { y = cr.yCenter() - textHeight /2; }
        }

        record LineStatistics(float text, float whiteSpace, int nSpace) {};
        for (List<Fragment> fragments : fragmentLines) {
            // determine the number and size of whitespace and text fragments
            LineStatistics fi = fragments.stream().map(fragment -> {
                boolean isWS = TextUtil.isBlank(fragment.text());
                return new LineStatistics(isWS ? 0f: fragment.w(), isWS ? fragment.w() : 0f, isWS ? 1 : 0);
            })
                    .reduce((a,b) -> new LineStatistics(a.text + b.text, a.whiteSpace + b.whiteSpace, a.nSpace + b.nSpace))
                    .orElseGet(() -> new LineStatistics(0f, 0f, 1));

            float spaceToDistribute = cr.width() - fi.text - fi.whiteSpace;
            float totalSpace = fi.whiteSpace + spaceToDistribute;

            float x= cr.xMin();
            for (Fragment fragment : fragments) {
                switch (cs.effectiveHAlign(cell.getCellType())) {
                    case ALIGN_JUSTIFY -> {
                        // distribute remaining space by evenly expanding existind whitespace
                        if (TextUtil.isBlank(fragment.text())) {
                            x += fragment.w() * (totalSpace / fi.whiteSpace() - 1);
                        }
                    }
                    case ALIGN_RIGHT -> {
                        if (fragment.x() == 0f) {
                            // push everything to the right
                            x += spaceToDistribute;
                        }
                    }
                    case ALIGN_CENTER -> {
                        if (fragment.x() == 0f) {
                            // push everything halfway right
                            x += spaceToDistribute/2f;
                        }
                    }
                    case ALIGN_LEFT, ALIGN_AUTOMATIC -> { /* nothing to do */}
                }
                g.setFont(fragment.font);
                g.drawText(fragment.text.toString(), x + fragment.x, y + fragment.y + baseLine);
            }
            y += fillerHeight;
        }
    }

    private List<Run> splitLine(RichText line, boolean wrapping) {
        if (!wrapping) {
            return line.runs();
        }

        return Arrays.stream(line.split("(?<=\\s)|(?=\\s)"))
                .flatMap(part -> part.runs().stream())
                .toList();
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
