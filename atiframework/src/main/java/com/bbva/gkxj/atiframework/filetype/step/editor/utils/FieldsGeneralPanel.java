package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

import com.bbva.gkxj.atiframework.components.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Clase que representa los campos de configuración general para los pasos ETL,
 * aplicable a campos fijos, campos XML o campos de API Request.
 */
public class FieldsGeneralPanel extends JPanel {

    /**
     * Lista de datos de campos fijos.
     */
    private final List<FieldsData> fieldsData = new ArrayList<>();

    /**
     * Campo de entrada para el nombre del campo.
     */
    private final AtiTextField fieldNameInput = new AtiTextField();

    /**
     * Campo de entrada para la longitud del campo.
     */
    private final AtiTextField fieldLength = new AtiTextField();

    /**
     * Área de texto para la expresión regular.
     */
    private final AtiResizableTextArea regularExpression = new AtiResizableTextArea();

    /**
     * Campo de entrada para el formato de fecha.
     */
    private final AtiTextField dateFieldFormat = new AtiTextField();

    /**
     * Campo de entrada para el idioma.
     */
    private final AtiTextField language = new AtiTextField();

    /**
     * Campo de entrada para el país.
     */
    private final AtiTextField country = new AtiTextField();

    /**
     * Campo de entrada para el formato de campo double.
     */
    private final AtiTextField doubleFieldFormat = new AtiTextField();

    /**
     * Campo de entrada para el delimitador decimal de double.
     */
    private final AtiTextField doubleDecimalDelimiter = new AtiTextField();

    /**
     * Campo de entrada para el delimitador de agrupación de double.
     */
    private final AtiTextField doubleGroupingDelimiter = new AtiTextField();

    /**
     * Campo de entrada para el formato de campo long.
     */
    private final AtiTextField longFieldFormat = new AtiTextField();

    /**
     * Campo de entrada para el delimitador decimal de long.
     */
    private final AtiTextField longDecimalDelimiter = new AtiTextField();

    /**
     * Combo para el nombre interno.
     */
    private AtiComboBox internalNameCombo;

    /**
     * Campo de entrada para XPath o JsonPath.
     */
    private AtiTextField xpathField;

    /**
     * Combo para el tipo de campo.
     */
    private AtiComboBox fieldTypeCombo;

    /**
     * Contenedor de configuración.
     */
    private JPanel configContainer;

    /**
     * Layout de tarjetas para la configuración.
     */
    private CardLayout configCardLayout;


    /**
     * Indica si se está actualizando la interfaz de usuario.
     */
    private boolean isUpdatingUI = false;

    /**
     * Selección actual de campo fijo.
     */
    private FieldsData currentSelection = null;

    /**
     * Callback para cambios en el formulario.
     */
    private Runnable changeCallback;

    /**
     * Tipos de campo clásicos.
     */
    private static final String[] NORMAL_FIELD_TYPES = {"", "String", "Integer", "Long", "Double", "Date"};

    /**
     * Tipos de campo para XML.
     */
    private static final String[] EXTENDED_FIELD_TYPES = {"", "String", "Integer", "Long", "Double", "Date", "List", "Node List"};

    /**
     * Panel divisor de tabla para campos fijos.
     */
    private AtiTableSplitterPanel<FieldsData> tableSplitter;

    /**
     * Tipo de paso actual.
     */
    private final String stepType;

    /**
     * Tipo de panel actual.
     */
    private final String panelType;

    /**
     * Modo del panel de campos.
     */
    private final FieldsPanelMode fieldsPanelMode;

    /**
     * Contador interno para generar IDs secuenciales de nuevos campos.
     */
    private int fieldCounter = 0;


    /**
     * Constructor con modo explícito.
     */
    public FieldsGeneralPanel(StepType stepType, PanelType panelType, FieldsPanelMode fieldsPanelMode) {
        this.stepType = stepType.getValue();
        this.panelType = panelType.getValue();
        this.fieldsPanelMode = fieldsPanelMode;
        initUI();
        setupListeners();
    }

    /**
     * Establece el callback que se invocará cuando el usuario modifique datos en el formulario.
     * Este callback debe disparar la sincronización Form -> JSON a través de la cadena de editores padre.
     */
    public void setChangeCallback(Runnable callback) {
        this.changeCallback = callback;
    }

