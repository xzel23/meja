/*
 *
 */
package com.dua3.meja.excelviewer;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.ui.WorkbookView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * The Model class for {@code ExcelViewer} implementations.
 *
 * @param <WV> the {@link WorkbookView} generic parameter
 * @param <SV> the {@link SheetView} generic parameter
 */
public class ExcelViewerModel<WV extends WorkbookView<SV>, SV extends SheetView> {

    interface ExcelViewer<WV extends WorkbookView<SV>, SV extends SheetView> {
        Optional<SV> getCurrentView();

        Optional<SV> getViewForSheet(Sheet sheet);

        void setEditable(boolean b);

        void workbookChanged(URI oldUri, URI newUri);
    }

    private static final Logger LOGGER = LogManager.getLogger(ExcelViewerModel.class);

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

    private static Optional<URI> getUri(@Nullable Workbook workbook) {
        return workbook == null ? Optional.empty() : workbook.getUri();
    }

    private final String appName;

    private final int year;

    private final String author;

    /**
     * The currently opened workbook.
     */
    private @Nullable Workbook workbook;

    /**
     * The current directory.
     * <p>
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
    protected void adjustColumns(@Nullable SheetView view) {
        if (view != null) {
            view.getSheet().autoSizeColumns();
        }
    }

    /**
     * Splits the current sheet at the position of the current cell in the given view.
     * If the provided view is null, the method does nothing.
     *
     * @param view the {@code SheetView} containing the sheet and current cell,
     *             or {@code null} if no view is provided
     */
    protected void splitAtCurrentCell(@Nullable SheetView view) {
        if (view != null) {
            Sheet sheet = view.getSheet();
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

    /**
     * Retrieves the text of the license formatted with the provided year and author.
     *
     * @return the formatted license text as a {@code String}.
     */
    public String getLicenseText() {
        return String.format(LICENSE, year, author);
    }

    /**
     * Retrieves the URI associated with the workbook in the current context, if available.
     *
     * @return an {@code Optional<URI>} containing the URI of the workbook if the workbook is not null,
     *         or an empty {@code Optional} if the workbook is null.
     */
    Optional<URI> getUri() {
        return getUri(workbook);
    }

    /**
     * Retrieves the current workbook associated with the model, if available.
     *
     * @return an {@code Optional<Workbook>} containing the current workbook if it is not null,
     *         or an empty {@code Optional} if the workbook is null.
     */
    public Optional<Workbook> getWorkbook() {
        return Optional.ofNullable(workbook);
    }

    /**
     * Saves the current workbook to the specified URI.
     * <p>
     * This method writes the current workbook to the provided URI. If no workbook
     * is currently open, the method logs a warning and returns without performing
     * any operation. The format of the saved file is determined by the extension
     * of the provided URI.
     *
     * @param uri the URI to which the workbook is to be saved.
     * @throws IOException if an I/O error occurs during the saving process.
     */
    public void saveWorkbook(URI uri) throws IOException {
        if (workbook == null) {
            LOGGER.warn("no workbook open");
            return;
        }

        LOGGER.trace("writing workbook to {}", uri);
        workbook.write(uri);
        LOGGER.debug("workbook written to {}", uri);
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
    public void setWorkbook(@Nullable Workbook workbook) {
        if (this.workbook != null) {
            try {
                this.workbook.close();
            } catch (IOException ex) {
                LOGGER.error("exception closing workbook", ex);
            }
        }
        this.workbook = workbook;
        LOGGER.debug("workbook changed to {}", () -> getUri(this.workbook).map(Object::toString).orElse(""));
    }

    /**
     * Sets the zoom level for all sheets in the current workbook.
     * If no workbook is open, the method returns without performing any operation.
     *
     * @param zoom the zoom level to be applied to all sheets in the workbook.
     */
    protected void setZoom(float zoom) {
        if (workbook == null) {
            return;
        }
        for (Sheet sheet : workbook) {
            sheet.setZoom(zoom);
        }
    }

    /**
     * Retrieves application information, including the application name and license text.
     *
     * @return a formatted string containing the application name followed by the license text.
     */
    protected String getInfo() {
        return String.format("%s%n%n%s%n", appName, getLicenseText());
    }

}
