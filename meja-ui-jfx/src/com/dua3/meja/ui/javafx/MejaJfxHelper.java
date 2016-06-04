/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dua3.meja.ui.javafx;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.util.MejaHelper;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridChange;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class MejaJfxHelper {

    /**
     * Create a TableModel to be used with JTable.
     *
     * @param sheet the sheet to create a model for
     * @return table model instance of {@code JTableModel} for the sheet
     */
    public static Grid getGrid(final Sheet sheet) {
        return new GridImpl(sheet);
    }

    /**
     * Show a file open dialog and load the selected workbook.
     *
     * @param parent the parent component to use for the dialog
     * @param file the directory to set in the open dialog or the default file
     * @return the workbook the user chose or null if dialog was canceled
     * @throws IOException if a workbook was selected but could not be loaded
     */
    public static Workbook showDialogAndOpenWorkbook(Window parent, File file) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(file == null || file.isDirectory() ? file : file.getParentFile());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
        fc.getExtensionFilters().addAll(getExtensionFilters(OpenMode.READ));

        file = fc.showOpenDialog(parent);

        Workbook workbook = null;
        if (file != null) {
            FileChooser.ExtensionFilter ef = fc.getSelectedExtensionFilter();
            Optional<FileType> type = Arrays.stream(FileType.values())
                    .filter((ft)->ft.getDescription().equals(ef.getDescription()) && Arrays.asList(ft.getExtensions()).equals(ef.getExtensions()))
                    .findFirst();

            if (type.isPresent()) {
                // load workbook using the factory from the used filter definition
                final WorkbookFactory factory = type.get().getFactory();
                workbook = factory.open(file);
            } else {
                // another filter was used (ie. "all files")
                workbook = MejaHelper.openWorkbook(file);
            }
        }
        return workbook;
    }

    public static FileChooser.ExtensionFilter[] getExtensionFilters(OpenMode mode) {
        return Arrays.stream(FileType.values())
                .filter(ft -> ft.isSupported(mode))
                .map(ft -> new FileChooser.ExtensionFilter(ft.getDescription(), ft.getExtensions()))
                .toArray((size) -> new FileChooser.ExtensionFilter[size]);
    }

    /**
     * Show file selection dialog and save workbook.
     * <p>
     * A file selection dialog is shown and the workbook is saved to the
     * selected file. If the file already exists, a confirmation dialog is
     * shown, asking the user whether to overwrite the file.</p>
     *
     * @param parent the parent component for the dialog
     * @param workbook the workbook to save
     * @param file the file to set the default path in the dialog
     * @return the URI the file was saved to or {@code null} if the user
     * canceled the dialog
     * @throws IOException if an exception occurs while saving
     */
    public static URI showDialogAndSaveWorkbook(Component parent, Workbook workbook, File file) throws IOException {
        JFileChooser jfc = new JFileChooser(file == null || file.isDirectory() ? file : file.getParentFile());

        int rc = jfc.showSaveDialog(parent);

        URI uri = null;
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();

            if (file.exists()) {
                rc = JOptionPane.showConfirmDialog(
                        parent,
                        "File '" + file.getAbsolutePath() + "' already exists. Overwrite?",
                        "File exists",
                        JOptionPane.YES_NO_OPTION);
                if (rc != JOptionPane.YES_OPTION) {
                    Logger.getLogger(MejaHelper.class.getName()).log(Level.INFO, "User selected not to overwrite file.");
                    return null;
                }
            }

            FileType type = FileType.forFile(file);
            if (type != null) {
                type.getWriter().write(workbook, file);
            } else {
                workbook.write(file, true);
            }
            uri = file.toURI();

        }
        return uri;
    }

    private MejaJfxHelper() {
    }

    private static class GridImpl implements Grid {

        private final Sheet sheet;

        private final Map<EventType<? extends GridChange>, List<EventHandler<? extends GridChange>>> eventHandlers = new HashMap<>();

        private final ObservableList<String> rowHeaders = new ObservableListBase<String>() {
            @Override
            public String get(int index) {
                return Integer.toString(index+1);
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

        private final ObservableListBase<ObservableList<SpreadsheetCell>> rows =
                new ObservableListBase<ObservableList<SpreadsheetCell>>() {
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
        public SpreadsheetView.SpanType getSpanType(SpreadsheetView spv, int row, int column) {
            Cell cell = sheet.getCell(row, column);
            Cell logicalCell = cell.getLogicalCell();

            if (cell==logicalCell) {
                return cell.getVerticalSpan() == 1
                        ? SpreadsheetView.SpanType.NORMAL_CELL
                        : SpreadsheetView.SpanType.ROW_VISIBLE;
            }

            if (logicalCell.getRowNumber()==row) {
                return SpreadsheetView.SpanType.COLUMN_SPAN_INVISIBLE;
            }

            if (logicalCell.getColumnNumber()==column) {
                return SpreadsheetView.SpanType.ROW_SPAN_INVISIBLE;
            }

            return SpreadsheetView.SpanType.BOTH_INVISIBLE;
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
            if (handlers!=null) {
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

}
