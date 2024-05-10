
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
package com.dua3.meja.ui.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.WorkbookView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Swing component for displaying instances of class {@link Workbook}.
 *
 * @author axel
 */
public class FxWorkbookView extends BorderPane implements WorkbookView {

    private Workbook workbook;
    private final TabPane content;

    /**
     * Construct a new {@code WorkbookView}.
     */
    public FxWorkbookView() {
        content = new TabPane();
        setCenter(content);
    }

    /**
     * Get the {@link FxSheetView} that is currently visible.
     *
     * @return the {@link FxSheetView} displayed on the visible tab of this view
     */
    @Override
    public Optional<FxSheetView> getCurrentView() {

        return Optional.ofNullable(
                content.getSelectionModel().getSelectedItem())
                .map(Tab::getContent)
                .map(node -> node instanceof FxSheetView fxsv ? fxsv : null);
    }

    /**
     * Get view for sheet.
     *
     * @param sheet the sheet
     * @return the view for the requested sheet or {@code null} if not found
     */
    public Optional<FxSheetView> getViewForSheet(Sheet sheet) {
        return streamSheetViews()
                .filter(sv -> sv.getSheet().filter(s -> s == sheet).isPresent())
                .findFirst();
    }

    /**
     * Get view for sheet.
     *
     * @param sheetName name of the sheet
     * @return the view for the requested sheet or {@code null} if not found
     */
    @Override
    public Optional<FxSheetView> getViewForSheet(String sheetName) {
        return streamSheetViews()
                .filter(sv -> sv.getSheet().filter(s -> Objects.equals(s.getSheetName(), sheetName)).isPresent())
                .findFirst();
    }

    /**
     * Stream the {@link FxSheetView} objects from the content tabs.
     *
     * @return a stream of {@link FxSheetView} objects from the content tabs
     */
    private Stream<FxSheetView> streamSheetViews() {
        return content.getTabs().stream().map(Tab::getContent)
                .map(node -> node instanceof FxSheetView fxsv ? fxsv : null)
                .filter(Objects::nonNull);
    }

    /**
     * Get Workbook.
     *
     * @return the workbook displayed
     */
    @Override
    public Optional<Workbook> getWorkbook() {
        return Optional.ofNullable(workbook);
    }

    /**
     * Set editable state.
     *
     * @param editable set to {@code true} to allow editing of the displayed
     *                 workbook
     */
    @Override
    public void setEditable(boolean editable) {
        if (content == null) {
            return;
        }

        content.getTabs().stream().map(Tab::getContent).forEach( node -> {
            if (node instanceof FxSheetView fxsv) {
                fxsv.setEditable(editable);
            }
        });
    }

    /**
     * Set the workbook.
     *
     * @param workbook the workbook to display
     */
    @Override
    public void setWorkbook(@Nullable Workbook workbook) {
        content.getTabs().clear();

        if (this.workbook != null) {
            this.workbook.removePropertyChangeListener(Workbook.PROPERTY_SHEET_ADDED, this);
            this.workbook.removePropertyChangeListener(Workbook.PROPERTY_SHEET_REMOVED, this);
        }

        this.workbook = workbook;

        if (workbook != null) {
            for (int i = 0; i < workbook.getSheetCount(); i++) {
                Sheet sheet = workbook.getSheet(i);
                final FxSheetView sheetView = new FxSheetView(sheet);
                content.getTabs().add(new Tab(sheet.getSheetName(), sheetView));
            }
            if (workbook.getSheetCount() > 0) {
                content.getSelectionModel().select(workbook.getCurrentSheetIndex());
            }

            workbook.addPropertyChangeListener(Workbook.PROPERTY_SHEET_ADDED, this);
            workbook.addPropertyChangeListener(Workbook.PROPERTY_SHEET_REMOVED, this);
        }
    }
}
