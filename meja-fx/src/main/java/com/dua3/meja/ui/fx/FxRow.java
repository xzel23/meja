package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Row;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.math.geometry.Scale2f;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;
import javafx.stage.Screen;

public class FxRow extends IndexedCell<Row> {
    private final ObservableList<Row> rows;

    private FxSheetViewDelegate delegate;
    Scale2f displayScale = FxUtil.getDisplayScale(Screen.getPrimary());

    public FxRow(ObservableList<Row> rows, FxSheetViewDelegate delegate) {
        this.rows = rows;
        this.delegate = delegate;
    }

    public double getRowWidth() {
        getSkin();
        return displayScale.sx() * delegate.getSheetWidthInPoints();
    }

    public double getRowHeight() {
        int idx = getIndex();
        return displayScale.sy() * (idx < 0 ? delegate.getDefaultRowHeight() : delegate.getRowHeightInPoints(rows.get(idx).getRowNumber()));
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
