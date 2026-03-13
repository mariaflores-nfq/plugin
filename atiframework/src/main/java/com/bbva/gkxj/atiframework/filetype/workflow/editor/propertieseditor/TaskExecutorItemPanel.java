package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.components.AtiLabeledComponent;
import com.bbva.gkxj.atiframework.components.AtiResizableTextArea;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData.TaskExecutorData;
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
 * Panel de UI para la edición individual de un Task Executor.
 * <p>
 * Este componente permite configurar los parámetros de un pool de hilos de Spring,
 * utilizando el sistema de componentes Ati para mantener la coherencia visual.
 * </p>
 */
public class TaskExecutorItemPanel extends JPanel {

    private final AtiTextField codeField, prefixField, corePoolField, maxPoolField, queueField;
    private final AtiResizableTextArea descArea;
    private final JLabel headerLabel;
    private int currentIndex;

    public TaskExecutorItemPanel(TaskExecutorData data, int index,
                                 Consumer<TaskExecutorItemPanel> onDelete,
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

        // --- Configuración del Encabezado ---
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        String initialTitle = data.code == null || data.code.isEmpty() ? "New Executor" : data.code;
        headerLabel = new JLabel(String.format("%02d %s", index, initialTitle));
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headerLabel.setForeground(new JBColor(new Color(26, 115, 232), new Color(88, 157, 246)));

        JButton btnDelete = new JButton(AllIcons.General.Delete);
        btnDelete.setOpaque(false);
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> onDelete.accept(this));

        headerRow.add(headerLabel, BorderLayout.WEST);
        headerRow.add(btnDelete, BorderLayout.EAST);

        add(headerRow);
        add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Inicialización de campos con componentes Ati ---
        codeField = new AtiTextField();
        WorkflowThemeUtils.applyWorkflowTheme(codeField); // <-- APLICAR TEMA
        codeField.setText(data.code != null ? data.code : "");

        prefixField = new AtiTextField();
        WorkflowThemeUtils.applyWorkflowTheme(prefixField); // <-- APLICAR TEMA
        prefixField.setText(data.namePrefix != null ? data.namePrefix : "");

        JTextArea internalDesc = new JTextArea(data.description != null ? data.description : "");
        internalDesc.setLineWrap(true);
        internalDesc.setWrapStyleWord(true);
        descArea = new AtiResizableTextArea("Description", internalDesc, true);
        WorkflowThemeUtils.applyWorkflowTheme(descArea); // <-- APLICAR TEMA

        corePoolField = new AtiTextField();
        WorkflowThemeUtils.applyWorkflowTheme(corePoolField); // <-- APLICAR TEMA
        corePoolField.setText(data.corePoolSize != null ? data.corePoolSize : "3");

        maxPoolField = new AtiTextField();
        WorkflowThemeUtils.applyWorkflowTheme(maxPoolField); // <-- APLICAR TEMA
        maxPoolField.setText(data.maxPoolSize != null ? data.maxPoolSize : "10");

        queueField = new AtiTextField();
        WorkflowThemeUtils.applyWorkflowTheme(queueField); // <-- APLICAR TEMA
        queueField.setText(data.queueCapacity != null ? data.queueCapacity : "50");

        // Listeners
        DocumentListener globalDocListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void removeUpdate(DocumentEvent e) { onChange.run(); }
            @Override public void changedUpdate(DocumentEvent e) { onChange.run(); }
        };

        codeField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateTitle() {
                String currentText = codeField.getText().trim();
                headerLabel.setText(String.format("%02d %s", currentIndex, currentText.isEmpty() ? "New Executor" : currentText));
                onChange.run();
            }
            @Override public void insertUpdate(DocumentEvent e) { updateTitle(); }
            @Override public void removeUpdate(DocumentEvent e) { updateTitle(); }
            @Override public void changedUpdate(DocumentEvent e) { updateTitle(); }
        });

        prefixField.getDocument().addDocumentListener(globalDocListener);
        descArea.getDocument().addDocumentListener(globalDocListener);
        corePoolField.getDocument().addDocumentListener(globalDocListener);
        maxPoolField.getDocument().addDocumentListener(globalDocListener);
        queueField.getDocument().addDocumentListener(globalDocListener);

        // --- Construcción del Formulario ---
        add(createWrapper(new AtiLabeledComponent("Task Executor Code", codeField)));
        add(createWrapper(new AtiLabeledComponent("Name Prefix", prefixField)));

        // AtiResizableTextArea ya incluye su etiqueta y control de tamaño
        add(createWrapper(descArea));

        add(createWrapper(new AtiLabeledComponent("Core Pool Size", corePoolField)));
        add(createWrapper(new AtiLabeledComponent("Max Pool Size", maxPoolField)));
        add(createWrapper(new AtiLabeledComponent("Queue Capacity", queueField)));
    }

    public TaskExecutorData getData() {
        TaskExecutorData data = new TaskExecutorData();
        data.code = codeField.getText().trim();
        data.namePrefix = prefixField.getText().trim();
        data.description = descArea.getText().trim();
        data.corePoolSize = corePoolField.getText().trim();
        data.maxPoolSize = maxPoolField.getText().trim();
        data.queueCapacity = queueField.getText().trim();
        return data;
    }

    public void updateIndex(int newIndex) {
        this.currentIndex = newIndex;
        String currentText = codeField.getText().trim();
        headerLabel.setText(String.format("%02d %s", currentIndex, currentText.isEmpty() ? "New Executor" : currentText));
    }

    /**
     * Reemplaza el antiguo createFormField.
     * Simplemente asegura el margen inferior para separar los componentes Ati.
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