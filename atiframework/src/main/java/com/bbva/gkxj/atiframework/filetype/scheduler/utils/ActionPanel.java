package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.intellij.openapi.fileEditor.FileEditorManager;
import icons.AtiIcons;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.TextFieldWithAutoCompletion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import com.bbva.gkxj.atiframework.components.AtiScriptPanel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.*;

/**
 * Panel principal para la gestión de la sección "Action" (Batch y Queries) del Scheduler.
 */
public abstract class ActionPanel extends JPanel {

    /** Identificador de la carta para consultas Mongo. */
    private static final String CARD_MONGO = "CARD_MONGO";

    /** Identificador de la carta para consultas SQL. */
    private static final String CARD_SQL = "CARD_SQL";

    /** Proyecto de IntelliJ asociado al panel. */
    private final Project myProject;

    /** Fichero virtual cuyo contenido se está editando. */
    private final VirtualFile myFile;

    /** Documento (texto) asociado al fichero que se edita. */
    private final com.intellij.openapi.editor.Document myDocument;

    /** Campo de texto para el código del batch. */
    private TextFieldWithAutoCompletion<String> batchCodeField;

    /** Pestañas principales del panel (Batch / Queries). */
    private JBTabbedPane tabbedPane;

    /** Etiqueta que muestra el número de parámetros batch. */
    private JLabel batchCountLabel;

    /** Etiqueta que muestra el número de queries. */
    private JLabel queryCountLabel;

    /** Campo para el nombre del parámetro batch. */
    private JTextField bpNameField;

    /** Campo para el parámetro de consulta asociado al parámetro batch. */
    private JTextField bpQueryField;

    /** Campo para el valor fijo del parámetro batch. */
    private JTextField bpFixedField;

    /** Área de texto para el script asociado al parámetro batch. */
    private AtiScriptPanel bpScriptArea;

    /** Campo para el código de la query. */
    private JTextField qCodeField;

    /** Combo para seleccionar el tipo de query (MONGO/SQL). */
    private JComboBox<String> qTypeCombo;

    /** Campo para la colección en consultas Mongo. */
    private JTextField qCollectionField;

    /** Combo para seleccionar la fuente de datos en consultas Mongo. */
    private JComboBox<String> mongoDbSourceCombo;

    /** Combo para seleccionar la fuente de datos en consultas SQL. */
    private JComboBox<String> sqlDbSourceCombo;

    /** Área de texto para la consulta SQL. */
    private AtiScriptPanel qSqlArea;

    /** Contenedor que alberga las cartas específicas por tipo de query. */
    private JPanel specificQueryContainer;

    /** Layout de tipo CardLayout para alternar entre vistas de query. */
    private CardLayout queryCardLayout;

    /** Modelo de la tabla de filtros de Mongo. */
    private DefaultTableModel qFiltersModel;

    /** Tabla que muestra los filtros de una query Mongo. */
    private JTable qFiltersTable;

    /** Etiqueta que muestra el número de filtros. */
    private JLabel lblFiltersCount;

    /** Botón para añadir un nuevo filtro. */
    private JButton btnNewFilter;

    /** Modelo de la tabla de parámetros batch. */
    private DefaultTableModel batchTableModel;

    /** Tabla que lista los parámetros batch. */
    private JTable batchTable;

    /** Modelo de la tabla de queries. */
    private DefaultTableModel queryTableModel;

    /** Tabla que lista las queries. */
    private JTable queryTable;

    /** Botón para añadir un nuevo BatchParam. */
    private JButton btnNewBatchParam;

    /** Botón para añadir una nueva Query. */
    private JButton btnNewQuery;

    /** SplitPane que contiene la lista y el formulario de Batch Params. */
    private JSplitPane batchSplitPane;

    /** Panel que contiene el formulario de Batch Params. */
    private JPanel batchFormPanel;

    /** Scroll que envuelve el formulario de Batch Params. */
    private JScrollPane batchFormScroll;

    /** Panel vacío mostrado cuando no hay selección de Batch. */
    private JPanel emptyBatchPanel;

    /** SplitPane que contiene la lista y el formulario de Queries. */
    private JSplitPane querySplitPane;

    /** Panel que contiene el formulario de Queries. */
    private JPanel queryFormPanel;

    /** Scroll que envuelve el formulario de Queries. */
    private JScrollPane queryFormScroll;

    /** Panel vac��o mostrado cuando no hay selección de Query. */
    private JPanel emptyQueryPanel;

    /** Lista de parámetros batch cargados en memoria. */
    private List<BatchParamData> batchParamsList = new ArrayList<>();

    /** Lista de queries cargadas en memoria. */
    private List<QueryData> queriesList = new ArrayList<>();

    /** Parámetro batch actualmente seleccionado/edición. */
    private BatchParamData currentBatchParam = null;

    /** Query actualmente seleccionada/edición. */
    private QueryData currentQuery = null;

    /** Indicador para evitar reentradas durante la actualización de la UI. */
    private boolean isUpdatingUI = false;

    /** Listener de documento proporcionado por el editor padre. */
    private javax.swing.event.DocumentListener parentTextListener;
    /** Listener de acciones proporcionado por el editor padre */
    private ActionListener parentActionListener;

    /** Listener de cambios (spinners/u otros) proporcionado por el editor padre. */
    private ChangeListener parentChangeListener;

