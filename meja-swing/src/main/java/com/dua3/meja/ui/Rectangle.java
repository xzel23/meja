/*
 * Copyright 2016 Axel Howind.
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
package com.dua3.meja.ui;

import com.dua3.utility.lang.LangUtil;

/**
 * The Rectangle class represents a rectangle with a position and dimensions.
 * It provides methods to manipulate and retrieve information about the rectangle.
 */
public final class Rectangle {

    private double x;
    private double y;
    private double w;
    private double h;

    /**
     * Constructs a new Rectangle object with the given coordinates and dimensions.
     * <p>
     * The x-coordinate represents the horizontal position of the rectangle.
     * <p>
     * The y-coordinate represents the vertical position of the rectangle.
     * <p>
     * The width (w) and height (h) represent the dimensions of the rectangle.
     *
     * @param x the x-coordinate of the rectangle
     * @param y the y-coordinate of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     * @throws IllegalArgumentException if the width or height is negative
     */
    public Rectangle(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        LangUtil.check(w >= 0, "negative with: ", w);
        LangUtil.check(h >= 0, "negative height: ", h);
    }

    /**
     * Constructs a new Rectangle object as a copy of the given Rectangle object.
     *
     * @param r the Rectangle object to copy
     */
    public Rectangle(Rectangle r) {
        this(r.x, r.y, r.w, r.h);
    }

    /**
     * Returns the vertical coordinate of the bottom edge of this Rectangle.
     *
     * @return the vertical coordinate of the bottom edge
     */
    public double getBottom() {
        return y + h;
    }

    /**
     * Returns the height of this Rectangle.
     *
     * @return the height of this Rectangle
     */
    public double getHeight() {
        return h;
    }

    /**
     * Retrieves the left coordinate of the object.
     *
     * @return The left coordinate.
     */
    public double getLeft() {
        return x;
    }

    /**
     * Returns the right coordinate of the rectangle.
     *
     * @return the right coordinate of the rectangle
     */
    public double getRight() {
        return x + w;
    }

    /**
     * Returns the top coordinate of the rectangle.
     * The top coordinate is the y-coordinate of the upper-left corner of the rectangle.
     *
     * @return the top coordinate of the rectangle
     */
    public double getTop() {
        return y;
    }

    /**
     * Returns the width value of the given object.
     *
     * @return the width value of the object as a double
     */
    public double getWidth() {
        return w;
    }

    /**
     * Returns the x-coordinate of a point.
     *
     * @return the x-coordinate of the point
     */
    public double getX() {
        return x;
    }

    /**
     * Retrieves the value of y.
     *
     * @return The value of y as a double.
     */
    public double getY() {
        return y;
    }

    /**
     * Set the height of an object.
     *
     * @param h The height to be set.
     */
    public void setHeight(double h) {
        this.h = h;
    }

    /**
     * Sets the width of an object.
     *
     * @param w the width value to set
     */
    public void setWidth(double w) {
        this.w = w;
    }

    /**
     * Sets the horizontal coordinate of the top left corner of this Rectangle to the specified value.
     *
     * @param x the horizontal coordinate to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the vertical coordinate of the top left corner of this Rectangle to the specified value.
     *
     * @param y the vertical coordinate to set
     */
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + w + ", " + h + "]";
    }

    /**
     * Translates this Rectangle by specified delta values in the x and y coordinates.
     *
     * @param dx the delta value to be added to the x coordinate
     * @param dy the delta value to be added to the y coordinate
     */
    public void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }

}
