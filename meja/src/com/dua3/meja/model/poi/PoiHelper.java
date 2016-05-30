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
package com.dua3.meja.model.poi;

import com.dua3.meja.model.HAlign;
import com.dua3.meja.model.VAlign;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class PoiHelper {
    private static final Logger LOGGER = Logger.getLogger(PoiHelper.class.getName());

    public static HAlign poiToHAlign(final short alignment) {
        switch (alignment) {
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_LEFT:
                return HAlign.ALIGN_LEFT;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER:
                return HAlign.ALIGN_CENTER;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_RIGHT:
                return HAlign.ALIGN_RIGHT;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER_SELECTION:
                return HAlign.ALIGN_CENTER;
            case org.apache.poi.ss.usermodel.CellStyle.ALIGN_GENERAL:
                return HAlign.ALIGN_AUTOMATIC;
            default:
                return HAlign.ALIGN_JUSTIFY;
        }
    }

    public static short hAlignToPoi(HAlign hAlign) {
        switch (hAlign) {
            case ALIGN_LEFT:
                return org.apache.poi.ss.usermodel.CellStyle.ALIGN_LEFT;
            case ALIGN_RIGHT:
                return org.apache.poi.ss.usermodel.CellStyle.ALIGN_RIGHT;
            case ALIGN_CENTER:
                return org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER;
            case ALIGN_JUSTIFY:
                return org.apache.poi.ss.usermodel.CellStyle.ALIGN_JUSTIFY;
            case ALIGN_AUTOMATIC:
                return org.apache.poi.ss.usermodel.CellStyle.ALIGN_GENERAL;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static VAlign poiToVAlign(final short alignment) {
        switch (alignment) {
            case org.apache.poi.ss.usermodel.CellStyle.VERTICAL_TOP:
                return VAlign.ALIGN_TOP;
            case org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER:
                return VAlign.ALIGN_MIDDLE;
            case org.apache.poi.ss.usermodel.CellStyle.VERTICAL_BOTTOM:
                return VAlign.ALIGN_BOTTOM;
            case org.apache.poi.ss.usermodel.CellStyle.VERTICAL_JUSTIFY:
                return VAlign.ALIGN_JUSTIFY;
            default:
                LOGGER.log(Level.WARNING,
                        "Unknown value for vertical algnment: {0}", alignment);
                return VAlign.ALIGN_MIDDLE;
        }
    }

    public static short vAlignToPoi(VAlign vAlign) {
        switch (vAlign) {
            case ALIGN_TOP:
                return org.apache.poi.ss.usermodel.CellStyle.VERTICAL_TOP;
            case ALIGN_MIDDLE:
                return org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER;
            case ALIGN_BOTTOM:
                return org.apache.poi.ss.usermodel.CellStyle.VERTICAL_BOTTOM;
            case ALIGN_JUSTIFY:
                return org.apache.poi.ss.usermodel.CellStyle.VERTICAL_JUSTIFY;
            default:
                throw new IllegalArgumentException();
        }
    }

    private PoiHelper() {
        // no instantiation
    }
}
