package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.swing.SwingGraphics;
import com.dua3.utility.swing.SwingUtil;
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
import java.util.Objects;
import java.util.function.Function;

final class SwingSegmentView extends JPanel implements Scrollable, SegmentView {
    private static final Logger LOG = LogManager.getLogger(SwingSegmentView.class);

    private final transient SwingSheetViewDelegate svDelegate;
    private final transient SegmentViewDelegate ssvDelegate;

    SwingSegmentView(
            SwingSheetViewDelegate sheetViewDelegate,
            SheetView.Quadrant quadrant
    ) {
        super(null, false);
        this.svDelegate = sheetViewDelegate;
        this.ssvDelegate = new SegmentViewDelegate(this, svDelegate, quadrant);
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
        Function<Float, Integer> xS2Di = x -> Math.round(t.transform(x, 0).x());

        Function<Integer, Float> yD2S = y -> ti.transform(Vector2f.of(0, y)).y();
        Function<Float, Integer> yS2Di = y -> Math.round(t.transform(0, y).y());

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
                ssvDelegate.getTransformation().inverse().ifPresent(ti -> {
                    Vector2f q = ti.transform(p.x, p.y);
                    Cell cell = svDelegate.getCellAt(q.x(), q.y());
                    svDelegate.onMousePressed(cell);
                });
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        LOG.debug("paintComponent(): ({},{}) - ({},{})", ssvDelegate.getStartRow(), ssvDelegate.getStartColumn(), ssvDelegate.getEndRow(), ssvDelegate.getEndColumn());

        try (var __ = svDelegate.automaticReadLock()) {
            // clear background by calling super method
            super.paintComponent(g);

            Rectangle bounds = getBounds();
            if (bounds.width == 0 || bounds.height == 0) {
                return;
            }

            Rectangle clipBounds = Objects.requireNonNullElse(g.getClipBounds(), bounds);

            Graphics2D g2d = (Graphics2D) g;
            SwingUtil.setRenderingQuality(g2d);
            try (SwingGraphics sg = new SwingGraphics(g2d, bounds)) {
                AffineTransformation2f t = ssvDelegate.getTransformation();
                sg.setTransformation(t);
                LOG.debug("paintComponent() - transformation:\n{}", t::toMatrixString);

                Rectangle2f r = Rectangle2f.withCorners(
                        sg.transform((float) bounds.getMinX(), (float) bounds.getMinY()),
                        sg.transform((float) bounds.getMaxX(), (float) bounds.getMaxY())
                );

                // draw sheet
                svDelegate.getSheetPainter().drawSheet(sg, r);

                // draw split lines
                if (ssvDelegate.hasHLine()) {
                    float ySplit = svDelegate.getRowPos(svDelegate.getSplitRow()) + svDelegate.get1PxWidthInPoints();
                    sg.setStroke(Color.BLACK, svDelegate.get1PxHeightInPoints());
                    sg.strokeLine(-svDelegate.getRowLabelWidthInPoints(), ySplit, svDelegate.getSheetWidthInPoints(), ySplit);
                }
                if (ssvDelegate.hasVLine()) {
                    float xSplit = svDelegate.getColumnPos(svDelegate.getSplitColumn()) + svDelegate.get1PxWidthInPoints();
                    sg.setStroke(Color.BLACK, svDelegate.get1PxWidthInPoints());
                    sg.strokeLine(xSplit, -svDelegate.getColumnLabelHeightInPoints(), xSplit, svDelegate.getSheetHeightInPoints());
                }
            }
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

        LOG.trace("scrollIntoView()");
        try (var __ = svDelegate.automaticReadLock()) {
            Rectangle2f r = svDelegate.getCellRect(cell);
            AffineTransformation2f t = ssvDelegate.getTransformation();
            Rectangle bounds = SwingGraphics.convert(Rectangle2f.withCorners(
                    t.transform(r.min()),
                    t.transform(r.max())
            ));
            scrollRectToVisible(bounds);
        }
    }
}
