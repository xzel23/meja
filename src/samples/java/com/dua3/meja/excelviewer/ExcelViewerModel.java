/*
 *
 */
package com.dua3.meja.excelviewer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;

/**
 *
 * @author axel
 */
public class ExcelViewerModel {

    interface ExcelViewer {
        public SheetView getCurrentView();

        public SheetView getViewForSheet(Sheet sheet);

        public void setEditable(boolean b);

        public void workbookChanged(Path oldPath, Path newPath);
    }

    enum MessageType {
        ERROR,
        INFO;
    }

    private static final Logger LOGGER = LogManager.getLogger(ExcelViewer.class);

    private static final String LICENSE = "Copyright %d %s%n"
            + "%n"
            + "Licensed under the Apache License, Version 2.0 (the \"License\");%n"
            + "you may not use this file except in compliance with the License.%n"
            + "You may obtain a copy of the License at%n"
            + "%n"
            + "    http://www.apache.org/licenses/LICENSE-2.0%n"
            + "%n"
            + "Unless required by applicable law or agreed to in writing, software%n"
            + "distributed under the License is distributed on an \"AS IS\" BASIS,%n"
            + "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.%n"
            + "See the License for the specific language governing permissions and%n"
            + "limitations under the License.%n";

    private static Optional<Path> getPath(Workbook workbook) {
        return workbook == null ? Optional.empty() : workbook.getPath();
    }

    private final String appName;

    private final int year;

    private final String author;

    /**
     * The currently opened workbook.
     */
    protected Workbook workbook = null;

    /**
     * The current directory.
     *
     * This is the default directory selected in the Open and Save To dialogs.
     */
    private Path currentPath = Paths.get("");

    ExcelViewerModel(String appName, int year, String author) {
        this.appName = appName;
        this.year = year;
        this.author = author;
    }

    /**
     * Adjust all column sizes.
     *
     * @param view
     *            the view
     */
    protected void adjustColumns(SheetView view) {
        if (view != null) {
            view.getSheet().autoSizeColumns();
        }
    }

    protected void freezeAtCurrentCell(SheetView view) {
        if (view != null) {
            final Sheet sheet = view.getSheet();
            Cell cell = sheet.getCurrentCell();
            sheet.splitAt(cell.getRowNumber(), cell.getColumnNumber());
        }
    }

    /**
     * Returns the current directory for this window.
     *
     * @return current directory
     */
    public Path getCurrentPath() {
        return currentPath;
    }

    public String getLicenseText() {
        return String.format(LICENSE, year, author);
    }

    Optional<Path> getPath() {
        return getPath(workbook);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void saveWorkbook(Path path) throws IOException {
        if (workbook == null) {
            LOGGER.warn("No Workbook open.");
            return;
        }

        LOGGER.info("Writing workbook to {}.", path);
        workbook.write(path);
        LOGGER.info("Workbook written to {}.", path);
    }

    /**
     * Sets the current directory for this window.
     *
     * @param path
     *            directory to set as current directory
     */
    public void setPath(Path path) {
        this.currentPath = path;
    }

    /**
     * Set the current workbook.
     *
     * @param workbook
     *            the workbook
     */
    public void setWorkbook(Workbook workbook) {
        if (this.workbook != null) {
            try {
                this.workbook.close();
            } catch (IOException ex) {
                LOGGER.error("IOException when closing workbook.", ex);
            }
        }
        this.workbook = workbook;
        LOGGER.info("Workbook changed to {}.", getPath(this.workbook));
    }

    protected void setZoom(float zoom) {
        for (Sheet sheet : workbook) {
            sheet.setZoom(zoom);
        }
    }

    protected void showInfo() {
        System.out.format("%s%n%n%s%n", appName, getLicenseText());
    }

}