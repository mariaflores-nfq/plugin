package com.bbva.gkxj.atiframework.components;

import javax.swing.*;
import java.awt.*;

public class AtiLabeledComponent extends JPanel {

    public AtiLabeledComponent(String labelText, JComponent comp) {
        super(new BorderLayout(0, 4));
        setOpaque(false);
        JLabel l = new JLabel(labelText);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        add(l, BorderLayout.NORTH);
        add(comp, BorderLayout.CENTER);
    }
}

