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

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class PoiHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoiHelper.class);

    public static HorizontalAlignment hAlignToPoi(HAlign hAlign) {
        switch (hAlign) {
        case ALIGN_LEFT:
            return HorizontalAlignment.LEFT;
        case ALIGN_RIGHT:
            return HorizontalAlignment.RIGHT;
        case ALIGN_CENTER:
            return HorizontalAlignment.CENTER;
        case ALIGN_JUSTIFY:
            return HorizontalAlignment.JUSTIFY;
        case ALIGN_AUTOMATIC:
            return HorizontalAlignment.GENERAL;
        default:
            throw new IllegalArgumentException();
        }
    }

    public static HAlign poiToHAlign(HorizontalAlignment alignment) {
        switch (alignment) {
        case LEFT:
            return HAlign.ALIGN_LEFT;
        case CENTER:
            return HAlign.ALIGN_CENTER;
        case RIGHT:
            return HAlign.ALIGN_RIGHT;
        case CENTER_SELECTION:
            return HAlign.ALIGN_CENTER;
        case GENERAL:
            return HAlign.ALIGN_AUTOMATIC;
        default:
            return HAlign.ALIGN_JUSTIFY;
        }
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
            return VAlign.ALIGN_MIDDLE;
        default:
            LOGGER.warn("Unknown value for vertical algnment: {}", alignment);
            return VAlign.ALIGN_MIDDLE;
        }
    }

    public static VerticalAlignment vAlignToPoi(VAlign vAlign) {
        switch (vAlign) {
        case ALIGN_TOP:
            return VerticalAlignment.TOP;
        case ALIGN_MIDDLE:
            return VerticalAlignment.CENTER;
        case ALIGN_BOTTOM:
            return VerticalAlignment.BOTTOM;
        case ALIGN_JUSTIFY:
            return VerticalAlignment.JUSTIFY;
        default:
            throw new IllegalArgumentException();
        }
    }

    private PoiHelper() {
        // no instantiation
    }

}
