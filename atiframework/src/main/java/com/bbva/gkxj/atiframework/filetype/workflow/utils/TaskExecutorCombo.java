package com.bbva.gkxj.atiframework.filetype.workflow.utils;

import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor.BaseCommonEditor;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;

import javax.swing.*;
import java.util.List;
import java.util.Vector;

/**
 * Clase abstracta que extiende la funcionalidad de edición común para añadir la gestión de ejecutores de tareas.
 * <p>
 * Proporciona una interfaz estandarizada con un componente {@link AtiComboBox} para que el usuario pueda
 * seleccionar un "Task Executor" de una lista predefinida, manteniendo la coherencia visual del framework.
 * Esta lista se inyecta dinámicamente desde los metadatos globales del flujo de trabajo a través del
 * método {@link #setGlobalExecutors(List)}.
 * </p>
 * <p>
 * Al heredar de {@link BaseCommonEditor}, también incluye y gestiona los campos básicos de identificación
 * (como el título del panel, el código del componente y la descripción) definidos en la clase base.
 * </p>
 */
public abstract class TaskExecutorCombo extends BaseCommonEditor {

    /** Componente visual desplegable personalizado (Ati) para la selección del ejecutor de tareas. */
    protected AtiComboBox taskExecutorCombo = new AtiComboBox(new Object[0]);

    /** * Lista de los códigos de los ejecutores globales disponibles.
     * Es inyectada desde el controlador principal o el panel de propiedades superior.
     */
    protected List<String> globalExecutors;

    /**
     * Inyecta la lista de ejecutores globales definidos en el nivel raíz del workflow.
     * <p>
     * <b>Importante:</b> Este método DEBE llamarse antes de ejecutar {@link #loadExecutableData(WorkflowJsonData)}
     * para asegurar que el componente desplegable tenga opciones disponibles al momento de renderizar.
     * </p>
     * @param globalExecutors Lista de cadenas de texto con los códigos de los ejecutores disponibles.
     */
    public void setGlobalExecutors(List<String> globalExecutors) {
        this.globalExecutors = globalExecutors;
    }

    /**
     * Construye la interfaz de usuario específica para componentes que ejecutan tareas asíncronas.
     * <p>
     * Este método invoca la construcción de la interfaz base común y añade una nueva fila
     * al formulario que contiene el menú desplegable (Combo) de ejecutores. Además, registra
     * automáticamente los listeners necesarios para detectar cambios en la selección.
     * </p>
     *
     * @param panelTitle Título que se mostrará en la cabecera del panel de propiedades.
     * @param codeLabel  Texto descriptivo para la etiqueta del campo del código del componente.
     */
    protected void buildExecutableUI(String panelTitle, String codeLabel) {
        // Construye el ID, Component Code y Description (con AtiResizableTextArea)
        buildCommonUI(panelTitle, codeLabel);
        WorkflowThemeUtils.applyWorkflowTheme(taskExecutorCombo);
        // Registramos el listener reactivo y añadimos el combo envuelto en un AtiLabeledComponent
        addComboListener(taskExecutorCombo);
        addFormField("Task Executor", taskExecutorCombo);
    }

    /**
     * Carga los datos del modelo en los componentes visuales de la interfaz de usuario.
     * <p>
     * Inicializa y puebla el modelo interno del {@link AtiComboBox} utilizando la lista
     * de {@code globalExecutors} inyectada previamente. Posteriormente, si el objeto
     * de datos del nodo ya posee un ejecutor asignado, lo establece como la opción seleccionada.
     * </p>
     * @param nodeData Objeto {@link WorkflowJsonData} que contiene la configuración específica del nodo seleccionado.
     */
    public void loadExecutableData(WorkflowJsonData nodeData) {
        loadCommonData(nodeData);

        // ACTIVAMOS LA PROTECCIÓN: Evitamos que el combo dispare eventos 'falsos' al ser rellenado
        boolean previousUpdatingState = isUpdating;
        isUpdating = true;

        try {
            // Optimización: Usar un Vector permite inicializar el modelo de una sola vez sin bucles for
            Vector<String> options = new Vector<>();
            options.add(""); // Añadimos opción vacía por defecto

            if (this.globalExecutors != null && !this.globalExecutors.isEmpty()) {
                options.addAll(this.globalExecutors);
            }

            // Asignar el modelo o cambiar el selectedItem dispara eventos internos de Swing.
            // Gracias a 'isUpdating = true', el addComboListener ignorará estos cambios transitorios.
            taskExecutorCombo.setModel(new DefaultComboBoxModel<>(options));

            if (nodeData.getTaskExecutor() != null) {
                taskExecutorCombo.setSelectedItem(nodeData.getTaskExecutor());
            } else {
                taskExecutorCombo.setSelectedIndex(0); // Por seguridad, forzamos el elemento vacío si es null
            }
        } finally {
            // RESTAURAMOS EL ESTADO ORIGINAL
            isUpdating = previousUpdatingState;
        }
    }

    /**
     * Sincroniza y extrae la selección actual de la interfaz de usuario para guardarla en el modelo de datos.
     * <p>
     * Obtiene el valor seleccionado en el combo de ejecutores. Si el valor está vacío o no hay
     * selección, se encarga de guardar un valor {@code null} en el objeto subyacente para
     * mantener el JSON resultante limpio.
     * </p>
     * @param nodeData Objeto {@link WorkflowJsonData} de destino donde se persistirá la información de la UI.
     */
    public void saveExecutableData(WorkflowJsonData nodeData) {
        saveCommonData(nodeData);

        Object selected = taskExecutorCombo.getSelectedItem();
        // Optimización: Añadimos trim() para limpiar posibles espacios en blanco accidentales
        String selectedStr = selected != null ? selected.toString().trim() : "";

        // Si la cadena está vacía (el usuario seleccionó la opción en blanco), guardamos null
        nodeData.setTaskExecutor(selectedStr.isEmpty() ? null : selectedStr);
    }
}