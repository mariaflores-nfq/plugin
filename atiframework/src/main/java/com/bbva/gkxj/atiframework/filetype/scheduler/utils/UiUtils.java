package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de utilidad que centraliza recursos visuales y métodos auxiliares para la interfaz de usuario.
 */
public class UiUtils {

    /** Días de la semana para selectores. */
    public static final String [] DAYS_OF_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"};

    /** Tipo de periodicidad del Trigger. */
    public static final String[] SCHEDULE_TYPES = {"Monthly", "Weekly", "Daily"};

    /** Nombres de los meses del año para selectores. */
    public static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    /** Días del mes. */
    public static final String[] DAYS_OF_THE_MONTHS = {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
            "31"
    };

    /** Tipos de eventos del mes.*/
    public static final String[] MONTH_EVENTS = {
            "First week", "Second week", "Third week", "Fourth week", "Fifth week"
    };

    /** Tipos de nivel de issue. */
    public static final String[] ISSUE_LEVELS = {"No level","Info","Warning","Critical"};

    /** Mensaje para selector de días de la semana vacío.*/
    public static final String EMPTY_WEEK_DAYS_MSG = "Select week days";

    /** Mensaje para selector de meses vacío.*/
    public static final String EMPTY_MONTHS_MSG = "Select months";

    /** Mensaje para selector de días del mes vacío.*/
    public static final String EMPTY_DAYS_MONTHS_MSG = "Select days of month";

    /** Mensaje para selector de días de la semana vacío.*/
    public static final String EMPTY_WEEK_DAYS_AFFECTED_MSG = "Select week days affected";

    /** Tipo de archivo File Watcher.*/
    public static final String TYPE_FILE_WATCHER = "File Watcher";

    /** Tipo de archivo File Nova Transfer Watcher.*/
    public static final String TYPE_NOVA = "Nova Transfer Watcher";

    /** Tipo de archivo consulta Mongo.*/
    public static final String TYPE_MONGO = "Mongo Query";

    /** Tipo de archivo consulta SQL.*/
    public static final String TYPE_SQL = "SQL Query";

    /** Constante para columna. */
    public static final int ACTION_COLUMN_INDEX = 3;

    /** Constante para columna. */
    public static final int FILTER_ACTION_COLUMN_INDEX = 2;

    /** Constante para columna. */
    public static final int ACTION_COL = 2;

    /** Tipo de BBDD. */
    public static final String[] DB_SOURCE_OPTIONS = {"Config", "Data", "Oracle"};


    /**
     * Actualiza el valor de un JComboBox al índice relativo al valor dado
     *
     * @param comboBox JComboBox a actualizar
     * @param valueToSet valor del índice a poner
     */
    public static void setComboBoxValueIgnoreCase(JComboBox<String> comboBox, String valueToSet) {
        if (valueToSet == null) return;

        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item.equalsIgnoreCase(valueToSet)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Aplica un borde visual compartido a un panel contenedor que reacciona al foco de sus hijos.
     * El borde del panel contenedor se iluminará cuando cualquiera de los componentes internos reciba el foco
     *
     * @param wrapper El panel contenedor al que se aplicará el borde.
     * @param focusSource El componente principal que disparará el cambio de borde.
     * @param secondaryComp Un componente secundario opcional que también mantendrá el borde activo.
     */
    public static void applyUnifiedFocusBorder(JPanel wrapper, JComponent focusSource, JComponent secondaryComp) {

        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BORDER_SOFT, 1),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
        );

        Border focus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BBVA_NAVY, 2),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        );
        wrapper.setBorder(normal);

        FocusAdapter focusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrapper.setBorder(focus);
                wrapper.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                boolean focusWentToButton = (secondaryComp != null && secondaryComp.hasFocus());

                if (!focusWentToButton) {
                    wrapper.setBorder(normal);
                    wrapper.repaint();
                }
            }
        };
        focusSource.addFocusListener(focusListener);

        if (secondaryComp != null) {
            secondaryComp.addFocusListener(focusListener);
        }
    }

    /**
     * Aplica un borde que se vuelve azul cuando el componente recibe el foco.
     * Configura el borde inicial y añade un listener para alternar automáticamente
     * entre el estilo normal y el estilo resaltado (azul) al hacer clic o tabular.
     *
     * @param c El componente al que se le aplicará el estilo.
     */
    public static void applyBlueFocusBorder(JComponent c) {

        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BORDER_SOFT, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        Border focus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BBVA_NAVY, 2),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)
        );

        c.setBorder(normal);

        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                c.setBorder(focus);
            }

            @Override
            public void focusLost(FocusEvent e) {
                c.setBorder(normal);
            }
        });
    }

    /**
     * Aplica un borde que reacciona al foco en un JScrollPane basándose en el estado del componente interno.
     *
     * @param scrollPane El JScrollPane contenedor que actuará como borde visual.
     * @param innerComponent El componente dentro del scroll (ej. JTextArea) que recibe el foco real.
     */
    public static void applyScrollFocusBorder(JComponent scrollPane, JComponent innerComponent) {
        scrollPane.setBackground(Color.WHITE);

        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BORDER_SOFT, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8));

        Border focus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BBVA_NAVY, 2),
                BorderFactory.createEmptyBorder(4, 7, 4, 7));

        scrollPane.setBorder(normal);

        innerComponent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { scrollPane.setBorder(focus); scrollPane.repaint(); }
            @Override
            public void focusLost(FocusEvent e) { scrollPane.setBorder(normal); scrollPane.repaint(); }
        });
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
    public static void styleSpinner(JSpinner spinner) {

        JComponent editor = spinner.getEditor();
        JFormattedTextField tf = null;
        if (editor instanceof JSpinner.DefaultEditor) {
            tf = ((JSpinner.DefaultEditor) editor).getTextField();
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

}
