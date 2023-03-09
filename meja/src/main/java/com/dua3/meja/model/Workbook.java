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

import com.dua3.utility.io.FileType;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.options.Arguments;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoubleConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Workbook class.
 * <p>
 * A workbook consists of different sheets which can be accessed by number or
 * name. Use one of the concrete implementations of {@link WorkbookFactory} to
 * read a workbook from disk.
 * </p>
 *
 * @author axel
 */
public interface Workbook extends AutoCloseable, Iterable<Sheet> {

    String PROPERTY_ACTIVE_SHEET = "ACTIVE_SHEET";
    String PROPERTY_SHEET_ADDED = "SHEET_ADDED";
    String PROPERTY_SHEET_REMOVED = "SHEET_REMOVED";

    /**
     * Add property change listener.
     *
     * @param listener the listener
     * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Add property change listener.
     *
     * @param propertyName the property name
     * @param listener     the listener
     * @see PropertyChangeSupport#addPropertyChangeListener(String,
     * PropertyChangeListener)
     */
    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Close workbook.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    void close() throws IOException;

    /**
     * Copy workbook data from another workbook.
     *
     * @param other workbook to copy
     */
    default void copy(Workbook other) {
        // copy styles
        for (String styleName : other.getCellStyleNames()) {
            CellStyle cellStyle = other.getCellStyle(styleName);
            CellStyle newCellStyle = getCellStyle(styleName);
            newCellStyle.copyStyle(cellStyle);
        }

        // copy sheets
        for (int sheetNr = 0; sheetNr < other.getSheetCount(); sheetNr++) {
            Sheet sheet = other.getSheet(sheetNr);
            Sheet newSheet = createSheet(sheet.getSheetName());
            newSheet.copy(sheet);
        }
    }

    /**
     * Register a copy of a cell style.
     *
     * @param styleName name for this cell style.
     * @param style     the style to copy
     * @return the new cell style
     */
    CellStyle copyCellStyle(String styleName, CellStyle style);

    /**
     * Add a new sheet as last sheet of this workbook.
     *
     * @param sheetName the name of the sheet
     * @return the new sheet
     */
    Sheet createSheet(String sheetName);

    /**
     * Get registered cell style. If no style is registered under {@code name}, a
     * new one is created.
     *
     * @param name cell style name
     * @return the registered cell style for {@code name}
     */
    CellStyle getCellStyle(String name);

    /**
     * Get names of all cell styles.
     *
     * @return list of cell style names
     */
    List<String> getCellStyleNames();

    /**
     * Get stream of cell styles contained in workbook.
     *
     * @return stream of cell styles
     */
    Stream<? extends CellStyle> cellStyles();

    /**
     * Get the current sheet.
     *
     * @return the current sheet.
     * @throws IllegalArgumentException if the current sheet does not exist (i.e.
     *                                  the workbook does not contain any sheets)
     */
    Sheet getCurrentSheet();

    /**
     * Set the current sheet.
     *
     * @param idx index of the sheet to make current
     * @throws IndexOutOfBoundsException if no sheet exists at the given index
     */
    void setCurrentSheet(int idx);

    /**
     * Get index of current sheet.
     *
     * @return index of current sheet
     */
    int getCurrentSheetIndex();

    /**
     * Get default cell style for this workbook.
     *
     * @return the default cell style
     */
    CellStyle getDefaultCellStyle();

    /**
     * Get sheet by number.
     *
     * @param sheetNr number of sheet
     * @return sheet
     * @throws IndexOutOfBoundsException if no sheet exists at the given index
     */
    Sheet getSheet(int sheetNr);

    /**
     * Get sheet by name.
     *
     * @param sheetName name of sheet
     * @return sheet
     * @throws IllegalArgumentException if no sheet exists with the given name
     */
    default Sheet getSheetByName(String sheetName) {
        int idx = getSheetIndexByName(sheetName);

        if (idx < 0) {
            throw new IllegalArgumentException("No sheet with name '" + sheetName + "'.");
        }

        return getSheet(idx);
    }

    /**
     * Find sheet by name. This works like {@link #getSheetByName(String)} except that it returns an Optional and does
     * not throw an {@code IllegalArgumentException} when the sheet does not exist.
     *
     * @param sheetName name of sheet
     * @return an Optional holding the sheet or an empty Optional
     */
    default Optional<Sheet> findSheetByName(String sheetName) {
        int idx = getSheetIndexByName(sheetName);
        return idx >= 0 ? Optional.of(getSheet(idx)) : Optional.empty();
    }

    /**
     * Returns number of sheets in this workbook.
     *
     * @return sheet
     */
    int getSheetCount();

