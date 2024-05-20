package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SegmentView;
import com.dua3.meja.ui.SegmentViewDelegate;
import javafx.scene.layout.Pane;

import java.util.function.IntSupplier;

public class FxSegmentView extends Pane implements SegmentView {
    private final FxSheetViewDelegate svDelegate;
    private final FxSegmentViewDelegate fsvDelegate;

    FxSegmentView(
            FxSheetViewDelegate sheetViewDelegate,
            IntSupplier startRow,
            IntSupplier endRow,
            IntSupplier startColumn,
            IntSupplier endColumn
    ) {
        this.svDelegate = sheetViewDelegate;
        this.fsvDelegate = new FxSegmentViewDelegate(this, svDelegate, startRow, endRow, startColumn, endColumn);
        init();
    }

    private void init() {
        // TODO listen to mouse events
        /*
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                translateMousePosition(p);
                svDelegate.onMousePressed(p.x, p.y);
            }
        });
         */
    }

    @Override
    public SegmentViewDelegate getDelegate() {
        return null;
    }

    @Override
    public Sheet getSheet() {
        return null;
    }

    @Override
    public void setViewSizeOnDisplay(float w, float h) {

    }
}
