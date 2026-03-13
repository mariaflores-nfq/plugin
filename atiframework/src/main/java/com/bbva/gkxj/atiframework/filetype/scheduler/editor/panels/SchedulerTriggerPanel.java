package com.bbva.gkxj.atiframework.filetype.scheduler.editor.panels;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.HourDocumentFilter;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.google.gson.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.JsonUtils.*;
import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.*;

/**
 * Panel de edición para la configuración de disparadores (Triggers) del planificador.
 *
 * Esta clase gestiona la interfaz que permite al usuario establecer parámetros de ejecución diaria, semanal o mensual,
 * conectando bidireccionalmente los componentes de la UI con el archivo de configuración de extensión .sch
 * relativo a la planificación.
 */
public class SchedulerTriggerPanel extends JPanel {

    /** Proyecto de IntelliJ asociado al panel. */
    private final Project myProject;

    /** Documento (texto) asociado al fichero que se edita. */
    private final Document myDocument;

    /** Combo para seleccionar el tipo de planificación (Diaria/Semanal/Mensual). */
    private JComboBox<String> typeCombo;

    /** Campo de texto para la hora de inicio. */
    private JTextField hourFromField;

    /** Campo de texto para la hora de fin. */
    private JTextField hourToField;

    /** Selector numérico para definir el intervalo de repetición. */
    private JSpinner repeatValue;

    /** Desplegable para seleccionar la unidad de tiempo del intervalo de repetición (horas/minutos/segundos). */
    private JComboBox<String> repeatUnit;

    /**
     * Campo de texto de solo lectura que muestra el resumen de los días seleccionados.
     * Actúa como disparador para abrir el popup de selección.
     */
    private JTextField selectedDays;

    /** Etiqueta para la sección de días de la semana. */
    private JLabel lblDays;

    /** Panel contenedor del componente personalizado de selección múltiple para días de la semana. */
    private JPanel daysMultiSelect;

    /** Lista que almacena las referencias a los checkboxes individuales (Lunes-Domingo) para gestionar su estado. */
    private final List<JCheckBox> dayweekDaysCheckBoxes = new ArrayList<>();

    /**
     * Campo de texto de solo lectura que muestra el resumen de los meses seleccionados.
     * Actúa como disparador para abrir el popup de selección.
     */
    private JTextField selectedMonths;

    /** Etiqueta para la sección de meses. */
    private JLabel lblMonths;

    /** Panel contenedor del componente personalizado de selección múltiple para meses. */
    private JPanel monthsMultiSelect;

    /** Lista que almacena las referencias a los checkboxes individuales (Enero-Diciembre) para gestionar su estado. */
    private final List<JCheckBox> monthsCheckBoxes = new ArrayList<>();

    /**
     * Campo de texto de solo lectura que muestra el resumen de los días del mes seleccionados.
     * Actúa como disparador para abrir el popup de selección.
     */
    private JTextField selectedDaysOfMonths;

    /** Etiqueta para la sección de días del mes. */
    private JLabel lblDaysOfMonths;

    /** Panel contenedor del componente personalizado de selección múltiple para días del mes. */
    private JPanel daysOfMonthsMultiSelect;

    /** Lista que almacena las referencias a los checkboxes individuales (1-31) para gestionar su estado. */
    private final List<JCheckBox> daysOfMonthsCheckBoxes = new ArrayList<>();

    /** Desplegable para seleccionar el evento del mes(Primera-Quinta semana). */
    private JComboBox<String> monthEvent;

    /**
     * Campo de texto de solo lectura que muestra el resumen de los días de la semana seleccionados.
     * Actúa como disparador para abrir el popup de selección.
     */
    private JTextField selectedWeekDaysAffected;

    /** Etiqueta para la sección de días de la semana. */
    private JLabel lblWeekDaysAffected;

    /** Panel contenedor del componente personalizado de selección múltiple para días de la semana. */
    private JPanel weekDaysAffectedMultiSelect;

    /** Lista que almacena las referencias a los checkboxes individuales (Lunes-Domingo) para gestionar su estado. */
    private final List<JCheckBox> weekDaysAffectedCheckBoxes = new ArrayList<>();

