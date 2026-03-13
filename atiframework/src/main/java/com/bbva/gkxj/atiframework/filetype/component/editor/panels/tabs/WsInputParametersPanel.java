package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WsInputParameter;
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

public class WsInputParametersPanel extends JPanel {

    private JPanel inputParamsContent;
    private JPanel form;
    private AtiSimpleTablePanel<WsInputParameter> inputParamsTable;
    private JLabel btnToggle, toggleLabel;
    private boolean isExpanded = true;
    private boolean isPopulating = false;
    private final Runnable onChange;

    private AtiTextField ipFieldNameField, ipPayloadPathField, ipFixedValueField, ipFormatField, ipDecimalCombo, ipGroupingCombo, ipLangField, ipCountryField, ipTzField;
    private AtiResizableTextArea ipDescField;
    private AtiComboBox ipValueTypeCombo, ipFieldTypeCombo;

    private JComponent wrapPayloadPath, wrapFixedValue, wrapFormat, wrapDecimal, wrapGrouping, wrapLang, wrapCountry, wrapTz;

    public WsInputParametersPanel(Runnable onChange) {
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(JBUI.Borders.empty(15, 0));
        initComponents();
    }

    private void initComponents() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        header.setOpaque(false);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggle = new JLabel(AllIcons.General.ArrowDown);
        toggleLabel = new JLabel("Input Parameters (0)");
        toggleLabel.setForeground(UIUtil.getLabelForeground());
        toggleLabel.setFont(toggleLabel.getFont().deriveFont(Font.BOLD, 13f));
        header.add(btnToggle);
        header.add(toggleLabel);

        inputParamsContent = new JPanel(new BorderLayout(0, 15));
        inputParamsContent.setOpaque(false);
        inputParamsContent.setBorder(JBUI.Borders.emptyLeft(20));

        inputParamsTable = new AtiSimpleTablePanel<>("Nº of elements", "Field Name", WsInputParameter::new,
                item -> item.fieldName != null && !item.fieldName.isEmpty() ? item.fieldName : "{New Parameter}");
        inputParamsTable.setPreferredSize(new Dimension(0, 150));
        inputParamsTable.setOnChange(() -> {
            toggleLabel.setText("Input Parameters (" + inputParamsTable.getDataList().size() + ")");
            notifyChange();
        });

        // Inicializar componentes UI base
        ipFieldNameField = createTextField();
        ipDescField = createResizableTextArea();
        ipPayloadPathField = createTextField();
        ipFixedValueField = createTextField();
        ipFormatField = createTextField();
        ipDecimalCombo = createTextField();
        ipGroupingCombo = createTextField();
        ipLangField = createTextField();
        ipCountryField = createTextField();
        ipTzField = createTextField();

        ipValueTypeCombo = new AtiComboBox(new String[]{"PAYLOAD_PATH", "FIXED_VALUE"});
        ipFieldTypeCombo = new AtiComboBox(new String[]{
                "STRING", "INTEGER", "LONG", "DOUBLE", "BOOLEAN", "DATE", "JSON_OBJECT",
                "A_STRING", "A_INTEGER", "A_LONG", "A_DOUBLE", "A_BOOLEAN", "A_DATE", "A_JSON_OBJECT"
        });
        WorkflowThemeUtils.applyWorkflowTheme(ipValueTypeCombo);
        WorkflowThemeUtils.applyWorkflowTheme(ipFieldTypeCombo);

        ipValueTypeCombo.addActionListener(e -> { updateVisibilityAndLayout(); notifyChange(); });
        ipFieldTypeCombo.addActionListener(e -> { updateVisibilityAndLayout(); notifyChange(); });

