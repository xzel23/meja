package com.dua3.meja.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.dua3.utility.options.Option;
import com.dua3.utility.options.OptionSet;
import com.dua3.utility.options.OptionValues;
import com.dua3.utility.options.Option.ChoiceOption;
import com.dua3.utility.swing.SwingUtil;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {

    private final JPanel settingsPanel;
    private OptionValues result = OptionValues.empty();

    @SuppressWarnings({ "rawtypes" })
    SettingsDialog(Component parent, String title, String text, OptionSet options) {
        super((JFrame) SwingUtilities.getRoot(parent), title, true);
        setLayout(new BorderLayout());

        add(new JLabel(text), BorderLayout.NORTH);

        List<JComponent> inputs = new ArrayList<>(options.size());

        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(options.size(), 2));
        for (Option<?> option : options) {
            settingsPanel.add(new JLabel(option.getName()));
            if (option instanceof Option.ChoiceOption) {
                JComboBox<Supplier<?>> cb = new JComboBox<>(new Vector<Supplier<?>>(((Option.ChoiceOption)option).getChoices()));
                cb.setSelectedItem(option.getDefault());
                inputs.add(cb);
                settingsPanel.add(cb);
            } else {
                JTextField tf = new JTextField(String.valueOf(option.getDefault()));
                inputs.add(tf);
                settingsPanel.add(tf);
            }
        }
        add(settingsPanel, BorderLayout.CENTER);

        add(new JButton(SwingUtil.createAction("OK", () -> {
            result = new OptionValues();
            int i=0;
            for (var option: options) {
                JComponent component = inputs.get(i++);
                if (option instanceof ChoiceOption) {                    
                    result.put(option, (Supplier) ((JComboBox) component).getSelectedItem());
                } else {
                    result.put(option, () -> ((JTextField) component).getText());
                }
            }
            this.dispose();
        })), BorderLayout.SOUTH);
        pack();

        setLocationRelativeTo(parent);
    }

    SettingsDialog(JFrame parent, String title, String text, Option<?>... options) {
        this(parent, title, text, new OptionSet(options));
    }

    public OptionValues getResult() {
        return result;
    }
}
