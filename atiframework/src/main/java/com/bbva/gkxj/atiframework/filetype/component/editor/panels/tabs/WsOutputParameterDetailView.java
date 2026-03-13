package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.AtiLabeledComponent;
import com.bbva.gkxj.atiframework.components.AtiResizableTextArea;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WsOutputParameter;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class WsOutputParameterDetailView extends JPanel {

    private AtiTextField fieldNameField;
    private AtiResizableTextArea descriptionField;
    private AtiTextField payloadPathField;

    private Runnable onChange;
    private boolean isPopulating = false;

    public WsOutputParameterDetailView(Runnable onChange) {
        this.onChange = onChange;
        // Cambiamos el layout principal a BorderLayout para que el ScrollPane ocupe todo
        setLayout(new BorderLayout());
        setOpaque(false);
        initComponents();
    }

    private void initComponents() {
        // Creamos un contenedor interno para agrupar los campos
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(JBUI.Borders.empty(15, 20));

        fieldNameField = createTextField();
        descriptionField = createResizableTextArea();
        payloadPathField = createTextField();

        content.add(new AtiLabeledComponent("Field Name", fieldNameField));
        content.add(Box.createVerticalStrut(15));

        content.add(new AtiLabeledComponent("Description", descriptionField));
        content.add(Box.createVerticalStrut(15));

        content.add(new AtiLabeledComponent("Payload Path", payloadPathField));

        // Empuja los elementos hacia arriba
        content.add(Box.createVerticalGlue());

        // Envolvemos el contenido en un JBScrollPane de IntelliJ
        JBScrollPane scrollPane = new JBScrollPane(content);
        scrollPane.setBorder(null); // Quitamos el borde para que se integre mejor
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Añadimos el scroll al centro del panel principal
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadData(WsOutputParameter data) {
        this.isPopulating = true;
        try {
            fieldNameField.setText(data.fieldName != null ? data.fieldName : "");
            descriptionField.setText(data.description != null ? data.description : "");
            payloadPathField.setText(data.payloadPath != null ? data.payloadPath : "");
        } finally {
            this.isPopulating = false;
        }
    }

    public void saveData(WsOutputParameter data) {
        data.fieldName = getNullIfEmpty(fieldNameField.getText());
        data.description = getNullIfEmpty(descriptionField.getText());
        data.payloadPath = getNullIfEmpty(payloadPathField.getText());
    }

    private AtiTextField createTextField() {
        AtiTextField field = WorkflowThemeUtils.createThemedTextField();
        field.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        return field;
    }

    private AtiResizableTextArea createResizableTextArea() {
        AtiResizableTextArea field = WorkflowThemeUtils.createThemedResizableTextArea();
        field.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        return field;
    }

    private String getNullIfEmpty(String text) {
        return (text == null || text.trim().isEmpty()) ? null : text;
    }

    private void notifyChange() {
        if (!isPopulating && onChange != null) {
            onChange.run();
        }
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }
}