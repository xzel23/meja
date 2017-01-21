package com.dua3.meja.samples;

import java.awt.HeadlessException;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFrame;

import com.dua3.meja.model.CellStyle;
import com.dua3.meja.model.Color;
import com.dua3.meja.model.FillPattern;
import com.dua3.meja.model.Row;
import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.ui.swing.SwingWorkbookView;

/**
 *
 * @author a5xysq1
 */
@SuppressWarnings("serial")
public class KitchenSink extends JFrame {

    public static void main(String[] args) {
        KitchenSink instance = new KitchenSink();
        instance.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        instance.setVisible(true);
    }

	private Workbook wb;

    public KitchenSink() throws HeadlessException {
        super("Méja Kitchensink demo");
        init();
    }

    private void init() {
        setSize(800, 600);
        final SwingWorkbookView view = new SwingWorkbookView();
        this.setContentPane(view);

        wb = createWorkbook(GenericWorkbookFactory.instance());
        
        view.setWorkbook(wb);
    }

    private static Workbook createWorkbook(WorkbookFactory factory) {
        Workbook wb = factory.create();

        addColorSheet(wb);

        return wb;
    }

    private static void addColorSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("colors");

        Row row = sheet.getRow(0);
        row.getCell(0).set("Color");
        row.getCell(1).set("Code");
        row.getCell(2).set("darker");
        row.getCell(3).set("brighter");

        sheet.splitAt(1, 0);

        for (Map.Entry<String, Color> e: Color.palette().entrySet()) {
            String name = e.getKey();

            CellStyle cs = wb.getCellStyle(name);
            cs.setFillFgColor(e.getValue());
            cs.setFillPattern(FillPattern.SOLID);

            CellStyle csDark = wb.getCellStyle(name+"Dark");
            csDark.setFillFgColor(e.getValue().darker());
            csDark.setFillPattern(FillPattern.SOLID);

            CellStyle csBright = wb.getCellStyle(name+"Bright");
            csBright.setFillFgColor(e.getValue().brighter());
            csBright.setFillPattern(FillPattern.SOLID);

            row = sheet.getRow(sheet.getRowCount());
            row.getCell(0).set(name).setCellStyle(cs);
            row.getCell(1).set(e.getValue().toString()).setCellStyle(cs);
            row.getCell(2).set("darker").setCellStyle(csDark);
            row.getCell(3).set("brighter").setCellStyle(csBright);
        }
    }
    
    @Override
    public void dispose() {
    	try {
			wb.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
    	super.dispose();
    }
}
