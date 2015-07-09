/*
 * Copyright 2015 Axel Howind.
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

/**
 * Search options.
 */
public enum SearchOptions {
    /**
     * Ignore text case when searching,
     *//**
     * Ignore text case when searching,
     */
    IGNORE_CASE, 
    /**
     * Compare complete cell content when searching.
     */
    MATCH_COMPLETE_TEXT,
    /**
     * Start searching from current cell, not first cell.
     */
    SEARCH_FROM_CURRENT,
    /**
     * Update the current cell if match is found.
     */
    UPDATE_CURRENT_CELL_WHEN_FOUND,
    /**
     * Search in  formula text instead of result.
     */
    SEARCH_FORMLUA_TEXT;
}
