package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.AdapterFieldData;
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

/**
 * Panel de interfaz gráfica que proporciona la vista de detalle para la configuración
 * de un campo individual (Field) dentro del editor de componentes.
 * <p>
 * Esta vista permite al usuario definir las propiedades básicas de un campo (nombre, path, tipo),
 * así como configurar opciones avanzadas que se muestran dinámicamente dependiendo del tipo
 * de dato seleccionado (por ejemplo, formatos de fecha o delimitadores numéricos).
 * </p>
 */
public class FieldDetailView extends JPanel {

    // --- Campos de Datos ---
    private AtiTextField fieldNameField, payloadPathField, priorityField, extractionValueField;
    private AtiComboBox fieldTypeCombo, extractionTypeCombo;
    private AtiScriptPanel scriptField;

    // --- Configuradores ---
    private AtiTextField fieldFormatField, decimalDelimiterField, groupingDelimiterField;
    private AtiTextField languageField, countryField, timeZoneField;
    private AtiTextField fieldLengthField, regexField;

    // --- Envoltorios y Contenedores ---
    private JPanel gridWrapper;
    private JPanel configGrid;
    private AtiLabeledComponent fieldLengthWrapper, regexWrapper;
    private AtiLabeledComponent fieldFormatWrapper, decimalDelimiterWrapper, groupingDelimiterWrapper;
    private AtiLabeledComponent languageWrapper, countryWrapper, timeZoneWrapper;
    private AtiLabeledComponent extractionWrapper, payloadPathWrapper;

    private JPanel configSection;
    private Runnable onChange;

    /**
     * Bandera utilizada para evitar que se disparen eventos de actualización visual
     * mientras se cargan los datos programáticamente desde el modelo.
     */
    private boolean isPopulating = false;
    private final boolean isOutput;

    /**
     * Construye una nueva vista de detalles de campo.
     *
     * @param componentType El tipo de componente actual (ej. "InputAdapter", "OutputAdapter").
     * Se utiliza para adaptar ciertas etiquetas (como JSONPath vs Payload Path).
     * @param onChange      Un {@link Runnable} que se ejecutará cada vez que el usuario modifique algún valor del formulario.
     */
    public FieldDetailView(String componentType, Runnable onChange) {
        this.onChange = onChange;
        this.isOutput = componentType != null && componentType.contains("Output");
        setLayout(new BorderLayout());
        setOpaque(false);
        initComponents();
    }

    /**
     * Actualiza la función de callback que se ejecuta al detectar cambios en la interfaz.
     *
     * @param onChange El nuevo {@link Runnable} a ejecutar ante cambios.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Inicializa y organiza todos los componentes visuales de la interfaz utilizando
     * varios administradores de diseño (Layout Managers) para asegurar una presentación correcta y responsiva.
     */
    private void initComponents() {
        // Contenedor principal que agrupa todo hacia arriba
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // --- SECCIÓN 1: Datos Básicos ---
        JPanel topGrid = new JPanel(new GridBagLayout());
        topGrid.setOpaque(false);
        topGrid.setBorder(JBUI.Borders.empty(10, 0, 20, 0));

        GridBagConstraints topC = new GridBagConstraints();
        topC.insets = new Insets(0, 0, 15, 25);
        topC.fill = GridBagConstraints.HORIZONTAL;
        topC.anchor = GridBagConstraints.NORTHWEST;
        topC.weightx = 0.5;

        // Fila 0 - Columna 0: Field Name
        topC.gridy = 0;
        topC.gridx = 0;
        
        fieldNameField = WorkflowThemeUtils.createThemedTextField();
        topGrid.add(wrapField("Field Name", fieldNameField), topC);

        // Fila 0 - Columna 1: Payload Path / JSONPath
        topC.gridx = 1;
        
        payloadPathField = WorkflowThemeUtils.createThemedTextField();
        String pathLabel = isOutput ? "JSONPath" : "Payload Path";
        payloadPathWrapper = wrapField(pathLabel, payloadPathField);
        topGrid.add(payloadPathWrapper, topC);

        // Fila 1 - Columna 0: Priority
        topC.gridy = 1;
        topC.gridx = 0;
        
        priorityField = WorkflowThemeUtils.createThemedTextField();
        topGrid.add(wrapField("Priority", priorityField), topC);

        // Fila 1 - Columna 1: Field Type Combo
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

        // --- SECCIÓN 2: Field Configuration ---
        configSection = new JPanel(new BorderLayout());
        configSection.setOpaque(false);
        configSection.add(createSectionHeader("Field Configuration"), BorderLayout.NORTH);

        configGrid = new JPanel(new GridBagLayout());
        configGrid.setOpaque(false);
        configGrid.setBorder(JBUI.Borders.empty(15, 0, 0, 0));

        // FIX: Instanciamos los campos usando el factory para evitar desbordes
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

        // Añadimos el grid a la sección
        configSection.add(configGrid, BorderLayout.CENTER);

        mainContent.add(configSection);

        // --- SECCIÓN 3: Extraction ---
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

        // --- SECCIÓN 4: Script ---
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

        // Envolver el mainContent en un JBScrollPane
        JBScrollPane scrollPane = new JBScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        // Deshabilitar la barra horizontal para forzar a que todo se ajuste al ancho
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Añadimos el ScrollPane a la zona CENTRO del panel raíz (para que ocupe todo el espacio)
        add(scrollPane, BorderLayout.CENTER);

        updateConfigVisibility();
    }