    /**
     * Notifica al editor padre que hubo un cambio en el formulario para que sincronice Form -> JSON.
     */
    private void notifyChange() {
        if (!isUpdatingUI && changeCallback != null) {
            changeCallback.run();
        }
    }

    /**
     * Inicializa la interfaz gráfica del panel delegando la tabla y splitter al componente reutilizable.
     */
    private void initUI() {
        this.setLayout(new BorderLayout());

        JPanel formPanel = buildFormPanel();

        tableSplitter = new AtiTableSplitterPanel<>(
                fieldsPanelMode.fieldTableName() + " Fields",
                "Fields",
                () -> {
                    fieldCounter++;
                    return new FieldsData(String.format("%02d", fieldCounter));
                },
                FieldsData::getId,
                FieldsData::getName,
                formPanel
        );

        tableSplitter.setChangeCallback(this::notifyChange);

        tableSplitter.setSelectionListener(item -> {
            if (currentSelection != null) {
                syncCurrentSelectionFromUI();
            }
            currentSelection = item;
            loadDataIntoForm(currentSelection);
            showSpecificCard(currentSelection.getType());
        });

        tableSplitter.setDeselectionListener(() -> {
            currentSelection = null;
        });

        this.add(tableSplitter, BorderLayout.CENTER);
    }

