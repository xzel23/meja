package com.dua3.meja.ui.swing;

import com.dua3.meja.model.Cell;
import com.dua3.meja.model.SearchOptions;
import com.dua3.meja.model.SearchSettings;
import com.dua3.utility.swing.SwingUtil;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.Optional;

final class SwingSearchDialog extends JDialog {

    private final SwingSheetView swingSheetView;
    private final JTextField jtfText = new JTextField(40);
    private final JCheckBox jcbIgnoreCase = new JCheckBox("ignore case", true);
    private final JCheckBox jcbMatchCompleteText = new JCheckBox("match complete text", false);

    SwingSearchDialog(SwingSheetView swingSheetView) {
        this.swingSheetView = swingSheetView;
        init();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            jtfText.requestFocusInWindow();
            jtfText.selectAll();
        }
    }

    private void init() {
        setTitle("Search");
        setModal(true);
        setResizable(false);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        getRootPane().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // text label
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new JLabel("Text:"), c);

        // text input
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 4;
        c.gridheight = 1;
        add(jtfText, c);

        // options
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new JLabel("Options:"), c);

        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(jcbIgnoreCase, c);

        c.gridx = 3;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(jcbMatchCompleteText, c);

        // submit button
        c.gridx = 4;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        JButton submitButton = new JButton(SwingUtil.createAction("Search", this::doSearch));
        add(submitButton, c);

        // close button
        c.gridx = 5;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        add(new JButton(SwingUtil.createAction("Close", e -> setVisible(false))), c);

        // Enter starts search
        SwingUtilities.getRootPane(submitButton).setDefaultButton(submitButton);

        // Escape closes dialog
        final Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        };

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "ESCAPE_KEY");
        rootPane.getActionMap().put("ESCAPE_KEY", escapeAction);

        // pack layout
        pack();
    }

    void doSearch() {
        swingSheetView.getSheet().ifPresent(sheet -> {
            EnumSet<SearchOptions> options = EnumSet.of(SearchOptions.SEARCH_FROM_CURRENT);

            if (jcbIgnoreCase.isSelected()) {
                options.add(SearchOptions.IGNORE_CASE);
            }

            if (jcbMatchCompleteText.isSelected()) {
                options.add(SearchOptions.MATCH_COMPLETE_TEXT);
            }

            Optional<Cell> oc = sheet.find(getText(), SearchSettings.of(options));
            if (oc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Text was not found.");
            } else {
                Cell cell = oc.get();
                swingSheetView.setCurrentCell(cell.getRowNumber(), cell.getColumnNumber());
            }
        });
    }

    String getText() {
        return jtfText.getText();
    }

}
