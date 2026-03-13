package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import javax.swing.*;

/**
 * Editor específico para componentes de tipo Aggregator.
 * <p>
 * Este editor permite configurar las estrategias de correlación, agregación y liberación
 * (release) de mensajes. Estos parámetros son fundamentales para determinar cómo se agrupan
 * y procesan múltiples mensajes entrantes en uno solo.
 * </p>
 */
public class AgregatorEditor extends BaseCommonEditor {

    /** Selector para definir la estrategia de correlación (cómo identificar mensajes del mismo grupo). */
    private JComboBox<String> correlationTypeField;

    /** Selector para definir la lógica de agregación (cómo mezclar los mensajes). */
    private JComboBox<String> aggregationTypeField;

    /** Selector para definir cuándo se debe liberar el grupo de mensajes (timeout, tamaño, etc.). */
    private JComboBox<String> releaseTypeField;

    /**
     * Construye la interfaz específica para el Agregador.
     * <p>
     * Inicializa los combos con opciones predefinidas (Header, Javascript, Java Class, etc.)
     * y registra los escuchadores para notificar cambios al controlador del grafo.
     * </p>
     */
    @Override
    protected void buildSpecificUI() {
        // Inicializa campos comunes heredados
        buildCommonUI("Aggregator Details", "Component Code");

        // Configuración de Correlation Type
        correlationTypeField = new JComboBox<>(new String[]{"Header", "Payload Path", "Javascript"});
        addComboListener(correlationTypeField);
        addFormField("Correlation type", correlationTypeField);

        // Configuración de Aggregation Type
        aggregationTypeField = new JComboBox<>(new String[]{"Javascript", "Java class"});
        addComboListener(aggregationTypeField);
        addFormField("Aggregation type", aggregationTypeField);

        // Configuración de Release Type
        releaseTypeField = new JComboBox<>(new String[]{"Message group size", "Timeout", "Javascript", "Java Class"});
        addComboListener(releaseTypeField);
        addFormField("Release type", releaseTypeField);
    }

    /**
     * Carga los datos del modelo {@link WorkflowJsonData} en los componentes de la UI.
     * <p>
     * Utiliza el flag {@code isUpdating} para evitar que la carga inicial de datos
     * dispare eventos de modificación en el modelo.
     * </p>
     * * @param data Objeto de datos que contiene la configuración del nodo Aggregator.
     */
    @Override
    public void loadData(WorkflowJsonData data) {
        loadCommonData(data);
        isUpdating = true;
        try {
            if (data.getCorrelationType() != null && !data.getCorrelationType().trim().isEmpty()) {
                correlationTypeField.setSelectedItem(data.getCorrelationType());
            } else {
                correlationTypeField.setSelectedIndex(0);
            }

            if (data.getAggregationType() != null && !data.getAggregationType().trim().isEmpty()) {
                aggregationTypeField.setSelectedItem(data.getAggregationType());
            } else {
                aggregationTypeField.setSelectedIndex(0);
            }

            if (data.getReleaseType() != null && !data.getReleaseType().trim().isEmpty()) {
                releaseTypeField.setSelectedItem(data.getReleaseType());
            } else {
                releaseTypeField.setSelectedIndex(0);
            }
        } finally {
            isUpdating = false;
        }
    }

    /**
     * Extrae los valores seleccionados en la UI y los persiste en el objeto {@link WorkflowJsonData}.
     * <p>
     * Al guardar un Aggregator, se limpian campos que pertenecen a otros tipos de nodos
     * (como {@code taskExecutor} o {@code javaClassName}) para asegurar la consistencia del JSON.
     * </p>
     * * @param data Objeto de datos donde se volcarán los valores del formulario.
     */
    @Override
    public void saveData(WorkflowJsonData data) {
        saveCommonData(data);
        data.setCorrelationType((String) correlationTypeField.getSelectedItem());
        data.setAggregationType((String) aggregationTypeField.getSelectedItem());
        data.setReleaseType((String) releaseTypeField.getSelectedItem());

        // Limpieza de campos no aplicables a este tipo de componente
        data.setTaskExecutor(null);
        data.setSubtype(null);
        data.setJavaClassName(null);
        data.setRootElement(null);
    }
}