        wrapPayloadPath = new AtiLabeledComponent("Payload Path", ipPayloadPathField);
        wrapFixedValue = new AtiLabeledComponent("Fixed Value", ipFixedValueField);
        wrapFormat = new AtiLabeledComponent("Field Format", ipFormatField);
        wrapDecimal = new AtiLabeledComponent("Decimal Delimiter", ipDecimalCombo);
        wrapGrouping = new AtiLabeledComponent("Grouping Delimiter", ipGroupingCombo);
        wrapLang = new AtiLabeledComponent("Language", ipLangField);
        wrapCountry = new AtiLabeledComponent("Country", ipCountryField);
        wrapTz = new AtiLabeledComponent("TimeZone", ipTzField);

        // Contenedor del formulario
        form = new JPanel(new GridBagLayout());
        form.setOpaque(true);
        form.setBackground(UIUtil.getPanelBackground());
        form.setBorder(JBUI.Borders.empty(15));

        // Primera construcción del layout
        buildFormLayout();

        inputParamsTable.setSelectionListener(item -> {
            boolean wasPopulating = isPopulating;
            isPopulating = true;
            try {
                if (item != null) {
                    ipFieldNameField.setText(item.fieldName);
                    ipDescField.setText(item.description);
                    ipValueTypeCombo.setSelectedItem(item.inputValueType != null ? item.inputValueType : "PAYLOAD_PATH");
                    ipPayloadPathField.setText(item.payloadPath);
                    ipFixedValueField.setText(item.extractionValue);
                    ipFieldTypeCombo.setSelectedItem(item.fieldType != null ? item.fieldType : "STRING");
                    ipFormatField.setText(item.fieldFormat);
                    ipDecimalCombo.setText(item.decimalDelimiter);
                    ipGroupingCombo.setText(item.groupingDelimiter);
                    ipLangField.setText(item.language);
                    ipCountryField.setText(item.country);
                    ipTzField.setText(item.timeZone);
                } else {
                    ipFieldNameField.setText(""); ipDescField.setText("");
                    ipPayloadPathField.setText(""); ipFixedValueField.setText("");
                    ipFormatField.setText(""); ipDecimalCombo.setText(""); ipGroupingCombo.setText("");
                    ipLangField.setText(""); ipCountryField.setText(""); ipTzField.setText("");
                }
                updateVisibilityAndLayout();
            } finally { isPopulating = wasPopulating; }
        });

