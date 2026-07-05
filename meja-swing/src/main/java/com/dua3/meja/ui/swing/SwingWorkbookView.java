
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
import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow;

/**
 * Swing component for displaying instances of class {@link Workbook}.
 */
public final class SwingWorkbookView extends JComponent implements WorkbookView<SwingSheetView>, ChangeListener, Flow.Subscriber<WorkbookEvent> {
    private static final Logger LOG = LogManager.getLogger(SwingWorkbookView.class);

    // the workbook to show
    private transient @Nullable Workbook workbook;
    private boolean editable;
    private transient @Nullable Container toolbarParent;

    /**
     * Tabbed pane that contains the sheets of the workbook as tabs.
     */
    private final JTabbedPane content;

    /**
     * Construct a new {@code WorkbookView}.
     */
    public SwingWorkbookView() {
        setLayout(new CardLayout());

        content = new JTabbedPane(SwingConstants.BOTTOM);
        content.setUI(new BasicTabbedPaneUI() {
            @Override
            protected Insets getContentBorderInsets(int tabPlacement) {
                return new Insets(0, 0, 0, 0);
            }
        });
        content.addChangeListener(this);
        add(content);
    }

    @Override
    public Optional<SwingSheetView> getCurrentView() {
        Component component = content.getSelectedComponent();
        return component instanceof SwingSheetView swingSheetView ? Optional.of(swingSheetView) : Optional.empty();
    }

    @Override
    public Optional<SwingSheetView> getViewForSheet(Sheet sheet) {
        for (int i = 0; i < content.getTabCount(); i++) {
            Component view = content.getComponentAt(i);
            if (view instanceof SwingSheetView swingSheetView && sheet == swingSheetView.getSheet()) {
                return Optional.of(swingSheetView);
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
            if (view instanceof SwingSheetView swingSheetView && Objects.equals(swingSheetView.getSheet().getSheetName(), sheetName)) {
                return Optional.of(swingSheetView);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Workbook> getWorkbook() {
        return Optional.ofNullable(workbook);
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
        if (workbook != null) {
            LangUtil.asUnmodifiableList(content.getComponents())
                    .stream()
                    .filter(SwingSheetView.class::isInstance)
                    .map(SwingSheetView.class::cast)
                    .forEach(sv -> sv.setEditable(editable));
        }
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Returns the container used as application toolbar parent for all sheet views.
     *
     * @return the toolbar parent container or {@code null}
     */
    public @Nullable Container getToolbarParent() {
        return toolbarParent;
    }

    /**
     * Sets the application toolbar parent for all current and future sheet views.
     * If {@code null}, sheets will show the edit toolbar as floating while editing.
     *
     * @param toolbarParent the toolbar parent container or {@code null}
     */
    public void setToolbarParent(@Nullable Container toolbarParent) {
        this.toolbarParent = toolbarParent;
        if (workbook != null) {
            LangUtil.asUnmodifiableList(content.getComponents())
                    .stream()
                    .filter(SwingSheetView.class::isInstance)
                    .map(SwingSheetView.class::cast)
                    .forEach(sv -> sv.setToolbarParent(toolbarParent));
        }
    }

    @Override
    public void setWorkbook(@Nullable Workbook workbook) {
        content.removeAll();

        if (subscription != null) {
            subscription.cancel();
            this.subscription = null;
        }

        this.workbook = workbook;

        if (workbook != null) {
            for (int i = 0; i < workbook.getSheetCount(); i++) {
                Sheet sheet = workbook.getSheet(i);
                final SwingSheetView sheetView = new SwingSheetView(sheet);
                sheetView.setEditable(isEditable());
                sheetView.setToolbarParent(toolbarParent);
                sheetView.updateContent();
                content.addTab(sheet.getSheetName(), sheetView);
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
            default -> { /* do nothing */ }
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
