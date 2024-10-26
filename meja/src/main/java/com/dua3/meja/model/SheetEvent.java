package com.dua3.meja.model;

import com.dua3.utility.data.Pair;
import org.jspecify.annotations.Nullable;

/**
 * SheetEvent is an interface that extends the Event class for handling various
 * events related to a Sheet object.
 */
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

    /**
     * Class representing a zoom change event for a {@link Sheet}.
     * This event indicates that the zoom level of a sheet has changed from an old value to a new value.
     * It extends {@link EventDoubleValueChanged} to handle events where the value change involves double precision numbers.
     */
    class ZoomChanged extends EventDoubleValueChanged<Sheet> implements SheetEvent {
        /**
         * Constructs a new ZoomChanged event.
         *
         * @param source The source of the event, typically the sheet where the zoom change occurred.
         * @param valueOld The old zoom value before the change.
         * @param valueNew The new zoom value after the change.
         */
        public ZoomChanged(Sheet source, double valueOld, double valueNew) {
            super(source, ZOOM_CHANGED, valueOld, valueNew);
        }
    }

    /**
     * The LayoutChanged class represents an event indicating that the layout
     * of a Sheet has been changed. It extends from AbstractEvent and
     * implements the SheetEvent interface.
     */
    class LayoutChanged extends AbstractEvent<Sheet> implements SheetEvent {
        /**
         * Constructs a new LayoutChanged event.
         *
         * @param source the sheet where the layout change occurred.
         */
        public LayoutChanged(Sheet source) {
            super(source, LAYOUT_CHANGED);
        }
    }

    /**
     * The SplitSet class represents an event that indicates a change in the split settings
     * of a Sheet. This class extends from EventValueChanged, capturing the state before
     * and after the split setting change, and implements the SheetEvent interface to conform
     * to the event handling model for Sheets.
     *
     * <p>The event is triggered when the split setting of a Sheet changes, encapsulating
     * the old and new split values represented as a Pair of Integer values for split row and column.
     */
    class SplitSet extends EventValueChanged<Sheet, Pair<Integer, Integer>> implements SheetEvent {
        /**
         * Constructor for creating a SplitSet event.
         *
         * @param source The source sheet where the event originated.
         * @param valueOld The old value pair before the split change.
         * @param valueNew The new value pair after the split change.
         */
        public SplitSet(Sheet source, Pair<Integer, Integer> valueOld, Pair<Integer, Integer> valueNew) {
            super(source, SPLIT_CHANGED, valueOld, valueNew);
        }
    }

    /**
     * Event triggered when the active cell within a sheet changes.
     * <p>This class extends the {@code EventValueChanged} to  handle changes of the active cell
     * within a spreadsheet.
     */
    class ActiveCellChanged extends EventValueChanged<Sheet, Cell> implements SheetEvent {
        /**
         * Constructs an ActiveCellChanged event.
         *
         * @param source   the sheet where the active cell change occurred
         * @param valueOld the previous active cell, can be null
         * @param valueNew the new active cell, can be null
         */
        public ActiveCellChanged(Sheet source, @Nullable Cell valueOld, @Nullable Cell valueNew) {
            super(source, ACTIVE_CELL_CHANGED, valueOld, valueNew);
        }
    }

    /**
     * Represents an event that is triggered when a cell in a sheet changes.
     * This class extends {@link EventValueChanged} with a specific sheet source
     * and provides the cell where the change occurred.
     *
     * @param <V> the type of the value that has changed in the cell.
     */
    abstract class CellChanged<V> extends EventValueChanged<Sheet, V> implements SheetEvent {
        private final Cell cell;

        /**
         * Constructs a new CellChanged event.
         *
         * @param source the sheet where the change occurred
         * @param type the type of the event
         * @param cell the cell that was changed
         * @param valueOld the old value in the cell, may be null
         * @param valueNew the new value in the cell, may be null
         */
        public CellChanged(Sheet source, String type, Cell cell, @Nullable V valueOld, @Nullable V valueNew) {
            super(source, type, valueOld, valueNew);
            this.cell = cell;
        }

        /**
         * Retrieves the cell that was changed.
         *
         * @return the cell that was changed
         */
        public Cell cell() {
            return cell;
        }
    }

    /**
     * The {@code CellValueChanged} represents an event that indicates that the value of a cell in a sheet
     * has changed.
     *
     * <p>This class extends {@code CellChanged} with a type parameter of {@code Object}
     * and implements {@code SheetEvent}.
     *
     * <p>It contains information about the source sheet, the cell in question, the old value,
     * and the new value.
     */
    class CellValueChanged extends CellChanged<Object> implements SheetEvent {
        /**
         * Constructs a new {@code CellValueChanged} event.
         *
         * @param source The source sheet on which the cell value change occurred.
         * @param cell The cell whose value has changed.
         * @param valueOld The old value of the cell, which may be null.
         * @param valueNew The new value of the cell, which may be null.
         */
        public CellValueChanged(Sheet source, Cell cell, @Nullable Object valueOld, @Nullable Object valueNew) {
            super(source, CELL_VALUE_CHANGED, cell, valueOld, valueNew);
        }
    }

    /**
     * Event class representing a change in a cell's style within a sheet.
     *
     * <p>This class extends {@link CellChanged} to include details about the cell whose style has changed.
     * It implements the {@link SheetEvent} interface to indicate it is a type of sheet event.
     */
    class CellStyleChanged extends CellChanged<Object> implements SheetEvent {
        /**
         * Constructs a new CellStyleChanged event.
         *
         * @param source the sheet where the cell style change occurred
         * @param cell the cell whose style has changed
         * @param valueOld the old value of the cell style (may be null)
         * @param valueNew the new value of the cell style (may be null)
         */
        public CellStyleChanged(Sheet source, Cell cell, @Nullable Object valueOld, @Nullable Object valueNew) {
            super(source, CELL_STYLE_CHANGED, cell, valueOld, valueNew);
        }
    }

    /**
     * Event representing the addition of rows to a sheet.
     *
     * <p>Extends {@code AbstractEvent<Sheet>} and implements {@code SheetEvent}.
     * This event is triggered when one or more rows are added to the sheet.
     *
     * <p>The event contains the indices of the first and last row that were added (inclusive).
     */
    class RowsAdded extends AbstractEvent<Sheet> implements SheetEvent {
        private final int first;
        private final int last;

        /**
         * Constructs a RowsAdded event for a given Sheet.
         *
         * @param source the Sheet where the event originated.
         * @param first the index of the first row that was added.
         * @param last the index of the last row that was added.
         */
        public RowsAdded(Sheet source, int first, int last) {
            super(source, ROWS_ADDED);
            this.first = first;
            this.last = last;
        }

        /**
         * Returns the index of the first inserted row.
         *
         * @return the index of the first inserted row
         */
        public int first() {
            return first;
        }

        /**
         * Returns the index of the last inserted row.
         *
         * @return the index of the last inserted row
         */
        public int last() {
            return last;
        }
    }

    /**
     * Event representing the addition of columns to a sheet.
     *
     * <p>Extends {@code AbstractEvent<Sheet>} and implements {@code SheetEvent}.
     * This event is triggered when one or more columns are added to the sheet.
     *
     * <p>The event contains the indices of the first and last column that were added (inclusive).
     */
    class ColumnsAdded extends AbstractEvent<Sheet> implements SheetEvent {
        private final int first;
        private final int last;

        /**
         * Constructs a ColumnsAdded event for a given Sheet.
         *
         * @param source the Sheet where the event originated.
         * @param first the index of the first column that was added.
         * @param last the index of the last column that was added.
         */
        public ColumnsAdded(Sheet source, int first, int last) {
            super(source, COLUMNS_ADDED);
            this.first = first;
            this.last = last;
        }

        /**
         * Returns the index of the first inserted column.
         *
         * @return the index of the first inserted column
         */
        public int first() {
            return first;
        }

        /**
         * Returns the index of the last inserted column.
         *
         * @return the index of the last inserted column.
         */
        public int last() {
            return last;
        }
    }
}
