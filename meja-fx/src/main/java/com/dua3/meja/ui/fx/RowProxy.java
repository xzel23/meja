package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Row;

public class RowProxy {
    public static final RowProxy ROW_PROXY_EMPTY = new RowProxy(Type.EMPTY, null);
    public static final RowProxy ROW_PROXY_CLOLUMN_LABELS = new RowProxy(Type.CLOUMN_LABELS, null);
    public static final RowProxy ROW_PROXY_SPLIT_LINE = new RowProxy(Type.SPLIT_LINE, null);

    enum Type {
        ROW,
        EMPTY,
        CLOUMN_LABELS,
        SPLIT_LINE
    }

    private final Type type;
    private final Row row;

    private RowProxy(Type type, @Nullable Row row) {
        this.type = type;
        this.row = row;
    }

    public static RowProxy row(@Nullable Row row) {
        return row==null ? ROW_PROXY_EMPTY : new RowProxy(Type.ROW, row);
    }

    public Type getType() {
        return type;
    }

    public Row getRow() {
        return row;
    }
}