    /**
     * Get index of sheet by name.
     *
     * @param sheetName name of sheet
     * @return index of sheet or -1 if no sheet with this name exists
     */
    default int getSheetIndexByName(String sheetName) {
        int idx = 0;
        for (Sheet sheet : this) {
            if (sheet.getSheetName().equals(sheetName)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    /**
     * Get the sheet with the given name or create it if not yet contained in workbook.
     *
     * @param sheetName the name of the sheet
     * @return the sheet with the name passed as argument
     */
    default Sheet getOrCreateSheet(String sheetName) {
        return findSheetByName(sheetName).orElse(createSheet(sheetName));
    }

    /**
     * Get the URI for this workbook.
     *
     * <p>
     * When a workbook is opened, the URI is set so that it can be used to later
     * save the workbook back to the same location.
     * </p>
     *
     * @return the URI for this workbook
     */
    Optional<URI> getUri();

    /**
     * Set URI for this workbook. See {@link #getUri}.
     *
     * @param uri the URI to set.
     */
    void setUri(URI uri);

    /**
     * Check if a style with this name is defined.
     *
     * @param name the name of the cell style
     * @return true, if style is present
     */
    boolean hasCellStyle(String name);

    /**
     * Remove property change listener.
     *
     * @param listener the listener
     * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove property change listener.
     *
     * @param propertyName the name of the property
     * @param listener     the listener
     * @see PropertyChangeSupport#removePropertyChangeListener(String,
     * PropertyChangeListener)
     */
    void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Remove sheet by number.
     *
     * @param idx number of sheet
     * @throws IndexOutOfBoundsException if no sheet exists at the given index
     */
    void removeSheet(int idx);

    /**
     * Remove sheet by name.
     *
     * @param sheetName name of sheet
     * @throws IllegalArgumentException if no sheet exists with the given name
     */
    default void removeSheetByName(String sheetName) {
        int idx = getSheetIndexByName(sheetName);

        if (idx < 0) {
            throw new IllegalArgumentException("No sheet with name '" + sheetName + "'.");
        }

        removeSheet(idx);
    }

    /**
     * Test whether object caching is enabled.
     *
     * @return state of object caching
     */
    boolean isObjectCachingEnabled();

    /**
     * Set object caching.
     * <br>
     * When object caching is enabled, cell values will be added to a simple cache. This helps to reduce memory
     * consumption when adding many instances to a workbook that are technically equal. A good example is
     * adding calculated dates to a workbook with many cells containing instances representing the same values.
     * The effect on memory consumption may vary between different workbook implementations. When in doubt, measure
     * with realistic data for your use case.
     *
     * @param enable flag indicating whether to en- or disable object caching
     */
    void setObjectCaching(boolean enable);

    /**
     * Writes the workbook to a URI using standard options.
     *
     * @param uri the URI to write to.
     *            <p>
     *            The file format to used is determined by the extension of
     *            {@code uri} which must be one of the extensions defined in
     *            {@link FileType}.
     *            </p>
     * @throws IOException if an I/O error occurs
     */
    default void write(URI uri) throws IOException {
        write(uri, Arguments.empty());
    }

    /**
     * Writes the workbook to a file.
     *
     * @param uri     the URI to write to.
     *                <p>
     *                The file format to used is determined by the extension of
     *                {@code uri} which must be one of the extensions defined in
     *                {@link FileType}.
     *                </p>
     * @param options special options to use (supported options depend on the file
     *                type)
     * @throws IOException if an I/O error occurs
     */
    default void write(URI uri, Arguments options) throws IOException {
        write(uri, options, p -> {
        });
    }

    default void write(URI uri, Arguments options, DoubleConsumer updateProgress) throws IOException {
        FileType<?> type = FileType
                .forUri(uri)
                .orElseThrow(() -> new IllegalArgumentException("cannot determine file type for " + uri));
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(IoUtil.toPath(uri)))) {
            write(type, out, options, updateProgress);
        }
    }

    /**
     * Writes the workbook to a Path using standard options.
     *
     * @param path the path to write to.
     *             <p>
     *             The file format to used is determined by the extension of
     *             {@code path} which must be one of the extensions defined in
     *             {@link FileType}.
     *             </p>
     * @throws IOException if an I/O error occurs
     */
    default void write(Path path) throws IOException {
        write(path, Arguments.empty());
    }

    /**
     * Writes the workbook to a file.
     *
     * @param path    the path to write to.
     *                <p>
     *                The file format to used is determined by the extension of
     *                {@code uri} which must be one of the extensions defined in
     *                {@link FileType}.
     *                </p>
     * @param options special options to use (supported options depend on the file
     *                type)
     * @throws IOException if an I/O error occurs
     */
    default void write(Path path, Arguments options) throws IOException {
        write(path, options, p -> {
        });
    }


    default void write(Path path, Arguments options, DoubleConsumer updateProgress) throws IOException {
        FileType<?> type = FileType
                .forExtension(IoUtil.getExtension(path))
                .orElseThrow(() -> new IllegalArgumentException("cannot determine file type for " + path));
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
            write(type, out, options, updateProgress);
        }
    }

    /**
     * Writes the workbook to a stream using default options.
     *
     * @param fileType the file type to use
     * @param out      output stream to write to
     * @throws IOException if an I/O error occurs
     */
    default void write(FileType<?> fileType, OutputStream out) throws IOException {
        write(fileType, out, Arguments.empty());
    }

    /**
     * Writes the workbook to a stream.
     *
     * @param fileType the file type to use
     * @param out      output stream to write to
     * @param options  special options to use (supported options depend on the file
     *                 type)
     * @throws IOException if an I/O error occurs
     */
    default void write(FileType<?> fileType, OutputStream out, Arguments options) throws IOException {
        write(fileType, out, options, p -> {
        });
    }

    /**
     * Writes the workbook to a stream.
     *
     * @param fileType       the file type to use
     * @param out            output stream to write to
     * @param options        special options to use (supported options depend on the file
     *                       type)
     * @param updateProgress callback for progress updates; parameter is between 0.0 and 1.0 or Double.MAX_VALUE for indeterminate
     * @throws IOException if an I/O error occurs
     */
    void write(FileType<?> fileType, OutputStream out, Arguments options, DoubleConsumer updateProgress) throws IOException;

    /**
     * Get cached instance of object.
     *
     * @param <T> object type
     * @param obj the object to lookup
     * @return the cached instance, if an instance equal to {@code obj} is present
     * in the cache, {@code obj} otherwise
     */
    <T> T cache(T obj);

    /**
     * Create a stream of the rows in this sheet.
     *
     * @return stream of rows
     */
    default Stream<? extends Sheet> sheets() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

}
