package com.bbva.gkxj.atiframework.components;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa un JSpinner personalizado
 */
public class AtiJSpinner extends JSpinner {

    /** Evita que commitEdit() provoque un bucle al modificar el documento */
    private boolean isUpdating = false;

    /**
     * Constructor principal que recibe un modelo personalizado
     * @param model Modelo de datos del JSpinner
     */
    public AtiJSpinner(SpinnerModel model) {
        super(model);
        styleSpinner(this);
    }

    /**
     * Constructor por defecto, reutiliza el constructor principal con datos por defecto
     */
    public AtiJSpinner() {
        this(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    }

    /**
     * Aplica un estilo visual personalizado y comportamiento interactivo al JSpinner.
     *
     * - Alinea el texto a la izquierda.
     * - Configura un borde reactivo que se resalta en azul cuando el campo recibe el foco.
     * - Oculta las flechas de incremento/decremento por defecto y solo las hace visibles con el cursor encima.
     *
     * @param spinner El componente JSpinner al que se aplicará el estilo.
     */
    private static void styleSpinner(JSpinner spinner) {

        JComponent editor = spinner.getEditor();
        JFormattedTextField tf = null;
        if (editor instanceof DefaultEditor) {
            tf = ((DefaultEditor) editor).getTextField();
            tf.setHorizontalAlignment(SwingConstants.LEFT);
            tf.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        }

        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BORDER_SOFT, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        Border focus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BBVA_NAVY, 2),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)
        );

        spinner.setBorder(normal);

        if (tf != null) {
            tf.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    spinner.setBorder(focus);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    spinner.setBorder(normal);
                }
            });
        }

        List<Component> buttons = new ArrayList<>();
        for (Component c : spinner.getComponents()) {
            if (c instanceof JButton) {
                buttons.add(c);
                c.setVisible(false);
            }
        }

        MouseAdapter hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                for (Component b : buttons){
                    b.setVisible(true);
                    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point pointInSpinner = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), spinner);
                if (!spinner.contains(pointInSpinner)) {
                    for (Component b : buttons) b.setVisible(false);
                }
            }
        };

        spinner.addMouseListener(hoverListener);
        if (tf != null)
            tf.addMouseListener(hoverListener);
        for (Component b : buttons)
            b.addMouseListener(hoverListener);
    }

    /**
     * Registra un DocumentListener sobre el campo de texto interno para disparar
     * cambios en tiempo real. Usa el flag {@code isUpdating} para evitar el bucle
     * que provocaría que commitEdit() modifique el documento y vuelva a disparar el listener.
     *
     * <p>Nota: {@code changedUpdate} no se implementa porque en un {@code PlainDocument}
     * (el que usa JTextField) ese evento solo se dispara ante cambios de atributo (negrita,
     * color…), nunca ante cambios de texto.</p>
     */
    private void addTextListenerToSpinner() {
        JComponent editor = getEditor();
        if (!(editor instanceof DefaultEditor)) return;

        JTextField textField = ((DefaultEditor) editor).getTextField();
        textField.getDocument().addDocumentListener(new DocumentListener() {

            private void forceCommit() {
                if (isUpdating) return;
                isUpdating = true;
                try {
                    commitEdit();
                } catch (java.text.ParseException ignored) {
                    // Valor incompleto mientras el usuario escribe: se ignora
                } finally {
                    isUpdating = false;
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                forceCommit();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                forceCommit();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                forceCommit();
            }
        });
    }
}
