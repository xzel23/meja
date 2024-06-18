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

/**
 * Direction.
 *
 * @author axel
 */
public enum Direction {
    /**
     * North (top) direction.
     */
    NORTH("top"),
    /**
     * East (right) direction.
     */
    EAST("right"),
    /**
     * South (bottom) direction.
     */
    SOUTH("bottom"),

    /**
     * West (left) direction.
     */
    WEST("left");

    private final String cssName;

    Direction(String cssName) {
        this.cssName = cssName;
    }

    /**
     * Retrieves the CSS name associated with an element.
     *
     * @return The CSS name of the element.
     */
    public String getCssName() {
        return cssName;
    }

    /**
     * Returns the inverse direction of the current direction.
     *
     * @return The inverse direction.
     */
    public Direction inverse() {
        return switch (this) {
            case NORTH -> SOUTH;
            case EAST -> WEST;
            case SOUTH -> NORTH;
            case WEST -> EAST;
        };
    }
}
