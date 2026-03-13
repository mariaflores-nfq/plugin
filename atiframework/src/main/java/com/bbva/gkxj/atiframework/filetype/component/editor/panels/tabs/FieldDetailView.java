package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

import static com.bbva.gkxj.atiframework.filetype.component.utils.ComponentConstants.EXTRACTION_TYPE_COMBO;
import static com.bbva.gkxj.atiframework.filetype.component.utils.ComponentConstants.FIELD_TYPE_LIST;

public class FieldDetailView extends JPanel {

    // --- Componentes Gráficos ---
    private AtiTextField fieldNameField, payloadPathField, priorityField, extractionValueField;
    private AtiComboBox fieldTypeCombo, extractionTypeCombo;
    private AtiScriptPanel scriptField;
    private AtiTextField fieldFormatField, decimalDelimiterField, groupingDelimiterField;
    private AtiTextField languageField, countryField, timeZoneField;
    private AtiTextField fieldLengthField, regexField;

    private JPanel configGrid;
    private AtiLabeledComponent fieldLengthWrapper, regexWrapper;
    private AtiLabeledComponent fieldFormatWrapper, decimalDelimiterWrapper, groupingDelimiterWrapper;
    private AtiLabeledComponent languageWrapper, countryWrapper, timeZoneWrapper;
    private AtiLabeledComponent extractionWrapper, payloadPathWrapper;
    private JPanel configSection;

    private Runnable onChange;
    private boolean isPopulating = false;
    private final boolean isOutput;

    public FieldDetailView(String componentType, Runnable onChange) {
        this.onChange = onChange;
        this.isOutput = componentType != null && componentType.contains("Output");
        setLayout(new BorderLayout());
        setOpaque(false);
        initComponents();
    }

    public void setOnChange(Runnable onChange) { this.onChange = onChange; }
    public void setPopulating(boolean populating) { this.isPopulating = populating; }

    private void initComponents() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // SECCIÓN 1: Datos Básicos
        JPanel topGrid = new JPanel(new GridBagLayout());
        topGrid.setOpaque(false);
        topGrid.setBorder(JBUI.Borders.empty(10, 0, 20, 0));
        GridBagConstraints topC = new GridBagConstraints();
        topC.insets = new Insets(0, 0, 15, 25);
        topC.fill = GridBagConstraints.HORIZONTAL;
        topC.anchor = GridBagConstraints.NORTHWEST;
        topC.weightx = 0.5;

        topC.gridy = 0; topC.gridx = 0;
        fieldNameField = WorkflowThemeUtils.createThemedTextField();
        topGrid.add(wrapField("Field Name", fieldNameField), topC);

        topC.gridx = 1;
        payloadPathField = WorkflowThemeUtils.createThemedTextField();
        String pathLabel = isOutput ? "JSONPath" : "Payload Path";
        payloadPathWrapper = wrapField(pathLabel, payloadPathField);
        topGrid.add(payloadPathWrapper, topC);

        topC.gridy = 1; topC.gridx = 0;
        priorityField = WorkflowThemeUtils.createThemedTextField();
        topGrid.add(wrapField("Priority", priorityField), topC);

        topC.gridx = 1;
        fieldTypeCombo = new AtiComboBox(FIELD_TYPE_LIST);
        WorkflowThemeUtils.applyWorkflowTheme(fieldTypeCombo);
        fieldTypeCombo.addActionListener(e -> {
            if (!isPopulating) clearConfigFields();
            updateConfigVisibility();
            notifyChange();
        });
        topGrid.add(new AtiLabeledComponent("Field Type", fieldTypeCombo), topC);

        mainContent.add(topGrid);

        // SECCIÓN 2: Configuración (Formatos)
        configSection = new JPanel(new BorderLayout());
        configSection.setOpaque(false);
        configSection.add(createSectionHeader("Field Configuration"), BorderLayout.NORTH);

        configGrid = new JPanel(new GridBagLayout());
        configGrid.setOpaque(false);
        configGrid.setBorder(JBUI.Borders.empty(15, 0, 0, 0));

