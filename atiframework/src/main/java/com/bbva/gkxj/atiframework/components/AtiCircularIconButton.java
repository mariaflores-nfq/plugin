package com.bbva.gkxj.atiframework.components;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

/**
 * Clase que representa un Icono circular de fondo relleno con un icono personalizado en el centro.
 */
public class AtiCircularIconButton extends JButton {

    /** Tamaño del botón. */
    private static final int BUTTON_SIZE = 20;

    /** Color de fondo del Botón. */
    private final Color baseColor = SchedulerTheme.BBVA_BLUE;


    /**
     * Construye un botón circular dado un Icono.
     * @param icon Icono interior
     */
    public AtiCircularIconButton(Icon icon) {
        super();
        setIcon(icon);

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(0, 0, 0, 0));
        setBorder(BorderFactory.createEmptyBorder());

        setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));

    }

    /**
     * Construye un botón circular dado un Icono.
     * @param icon Icono interior
     */
    public AtiCircularIconButton(Icon icon, ActionListener action) {
        super();
        setIcon(icon);

        if(action != null)
            addActionListener(action);

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(0, 0, 0, 0));
        setBorder(BorderFactory.createEmptyBorder());

        setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));

    }

    /**
     * Pinta el componente en la interfaz.
     * @param g Gráficos
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2.setColor(baseColor.darker());
        } else if (getModel().isRollover()) {
            g2.setColor(baseColor.brighter());
        } else {
            g2.setColor(baseColor);
        }

        g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
        g2.dispose();

        super.paintComponent(g);
    }

    /**
     * Establece su tamaño
     * @return Dimensión del botón.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BUTTON_SIZE, BUTTON_SIZE);
    }
}