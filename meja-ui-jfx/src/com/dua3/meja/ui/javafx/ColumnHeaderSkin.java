/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.MejaHelper;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class ColumnHeaderSkin extends SkinBase<ColumnHeader> {

    private Canvas canvas = null;

    public ColumnHeaderSkin(ColumnHeader columnHeader) {
        super(columnHeader);
        init(columnHeader);
        redraw();
    }

    private void init(ColumnHeader columnHeader) {
        canvas = new Canvas();
        getChildren().setAll(canvas);

        columnHeader.setMinSize(getPreferredWidth(), getPreferredHeight());
        columnHeader.setPrefSize(getPreferredWidth(), getPreferredHeight());
        columnHeader.setMaxSize(getPreferredWidth(), getPreferredHeight());

        columnHeader.addEventHandler(
                ColumnHeader.EVENT_TYPE_LAYOUT_CHANGED,
                (Event t) -> redraw()
        );
    }

    private void redraw() {
        ColumnHeader columnHeader = (ColumnHeader) getNode();
        Sheet sheet = columnHeader.getSheet();

        if (sheet == null) {
            return;
        }

        canvas.setWidth(getPreferredWidth());
        canvas.setHeight(getPreferredHeight());

        GraphicsContext gc = canvas.getGraphicsContext2D();

        double x = 0;
        double y = 0;
        double h = canvas.getHeight();
        for (int j = columnHeader.getFirstColumn(); j < columnHeader.getLastColumn(); j++) {
            String text = MejaHelper.getColumnName(j);
            double w = sheet.getColumnWidth(j);
            gc.setFill(Color.LIGHTGREY);
            gc.fillRect(x, y, w, h);
            gc.strokeText(text, x + w / 2, y + h / 2, w);
            x += w;
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

    private double getPreferredWidth() {
        ColumnHeader columnHeader = (ColumnHeader) getNode();
        Sheet sheet = columnHeader.getSheet();

        if (sheet == null) {
            return 0;
        }

        double w = 0;
        for (int j = columnHeader.getFirstColumn(); j <= columnHeader.getLastColumn(); j++) {
            w += sheet.getColumnWidth(j);
        }
        return w;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getPreferredHeight();
    }

    private double getPreferredHeight() {
        return 12;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getPreferredHeight();
    }

}
