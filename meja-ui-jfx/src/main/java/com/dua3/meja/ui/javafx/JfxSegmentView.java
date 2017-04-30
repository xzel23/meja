/*
 *
 */
package com.dua3.meja.ui.javafx;

import java.util.function.IntSupplier;

import com.dua3.meja.ui.SegmentView;

import javafx.scene.control.Skin;

/**
 *
 * @author Axel Howind
 */
public class JfxSegmentView extends SheetControl
        implements SegmentView<JfxSheetView, JfxGraphicsContext> {

    private final IntSupplier startRow;
    private final IntSupplier endRow;
    private final IntSupplier startColumn;
    private final IntSupplier endColumn;

    private double viewWidth = 0;
    private double viewHeight = 0;

    JfxSegmentView(JfxSheetView sheetView, IntSupplier startRow, IntSupplier endRow, IntSupplier startColumn,
            IntSupplier endColumn) {
        super(sheetView);
        this.startRow = startRow;
        this.endRow = endRow;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new JfxSegmentViewSkin(this);
    }

    @Override
    public int getBeginColumn() {
        return startColumn.getAsInt();
    }

    @Override
    public int getBeginRow() {
        return startRow.getAsInt();
    }

    @Override
    public int getEndColumn() {
        return endColumn.getAsInt();
    }

    @Override
    public int getEndRow() {
        return endRow.getAsInt();
    }

    double getViewHeight() {
        return viewHeight;
    }

    double getViewWidth() {
        return viewWidth;
    }

    @Override
    protected void layoutChanged() {
        updateLayout();
    }

    @Override
    public void setViewSize(double width, double height) {
        this.viewWidth = width;
        this.viewHeight = height;
    }

    @Override
    public void updateLayout() {
        SegmentView.super.updateLayout();
        final Skin<?> skin = getSkin();
        if (skin != null) {
            ((JfxSegmentViewSkin) skin).redraw();
        }
    }

}
