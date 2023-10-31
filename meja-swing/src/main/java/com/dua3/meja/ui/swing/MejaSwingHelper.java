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
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.util.TableOptions;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import com.dua3.utility.swing.SwingFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/**
 * Helper class.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public final class MejaSwingHelper {

    private static final Logger LOG = LogManager.getLogger(MejaSwingHelper.class);

    /**
     * Create a TableModel to be used with JTable.
     *
     * @param sheet the sheet to create a model for
     * @param options the options to use
     * @return table model instance of {@code JTableModel} for the sheet
     */
    public static TableModel getTableModel(final Sheet sheet, TableOptions... options) {
        return new SheetTableModel(sheet, options);
    }


    /**
     * Show a file open dialog and load the selected workbook.
     *
     * @param parent the parent component to use for the dialog
     * @param uri    the directory to set in the open dialog or the default path
     * @return the workbook the user chose or null if dialog was canceled
     * @throws IOException if a workbook was selected but could not be loaded
     */
    public static Optional<Workbook> showDialogAndOpenWorkbook(Component parent, URI uri) throws IOException {
        Path path = IoUtil.toPath(uri);
        boolean defaultFS = path.getFileSystem().equals(FileSystems.getDefault());
        File file = defaultFS ? path.toFile() : new File(".");

        JFileChooser jfc = new JFileChooser(file.isDirectory() ? file : file.getParentFile());

        for (FileFilter filter : SwingFileFilter.getFilters(OpenMode.READ, Workbook.class)) {
            jfc.addChoosableFileFilter(filter);
        }

        int rc = jfc.showOpenDialog(parent);

        if (rc != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        return openWorkbook(parent, jfc.getSelectedFile().toURI());
    }

    /**
     * Open a workbook from a given URI. A dialog to sepcify options is shown if required.
     *
     * @param parent the parent component to use
     * @param uri the URI of the workbook to open
     * @return an Optional containing the opened workbook, or an empty Optional if the dialog was canceled
     * @throws IOException if the selected workbook could not be loaded
     */
    public static Optional<Workbook> openWorkbook(Component parent, URI uri) throws IOException {
        FileType<Workbook> fileType = FileType.forUri(uri, Workbook.class)
                .orElseThrow(() -> new IllegalArgumentException("unknown filetype: " + uri));

        // load
        return Optional.of(fileType.read(uri, t -> showOptionsDialog(parent, t)));
    }

    private static Arguments showOptionsDialog(Component parent, FileType<?> fileType) {
        Collection<Option<?>> settings = fileType.getSettings();
        Arguments importSettings = Arguments.empty(); // default is empty
        if (!settings.isEmpty()) {
            SettingsDialog dialog = new SettingsDialog(parent, fileType.getName() + " - Settings",
                    "Please verify the import settings:", settings);
            dialog.setVisible(true);
            importSettings = dialog.getResult();
        }
        return importSettings;
    }

    /**
     * Show file selection dialog and save workbook.
     * <p>
     * A file selection dialog is shown and the workbook is saved to the selected
     * file. If the file already exists, a confirmation dialog is shown, asking the
     * user whether to overwrite the file.
     * </p>
     *
     * @param parent   the parent component for the dialog
     * @param workbook the workbook to save
     * @param uri      the URI to set the default path in the dialog
     * @return the URI the file was saved to or {@code null} if the user canceled
     * the dialog
     * @throws IOException if an exception occurs while saving
     */
    public static Optional<URI> showDialogAndSaveWorkbook(Component parent, Workbook workbook, URI uri)
            throws IOException {
        Path path = IoUtil.toPath(uri);
        boolean defaultFS = path.getFileSystem().equals(FileSystems.getDefault());
        File file = defaultFS ? path.toFile() : new File(".");

        JFileChooser jfc = new JFileChooser(file.isDirectory() ? file : file.getParentFile());

        int rc = jfc.showSaveDialog(parent);

        if (rc != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        file = jfc.getSelectedFile();

        if (file.exists()) {
            rc = JOptionPane.showConfirmDialog(parent,
                    "File '" + file.getAbsolutePath() + "' already exists. Overwrite?", "File exists",
                    JOptionPane.YES_NO_OPTION);
            if (rc != JOptionPane.YES_OPTION) {
                LOG.debug("user chose not to overwrite file");
                return Optional.empty();
            }
        }

        URI targetUri = file.toURI();
        workbook.write(targetUri);
        return Optional.of(targetUri);
    }

    private MejaSwingHelper() {
    }
}
