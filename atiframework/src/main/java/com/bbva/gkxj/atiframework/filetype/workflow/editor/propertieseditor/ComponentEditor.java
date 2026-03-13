package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;

/**
 * Editor genérico de componentes para el flujo de trabajo.
 * <p>
 * Se utiliza como implementación estándar para nodos que solo requieren la edición de
 * atributos comunes (ID y Código). Actúa como un "limpiador" de datos, ya que al guardar
 * se asegura de resetear cualquier campo específico que pudiera haber pertenecido a otro
 * tipo de componente previamente asignado a la celda.
 * </p>
 */
public class ComponentEditor extends BaseCommonEditor {

    /** Título que se mostrará en la cabecera del panel de propiedades. */
    private final String panelTitle;

    /** Etiqueta descriptiva para el campo del código técnico del componente. */
    private final String codeLabelTitle;

    /**
     * Crea una nueva instancia del editor genérico.
     * * @param panelTitle     Título para el panel (ej. "Component Properties").
     * @param codeLabelTitle Etiqueta para el campo de código (ej. "Operation Code").
     */
    public ComponentEditor(String panelTitle, String codeLabelTitle) {
        this.panelTitle = panelTitle;
        this.codeLabelTitle = codeLabelTitle;
    }

    /**
     * Construye la interfaz visual delegando exclusivamente en la estructura común
     * definida en {@link BaseCommonEditor}.
     */
    @Override
    protected void buildSpecificUI() {
        buildCommonUI(panelTitle, codeLabelTitle);
    }

    /**
     * Carga los datos comunes del workflow en los campos del editor.
     * * @param data Objeto {@link WorkflowJsonData} con la información del componente.
     */
    @Override
    public void loadData(WorkflowJsonData data) {
        loadCommonData(data);
    }

    /**
     * Persiste los datos básicos en el modelo y realiza una limpieza integral de atributos.
     * <p>
     * Esta limpieza es fundamental cuando un nodo cambia de tipo (por ejemplo, de un Aggregator
     * a un Componente simple), evitando que queden restos de configuración (como correlationType
     * o taskExecutor) en el JSON final.
     * </p>
     * * @param data Objeto {@link WorkflowJsonData} donde se guardarán los cambios.
     */
    @Override
    public void saveData(WorkflowJsonData data) {
        // Guarda ID y ComponentCode
        saveCommonData(data);

        // Resetea todos los campos específicos de otros tipos de editores
        data.setTaskExecutor(null);
        data.setCorrelationType(null);
        data.setAggregationType(null);
        data.setReleaseType(null);
        data.setSubtype(null);
        data.setJavaClassName(null);
        data.setRootElement(null);
    }
}