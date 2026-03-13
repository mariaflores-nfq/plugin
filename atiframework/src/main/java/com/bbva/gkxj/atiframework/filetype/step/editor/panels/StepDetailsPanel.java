package com.bbva.gkxj.atiframework.filetype.step.editor.panels;

import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.EtlStepType;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants;
import com.google.gson.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.TextFieldWithAutoCompletion;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;

import static com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants.STEP_DETAILS_PANEL_TITLE;


/**
 * Panel de edición para la configuración general del planificador.
 * <p>
 * Esta clase gestiona la interfaz que permite al usuario establecer distintos parámetros de ejecución conectando
 * bidireccionalmente los componentes de la UI con el archivo de configuración de extensión .step relativo a la planificación.
 */
public class StepDetailsPanel extends JPanel {

    /**
     * Proyecto de IntelliJ asociado al panel.
     */
    private final Project myProject;

    /**
     * Documento (texto) asociado al fichero que se edita.
     */
    private final Document myDocument;

    /**
     * Campo de texto para el planCode.
     */
    private JTextField stepCodeField = new JTextField("New Step", 15);

    /**
     * Campo de texto para la versión.
     */
    private JTextField versionField = new JTextField("V1.0.0", 10);

    /**
     * Campo de texto para el estado.
     */
    private JTextField statusField = new JTextField("NOT_PUBLISHED", 12);

    /**
     * Desplegable para seleccionar el tipo de componente .
     */
    AtiComboBox typeField = new AtiComboBox(EtlStepType.getLabels());
    /**
     * Campo de texto para la descripción.
     */
    private JTextArea descriptionField = new JTextArea(3, 40);

    /**
     * Crea un nuevo StepDetailsPanel asociado a un proyecto y un fichero.
     *
     * @param project proyecto de IntelliJ donde se está editando.
     * @param file    fichero virtual cuyo contenido JSON se edita en este panel.
     */
    public StepDetailsPanel(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        createUIComponents();
    }

    /**
     * Inicializa y configura todos los componentes visuales del panel.
     */
    private void createUIComponents() {

        setLayout(new BorderLayout());
        setBackground(SchedulerTheme.BG_MAIN);

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(),
                STEP_DETAILS_PANEL_TITLE
        );
        border.setTitleFont(SchedulerTheme.TITLE_FONT);
        border.setTitleColor(SchedulerTheme.TEXT_MAIN);

