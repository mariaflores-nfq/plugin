package com.bbva.gkxj.atiframework.filetype.scheduler.editor.panels;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel de edición para la configuración general del planificador.
 *
 * Esta clase gestiona las diferentes secciones que permiten al usuario establecer distintos parámetros de ejecución
 */
public class SchedulerBehaviourPanel extends JPanel {

    /** Proyecto de IntelliJ asociado al panel. */
    private final Project myProject;

    /** Referencia al archivo virtual que se está editando. */
    private final VirtualFile myFile;

    /** Pestaña Trigger. */
    private SchedulerTriggerPanel schedulerTriggerPanel;

    /** Pestaña Conditions. */
    private SchedulerConditionsPanel schedulerConditionsPanel;

    /** Pestaña ActionPanel. */
    private SchedulerActionPanel schedulerActionPanel;

    /** Pestaña ActionErrorPanel. */
    private SchedulerActionErrorPanel schedulerActionErrorPanel;

    /** Pestaña FailedTreatmentPanel. */
    private SchedulerFailedTreatmentPanel schedulerFailedTreatmentPanel;

    /**
     * Crea un nuevo SchedulerBehaviourPanel asociado a un proyecto y un fichero.
     *
     * @param project proyecto de IntelliJ donde se está editando.
     * @param file    fichero virtual cuyo contenido JSON se edita en este panel.
     */
    public SchedulerBehaviourPanel(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myFile = file;
        createUIComponents();
    }

    /**
     * Inicializa y configura todos los componentes visuales del panel.
     */
    private void createUIComponents() {

        setLayout(new BorderLayout());
        setBackground(SchedulerTheme.BG_MAIN);

        TitledBorder title = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(),
                "Scheduler Behaviour"
        );
        title.setTitleFont(SchedulerTheme.TITLE_FONT);
        title.setTitleColor(SchedulerTheme.TEXT_MAIN);

        setBorder(BorderFactory.createCompoundBorder(
                title,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        UIManager.put("TabbedPane.borderHightlightColor", SchedulerTheme.BBVA_BLUE);
        UIManager.put("TabbedPane.darkShadow", SchedulerTheme.BBVA_BLUE);
        UIManager.put("TabbedPane.light", SchedulerTheme.BBVA_BLUE);
        UIManager.put("TabbedPane.tabInsets", new Insets(4, 10, 4, 10));

        schedulerTriggerPanel = new SchedulerTriggerPanel(myProject, myFile);
        schedulerConditionsPanel = new SchedulerConditionsPanel(myProject, myFile);
        schedulerActionPanel = new SchedulerActionPanel(myProject, myFile);
        schedulerActionErrorPanel = new SchedulerActionErrorPanel(myProject, myFile);
        schedulerFailedTreatmentPanel = new SchedulerFailedTreatmentPanel(myProject, myFile);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Trigger Info", schedulerTriggerPanel);
        tabs.addTab("Conditions Info", schedulerConditionsPanel);
        tabs.addTab("Action", schedulerActionPanel);
        tabs.addTab("Action on Error", schedulerActionErrorPanel);
        tabs.addTab("Failed Treatment", schedulerFailedTreatmentPanel);

        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Carga los datos del JSON en los paneles visuales.
     *
     * @param jsonObject Objeto con los datos
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (schedulerTriggerPanel != null) {
            schedulerTriggerPanel.updateForm(jsonObject);
        }

        if (schedulerConditionsPanel != null) {
            schedulerConditionsPanel.updateForm(jsonObject);
        }

        if (schedulerActionPanel != null) {
            schedulerActionPanel.updateForm(jsonObject);
        }

        if (schedulerActionErrorPanel != null) {
            schedulerActionErrorPanel.updateForm(jsonObject);
        }

        if( schedulerFailedTreatmentPanel != null){
            schedulerFailedTreatmentPanel.updateForm(jsonObject);
        }

    }

    /**
     * Vuelca los datos de los paneles visuales al objeto JSON.
     *
     * @param jsonObject Objeto con los datos
     */
    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (schedulerTriggerPanel != null) {
            schedulerTriggerPanel.updateDocument(jsonObject);
        }

        if (schedulerConditionsPanel != null) {
            schedulerConditionsPanel.updateDocument(jsonObject);
        }

        if (schedulerActionPanel != null) {
            schedulerActionPanel.updateDocument(jsonObject);
        }

        if (schedulerActionErrorPanel != null) {
            schedulerActionErrorPanel.updateDocument(jsonObject);
        }

        if (schedulerFailedTreatmentPanel != null) {
            schedulerFailedTreatmentPanel.updateDocument(jsonObject);
        }
    }

    /**
     * Propaga los listeners de eventos del editor principal a todos los sub-paneles componentes.
     *
     * @param textListener   listener para cambios en campos de texto.
     * @param actionListener listener para acciones (combos, botones, etc.).
     * @param changeListener listener para cambios en componentes tipo spinner.
     */
    public void addFieldListeners(javax.swing.event.DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {

        if (schedulerTriggerPanel != null) {
            schedulerTriggerPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if (schedulerConditionsPanel != null) {
            schedulerConditionsPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if (schedulerActionPanel != null) {
            schedulerActionPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if (schedulerActionErrorPanel != null) {
            schedulerActionErrorPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if (schedulerFailedTreatmentPanel != null) {
            schedulerFailedTreatmentPanel.addFieldListeners(textListener, actionListener);
        }
    }
}