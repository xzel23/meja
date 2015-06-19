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
package com.dua3.meja.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author axel TODO: see below number of fields require fixed number of columns
 * allow lesser fields than columns append columns as necessary
 *
 * optional: trim field values
 */
public class CsvReader implements DataReader, AutoCloseable {

    public static CsvReader createReader(RowBuilder builder, File file) throws FileNotFoundException {
        return new CsvReader(Csv.DEFAULT_SEPARATOR, Csv.DEFAULT_DELIMITER, builder, new FileReader(file), file.getName());
    }

    public static CsvReader createReader(RowBuilder builder, InputStream in) throws IOException {
        try (Reader reader = new InputStreamReader(in)) {
            return new CsvReader(Csv.DEFAULT_SEPARATOR, Csv.DEFAULT_DELIMITER, builder, reader, "[stream]");
        }
    }

    private RowBuilder rowBuilder;
    private int rowNumber;
    private int rowsRead;
    private int lineNumber;
    private Pattern patternField;
    private BufferedReader reader;
    private List<String> columnNames;
    private boolean ignoreExcessFields;
    private boolean ignoreMissingFields;
    private final String source;

    public CsvReader(char separator, char delimiter, RowBuilder rowBuilder, Reader reader, String source) {
        this(separator, delimiter, rowBuilder, new BufferedReader(reader), source);
    }

    public CsvReader(char separator, char delimiter, RowBuilder rowBuilder, BufferedReader reader, String source) {
        this.rowBuilder = rowBuilder;
        this.reader = reader;
        this.columnNames = null;
        this.ignoreExcessFields = false;
        this.ignoreMissingFields = false;
        this.source = source;

        String sep = Pattern.quote(String.valueOf(separator));
        String del = Pattern.quote(String.valueOf(delimiter));

        // create pattern for matching of csv fields
        String regexEnd = "(?:" + sep + "|$)";
        // pattern group1: unquoted field
        String regexUnquotedField = "(?:((?:[^" + del + sep + "][^" + sep + "]*)?)" + regexEnd + ")";
        // pattern group2: quoted field
        String regexQuotedField = "(?:" + del + "((?:[^" + del + "]|" + del + del + ")*)" + del + regexEnd + ")";
        // pattern group3: start of quoted field with embedded newline (group must contain delimiter!)
        String regexStartQuotedFieldWithLineBreak = "(" + del + "(?:[^" + del + "]*(?:" + del + del + ")?)*$)";

        String regexField = "^(?:"
                + regexQuotedField
                + "|" + regexUnquotedField
                + "|" + regexStartQuotedFieldWithLineBreak
                + ")";
        patternField = Pattern.compile(regexField);
    }

    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    @Override
    public int getRowsRead() {
        return rowsRead;
    }

    @Override
    public int ignoreRows(int rowsToIgnore) throws IOException {
        int ignored = 0;
        while (ignored < rowsToIgnore) {
            reader.readLine();
            lineNumber++;
            ignored++;
        }
        return ignored;
    }

    /* (non-Javadoc)
     * @see com.dua3.data.DataReader#read()
     */
    @Override
    public int readAll() throws IOException, DataException {
        return readRows(0);
    }

    /* (non-Javadoc)
     * @see com.dua3.data.DataReader#read(int)
     */
    @Override
    public int readSome(int rowsToRead) throws IOException, DataException {
        if (rowsToRead > 0) {
            return readRows(rowsToRead);
        } else {
            return 0;
        }
    }

    /**
     * read some rows with CSV data.
     *
     * @param maxRows	maximum number of rows to be read or 0 to read till end of
     * input
     * @return number of rows read
     * @throws IOException
     * @throws CsvFormatException
     */
    private int readRows(int maxRows) throws IOException, DataException {
        int read = 0;
        while (maxRows == 0 || read < maxRows) {
            if (readRow(rowBuilder) < 0) {
                break;
            }
            read++;
        }
        return read;
    }

    /**
     * read a single row of CSV data.
     *
     * @return number of fields in row or -1 when end of input is reached
     * @throws IOException
     * @throws DataException
     */
    private int readRow(RowBuilder rb) throws IOException, DataException {
        String line = reader.readLine();

        if (line == null) {
            return -1;
        }

        lineNumber++;

        rb.startRow();
        int columnNr = 0;
        Matcher matcher = patternField.matcher(line);
        while (matcher.lookingAt()) {
            // group 1 refers to a quoted field, group 2 to an unquoted field
            // since we have a match, either group 1 or group 2 matches and
            // contains the field's value.
            String field = matcher.group(1);
            if (field != null) {
                field = field.replaceAll("\"\"", "\"");
            } else {
                field = matcher.group(2);
            }

            // check for linebreak in quoted field
            int currentLine = lineNumber;
            if (field == null && matcher.group(3) != null) {
                String nextLine = reader.readLine();
                if (nextLine == null) {
                    throw new CsvFormatException("Unexpected end of input while looking for matching delimiter.", getSource(), currentLine);
                }
                lineNumber++;
                line = matcher.group(3) + "\n" + nextLine;
                matcher = patternField.matcher(line);
                continue;
            }

            assert field != null;
            rb.add(field);
            columnNr++;

            // check for end of line
            if (matcher.hitEnd()) {
                break;
            }

            // move region behind end of last match
            matcher.region(matcher.end(), matcher.regionEnd());
        }

        // if unparsed input remains, the input line is not in csv-format
        if (!matcher.hitEnd()) {
            throw new CsvFormatException("invalid csv data.", getSource(), getLineNumber());
        }

        // check number of fields
        if (!ignoreMissingFields && columnNames != null && columnNr < columnNames.size()) {
            throw new CsvFormatException("not enough fields.", getSource(), getLineNumber());
        }

        rowNumber++;
        rowsRead++;
        rb.endRow();

        return columnNr;
    }

    /**
     * @param columnNr
     * @return name of column or columnNr as String if no name was set
     * @throws IOException
     */
    public String getColumnName(int columnNr) throws IOException {
        if (columnNames == null) {
            return Integer.toString(columnNr);
        } else if (columnNr < columnNames.size()) {
            return columnNames.get(columnNr);
        } else {
            return null;
        }
    }

    public void readColumnNames() throws IOException, DataException {
        RowBuilder.ListRowBuilder rb = new RowBuilder.ListRowBuilder();
        readRow(rb);
        columnNames = rb.getRow();
    }

    /**
     * @param columnNames the columnNames to set
     */
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * @return the columnNames
     */
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    /**
     * @param ignoreExcessFields the ignoreExcessFields to set
     */
    public void setIgnoreExcessFields(boolean ignoreExcessFields) {
        this.ignoreExcessFields = ignoreExcessFields;
    }

    /**
     * @return the ignoreExcessFields
     */
    public boolean getIgnoreExcessFields() {
        return ignoreExcessFields;
    }

    /**
     * @param ignoreMissingFields the ignoreMissingFields to set
     */
    public void setIgnoreMissingFields(boolean ignoreMissingFields) {
        this.ignoreMissingFields = ignoreMissingFields;
    }

    /**
     * @return the ignoreMissingFields
     */
    public boolean getIgnoreMissingFields() {
        return ignoreMissingFields;
    }

    /**
     * @return the lineNumber
     */
    public int getLineNumber() {
        return lineNumber;
    }

    private String getSource() {
        return source;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
