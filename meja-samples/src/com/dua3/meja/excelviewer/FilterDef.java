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
package com.dua3.meja.excelviewer;

import com.dua3.meja.model.WorkbookFactory;
import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author axel
 */
public class FilterDef extends javax.swing.filechooser.FileFilter implements FileFilter {

    public final String description;
    public final String[] extensions;
    public final OpenMode mode;
    public final Object factory;

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

    public WorkbookFactory getFactory() {
        return (WorkbookFactory) factory;
    }

}
