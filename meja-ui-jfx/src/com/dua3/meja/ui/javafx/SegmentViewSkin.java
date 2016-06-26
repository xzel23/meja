/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import java.util.concurrent.locks.Lock;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public abstract class SegmentViewSkin extends SkinBase<SegmentView> {

    protected Canvas canvas = null;

    protected SegmentViewSkin(SegmentView control) {
        super(control);
    }

    protected void init(SegmentView control) {
        canvas = new Canvas();
        getChildren().setAll(canvas);
        control.setMinSize(getPreferredWidth(), getPreferredHeight());
        control.setPrefSize(getPreferredWidth(), getPreferredHeight());
        control.setMaxSize(getPreferredWidth(), getPreferredHeight());
        control.addEventHandler(SegmentView.EVENT_TYPE_LAYOUT_CHANGED, (Event t) -> redraw());
    }

    protected void redraw() {
        SegmentView view = (SegmentView) getNode();
        Sheet sheet = view.getSheet();
        if (sheet == null) {
            return;
        }

        Lock readLock = sheet.readLock();
        readLock.lock();
        try {
            canvas.setWidth(getPreferredWidth());
            canvas.setHeight(getPreferredHeight());

            GraphicsContext gc = canvas.getGraphicsContext2D();
            /*
        g2d.setBackground(sheet.getWorkbook().getDefaultCellStyle().getFillBgColor());
        g2d.clearRect(getColumnPos(0), getRowPos(0), getSheetWidth(), getSheetHeight());

                    drawCells(g2d, CellDrawMode.DRAW_CELL_BACKGROUND);
                    drawCells(g2d, CellDrawMode.DRAW_CELL_BORDER);
                    drawCells(g2d, CellDrawMode.DRAW_CELL_FOREGROUND);
                    drawSelection(g2d);
             */
        } finally {
            readLock.unlock();
        }

    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getPreferredWidth();
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getPreferredWidth();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getPreferredHeight();
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getPreferredHeight();
    }

    private double getPreferredWidth() {
        SegmentView view = (SegmentView) getNode();
        Sheet sheet = view.getSheet();

        if (sheet == null) {
            return 0;
        }

        double w = 0;
        for (int j = view.getStartColumn(); j <= view.getEndColumn(); j++) {
            w += sheet.getColumnWidth(j);
        }
        return w;
    }

    private double getPreferredHeight() {
        SegmentView view = (SegmentView) getNode();
        Sheet sheet = view.getSheet();

        if (sheet == null) {
            return 0;
        }

        double h = 0;
        for (int i = view.getBeginRow(); i <= view.getEndRow(); i++) {
            h += sheet.getRowHeight(i);
        }
        return h;
    }

}
