package com.bbva.gkxj.atiframework.filetype.scheduler.editor.panels;

import com.bbva.gkxj.atiframework.filetype.scheduler.editor.IntelliJCompatibleDateChooser;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
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
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.TextFieldWithAutoCompletion;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Panel de edición para la configuración general del planificador.
 *
 * Esta clase gestiona la interfaz que permite al usuario establecer distintos parámetros de ejecución conectando
 * bidireccionalmente los componentes de la UI con el archivo de configuración de extensión .sch relativo a la planificación.
 */
public class SchedulerDetailsPanel extends JPanel {

    /** Proyecto de IntelliJ asociado al panel. */
    private final Project myProject;

    /** Documento (texto) asociado al fichero que se edita. */
    private final Document myDocument;

    /** Tamaño de los calendarios. */
    private static final Dimension CALENDAR_FIELD_SIZE = new Dimension(150, 35);

    /** Campo de texto para el planCode. */
    private JTextField planCodeField = new JTextField("New Plan", 15);

    /** Campo de texto para la versión. */
    private JTextField versionField = new JTextField("V1.0.0", 10);

    /** Campo de texto para el estado. */
    private JTextField statusField = new JTextField("NOT_PUBLISHED", 12);

    /** Selector de fecha de inicio. */
    private IntelliJCompatibleDateChooser validFromField = new IntelliJCompatibleDateChooser();

    /** Selector de fecha de fin. */
    private IntelliJCompatibleDateChooser validToField = new IntelliJCompatibleDateChooser();

    /** Campo de texto para la descripción. */
    private JTextArea descriptionField = new JTextArea(3, 40);

    /** * Campo con autocompletado para el código de calendario.
     */
    private TextFieldWithAutoCompletion<String> calendarCodeField;

    /**
     * Crea un nuevo SchedulerDetailsPanel asociado a un proyecto y un fichero.
     *
     * @param project proyecto de IntelliJ donde se está editando.
     * @param file    fichero virtual cuyo contenido JSON se edita en este panel.
     */
    public SchedulerDetailsPanel(@NotNull Project project, @NotNull VirtualFile file) {
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
                "Scheduler Details"
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
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        form.add(createLabel("Plan code"), c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        form.add(createLabel("Calendar Code"), c);
        c.gridy = 3;
        List<String> calendarVariants = getAllCalendarCodes(myProject);
        TextFieldWithAutoCompletion.StringsCompletionProvider provider =
                new TextFieldWithAutoCompletion.StringsCompletionProvider(calendarVariants, null);

        calendarCodeField = new TextFieldWithAutoCompletion<>(myProject, provider, true, null);
        calendarCodeField.setPlaceholder("Select calendar code...");
        calendarCodeField.setBackground(SchedulerTheme.BG_CARD);
        calendarCodeField.setOpaque(true);
        installClickToShowListener(calendarCodeField);
        calendarCodeField.setPreferredSize(CALENDAR_FIELD_SIZE);
        calendarCodeField.setMinimumSize(CALENDAR_FIELD_SIZE);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0.0;

        form.add(calendarCodeField, c);
        c.gridy = 1;
        c.weightx = 1.5;
        planCodeField.setEnabled(false);
        planCodeField.setDisabledTextColor(Color.GRAY);
        planCodeField.setBackground(SchedulerTheme.BG_CARD);
        planCodeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SchedulerTheme.BORDER_SOFT, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        form.add(planCodeField, c);

        c.gridx = 1;
        c.gridy = 0;
        form.add(createLabel("Version"), c);

        c.gridy = 1;
        c.weightx = 0.5;
        applyBlueFocusBorder(versionField);
        versionField.setBackground(SchedulerTheme.BG_CARD);
        applyDefaultText(versionField, "V1.0.0");
        form.add(versionField, c);

        c.gridx = 2;
        c.gridy = 0;
        form.add(createLabel("Status"), c);

        c.gridy = 1;
        c.weightx = 1.5;
        applyBlueFocusBorder(statusField);
        statusField.setBackground(SchedulerTheme.BG_CARD);
        applyDefaultText(statusField, "NOT_PUBLISHED");
        form.add(statusField, c);

        c.gridx = 3;
        c.gridy = 0;
        form.add(createLabel("Valid From"), c);
        c.gridy = 1;
        c.weightx = 0.5;
        validFromField.setBackground(SchedulerTheme.BG_CARD);
        form.add(validFromField, c);

        c.gridx = 4;
        c.gridy = 0;
        form.add(createLabel("Valid To"), c);
        c.gridy = 1;
        c.weightx = 0.5;
        validToField.setBackground(SchedulerTheme.BG_CARD);
        form.add(validToField, c);



        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
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
     * Busca todos los archivos con extensión .calendar en el proyecto.
     */
    private List<String> getAllCalendarCodes(Project project) {
        Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(
                project,
                "calendar",
                GlobalSearchScope.projectScope(project)
        );

        return files.stream()
                .map(VirtualFile::getNameWithoutExtension)
                .sorted()
                .collect(Collectors.toList());
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
     * @return
     */
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.planCodeField;
    }

    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado.
     *
     * @param jsonObject objeto JSON con los datos a cargar en el formulario.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (jsonObject.has("planCode") && !jsonObject.get("planCode").isJsonNull()) {
            planCodeField.setText(jsonObject.get("planCode").getAsString());
        }
        if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull()) {
            statusField.setText(jsonObject.get("status").getAsString());
        }
        if (jsonObject.has("version") && !jsonObject.get("version").isJsonNull()) {
            versionField.setText(jsonObject.get("version").getAsString());
        }
        if (jsonObject.has("validFrom") && !jsonObject.get("validFrom").isJsonNull()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                validFromField.setDate(sdf.parse(jsonObject.get("validFrom").getAsString()));
            } catch (ParseException e) {
                validFromField.setDate(null);
            }
        } else {
            validFromField.setDate(null);
        }

