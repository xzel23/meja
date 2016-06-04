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
package com.dua3.meja.ui.javafx;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;

/**
 * Swing component for displaying instances of class {@link Workbook}.
 *
 * @author axel
 */
public class JfxWorkbookView extends Region implements com.dua3.meja.ui.WorkbookView {

    private Workbook workbook;
    private TabPane content = null;

    /**
     * Construct a new {@code WorkbookView}.
     */
    public JfxWorkbookView() {
    }

    /**
     * Set the workbook.
     * @param workbook the workbook to display
     */
    @Override
    public void setWorkbook(Workbook workbook) {
        if (content != null) {
            this.getChildren().remove(content);
            content = null;
        }

        this.workbook = workbook;

        if (workbook != null) {
            // Create a new JTabbedPane with one tab per sheet.
            content = new TabPane();
            for (int i = 0; i < workbook.getSheetCount(); i++) {
                Sheet sheet = workbook.getSheet(i);
                final JfxSheetView sheetView = new JfxSheetView();
                sheetView.setSheet(sheet);
                content.getTabs().add(new Tab(sheet.getSheetName(), sheetView));
            }
            this.getChildren().add(content);
        }


    }

    /**
     * Get Workbook.
     * @return the workbook displayed
     */
    @Override
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * Set editable state.
     * @param editable set to {@code true} to allow editing of the displayed workbook
     */
    @Override
    public void setEditable(boolean editable) {
        if (content == null) {
            return;
        }

        for (Tab tab: content.getTabs()) {
            Node view = tab.getContent();
            if (view != null) {
                //assert view instanceof JfxSheetView;
                //((JfxSheetView) view).setEditable(editable);
            }
        }
    }

    /**
     * Get view for sheet.
     * @param sheetName name of the sheet
     * @return the view for the requested sheet or {@code null} if not found
     */
    @Override
    public SheetView getViewForSheet(String sheetName) { // FIXME change return type
        for (Tab tab: content.getTabs()) {
            Node view = tab.getContent();
            if (view != null) {
                //assert view instanceof JfxSheetView;
                //JfxSheetView sheetView = (JfxSheetView) view;
                //if (sheetView.getSheet().getSheetName().equals(sheetName)) {
                //    return sheetView;
                //}
            }
        }
        return null;
    }

    /**
     * Get the {@link SwingSheetView} that is currently visible.
     * @return the {@link SwingSheetView} displayed on the visible tab of this view
     */
    @Override
    public SheetView getCurrentView() {
        // FIXME
        return null;
    }

    public SheetView getViewForSheet(Sheet sheet) {
        if (content==null) {
            return null;
        }

        for (Tab tab: content.getTabs()) {
            Node view = tab.getContent();
            if (view != null) {
                //assert view instanceof JfxSheetView;
                //JfxSheetView sheetView = (SwingSheetView) view;
                //if (sheet==sheetView.getSheet()) {
                //    return sheetView;
                //}
            }
        }
        return null;
    }
}