        fieldLengthField = WorkflowThemeUtils.createThemedTextField();
        fieldLengthWrapper = wrapField("Field Length", fieldLengthField);
        regexField = WorkflowThemeUtils.createThemedTextField();
        regexWrapper = wrapField("Regular Expression", regexField);
        fieldFormatField = WorkflowThemeUtils.createThemedTextField();
        fieldFormatWrapper = wrapField("Field Format", fieldFormatField);
        decimalDelimiterField = WorkflowThemeUtils.createThemedTextField();
        decimalDelimiterWrapper = wrapField("Decimal Delimiter", decimalDelimiterField);
        groupingDelimiterField = WorkflowThemeUtils.createThemedTextField();
        groupingDelimiterWrapper = wrapField("Grouping Delimiter", groupingDelimiterField);
        languageField = WorkflowThemeUtils.createThemedTextField();
        languageWrapper = wrapField("Language", languageField);
        countryField = WorkflowThemeUtils.createThemedTextField();
        countryWrapper = wrapField("Country", countryField);
        timeZoneField = WorkflowThemeUtils.createThemedTextField();
        timeZoneWrapper = wrapField("TimeZone", timeZoneField);

        configSection.add(configGrid, BorderLayout.CENTER);
        mainContent.add(configSection);

        // SECCIÓN 3: Extraction
        JPanel extractGrid = new JPanel(new GridLayout(0, 2, 25, 15));
        extractGrid.setOpaque(false);
        extractGrid.setBorder(JBUI.Borders.empty(10, 0, 20, 25));

        Object[] extractionOptions = isOutput ? new String[]{"Payload Path", "Fixed Value"} : EXTRACTION_TYPE_COMBO;
        extractionTypeCombo = new AtiComboBox(extractionOptions);
        WorkflowThemeUtils.applyWorkflowTheme(extractionTypeCombo);
        extractionTypeCombo.addActionListener(e -> { updateExtractionLabel(); notifyChange(); });
        extractGrid.add(new AtiLabeledComponent("Extraction Type", extractionTypeCombo));

        extractionValueField = WorkflowThemeUtils.createThemedTextField();
        extractionWrapper = wrapField(isOutput ? "Payload Path" : "XPath", extractionValueField);
        extractGrid.add(extractionWrapper);
        mainContent.add(extractGrid);

        // SECCIÓN 4: Script
        mainContent.add(createSectionHeader("Script"));
        scriptField = new AtiScriptPanel();
        scriptField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        JPanel scriptWrapper = new JPanel(new BorderLayout());
        scriptWrapper.setOpaque(false);
        scriptWrapper.setBorder(JBUI.Borders.empty(10, 0, 0, 0));
        scriptWrapper.add(scriptField, BorderLayout.CENTER);
        mainContent.add(scriptWrapper);

