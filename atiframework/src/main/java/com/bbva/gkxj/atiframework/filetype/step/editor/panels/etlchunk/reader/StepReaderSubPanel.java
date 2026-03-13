package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.reader;

import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;

/**
 * Interfaz que define los métodos que deben implementar los subpaneles de edición de steps.
 */
public interface StepReaderSubPanel {
    void updateForm(JsonObject jsonObject);
    void updateDocument(JsonObject jsonObject);
    void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener);
    JPanel getComponent();
}