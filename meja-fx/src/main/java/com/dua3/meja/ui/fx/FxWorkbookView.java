
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

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookEvent;
import com.dua3.meja.ui.WorkbookView;
import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.geometry.Side;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow;

/**
 * Swing component for displaying instances of class {@link Workbook}.
 *
 * @author axel
 */
public class FxWorkbookView extends BorderPane implements WorkbookView<FxSheetView>, Flow.Subscriber<WorkbookEvent> {
    private static final Logger LOG = LogManager.getLogger(FxWorkbookView.class);

    private transient @Nullable Workbook workbook;
    private final TabPane content;

    /**
     * Construct a new {@code WorkbookView}.
     */
    public FxWorkbookView() {
        content = new TabPane();
        content.setSide(Side.BOTTOM);
        content.getSelectionModel().selectedIndexProperty().addListener((v, o, n) -> {
            int idx = n.intValue();
            if (workbook != null && idx >= 0) {
                workbook.setCurrentSheet(idx);
                Platform.runLater(
                        () -> getCurrentView().ifPresent(FxSheetView::requestFocus)
                );
            }
        });


        setCenter(content);

        // prevent arrow keys from being consumed by the tab pane
        addArrowKeyFilter(content);
    }

    private void addArrowKeyFilter(EventTarget target) {
        target.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().isArrowKey()) {
                getCurrentView().ifPresent(view -> {
                    if (!event.isConsumed()) {
                        view.onKeyPressed(event);
                    }
                });
                event.consume();
            }
        });
    }

    /**
     * Get the {@link FxSheetView} that is currently visible.
     *
     * @return the {@link FxSheetView} displayed on the visible tab of this view
     */
    @Override
    public Optional<FxSheetView> getCurrentView() {
        //noinspection ReturnOfNull
        return Optional.of(content)
                .map(TabPane::getSelectionModel)
                .map(SelectionModel::getSelectedItem)
                .map(Tab::getContent)
                .map(content -> content instanceof FxSheetView sv ? sv : null);
    }

    @Override
    public Optional<FxSheetView> getViewForSheet(Sheet sheet) {
        //noinspection ReturnOfNull
        return content.getTabs().stream()
                .map(Tab::getContent)
                .map(content -> content instanceof FxSheetView sv ? sv : null)
                .filter(Objects::nonNull)
                .filter(sv -> sv.getSheet() == sheet)
                .findFirst();
    }

    /**
     * Get view for sheet.
     *
     * @param sheetName name of the sheet
     * @return the view for the requested sheet or {@code null} if not found
     */
    public Optional<FxSheetView> getViewForSheet(String sheetName) {
        //noinspection ReturnOfNull
        return content.getTabs().stream()
                .map(Tab::getContent)
                .map(content -> content instanceof FxSheetView sv ? sv : null)
                .filter(Objects::nonNull)
                .filter(sv -> Objects.equals(sv.getSheet().getSheetName(), sheetName))
                .findFirst();
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
        //noinspection ReturnOfNull
        content.getTabs().stream()
                .map(Tab::getContent)
                .map(content -> content instanceof FxSheetView sv ? sv : null)
                .filter(Objects::nonNull)
                .forEach(sv -> sv.setEditable(editable));
    }

    /**
     * Set the workbook.
     *
     * @param workbook the workbook to display
     */
    @Override
    public void setWorkbook(@Nullable Workbook workbook) {
        content.getTabs().clear();

        if (this.subscription != null) {
            this.subscription.cancel();
            this.subscription = null;
        }

        this.workbook = workbook;

        if (workbook != null) {
            content.getTabs().setAll(
                    workbook.sheets()
                            .map(sheet -> new Tab(sheet.getSheetName(), new FxSheetView(sheet)))
                            .toList()
            );
            if (workbook.getSheetCount() > 0) {
                content.getSelectionModel().select(workbook.getCurrentSheetIndex());
            }

            workbook.subscribe(this);
        }
    }

    private transient Flow.@Nullable Subscription subscription = null;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        if (this.subscription != null) {
            this.subscription.cancel();
        }
        this.subscription = subscription;
        this.subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(WorkbookEvent item) {
        switch (item.type()) {
            case WorkbookEvent.SHEET_ADDED, WorkbookEvent.SHEET_REMOVED -> {
                LOG.debug("handling event: {}", item);
                setWorkbook(item.source());
            }
            default -> {}
        }
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("error with subscription", throwable);
    }

    @Override
    public void onComplete() {
        LOG.debug("subscription completed");
        this.subscription = null;
    }
}
