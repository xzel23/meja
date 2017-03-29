/*
 *
 */
package com.dua3.meja.ui.swing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.WorkbookFactory;

/**
 * A FileFilter class to be used as a drop-in file filter for dialogs.
 *
 * This class can be used in all cases where one of the three standard Java
 * file filter implementations is required, namely:
 * <ul>
 * <li>{@link java.io.FileFilter java.io.FileFilter}</li>
 * <li>{@link java.io.FilenameFilter java.io.FilenameFilter}</li>
 * <li>{@link javax.swing.filechooser.FileFilter javax.swing.filechooser.FileFilter}</li>
 * </ul>
 */
public class SwingFileFilter extends javax.swing.filechooser.FileFilter
            implements java.io.FileFilter, java.io.FilenameFilter {
    public static List<SwingFileFilter> getFilters(OpenMode mode) {
        List<FileType> fileTypes = FileType.getFileTypes(mode);
        List<SwingFileFilter> filters = new ArrayList<>(fileTypes.size());
        for (FileType ft: fileTypes) {
            filters.add(new SwingFileFilter(ft));
        }
        return filters;
    }

        private final FileType fileType;

        private SwingFileFilter(FileType fileType) {
            this.fileType = fileType;
        }

        @Override
        public boolean accept(File pathname) {
            for (String ext : fileType.getExtensions()) {
                if (pathname.getName().toLowerCase().endsWith(ext.substring(1).toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean accept(File dir, String name) {
            name = name.toLowerCase();
            for (String ext : fileType.getExtensions()) {
                if (name.endsWith(ext.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return fileType.getDescription();
        }

        /**
         * Get factory to use with this filter.
         *
         * @return an instance of {@link WorkbookFactory} to use this filter
         * with
         */
        public WorkbookFactory getFactory() {
            return fileType.getFactory();
        }

        public FileType getFileType() {
          return fileType;
        }
}
