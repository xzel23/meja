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
 * Horizontal alignment.
 *
 * @author axel
 */
public enum HAlign {
    /**
     * Align text to the left.
     */
    ALIGN_LEFT(false),
    /**
     * Align text centered.
     */
    ALIGN_CENTER(false),
    /**
     * Align text to the right.
     */
    ALIGN_RIGHT(false),
    /**
     * Align text justified.
     */
    ALIGN_JUSTIFY(true),
    /**
     * Use automatic alignment depending on cell content.
     */
    ALIGN_AUTOMATIC(false);

    // whether text is wrapped regardless of text wrap setting in cell style
    private final boolean wrap;

    private HAlign(boolean wrap) {
        this.wrap = wrap;

    }

    /**
     * Get automatic wrapping for this alignment.
     *
     * When laying out text justified, the text is automatically wrapped
     * regardless of the cell style setting.
     *
     * @return true if text should be wrapped regardless of cell style
     */
    public boolean isWrap() {
        return wrap;
    }
}
