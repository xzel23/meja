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
import com.dua3.utility.swing.SwingUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;
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
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger(KitchenSink.class);

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingUtil.setNativeLookAndFeel();
            KitchenSink instance = new KitchenSink();
            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }

    private Workbook wb;

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
        this.setContentPane(view);

        wb = createWorkbook(GenericWorkbookFactory.instance());

        view.setWorkbook(wb);
    }
}
