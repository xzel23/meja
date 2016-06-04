/*
 *
 */
package com.dua3.meja.excelviewer;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.util.MejaHelper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author axel
 */
public class ExcelViewerModel {

    private static final Logger LOGGER = Logger.getLogger(ExcelViewer.class .getName());

    private final String appName;
    private final int year;
    private final String author;

    void openWorkbook(File file) throws IOException {
        setWorkbook(MejaHelper.openWorkbook(file));
    }

    URI getUri() {
        return workbook==null ? null : workbook.getUri();
    }

    enum MessageType {
        ERROR,
        INFO;
    }

    interface ExcelViewer {
        public void workbookChanged(URI oldUri, URI newUri);

        public void setEditable(boolean b);

        public SheetView getCurrentView();

        public SheetView getViewForSheet(Sheet sheet);
    }

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

    public String getLicenseText() {
        return String.format(LICENSE, year, author);
    }

    protected void showInfo() {
        System.out.format("%s%n%n%s%n", appName, getLicenseText());
    }

    ExcelViewerModel(String appName, int year, String author) {
        this.appName = appName;
        this.year = year;
        this.author = author;
    }

    /**
     * The currently opened workbook.
     */
    protected Workbook workbook = null;

    /**
     * The current directory.
     *
     * This is the default directory selected in the Open and Save To dialogs.
     */
    private File currentDir = new File(".");

    /**
     * Sets the current directory for this window.
     *
     * @param currentDir directory to set as current directory
     */
    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    /**
     * Returns the current directory for this window.
     *
     * @return current directory
     */
    public File getCurrentDir() {
        return currentDir;
    }

    protected void setZoom(float zoom) {
        for (Sheet sheet : workbook) {
            sheet.setZoom(zoom);
        }
    }

    protected void freezeAtCurrentCell(SheetView view) {
        if (view != null) {
            Cell cell = view.getSheet().getCurrentCell();
            view.getSheet().splitAt(cell.getRowNumber(), cell.getColumnNumber());
            view.updateContent();
        }
    }

    /**
     * Adjust all column sizes.
     */
    protected void adjustColumns(SheetView view) {
        if (view != null) {
            view.getSheet().autoSizeColumns();
            view.updateContent();
        }
    }

    /**
     * Set the current workbook.
     *
     * @param workbook the workbook
     */
    public void setWorkbook(Workbook workbook) {
        if (this.workbook != null) {
            try {
                this.workbook.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "IOException when closing workbook.", ex);
            }
        }
        this.workbook = workbook;
        LOGGER.log(Level.INFO, "Workbook changed to {0}.", getUri(this.workbook));
    }

    private URI getUri(Workbook workbook) {
        return workbook != null ? workbook.getUri() : null;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void saveWorkbook(URI uri) throws IOException {
        if (workbook == null) {
            LOGGER.log(Level.WARNING, "No Workbook open.");
            return;
        }

        final File file = new File(uri);
        workbook.write(file, true);
        LOGGER.log(Level.INFO, "Workbook written to {0}.", file.getAbsolutePath());
    }

}
