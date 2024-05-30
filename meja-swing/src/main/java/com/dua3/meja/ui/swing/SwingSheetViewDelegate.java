package com.dua3.meja.ui.swing;

import com.dua3.meja.ui.CellRenderer;
import com.dua3.meja.ui.SheetViewDelegate;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.util.function.Function;

public class SwingSheetViewDelegate extends SheetViewDelegate {

    private final CellRenderer cellRenderer;
    private final SwingSheetPainter sheetPainter;

    public SwingSheetViewDelegate(
            SwingSheetView owner,
            Function<SwingSheetViewDelegate, CellRenderer> cellRendererFactory
    ) {
        super(owner);
        this.cellRenderer = cellRendererFactory.apply(this);
        this.sheetPainter = new SwingSheetPainter(this, cellRenderer);
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

    @Override
    protected Dimension2f calculateLabelDimension(String text) {
        JLabel label = new JLabel(text);
        label.getInsets().set(0, 0, 0, 0);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        Dimension dimension = label.getPreferredSize();
        return new Dimension2f(wD2S((float) dimension.getWidth()), 0.7f*hD2S((float) dimension.getHeight()));
    }

}
