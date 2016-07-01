/*
 * Copyright 2016 Axel Howind <axel@dua3.com>.
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
package com.dua3.meja.ui;

public final class Rectangle {

    private double x;
    private double y;
    private double w;
    private double h;

    public Rectangle(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        if (w < 0 || h < 0) {
            throw new IllegalArgumentException("negative width or height");
        }
    }

    public Rectangle(Rectangle r) {
        this(r.x, r.y, r.w, r.h);
    }

    private static int round(double x) {
        return (int) Math.round(x);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getW() {
        return w;
    }

    public double getH() {
        return h;
    }

    public double getLeft() {
        return x;
    }

    public double getRight() {
        return x + w;
    }

    public double getTop() {
        return y;
    }

    public double getBottom() {
        return y + h;
    }

    public void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setW(double w) {
        this.w = w;
    }

    public void setH(double h) {
        this.h = h;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + w + ", " + h + "]";
    }

}