    /**
     * Construye el panel del formulario con los campos de configuración y el layout adecuado.
     * @return JPanel con el formulario construido y listo para ser integrado en el splitter.
     */
    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(JBUI.Borders.empty(10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5, 0);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        fieldNameInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (!isUpdatingUI) {
                    updateData("fieldName", fieldNameInput.getText());
                    tableSplitter.updateSelectedRowName(fieldNameInput.getText());
                    notifyChange();
                }
            }
        });

        String[] fieldTypeOptions = fieldsPanelMode.supportsComplexTypes() ? EXTENDED_FIELD_TYPES : NORMAL_FIELD_TYPES;
        fieldTypeCombo = new AtiComboBox(fieldTypeOptions);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        form.add(new AtiLabeledComponent("Field Name", fieldNameInput), gbc);

        gbc.gridx = 1;
        gbc.insets = JBUI.insets(5, 10, 5, 0);
        if (fieldsPanelMode.usesPathExpressions()) {
            xpathField = new AtiTextField();
            String xpathLabel = PanelType.API_REQUEST.getValue().equals(panelType) ? "Json Path" : "XPath";
            form.add(new AtiLabeledComponent(xpathLabel, xpathField), gbc);
        } else {
            internalNameCombo = new AtiComboBox(new String[]{""});
            form.add(new AtiLabeledComponent("Internal Name", internalNameCombo), gbc);
        }

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = JBUI.insets(5, 0);
        form.add(new AtiLabeledComponent("Field Type", fieldTypeCombo), gbc);

        configCardLayout = new CardLayout();
        configContainer = new JPanel(configCardLayout);

        configContainer.add(new JPanel(), "NONE");
        configContainer.add(buildDynamicSection(buildStringFields()), "String");
        configContainer.add(buildDynamicSection(buildNumericFields(false)), "IntDouble");
        configContainer.add(buildDynamicSection(buildNumericFields(true)), "Long");
        configContainer.add(buildDynamicSection(buildDateFields()), "Date");

        gbc.gridy = 2;
        gbc.weighty = 0.0;
        form.add(configContainer, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        form.add(Box.createVerticalGlue(), gbc);

        return form;
    }

    /**
     * Construye una sección dinámica del formulario que se muestra u oculta según el tipo de campo seleccionado.
     * @param fieldsPanel Panel con los campos específicos para el tipo de dato (String, Integer, Date, etc) que se desea mostrar.
     * @return JPanel con el título de la sección y los campos específicos, listo para ser agregado al CardLayout del formulario.
     */
    private JPanel buildDynamicSection(JPanel fieldsPanel) {
        JPanel section = new JPanel(new BorderLayout(0, 10));
        section.setBorder(JBUI.Borders.emptyTop(20));

        JLabel configTitle = new JLabel("Field Configuration");
        configTitle.setFont(configTitle.getFont().deriveFont(Font.PLAIN, 18f));

        section.add(configTitle, BorderLayout.NORTH);
        section.add(fieldsPanel, BorderLayout.CENTER);
        return section;
    }

    /**
     * Construye el panel con los campos específicos para el tipo String.
     * @return JPanel con los campos de configuración para campos de tipo String (Field Length y Regular Expression).
     */
    private JPanel buildStringFields() {
        JPanel p = new JPanel(new VerticalFlowLayout(0, 0, 10, true, false));
        p.add(new AtiLabeledComponent("Field Length", fieldLength));
        p.add(new AtiLabeledComponent("Regular Expression", regularExpression));
        return p;
    }

    /**
     * Construye el panel con los campos específicos para los tipos numéricos (Integer, Double, Long).
     *
     * @param isLong Indica si se están construyendo los campos para tipo Long, lo que determina qué campos mostrar.
     * @return JPanel con los campos de configuración para campos numéricos, adaptado según si es Long o Integer/Double.
     */
    private JPanel buildNumericFields(boolean isLong) {
        JPanel p = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;

        if (isLong) {
            c.gridx = 0;
            c.weightx = 1.0;
            c.insets = JBUI.insetsBottom(5);
            c.gridwidth = 2;
            p.add(new AtiLabeledComponent("Field Format", longFieldFormat), c);

            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 1.0;
            c.insets = JBUI.emptyInsets();
            c.gridwidth = 2;
            p.add(new AtiLabeledComponent("Decimal delimiter", longDecimalDelimiter), c);
        } else {
            c.gridx = 0;
            c.weightx = 0.5;
            c.insets = JBUI.insetsRight(10);
            p.add(new AtiLabeledComponent("Field Format", doubleFieldFormat), c);

            c.gridx = 1;
            c.weightx = 0.5;
            c.insets = JBUI.insetsLeft(10);
            p.add(new AtiLabeledComponent("Decimal delimiter", doubleDecimalDelimiter), c);

            c.gridy = 1;
            c.gridx = 0;
            c.gridwidth = 2;
            c.weightx = 1.0;
            c.insets = JBUI.insetsTop(10);
            p.add(new AtiLabeledComponent("Grouping delimiter", doubleGroupingDelimiter), c);
        }
        return p;
    }

    /**
     * Construye el panel con los campos específicos para el tipo Date.
     *
     * @return JPanel con los campos de configuración para campos de tipo Date (Field Format, Language y Country).
     */
    private JPanel buildDateFields() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridy = 0;
        c.gridwidth = 2;
        p.add(new AtiLabeledComponent("Field Format", dateFieldFormat), c);
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.insets = JBUI.insetsTop(10);
        p.add(new AtiLabeledComponent("Language", language), c);
        c.gridx = 1;
        c.insets = JBUI.insets(10, 10, 0, 0);
        p.add(new AtiLabeledComponent("Country", country), c);
        return p;
    }

    /**
     * Configura los listeners de los campos del formulario para detectar cambios y actualizar el modelo de datos en consecuencia.
     * Cada vez que el usuario modifica un campo, se actualiza el objeto FixedFieldData correspondiente y se notifica al editor padre para sincronizar Form -> JSON.
     */
    private void setupListeners() {

        fieldTypeCombo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) return;
            String type = (String) e.getItem();
            showSpecificCard(type);
            if (!isUpdatingUI) {
                updateData("fieldType", type);
                notifyChange();
            }
        });

        setupTextFieldListener(fieldLength, "fieldLength");
        setupTextFieldListener(regularExpression.getTextArea(), "fieldRegex");

        setupTextFieldListener(doubleFieldFormat, "fieldFormat");
        setupTextFieldListener(doubleDecimalDelimiter, "decimalDelimiter");
        setupTextFieldListener(doubleGroupingDelimiter, "groupingDelimiter");

        setupTextFieldListener(longFieldFormat, "fieldFormat");
        setupTextFieldListener(longDecimalDelimiter, "decimalDelimiter");

        setupTextFieldListener(dateFieldFormat, "fieldFormat");
        setupTextFieldListener(language, "language");
        setupTextFieldListener(country, "country");

        if (fieldsPanelMode.usesPathExpressions() && xpathField != null) {
            String xpathKey = "apiRequest".equals(panelType) ? "jsonPath" : "xpath";
            setupTextFieldListener(xpathField, xpathKey);
        }
    }

    /**
     * Método auxiliar para configurar un listener de teclado en un JTextComponent (como JTextField o JTextArea)
     * que actualiza el modelo de datos cada vez que el usuario suelta una tecla.
     *
     * @param field   el componente de texto al que se le agregará el listener.
     * @param jsonKey la clave del campo en el modelo de datos que se actualizará con el texto del campo.
     */
    private void setupTextFieldListener(javax.swing.text.JTextComponent field, String jsonKey) {
        field.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (!isUpdatingUI) {
                    updateData(jsonKey, field.getText());
                    notifyChange();
                }
            }
        });
    }

    /**
     * Actualiza el objeto FixedFieldData actualmente seleccionado con el nuevo valor ingresado por el usuario.
     * Usado desde los listeners de los campos del formulario cada vez que el usuario modifica un valor.
     *
     * @param key la clave del campo que se está actualizando (por ejemplo, "fieldName", "fieldType", "fieldLength", etc).
     * @param val el nuevo valor ingresado por el usuario para ese campo.
     */
    private void updateData(String key, String val) {
        int row = tableSplitter.getSelectedRow();
        if (row < 0 || row >= fieldsData.size()) return;
        FieldsData data = fieldsData.get(row);

        switch (key) {
            case "fieldName" -> {
                data.setName(val);
                data.setFieldName(val);
            }
            case "fieldType" -> {
                data.setType(val);
                data.setFieldType(val);
            }
            case "fieldLength" -> data.setFieldLength(val);
            case "fieldRegex" -> data.setFieldRegex(val);
            case "fieldFormat" -> data.setFieldFormat(val);
            case "decimalDelimiter" -> data.setDecimalDelimiter(val);
            case "groupingDelimiter" -> data.setGroupingDelimiter(val);
            case "language" -> data.setLanguage(val);
            case "country" -> data.setCountry(val);
            case "xpath" -> data.setXpath(val);
            case "jsonPath" -> data.setJsonPath(val);
        }
    }

    /**
     * Carga los datos de un objeto FixedFieldData en el formulario para que el usuario pueda verlos y editarlos.
     * Es llamado cada vez que el usuario selecciona un campo diferente en la tabla, o cuando se carga la configuración desde el JSON.
     *
     * @param data el objeto FixedFieldData cuyos datos se cargarán en el formulario. Si es null, no se hace nada.
     */
    private void loadDataIntoForm(FieldsData data) {
        if (data == null) return;
        isUpdatingUI = true;

        try {
            fieldNameInput.setText(data.getName());
            fieldTypeCombo.setSelectedItem(data.getType());

            if (fieldsPanelMode.usesPathExpressions() && xpathField != null) {
                if ("apiRequest".equals(panelType)) {
                    xpathField.setText(data.getJsonPath());
                } else {
                    xpathField.setText(data.getXpath());
                }
            }

            fieldLength.setText(data.getFieldLength());
            regularExpression.getTextArea().setText(data.getFieldRegex());

            doubleFieldFormat.setText(data.getFieldFormat());
            dateFieldFormat.setText(data.getFieldFormat());
            longFieldFormat.setText(data.getFieldFormat());

            longDecimalDelimiter.setText(data.getDecimalDelimiter());
            doubleDecimalDelimiter.setText(data.getDecimalDelimiter());
            doubleGroupingDelimiter.setText(data.getGroupingDelimiter());

            country.setText(data.getCountry());
            language.setText(data.getLanguage());

            showSpecificCard(data.getType());

        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Muestra u oculta las secciones de configuración adicional según el tipo de campo seleccionado.
     * @param type Tipo de campo seleccionado, determina qué sección mostrar. Si es null o vacío, se ocultan las secciones.
     */
    private void showSpecificCard(String type) {
        if (this.configCardLayout == null) return;

        String safeType = (type == null) ? "" : type;
        String cardName = switch (safeType) {
            case "", "List", "Node List" -> "NONE";
            case "Integer", "Double" -> "IntDouble";
            case "Long" -> "Long";
            case "Date" -> "Date";
            default -> "String";
        };
        configCardLayout.show(configContainer, cardName);
    }

    /**
     * Actualiza el formulario con los datos del JSON.
     * @param jsonObject objeto JSON que contiene la configuración.
     */
    public void updateForm(JsonObject jsonObject) {

        if (jsonObject == null) return;

        isUpdatingUI = true;

        try {
            fieldsData.clear();

            if (jsonObject.has(stepType) && jsonObject.get(stepType).isJsonObject()) {
                JsonObject readerWriter = jsonObject.getAsJsonObject(stepType);

                String subKey = panelType;
                String listKey = fieldsPanelMode.fieldListKey();

                if (readerWriter.has(subKey) && readerWriter.get(subKey).isJsonObject()) {
                    JsonObject subObject = readerWriter.getAsJsonObject(subKey);

                    if (subObject.has(listKey) && subObject.get(listKey).isJsonArray()) {
                        var jsonArray = subObject.getAsJsonArray(listKey);

                        for (int i = 0; i < jsonArray.size(); i++) {
                            JsonObject item = jsonArray.get(i).getAsJsonObject();
                            FieldsData data = parseFixedFieldData(item, i);
                            fieldsData.add(data);
                        }
                    }
                }
            }

            fieldCounter = fieldsData.size();
            tableSplitter.reloadData(fieldsData);

        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Lee del JSON el valor de una clave de forma segura (sin NullPointerException).
     * Si no existe o es nulo, devuelve cadena vacía "".
     */
    private String getSafeString(JsonObject obj, String key) {
        if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "";
    }

    /**
     * Lee del JSON los campos específicos del tipo de dato y los aplica al FieldsData.
     */
    private void readTypeFieldsFromJson(JsonObject item, String type, FieldsData data) {
        switch (type) {
            case "String" -> {
                data.setFieldLength(getSafeString(item, "fieldLength"));
                data.setFieldRegex(getSafeString(item, "fieldRegex"));
            }
            case "Integer", "Double" -> {
                data.setFieldFormat(getSafeString(item, "fieldFormat"));
                data.setDecimalDelimiter(getSafeString(item, "decimalDelimiter"));
                data.setGroupingDelimiter(getSafeString(item, "groupingDelimiter"));
            }
            case "Long" -> {
                data.setFieldFormat(getSafeString(item, "fieldFormat"));
                data.setDecimalDelimiter(getSafeString(item, "decimalDelimiter"));
            }
            case "Date" -> {
                data.setFieldFormat(getSafeString(item, "fieldFormat"));
                data.setLanguage(getSafeString(item, "language"));
                data.setCountry(getSafeString(item, "country"));
            }
        }
    }

    /**
     * Escribe en el JsonObject los campos específicos del tipo de dato desde un FieldsData.
     */
    private void writeTypeFieldsToJson(String type, FieldsData data, JsonObject item) {
        switch (type) {
            case "String" -> {
                item.addProperty("fieldLength", data.getFieldLength());
                item.addProperty("fieldRegex", data.getFieldRegex());
            }
            case "Integer", "Double" -> {
                item.addProperty("fieldFormat", data.getFieldFormat());
                item.addProperty("decimalDelimiter", data.getDecimalDelimiter());
                item.addProperty("groupingDelimiter", data.getGroupingDelimiter());
            }
            case "Long" -> {
                item.addProperty("fieldFormat", data.getFieldFormat());
                item.addProperty("decimalDelimiter", data.getDecimalDelimiter());
            }
            case "Date" -> {
                item.addProperty("fieldFormat", data.getFieldFormat());
                item.addProperty("language", data.getLanguage());
                item.addProperty("country", data.getCountry());
            }
        }
    }

    /**
     * Sincroniza desde la UI los campos específicos del tipo de dato hacia el FieldsData seleccionado.
     */
    private void syncTypeFieldsFromUI(String type, FieldsData data) {
        switch (type) {
            case "String" -> {
                data.setFieldLength(fieldLength.getText());
                data.setFieldRegex(regularExpression.getTextArea().getText());
            }
            case "Integer", "Double" -> {
                data.setFieldFormat(doubleFieldFormat.getText());
                data.setDecimalDelimiter(doubleDecimalDelimiter.getText());
                data.setGroupingDelimiter(doubleGroupingDelimiter.getText());
            }
            case "Long" -> {
                data.setFieldFormat(longFieldFormat.getText());
                data.setDecimalDelimiter(longDecimalDelimiter.getText());
            }
            case "Date" -> {
                data.setFieldFormat(dateFieldFormat.getText());
                data.setLanguage(language.getText());
                data.setCountry(country.getText());
            }
        }
    }

    /**
     * Parsea un elemento JSON de la lista de campos y lo convierte en un FieldsData.
     *
     * @param item  objeto JSON que representa un campo.
     * @param index índice del campo dentro de la lista, usado para generar el id.
     * @return instancia de FieldsData inicializada con los datos del JSON.
     */
    private FieldsData parseFixedFieldData(JsonObject item, int index) {
        FieldsData data = new FieldsData(String.format("%02d", index + 1));

        data.setName(getSafeString(item, "fieldName"));
        data.setCsvInternalName(getSafeString(item, "csvInternalName"));
        data.setXpath(getSafeString(item, "xpath"));
        data.setJsonPath(getSafeString(item, "jsonPath"));

        if (item.has("fieldType")) {
            String type = item.get("fieldType").getAsString();
            data.setType(type);
            readTypeFieldsFromJson(item, type, data);
        }
        return data;
    }

    /**
     * Actualiza el objeto JSON con los datos ingresados por el usuario en el formulario.
     * Se llama cada vez que el usuario hace un cambio en el formulario para sincronizar Form -> JSON.
     *
     * @param jsonObject objeto JSON que se actualizará con los datos del formulario.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;
        String subKey = panelType;
        String listKey = fieldsPanelMode.fieldListKey();

        fieldsData.clear();
        fieldsData.addAll(tableSplitter.getDataList());

        syncCurrentSelectionFromUI();
        if (fieldsData.isEmpty()) {
            if (jsonObject.has(stepType)) {
                JsonObject readerWriter = jsonObject.getAsJsonObject(stepType);
                if (Objects.equals(panelType, PanelType.FIXED.getValue()) ||
                        Objects.equals(panelType, PanelType.XML.getValue()) ||
                        Objects.equals(panelType, PanelType.CSV.getValue())) {
                    if (readerWriter.has(subKey)) {
                        JsonObject subObject = readerWriter.getAsJsonObject(subKey);
                        subObject.remove(listKey);
                        if (subObject.entrySet().isEmpty()) {
                            readerWriter.remove(subKey);
                        }
                    }
                }
            }
            return;
        }

        JsonArray fieldDataArray = new JsonArray();
        for (FieldsData data : fieldsData) {
            JsonObject item = new JsonObject();
            String type = (data.getType() == null) ? "" : data.getType();
            item.addProperty("fieldType", type);
            item.addProperty("fieldName", data.getName());

            if (fieldsPanelMode.usesPathExpressions()) {
                if ("apiRequest".equals(panelType)) {
                    item.addProperty("jsonPath", data.getJsonPath());
                } else {
                    item.addProperty("xpath", data.getXpath());
                }
            } else {
                item.addProperty("csvInternalName", data.getCsvInternalName());
            }

            writeTypeFieldsToJson(type, data, item);
            fieldDataArray.add(item);
        }

        if (!jsonObject.has(stepType)) {
            jsonObject.add(stepType, new JsonObject());
        }
        JsonObject readerWriter = jsonObject.getAsJsonObject(stepType);

        if ("apiRequest".equals(panelType)) {
            if (!readerWriter.has("apiRequest")) {
                readerWriter.add("apiRequest", new JsonObject());
            }
            JsonObject apiRequestObj = readerWriter.getAsJsonObject("apiRequest");
            apiRequestObj.add(listKey, fieldDataArray);
        } else {
            if (!readerWriter.has(subKey)) {
                readerWriter.add(subKey, new JsonObject());
            }
            JsonObject subObject = readerWriter.getAsJsonObject(subKey);
            subObject.add(listKey, fieldDataArray);
        }
    }

    /**
     * Sincroniza los datos de los campos visuales actuales hacia el objeto Java seleccionado.
     * Esto asegura que lo que el usuario acaba de escribir (y quizás no ha perdido el foco)
     * se guarde en el modelo antes de generar el JSON.
     */
    private void syncCurrentSelectionFromUI() {
        if (currentSelection == null) return;

        currentSelection.setName(fieldNameInput.getText());
        currentSelection.setFieldName(fieldNameInput.getText());

        if (fieldsPanelMode.usesPathExpressions()) {
            if ("apiRequest".equals(panelType)) {
                currentSelection.setJsonPath(xpathField.getText());
            } else {
                currentSelection.setXpath(xpathField.getText());
            }
        } else {
            if (internalNameCombo != null && internalNameCombo.getSelectedItem() != null) {
                currentSelection.setCsvInternalName(internalNameCombo.getSelectedItem().toString());
            }
        }

        Object selectedTypeObj = fieldTypeCombo.getSelectedItem();
        String type = (selectedTypeObj != null) ? selectedTypeObj.toString() : "";

        currentSelection.setType(type);
        currentSelection.setFieldType(type);

        syncTypeFieldsFromUI(type, currentSelection);
    }

}
