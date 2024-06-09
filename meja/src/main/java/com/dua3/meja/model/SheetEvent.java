package com.dua3.meja.model;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.data.Pair;

public interface SheetEvent extends Event<Sheet> {
    /**
     * zoom factor.
     */
    String ZOOM_CHANGED = "ZOOM_CHANGED";
    /**
     * layout change.
     */
    String LAYOUT_CHANGED = "LAYOUT_CHANGED";
    /**
     * split row in sheet.
     */
    String SPLIT_CHANGED = "SPLIT_CHANGED";
    /**
     * active cell in sheet.
     */
    String ACTIVE_CELL_CHANGED = "ACTIVE_CELL_CHANGED";
    /**
     * cell content.
     */
    String CELL_VALUE_CHANGED = "CELL_VALUE_CHANGED";
    /**
     * cell style.
     */
    String CELL_STYLE_CHANGED = "CELL_STYLE_CHANGED";
    /**
     * rows added.
     */
    String ROWS_ADDED = "ROWS_ADDED";
    /**
     * columns added.
     */
    String COLUMNS_ADDED = "COLUMNS_ADDED";

    class ZoomChanged extends EventDoubleValueChanged<Sheet> implements SheetEvent {
        public ZoomChanged(Sheet source, double valueOld, double valueNew) {
            super(source, ZOOM_CHANGED, valueOld, valueNew);
        }
    }

    class LayoutChanged extends AbstractEvent<Sheet> implements SheetEvent {
        public LayoutChanged(Sheet source) {
            super(source, LAYOUT_CHANGED);
        }
    }

    class SplitSet extends EventValueChanged<Sheet, Pair<Integer, Integer>> implements SheetEvent {
        public SplitSet(Sheet source, Pair<Integer, Integer> valueOld, Pair<Integer, Integer> valueNew) {
            super(source, SPLIT_CHANGED, valueOld, valueNew);
        }
    }

    class ActiveCellChanged extends EventValueChanged<Sheet, Cell> implements SheetEvent {
        public ActiveCellChanged(Sheet source, @Nullable Cell valueOld, @Nullable Cell valueNew) {
            super(source, ACTIVE_CELL_CHANGED, valueOld, valueNew);
        }
    }

    abstract class CellChanged<V> extends EventValueChanged<Sheet, V> implements SheetEvent {
        private final Cell cell;

        public CellChanged(Sheet source, String type, Cell cell, @Nullable V valueOld, @Nullable V valueNew) {
            super(source, type, valueOld, valueNew);
            this.cell = cell;
        }

        public Cell cell() {
            return cell;
        }
    }

    class CellValueChanged extends CellChanged<Object> implements SheetEvent {
        public CellValueChanged(Sheet source, Cell cell, @Nullable Object valueOld, @Nullable Object valueNew) {
            super(source, CELL_VALUE_CHANGED, cell, valueOld, valueNew);
        }
    }

    class CellStyleChanged extends CellChanged<Object> implements SheetEvent {
        public CellStyleChanged(Sheet source, Cell cell, @Nullable Object valueOld, @Nullable Object valueNew) {
            super(source, CELL_STYLE_CHANGED, cell, valueOld, valueNew);
        }
    }

    class RowsAdded extends AbstractEvent<Sheet> implements SheetEvent {
        private final int first;
        private final int last;

        public RowsAdded(Sheet source, int first, int last) {
            super(source, ROWS_ADDED);
            this.first = first;
            this.last = last;
        }

        public int first() {
            return first;
        }

        public int last() {
            return last;
        }
    }

    class ColumnsAdded extends AbstractEvent<Sheet> implements SheetEvent {
        private final int first;
        private final int last;

        public ColumnsAdded(Sheet source, int first, int last) {
            super(source, COLUMNS_ADDED);
            this.first = first;
            this.last = last;
        }

        public int first() {
            return first;
        }

        public int last() {
            return last;
        }
    }
}
