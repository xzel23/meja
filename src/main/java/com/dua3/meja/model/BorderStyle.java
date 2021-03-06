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

import com.dua3.utility.Color;

/**
 * Definition of a cell border.
 */
public class BorderStyle {

    private final float width;
    private final Color color;

    /**
     * Construct a new {@code BorderStyle}.
     *
     * @param width the border width to use in points
     * @param color the border color to use
     */
    public BorderStyle(float width, Color color) {
        this.width = width;
        this.color = color;
    }

    /**
     * Get border color.
     *
     * @return the border color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get border width.
     *
     * @return the border width
     */
    public float getWidth() {
        return width;
    }

}
