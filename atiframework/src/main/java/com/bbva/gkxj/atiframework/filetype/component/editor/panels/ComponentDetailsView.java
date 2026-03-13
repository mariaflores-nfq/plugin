package com.bbva.gkxj.atiframework.filetype.component.editor.panels;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

import static com.bbva.gkxj.atiframework.filetype.component.utils.ComponentConstants.*;

public class ComponentDetailsView extends JPanel {

    // --- Campos Principales (Privados) ---
    private final AtiTextField componentCodeField = new AtiTextField();
    private final AtiTextField versionField = new AtiTextField();
    private final AtiTextField statusField = new AtiTextField();
    private final AtiComboBox nodeTypeField = new AtiComboBox(ALL_TYPES);
    private final AtiResizableTextArea descriptionArea;

    // --- Campos Dinámicos (Privados) ---
    private final AtiComboBox subtypeField = new AtiComboBox(new Object[0]);
    private AtiLabeledComponent subtypeWrapper;

    private final AtiComboBox correlationStrategyField = new AtiComboBox(CORRELATION_STRATEGIES);
    private final AtiComboBox aggregationStrategyField = new AtiComboBox(AGGREGATION_STRATEGIES);
    private final AtiComboBox releaseStrategyField = new AtiComboBox(RELEASE_STRATEGIES);

    private JPanel dynamicPanel;
    private Runnable onFormChangedCallback;

    private static final Color COLOR_WHITE = Color.WHITE;
    private static final Color COLOR_NAVY = new Color(0, 51, 102);
    private static final Color COLOR_BORDER_LIGHT = new Color(210, 210, 210);

