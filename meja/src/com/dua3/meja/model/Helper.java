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

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Helper class.
 *
 * @author Axel Howind <axel@dua3.com>
 */
public class Helper {

    private Helper() {
    }

    /**
     * Create a TableModel to be used with JTable.
     * @param sheet
     * @return table model
     */
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
                int col = getSheetCol(columnIndex);
                StringBuilder sb = new StringBuilder();
                do {
                    sb.append((char) ('A' + col % 26));
                    col /= 26;
                } while (col > 0);
                return sb.toString();
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
            public Object getValueAt(int rowIndex, int columnIndex) {
                int rowNum = getSheetRow(rowIndex);
                int colNum = getSheetCol(columnIndex);
                Row row = sheet.getRow(rowNum);
                Cell cell = row == null ? null : row.getCell(colNum);
                return cell;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            private int getSheetCol(int columnIndex) {
                return columnIndex + sheet.getFirstColNum();
            }

            private int getSheetRow(int rowIndex) {
                return rowIndex + sheet.getFirstRowNum();
            }
        };

    }

}
