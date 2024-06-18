package com.dua3.meja.ui.swing;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.math.geometry.Rectangle2f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import java.awt.Container;
import java.awt.Point;

final class SwingSheetPane extends JScrollPane {
    private static final Logger LOG = LogManager.getLogger(SwingSheetPane.class);

    public static final Rectangle2f EMPTY_RECTANGLE = Rectangle2f.of(0, 0, 0, 0);
    private final SwingSheetViewDelegate svDelegate;
    final SwingSegmentView topLeftQuadrant;
    final SwingSegmentView topRightQuadrant;
    final SwingSegmentView bottomLeftQuadrant;
    final SwingSegmentView bottomRightQuadrant;

    SwingSheetPane(SwingSheetViewDelegate svDelegate) {
        this.svDelegate = svDelegate;

        topLeftQuadrant = new SwingSegmentView(svDelegate, SheetView.Quadrant.TOP_LEFT);
        topRightQuadrant = new SwingSegmentView(svDelegate, SheetView.Quadrant.TOP_RIGHT);
        bottomLeftQuadrant = new SwingSegmentView(svDelegate, SheetView.Quadrant.BOTTOM_LEFT);
        bottomRightQuadrant = new SwingSegmentView(svDelegate, SheetView.Quadrant.BOTTOM_RIGHT);

        init();
    }

    /**
     * Scroll cell into view.
     *
     * @param cell the cell to scroll to
     */
    public void ensureCellIsVisible(@Nullable Cell cell) {
        if (cell == null) {
            return;
        }

        bottomRightQuadrant.scrollIntoView(cell);
    }

    @Override
    public void validate() {
        svDelegate.getSheet().ifPresent(sheet -> {
            topLeftQuadrant.validate();
            topRightQuadrant.validate();
            bottomLeftQuadrant.validate();
            bottomRightQuadrant.validate();
        });

        super.validate();
    }

    public Rectangle2f getCellRectInViewCoordinates(Cell cell) {
        return svDelegate.getSheet().map(sheet -> {
            boolean isTop = cell.getRowNumber() < sheet.getSplitRow();
            boolean isLeft = cell.getColumnNumber() < sheet.getSplitColumn();

            final SwingSegmentView quadrant;
            if (isTop) {
                quadrant = isLeft ? topLeftQuadrant : topRightQuadrant;
            } else {
                quadrant = isLeft ? bottomLeftQuadrant : bottomRightQuadrant;
            }

            boolean insideViewPort = !(isLeft && isTop);

            final Container parent = quadrant.getParent();
            Point pos = insideViewPort ? ((JViewport) parent).getViewPosition() : new Point();

            int i = cell.getRowNumber();
            int j = cell.getColumnNumber();
            float x = svDelegate.getColumnPos(j);
            float w = svDelegate.getColumnPos(j + cell.getHorizontalSpan()) - x + 1;
            float y = svDelegate.getRowPos(i);
            float h = svDelegate.getRowPos(i + cell.getVerticalSpan()) - y + 1;
            x -= quadrant.getSvDelegate().getXMinInViewCoordinates();
            x += parent.getX();
            x -= pos.x;
            y -= quadrant.getSvDelegate().getYMinInViewCoordinates();
            y += parent.getY();
            y -= pos.y;

            return new Rectangle2f(x, y, w, h);
        }).orElse(EMPTY_RECTANGLE);
    }

    private void init() {
        // set quadrant painters
        setViewportView(bottomRightQuadrant);
        setColumnHeaderView(topRightQuadrant);
        setRowHeaderView(bottomLeftQuadrant);
        setCorner(ScrollPaneConstants.UPPER_LEADING_CORNER, topLeftQuadrant);

        setViewportBorder(BorderFactory.createEmptyBorder());
    }

    public void repaintSheet(Rectangle2f rect) {
        LOG.debug("repaintSheet('{}'): {}", svDelegate.getSheet().map(Sheet::getSheetName), rect);

        topLeftQuadrant.repaintSheet(rect);
        topRightQuadrant.repaintSheet(rect);
        bottomLeftQuadrant.repaintSheet(rect);
        bottomRightQuadrant.repaintSheet(rect);
    }

    public void setScrollable(boolean b) {
        getHorizontalScrollBar().setEnabled(b);
        getVerticalScrollBar().setEnabled(b);
        getViewport().getView().setEnabled(b);
    }

}