    /**
     * Notifica a los listeners registrados que ha ocurrido un cambio en el formulario.
     * La notificación se omite si la vista está siendo poblada programáticamente.
     */
    private void notifyChange() {
        if (!isPopulating && onChange != null) onChange.run();
    }

    /**
     * Crea un panel con estilo de encabezado para separar visualmente las secciones del formulario.
     *
     * @param title El título de la sección a mostrar.
     * @return Un {@link JPanel} formateado como encabezado.
     */
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

    /**
     * Envuelve un campo de texto en un componente con etiqueta,
     * añadiendo también un listener para notificar cambios.
     * (Nota: el tema ya se aplicó en el factory WorkflowThemeUtils.createThemedTextField)
     *
     * @param label La etiqueta descriptiva para el campo.
     * @param field El componente {@link AtiTextField} a envolver.
     * @return Un {@link AtiLabeledComponent} que contiene el campo y su etiqueta.
     */
    private AtiLabeledComponent wrapField(String label, AtiTextField field) {
        // Ya no hace falta llamar a applyWorkflowTheme porque el Factory lo hace,
        // pero lo dejamos si alguna vez pasas un field normal.
        field.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        return new AtiLabeledComponent(label, field);
    }

    /**
     * Gestiona la visibilidad dinámica de los campos en la sección de "Field Configuration".
     * Muestra u oculta los componentes según el tipo de campo (ej. Numérico, Fecha)
     * seleccionado en el desplegable y redibuja la interfaz utilizando GridBagLayout.
     */
    private void updateConfigVisibility() {
        if (configGrid == null) return;

        configGrid.removeAll();

        String type = (String) fieldTypeCombo.getSelectedItem();
        if (type == null) type = "";

        boolean isNumeric = type.contains("INTEGER") || type.contains("LONG") || type.contains("DOUBLE");
        boolean isDate = type.contains("DATE");

        // Configuramos las reglas del GridBagLayout al estilo de ComponentDetailsView
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 15, 25); // Margen inferior y derecho
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0.5; // Otorga el mismo ancho a las dos columnas

        int row = 0;

        // Fila 1: Comunes
        c.gridy = row++;
        c.gridx = 0; configGrid.add(fieldLengthWrapper, c);
        c.gridx = 1; configGrid.add(regexWrapper, c);

        // Fila 2: Format
        if (isNumeric || isDate) {
            c.gridy = row++;
            c.gridx = 0; configGrid.add(fieldFormatWrapper, c);
            c.gridx = 1; // Dejamos la columna 2 vacía
        }

        // Fila 3: Numéricos
        if (isNumeric) {
            c.gridy = row++;
            c.gridx = 0; configGrid.add(decimalDelimiterWrapper, c);
            c.gridx = 1; configGrid.add(groupingDelimiterWrapper, c);
        }

        // Filas 4 y 5: Fechas
        if (isDate) {
            c.gridy = row++;
            c.gridx = 0; configGrid.add(languageWrapper, c);
            c.gridx = 1; configGrid.add(countryWrapper, c);

            c.gridy = row++;
            c.gridx = 0; configGrid.add(timeZoneWrapper, c);
        }

        // Repintamos la sección
        configGrid.revalidate();
        configGrid.repaint();

