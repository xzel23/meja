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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;

/**
 * Swing component for displaying instances of class {@link Workbook}.
 *
 * @author axel
 */
public class WorkbookView extends JComponent {

    private static final long serialVersionUID = 1L;

    private Workbook workbook;
    private JTabbedPane content = null;

    public WorkbookView() {
        setLayout(new CardLayout());
    }

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
                final JScrollPane scrollPane = new JScrollPane(sheetView,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                content.addTab(sheet.getSheetName(), scrollPane);
            }
            content.setSelectedIndex(0);
            add(content);
            revalidate();
        }
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setEditable(boolean editable) {
        for (int i = 0; i < content.getTabCount(); i++) {
            Component scrollPane = content.getComponentAt(i);
            if (scrollPane != null) {
                assert scrollPane instanceof JScrollPane;
                for (Component viewPort : ((JScrollPane) scrollPane).getComponents()) {
                    if (viewPort == null || !(viewPort instanceof JViewport)) {
                        continue;
                    }
                    for (Component c : ((JViewport) viewPort).getComponents()) {
                        if (c instanceof SheetView) {
                            ((SheetView) c).setEditable(editable);
                        }
                    }
                }
            }
        }
    }

    public SheetView getViewForSheet(String sheetName) {
        for (int i = 0; i < content.getTabCount(); i++) {
            Component scrollPane = content.getComponentAt(i);
            if (scrollPane != null) {
                assert scrollPane instanceof JScrollPane;
                for (Component viewPort : ((JScrollPane) scrollPane).getComponents()) {
                    if (viewPort == null || !(viewPort instanceof JViewport)) {
                        continue;
                    }
                    for (Component c : ((JViewport) viewPort).getComponents()) {
                        if (c instanceof SheetView) {
                            SheetView sheetView = (SheetView) c;
                            if (sheetView.getSheet().getSheetName().equals(sheetName)) {
                                return sheetView;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
