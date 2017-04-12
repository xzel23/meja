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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interface used in {@link WorkbookReader} implementations to create rows of
 * data.
 */
public interface RowBuilder {

    /**
     * Start a new row.
     */
    public void startRow();

    /**
     * Add a value.
     * @param value the value to add
     */
    public void add(String value);

    /**
     * End the current row.
     */
    public void endRow();

    /**
     * Create a row as {@code List<String>}.
     */
    public class ListRowBuilder implements RowBuilder {

        private List<String> row;

        @Override
        public void add(String value) {
            row.add(value);
        }

        @Override
        public void startRow() {
            row = new ArrayList<>();
        }

        @Override
        public void endRow() {
          // nop
        }

        /**
         * Return the constructed row as {@code List<String>}.
         * @return the row as {@code List<String>}
         */
        public List<String> getRow() {
            return Collections.unmodifiableList(row);
        }
    }
}
