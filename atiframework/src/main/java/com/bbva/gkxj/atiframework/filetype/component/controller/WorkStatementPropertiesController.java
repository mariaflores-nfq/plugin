package com.bbva.gkxj.atiframework.filetype.component.controller;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs.WorkStatementTabView;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WorkStatementData;

import java.util.ArrayList;

public class WorkStatementPropertiesController {

    private final WorkStatementTabView view;
    private final Runnable onFormChanged;
    private boolean isPopulating = false;

    private ComponentJsonData currentModel = new ComponentJsonData();

    public WorkStatementPropertiesController(WorkStatementTabView view, Runnable onFormChanged) {
        this.view = view;
        this.onFormChanged = onFormChanged;
        initListeners();
    }

    private void initListeners() {
        if (view.getSplitterPanel() == null) return;

        view.getSplitterPanel().setChangeCallback(this::notifyChange);

        view.getSplitterPanel().setSelectionListener(item -> {
            this.isPopulating = true;
            try {
                view.getDetailView().loadData(item);
            } finally {
                this.isPopulating = false;
            }
        });

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

    public void loadDataFromConfig(ComponentJsonData model) {
        if (model == null) return;
        this.currentModel = model;
        this.isPopulating = true;
        try {
            // Lee directamente la lista de la raíz del JSON
            view.getSplitterPanel().reloadData(
                    model.getWorkStatementList() != null ? model.getWorkStatementList() : new ArrayList<>()
            );
        } finally {
            this.isPopulating = false;
        }
    }

    public void updateModelFromUI(ComponentJsonData globalModel) {
        if (view.getSplitterPanel() != null) {
            // Guarda directamente la lista en la raíz del JSON
            globalModel.setWorkStatementList(new ArrayList<>(view.getSplitterPanel().getDataList()));
        }
    }

    private void notifyChange() {
        if (!isPopulating && onFormChanged != null) {
            onFormChanged.run();
        }
    }
}