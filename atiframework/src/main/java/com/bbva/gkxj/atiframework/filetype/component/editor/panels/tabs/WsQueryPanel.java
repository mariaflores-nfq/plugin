package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.components.AtiLabeledComponent;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WorkStatementData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class WsQueryPanel extends JPanel {

    private final AtiTextField queryCodeField;
    private final AtiComboBox queryTypeCombo;
    private final AtiComboBox dbSourceCombo;
    private final AtiTextField collectionField;

    public WsQueryPanel(Runnable onChange) {
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 0;

        queryCodeField = createTextField(onChange);
        queryTypeCombo = new AtiComboBox(new String[]{"Mongo Query", "SQL Query"});
        dbSourceCombo = new AtiComboBox(new String[]{"MongoDb", "SQLServer"});

        WorkflowThemeUtils.applyWorkflowTheme(queryTypeCombo);
        WorkflowThemeUtils.applyWorkflowTheme(dbSourceCombo);
        queryTypeCombo.addActionListener(e -> onChange.run());
        dbSourceCombo.addActionListener(e -> onChange.run());

        // --- FILA 0 ---
        gbc.gridy = 0; gbc.weightx = 0.33;
        gbc.gridx = 0; gbc.insets = new Insets(5, 0, 10, 15);
        add(new AtiLabeledComponent("Query Code", queryCodeField), gbc);
        gbc.gridx = 1;
        add(new AtiLabeledComponent("Query Type", queryTypeCombo), gbc);
        gbc.gridx = 2; gbc.insets = new Insets(5, 0, 10, 0);
        add(new AtiLabeledComponent("Db Source", dbSourceCombo), gbc);

        // --- FILA 1 ---
        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 3; gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 10, 0);
        collectionField = createTextField(onChange);
        JPanel mongoSpecificPanel = new JPanel(new BorderLayout());
        mongoSpecificPanel.setOpaque(false);
        mongoSpecificPanel.add(new AtiLabeledComponent("Collection", collectionField), BorderLayout.CENTER);
        add(mongoSpecificPanel, gbc);

        // --- FILA 2 (GLUE) ---
        gbc.gridy = 2; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        add(Box.createGlue(), gbc);
    }

    public void loadData(WorkStatementData data) {
        queryCodeField.setText(data.queryCode != null ? data.queryCode : "");
        // Asumiendo que determinamos el queryType base al dbSource si no viene
        String qType = "MongoDb".equals(data.dbSource) ? "Mongo Query" : "SQL Query";
        queryTypeCombo.setSelectedItem(qType);
        dbSourceCombo.setSelectedItem(data.dbSource != null ? data.dbSource : "MongoDb");
        collectionField.setText(data.collectionName != null ? data.collectionName : "");
    }

    public void saveData(WorkStatementData data) {
        data.queryCode = getNullIfEmpty(queryCodeField.getText());
        data.dbSource = (String) dbSourceCombo.getSelectedItem();
        data.collectionName = getNullIfEmpty(collectionField.getText());
    }

    private AtiTextField createTextField(Runnable onChange) {
        AtiTextField field = WorkflowThemeUtils.createThemedTextField();
        field.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { onChange.run(); }
        });
        return field;
    }

    private String getNullIfEmpty(String text) {
        return (text == null || text.trim().isEmpty()) ? null : text;
    }
}