    public ComponentDetailsView() {
        JTextArea textArea = new JTextArea(3, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        descriptionArea = new AtiResizableTextArea("Description", textArea, true);

        applyWorkflowStyles();
        createUIComponents();
        initInternalListeners();
    }

    private void applyWorkflowStyles() {
        WorkflowThemeUtils.applyWorkflowTheme(componentCodeField);
        WorkflowThemeUtils.applyWorkflowTheme(versionField);
        WorkflowThemeUtils.applyWorkflowTheme(statusField);
        WorkflowThemeUtils.applyWorkflowTheme(nodeTypeField);
        WorkflowThemeUtils.applyWorkflowTheme(subtypeField);
        WorkflowThemeUtils.applyWorkflowTheme(correlationStrategyField);
        WorkflowThemeUtils.applyWorkflowTheme(aggregationStrategyField);
        WorkflowThemeUtils.applyWorkflowTheme(releaseStrategyField);
        WorkflowThemeUtils.applyWorkflowTheme(descriptionArea);
    }

    private void createUIComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_LIGHT, 1));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(6, 0, 0, 0, COLOR_NAVY),
                BorderFactory.createEmptyBorder(15, 20, 10, 20)
        ));

        AtiJLabel titleLabel = new AtiJLabel("Component Details");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_NAVY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        card.add(headerPanel, BorderLayout.NORTH);

        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(COLOR_WHITE);
        formContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 15, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridy = 0; c.gridx = 0; c.weightx = 1.0;
        formContainer.add(new AtiLabeledComponent("Component Code", componentCodeField), c);
        c.gridx = 1; c.weightx = 0.3;
        formContainer.add(new AtiLabeledComponent("Version", versionField), c);
        c.gridx = 2; c.weightx = 0.5;
        formContainer.add(new AtiLabeledComponent("Status", statusField), c);
        c.gridx = 3; c.weightx = 0.5;
        formContainer.add(new AtiLabeledComponent("Type", nodeTypeField), c);

        dynamicPanel = new JPanel(new GridBagLayout());
        dynamicPanel.setBackground(COLOR_WHITE);
        c.gridx = 0; c.gridy = 1; c.gridwidth = 4;
        formContainer.add(dynamicPanel, c);

        c.gridy = 2; c.gridwidth = 4;
        formContainer.add(descriptionArea, c);

        card.add(formContainer, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private void initInternalListeners() {
        DocumentAdapter docListener = new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        };

        componentCodeField.getDocument().addDocumentListener(docListener);
        versionField.getDocument().addDocumentListener(docListener);
        statusField.getDocument().addDocumentListener(docListener);
        descriptionArea.getTextArea().getDocument().addDocumentListener(docListener);

        nodeTypeField.addActionListener(e -> {
            updateDynamicFields((String) nodeTypeField.getSelectedItem());
            notifyChange();
        });
        subtypeField.addActionListener(e -> notifyChange());
        correlationStrategyField.addActionListener(e -> notifyChange());
        aggregationStrategyField.addActionListener(e -> notifyChange());
        releaseStrategyField.addActionListener(e -> notifyChange());
    }

    private void notifyChange() {
        if (onFormChangedCallback != null) onFormChangedCallback.run();
    }

    public void updateDynamicFields(String type) {
        dynamicPanel.removeAll();
        GridBagConstraints dc = new GridBagConstraints();
        dc.insets = new Insets(0, 0, 0, 20);
        dc.fill = GridBagConstraints.HORIZONTAL;
        dc.weightx = 1.0; dc.gridy = 0;

        String subtypeLabel = "Subtype";
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        switch (type != null ? type : "") {
            case TYPE_INPUT_ADAPTER:
            case TYPE_OUTPUT_ADAPTER:
                subtypeLabel = "Adapter Type";
                model.addElement(SUBTYPE_JMS);
                model.addElement(SUBTYPE_ASYNC_API);
                if (TYPE_OUTPUT_ADAPTER.equals(type)) model.addElement(SUBTYPE_DATABASE);
                break;
            case TYPE_ENRICHER:
                subtypeLabel = "Enricher Type";
                model.addElement(SUBTYPE_WORKSTATEMENT);
                break;
            case TYPE_SPLITTER:
                subtypeLabel = "Splitter Type";
                model.addElement("Java Class");
                model.addElement("Javascript");
                model.addElement("By Root Element");
                break;
            case TYPE_AGGREGATOR:
                dc.gridx = 0; dynamicPanel.add(new AtiLabeledComponent("Correlation Strategy", correlationStrategyField), dc);
                dc.gridx = 1; dynamicPanel.add(new AtiLabeledComponent("Aggregation Strategy", aggregationStrategyField), dc);
                dc.gridx = 2; dynamicPanel.add(new AtiLabeledComponent("Release Strategy", releaseStrategyField), dc);
                break;
        }

        if (!TYPE_AGGREGATOR.equals(type) && model.getSize() > 0) {
            subtypeField.setModel(model);
            subtypeWrapper = new AtiLabeledComponent(subtypeLabel, subtypeField);
            dc.gridx = 0;
            dynamicPanel.add(subtypeWrapper, dc);
        }

        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }

    // =================================================================================
    // API PÚBLICA PARA EL CONTROLADOR
    // =================================================================================

    public void setOnFormChanged(Runnable callback) { this.onFormChangedCallback = callback; }

    public String getComponentCode() { return componentCodeField.getText(); }
    public void setComponentCode(String val) { componentCodeField.setText(val != null ? val : ""); }

    public String getVersion() { return versionField.getText(); }
    public void setVersion(String val) { versionField.setText(val != null ? val : ""); }

    public String getStatus() { return statusField.getText(); }
    public void setStatus(String val) { statusField.setText(val != null ? val : ""); }

    public String getDescription() { return descriptionArea.getTextArea().getText(); }
    public void setDescription(String val) { descriptionArea.getTextArea().setText(val != null ? val : ""); }

    public String getNodeType() { return (String) nodeTypeField.getSelectedItem(); }
    public void setNodeType(String val) { nodeTypeField.setSelectedItem(val); }
    public void setNodeTypeEnabled(boolean enabled) { nodeTypeField.setEnabled(enabled); }

    public String getSubtype() { return (String) subtypeField.getSelectedItem(); }
    public void setSubtype(String val) { subtypeField.setSelectedItem(val); }

    public JComponent getFocusTarget() { return componentCodeField; }
}