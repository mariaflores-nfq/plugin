package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FieldData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static com.bbva.gkxj.atiframework.filetype.component.utils.ComponentConstants.*;

public class AdapterTabView extends JPanel {

    private AtiComboBox jmsConnectionCombo;
    private AtiTextField queueNameField;
    private JToggleButton isCriticalSwitch;
    private AtiTextField javaClassNameField;
    private AtiComboBox messageTypeCombo;
    private AtiTableSplitterPanel<FieldData> splitterPanel;
    private FieldDetailView detailView;
    private JPanel fieldsSectionContainer;

    private final String componentType;
    private final String subtype;
    private Runnable onFormChangedCallback;

    public AdapterTabView(String componentType, String subtype) {
        this.componentType = componentType;
        this.subtype = subtype;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initComponents();
        initInternalListeners();
    }

    private void initComponents() {
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setOpaque(false);
        gridPanel.setBorder(JBUI.Borders.empty(20, 20, 0, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 20, 20);

        if (SUBTYPE_ASYNC_API.equals(subtype)) {
            javaClassNameField = WorkflowThemeUtils.createThemedTextField();
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
            gridPanel.add(new AtiLabeledComponent("Java Class Name", javaClassNameField), gbc);

            if (TYPE_INPUT_ADAPTER.equals(componentType)) {
                messageTypeCombo = new AtiComboBox(new Object[]{"", "XML", "JSON"});
                WorkflowThemeUtils.applyWorkflowTheme(messageTypeCombo);
                gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
                gridPanel.add(new AtiLabeledComponent("Message Type", messageTypeCombo), gbc);
            }
        } else {
            jmsConnectionCombo = new AtiComboBox(new Object[]{"", "Operation", "Admin"});
            WorkflowThemeUtils.applyWorkflowTheme(jmsConnectionCombo);
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
            gridPanel.add(new AtiLabeledComponent("JMS Connection", jmsConnectionCombo), gbc);

            queueNameField = WorkflowThemeUtils.createThemedTextField();
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
            gridPanel.add(new AtiLabeledComponent("Queue Name", queueNameField), gbc);

            messageTypeCombo = new AtiComboBox(MESSAGE_TYPE_LIST);
            WorkflowThemeUtils.applyWorkflowTheme(messageTypeCombo);
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
            gridPanel.add(new AtiLabeledComponent("Message Type", messageTypeCombo), gbc);

            if (TYPE_INPUT_ADAPTER.equals(componentType)) {
                isCriticalSwitch = createCustomSwitch();
                JPanel swPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                swPanel.setOpaque(false);
                swPanel.add(isCriticalSwitch);
                swPanel.add(new JLabel("Is Critical"));
                gbc.gridx = 1; gbc.gridy = 1;
                gridPanel.add(swPanel, gbc);
            }
        }

        add(gridPanel, BorderLayout.NORTH);

        fieldsSectionContainer = new JPanel(new BorderLayout());
        fieldsSectionContainer.setOpaque(false);
        fieldsSectionContainer.setBorder(JBUI.Borders.empty(0, 20, 20, 20));
        fieldsSectionContainer.setVisible(false);

        detailView = new FieldDetailView(this.componentType, () -> {});
        Dimension hackSize = new Dimension(100, 500);
        detailView.setPreferredSize(hackSize);
        detailView.setMinimumSize(hackSize);

        // Actualizado a FieldData
        splitterPanel = new AtiTableSplitterPanel<>(
                "Fields", "Fields",
                FieldData::new,
                (AtiTableSplitterPanel.ItemIdExtractor<FieldData>) item -> String.format("%02d", (splitterPanel.getDataList().indexOf(item) + 1)),
                (AtiTableSplitterPanel.ItemNameExtractor<FieldData>) item -> item.fieldName != null ? item.fieldName : "New Field",
                detailView
        );

        splitterPanel.setMinimumSize(new Dimension(250, 0));
        fieldsSectionContainer.add(splitterPanel, BorderLayout.CENTER);
        add(fieldsSectionContainer, BorderLayout.CENTER);
    }

    private void initInternalListeners() {
        if (jmsConnectionCombo != null) jmsConnectionCombo.addActionListener(e -> notifyChange());
        if (isCriticalSwitch != null) isCriticalSwitch.addActionListener(e -> notifyChange());
        if (queueNameField != null) queueNameField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        if (javaClassNameField != null) javaClassNameField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
    }

    private void notifyChange() {
        if (onFormChangedCallback != null) onFormChangedCallback.run();
    }

    public String getJmsConnection() { return jmsConnectionCombo != null ? (String) jmsConnectionCombo.getSelectedItem() : ""; }
    public void setJmsConnection(String val) { if (jmsConnectionCombo != null) jmsConnectionCombo.setSelectedItem(val); }

    public String getQueueName() { return queueNameField != null ? queueNameField.getText() : ""; }
    public void setQueueName(String val) { if (queueNameField != null) queueNameField.setText(val); }

    public String getJavaClassName() { return javaClassNameField != null ? javaClassNameField.getText() : ""; }
    public void setJavaClassName(String val) { if (javaClassNameField != null) javaClassNameField.setText(val); }

    public String getMessageType() { return messageTypeCombo != null ? (String) messageTypeCombo.getSelectedItem() : ""; }
    public void setMessageType(String val) { if (messageTypeCombo != null) messageTypeCombo.setSelectedItem(val); }

    public boolean isCritical() { return isCriticalSwitch != null && isCriticalSwitch.isSelected(); }
    public void setCritical(boolean val) { if (isCriticalSwitch != null) isCriticalSwitch.setSelected(val); }

    // Cambiado a FieldData
    public List<FieldData> getTableData() { return splitterPanel != null ? splitterPanel.getDataList() : null; }
    public void setTableData(List<FieldData> data) { if (splitterPanel != null) splitterPanel.reloadData(data); }

    public FieldData getSelectedTableItem() { return splitterPanel != null ? splitterPanel.getCurrentSelection() : null; }
    public void updateSelectedRowName(String name) { if (splitterPanel != null) splitterPanel.updateSelectedRowName(name); }

    public void loadDetailData(FieldData item) { if (detailView != null) detailView.loadData(item); }
    public void saveDetailData(FieldData item) { if (detailView != null) detailView.saveData(item); }
    public void updateDetailExtractionLabel(String type) { if (detailView != null) detailView.updateExtractionLabel(type); }

    public void setOnFormChanged(Runnable callback) { this.onFormChangedCallback = callback; }
    public void setOnTableSelectionChanged(AtiTableSplitterPanel.SelectionListener<FieldData> callback) {
        if (splitterPanel != null) splitterPanel.setSelectionListener(callback);
    }
    public void setOnDetailViewChanged(Runnable callback) { if (detailView != null) detailView.setOnChange(callback); }
    public void setOnMessageTypeChanged(Consumer<String> callback) {
        if (messageTypeCombo != null) {
            messageTypeCombo.addActionListener(e -> {
                String type = (String) messageTypeCombo.getSelectedItem();
                callback.accept(type != null ? type : "");
            });
        }
    }

    public void updateFieldsContext(String type) {
        if (SUBTYPE_ASYNC_API.equals(subtype) && TYPE_OUTPUT_ADAPTER.equals(componentType)) {
            fieldsSectionContainer.setVisible(false);
            return;
        }
        boolean hasType = type != null && !type.trim().isEmpty();
        fieldsSectionContainer.setVisible(hasType);
        if (hasType && detailView != null) {
            detailView.updateExtractionLabel(type);
            try { splitterPanel.setTitle(type + " Fields"); } catch (Exception e) {}
        }
        revalidate();
        repaint();
    }

    private JToggleButton createCustomSwitch() { return new JToggleButton(); }
}