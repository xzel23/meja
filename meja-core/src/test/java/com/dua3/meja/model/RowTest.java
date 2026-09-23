package com.dua3.meja.model;

import com.dua3.meja.model.generic.GenericRow;
import com.dua3.meja.model.generic.GenericSheet;
import com.dua3.meja.model.generic.GenericWorkbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RowTest {

    @Test
    void testDefaultRowMethodsAndIterator() {
        GenericWorkbook wb = GenericWorkbookFactory.instance().create();
        GenericSheet sheet = wb.createSheet("RowTest");
        sheet.getCell(0, 0).set("C0");
        sheet.getCell(0, 1).set("C1");
        sheet.getCell(0, 2).set("C2");

        TestRow row = new TestRow((GenericRow) sheet.getRow(0));

        // Test default iterator (Row$1)
        Iterator<Cell> it = row.iterator();
        assertTrue(it.hasNext());
        assertEquals(0, it.next().getColumnNumber());
        assertTrue(it.hasNext());
        assertEquals(1, it.next().getColumnNumber());
        assertTrue(it.hasNext());
        assertEquals(2, it.next().getColumnNumber());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
        assertThrows(UnsupportedOperationException.class, it::remove);

        // Test cells() stream
        assertEquals(3, row.cells().count());

        // Test default createCell overloads
        assertNotNull(row.createCell());
        assertNotNull(row.createCell("StringVal"));
        assertNotNull(row.createCell(123));

        // Test default getRowHeight
        assertTrue(row.getRowHeight() > 0);
    }

    private static class TestRow implements Row {
        private final GenericRow delegate;

        TestRow(GenericRow delegate) {
            this.delegate = delegate;
        }

        @Override
        public Workbook getWorkbook() {
            return delegate.getWorkbook();
        }

        @Override
        public Sheet getSheet() {
            return delegate.getSheet();
        }

        @Override
        public int getRowNumber() {
            return delegate.getRowNumber();
        }

        @Override
        public int getColumnCount() {
            return delegate.getColumnCount();
        }

        @Override
        public Cell getCell(int cellNum) {
            return delegate.getCell(cellNum);
        }

        @Override
        public Optional<Cell> getCellIfExists(int cellNum) {
            return delegate.getCellIfExists(cellNum);
        }

        @Override
        public void copy(Row other) {
            delegate.copy(other);
        }
    }
}
