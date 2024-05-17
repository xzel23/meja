package com.dua3.meja.fx.samples;

import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.ui.fx.FxWorkbookView;
import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map.Entry;

/**
 * Kitchensink example.
 */
public final class FxKitchenSink extends Application {
    private static final Logger LOG = LogManager.getLogger(FxKitchenSink.class);

    public static void main(String[] args) {
        launch(args);
    }

    private static void addColorSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("colors");

        Row row = sheet.getRow(0);
        row.getCell(0).set("Color");
        row.getCell(1).set("Code");
        row.getCell(2).set("darker");
        row.getCell(3).set("brighter");

        sheet.splitAt(1, 0);

        for (Entry<String, Color> e : Color.palette().entrySet()) {
            String name = e.getKey();

            CellStyle cs = wb.getCellStyle(name);
            cs.setFillFgColor(e.getValue());
            cs.setFillPattern(FillPattern.SOLID);

            CellStyle csDark = wb.getCellStyle(name + "Dark");
            csDark.setFillFgColor(e.getValue().darker());
            csDark.setFillPattern(FillPattern.SOLID);

            CellStyle csBright = wb.getCellStyle(name + "Bright");
            csBright.setFillFgColor(e.getValue().brighter());
            csBright.setFillPattern(FillPattern.SOLID);

            row = sheet.getRow(sheet.getRowCount());
            row.getCell(0).set(name).setCellStyle(cs);
            row.getCell(1).set(e.getValue().toString()).setCellStyle(cs);
            row.getCell(2).set("darker").setCellStyle(csDark);
            row.getCell(3).set("brighter").setCellStyle(csBright);
        }
    }

    private static void addTextColorSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("text colors");

        Row row = sheet.getRow(0);
        row.getCell(0).set("Color");
        row.getCell(1).set("Code");
        row.getCell(2).set("darker");
        row.getCell(3).set("brighter");

        sheet.splitAt(1, 0);

        CellStyle csDefault = wb.getDefaultCellStyle();
        Font fontDefault = csDefault.getFont();
        for (Entry<String, Color> e : Color.palette().entrySet()) {
            String name = "font" + e.getKey();

            CellStyle cs = wb.getCellStyle(name);
            cs.setFont(fontDefault.deriveFont(FontDef.color(e.getValue())));

            CellStyle csDark = wb.getCellStyle(name + "Dark");
            csDark.setFont(fontDefault.deriveFont(FontDef.color(e.getValue().darker())));

            CellStyle csBright = wb.getCellStyle(name + "Bright");
            csBright.setFont(fontDefault.deriveFont(FontDef.color(e.getValue().brighter())));

            row = sheet.getRow(sheet.getRowCount());
            row.getCell(0).set(name).setCellStyle(cs);
            row.getCell(1).set(e.getValue().toString()).setCellStyle(cs);
            row.getCell(2).set("darker").setCellStyle(csDark);
            row.getCell(3).set("brighter").setCellStyle(csBright);
        }
    }

    private static Workbook createWorkbook(WorkbookFactory<?> factory) {
        Workbook wb = factory.create();

        addColorSheet(wb);
        addTextColorSheet(wb);

        return wb;
    }

    private Workbook wb;

    @Override
    public void start(Stage primaryStage) throws Exception {
        final FxWorkbookView view = new FxWorkbookView();
        Scene scene = new Scene(view, 1000, 600);

        primaryStage.setTitle(getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();

        wb = createWorkbook(GenericWorkbookFactory.instance());
        view.setWorkbook(wb);
    }
}
