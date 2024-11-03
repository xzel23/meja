/*
 *
 */
package com.dua3.meja.excelviewer;

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
 * @author axel
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
            view.getSheet().ifPresent(Sheet::autoSizeColumns);
        }
    }

    protected void freezeAtCurrentCell(@Nullable SheetView view) {
        if (view != null) {
            view.getSheet()
                    .flatMap(Sheet::getCurrentCell)
                    .ifPresent(cell -> cell.getSheet().splitAt(cell.getRowNumber(), cell.getColumnNumber()));
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

    public Optional<Workbook> getWorkbook() {
        return Optional.ofNullable(workbook);
    }

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
        LOGGER.debug("workbook changed to {}", getUri(this.workbook).map(Object::toString).orElse(""));
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
