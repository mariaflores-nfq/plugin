package com.bbva.gkxj.atiframework.filetype.scheduler.editor.panels;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

/**
 * Panel principal que contiene los subpaneles de detalles y comportamiento del scheduler.
 */
public class BodyPanel extends JPanel {

    /** Panel de detalles del scheduler */
    private SchedulerDetailsPanel schedulerDetailsPanel;

    /** Panel de comportamiento del scheduler */
    private SchedulerBehaviourPanel schedulerBehaviourPanel;

    /**
     * Constructor del BodyPanel.
     * @param project Proyecto de IntelliJ asociado al panel.
     * @param virtualFile Archivo virtual que representa el recurso abierto.
     */
    public BodyPanel(Project project, VirtualFile virtualFile) {
        schedulerDetailsPanel = new SchedulerDetailsPanel(project, virtualFile);
        schedulerBehaviourPanel = new SchedulerBehaviourPanel(project, virtualFile);
        createUIComponents();
    }

    /**
     * Inicializa y configura todos los componentes visuales del panel.
     */
    private void createUIComponents() {
        // BorderLayout para que el ScrollPane ocupe el espacio
        setLayout(new BorderLayout());
        setBackground(SchedulerTheme.BBVA_NAVY);

        // Crear un panel contenedor para los elementos internos al scroll
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(SchedulerTheme.BBVA_NAVY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));

        // Añadir componentes al contentPanel
        contentPanel.add(schedulerDetailsPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(schedulerBehaviourPanel);

        // Evitar que en pantallas grandes se estire demasiado
        contentPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JBScrollPane(contentPanel);

        // Configuración estética del ScrollPane
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // Si el tamaño de la pantalla es bajo, permitir hacer scroll al contenido
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(SchedulerTheme.BBVA_NAVY);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Determina que control va a recibir el foco al abrir el editor.
     * @return
     */
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.schedulerDetailsPanel.getPreferredFocusedComponent();
    }

    public void addFieldListeners(javax.swing.event.DocumentListener textListener, ActionListener actionListener, PropertyChangeListener dateChangeListener, ChangeListener changeListener) {
        this.schedulerDetailsPanel.addFieldListeners(textListener, actionListener, dateChangeListener);
        this.schedulerBehaviourPanel.addFieldListeners(textListener, actionListener, changeListener);
    }

    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado.
     * @param jsonObject
     */
    public void updateForm(JsonObject jsonObject) {
        this.schedulerDetailsPanel.updateForm(jsonObject);
        this.schedulerBehaviourPanel.updateForm(jsonObject);
    }

    /**
     * Actualiza el JsonObject proporcionado con los datos del formulario.
     * @param jsonObject
     */
    public void updateDocument(JsonObject jsonObject) {
        this.schedulerDetailsPanel.updateDocument(jsonObject);
        this.schedulerBehaviourPanel.updateDocument(jsonObject);
    }

    public void validateDateFields(){
        this.schedulerDetailsPanel.validateDateFields();
    }
}