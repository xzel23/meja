package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.ui.SheetView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;

import java.util.Locale;

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
        delegate.updateLayout();

        topLeftQuadrant = new FxSegmentView(delegate, FxSegmentView.Quadrant.TOP_LEFT, rows);
        topRightQuadrant = new FxSegmentView(delegate, FxSegmentView.Quadrant.TOP_RIGHT, rows);
        bottomLeftQuadrant = new FxSegmentView(delegate, FxSegmentView.Quadrant.BOTTOM_LEFT, rows);
        bottomRightQuadrant = new FxSegmentView(delegate, FxSegmentView.Quadrant.BOTTOM_RIGHT, rows);

        // add to grid
        add(topLeftQuadrant, 0, 0);
        add(topRightQuadrant, 1, 0);
        add(bottomLeftQuadrant, 0, 1);
        add(bottomRightQuadrant, 1, 1);

        // bind size properties
        double wFixed = topLeftQuadrant.getMinWidth();
        double hFixed = topLeftQuadrant.getMinHeight();

        topRightQuadrant.prefWidthProperty().bind(widthProperty().subtract(wFixed));
        bottomLeftQuadrant.prefHeightProperty().bind(heightProperty().subtract(hFixed));
    }

    @Override
    public FxSheetViewDelegate getDelegate() {
        return delegate;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
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
