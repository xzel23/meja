/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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

import javax.swing.table.TableModel;

/**
 *
 * @author axel
 */
public interface Sheet extends Iterable<Row> {

	/**
	 * Get table model suitable for displaying sheet data in a JTable.
	 * @return table model
	 */
    TableModel getTableModel();

    /**
     * Get name of sheet.
     * @return name of sheet
     */
    String getSheetName();

    int getFirstColNum();

    int getFirstRowNum();

    int getLastColNum();

    int getLastRowNum();

    int getNumberOfColumns();

    int getNumberOfRows();

    double getColumnWidth(int colNum);

    double getRowHeight(int rowNum);

    CellStyle getDefaultCellStyle();

    Row getRow(int row);

    Workbook getWorkbook();

    Cell getCell(int i, int j);
    
    /**
     * Freeze view.
     * 
     * Freezes the sheet so, that rows <em>above</em> i and columns <em>to the left<em> of j remain in view when scrolling.
     *  
     * @param i row number
     * @param j column number
     */
	void freeze(int i, int j);
}
