/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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

/**
 *
 * @author axel
 */
public enum VAlign {
    /**
     * Align text to the top.
     */
    ALIGN_TOP(false),
    /**
     * Align text vertically centered.
     */
    ALIGN_MIDDLE(false),
    /**
     * Align text to the bottom.
     */
    ALIGN_BOTTOM(false),
    /**
     * Align text lines equally spaced.
     */
    ALIGN_JUSTIFY(false);

    private final boolean wrap;

    private VAlign(boolean wrap) {
        this.wrap = wrap;

    }

    /**
     * Get automatic wrapping for this alignment.
     * 
     * When laying out text justified horizontally, the text is automatically
     * wrapped regardless of the cell style setting.
     * @return true if text should be wrapped regardless of cell style
     */
    public boolean isWrap() {
        return wrap;
    }
}
