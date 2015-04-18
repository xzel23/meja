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
package com.dua3.meja.model;

import java.text.DateFormat;
import java.text.NumberFormat;
import javax.swing.table.TableModel;

/**
 *
 * @author axel
 */
public interface Sheet extends Iterable<Row> {

    /**
     * Get table model suitable for displaying sheet data in a JTable.
     *
     * @return table model
     */
    TableModel getTableModel();

    /**
     * Get name of sheet.
     *
     * @return name of sheet
     */
    String getSheetName();

    /**
     * Get number of first used column.
     * @return the first used column number
     */
    int getFirstColNum();

    /**
     * Get number of first used row.
     * @return the first used column number
     */
    int getFirstRowNum();

    /**
     * Get number of last used column.
     * @return the last used column number
     */
    int getLastColNum();

    /**
     * Get number of last used row.
     * @return the last used row number
     */
    int getLastRowNum();

    /**
     * Get number of columns.
     * @return number of columns in this sheet
     */
    int getNumberOfColumns();

    /**
     * Get number of rows.
     * @return number of rows in this sheet
     */
    int getNumberOfRows();

    /**
     * Get column width.
     * @param j the column number
     * @return width of the column with number {@code j} in points
     */
    float getColumnWidth(int j);

    /**
     * Get row heigt.
     * @param i the row number
     * @return height of the row with number {@code i} in points
     */
    float getRowHeight(int i);

    /**
     * Get row.
     * @param i the row number
     * @return the row with row number {@code i}
     */
    Row getRow(int i);

    /**
     * Get workbook.
     * @return the workbook this sheet belongs to
     */
    Workbook getWorkbook();

    /**
     * Get cell.
     * @param i the row number
     * @param j the column number
     * @return the cell at row {@code i} and column {@code j}
     */
    Cell getCell(int i, int j);

    /**
     * Split (freeze) view.
     *
     * Splits the sheet so that rows <em>above</em> i and columns
     * <em>to the left</em> of j remain in view when scrolling.
     *
     * @param i row number
     * @param j column number
     */
    void splitAt(int i, int j);

    /**
     * Get the split row.
     * @return the number of the first row that will not remain at a fixed position when scrolling
     */
    int getSplitRow();

    /**
     * Get the frozen column.
     * @return the number of the first column that will not remain at a fixed position when scrolling
     */
    int getSplitColumn();

    /**
     * Adjusts the size of the column to its contents (optional operation).
     *
     * @param j the column to resize
     */
    void autoSizeColumn(int j);

    /**
     * Sets an automatic filter on the given row (optional operation).
     *
     * @param i the row number
     */
    void setAutofilterRow(int i);

    /**
     * Get number format for this sheet.
     * <p>
     * The number format is used when parsing user input.
     * </p>
     * @return the number format for this sheet
     */
    NumberFormat getNumberFormat();

    /**
     * Get date format for this sheet.
     * <p>
     * The date format is used when parsing user input.
     * </p>
     * @return the date format for this sheet
     */
    DateFormat getDateFormat();
}
