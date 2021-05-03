package com.dua3.meja.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.*;

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

import com.dua3.utility.options.Option;
import com.dua3.utility.options.ChoiceOption;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.swing.SwingUtil;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {

    private final JPanel settingsPanel;
    private Arguments result = Arguments.empty();

    @SuppressWarnings({"rawtypes", "unchecked"})
    SettingsDialog(Component parent, String title, String text, Collection<Option<?>> options) {
        super((JFrame) SwingUtilities.getRoot(parent), title, true);
        setLayout(new BorderLayout());

        add(new JLabel(text), BorderLayout.NORTH);

        List<JComponent> inputs = new ArrayList<>(options.size());

        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(options.size(), 2));
        for (Option<?> option : options) {
            settingsPanel.add(new JLabel(option.displayName()));
            if (option instanceof ChoiceOption) {
                ChoiceOption co = (ChoiceOption) option;
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
            int i=0;
            for (Option option: options) {
                JComponent component = inputs.get(i++);
                Object value;
                if (option instanceof ChoiceOption) {
                    value = ((JComboBox) component).getSelectedItem();
                } else {
                    value = ((JTextComponent) component).getText();
                }
                entries.add(Arguments.createEntry(option, value));
            }
            result = Arguments.of(entries.toArray(Arguments.Entry[]::new));
            this.dispose();
        })), BorderLayout.SOUTH);
        pack();

        setLocationRelativeTo(parent);
    }

    SettingsDialog(JFrame parent, String title, String text, Option<?>... options) {
        this(parent, title, text, Arrays.asList(options));
    }

    public Arguments getResult() {
        return result;
    }
}
