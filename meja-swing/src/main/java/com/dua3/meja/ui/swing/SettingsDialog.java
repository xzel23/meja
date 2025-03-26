package com.dua3.meja.ui.swing;

import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ChoiceOption;
import com.dua3.utility.options.Option;
import com.dua3.utility.swing.SwingUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * A dialog box for setting options.
 * This dialog is a subclass of JDialog and provides a user interface for
 * configuring settings using various options.
 */
public class SettingsDialog extends JDialog {

    private Arguments result = Arguments.empty();

    @SuppressWarnings({"rawtypes", "unchecked"})
    SettingsDialog(Component parent, String title, String text, Collection<? extends Option<?>> options) {
        super((JFrame) SwingUtilities.getRoot(parent), title, true);
        setLayout(new BorderLayout());

        add(new JLabel(text), BorderLayout.PAGE_START);

        List<JComponent> inputs = new ArrayList<>(options.size());

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(options.size(), 2));
        for (Option<?> option : options) {
            settingsPanel.add(new JLabel(option.displayName()));
            if (option instanceof ChoiceOption co) {
                JComboBox<ChoiceOption.Choice<?>> cb = new JComboBox<>(new Vector<>(co.choices()));
                cb.setSelectedItem(co.choice(co.getDefault()));
                inputs.add(cb);
                settingsPanel.add(cb);
            } else {
                JTextField tf = new JTextField();
                inputs.add(tf);
                settingsPanel.add(tf);
            }
        }
        add(settingsPanel, BorderLayout.CENTER);

        add(new JButton(SwingUtil.createAction("OK", () -> {
            Deque<Arguments.Entry<?>> entries = new LinkedList<>();
            int i = 0;
            for (Option option : options) {
                JComponent component = inputs.get(i++);
                Object value;
                if (option instanceof ChoiceOption) {
                    value = ((JComboBox) component).getSelectedItem();
                } else {
                    value = ((JTextComponent) component).getText();
                }
                if (value != null) {
                    entries.add(Arguments.createEntry(option, value));
                } else {
                    entries.add(Arguments.createEntry(option));
                }
            }
            result = Arguments.of(entries.toArray(Arguments.Entry[]::new));
            dispose();
        })), BorderLayout.PAGE_END);
        pack();

        setLocationRelativeTo(parent);
    }

    /**
     * Constructs a new {@code SettingsDialog} with the given parameters and initializes
     * it with the specified options.
     *
     * @param parent the parent frame for this dialog
     * @param title the title of the dialog
     * @param text the text or message to display at the top of the dialog
     * @param options the array of {@link Option} objects representing the configurable settings
     */
    SettingsDialog(JFrame parent, String title, String text, Option<?>... options) {
        this(parent, title, text, List.of(options));
    }

    /**
     * Retrieves the result of the user's input from the options dialog.
     *
     * @return an {@link Arguments} object containing the values input by the user
     *         for the configured options in the dialog.
     */
    public Arguments getResult() {
        return result;
    }
}
