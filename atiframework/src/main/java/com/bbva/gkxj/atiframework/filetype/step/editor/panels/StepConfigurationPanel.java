package com.bbva.gkxj.atiframework.filetype.step.editor.panels;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.reader.StepReaderPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.writer.StepWriterPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.panels.filemanteinance.StepFileTaskPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.panels.jasperreport.StepJasperReportPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.panels.queriestasklet.StepQueryTaskPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.EtlStepType;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;

import static com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants.STEP_CONFIGURATION_PANEL_TITLE;

/**
 * Panel de edición para la configuración general del planificador.
 *
 * Esta clase gestiona las diferentes secciones que permiten al usuario establecer distintos parámetros de ejecución
 */
public class StepConfigurationPanel extends JPanel {

    /**
     * Proyecto de IntelliJ asociado al panel.
     */
    private final Project myProject;

    /**
     * Referencia al archivo virtual que se está editando.
     */
    private final VirtualFile myFile;

    /**
     * Grupo de pestañas de configuración de parámetros
     */
    private JTabbedPane tabs;

    /**
     * Pestaña Step Parameter.
     */
    private StepParametersPanel stepParametersPanel;

    /**
     * Pestaña Step Reader.
     */
    private StepReaderPanel stepReaderPanel;

    /**
     * Pestaña Step Writer.
     */
    private StepWriterPanel stepWriterPanel;

    /**
     * Pestaña Critical Issue.
     */
    private StepCriticalIssuePanel stepCriticalIssuePanel;

    /**
     * Pestaña FailedTreatmentPanel.
     */
    private StepIssueTreatmentPanel stepIssueTreatmentPanel;

    /**
     * Pestaña StepFileTaskPanel
     */
    private StepFileTaskPanel stepFileTaskPanel;

    /**
     * Pestaña StepJasperReportPanel
     */
    private StepJasperReportPanel stepJasperReportPanel;

    /**
     * Pestaña para Query Task (usa StepQueryTaskPanel)
     */
    private StepQueryTaskPanel stepQueryTaskPanel;

    /**
     * Tipo de step actualmente seleccionado para detectar cambios.
     */
    private String currentStepType;

    /**
     * Referencia al JsonObject actual para poder limpiar datos al cambiar de tipo.
     */
    private JsonObject currentJsonObject;

    /**
     * Crea un nuevo  asociado a un proyecto y un fichero.
     *
     * @param project proyecto de IntelliJ donde se está editando.
     * @param file    fichero virtual cuyo contenido JSON se edita en este panel.
     */
    public StepConfigurationPanel(@NotNull Project project, @NotNull VirtualFile file) {
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
                STEP_CONFIGURATION_PANEL_TITLE
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
        UIManager.put("TabbedPane.tabInsets", JBUI.insets(4, 10));

        tabs = addStepTabs();
        add(tabs, BorderLayout.CENTER);

        setPreferredSize(new Dimension(0, 500));
        setMinimumSize(new Dimension(0, 400));
    }

