/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Sheet.RowInfo;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.TableOptions;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import com.dua3.utility.swing.SwingFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public final class MejaSwingHelper {

    private static final class SheetTableModel extends AbstractTableModel {
        private final Sheet sheet;
        private final boolean firstRowIsHeader;
        private boolean editable = false;
        @Serial
        private static final long serialVersionUID = 1L;

        private final SheetListener sl;

        private SheetTableModel(Sheet sheet, TableOptions... options) {
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
                        RowInfo ri = (RowInfo) evt.getNewValue();
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
                    SwingUtilities.invokeAndWait(dispatcher);
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
            return Cell.class;
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
            return sheet.getCellIfExists(i,j).map(Cell::get).orElse(null);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return editable;
        }

        @Override
        public void setValueAt(Object value, int i, int j) {
            sheet.getCell(i,j).set(value);
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

    private static final Logger LOG = LoggerFactory.getLogger(MejaSwingHelper.class);

    /**
     * Create a TableModel to be used with JTable.
     *
     * @param sheet the sheet to create a model for
     * @param options the options to use
     * @return table model instance of {@code JTableModel} for the sheet
     */
    public static TableModel getTableModel(final Sheet sheet, TableOptions... options) {
        return new SheetTableModel(sheet, options);
    }


    /**
     * Show a file open dialog and load the selected workbook.
     *
     * @param parent the parent component to use for the dialog
     * @param uri    the directory to set in the open dialog or the default path
     * @return the workbook the user chose or null if dialog was canceled
     * @throws IOException if a workbook was selected but could not be loaded
     */
    public static Optional<Workbook> showDialogAndOpenWorkbook(Component parent, URI uri) throws IOException {
        Path path = IoUtil.toPath(uri);
        boolean defaultFS = path.getFileSystem().equals(FileSystems.getDefault());
        File file = defaultFS ? path.toFile() : new File(".");

        JFileChooser jfc = new JFileChooser(file.isDirectory() ? file : file.getParentFile());

        for (FileFilter filter : SwingFileFilter.getFilters(OpenMode.READ, Workbook.class)) {
            jfc.addChoosableFileFilter(filter);
        }

        int rc = jfc.showOpenDialog(parent);

        if (rc != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        return openWorkbook(parent, jfc.getSelectedFile().toURI());
    }

    public static Optional<Workbook> openWorkbook(Component parent, URI uri) throws IOException {
        FileType<Workbook> fileType = FileType.forUri(uri, Workbook.class)
                .orElseThrow(() -> new IllegalArgumentException("unknown filetype: " + uri));

        // load
        return Optional.of(fileType.read(uri, t -> showOptionsDialog(parent, t)));
    }

    private static Arguments showOptionsDialog(Component parent, FileType<?> fileType) {
        Collection<Option<?>> settings = fileType.getSettings();
        Arguments importSettings = Arguments.empty(); // default is empty
        if (!settings.isEmpty()) {
            SettingsDialog dialog = new SettingsDialog(parent, fileType.getName() + " - Settings",
                    "Please verify the import settings:", settings);
            dialog.setVisible(true);
            importSettings = dialog.getResult();
        }
        return importSettings;
    }

    /**
     * Show file selection dialog and save workbook.
     * <p>
     * A file selection dialog is shown and the workbook is saved to the selected
     * file. If the file already exists, a confirmation dialog is shown, asking the
     * user whether to overwrite the file.
     * </p>
     *
     * @param parent   the parent component for the dialog
     * @param workbook the workbook to save
     * @param uri      the URI to set the default path in the dialog
     * @return the URI the file was saved to or {@code null} if the user canceled
     * the dialog
     * @throws IOException if an exception occurs while saving
     */
    public static Optional<URI> showDialogAndSaveWorkbook(Component parent, Workbook workbook, URI uri)
            throws IOException {
        Path path = IoUtil.toPath(uri);
        boolean defaultFS = path.getFileSystem().equals(FileSystems.getDefault());
        File file = defaultFS ? path.toFile() : new File(".");

        JFileChooser jfc = new JFileChooser(file.isDirectory() ? file : file.getParentFile());

        int rc = jfc.showSaveDialog(parent);

        if (rc != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        file = jfc.getSelectedFile();

        if (file.exists()) {
            rc = JOptionPane.showConfirmDialog(parent,
                    "File '" + file.getAbsolutePath() + "' already exists. Overwrite?", "File exists",
                    JOptionPane.YES_NO_OPTION);
            if (rc != JOptionPane.YES_OPTION) {
                LOG.debug("user chose not to overwrite file");
                return Optional.empty();
            }
        }

        Optional<FileType<Workbook>> type = FileType.forUri(file.toURI(), Workbook.class);
        if (type.isPresent()) {
            type.get().write(file.toURI(), workbook);
        } else {
            workbook.write(file.toURI());
        }

        return Optional.of(file.toURI());
    }

    private MejaSwingHelper() {
    }
}
