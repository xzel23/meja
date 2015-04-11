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

import java.util.Iterator;
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

    public static String getColumnName(int j) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append((char) ('A' + j % 26));
            j /= 26;
        } while (j > 0);
        return sb.toString();
    }

    /**
     * Create a TableModel to be used with JTable.
     *
     * @param sheet
     * @return table model
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
}
