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
package com.dua3.meja.model.poi;

import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Axel Howind (axel@dua3.com)
 */
public final class PoiHelper {

    private static final Logger LOGGER = LogManager.getLogger(PoiHelper.class);

    /**
     * Converts a custom horizontal alignment value to a corresponding Apache POI HorizontalAlignment value.
     *
     * @param hAlign the custom horizontal alignment value to be converted
     * @return the converted Apache POI HorizontalAlignment value
     */
    public static HorizontalAlignment hAlignToPoi(HAlign hAlign) {
        return switch (hAlign) {
            case ALIGN_LEFT -> HorizontalAlignment.LEFT;
            case ALIGN_RIGHT -> HorizontalAlignment.RIGHT;
            case ALIGN_CENTER -> HorizontalAlignment.CENTER;
            case ALIGN_JUSTIFY -> HorizontalAlignment.JUSTIFY;
            case ALIGN_AUTOMATIC -> HorizontalAlignment.GENERAL;
        };
    }

    /**
     * Converts an Apache POI HorizontalAlignment value to a corresponding custom horizontal alignment value.
     *
     * @param alignment the Apache POI HorizontalAlignment value to be converted
     * @return the converted custom horizontal alignment value
     */
    public static HAlign poiToHAlign(HorizontalAlignment alignment) {
        return switch (alignment) {
            case LEFT -> HAlign.ALIGN_LEFT;
            case CENTER -> HAlign.ALIGN_CENTER;
            case RIGHT -> HAlign.ALIGN_RIGHT;
            case CENTER_SELECTION -> HAlign.ALIGN_CENTER;
            case GENERAL -> HAlign.ALIGN_AUTOMATIC;
            case FILL -> HAlign.ALIGN_JUSTIFY;
            case JUSTIFY -> HAlign.ALIGN_JUSTIFY;
            case DISTRIBUTED -> HAlign.ALIGN_JUSTIFY;
        };
    }

    /**
     * Converts an Apache POI VerticalAlignment value to a corresponding custom vertical alignment value.
     *
     * @param alignment the Apache POI VerticalAlignment value to be converted
     * @return the converted custom vertical alignment value
     */
    public static VAlign poiToVAlign(VerticalAlignment alignment) {
        return switch (alignment) {
            case TOP -> VAlign.ALIGN_TOP;
            case CENTER -> VAlign.ALIGN_MIDDLE;
            case BOTTOM -> VAlign.ALIGN_BOTTOM;
            case JUSTIFY -> VAlign.ALIGN_JUSTIFY;
            case DISTRIBUTED -> VAlign.ALIGN_DISTRIBUTED;
        };
    }

    /**
     * Converts a custom vertical alignment value to a corresponding Apache POI VerticalAlignment value.
     *
     * @param vAlign the custom vertical alignment value to be converted
     * @return the converted Apache POI VerticalAlignment value
     */
    public static VerticalAlignment vAlignToPoi(VAlign vAlign) {
        return switch (vAlign) {
            case ALIGN_TOP -> VerticalAlignment.TOP;
            case ALIGN_MIDDLE -> VerticalAlignment.CENTER;
            case ALIGN_BOTTOM -> VerticalAlignment.BOTTOM;
            case ALIGN_JUSTIFY -> VerticalAlignment.JUSTIFY;
            case ALIGN_DISTRIBUTED -> VerticalAlignment.DISTRIBUTED;
        };
    }

    private PoiHelper() {
        // no instantiation
    }

}
