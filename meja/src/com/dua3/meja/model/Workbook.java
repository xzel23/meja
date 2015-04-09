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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Workbook class.
 *
 * A workbook consists of different sheets which can be accessed by number
 * or name.
 *
 * @author axel
 */
public interface Workbook extends AutoCloseable {

    /**
     * Returns number of sheets in this workbook.
     * @return sheet
     */
    int getNumberOfSheets();

    /**
     * Get sheet by number,
     * @param sheetNr number of sheet
     * @return sheet or {@code null}
     */
    Sheet getSheetByNr(int sheetNr);

    /**
     * Get sheet by name.
     * @param sheetName name of sheet
     * @return sheet or {@code null}
     */
    Sheet getSheetByName(String sheetName);

    /**
     * Writes the workbook to a stream.
     * @param out output stream to write to
     * @throws java.io.IOException 
     */
    void write(OutputStream out) throws IOException;
    
    /**
     * Writes the workbook to a file.
     * @param file file to write to
     * @param overwriteIfExists set to true if an existing file should be overwritten
     * @throws java.io.IOException 
     */
    void write(File file, boolean overwriteIfExists) throws IOException;
    
    /**
     * Add a new sheet as last sheet of this workbook.
     * @param sheetName
     * @return the new sheet
     */
    Sheet createSheet(String sheetName);
    
    /**
     * Get registered cell style.
     * @param name cell style name
     * @return the registered cell style for {@code name} or null if not found
     */
    CellStyle getCellStyle(String name);
    
    /**
     * Create and register a new cell style.
     * @param styleName name for this cell style.
     * @return the new cell style
     */
    CellStyle createCellStyle(String styleName);
    
    /**
     * Register a copy of a cell style.
     * @param styleName name for this cell style.
     * @param style the style to copy
     * @return the new cell style
     */
    CellStyle copyCellStyle(String styleName, CellStyle style);
        
}
