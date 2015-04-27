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
package com.dua3.meja.util;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.WorkbookFactory.FilterDef;
import com.dua3.meja.model.WorkbookFactory.OpenMode;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
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
public class MejaHelper {

    private MejaHelper() {
    }

    /**
     * Translate column number to column name.
     *
     * @param j the column number
     * @return the column name
     */
    public static String getColumnName(int j) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) ('A' + j % 26));
        j /= 26;
        while (j > 0) {
            sb.insert(0, (char) ('A' + j % 26 - 1));
            j /= 26;
        }
        return sb.toString();
    }

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
     * Create row iterator.
     *
     * @param sheet the sheet for which to create a row iterator
     * @return row iterator for {@code sheet}
     */
    public static Iterator<Row> createRowIterator(final Sheet sheet) {
        return new Iterator<Row>() {

            private int rowNum = sheet.getFirstRowNum();

            @Override
            public boolean hasNext() {
                return rowNum < sheet.getLastRowNum();
            }

            @Override
            public Row next() {
                return sheet.getRow(rowNum++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing of rows is not supported.");
            }
        };
    }

    /**
     * Create cell iterator.
     *
     * @param row the row for which to create a cell iterator
     * @return cell iterator for {@code row}
     */
    public static Iterator<Cell> createCellIterator(final Row row) {
        return new Iterator<Cell>() {

            private int colNum = row.getFirstCellNum();

            @Override
            public boolean hasNext() {
                return colNum < row.getLastCellNum();
            }

            @Override
            public Cell next() {
                return row.getCell(colNum++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing of rows is not supported.");
            }
        };
    }

    /**
     * Translate column name to column number.
     *
     * @param colName the name of the column, ie. "A", "B",... , "AA", "AB",...
     * @return the column number
     * @throws IllegalArgumentException if {@code colName} is not a valid column
     * name
     */
    public static int getColumnNumber(String colName) {
        int col = 0;
        for (char c : colName.toLowerCase().toCharArray()) {
            if (c < 'a' || 'z' < c) {
                throw new IllegalArgumentException("'" + colName + "' ist no valid column name.");
            }

            col = col * ('z' - 'a' + 1) + (c - 'a' + 1);
        }
        return col - 1;
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
        JFileChooser jfc = new JFileChooser(file.isDirectory() ? file : file.getParentFile());

        for (FilterDef filterDef : WorkbookFactory.getFileFilters(WorkbookFactory.OpenMode.READ)) {
            jfc.addChoosableFileFilter(filterDef);
        }

        int rc = jfc.showOpenDialog(parent);

        Workbook workbook = null;
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();
            FileFilter filter = jfc.getFileFilter();

            if (filter instanceof WorkbookFactory.FilterDef) {
                // load workbook using the factory from the used filter definition
                final WorkbookFactory factory = ((WorkbookFactory.FilterDef) filter).getFactory();
                workbook = factory.open(file);
            } else {
                // another filter was used (ie. "all files")
                workbook = openWorkbook(file);
            }
        }
        return workbook;
    }

    /**
     * Open workbook file.
     * <p>
     * This method inspects the file name extension to determine which factory
     * should be used for loading. If there are multiple factories registered
     * for the extension, the matching factories are tried in sequential order.
     * If loading succeeds, the workbook is returned.
     *
     * @param file the workbook file
     * @return the workbook loaded from file
     * @throws IOException if workbook could not be loaded
     */
    public static Workbook openWorkbook(File file) throws IOException {
        for (FilterDef filterDef : WorkbookFactory.getFileFilters(OpenMode.READ)) {
            try {
                final WorkbookFactory factory = filterDef.getFactory();
                if (filterDef.accept(file)) {
                    return factory.open(file);
                }
            } catch (IOException ex) {
                Logger.getLogger(MejaHelper.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        throw new IOException("Could not load '" + file.getPath() + "' with any of the available filters.");
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
        JFileChooser jfc = new JFileChooser(file.isDirectory() ? file : file.getParentFile());

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

            workbook.write(file, true);
            uri = file.toURI();

        }
        return uri;
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }
}
