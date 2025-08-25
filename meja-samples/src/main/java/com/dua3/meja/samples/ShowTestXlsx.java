package com.dua3.meja.samples;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.ui.swing.SwingWorkbookView;
import com.dua3.meja.util.MejaHelper;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.swing.SwingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.Serial;
import java.net.URISyntaxException;

/**
 * The {@code ShowTestXlsx} class represents a demonstration GUI application
 * that uses a Swing-based interface to display and interact with
 * an Excel (XLSX) workbook.
 *
 * <p>This class extends {@link JFrame} to provide a window-based graphical
 * application. It initializes and displays the workbook data within a custom
 * Swing view component. The workbook is loaded from a resource file named
 * {@code test.xlsx} and is managed using the Apache POI library.
 *
 * <p>The application also sets a native look and feel, ensures proper resource
 * cleanup by closing the workbook upon disposal, and handles errors during
 * initialization and termination.
 * <p>
 * <strong>Functional Overview:</strong>
 * <ul>
 *   <li>Loads an XLSX workbook from the {@code test.xlsx} resource.</li>
 *   <li>Uses the {@code SwingWorkbookView} to render and display the workbook
 *       within the application window.</li>
 *   <li>Provides proper handling of window disposal and workbook cleanup.</li>
 *   <li>Logs errors that occur during initialization or cleanup.</li>
 * </ul>
 */
public final class ShowTestXlsx extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger(ShowTestXlsx.class);

    /**
     * The main entry point of the application. This method initializes
     * the ShowTestXlsx demo application by setting the native look and feel,
     * creating an instance of the main application window, and making it visible.
     * It also handles any exceptions that may occur during the initialization process.
     *
     * @param args command-line arguments (not used in this implementation)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                SwingUtil.setNativeLookAndFeel();
                ShowTestXlsx instance = new ShowTestXlsx();
                instance.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                instance.setVisible(true);
            } catch (RuntimeException e) {
                LOG.error("exception occurred while initializing the application", e);
                throw e;
            } catch (Exception e) {
                LOG.error("exception occurred while initializing the application", e);
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * The {@link Workbook} instance.
     */
    private final Workbook wb;

    private ShowTestXlsx() throws URISyntaxException, IOException {
        super("Méja Kitchensink demo");
        wb = MejaHelper.openWorkbook(LangUtil.getResourceURL(ShowTestXlsx.class, "test.xlsx").toURI());
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
