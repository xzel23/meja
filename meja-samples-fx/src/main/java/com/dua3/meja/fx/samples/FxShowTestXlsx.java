package com.dua3.meja.fx.samples;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.fx.FxWorkbookView;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.fx.FxLogWindow;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.LogUtil;
import com.dua3.utility.logging.log4j.LogUtilLog4J;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * A simple demo application that just displays the file 'test.xlsx'.
 */
public final class FxShowTestXlsx extends Application {

    private static final @Nullable LogBuffer logBuffer;

    private static final boolean SHOW_LOG_WINDOW = false;

    static {
        LogBuffer buffer = null;
        if (SHOW_LOG_WINDOW) {
            LogUtilLog4J.init(LogLevel.TRACE);
            buffer = new LogBuffer(100000);
            LogUtil.getGlobalDispatcher().addLogEntryHandler(buffer);
        }
        logBuffer = buffer;
    }

    private final Workbook wb;

    /**
     * Constructor.
     * @throws IOException when the workbork could not be read.
     * @throws URISyntaxException when the workbook URI is invalid.
     */
    public FxShowTestXlsx() throws IOException, URISyntaxException {
        this.wb = MejaHelper.openWorkbook(LangUtil.getResourceURL(FxShowTestXlsx.class, "test.xlsx").toURI());
    }

    /**
     * The main entry point of the application. This method initializes the global log entry handler
     * with a log buffer and launches the JavaFX application.
     *
     * @param args the command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        if (logBuffer != null) {
            new FxLogWindow(logBuffer).show();
        }

        final FxWorkbookView view = new FxWorkbookView();
        Scene scene = new Scene(view, 1000, 600);

        primaryStage.setTitle(getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();

        view.setWorkbook(wb);
    }
}
