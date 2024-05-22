package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;

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

        // create quadrants
        ObservableList<Row> rows = delegate.getSheet().map(ObservableSheet::new).map(s -> (ObservableList<Row>) s).orElse(FXCollections.emptyObservableList());

        final int splitRow = sheet != null ? sheet.getSplitRow() : 0;
        final int splitColumn = sheet != null ? sheet.getSplitColumn() : 0;

        topLeftQuadrant = new FxSegmentView(delegate, FxSegmentView.Quadrant.TOP_LEFT, rows, splitRow, splitColumn);
        topRightQuadrant = new FxSegmentView(delegate, FxSegmentView.Quadrant.TOP_RIGHT, rows, splitRow, splitColumn);
        bottomLeftQuadrant = new FxSegmentView(delegate, FxSegmentView.Quadrant.BOTTOM_LEFT, rows, splitRow, splitColumn);
        bottomRightQuadrant = new FxSegmentView(delegate, FxSegmentView.Quadrant.BOTTOM_RIGHT, rows, splitRow, splitColumn);

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