    /**
     * Crea un nuevo ActionPanel asociado a un proyecto y un fichero.
     *
     * @param project proyecto de IntelliJ donde se está editando.
     * @param file    fichero virtual cuyo contenido JSON se edita en este panel.
     */
    public ActionPanel(@NotNull Project project, @NotNull VirtualFile file) {
        setLayout(new BorderLayout());
        this.myProject = project;
        this.myFile = file;
        this.myDocument = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getDocument(file);

        setBackground(SchedulerTheme.BG_MAIN);

        batchCountLabel = new JLabel("Nº of elements (0)");
        queryCountLabel = new JLabel("Nº of elements (0)");

        add(createTopPanel(), BorderLayout.NORTH);

        tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("Batch Parameters", createBatchParamsTab());
        tabbedPane.addTab("Queries for Parameters", createQueriesTab());
        tabbedPane.setVisible(false);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Devuelve la clave raíz en el JSON para este panel.
     *
     * @return clave raíz como String.
     */
    protected abstract String getJsonRootKey();

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
        this.parentChangeListener = changeListener;

        if (batchCodeField != null) {
            batchCodeField.addDocumentListener(new com.intellij.openapi.editor.event.DocumentListener() {
                @Override
                public void documentChanged(@NotNull com.intellij.openapi.editor.event.DocumentEvent event) {
                    if (isUpdatingUI) return;
                    if (parentActionListener != null) {
                        parentActionListener.actionPerformed(new ActionEvent(batchCodeField, ActionEvent.ACTION_PERFORMED, "BATCH_CODE_CHANGED"));
                    }
                    String currentText = batchCodeField.getText().trim();
                    if (!currentText.isEmpty()) {
                        loadBatchParamsFromProject(currentText);
                    }
                }
            });
        }

        // Batch Params
        if (bpNameField != null) bpNameField.getDocument().addDocumentListener(textListener);
        if (bpQueryField != null) bpQueryField.getDocument().addDocumentListener(textListener);
        if (bpFixedField != null) bpFixedField.getDocument().addDocumentListener(textListener);
        if (bpScriptArea != null) bpScriptArea.getTextArea().getDocument().addDocumentListener(textListener);
        if (btnNewBatchParam != null) btnNewBatchParam.addActionListener(actionListener);

        // Queries
        if (qCodeField != null) qCodeField.getDocument().addDocumentListener(textListener);
        if (qTypeCombo != null) qTypeCombo.addActionListener(actionListener);
        if (btnNewQuery != null) btnNewQuery.addActionListener(actionListener);

        attachParentListenerToQuerySpecifics(textListener, actionListener);

        if (tabbedPane != null) tabbedPane.addChangeListener(changeListener);
    }


