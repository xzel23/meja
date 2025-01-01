package com.dua3.meja.ui.fx;

import com.dua3.meja.model.Row;
import org.jspecify.annotations.Nullable;

/**
 * The RowProxy class represents a proxy for a single row in a sheet. It is needed to differentiate between actual
 * rows of the sheet, and the split line that for technical reasons is also implemented as an {@link FxRow} instance.
 */
public class RowProxy {
    /**
     * Represents a constant instance of RowProxy with an empty type and null row.
     */
    public static final RowProxy ROW_PROXY_EMPTY = new RowProxy(Type.EMPTY, null);
    /**
     * Represents a proxy for the column labels of the rows in a sheet.
     */
    public static final RowProxy ROW_PROXY_CLOLUMN_LABELS = new RowProxy(Type.CLOUMN_LABELS, null);
    /**
     * The ROW_PROXY_SPLIT_LINE represents a proxy for the split line in a sheet.
     */
    public static final RowProxy ROW_PROXY_SPLIT_LINE = new RowProxy(Type.SPLIT_LINE, null);

    /**
     * The {@code Type} enum defines the different types of rows that a {@link RowProxy} object can represent in a sheet.
     * This is used to differentiate actual rows from other special representations like empty rows, column labels, and split lines.
     */
    public enum Type {
        /** Represents an actual data row in the sheet. */
        ROW,
        /** Represents an empty row in the sheet. */
        EMPTY,
        /** Represents a row containing column labels in the sheet. */
        CLOUMN_LABELS,
        /** Represents a split line in the sheet, inserted after fixed rows on top. */
        SPLIT_LINE
    }

    /**
     * Represents the type of this {@link RowProxy} object.
     * The Type enum consists of the following values:
     * - ROW: Represents a regular row in a sheet.
     * - EMPTY: Represents an empty row.
     * - COLUMN_LABELS: Represents a row containing column labels.
     * - SPLIT_LINE: Represents a split line in the sheet.
     */
    private final Type type;
    /**
     *
     */
    private final @Nullable Row row;

    /**
     * The RowProxy class represents a proxy for a single row in a sheet. It is used to differentiate between actual rows of the sheet and other types of rows, such as empty rows
     * , column label rows, and split lines.
     *
     * @param type the type of the row proxy
     * @param row the actual row object, or null if the row proxy represents an empty row, column label row, or split line
     */
    private RowProxy(Type type, @Nullable Row row) {
        this.type = type;
        this.row = row;
    }

    /**
     * Returns a {@code RowProxy} object representing the given row. If the row is null, it returns {@link RowProxy#ROW_PROXY_EMPTY}.
     *
     * @param row the row to create a RowProxy for
     * @return a RowProxy object representing the given row, or ROW_PROXY_EMPTY if row is null
     */
    public static RowProxy row(@Nullable Row row) {
        return row == null ? ROW_PROXY_EMPTY : new RowProxy(Type.ROW, row);
    }

    /**
     * Returns the type of the RowProxy object.
     *
     * @return the type of the RowProxy object
     */
    public Type getType() {
        return type;
    }

    /**
     * Retrieves the row associated with this RowProxy instance.
     *
     * @return the row associated with this RowProxy instance
     */
    public Row getRow() {
        assert type == Type.ROW && row != null;
        return row;
    }
}