        if (configSection != null) {
            configSection.revalidate();
            configSection.repaint();
        }
    }

    /**
     * Limpia los valores de los campos de configuración condicional.
     * Se invoca típicamente cuando el usuario cambia el tipo de campo en la interfaz.
     */
    private void clearConfigFields() {
        fieldFormatField.setText("");
        decimalDelimiterField.setText("");
        groupingDelimiterField.setText("");
        languageField.setText("");
        countryField.setText("");
        timeZoneField.setText("");
    }

    /**
     * Actualiza internamente el texto de la etiqueta de extracción, utilizando parámetros por defecto.
     */
    private void updateExtractionLabel() {
        updateExtractionLabel(isOutput ? null : "XML");
    }

    /**
     * Actualiza la etiqueta del campo de valor de extracción en función del tipo de extracción
     * seleccionado y el tipo de mensaje (XML, JSON, CSV).
     *
     * @param messageType El tipo de mensaje actual (ej. "XML", "JSON").
     */
    public void updateExtractionLabel(String messageType) {
        String selected = (String) extractionTypeCombo.getSelectedItem();
        if (selected == null) return;

        String labelText = selected + " *";

        if (!isOutput && !"Fixed Value".equals(selected)) {
            if ("XML".equals(messageType)) labelText = "XPath";
            else if ("JSON".equals(messageType)) labelText = "JSON Path";
            else if ("CSV".equals(messageType)) labelText = "Column Index";
        }

        for (Component child : extractionWrapper.getComponents()) {
            if (child instanceof JLabel) {
                ((JLabel) child).setText(labelText);
                break;
            }
        }
    }

    /**
     * Carga los datos de un objeto de modelo en la vista.
     * Durante este proceso se deshabilitan temporalmente las notificaciones de cambio
     * para evitar eventos recursivos no deseados.
     *
     * @param data El objeto {@link AdapterFieldData} con los valores a cargar en el formulario.
     */
    public void loadData(AdapterFieldData data) {
        this.isPopulating = true;
        try {
            fieldNameField.setText(data.fieldName != null ? data.fieldName : "");
            payloadPathField.setText(data.payloadPath != null ? data.payloadPath : "");
            priorityField.setText(data.priority != null ? String.valueOf(data.priority) : "1");

            fieldTypeCombo.setSelectedItem(data.fieldType);
            extractionTypeCombo.setSelectedItem(data.extractionType);
            extractionValueField.setText(data.extractionValue != null ? data.extractionValue : "");

            fieldFormatField.setText(data.fieldFormat != null ? data.fieldFormat : "");
            decimalDelimiterField.setText(data.decimalDelimiter != null ? data.decimalDelimiter : "");
            groupingDelimiterField.setText(data.groupingDelimiter != null ? data.groupingDelimiter : "");

            languageField.setText(data.language != null ? data.language : "");
            countryField.setText(data.country != null ? data.country : "");
            timeZoneField.setText(data.timeZone != null ? data.timeZone : "");

            fieldLengthField.setText(data.fieldLength != null ? data.fieldLength : "");
            regexField.setText(data.regularExpression != null ? data.regularExpression : "");

            scriptField.setText(data.script != null ? data.script : "");

            updateConfigVisibility();
        } finally {
            this.isPopulating = false;
        }
    }

    /**
     * Extrae los valores actuales de los campos del formulario y los guarda en el objeto de modelo proporcionado.
     * Filtra los campos condicionales vaciándolos (null) si el tipo de campo actual no los requiere.
     *
     * @param data El objeto {@link AdapterFieldData} donde se guardarán los datos de la vista.
     */
    public void saveData(AdapterFieldData data) {
        data.fieldName = getNullIfEmpty(fieldNameField.getText().trim());
        data.payloadPath = getNullIfEmpty(payloadPathField.getText().trim());
        try { data.priority = Integer.parseInt(priorityField.getText().trim()); } catch (Exception e) { data.priority = 1; }

        String type = (String) fieldTypeCombo.getSelectedItem();
        data.fieldType = type;
        data.extractionType = (String) extractionTypeCombo.getSelectedItem();
        data.extractionValue = getNullIfEmpty(extractionValueField.getText().trim());

        data.fieldLength = getNullIfEmpty(fieldLengthField.getText().trim());
        data.regularExpression = getNullIfEmpty(regexField.getText().trim());
        data.script = getNullIfEmpty(scriptField.getText());

        boolean isNumeric = type != null && (type.contains("INTEGER") || type.contains("LONG") || type.contains("DOUBLE"));
        boolean isDate = type != null && type.contains("DATE");

        if (isNumeric || isDate) {
            data.fieldFormat = getNullIfEmpty(fieldFormatField.getText().trim());
        } else {
            data.fieldFormat = null;
        }

        if (isNumeric) {
            data.decimalDelimiter = getNullIfEmpty(decimalDelimiterField.getText().trim());
            data.groupingDelimiter = getNullIfEmpty(groupingDelimiterField.getText().trim());
        } else {
            data.decimalDelimiter = null;
            data.groupingDelimiter = null;
        }

        if (isDate) {
            data.language = getNullIfEmpty(languageField.getText().trim());
            data.country = getNullIfEmpty(countryField.getText().trim());
            data.timeZone = getNullIfEmpty(timeZoneField.getText().trim());
        } else {
            data.language = null;
            data.country = null;
            data.timeZone = null;
        }
    }

    /**
     * Utilidad para transformar cadenas vacías en valores nulos.
     *
     * @param text El texto a evaluar.
     * @return El texto original si no está vacío; {@code null} si está vacío o es nulo.
     */
    private String getNullIfEmpty(String text) {
        return (text == null || text.trim().isEmpty()) ? null : text;
    }
}