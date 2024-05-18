package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SheetPainterBase;
import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.swing.SwingUtil;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntSupplier;

final class SwingSegmentView extends JPanel implements Scrollable, SegmentView<SwingSheetView, Graphics2D, Rectangle> {
    private final SheetViewDelegate<Graphics2D, Rectangle> svDelegate;
    private final IntSupplier startRow;
    private final IntSupplier endRow;
    private final IntSupplier startColumn;
    private final IntSupplier endColumn;

    SwingSegmentView(SheetViewDelegate<Graphics2D, Rectangle> sheetViewDelegate, IntSupplier startRow, IntSupplier endRow, IntSupplier startColumn, IntSupplier endColumn) {
        super(null, false);
        this.svDelegate = sheetViewDelegate;
        this.startRow = startRow;
        this.endRow = endRow;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        init();
    }

    @Override
    public int getBeginColumn() {
        return startColumn.getAsInt();
    }

    @Override
    public int getBeginRow() {
        return startRow.getAsInt();
    }

    @Override
    public int getEndColumn() {
        return endColumn.getAsInt();
    }

    @Override
    public int getEndRow() {
        return endRow.getAsInt();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
        return 3 * getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            // scroll vertical
            if (direction < 0) {
                // scroll up
                final float y = svDelegate.yD2S(visibleRect.y);
                final int yD = svDelegate.yS2D(y);
                int i = svDelegate.getSheetPainter().getRowNumberFromY(y);
                int posD = yD;
                while (i >= 0 && yD <= posD) {
                    posD = svDelegate.yS2D(svDelegate.getSheetPainter().getRowPos(i--));
                }
                return yD - posD;
            } else {
                // scroll down
                final float y = svDelegate.yD2S(visibleRect.y + visibleRect.height);
                final int yD = svDelegate.yS2D(y);
                int i = svDelegate.getSheetPainter().getRowNumberFromY(y);
                int posD = yD;
                while (i <= svDelegate.getSheetPainter().getRowCount() && posD <= yD) {
                    posD = svDelegate.yS2D(svDelegate.getSheetPainter().getRowPos(i++));
                }
                return posD - yD;
            }
        } else // scroll horizontal
        {
            if (direction < 0) {
                // scroll left
                final float x = svDelegate.xD2S(visibleRect.x);
                final int xD = svDelegate.xS2D(x);
                int j = svDelegate.getSheetPainter().getColumnNumberFromX(x);
                int posD = xD;
                while (j >= 0 && xD <= posD) {
                    posD = svDelegate.xS2D(svDelegate.getSheetPainter().getColumnPos(j--));
                }
                return xD - posD;
            } else {
                // scroll right
                final float x = svDelegate.xD2S(visibleRect.x + visibleRect.width);
                int xD = svDelegate.xS2D(x);
                int j = svDelegate.getSheetPainter().getColumnNumberFromX(x);
                int posD = xD;
                while (j <= svDelegate.getSheetPainter().getColumnCount() && posD <= xD) {
                    posD = svDelegate.xS2D(svDelegate.getSheetPainter().getColumnPos(j++));
                }
                return posD - xD;
            }
        }
    }

    @Override
    public Sheet getSheet() {
        return svDelegate.getSheet().orElse(null);
    }

    @Override
    public SheetPainterBase<Graphics2D, Rectangle> getSheetPainter() {
        return svDelegate.getSheetPainter();
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return true;
    }

    @Override
    public void setViewSize(float wd, float hd) {
        int w = svDelegate.wS2D(wd);
        int h = svDelegate.hS2D(hd);
        Dimension d = new Dimension(w, h);
        setSize(d);
        setPreferredSize(d);
    }

    @Override
    public void validate() {
        updateLayout();
        super.validate();
    }

    private void init() {
        setOpaque(true);
        setDoubleBuffered(false);
        // listen to mouse events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                translateMousePosition(p);
                svDelegate.onMousePressed(p.x, p.y);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // clear background by calling super method
        super.paintComponent(g2d);

        svDelegate.getSheet().ifPresent(sheet -> {
            // set transformation
            final int x = getXMinInViewCoordinates();
            final int y = getYMinInViewCoordinates();
            g2d.translate(-x, -y);

            // get dimensions
            final int width = getWidth();
            final int height = getHeight();

            // draw sheet
            svDelegate.getSheetPainter().drawSheet(g2d);

            // draw split lines
            g2d.setColor(SwingUtil.toAwtColor(Color.BLACK));
            g2d.setStroke(new BasicStroke());
            if (hasHLine()) {
                g2d.drawLine(x, height + y - 1, width + x - 1, height + y - 1);
            }
            if (hasVLine()) {
                g2d.drawLine(width + x - 1, y, width + x - 1, height + y - 1);
            }
        });
    }

    int getXMinInViewCoordinates() {
        float x = svDelegate.getSheetPainter().getColumnPos(getBeginColumn());
        if (hasRowHeaders()) {
            x -= svDelegate.getSheetPainter().getRowLabelWidth();
        }
        return svDelegate.xS2D(x);
    }

    int getYMinInViewCoordinates() {
        float y = svDelegate.getSheetPainter().getRowPos(getBeginRow());
        if (hasColumnHeaders()) {
            y -= svDelegate.getSheetPainter().getColumnLabelHeight();
        }
        return svDelegate.yS2D(y);
    }

    void repaintSheet(Rectangle2f rect) {
        java.awt.Rectangle rect2 = svDelegate.rectS2D(rect);
        rect2.translate(-getXMinInViewCoordinates(), -getYMinInViewCoordinates());
        repaint(rect2);
    }

    void translateMousePosition(Point p) {
        p.translate(getXMinInViewCoordinates(), getYMinInViewCoordinates());
    }
}
