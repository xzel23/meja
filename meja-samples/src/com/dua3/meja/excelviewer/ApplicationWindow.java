/*
 *
 */
package com.dua3.meja.excelviewer;

import com.dua3.meja.model.InvalidFormatException;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbookFactory;
import com.dua3.meja.ui.swing.WorkbookView;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author axel
 */
public class ApplicationWindow extends JFrame {

    /**
     * The application name to show in title bar.
     */
    public final static String APPLICATION_NAME = "Méja ExcelViewer";

    /**
     * The application instance this window belongs to.
     */
    private final ExcelViewer application;

    /**
     * The currently opened workbook.
     */
    private Workbook workbook = null;

    /**
     * The current directory.
     *
     * This is the default directory selected in the Open and Save To dialogs.
     */
    private File currentDir = new File(".");
    private WorkbookView workbookView = null;

    /**
     * Constructor
     *
     * @param application the application instance this window belongs to
     */
    public ApplicationWindow(ExcelViewer application) {
        super(APPLICATION_NAME);

        this.application = application;

        createMenu();

        createContent();

        pack();
    }

    /**
     * Sets the current directory for this window.
     *
     * @param currentDir directory to set as current directory
     */
    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    /**
     * Returns the current directory for this window.
     *
     * @return current directory
     */
    public File getCurrentDir() {
        return currentDir;
    }

    /**
     * Creates the application menu bar.
     */
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu mnFile = new JMenu("File");
        mnFile.add(new AbstractAction("Open") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showOpenDialog();
            }
        });
        mnFile.addSeparator();
        mnFile.add(new AbstractAction("Exit") {

            @Override
            public void actionPerformed(ActionEvent e) {
                closeApplication();
            }
        });
        menuBar.add(mnFile);

        setJMenuBar(menuBar);
    }

    private void createContent() {
        setLayout(new BorderLayout());
        workbookView = new WorkbookView();
        add(workbookView, BorderLayout.CENTER);
    }


    /**
     * Close the application window.
     */
    private void closeApplication() {
        Logger.getLogger(ApplicationWindow.class.getName()).log(Level.INFO, "Closing.");
        dispose();
    }

    /**
     * Show the Open dialog.
     */
    private void showOpenDialog() {
        JFileChooser jfc = new JFileChooser(currentDir);

        for (FileFilter filter : application.getFileFilters(OpenMode.READ)) {
            jfc.addChoosableFileFilter(filter);
        }

        int rc = jfc.showOpenDialog(this);

        if (rc == JFileChooser.APPROVE_OPTION) {
            final File file = jfc.getSelectedFile();

            FileFilter filter = jfc.getFileFilter();
            if (filter instanceof FilterDef) {
                final Object factory = ((FilterDef) filter).getFactory();
                if (!(factory instanceof WorkbookFactory)) {
                    throw new IllegalStateException("Factory must be instance of WorkbookFactory");
                }
                openFile(file, (WorkbookFactory) factory);
            } else {
                openFile(file, PoiWorkbookFactory.instance());
            }

        }
    }

    /**
     * Open the file.
     *
     * @param file the file to open
     */
    private void openFile(File file, WorkbookFactory factory) {
        try {
            setWorkbook(factory.open(file));
            Logger.getLogger(ApplicationWindow.class.getName()).log(Level.INFO, "Successfully loaded ''{0}''.", file);
            setTitle(APPLICATION_NAME+ " - "+file.getName());
            currentDir=file.getParentFile();
        } catch (IOException | InvalidFormatException ex) {
            Logger.getLogger(ApplicationWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error loading file", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Set the current workbook.
     * @param workbook
     */
    public void setWorkbook(Workbook workbook) {
        this.workbook=workbook;
        workbookView.setWorkbook(workbook);
    }

}
