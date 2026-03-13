package com.bbva.gkxj.atiframework.filetype.component.controller;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs.WorkStatementTabView;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WorkStatementConfig;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WorkStatementData;

import java.util.ArrayList;

/**
 * Controlador de propiedades para el Tab de WorkStatements (Enricher).
 * <p>
 * Orquestra la sincronización entre la lista de WorkStatements y el formulario de detalle,
 * gestionando el ciclo de vida de los datos (Carga/Guardado/Notificación).
 */
public class WorkStatementPropertiesController {

    private final WorkStatementTabView view;
    private final Runnable onFormChanged;
    private boolean isPopulating = false;

    private WorkStatementConfig currentConfig = new WorkStatementConfig();

    public WorkStatementPropertiesController(WorkStatementTabView view, Runnable onFormChanged) {
        this.view = view;
        this.onFormChanged = onFormChanged;
        initListeners();
    }

    /**
     * Conecta los eventos de selección de la tabla con la carga del detalle.
     */
    private void initListeners() {
        if (view.getSplitterPanel() == null) return;

        // Notificar cambios estructurales (add/remove)
        view.getSplitterPanel().setChangeCallback(this::notifyChange);

        // Al seleccionar un WorkStatement en la lista
        view.getSplitterPanel().setSelectionListener(item -> {
            this.isPopulating = true;
            try {
                view.getDetailView().loadData(item);
            } finally {
                this.isPopulating = false;
            }
        });

        // Al modificar cualquier campo del detalle
        view.getDetailView().setOnChange(() -> {
            if (isPopulating) return;
            WorkStatementData current = view.getSplitterPanel().getCurrentSelection();
            if (current != null) {
                view.getDetailView().saveData(current);
                view.getSplitterPanel().updateSelectedRowName(current.wsCode);
                notifyChange();
            }
        });
    }

    /**
     * Carga la configuración completa desde el modelo JSON.
     */
    public void loadDataFromConfig(WorkStatementConfig config) {
        if (config == null) return;
        this.currentConfig = config;
        this.isPopulating = true;
        try {
            view.getSplitterPanel().reloadData(
                    config.workStatements != null ? config.workStatements : new ArrayList<>()
            );
        } finally {
            this.isPopulating = false;
        }
    }

    /**
     * Extrae los datos actuales de la UI para persistencia.
     */
    public WorkStatementConfig getDataFromUI() {
        if (view.getSplitterPanel() != null) {
            currentConfig.workStatements = new ArrayList<>(view.getSplitterPanel().getDataList());
        }
        return currentConfig;
    }

    private void notifyChange() {
        if (!isPopulating && onFormChanged != null) {
            onFormChanged.run();
        }
    }
}