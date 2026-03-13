package com.bbva.gkxj.atiframework.filetype.batch.editor;

import com.bbva.gkxj.atiframework.filetype.batch.editor.panels.BatchBodyPanel;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Editor de formulario para ficheros de tipo batch.
 *
 * Sincroniza un formulario Swing (`BodyPanel`) con el contenido JSON
 * del documento asociado al `VirtualFile`, evitando recursividad infinita mediante la marca updating.
 */
public class BatchFormEditor extends UserDataHolderBase implements FileEditor {

    /**
     * Proyecto
     */
    private final Project myProject;

    /**
     * Documento
     */
    private final Document myDocument;

    /**
     * Archivo
     */
    private final VirtualFile myFile;
    /**
     * Sincronizar el editor de texto y formulario.
     */
    private final List<PropertyChangeListener> myListeners = new ArrayList<>();
    /**
     * Objeto JSON
     */
    private JsonObject jsonObject;

    /**
     * Panel de edición de Batch
     */
    private BatchBodyPanel batchMainPanel;

    /**
     * Bandera para evitar actualizaciones recursivas entre el editor
     * de texto y el editor de formulario.
     */
    private volatile Boolean updating = false;

    /**
     * Constructor parametrizado
     *
     * @param project Proyecto
     * @param file    Archivo de configuración
     */
    public BatchFormEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myFile = file;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        this.jsonObject = createJsonObjectFromDocument(Objects.requireNonNull(this.myDocument));
        createUIComponents();
        addFieldListeners();
        loadInitialData();
    }

    /**
     * Inicializar elementos de la interfaz
     */
    private void createUIComponents() {
        this.batchMainPanel = new BatchBodyPanel(myProject, myFile);
    }

    /**
     * Carga los datos iniciales del documento JSON en el formulario.
     */
    private void loadInitialData() {
        updateForm();
    }


    /**
     * Importante para evitar la recursividad infinita al actualizar el formulario desde el editor de texto
     *
     * @return si está actualizando o no.
     */
    public Boolean isUpdating() {
        return updating;
    }

    /**
     * Actualiza el formulario a partir del contenido actual del documento de texto asociado.
     *
     * Si updating es true, la llamada se ignora para evitar recursividad.
     */
    public void updateForm() {
        if (updating) return;
        updating = true;
        try {
            // 1. Leemos del documento de texto
            this.jsonObject = createJsonObjectFromDocument(this.myDocument);
            // 2. Pasamos el objeto a los paneles visuales
            if (this.batchMainPanel != null) {
                this.batchMainPanel.updateForm(this.jsonObject);
            }
        } finally {
            updating = false;
        }
    }

    /**
     * Actualiza el documento JSON a partir del estado actual del formulario.
     *
     * Si updating es true, la llamada se ignora para evitar recursividad.
     */
    private void updateDocument() {
        if (updating) return;
        updating = true;
        try {
            this.jsonObject = createJsonObjectFromDocument(this.myDocument);
            this.batchMainPanel.updateDocument(this.jsonObject);
            saveJsonToDocument();
        } finally {
            updating = false;
        }
    }

    private void saveJsonToDocument() {
        if (this.jsonObject == null || this.myDocument == null) return;

        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            try {
                String jsonString = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this.jsonObject);
                this.myDocument.setText(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public @NotNull JComponent getComponent() {
        return this.batchMainPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.batchMainPanel.getPreferredFocusedComponent();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "";
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {

    }


    @Override
    public boolean isModified() {
        return FileDocumentManager.getInstance().isFileModified(myFile);
    }

    @Override
    public boolean isValid() {
        return myFile.isValid();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
        myListeners.add(propertyChangeListener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
        myListeners.remove(propertyChangeListener);
    }

    @Override
    public void dispose() {
        myListeners.clear();
    }

    @Override
    public @Nullable VirtualFile getFile() {
        return myFile;
    }

    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (PropertyChangeListener listener : myListeners) {
            listener.propertyChange(event);
        }
    }


    /**
     * Registra los listeners necesarios en los campos del formulario para
     * sincronizar los cambios con el documento JSON y validar fechas.
     */
    private void addFieldListeners() {
        javax.swing.event.DocumentListener textListener = new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateDocument();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateDocument();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateDocument();
            }
        };

        ActionListener actionListener = e -> updateDocument();
        ChangeListener changeListener = evt -> updateDocument();
        this.batchMainPanel.addFieldListeners(textListener, actionListener, changeListener);
    }

    private JsonObject createJsonObjectFromDocument(@NotNull Document document) {
        try {
            String text = document.getText();
            if (text == null || text.isEmpty()) return new JsonObject();
            String jsonClean = text.replaceAll("ObjectId\\(\"(.*?)\"\\)", "\"$1\"");
            return JsonParser.parseString(jsonClean).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new JsonObject();
        }
    }
}