        if (jsonObject.has("validTo") && !jsonObject.get("validTo").isJsonNull()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                validToField.setDate(sdf.parse(jsonObject.get("validTo").getAsString()));
            } catch (ParseException e) {
                validToField.setDate(null);
            }
        } else {
            validToField.setDate(null);
        }
        if (jsonObject.has("description") && !jsonObject.get("description").isJsonNull()) {
            descriptionField.setText(jsonObject.get("description").getAsString());
        }
        if (jsonObject.has("calendarCode") && !jsonObject.get("calendarCode").isJsonNull()) {
            calendarCodeField.setText(jsonObject.get("calendarCode").getAsString());
        }
    }

    /**
     * Actualiza el JsonObject con los datos actuales del formulario.
     *
     * @param jsonObject objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {

        jsonObject.addProperty("planCode", planCodeField.getText());
        jsonObject.addProperty("version", versionField.getText());
        jsonObject.addProperty("status", statusField.getText());
        jsonObject.addProperty("calendarCode", calendarCodeField.getText());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFrom = validFromField.getDate();
        Date dateTo = validToField.getDate();
        if (dateFrom != null) {
            jsonObject.addProperty("validFrom", sdf.format(dateFrom));
        } else {
            jsonObject.remove("validFrom");
        }
        if (dateTo != null) {
            jsonObject.addProperty("validTo", sdf.format(dateTo));
        } else {
            jsonObject.remove("validTo");
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
     * @param textListener   listener para cambios en campos de texto.
     * @param actionListener listener para acciones (combos, botones, etc.).
     * @param dateChangeListener listener para cambios en componentes.
     */
    public void addFieldListeners(javax.swing.event.DocumentListener textListener, ActionListener actionListener, PropertyChangeListener dateChangeListener) {
        validFromField.addPropertyChangeListener("date", dateChangeListener);
        validToField.addPropertyChangeListener("date", dateChangeListener);

        // Listener para el campo de autocompletado: Disparar evento de guardado al cambiar texto
        if (calendarCodeField != null) {
            calendarCodeField.addDocumentListener(new com.intellij.openapi.editor.event.DocumentListener() {
                @Override
                public void documentChanged(@NotNull com.intellij.openapi.editor.event.DocumentEvent event) {
                    if (actionListener != null) {
                        // Simulamos una acción para forzar el updateDocument en el padre
                        actionListener.actionPerformed(
                                new ActionEvent(calendarCodeField, ActionEvent.ACTION_PERFORMED, "CALENDAR_CHANGED")
                        );
                    }
                }
            });
        }

        planCodeField.getDocument().addDocumentListener(textListener);
        versionField.getDocument().addDocumentListener(textListener);
        descriptionField.getDocument().addDocumentListener(textListener);
        statusField.getDocument().addDocumentListener(textListener);
    }


    /**
     * Valida la coherencia temporal entre las fechas de inicio y fin.
     *
     * Comprueba que la fecha Valid From sea estrictamente anterior a Valid To.
     * Si la validación falla muestra una alerta de error, limpia el campo de fecha final y devuelve el foco
     * al campo inicial para su corrección.
     */
    public void validateDateFields() {
        Date validFromDate = validFromField.getDate();
        Date validToDate = validToField.getDate();

        if (validFromDate != null && validToDate != null) {
            if (!validFromDate.before(validToDate)) {
                JOptionPane.showMessageDialog(
                        SchedulerDetailsPanel.this,
                        "Valid From debe ser anterior a Valid To",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE
                );
                validToField.setDate(null);
                validFromField.requestFocus();
            }
        }
    }

    /**
     * Aplicar correctamente el foco al campo asociado a la Descripción
     *
     * @param wrapperPanel Elemento contenedor
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
}