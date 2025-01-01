package com.dua3.meja.samples;

import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.ui.swing.SwingWorkbookView;
import com.dua3.utility.data.Color;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.log4j.LogUtilLog4J;
import com.dua3.utility.swing.SwingLogFrame;
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.FontUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.io.IOException;
import java.io.Serial;
import java.util.Map.Entry;

/**
 * Kitchensink example.
 */
public final class KitchenSink extends JFrame {
    private static final Logger LOG;

    private static final boolean SHOW_LOG_WINDOW = System.getProperty("logwindow") != null;

    static {
        // this must be done before any logger is created!
        if (SHOW_LOG_WINDOW) {
            LogUtilLog4J.init(LogLevel.TRACE);
            new SwingLogFrame("LOG").setVisible(true);
        }

        LOG = LogManager.getLogger("KitchenSink");
    }

    @Serial
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        LOG.info("starting up ...");
        SwingUtilities.invokeLater(() -> {
            SwingUtil.setNativeLookAndFeel();
            KitchenSink instance = new KitchenSink();
            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }

    private static void addColorSheet(Workbook wb) {
        LOG.info("Adding color sheet");

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

        sheet.autoSizeColumn(0);
    }

    private static void addTextColorSheet(Workbook wb) {
        LOG.info("Adding text color sheet");

        Sheet sheet = wb.createSheet("text colors");

        Row row = sheet.getRow(0);
        row.getCell(0).set("Color");
        row.getCell(1).set("Code");
        row.getCell(2).set("darker");
        row.getCell(3).set("brighter");

        sheet.splitAt(1, 0);

        CellStyle csDefault = wb.getDefaultCellStyle();
        Font fontDefault = csDefault.getFont();
        FontUtil<?> fontUtil = FontUtil.getInstance();
        for (Entry<String, Color> e : Color.palette().entrySet()) {
            String name = "font" + e.getKey();

            CellStyle cs = wb.getCellStyle(name);
            cs.setFont(fontUtil.deriveFont(fontDefault, FontDef.color(e.getValue())));

            CellStyle csDark = wb.getCellStyle(name + "Dark");
            csDark.setFont(fontUtil.deriveFont(fontDefault, FontDef.color(e.getValue().darker())));

            CellStyle csBright = wb.getCellStyle(name + "Bright");
            csBright.setFont(fontUtil.deriveFont(fontDefault, FontDef.color(e.getValue().brighter())));

            row = sheet.getRow(sheet.getRowCount());
            row.getCell(0).set(e.getKey()).setCellStyle(cs);
            row.getCell(1).set(e.getValue().toString()).setCellStyle(cs);
            row.getCell(2).set("darker").setCellStyle(csDark);
            row.getCell(3).set("brighter").setCellStyle(csBright);
        }

        sheet.autoSizeColumn(0);
    }

    private static Workbook createWorkbook(WorkbookFactory<?> factory) {
        Workbook wb = factory.create();

        addColorSheet(wb);
        addTextColorSheet(wb);

        return wb;
    }

    private final Workbook wb = createWorkbook(GenericWorkbookFactory.instance());

    private KitchenSink() {
        super("Méja Kitchensink demo");
        init();
    }

    @Override
    public void dispose() {
        try {
            wb.close();
        } catch (IOException e) {
            LOG.error("exception occurred while closing workbook", e);
        }
        super.dispose();
    }

    private void init() {
        setSize(800, 600);
        final SwingWorkbookView view = new SwingWorkbookView();
        setContentPane(view);

        view.setWorkbook(wb);
    }
}
