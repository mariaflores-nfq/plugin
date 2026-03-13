package com.bbva.gkxj.atiframework.filetype.scheduler.editor;

import com.bbva.gkxj.atiframework.filetype.scheduler.editor.panels.BodyPanel;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor de formulario para ficheros de tipo scheduler.
 *
 * Sincroniza un formulario Swing (`BodyPanel`) con el contenido JSON
 * del documento asociado al `VirtualFile`, evitando recursividad infinita mediante la marca updating.
 */
public class SchFormEditor extends UserDataHolderBase implements FileEditor {

    private final Project myProject;
    private final Document myDocument;
    private final VirtualFile myFile;
    private JsonObject jsonObject;
    private BodyPanel schMainPanel;
    /**
     * Bandera para evitar actualizaciones recursivas entre el editor
     * de texto y el editor de formulario.
     */
    private volatile Boolean updating = false;

    // Importante para sincronizar entre el editor de texto y el del formulario.
    private final List<PropertyChangeListener> myListeners = new ArrayList<>();

    /**
     * Crea una nueva instancia del editor de formulario para el fichero dado.
     *
     * @param project proyecto al que pertenece el fichero.
     * @param file    fichero virtual que se va a editar.
     */
    public SchFormEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myFile = file;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        this.jsonObject = createJsonObjectFromDocument(this.myDocument);
        createUIComponents();
        loadInitialData();
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
     * Crea e inicializa los componentes de interfaz de usuario del editor,
     * incluyendo el panel principal del formulario y sus listeners.
     */
    private void createUIComponents() {
        this.schMainPanel = new BodyPanel(myProject, myFile);
        addFieldListeners();
    }

    /**
     * Carga los datos iniciales del documento JSON en el formulario.
     */
    private void loadInitialData() {
        updateForm();
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
            this.jsonObject = createJsonObjectFromDocument(this.myDocument);
            this.schMainPanel.updateForm(this.jsonObject);
        } finally {
            updating = false;
        }

    }

    /**
     * Valida los campos de fecha del formulario.
     *
     * Si updating es true, la llamada se ignora para evitar recursividad.
     */
    private void validateDateFields() {
        if (updating) return;
        updating = true;
        try {
            this.schMainPanel.validateDateFields();
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
            this.schMainPanel.updateDocument(this.jsonObject);
            String newContent = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this.jsonObject);
            WriteCommandAction.runWriteCommandAction(myProject, () ->
                    myDocument.setText(newContent)
            );
        } finally {
            updating = false;
        }
    }

    /**
     * Devuelve el componente principal del editor.
     *
     * @return componente Swing del editor.
     */
    @Override
    public @NotNull JComponent getComponent() {
        return schMainPanel;
    }

    /**
     * Determina qué componente debe recibir el foco al abrir el editor.
     *
     * @return componente preferido para el foco.
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.schMainPanel.getPreferredFocusedComponent();
    }

    /**
     * Devuelve el nombre del editor.
     *
     * @return nombre del editor.
     */
    @Override
    public @NotNull String getName() {
        return "ATI Scheduler Form Editor";
    }

    /**
     * Establece el estado del editor.
     *
     * @param state estado del editor.
     */
    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    /**
     * Indica si el contenido del editor ha sido modificado.
     *
     * @return true si el contenido ha cambiado, false en caso contrario.
     */
    @Override
    public boolean isModified() {
        return FileDocumentManager.getInstance().isFileModified(myFile);
    }

    /**
     * Indica si el fichero virtual asociado sigue siendo válido.
     *
     * @return true si el fichero es válido, false en caso contrario.
     */
    @Override
    public boolean isValid() {
        return myFile.isValid();
    }

    /**
     * Registra un listener para cambios de propiedades del editor.
     *
     * @param propertyChangeListener listener a registrar.
     */
    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
        myListeners.add(propertyChangeListener);
    }

    /**
     * Elimina un listener previamente registrado para cambios de propiedades.
     *
     * @param propertyChangeListener listener a eliminar.
     */
    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
        myListeners.remove(propertyChangeListener);
    }

    /**
     * Libera los recursos asociados al editor.
     */
    @Override
    public void dispose() {
        myListeners.clear();
    }

    /**
     * Devuelve el VirtualFile asociado a este editor.
     *
     * @return fichero virtual o null.
     */
    @Override
    public @Nullable VirtualFile getFile() {
        return myFile;
    }

    /**
     * Notifica a todos los listeners registrados un cambio de propiedad.
     *
     * @param propertyName nombre de la propiedad que ha cambiado.
     * @param oldValue     valor anterior.
     * @param newValue     valor nuevo.
     */
    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (PropertyChangeListener listener : myListeners) {
            listener.propertyChange(event);
        }
    }

    /**
     * Construye un JsonObject a partir del contenido del documento.
     *
     * @param document documento del editor de texto cuyo contenido se va a parsear.
     * @return objeto JSON resultante o un JsonObject vacío si el contenido no es válido.
     */
    private JsonObject createJsonObjectFromDocument(@NotNull Document document) {
        try {
            if (document.getText() != null && !document.getText().trim().isEmpty()) {
                return JsonParser.parseString(document.getText()).getAsJsonObject();
            } else {
                return new JsonObject();
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON in file: " + this.myFile.getPath() + " - " + e.getMessage());
            return new JsonObject();
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
        PropertyChangeListener dateChangeListener = evt -> {
            if ("date".equals(evt.getPropertyName())) {
                validateDateFields();
                updateDocument();
            }
        };
        ActionListener actionListener = e -> {
            updateDocument();
        };
        ChangeListener changeListener = evt -> updateDocument();
        this.schMainPanel.addFieldListeners(textListener, actionListener, dateChangeListener, changeListener);
    }

}
