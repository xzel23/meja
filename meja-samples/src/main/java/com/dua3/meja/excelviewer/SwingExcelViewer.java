/*
 * Copyright 2015 Axel Howind.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.excelviewer;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.swing.SwingUtil;

/**
 * A sample Swing application that uses the Meja library to load and display
 * Excel sheets or CSv data.
 *
 * @author axel
 */
@SuppressWarnings("serial")
public class SwingExcelViewer extends JFrame implements ExcelViewerModel.ExcelViewer, DropTargetListener {

    private static final Logger LOG = Logger.getLogger(SwingExcelViewer.class.getName());

    private static final int STATUS_ERROR = 1;

    public static final String PROPERTY_FILE_CHANGED = "file changed";

    /**
     * The application name to show in title bar.
     */
    public static final String APPLICATION_NAME = "Meja ExcelViewer";

    public static final String AUTHOR = "Axel Howind";

    public static final int YEAR = 2015;

    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtil.setNativeLookAndFeel(APPLICATION_NAME);

        SwingUtilities.invokeLater(() -> {
            ExcelViewerModel model = new ExcelViewerModel(APPLICATION_NAME, YEAR, AUTHOR);
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
                    viewer.setWorkbook(MejaHelper.openWorkbook(file.toURI()));
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Could not load workbook from " + file.getAbsolutePath(), ex);
                }
            }
        });
    }

    private final ExcelViewerModel model;

    private SwingWorkbookView workbookView;

    /**
     * Constructor.
     *
     * @param model the model
     */
    public SwingExcelViewer(ExcelViewerModel model) {
        super(APPLICATION_NAME);
        this.model = Objects.requireNonNull(model);
        createMenu();
        createContent();
        pack();
        setDropTarget(new DropTarget(this, this));
    }

    /**
     * Close the application window.
     */
    protected void closeApplication() {
        LOG.info("Closing.");
        dispose();
    }

    private void createContent() {
        setLayout(new BorderLayout());
        SwingWorkbookView view = new SwingWorkbookView();
        add(view, BorderLayout.CENTER);
        this.workbookView = view;
    }

    /**
     * Creates the application menu bar.
     */
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu mnFile = new JMenu("File");
        mnFile.add(SwingUtil.createAction("Open...", this::showOpenDialog));
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
        mnFile.add(SwingUtil.createAction("Save as...", this::showSaveAsDialog));
        mnFile.addSeparator();
        mnFile.add(SwingUtil.createAction("Exit", this::closeApplication));
        menuBar.add(mnFile);

        // Edit menu
        JMenu mnEdit = new JMenu("Edit");
        mnEdit.add(SwingUtil.createAction("Adjust all column widths", e -> model.adjustColumns(getCurrentView())));
        menuBar.add(mnEdit);

        // Options menu
        JMenu mnOptions = new JMenu("Options");

        JMenu mnLookAndFeel = new JMenu("Look & Feel");
        mnLookAndFeel.add(SwingUtil.createAction("System Default",
                e -> setLookAndFeel(UIManager.getSystemLookAndFeelClassName())));
        mnLookAndFeel.add(SwingUtil.createAction("Cross Platform",
                e -> setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())));
        mnLookAndFeel.addSeparator();
        for (final UIManager.LookAndFeelInfo lAF : UIManager.getInstalledLookAndFeels()) {
            mnLookAndFeel.add(SwingUtil.createAction(lAF.getName(), e -> setLookAndFeel(lAF.getClassName())));
        }

        mnOptions.add(mnLookAndFeel);

        JMenu mnZoom = new JMenu("Zoom");
        for (final int zoom : new int[] { 25, 50, 75, 100, 125, 150, 200, 400 }) {
            mnZoom.add(SwingUtil.createAction(zoom + "%", e -> setZoom(zoom / 100.0f)));
        }
        mnOptions.add(mnZoom);

        mnOptions.addSeparator();

        mnOptions.add(SwingUtil.createAction("Freeze", e -> model.freezeAtCurrentCell(getCurrentView())));

        menuBar.add(mnOptions);

        // Help menu
        JMenu mnHelp = new JMenu("Help");
        mnHelp.add(SwingUtil.createAction("About ...", e -> {
            String title = "About " + APPLICATION_NAME;
            String msg = model.getLicenseText();
            JOptionPane.showMessageDialog(SwingExcelViewer.this, msg, title, JOptionPane.INFORMATION_MESSAGE, null);
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }));
        menuBar.add(mnHelp);

        setJMenuBar(menuBar);
    }

    @Override
    public SheetView getCurrentView() {
        return workbookView.getCurrentView();
    }

    @Override
    public SheetView getViewForSheet(Sheet sheet) {
        return workbookView.getViewForSheet(sheet);
    }

    private void saveWorkbook() {
        Workbook workbook = model.getWorkbook();
        if (workbook == null) {
            return;
        }

        Optional<URI> uri = workbook.getUri();
        try {
            if (uri.isEmpty()) {
                final Optional<URI> newUri = MejaSwingHelper.showDialogAndSaveWorkbook(this, workbook,
                        model.getCurrentUri());
                if (newUri.isEmpty()) {
                    // user cancelled the dialog
                    LOG.info("save-dialog was cancelled.");
                    return;
                }
                workbookChanged(null /* old path was not set */, newUri.get());
            } else {
                model.saveWorkbook(uri.get());
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "IO-Error saving workbook.", ex);
            JOptionPane.showMessageDialog(this, "IO-Error saving workbook.", "Workbook could not be saved.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void setEditable(boolean editable) {
        workbookView.setEditable(editable);
    }

    private void setLookAndFeel(String lookAndFeelClassName) {
        try {
            UIManager.setLookAndFeel(lookAndFeelClassName);
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, "Could not set Look&Feel.", ex);
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void setZoom(float f) {
        model.setZoom(f);
    }

    /**
     * Show the Open dialog.
     */
    private void showOpenDialog() {
        try {
            final Optional<Workbook> newWorkbook = MejaSwingHelper.showDialogAndOpenWorkbook(this,
                    model.getCurrentUri());
            if (newWorkbook.isPresent()) {
                setWorkbook(newWorkbook.get());
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Exception loading workbook.", ex);
            JOptionPane.showMessageDialog(this, "Error loading workbook: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Unknown Exception caught, will be rethrown after message dialog: {0}",
                    ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading workbook: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            throw ex; // rethrow
        }
    }

    private void setWorkbook(final Workbook newWorkbook) {
        final Optional<URI> oldPath = model.getUri();
        model.setWorkbook(newWorkbook);
        final Optional<URI> newPath = model.getUri();
        workbookChanged(oldPath.orElse(null), newPath.orElse(null));
    }

    /**
     * Show the "Save as" dialog.
     */
    private void showSaveAsDialog() {
        try {
            Workbook workbook = model.getWorkbook();
            final Optional<URI> uri = MejaSwingHelper.showDialogAndSaveWorkbook(this, workbook,
                    model.getCurrentUri());
            if (uri.isPresent()) {
                workbook.setUri(uri.get());
                updateUri(uri.get());
                LOG.info("Saved workbook to '" + uri.get() + "'.'");
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Exception saving workbook.", ex);
            JOptionPane.showMessageDialog(this, "Error saving workbook: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE,
                    "Unknown Exception caught, will be rethrown after message dialog: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading workbook: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            throw ex; // rethrow
        }
    }

    private void updateUri(URI path) {
        if (path != null) {
            setTitle(APPLICATION_NAME + " - " + path.toString());
            model.setUri(path);
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

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        // nop
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        // nop
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // nop
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // nop
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                @SuppressWarnings("unchecked")
                List<File> files = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                if (files.size() == 1) {
                    URI uri = files.get(0).toURI();
                    Optional<Workbook> workbook = MejaSwingHelper.openWorkbook(this, uri);
                    if (workbook.isPresent()) {
                        setWorkbook(workbook.get());
                        dtde.getDropTargetContext().dropComplete(true);
                    } else {
                        LOG.log(Level.WARNING, () -> "Could not process dropped item '" + uri + "'.");
                        dtde.getDropTargetContext().dropComplete(false);
                    }
                }
            } else {
                LOG.warning("DataFlavor.javaFileListFlavor is not supported, drop rejected.");
                dtde.rejectDrop();
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Exception when processing dropped item, rejecting drop.", ex);
            dtde.rejectDrop();
        }
    }

}
