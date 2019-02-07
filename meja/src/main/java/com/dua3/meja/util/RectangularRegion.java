/*
 * Copyright 2015 a5xysq1.
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
package com.dua3.meja.util;

import com.dua3.utility.text.ToStringBuilder;

/**
 *
 * @author a5xysq1
 */
public class RectangularRegion {

    private final int rowMin;
    private final int rowMax;
    private final int colMin;
    private final int colMax;

    /**
     * Construct instance of {@code RectangularRegion}.
     *
     * @param rowMin first row
     * @param rowMax last row {inclusive}
     * @param colMin first column
     * @param colMax last column (inclusive)
     */
    public RectangularRegion(int rowMin, int rowMax, int colMin, int colMax) {
        this.rowMin = rowMin;
        this.rowMax = rowMax;
        this.colMin = colMin;
        this.colMax = colMax;
    }

    /**
     * Test if cell is contained in region.
     *
     * @param i the row number
     * @param j the column number
     * @return true if the cell at row {@code i} and column {@code j} is contained
     *         in this region
     */
    public boolean contains(int i, int j) {
        return rowMin <= i && i <= rowMax && colMin <= j && j <= colMax;
    }

    /**
     * Get first column number.
     *
     * @return number of first column
     */
    public int getFirstColumn() {
        return colMin;
    }

    /**
     * Get first row number.
     *
     * @return number of first row
     */
    public int getFirstRow() {
        return rowMin;
    }

    /**
     * Get last column number.
     *
     * @return number of last column (inclusive)
     */
    public int getLastColumn() {
        return colMax;
    }

    /**
     * Get last row number.
     *
     * @return number of last row (inclusive)
     */
    public int getLastRow() {
        return rowMax;
    }

    /**
     * Test for intersection of two regions.
     *
     * @param other the region to test against
     * @return true if both regions intersect
     */
    public boolean intersects(RectangularRegion other) {
        return Math.min(rowMax, other.rowMax) - Math.max(rowMin, other.rowMin) >= 0
                && Math.min(colMax, other.colMax) - Math.max(colMin, other.colMin) >= 0;
    }

    @Override
    public String toString() {
        return new ToStringBuilder().add("rowMin", rowMin).add("rowMax", rowMax).add("colMin", colMin)
                .add("colMax", colMax).toString();
    }
}
