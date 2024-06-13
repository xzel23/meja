package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.IntSupplier;

final class SwingSegmentView extends JPanel implements Scrollable, SegmentView {
    private static final Logger LOG = LogManager.getLogger(SwingSegmentView.class);

    private final transient SwingSheetViewDelegate svDelegate;
    private final transient SegmentViewDelegate ssvDelegate;

    SwingSegmentView(
            SwingSheetViewDelegate sheetViewDelegate,
            IntSupplier startRow,
            IntSupplier endRow,
            IntSupplier startColumn,
            IntSupplier endColumn
    ) {
        super(null, false);
        this.svDelegate = sheetViewDelegate;
        this.ssvDelegate = new SwingSegmentViewDelegate(this, svDelegate, startRow, endRow, startColumn, endColumn);
        init();
    }

    public SegmentViewDelegate getSvDelegate() {
        return ssvDelegate;
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
        AffineTransformation2f t = ssvDelegate.getTransformation();
        AffineTransformation2f ti = t.inverse().orElse(AffineTransformation2f.identity());

        Function<Integer, Float> xD2S = x -> ti.transform(Vector2f.of(x, 0)).x();
        Function<Float,Integer> xS2Di = x -> Math.round(t.transform(x, 0).x());

        Function<Integer, Float> yD2S = y -> ti.transform(Vector2f.of(0, y)).y();
        Function<Float,Integer> yS2Di = y -> Math.round(t.transform(0, y).y());

        if (orientation == SwingConstants.VERTICAL) {
            // scroll vertical
            if (direction < 0) {
                // scroll up
                final float y = yD2S.apply(visibleRect.y);
                final int yD = yS2Di.apply(y);
                int i = svDelegate.getRowNumberFromY(y);
                int posD = yD;
                while (i >= 0 && yD <= posD) {
                    posD = yS2Di.apply(svDelegate.getRowPos(i--));
                }
                return yD - posD;
            } else {
                // scroll down
                final float y = yD2S.apply(visibleRect.y + visibleRect.height);
                final int yD = yS2Di.apply(y);
                int i = svDelegate.getRowNumberFromY(y);
                int posD = yD;
                while (i <= svDelegate.getRowCount() && posD <= yD) {
                    posD = yS2Di.apply(svDelegate.getRowPos(i++));
                }
                return posD - yD;
            }
        } else // scroll horizontal
        {
            if (direction < 0) {
                // scroll left
                final float x = xD2S.apply(visibleRect.x);
                final int xD = xS2Di.apply(x);
                int j = svDelegate.getColumnNumberFromX(x);
                int posD = xD;
                while (j >= 0 && xD <= posD) {
                    posD = xS2Di.apply(svDelegate.getColumnPos(j--));
                }
                return xD - posD;
            } else {
                // scroll right
                final float x = xD2S.apply(visibleRect.x + visibleRect.width);
                int xD = xS2Di.apply(x);
                int j = svDelegate.getColumnNumberFromX(x);
                int posD = xD;
                while (j <= svDelegate.getColumnCount() && posD <= xD) {
                    posD = xS2Di.apply(svDelegate.getColumnPos(j++));
                }
                return posD - xD;
            }
        }
    }

    @Override
    public void setViewSizeOnDisplay(float w, float h) {
        Dimension dimension = new Dimension(Math.round(w), Math.round(h));
        setSize(dimension);
        setPreferredSize(dimension);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return true;
    }

    @Override
    public void validate() {
        ssvDelegate.updateLayout();
        super.validate();
    }

    private void init() {
        setOpaque(true);
        // listen to mouse events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LOG.trace("mouse pressed: {}", e);
                Point p = e.getPoint();
                ssvDelegate.getTransformation().inverse().ifPresent( ti -> {
                    Vector2f q = ti.transform(p.x, p.y);
                    Cell cell = svDelegate.getCellAt(q.x(), q.y());
                    svDelegate.onMousePressed(cell);
                });
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        LOG.debug("paintComponent(): ({},{}) - ({},{})", ssvDelegate.getBeginRow(), ssvDelegate.getBeginColumn(), ssvDelegate.getEndRow(), ssvDelegate.getEndColumn());

        Lock lock = svDelegate.readLock();
        lock.lock();
        try {
            // clear background by calling super method
            super.paintComponent(g);

            svDelegate.getSheet().ifPresent(sheet -> {
                Rectangle bounds = getBounds();
                if (bounds.width == 0 || bounds.height == 0) {
                    return;
                }

                SwingGraphics sg = new SwingGraphics((Graphics2D) g.create(), bounds);
                AffineTransformation2f t = ssvDelegate.getTransformation();
                sg.setTransformation(t);
                LOG.debug("paintComponent() - transformation:\n{}", t::toMatrixString);

                // draw sheet
                svDelegate.getSheetPainter().drawSheet(sg);

                // draw split lines
                if (ssvDelegate.hasHLine()) {
                    float ySplit = svDelegate.getRowPos(svDelegate.getSplitRow()) + svDelegate.get1PxWidth();
                    sg.setStroke(Color.BLACK, svDelegate.get1PxHeight());
                    sg.strokeLine(-svDelegate.getRowLabelWidth(), ySplit, svDelegate.getSheetWidthInPoints(), ySplit);
                }
                if (ssvDelegate.hasVLine()) {
                    float xSplit = svDelegate.getColumnPos(svDelegate.getSplitColumn()) + svDelegate.get1PxWidth();
                    sg.setStroke(Color.BLACK, svDelegate.get1PxWidth());
                    sg.strokeLine(xSplit, -svDelegate.getColumnLabelHeight(), xSplit, svDelegate.getSheetHeightInPoints());
                }
            });
        } finally {
            lock.unlock();
        }
    }

    void repaintSheet(Rectangle2f rect) {
        AffineTransformation2f t = ssvDelegate.getTransformation();
        Rectangle2f bounds = Rectangle2f.withCorners(
                t.transform(rect.min()),
                t.transform(rect.max())
        );
        repaint(SwingGraphics.convertCovering(bounds));
    }

    public void scrollIntoView(Cell cell) {
        if (ssvDelegate.isAboveSplit() || ssvDelegate.isAboveSplit()) {
            // only the bottom right quadrant is responsible for srolling
            return;
        }

        Rectangle2f r = svDelegate.getCellRect(cell);
        AffineTransformation2f t = ssvDelegate.getTransformation();
        Rectangle bounds = SwingGraphics.convert(Rectangle2f.withCorners(
                t.transform(r.min()),
                t.transform(r.max())
        ));

        scrollRectToVisible(bounds);
    }
}
