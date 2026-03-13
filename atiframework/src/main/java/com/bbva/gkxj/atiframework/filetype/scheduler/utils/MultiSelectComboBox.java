package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 * JComboBox editado para ser Múltiple
 */
public class MultiSelectComboBox extends JComboBox<String> {

    private final List<JCheckBox> checkBoxes = new ArrayList<>();
    private final String defaultText;
    private JBPopup myPopup;

    public MultiSelectComboBox(String[] options, String defaultText) {
        this.defaultText = defaultText;

        setEditable(false);
        setModel(new DefaultComboBoxModel<>(new String[]{defaultText}));
        for (String option : options) {
            JCheckBox cb = new JCheckBox(option);
            cb.setFocusPainted(false);
            // Listener: Al marcar un check, actualizamos el texto del combo padre
            cb.addActionListener(e -> updateComboBoxLabel());
            checkBoxes.add(cb);
        }
    }

    @Override
    public void setPopupVisible(boolean visiblePopup) {
        if (visiblePopup) {
            if (myPopup == null || !myPopup.isVisible()) {
                showCustomPopup();
            }
        }
        // super.setPopupVisible(v) mostraría el popup doble.
    }

    private void showCustomPopup() {
        JPanel popupContent = new JPanel();
        popupContent.setLayout(new BoxLayout(popupContent, BoxLayout.Y_AXIS));
        popupContent.setBackground(SchedulerTheme.BG_CARD);
        popupContent.setBorder(JBUI.Borders.empty(5));

        for (JCheckBox cb : checkBoxes) {
            popupContent.add(cb);
        }

        JBScrollPane scrollPane = new JBScrollPane(popupContent);
        scrollPane.setBorder(null);
        int contentHeight = Math.min(checkBoxes.size() * 26 + 20, 200);
        scrollPane.setPreferredSize(new Dimension(getWidth(), contentHeight));

        myPopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(scrollPane, null)
                .setRequestFocus(true)
                .setFocusable(true)
                .setResizable(false)
                .createPopup();

        myPopup.showUnderneathOf(this);
    }

    private void updateComboBoxLabel() {
        // Recogemos los seleccionados
        String selected = checkBoxes.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .collect(Collectors.joining(", "));

        if (selected.isEmpty()) {
            selected = defaultText;
        }

        // Actualizamos el MODELO del combo real.
        setModel(new DefaultComboBoxModel<>(new String[]{selected}));
    }

    public void setSelectedItems(List<String> items) {
        checkBoxes.forEach(cb -> cb.setSelected(false));
        if (items != null) {
            for (String item : items) {
                for (JCheckBox cb : checkBoxes) {
                    if (cb.getText().equalsIgnoreCase(item)) {
                        cb.setSelected(true);
                    }
                }
            }
        }
        updateComboBoxLabel();
    }

    public List<String> getSelectedItems() {
        return checkBoxes.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .collect(Collectors.toList());
    }
}