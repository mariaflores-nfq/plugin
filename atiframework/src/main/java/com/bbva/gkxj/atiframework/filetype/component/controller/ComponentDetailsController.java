package com.bbva.gkxj.atiframework.filetype.component.controller;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.ComponentDetailsView;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData;
import com.google.gson.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.bbva.gkxj.atiframework.filetype.component.utils.ComponentConstants.*;

public class ComponentDetailsController {

    private final Project myProject;
    private final Document myDocument;
    private final ComponentDetailsView view;
    private final Runnable onFormChanged;

    private AdapterPropertiesController adapterController;
    private FilterPropertiesController filterController;
    private WorkStatementPropertiesController wsController;

    private ComponentJsonData currentModel = new ComponentJsonData();
    private boolean isPopulating = false;

    public ComponentDetailsController(@NotNull Project project, @NotNull VirtualFile file, Runnable onFormChanged) {
        this.myProject = project;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        this.onFormChanged = onFormChanged;
        this.view = new ComponentDetailsView();

        this.view.setOnFormChanged(() -> {
            if (!isPopulating && this.onFormChanged != null) {
                this.onFormChanged.run();
            }
        });
    }

    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;
        this.isPopulating = true;
        try {
            Gson gson = new Gson();
            this.currentModel = gson.fromJson(jsonObject, ComponentJsonData.class);

            view.setComponentCode(currentModel.getComponentCode());
            view.setVersion(currentModel.getVersion());
            view.setStatus(currentModel.getStatus());
            view.setDescription(currentModel.getDescription());

            String rawType = currentModel.getNodeType();
            if (rawType != null) {
                String normalizedType = normalizeType(rawType);
                view.setNodeType(normalizedType);
                view.setNodeTypeEnabled(false);
                view.updateDynamicFields(normalizedType);
            }

            // En el nuevo modelo, los subtipos se guardan con sus propios nombres (ej. inputAdapterType)
            // Por retrocompatibilidad visual, mantenemos la lógica de la UI:
            String subtype = getCurrentSubtypeFromModel();
            if (subtype != null && !subtype.isEmpty()) {
                view.setSubtype(subtype);
            }

        } finally {
            this.isPopulating = false;
        }
    }

    // Métodos para cargar datos en los tabs delegados pasando el modelo unificado
    public void loadAdapterData() {
        if (adapterController != null) adapterController.loadDataFromConfig(currentModel);
    }

    public void loadFilterData() {
        if (filterController != null) filterController.loadDataFromConfig(currentModel);
    }

    public void loadWorkStatementData() {
        if (wsController != null) wsController.loadDataFromConfig(currentModel);
    }

    public void updateDocument(JsonObject originalJson) {
        // 1. Extraer datos básicos de la UI principal
        currentModel.setComponentCode(view.getComponentCode());
        currentModel.setVersion(view.getVersion());
        currentModel.setStatus(view.getStatus());
        currentModel.setDescription(view.getDescription());

        String type = view.getNodeType();
        String subtype = view.getSubtype();
        currentModel.setNodeType(type);

        // Guardar el subtipo en la variable correcta según el tipo de nodo
        if (TYPE_INPUT_ADAPTER.equals(type)) currentModel.setInputAdapterType(subtype);
        else if (TYPE_OUTPUT_ADAPTER.equals(type)) currentModel.setOutputAdapterType(subtype);

        // 2. Extraer datos de los controladores delegados (Pestañas)
        if (adapterController != null) currentModel = adapterController.getDataFromUI();
        if (filterController != null) currentModel = filterController.getDataFromUI();
        if (wsController != null) currentModel = wsController.getDataFromUI();

        // 3. Serializar y guardar el documento
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement updatedTree = gson.toJsonTree(currentModel);

        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(updatedTree));
        });
    }

    private String normalizeType(String rawType) {
        if (rawType == null) return "";
        if (rawType.equalsIgnoreCase("Input")) return TYPE_INPUT_ADAPTER;
        if (rawType.equalsIgnoreCase("Output")) return TYPE_OUTPUT_ADAPTER;
        return rawType;
    }

    private String getCurrentSubtypeFromModel() {
        if (TYPE_INPUT_ADAPTER.equals(currentModel.getNodeType())) return currentModel.getInputAdapterType();
        if (TYPE_OUTPUT_ADAPTER.equals(currentModel.getNodeType())) return currentModel.getOutputAdapterType();
        return null; // Añade aquí lógica para Enricher/Aggregator si la usabas
    }

    // --- Getters y Setters ---
    public void setAdapterController(AdapterPropertiesController c) { this.adapterController = c; }
    public void setFilterController(FilterPropertiesController c) { this.filterController = c; }
    public void setWorkStatementController(WorkStatementPropertiesController c) { this.wsController = c; }

    public boolean isPopulating() { return isPopulating; }
    public String getCurrentType() { return view.getNodeType(); }
    public String getCurrentSubtype() { return view.getSubtype(); }
    public JPanel getView() { return view; }
    public JComponent getPreferredFocusedComponent() { return view.getFocusTarget(); }
}