package com.bbva.gkxj.atiframework.filetype.component.controller;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs.FilterTabView;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FilterMapData;

import java.util.ArrayList;

public class FilterPropertiesController {

    private final FilterTabView view;
    private final Runnable onFormChanged;

    private boolean isPopulating = false;
    private ComponentJsonData currentModel = new ComponentJsonData();

    public FilterPropertiesController(FilterTabView view, Runnable onFormChanged) {
        this.view = view;
        this.onFormChanged = onFormChanged;
        initListeners();
    }

    private void initListeners() {
        // Añadir, borrar o reordenar filas en la tabla
        view.setOnTableChangeCallback(this::notifyChange);

        // Seleccionar una fila (Cargar en el detalle)
        view.setOnTableSelectionListener(item -> {
            this.isPopulating = true;
            try {
                view.setFilterCode(item.filterCode);
                view.setScript(item.script);
            } finally {
                this.isPopulating = false;
            }
        });

        // Editar campos en el formulario derecho (Guardar en memoria)
        view.setOnFormChanged(() -> {
            if (isPopulating) return;
            FilterMapData current = view.getSelectedTableItem();
            if (current != null) {
                // El controlador guarda en el modelo temporal
                current.filterCode = getNullIfEmpty(view.getFilterCode());
                current.script = getNullIfEmpty(view.getScript());

                view.updateSelectedRowName(current.filterCode);
                notifyChange();
            }
        });
    }

    public void loadDataFromConfig(ComponentJsonData model) {
        if (model == null) return;
        this.currentModel = model;
        this.isPopulating = true;
        try {
            // El JSON refactorizado usa "filterScriptMap"
            view.setTableData(model.getFilterScriptMap() != null ? model.getFilterScriptMap() : new ArrayList<>());
        } finally {
            this.isPopulating = false;
        }
    }

    public void updateModelFromUI(ComponentJsonData globalModel) {
        globalModel.setFilterScriptMap(new ArrayList<>(view.getTableData()));
    }

    private void notifyChange() {
        if (!isPopulating && onFormChanged != null) onFormChanged.run();
    }

    private String getNullIfEmpty(String text) {
        return (text == null || text.trim().isEmpty()) ? null : text;
    }
}