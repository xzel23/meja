/*
 *
 */
package com.dua3.meja.excelviewer;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;
import com.dua3.utility.logging.LogUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author axel
 */
public class ExcelViewerModel {

    interface ExcelViewer {
        SheetView getCurrentView();

        SheetView getViewForSheet(Sheet sheet);

        void setEditable(boolean b);

        void workbookChanged(URI oldUri, URI newUri);
    }

    enum MessageType {
        ERROR, INFO
    }

    private static final Logger LOGGER = Logger.getLogger(ExcelViewerModel.class.getName());

    private static final String LICENSE = "Copyright %d %s%n" + "%n"
            + "Licensed under the Apache License, Version 2.0 (the \"License\");%n"
            + "you may not use this file except in compliance with the License.%n"
            + "You may obtain a copy of the License at%n" + "%n" + "    http://www.apache.org/licenses/LICENSE-2.0%n"
            + "%n" + "Unless required by applicable law or agreed to in writing, software%n"
            + "distributed under the License is distributed on an \"AS IS\" BASIS,%n"
            + "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.%n"
            + "See the License for the specific language governing permissions and%n"
            + "limitations under the License.%n";

    private static Optional<URI> getUri(Workbook workbook) {
        return workbook == null ? Optional.empty() : workbook.getUri();
    }

    private final String appName;

    private final int year;

    private final String author;

    /**
     * The currently opened workbook.
     */
    private Workbook workbook = null;

    /**
     * The current directory.
     *
     * This is the default directory selected in the Open and Save To dialogs.
     */
    private URI currentUri = Paths.get("").toUri();

    ExcelViewerModel(String appName, int year, String author) {
        this.appName = appName;
        this.year = year;
        this.author = author;
    }

    /**
     * Adjust all column sizes.
     *
     * @param view the view
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
     * @return URI of current directory
     */
    public URI getCurrentUri() {
        return currentUri;
    }

    public String getLicenseText() {
        return String.format(LICENSE, year, author);
    }

    Optional<URI> getUri() {
        return getUri(workbook);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void saveWorkbook(URI uri) throws IOException {
        if (workbook == null) {
            LOGGER.warning("No Workbook open.");
            return;
        }

        LOGGER.fine(LogUtil.format("Writing workbook to %s", uri));
        workbook.write(uri);
        LOGGER.info(LogUtil.format("Workbook written to %s", uri));
    }

    /**
     * Sets the current directory for this window.
     *
     * @param uri directory to set as current directory
     */
    public void setUri(URI uri) {
        this.currentUri = uri;
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
        LOGGER.info(() -> "Workbook changed to " + getUri(this.workbook).map(Object::toString).orElse(""));
    }

    protected void setZoom(float zoom) {
        for (Sheet sheet : workbook) {
            sheet.setZoom(zoom);
        }
    }

    protected String getInfo() {
        return String.format("%s%n%n%s%n", appName, getLicenseText());
    }

}
