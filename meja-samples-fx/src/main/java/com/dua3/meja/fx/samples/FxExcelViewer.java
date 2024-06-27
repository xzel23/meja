package com.dua3.meja.fx.samples;

import com.dua3.meja.ui.fx.FxWorkbookView;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.fx.controls.Dialogs;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class FxExcelViewer extends Application {

    private FxWorkbookView fxWorkbookView;

    @Override
    public void start(Stage primaryStage) {
        // Initial setup
        primaryStage.setTitle("Spreadsheet Viewer");

        // Create UI components
        Label instructionLabel = new Label("Select a spreadsheet to view:");
        Button openFileButton = new Button("Open Spreadsheet");

        // FxWorkbookView setup (assuming FxWorkbookView has a default constructor)
        fxWorkbookView = new FxWorkbookView();

        // Setup file chooser action
        openFileButton.setOnAction(event -> openSpreadsheet(primaryStage));

        // Layout
        VBox root = new VBox(10, instructionLabel, openFileButton, fxWorkbookView);
        root.setSpacing(10);

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

    public static void main(String[] args) {
        launch(args);
    }
}