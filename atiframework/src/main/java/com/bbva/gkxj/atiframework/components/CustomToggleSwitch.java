package com.bbva.gkxj.atiframework.components;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Toggle Switch personalizado
 */
public class CustomToggleSwitch extends JComponent {
    private boolean selected = false;
    private final int width = 34;
    private final int height = 18;

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public CustomToggleSwitch() {
        setPreferredSize(new Dimension(width, height));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                selected = !selected;
                repaint();
                fireStateChanged();
            }
        });
    }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (selected) {
            g2.setColor(SchedulerTheme.BBVA_NAVY);
        } else {
            g2.setColor(new Color(180, 180, 180));
        }
        g2.fillRoundRect(0, 0, width, height, height, height);

        g2.setColor(Color.WHITE);
        int thumbSize = height - 4;
        int thumbX = selected ? width - thumbSize - 2 : 2;
        g2.fillOval(thumbX, 2, thumbSize, thumbSize);

        g2.dispose();
    }
    private void fireStateChanged() {
        if (changeListeners.isEmpty()) return;
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(event);
        }
    }
    public void addChangeListener(ChangeListener listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }}
