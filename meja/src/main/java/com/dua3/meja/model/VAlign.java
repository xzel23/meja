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
 * Vertical Alignment.
 *
 * @author axel
 */
public enum VAlign {
    /**
     * Align text to the top.
     */
    ALIGN_TOP(false, "vertical-align: top;"),
    /**
     * Align text vertically centered.
     */
    ALIGN_MIDDLE(false, "vertical-align: middle;"),
    /**
     * Align text to the bottom.
     */
    ALIGN_BOTTOM(false, "vertical-align: bottom;"),
    /**
     * Align text lines equally spaced.
     */
    ALIGN_JUSTIFY(false, "vertical-align: bottom;"),
    /**
     * Align text lines equally spaced.
     */
    ALIGN_DISTRIBUTED(false, "vertical-align: bottom; white-space: pre-wrap !important;");

    private final boolean wrap;

    private final String css;
    
    VAlign(boolean wrap, String css) {
        this.wrap = wrap;
        this.css = css;
    }

    /**
     * Get automatic wrapping for this alignment.
     * <p>
     * When laying out text justified horizontally, the text is automatically
     * wrapped regardless of the cell style setting.
     *
     * @return true if text should be wrapped regardless of cell style
     */
    public boolean isWrap() {
        return wrap;
    }

    /**
     * Get CSS style definition.
     * @return CSS style (i.e. "vertical-align: ...;")
     */
    public String getCssStyle() {
        return css;
    }
}