        inputParamsContent.add(inputParamsTable, BorderLayout.NORTH);
        inputParamsContent.add(form, BorderLayout.CENTER);

        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                isExpanded = !isExpanded;
                inputParamsContent.setVisible(isExpanded);
                btnToggle.setIcon(isExpanded ? AllIcons.General.ArrowDown : AllIcons.General.ArrowRight);
            }
        });

        add(header, BorderLayout.NORTH);
        add(inputParamsContent, BorderLayout.CENTER);
    }

    /**
     * Reconstruye la cuadrícula (GridBagLayout) ignorando los componentes que no están visibles,
     * para que todo quede apilado limpiamente sin huecos.
     */
    private void buildFormLayout() {
        form.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0.5;
        gbc.weighty = 0;
        gbc.insets = JBUI.insets(0, 0, 10, 15);

        int row = 0;
        int col = 0;

        // Fila 1 siempre fija
        addComponentToGrid(new AtiLabeledComponent("Field Name", ipFieldNameField), gbc, col++, row);
        gbc.insets = JBUI.insets(0, 0, 10, 0); // Quitar margen derecho para el segundo
        addComponentToGrid(new AtiLabeledComponent("Description", ipDescField), gbc, col++, row);

        row++; col = 0; gbc.insets = JBUI.insets(0, 0, 10, 15);

        // Fila 2 siempre fija
        addComponentToGrid(new AtiLabeledComponent("Input Value Type", ipValueTypeCombo), gbc, col++, row);
        gbc.insets = JBUI.insets(0, 0, 10, 0);
        addComponentToGrid(new AtiLabeledComponent("Field Type", ipFieldTypeCombo), gbc, col++, row);

        row++; col = 0;

        // --- Lista de componentes dinámicos a evaluar ---
        List<JComponent> visibleComponents = new ArrayList<>();

        if (wrapPayloadPath.isVisible()) visibleComponents.add(wrapPayloadPath);
        if (wrapFixedValue.isVisible()) visibleComponents.add(wrapFixedValue);
        if (wrapFormat.isVisible()) visibleComponents.add(wrapFormat);
        if (wrapDecimal.isVisible()) visibleComponents.add(wrapDecimal);
        if (wrapGrouping.isVisible()) visibleComponents.add(wrapGrouping);
        if (wrapLang.isVisible()) visibleComponents.add(wrapLang);
        if (wrapCountry.isVisible()) visibleComponents.add(wrapCountry);
        if (wrapTz.isVisible()) visibleComponents.add(wrapTz);

        // Distribuir dinámicamente en 2 columnas
        for (JComponent comp : visibleComponents) {
            gbc.insets = col == 0 ? JBUI.insets(0, 0, 10, 15) : JBUI.insets(0, 0, 10, 0);
            addComponentToGrid(comp, gbc, col, row);

            col++;
            if (col > 1) { // Salto de línea si llegamos a la segunda columna
                col = 0;
                row++;
            }
        }

        // Truco para evitar que los campos se estiren verticalmente y empujen hacia el centro
        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(Box.createGlue(), gbc);

        form.revalidate();
        form.repaint();
    }

    private void addComponentToGrid(JComponent comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        form.add(comp, gbc);
    }

    private void updateVisibilityAndLayout() {
        String valType = (String) ipValueTypeCombo.getSelectedItem();
        String fieldType = (String) ipFieldTypeCombo.getSelectedItem();
        if (fieldType == null) fieldType = "";

        boolean isPayload = "PAYLOAD_PATH".equals(valType);
        boolean isNumeric = fieldType.contains("INTEGER") || fieldType.contains("LONG") || fieldType.contains("DOUBLE");
        boolean isDate = fieldType.contains("DATE");


        wrapPayloadPath.setVisible(isPayload);
        wrapFixedValue.setVisible(!isPayload);
        wrapFormat.setVisible(isNumeric || isDate);
        wrapDecimal.setVisible(isNumeric);
        wrapGrouping.setVisible(isNumeric);
        wrapLang.setVisible(isDate);
        wrapCountry.setVisible(isDate);
        wrapTz.setVisible(isDate);

        buildFormLayout();
    }

    public void loadData(List<WsInputParameter> data) {
        this.isPopulating = true;
        try {
            inputParamsTable.reloadData(data != null ? data : new ArrayList<>());
            toggleLabel.setText("Input Parameters (" + inputParamsTable.getDataList().size() + ")");
        } finally { this.isPopulating = false; }
    }

    public List<WsInputParameter> getData() {
        return new ArrayList<>(inputParamsTable.getDataList());
    }

    private void notifyChange() {
        if (isPopulating) return;
        WsInputParameter current = inputParamsTable.getCurrentSelection();
        if (current != null) {
            current.fieldName = ipFieldNameField.getText();
            current.description = ipDescField.getText();
            current.inputValueType = (String) ipValueTypeCombo.getSelectedItem();
            current.payloadPath = ipPayloadPathField.getText();
            current.extractionValue = ipFixedValueField.getText();
            current.fieldType = (String) ipFieldTypeCombo.getSelectedItem();

            current.fieldFormat = wrapFormat.isVisible() ? ipFormatField.getText() : null;
            current.decimalDelimiter = wrapDecimal.isVisible() ? ipDecimalCombo.getText() : null;
            current.groupingDelimiter = wrapGrouping.isVisible() ? ipGroupingCombo.getText() : null;
            current.language = wrapLang.isVisible() ? ipLangField.getText() : null;
            current.country = wrapCountry.isVisible() ? ipCountryField.getText() : null;
            current.timeZone = wrapTz.isVisible() ? ipTzField.getText() : null;

            inputParamsTable.refreshSelectedRow();
        }
        if (onChange != null) onChange.run();
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
}