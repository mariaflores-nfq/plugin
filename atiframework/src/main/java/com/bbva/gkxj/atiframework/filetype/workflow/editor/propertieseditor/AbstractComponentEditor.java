package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.components.AtiLabeledComponent;
import com.bbva.gkxj.atiframework.components.AtiResizableTextArea;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;

import static com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles.UI_HEADER_FOCUS;

/**
 * Clase base abstracta para la creación de editores de propiedades de componentes.
 * <p>
 * Proporciona la infraestructura común para construir formularios Swing,
 * utilizando el ecosistema de componentes personalizados (AtiTextField, AtiComboBox, etc.)
 * para garantizar la coherencia visual en todo el plugin.
 * </p>
 */
public abstract class AbstractComponentEditor implements ComponentEditorStrategy {

    protected JPanel contentPanel;
    protected Runnable onChangeCallback;
    protected boolean isUpdating = false;

    // --- AHORA USAMOS TUS COMPONENTES PERSONALIZADOS ---
    protected AtiTextField idField;
    protected AtiTextField componentCodeField;

    protected AtiResizableTextArea descriptionArea;
    protected JTextArea descriptionField;

    @Override
    public JPanel buildUI(Runnable onChangeCallback) {
        this.onChangeCallback = onChangeCallback;

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(WorkFlowStyles.UI_PANEL_BG);

        // Instanciamos los AtiTextField
        idField = new AtiTextField();
        componentCodeField = new AtiTextField();
        WorkflowThemeUtils.applyWorkflowTheme(idField);
        WorkflowThemeUtils.applyWorkflowTheme(componentCodeField);

        JTextArea innerTextArea = new JTextArea();
        innerTextArea.setLineWrap(true);
        innerTextArea.setWrapStyleWord(true);

        descriptionArea = new AtiResizableTextArea("Description", innerTextArea, true);
        descriptionField = descriptionArea.getTextArea();

        WorkflowThemeUtils.applyWorkflowTheme(descriptionArea);

        addChangeListener(idField);
        addChangeListener(componentCodeField);
        addChangeListener(descriptionField);

        buildSpecificUI();

        return contentPanel;
    }

    protected abstract void buildSpecificUI();

    @Override
    public void loadData(Object value) {
        if (value instanceof WorkflowJsonData) {
            loadData((WorkflowJsonData) value);
        }
    }

    @Override
    public void saveData(Object value) {
        if (value instanceof WorkflowJsonData) {
            saveData((WorkflowJsonData) value);
        }
    }

    protected void addSectionTitle(String title) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(WorkFlowStyles.UI_PANEL_BG);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titlePanel.setBorder(JBUI.Borders.empty(10, 0, 15, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        titleLabel.setForeground(WorkFlowStyles.UI_TEXT_MAIN);

        titlePanel.add(titleLabel, BorderLayout.WEST);
        contentPanel.add(titlePanel);
    }

    public abstract void loadData(WorkflowJsonData data);
    public abstract void saveData(WorkflowJsonData data);

    /**
     * Utilidad para añadir campos simples.
     * Ahora delega la creación visual a tu clase {@link AtiLabeledComponent}.
     */
    protected JPanel addFormField(String labelText, JComponent inputComponent) {
        // Configuramos el tamaño del input
        int height = 30;
        inputComponent.setPreferredSize(new Dimension(0, height));
        inputComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        // Envolvemos tu componente en el AtiLabeledComponent para que pinte la etiqueta
        AtiLabeledComponent labeledComponent = new AtiLabeledComponent(labelText, inputComponent);
        labeledComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        labeledComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, height + 25));

        // Lo metemos en un wrapper solo para darle el margen inferior de 15px
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(WorkFlowStyles.UI_PANEL_BG);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(labeledComponent);
        wrapper.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(wrapper);
        return wrapper;
    }

    /**
     * Utilidad para añadir componentes compuestos que ya tienen su propia etiqueta
     * (como AtiResizableTextArea).
     */
    protected JPanel addCompoundField(JComponent compoundComponent) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(WorkFlowStyles.UI_PANEL_BG);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        compoundComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        compoundComponent.setPreferredSize(new Dimension(0, 80));
        compoundComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        wrapper.add(compoundComponent);
        wrapper.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(wrapper);
        return wrapper;
    }

    protected void setReadOnlyStyle(JComponent field) {
        if (field instanceof JTextComponent) {
            ((JTextComponent) field).setEditable(false);
        }
        field.setBackground(WorkFlowStyles.UI_READONLY_BG);
        field.setForeground(WorkFlowStyles.UI_READONLY_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WorkFlowStyles.UI_READONLY_BORDER),
                JBUI.Borders.empty(4, 8)
        ));
    }

    protected void addChangeListener(JTextComponent field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { notifyChange(); }
            public void removeUpdate(DocumentEvent e) { notifyChange(); }
            public void changedUpdate(DocumentEvent e) { notifyChange(); }
        });
    }

    protected void addComboListener(JComboBox<?> combo) {
        combo.addActionListener(e -> notifyChange());
    }

    protected void notifyChange() {
        if (!isUpdating && onChangeCallback != null) {
            onChangeCallback.run();
        }
    }

}