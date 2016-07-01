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

import com.dua3.meja.model.Color;

public interface GraphicsContext {

    void setColor(Color color);

    void drawLine(double x, double y, double i, double y0);

    void drawRect(double x, double y, double width, double height);

    void fillRect(double x, double y, double width, double height);

    public void setStroke(Color color, double width);

    public Rectangle getClipBounds();

}
