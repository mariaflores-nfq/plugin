package com.bbva.gkxj.atiframework.filetype.component.controller;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.ComponentDetailsView;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
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
            // Usamos Jackson en lugar de Gson para respetar @JsonUnwrapped
            ObjectMapper mapper = new ObjectMapper();
            // Ignorar propiedades desconocidas por si el JSON tiene basura
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            // Convertimos el JsonObject de la API de IntelliJ a String para que Jackson lo lea
            this.currentModel = mapper.readValue(jsonObject.toString(), ComponentJsonData.class);

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

            // Recuperar el subtipo
            String subtype = getCurrentSubtypeFromModel();
            if (subtype != null && !subtype.isEmpty()) {
                view.setSubtype(subtype);
            }

        } catch (Exception e) {
            e.printStackTrace(); // Útil para ver si hay algún error de parseo en la consola de IntelliJ
        } finally {
            this.isPopulating = false;
        }
    }

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
        currentModel.setComponentCode(getNullIfEmpty(view.getComponentCode()));
        currentModel.setVersion(getNullIfEmpty(view.getVersion()));
        currentModel.setStatus(getNullIfEmpty(view.getStatus()));
        currentModel.setDescription(getNullIfEmpty(view.getDescription()));

        String type = view.getNodeType();
        String subtype = view.getSubtype();
        currentModel.setNodeType(type);

        // Guardar el subtipo en la variable correcta
        if (TYPE_INPUT_ADAPTER.equals(type)) currentModel.setInputAdapterType(subtype);
        else if (TYPE_OUTPUT_ADAPTER.equals(type)) currentModel.setOutputAdapterType(subtype);

        // EXTRA: Si tienes campos de Aggregator en tu modelo, guárdalos aquí:
        /*
        if (TYPE_AGGREGATOR.equals(type)) {
            currentModel.setCorrelationType((String) view.getCorrelationStrategyField().getSelectedItem());
            currentModel.setAggregationType((String) view.getAggregationStrategyField().getSelectedItem());
            currentModel.setReleaseType((String) view.getReleaseStrategyField().getSelectedItem());
        }
        */

        // 2. Extraer datos de los controladores delegados (Pestañas)
        if (adapterController != null) currentModel = adapterController.getDataFromUI();
        if (filterController != null) currentModel = filterController.getDataFromUI();
        if (wsController != null) currentModel = wsController.getDataFromUI();

        // 3. Serializar con JACKSON y guardar en el documento
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Evitar escribir campos a null en el JSON final
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            // Formatear bonito (Pretty Print)
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            String finalJson = mapper.writeValueAsString(currentModel);

            // CORRECCIÓN: Normalizar los saltos de línea a formato Unix (\n) para IntelliJ
            String normalizedJson = StringUtil.convertLineSeparators(finalJson);

            WriteCommandAction.runWriteCommandAction(myProject, () -> {
                this.myDocument.setText(normalizedJson); // Usar el texto normalizado
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        return null;
    }

    private String getNullIfEmpty(String text) {
        return (text == null || text.trim().isEmpty()) ? null : text;
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