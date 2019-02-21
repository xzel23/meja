package com.dua3.meja.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.dua3.utility.options.Option;
import com.dua3.utility.options.Options;
import com.dua3.utility.swing.SwingUtil;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {

    private final JPanel settingsPanel;
    private Options result = Options.empty();

    @SuppressWarnings({ "rawtypes" })
    SettingsDialog(Component parent, String title, String text, List<Option<?>> options) {
        super((JFrame) SwingUtilities.getRoot(parent), title, true);
        setLayout(new BorderLayout());

        add(new JLabel(text), BorderLayout.NORTH);

        List<JComponent> inputs = new ArrayList<>(options.size());

        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(options.size(), 2));
        for (Option<?> option : options) {
            settingsPanel.add(new JLabel(option.getName()));
            if (option instanceof Option.ChoiceOption) {
                JComboBox<Supplier<?>> cb = new JComboBox<>(new Vector<Supplier<?>>((Option.ChoiceOption)option.getChoices()));
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
            result = new Options();
            for (int i = 0; i < options.size(); i++) {
                result.put((Option) options.get(i), (Supplier) inputs.get(i).getSelectedItem());
            }
            this.dispose();
        })), BorderLayout.SOUTH);
        pack();

        setLocationRelativeTo(parent);
    }

    SettingsDialog(Frame parent, String title, String text, Option<?>... options) {
        this(parent, title, text, Arrays.asList(options));
    }

    public Options getResult() {
        return result;
    }
}
