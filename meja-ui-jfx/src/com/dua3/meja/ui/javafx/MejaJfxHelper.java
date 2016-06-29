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

package com.dua3.meja.ui.javafx;

import com.dua3.meja.io.FileType;
import com.dua3.meja.io.OpenMode;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.util.MejaHelper;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class MejaJfxHelper {

    /**
     * Show a file open dialog and load the selected workbook.
     *
     * @param parent the parent component to use for the dialog
     * @param file the directory to set in the open dialog or the default file
     * @return the workbook the user chose or null if dialog was canceled
     * @throws IOException if a workbook was selected but could not be loaded
     */
    public static Workbook showDialogAndOpenWorkbook(Window parent, File file) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(file == null || file.isDirectory() ? file : file.getParentFile());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
        fc.getExtensionFilters().addAll(getExtensionFilters(OpenMode.READ));

        file = fc.showOpenDialog(parent);

        Workbook workbook = null;
        if (file != null) {
            FileChooser.ExtensionFilter ef = fc.getSelectedExtensionFilter();
            Optional<FileType> type = Arrays.stream(FileType.values())
                    .filter((ft)->ft.getDescription().equals(ef.getDescription()) && Arrays.asList(ft.getExtensions()).equals(ef.getExtensions()))
                    .findFirst();

            if (type.isPresent()) {
                // load workbook using the factory from the used filter definition
                final WorkbookFactory factory = type.get().getFactory();
                workbook = factory.open(file);
            } else {
                // another filter was used (ie. "all files")
                workbook = MejaHelper.openWorkbook(file);
            }
        }
        return workbook;
    }

    public static FileChooser.ExtensionFilter[] getExtensionFilters(OpenMode mode) {
        return Arrays.stream(FileType.values())
                .filter(ft -> ft.isSupported(mode))
                .map(ft -> new FileChooser.ExtensionFilter(ft.getDescription(), ft.getExtensions()))
                .toArray((size) -> new FileChooser.ExtensionFilter[size]);
    }

    /**
     * Show file selection dialog and save workbook.
     * <p>
     * A file selection dialog is shown and the workbook is saved to the
     * selected file. If the file already exists, a confirmation dialog is
     * shown, asking the user whether to overwrite the file.</p>
     *
     * @param parent the parent component for the dialog
     * @param workbook the workbook to save
     * @param file the file to set the default path in the dialog
     * @return the URI the file was saved to or {@code null} if the user
     * canceled the dialog
     * @throws IOException if an exception occurs while saving
     */
    public static URI showDialogAndSaveWorkbook(Component parent, Workbook workbook, File file) throws IOException {
        JFileChooser jfc = new JFileChooser(file == null || file.isDirectory() ? file : file.getParentFile());

        int rc = jfc.showSaveDialog(parent);

        URI uri = null;
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();

            if (file.exists()) {
                rc = JOptionPane.showConfirmDialog(
                        parent,
                        "File '" + file.getAbsolutePath() + "' already exists. Overwrite?",
                        "File exists",
                        JOptionPane.YES_NO_OPTION);
                if (rc != JOptionPane.YES_OPTION) {
                    Logger.getLogger(MejaHelper.class.getName()).log(Level.INFO, "User selected not to overwrite file.");
                    return null;
                }
            }

            FileType type = FileType.forFile(file);
            if (type != null) {
                type.getWriter().write(workbook, file);
            } else {
                workbook.write(file, true);
            }
            uri = file.toURI();

        }
        return uri;
    }

    private MejaJfxHelper() {
    }

}
