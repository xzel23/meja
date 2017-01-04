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

import java.io.File;
import java.io.IOException;
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
     * <p>
     * The file type is determined automatically based on the extension so
     * that it is possible to open a CSV file as Excel workbook.
     * </p>
     * @param file the workbook file
     * @return workbook
     * @throws IOException
     */
    public abstract Workbook open(File file) throws IOException;

}
