
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

import java.awt.CardLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.ui.WorkbookView;

/**
 * Swing component for displaying instances of class {@link Workbook}.
 *
 * @author axel
 */
@SuppressWarnings("serial")
public class SwingWorkbookView extends JComponent implements WorkbookView, ChangeListener, PropertyChangeListener {

    private Workbook workbook = null;
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
    public SwingSheetView getCurrentView() {
        Component component = content != null ? content.getSelectedComponent() : null;
        return component instanceof SwingSheetView ? (SwingSheetView) component : null;
    }

    public SwingSheetView getViewForSheet(Sheet sheet) {
        for (int i = 0; i < content.getTabCount(); i++) {
            Component view = content.getComponentAt(i);
            if (view != null) {
                assert view instanceof SwingSheetView;
                SwingSheetView sheetView = (SwingSheetView) view;
                //noinspection ObjectEquality
                if (sheet == sheetView.getSheet()) {
                    return sheetView;
                }
            }
        }
        return null;
    }

    /**
     * Get view for sheet.
     *
     * @param sheetName name of the sheet
     * @return the view for the requested sheet or {@code null} if not found
     */
    @Override
    public SwingSheetView getViewForSheet(String sheetName) {
        for (int i = 0; i < content.getTabCount(); i++) {
            Component view = content.getComponentAt(i);
            if (view != null) {
                assert view instanceof SwingSheetView;
                SwingSheetView sheetView = (SwingSheetView) view;
                if (sheetView.getSheet().getSheetName().equals(sheetName)) {
                    return sheetView;
                }
            }
        }
        return null;
    }

    /**
     * Get Workbook.
     *
     * @return the workbook displayed
     */
    @Override
    public Workbook getWorkbook() {
        return workbook;
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
            if (view != null) {
                assert view instanceof SwingSheetView;
                ((SheetView) view).setEditable(editable);
            }
        }
    }

    /**
     * Set the workbook.
     *
     * @param workbook the workbook to display
     */
    @Override
    public void setWorkbook(Workbook workbook) {
        content.removeAll();

        if (this.workbook!=null) {
            this.workbook.removePropertyChangeListener(Workbook.PROPERTY_SHEET_ADDED, this);
            this.workbook.removePropertyChangeListener(Workbook.PROPERTY_SHEET_REMOVED, this);
        }

        this.workbook = workbook;

        if (workbook != null) {
            for (int i = 0; i < workbook.getSheetCount(); i++) {
                Sheet sheet = workbook.getSheet(i);
                final SwingSheetView sheetView = new SwingSheetView(sheet);
                content.addTab(sheet.getSheetName(), sheetView);
            }
            if (workbook.getSheetCount() > 0) {
                content.setSelectedIndex(workbook.getCurrentSheetIndex());
            }
            revalidate();

            workbook.addPropertyChangeListener(Workbook.PROPERTY_SHEET_ADDED, this);
            workbook.addPropertyChangeListener(Workbook.PROPERTY_SHEET_REMOVED, this);
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        //noinspection ObjectEquality
        if (evt.getSource() == content) {
            int idx = content.getSelectedIndex();
            if (workbook != null && idx>=0) {
                workbook.setCurrentSheet(idx);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        switch (property) {
            case Workbook.PROPERTY_SHEET_ADDED, Workbook.PROPERTY_SHEET_REMOVED -> SwingUtilities.invokeLater(() -> setWorkbook(workbook));
            default -> {
            }
        }
    }
}
