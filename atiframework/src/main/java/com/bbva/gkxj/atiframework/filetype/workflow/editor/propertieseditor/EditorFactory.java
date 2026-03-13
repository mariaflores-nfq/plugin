package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData.EdgeData;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;

/**
 * Factoría encargada de instanciar la estrategia de edición adecuada para cada elemento del workflow.
 * <p>
 * Esta clase centraliza la lógica de decisión para determinar qué panel de propiedades
 * debe mostrarse en función de si se ha seleccionado:
 * <ul>
 * <li>La configuración global del Workflow.</li>
 * <li>Una conexión (Edge) entre nodos.</li>
 * <li>Un nodo específico (Input, Enricher, Aggregator, etc.).</li>
 * </ul>
 * </p>
 */
public class EditorFactory {

    /**
     * Devuelve una implementación de {@link ComponentEditorStrategy} basada en el valor de la celda.
     * <p>
     * El método evalúa el tipo de objeto y, en caso de ser un nodo de workflow, analiza su
     * propiedad {@code type} para retornar un editor especializado o uno genérico.
     * </p>
     * * @param cellValue El valor contenido en la celda seleccionada (Object).
     * @param isGlobal  Indica si se solicita el editor de propiedades globales del archivo.
     * @return Una instancia de {@link ComponentEditorStrategy} o {@code null} si no hay un editor compatible.
     */
    public static ComponentEditorStrategy getEditor(Object cellValue, boolean isGlobal) {

        // 1. Prioridad: Propiedades globales del Workflow
        if (isGlobal) {
            return new GlobalWorkflowEditor();
        }

        // 2. Conexiones (Edges)
        if (cellValue instanceof EdgeData) {
            return new EdgePropertiesEditor();
        }

        // 3. Nodos del Workflow
        if (cellValue instanceof WorkflowJsonData) {
            WorkflowJsonData data = (WorkflowJsonData) cellValue;
            String type = data.getType() != null ? data.getType().toUpperCase() : "";

            switch (type) {
                case "INPUT":
                case "INPUT ADAPTER":
                    return new InputAdapterEditor();

                case "ENRICHER":
                    return new EnricherEditor();

                case "AGREGATOR":
                case "AGGREGATOR": // Soporte para errores tipográficos comunes
                    return new AgregatorEditor();

                case "SPLITTER":
                    return new SplitterEditor();

                case "SUBWORKFLOW":
                    return new ComponentEditor("SubWorkflow Details", "Workflow Code");

                case "ROUTER":
                    return new ComponentEditor("Router Details", "Component Code");

                case "FILTER":
                    return new ComponentEditor("Filter Details", "Component Code");

                case "OUTPUT":
                case "OUTPUT ADAPTER":
                    return new OutputAdapterEditor();

                default:
                    // Retorna un editor básico si el tipo no está mapeado
                    return new ComponentEditor("", "");
            }
        }

        return null;
    }
}