    /**
     * Carga los datos del JSON en los paneles visuales.
     * Solo carga datos en los paneles correspondientes al tipo de step actualmente seleccionado.
     *
     * @param jsonObject Objeto con los datos
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        this.currentJsonObject = jsonObject;

        if (jsonObject.has(StepConstants.JSON_NODE_STEP_TYPE) && !jsonObject.get(StepConstants.JSON_NODE_STEP_TYPE).isJsonNull()) {
            this.currentStepType = jsonObject.get(StepConstants.JSON_NODE_STEP_TYPE).getAsString();
        }

        if (stepParametersPanel != null) {
            stepParametersPanel.updateForm(jsonObject);
        }

        if (stepIssueTreatmentPanel != null) {
            stepIssueTreatmentPanel.updateForm(jsonObject);
        }

        if (stepCriticalIssuePanel != null) {
            stepCriticalIssuePanel.updateForm(jsonObject);
        }

        EtlStepType type = EtlStepType.fromValue(currentStepType);
        if (type == null) {
            type = EtlStepType.fromString(currentStepType);
        }
        if (type != null) {
            cleanResidualNodes(type);

            switch (type) {
                case ETL_CHUNK:
                    if (stepReaderPanel != null) {
                        stepReaderPanel.updateForm(jsonObject);
                    }
                    if (stepWriterPanel != null) {
                        stepWriterPanel.updateForm(jsonObject);
                    }
                    break;
                case JASPER_REPORT:
                    if (stepJasperReportPanel != null) {
                        stepJasperReportPanel.updateForm(jsonObject);
                    }
                    break;
                case FILE_MAINTENANCE:
                    if (stepFileTaskPanel != null) {
                        stepFileTaskPanel.updateForm(jsonObject);
                    }
                    break;
                case QUERIES_TASK:
                    if (stepQueryTaskPanel != null) {
                        stepQueryTaskPanel.updateForm(jsonObject);
                    }
                    break;
            }
        }

    }

    /**
     * Vuelca los datos de los paneles visuales al objeto JSON.
     * Solo vuelca los datos de los paneles correspondientes al tipo de step actualmente seleccionado,
     * evitando que paneles de tipos anteriores re-escriban nodos JSON que ya fueron limpiados.
     *
     * @param jsonObject Objeto con los datos
     */
    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (stepParametersPanel != null) {
            stepParametersPanel.updateDocument(jsonObject);
        }

        if (stepIssueTreatmentPanel != null) {
            stepIssueTreatmentPanel.updateDocument(jsonObject);
        }

        if (stepCriticalIssuePanel != null) {
            stepCriticalIssuePanel.updateDocument(jsonObject);
        }

