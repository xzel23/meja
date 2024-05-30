package com.dua3.meja.ui.swing;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.utility.math.geometry.Rectangle2f;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import java.awt.Container;
import java.awt.Point;
import java.util.function.IntSupplier;

final class SwingSheetPane extends JScrollPane {
    public static final Rectangle2f EMPTY_RECTANGLE = Rectangle2f.of(0, 0, 0, 0);
    private final SwingSheetViewDelegate svDelegate;
    final SwingSegmentView topLeftQuadrant;
    final SwingSegmentView topRightQuadrant;
    final SwingSegmentView bottomLeftQuadrant;
    final SwingSegmentView bottomRightQuadrant;

    SwingSheetPane(SwingSheetViewDelegate svDelegate) {
        this.svDelegate = svDelegate;
        // define row and column ranges and set up segments
        final IntSupplier startColumn = () -> 0;
        final IntSupplier splitColumn = () -> svDelegate.getSheet().map(Sheet::getSplitColumn).orElse(0);
        final IntSupplier endColumn = () -> svDelegate.getSheet().map(Sheet::getColumnCount).orElse(0);

        final IntSupplier startRow = () -> 0;
        final IntSupplier splitRow = () -> svDelegate.getSheet().map(Sheet::getSplitRow).orElse(0);
        final IntSupplier endRow = () -> svDelegate.getSheet().map(Sheet::getRowCount).orElse(0);

        topLeftQuadrant = new SwingSegmentView(svDelegate, startRow, splitRow, startColumn, splitColumn);
        topRightQuadrant = new SwingSegmentView(svDelegate, startRow, splitRow, splitColumn, endColumn);
        bottomLeftQuadrant = new SwingSegmentView(svDelegate, splitRow, endRow, startColumn, splitColumn);
        bottomRightQuadrant = new SwingSegmentView(svDelegate, splitRow, endRow, splitColumn, endColumn);

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

        Rectangle2f cellRect = svDelegate.getCellRect(cell);
        boolean aboveSplit = svDelegate.getSplitY() >= cellRect.xMax();
        boolean toLeftOfSplit = svDelegate.getSplitX() >= cellRect.xMax();

        cellRect = cellRect.translate(
                toLeftOfSplit ? 0 : -svDelegate.getSplitX(),
                aboveSplit ? 0 : -svDelegate.getSplitY()
        );

        //noinspection StatementWithEmptyBody
        if (aboveSplit && toLeftOfSplit) {
            // nop: cell is always visible!
        } else if (aboveSplit) {
            // only scroll dx
            java.awt.Rectangle r = new java.awt.Rectangle(
                    svDelegate.xS2Di(cellRect.x()), 1,
                    svDelegate.wS2Di(cellRect.width()), 1
            );
            bottomRightQuadrant.scrollRectToVisible(r);
        } else if (toLeftOfSplit) {
            // only scroll dy
            java.awt.Rectangle r = new java.awt.Rectangle(
                    1, svDelegate.yS2Di(cellRect.y()),
                    1, svDelegate.hS2Di(cellRect.height())
            );
            bottomRightQuadrant.scrollRectToVisible(r);
        } else {
            bottomRightQuadrant.scrollRectToVisible(svDelegate.rectS2D(cellRect));
        }
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
        setDoubleBuffered(false);

        // set quadrant painters
        setViewportView(bottomRightQuadrant);
        setColumnHeaderView(topRightQuadrant);
        setRowHeaderView(bottomLeftQuadrant);
        setCorner(ScrollPaneConstants.UPPER_LEADING_CORNER, topLeftQuadrant);

        setViewportBorder(BorderFactory.createEmptyBorder());
    }

    public void repaintSheet(Rectangle2f rect) {
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
