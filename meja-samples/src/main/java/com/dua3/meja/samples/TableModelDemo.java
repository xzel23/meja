package com.dua3.meja.samples;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.ui.swing.MejaSwingHelper;
import com.dua3.meja.util.TableOptions;
import com.dua3.utility.swing.SwingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.io.IOException;
import java.io.Serial;
import java.time.LocalDateTime;

public class TableModelDemo extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger(TableModelDemo.class);

    public static void main(String[] args) throws IOException {
        try (Workbook wb = GenericWorkbookFactory.instance().create()) {
            Sheet sheet = wb.createSheet("TableModelDemo");
            sheet.createRow("Nr.", "Time");

            SwingUtilities.invokeLater(() -> {
                SwingUtil.setNativeLookAndFeel();
                TableModelDemo instance = new TableModelDemo(sheet);
                instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                instance.setVisible(true);
            });
        }
    }

    private TableModelDemo(Sheet sheet) {
        super("TableModel demo");
        setSize(800, 600);

        this.sheet = sheet;

        JTable table = new JTable();
        setContentPane(new JScrollPane(table));

        table.setModel(MejaSwingHelper.getTableModel(sheet, TableOptions.FIRST_ROW_IS_HEADER, TableOptions.EDITABLE));

        new Thread(() -> {
            for (int i = 1; i < 50; i++) {
                sheet.createRow(i, LocalDateTime.now());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private final Sheet sheet;
}
