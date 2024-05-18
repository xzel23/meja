package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.math.geometry.Rectangle2f;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.function.Function;

public class SwingSheetViewDelegate extends SheetViewDelegate<Graphics2D, java.awt.Rectangle> {

    public SwingSheetViewDelegate(
            SwingSheetView owner,
            Function<? super SheetViewDelegate<Graphics2D, Rectangle>, SwingSheetPainter> sheetPainterFactory
    ) {
        super(owner, sheetPainterFactory);
    }

    @Override
    public Rectangle2f rectD2S(java.awt.Rectangle r) {
        final float x1 = xD2S(r.x);
        final float y1 = yD2S(r.y);
        final float x2 = xD2S(r.x + r.width);
        final float y2 = yD2S(r.y + r.height);
        return new Rectangle2f(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public java.awt.Rectangle rectS2D(Rectangle2f r) {
        final int x1 = xS2D(r.xMin());
        final int y1 = yS2D(r.yMin());
        final int x2 = xS2D(r.xMax());
        final int y2 = yS2D(r.yMax());
        return new java.awt.Rectangle(x1, y1, x2 - x1, y2 - y1);
    }
}
