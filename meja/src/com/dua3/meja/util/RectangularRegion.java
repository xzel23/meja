/*
 * Copyright 2015 a5xysq1.
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
package com.dua3.meja.util;

/**
 *
 * @author a5xysq1
 */
public class RectangularRegion {

    private final int rowMin;
    private final int rowMax;
    private final int colMin;
    private final int colMax;

    public RectangularRegion(int rowMin, int rowMax, int colMin, int colMax) {
        this.rowMin = rowMin;
        this.rowMax = rowMax;
        this.colMin = colMin;
        this.colMax = colMax;
    }

    public boolean contains(int i, int j) {
        return rowMin <= i && i <= rowMax && colMin <= j && j <= colMax;
    }

    public int getFirstRow() {
        return rowMin;
    }

    public int getFirstColumn() {
        return colMin;
    }

    public int getLastColumn() {
        return colMax;
    }

    public int getLastRow() {
        return rowMax;
    }
}