    /**
     * Carga los parámetros desde el archivo .batch del proyecto, actualiza UI y fuerza guardado JSON.
     */
    private void loadBatchParamsFromProject(String batchCode) {
        ApplicationManager.getApplication().runReadAction(() -> {
            Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(
                    myProject, batchCode + ".batch", GlobalSearchScope.projectScope(myProject)
            );

            if (files.isEmpty()) return;

            VirtualFile batchFile = files.iterator().next();
            PsiFile psiFile = PsiManager.getInstance(myProject).findFile(batchFile);
            if (psiFile == null) return;

            try {
                JsonObject rootObj = JsonParser.parseString(psiFile.getText()).getAsJsonObject();
                JsonArray paramList = null;
                if (rootObj.has("parameterList")) paramList = rootObj.getAsJsonArray("parameterList");
                else if (rootObj.has("parameters")) paramList = rootObj.getAsJsonArray("parameters");
                else if (rootObj.has("batch") && rootObj.getAsJsonObject("batch").has("parameterList")) paramList = rootObj.getAsJsonObject("batch").getAsJsonArray("parameterList");

                if (paramList == null || paramList.size() == 0) return;

                JsonArray finalParamList = paramList;
                ApplicationManager.getApplication().invokeLater(() -> {
                    try {
                        batchParamsList.clear();
                        batchTableModel.setRowCount(0);

                        int count = 0;
                        for (int i = 0; i < finalParamList.size(); i++) {
                            JsonElement el = finalParamList.get(i);
                            if (!el.isJsonObject()) continue;
                            JsonObject pObj = el.getAsJsonObject();

                            if (!pObj.has("schedulerParamName")) continue;

                            String pName = pObj.get("schedulerParamName").getAsString();
                            String scriptVal = pObj.has("scriptValue") ? pObj.get("scriptValue").getAsString() : "";
                            String fixedVal = pObj.has("fixedValue") ? pObj.get("fixedValue").getAsString() : "";

                            count++;
                            BatchParamData data = new BatchParamData(String.format("%02d", count));
                            data.setParamName(pName);
                            data.setScript(scriptVal);
                            data.setFixedValue(fixedVal);
                            data.setQueryParam("");

                            batchParamsList.add(data);
                            batchTableModel.addRow(new Object[]{data.getId(), data.getParamName(), ""});
                        }
                        batchCountLabel.setText("Nº of elements (" + batchParamsList.size() + ")");
                        forceSave();
                    } catch(Exception e) { e.printStackTrace(); }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void forceSave() {
        if (isUpdatingUI) return;
        try {
            String text = myDocument.getText();
            if (text != null && !text.isEmpty()) {
                JsonObject currentJson = JsonParser.parseString(text).getAsJsonObject();
                updateDocument(currentJson);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void attachParentListenerToQuerySpecifics(javax.swing.event.DocumentListener textListener, ActionListener actionListener) {
        if (qCollectionField != null) qCollectionField.getDocument().addDocumentListener(textListener);
        if (mongoDbSourceCombo != null) mongoDbSourceCombo.addActionListener(actionListener);
        if (sqlDbSourceCombo != null) sqlDbSourceCombo.addActionListener(actionListener);
        if (btnNewFilter != null) btnNewFilter.addActionListener(actionListener);

        if (qSqlArea != null) qSqlArea.getTextArea().getDocument().addDocumentListener(textListener);
    }

    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado.
     *
     * @param jsonObject objeto JSON con los datos a cargar en el formulario.
     */

    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        int selectedBatchRow = batchTable.getSelectedRow();
        int selectedQueryRow = queryTable.getSelectedRow();

        isUpdatingUI = true;
        String rootKey = getJsonRootKey();

        try {
            if (jsonObject.has(rootKey)) {
                JsonObject batchObj = jsonObject.getAsJsonObject(rootKey);
                if (batchObj.has("batchCode")) {
                    batchCodeField.setText(batchObj.get("batchCode").getAsString());
                    tabbedPane.setVisible(true);
                } else {
                    batchCodeField.setText("");
                    tabbedPane.setVisible(false);
                }
                batchParamsList.clear();
                batchTableModel.setRowCount(0);
                if (batchObj.has("jobParameterList")) {
                    JsonArray arr = batchObj.getAsJsonArray("jobParameterList");
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject o = arr.get(i).getAsJsonObject();
                        BatchParamData d = parseBatchParamData(o, i);
                        batchParamsList.add(d);
                        batchTableModel.addRow(new Object[]{d.getId(), d.getParamName(), ""});
                    }
                }
                batchCountLabel.setText("Nº of elements (" + batchParamsList.size() + ")");

                queriesList.clear();
                queryTableModel.setRowCount(0);
                if (batchObj.has("parameterQueryList")) {
                    JsonArray arr = batchObj.getAsJsonArray("parameterQueryList");
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject o = arr.get(i).getAsJsonObject();
                        QueryData d = parseQueryData(o, i);
                        queriesList.add(d);
                        queryTableModel.addRow(new Object[]{d.getId(), d.getQueryCode(), ""});
                    }
                }
                queryCountLabel.setText("Nº of elements (" + queriesList.size() + ")");
            } else {
                batchCodeField.setText("");
                tabbedPane.setVisible(false);
                clearInternalData();
                selectedBatchRow = -1;
                selectedQueryRow = -1;
            }

            if (selectedBatchRow != -1 && selectedBatchRow < batchParamsList.size()) {
                batchTable.setRowSelectionInterval(selectedBatchRow, selectedBatchRow);
                currentBatchParam = batchParamsList.get(selectedBatchRow);
                loadBatchParamForm();
                showRightPanelBatch(true);
            } else {
                currentBatchParam = null;
                showRightPanelBatch(false);
            }

            if (selectedQueryRow != -1 && selectedQueryRow < queriesList.size()) {
                queryTable.setRowSelectionInterval(selectedQueryRow, selectedQueryRow);
                currentQuery = queriesList.get(selectedQueryRow);
                loadQueryForm();
                showRightPanelQuery(true);
            } else {
                currentQuery = null;
                showRightPanelQuery(false);
            }
        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Parsea un JsonObject en un BatchParamData.
     *
     * @param jsonObject     JsonObject a parsear.
     * @param index índice del parámetro en la lista (para generar ID).
     * @return BatchParamData con los datos parseados.
     */
    private BatchParamData parseBatchParamData(JsonObject jsonObject, int index) {
        BatchParamData batchParamData = new BatchParamData(String.format("%02d", index + 1));
        if (jsonObject.has("paramName")) batchParamData.setParamName(jsonObject.get("paramName").getAsString());
        if (jsonObject.has("queryParam")) batchParamData.setQueryParam(jsonObject.get("queryParam").getAsString());
        if (jsonObject.has("fixedValue")) batchParamData.setFixedValue(jsonObject.get("fixedValue").getAsString());
        if (jsonObject.has("scriptValue")) batchParamData.setScript(jsonObject.get("scriptValue").getAsString());
        return batchParamData;
    }

    /**
     * Parsea un JsonObject en un QueryData.
     *
     * @param jsonObject     JsonObject a parsear.
     * @param index índice de la query en la lista (para generar ID).
     * @return QueryData con los datos parseados.
     */
    private QueryData parseQueryData(JsonObject jsonObject, int index) {
        QueryData queryData = new QueryData(String.format("%02d", index + 1));
        if (jsonObject.has("queryCode")) queryData.setQueryCode(jsonObject.get("queryCode").getAsString());
        if (jsonObject.has("dbSource")) queryData.setDbSource(jsonObject.get("dbSource").getAsString());

        if (jsonObject.has("sqlQuery")) {
            queryData.setType(TYPE_SQL);
            queryData.setSqlQuery(jsonObject.get("sqlQuery").getAsString());
        } else if (jsonObject.has("mongoQuery")) {
            queryData.setType(TYPE_MONGO);
            JsonObject mongoQuery = jsonObject.getAsJsonObject("mongoQuery");
            if (mongoQuery.has("collectionName")) queryData.setCollection(mongoQuery.get("collectionName").getAsString());
            if (mongoQuery.has("filter")) {
                JsonArray fa = mongoQuery.getAsJsonArray("filter");
                for (int j = 0; j < fa.size(); j++) {
                    queryData.getFilters().add(new FilterData(String.format("%02d", j + 1), fa.get(j).getAsString()));
                }
            }
        }
        return queryData;
    }

    /**
     * Actualiza el JsonObject con los datos actuales del formulario.
     *
     * @param jsonObject objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;
        syncCurrentSelectionFromUI();
        String rootKey = getJsonRootKey();

        String batchCode = batchCodeField.getText().trim();
        if (batchCode.isEmpty()) {
            if (jsonObject.has(rootKey)) jsonObject.remove(rootKey);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            WriteCommandAction.runWriteCommandAction(myProject, () -> {
                this.myDocument.setText(gson.toJson(jsonObject));
            });
            return;
        }

        JsonObject batchObj = new JsonObject();
        batchObj.addProperty("batchCode", batchCode);

        // Batch Params
        JsonArray paramsArr = new JsonArray();
        for (BatchParamData bp : batchParamsList) {
            JsonObject object = new JsonObject();
            object.addProperty("paramName", bp.getParamName());
            object.addProperty("queryParam", bp.getQueryParam());
            object.addProperty("fixedValue", bp.getFixedValue());
            object.addProperty("scriptValue", bp.getScript());
            paramsArr.add(object);
        }
        batchObj.add("jobParameterList", paramsArr);

        // Queries
        JsonArray jsonArray = new JsonArray();
        for (QueryData data : queriesList) {
            JsonObject object = new JsonObject();
            object.addProperty("queryCode", data.getQueryCode());
            object.addProperty("dbSource", data.getDbSource());

            if (TYPE_SQL.equals(data.getType())) {
                object.addProperty("sqlQuery", data.getSqlQuery());
            } else {
                JsonObject mongo = new JsonObject();
                mongo.addProperty("collectionName", data.getCollection());
                JsonArray fa = new JsonArray();
                for (FilterData fd : data.getFilters()) {
                    fa.add(fd.getFilterValue());
                }
                mongo.add("filter", fa);
                object.add("mongoQuery", mongo);
            }
            jsonArray.add(object);
        }
        batchObj.add("parameterQueryList", jsonArray);

        jsonObject.add(rootKey, batchObj);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(jsonObject));
        });
    }

    /**
     * Sincroniza los datos del formulario actual al estado interno (currentBatchParam y currentQuery).
     */
    private void syncCurrentSelectionFromUI() {
        if (currentBatchParam != null) {
            currentBatchParam.setParamName(bpNameField.getText());
            currentBatchParam.setQueryParam(bpQueryField.getText());
            currentBatchParam.setFixedValue(bpFixedField.getText());
            currentBatchParam.setScript(bpScriptArea.getText());

            int row = batchParamsList.indexOf(currentBatchParam);
            if(row != -1 && !currentBatchParam.getParamName().equals(batchTableModel.getValueAt(row, 1))) {
                batchTableModel.setValueAt(currentBatchParam.getParamName(), row, 1);
            }
        }

        if (currentQuery != null) {
            currentQuery.setQueryCode(qCodeField.getText());
            if (TYPE_SQL.equals(currentQuery.getType())) {
                currentQuery.setSqlQuery(qSqlArea.getText());
                if (sqlDbSourceCombo != null) {
                    currentQuery.setDbSource((String) sqlDbSourceCombo.getSelectedItem());
                }
            } else {
                currentQuery.setCollection(qCollectionField.getText());
                if (mongoDbSourceCombo != null) {
                    currentQuery.setDbSource((String) mongoDbSourceCombo.getSelectedItem());
                }
            }

            int row = queriesList.indexOf(currentQuery);
            if(row != -1 && !currentQuery.getQueryCode().equals(queryTableModel.getValueAt(row, 1))) {
                queryTableModel.setValueAt(currentQuery.getQueryCode(), row, 1);
            }
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
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            void update() { if(!isUpdatingUI) consumer.accept(field.getText()); }
        });
        if (this.parentTextListener != null) {
            field.getDocument().addDocumentListener(this.parentTextListener);
        }
    }

    /** Obtiene la lista de códigos batch disponibles en el proyecto para autocompletado.
     *
     * @param project proyecto de IntelliJ donde buscar los archivos .batch.
     * @return lista de códigos batch (nombres de archivos sin extensión).
     */
    private List<String> getAllBatchCodes(Project project) {
        Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(
                project,
                "batch",
                GlobalSearchScope.projectScope(project)
        );
        return files.stream()
                .map(VirtualFile::getNameWithoutExtension)
                .sorted()
                .collect(Collectors.toList());
    }

    /** Instala un MouseListener en el campo de texto para mostrar las sugerencias de autocompletado al hacer clic.
     *
     * @param field campo de texto con autocompletado al que se le instalará el listener.
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
     * Crea el panel superior con el campo Batch Code y el botón de visibilidad.
     *
     * @return JPanel con los componentes superiores.
     */
    private JPanel createTopPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        p.setBorder(new EmptyBorder(10, 15, 10, 15));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;

        c.gridx=0; c.gridy=0;
        p.add(createLabel("Batch Code"), c);

        c.gridy=1; c.weightx=1.0;

        List<String> batchVariants = getAllBatchCodes(myProject);
        TextFieldWithAutoCompletion.StringsCompletionProvider provider =
                new TextFieldWithAutoCompletion.StringsCompletionProvider(batchVariants, null);

        batchCodeField = new TextFieldWithAutoCompletion<>(myProject, provider, true, null);
        batchCodeField.setPlaceholder("Select or type batch code...");
        batchCodeField.setBackground(SchedulerTheme.BG_CARD);

        installClickToShowListener(batchCodeField);

        batchCodeField.addDocumentListener(new com.intellij.openapi.editor.event.DocumentListener() {
            @Override
            public void documentChanged(@NotNull com.intellij.openapi.editor.event.DocumentEvent event) {
                checkVisibility();
            }

            void checkVisibility() {
                if(isUpdatingUI) return;
                boolean hasText = !batchCodeField.getText().trim().isEmpty();
                tabbedPane.setVisible(hasText);
                if (!hasText) clearInternalData();
            }
        });
        p.add(batchCodeField, c);

        c.gridx=1; c.weightx=0.0; c.insets = new Insets(0, 5, 0, 0);

        JButton eyeBtn = new JButton(AllIcons.General.InspectionsEye);
        eyeBtn.setPreferredSize(new Dimension(30, 28));

        eyeBtn.addActionListener(e -> {
            String currentBatchCode = batchCodeField.getText().trim();
            if (currentBatchCode.isEmpty()) return;
            ApplicationManager.getApplication().runReadAction(() -> {

                Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(
                        myProject,
                        currentBatchCode + ".batch",
                        GlobalSearchScope.projectScope(myProject)
                );

                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!files.isEmpty()) {
                        VirtualFile fileToOpen = files.iterator().next();
                        FileEditorManager.getInstance(myProject).openFile(fileToOpen, true);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se encontró el archivo: " + currentBatchCode + ".batch",
                                "Batch no encontrado",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        });

        p.add(eyeBtn, c);
        return p;
    }

    /**
     * Limpia los datos internos y resetea las tablas y formularios.
     */
    private void clearInternalData() {
        batchParamsList.clear();
        queriesList.clear();
        batchTableModel.setRowCount(0);
        queryTableModel.setRowCount(0);
        batchCountLabel.setText("Nº of elements (0)");
        queryCountLabel.setText("Nº of elements (0)");
        currentBatchParam = null;
        showRightPanelBatch(false);
        currentQuery = null;
        showRightPanelQuery(false);
    }


    /**
     * Crea la pestaña de Batch Parameters con su tabla y formulario.
     * @return JSplitPane con la estructura de la pestaña.
     */
    private JSplitPane createBatchParamsTab() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SchedulerTheme.BG_MAIN);
        sidebar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        btnNewBatchParam = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btnNewBatchParam.addActionListener(e -> addNewBatchParam());
        header.add(batchCountLabel, BorderLayout.WEST); header.add(btnNewBatchParam, BorderLayout.EAST);

        String[] cols = {"#", "Param Name", "Actions"};
        batchTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        batchTable = createStyleTable(batchTableModel);
        batchTable.getColumnModel().getColumn(ACTION_COL).setCellRenderer(new MoreActionsRenderer());

        batchTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && batchTable.getSelectedRow() != -1 && batchTable.getSelectedRow() < batchParamsList.size()) {
                currentBatchParam = batchParamsList.get(batchTable.getSelectedRow());
                loadBatchParamForm();
                showRightPanelBatch(true);
            }
        });
        addActionsListener(batchTable, 0);

        JScrollPane scrollSide = new JScrollPane(batchTable);
        scrollSide.setBorder(new EmptyBorder(5,0,0,0));
        sidebar.add(header, BorderLayout.NORTH);
        sidebar.add(scrollSide, BorderLayout.CENTER);

        emptyBatchPanel = new JPanel();
        emptyBatchPanel.setBackground(SchedulerTheme.BG_MAIN);
        batchFormPanel = createBatchParamsForm();
        batchFormScroll = new JScrollPane(batchFormPanel);
        batchFormScroll.setBorder(null);
        batchFormScroll.getVerticalScrollBar().setUnitIncrement(16);

        batchSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, emptyBatchPanel);
        batchSplitPane.setDividerLocation(350);
        batchSplitPane.setDividerSize(1);
        batchSplitPane.setBorder(null);
        return batchSplitPane;
    }

    /**
     * Crea el formulario para editar los Batch Parameters.
     * @return JPanel con el formulario de Batch Parameters.
     */
    private JPanel createBatchParamsForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,5,5);
        c.weightx=1.0;
        c.anchor = GridBagConstraints.WEST;

        c.gridy=0;
        c.weightx=0.33;
        c.gridx=0; p.add(createLabel("Param Name"), c);
        c.gridx=1; p.add(createLabel("Query Param"), c);
        c.gridx=2; p.add(createLabel("Fixed Value"), c);

        c.gridy=1;
        c.gridx=0;
        bpNameField = new JTextField();
        applyBlueFocusBorder(bpNameField);
        addTextListener(bpNameField, val -> { if(currentBatchParam!=null) {
            currentBatchParam.setParamName(val);
            int idx = batchParamsList.indexOf(currentBatchParam);
            if(idx!=-1) batchTableModel.setValueAt(val, idx, 1);
        }});
        p.add(bpNameField, c);

        c.gridx=1;
        bpQueryField = new JTextField();
        applyBlueFocusBorder(bpQueryField);
        addTextListener(bpQueryField, val -> { if(currentBatchParam!=null) currentBatchParam.setQueryParam(val); });
        p.add(bpQueryField, c);

        c.gridx=2;
        bpFixedField = new JTextField();
        applyBlueFocusBorder(bpFixedField);
        addTextListener(bpFixedField, val -> { if(currentBatchParam!=null) currentBatchParam.setFixedValue(val); });
        p.add(bpFixedField, c);

        c.gridy=2;
        c.gridx=0;
        c.gridwidth=3;
        p.add(createLabel("Script"), c);

        c.gridy=3;
        c.weighty=0.0;
        c.fill=GridBagConstraints.BOTH;
        bpScriptArea = new AtiScriptPanel(10);
        bpScriptArea.getTextArea().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { u(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { u(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { u(); }
            void u() { if(!isUpdatingUI && currentBatchParam!=null) currentBatchParam.setScript(bpScriptArea.getText()); }
        });
        if(parentTextListener!=null) bpScriptArea.getTextArea().getDocument().addDocumentListener(parentTextListener);

        p.add(bpScriptArea, c);

        c.gridy = 4;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.VERTICAL;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        p.add(filler,c);

        return p;
    }


    /**
     * Crea la pestaña de Queries con su tabla y formulario.
     * @return JSplitPane con la estructura de la pestaña.
     */
    private JSplitPane createQueriesTab() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SchedulerTheme.BG_MAIN);
        sidebar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        btnNewQuery = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btnNewQuery.addActionListener(e -> addNewQuery());
        header.add(queryCountLabel, BorderLayout.WEST); header.add(btnNewQuery, BorderLayout.EAST);

        String[] cols = {"#", "Query Code", "Actions"};
        queryTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        queryTable = createStyleTable(queryTableModel);
        queryTable.getColumnModel().getColumn(ACTION_COL).setCellRenderer(new MoreActionsRenderer());

        queryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && queryTable.getSelectedRow() != -1 && queryTable.getSelectedRow() < queriesList.size()) {
                currentQuery = queriesList.get(queryTable.getSelectedRow());
                loadQueryForm();
                showRightPanelQuery(true);
            }
        });
        addActionsListener(queryTable, 1);

        JScrollPane scrollSide = new JScrollPane(queryTable); scrollSide.setBorder(new EmptyBorder(5,0,0,0));
        sidebar.add(header, BorderLayout.NORTH); sidebar.add(scrollSide, BorderLayout.CENTER);

        emptyQueryPanel = new JPanel(); emptyQueryPanel.setBackground(SchedulerTheme.BG_MAIN);
        queryFormPanel = createQueryFormPanel();
        queryFormScroll = new JScrollPane(queryFormPanel);
        queryFormScroll.setBorder(null);
        queryFormScroll.getVerticalScrollBar().setUnitIncrement(16);

        querySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, emptyQueryPanel);
        querySplitPane.setDividerLocation(350); querySplitPane.setDividerSize(1); querySplitPane.setBorder(null);
        return querySplitPane;
    }

    /**
     * Crea el formulario para editar las Queries.
     * @return JPanel con el formulario de Queries.
     */
    private JPanel createQueryFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5,5,5,5); c.weightx=1.0; c.anchor=GridBagConstraints.WEST;

        c.gridy=0;
        c.gridx=0; c.weightx=0.5; p.add(createLabel("Query Code"), c);
        c.gridx=1; c.weightx=0.5; p.add(createLabel("Type"), c);

        c.gridy=1;
        c.gridx=0;
        qCodeField = new JTextField(); applyBlueFocusBorder(qCodeField);
        addTextListener(qCodeField, val -> {
            if(currentQuery!=null) {
                currentQuery.setQueryCode(val);
                int idx = queriesList.indexOf(currentQuery);
                if(idx!=-1) queryTableModel.setValueAt(val, idx, 1);
            }
        });
        p.add(qCodeField, c);

        c.gridx=1;
        qTypeCombo = new JComboBox<>(new String[]{TYPE_MONGO, TYPE_SQL}); applyBlueFocusBorder(qTypeCombo);
        qTypeCombo.addActionListener(e -> {
            if(isUpdatingUI || currentQuery==null) return;
            String newType = (String) qTypeCombo.getSelectedItem();
            currentQuery.setType(newType);
            loadQueryForm();
            if(parentActionListener!=null) parentActionListener.actionPerformed(e);
        });
        p.add(qTypeCombo, c);
        c.gridy=2; c.gridx=0; c.gridwidth=2; c.weighty=1.0; c.fill=GridBagConstraints.BOTH; c.insets = new Insets(0,0,0,0);

        initSpecificQueryCards();
        p.add(specificQueryContainer, c);

        return p;
    }

    /**
     * Inicializa el contenedor de cartas para los tipos específicos de consulta.
     */
    private void initSpecificQueryCards() {
        queryCardLayout = new CardLayout();
        specificQueryContainer = new JPanel(queryCardLayout);
        specificQueryContainer.setBackground(SchedulerTheme.BG_MAIN);
        specificQueryContainer.add(createMongoPanel(), CARD_MONGO);
        specificQueryContainer.add(createSqlPanel(), CARD_SQL);
    }

    private void showSpecificQueryCard(String type) {
        if(specificQueryContainer == null) return;
        if(TYPE_SQL.equals(type)) queryCardLayout.show(specificQueryContainer, CARD_SQL);
        else queryCardLayout.show(specificQueryContainer, CARD_MONGO);
    }

    /**
     * Crea el subpanel para consultas SQL.
     * @return JPanel con los campos específicos para SQL.
     */
    private JPanel createMongoPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5,5,5,5); c.weightx=1.0; c.anchor=GridBagConstraints.WEST;
        c.gridy=0;
        c.gridx=0; c.weightx=0.5; p.add(createLabel("Collection"), c);
        c.gridx=1; c.weightx=0.5; p.add(createLabel("Db Source"), c);

        c.gridy=1;
        c.gridx=0;
        qCollectionField = new JTextField(); applyBlueFocusBorder(qCollectionField);
        addTextListener(qCollectionField, val -> { if(currentQuery!=null) currentQuery.setCollection(val); });
        p.add(qCollectionField, c);

        c.gridx=1;
        mongoDbSourceCombo = new JComboBox<>(new String[]{"Config", "Data", "Oracle"}); applyBlueFocusBorder(mongoDbSourceCombo);
        mongoDbSourceCombo.addActionListener(e -> {
            if(!isUpdatingUI && currentQuery!=null) currentQuery.setDbSource((String)mongoDbSourceCombo.getSelectedItem());
        });
        p.add(mongoDbSourceCombo, c);

        c.gridy=2; c.gridx=0; c.gridwidth=2; c.insets = new Insets(15, 5, 5, 5);
        p.add(createLabel("Aggregate Query"), c);

        c.gridy=3; c.weighty=1.0; c.fill=GridBagConstraints.BOTH;
        p.add(createFiltersSubTable(), c);

        return p;
    }

    /**
     * Crea el subpanel para consultas SQL.
     * @return JPanel con los campos para consultas SQL.
     */
    private JPanel createSqlPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5,5,5,5); c.weightx=1.0; c.anchor=GridBagConstraints.WEST;
        c.gridy=0; c.gridx=0; p.add(createLabel("Db Source"), c);

        c.gridy=1;
        sqlDbSourceCombo = new JComboBox<>(new String[]{"Config", "Data", "Oracle"}); applyBlueFocusBorder(sqlDbSourceCombo);
        sqlDbSourceCombo.addActionListener(e -> {
            if(!isUpdatingUI && currentQuery!=null) currentQuery.setDbSource((String)sqlDbSourceCombo.getSelectedItem());
        });
        p.add(sqlDbSourceCombo, c);

        c.gridy=2; c.insets = new Insets(15, 5, 5, 5);
        p.add(createLabel("SQL Query"), c);

        c.gridy=3; c.weighty=1.0; c.fill=GridBagConstraints.BOTH; c.insets = new Insets(0, 0, 0, 0);
        qSqlArea = new AtiScriptPanel(8);
        qSqlArea.getTextArea().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { u(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { u(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { u(); }
            void u() { if(!isUpdatingUI && currentQuery!=null) currentQuery.setSqlQuery(qSqlArea.getText()); }
        });

        p.add(qSqlArea, c);
        return p;
    }

    /**
     * Crea el subpanel con la tabla de filtros para consultas Mongo.
     * @return JPanel con la tabla de filtros.
     */
    private JPanel createFiltersSubTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(SchedulerTheme.BORDER_SOFT));

        JPanel header = new JPanel(new BorderLayout()); header.setBackground(Color.WHITE); header.setBorder(new EmptyBorder(5, 10, 5, 10));
        lblFiltersCount = new JLabel("Filters(0)"); lblFiltersCount.setForeground(Color.GRAY);
        btnNewFilter = new AtiCircularIconButton(AtiIcons.ADD_ICON); btnNewFilter.setPreferredSize(new Dimension(60, 25));
        btnNewFilter.addActionListener(e -> addNewFilter());
        header.add(lblFiltersCount, BorderLayout.WEST); header.add(btnNewFilter, BorderLayout.EAST);

        String[] cols = {"#", "Filter", "Actions"};
        qFiltersModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return c==1; } };
        qFiltersTable = createStyleTable(qFiltersModel);
        qFiltersTable.getColumnModel().getColumn(ACTION_COL).setCellRenderer(new MoreActionsRenderer());

        qFiltersTable.getDefaultEditor(Object.class).addCellEditorListener(new CellEditorListener() {
            public void editingStopped(ChangeEvent e) {
                int row = qFiltersTable.getSelectedRow();
                if(row!=-1 && currentQuery!=null && row<currentQuery.getFilters().size()) {
                    currentQuery.getFilters().get(row).setFilterValue((String) qFiltersTable.getValueAt(row, 1));
                    if(parentActionListener!=null) parentActionListener.actionPerformed(new ActionEvent(qFiltersTable, ActionEvent.ACTION_PERFORMED, "TableEdited"));
                }
            }
            public void editingCanceled(ChangeEvent e) {}
        });
        addActionsListener(qFiltersTable, 2);

        JScrollPane sp = new JScrollPane(qFiltersTable); sp.setBorder(null); sp.setPreferredSize(new Dimension(0, 150));
        panel.add(header, BorderLayout.NORTH); panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Muestra u oculta el panel derecho del SplitPane de Batch Params.
     * @param show true para mostrar el formulario, false para mostrar el panel vacío.
     */
    private void showRightPanelBatch(boolean show) {
        Component right = show ? batchFormScroll : emptyBatchPanel;
        if(batchSplitPane.getRightComponent()!=right) {
            batchSplitPane.setRightComponent(right);
            batchSplitPane.revalidate();
            batchSplitPane.repaint();
        }
    }

    /**
     * Muestra u oculta el panel derecho del SplitPane de Queries.
     * @param show true para mostrar el formulario, false para mostrar el panel vacío.
     */
    private void showRightPanelQuery(boolean show) {
        Component right = show ? queryFormScroll : emptyQueryPanel;
        if(querySplitPane.getRightComponent()!=right) {
            querySplitPane.setRightComponent(right);
            querySplitPane.revalidate();
            querySplitPane.repaint();
        }
    }

    /**
     * Añade un nuevo BatchParam a la lista.
     */
    private void addNewBatchParam() {
        int n = batchParamsList.size() + 1;
        BatchParamData d = new BatchParamData(String.format("%02d", n));
        batchParamsList.add(d);
        batchTableModel.addRow(new Object[]{d.getId(), d.getParamName(), ""});
        batchCountLabel.setText("Nº of elements (" + n + ")");
        batchTable.setRowSelectionInterval(n - 1, n - 1);
        if (parentActionListener != null) parentActionListener.actionPerformed(new ActionEvent(btnNewBatchParam, ActionEvent.ACTION_PERFORMED, "NEW_BATCH_PARAM"));
    }

    /**
     * Añade una nueva query a la lista.
     */
    private void addNewQuery() {
        int i = queriesList.size() + 1;
        QueryData queryData = new QueryData(String.format("%02d", i));
        queriesList.add(queryData);
        queryTableModel.addRow(new Object[]{queryData.getId(), queryData.getQueryCode(), ""});
        queryCountLabel.setText("Nº of elements (" + i + ")");
        queryTable.setRowSelectionInterval(i - 1, i - 1);
        if (parentActionListener != null) parentActionListener.actionPerformed(new ActionEvent(btnNewQuery, ActionEvent.ACTION_PERFORMED, "NEW_QUERY"));
    }

    /**
     * Elimina un BatchParam de la lista.
     * @param idx índice del BatchParam a eliminar.
     */
    private void deleteBatchParam(int idx) {
        if(idx<0) return;
        batchParamsList.remove(idx);
        batchTableModel.removeRow(idx);
        for(int i=0; i<batchParamsList.size(); i++) {
            String nid = String.format("%02d", i+1);
            batchParamsList.get(i).setId(nid);
            batchTableModel.setValueAt(nid, i, 0);
        }
        batchCountLabel.setText("Nº of elements (" + batchParamsList.size() + ")");
        if(batchParamsList.isEmpty()) { showRightPanelBatch(false); currentBatchParam=null; }
        else { int s = Math.max(0, idx-1); batchTable.setRowSelectionInterval(s,s); }
        if(parentActionListener!=null) parentActionListener.actionPerformed(new ActionEvent(batchTable, ActionEvent.ACTION_PERFORMED, "Deleted"));
    }

    /**
     * Elimina una query de la lista.
     * @param idx índice de la query a eliminar.
     */
    private void deleteQuery(int idx) {
        if(idx<0) return;
        queriesList.remove(idx);
        queryTableModel.removeRow(idx);
        for(int i=0; i<queriesList.size(); i++) {
            String nid = String.format("%02d", i+1);
            queriesList.get(i).setId(nid);
            queryTableModel.setValueAt(nid, i, 0);
        }
        queryCountLabel.setText("Nº of elements (" + queriesList.size() + ")");
        if(queriesList.isEmpty()) { showRightPanelQuery(false); currentQuery=null; }
        else { int s = Math.max(0, idx-1); queryTable.setRowSelectionInterval(s,s); }
        if(parentActionListener!=null) parentActionListener.actionPerformed(new ActionEvent(queryTable, ActionEvent.ACTION_PERFORMED, "Deleted"));
    }

    /**
     * Añade un nuevo filtro a la query actual.
     */
    private void addNewFilter() {
        if(currentQuery==null) return;
        int n = currentQuery.getFilters().size()+1;
        FilterData f = new FilterData(String.format("%02d", n), "new Filter " + n);
        currentQuery.getFilters().add(f);
        qFiltersModel.addRow(new Object[]{f.getId(), f.getFilterValue(), ""});
        lblFiltersCount.setText("Filters(" + n + ")");
        if(parentActionListener!=null) parentActionListener.actionPerformed(new ActionEvent(btnNewFilter, ActionEvent.ACTION_PERFORMED, "FilterAdded"));
    }

    /**
     * Elimina un filtro de la query actual.
     * @param row índice del filtro a eliminar.
     */
    private void deleteFilter(int row) {
        if(currentQuery==null) return;
        currentQuery.getFilters().remove(row);
        qFiltersModel.removeRow(row);
        for(int i=0; i<currentQuery.getFilters().size(); i++) {
            String nid = String.format("%02d", i+1);
            currentQuery.getFilters().get(i).setId(nid);
            qFiltersModel.setValueAt(nid, i, 0);
        }
        lblFiltersCount.setText("Filters(" + currentQuery.getFilters().size() + ")");
        if(parentActionListener!=null) parentActionListener.actionPerformed(new ActionEvent(qFiltersTable, ActionEvent.ACTION_PERFORMED, "FilterDeleted"));
    }

    /**
     * Carga los datos del BatchParamData actual en el formulario.
     */
    private void loadBatchParamForm() {
        if(currentBatchParam==null) return;
        isUpdatingUI=true;
        bpNameField.setText(currentBatchParam.getParamName());
        bpQueryField.setText(currentBatchParam.getQueryParam());
        bpFixedField.setText(currentBatchParam.getFixedValue());
        bpScriptArea.setText(currentBatchParam.getScript());
        isUpdatingUI=false;
    }

    /**
     * Carga los datos del QueryData actual en el formulario.
     */
    private void loadQueryForm() {
        if(currentQuery==null) return;
        isUpdatingUI=true;

        qCodeField.setText(currentQuery.getQueryCode());
        qTypeCombo.setSelectedItem(currentQuery.getType());
        showSpecificQueryCard(currentQuery.getType());

        if(qCollectionField != null) qCollectionField.setText(currentQuery.getCollection());
        if(mongoDbSourceCombo != null) mongoDbSourceCombo.setSelectedItem(currentQuery.getDbSource());
        if(sqlDbSourceCombo != null) sqlDbSourceCombo.setSelectedItem(currentQuery.getDbSource());
        if(qSqlArea != null) qSqlArea.setText(currentQuery.getSqlQuery());

        if(qFiltersModel != null) {
            qFiltersModel.setRowCount(0);
            for(FilterData fd : currentQuery.getFilters()) {
                qFiltersModel.addRow(new Object[]{fd.getId(), fd.getFilterValue(), ""});
            }
            lblFiltersCount.setText("Filters(" + currentQuery.getFilters().size() + ")");
        }

        isUpdatingUI=false;
    }

    /**
     * Crea una tabla JTable con estilos personalizados.
     * @param m Modelo de tabla a usar.
     * @return JTable con los estilos aplicados.
     */
    private JTable createStyleTable(DefaultTableModel m) {
        JTable t = new JTable(m); t.setRowHeight(35); t.setShowGrid(false); t.setSelectionBackground(new Color(232, 244, 253)); return t;
    }

    /**
     * Crea una etiqueta JLabel con estilos personalizados.
     * @param t Texto de la etiqueta.
     * @return JLabel con los estilos aplicados.
     */
    private JLabel createLabel(String t) { JLabel l = new JLabel(t); l.setFont(new Font("Lato", Font.BOLD, 14)); l.setForeground(SchedulerTheme.TEXT_MAIN); return l; }

    /**
     * Añade un listener para gestionar el clic en la columna de acciones (3 puntos).
     * Muestra un menú popup con la opción de borrar.
     * * @param table Tabla a la que se añade el listener.
     * @param type Tipo de tabla: 0=BatchParams, 1=Queries, 2=Filters.
     */
    private void addActionsListener(JTable table, int type) {
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r != -1 && c == ACTION_COL) {
                    showActionsPopup(table, r, type, e.getPoint());
                }
            }
        });
    }

    /**
     * Muestra el menú popup con la opción de eliminar.
     */
    private void showActionsPopup(JTable table, int rowIndex, int type, Point p) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete", AtiIcons.TRASH_ICON);

        deleteItem.addActionListener(e -> {
            if (type == 0) deleteBatchParam(rowIndex);
            else if (type == 1) deleteQuery(rowIndex);
            else if (type == 2) deleteFilter(rowIndex);
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
}
