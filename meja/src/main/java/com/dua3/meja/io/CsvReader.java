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
package com.dua3.meja.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dua3.meja.util.Options;

/**
 * @author axel TODO: see below number of fields require fixed number of columns
 *         allow lesser fields than columns append columns as necessary
 *
 *         optional: trim field values
 */
public class CsvReader extends Csv
        implements DataReader, AutoCloseable {

    // the unicode codepoint for the UTF-8 BOM
    private static final int UTF8_BOM = 0xfeff;
    // the bytes sequence the UTF-8 BOM
    private static final byte[] UTF8_BOM_BYTES = { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };

    static CsvReader create(RowBuilder builder, BufferedReader reader, Options options) throws IOException {
        CsvReader csvReader = new CsvReader(builder, reader, "[stream]", options);
        return csvReader;
    }

    public static CsvReader create(RowBuilder builder, File file, Options options) throws IOException {
        Charset cs = getCharset(options);
        return create(builder, Files.newBufferedReader(file.toPath(), cs), options);
    }

    public static CsvReader create(RowBuilder builder, InputStream in, Options options) throws IOException {
        // auto-detect UTF-8 with BOM (BOM marker overrides the CharSet
        // selection in options)
        Charset charset = getCharset(options);
        if (in.markSupported()) {
            int bomLength = UTF8_BOM_BYTES.length;
            byte[] buffer = new byte[bomLength];
            in.mark(bomLength);
            if (in.read(buffer) != bomLength || !Arrays.equals(UTF8_BOM_BYTES, buffer)) {
                in.reset();
            } else {
                charset = StandardCharsets.UTF_8;
            }
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        return create(builder, reader, options);
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

    public CsvReader(RowBuilder rowBuilder, BufferedReader reader, String source, Options options) throws IOException {
        this.rowBuilder = rowBuilder;
        this.reader = reader;
        this.columnNames = null;
        this.ignoreExcessFields = false;
        this.ignoreMissingFields = false;
        this.source = source;

        // remove optional UTF-8 BOM from content
        // this should be ok independent of the actual encoding since the
        // unicode representing
        // the UTF-8 BOM marker should only occur at the beginning of UTF-8
        // texts, and the old
        // (now obsolete) meaning as "ZERO WIDTH NON-BREAKING SPACE (ZWNBSP)"
        // does not make sense
        // at the beginning of a text.
        //
        // http://www.unicode.org/faq/utf_bom.html:
        // > In the absence of a protocol supporting its use as a BOM and when
        // not at the beginning
        // > of a text stream, U+FEFF should normally not occur.
        reader.mark(1);
        if (reader.read() != UTF8_BOM) {
            reader.reset();
        }

        String sep = Pattern.quote(String.valueOf(getSeparator(options)));
        String del = Pattern.quote(String.valueOf(getDelimiter(options)));

        // create pattern for matching of csv fields
        String regexEnd = "(?:" + sep + "|$)";
        // pattern group1: unquoted field
        String regexUnquotedField = "(?:((?:[^" + del + sep + "][^" + sep + "]*)?)" + regexEnd + ")";
        // pattern group2: quoted field
        String regexQuotedField = "(?:" + del + "((?:[^" + del + "]|" + del + del + ")*)" + del + regexEnd + ")";
        // pattern group3: start of quoted field with embedded newline (group
        // must contain delimiter!)
        String regexStartQuotedFieldWithLineBreak = "(" + del + "(?:[^" + del + "]*(?:" + del + del + ")?)*$)";

        String regexField = "^(?:"
                + regexQuotedField
                + "|" + regexUnquotedField
                + "|" + regexStartQuotedFieldWithLineBreak
                + ")";
        patternField = Pattern.compile(regexField);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * @param columnNr
     *            the column number
     * @return name of column or columnNr as String if no name was set
     */
    public String getColumnName(int columnNr) {
        if (columnNames == null) {
            return Integer.toString(columnNr);
        } else if (columnNr < columnNames.size()) {
            return columnNames.get(columnNr);
        } else {
            return null;
        }
    }

    /**
     * @return the columnNames
     */
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    /**
     * @return the ignoreExcessFields
     */
    public boolean getIgnoreExcessFields() {
        return ignoreExcessFields;
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

    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    @Override
    public int getRowsRead() {
        return rowsRead;
    }

    private String getSource() {
        return source;
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

    /*
     * (non-Javadoc)
     *
     * @see com.dua3.data.DataReader#read()
     */
    @Override
    public int readAll() throws IOException {
        return readRows(0);
    }

    public void readColumnNames() throws IOException {
        RowBuilder.ListRowBuilder rb = new RowBuilder.ListRowBuilder();
        readRow(rb);
        columnNames = rb.getRow();
    }

    /**
     * read a single row of CSV data.
     *
     * @return number of fields in row or -1 when end of input is reached
     * @throws IOException
     */
    private int readRow(RowBuilder rb) throws IOException {
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
                    throw new CsvFormatException("Unexpected end of input while looking for matching delimiter.",
                            getSource(), currentLine);
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
     * read some rows with CSV data.
     *
     * @param maxRows
     *            maximum number of rows to be read or 0 to read till end of
     *            input
     * @return number of rows read
     * @throws IOException
     * @throws CsvFormatException
     */
    private int readRows(int maxRows) throws IOException {
        int read = 0;
        while (maxRows == 0 || read < maxRows) {
            if (readRow(rowBuilder) < 0) {
                break;
            }
            read++;
        }
        return read;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dua3.data.DataReader#read(int)
     */
    @Override
    public int readSome(int rowsToRead) throws IOException {
        if (rowsToRead > 0) {
            return readRows(rowsToRead);
        } else {
            return 0;
        }
    }

    /**
     * @param columnNames
     *            the columnNames to set
     */
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * @param ignoreExcessFields
     *            the ignoreExcessFields to set
     */
    public void setIgnoreExcessFields(boolean ignoreExcessFields) {
        this.ignoreExcessFields = ignoreExcessFields;
    }

    /**
     * @param ignoreMissingFields
     *            the ignoreMissingFields to set
     */
    public void setIgnoreMissingFields(boolean ignoreMissingFields) {
        this.ignoreMissingFields = ignoreMissingFields;
    }

}
