package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.TableOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

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

    final class SheetListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case Sheet.PROPERTY_ROWS_ADDED -> {
                    Sheet.RowInfo ri = (Sheet.RowInfo) evt.getNewValue();
                    int firstRow = convertRowNumberSheetToJTable(ri.firstRow());
                    int lastRow = convertRowNumberSheetToJTable(ri.lastRow());

                    LOG.debug("rows added, firstRow={}, lastRow={}, firstRowIsHeader={}", firstRow, lastRow, firstRowIsHeader);

                    if (firstRowIsHeader && firstRow == -1) {
                        // header change!
                        LOG.debug("head row added!");
                        runOnEDT(SheetTableModel.this::fireTableStructureChanged);
                    } else {
                        assert firstRow >= 0 : "invalid state detected, firstRowIsHeader=" + firstRowIsHeader + ", firstRow=" + firstRow;
                        runOnEDT(() -> fireTableRowsInserted(firstRow, lastRow));
                    }
                }
                case Sheet.PROPERTY_CELL_CONTENT -> {
                    if (!(evt.getSource() instanceof Cell cell)) {
                        throw new IllegalStateException("event source is not a Cell: " + evt.getSource().getClass());
                    }
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
                case Sheet.PROPERTY_LAYOUT_CHANGED, Sheet.PROPERTY_COLUMNS_ADDED -> {
                    LOG.debug("table structure changed, event: {}", evt);
                    runOnEDT(SheetTableModel.this::fireTableStructureChanged);
                }
                default -> LOG.debug("event igored: {}", evt);
            }
        }

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

        public void detach() {
            sheet.removePropertyChangeListener(this);
        }

        public void attach() {
            sheet.addPropertyChangeListener(this);
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
    public Object getValueAt(int i, int j) {
        return sheet.getCellIfExists(convertRowNumberJTableToSheet(i), j).flatMap(Cell::get).orElse(null);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editable;
    }

    @Override
    public void setValueAt(Object value, int i, int j) {
        sheet.getCell(convertRowNumberSheetToJTable(i), j).set(value);
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

        if (listenerList.getListenerCount() == 0) {
            sl.detach();
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
