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

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.SearchOptions;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.Lock;
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
                return rowNum <= sheet.getLastRowNum();
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
                return colNum <= row.getLastCellNum();
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
        FileType fileType = FileType.forFile(file);

        if (fileType == null) {
            throw new IllegalArgumentException("Could not determine type of file '" + file.getPath() + ".");
        }

        if (!fileType.isSupported(OpenMode.READ)) {
            throw new IllegalArgumentException("Reading is not supported for files of type '" + fileType.getDescription() + ".");
        }

        try {
            return fileType.getFactory().open(file);
        } catch (IOException ex) {
            Logger.getLogger(MejaHelper.class.getName()).log(Level.WARNING, null, ex);
            throw new IOException("Could not load '" + file.getPath() + "'.", ex);
        }
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

    /**
     * Extract file extension.
     * <p>
     * The file extension is the part of the filename from the last dot to the
     * end (including the dot). The extension of the file
     * {@literal /foo/bar/file.txt} is ".txt".
     * </p>
     * <p>
     * If the filename doesn't contain a dot, the extension is the empty string
     * ("").
     * </p>
     *
     * @param file the file
     * @return the file extension including the dot.
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }

    /**
     * Get cell position as string.
     *
     * @param cell
     * @return the cell in Excel conventions, ie "A1" for the first cell.
     */
    public static String getCellPosition(Cell cell) {
        return getColumnName(cell.getColumnNumber()) + (cell.getRowNumber() + 1);
    }

    /**
     * Get cell position as string.
     *
     * @param cell
     * @return the cell in Excel conventions, ie "sheet1!A1" for the first cell.
     */
    public static String getGlobalCellPosition(Cell cell) {
        return cell.getSheet().getSheetName() + "!" + getCellPosition(cell);
    }

    /**
     * Return copy of workbook in a different implementation.
     *
     * @param <WORKBOOK> the workbook class of the target
     * @param clazz {@code Class} instance for the workbook class of the target
     * @param workbook the source workbook
     * @return workbook instance of type {@code WORKBOOK} with the contents of
     * {@code workbook}
     */
    public static <WORKBOOK extends Workbook> WORKBOOK cloneWorkbookAs(Class<WORKBOOK> clazz, Workbook workbook) {
        try {
            WORKBOOK newWorkbook = clazz.getConstructor(Locale.class).newInstance(workbook.getLocale());
            newWorkbook.setUri(workbook.getUri());

            // copy styles
            for (String styleName : workbook.getCellStyleNames()) {
                CellStyle cellStyle = workbook.getCellStyle(styleName);
                CellStyle newCellStyle = newWorkbook.getCellStyle(styleName);
                newCellStyle.copyStyle(cellStyle);
            }

            // copy sheets
            for (int sheetNr = 0; sheetNr < workbook.getNumberOfSheets(); sheetNr++) {
                Sheet sheet = workbook.getSheetByNr(sheetNr);
                Sheet newSheet = newWorkbook.createSheet(sheet.getSheetName());
                newSheet.copy(sheet);
            }
            return newWorkbook;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException("Error cloning workbook: " + ex.getMessage(), ex);
        }
    }

    public static
            Writer createWriter(Appendable app) {
        if (app instanceof Writer) {
            return (Writer) app;
        } else {
            return new AppendableWriter(app);
        }
    }

    /**
     * Copy sheet data.
     * <p>
     * Copies all data from one sheet to another. Sheets may be instances
     * of different implementation classes.
     * </p>
     * @param dst the destination sheet
     * @param src the source sheet
     */
    public static void copySheetData(Sheet dst, Sheet src) {
        // copy split
        dst.splitAt(src.getSplitRow(), src.getSplitColumn());
        // copy column widths
        for (int j = src.getFirstColNum(); j <= src.getLastColNum(); j++) {
            dst.setColumnWidth(j, src.getColumnWidth(j));
        }
        // copy merged regions
        for (RectangularRegion rr : src.getMergedRegions()) {
            dst.addMergedRegion(rr);
        }
        // copy row data
        for (Row row : src) {
            final int i = row.getRowNumber();
            dst.getRow(i).copy(row);
            dst.setRowHeight(i, src.getRowHeight(i));
        }
    }

    /**
     * Find cell containing text in sheet.
     * @param sheet the sheet
     * @param text the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Cell find(Sheet sheet, String text, SearchOptions... options) {
        // EnumSet.of throws IllegalArgumentException if options is empty, so
        // use a standard HashSet instead.
        return find(sheet, text, new HashSet<>(Arrays.asList(options)));
    }

    /**
     * Find cell containing text in sheet.
     * @param sheet the sheet
     * @param text the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Cell find(Sheet sheet, String text, Set<SearchOptions> options) {
        boolean searchFromCurrent = options.contains(SearchOptions.SEARCH_FROM_CURRENT);
        boolean ignoreCase = options.contains(SearchOptions.IGNORE_CASE);
        boolean matchComplete = options.contains(SearchOptions.MATCH_COMPLETE_TEXT);
        boolean updateCurrent = options.contains(SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND);
        boolean searchFormula = options.contains(SearchOptions.SEARCH_FORMLUA_TEXT);

        if (ignoreCase) {
            text = text.toLowerCase();
        }

        Cell cell = searchFromCurrent ? sheet.getCurrentCell() : null;
        int iStart = cell != null ? cell.getRowNumber() : sheet.getLastRowNum();
        int jStart = cell != null ? cell.getColumnNumber() : sheet.getLastColNum();
        int i = iStart;
        int j = jStart;
        do {
            // move to next cell
            if (j < sheet.getRow(i).getLastCellNum()) {
                j++;
            } else {
                j = 0;
                if (i < sheet.getLastRowNum()) {
                    i++;
                } else {
                    i = 0;
                }
            }

            cell = sheet.getCell(i, j);

            // check cell content
            String cellText;
            if (searchFormula && cell.getCellType() == CellType.FORMULA) {
                cellText = cell.getFormula();
            } else {
                cellText = cell.getAsText();
            }

            if (ignoreCase) {
                cellText = cellText.toLowerCase();
            }

            if (matchComplete && cellText.equals(text)
                    || !matchComplete && cellText.contains(text)) {
                // found!
                if (updateCurrent) {
                    sheet.setCurrentCell(cell);
                }
                return cell;
            }
        } while (i != iStart || j != jStart);
        return null;
    }

    /**
     * Find cell containing text in row.
     * @param row the row
     * @param text the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Cell find(Row row, String text, SearchOptions... options) {
        // EnumSet.of throws IllegalArgumentException if options is empty, so
        // use a standard HashSet instead.
        return find(row, text, new HashSet<>(Arrays.asList(options)));
    }

    /**
     * Find cell containing text in row.
     * @param row the row
     * @param text the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Cell find(Row row, String text, Set<SearchOptions> options) {
        boolean searchFromCurrent = options.contains(SearchOptions.SEARCH_FROM_CURRENT);
        boolean ignoreCase = options.contains(SearchOptions.IGNORE_CASE);
        boolean matchComplete = options.contains(SearchOptions.MATCH_COMPLETE_TEXT);
        boolean updateCurrent = options.contains(SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND);
        boolean searchFormula = options.contains(SearchOptions.SEARCH_FORMLUA_TEXT);

        if (ignoreCase) {
            text = text.toLowerCase();
        }

        int jStart = searchFromCurrent ? row.getSheet().getCurrentCell().getColumnNumber() : row.getLastCellNum();
        int j = jStart;
        do {
            // move to next cell
            if (j < row.getLastCellNum()) {
                j++;
            } else {
                j = 0;
            }

            Cell cell = row.getCell(j);

            // check cell content
            String cellText;
            if (searchFormula && cell.getCellType() == CellType.FORMULA) {
                cellText = cell.getFormula();
            } else {
                cellText = cell.getAsText();
            }

            if (ignoreCase) {
                cellText = cellText.toLowerCase();
            }

            if (matchComplete && cellText.equals(text)
                    || !matchComplete && cellText.contains(text)) {
                // found!
                if (updateCurrent) {
                    row.getSheet().setCurrentCell(cell);
                }
                return cell;
            }
        } while (j != jStart);

        return null;
    }

    private MejaHelper() {
    }

}
