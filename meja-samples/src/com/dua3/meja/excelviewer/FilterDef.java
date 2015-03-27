/*
 *
 */
package com.dua3.meja.excelviewer;

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

    public Object getFactory() {
        return factory;
    }

}
