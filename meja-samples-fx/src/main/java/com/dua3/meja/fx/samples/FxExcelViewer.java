package com.dua3.meja.fx.samples;

import com.dua3.meja.ui.fx.FxWorkbookView;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.fx.controls.Controls;
import com.dua3.utility.fx.controls.Dialogs;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * The {@code FxExcelViewer} class provides a JavaFX application for viewing
 * spreadsheet files. This application allows users to open and displayExcel files.
 */
public class FxExcelViewer extends Application {

    private final FxWorkbookView fxWorkbookView = new FxWorkbookView();

    /**
     * The main entry point for the JavaFX application. This method launches the
     * JavaFX Runtime environment and initializes the primary application stage.
     *
     * @param args the command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Spreadsheet Viewer");

        // create menu
        MenuBar menuBar = new MenuBar(
                Controls.menu("File",
                        Controls.menuItem("Open", () -> openSpreadsheet(primaryStage)),
                        Controls.menuItem("Exit", Platform::exit)
                )
        );
        menuBar.setUseSystemMenuBar(true);

        // create tool bar
        ToolBar toolBar = new ToolBar(
                Controls.button().graphic(
                        Controls.graphic("fth-folder"))
                        .action(() -> openSpreadsheet(primaryStage))
                        .build(),
                Controls.slider()
                        .min(0.25)
                        .max(2)
                        .bind(fxWorkbookView.currentViewZoomProperty())
                        .blockIncrement(0.25)
                        .build()
        );

        // layout
        VBox root = new VBox();
        VBox.setVgrow(fxWorkbookView, Priority.ALWAYS);
        root.getChildren().addAll(menuBar, toolBar, fxWorkbookView);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to open a spreadsheet and display it in the FxWorkbookView
    private void openSpreadsheet(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Spreadsheets", "*.xlsx", "*.xls")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                fxWorkbookView.setWorkbook(MejaHelper.openWorkbook(selectedFile.toURI()));
            } catch (IOException e) {
                Dialogs.error(fxWorkbookView.getScene().getWindow())
                        .title("Error opening spreadsheet")
                        .text("Could not open spreadsheet: " + e.getMessage())
                        .showAndWait();
            }
        }
    }
}