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

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.ui.swing.WorkbookView;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author axel
 */
public class ApplicationWindow extends JFrame {

    public static final String PROPERTY_FILE_CHANGED = "file changed";

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

    private File workbookFile = null;

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

        // File menu
        JMenu mnFile = new JMenu("File");
        mnFile.add(new AbstractAction("Open") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showOpenDialog();
            }
        });
        mnFile.add(new AbstractAction("Save") {

            {
                // enable when workbook is loaded
                PropertyChangeListener listener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (PROPERTY_FILE_CHANGED.equals(evt.getPropertyName())) {
                            setEnabled(evt.getNewValue() != null);
                        }
                    }
                };
                ApplicationWindow.this.addPropertyChangeListener(PROPERTY_FILE_CHANGED, listener);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                saveWorkbook();
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

        // Options menu
        JMenu mnOptions = new JMenu("Options");

        JMenu mnLookAndFeel = new JMenu("Look & Feel");
        mnLookAndFeel.add(new AbstractAction("System Default") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        });
        mnLookAndFeel.add(new AbstractAction("Cross Platform") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        });
        mnLookAndFeel.addSeparator();
        for (final UIManager.LookAndFeelInfo lAF : UIManager.getInstalledLookAndFeels()) {
            mnLookAndFeel.add(new AbstractAction(lAF.getName()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setLookAndFeel(lAF.getClassName());
                }
            });
        }

        mnOptions.add(mnLookAndFeel);

        JMenu mnZoom = new JMenu("Zoom");
        for (final int zoom : new int[]{25, 50, 75, 100, 125, 150, 200, 400}) {
            mnZoom.add(new AbstractAction(zoom + "%") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setZoom(zoom / 100.0f);
                }
            });
        }
        mnOptions.add(mnZoom);

        menuBar.add(mnOptions);

        // Help menu
        JMenu mnHelp = new JMenu("Help");
        mnHelp.add(new AbstractAction("About ...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = "About " + ExcelViewer.getApplicationName();
                String msg = ExcelViewer.getLicenseText();
                JOptionPane.showMessageDialog(ApplicationWindow.this, msg, title, JOptionPane.INFORMATION_MESSAGE, null);
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        });
        menuBar.add(mnHelp);

        setJMenuBar(menuBar);
    }

    private void setLookAndFeel(String lookAndFeelClassName) {
        try {
            UIManager.setLookAndFeel(lookAndFeelClassName);
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ExcelViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void setZoom(float zoom) {
        for (Sheet sheet: workbook) {
            sheet.setZoom(zoom);
            workbookView.getViewForSheet(sheet.getSheetName()).invalidate();
        }
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
                final WorkbookFactory factory = ((FilterDef) filter).getFactory();
                openFile(file, factory);
            } else {
                openFile(file);
            }
        }
    }

    /**
     * Open file.
     *
     * This method does not propagate IOExceptions. Instead, a message dialog is
     * shown.
     *
     * @param file the file to open
     * @param factory the factory to use
     */
    public void openFile(File file, WorkbookFactory factory) {
        try {
            doOpenFile(file, factory);
        } catch (IOException ex) {
            Logger.getLogger(ApplicationWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error loading file", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Open file.
     *
     * @param file the file to open
     * @param factory the factory to use
     * @throws IOException
     */
    private void doOpenFile(File file, WorkbookFactory factory) throws IOException {
        setWorkbook(factory.open(file), file);
        Logger.getLogger(ApplicationWindow.class.getName()).log(Level.INFO, "Successfully loaded ''{0}''.", file);
        setTitle(APPLICATION_NAME + " - " + file.getName());
        currentDir = file.getParentFile();
    }

    /**
     * Open file, trying all available filters.
     *
     * This method does not propagate IOExceptions. Instead, a message dialog is
     * shown.
     *
     * @param file the file to open
     */
    public void openFile(File file) {
        try {
            doOpenFile(file);
        } catch (IOException ex) {
            Logger.getLogger(ApplicationWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error loading file", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Open file, trying all available filters.
     *
     * @param file the file to open
     * @throws IOException
     */
    private void doOpenFile(File file) throws IOException {
        for (FilterDef filter : application.getFileFilters(OpenMode.READ)) {
            try {
                if (filter.accept(file)) {
                    doOpenFile(file, filter.getFactory());
                    return;
                }
            } catch (IOException ex) {
                Logger.getLogger(ApplicationWindow.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        throw new IOException("Could not load '" + file.getPath() + "' with any of the available filters.");
    }

    /**
     * Set the current workbook.
     *
     * @param workbook the workbook
     * @param file the workbook file (used for saving)
     */
    public void setWorkbook(Workbook workbook, File file) {
        try {
            if (this.workbook != null) {
                this.workbook.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(ApplicationWindow.class.getName()).log(Level.SEVERE, "IOException when closing workbook.", ex);
        }

        File oldFile = workbookFile;
        this.workbook = workbook;
        this.workbookFile = file;
        workbookView.setWorkbook(workbook);
        workbookView.setEditable(true);
        firePropertyChange(PROPERTY_FILE_CHANGED, oldFile, this.workbookFile);
    }

    public void saveWorkbook() {
        if (workbook == null || workbookFile == null) {
            return;
        }

        try {
            workbook.write(workbookFile, true);
            Logger.getLogger(ApplicationWindow.class.getName()).log(
                    Level.INFO,
                    "Workbook saved to {0}.",
                    workbookFile.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(ApplicationWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(
                    this,
                    "IO-Error saving workbook to " + workbookFile.getAbsolutePath() + ".",
                    "Workbook could not be saved.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
