
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
import com.dua3.meja.model.WorkbookEvent;
import com.dua3.meja.ui.WorkbookView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.CardLayout;
import java.awt.Component;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow;

/**
 * Swing component for displaying instances of class {@link Workbook}.
 *
 * @author axel
 */
public class SwingWorkbookView extends JComponent implements WorkbookView<SwingSheetView>, ChangeListener, Flow.Subscriber<WorkbookEvent> {
    private static final Logger LOG = LogManager.getLogger(SwingWorkbookView.class);

    private transient @Nullable Workbook workbook;
    private final JTabbedPane content;

    /**
     * Construct a new {@code WorkbookView}.
     */
    public SwingWorkbookView() {
        setLayout(new CardLayout());

        content = new JTabbedPane(SwingConstants.BOTTOM);
        content.addChangeListener(this);
        add(content);
    }

    /**
     * Get the {@link SwingSheetView} that is currently visible.
     *
     * @return the {@link SwingSheetView} displayed on the visible tab of this view
     */
    @Override
    public Optional<SwingSheetView> getCurrentView() {
        Component component = content != null ? content.getSelectedComponent() : null;
        return component instanceof SwingSheetView swingSheetView ? Optional.of(swingSheetView) : Optional.empty();
    }

    @Override
    public Optional<SwingSheetView> getViewForSheet(Sheet sheet) {
        for (int i = 0; i < content.getTabCount(); i++) {
            Component view = content.getComponentAt(i);
            if (view instanceof SwingSheetView swingSheetView) {
                //noinspection ObjectEquality
                if (sheet == swingSheetView.getSheet().orElse(null)) {
                    return Optional.of(swingSheetView);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get view for sheet.
     *
     * @param sheetName name of the sheet
     * @return the view for the requested sheet or {@code null} if not found
     */
    public Optional<SwingSheetView> getViewForSheet(String sheetName) {
        for (int i = 0; i < content.getTabCount(); i++) {
            Component view = content.getComponentAt(i);
            if (view instanceof SwingSheetView swingSheetView) {
                if (Objects.equals(swingSheetView.getSheet().map(Sheet::getSheetName).orElse(null), sheetName)) {
                    return Optional.of(swingSheetView);
                }
            }
        }
        return Optional.empty();
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

        for (int i = 0; i < content.getTabCount(); i++) {
            Component view = content.getComponentAt(i);
            if (view instanceof SwingSheetView swingSheetView) {
                swingSheetView.setEditable(editable);
            }
        }
    }

    /**
     * Set the workbook.
     *
     * @param workbook the workbook to display
     */
    @Override
    public void setWorkbook(@Nullable Workbook workbook) {
        content.removeAll();

        if (this.subscription != null) {
            this.subscription.cancel();
            this.subscription = null;
        }

        this.workbook = workbook;

        if (workbook != null) {
            for (int i = 0; i < workbook.getSheetCount(); i++) {
                Sheet sheet = workbook.getSheet(i);
                final SwingSheetView sheetView = new SwingSheetView();
                content.addTab(sheet.getSheetName(), sheetView);
                sheetView.setSheet(sheet);
            }
            if (workbook.getSheetCount() > 0) {
                content.setSelectedIndex(workbook.getCurrentSheetIndex());
            }
            revalidate();

            workbook.subscribe(this);
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        //noinspection ObjectEquality
        if (evt.getSource() == content) {
            int idx = content.getSelectedIndex();
            if (workbook != null && idx >= 0) {
                workbook.setCurrentSheet(idx);
            }
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
                SwingUtilities.invokeLater(() -> setWorkbook(item.source()));
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
