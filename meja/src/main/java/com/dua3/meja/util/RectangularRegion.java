/*
 * Copyright 2015 axel@dua3.com.
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

import com.dua3.utility.lang.LangUtil;

/**
 * A rectangular region.
 *
 * @param firstRow    first row
 * @param lastRow     last row {inclusive}
 * @param firstColumn first column
 * @param lastColumn  last column (inclusive)
 */
public record RectangularRegion(int firstRow, int lastRow, int firstColumn, int lastColumn) {

    /**
     * Creates a RectangularRegion object.
     *
     * @param firstRow     the index of the first row in the region
     * @param lastRow      the index of the last row in the region
     * @param firstColumn  the index of the first column in the region
     * @param lastColumn   the index of the last column in the region
     * @throws IllegalArgumentException if the indices are not in a valid range
     */
    public RectangularRegion {
        LangUtil.check(firstRow <= lastRow && firstColumn <= lastColumn);
    }

    /**
     * Test if cell is contained in region.
     *
     * @param i the row number
     * @param j the column number
     * @return true if the cell at row {@code i} and column {@code j} is contained
     * in this region
     */
    public boolean contains(int i, int j) {
        return firstRow <= i && i <= lastRow && firstColumn <= j && j <= lastColumn;
    }

    /**
     * Test for intersection of two regions.
     *
     * @param other the region to test against
     * @return true if both regions intersect
     */
    public boolean intersects(RectangularRegion other) {
        return Math.min(lastRow, other.lastRow) - Math.max(firstRow, other.firstRow) >= 0
                && Math.min(lastColumn, other.lastColumn) - Math.max(firstColumn, other.firstColumn) >= 0;
    }

}
