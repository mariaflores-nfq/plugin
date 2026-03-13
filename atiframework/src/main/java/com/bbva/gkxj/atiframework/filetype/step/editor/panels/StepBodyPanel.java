package com.bbva.gkxj.atiframework.filetype.step.editor.panels;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

/**
 * Panel principal que contiene los subpaneles de detalles y comportamiento del step.
 */
public class StepBodyPanel extends JPanel {

    /**
     * Panel de detalles del step
     */
    private StepDetailsPanel stepDetailsPanel;

    /**
     * Panel de comportamiento del step
     */
    private StepConfigurationPanel stepConfigurationPanel;

    /**
     * Constructor del BodyPanel.
     *
     * @param project     Proyecto de IntelliJ asociado al panel.
     * @param virtualFile Archivo virtual que representa el recurso abierto.
     */
    public StepBodyPanel(Project project, VirtualFile virtualFile) {
        stepDetailsPanel = new StepDetailsPanel(project, virtualFile);
        stepConfigurationPanel = new StepConfigurationPanel(project, virtualFile);
        createUIComponents();
        setupPanelInteractions();
    }



    /**
     * Inicializa y configura todos los componentes visuales del panel.
     */
    private void createUIComponents() {
        setLayout(new BorderLayout());
        setBackground(SchedulerTheme.BBVA_NAVY);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension pref = super.getPreferredSize();
                return new Dimension(pref.width, Math.max(pref.height, 800));
            }
        };
        contentPanel.setBackground(SchedulerTheme.BBVA_NAVY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));

        contentPanel.add(stepDetailsPanel, BorderLayout.NORTH);
        contentPanel.add(stepConfigurationPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(SchedulerTheme.BBVA_NAVY);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Establecer conexiones entre paneles para interfaz dinámica de pestañas
     * Conecta los eventos del panel de detalles con la interfaz del panel de configuración.
     */
    private void setupPanelInteractions() {
        String initialType = stepDetailsPanel.getSelectedType();
        if (initialType != null) {
            stepConfigurationPanel.updateVisibilityBasedOnType(initialType);
        }
        stepDetailsPanel.addTypeChangeListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String newSelectedType = (String) e.getItem();
                stepConfigurationPanel.updateVisibilityBasedOnType(newSelectedType);
            }
        });
    }

    /**
     * Determina que control va a recibir el foco al abrir el editor.
     *
     * @return
     */
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.stepDetailsPanel.getPreferredFocusedComponent();
    }

    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        this.stepDetailsPanel.addFieldListeners(textListener, actionListener,null);
        this.stepConfigurationPanel.addFieldListeners(textListener, actionListener,changeListener);
    }

    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado.
     *
     * @param jsonObject
     */
    public void updateForm(JsonObject jsonObject) {
        this.stepDetailsPanel.updateForm(jsonObject);
        this.stepConfigurationPanel.updateForm(jsonObject);
    }

    /**
     * Actualiza el JsonObject proporcionado con los datos del formulario.
     *
     * @param jsonObject
     */
    public void updateDocument(JsonObject jsonObject) {
        this.stepDetailsPanel.updateDocument(jsonObject);
        this.stepConfigurationPanel.updateDocument(jsonObject);
    }

}