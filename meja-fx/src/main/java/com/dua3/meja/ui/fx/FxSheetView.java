package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import javafx.scene.layout.GridPane;

import java.util.function.IntSupplier;

public class FxSheetView extends GridPane implements SheetView {

    private final FxSheetViewDelegate delegate;

    final FxSegmentView topLeftQuadrant;
    final FxSegmentView topRightQuadrant;
    final FxSegmentView bottomLeftQuadrant;
    final FxSegmentView bottomRightQuadrant;

    public FxSheetView() {
        this(null);
    }

    public FxSheetView(@Nullable Sheet sheet) {
        this.delegate = new FxSheetViewDelegate(this);
        delegate.setSheet(sheet);

        // determine row and column ranges per quadrant
        final IntSupplier startColumn = () -> 0;
        final IntSupplier splitColumn = () -> sheet != null ? sheet.getSplitColumn() : 0;
        final IntSupplier endColumn = () -> sheet != null ? sheet.getColumnCount() : 0;

        final IntSupplier startRow = () -> 0;
        final IntSupplier splitRow = () -> sheet != null ? sheet.getSplitRow() : 0;
        final IntSupplier endRow = () -> sheet != null ? sheet.getRowCount() : 0;

        // create quadrants
        topLeftQuadrant = new FxSegmentView(delegate, startRow, splitRow, startColumn, splitColumn);
        topRightQuadrant = new FxSegmentView(delegate, startRow, splitRow, splitColumn, endColumn);
        bottomLeftQuadrant = new FxSegmentView(delegate, splitRow, endRow, startColumn, splitColumn);
        bottomRightQuadrant = new FxSegmentView(delegate, splitRow, endRow, splitColumn, endColumn);

        // add to grid
        add(topLeftQuadrant, 0, 0);
        add(topRightQuadrant, 1, 0);
        add(bottomLeftQuadrant, 0, 1);
        add(bottomRightQuadrant, 1, 1);
    }

    @Override
    public FxSheetViewDelegate getDelegate() {
        return delegate;
    }

    @Override
    public void scrollToCurrentCell() {

    }

    @Override
    public void stopEditing(boolean commit) {

    }

    @Override
    public void repaintCell(Cell cell) {

    }

    @Override
    public void updateContent() {

    }

    @Override
    public boolean requestFocusInWindow() {
        return false;
    }

    @Override
    public void copyToClipboard() {

    }

    @Override
    public void showSearchDialog() {

    }

    @Override
    public void startEditing() {

    }
    
    /**
     * Scroll cell into view.
     *
     * @param cell the cell to scroll to
     */
    public void ensureCellIsVisible(@Nullable Cell cell) {
    }

}
