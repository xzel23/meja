package com.dua3.meja.model;

import com.dua3.meja.model.Cell.CellException;
import com.dua3.utility.text.RichText;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CellExceptionTest {

    @Test
    void testCellException() {
        Cell dummyCell = new DummyCell();
        CellException ex1 = new CellException(dummyCell, "Something went wrong");
        assertTrue(ex1.getMessage().contains("Something went wrong"));

        Throwable cause = new RuntimeException("cause");
        CellException ex2 = new CellException(dummyCell, "Another error", cause);
        assertSame(cause, ex2.getCause());
        assertTrue(ex2.getMessage().contains("Another error"));
    }

    private static class DummyCell implements Cell {
        @Override
        public String getCellRef(RefOption... options) {
            return "'TestSheet'!A1";
        }

        @Override
        public Workbook getWorkbook() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Sheet getSheet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Row getRow() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getRowNumber() {
            return 0;
        }

        @Override
        public int getColumnNumber() {
            return 0;
        }

        @Override
        public CellType getCellType() {
            return CellType.BLANK;
        }

        @Override
        public CellType getResultType() {
            return CellType.BLANK;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void clear() {
            // do nothing
        }

        @Override
        public Cell set(@Nullable Boolean arg) {
            return this;
        }

        @Override
        public Cell set(@Nullable Number arg) {
            return this;
        }

        @Override
        public Cell set(@Nullable LocalDate arg) {
            return this;
        }

        @Override
        public Cell set(@Nullable LocalDateTime arg) {
            return this;
        }

        @Override
        public Cell set(@Nullable RichText arg) {
            return this;
        }

        @Override
        public Cell set(@Nullable String arg) {
            return this;
        }

        @Override
        public Cell setFormula(String formula) {
            return this;
        }

        @Override
        public Cell setError() {
            return this;
        }

        @Override
        public boolean getBoolean() {
            return false;
        }

        @Override
        public Number getNumber() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LocalDate getDate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LocalDateTime getDateTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public RichText getText() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getFormula() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CellStyle getCellStyle() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Cell setCellStyle(CellStyle style) {
            return this;
        }

        @Override
        public Optional<URI> getHyperlink() {
            return Optional.empty();
        }

        @Override
        public Cell setHyperlink(URI uri) {
            return this;
        }

        @Override
        public Cell clearHyperlink() {
            return this;
        }

        @Override
        public int getHorizontalSpan() {
            return 1;
        }

        @Override
        public int getVerticalSpan() {
            return 1;
        }

        @Override
        public Cell getLogicalCell() {
            return this;
        }

        @Override
        public void unMerge() {
            // do nothing
        }

        @Override
        public String toString(Locale locale) {
            return "";
        }

        @Override
        public RichText getAsText(Locale locale) {
            return RichText.emptyText();
        }

        @Override
        public @Nullable Object getOrDefault(@Nullable Object defaultValue) {
            return defaultValue;
        }
    }
}
