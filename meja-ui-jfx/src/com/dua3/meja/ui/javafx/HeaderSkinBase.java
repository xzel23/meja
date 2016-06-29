/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SkinBase;
import javafx.scene.paint.Color;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 * @param <C>
 */
public abstract class HeaderSkinBase<C extends HeaderBase> extends SkinBase<C> {

    protected Canvas canvas = null;

    protected HeaderSkinBase(C control) {
        super(control);
    }

    protected void init(C control) {
        canvas = new Canvas();
        getChildren().setAll(canvas);
        control.setMinSize(getPreferredWidth(), getPreferredHeight());
        control.setPrefSize(getPreferredWidth(), getPreferredHeight());
        control.setMaxSize(getPreferredWidth(), getPreferredHeight());
        control.addEventHandler(C.EVENT_TYPE_LAYOUT_CHANGED, (Event t) -> redraw());
    }

    protected void redraw() {
        HeaderBase header = (HeaderBase) getNode();
        Sheet sheet = header.getSheet();
        if (sheet == null) {
            return;
        }

        canvas.setWidth(getPreferredWidth());
        canvas.setHeight(getPreferredHeight());

        GraphicsContext gc = canvas.getGraphicsContext2D();

        double x = 0;
        double y = 0;
        for (int i = header.getBegin(); i < header.getEnd(); i++) {
            String text = getName(i);
            double w = getWidth(sheet, i);
            double h = getHeight(sheet, i);
            gc.setFill(Color.LIGHTGREY);
            gc.fillRect(x, y, w, h);
            gc.strokeText(text, x + w / 2, y + h / 2, w);
            x = nextX(x, w);
            y = nextY(y, h);
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

    protected abstract double getPreferredWidth();
    protected abstract double getPreferredHeight();

    protected abstract String getName(int i);
    protected abstract double getWidth(Sheet sheet, int i);
    protected abstract double getHeight(Sheet sheet, int i);

    protected double nextX(double x, double w) {
        return x;
    }
    protected double nextY(double y, double h) {
        return y;
    }

}
