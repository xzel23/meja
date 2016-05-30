/*
 *
 */
package com.dua3.meja.excelviewer;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.SheetView;
import com.dua3.meja.util.MejaJfxHelper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
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
import javax.swing.JOptionPane;

/**
 *
 * @author axel
 */
public class JfxExcelViewer extends Application {

    private static final Logger LOGGER = Logger.getLogger(JfxExcelViewer.class.getName());
    /**
     * The application name to show in title bar.
     */
    public final static String APPLICATION_NAME = "Méja ExcelViewer";

    public final static String AUTHOR = "Axel Howind";

    public static final int YEAR = 2015;

    private final ExcelViewerModel model = new ExcelViewerModel(APPLICATION_NAME,YEAR, AUTHOR);

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);

        // File menu
        final Menu mFile = new Menu("File");

        final MenuItem miOpen = new MenuItem("Open...");
        miOpen.setOnAction((e) -> showOpenDialog());

        final MenuItem miSave = new MenuItem("Save");
        miSave.setOnAction((e) -> saveWorkbook());
        miSave.setOnMenuValidation((e) -> miSave.setDisable(model.getWorkbook()==null));

        final MenuItem miSaveAs = new MenuItem("Save");
        miSaveAs.setOnAction((e) -> showSaveAsDialog());
        miSaveAs.setOnMenuValidation((e) -> miSave.setDisable(model.getWorkbook()==null));

        final MenuItem miExit = new MenuItem("Exit");
        miExit.setOnAction( e -> closeApplication() );
        mFile.getItems().addAll(miOpen, miSave, miSaveAs, new SeparatorMenuItem(), miExit);

        // Edit menu
        final Menu mEdit = new Menu("Edit");

        final MenuItem miAdjustColumns = new MenuItem("Adjust all column widths");
        miAdjustColumns.setOnAction( (e) -> model.adjustColumns(getCurrentView()) );

        mEdit.getItems().addAll(miAdjustColumns);

        // Options menu
        final Menu mOptions = new Menu("Options");

        final Menu mZoom = new Menu("Zoom");
        for (final int zoom : new int[]{25, 50, 75, 100, 125, 150, 200, 400}) {
            final MenuItem miZoom = new MenuItem(zoom + "%");
            miZoom.setOnAction((e) -> setZoom(zoom / 100.0f));
            mZoom.getItems().add(miZoom);
        }

        final MenuItem miFreeze = new MenuItem("Freeze");
        miFreeze.setOnAction((e) -> model.freezeAtCurrentCell(getCurrentView()));

        mOptions.getItems().addAll(mZoom, new SeparatorMenuItem(), miFreeze);

        // Help menu
        final Menu mHelp = new Menu("Help");
        final MenuItem miAbout = new MenuItem("About...");
        miAbout.setOnAction((e) -> showAboutDialog());
        mHelp.getItems().addAll(miAbout);

        // create and add the menubar
        MenuBar mb = new MenuBar(mFile, mEdit, mOptions, mHelp);

        root.setTop(mb);

        primaryStage.setTitle(APPLICATION_NAME);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    void closeApplication() {
        Platform.exit();
    }

    private void showOpenDialog() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

     private void saveWorkbook() {
        Workbook workbook = model.getWorkbook();
        if (workbook == null) {
            return;
        }

        URI uri = workbook.getUri();
        try {
            if (uri == null) {
                uri = MejaJfxHelper.showDialogAndSaveWorkbook(null, workbook, model.getCurrentDir());
                if (uri == null) {
                    // user cancelled the dialog
                    return;
                }
            } else {
                File file = new File(uri);
                model.saveWorkbook(uri);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IO-Error saving workbook", ex);
            JOptionPane.showMessageDialog(
                    null,
                    "IO-Error saving workbook.",
                    "Workbook could not be saved.",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSaveAsDialog() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private SheetView getCurrentView() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void setZoom(float f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void showAboutDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About "+APPLICATION_NAME);
        alert.setHeaderText(APPLICATION_NAME);
        alert.setContentText(model.getLicenseText());
        alert.setResizable(true);
        alert.show();
    }
}
