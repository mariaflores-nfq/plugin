package com.bbva.gkxj.atiframework.filetype.batch.editor.panels;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel principal que contiene los subpaneles de detalles y comportamiento del batch editor.
 */
public class BatchBodyPanel extends JPanel {

    private final BatchEditorPanel batchEditorPanel;

    public BatchBodyPanel(Project project, VirtualFile virtualFile) {
        batchEditorPanel = new BatchEditorPanel(project, virtualFile);
        createUIComponents();
    }

    private void createUIComponents() {
        setLayout(new BorderLayout());
        this.add(batchEditorPanel);
    }

    /**
     * Determina que control va a recibir el foco al abrir el editor.
     *
     * @return
     */
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.batchEditorPanel.getPreferredFocusedComponent();
    }

    /**
     * Actualiza el JsonObject proporcionado con los datos del formulario.
     *
     * @param jsonObject
     */
    public void updateDocument(JsonObject jsonObject) {
        this.batchEditorPanel.updateDocument(jsonObject);
    }


    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        this.batchEditorPanel.addFieldListeners(textListener, actionListener, changeListener);
    }

    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado.
     *
     * @param jsonObject
     */
    public void updateForm(JsonObject jsonObject) {
        if (this.batchEditorPanel != null) {
            this.batchEditorPanel.updateForm(jsonObject);
        }
    }

}
