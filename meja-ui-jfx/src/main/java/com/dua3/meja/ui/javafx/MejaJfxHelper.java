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

package com.dua3.meja.ui.javafx;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Color;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.util.MejaHelper;

import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class MejaJfxHelper {
    private static final Logger LOGGER =LoggerFactory.getLogger(MejaJfxHelper.class);
    
    public static FileChooser.ExtensionFilter[] getExtensionFilters(OpenMode mode) {
        return Arrays.stream(FileType.values())
                .filter(ft -> ft.isSupported(mode))
                .map(ft -> new FileChooser.ExtensionFilter(ft.getDescription(), ft.getExtensions()))
                .toArray(size -> new FileChooser.ExtensionFilter[size]);
    }

    /**
     * Show a file open dialog and load the selected workbook.
     *
     * @param parent
     *            the parent component to use for the dialog
     * @param path
     *            the directory to set in the open dialog or the default file
     * @return the workbook the user chose or null if dialog was canceled
     * @throws IOException
     *             if a workbook was selected but could not be loaded
     */
    public static Optional<Workbook> showDialogAndOpenWorkbook(Window parent, Path path) throws IOException {
        boolean defaultFS = path.getFileSystem().equals(FileSystems.getDefault());
        File file = defaultFS ? path.toFile() : new File(".");

        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(file == null || file.isDirectory() ? file : file.getParentFile());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
        fc.getExtensionFilters().addAll(getExtensionFilters(OpenMode.READ));

        file = fc.showOpenDialog(parent);

        if (file == null) {
            return Optional.empty();
        }

        path = file.toPath();

        FileChooser.ExtensionFilter ef = fc.getSelectedExtensionFilter();
        Optional<FileType> type = Arrays.stream(FileType.values())
                .filter(ft -> ft.getDescription().equals(ef.getDescription())
                        && Arrays.asList(ft.getExtensions()).equals(ef.getExtensions()))
                .findFirst();

        if (type.isPresent()) {
            // load workbook using the factory from the used filter definition
            final WorkbookFactory<?> factory = type.get().getFactory();
            return Optional.of(factory.open(path));
        } else {
            // another filter was used (ie. "all files")
            return Optional.of(MejaHelper.openWorkbook(path));
        }
    }

    /**
     * Show file selection dialog and save workbook.
     * <p>
     * A file selection dialog is shown and the workbook is saved to the
     * selected file. If the file already exists, a confirmation dialog is
     * shown, asking the user whether to overwrite the file.
     * </p>
     *
     * @param parent
     *            the parent component for the dialog
     * @param workbook
     *            the workbook to save
     * @param path
     *            the file to set the default path in the dialog
     * @return the URI the file was saved to or {@code null} if the user
     *         canceled the dialog
     * @throws IOException
     *             if an exception occurs while saving
     */
    public static Optional<Path> showDialogAndSaveWorkbook(Component parent, Workbook workbook, Path path)
            throws IOException {
        boolean defaultFS = path.getFileSystem().equals(FileSystems.getDefault());
        File file = defaultFS ? path.toFile() : new File(".");

        JFileChooser jfc = new JFileChooser(file == null || file.isDirectory() ? file : file.getParentFile());

        int rc = jfc.showSaveDialog(parent);

        if (rc != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        file = jfc.getSelectedFile();

        if (file.exists()) {
            rc = JOptionPane.showConfirmDialog(
                    parent,
                    "File '" + file.getAbsolutePath() + "' already exists. Overwrite?",
                    "File exists",
                    JOptionPane.YES_NO_OPTION);
            if (rc != JOptionPane.YES_OPTION) {
                LOGGER.info("User chose not to overwrite file.");
                return Optional.empty();
            }
        }

        Optional<FileType> type = FileType.forFile(file);
        if (type.isPresent()) {
            type.get().getWriter().write(workbook, file.toPath());
        } else {
            workbook.write(file.toPath());
        }

        return Optional.of(file.toPath());
    }

    static Paint toJfxColor(Color c) {
        return javafx.scene.paint.Color.rgb(c.r(), c.g(), c.b(), c.af());
    }

    private MejaJfxHelper() {
    }

}
