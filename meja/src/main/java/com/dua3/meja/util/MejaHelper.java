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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.RefOption;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.SearchOptions;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.text.RichText;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class MejaHelper {

    private static final Logger LOGGER = Logger.getLogger(MejaHelper.class.getName());

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
        return new String(sb);
    }

    /**
     * Get row name as String.
     * @param i the row number as used in Excel spreadsheets
     * @return the row name ("1" for row number 0)
     */
	public static String getRowName(int i) {
		return Integer.toString(i+1);
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
      final int stride = 'z' - 'a' + 1;
      int col = 0;
      for (char c : colName.toLowerCase().toCharArray()) {
        if (c < 'a' || 'z' < c) {
          throw new IllegalArgumentException("'" + colName + "' ist no valid column name.");
        }

        int d = c - 'a' + 1;
        col = col * stride + d;
      }
      return col - 1;
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
            LOGGER.log(Level.WARNING, null, ex);
            throw new IOException("Could not load '" + file.getPath() + "'.", ex);
        }
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
     * Get cell reference as string.
     *
     * @param cell
     * @param options  Options to use
     * @return the cell in Excel conventions, ie "A1" for the first cell.
     */
    public static String getCellRef(Cell cell, RefOption... options) {
        String prefixRow = "";
        String prefixColumn = "";
        String sheet = "";

        for (RefOption o: options) {
            switch (o) {
            case FIX_COLUMN:
                prefixColumn = "$";
                break;
            case FIX_ROW:
                prefixRow = "$";
                break;
            case WITH_SHEET:
                sheet = "'" + cell.getSheet().getSheetName() + "'!";
                break;
            }
        }

        String ref = sheet
                + prefixColumn + getColumnName(cell.getColumnNumber())
                + prefixRow + (cell.getRowNumber() + 1);

        return ref;
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
            newWorkbook.setUri(workbook.getUri().orElse(null));

            // copy styles
            for (String styleName : workbook.getCellStyleNames()) {
                CellStyle cellStyle = workbook.getCellStyle(styleName);
                CellStyle newCellStyle = newWorkbook.getCellStyle(styleName);
                newCellStyle.copyStyle(cellStyle);
            }

            // copy sheets
            for (int sheetNr = 0; sheetNr < workbook.getSheetCount(); sheetNr++) {
                Sheet sheet = workbook.getSheet(sheetNr);
                Sheet newSheet = newWorkbook.createSheet(sheet.getSheetName());
                newSheet.copy(sheet);
            }
            return newWorkbook;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException("Error cloning workbook: " + ex.getMessage(), ex);
        }
    }

    public static Writer createWriter(Appendable app) {
        if (app instanceof Writer) {
            return (Writer) app;
        } else {
            return new AppendableWriter(app);
        }
    }

    /**
     * Copy sheet data.
     * <p>
     * Copies all data from one sheet to another. Sheets may be instances of
     * different implementation classes.
     * </p>
     *
     * @param dst the destination sheet
     * @param src the source sheet
     */
    public static void copySheetData(Sheet dst, Sheet src) {
        // copy split
        dst.splitAt(src.getSplitRow(), src.getSplitColumn());
        // set autofilter
        dst.setAutofilterRow(src.getAutoFilterRow());
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
     *
     * @param sheet the sheet
     * @param text the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return the cell found or {@code null} if nothing found
     */
    public static Optional<Cell> find(Sheet sheet, String text, SearchOptions... options) {
        // EnumSet.of throws IllegalArgumentException if options is empty, so
        // use a standard HashSet instead.
        return find(sheet, text, new HashSet<>(Arrays.asList(options)));
    }

    /**
     * Find cell containing text in sheet.
     *
     * @param sheet the sheet
     * @param text the text to searcg for
     * @param options the {@link SearchOptions} to use
     * @return {@code Optional} holding the cell found or empty
     */
    public static Optional<Cell> find(Sheet sheet, String text, Set<SearchOptions> options) {
        Lock lock = sheet.readLock();
        lock.lock();
        try {
            boolean searchFromCurrent = options.contains(SearchOptions.SEARCH_FROM_CURRENT);
            boolean ignoreCase = options.contains(SearchOptions.IGNORE_CASE);
            boolean matchComplete = options.contains(SearchOptions.MATCH_COMPLETE_TEXT);
            boolean updateCurrent = options.contains(SearchOptions.UPDATE_CURRENT_CELL_WHEN_FOUND);
            boolean searchFormula = options.contains(SearchOptions.SEARCH_FORMLUA_TEXT);

            if (isEmpty(sheet)) {
              return Optional.empty();
            }

            if (ignoreCase) {
                text = text.toLowerCase();
            }

            Cell end = null;
            Cell cell;
            if (searchFromCurrent) {
                cell = nextCell(sheet.getCurrentCell());
            } else {
                cell = sheet.getCell(sheet.getFirstRowNum(), sheet.getFirstColNum());
            }

            while (end==null || !(cell.getRowNumber()==end.getRowNumber() && cell.getColumnNumber()==end.getColumnNumber())) {
                if (end == null) {
                  // remember the first visited cell
                  end = cell;
                }

                // check cell content
                String cellText;
                if (searchFormula && cell.getCellType() == CellType.FORMULA) {
                    cellText = cell.getFormula();
                } else {
                    cellText = cell.toString();
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
                    return Optional.of(cell);
                }

                // move to next cell
                cell = nextCell(cell);
            }

            // not found
            return Optional.empty();
        } finally {
          lock.unlock();
        }
    }

    private static Cell nextCell(Cell cell) {
      // move to next cell
      Row row = cell.getRow();
      int j = cell.getColumnNumber();
      if (j<row.getLastCellNum()) {
        // cell is not the last one in row -> move right
        return row.getCell(j+1);
      } else {
        // cell is the last one in row...
        Sheet sheet = row.getSheet();
        int i = row.getRowNumber();
        if (i < sheet.getLastRowNum()) {
          // not the last row -> move to next row
          row = sheet.getRow(i+1);
        } else {
          // last row -> move to first row
          row = sheet.getRow(sheet.getFirstRowNum());
        }
        // return the first cell of the new row
        return row.getCell(row.getFirstCellNum());
      }
    }

    /**
     * Test if sheet is empty.
     * @param sheet
     *    the sheet to test
     * @return
     *    true, if the sheet is empty
     */
    public static boolean isEmpty(Sheet sheet) {
      return sheet.getRowCount()==0;
    }

    /**
     * Find cell containing text in row.
     *
     * @param row the row
     * @param text the text to search for
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
     *
     * @param row the row
     * @param text the text to search for
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
                cellText = cell.toString();
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

    @SuppressWarnings("deprecation") // because of using cell.set(java.util.Date)
    public static void set(Cell cell, Object arg) {
        if (arg == null) {
            cell.clear();
        } else if (arg instanceof Number) {
            cell.set((Number) arg);
        } else if (arg instanceof Boolean) {
            cell.set((Boolean) arg);
        } else if (arg instanceof LocalDateTime) {
            cell.set((LocalDateTime) arg);
        } else if (arg instanceof Date) {
            cell.set((Date) arg);
        } else if (arg instanceof RichText) {
            cell.set((RichText) arg);
        } else {
            cell.set(String.valueOf(arg));
        }
    }

    private MejaHelper() {
    }

    public static float decodeFontSize(String s) throws NumberFormatException {
        float factor = 1f;
        if (s.endsWith("pt")) {
            s = s.substring(0, s.length()-2);
            factor = 1f;
        } else if (s.endsWith("px")) {
            s = s.substring(0, s.length()-2);
            factor = 96f/72f;
        }
        return factor * Float.parseFloat(s);
    }

}
