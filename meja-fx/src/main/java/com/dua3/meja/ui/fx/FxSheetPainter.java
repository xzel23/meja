/*
 *
 */
package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetPainterBase;
import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;

public class FxSheetPainter extends SheetPainterBase<Canvas, Rectangle2D> {

    private float labelHeight;
    private float labelWidth;
    private Label labelPainter = new Label();

    protected FxSheetPainter(SheetViewDelegate<Rectangle2D> delegate) {
        super();
    }

    @Override
    public float getColumnLabelHeight() {
        return labelHeight;
    }

    @Override
    protected SheetViewDelegate<Rectangle2D> getDelegate() {
        return null;
    }

    @Override
    public float getRowLabelWidth() {
        return labelWidth;
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

    @Override
    public void update(@Nullable Sheet sheet) {
        super.update(sheet);

        if (sheet != null) {
            // create a string with the maximum number of digits needed to
            // represent the highest row number (use a string only consisting
            // of zeroes instead of the last row number because a proportional
            // font might be used)
            StringBuilder sb = new StringBuilder("0");
            for (int i = 1; i <= getRowCount(); i *= 10) {
                sb.append('0');
            }
            labelPainter.setText(new String(sb));
            labelPainter.autosize();
            Bounds bounds = labelPainter.getLayoutBounds();
            labelWidth = getDelegate().wD2S((float) bounds.getWidth());
            labelHeight = getDelegate().hD2S((float) bounds.getHeight());
        }
    }

}
