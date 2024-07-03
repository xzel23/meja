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

/**
 * A simple demo application that just displays the file 'test.xlsx'.
 */
public final class FxShowTestXlsx extends Application {

    private static LogBuffer logBuffer;

    public static void main(String[] args) {
        LogUtilLog4J.init(LogLevel.TRACE);
        logBuffer = new LogBuffer(100000);
        LogUtil.getGlobalDispatcher().addLogEntryHandler(logBuffer);
        launch(args);
    }

    private Workbook wb;

    @Override
    public void start(Stage primaryStage) throws Exception {
        new FxLogWindow(logBuffer).show();

        final FxWorkbookView view = new FxWorkbookView();
        Scene scene = new Scene(view, 1000, 600);

        primaryStage.setTitle(getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();

        wb =  MejaHelper.openWorkbook(LangUtil.getResourceURL(FxShowTestXlsx.class, "test.xlsx").toURI());
        view.setWorkbook(wb);
    }
}
