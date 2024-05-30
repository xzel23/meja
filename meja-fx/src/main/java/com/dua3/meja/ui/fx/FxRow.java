package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Row;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;

public class FxRow extends IndexedCell<Row> {
    private final ObservableList<Row> rows;

    private FxSheetViewDelegate delegate;

    public FxRow(ObservableList<Row> rows, FxSheetViewDelegate delegate) {
        this.rows = rows;
        this.delegate = delegate;
    }

    public double getRowWidth() {
        return delegate.getSheetWidthInPoints();
    }

    public double getRowHeight() {
        int idx = getIndex();
        return idx < 0 ? delegate.getDefaultRowHeight() : delegate.getRowHeightInPoints(rows.get(idx).getRowNumber());
    }

    @Override
    public void updateIndex(int i) {
        super.updateIndex(i);
        Row row = i < 0 || i >= rows.size() ? null : rows.get(i);
        updateItem(row, row==null);
    }

    @Override
    protected void updateItem(@Nullable Row item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setWidth(delegate.getSheetWidthInPoints());
            setHeight(delegate.getDefaultRowHeight());
        } else {
            setText(null);
            setGraphic(null);
            setWidth(delegate.getSheetWidthInPoints());
            setHeight(delegate.getRowHeightInPoints(item.getRowNumber()));
        }
    }

    @Override
    protected FxRowSkin createDefaultSkin() {
        return new FxRowSkin(this);
    }

    public FxSheetViewDelegate getDelegate() {
        return delegate;
    }
}
