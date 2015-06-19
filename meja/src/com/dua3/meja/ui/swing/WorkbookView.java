/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

/**
 * Swing component for displaying instances of class {@link Workbook}.
 *
 * @author axel
 */
public class WorkbookView extends JComponent {

    private static final long serialVersionUID = 1L;

    private Workbook workbook;
    private JTabbedPane content = null;

    /**
     * Construct a new {@code WorkbookView}.
     */
    public WorkbookView() {
        setLayout(new CardLayout());
    }

    /**
     * Set the workbook.
     * @param workbook the workbook to display
     */
    public void setWorkbook(Workbook workbook) {
        if (content != null) {
            remove(content);
            content = null;
        }

        this.workbook = workbook;

        if (workbook != null) {
            // Create a new JTabbedPane with one tab per sheet.
            content = new JTabbedPane(JTabbedPane.BOTTOM);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetByNr(i);
                final SheetView sheetView = new SheetView(sheet);
                content.addTab(sheet.getSheetName(), sheetView);
            }
            content.setSelectedIndex(0);
            add(content);
            revalidate();
        }
    }

    /**
     * Get Workbook.
     * @return the workbook displayed
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * Set editable state.
     * @param editable set to {@code true} to allow editing of the displayed workbook
     */
    public void setEditable(boolean editable) {
        if (content == null) {
            return;
        }

        for (int i = 0; i < content.getTabCount(); i++) {
            Component view = content.getComponentAt(i);
            if (view != null) {
                assert view instanceof SheetView;
                ((SheetView) view).setEditable(editable);
            }
        }
    }

    /**
     * Get view for sheet.
     * @param sheetName name of the sheet
     * @return the view for the requested sheet or {@code null} if not found
     */
    public SheetView getViewForSheet(String sheetName) {
        for (int i = 0; i < content.getTabCount(); i++) {
            Component view = content.getComponentAt(i);
            if (view != null) {
                assert view instanceof SheetView;
                SheetView sheetView = (SheetView) view;
                if (sheetView.getSheet().getSheetName().equals(sheetName)) {
                    return sheetView;
                }
            }
        }
        return null;
    }

    /**
     * Get the {@link SheetView} that is currently visible.
     * @return the {@link SheetView} displayed on the visible tab of this view
     */
    public SheetView getCurrentView() {
        Component component = content.getSelectedComponent();
        return component instanceof SheetView ? (SheetView) component : null;
    }
}
