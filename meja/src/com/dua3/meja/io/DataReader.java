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
package com.dua3.meja.io;

import java.io.IOException;

/**
 * @author axel
 *
 */
public interface DataReader {

    /**
     * Read the given number of rows from input. Reading stops when rowsToRead
     * rows are read, EOF is reached, or an exception occurs.
     *
     * @param rowsToRead	maximum number of Rows to be read
     * @throws IOException
     * @return the number of rows read.
     * @throws DataException
     */
    public int readSome(int rowsToRead) throws IOException, DataException;

    /**
     * Read rows till end of input is reached.
     *
     * @throws IOException
     * @return the number of rows read.
     */
    public int readAll() throws IOException, DataException;

    /**
     * Ignore the given number of rows.
     *
     * @param rowsToIgnore	maximum number of Rows to be read.
     * @throws IOException
     * @return the number of rows ignored.
     */
    public int ignoreRows(int rowsToIgnore) throws IOException;

    /**
     * @return the number of the current row, zero based
     */
    public int getRowNumber();

    /**
     * @return the number of rows read. Unless ignore was called, this will be
     * equal to the value returned by getRowNumber().
     */
    public int getRowsRead();
}
