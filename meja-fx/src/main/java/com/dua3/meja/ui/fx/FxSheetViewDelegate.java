package com.dua3.meja.ui.fx;

import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.math.geometry.Rectangle2f;
import javafx.geometry.Rectangle2D;

public class FxSheetViewDelegate extends SheetViewDelegate {

    public FxSheetViewDelegate(
            FxSheetView owner
    ) {
        super(owner);
    }

    public Rectangle2f rectD2S(Rectangle2D r) {
        final float x1 = xD2S((float) r.getMinX());
        final float y1 = yD2S((float) r.getMinY());
        final float x2 = xD2S((float) r.getMaxX());
        final float y2 = yD2S((float) r.getMaxY());
        return new Rectangle2f(x1, y1, x2 - x1, y2 - y1);
    }

    public Rectangle2D rectS2D(Rectangle2f r) {
        float x1 = xS2D(r.xMin());
        float y1 = yS2D(r.yMin());
        float x2 = xS2D(r.xMax());
        float y2 = yS2D(r.yMax());
        return new Rectangle2D(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public int getColumnNumberFromX(float x) {
        return 0;
    }

    @Override
    public int getRowNumberFromY(float v) {
        return 0;
    }

    @Override
    public float getColumnPos(int column) {
        return 0;
    }

    @Override
    public float getRowPos(int row) {
        return 0;
    }

    @Override
    public float getRowLabelWidth() {
        return 0;
    }

    @Override
    public float getColumnLabelHeight() {
        return 0;
    }
}
