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
import javax.swing.WindowConstants;
import java.io.IOException;
import java.io.Serial;
import java.net.URISyntaxException;

/**
 * Kitchensink example.
 */
public final class ShowTestXlsx extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger(ShowTestXlsx.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                SwingUtil.setNativeLookAndFeel();
                ShowTestXlsx instance = new ShowTestXlsx();
                instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                instance.setVisible(true);
            } catch (RuntimeException e) {
                LOG.error("exception occurred while initializing the application", e);
                throw e;
            } catch (Exception e) {
                LOG.error("exception occurred while initializing the application", e);
                throw new RuntimeException(e);
            }
        });
    }

    private final Workbook wb;

    private ShowTestXlsx() throws URISyntaxException, IOException {
        super("Méja Kitchensink demo");
        wb =  MejaHelper.openWorkbook(LangUtil.getResourceURL(ShowTestXlsx.class, "test.xlsx").toURI());
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
