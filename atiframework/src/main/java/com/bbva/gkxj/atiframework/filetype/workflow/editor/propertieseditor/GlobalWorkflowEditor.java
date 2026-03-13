package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.util.ui.JBUI;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Editor de propiedades globales para el archivo de Workflow.
 * <p>
 * A diferencia de los editores de nodos individuales, esta clase se encarga de la configuración
 * de nivel raíz del documento. Gestiona metadatos generales y coordina dos secciones dinámicas
 * críticas: {@link TaskExecutorsSectionPanel} y {@link EnvironmentVariablesSectionPanel}.
 * Utiliza los componentes del sistema de diseño Ati ({@link AtiTextField}).
 * </p>
 * <p>
 * Los campos de identificación técnica (Workflow Code, ID, Status) se presentan como solo lectura
 * para garantizar la integridad de la cabecera del JSON, permitiendo la edición de la descripción
 * y listas de instancias intermedias.
 * </p>
 */
public class GlobalWorkflowEditor extends AbstractComponentEditor {

    /** Campo de texto personalizado para el código identificador del flujo de trabajo (Solo lectura). */
    private AtiTextField workflowCodeField;

    /** Campo de texto personalizado que indica el estado actual del flujo (Solo lectura). */
    private AtiTextField statusField;

    /** Campo de texto personalizado para la lista de IDs de instancias intermedias, manejado como una cadena separada por comas. */
    private AtiTextField middleInstanceField;

    /** Sección para la gestión de hilos y ejecutores de tareas asíncronas. */
    private TaskExecutorsSectionPanel executorsSection;

    /** Sección para la gestión de las variables de entorno del flujo. */
    private EnvironmentVariablesSectionPanel variablesSection;

    /**
     * Construye la interfaz de usuario global.
     * <p>
     * Organiza el panel en tres bloques principales:
     * <ol>
     * <li><b>General:</b> Metadatos básicos del flujo.</li>
     * <li><b>Middle Instances:</b> Configuración de instancias.</li>
     * <li><b>Secciones Dinámicas:</b> Listas de ejecutores y variables de entorno.</li>
     * </ol>
     * </p>
     */
    @Override
    protected void buildSpecificUI() {

        // --- Encabezado de Sección General ---
        JPanel generalHeader = new JPanel(new BorderLayout());
        generalHeader.setBackground(WorkFlowStyles.UI_PANEL_BG);
        generalHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("General");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        generalHeader.add(title, BorderLayout.WEST);
        generalHeader.setBorder(JBUI.Borders.emptyBottom(10));
        contentPanel.add(generalHeader);

        // --- Inicialización de campos de metadatos ---
        workflowCodeField = new AtiTextField();
        setReadOnlyStyle(workflowCodeField);

        // NO instanciamos idField de nuevo, ya viene instanciado y con listener desde AbstractComponentEditor
        setReadOnlyStyle(idField);

        statusField = new AtiTextField();
        setReadOnlyStyle(statusField);

        addFormField("Workflow code", workflowCodeField);
        addFormField("ID", idField);
        addFormField("Status", statusField);

        addCompoundField(descriptionArea);

        // --- Sección Middle Instance Id List ---
        // Usamos directamente addFormField para aprovechar AtiLabeledComponent y el sistema de diseño
        middleInstanceField = new AtiTextField();
        WorkflowThemeUtils.applyWorkflowTheme(middleInstanceField);
        addChangeListener(middleInstanceField);
        addFormField("Middle Instance Id List", middleInstanceField);

        // --- Integración de Secciones Dinámicas ---
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Se pasa notifyChange como callback para que cualquier cambio interno
        // en las listas marque el documento como modificado.
        executorsSection = new TaskExecutorsSectionPanel(this::notifyChange);
        contentPanel.add(executorsSection);

        variablesSection = new EnvironmentVariablesSectionPanel(this::notifyChange);
        contentPanel.add(variablesSection);
    }

    /**
     * Carga los datos globales del modelo JSON en los componentes de la interfaz.
     */
    @Override
    public void loadData(WorkflowJsonData data) {
        isUpdating = true;
        try {
            workflowCodeField.setText(data.getWorkflowCode() != null ? data.getWorkflowCode() : "");
            idField.setText(data.getId() != null ? data.getId() : "");
            statusField.setText(data.getStatus() != null ? data.getStatus() : "");

            descriptionField.setText(data.getDescription() != null ? data.getDescription() : "");

            // Conversión de Lista a String (UI)
            if (data.getMiddleInstanceIdList() != null && !data.getMiddleInstanceIdList().isEmpty()) {
                middleInstanceField.setText(String.join(", ", data.getMiddleInstanceIdList()));
            } else {
                middleInstanceField.setText("");
            }

            executorsSection.loadData(data.getTaskExecutors());
            variablesSection.loadData(data.getEnvironmentVariables());
        } finally {
            isUpdating = false;
        }
    }

    /**
     * Persiste los datos de la interfaz global de vuelta al modelo de datos.
     */
    @Override
    public void saveData(WorkflowJsonData data) {

        data.setDescription(descriptionField.getText());

        // Persistir cambios en las sub-secciones
        executorsSection.saveData(data);
        variablesSection.saveData(data);

        // Conversión de String (UI) a Lista (Modelo)
        String middleText = middleInstanceField.getText().trim();
        if (!middleText.isEmpty()) {
            data.setMiddleInstanceIdList(new ArrayList<>(Arrays.asList(middleText.split("\\s*,\\s*"))));
        } else {
            data.setMiddleInstanceIdList(null);
        }
    }

    /**
     * Método auxiliar para añadir filas de acción con botones.
     * Útil para futuras expansiones de la interfaz global.
     * @param text Texto descriptivo de la fila.
     */
    private void addListActionRow(String text) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(WorkFlowStyles.UI_PANEL_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnAdd = new JButton("+");
        btnAdd.setFont(new Font("Arial", Font.BOLD, 22));
        btnAdd.setForeground(WorkFlowStyles.UI_ICON_ACTION);
        btnAdd.setOpaque(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorder(null);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        row.add(label, BorderLayout.WEST);
        row.add(btnAdd, BorderLayout.EAST);
        row.setBorder(JBUI.Borders.empty(5, 0));

        contentPanel.add(row);
    }
}