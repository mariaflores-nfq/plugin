package com.bbva.gkxj.atiframework.filetype.component.controller;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.ComponentDetailsView;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.AdapterConfig;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WorkStatementConfig;
import com.google.gson.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;

import static com.bbva.gkxj.atiframework.filetype.component.utils.ComponentConstants.*;
import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.setComboBoxValueIgnoreCase;

/**
 * Controlador principal de los detalles del componente.
 * Orquestra el flujo de datos entre el JSON del archivo y los controladores de las pestañas específicas.
 */
public class ComponentDetailsController {

    private final Project myProject;
    private final Document myDocument;
    private final ComponentDetailsView view;
    private final Runnable onFormChanged;

    private AdapterPropertiesController adapterController;
    private FilterPropertiesController filterController;
    private WorkStatementPropertiesController wsController;

    private boolean isPopulating = false;

    public ComponentDetailsController(@NotNull Project project, @NotNull VirtualFile file, Runnable onFormChanged) {
        this.myProject = project;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        this.onFormChanged = onFormChanged;
        this.view = new ComponentDetailsView();
        setupInternalListeners();
    }

    private void setupInternalListeners() {
        DocumentAdapter commonListener = new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) {
                if (!isPopulating) onFormChanged.run();
            }
        };

        view.getComponentCodeField().getDocument().addDocumentListener(commonListener);
        view.getVersionField().getDocument().addDocumentListener(commonListener);
        view.getStatusField().getDocument().addDocumentListener(commonListener);
        view.getDescriptionField().getDocument().addDocumentListener(commonListener);

        view.getNodeTypeField().addActionListener(e -> {
            if (!isPopulating) {
                view.updateDynamicFields(getCurrentType());
                onFormChanged.run();
            }
        });

        view.getSubtypeField().addActionListener(e -> { if (!isPopulating) onFormChanged.run(); });
    }

    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;
        this.isPopulating = true;
        try {
            if (jsonObject.has("componentCode")) view.getComponentCodeField().setText(jsonObject.get("componentCode").getAsString());
            if (jsonObject.has("version")) view.getVersionField().setText(jsonObject.get("version").getAsString());
            if (jsonObject.has("status")) view.getStatusField().setText(jsonObject.get("status").getAsString());
            if (jsonObject.has("description")) view.getDescriptionField().setText(jsonObject.get("description").getAsString());

            if (jsonObject.has("type")) {
                String rawType = jsonObject.get("type").getAsString();
                String normalizedType = normalizeType(rawType);
                setComboBoxValueIgnoreCase(view.getNodeTypeField(), normalizedType);
                view.getNodeTypeField().setEnabled(false);
                view.updateDynamicFields(normalizedType);
            }

            if (jsonObject.has("subtype")) {
                setComboBoxValueIgnoreCase(view.getSubtypeField(), jsonObject.get("subtype").getAsString());
            }
        } finally {
            this.isPopulating = false;
        }
    }

    /**
     * Carga datos de Filter desde el JSON.
     */
    public void loadFilterDataFromJson(JsonObject filterJson) {
        if (filterController != null && filterJson != null) {
            ComponentJsonData.FilterConfig config = new Gson().fromJson(filterJson, ComponentJsonData.FilterConfig.class);
            filterController.loadDataFromConfig(config);
        }
    }

    /**
     * Carga datos de WorkStatement desde el JSON.
     */
    public void loadWorkStatementDataFromJson(JsonObject wsJson) {
        if (wsController != null && wsJson != null) {
            WorkStatementConfig config = new Gson().fromJson(wsJson, WorkStatementConfig.class);
            wsController.loadDataFromConfig(config);
        }
    }

    /**
     * Carga datos de Adaptadores (JMS/Async) desde el JSON.
     */
    public void loadAdapterDataFromJson(JsonObject adapterJson) {
        if (adapterController != null && adapterJson != null) {
            AdapterConfig config = new Gson().fromJson(adapterJson, AdapterConfig.class);
            adapterController.loadDataFromConfig(config);
        }
    }

    public void updateDocument(JsonObject jsonObject) {
        jsonObject.addProperty("componentCode", view.getComponentCodeField().getText());
        jsonObject.addProperty("version", view.getVersionField().getText());
        jsonObject.addProperty("status", view.getStatusField().getText());
        jsonObject.addProperty("description", view.getDescriptionField().getText());

        String type = getCurrentType();
        String subtype = getCurrentSubtype();
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("subtype", subtype);

        // Guardado de Adaptadores
        if (adapterController != null) {
            JsonElement configJson = new Gson().toJsonTree(adapterController.getDataFromUI());
            if (SUBTYPE_JMS.equals(subtype)) jsonObject.add("jmsConfig", configJson);
            else if (SUBTYPE_ASYNC_API.equals(subtype)) jsonObject.add("asyncApiConfig", configJson);
        }

        // Guardado de Filters
        if (filterController != null && TYPE_FILTER.equals(type)) {
            jsonObject.add("filterConfig", new Gson().toJsonTree(filterController.getDataFromUI()));
        }

        // Guardado de WorkStatements
        if (wsController != null && TYPE_ENRICHER.equals(type)) {
            jsonObject.add("workStatementConfig", new Gson().toJsonTree(wsController.getDataFromUI()));
        }

        // Escritura física
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WriteCommandAction.runWriteCommandAction(myProject, () -> this.myDocument.setText(gson.toJson(jsonObject)));
    }

    private String normalizeType(String rawType) {
        if (rawType == null) return "";
        if (rawType.equalsIgnoreCase("Input")) return TYPE_INPUT_ADAPTER;
        if (rawType.equalsIgnoreCase("Output")) return TYPE_OUTPUT_ADAPTER;
        return rawType;
    }

    // --- Getters y Setters ---
    public void setAdapterController(AdapterPropertiesController c) { this.adapterController = c; }
    public void setFilterController(FilterPropertiesController c) { this.filterController = c; }
    public void setWorkStatementController(WorkStatementPropertiesController c) { this.wsController = c; }

    public boolean isPopulating() { return isPopulating; }
    public String getCurrentType() { return (String) view.getNodeTypeField().getSelectedItem(); }
    public String getCurrentSubtype() { return (String) view.getSubtypeField().getSelectedItem(); }
    public JPanel getView() { return view; }
    public JComponent getPreferredFocusedComponent() { return view.getComponentCodeField(); }
}