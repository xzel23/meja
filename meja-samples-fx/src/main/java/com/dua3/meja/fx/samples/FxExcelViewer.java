package com.dua3.meja.fx.samples;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.poi.io.FileTypeExcel;
import com.dua3.meja.ui.fx.FxWorkbookView;
import com.dua3.utility.fx.FxLauncher;
import com.dua3.utility.fx.controls.Controls;
import com.dua3.utility.fx.controls.Dialogs;
import com.dua3.utility.fx.controls.SliderWithButtons;
import com.dua3.utility.io.IoUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The {@code FxExcelViewer} class provides a JavaFX application for viewing
 * spreadsheet files. This application allows users to open and displayExcel files.
 */
public final class FxExcelViewer {

    private static final String APP_NAME = "Meja Excel Viewer";
    private static final String APP_VERSION = "0.1";
    private static final String COPYRIGHT = "2025";
    private static final String DEVELOPER_MAIL = "axh@dua3.com";
    private static final String APP_DESCRIPTION = "An Excel Viewer";

    private FxExcelViewer() {
        // nothing to do
    }

    /**
     * The main entry point for the JavaFX application. This method launches the
     * JavaFX Runtime environment and initializes the primary application stage.
     *
     * @param args the command-line arguments passed to the application
     */
    public static void main(String[] args) {
        FxLauncher.launchApplication(
                FxExcelViewerApplication.class.getName(),
                args,
                APP_NAME,
                APP_VERSION,
                COPYRIGHT,
                DEVELOPER_MAIL,
                APP_DESCRIPTION
        );
    }

    /**
     * The Application class.
     *
     * <p><strong>Note:</strong> The application class must not be a top level class when using the
     * {@link FxLauncher} to start the application as JavaFX "hacks" the startup code to
     * start the JavaFX platform before {@code main()} is called.
     */
    public static class FxExcelViewerApplication extends Application {
        private final FxWorkbookView fxWorkbookView = new FxWorkbookView();

        /**
         * Default constructor for the FxExcelViewerApplication class.
         *
         * <p>This constructor initializes an instance of the FxExcelViewerApplication.
         * No specific initialization operations are performed in this constructor.
         */
        public FxExcelViewerApplication() {
            // nothing to do
        }

        @Override
        public void start(Stage primaryStage) {
            primaryStage.setTitle("Excel Viewer");

            FxLauncher.showLogWindow(primaryStage);

            // create menu
            MenuBar menuBar = new MenuBar(
                    Controls.menu()
                            .text("File")
                            .items(
                                    Controls.menuItem().text("Open").action(() -> openWorkbook(primaryStage)).build(),
                                    Controls.menuItem().text("Save As...").action(() -> saveWorkbookAs(primaryStage)).build(),
                                    Controls.menuItem().text("Exit").action(Platform::exit).build()
                            )
                            .build(),
                    Controls.menu()
                            .text("Help").items(
                                    Controls.menuItem().text("System Information").action(() -> Dialogs.alert(primaryStage, AlertType.INFORMATION)).build()
                            )
                            .build()
            );
            menuBar.setUseSystemMenuBar(true);

            // create toolbar
            ToolBar toolBar = new ToolBar(
                    Controls.button()
                            .graphic(Controls.graphic("fth-folder"))
                            .action(() -> openWorkbook(primaryStage))
                            .build(),
                    Controls.button()
                            .graphic(Controls.graphic("fth-save"))
                            .action(() -> saveWorkbookAs(primaryStage))
                            .enabled(Bindings.isNotNull(fxWorkbookView.workbookProperty()))
                            .build(),
                    Controls.slider()
                            .mode(SliderWithButtons.Mode.SLIDER_VALUE)
                            .formatter((v, max) -> "%3.0f%%".formatted(100 * v))
                            .min(0.25)
                            .max(2)
                            .blockIncrement(0.25)
                            .bindBidirectional(fxWorkbookView.currentViewZoomProperty())
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

        /**
         * Show a file dialog and let the user open a workbook to display.
         *
         * @param stage the stage to use as the dialog parent
         */
        private void openWorkbook(Stage stage) {
            try {
                Dialogs.openFile(stage, Workbook.class, FileTypeExcel.instance())
                        .ifPresent(fxWorkbookView::setWorkbook);
            } catch (Dialogs.UnsupportedFileTypeException e) {
                Dialogs.alert(stage, AlertType.ERROR)
                        .title("Unsupported file type")
                        .text("The correct file type to use for opening the workbook could not be determined.")
                        .showAndWait();
            } catch (IOException e) {
                Dialogs.alert(stage, AlertType.ERROR)
                        .title("Error opening workbook")
                        .text("Could not open workbook: " + e.getMessage())
                        .showAndWait();
            }
        }

        /**
         * Show a file dialog and let the user export the current workbook to a file of the provided type.
         *
         * @param stage the stage to use as the dialog parent
         */
        private void saveWorkbookAs(Stage stage) {
            fxWorkbookView.getWorkbook().ifPresent(wb -> {
                try {
                    Dialogs.saveToFile(
                            stage,
                            wb,
                            FileTypeExcel.instance(),
                            wb.getUri().map(IoUtil::toPath).orElse(null)
                    );
                } catch (Dialogs.UnsupportedFileTypeException e) {
                    Dialogs.alert(stage, AlertType.ERROR)
                            .title("Unsupported file type")
                            .text("The correct file type to use for saving the workbook could not be determined.")
                            .showAndWait();
                } catch (IOException e) {
                    Dialogs.alert(stage, AlertType.ERROR)
                            .title("Error saving workbook")
                            .text("Could not save workbook: " + e.getMessage())
                            .showAndWait();
                }
            });
        }
    }
}