package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FieldData;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FormattingConfig;
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

    // --- Componentes Gráficos (Mismos que tenías) ---
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

    private void initComponents() {
        // (Todo el código de initComponents se mantiene exactamente igual,
        // ya que solo define el diseño y no toca el modelo directamente).
        // ... (Copiado de tu clase original para abreviar)

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
    private JPanel createSectionHeader(String title) { /* Igual */ JPanel p = new JPanel(new BorderLayout()); p.setBackground(new Color(242, 242, 242)); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32)); p.setBorder(JBUI.Borders.empty(5, 10)); JLabel l = new JLabel(title); l.setFont(l.getFont().deriveFont(Font.BOLD, 12f)); l.setForeground(new Color(102, 102, 102)); p.add(l, BorderLayout.WEST); return p; }
    private AtiLabeledComponent wrapField(String label, AtiTextField field) { /* Igual */ field.getDocument().addDocumentListener(new DocumentAdapter() { @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); } }); return new AtiLabeledComponent(label, field); }

    private void updateConfigVisibility() { /* Igual, la lógica visual no cambia */
        if (configGrid == null) return;
        configGrid.removeAll();
        String type = (String) fieldTypeCombo.getSelectedItem();
        if (type == null) type = "";
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

    private void clearConfigFields() { fieldFormatField.setText(""); decimalDelimiterField.setText(""); groupingDelimiterField.setText(""); languageField.setText(""); countryField.setText(""); timeZoneField.setText(""); }
    private void updateExtractionLabel() { updateExtractionLabel(isOutput ? null : "XML"); }
    public void updateExtractionLabel(String messageType) {
        String selected = (String) extractionTypeCombo.getSelectedItem();
        if (selected == null) return;
        String labelText = selected + " *";
        if (!isOutput && !"Fixed Value".equals(selected)) {
            if ("XML".equals(messageType)) labelText = "XPath";
            else if ("JSON".equals(messageType)) labelText = "JSON Path";
            else if ("CSV".equals(messageType)) labelText = "Column Index";
        }
        for (Component child : extractionWrapper.getComponents()) { if (child instanceof JLabel) { ((JLabel) child).setText(labelText); break; } }
    }

    // =================================================================================
    // CARGAR Y GUARDAR DATOS (AQUÍ ESTÁ LA REFACTORIZACIÓN DEL OBJETO)
    // =================================================================================

    public void loadData(FieldData data) {
        this.isPopulating = true;
        try {
            // Nombres actualizados para usar BaseField heredado y las nuevas propiedades
            fieldNameField.setText(data.fieldName != null ? data.fieldName : "");
            payloadPathField.setText(data.payloadPath != null ? data.payloadPath : "");
            priorityField.setText(data.priority != null ? String.valueOf(data.priority) : "1");

            fieldTypeCombo.setSelectedItem(data.type); // En tu excel es 'type', no 'fieldType'

            // Lógica para mapear extracción (XPath, JSONPath, etc) a tu UI Combo
            String extractionVal = "";
            String comboVal = "";
            if (data.xpath != null) { extractionVal = data.xpath; comboVal = "XPath"; }
            else if (data.jsonPath != null) { extractionVal = data.jsonPath; comboVal = "JSON Path"; }
            else if (data.outputMessageFixedValue != null) { extractionVal = data.outputMessageFixedValue; comboVal = "Fixed Value"; }
            else if (data.outputMessagePath != null) { extractionVal = data.outputMessagePath; comboVal = "Payload Path"; }

            extractionTypeCombo.setSelectedItem(comboVal);
            extractionValueField.setText(extractionVal);

            // Cargar de FormattingConfig
            FormattingConfig format = data.formattingConfig;
            if (format != null) {
                fieldFormatField.setText(format.fieldFormat != null ? format.fieldFormat : "");
                decimalDelimiterField.setText(format.decimalDelimiter != null ? format.decimalDelimiter : "");
                groupingDelimiterField.setText(format.groupingDelimiter != null ? format.groupingDelimiter : "");
                languageField.setText(format.language != null ? format.language : "");
                countryField.setText(format.country != null ? format.country : "");
                timeZoneField.setText(format.timeZone != null ? format.timeZone : "");
                fieldLengthField.setText(format.fieldLength != null ? String.valueOf(format.fieldLength) : "");
                regexField.setText(format.fieldRegex != null ? format.fieldRegex : "");
            } else {
                clearConfigFields();
                fieldLengthField.setText("");
                regexField.setText("");
            }

            // Nota: En FieldData del nuevo modelo no veo propiedad 'script',
            // asegúrate de que exista si tu UI la requiere.
            // scriptField.setText(data.script != null ? data.script : "");

            updateConfigVisibility();
        } finally {
            this.isPopulating = false;
        }
    }

    public void saveData(FieldData data) {
        data.fieldName = getNullIfEmpty(fieldNameField.getText().trim());
        data.payloadPath = getNullIfEmpty(payloadPathField.getText().trim());
        try { data.priority = Integer.parseInt(priorityField.getText().trim()); } catch (Exception e) { data.priority = 1; }

        String type = (String) fieldTypeCombo.getSelectedItem();
        data.type = type;

        // Limpiamos los valores de extracción primero
        data.xpath = null; data.jsonPath = null; data.outputMessageFixedValue = null; data.outputMessagePath = null;

        String extractionType = (String) extractionTypeCombo.getSelectedItem();
        String extractionValue = getNullIfEmpty(extractionValueField.getText().trim());

        if ("XPath".equals(extractionType)) data.xpath = extractionValue;
        else if ("JSON Path".equals(extractionType)) data.jsonPath = extractionValue;
        else if ("Fixed Value".equals(extractionType)) data.outputMessageFixedValue = extractionValue;
        else if ("Payload Path".equals(extractionType)) data.outputMessagePath = extractionValue;

        // Gestionamos el objeto de formateo
        if (data.formattingConfig == null) data.formattingConfig = new FormattingConfig();
        FormattingConfig format = data.formattingConfig;

        try { format.fieldLength = Integer.parseInt(fieldLengthField.getText().trim()); }
        catch (Exception e) { format.fieldLength = null; }
        format.fieldRegex = getNullIfEmpty(regexField.getText().trim());

        boolean isNumeric = type != null && (type.contains("INTEGER") || type.contains("LONG") || type.contains("DOUBLE"));
        boolean isDate = type != null && type.contains("DATE");

        format.fieldFormat = (isNumeric || isDate) ? getNullIfEmpty(fieldFormatField.getText().trim()) : null;

        if (isNumeric) {
            format.decimalDelimiter = getNullIfEmpty(decimalDelimiterField.getText().trim());
            format.groupingDelimiter = getNullIfEmpty(groupingDelimiterField.getText().trim());
        } else {
            format.decimalDelimiter = null; format.groupingDelimiter = null;
        }

        if (isDate) {
            format.language = getNullIfEmpty(languageField.getText().trim());
            format.country = getNullIfEmpty(countryField.getText().trim());
            format.timeZone = getNullIfEmpty(timeZoneField.getText().trim());
        } else {
            format.language = null; format.country = null; format.timeZone = null;
        }
    }

    private String getNullIfEmpty(String text) { return (text == null || text.trim().isEmpty()) ? null : text; }
}