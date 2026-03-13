package com.bbva.gkxj.atiframework.filetype.component.controller;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs.FilterTabView;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FilterConfig;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FilterData;

import java.util.ArrayList;

/**
 * Controlador encargado de gestionar la lógica de negocio y la persistencia de la pestaña de configuración de Filters.
 * <p>
 * Actúa como mediador entre la vista unificada {@link FilterTabView} y el modelo de datos {@link FilterConfig}.
 * Se encarga de sincronizar la lista de scripts de filtrado (Splitter) con el formulario de edición,
 * gestionando la carga de datos desde el archivo y la extracción de los mismos para su guardado.
 * </p>
 */
public class FilterPropertiesController {

    /** Instancia de la vista unificada que contiene la tabla y el formulario de detalle. */
    private final FilterTabView view;

    /** Callback para notificar cambios en el formulario al orquestador principal del editor. */
    private final Runnable onFormChanged;

    /** Bandera para evitar disparar eventos de cambio durante la carga programática de datos. */
    private boolean isPopulating = false;

    /** Referencia al modelo de configuración de filtros actual. */
    private FilterConfig currentConfig = new FilterConfig();

    /**
     * Construye un nuevo controlador para las propiedades de filtrado.
     *
     * @param view          La vista de la pestaña Filter.
     * @param onFormChanged Callback de notificación de cambios.
     */
    public FilterPropertiesController(FilterTabView view, Runnable onFormChanged) {
        this.view = view;
        this.onFormChanged = onFormChanged;
        initListeners();
    }

    /**
     * Inicializa y registra los listeners de eventos para la interacción entre la tabla (Splitter)
     * y el formulario de detalle.
     * <p>
     * Gestiona tres flujos principales:
     * 1. Cambios estructurales en la lista (adición/borrado).
     * 2. Selección de un filtro para su edición.
     * 3. Sincronización en tiempo real del texto editado hacia el modelo en memoria.
     * </p>
     */
    private void initListeners() {
        if (view.getSplitterPanel() == null) return;

        // Avisar si se añade, borra o reordena un elemento en la tabla
        view.getSplitterPanel().setChangeCallback(this::notifyChange);

        // Cuando el usuario hace clic en una fila de la tabla, cargamos el objeto en el detalle
        view.getSplitterPanel().setSelectionListener(item -> {
            this.isPopulating = true;
            try {
                view.loadData(item); // Cargar datos en el formulario
            } finally {
                this.isPopulating = false;
            }
        });

        // Cuando el usuario escribe texto en el formulario de la derecha, actualizamos el modelo
        view.setOnChange(() -> {
            if (isPopulating) return;
            FilterData current = view.getSplitterPanel().getCurrentSelection();
            if (current != null) {
                view.saveData(current); // Guardar el texto en el objeto en memoria
                // Refrescar el nombre en la lista para que coincida con el Filter Code editado
                view.getSplitterPanel().updateSelectedRowName(current.filterCode);
                notifyChange();
            }
        });
    }

    /**
     * Carga la configuración de filtros desde el modelo hacia la interfaz gráfica.
     * <p>
     * Vuelca la lista de filtros almacenada en el JSON en el componente de tabla.
     * </p>
     *
     * @param config El objeto {@link FilterConfig} con la lista de scripts de filtrado.
     */
    public void loadDataFromConfig(FilterConfig config) {
        if (config == null) return;
        this.currentConfig = config;
        this.isPopulating = true;
        try {
            view.getSplitterPanel().reloadData(
                    config.filters != null ? config.filters : new ArrayList<>()
            );
        } finally {
            this.isPopulating = false;
        }
    }

    /**
     * Extrae la información actual de la interfaz gráfica para su persistencia.
     * <p>
     * Recopila todos los elementos presentes en la lista del Splitter y los consolida
     * en el objeto de configuración.
     * </p>
     *
     * @return El objeto {@link FilterConfig} actualizado con los datos de la UI.
     */
    public FilterConfig getDataFromUI() {
        if (view.getSplitterPanel() != null) {
            currentConfig.filters = new ArrayList<>(view.getSplitterPanel().getDataList());
        }
        return currentConfig;
    }

    /**
     * Notifica un cambio al sistema de persistencia general, siempre que no se
     * esté realizando una operación de carga de datos (populating).
     */
    private void notifyChange() {
        if (!isPopulating && onFormChanged != null) onFormChanged.run();
    }
}