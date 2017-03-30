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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.dua3.meja.io.FileType;

/**
 * Workbook class.
 * <p>
 * A workbook consists of different sheets which can be accessed by number
 * or name. Use one of the concrete implementations of {@link WorkbookFactory}
 * to read a workbook from disk.
 * </p>
 * @author axel
 */
public interface Workbook extends AutoCloseable, Iterable<Sheet> {

    public static final String PROPERTY_ACTIVE_SHEET = "active sheet";
    public static final String PROPERTY_SHEET_ADDED = "sheet added";
    public static final String PROPERTY_SHEET_REMOVED = "sheet removed";

    /**
     * Get the URI for this workbook.
     *
     * <p>
     * When a workbook is opened, the URI is set so that it can be used to later
     * save the file back to the same location.
     * </p>
     * @return the URI for this workbook
     */
    Optional<URI> getUri();

    /**
     * Set URI for this workbook.
     * See {@link #getUri}.
     * @param uri the URI to set.
     */
    void setUri(URI uri);

    /**
     * Returns number of sheets in this workbook.
     * @return sheet
     */
    int getSheetCount();

    /**
     * Get sheet by number.
     * @param sheetNr number of sheet
     * @return
     *   sheet
     * @throws IndexOutOfBoundsException
     *   if no sheet exists at the given index
     */
    Sheet getSheet(int sheetNr);

    /**
     * Get sheet by name.
     * @param sheetName name of sheet
     * @return sheet
     * @throws IllegalArgumentException
     *   if no sheet exists with the given name
     */
    Sheet getSheetByName(String sheetName);

    Sheet getCurrentSheet();

    int getCurrentSheetIndex();

    /**
     * Set the current sheet.
     * @param idx
     *   index of the sheet to make current
     * @throws IndexOutOfBoundsException
     *   if no sheet exists at the given index
     */
    void setCurrentSheet(int idx);

    /**
     * Remove sheet by number.
     * @param idx number of sheet
     * @throws IndexOutOfBoundsException
     *   if no sheet exists at the given index
     */
    void removeSheet(int idx);

    /**
     * Remove sheet by name.
     * @param sheetName name of sheet
     * @return true, if sheet with that name was found and removed
     * @throws IllegalArgumentException
     *   if no sheet exists with the given name
     */
    boolean removeSheetByName(String sheetName);

    /**
     * Writes the workbook to a stream.
     * @param fileType the file type to use
     * @param out output stream to write to
     * @throws java.io.IOException
     */
    void write(FileType fileType, OutputStream out) throws IOException;

    /**
     * Writes the workbook to a file.
     *
     * @param file the file to write to.
     * <p>
     * The file format to used is determined
     * by the extension of {@code file} which must be one of the extensions
     * defined in {@link FileType}.
     * </p>
     * @param overwriteIfExists set to true if an existing file should be overwritten
     * @return true if workbook was written to file, otherwise false
     * @throws java.io.IOException
     */
    boolean write(File file, boolean overwriteIfExists) throws IOException;

    /**
     * Add a new sheet as last sheet of this workbook.
     * @param sheetName
     * @return the new sheet
     */
    Sheet createSheet(String sheetName);

    /**
     * Get default cell style for this workbook.
     * @return the default cell style
     */
    CellStyle getDefaultCellStyle();

    /**
     * Get registered cell style.
     * If no style is registered under {@code name}, a new one is created.
     * @param name cell style name
     * @return the registered cell style for {@code name}
     */
    CellStyle getCellStyle(String name);

    /**
     * Register a copy of a cell style.
     * @param styleName name for this cell style.
     * @param style the style to copy
     * @return the new cell style
     */
    CellStyle copyCellStyle(String styleName, CellStyle style);

    /**
     * Close workbook.
     * @throws IOException
     */
    @Override
    void close() throws IOException;

    /**
     * Get names of all cell styles.
     * @return list of cell style names
     */
    List<String> getCellStyleNames();

    /**
     * Check if a style with this name is defined.
     * @return true, if style is present
     */
    boolean hasCellStyle(String name);

    /**
     * Get the locale for this workbook.
     * @return the workbook`s locale
     */
    Locale getLocale();

    /**
     * Copy workbook data from another workbook.
     * @param other workbook to copy
     */
    void copy(Workbook other);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

}
