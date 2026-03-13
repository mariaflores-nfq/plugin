package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.components.AtiLabeledComponent;
import com.bbva.gkxj.atiframework.components.AtiResizableTextArea;
import com.bbva.gkxj.atiframework.components.AtiScriptPanel;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData.EnvironmentVariableData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel de UI que representa el editor de una variable de entorno individual.
 * <p>
 * Implementa una sincronización reactiva y utiliza el sistema de componentes Ati
 * para garantizar la uniformidad visual del formulario.
 * </p>
 */
public class EnvironmentVariableItemPanel extends JPanel {

    private final AtiTextField nameField, fieldLengthField;
    private final AtiComboBox typeCombo;
    private final AtiResizableTextArea regexArea, descArea, fixedValueArea;
    private final AtiScriptPanel scriptField;
    private final JLabel headerLabel;
    private int currentIndex;

    public EnvironmentVariableItemPanel(EnvironmentVariableData data, int index,
                                        Consumer<EnvironmentVariableItemPanel> onDelete,
                                        Runnable onChange) {
        this.currentIndex = index;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(JBColor.PanelBackground);

        setBorder(BorderFactory.createCompoundBorder(
                JBUI.Borders.emptyBottom(15),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(JBColor.border()),
                        JBUI.Borders.empty(10)
                )
        ));

        // --- Fila de Encabezado ---
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        String initialTitle = data.variableName == null || data.variableName.isEmpty() ? "nueva_variable" : data.variableName;
        headerLabel = new JLabel(String.format("%02d %s", index, initialTitle));
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headerLabel.setForeground(new JBColor(new Color(26, 115, 232), new Color(88, 157, 246)));

        JButton btnDelete = new JButton(AllIcons.General.Delete);
        btnDelete.setToolTipText("Remove this variable");
        btnDelete.setOpaque(false);
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> onDelete.accept(this));

        headerRow.add(headerLabel, BorderLayout.WEST);
        headerRow.add(btnDelete, BorderLayout.EAST);
        add(headerRow);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Inicialización de Campos con Ati Components ---
        nameField = new AtiTextField();
        WorkflowThemeUtils.applyWorkflowTheme(nameField); // <-- APLICAR TEMA
        nameField.setText(data.variableName != null ? data.variableName : "");

        typeCombo = new AtiComboBox(new String[]{"String", "Integer", "Boolean", "Object"});
        WorkflowThemeUtils.applyWorkflowTheme(typeCombo); // <-- APLICAR TEMA
        typeCombo.setSelectedItem(data.type != null ? data.type : "String");

        regexArea = createResizableArea("Field Configuration", data.regularExpression);
        WorkflowThemeUtils.applyWorkflowTheme(regexArea); // <-- APLICAR TEMA

        fieldLengthField = new AtiTextField();
        WorkflowThemeUtils.applyWorkflowTheme(fieldLengthField); // <-- APLICAR TEMA
        fieldLengthField.setText(data.fieldLength != null ? data.fieldLength : "");

        descArea = createResizableArea("Description", data.description);
        WorkflowThemeUtils.applyWorkflowTheme(descArea); // <-- APLICAR TEMA

        fixedValueArea = createResizableArea("Fixed Value", data.fixedValue);
        WorkflowThemeUtils.applyWorkflowTheme(fixedValueArea); // <-- APLICAR TEMA

        scriptField = new AtiScriptPanel();
        if (data.scriptValue != null) {
            scriptField.setText(data.scriptValue);
        }

        // --- Configuración de Listeners ---
        DocumentListener globalDocListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
        };

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateTitle() {
                String currentText = nameField.getText().trim();
                headerLabel.setText(String.format("%02d %s", currentIndex, currentText.isEmpty() ? "nueva_variable" : currentText));
                onChange.run();
            }
            @Override public void insertUpdate(DocumentEvent e) { updateTitle(); }
            @Override public void removeUpdate(DocumentEvent e) { updateTitle(); }
            @Override public void changedUpdate(DocumentEvent e) { updateTitle(); }
        });

        typeCombo.addActionListener(e -> onChange.run());
        regexArea.getDocument().addDocumentListener(globalDocListener);
        fieldLengthField.getDocument().addDocumentListener(globalDocListener);
        descArea.getDocument().addDocumentListener(globalDocListener);
        fixedValueArea.getDocument().addDocumentListener(globalDocListener);
        scriptField.getDocument().addDocumentListener(globalDocListener);

        // --- Montaje de la UI ---
        add(createWrapper(new AtiLabeledComponent("Variable Name", nameField)));
        add(createWrapper(new AtiLabeledComponent("Type of variable", typeCombo)));
        add(createWrapper(regexArea));

        // Campo sin etiqueta, solo le ponemos un tooltip para contexto
        fieldLengthField.setToolTipText("Field Length");
        add(createWrapper(fieldLengthField));

        add(createWrapper(descArea));
        add(createWrapper(fixedValueArea));

        scriptField.setPreferredSize(new Dimension(0, 200)); // Le damos altura inicial al panel de script
        add(createWrapper(new AtiLabeledComponent("Script Value", scriptField)));
    }

    public EnvironmentVariableData getData() {
        EnvironmentVariableData data = new EnvironmentVariableData();
        data.variableName = nameField.getText().trim();
        data.type = (String) typeCombo.getSelectedItem();
        data.regularExpression = regexArea.getText().trim();
        data.fieldLength = fieldLengthField.getText().trim();
        data.description = descArea.getText().trim();
        data.fixedValue = fixedValueArea.getText().trim();
        data.scriptValue = scriptField.getText().trim();
        return data;
    }

    public void updateIndex(int newIndex) {
        this.currentIndex = newIndex;
        String currentText = nameField.getText().trim();
        headerLabel.setText(String.format("%02d %s", currentIndex, currentText.isEmpty() ? "nueva_variable" : currentText));
    }

    /**
     * Utilidad para crear rápidamente un AtiResizableTextArea a partir de un texto.
     */
    private AtiResizableTextArea createResizableArea(String title, String initialText) {
        JTextArea area = new JTextArea(initialText != null ? initialText : "");
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return new AtiResizableTextArea(title, area, true);
    }

    /**
     * Envuelve los componentes para darles margen inferior y evitar que se amontonen.
     */
    private JPanel createWrapper(JComponent comp) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(comp);
        wrapper.add(Box.createRigidArea(new Dimension(0, 10)));
        return wrapper;
    }
}