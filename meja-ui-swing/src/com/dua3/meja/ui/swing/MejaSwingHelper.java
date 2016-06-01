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
package com.dua3.meja.ui.swing;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.util.MejaHelper;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class MejaSwingHelper {

    /**
     * Create a TableModel to be used with JTable.
     *
     * @param sheet the sheet to create a model for
     * @return table model instance of {@code JTableModel} for the sheet
     */
    @SuppressWarnings("serial")
    public static TableModel getTableModel(final Sheet sheet) {
        return new AbstractTableModel() {

            @Override
            public int getRowCount() {
                return sheet.getNumberOfRows();
            }

            @Override
            public int getColumnCount() {
                return sheet.getNumberOfColumns();
            }

            @Override
            public String getColumnName(int columnIndex) {
                return MejaHelper.getColumnName(columnIndex);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Cell.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public Object getValueAt(int i, int j) {
                Row row = sheet.getRow(i);
                Cell cell = row == null ? null : row.getCell(j);
                return cell;
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
     * @param parent the parent component to use for the dialog
     * @param file the directory to set in the open dialog or the default file
     * @return the workbook the user chose or null if dialog was canceled
     * @throws IOException if a workbook was selected but could not be loaded
     */
    public static Workbook showDialogAndOpenWorkbook(Component parent, File file) throws IOException {
        JFileChooser jfc = new JFileChooser(file == null || file.isDirectory() ? file : file.getParentFile());

        for (FileFilter filter : FileType.getFileFilters(OpenMode.READ)) {
            jfc.addChoosableFileFilter(filter);
        }

        int rc = jfc.showOpenDialog(parent);

        Workbook workbook = null;
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();
            FileFilter filter = jfc.getFileFilter();

            if (filter instanceof FileType.FileFilter) {
                // load workbook using the factory from the used filter definition
                final WorkbookFactory factory = ((FileType.FileFilter) filter).getFactory();
                workbook = factory.open(file);
            } else {
                // another filter was used (ie. "all files")
                workbook = MejaHelper.openWorkbook(file);
            }
        }
        return workbook;
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

    private MejaSwingHelper() {
    }

}
