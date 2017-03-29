package com.dua3.meja.ui.swing;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dua3.meja.util.Option;

public class SettingsDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  private final JPanel settingsPanel;
  private Map<Option<?>, Object> result = null;

  SettingsDialog(Frame parent, String title, String text, List<Option<?>> options) {
    super(parent, title, true);
    setLayout(new BorderLayout());

    add(new JLabel(text), BorderLayout.NORTH);

    List<JComboBox<?>> inputs = new ArrayList<>(options.size());

    settingsPanel = new JPanel();
    settingsPanel.setLayout(new GridLayout(options.size(), 2));
    for (Option<?> option: options) {
      settingsPanel.add(new JLabel(option.getName()));
      JComboBox<?> cb = new JComboBox<>(option.getChoices());
      cb.setSelectedItem(option.getDefaultChoice());
      inputs.add(cb);
      settingsPanel.add(cb);
    }
    add(settingsPanel, BorderLayout.CENTER);

    add(new JButton(MejaSwingHelper.createAction("OK", () -> {
      result = new HashMap<>();
      for(int i=0; i<options.size(); i++) {
        result.put(options.get(i), inputs.get(i).getSelectedItem());
      }
      this.dispose();
    })), BorderLayout.SOUTH);
    pack();

    setLocationRelativeTo(parent);
  }

  SettingsDialog(Frame parent, String title, String text, Option<?>... options) {
    this(parent, title, text, Arrays.asList(options));
  }

  public Map<Option<?>, Object> getResult() {
    return result;
  }
}
