package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WorkStatementData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;

/**
 * Vista de detalle principal actuando como orquestador.
 * Las lógicas complejas se delegan a WsQueryPanel, WsInputParametersPanel y WsScriptListPanel.
 */
public class WorkStatementDetailView extends JPanel {

    // Controles Básicos
    private AtiTextField wsCodeField;
    private AtiResizableTextArea wsDescriptionField;
    private AtiComboBox dependencyListCombo;
    private AtiScriptPanel shouldExecuteField;
    private AtiComboBox wsTypeCombo;

    // Sub-paneles complejos delegados
    private WsInputParametersPanel inputParamsPanel;
    private WsQueryPanel queryPanel;
    private WsScriptListPanel scriptListPanel;

    private Runnable onChange;
    private boolean isPopulating = false;

    public WorkStatementDetailView(Runnable onChange) {
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setOpaque(false);
        initComponents();
    }

    private void initComponents() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(JBUI.Borders.empty(15, 20));

        // =========================================================
        // 1. FILA 1: Code y Description
        // =========================================================
        JPanel topRow = new JPanel(new GridBagLayout());
        topRow.setOpaque(false);

        wsCodeField = createTextField();
        wsDescriptionField = createResizableTextArea();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 0; gbc.weightx = 0.5; gbc.insets = JBUI.insets(10);

        gbc.gridx = 0; topRow.add(new AtiLabeledComponent("WorkStatement Code", wsCodeField), gbc);
        gbc.gridx = 1; topRow.add(new AtiLabeledComponent("Description", wsDescriptionField), gbc);
        content.add(topRow);

        // =========================================================
        // 2. DEPENDENCY & SHOULD EXECUTE
        // =========================================================
        content.add(Box.createVerticalStrut(15));
        dependencyListCombo = new AtiComboBox(new String[]{""});
        WorkflowThemeUtils.applyWorkflowTheme(dependencyListCombo);
        content.add(new AtiLabeledComponent("Dependency list", dependencyListCombo));

        content.add(Box.createVerticalStrut(15));
        shouldExecuteField = new AtiScriptPanel();
        content.add(new AtiLabeledComponent("Should be executed", shouldExecuteField));
        shouldExecuteField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });

        // =========================================================
        // 3. INPUT PARAMETERS (Panel Delegado)
        // =========================================================
        content.add(Box.createVerticalStrut(20));
        inputParamsPanel = new WsInputParametersPanel(this::notifyChange);
        content.add(inputParamsPanel);

        // =========================================================
        // 4. WORKSTATEMENT TYPE Y PANELES CONDICIONALES
        // =========================================================
        content.add(Box.createVerticalStrut(20));
        wsTypeCombo = new AtiComboBox(new String[]{"Query", "Script", "Query/Script"});
        wsTypeCombo.addActionListener(e -> { switchMainType(); notifyChange(); });
        WorkflowThemeUtils.applyWorkflowTheme(wsTypeCombo);
        content.add(new AtiLabeledComponent("WorkStatement Type", wsTypeCombo));

        content.add(Box.createVerticalStrut(15));

        queryPanel = new WsQueryPanel(this::notifyChange);
        content.add(queryPanel);

        scriptListPanel = new WsScriptListPanel(this::notifyChange);
        content.add(scriptListPanel);

        JBScrollPane scroll = new JBScrollPane(content);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        switchMainType();
    }

    private void switchMainType() {
        String type = (String) wsTypeCombo.getSelectedItem();
        boolean isQuery = "Query".equals(type) || "Query/Script".equals(type);
        boolean isScript = "Script".equals(type) || "Query/Script".equals(type);

        queryPanel.setVisible(isQuery);
        scriptListPanel.setVisible(isScript);

        revalidate();
        repaint();
    }

    public void loadData(WorkStatementData data) {
        this.isPopulating = true;
        try {
            wsCodeField.setText(data.wsCode);
            wsDescriptionField.setText(data.description);
            shouldExecuteField.setText(data.shouldBeExecuted);
            wsTypeCombo.setSelectedItem(data.wsType);

            // Carga delegada a los sub-paneles
            inputParamsPanel.loadData(data.enricherInputParameters);

            if ("Query".equals(data.wsType) || "Query/Script".equals(data.wsType)) {
                queryPanel.loadData(data);
            }
            if ("Script".equals(data.wsType) || "Query/Script".equals(data.wsType)) {
                scriptListPanel.loadData(data.enrichScriptList);
            }

            switchMainType();
        } finally {
            this.isPopulating = false;
        }
    }

    public void saveData(WorkStatementData data) {
        data.wsCode = wsCodeField.getText();
        data.description = wsDescriptionField.getText();
        data.shouldBeExecuted = shouldExecuteField.getText();
        data.wsType = (String) wsTypeCombo.getSelectedItem();

        // Extracción delegada a los sub-paneles
        data.enricherInputParameters = inputParamsPanel.getData();

        if ("Query".equals(data.wsType) || "Query/Script".equals(data.wsType)) {
            queryPanel.saveData(data);
        } else {
            // Vaciar campos si cambiamos el tipo
            data.queryCode = null; data.queryType = null; data.dbSource = null; data.collectionName = null;
        }

        if ("Script".equals(data.wsType) || "Query/Script".equals(data.wsType)) {
            data.enrichScriptList = scriptListPanel.getData();
        } else {
            data.enrichScriptList = new ArrayList<>();
        }
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

    public void setOnChange(Runnable onChange) { this.onChange = onChange; }

    private void notifyChange() {
        if (!isPopulating && onChange != null) {
            onChange.run();
        }
    }
}