        EtlStepType type = EtlStepType.fromValue(currentStepType);
        if (type == null) {
            type = EtlStepType.fromString(currentStepType);
        }
        if (type != null) {
            cleanResidualNodesFromJson(jsonObject, type);

            switch (type) {
                case ETL_CHUNK:
                    if (stepReaderPanel != null) {
                        stepReaderPanel.updateDocument(jsonObject);
                    }
                    if (stepWriterPanel != null) {
                        stepWriterPanel.updateDocument(jsonObject);
                    }
                    break;
                case JASPER_REPORT:
                    if (stepJasperReportPanel != null) {
                        stepJasperReportPanel.updateDocument(jsonObject);
                    }
                    break;
                case FILE_MAINTENANCE:
                    if (stepFileTaskPanel != null) {
                        stepFileTaskPanel.updateDocument(jsonObject);
                    }
                    break;
                case QUERIES_TASK:
                    if (stepQueryTaskPanel != null) {
                        stepQueryTaskPanel.updateDocument(jsonObject);
                    }
                    break;
            }
        }

    }

    /**
     * Registra los listeners en todos los subpaneles para que los cambios se propaguen al documento.
     */
    public void addFieldListeners(javax.swing.event.DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {

        if (stepParametersPanel != null) {
            stepParametersPanel.addFieldListeners(textListener, actionListener);
        }

        if (stepIssueTreatmentPanel != null) {
            stepIssueTreatmentPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if (stepCriticalIssuePanel != null) {
            stepCriticalIssuePanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if (stepReaderPanel != null) {
            stepReaderPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if (stepWriterPanel != null) {
            stepWriterPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if(stepQueryTaskPanel != null) {
            stepQueryTaskPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if(stepFileTaskPanel != null){
            stepFileTaskPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

        if(stepJasperReportPanel != null){
            stepJasperReportPanel.addFieldListeners(textListener, actionListener, changeListener);
        }

    }

    public void updateVisibilityBasedOnType(String selectedType) {
        if (tabs == null) return;
        currentStepType = selectedType;

        EtlStepType type = EtlStepType.fromString(selectedType);
        if (type == null) {
            type = EtlStepType.fromValue(selectedType);
        }
        int selectedIndex = tabs.getSelectedIndex();
        String selectedTitle = selectedIndex >= 0 ? tabs.getTitleAt(selectedIndex) : null;
        tabs.removeAll();

        tabs.addTab("Step Parameters", stepParametersPanel);
        if (type != null) {
            switch (type) {
                case ETL_CHUNK:
                    tabs.addTab("Reader", stepReaderPanel);
                    tabs.addTab("Writer", stepWriterPanel);
                    break;
                case JASPER_REPORT:
                    tabs.addTab("Jasper Report", stepJasperReportPanel);
                    break;
                case FILE_MAINTENANCE:
                    tabs.addTab("File Task", stepFileTaskPanel);
                    break;
                case QUERIES_TASK:
                    tabs.addTab("Query Task", stepQueryTaskPanel);
                    break;
            }
        }

        if (stepIssueTreatmentPanel != null) {
            tabs.addTab("Issue Treatment", stepIssueTreatmentPanel);
        }
        if (stepCriticalIssuePanel != null) {
            tabs.addTab("Critical Issue", stepCriticalIssuePanel);
        }

        if (selectedTitle != null) {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                if (tabs.getTitleAt(i).equals(selectedTitle)) {
                    tabs.setSelectedIndex(i);
                    break;
                }
            }
        }

        this.revalidate();
        this.repaint();
    }


    /**
     * Añade las pestañas de configuración de parámetros a la interfaz
     *
     * @return Grupo de pestañas añadidas
     */
    public JTabbedPane addStepTabs() {
        stepParametersPanel = new StepParametersPanel(myProject, myFile);
        stepReaderPanel = new StepReaderPanel(myProject, myFile);
        stepWriterPanel = new StepWriterPanel(myProject, myFile);
        stepIssueTreatmentPanel = new StepIssueTreatmentPanel();
        stepCriticalIssuePanel = new StepCriticalIssuePanel(myProject, myFile);
        stepFileTaskPanel = new StepFileTaskPanel();
        stepJasperReportPanel = new StepJasperReportPanel(myProject, myFile);
        stepQueryTaskPanel = new StepQueryTaskPanel(myProject, myFile);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Step Parameters", stepParametersPanel);
        tabs.addTab("Reader", stepReaderPanel);
        tabs.addTab("Writer", stepWriterPanel);
        tabs.addTab("Issue Treatment", stepIssueTreatmentPanel);
        tabs.addTab("Critical Issue", stepCriticalIssuePanel);
        return tabs;
    }

    /**
     * Elimina del JSON los nodos que no corresponden al tipo de step actualmente seleccionado.
     * Esto resuelve el caso de archivos que ya tienen nodos residuales guardados de tipos anteriores.
     *
     * @param currentType El tipo de step actualmente seleccionado
     */
    private void cleanResidualNodes(EtlStepType currentType) {
        if (currentJsonObject == null || currentType == null) return;
        cleanResidualNodesFromJson(currentJsonObject, currentType);
    }

    /**
     * Elimina del JSON proporcionado los nodos que no corresponden al tipo indicado.
     * A diferencia de cleanResidualNodes, este método trabaja sobre el jsonObject que se le pasa,
     * no sobre la referencia interna currentJsonObject (que puede estar desactualizada).
     *
     * @param json        Objeto JSON del que eliminar nodos residuales
     * @param currentType El tipo de step actualmente seleccionado
     */
    private void cleanResidualNodesFromJson(JsonObject json, EtlStepType currentType) {
        if (json == null || currentType == null) return;

        for (EtlStepType t : EtlStepType.values()) {
            if (t != currentType) {
                switch (t) {
                    case ETL_CHUNK:
                        json.remove(StepConstants.JSON_NODE_READER);
                        json.remove(StepConstants.JSON_NODE_WRITER);
                        break;
                    case JASPER_REPORT:
                        json.remove(StepConstants.JSON_NODE_JASPER_REPORT);
                        break;
                    case FILE_MAINTENANCE:
                        json.remove(StepConstants.JSON_NODE_FILE_TASK);
                        break;
                    case QUERIES_TASK:
                        json.remove(StepConstants.JSON_NODE_QUERY_LIST);
                        break;
                }
            }
        }
    }


}
