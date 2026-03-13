package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import javax.swing.*;

/**
 * Editor especializado para componentes de tipo Splitter.
 * <p>
 * El Splitter se encarga de dividir un mensaje compuesto en varios mensajes individuales.
 * Este editor gestiona la visibilidad de los campos técnicos basándose en la estrategia de división:
 * <ul>
 * <li>Si el subtipo es <b>Java Class</b>, se habilita la visualización del nombre de la clase.</li>
 * <li>Si el subtipo es <b>Root Element</b>, se habilita el campo para visualizar el nodo raíz.</li>
 * </ul>
 * Utiliza los componentes visuales personalizados Ati (como {@link AtiTextField}) para
 * mantener la coherencia estética del formulario.
 * </p>
 */
public class SplitterEditor extends BaseCommonEditor {

    /** Campo de texto personalizado para mostrar el subtipo de división (ej. "Java Class", "Root Element", "Default"). */
    private AtiTextField subtypeField;

    /** Campo de texto personalizado para especificar la clase Java encargada de la lógica de división. */
    private AtiTextField javaClassNameField;

    /** Campo de texto personalizado para definir el elemento raíz sobre el cual se realizará la división. */
    private AtiTextField rootElementField;

    /** Contenedor de la fila de Clase Java para controlar su visibilidad dinámica (incluye etiqueta y campo). */
    private JPanel javaClassPanelWrapper;

    /** Contenedor de la fila de Elemento Raíz para controlar su visibilidad dinámica (incluye etiqueta y campo). */
    private JPanel rootElementPanelWrapper;

    /**
     * Construye la interfaz visual específica para el Splitter.
     * <p>
     * Define los campos adicionales utilizando {@link AtiTextField} y guarda las referencias
     * a sus contenedores (wrappers) para permitir ocultarlos o mostrarlos programáticamente
     * durante la carga de datos.
     * </p>
     */
    @Override
    protected void buildSpecificUI() {
        // Inicializa la base común (ID, Component Code, Description con AtiResizableTextArea)
        buildCommonUI("Splitter Details", "Component Code");

        subtypeField = new AtiTextField();
        setReadOnlyStyle(subtypeField);
        addFormField("Subtype", subtypeField);

        javaClassNameField = new AtiTextField();
        setReadOnlyStyle(javaClassNameField);
        javaClassPanelWrapper = addFormField("Java class Name", javaClassNameField);

        rootElementField = new AtiTextField();
        setReadOnlyStyle(rootElementField);
        rootElementPanelWrapper = addFormField("Root Element", rootElementField);
    }

    /**
     * Carga los datos del modelo en los componentes de la interfaz.
     * <p>
     * Determina qué campos técnicos deben ser visibles analizando el contenido de {@code subtype}.
     * Realiza un {@code revalidate()} para asegurar que el layout se ajuste tras cambiar la visibilidad.
     * </p>
     * @param data El objeto {@link WorkflowJsonData} con la configuración del nodo Splitter.
     */
    @Override
    public void loadData(WorkflowJsonData data) {
        loadCommonData(data);
        isUpdating = true;
        try {
            String subtype = data.getSubtype() != null ? data.getSubtype() : "";
            subtypeField.setText(subtype);
            javaClassNameField.setText(data.getJavaClassName() != null ? data.getJavaClassName() : "");
            rootElementField.setText(data.getRootElement() != null ? data.getRootElement() : "");

            // Lógica de visibilidad condicional
            javaClassPanelWrapper.setVisible("Java Class".equals(subtype));
            rootElementPanelWrapper.setVisible("Root Element".equals(subtype));

            // Refresco del panel para actualizar la posición de los componentes visibles
            contentPanel.revalidate();
            contentPanel.repaint();
        } finally {
            isUpdating = false;
        }
    }

    /**
     * Persiste los cambios de la interfaz al modelo y realiza una limpieza preventiva.
     * <p>
     * Al guardar un Splitter, se limpian explícitamente los campos que pertenecen a
     * componentes de agregación o ejecución (como {@code taskExecutor}, {@code correlationType}
     * o {@code releaseType}) para evitar inconsistencias y "basura" en el JSON resultante.
     * </p>
     * @param data El objeto {@link WorkflowJsonData} de destino.
     */
    @Override
    public void saveData(WorkflowJsonData data) {
        saveCommonData(data);

        // Limpieza de campos incompatibles con el tipo Splitter
        data.setTaskExecutor(null);
        data.setCorrelationType(null);
        data.setAggregationType(null);
        data.setReleaseType(null);
    }
}