/*
 *
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
