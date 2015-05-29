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

import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Abstract base class for workbook factories.
 *
 * @author axel
 */
public abstract class WorkbookFactory {

    /**
     * Create a new Workbook instance with the given locale.
     *
     * @param locale the locale to use
     * @return workbook
     */
    public abstract Workbook create(Locale locale);

    /**
     * Create a new Workbook instance with the given locale, use streaming if available.
     *
     * @param locale the locale to use
     * @return workbook
     */
    public abstract Workbook createStreaming(Locale locale);
    
    /**
     * Create a new Workbook instance.
     *
     * @return workbook
     */
    public abstract Workbook create();

    /**
     * Create a new Workbook instance, use streaming if available.
     *
     * @return workbook
     */
    public abstract Workbook createStreaming();

    /**
     * Load workbook from file.
     *
     * @param file workbook file
     * @return workbook
     * @throws IOException
     */
    public abstract Workbook open(File file) throws IOException;

    /**
     * Mode for opening files.
     */
    public enum OpenMode {

        /**
         * Open file for reading.
         */
        READ,
        /**
         * Open file for writing.
         */
        WRITE,
        /**
         * Open file for reading and/or writing.
         */
        READ_AND_WRITE

    }

    /**
     * Definition of file filters.
     */
    public static class FilterDef extends javax.swing.filechooser.FileFilter implements FileFilter {

        public final String description;
        public final String[] extensions;
        public final OpenMode mode;
        public final Object factory;

        /**
         * Construct a new FilterDef instance.
         *
         * @param description the description to show in file dialogs
         * @param mode the {@link OpenMode} supported by this filter
         * @param factory the {@link WorkbookFactory} used by this filter
         * @param extensions the supported file extensions for this filter
         */
        FilterDef(String description, OpenMode mode, Object factory, String... extensions) {
            this.description = description;
            this.mode = mode;
            this.factory = factory;
            this.extensions = extensions;
        }

        @Override
        public boolean accept(File pathname) {
            for (String extension : extensions) {
                if (pathname.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check if the requested operation is supported ny this filter.
         *
         * @param modeRequested the {@link OpenMode}
         * @return true if operation is suppurted
         */
        boolean isAppplicable(OpenMode modeRequested) {
            switch (modeRequested) {
                case READ:
                    return mode == OpenMode.READ || mode == OpenMode.READ_AND_WRITE;
                case WRITE:
                    return mode == OpenMode.WRITE || mode == OpenMode.READ_AND_WRITE;
                default:
                    throw new IllegalArgumentException(String.valueOf(modeRequested));
            }
        }

        @Override
        public String getDescription() {
            return description;
        }

        /**
         * Get factory to use with this filter.
         * @return an instance of {@link WorkbookFactory} to use this filter with
         */
        public WorkbookFactory getFactory() {
            return (WorkbookFactory) factory;
        }

    }

    private static final FilterDef[] filters = {
        new FilterDef("Excel Files", OpenMode.READ_AND_WRITE, PoiWorkbookFactory.instance(), ".xls", ".xlsx", ".xlsm"),
        new FilterDef("CSV Files", OpenMode.READ_AND_WRITE, GenericWorkbookFactory.instance(), ".csv", ".txt")
    };

    /**
     * Return list of filters supporting the requested operation.
     * @param mode the {@link OpenMode}
     * @return list of filters that support {@code mode}
     */
    public static List<FilterDef> getFileFilters(OpenMode mode) {
        List<FilterDef> list = new ArrayList<>();
        for (final FilterDef filter : filters) {
            if (filter.isAppplicable(mode)) {
                list.add(filter);
            }
        }
        return list;
    }

}
