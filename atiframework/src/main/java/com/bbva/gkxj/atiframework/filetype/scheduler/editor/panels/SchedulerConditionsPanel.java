package com.bbva.gkxj.atiframework.filetype.scheduler.editor.panels;

import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.bbva.gkxj.atiframework.components.AtiScriptPanel;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.*;
import icons.AtiIcons;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.*;

/**
 * Panel principal que gestiona la edición de la lista de condiciones
 * en el scheduler, incluyendo la tabla lateral y el formulario de detalle.
 */
public class SchedulerConditionsPanel extends JPanel {

    /**
     * Nombre de la carta que contiene los campos específicos para File Watcher.
     */
    private static final String CARD_FILE_WATCHER = "CARD_FW";
    /**
     * Nombre de la carta que contiene los campos específicos para Nova.
     */
    private static final String CARD_NOVA = "CARD_NOVA";
    /**
     * Nombre de la carta que contiene los campos específicos para Mongo.
     */
    private static final String CARD_MONGO = "CARD_MONGO";

    /**
     * Nombre de la carta que contiene los campos específicos para SQL.
     */
    private static final String CARD_SQL = "SQL Query";

    /**
     * Contenedor que usa CardLayout para mostrar los campos específicos según el tipo.
     */
    private JPanel specificFieldsContainer;
    /**
     * Layout tipo tarjeta para `specificFieldsContainer`.
     */
    private CardLayout cardLayout;

    /**
     * Proyecto de IntelliJ asociado al panel.
     */
    private final Project myProject;

    /**
     * Documento (texto) asociado al fichero que se edita.
     */
    private final Document myDocument;

    /**
     * Split pane principal que divide sidebar y detalle del formulario.
     */
    private JSplitPane splitPane;

    /**
     * Panel del formulario derecho (detalle).
     */
    private JPanel formPanel;

    /**
     * Scroll pane que envuelve `formPanel`.
     */
    private JScrollPane formScrollPane;

    /**
     * Panel vacío mostrado cuando no hay selección.
     */
    private JPanel emptyPanel;

    /**
     * Modelo de la tabla de condiciones.
     */
    private DefaultTableModel tableModel;

    /**
     * Tabla que lista las condiciones en la barra lateral.
     */
    private JTable table;

    /**
     * Etiqueta que muestra el contador de condiciones.
     */
    private JLabel countLabel;

    /**
     * Campo de texto para el nombre de la condición.
     */
    private JTextField nameField;

    /**
     * Combo para seleccionar el tipo de condición.
     */
    private JComboBox<String> typeCombo;

    /**
     * Spinner para el intervalo de comprobación.
     */
    private JSpinner checkEverySpinner;

    /**
     * Combo para la unidad del intervalo (minutes/hours/seconds).
     */
    private JComboBox<String> unitCombo;

    /**
     * Checkbox que indica si la condición es forzada al final.
     */
    private JCheckBox forcedCheck;

    /**
     * Área de texto para el script asociado a la condición.
     */
    private AtiScriptPanel scriptArea;

    /**
     * Área de texto para el script asociado a la condición.
     */
    private AtiScriptPanel sqlArea;

    /**
     * Campo para la ruta en File Watcher.
     */
    private JTextField fwPathField;

    /**
     * Campo para el patrón de fichero en File Watcher.
     */
    private JTextField fwPatternField;

    /**
     * Campo para el nombre del parámetro de fichero en File Watcher.
     */
    private JTextField fwParamField;

    /**
     * Campo para la ruta en Nova.
     */
    private JTextField novaPathField;

    /**
     * Campo para el patrón de fichero en Nova.
     */
    private JTextField novaPatternField;

    /**
     * Campo para el nombre del parámetro de fichero en Nova.
     */
    private JTextField novaParamField;

    /**
     * Campo para el nombre de transfer en Nova.
     */
    private JTextField novaTransferNameField;

    /**
     * Campo para la colección en consultas Mongo.
     */
    private JTextField mongoCollectionField;

    /**
     * Combo para la fuente de base de datos en consultas Mongo.
     */
    private JComboBox<String> mongoDbSourceCombo;

    /**
     * Combo para la fuente de base de datos en consultas SQL (separado para evitar compartir estado).
     */
    private JComboBox<String> sqlDbSourceCombo;

    /**
     * Modelo de la tabla de filtros (subtabla dentro de Mongo).
     */
    private DefaultTableModel filtersTableModel;

    /**
     * Tabla que muestra los filtros de la consulta Mongo.
     */
    private JTable filtersTable;

    /**
     * Etiqueta que muestra el número de filtros actuales.
     */
    private JLabel filtersCountLabel;

    /**
     * Lista en memoria de ConditionData que representa el modelo del panel.
     */
    private List<ConditionData> conditionsList = new ArrayList<>();

    /**
     * Condición actualmente seleccionada en el formulario (puede ser null).
     */
    private ConditionData currentSelection = null;

    /**
     * Indicador para evitar ciclos de actualización UI -> modelo -> UI.
     */
    private boolean isUpdatingUI = false;

