package com.bbva.gkxj.atiframework.filetype.component.controller;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs.AdapterTabView;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FieldData;

import java.util.ArrayList;
import java.util.List;

public class AdapterPropertiesController {

    private final AdapterTabView view;
    private final Runnable onFormChanged;

    private boolean isPopulating = false;
    private ComponentJsonData currentModel = new ComponentJsonData();
    private String lastSelectedType = "";

    // Buffers en memoria gestionados por el controlador
    // (Dado que el nuevo modelo solo guarda la lista final)
    private List<FieldData> xmlBuffer = new ArrayList<>();
    private List<FieldData> jsonBuffer = new ArrayList<>();
    private List<FieldData> csvBuffer = new ArrayList<>();

    public AdapterPropertiesController(AdapterTabView view, Runnable onFormChanged) {
        this.view = view;
        this.onFormChanged = onFormChanged;
        initListeners();
    }

    private void initListeners() {
        view.setOnFormChanged(this::notifyChange);

        view.setOnMessageTypeChanged(newType -> {
            if (isPopulating) return;
            saveCurrentListToBuffer(lastSelectedType);
            view.updateFieldsContext(newType);
            loadBufferToTable(newType);
            lastSelectedType = newType;
            notifyChange();
        });

        view.setOnTableSelectionChanged(item -> {
            this.isPopulating = true;
            try {
                view.updateDetailExtractionLabel(view.getMessageType());
                view.loadDetailData(item);
            } finally {
                this.isPopulating = false;
            }
        });

        view.setOnDetailViewChanged(() -> {
            if (isPopulating) return;
            FieldData current = view.getSelectedTableItem();
            if (current != null) {
                view.saveDetailData(current);
                view.updateSelectedRowName(current.fieldName);
                notifyChange();
            }
        });
    }

    private void saveCurrentListToBuffer(String type) {
        if (type == null || view.getTableData() == null) return;
        switch (type) {
            case "XML" -> this.xmlBuffer = new ArrayList<>(view.getTableData());
            case "JSON" -> this.jsonBuffer = new ArrayList<>(view.getTableData());
            case "CSV" -> this.csvBuffer = new ArrayList<>(view.getTableData());
        }
    }

    private void loadBufferToTable(String type) {
        this.isPopulating = true;
        try {
            switch (type) {
                case "XML" -> view.setTableData(this.xmlBuffer);
                case "JSON" -> view.setTableData(this.jsonBuffer);
                case "CSV" -> view.setTableData(this.csvBuffer);
                default -> view.setTableData(new ArrayList<>());
            }
        } finally {
            this.isPopulating = false;
        }
    }

    /**
     * Carga el modelo unificado ComponentJsonData hacia la vista.
     */
    public void loadDataFromConfig(ComponentJsonData model) {
        if (model == null) return;
        this.currentModel = model;
        this.isPopulating = true;

        try {
            // Cargar propiedades básicas (nombres actualizados según el nuevo modelo)
            view.setJmsConnection(model.getJmsConnector()); // Nota: En el JSON es jmsConnector
            view.setQueueName(model.getQueueName());
            view.setJavaClassName(model.getAsyncApiClassName()); // En el JSON es asyncApiClassName
            view.setMessageType(model.getMessageType());
            view.setCritical(model.isCritical());

            this.lastSelectedType = model.getMessageType() != null ? model.getMessageType() : "";

            // Inyectar la lista de campos en el buffer correcto según el tipo que venía en el JSON
            List<FieldData> savedFields = model.getFieldDataList() != null ? model.getFieldDataList() : new ArrayList<>();
            switch (this.lastSelectedType) {
                case "XML" -> this.xmlBuffer = new ArrayList<>(savedFields);
                case "JSON" -> this.jsonBuffer = new ArrayList<>(savedFields);
                case "CSV" -> this.csvBuffer = new ArrayList<>(savedFields);
            }

            view.updateFieldsContext(lastSelectedType);
            loadBufferToTable(lastSelectedType);
        } finally {
            this.isPopulating = false;
        }
    }

    /**
     * Extrae los datos de la UI y los inyecta en el ComponentJsonData global.
     */
    public void updateModelFromUI(ComponentJsonData globalModel) {
        saveCurrentListToBuffer(lastSelectedType);

        // Nombres actualizados
        globalModel.setJmsConnector(view.getJmsConnection());
        globalModel.setQueueName(view.getQueueName());
        globalModel.setAsyncApiClassName(view.getJavaClassName());
        globalModel.setMessageType(view.getMessageType());
        globalModel.setCritical(view.isCritical());

        // Asignar al modelo general SOLO la lista correspondiente al tipo seleccionado
        switch (lastSelectedType) {
            case "XML" -> globalModel.setFieldDataList(new ArrayList<>(xmlBuffer));
            case "JSON" -> globalModel.setFieldDataList(new ArrayList<>(jsonBuffer));
            case "CSV" -> globalModel.setFieldDataList(new ArrayList<>(csvBuffer));
            default -> globalModel.setFieldDataList(new ArrayList<>());
        }
    }
    /**
     * Resetea todos los datos del formulario y los buffers cuando cambia el subtipo de adaptador.
     */
    public void clearAllData() {
        this.isPopulating = true; // Bloqueamos los eventos circulares
        try {
            // 1. Vaciar memoria
            this.xmlBuffer.clear();
            this.jsonBuffer.clear();
            this.csvBuffer.clear();
            this.lastSelectedType = "";

            // 2. Limpiar la UI
            view.setJmsConnection("");
            view.setQueueName("");
            view.setJavaClassName("");
            view.setMessageType("");
            view.setCritical(false);

            view.setTableData(new ArrayList<>());
            view.updateFieldsContext("");

        } finally {
            this.isPopulating = false;
            notifyChange(); // Avisamos de que hay que guardar este "reseteo"
        }
    }
    private void notifyChange() {
        if (!isPopulating && onFormChanged != null) {
            onFormChanged.run();
        }
    }
}