    /**
     * Crea un nuevo SchedulerTriggerPanel asociado a un proyecto y un fichero.
     * @param project proyecto de IntelliJ donde se está editando.
     * @param file    fichero virtual cuyo contenido JSON se edita en este panel.
     */
    public SchedulerTriggerPanel(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        createUIComponents();
    }

    /**
     * Inicializa y configura todos los componentes visuales del panel.
     */
    void createUIComponents() {
        setLayout(new GridBagLayout());
        setBackground(SchedulerTheme.BG_CARD);

        typeCombo = new JComboBox<>(SCHEDULE_TYPES);
        typeCombo.setBackground(SchedulerTheme.BG_CARD);
        typeCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyBlueFocusBorder(typeCombo);

        selectedMonths = new JTextField("Select months");
        monthsMultiSelect = createMultiSelectCombo(
                MONTHS, selectedMonths, monthsCheckBoxes
        );
        lblMonths = createLabel("Months");
        monthsMultiSelect.setBackground(SchedulerTheme.BG_CARD);
        applyTooltipRecursively(monthsMultiSelect, "Select one or more months when the scheduler will run");
        applyBlueFocusBorder(monthsMultiSelect);

        selectedDays = new JTextField("Select days");
        daysMultiSelect = createMultiSelectCombo(
                DAYS_OF_WEEK, selectedDays, dayweekDaysCheckBoxes
        );
        lblDays = createLabel("Days of the week");
        daysMultiSelect.setBackground(SchedulerTheme.BG_CARD);
        applyTooltipRecursively(daysMultiSelect, "Select one or more days when the scheduler will run");
        applyBlueFocusBorder(daysMultiSelect);

        selectedDaysOfMonths = new JTextField("Select days of the months");
        daysOfMonthsMultiSelect = createMultiSelectCombo(
                DAYS_OF_THE_MONTHS, selectedDaysOfMonths, daysOfMonthsCheckBoxes
        );
        lblDaysOfMonths = createLabel("Days of the Months");
        daysOfMonthsMultiSelect.setBackground(SchedulerTheme.BG_CARD);
        applyTooltipRecursively(daysOfMonthsMultiSelect, "Select one or more days when the scheduler will run");
        applyBlueFocusBorder(daysOfMonthsMultiSelect);

        monthEvent = new JComboBox<>(MONTH_EVENTS);
        monthEvent.setBackground(SchedulerTheme.BG_CARD);
        monthEvent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyBlueFocusBorder(monthEvent);

        selectedWeekDaysAffected = new JTextField("Select week days affected");
        weekDaysAffectedMultiSelect = createMultiSelectCombo(
                DAYS_OF_WEEK, selectedWeekDaysAffected, weekDaysAffectedCheckBoxes
        );
        lblWeekDaysAffected = createLabel("Week Days Affected");
        weekDaysAffectedMultiSelect.setBackground(SchedulerTheme.BG_CARD);
        applyTooltipRecursively(weekDaysAffectedMultiSelect, "Select one or more days");
        applyBlueFocusBorder(weekDaysAffectedMultiSelect);

        hourFromField = new JTextField("09:00", 8);
        hourFromField.setBackground(SchedulerTheme.BG_CARD);
        ((AbstractDocument) hourFromField.getDocument()).setDocumentFilter(new HourDocumentFilter());
        applyBlueFocusBorder(hourFromField);

        hourToField = new JTextField("21:00", 8);
        hourToField.setBackground(SchedulerTheme.BG_CARD);
        ((AbstractDocument) hourToField.getDocument()).setDocumentFilter(new HourDocumentFilter());
        applyBlueFocusBorder(hourToField);

        repeatValue = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        repeatValue.setBackground(SchedulerTheme.BG_CARD);
        styleSpinner(repeatValue);
        applyBlueFocusBorder(repeatValue);

        repeatUnit = new JComboBox<>(new String[]{"hours", "minutes", "seconds"});
        repeatUnit.setBackground(SchedulerTheme.BG_CARD);
        repeatUnit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyBlueFocusBorder(repeatUnit);

        refreshLayout(typeCombo.getSelectedItem().toString());
    }

    /**
     * Aplica un tooltip a un componente y a todos sus componentes hijos relacionados.
     *
     * @param comp El componente raíz
     * @param tooltip El texto del tooltip
     */
    private void applyTooltipRecursively(JComponent comp, String tooltip) {
        comp.setToolTipText(tooltip);
        for (Component c : comp.getComponents()) {
            if (c instanceof JComponent jc) {
                applyTooltipRecursively(jc, tooltip);
            }
        }
    }

