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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public final class PoiHelper {

    private static final Logger LOGGER = Logger.getLogger(PoiHelper.class.getName());

    public static HorizontalAlignment hAlignToPoi(HAlign hAlign) {
        return switch (hAlign) {
            case ALIGN_LEFT -> HorizontalAlignment.LEFT;
            case ALIGN_RIGHT -> HorizontalAlignment.RIGHT;
            case ALIGN_CENTER -> HorizontalAlignment.CENTER;
            case ALIGN_JUSTIFY -> HorizontalAlignment.JUSTIFY;
            case ALIGN_AUTOMATIC -> HorizontalAlignment.GENERAL;
            default -> throw new IllegalArgumentException();
        };
    }

    public static HAlign poiToHAlign(HorizontalAlignment alignment) {
        return switch (alignment) {
            case LEFT -> HAlign.ALIGN_LEFT;
            case CENTER -> HAlign.ALIGN_CENTER;
            case RIGHT -> HAlign.ALIGN_RIGHT;
            case CENTER_SELECTION -> HAlign.ALIGN_CENTER;
            case GENERAL -> HAlign.ALIGN_AUTOMATIC;
            default -> HAlign.ALIGN_JUSTIFY;
        };
    }

    public static VAlign poiToVAlign(VerticalAlignment alignment) {
        switch (alignment) {
        case TOP:
            return VAlign.ALIGN_TOP;
        case CENTER:
            return VAlign.ALIGN_MIDDLE;
        case BOTTOM:
            return VAlign.ALIGN_BOTTOM;
        case JUSTIFY:
            return VAlign.ALIGN_JUSTIFY;
        case DISTRIBUTED:
            return VAlign.ALIGN_DISTRIBUTED;
        default:
            LOGGER.log(Level.WARNING, "Unknown value for vertical alignment: {0}", alignment);
            return VAlign.ALIGN_MIDDLE;
        }
    }

    public static VerticalAlignment vAlignToPoi(VAlign vAlign) {
        return switch (vAlign) {
            case ALIGN_TOP -> VerticalAlignment.TOP;
            case ALIGN_MIDDLE -> VerticalAlignment.CENTER;
            case ALIGN_BOTTOM -> VerticalAlignment.BOTTOM;
            case ALIGN_JUSTIFY -> VerticalAlignment.JUSTIFY;
            case ALIGN_DISTRIBUTED -> VerticalAlignment.DISTRIBUTED;
            default -> throw new IllegalArgumentException();
        };
    }

    private PoiHelper() {
        // no instantiation
    }

}
