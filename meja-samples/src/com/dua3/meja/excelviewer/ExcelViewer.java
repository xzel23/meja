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

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.swing.SheetView;
import com.dua3.meja.ui.swing.WorkbookView;
import com.dua3.meja.util.MejaHelper;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 * A sample Swing application that uses the Meja library to load and display
 * Excel sheets or CSv data.
 *
 * @author axel
 */
public class ExcelViewer extends JFrame {

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

        ExcelViewer window = new ExcelViewer();
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        window.setSize(600, 400);
        window.setVisible(true);

        if (file != null) {
            window.setCurrentDir(file.getParentFile());
            try {
                window.setWorkbook(MejaHelper.openWorkbook(file));
            } catch (IOException ex) {
                Logger.getLogger(ExcelViewer.class.getName()).log(Level.SEVERE, "Could not load workbook from " + file.getAbsolutePath(), ex);
            }
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

    public static final String PROPERTY_FILE_CHANGED = "file changed";

    /**
     * The application name to show in title bar.
     */
    public final static String APPLICATION_NAME = "Méja ExcelViewer";

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
     * Constructor.
     */
    public ExcelViewer() {
        super(APPLICATION_NAME);
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
        mnFile.add(new AbstractAction("Open...") {

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
                ExcelViewer.this.addPropertyChangeListener(PROPERTY_FILE_CHANGED, listener);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                saveWorkbook();
            }

        });
        mnFile.add(new AbstractAction("Save as...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showSaveAsDialog();
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

        mnOptions.addSeparator();

        mnOptions.add(new AbstractAction("Freeze") {
            @Override
            public void actionPerformed(ActionEvent e) {
                freezeAtCurrentCell();
            }
        });

        menuBar.add(mnOptions);

        // Help menu
        JMenu mnHelp = new JMenu("Help");
        mnHelp.add(new AbstractAction("About ...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = "About " + ExcelViewer.getApplicationName();
                String msg = ExcelViewer.getLicenseText();
                JOptionPane.showMessageDialog(ExcelViewer.this, msg, title, JOptionPane.INFORMATION_MESSAGE, null);
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
        for (Sheet sheet : workbook) {
            sheet.setZoom(zoom);
            final SheetView view = workbookView.getViewForSheet(sheet.getSheetName());
            view.updateContent();
        }
    }

    private void freezeAtCurrentCell() {
        final SheetView view = workbookView.getCurrentView();
        if (view != null) {
            final int i = view.getCurrentRowNum();
            final int j = view.getCurrentColNum();
            view.getSheet().splitAt(i, j);
            view.updateContent();
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
        Logger.getLogger(ExcelViewer.class.getName()).log(Level.INFO, "Closing.");
        dispose();
    }

    /**
     * Show the Open dialog.
     */
    private void showOpenDialog() {
        try {
            final Workbook newWorkbook = MejaHelper.showDialogAndOpenWorkbook(this, currentDir);
            if (newWorkbook != null) {
                setWorkbook(newWorkbook);
                Logger.getLogger(ExcelViewer.class.getName()).log(Level.INFO, "Successfully loaded ''{0}''.", newWorkbook.getUri());
            }
        } catch (IOException ex) {
            Logger.getLogger(ExcelViewer.class.getName()).log(Level.SEVERE, "Exception loading workbook.", ex);
            JOptionPane.showMessageDialog(this, "Error loading workbook: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show the "Save as" dialog.
     */
    private void showSaveAsDialog() {
        try {
            final URI uri = MejaHelper.showDialogAndSaveWorkbook(this, workbook, currentDir);
            if (uri != null) {
                workbook.setUri(uri);
                updateTitle(uri);
                Logger.getLogger(ExcelViewer.class.getName()).log(Level.INFO, "Successfully saved ''{0}''.", uri);
            }
        } catch (IOException ex) {
            Logger.getLogger(ExcelViewer.class.getName()).log(Level.SEVERE, "Exception saving workbook.", ex);
            JOptionPane.showMessageDialog(this, "Error saving workbook: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Set the current workbook.
     *
     * @param workbook the workbook
     */
    public void setWorkbook(Workbook workbook) {
        final URI oldUri;
        if (this.workbook != null) {
            oldUri = this.workbook.getUri();
            try {
                this.workbook.close();
            } catch (Exception ex) {
                Logger.getLogger(ExcelViewer.class.getName()).log(Level.SEVERE, "IOException when closing workbook.", ex);
            }
        } else {
            oldUri = null;
        }

        this.workbook = workbook;

        workbookView.setWorkbook(workbook);
        workbookView.setEditable(true);

        URI newUri = workbook != null ? workbook.getUri() : null;
        updateTitle(newUri);
        firePropertyChange(PROPERTY_FILE_CHANGED, oldUri, newUri);
    }

    public void updateTitle(URI uri) {
        if (uri != null) {
            setTitle(APPLICATION_NAME + " - " + uri.getPath());
            try {
                currentDir = new File(uri);
            } catch (IllegalArgumentException e) {
                //nop
            }
        } else {
            setTitle(APPLICATION_NAME);
        }
    }

    public void saveWorkbook() {
        if (workbook == null) {
            return;
        }

        URI uri = workbook.getUri();
        try {
            if (uri == null) {
                uri = MejaHelper.showDialogAndSaveWorkbook(this, workbook, currentDir);
                if (uri == null) {
                    // user cancelled the dialog
                    return;
                }
            } else {
                File file = new File(uri);
                workbook.write(file, true);
            }
            Logger.getLogger(ExcelViewer.class.getName()).log(
                    Level.INFO,
                    "Workbook saved to {0}.",
                    uri.getPath());
        } catch (IOException ex) {
            Logger.getLogger(ExcelViewer.class.getName()).log(Level.SEVERE, "IO-Error saving workbook", ex);
            JOptionPane.showMessageDialog(
                    this,
                    "IO-Error saving workbook.",
                    "Workbook could not be saved.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
