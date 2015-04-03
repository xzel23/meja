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
package com.dua3.meja.model.generic;

import com.dua3.meja.model.Workbook;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Axel Howind <axel@dua3.com>
 */
public abstract class GenericWorkbook implements Workbook {

    final List<GenericSheet> sheets = new ArrayList<>();

    public GenericWorkbook() {
    }

    @Override
    public int getNumberOfSheets() {
        return sheets.size();
    }

    @Override
    public GenericSheet getSheetByNr(int sheetNr) {
        return sheets.get(sheetNr);
    }

    @Override
    public GenericSheet getSheetByName(String sheetName) {
        for (GenericSheet sheet: sheets) {
            if (sheet.getSheetName().equals(sheetName)) {
                return sheet;
            }
        }
        throw new IllegalArgumentException("No sheet '" + sheetName + "'.");
    }

}
