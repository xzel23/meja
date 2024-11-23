package com.dua3.meja.fx.samples;

import com.dua3.meja.ui.fx.FxWorkbookView;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.fx.controls.Dialogs;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class FxExcelViewer extends Application {

    private final FxWorkbookView fxWorkbookView = new FxWorkbookView();

    @Override
    public void start(Stage primaryStage) {
        // Create MenuBar
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(event -> openSpreadsheet(primaryStage));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(event -> Platform.exit());
        fileMenu.getItems().addAll(openItem, exitItem);

        menuBar.getMenus().add(fileMenu);

        primaryStage.setTitle("Spreadsheet Viewer");

        // Layout
        VBox root = new VBox();
        VBox.setVgrow(fxWorkbookView, Priority.ALWAYS);
        root.getChildren().addAll(menuBar, fxWorkbookView);

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