package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.math.geometry.Rectangle2f;

import java.util.function.Function;

public class SwingSheetViewDelegate extends SheetViewDelegate {

    private final SwingSheetPainter sheetPainter;

    public SwingSheetViewDelegate(
            SwingSheetView owner,
            Function<SwingSheetViewDelegate, SwingSheetPainter> sheetPainterFactory
    ) {
        super(owner);
        this.sheetPainter = sheetPainterFactory.apply(this);
    }

    public SwingSheetPainter getSheetPainter() {
        return sheetPainter;
    }

    public Rectangle2f rectD2S(java.awt.Rectangle r) {
        final float x1 = xD2S(r.x);
        final float y1 = yD2S(r.y);
        final float x2 = xD2S(r.x + r.width);
        final float y2 = yD2S(r.y + r.height);
        return new Rectangle2f(x1, y1, x2 - x1, y2 - y1);
    }

    public java.awt.Rectangle rectS2D(Rectangle2f r) {
        final int x1 = Math.round(xS2D(r.xMin()));
        final int y1 = Math.round(yS2D(r.yMin()));
        final int x2 = Math.round(xS2D(r.xMax()));
        final int y2 = Math.round(yS2D(r.yMax()));
        return new java.awt.Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    public int getColumnNumberFromX(float x) {
        return sheetPainter.getColumnNumberFromX(x);
    }

    public int getRowNumberFromY(float v) {
        return sheetPainter.getRowNumberFromY(v);
    }

    public float getColumnPos(int column) {
        return sheetPainter.getColumnPos(column);
    }

    public float getRowPos(int row) {
        return sheetPainter.getRowPos(row);
    }

    public float getRowLabelWidth() {
        return sheetPainter.getRowLabelWidth();
    }
    public float getColumnLabelHeight() {
        return sheetPainter.getColumnLabelHeight();
    }

}
