package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Componente de tipo interruptor \("toggle switch"\) personalizado para Swing.
 *
 * Representa un interruptor binario \[activado/desactivado\] que notifica los
 * cambios de estado mediante un {@link Consumer} de {@link Boolean}.
 * El componente es clicable cuando está habilitado y muestra un estilo
 * visual consistente con la paleta de colores de IntelliJ \({@link JBColor}\).
 *
 */
public class ToggleSwitch extends JComponent {

    /** Anchura del interruptor en píxeles. */
    private final int width = 36;

    /** Altura del interruptor en píxeles. */
    private final int height = 20;

    /** Estado actual del interruptor: {@code true} si está activado. */
    public boolean selected = false;

    /**
     * Listener opcional que se invoca cada vez que cambia el estado del interruptor.
     * El valor {@link Boolean} indica el nuevo estado después del cambio.
     */
    private Consumer<Boolean> onStateChanged;

    /**
     * Crea un nuevo {@code ToggleSwitch} con el tamaño y el cursor
     * predeterminados, y registra el manejador de eventos de ratón para
     * alternar el estado al hacer clic.
     */
    public ToggleSwitch() {
        setPreferredSize(new Dimension(width, height));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isEnabled()) {
                    selected = !selected;
                    repaint();
                    if (onStateChanged != null) {
                        onStateChanged.accept(selected);
                    }
                }
            }
        });
    }

    /**
     * Registra un listener que se ejecutará cada vez que cambie el estado
     * del interruptor.
     *
     * @param listener consumidor que recibe el nuevo estado del interruptor
     *                 \({@code true} si se activa, {@code false} si se desactiva\)
     */
    public void setOnStateChanged(Consumer<Boolean> listener) {
        this.onStateChanged = listener;
    }

    /**
     * Establece si el componente está habilitado o deshabilitado.
     *
     * @param enabled {@code true} para habilitar el componente, {@code false} para deshabilitarlo
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        repaint();
    }

    /**
     * Pinta el interruptor con bordes redondeados y un círculo deslizante
     * que indica el estado actual.
     * Utiliza colores diferentes para los estados activo, inactivo y deshabilitado,
     * manteniendo el antialiasing para un mejor resultado visual.
     *
     * @param g contexto gráfico proporcionado por el sistema de dibujo de Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color activeColor = new JBColor(new Color(0x4B6EAF), new Color(0x4B6EAF));
        Color inactiveColor = new JBColor(new Color(0xB0B0B0), new Color(0xB0B0B0));
        Color circleColor = JBColor.WHITE;

        if (!isEnabled()) {
            activeColor = new JBColor(new Color(0x9CAECF), new Color(0x9CAECF));
            inactiveColor = new JBColor(new Color(0xE0E0E0), new Color(0xE0E0E0));
            circleColor = new JBColor(new Color(0xF2F2F2), new Color(0xF2F2F2));
        }

        if (selected) {
            g2.setColor(activeColor);
        } else {
            g2.setColor(inactiveColor);
        }
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, height, height));

        g2.setColor(circleColor);
        int circleD = height - 4;
        int x = selected ? (width - circleD - 2) : 2;
        g2.fillOval(x, 2, circleD, circleD);
    }
}