package com.dua3.meja.samples;

import com.dua3.meja.model.Sheet;
import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import com.dua3.meja.ui.swing.MejaSwingHelper;
import com.dua3.meja.util.TableOptions;
import com.dua3.utility.swing.SwingUtil;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.Serial;
import java.time.LocalDateTime;

/**
 * Demonstrates the usage of a table model in a Swing application.
 * This class extends {@link JFrame} and creates a GUI displaying a table
 * backed by a data model derived from a {@link Sheet} object. It integrates
 * with a workbook and dynamically updates the table contents.
 * <p>
 * Features:
 * <ul>
 *   <li>Uses a {@link JTable} to display data.</li>
 *   <li>Supports dynamic row creation in the table.</li>
 *   <li>Uses threading to simulate real-time data population.</li>
 *   <li>Applies table options for header and editability configurations.</li>
 *   <li>Uses the native look-and-feel for better UI integration.</li>
 * </ul>
 * <p>
 * How it works:
 * <ol>
 *   <li>An instance of {@link Workbook} is created and a {@link Sheet}
 *   with initial headers is added.</li>
 *   <li>A {@link JTable} is initialized and associated with a model
 *   created from the {@link Sheet} using {@link MejaSwingHelper}.</li>
 *   <li>Rows are dynamically added to the sheet in a separate thread,
 *   updating the table view in real time.</li>
 * </ol>
 */
public final class TableModelDemo extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The entry point of the application that demonstrates a table model in a Swing GUI.
     * This method initializes a workbook, creates a sheet, populates it with headers,
     * and launches the {@code TableModelDemo} JFrame to display and interact with the data.
     *
     * @param args Command-line arguments passed to the program (unused).
     * @throws IOException If there is an I/O issue during workbook creation or usage.
     */
    public static void main(String[] args) throws IOException {
        try (Workbook wb = GenericWorkbookFactory.instance().create()) {
            Sheet sheet = wb.createSheet("TableModelDemo");
            sheet.createRow("Nr.", "Time");

            SwingUtilities.invokeLater(() -> {
                SwingUtil.setNativeLookAndFeel();
                TableModelDemo instance = new TableModelDemo(sheet);
                instance.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                instance.setVisible(true);
            });
        }
    }

    private TableModelDemo(Sheet sheet) {
        super("TableModel demo");
        setSize(800, 600);

        JTable table = new JTable();
        setContentPane(new JScrollPane(table));

        table.setModel(MejaSwingHelper.getTableModel(sheet, TableOptions.FIRST_ROW_IS_HEADER, TableOptions.EDITABLE));

        // Use virtual thread for more efficient I/O operations
        Thread.startVirtualThread(() -> {
            for (int i = 1; i < 50; i++) {
                sheet.createRow(i, LocalDateTime.now());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

}
