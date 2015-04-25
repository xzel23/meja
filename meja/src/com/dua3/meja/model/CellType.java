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
package com.dua3.meja.model;

/**
 * Definition of cell types.
 * 
 * @author axel
 */
public enum CellType {
    /**
     * Type for blank (empty) cells.
     */
    BLANK,
    /**
     * Type for boolean ({@code true}|{@code false}) cells.
     */
    BOOLEAN,
    /**
     * Type for error cells.
     */
    ERROR,
    /**
     * Type for formula cells.
     */
    FORMULA,
    /**
     * Type for numeric cells.
     */
    NUMERIC,
    /**
     * Type for text cells.
     */
    TEXT;
}
