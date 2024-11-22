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
package com.dua3.meja.model;

import com.dua3.utility.data.Color;

/**
 * Definition of a cell border.
 *
 * @param width the border width
 * @param color the border color
 */
public record BorderStyle(float width, Color color) {
    /**
     * A BorderStyle instance representing an empty border,
     * defined with a width of 0.0f and color of black.
     */
    public static final BorderStyle EMPTY_BORDER = new BorderStyle(0.0f, Color.BLACK);

    /**
     * Checks if the border style has a width of 0.
     * This method is used to determine if the border should be considered as none.
     *
     * @return true if the border style has a width of 0, false otherwise
     */
    public boolean isNone() {
        return width == 0.0f;
    }
}

