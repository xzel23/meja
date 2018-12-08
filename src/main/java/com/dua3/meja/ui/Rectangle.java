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

        LangUtil.check(w >= 0, "negative with: ", w);
        LangUtil.check(h >= 0, "negative height: ", h);
    }

    public Rectangle(Rectangle r) {
        this(r.x, r.y, r.w, r.h);
    }

    public double getBottom() {
        return y + h;
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

    public double getW() {
        return w;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setH(double h) {
        this.h = h;
    }

    public void setW(double w) {
        this.w = w;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + w + ", " + h + "]";
    }

    public void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }

}