    /**
     * Crea una etiqueta con el estilo estándar.
     *
     * @param text El texto de la etiqueta.
     * @return JLabel configurado.
     */
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Lato", Font.BOLD, 14));
        l.setForeground(SchedulerTheme.TEXT_MAIN);
        return l;
    }

    /**
     * Actualiza el JsonObject con los datos actuales del formulario.
     *
     * @param jsonObject objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null) return;

        JsonObject trigger;
        if (jsonObject.has("trigger")) {
            trigger = jsonObject.getAsJsonObject("trigger");
        } else {
            trigger = new JsonObject();
        }

        String currentType = ((String) Objects.requireNonNull(typeCombo.getSelectedItem())).toUpperCase();
        trigger.addProperty("type", currentType);

        JsonObject repeat;
        if (jsonObject.has("repeat")) {
            repeat = trigger.getAsJsonObject("repeat");
        } else {
            repeat = new JsonObject();
        }

        if (repeat.has("weekDays")) repeat.remove("weekDays");
        repeat.addProperty("executionTime", hourFromField.getText());
        repeat.addProperty("maxExecutionTime", hourToField.getText());

        Integer valueEvery = ((Integer) repeatValue.getValue());
        String selectedUnit = (String) repeatUnit.getSelectedItem();
        switch (Objects.requireNonNull(selectedUnit)) {
            case "hours" -> valueEvery *= 3600;
            case "minutes" -> valueEvery *= 60;
        }
        repeat.addProperty("repeatEvery", valueEvery);
        trigger.add("repeat", repeat);

        if (trigger.has("months")) trigger.remove("months");
        if (trigger.has("days")) trigger.remove("days");
        if (trigger.has("eventWeek")) trigger.remove("eventWeek");
        if (trigger.has("weekDays")) trigger.remove("weekDays");

        if ("WEEKLY".equals(currentType)) {
            JsonArray weekDaysJson = getJsonArrayFromCheckBoxes(dayweekDaysCheckBoxes);
            if (weekDaysJson.size() > 0) {
                repeat.add("weekDays", weekDaysJson);
            }

        } else if ("MONTHLY".equals(currentType)) {
            JsonArray monthsJson = getJsonArrayFromCheckBoxes(monthsCheckBoxes);
            if (monthsJson.size() > 0) trigger.add("months", monthsJson);

            JsonArray daysJson = getJsonIntArrayFromCheckBoxes(daysOfMonthsCheckBoxes);
            if (daysJson.size() > 0) trigger.add("days", daysJson);

            String eventWeek = monthEvent.getSelectedItem().toString();
            if (eventWeek != null) trigger.addProperty("eventWeek", eventWeek);

            JsonArray affectedDaysJson = getJsonArrayFromCheckBoxes(weekDaysAffectedCheckBoxes);
            if (affectedDaysJson.size() > 0) trigger.add("weekDays", affectedDaysJson);
        }
        jsonObject.add("trigger", trigger);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(jsonObject));
        });
    }


    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     *
     * @param textListener   listener para cambios en campos de texto.
     * @param actionListener listener para acciones (combos, botones, etc.).
     * @param changeListener listener para cambios en componentes tipo spinner.
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {

        typeCombo.addActionListener(actionListener);
        monthEvent.addActionListener(actionListener);
        repeatValue.addChangeListener(changeListener);
        repeatUnit.addActionListener(actionListener);
        hourFromField.getDocument().addDocumentListener(textListener);
        hourToField.getDocument().addDocumentListener(textListener);

        typeCombo.addActionListener(e -> {
            String selectedType = (String) typeCombo.getSelectedItem();
            refreshLayout(selectedType);
            if (actionListener != null) {
                actionListener.actionPerformed(e);
            }
        });

        addMultiSelectListeners(dayweekDaysCheckBoxes, selectedDays, actionListener, EMPTY_WEEK_DAYS_MSG);
        addMultiSelectListeners(monthsCheckBoxes, selectedMonths, actionListener, EMPTY_MONTHS_MSG);
        addMultiSelectListeners(daysOfMonthsCheckBoxes, selectedDaysOfMonths, actionListener, EMPTY_DAYS_MONTHS_MSG);
        addMultiSelectListeners(weekDaysAffectedCheckBoxes, selectedWeekDaysAffected, actionListener, EMPTY_WEEK_DAYS_AFFECTED_MSG);
    }

    /**
     * Configura los listeners para un grupo de checkboxes de selección múltiple.
     *
     * Añade dos acciones a cada checkbox de la lista:
     * - Ejecutar el listener principal (para guardar datos).
     * - Actualizar inmediatamente el texto visual del campo resumen.
     *
     * @param checkBoxes Lista de checkboxes a los que se añadirán los eventos.
     * @param displayField Campo de texto donde se muestra el resumen de la selección.
     * @param mainActionListener Listener encargado de la lógica de guardado.
     * @param emptyMsg Mensaje a mostrar en el campo de texto cuando no hay nada seleccionado.
     */
    private void addMultiSelectListeners(List<JCheckBox> checkBoxes, JTextField displayField, ActionListener mainActionListener, String emptyMsg) {
        if (checkBoxes == null) return;

        ActionListener uiUpdater = e -> updateSelectedOptionsText(displayField, checkBoxes, emptyMsg);

        checkBoxes.forEach(cb -> {
            cb.addActionListener(mainActionListener);
            cb.addActionListener(uiUpdater);
        });
    }

    /**
     * Crea un componente UI personalizado que simula un ComboBox de selección múltiple.
     *
     * El componente combina un campo de texto de solo lectura con un botón desplegable.
     * Al hacer clic en cualquiera de los dos, se abre un popup que contiene una lista
     * desplazable de checkboxes generados a partir de los valores proporcionados.
     *
     * @param values Array de cadenas con las opciones disponibles para mostrar en la lista.
     * @param selectedBoxes El campo de texto donde se mostrará la selección (se configura aquí como no editable).
     * @param checkBoxes Una lista vacía que será poblada con los objetos JCheckBox creados, para su posterior gestión.
     * @return Un JPanel que contiene el componente visual completo.
     */
    private JPanel createMultiSelectCombo(String[] values, JTextField selectedBoxes, List<JCheckBox> checkBoxes) {

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(SchedulerTheme.BG_CARD);

        selectedBoxes.setBorder(BorderFactory.createEmptyBorder());
        selectedBoxes.setEditable(false);
        selectedBoxes.setFocusable(false);
        selectedBoxes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel popupContent = new JPanel();
        popupContent.setLayout(new BoxLayout(popupContent, BoxLayout.Y_AXIS));
        popupContent.setBackground(SchedulerTheme.BG_CARD);
        popupContent.setBorder(JBUI.Borders.empty(5));

        for (String value : values) {
            JCheckBox cb = new JCheckBox(value);
            cb.setBackground(SchedulerTheme.BG_CARD);
            cb.setFocusPainted(false);
            checkBoxes.add(cb);
            popupContent.add(cb);
        }

        JBScrollPane scrollPane = new JBScrollPane(popupContent);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        int contentHeight = Math.min(values.length * 26, 182);
        scrollPane.setPreferredSize(new Dimension(160, contentHeight));

        JButton toggle = new JButton();
        toggle.setIcon(com.intellij.icons.AllIcons.General.ChevronDown);
        toggle.setPreferredSize(new Dimension(26, 26));
        toggle.setBorder(BorderFactory.createEmptyBorder());
        toggle.setContentAreaFilled(false);
        toggle.setFocusPainted(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Runnable openPopupAction = () -> {
            JBPopup popup = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(scrollPane, null)
                    .setRequestFocus(true)
                    .setFocusable(true)
                    .setResizable(false)
                    .setMovable(false)
                    .createPopup();
            popup.showUnderneathOf(container);
        };

        toggle.addActionListener(e -> openPopupAction.run());

        selectedBoxes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openPopupAction.run();
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(SchedulerTheme.BG_CARD);
        top.add(selectedBoxes, BorderLayout.CENTER);
        top.add(toggle, BorderLayout.EAST);

        container.add(top, BorderLayout.CENTER);
        return container;
    }


    /**
     * Actualiza dinámicamente el diseño del panel según el tipo de planificador seleccionado.
     *
     * @param type El tipo de frecuencia seleccionado (DAILY/WEEKLY/MONTHLY).
     */
    private void refreshLayout(String type) {
        this.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;

        if ("MONTHLY".equalsIgnoreCase(type)) {
            layoutMonthly(c);
        } else if ("WEEKLY".equalsIgnoreCase(type)) {
            layoutWeekly(c);
        } else {
            layoutDaily(c);
        }
        this.revalidate();
        this.repaint();
    }

    /**
     * Configura la distribución visual de componentes para la frecuencia Mensual.
     *
     * @param c Las restricciones de GridBagLayout utilizadas para posicionar los elementos.
     */
    private void layoutMonthly(GridBagConstraints c) {
        addStack(c, 0, 0, createLabel("Type"), typeCombo);
        addStack(c, 1, 0, lblMonths, monthsMultiSelect);
        addStack(c, 2, 0, lblDaysOfMonths, daysOfMonthsMultiSelect);
        addStack(c, 3, 0, createLabel("Hour from"), hourFromField);
        addStack(c, 4, 0, createLabel("Hour to"), hourToField);

        addStack(c, 0, 2, createLabel("Repeat every"), repeatValue);
        addToGrid(c, repeatUnit, 1, 3);

        addStack(c, 0, 4, createLabel("Months events"), monthEvent);
        addStack(c, 1, 4, lblWeekDaysAffected, weekDaysAffectedMultiSelect);
        addVerticalFiller(c, 6, 5);
    }

    /**
     * Configura la distribución visual de componentes para la frecuencia Semanal.
     *
     * @param c Las restricciones de GridBagLayout utilizadas para posicionar los elementos.
     */
    private void layoutWeekly(GridBagConstraints c) {
        addStack(c, 0, 0, createLabel("Type"), typeCombo);
        addStack(c, 1, 0, lblDays, daysMultiSelect);
        addStack(c, 2, 0, createLabel("Hour from"), hourFromField);
        addStack(c, 3, 0, createLabel("Hour to"), hourToField);
        addStack(c, 4, 0, createLabel("Repeat every"), repeatValue);
        addToGrid(c, repeatUnit, 5, 1);
        addVerticalFiller(c, 6, 5);
    }

    /**
     * Configura la distribución visual de componentes para la frecuencia Diaria.
     *
     * @param c Las restricciones de GridBagLayout utilizadas para posicionar los elementos.
     */
    private void layoutDaily(GridBagConstraints c) {
        addStack(c, 0, 0, createLabel("Type"), typeCombo);
        addStack(c, 1, 0, createLabel("Hour from"), hourFromField);
        addStack(c, 2, 0, createLabel("Hour to"), hourToField);
        addStack(c, 3, 0, createLabel("Repeat every"), repeatValue);
        addToGrid(c, repeatUnit, 4, 1);
        addVerticalFiller(c, 6, 5);
    }

    /**
     * Añade un panel invisible en la parte inferior para mandar los componentes arriba
     *
     * @param c El objeto GridBagConstraints actual (se modificará para este componente).
     * @param row La fila (gridy) donde se debe colocar el relleno (debe ser la última).
     * @param width El ancho (gridwidth) que debe ocupar (número total de columnas del formulario).
     */
    private void addVerticalFiller(GridBagConstraints c, int row, int width) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0.0;
        c.weighty = 1.0;
        c.gridwidth = width;
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.NORTH;

        JPanel filler = new JPanel();
        filler.setOpaque(false);
        add(filler, c);
    }


    /**
     * Añadir un componente al panel en una posición específica de la rejilla.
     *
     * @param c El objeto GridBagConstraints a modificar.
     * @param comp El componente visual a añadir.
     * @param x La columna de la rejilla.
     * @param y La fila de la rejilla.
     */
    private void addToGrid(GridBagConstraints c, JComponent comp, int x, int y) {
        c.gridx = x;
        c.gridy = y;
        add(comp, c);
    }

    /**
     * Apilar verticalmente una etiqueta y un componente en la misma columna. Utilizado para crear pares etiqueta/input
     * de forma vertical.
     *
     * @param c El objeto GridBagConstraints a modificar.
     * @param x La columna de la rejilla donde se colocarán ambos elementos.
     * @param yStart La fila inicial para la etiqueta superior.
     * @param topLabel La etiqueta que irá arriba.
     * @param bottomComp El componente principal que irá debajo.
     */
    private void addStack(GridBagConstraints c, int x, int yStart, JComponent topLabel, JComponent bottomComp) {
        addToGrid(c, topLabel, x, yStart);
        addToGrid(c, bottomComp, x, yStart + 1);
    }


    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado.
     *
     * @param jsonObject objeto JSON con los datos a cargar en el formulario.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("trigger") || jsonObject.get("trigger").isJsonNull()) {
            return;
        }

        JsonObject trigger = jsonObject.getAsJsonObject("trigger");
        String type = (trigger.has("type") && !trigger.get("type").isJsonNull())
                ? trigger.get("type").getAsString()
                : "DAILY";

        setComboSelection(typeCombo, type);
        refreshLayout(type);

        if (trigger.has("repeat") && !trigger.get("repeat").isJsonNull()) {
            JsonObject repeat = trigger.getAsJsonObject("repeat");

            if (repeat.has("executionTime") && !repeat.get("executionTime").isJsonNull())
                hourFromField.setText(repeat.get("executionTime").getAsString());

            if (repeat.has("maxExecutionTime") && !repeat.get("maxExecutionTime").isJsonNull())
                hourToField.setText(repeat.get("maxExecutionTime").getAsString());

            if (repeat.has("repeatEvery") && !repeat.get("repeatEvery").isJsonNull()) {
                calculateAndSetRepeatUnit(repeat.get("repeatEvery").getAsInt());
            }

            if ("WEEKLY".equalsIgnoreCase(type) && repeat.has("weekDays") && !repeat.get("weekDays").isJsonNull()) {
                loadCheckBoxesFromJson(repeat.getAsJsonArray("weekDays"), dayweekDaysCheckBoxes, selectedDays, EMPTY_WEEK_DAYS_MSG);
            }
        }

        if ("MONTHLY".equalsIgnoreCase(type)) {
            if (trigger.has("months") && !trigger.get("months").isJsonNull()) {
                loadCheckBoxesFromJson(trigger.getAsJsonArray("months"), monthsCheckBoxes, selectedMonths, EMPTY_MONTHS_MSG);
            }

            if (trigger.has("days") && !trigger.get("days").isJsonNull()) {
                loadCheckBoxesFromJson(trigger.getAsJsonArray("days"), daysOfMonthsCheckBoxes, selectedDaysOfMonths, EMPTY_DAYS_MONTHS_MSG);
            }

            if (trigger.has("eventWeek") && !trigger.get("eventWeek").isJsonNull()) {
                String evt = trigger.get("eventWeek").getAsString();
                setComboSelection(monthEvent, evt);
            }

            if (trigger.has("weekDays") && !trigger.get("weekDays").isJsonNull()) {
                loadCheckBoxesFromJson(trigger.getAsJsonArray("weekDays"),
                        weekDaysAffectedCheckBoxes, selectedWeekDaysAffected, EMPTY_WEEK_DAYS_AFFECTED_MSG);
            }
        }
    }

    /**
     * Selecciona un elemento en el JComboBox buscando una coincidencia de texto que ignore mayúsculas y minúsculas.
     * Recorre los elementos del combo y selecciona el primero que coincida con el valor proporcionado.
     *
     * @param combo El JComboBox donde se realizará la selección.
     * @param value El valor de texto a buscar y seleccionar.
     */
    private void setComboSelection(JComboBox<String> combo, String value) {
        if (value == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equalsIgnoreCase(value)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Calcula y establece la unidad de tiempo más apropiada (Horas, Minutos o Segundos)
     * basándose en un valor total en segundos.
     *
     * @param totalSeconds El valor total en segundos a convertir y mostrar.
     */
    private void calculateAndSetRepeatUnit(int totalSeconds) {
        String unit = "seconds";
        int value = totalSeconds;

        if (totalSeconds > 0) {
            if (totalSeconds % 3600 == 0) {
                unit = "hours";
                value = totalSeconds / 3600;
            } else if (totalSeconds % 60 == 0) {
                unit = "minutes";
                value = totalSeconds / 60;
            }
        }
        repeatValue.setValue(value);
        setComboSelection(repeatUnit, unit);
    }

}