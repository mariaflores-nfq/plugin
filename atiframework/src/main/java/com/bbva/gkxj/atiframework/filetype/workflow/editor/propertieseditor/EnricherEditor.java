package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.TaskExecutorCombo;

import javax.swing.*;

/**
 * Editor especializado para componentes de tipo Enricher (Enriquecedor) dentro del workflow.
 * <p>
 * Un Enricher es un componente diseñado para ampliar o modificar la información del mensaje
 * en tránsito (por ejemplo, consultando a una base de datos o un servicio externo).
 * </p>
 * <p>
 * Esta implementación extiende de {@link TaskExecutorCombo}, proporcionando automáticamente
 * la capacidad de configurar el <b>Task Executor</b> asociado para su procesamiento asíncrono.
 * Además, gestiona de forma visual el campo "Subtype", el cual se presenta como solo lectura
 * para informar sobre la especialización técnica del enriquecedor.
 * </p>
 */
public class EnricherEditor extends TaskExecutorCombo {

    /** Campo de texto de solo lectura para mostrar el subtipo específico del enriquecedor. */
    private JTextField subtypeField;

    /**
     * Construye la interfaz visual específica para el nodo Enricher.
     * <p>
     * Utiliza el método heredado {@code buildExecutableUI} para renderizar los campos comunes
     * (como ID y Código) y el menú desplegable del Task Executor. Posteriormente, inicializa
     * y añade el campo {@code subtypeField} configurándolo con un estilo visual de solo lectura.
     * </p>
     */
    @Override
    protected void buildSpecificUI() {
        buildExecutableUI("Enricher Details", "Component Code");

        subtypeField = new JTextField();
        setReadOnlyStyle(subtypeField);
        addFormField("Subtype", subtypeField);
    }

    /**
     * Carga los datos del modelo {@link WorkflowJsonData} en los componentes de la interfaz.
     * <p>
     * Sincroniza tanto los datos comunes de ejecución (llamando a la clase padre) como el valor
     * específico del subtipo. Utiliza la bandera {@code isUpdating} para evitar que se
     * disparen eventos de modificación accidentalmente durante el volcado de datos.
     * </p>
     *
     * @param data Objeto de datos que contiene la configuración del nodo Enricher a visualizar.
     */
    @Override
    public void loadData(WorkflowJsonData data) {
        loadExecutableData(data);

        isUpdating = true;
        try {
            subtypeField.setText(data.getSubtype() != null ? data.getSubtype() : "");
        } finally {
            isUpdating = false;
        }
    }

    /**
     * Persiste los cambios de la interfaz al modelo de datos y limpia atributos incompatibles.
     * <p>
     * Guarda la configuración del Task Executor y los metadatos básicos. Además, actúa como
     * un mecanismo de limpieza (sanitización): al ser un nodo de tipo Enricher, se asegura de
     * que propiedades exclusivas de otros tipos de componentes (como los atributos de un Aggregator
     * o un Splitter) se establezcan explícitamente a {@code null}. Esto mantiene la integridad
     * y ligereza del archivo JSON resultante.
     * </p>
     *
     * @param data Objeto de datos de destino donde se volcarán los valores del formulario.
     */
    @Override
    public void saveData(WorkflowJsonData data) {
        saveExecutableData(data);

        // Limpieza proactiva de campos de lógica estructural no aplicables a un Enricher
        data.setCorrelationType(null);
        data.setAggregationType(null);
        data.setReleaseType(null);
        data.setJavaClassName(null);
        data.setRootElement(null);
    }
}