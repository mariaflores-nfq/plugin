package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WsScriptData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class WsScriptListPanel extends JPanel {

    private JPanel scriptListContent;
    private AtiSimpleTablePanel<WsScriptData> scriptListTable;
    private JLabel btnScriptToggle, scriptToggleLabel;
    private boolean isScriptExpanded = true;
    private boolean isPopulating = false;
    private Runnable onChangeCallback;

    private AtiTextField scriptFieldNameField;
    private AtiResizableTextArea scriptDescField;
    private AtiTextField scriptPayloadPathField;
    private AtiScriptPanel scriptEditorPanel;

    public WsScriptListPanel(Runnable onChange) {
        this.onChangeCallback = onChange;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(JBUI.Borders.empty(15, 0));
        initComponents();
    }

    private void initComponents() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        header.setOpaque(false);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnScriptToggle = new JLabel(AllIcons.General.ArrowDown);
        scriptToggleLabel = new JLabel("Script list (0)");
        scriptToggleLabel.setForeground(UIUtil.getLabelForeground());
        scriptToggleLabel.setFont(scriptToggleLabel.getFont().deriveFont(Font.BOLD, 13f));
        header.add(btnScriptToggle);
        header.add(scriptToggleLabel);

        scriptListContent = new JPanel(new BorderLayout(0, 15));
        scriptListContent.setOpaque(false);
        scriptListContent.setBorder(JBUI.Borders.emptyLeft(20));

        scriptListTable = new AtiSimpleTablePanel<>("Nº of elements", "Field Name", WsScriptData::new,
                item -> item.fieldName != null && !item.fieldName.isEmpty() ? item.fieldName : "{New Script}");
        scriptListTable.setPreferredSize(new Dimension(0, 150));
        scriptListTable.setOnChange(() -> { updateScriptListTitle(); notifyChange(); });

        scriptListTable.setSelectionListener(scriptItem -> {
            boolean wasPopulating = isPopulating;
            isPopulating = true;
            try {
                if (scriptItem != null) {
                    scriptFieldNameField.setText(scriptItem.fieldName != null ? scriptItem.fieldName : "");
                    scriptDescField.setText(scriptItem.description != null ? scriptItem.description : "");
                    scriptPayloadPathField.setText(scriptItem.payloadPath != null ? scriptItem.payloadPath : "");
                    scriptEditorPanel.setText(scriptItem.script != null ? scriptItem.script : "");
                } else {
                    scriptFieldNameField.setText("");
                    scriptDescField.setText("");
                    scriptPayloadPathField.setText("");
                    scriptEditorPanel.setText("");
                }
            } finally { isPopulating = wasPopulating; }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(true);
        form.setBackground(UIUtil.getPanelBackground());
        form.setBorder(JBUI.Borders.empty(15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.NORTHWEST;

        scriptFieldNameField = createTextField();
        scriptDescField = createResizableTextArea();
        scriptPayloadPathField = createTextField();
        scriptEditorPanel = new AtiScriptPanel();

        scriptEditorPanel.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });

        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0.5; gbc.insets = JBUI.insets(0, 0, 15, 15);
        form.add(new AtiLabeledComponent("Field Name", scriptFieldNameField), gbc);
        gbc.gridx = 1; gbc.insets = JBUI.insets(0, 0, 15, 0);
        form.add(new AtiLabeledComponent("Description", scriptDescField), gbc);

        gbc.gridy = 1; gbc.gridx = 0; gbc.insets = JBUI.insets(0, 0, 15, 15);
        form.add(new AtiLabeledComponent("Payload Path", scriptPayloadPathField), gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH; gbc.insets = JBUI.insets(0, 0, 0, 0);
        form.add(new AtiLabeledComponent("Script", scriptEditorPanel), gbc);

        scriptListContent.add(scriptListTable, BorderLayout.NORTH);
        scriptListContent.add(form, BorderLayout.CENTER);

        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                isScriptExpanded = !isScriptExpanded;
                scriptListContent.setVisible(isScriptExpanded);
                btnScriptToggle.setIcon(isScriptExpanded ? AllIcons.General.ArrowDown : AllIcons.General.ArrowRight);
            }
        });

        add(header, BorderLayout.NORTH);
        add(scriptListContent, BorderLayout.CENTER);
    }

    private void updateScriptListTitle() {
        if (scriptToggleLabel != null) scriptToggleLabel.setText("Script list (" + scriptListTable.getDataList().size() + ")");
    }

    public void loadData(List<WsScriptData> scriptList) {
        this.isPopulating = true;
        try {
            scriptListTable.reloadData(scriptList != null ? scriptList : new ArrayList<>());
            updateScriptListTitle();
        } finally { this.isPopulating = false; }
    }

    public List<WsScriptData> getData() {
        return new ArrayList<>(scriptListTable.getDataList());
    }

    private void notifyChange() {
        if (isPopulating) return;
        WsScriptData currentScript = scriptListTable.getCurrentSelection();
        if (currentScript != null) {
            currentScript.fieldName = getNullIfEmpty(scriptFieldNameField.getText());
            currentScript.description = getNullIfEmpty(scriptDescField.getText());
            currentScript.payloadPath = getNullIfEmpty(scriptPayloadPathField.getText());
            currentScript.script = getNullIfEmpty(scriptEditorPanel.getText());

            scriptListTable.refreshSelectedRow();
        }
        if (onChangeCallback != null) onChangeCallback.run();
    }

    private AtiTextField createTextField() {
        AtiTextField f = WorkflowThemeUtils.createThemedTextField();
        f.getDocument().addDocumentListener(new DocumentAdapter() { @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }});
        return f;
    }

    private AtiResizableTextArea createResizableTextArea() {
        AtiResizableTextArea f = WorkflowThemeUtils.createThemedResizableTextArea();
        f.getDocument().addDocumentListener(new DocumentAdapter() { @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }});
        return f;
    }

    private String getNullIfEmpty(String text) {
        return (text == null || text.trim().isEmpty()) ? null : text;
    }
}