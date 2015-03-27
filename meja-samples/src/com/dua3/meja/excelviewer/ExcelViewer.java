/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
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
package com.dua3.meja.excelviewer;

import com.dua3.meja.model.poi.PoiWorkbookFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author axel
 */
public class ExcelViewer {

    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Set system L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ExcelViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

        ExcelViewer instance = new ExcelViewer();

        ApplicationWindow window = new ApplicationWindow(instance);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        window.setSize(600, 400);
        window.setVisible(true);
    }

    private FilterDef[] filters = {
        new FilterDef("Excel Files", OpenMode.READ_AND_WRITE, PoiWorkbookFactory.instance(), ".xls", ".xlsx")
    };

    public List<FileFilter> getFileFilters(OpenMode mode) {
        List<FileFilter> list = new ArrayList<>();
        for (final FilterDef filter : filters) {
            if (filter.isAppplicable(mode)) {
                list.add(filter);
            }
        }
        return list;
    }

}
