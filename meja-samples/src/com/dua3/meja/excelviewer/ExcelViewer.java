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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 *
 * @author axel
 */
public class ExcelViewer {

    private static final String PROGRAM_NAME = ExcelViewer.class.getSimpleName();
    private static final int YEAR = 2015;
    private static final String AUTHOR = "Axel Howind (axel@dua3.com)";
    private static final String LICENSE = "Copyright %d %s%n"
            + "%n"
            + "Licensed under the Apache License, Version 2.0 (the \"License\");%n"
            + "you may not use this file except in compliance with the License.%n"
            + "You may obtain a copy of the License at%n"
            + "%n"
            + "    http://www.apache.org/licenses/LICENSE-2.0%n"
            + "%n"
            + "Unless required by applicable law or agreed to in writing, software%n"
            + "distributed under the License is distributed on an \"AS IS\" BASIS,%n"
            + "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.%n"
            + "See the License for the specific language governing permissions and%n"
            + "limitations under the License.%n";

    private static final int STATUS_ERROR = 1;

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

        if (args.length > 1) {
            info();
            System.exit(STATUS_ERROR);
        }

        File file = args.length == 1 ? new File(args[0]) : null;

        ExcelViewer instance = new ExcelViewer();

        ApplicationWindow window = new ApplicationWindow(instance);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        window.setSize(600, 400);
        window.setVisible(true);

        if (file!=null) {
            window.setCurrentDir(file.getParentFile());
            window.openFile(file);
        }
    }

    public static String getApplicationName() {
        return PROGRAM_NAME;
    }

    public static String getLicenseText() {
        return String.format(LICENSE, YEAR, AUTHOR);
    }

    private static void info() {
        System.out.format("%s%n%n%s%n", getApplicationName(), getLicenseText());
    }

    private FilterDef[] filters = {
        new FilterDef("Excel Files", OpenMode.READ_AND_WRITE, PoiWorkbookFactory.instance(), ".xls", ".xlsx", ".xlsm")
    };

    public List<FilterDef> getFileFilters(OpenMode mode) {
        List<FilterDef> list = new ArrayList<>();
        for (final FilterDef filter : filters) {
            if (filter.isAppplicable(mode)) {
                list.add(filter);
            }
        }
        return list;
    }

}
