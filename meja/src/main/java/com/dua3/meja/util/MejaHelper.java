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
package com.dua3.meja.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.io.WorkbookReader;
import com.dua3.meja.model.Cell;
import com.dua3.meja.model.CellType;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.SearchOptions;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.utility.lang.LangUtil;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class MejaHelper {

    private static final Logger LOGGER = Logger.getLogger(MejaHelper.class.getName());

    /**
     * Find cell containing text in row.
     *
     * @param row     the row
     * @param text    the text to search for
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
     * @param row     the row
     * @param text    the text to search for
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
            text = text.toLowerCase(Locale.ROOT);
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
                cellText = cellText.toLowerCase(Locale.ROOT);
            }

            if (matchComplete && cellText.equals(text) || !matchComplete && cellText.contains(text)) {
                // found!
                if (updateCurrent) {
                    row.getSheet().setCurrentCell(cell);
                }
                return cell;
            }
        } while (j != jStart);

        return null;
    }

    /**
     * Find cell containing text in sheet.
     *
     * @param sheet   the sheet
     * @param text    the text to searcg for
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
     * @param sheet   the sheet
     * @param text    the text to searcg for
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
                text = text.toLowerCase(Locale.ROOT);
            }

            Cell end = null;
            Cell cell;
            if (searchFromCurrent) {
                cell = nextCell(sheet.getCurrentCell());
            } else {
                cell = sheet.getCell(sheet.getFirstRowNum(), sheet.getFirstColNum());
            }

            while (end == null || !(cell.getRowNumber() == end.getRowNumber()
                    && cell.getColumnNumber() == end.getColumnNumber())) {
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
                    cellText = cellText.toLowerCase(Locale.ROOT);
                }

                if (matchComplete && cellText.equals(text) || !matchComplete && cellText.contains(text)) {
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

    /**
     * Test if sheet is empty.
     *
     * @param sheet the sheet to test
     * @return true, if the sheet is empty
     */
    public static boolean isEmpty(Sheet sheet) {
        return sheet.getRowCount() == 0;
    }

    private static Cell nextCell(Cell cell) {
        // move to next cell
        Row row = cell.getRow();
        int j = cell.getColumnNumber();
        if (j < row.getLastCellNum()) {
            // cell is not the last one in row -> move right
            return row.getCell(j + 1);
        } else {
            // cell is the last one in row...
            Sheet sheet = row.getSheet();
            int i = row.getRowNumber();
            if (i < sheet.getLastRowNum()) {
                // not the last row -> move to next row
                row = sheet.getRow(i + 1);
            } else {
                // last row -> move to first row
                row = sheet.getRow(sheet.getFirstRowNum());
            }
            // return the first cell of the new row
            return row.getCell(row.getFirstCellNum());
        }
    }

    /**
     * Open workbook file.
     * <p>
     * This method inspects the file name extension to determine which factory
     * should be used for loading. If there are multiple factories registered for
     * the extension, the matching factories are tried in sequential order. If
     * loading succeeds, the workbook is returned.
     * </p>
     * 
     * @param path the workbook path
     * @return the workbook loaded from file
     * @throws IOException if workbook could not be loaded
     */
    public static Workbook openWorkbook(Path path) throws IOException {
        FileType fileType = FileType.forPath(path).orElseThrow(
                () -> new IllegalArgumentException("Could not determine type of file '" + path.toString() + "."));

        LangUtil.check(fileType.isSupported(OpenMode.READ), "Reading is not supported for files of type '%s'.",
                fileType.getName());

        try {
            WorkbookFactory<?> factory = fileType.factory();
            return factory.open(path);
        } catch (IOException ex) {
            String msg = "Could not load workbook '" + path + "'.";
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new IOException(msg, ex);
        }
    }

    private MejaHelper() {
    }

}
