package com.dua3.meja.fx.samples;

import com.dua3.fx.application.FxApplicationHelper;
import com.dua3.meja.io.FileTypeHtml;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.io.FileTypeCsv;
import com.dua3.meja.model.poi.io.FileTypeExcel;
import com.dua3.meja.model.poi.io.FileTypeXls;
import com.dua3.meja.model.poi.io.FileTypeXlsx;
import com.dua3.meja.ui.fx.FxWorkbookView;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.fx.controls.Controls;
import com.dua3.utility.fx.controls.Dialogs;
import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.lang.SystemInfo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@code FxExcelViewer} class provides a JavaFX application for viewing
 * spreadsheet files. This application allows users to open and displayExcel files.
 */
public final class FxExcelViewer {

    private static final String APP_NAME = "Meja Excel Viewer";
    private static final String APP_VERSION = "0.1";
    private static final String COPYRIGHT = "2025";
    private static final String DEVELOPER_MAIL = "axh@dua3.com";
    private static final String APP_DESCRIPTION = "An Excel Viewer Application";

    /**
     * The main entry point for the JavaFX application. This method launches the
     * JavaFX Runtime environment and initializes the primary application stage.
     *
     * @param args the command-line arguments passed to the application
     */
    public static void main(String[] args) {
        FxApplicationHelper.runApplication(
                FxExcelViewerApplication.class.getName(),
                args,
                APP_NAME,
                APP_VERSION,
                COPYRIGHT,
                DEVELOPER_MAIL,
                APP_DESCRIPTION
        );
    }

    private FxExcelViewer() {
        // nothing to do
    }

    /**
     * The Application class.
     *
     * <p><strong>Note:</strong> The application class must not be a top level class when using the
     * {@link FxApplicationHelper} to start the application as JavaFX "hacks" the startup code to
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

            FxApplicationHelper.showLogWindow(primaryStage);

            // create menu
            MenuBar menuBar = new MenuBar(
                    Controls.menu("File",
                            Controls.menuItem("Open", () -> openWorkbook(primaryStage)),
                            Controls.menuItem("Export...",
                                    () -> export(primaryStage, FileTypeCsv.instance(), FileTypeHtml.instance()),
                                    Bindings.isNotNull(fxWorkbookView.workbookProperty())),
                            Controls.menuItem("Exit", Platform::exit)
                    ),
                    Controls.menu("Help",
                            Controls.menuItem("System Information", () ->
                                    Dialogs.information(primaryStage)
                                            .title("System Information")
                                            .header(SystemInfo.getSystemInfo().formatted())
                                            .build()
                                            .showAndWait()
                            )
                    )
            );
            menuBar.setUseSystemMenuBar(true);

            // create toolbar
            ToolBar toolBar = new ToolBar(
                    Controls.button().graphic(
                                    Controls.graphic("fth-folder"))
                            .action(() -> openWorkbook(primaryStage))
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

        /**
         * Show a file dialog and let the user open a workbook to display.
         *
         * @param stage the stage to use as the dialog parent
         */
        private void openWorkbook(Stage stage) {
            FileChooser fileChooser = new FileChooser();

            Stream.of(FileTypeExcel.instance(), FileTypeXlsx.instance(), FileTypeXls.instance())
                    .filter(ft -> ft.isSupported(OpenMode.READ))
                    .map(ft -> new FileChooser.ExtensionFilter(ft.getName(), ft.getExtensionPatterns()))
                    .forEach(fileChooser.getExtensionFilters()::add);

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                try {
                    fxWorkbookView.setWorkbook(MejaHelper.openWorkbook(selectedFile.toURI()));
                } catch (IOException e) {
                    Dialogs.error(fxWorkbookView.getScene().getWindow())
                            .title("Error opening workbook")
                            .text("Could not open workbook: " + e.getMessage())
                            .showAndWait();
                }
            }
        }

        /**
         * Show a file dialog and let the user export the current workbook to a file of the provided type.
         *
         * @param stage the stage to use as the dialog parent
         * @param fileTypes vargargs array og the supported {@link FileType} instances
         */
        @SafeVarargs
        private void export(Stage stage, FileType<? extends Workbook>... fileTypes) {
            if (fxWorkbookView.getWorkbook().isEmpty()) {
                return;
            }

            // create file chooser
            FileChooser fileChooser = new FileChooser();

            // add extension filters
            Map<FileChooser.ExtensionFilter, FileType<? extends Workbook>> types = Arrays.stream(fileTypes)
                    .collect(Collectors
                            .toMap(ft ->
                                    new FileChooser.ExtensionFilter(ft.getName(), ft.getExtensionPatterns()),
                                    Function.identity(), (a, b) -> b,
                                    IdentityHashMap::new));
            fileChooser.getExtensionFilters().addAll(types.keySet());

            // get result file and type
            File selectedFile = fileChooser.showSaveDialog(stage);
            FileType<? extends Workbook> fileType = types.get(fileChooser.getSelectedExtensionFilter());

            // export
            if (selectedFile != null && fileType != null) {
                fxWorkbookView.getWorkbook().ifPresent(wb -> {
                    try (OutputStream out = Files.newOutputStream(selectedFile.toPath())) {
                        wb.write(fileType, out);
                    } catch (IOException e) {
                        Dialogs.error(fxWorkbookView.getScene().getWindow())
                                .title("Error exporting workbook")
                                .text("Could export the workbook as %s: %s", fileType.getName(), e.getMessage())
                                .showAndWait();
                    }
                });
            }
        }
    }
}