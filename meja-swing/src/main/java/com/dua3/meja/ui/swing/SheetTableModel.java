package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.TableOptions;
import com.dua3.utility.swing.SwingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

final class SheetTableModel extends AbstractTableModel {
    private static final Logger LOG = LoggerFactory.getLogger(MejaSwingHelper.class);

    private final Sheet sheet;
    private final boolean firstRowIsHeader;
    private boolean editable = false;
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
            final Runnable dispatcher;
            switch (evt.getPropertyName()) {
                case Sheet.PROPERTY_ROWS_ADDED -> {
                    Sheet.RowInfo ri = (Sheet.RowInfo) evt.getNewValue();
                    dispatcher = () -> fireTableRowsInserted(getRowNumber(ri.firstRow()),
                            getRowNumber(ri.lastRow()));
                }
                case Sheet.PROPERTY_CELL_CONTENT -> {
                    if (firstRowIsHeader && ((Cell) evt.getSource()).getRowNumber() == 0) {
                        dispatcher = SheetTableModel.this::fireTableStructureChanged;
                    } else {
                        assert evt.getSource() instanceof Cell;
                        Cell cell = (Cell) evt.getSource();
                        int i = cell.getRowNumber();
                        int j = cell.getColumnNumber();
                        dispatcher = () -> fireTableCellUpdated(i, j);
                    }
                }
                case Sheet.PROPERTY_LAYOUT_CHANGED, Sheet.PROPERTY_COLUMNS_ADDED ->
                        dispatcher = SheetTableModel.this::fireTableStructureChanged;
                default -> dispatcher = () -> LOG.debug("ignored event: {}", evt);
            }
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
    public Class<?> getColumnClass(int columnIndex) {
        return Object.class;
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
        // use getXXXIfExists() to avoid side effects
        return sheet.getCellIfExists(getRowNumber(i), j).map(Cell::get).orElse(null);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editable;
    }

    @Override
    public void setValueAt(Object value, int i, int j) {
        sheet.getCell(getRowNumber(i), j).set(value);
    }

    private int getRowNumber(int i) {
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
