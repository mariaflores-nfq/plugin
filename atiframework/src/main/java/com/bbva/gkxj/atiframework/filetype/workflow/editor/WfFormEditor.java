package com.bbva.gkxj.atiframework.filetype.workflow.editor;

import com.bbva.gkxj.atiframework.filetype.workflow.controller.GraphController;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de {@link FileEditor} que proporciona la vista visual/formulario para el workflow.
 * <p>
 * Esta clase gestiona la sincronización bidireccional:
 * <ul>
 * <li><b>Texto a Gráfico:</b> Escucha cambios en el {@link Document} y actualiza el panel visual.</li>
 * <li><b>Gráfico a Texto:</b> Escucha cambios en el {@link GraphController} y actualiza el JSON del documento.</li>
 * </ul>
 * Utiliza un flag {@code updating} para evitar bucles infinitos de retroalimentación durante la sincronización.
 */
public class WfFormEditor extends UserDataHolderBase implements FileEditor {

    private final Project myProject;
    private final Document myDocument;
    private final VirtualFile myFile;
    private WorkflowJsonData workflowData;
    private WfEditorPanel wfEditorPanel;
    private final Gson gson;

    /** Guardián de sincronización para evitar recursión entre eventos de documento y grafo. */
    private volatile boolean updating = false;

    private final List<PropertyChangeListener> myListeners = new ArrayList<>();

    /**
     * Construye el editor de formulario.
     * * @param project Proyecto de IntelliJ asociado.
     * @param file    Archivo virtual (.wf o .json) que se está editando.
     */
    public WfFormEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myFile = file;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        createUIComponents();
        loadInitialData();
    }

    /**
     * Inicializa los componentes de la interfaz de usuario y registra los listeners de eventos.
     */
    private void createUIComponents() {
        // CORRECCIÓN: Pasamos myProject y myFile al constructor de WfEditorPanel
        this.wfEditorPanel = new WfEditorPanel(myProject, myFile);
        addFieldListeners();
    }

    /**
     * Carga los datos iniciales del documento en el formulario visual.
     */
    private void loadInitialData() {
        updateForm();
    }

    /**
     * Lee el contenido del documento JSON y actualiza el modelo visual del grafo.
     * Si ya hay una actualización en curso, la llamada se ignora.
     */
    public void updateForm() {
        if (updating) return;
        updating = true;
        try {
            if (this.myDocument != null) {
                String jsonText = this.myDocument.getText();
                this.workflowData = createWorkflowDataFromDocument(jsonText);

                if (this.workflowData != null && wfEditorPanel.getController() != null) {
                    this.wfEditorPanel.loadWorkflowData(this.workflowData);
                }
            }
        } finally {
            updating = false;
        }
    }

    /**
     * Deserializa el texto JSON en un objeto de datos de workflow.
     * * @param jsonText Cadena JSON a procesar.
     * @return Objeto {@link WorkflowJsonData} o {@code null} si el JSON es inválido o está vacío.
     */
    private WorkflowJsonData createWorkflowDataFromDocument(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) return null;
        try {
            return this.gson.fromJson(jsonText, WorkflowJsonData.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * Configura los escuchadores de eventos.
     * <ol>
     * <li>Escucha cambios en el documento de texto para refrescar el grafo.</li>
     * <li>Escucha cambios en el controlador del grafo para persistir los cambios al texto.</li>
     * </ol>
     */
    private void addFieldListeners() {
        if (myDocument != null) {
            myDocument.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull DocumentEvent event) {
                    if (!updating) {
                        updateForm();
                    }
                }
            });
        }

        wfEditorPanel.onControllerReady(controller -> {
            controller.setGraphChangeListener(() -> {
                if (!isUpdating()) {
                    // Se utiliza invokeLater para asegurar que la actualización del documento
                    // ocurra fuera del despacho de eventos de mxGraph.
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(this::updateDocument);
                }
            });
        });
    }

    /**
     * Indica si el editor se encuentra actualmente en un proceso de sincronización.
     * * @return {@code true} si hay una actualización en curso.
     */
    public boolean isUpdating() {
        return updating;
    }

    /**
     * Exporta el estado actual del grafo y sobreescribe el contenido del documento de texto.
     * <p>
     * La operación de escritura se envuelve en un {@link WriteCommandAction} para que
     * el IDE la reconozca como una transacción única (permitiendo Undo/Redo).
     * </p>
     */
    private void updateDocument() {
        if (updating || myProject.isDisposed() || myDocument == null) return;
        updating = true;
        try {
            GraphController controller = wfEditorPanel.getController();
            if (controller == null) return;

            WorkflowJsonData updatedData = controller.exportGraphToJsonData();

            if (updatedData != null) {
                String currentJson = gson.toJson(updatedData);

                // Solo escribimos si el contenido ha cambiado realmente para evitar ruido en el historial
                if (!myDocument.getText().equals(currentJson)) {
                    WriteCommandAction.runWriteCommandAction(myProject, () -> {
                        myDocument.setText(currentJson);
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Considera usar un Logger de IntelliJ aquí en el futuro
        } finally {
            updating = false;
        }
    }

    @Override
    public @NotNull JComponent getComponent() { return wfEditorPanel; }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() { return this.wfEditorPanel; }

    @Override
    public @NotNull String getName() { return "Visual Editor"; }

    @Override
    public void setState(@NotNull FileEditorState state) {}

    @Override
    public boolean isModified() { return FileDocumentManager.getInstance().isFileModified(myFile); }

    @Override
    public boolean isValid() { return myFile.isValid(); }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) { myListeners.add(listener); }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) { myListeners.remove(listener); }

    /**
     * Libera los recursos del editor cuando este se cierra.
     */
    @Override
    public void dispose() { myListeners.clear(); }

    @Override
    public @Nullable VirtualFile getFile() { return myFile; }
}