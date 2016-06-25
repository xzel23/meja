/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class CornerHeaderSkin extends HeaderSkinBase<CornerHeader> {

    public CornerHeaderSkin(CornerHeader cornerHeader) {
        super(cornerHeader);
        init(cornerHeader);
        redraw();
    }

    @Override
    protected void init(CornerHeader control) {
        canvas = new Canvas();
        getChildren().setAll(canvas);
        control.setMinSize(getPreferredWidth(), getPreferredHeight());
        control.setPrefSize(getPreferredWidth(), getPreferredHeight());
        control.setMaxSize(getPreferredWidth(), getPreferredHeight());
        control.addEventHandler(CornerHeader.EVENT_TYPE_LAYOUT_CHANGED, (Event t) -> redraw());
    }

    @Override
    protected double getPreferredWidth() {
        return RowHeaderSkin.getDefaultWidth();
    }

    @Override
    protected double getPreferredHeight() {
        return ColumnHeaderSkin.getDefaultHeight();
    }

    @Override
    protected String getName(int i) {
        return "x";
    }

    @Override
    protected double getWidth(Sheet sheet, int i) {
        return getPreferredWidth();
    }

    @Override
    protected double getHeight(Sheet sheet, int i) {
        return getPreferredHeight();
    }

}
