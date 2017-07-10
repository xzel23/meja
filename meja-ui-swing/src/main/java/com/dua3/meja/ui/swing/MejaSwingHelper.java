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

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.util.MejaHelper;
import com.dua3.meja.util.Option;
import com.dua3.meja.util.Options;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class MejaSwingHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MejaSwingHelper.class);
    /**
     * Create a TableModel to be used with JTable.
     *
     * @param sheet
     *            the sheet to create a model for
     * @return table model instance of {@code JTableModel} for the sheet
     */
    public static TableModel getTableModel(final Sheet sheet) {
        return new AbstractTableModel() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

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
                return Sheet.getColumnName(columnIndex);
            }

            @Override
            public int getRowCount() {
                return sheet.getRowCount();
            }

            @Override
            public Object getValueAt(int i, int j) {
                Row row = sheet.getRow(i);
                return row == null ? null : row.getCell(j);
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

    }

    /**
     * Show a file open dialog and load the selected workbook.
     *
     * @param parent
     *            the parent component to use for the dialog
     * @param path
     *            the directory to set in the open dialog or the default path
     * @return the workbook the user chose or null if dialog was canceled
     * @throws IOException
     *             if a workbook was selected but could not be loaded
     */
    public static Optional<Workbook> showDialogAndOpenWorkbook(Component parent, Path path) throws IOException {
        boolean defaultFS = path.getFileSystem().equals(FileSystems.getDefault());
        File file = defaultFS ? path.toFile() : new File(".");

        JFileChooser jfc = new JFileChooser(file == null || file.isDirectory() ? file : file.getParentFile());

        for (FileFilter filter : SwingFileFilter.getFilters(OpenMode.READ)) {
            jfc.addChoosableFileFilter(filter);
        }

        int rc = jfc.showOpenDialog(parent);

        if (rc != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        path = jfc.getSelectedFile().toPath();
        FileFilter filter = jfc.getFileFilter();

        if (filter instanceof SwingFileFilter) {
            // get factory from the used filter definition
            final SwingFileFilter swingFileFilter = (SwingFileFilter) filter;
            final FileType fileType = swingFileFilter.getFileType();
            return openWorkbook(parent, path, fileType);
        } else {
            // another filter was used (ie. "all files")
            return Optional.of(MejaHelper.openWorkbook(path));
        }
    }

    public static Optional<Workbook> openWorkbook(Component parent, Path path) throws IOException {
        return openWorkbook(parent, path, FileType.forPath(path).orElse(FileType.CSV));
    }

    public static Optional<Workbook> openWorkbook(Component parent, Path path, final FileType fileType) throws IOException {
        if (fileType==null) {
            return Optional.empty();
        }

        final WorkbookFactory<?> factory = fileType.getFactory();

        // ask user for file type specific settings
        List<Option<?>> settings = fileType.getSettings();
        Options importSettings = Options.empty(); // default is empty
        if (!settings.isEmpty()) {
            SettingsDialog dialog = new SettingsDialog(parent, fileType.name() + " - Settings",
                    "Please verify the import settings:", settings);
            dialog.setVisible(true);
            importSettings = dialog.getResult();
        }

        // load
        return Optional.of(factory.open(path, importSettings));
    }

    /**
     * Show file selection dialog and save workbook.
     * <p>
     * A file selection dialog is shown and the workbook is saved to the
     * selected file. If the file already exists, a confirmation dialog is
     * shown, asking the user whether to overwrite the file.
     * </p>
     *
     * @param parent
     *            the parent component for the dialog
     * @param workbook
     *            the workbook to save
     * @param path
     *            the path to set the default path in the dialog
     * @return the URI the file was saved to or {@code null} if the user
     *         canceled the dialog
     * @throws IOException
     *             if an exception occurs while saving
     */
    public static Optional<Path> showDialogAndSaveWorkbook(Component parent, Workbook workbook, Path path)
            throws IOException {
        boolean defaultFS = path.getFileSystem().equals(FileSystems.getDefault());
        File file = defaultFS ? path.toFile() : new File(".");

        JFileChooser jfc = new JFileChooser(file == null || file.isDirectory() ? file : file.getParentFile());

        int rc = jfc.showSaveDialog(parent);

        if (rc != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        file = jfc.getSelectedFile();

        if (file.exists()) {
            rc = JOptionPane.showConfirmDialog(
                    parent,
                    "File '" + file.getAbsolutePath() + "' already exists. Overwrite?",
                    "File exists",
                    JOptionPane.YES_NO_OPTION);
            if (rc != JOptionPane.YES_OPTION) {
                LOGGER.info("User chose not to overwrite file.");
                return Optional.empty();
            }
        }

        Optional<FileType> type = FileType.forFile(file);
        if (type.isPresent()) {
            type.get().getWriter().write(workbook, file.toPath());
        } else {
            workbook.write(file.toPath());
        }

        return Optional.of(file.toPath());
    }

    public static Color toAwtColor(com.dua3.meja.model.Color color) {
        return new java.awt.Color(color.argb());
    }

    public static Color toAwtColor(String s) {
        return toAwtColor(com.dua3.meja.model.Color.valueOf(s));
    }

    private MejaSwingHelper() {
    }
}
