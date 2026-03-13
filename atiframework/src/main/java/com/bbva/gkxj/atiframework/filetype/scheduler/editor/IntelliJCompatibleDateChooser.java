package com.bbva.gkxj.atiframework.filetype.scheduler.editor;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.intellij.ui.components.JBPanel;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Date;

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.applyUnifiedFocusBorder;
import static icons.AtiIcons.CALENDAR_ICON;

/**
 * Componente de selector de fecha compatible con el estilo visual de IntelliJ.
 *
 * Envuelve un {@link com.toedter.calendar.JDateChooser} aplicando estilos y
 * comportamiento coherentes con la interfaz de usuario de IntelliJ Platform.
 * Proporciona un campo de texto para la fecha y un boton con icono de calendario.
 */
public class IntelliJCompatibleDateChooser extends JBPanel<IntelliJCompatibleDateChooser> {

    /**
     * Componente interno encargado de la seleccion de fecha.
     */
    private final JDateChooser dateChooser;

    /**
     * Tamaño preferido del campo de texto del selector de fecha.
     */
    private static final Dimension FIELD_SIZE = new Dimension(155, 40);

    /**
     * Crea un nuevo selector de fecha con estilo IntelliJ.
     *
     * Configura el {@link JDateChooser} interno, personaliza el editor de texto,
     * el boton del calendario y aplica el borde de foco unificado.
     *
     * @throws RuntimeException si se produce algun error al inicializar el selector de fechas
     */
    public IntelliJCompatibleDateChooser() {
        super(new BorderLayout(0,0));

        LookAndFeel previousLaf = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JTextFieldDateEditor customEditor = new JTextFieldDateEditor("yyyy-MM-dd", null, ' ') {
                @Override
                public void setForeground(Color fg) {
                    super.setForeground(SchedulerTheme.TEXT_MAIN);
                }
            };
            dateChooser = new JDateChooser(customEditor);
            dateChooser.setPreferredSize(FIELD_SIZE);

            JTextField editorComponent = (JTextField) dateChooser.getDateEditor().getUiComponent();
            JButton calendarButton = dateChooser.getCalendarButton();

            editorComponent.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            editorComponent.setOpaque(false);
            editorComponent.putClientProperty("JComponent.outline", null);
            editorComponent.putClientProperty("Validation.target", null);
            editorComponent.putClientProperty("SpellCheck", Boolean.FALSE);

            calendarButton.setText("");
            calendarButton.setBorder(null);
            calendarButton.setMargin(new Insets(0, 0, 0, 0));
            calendarButton.setContentAreaFilled(false);
            calendarButton.setFocusPainted(false);
            calendarButton.setOpaque(false);
            calendarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (CALENDAR_ICON != null) {
                calendarButton.setIcon(CALENDAR_ICON);
            }
            calendarButton.setPreferredSize(new Dimension(25, FIELD_SIZE.height));

            add(editorComponent, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);
            applyUnifiedFocusBorder(this, editorComponent, calendarButton);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo inicializar el selector de fechas", e);
        } finally {
            try {
                if (previousLaf != null) {
                    UIManager.setLookAndFeel(previousLaf);
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Obtiene la fecha actualmente seleccionada en el selector.
     *
     * @return la fecha seleccionada o si no hay ninguna
     */
    public Date getDate() {
        return dateChooser.getDate();
    }

    /**
     * Establece la fecha seleccionada en el selector.
     *
     * @param date fecha a establecer.
     */
    public void setDate(Date date) {
        dateChooser.setDate(date);
    }

    /**
     * Registra un {@link PropertyChangeListener} para escuchar cambios
     * en una propiedad concreta del selector de fecha subyacente.
     *
     * @param propertyName nombre de la propiedad a observar
     * @param listener     listener que recibir\u00e1 las notificaciones de cambio
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        dateChooser.addPropertyChangeListener(propertyName, listener);
    }
}