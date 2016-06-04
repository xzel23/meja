/*
 *
 */
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.util.MejaHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.EventHandler;
import javafx.event.EventType;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridChange;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.controlsfx.control.spreadsheet.SpreadsheetView.SpanType;

/**
 *
 * @author axel
 */
class GridImpl implements Grid {

    private final Sheet sheet;
    private final Map<EventType<? extends GridChange>, List<EventHandler<? extends GridChange>>> eventHandlers = new HashMap<>();
    private final ObservableList<String> rowHeaders = new ObservableListBase<String>() {
        @Override
        public String get(int index) {
            return Integer.toString(index + 1);
        }

        @Override
        public int size() {
            return sheet.getRowCount();
        }
    };
    private final ObservableList<String> columnHeaders = new ObservableListBase<String>() {
        @Override
        public String get(int index) {
            return MejaHelper.getColumnName(index);
        }

        @Override
        public int size() {
            return sheet.getColumnCount();
        }
    };
    private final ObservableListBase<ObservableList<SpreadsheetCell>> rows = new ObservableListBase<ObservableList<SpreadsheetCell>>() {
        @Override
        public ObservableList<SpreadsheetCell> get(int i) {
            return new ObservableListBase<SpreadsheetCell>() {
                @Override
                public SpreadsheetCell get(int j) {
                    Cell cell = sheet.getCell(j, j);
                    return new SpreadsheetCellBase(j, j, cell.getVerticalSpan(), cell.getHorizontalSpan(), translateCellType(cell.getCellType()));
                }

                @Override
                public int size() {
                    return sheet.getColumnCount();
                }
            };
        }

        @Override
        public int size() {
            return sheet.getRowCount();
        }
    };

    public GridImpl(Sheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public int getRowCount() {
        return sheet.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return sheet.getColumnCount();
    }

    @Override
    public ObservableList<ObservableList<SpreadsheetCell>> getRows() {
        return rows;
    }

    @Override
    public void setCellValue(int row, int column, Object value) {
        sheet.getRow(row).getCell(column).set(value);
    }

    @Override
    public SpanType getSpanType(SpreadsheetView spv, int i, int j) {
        if (i < 0 || j < 0 || i >= getRowCount() || j >= getColumnCount()) {
            return SpanType.NORMAL_CELL;
        }

        Cell cell = sheet.getCell(i, j);
        Cell logicalCell = cell.getLogicalCell();
        if (cell == logicalCell) {
            return cell.getVerticalSpan() == 1 ? SpanType.NORMAL_CELL : SpanType.ROW_VISIBLE;
        }
        if (logicalCell.getRowNumber() == i) {
            return SpanType.COLUMN_SPAN_INVISIBLE;
        }
        if (logicalCell.getColumnNumber() == j) {
            return SpanType.ROW_SPAN_INVISIBLE;
        }
        return SpanType.BOTH_INVISIBLE;
    }

    @Override
    public double getRowHeight(int row) {
        return sheet.getRowHeight(row);
    }

    @Override
    public boolean isRowResizable(int row) {
        return true;
    }

    @Override
    public ObservableList<String> getRowHeaders() {
        return rowHeaders;
    }

    @Override
    public ObservableList<String> getColumnHeaders() {
        return columnHeaders;
    }

    @Override
    public void spanRow(int count, int rowIndex, int colIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void spanColumn(int count, int rowIndex, int colIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRows(Collection<ObservableList<SpreadsheetCell>> rows) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <E extends GridChange> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        eventHandlers.getOrDefault(eventType, new ArrayList<>()).add(eventHandler);
    }

    @Override
    public <E extends GridChange> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        List<EventHandler<? extends GridChange>> handlers = eventHandlers.get(eventType);
        if (handlers != null) {
            handlers.remove(eventHandler);
        }
    }

    public SpreadsheetCellType<?> translateCellType(CellType cellType) {
        switch (cellType) {
            default:
                return SpreadsheetCellType.OBJECT;
        }
    }

}
