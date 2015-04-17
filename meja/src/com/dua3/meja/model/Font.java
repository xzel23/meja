/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
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
package com.dua3.meja.model;

import java.awt.Color;

/**
 *
 * @author axel
 */
public interface Font {

    /**
     * Get text color.
     * @return the text color.
     */
    Color getColor();
    
    /**
     * Get font size.
     * @return the font size in points.
     */
    float getSizeInPoints();
    
    /**
     * Get font family.
     * @return the font family as {@code String}.
     */
    String getFamily();
    
    /**
     * Get bold property.
     * @return true if font is bold.
     */
    boolean isBold();

    /**
     * Get italic property.
     * @return true if font is italic.
     */
    boolean isItalic();
    
    /**
     * Get underlined property.
     * @return true if font is underlined.
     */
    boolean isUnderlined();
    
    /**
     * Get strike-through property.
     * @return true if font is strike-through.
     */
    boolean isStrikeThrough();
}
