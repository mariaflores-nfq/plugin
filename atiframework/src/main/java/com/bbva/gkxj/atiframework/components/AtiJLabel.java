package com.bbva.gkxj.atiframework.components;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;

import javax.swing.*;
import java.awt.*;

public class AtiJLabel extends JLabel {
    public AtiJLabel(String text) {
        super(text);
        this.setFont(new Font("Lato", Font.BOLD, 14));
        this.setForeground(SchedulerTheme.TEXT_MAIN);
    }


    public JPanel createLabeledField(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }
}