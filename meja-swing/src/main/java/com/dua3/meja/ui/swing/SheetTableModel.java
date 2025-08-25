package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.SheetEvent;
import com.dua3.meja.util.TableOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.Flow;

final class SheetTableModel extends AbstractTableModel {
    private static final Logger LOG = LogManager.getLogger(SheetTableModel.class);

    private final Sheet sheet;
    private final boolean firstRowIsHeader;
    private boolean editable;
    @Serial
    private static final long serialVersionUID = 1L;

    private final SheetListener sl;

    SheetTableModel(Sheet sheet, TableOptions... options) {
        this.sheet = sheet;

        Set<TableOptions> optionSet = Set.of(options);
        this.firstRowIsHeader = optionSet.contains(TableOptions.FIRST_ROW_IS_HEADER);
        this.editable = optionSet.contains(TableOptions.EDITABLE);

        sl = new SheetListener();
    }

    final class SheetListener implements Flow.Subscriber<SheetEvent> {

        private static void runOnEDT(Runnable dispatcher) {
            try {
                if (SwingUtilities.isEventDispatchThread()) {
                    dispatcher.run();
                } else {
                    SwingUtilities.invokeAndWait(dispatcher);
                }
            } catch (InvocationTargetException | InterruptedException e) {
                LOG.warn("interrupted", e);
                Thread.currentThread().interrupt();
            }
        }

        private Flow.@Nullable Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            if (this.subscription != null) {
                this.subscription.cancel();
            }

            this.subscription = subscription;
            this.subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(SheetEvent item) {
            switch (item.type()) {
                case SheetEvent.ROWS_ADDED -> {
                    SheetEvent.RowsAdded event = (SheetEvent.RowsAdded) item;
                    int firstRow = convertRowNumberSheetToJTable(event.first());
                    int lastRow = convertRowNumberSheetToJTable(event.last());
                    if (firstRowIsHeader && firstRow == -1) {
                        // header change!
                        LOG.debug("head row added!");
                        runOnEDT(SheetTableModel.this::fireTableStructureChanged);
                    } else {
                        assert firstRow >= 0 : "invalid state detected, firstRowIsHeader=" + firstRowIsHeader + ", first=" + firstRow;
                        runOnEDT(() -> fireTableRowsInserted(firstRow, lastRow));
                    }
                }
                case SheetEvent.CELL_VALUE_CHANGED -> {
                    SheetEvent.CellValueChanged event = (SheetEvent.CellValueChanged) item;
                    Cell cell = event.cell();
                    int i = convertRowNumberSheetToJTable(cell.getRowNumber());
                    int j = cell.getColumnNumber();

                    LOG.debug("cell changed, table row={}, table column={}, firstRowIsHeader={}", i, j, firstRowIsHeader);

                    if (firstRowIsHeader && i == -1) {
                        // header change!
                        LOG.debug("head row data changed!");
                        runOnEDT(SheetTableModel.this::fireTableStructureChanged);
                    } else {
                        runOnEDT(() -> fireTableCellUpdated(i, j));
                    }
                }
                case SheetEvent.LAYOUT_CHANGED, SheetEvent.COLUMNS_ADDED -> {
                    LOG.debug("table structure changed, event: {}", item);
                    runOnEDT(SheetTableModel.this::fireTableStructureChanged);
                }
                default -> LOG.debug("event ignored: {}", item);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("error with subscription", throwable);
        }

        @Override
        public void onComplete() {
            LOG.debug("subscription completed");
            this.subscription = null;
        }

        public void attach() {
            sheet.subscribe(this);
        }

        public void detach() {
            assert subscription != null : "subscription not attached";
            subscription.cancel();
            subscription = null;
        }
    }

    @Override
    public int getColumnCount() {
        return sheet.getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (firstRowIsHeader) {
            return sheet.getRow(0).getCell(columnIndex).toString();
        } else {
            return Sheet.getColumnName(columnIndex);
        }
    }

    @Override
    public int getRowCount() {
        int n = sheet.getRowCount();
        return firstRowIsHeader && n > 0 ? n - 1 : n;
    }

    @Override
    public @Nullable Object getValueAt(int rowIndex, int columnIndex) {
        return sheet.getCellIfExists(convertRowNumberJTableToSheet(rowIndex), columnIndex).flatMap(Cell::get).orElse(null);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editable;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        sheet.getCell(convertRowNumberSheetToJTable(rowIndex), columnIndex).set(aValue);
    }

    /**
     * Convert row number of the sheet to row number of the table.
     *
     * @param i row number in the sheet
     * @return row number in the table
     */
    private int convertRowNumberSheetToJTable(int i) {
        return firstRowIsHeader ? i - 1 : i;
    }

    /**
     * Convert row number of the table to row number of the sheet.
     *
     * @param i row number in the sheet
     * @return row number in the table
     */
    private int convertRowNumberJTableToSheet(int i) {
        return firstRowIsHeader ? i + 1 : i;
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        super.removeTableModelListener(l);

        if (listenerList.getListenerCount() == 0 && sl.subscription != null) {
            sl.subscription.cancel();
            sl.subscription = null;
            LOG.debug("last TableModelListener was removed, detaching sheet listener from sheet");
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        super.addTableModelListener(l);

        if (listenerList.getListenerCount() == 1) {
            sl.attach();
            LOG.debug("first TableModelListener was added, attaching sheet listener to sheet");
        }
    }
}
