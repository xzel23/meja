package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Row;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;

public class FxRow extends IndexedCell<Row> {
    private final ObservableList<Row> rows;

    private double defaultRowHeight = 12;

    private double rowWidth;
    private double rowHeight;

    public FxRow(ObservableList<Row> rows, double width) {
        this.rows = rows;
        this.rowWidth = width;
    }

    public double getRowWidth() {
        return rowWidth;
    }

    public double getRowHeight() {
        return rowHeight;
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
            rowHeight = defaultRowHeight;
        } else {
            setWidth(rowWidth);
            setHeight(item.getSheet().getRowHeight(item.getRowNumber()));
            setText("row " + item.getRowNumber());
            rowHeight=item.getRowHeight();
        }
    }

    @Override
    protected Skin<FxRow> createDefaultSkin() {
        return new FxRowSkin(this);
    }


}