    /**
     * DocumentListener externo proporcionado por el editor padre.
     */
    private javax.swing.event.DocumentListener parentTextListener;

    /**
     * ActionListener externo proporcionado por el editor padre.
     */
    private ActionListener parentActionListener;

    /**
     * Crea un nuevo ConditionsPanel asociado a un proyecto y un fichero.
     *
     * @param project proyecto de IntelliJ donde se está editando.
     * @param file    fichero virtual cuyo contenido JSON se edita en este panel.
     */
    public SchedulerConditionsPanel(@NotNull Project project, @NotNull VirtualFile file) {
        setLayout(new BorderLayout());
        this.myProject = project;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);

        this.emptyPanel = new JPanel();
        this.emptyPanel.setBackground(SchedulerTheme.BG_MAIN);

        this.formPanel = createMainFormPanel();

        this.formScrollPane = new JScrollPane(this.formPanel);
        this.formScrollPane.setBorder(null);
        this.formScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel sidebar = createSidebar();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, emptyPanel);
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     *
     * @param textListener   listener para cambios en campos de texto.
     * @param actionListener listener para acciones (combos, botones, etc.).
     * @param changeListener listener para cambios en componentes tipo spinner.
     */
    public void addFieldListeners(javax.swing.event.DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        this.parentTextListener = textListener;
        this.parentActionListener = actionListener;

        if (nameField != null) nameField.getDocument().addDocumentListener(textListener);
        if (checkEverySpinner != null) checkEverySpinner.addChangeListener(changeListener);
        if (unitCombo != null) unitCombo.addActionListener(actionListener);
        if (forcedCheck != null) forcedCheck.addActionListener(actionListener);
        if (scriptArea != null) scriptArea.getDocument().addDocumentListener(textListener);

        attachParentListenerToSpecifics(textListener, actionListener);
    }

    /**
     * Añade los listeners del padre a los campos específicos de cada tipo de condición.
     *
     * @param textListener   listener de texto del editor padre.
     * @param actionListener listener de acción del editor padre.
     */
    private void attachParentListenerToSpecifics(javax.swing.event.DocumentListener textListener, ActionListener actionListener) {
        // File Watcher
        if (fwPathField != null) fwPathField.getDocument().addDocumentListener(textListener);
        if (fwPatternField != null) fwPatternField.getDocument().addDocumentListener(textListener);
        if (fwParamField != null) fwParamField.getDocument().addDocumentListener(textListener);

        // Nova
        if (novaPathField != null) novaPathField.getDocument().addDocumentListener(textListener);
        if (novaPatternField != null) novaPatternField.getDocument().addDocumentListener(textListener);
        if (novaParamField != null) novaParamField.getDocument().addDocumentListener(textListener);
        if (novaTransferNameField != null) novaTransferNameField.getDocument().addDocumentListener(textListener);

        // Mongo
        if (mongoCollectionField != null) mongoCollectionField.getDocument().addDocumentListener(textListener);
        if (mongoDbSourceCombo != null) mongoDbSourceCombo.addActionListener(actionListener);

        // SQL
        if (sqlDbSourceCombo != null) sqlDbSourceCombo.addActionListener(actionListener);
        if (sqlArea != null) sqlArea.getDocument().addDocumentListener(textListener);
    }

    /**
     * Carga la lista de condiciones del objeto JSON en el modelo y actualiza la UI.
     * Mantiene la selección previa de la tabla si es posible.
     *
     * @param jsonObject objeto JSON raíz que contiene la propiedad conditionList.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("conditionList")) return;

        int selectedRow = table.getSelectedRow();
        isUpdatingUI = true;

        try {
            conditionsList.clear();
            tableModel.setRowCount(0);

            JsonArray arr = jsonObject.getAsJsonArray("conditionList");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject item = arr.get(i).getAsJsonObject();
                ConditionData data = parseConditionData(item, i);
                conditionsList.add(data);
                tableModel.addRow(new Object[]{data.getId(), data.getName(), data.getType(), ""});
            }
            updateCountLabel();

            if (selectedRow != -1 && selectedRow < conditionsList.size()) {
                table.setRowSelectionInterval(selectedRow, selectedRow);
                currentSelection = conditionsList.get(selectedRow);
                loadDataIntoForm(currentSelection);
                showFormPanel(true);
            } else {
                currentSelection = null;
                showFormPanel(false);
            }

        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Parsea un elemento JSON de la lista de condiciones y lo convierte en un ConditionData.
     *
     * @param item  objeto JSON que representa una condición.
     * @param index índice de la condición dentro de la lista, usado para generar el id.
     * @return instancia de ConditionData inicializada con los datos del JSON.
     */
    private ConditionData parseConditionData(JsonObject item, int index) {
        ConditionData data = new ConditionData(String.format("%02d", index + 1));

        if (item.has("name")) data.setName(item.get("name").getAsString());
        if (item.has("forceAtEnd")) data.setForcedAtEnd(item.get("forceAtEnd").getAsBoolean());
        if (item.has("script")) data.setScript(item.get("script").getAsString());

        if (item.has("checkEvery")) {
            int totalSeconds = item.get("checkEvery").getAsInt();
            if (totalSeconds > 0 && totalSeconds % 3600 == 0) {
                data.setCheckEveryValue(totalSeconds / 3600);
                data.setCheckEveryUnit("hours");
            } else if (totalSeconds > 0 && totalSeconds % 60 == 0) {
                data.setCheckEveryValue(totalSeconds / 60);
                data.setCheckEveryUnit("minutes");
            } else {
                data.setCheckEveryValue(totalSeconds);
                data.setCheckEveryUnit("seconds");
            }
        }

        if (item.has("fileWatcher")) {
            data.setType(TYPE_FILE_WATCHER);
            JsonObject fw = item.getAsJsonObject("fileWatcher");
            if (fw.has("path")) data.setFilePath(fw.get("path").getAsString());
            if (fw.has("filePattern")) data.setFilePattern(fw.get("filePattern").getAsString());
            if (fw.has("fileParameterName")) data.setParamName(fw.get("fileParameterName").getAsString());
        } else if (item.has("novaTransferWatcher")) {
            data.setType(TYPE_NOVA);
            JsonObject nov = item.getAsJsonObject("novaTransferWatcher");
            if (nov.has("path")) data.setFilePath(nov.get("path").getAsString());
            if (nov.has("filePattern")) data.setFilePattern(nov.get("filePattern").getAsString());
            if (nov.has("fileParameterName")) data.setParamName(nov.get("fileParameterName").getAsString());
            if (nov.has("transferName")) data.setNovaTransferName(nov.get("transferName").getAsString());
        } else if (item.has("query")) {
            JsonObject query = item.getAsJsonObject("query");
            if (query.has("dbSource")) data.setDbSource(query.get("dbSource").getAsString());
            if (query.has("mongoQuery")) {
                data.setType(TYPE_MONGO);
                JsonObject mongo = query.getAsJsonObject("mongoQuery");
                if (mongo.has("collectionName")) data.setCollection(mongo.get("collectionName").getAsString());
                if (mongo.has("filter")) {
                    JsonArray filtersArr = mongo.getAsJsonArray("filter");
                    for (int j = 0; j < filtersArr.size(); j++) {
                        data.getFiltersList().add(new FilterData(String.format("%02d", j + 1), filtersArr.get(j).getAsString()));
                    }
                }
            } else if (query.has("sqlQuery")) {
                data.setType(TYPE_SQL);
                data.setSqlQuery(query.get("sqlQuery").getAsString());
                if (query.has("dbSource")) data.setDbSource(query.get("dbSource").getAsString());
            }
        }
        return data;
    }

    /**
     * Actualiza el JsonObject recibido con la lista de condiciones presentes en la UI
     * y escribe el contenido completo formateado en el documento asociado.
     *
     * @param jsonObject objeto JSON raíz que se actualizará con la propiedad conditionList.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;

        syncCurrentSelectionFromUI();

        JsonArray conditionListArray = new JsonArray();

        for (ConditionData data : conditionsList) {
            JsonObject item = new JsonObject();
            item.addProperty("name", data.getName());
            item.addProperty("forceAtEnd", data.isForcedAtEnd());

            int multiplier = 1;
            if ("minutes".equalsIgnoreCase(data.getCheckEveryUnit())) multiplier = 60;
            if ("hours".equalsIgnoreCase(data.getCheckEveryUnit())) multiplier = 3600;
            item.addProperty("checkEvery", data.getCheckEveryValue() * multiplier);

            item.addProperty("script", data.getScript());

            if (TYPE_FILE_WATCHER.equals(data.getType())) {
                JsonObject fw = new JsonObject();
                fw.addProperty("path", data.getFilePath());
                fw.addProperty("filePattern", data.getFilePattern());
                fw.addProperty("fileParameterName", data.getParamName());
                item.add("fileWatcher", fw);
            } else if (TYPE_NOVA.equals(data.getType())) {
                JsonObject nov = new JsonObject();
                nov.addProperty("path", data.getFilePath());
                nov.addProperty("filePattern", data.getFilePattern());
                nov.addProperty("fileParameterName", data.getParamName());
                nov.addProperty("transferName", data.getNovaTransferName());
                item.add("novaTransferWatcher", nov);
            } else if (TYPE_MONGO.equals(data.getType())) {
                JsonObject queryWrapper = new JsonObject();
                queryWrapper.addProperty("dbSource", data.getDbSource());
                JsonObject mongoQ = new JsonObject();
                mongoQ.addProperty("collectionName", data.getCollection());
                if (!data.getFiltersList().isEmpty()) {
                    JsonArray filtersArr = new JsonArray();
                    for (FilterData fd : data.getFiltersList()) filtersArr.add(fd.getFilterValue());
                    mongoQ.add("filter", filtersArr);
                }
                queryWrapper.add("mongoQuery", mongoQ);
                item.add("query", queryWrapper);
            } else if (TYPE_SQL.equals(data.getType())) {
                JsonObject queryWrapper = new JsonObject();
                queryWrapper.addProperty("dbSource", data.getDbSource());
                queryWrapper.addProperty("sqlQuery", data.getSqlQuery());
                item.add("query", queryWrapper);
            }
            conditionListArray.add(item);
        }

        jsonObject.add("conditionList", conditionListArray);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(jsonObject));
        });
    }

    /**
     * Sincroniza TODOS los campos visibles al objeto Java antes de guardar.
     */
    private void syncCurrentSelectionFromUI() {
        if (currentSelection == null) return;

        currentSelection.setName(nameField.getText());
        currentSelection.setScript(scriptArea != null ? scriptArea.getText() : "");
        currentSelection.setForcedAtEnd(forcedCheck.isSelected());
        currentSelection.setCheckEveryValue((Integer) checkEverySpinner.getValue());
        currentSelection.setCheckEveryUnit((String) unitCombo.getSelectedItem());

        String type = currentSelection.getType();
        if (TYPE_FILE_WATCHER.equals(type)) {
            if (fwPathField != null) currentSelection.setFilePath(fwPathField.getText());
            if (fwPatternField != null) currentSelection.setFilePattern(fwPatternField.getText());
            if (fwParamField != null) currentSelection.setParamName(fwParamField.getText());
        } else if (TYPE_NOVA.equals(type)) {
            if (novaPathField != null) currentSelection.setFilePath(novaPathField.getText());
            if (novaPatternField != null) currentSelection.setFilePattern(novaPatternField.getText());
            if (novaParamField != null) currentSelection.setParamName(novaParamField.getText());
            if (novaTransferNameField != null) currentSelection.setNovaTransferName(novaTransferNameField.getText());
        } else if (TYPE_MONGO.equals(type)) {
            if (mongoCollectionField != null) currentSelection.setCollection(mongoCollectionField.getText());
            if (mongoDbSourceCombo != null) currentSelection.setDbSource((String) mongoDbSourceCombo.getSelectedItem());
        } else if (TYPE_SQL.equals(type)) {
            if (sqlDbSourceCombo != null) currentSelection.setDbSource((String) sqlDbSourceCombo.getSelectedItem());
            if (sqlArea != null) currentSelection.setSqlQuery(sqlArea.getText());
        }
    }

    /**
     * Helper para añadir un DocumentListener interno (sincroniza el modelo) y, si existe,
     * el DocumentListener externo del editor padre al mismo campo de texto.
     * @param field    campo de texto al que se añadirán los listeners.
     * @param consumer consumidor que recibe el valor actual del campo para actualizar el modelo.
     */
    private void addTextListener(JTextField field, Consumer<String> consumer) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateModel(); }
            void updateModel() { if (!isUpdatingUI) consumer.accept(field.getText()); }
        });

        if (this.parentTextListener != null) {
            field.getDocument().addDocumentListener(this.parentTextListener);
        }
    }

    /**
     * Crea el panel principal del formulario de detalle de condición, incluyendo campos comunes, área de script
     * y el contenedor con CardLayout para los campos específicos por tipo.
     *
     * @return panel de formulario principal.
     */
    private JPanel createMainFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SchedulerTheme.BG_CARD);
        form.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.weightx = 0.4;
        c.gridx = 0;
        form.add(createLabel("Condition Name"), c);
        c.weightx = 0.3;
        c.gridx = 1;
        form.add(createLabel("Type"), c);
        c.weightx = 0.15;
        c.gridx = 2;
        form.add(createLabel("Check every"), c);

        c.gridy = 1;
        c.gridx = 0;
        nameField = new JTextField();
        applyBlueFocusBorder(nameField);
        addTextListener(nameField, val -> {
            if (currentSelection != null) {
                currentSelection.setName(val);
                int r = conditionsList.indexOf(currentSelection);
                if (r != -1) tableModel.setValueAt(val, r, 1);
            }
        });
        form.add(nameField, c);

        c.gridx = 1;
        typeCombo = new JComboBox<>(new String[]{TYPE_FILE_WATCHER, TYPE_NOVA, TYPE_MONGO, TYPE_SQL});
        applyBlueFocusBorder(typeCombo);
        typeCombo.addActionListener(e -> {
            if (isUpdatingUI || currentSelection == null) return;

            String newType = (String) typeCombo.getSelectedItem();
            currentSelection.setType(newType);

            int row = conditionsList.indexOf(currentSelection);
            if (row != -1) tableModel.setValueAt(newType, row, 2);

            loadDataIntoForm(currentSelection);
            showSpecificCard(newType);

            if (parentActionListener != null) {
                parentActionListener.actionPerformed(e);
            }
        });
        form.add(typeCombo, c);

        c.gridx = 2;
        checkEverySpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        styleSpinner(checkEverySpinner);
        applyBlueFocusBorder(checkEverySpinner);
        checkEverySpinner.addChangeListener(e -> {
            if (!isUpdatingUI && currentSelection != null)
                currentSelection.setCheckEveryValue((Integer) checkEverySpinner.getValue());
        });
        form.add(checkEverySpinner, c);

        c.gridx = 3;
        unitCombo = new JComboBox<>(new String[]{"minutes", "hours", "seconds"});
        applyBlueFocusBorder(unitCombo);
        unitCombo.addActionListener(e -> {
            if (!isUpdatingUI && currentSelection != null)
                currentSelection.setCheckEveryUnit((String) unitCombo.getSelectedItem());
        });
        form.add(unitCombo, c);

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 4;
        forcedCheck = new JCheckBox("Forced At End");
        forcedCheck.setOpaque(false);
        forcedCheck.addActionListener(e -> {
            if (!isUpdatingUI && currentSelection != null)
                currentSelection.setForcedAtEnd(forcedCheck.isSelected());
        });
        form.add(forcedCheck, c);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 4;
        form.add(createLabel("Script"), c);

        c.gridy = 4;
        c.gridx = 0;
        c.weighty = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 4;
        scriptArea = new AtiScriptPanel(6);
        scriptArea.setSyntaxStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);

        scriptArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            void update() { if (!isUpdatingUI && currentSelection != null) currentSelection.setScript(scriptArea.getText()); }
        });
        form.add(scriptArea, c);

        c.gridy = 5;
        c.gridx = 0;
        c.gridwidth = 4;
        c.weighty = 1.0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);

        initSpecificCardsPanel();
        form.add(specificFieldsContainer, c);
        return form;
    }

    /**
     * Inicializa el contenedor con CardLayout que agrupa los paneles
     * específicos de cada tipo de condición (FileWatcher, Nova, Mongo).
     */
    private void initSpecificCardsPanel() {
        cardLayout = new CardLayout();
        specificFieldsContainer = new JPanel(cardLayout);
        specificFieldsContainer.setBackground(SchedulerTheme.BG_MAIN);
        specificFieldsContainer.add(createFileWatcherPanel(), CARD_FILE_WATCHER);
        specificFieldsContainer.add(createNovaPanel(), CARD_NOVA);
        specificFieldsContainer.add(createMongoPanel(), CARD_MONGO);
        specificFieldsContainer.add(createSqlPanel(), CARD_SQL);
    }

    /**
     * Muestra la carta de campos específicos correspondiente al tipo de condición indicado.
     *
     * @param type tipo de condición (FILE_WATCHER, NOVA, MONGO).
     */
    private void showSpecificCard(String type) {
        if (specificFieldsContainer == null) return;

        switch (type) {
            case TYPE_NOVA:
                cardLayout.show(specificFieldsContainer, CARD_NOVA);
                break;
            case TYPE_MONGO:
                cardLayout.show(specificFieldsContainer, CARD_MONGO);
                break;
            case TYPE_SQL:
                cardLayout.show(specificFieldsContainer, CARD_SQL);
                break;
            case TYPE_FILE_WATCHER:
            default:
                cardLayout.show(specificFieldsContainer, CARD_FILE_WATCHER);
                break;
        }
    }

    /**
     * Crea el panel con los campos específicos del tipo File Watcher.
     *
     * @return panel de campos específicos de File Watcher.
     */
    private JPanel createFileWatcherPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 8, 10, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.weightx = 0.4;
        c.gridx = 0;
        p.add(createLabel("File Path"), c);
        c.weightx = 0.3;
        c.gridx = 1;
        p.add(createLabel("File Pattern"), c);
        c.weightx = 0.3;
        c.gridx = 2;
        p.add(createLabel("Param Name"), c);

        c.gridy = 1;
        fwPathField = new JTextField();
        applyBlueFocusBorder(fwPathField);
        addTextListener(fwPathField, val -> {
            if (currentSelection != null) currentSelection.setFilePath(val);
        });
        c.gridx = 0;
        p.add(fwPathField, c);

        fwPatternField = new JTextField();
        applyBlueFocusBorder(fwPatternField);
        addTextListener(fwPatternField, val -> {
            if (currentSelection != null) currentSelection.setFilePattern(val);
        });
        c.gridx = 1;
        p.add(fwPatternField, c);

        fwParamField = new JTextField();
        applyBlueFocusBorder(fwParamField);
        addTextListener(fwParamField, val -> {
            if (currentSelection != null) currentSelection.setParamName(val);
        });
        c.gridx = 2;
        p.add(fwParamField, c);

        c.gridy = 2;
        c.weighty = 1.0;
        p.add(new JPanel() {{
            setOpaque(false);
        }}, c);
        return p;
    }

    /**
     * Crea el panel con los campos específicos del tipo Nova.
     *
     * @return panel de campos específicos de Nova.
     */
    private JPanel createNovaPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 8, 10, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.weightx = 0.4;
        c.gridx = 0;
        p.add(createLabel("File Path"), c);
        c.weightx = 0.3;
        c.gridx = 1;
        p.add(createLabel("File Pattern"), c);
        c.weightx = 0.3;
        c.gridx = 2;
        p.add(createLabel("Param Name"), c);

        c.gridy = 1;
        novaPathField = new JTextField();
        applyBlueFocusBorder(novaPathField);
        addTextListener(novaPathField, val -> {
            if (currentSelection != null) currentSelection.setFilePath(val);
        });
        c.gridx = 0;
        p.add(novaPathField, c);

        novaPatternField = new JTextField();
        applyBlueFocusBorder(novaPatternField);
        addTextListener(novaPatternField, val -> {
            if (currentSelection != null) currentSelection.setFilePattern(val);
        });
        c.gridx = 1;
        p.add(novaPatternField, c);

        novaParamField = new JTextField();
        applyBlueFocusBorder(novaParamField);
        addTextListener(novaParamField, val -> {
            if (currentSelection != null) currentSelection.setParamName(val);
        });
        c.gridx = 2;
        p.add(novaParamField, c);

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(15, 8, 2, 8);
        p.add(createLabel("Nova Transfer Name"), c);

        c.gridy = 3;
        c.insets = new Insets(2, 8, 10, 8);
        novaTransferNameField = new JTextField();
        applyBlueFocusBorder(novaTransferNameField);
        addTextListener(novaTransferNameField, val -> {
            if (currentSelection != null) currentSelection.setNovaTransferName(val);
        });
        p.add(novaTransferNameField, c);

        c.gridy = 4;
        c.weighty = 1.0;
        p.add(new JPanel() {{
            setOpaque(false);
        }}, c);
        return p;
    }

    /**
     * Crea el panel con los campos específicos del tipo Mongo, incluyendo
     * la subtabla de filtros de consulta.
     *
     * @return panel de campos específicos de Mongo.
     */
    private JPanel createMongoPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 8, 10, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        Dimension fieldSize = new Dimension(220, 32);

        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.0;
        p.add(createLabel("Collection"), c);
        c.gridx = 1;
        p.add(createLabel("Db Source"), c);

        c.gridy = 1;
        c.gridx = 0;
        mongoCollectionField = new JTextField();
        mongoCollectionField.setPreferredSize(fieldSize);
        applyBlueFocusBorder(mongoCollectionField);
        addTextListener(mongoCollectionField, val -> {
            if (currentSelection != null) currentSelection.setCollection(val);
        });
        p.add(mongoCollectionField, c);

        c.gridx = 1;
        mongoDbSourceCombo = new JComboBox<>(DB_SOURCE_OPTIONS);
        mongoDbSourceCombo.setPreferredSize(fieldSize);
        applyBlueFocusBorder(mongoDbSourceCombo);
        mongoDbSourceCombo.addActionListener(e -> {
            if (currentSelection != null) currentSelection.setDbSource((String) mongoDbSourceCombo.getSelectedItem());
        });
        if (parentActionListener != null) mongoDbSourceCombo.addActionListener(parentActionListener);
        p.add(mongoDbSourceCombo, c);

        c.gridx = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(Box.createHorizontalGlue(), c);

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(20, 8, 5, 8);
        JLabel title = new JLabel("Aggregate Query");
        title.setFont(new Font("Lato", Font.PLAIN, 18));
        title.setForeground(SchedulerTheme.TEXT_MAIN);
        p.add(title, c);

        c.gridy = 3;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 8, 10, 8);
        p.add(createFiltersSubTable(), c);
        return p;
    }

    /**
     * Crea el panel con los campos específicos del tipo SQL, incluyendo el filtro de consulta
     *
     * @return panel de campos específicos de SQL.
     */
    private JPanel createSqlPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        Dimension fieldSize = new Dimension(220, 32);

        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.0;
        p.add(createLabel("Db Source"), c);

        c.gridy = 1;
        c.gridx = 0;
        sqlDbSourceCombo = new JComboBox<>(DB_SOURCE_OPTIONS);
        sqlDbSourceCombo.setPreferredSize(fieldSize);
        applyBlueFocusBorder(sqlDbSourceCombo);
        sqlDbSourceCombo.addActionListener(e -> {
            if (currentSelection != null) currentSelection.setDbSource((String) sqlDbSourceCombo.getSelectedItem());
        });
        if (parentActionListener != null) sqlDbSourceCombo.addActionListener(parentActionListener);
        p.add(sqlDbSourceCombo, c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(Box.createHorizontalGlue(), c);

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.insets = new Insets(15, 8, 5, 8);
        p.add(createLabel("SQL Query"), c);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 8, 10, 8);

        sqlArea = new AtiScriptPanel(6);
        sqlArea.setSyntaxStyle(SyntaxConstants.SYNTAX_STYLE_SQL);

        sqlArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            void update() { if (!isUpdatingUI && currentSelection != null) currentSelection.setSqlQuery(sqlArea.getText()); }
        });
        p.add(sqlArea, c);

        return p;
    }

    /**
     * Crea el panel que contiene la tabla de filtros de Mongo, su cabecera
     * con contador y botón de creación, así como los listeners de edición y borrado.
     *
     * @return panel con la subtabla de filtros.
     */
    private JPanel createFiltersSubTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(SchedulerTheme.BORDER_SOFT));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(5, 10, 5, 10));

        filtersCountLabel = new JLabel("Filters(0)");
        filtersCountLabel.setFont(new Font("Lato", Font.PLAIN, 12));
        filtersCountLabel.setForeground(Color.GRAY);

        JButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> {
            if (currentSelection == null) return;
            String newId = String.format("%02d", currentSelection.getFiltersList().size() + 1);
            currentSelection.getFiltersList().add(new FilterData(newId, "new Filter"));

            filtersCountLabel.setText("Filters(" + currentSelection.getFiltersList().size() + ")");
            filtersTableModel.addRow(new Object[]{newId, "new Filter", ""});

            if (parentActionListener != null) parentActionListener.actionPerformed(e);
        });

        header.add(filtersCountLabel, BorderLayout.WEST);
        header.add(btn, BorderLayout.EAST);

        String[] cols = {"#", "Filter", "Actions"};
        filtersTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return c == 1;
            }
        };

        filtersTable = new JTable(filtersTableModel);
        filtersTable.setRowHeight(25);
        filtersTable.setShowGrid(false);
        filtersTable.getColumnModel().getColumn(FILTER_ACTION_COLUMN_INDEX).setCellRenderer(new MoreActionsRenderer());

        filtersTable.getDefaultEditor(Object.class).addCellEditorListener(new CellEditorListener() {
            public void editingStopped(ChangeEvent e) {
                if (currentSelection == null) return;
                int row = filtersTable.getSelectedRow();
                if (row != -1 && row < currentSelection.getFiltersList().size()) {
                    currentSelection.getFiltersList().get(row).setFilterValue((String) filtersTable.getValueAt(row, 1));
                    if (parentActionListener != null)
                        parentActionListener.actionPerformed(new ActionEvent(filtersTable, ActionEvent.ACTION_PERFORMED, "TableEdited"));
                }
            }

            public void editingCanceled(ChangeEvent e) {
            }
        });
        addActionsListener(filtersTable, true);

        JScrollPane sp = new JScrollPane(filtersTable);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(0, 200));
        panel.add(header, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Crea el panel lateral (sidebar) que contiene la tabla de condiciones
     * y el botón para crear nuevas condiciones.
     *
     * @return panel lateral con la lista de condiciones.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SchedulerTheme.BG_MAIN);
        sidebar.setBorder(new EmptyBorder(15, 15, 15, 15));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        countLabel = new JLabel("Nº of elements (0)");
        countLabel.setFont(new Font("Lato", Font.PLAIN, 13));
        countLabel.setForeground(SchedulerTheme.TEXT_MAIN);
        JButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> {
            addNewCondition();
            if (parentActionListener != null) parentActionListener.actionPerformed(e);
        });
        header.add(countLabel, BorderLayout.WEST);
        header.add(btn, BorderLayout.EAST);

        String[] cols = {"#", "Condition Name", "Type", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setSelectionBackground(new Color(232, 244, 253));
        table.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setCellRenderer(new MoreActionsRenderer());
        addActionsListener(table, false);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1 && row < conditionsList.size()) {
                    currentSelection = conditionsList.get(row);
                    loadDataIntoForm(currentSelection);
                    showFormPanel(true);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new EmptyBorder(10, 0, 0, 0));
        sp.getViewport().setBackground(Color.WHITE);
        sidebar.add(header, BorderLayout.NORTH);
        sidebar.add(sp, BorderLayout.CENTER);
        return sidebar;
    }

    /**
     * Crea una nueva condición vacía, la añade al modelo y selecciona su fila
     * en la tabla lateral, actualizando también el contador.
     */
    private void addNewCondition() {
        int n = conditionsList.size() + 1;
        ConditionData d = new ConditionData(String.format("%02d", n));
        conditionsList.add(d);
        tableModel.addRow(new Object[]{d.getId(), d.getName(), d.getType(), ""});
        updateCountLabel();
        table.setRowSelectionInterval(n - 1, n - 1);
    }

    /**
     * Elimina la condición en la posición indicada, reindexa los ids restantes
     * y actualiza la selección y el panel de formulario.
     *
     * @param row índice de la fila/condición a eliminar.
     */
    private void deleteCondition(int row) {
        if (row < 0 || row >= conditionsList.size()) return;
        conditionsList.remove(row);
        tableModel.removeRow(row);
        for (int i = 0; i < conditionsList.size(); i++) {
            String id = String.format("%02d", i + 1);
            conditionsList.get(i).setId(id);
            tableModel.setValueAt(id, i, 0);
        }
        updateCountLabel();
        if (conditionsList.isEmpty()) {
            showFormPanel(false);
            currentSelection = null;
        } else {
            int s = Math.max(0, row - 1);
            table.setRowSelectionInterval(s, s);
        }
        if(parentActionListener!=null)
            parentActionListener.actionPerformed(new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "ConditionDeleted"));
    }


    /**
     * Actualiza la etiqueta de contador de condiciones en el panel lateral.
     */
    private void updateCountLabel() {
        countLabel.setText("Nº of elements (" + conditionsList.size() + ")");
    }

    /**
     * Muestra u oculta el formulario de detalle a la derecha del JSplitPane,alternando entre el formulario y un panel vacío.
     *
     * @param show true para mostrar el formulario, false para ocultarlo.
     */
    private void showFormPanel(boolean show) {
        Component right = show ? formScrollPane : emptyPanel;
        if (splitPane.getRightComponent() != right) {
            splitPane.setRightComponent(right);
            splitPane.revalidate();
            splitPane.repaint();
        }
    }

    /**
     * Carga todos los datos de una ConditionData concreta en los campos de la UI,
     * incluyendo los específicos y la tabla de filtros, y muestra la carta adecuada.
     *
     * @param data condición cuyos datos se van a reflejar en el formulario.
     */
    private void loadDataIntoForm(ConditionData data) {
        if (data == null) return;
        isUpdatingUI = true;

        try {
            nameField.setText(data.getName());
            typeCombo.setSelectedItem(data.getType());
            checkEverySpinner.setValue(data.getCheckEveryValue());
            unitCombo.setSelectedItem(data.getCheckEveryUnit());
            forcedCheck.setSelected(data.isForcedAtEnd());
            if (scriptArea != null) scriptArea.setText(data.getScript());

            // FileWatcher Fields
            fwPathField.setText(data.getFilePath());
            fwPatternField.setText(data.getFilePattern());
            fwParamField.setText(data.getParamName());

            // Nova Fields
            novaPathField.setText(data.getFilePath());
            novaPatternField.setText(data.getFilePattern());
            novaParamField.setText(data.getParamName());
            novaTransferNameField.setText(data.getNovaTransferName());

            // Mongo Fields
            mongoCollectionField.setText(data.getCollection());
            if (mongoDbSourceCombo != null) setComboBoxValueIgnoreCase(mongoDbSourceCombo, data.getDbSource());

            // SQL DB Source
            if (sqlDbSourceCombo != null) setComboBoxValueIgnoreCase(sqlDbSourceCombo, data.getDbSource());
            if (sqlArea != null) sqlArea.setText(data.getSqlQuery());

            reloadFiltersTable(data);
            showSpecificCard(data.getType());

        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Recarga por completo el contenido de la tabla de filtros a partir
     * de la lista de filtros de la condición indicada.
     *
     * @param data condición de la que se tomarán los filtros.
     */
    private void reloadFiltersTable(ConditionData data) {
        filtersTableModel.setRowCount(0);
        filtersCountLabel.setText("Filters(" + data.getFiltersList().size() + ")");
        for (FilterData f : data.getFiltersList()) {
            filtersTableModel.addRow(new Object[]{f.getId(), f.getFilterValue(), ""});
        }
    }

    /**
     * Añade un listener para gestionar el clic en la columna de acciones (3 puntos).
     * Muestra un menú popup con la opción de borrar.
     * @param table Tabla a la que se añade el listener.
     * @param isFilter true si es la tabla de filtros, false si es la de condiciones.
     */
    private void addActionsListener(JTable table, boolean isFilter) {
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                int actionColIndex = isFilter ? FILTER_ACTION_COLUMN_INDEX : ACTION_COLUMN_INDEX;
                if (r != -1 && c == actionColIndex) {
                    showActionsPopup(table, r, isFilter, e.getPoint());
                }
            }
        });
    }

    /**
     * Muestra el menú popup con la opción de eliminar.
     */
    private void showActionsPopup(JTable table, int rowIndex, boolean isFilter, Point p) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete", AtiIcons.TRASH_ICON);

        deleteItem.addActionListener(e -> {
            if (isFilter) {
                // Borrar filtro
                if (currentSelection != null) {
                    currentSelection.getFiltersList().remove(rowIndex);
                    filtersTableModel.removeRow(rowIndex);
                    // Reindexar
                    for (int i = 0; i < currentSelection.getFiltersList().size(); i++) {
                        String nid = String.format("%02d", i + 1);
                        currentSelection.getFiltersList().get(i).setId(nid);
                        filtersTableModel.setValueAt(nid, i, 0);
                    }
                    filtersCountLabel.setText("Filters(" + currentSelection.getFiltersList().size() + ")");
                    // Notificar
                    if (parentActionListener != null)
                        parentActionListener.actionPerformed(new ActionEvent(filtersTable, ActionEvent.ACTION_PERFORMED, "FilterDeleted"));
                }
            } else {
                // Borrar Condición
                deleteCondition(rowIndex);
            }
        });

        popup.add(deleteItem);
        popup.show(table, p.x, p.y);
    }

    /**
     * Renderizador que muestra siempre el icono de 3 puntos (More Actions).
     */
    private class MoreActionsRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t,v,s,f,r,c);
            setText("");
            setHorizontalAlignment(CENTER);
            setIcon(AllIcons.Actions.More);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }
    }

    /**
     * Crea una etiqueta estándar para el formulario, con fuente y color
     * acorde al tema del scheduler.
     *
     * @param t texto a mostrar en la etiqueta.
     * @return componente JLabel configurado.
     */
    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Lato", Font.BOLD, 14));
        l.setForeground(SchedulerTheme.TEXT_MAIN);
        return l;
    }
}
