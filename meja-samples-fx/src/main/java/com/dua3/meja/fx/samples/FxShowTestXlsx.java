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

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * A simple demo application that just displays the file 'test.xlsx'.
 */
public final class FxShowTestXlsx extends Application {

    private static final LogBuffer logBuffer = createLogBuffer();

    public FxShowTestXlsx() throws IOException, URISyntaxException {}

    private static LogBuffer createLogBuffer() {
        LogUtilLog4J.init(LogLevel.TRACE);
        return new LogBuffer(100000);
    }

    public static void main(String[] args) {
        LogUtil.getGlobalDispatcher().addLogEntryHandler(logBuffer);
        launch(args);
    }

    private Workbook wb =  MejaHelper.openWorkbook(LangUtil.getResourceURL(FxShowTestXlsx.class, "test.xlsx").toURI());

    @Override
    public void start(Stage primaryStage) {
        new FxLogWindow(logBuffer).show();

        final FxWorkbookView view = new FxWorkbookView();
        Scene scene = new Scene(view, 1000, 600);

        primaryStage.setTitle(getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();

        view.setWorkbook(wb);
    }
}
