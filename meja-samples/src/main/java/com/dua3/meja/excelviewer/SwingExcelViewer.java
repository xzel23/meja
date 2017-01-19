/*
 * Copyright 2015 Axel Howind.
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
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

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.ui.swing.MejaSwingHelper;
import com.dua3.meja.ui.swing.SwingWorkbookView;

/**
 * A sample Swing application that uses the Meja library to load and display
 * Excel sheets or CSv data.
 *
 * @author axel
 */
@SuppressWarnings("serial")
public class SwingExcelViewer extends JFrame implements ExcelViewerModel.ExcelViewer {

    private static final int STATUS_ERROR = 1;

    private final ExcelViewerModel model;

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
            Logger.getLogger(SwingExcelViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

        ExcelViewerModel model = new ExcelViewerModel(APPLICATION_NAME,YEAR, AUTHOR);
        SwingExcelViewer viewer = new SwingExcelViewer(model);

        if (args.length > 1) {
            model.showInfo();
            System.exit(STATUS_ERROR);
        }

        File file = args.length == 1 ? new File(args[0]) : null;

        viewer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        viewer.setSize(600, 400);
        viewer.setVisible(true);

        if (file != null) {
            try {
                model.setCurrentDir(file.getParentFile());
                model.openWorkbook(file);
            } catch (IOException ex) {
                Logger.getLogger(SwingExcelViewer.class.getName()).log(Level.SEVERE, "Could not load workbook from " + file.getAbsolutePath(), ex);
            }
        }
    }


    public static final String PROPERTY_FILE_CHANGED = "file changed";

    /**
     * The application name to show in title bar.
     */
    public final static String APPLICATION_NAME = "Meja ExcelViewer";

    public final static String AUTHOR = "Axel Howind";

    public static final int YEAR = 2015;

    private SwingWorkbookView workbookView;

    /**
     * Constructor.
     * @param model the model
     */
    public SwingExcelViewer(ExcelViewerModel model) {
        super(APPLICATION_NAME);
        this.model=Objects.requireNonNull(model);
        createMenu();
        createContent();
        pack();
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
                PropertyChangeListener listener = (PropertyChangeEvent evt) -> {
                    if (PROPERTY_FILE_CHANGED.equals(evt.getPropertyName())) {
                        setEnabled(evt.getNewValue() != null);
                    }
                };
                SwingExcelViewer.this.addPropertyChangeListener(PROPERTY_FILE_CHANGED, listener);
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

        // Edit menu
        JMenu mnEdit = new JMenu("Edit");
        mnEdit.add(new AbstractAction("Adjust all column widths") {

            @Override
            public void actionPerformed(ActionEvent e) {
                model.adjustColumns(getCurrentView());
            }
        });
        menuBar.add(mnEdit);

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
                model.freezeAtCurrentCell(getCurrentView());
            }
        });

        menuBar.add(mnOptions);

        // Help menu
        JMenu mnHelp = new JMenu("Help");
        mnHelp.add(new AbstractAction("About ...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = "About " + APPLICATION_NAME;
                String msg = model.getLicenseText();
                JOptionPane.showMessageDialog(SwingExcelViewer.this, msg, title, JOptionPane.INFORMATION_MESSAGE, null);
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
            Logger.getLogger(SwingExcelViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
        SwingUtilities.updateComponentTreeUI(this);
    }


    private void createContent() {
        setLayout(new BorderLayout());
        SwingWorkbookView view = new SwingWorkbookView();
        add(view, BorderLayout.CENTER);
        this.workbookView = view;
    }

    /**
     * Close the application window.
     */
    protected void closeApplication() {
        Logger.getLogger(SwingExcelViewer.class.getName()).log(Level.INFO, "Closing.");
        dispose();
    }


    /**
     * Show the Open dialog.
     */
    private void showOpenDialog() {
        try {
            final URI oldUri = model.getUri();
            final Workbook newWorkbook = MejaSwingHelper.showDialogAndOpenWorkbook(this, model.getCurrentDir());
            model.setWorkbook(newWorkbook);
            final URI newUri = model.getUri();
            workbookChanged(oldUri, newUri);
        } catch (IOException ex) {
            Logger.getLogger(SwingExcelViewer.class.getName()).log(Level.SEVERE, "Exception loading workbook.", ex);
            JOptionPane.showMessageDialog(this, "Error loading workbook: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show the "Save as" dialog.
     */
    private void showSaveAsDialog() {
        try {
            Workbook workbook = model.getWorkbook();
            final URI uri = MejaSwingHelper.showDialogAndSaveWorkbook(this, workbook, model.getCurrentDir());
            if (uri != null) {
                workbook.setUri(uri);
                updateUri(uri);
                Logger.getLogger(SwingExcelViewer.class.getName()).log(Level.INFO, "Successfully saved ''{0}''.", uri);
            }
        } catch (IOException ex) {
            Logger.getLogger(SwingExcelViewer.class.getName()).log(Level.SEVERE, "Exception saving workbook.", ex);
            JOptionPane.showMessageDialog(this, "Error saving workbook: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUri(URI uri) {
        if (uri != null) {
            setTitle(APPLICATION_NAME + " - " + uri.getPath());
            if (uri.getScheme().equalsIgnoreCase("file")) {
              model.setCurrentDir(new File(uri).getParentFile());
            }
        } else {
            setTitle(APPLICATION_NAME);
        }
    }

    @Override
    public void workbookChanged(URI oldUri, URI newUri) {
        firePropertyChange(PROPERTY_FILE_CHANGED, oldUri, newUri);
        workbookView.setWorkbook(model.getWorkbook());
        updateUri(newUri);
    }


    private void saveWorkbook() {
        Workbook workbook = model.getWorkbook();
        if (workbook == null) {
            return;
        }

        URI uri = workbook.getUri();
        try {
            if (uri == null) {
                final URI newUri = MejaSwingHelper.showDialogAndSaveWorkbook(this, workbook, model.getCurrentDir());
                if (newUri == null) {
                    // user cancelled the dialog
                    return;
                }
                workbookChanged(null /* uri */, newUri);
            } else {
                model.saveWorkbook(uri);
            }
        } catch (IOException ex) {
            Logger.getLogger(SwingExcelViewer.class.getName()).log(Level.SEVERE, "IO-Error saving workbook", ex);
            JOptionPane.showMessageDialog(
                    this,
                    "IO-Error saving workbook.",
                    "Workbook could not be saved.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void setEditable(boolean editable) {
        workbookView.setEditable(editable);
    }

    @Override
    public SheetView getCurrentView() {
        return workbookView.getCurrentView();
    }

    @Override
    public SheetView getViewForSheet(Sheet sheet) {
        return workbookView.getViewForSheet(sheet);
    }

    private void setZoom(float f) {
        model.setZoom(f);
    }

}
