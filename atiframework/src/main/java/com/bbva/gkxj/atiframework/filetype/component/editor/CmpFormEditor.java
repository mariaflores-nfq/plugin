package com.bbva.gkxj.atiframework.filetype.component.editor;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.ComponentBodyPanel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor de formulario nativo de IntelliJ para ficheros de componentes (.comp).
 * <p>
 * Sincroniza un formulario Swing ({@link ComponentBodyPanel}) con el contenido JSON
 * del documento de texto asociado al {@link VirtualFile}. Utiliza una bandera atómica (updating)
 * para evitar ciclos de actualización recursiva infinita entre la vista gráfica y el texto plano.
 * </p>
 */
public class CmpFormEditor extends UserDataHolderBase implements FileEditor {

    private final Project myProject;
    private final Document myDocument;
    private final VirtualFile myFile;
    private JsonObject jsonObject;
    private ComponentBodyPanel cmpMainPanel;

    /**
     * Bandera atómica para evitar actualizaciones recursivas infinitas entre
     * la escritura en el editor de texto y el repintado del formulario visual.
     */
    private volatile Boolean updating = false;

    /** Colección de oyentes registrados por la plataforma IntelliJ. */
    private final List<PropertyChangeListener> myListeners = new ArrayList<>();

    /**
     * Crea una nueva instancia del editor de formulario para el fichero dado.
     *
     * @param project El proyecto al que pertenece el fichero.
     * @param file    El fichero virtual (.comp) que se va a editar.
     */
    public CmpFormEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myFile = file;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        this.jsonObject = createJsonObjectFromDocument(this.myDocument);

        createUIComponents();
        loadInitialData();
    }

    /**
     * Indica si el editor se encuentra actualmente en un ciclo activo de sincronización de datos.
     * Importante para evitar la recursividad infinita desde eventos externos.
     *
     * @return true si se está sincronizando; false en caso contrario.
     */
    public Boolean isUpdating() {
        return updating;
    }

    /**
     * Inicializa los componentes de la interfaz de usuario.
     * Instancia el panel principal inyectando un callback (Runnable) que se ejecutará
     * cada vez que el usuario interactúe con el formulario, desencadenando la persistencia.
     */
    private void createUIComponents() {
        this.cmpMainPanel = new ComponentBodyPanel(myProject, myFile, this::updateDocument);
    }

    /**
     * Fuerza la primera carga de datos desde el documento JSON de texto hacia los campos visuales.
     */
    private void loadInitialData() {
        updateForm();
    }

    /**
     * Sincroniza el formulario Swing leyendo el contenido actual del documento de texto plano.
     * Si el sistema ya está en un ciclo de actualización, se aborta la ejecución silenciosamente.
     */
    public void updateForm() {
        if (updating) return;
        updating = true;
        try {
            this.jsonObject = createJsonObjectFromDocument(this.myDocument);
            this.cmpMainPanel.updateForm(this.jsonObject);
        } finally {
            updating = false;
        }
    }

    /**
     * Extrae el estado actual de todos los campos del formulario Swing, actualiza el objeto JSON,
     * y escribe el resultado en el documento físico del IDE.
     * Si el sistema ya está en un ciclo de actualización, se aborta la ejecución silenciosamente.
     */
    private void updateDocument() {
        if (updating) return;
        updating = true;
        try {
            this.jsonObject = createJsonObjectFromDocument(this.myDocument);
            this.cmpMainPanel.updateDocument(this.jsonObject);
        } finally {
            updating = false;
        }
    }

    /**
     * Proporciona el componente Swing raíz que IntelliJ incrustará en la pestaña del editor.
     * @return Panel principal del editor.
     */
    @Override
    public @NotNull JComponent getComponent() {
        return cmpMainPanel;
    }

    /**
     * Determina qué componente interno debe recibir el foco del teclado al abrir la pestaña.
     * @return Componente preferido para el foco.
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.cmpMainPanel.getPreferredFocusedComponent();
    }

    @Override
    public @NotNull String getName() {
        return "Component Form Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState state) { }

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

    /**
     * Construye un JsonObject a partir del texto plano contenido en el editor.
     *
     * @param document El documento de IntelliJ cuyo contenido se va a analizar.
     * @return El objeto JSON parseado, o un JsonObject vacío si la sintaxis del fichero es inválida.
     */
    private JsonObject createJsonObjectFromDocument(@NotNull Document document) {
        try {
            return JsonParser.parseString(document.getText()).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON in file: " + this.myFile.getPath() + " - " + e.getMessage());
            return new JsonObject();
        }
    }
}