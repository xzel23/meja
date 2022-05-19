/*
 *
 */
package com.dua3.meja.excelviewer;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;

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

    private static final String LICENSE = """
                                          Copyright %d %s
                                                                                    
                                          Licensed under the Apache License, Version 2.0 (the "License");
                                          you may not use this file except in compliance with the License.
                                          You may obtain a copy of the License at
                                                                                    
                                              https://www.apache.org/licenses/LICENSE-2.0
                                                                                    
                                          Unless required by applicable law or agreed to in writing, software
                                          distributed under the License is distributed on an "AS IS" BASIS,
                                          WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                                          See the License for the specific language governing permissions and
                                          limitations under the License.
                                          """;

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

        LOGGER.finer(() -> "Writing workbook to " + uri);
        workbook.write(uri);
        LOGGER.fine(() -> "Workbook written to " + uri);
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
