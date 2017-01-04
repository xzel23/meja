/*
 *
 */
package com.dua3.meja.ui.javafx;

import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.SkinBase;

/**
 *
 * @author Axel Howind
 */
public class JfxSegmentViewSkin extends SkinBase<JfxSegmentView> {

    protected Canvas canvas = null;

    protected JfxSegmentViewSkin(JfxSegmentView control) {
        super(control);
        init(control);
        redraw();
    }

    protected void init(JfxSegmentView control) {
        canvas = new Canvas();
        getChildren().setAll(canvas);
        control.setMinSize(getPreferredWidth(), getPreferredHeight());
        control.setPrefSize(getPreferredWidth(), getPreferredHeight());
        control.setMaxSize(getPreferredWidth(), getPreferredHeight());
        control.addEventHandler(JfxSegmentView.EVENT_TYPE_LAYOUT_CHANGED, (Event t) -> redraw());
    }

    protected void redraw() {
        canvas.setWidth(getPreferredWidth());
        canvas.setHeight(getPreferredHeight());

        JfxGraphicsContext gc = new JfxGraphicsContext(canvas.getGraphicsContext2D());
        getView().getSheetPainter().drawSheet(gc);
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

    private JfxSegmentView getView() {
        return (JfxSegmentView) getNode();
    }

    private double getPreferredWidth() {
        return getView().getViewWidth();
    }

    private double getPreferredHeight() {
        return getView().getViewHeight();
    }

}