        JBScrollPane scrollPane = new JBScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        updateConfigVisibility();
    }

    private void notifyChange() { if (!isPopulating && onChange != null) onChange.run(); }

    private JPanel createSectionHeader(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(242, 242, 242));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.setBorder(JBUI.Borders.empty(5, 10));
        JLabel l = new JLabel(title);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
        l.setForeground(new Color(102, 102, 102));
        p.add(l, BorderLayout.WEST);
        return p;
    }

    private AtiLabeledComponent wrapField(String label, AtiTextField field) {
        field.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        return new AtiLabeledComponent(label, field);
    }

    public void updateConfigVisibility() {
        if (configGrid == null) return;
        configGrid.removeAll();
        String type = getFieldTypeText();
        boolean isNumeric = type.contains("INTEGER") || type.contains("LONG") || type.contains("DOUBLE");
        boolean isDate = type.contains("DATE");
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 15, 25); c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.NORTHWEST; c.weightx = 0.5;
        int row = 0;
        c.gridy = row++; c.gridx = 0; configGrid.add(fieldLengthWrapper, c); c.gridx = 1; configGrid.add(regexWrapper, c);
        if (isNumeric || isDate) { c.gridy = row++; c.gridx = 0; configGrid.add(fieldFormatWrapper, c); }
        if (isNumeric) { c.gridy = row++; c.gridx = 0; configGrid.add(decimalDelimiterWrapper, c); c.gridx = 1; configGrid.add(groupingDelimiterWrapper, c); }
        if (isDate) { c.gridy = row++; c.gridx = 0; configGrid.add(languageWrapper, c); c.gridx = 1; configGrid.add(countryWrapper, c); c.gridy = row++; c.gridx = 0; configGrid.add(timeZoneWrapper, c); }
        configGrid.revalidate(); configGrid.repaint(); if (configSection != null) { configSection.revalidate(); configSection.repaint(); }
    }

    public void clearConfigFields() {
        fieldFormatField.setText(""); decimalDelimiterField.setText(""); groupingDelimiterField.setText("");
        languageField.setText(""); countryField.setText(""); timeZoneField.setText("");
    }

    public void updateExtractionLabel() { updateExtractionLabel(isOutput ? null : "XML"); }

    public void updateExtractionLabel(String messageType) {
        String selected = getExtractionTypeText();
        String labelText = selected + " *";
        if (!isOutput && !"Fixed Value".equals(selected)) {
            if ("XML".equals(messageType)) labelText = "XPath";
            else if ("JSON".equals(messageType)) labelText = "JSON Path";
            else if ("CSV".equals(messageType)) labelText = "Column Index";
        }
        for (Component child : extractionWrapper.getComponents()) {
            if (child instanceof JLabel) { ((JLabel) child).setText(labelText); break; }
        }
    }

    // --- Getters y Setters Simples (La "Vista Tonta") ---

    public String getFieldNameText() { return fieldNameField.getText().trim(); }
    public void setFieldNameText(String text) { fieldNameField.setText(text); }

    public String getPayloadPathText() { return payloadPathField.getText().trim(); }
    public void setPayloadPathText(String text) { payloadPathField.setText(text); }

    public String getPriorityText() { return priorityField.getText().trim(); }
    public void setPriorityText(String text) { priorityField.setText(text); }

    public String getFieldTypeText() { return fieldTypeCombo.getSelectedItem() != null ? (String) fieldTypeCombo.getSelectedItem() : ""; }
    public void setFieldTypeSelection(String type) { fieldTypeCombo.setSelectedItem(type); }

    public String getExtractionTypeText() { return extractionTypeCombo.getSelectedItem() != null ? (String) extractionTypeCombo.getSelectedItem() : ""; }
    public void setExtractionTypeSelection(String type) { extractionTypeCombo.setSelectedItem(type); }

    public String getExtractionValueText() { return extractionValueField.getText().trim(); }
    public void setExtractionValueText(String text) { extractionValueField.setText(text); }

    public String getFieldLengthText() { return fieldLengthField.getText().trim(); }
    public void setFieldLengthText(String text) { fieldLengthField.setText(text); }

    public String getRegexText() { return regexField.getText().trim(); }
    public void setRegexText(String text) { regexField.setText(text); }

    public String getFieldFormatText() { return fieldFormatField.getText().trim(); }
    public void setFieldFormatText(String text) { fieldFormatField.setText(text); }

    public String getDecimalDelimiterText() { return decimalDelimiterField.getText().trim(); }
    public void setDecimalDelimiterText(String text) { decimalDelimiterField.setText(text); }

    public String getGroupingDelimiterText() { return groupingDelimiterField.getText().trim(); }
    public void setGroupingDelimiterText(String text) { groupingDelimiterField.setText(text); }

    public String getLanguageText() { return languageField.getText().trim(); }
    public void setLanguageText(String text) { languageField.setText(text); }

    public String getCountryText() { return countryField.getText().trim(); }
    public void setCountryText(String text) { countryField.setText(text); }

    public String getTimeZoneText() { return timeZoneField.getText().trim(); }
    public void setTimeZoneText(String text) { timeZoneField.setText(text); }

    public String getScriptText() { return scriptField.getText().trim(); }
    public void setScriptText(String text) { scriptField.setText(text); }

    public boolean isOutput() { return isOutput; }
}