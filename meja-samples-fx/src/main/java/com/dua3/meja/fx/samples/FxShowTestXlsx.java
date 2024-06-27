package com.dua3.meja.fx.samples;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.fx.FxWorkbookView;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.lang.LangUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple demo application that just displays the file 'test.xlsx'.
 */
public final class FxShowTestXlsx extends Application {
    private static final Logger LOG = LogManager.getLogger(FxShowTestXlsx.class);

    public static void main(String[] args) {
        launch(args);
    }

    private Workbook wb;

    @Override
    public void start(Stage primaryStage) throws Exception {
        final FxWorkbookView view = new FxWorkbookView();
        Scene scene = new Scene(view, 1000, 600);

        primaryStage.setTitle(getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();

        wb =  MejaHelper.openWorkbook(LangUtil.getResourceURL(FxShowTestXlsx.class, "test.xlsx").toURI());
        view.setWorkbook(wb);
    }
}
