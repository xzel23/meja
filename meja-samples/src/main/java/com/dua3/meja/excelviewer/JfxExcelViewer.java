/*
 *
 */
package com.dua3.meja.excelviewer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.ui.javafx.JfxWorkbookView;
import com.dua3.meja.ui.javafx.MejaJfxHelper;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author axel
 */
public class JfxExcelViewer extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(JfxExcelViewer.class);
    
    /**
     * The application name to show in title bar.
     */
    public static final String APPLICATION_NAME = "Méja ExcelViewer";

    public static final String AUTHOR = "Axel Howind";

    public static final int YEAR = 2015;

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private final ExcelViewerModel model = new ExcelViewerModel(APPLICATION_NAME, YEAR, AUTHOR);

    private final JfxWorkbookView view = new JfxWorkbookView();

    private void closeApplication() {
        Platform.exit();
    }

    private SheetView getCurrentView() {
        return view.getCurrentView();
    }

    private void saveWorkbook() {
        Workbook workbook = model.getWorkbook();
        if (workbook == null) {
            return;
        }

        Optional<Path> path = workbook.getPath();
        try {
            if (!path.isPresent()) {
                path = MejaJfxHelper.showDialogAndSaveWorkbook(null, workbook, model.getPath().orElse(Paths.get("")));
                if (!path.isPresent()) {
                    // user cancelled the dialog
                    return;
                }
            } else {
                model.saveWorkbook(path.get());
            }
        } catch (IOException ex) {
            LOGGER.error("IO-Error saving workbook.", ex);
            JOptionPane.showMessageDialog(
                    null,
                    "IO-Error saving workbook.",
                    "Workbook could not be saved.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setZoom(float f) {
        model.setZoom(f);
    }

    private void showAboutDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About " + APPLICATION_NAME);
        alert.setHeaderText(APPLICATION_NAME);
        alert.setContentText(model.getLicenseText());
        alert.setResizable(true);
        alert.show();
    }

    private void showOpenDialog(Window parent) {
        try {
            final Optional<Path> oldPath = model.getPath();
            final Workbook newWorkbook = MejaJfxHelper.showDialogAndOpenWorkbook(parent, model.getPath().orElse(Paths.get("")))
                    .orElse(null);
            model.setWorkbook(newWorkbook);
            final Optional<Path> newPath = model.getPath();
            workbookChanged(oldPath.orElse(null), newPath.orElse(null));
        } catch (IOException ex) {
            LOGGER.error("Exception loading workbook.", ex);
            new Alert(AlertType.ERROR, "Error loading workbook: " + ex.getMessage()).showAndWait();
        }
    }

    private void showSaveAsDialog() {
        throw new UnsupportedOperationException("Not supported yet."); // To
                                                                       // change
                                                                       // body
                                                                       // of
                                                                       // generated
                                                                       // methods,
                                                                       // choose
                                                                       // Tools
                                                                       // |
                                                                       // Templates.
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);

        // File menu
        final Menu mFile = new Menu("File");

        final MenuItem miOpen = new MenuItem("Open...");
        miOpen.setOnAction(e -> showOpenDialog(primaryStage));

        final MenuItem miSave = new MenuItem("Save");
        miSave.setOnAction(e -> saveWorkbook());
        miSave.setOnMenuValidation(e -> miSave.setDisable(model.getWorkbook() == null));

        final MenuItem miSaveAs = new MenuItem("Save");
        miSaveAs.setOnAction(e -> showSaveAsDialog());
        miSaveAs.setOnMenuValidation(e -> miSave.setDisable(model.getWorkbook() == null));

        final MenuItem miExit = new MenuItem("Exit");
        miExit.setOnAction(e -> closeApplication());
        mFile.getItems().addAll(miOpen, miSave, miSaveAs, new SeparatorMenuItem(), miExit);

        // Edit menu
        final Menu mEdit = new Menu("Edit");

        final MenuItem miAdjustColumns = new MenuItem("Adjust all column widths");
        miAdjustColumns.setOnAction(e -> model.adjustColumns(getCurrentView()));

        mEdit.getItems().addAll(miAdjustColumns);

        // Options menu
        final Menu mOptions = new Menu("Options");

        final Menu mZoom = new Menu("Zoom");
        for (final int zoom : new int[] { 25, 50, 75, 100, 125, 150, 200, 400 }) {
            final MenuItem miZoom = new MenuItem(zoom + "%");
            miZoom.setOnAction(e -> setZoom(zoom / 100.0f));
            mZoom.getItems().add(miZoom);
        }

        final MenuItem miFreeze = new MenuItem("Freeze");
        miFreeze.setOnAction(e -> model.freezeAtCurrentCell(getCurrentView()));

        mOptions.getItems().addAll(mZoom, new SeparatorMenuItem(), miFreeze);

        // Help menu
        final Menu mHelp = new Menu("Help");
        final MenuItem miAbout = new MenuItem("About...");
        miAbout.setOnAction(e -> showAboutDialog());
        mHelp.getItems().addAll(miAbout);

        // create and add the menubar
        MenuBar mb = new MenuBar(mFile, mEdit, mOptions, mHelp);

        root.setTop(mb);

        // create workbook view
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        root.setCenter(view);

        primaryStage.setTitle(APPLICATION_NAME);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void workbookChanged(Path oldPath, Path newPath) {
        view.setWorkbook(model.getWorkbook());
    }
}
