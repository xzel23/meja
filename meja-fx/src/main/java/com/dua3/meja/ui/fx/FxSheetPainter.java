/*
 *
 */
package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.ui.SheetPainterBase;
import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import javafx.geometry.Rectangle2D;

import java.awt.Canvas;

public class FxSheetPainter extends SheetPainterBase<Canvas, Rectangle2D> {
    protected FxSheetPainter(SheetViewDelegate<Canvas, Rectangle2D> delegate) {
        super(delegate);
    }

    @Override
    public float getRowLabelWidth() {
        return 0;
    }

    @Override
    public float getColumnLabelHeight() {
        return 0;
    }

    @Override
    protected Rectangle2f getClipBounds(Canvas g) {
        return null;
    }

    @Override
    protected void drawBackground(Canvas g) {

    }

    @Override
    protected void drawLabel(Canvas g, Rectangle2f rect, String text) {

    }

    @Override
    protected void setColor(Canvas g, Color color) {

    }

    @Override
    protected void strokeLine(Canvas g, float v, float v1, float v2, float v3) {

    }

    @Override
    protected void strokeRect(Canvas g, float x, float y, float w, float h) {

    }

    @Override
    protected void fillRect(Canvas g, float x, float y, float w, float h) {

    }

    @Override
    protected void setStroke(Canvas g, Color color, float width) {

    }

    @Override
    protected void render(Canvas g, Cell cell, Rectangle2f textRect, Rectangle2f clipRect) {

    }
}