        setBorder(BorderFactory.createCompoundBorder(
                border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SchedulerTheme.BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BBVA_NAVY, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        form.add(createLabel("Step code"), c);

        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        stepCodeField.setEnabled(false);
        stepCodeField.setDisabledTextColor(Color.GRAY);
        stepCodeField.setBackground(SchedulerTheme.BG_CARD);
        applyBlueFocusBorder(stepCodeField);
        form.add(stepCodeField, c);

        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        form.add(createLabel("Type"), c);

        c.gridx = 4;
        c.gridy = 1;
        applyBlueFocusBorder(typeField);
        typeField.setBackground(SchedulerTheme.BG_CARD);
        typeField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        form.add(typeField, c);

        c.gridx = 1;
        c.gridy = 0;
        form.add(createLabel("Version"), c);

        c.gridy = 1;
        applyBlueFocusBorder(versionField);
        versionField.setBackground(SchedulerTheme.BG_CARD);
        applyDefaultText(versionField, "V1.0.0");
        form.add(versionField, c);

        c.gridx = 2;
        c.gridy = 0;
        form.add(createLabel("Status"), c);

        c.gridy = 1;
        applyBlueFocusBorder(statusField);
        statusField.setBackground(SchedulerTheme.BG_CARD);
        applyDefaultText(statusField, "NOT_PUBLISHED");
        form.add(statusField, c);

        c.gridx = 3;
        c.gridy = 0;


        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 4;
        c.anchor = GridBagConstraints.NORTH;
        form.add(createLabel("Description"), c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 5;
        c.weightx = 1.0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        descriptionField.setRows(4);
        descriptionField.setBorder(null);

        JScrollPane scroll = new JScrollPane(descriptionField);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setViewportBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SchedulerTheme.BG_CARD);
        wrapper.add(scroll, BorderLayout.CENTER);
        applyWrapperFocusBorder(wrapper, descriptionField);

        Dimension tamanoFijo = descriptionField.getPreferredScrollableViewportSize();
        int alturaFinal = tamanoFijo.height + 12;
        wrapper.setPreferredSize(new Dimension(10, alturaFinal));
        wrapper.setMinimumSize(new Dimension(10, alturaFinal));

        form.add(wrapper, c);

        c.gridy = 6;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        form.add(filler, c);

        add(form, BorderLayout.CENTER);
    }


    /**
     * Listener para abrir el popup de autocompletado al hacer clic si el campo está vacío.
     */
    private void installClickToShowListener(TextFieldWithAutoCompletion<String> field) {
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (field.getText().isEmpty()) {
                    Editor editor = field.getEditor();
                    if (editor != null) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            new CodeCompletionHandlerBase(CompletionType.BASIC)
                                    .invokeCompletion(myProject, editor);
                        });
                    }
                }
            }
        });
    }

    /**
     * Determina que control va a recibir el foco al abrir el editor.
     *
     * @return
     */
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.stepCodeField;
    }

    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado.
     *
     * @param jsonObject objeto JSON con los datos a cargar en el formulario.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (jsonObject.has("stepCode") && !jsonObject.get("stepCode").isJsonNull()) {
            stepCodeField.setText(jsonObject.get("stepCode").getAsString());
        }
        if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
            statusField.setText(jsonObject.get("status").getAsString());
        }
        if (jsonObject.has("version") && !jsonObject.get("version").isJsonNull()) {
            versionField.setText(jsonObject.get("version").getAsString());
        }
        if (jsonObject.has(StepConstants.JSON_NODE_STEP_TYPE) && !jsonObject.get(StepConstants.JSON_NODE_STEP_TYPE).isJsonNull()) {
            String jsonType = jsonObject.get(StepConstants.JSON_NODE_STEP_TYPE).getAsString();
            // Intentar mapear desde valor interno (TASKLET_*, CHUNK) o desde label
            EtlStepType et = EtlStepType.fromValue(jsonType);
            if (et == null) {
                et = EtlStepType.fromString(jsonType);
            }
            if (et != null) {
                typeField.setSelectedItem(et.getLabel());
            } else {
                typeField.setSelectedItem(jsonType);
            }
        }
        if (jsonObject.has("description") && !jsonObject.get("description").isJsonNull()) {
            descriptionField.setText(jsonObject.get("description").getAsString());
        }

    }

    /**
     * Actualiza el JsonObject con los datos actuales del formulario.
     *
     * @param jsonObject objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        jsonObject.addProperty("stepCode", stepCodeField.getText());
        jsonObject.addProperty("version", versionField.getText());
        jsonObject.addProperty("status", statusField.getText());
        // Guardamos el valor interno (TASKLET_*, CHUNK) en la propiedad stepType
        String selectedLabel = (String) typeField.getSelectedItem();
        EtlStepType et = EtlStepType.fromString(selectedLabel);
        if (et != null) {
            jsonObject.addProperty(StepConstants.JSON_NODE_STEP_TYPE, et.getValue());
        } else {
            jsonObject.addProperty(StepConstants.JSON_NODE_STEP_TYPE, selectedLabel);
        }

        jsonObject.addProperty("description", descriptionField.getText());
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(jsonObject));
        });

    }

    /**
     * Crea una etiqueta JLabel con el estilo predeterminado del panel.
     *
     * @param text texto a mostrar en la etiqueta.
     * @return JLabel configurado.
     */
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Lato", Font.BOLD, 14));
        l.setForeground(SchedulerTheme.TEXT_MAIN);
        return l;
    }

    /**
     * Aplica un borde azul al componente cuando recibe el foco.
     *
     * @param c componente al que se le aplica el comportamiento.
     */
    private void applyBlueFocusBorder(JComponent c) {

        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BORDER_SOFT, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        Border focus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BBVA_NAVY, 2),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)
        );

        c.setBorder(normal);

        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                c.setBorder(focus);
            }

            @Override
            public void focusLost(FocusEvent e) {
                c.setBorder(normal);
            }
        });

    }

    /**
     * Aplica el comportamiento de texto por defecto a un campo de texto.
     *
     * @param field       campo de texto a configurar.
     * @param defaultText texto por defecto a mostrar cuando el campo está vacío.
     */
    private void applyDefaultText(JTextField field, String defaultText) {

        field.setForeground(SchedulerTheme.TEXT_BLUE_LIGHT);

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (field.getForeground().equals(SchedulerTheme.TEXT_BLUE_LIGHT)) {
                    field.setForeground(SchedulerTheme.TEXT_MAIN);
                }
            }
        });

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(defaultText);
                    field.setForeground(SchedulerTheme.TEXT_BLUE_LIGHT);
                }
            }
        });
    }

    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     *
     * @param textListener       listener para cambios en campos de texto.
     * @param actionListener     listener para acciones (combos, botones, etc.).
     * @param dateChangeListener listener para cambios en componentes.
     */
    public void addFieldListeners(javax.swing.event.DocumentListener textListener, ActionListener actionListener, PropertyChangeListener dateChangeListener) {

        typeField.addActionListener(actionListener);
        stepCodeField.getDocument().addDocumentListener(textListener);
        versionField.getDocument().addDocumentListener(textListener);
        descriptionField.getDocument().addDocumentListener(textListener);
        statusField.getDocument().addDocumentListener(textListener);
    }


    /**
     * Aplicar correctamente el foco al campo asociado a la Descripción
     *
     * @param wrapperPanel   Elemento contenedor
     * @param innerComponent Elemento contenido en el interior de wrapperPanel
     */
    public static void applyWrapperFocusBorder(JPanel wrapperPanel, JComponent innerComponent) {
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BORDER_SOFT, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        );

        Border focus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BBVA_NAVY, 2),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)
        );

        SwingUtilities.invokeLater(() -> wrapperPanel.setBorder(normal));
        innerComponent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                wrapperPanel.setBorder(focus);
                wrapperPanel.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                wrapperPanel.setBorder(normal);
                wrapperPanel.repaint();
            }
        });
    }

    /**
     * Devuelve el tipo de interfaz Step Configuration seleccionado actualmente.
     */
    public String getSelectedType() {
        return (String) typeField.getSelectedItem();
    }

    /**
     * Permite a un componente externo escuchar cuando cambia el valor del JComboBox.
     */
    public void addTypeChangeListener(ItemListener listener) {
        typeField.addItemListener(listener);
    }

}
