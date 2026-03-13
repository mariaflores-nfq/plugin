package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;

/**
 * Clase base que agrupa la lógica y los componentes visuales comunes para los editores de nodos.
 * <p>
 * Proporciona métodos para construir y gestionar los campos que comparten la mayoría de los
 * componentes del workflow, tales como el Identificador (ID), el Código del Componente
 * y la Descripción técnica.
 * </p>
 */
public abstract class BaseCommonEditor extends AbstractComponentEditor {

    /**
     * Construye la interfaz de usuario común para el panel de propiedades.
     * <p>
     * Organiza los campos básicos en el orden estándar: Título de sección, Código, ID
     * y finalmente la Descripción (usando el nuevo componente redimensionable).
     * </p>
     * * @param panelTitle     El título que se mostrará en la cabecera del panel.
     * @param codeLabelTitle La etiqueta específica para el campo de código (ej: "Component Code" o "Service Code").
     */
    protected void buildCommonUI(String panelTitle, String codeLabelTitle) {
        addSectionTitle(panelTitle);

        // Campos de identificación técnica
        addFormField(codeLabelTitle, componentCodeField);
        addFormField("ID", idField);
        addCompoundField(descriptionArea);
    }

    /**
     * Mapea los valores del modelo de datos común hacia los campos de texto de la interfaz.
     * <p>
     * Utiliza el guardián {@code isUpdating} para asegurar que el volcado de datos
     * no dispare eventos de modificación accidentales.
     * </p>
     * * @param data El objeto {@link WorkflowJsonData} con la información del nodo.
     */
    public void loadCommonData(WorkflowJsonData data) {
        isUpdating = true;
        try {
            idField.setText(data.getId() != null ? data.getId() : "");
            componentCodeField.setText(data.getComponentCode() != null ? data.getComponentCode() : "");
            descriptionField.setText(data.getDescription() != null ? data.getDescription() : "");
        } finally {
            isUpdating = false;
        }
    }

    /**
     * Extrae la información de los campos comunes de la interfaz y la guarda en el modelo.
     * * @param data El objeto {@link WorkflowJsonData} donde se persistirán los cambios.
     */
    public void saveCommonData(WorkflowJsonData data) {
        data.setId(idField.getText());
        data.setComponentCode(componentCodeField.getText());
        // El guardado de datos funciona perfectamente con la referencia interna
        data.setDescription(descriptionField.getText());
    }
}