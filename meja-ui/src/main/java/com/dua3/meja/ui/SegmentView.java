/*
 *
 */
package com.dua3.meja.ui;

import com.dua3.meja.model.Sheet;

/**
 * @param <SV> the concrete class implementing SheetView
 * @param <G> the concrete class implementing GraphicsContext
 * @author Axel Howind
 */
public interface SegmentView<SV extends SheetView, G, R> {

    SegmentViewDelegate<SV, G, R> getDelegate();

    int getBeginColumn();

    int getBeginRow();

    int getEndColumn();

    int getEndRow();

    Sheet getSheet();

    SheetPainterBase<G, R> getSheetPainter();

    void setViewSizeOnDisplay(int w, int h);

    void setViewSize(float width, float height);

}
