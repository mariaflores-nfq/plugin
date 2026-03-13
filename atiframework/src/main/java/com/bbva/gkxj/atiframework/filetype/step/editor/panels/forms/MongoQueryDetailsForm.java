package com.bbva.gkxj.atiframework.filetype.step.editor.panels.forms;

import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.components.AtiResizableTextArea;
import com.bbva.gkxj.atiframework.components.AtiScriptPanel;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import icons.AtiIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Clase encapsula todos los componentes visuales y la lógica
 * de interacción para configurar una query MongoDB individual.
 *
 * Extiende {@link AbstractQueryDetailsForm} para heredar funcionalidad
 * común como creación de labels, gestión de acciones en tablas, etc.
 */
public class MongoQueryDetailsForm extends AbstractQueryDetailsForm {

    private static final String CARD_OP_UPDATE = "opUpdate";
    private static final String CARD_OP_INSERT = "opInsert";
    private static final String CARD_OP_DELETE = "opDelete";
    private static final String CARD_SUB_FILTER = "subFilter";
    private static final String CARD_SUB_UPDATE = "subUpdate";

    private AtiTextField queryCodeField;
    private AtiComboBox dbSourceCombo;
    private AtiTextField collectionField;
    private AtiScriptPanel scriptArea;
    private AtiTextField optionsField;
    private AtiComboBox operationCombo;

    private JPanel operationDynamicPanel;
    private CardLayout operationCardLayout;
    private JPanel updateOperationPanel;
    private JPanel insertOperationPanel;
    private JPanel deleteOperationPanel;

    private AtiResizableTextArea updateMongoQueryArea;
    private AtiResizableTextArea filterQueryArea;
    private AtiResizableTextArea insertQueryArea;
    private AtiResizableTextArea deleteQueryArea;

    private JPanel filterTabBtn;
    private JPanel updateTabBtn;
    private JPanel subOperationContentPanel;
    private CardLayout subOperationCardLayout;
    private boolean filterTabSelected = true;

    private Map<String, OperationCache> operationDataCache = new HashMap<>();
    private String currentOperation = "Update";

    private JTable paramsTable;
    private DefaultTableModel paramsModel;
    private JLabel paramsCountLabel;
    private JPanel paramsCardContainer;
    private CardLayout paramsCardLayout;

    private AtiTextField paramNameField;
    private AtiComboBox paramTypeCombo;
    private AtiTextField queryParamField;

    /**
     * Constructor del formulario de detalles MongoDB.
     *
     * @param queryCodeUpdater Callback para actualizar el queryCode en la tabla padre
     */
    public MongoQueryDetailsForm(Consumer<String> queryCodeUpdater) {
        super(queryCodeUpdater);
        initForm();
    }


    /**
     * Inicializa los campos específicos del formulario MongoDB:
     * Query Code, DB Source, Collection, Script, Options, Operation y Parameters.
     */
    @Override
    protected void initForm() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0; c.insets = new Insets(0, 0, 5, 10);
        c.gridx = 0; c.weightx = 0.5;c.gridwidth = 1;
        queryCodeField = new AtiTextField();
        queryCodeField.setPreferredSize(new Dimension(0, 30));
        JPanel qcPanel =  createLabeledField ("Query Code", queryCodeField);
        add(qcPanel, c);

        c.gridx = 1; c.insets = new Insets(0, 10, 15, 0);
        dbSourceCombo = new AtiComboBox(new String[]{"Config", "Oracle_Dev", "Mongo_Prod"});
        dbSourceCombo.setPreferredSize(new Dimension(0, 30));
        JPanel dbPanel = createLabeledField("Db Source", dbSourceCombo);
        add(dbPanel, c);


        c.gridy = 2; c.gridx = 0; c.gridwidth = 2;
        c.weightx = 1.0; c.insets = new Insets(0, 0, 5, 0);
        collectionField = new AtiTextField();
        collectionField.setPreferredSize(new Dimension(0, 30));
        JPanel collectionPanel =  createLabeledField ("Collection", collectionField);
        add(collectionPanel, c);

        c.gridy = 4; c.insets = new Insets(0, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        scriptArea = new AtiScriptPanel();
//        scriptArea.setMinimumSize(new Dimension(100, 100));
//        scriptArea.setPreferredSize(new Dimension(100, 150));
        JPanel scriptWrapper = createLabeledField("Script", scriptArea);
        scriptWrapper.setBackground(BG_SIDEBAR);
        add(scriptWrapper, c);

        c.gridy = 5; c.gridwidth = 1;
        c.insets = new Insets(15, 0, 5, 10);
        c.gridx = 0; c.weightx = 0.5;
        optionsField = new AtiTextField();
        optionsField.setPreferredSize(new Dimension(0, 30));
        JPanel optionWrapper = createLabeledField("Options", optionsField);
        add(optionWrapper, c);
        c.gridx = 1; c.insets = new Insets(15, 10, 5, 0);
        operationCombo = new AtiComboBox(new String[]{"Update", "Insert", "Delete"});
        operationCombo.setPreferredSize(new Dimension(0, 30));
        operationCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                handleOperationChange();
            }
        });
        JPanel operationWrapper = createLabeledField("Operation", operationCombo);
        add(operationWrapper, c);


        c.gridy = 7; c.gridx = 0; c.gridwidth = 2;
        c.weightx = 1.0; c.insets = new Insets(0, 0, 15, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        operationCardLayout = new CardLayout();
        operationDynamicPanel = new JPanel(operationCardLayout);
        operationDynamicPanel.setBackground(BG_SIDEBAR);

        updateOperationPanel = createUpdateOperationPanel();
        insertOperationPanel = createSingleOperationPanel("Insert");
        deleteOperationPanel = createSingleOperationPanel("Delete");

        operationDynamicPanel.add(updateOperationPanel, CARD_OP_UPDATE);
        operationDynamicPanel.add(insertOperationPanel, CARD_OP_INSERT);
        operationDynamicPanel.add(deleteOperationPanel, CARD_OP_DELETE);

        operationCardLayout.show(operationDynamicPanel, CARD_OP_UPDATE);
        add(operationDynamicPanel, c);

        c.gridy = 8; c.fill = GridBagConstraints.BOTH; c.weighty = 1.0;

        paramsCardLayout = new CardLayout();
        paramsCardContainer = new JPanel(paramsCardLayout);
        paramsCardContainer.setBackground(BG_SIDEBAR);

        paramsCardContainer.add(createParamsEmptyPanel(), CARD_PARAMS_EMPTY);

        JPanel paramsFullPanel = new JPanel(new BorderLayout(0, 5));
        paramsFullPanel.setBackground(BG_SIDEBAR);
        paramsFullPanel.add(createParamsTablePanel(), BorderLayout.CENTER);
        paramsFullPanel.add(createParamDetailsPanel(), BorderLayout.SOUTH);
        paramsCardContainer.add(paramsFullPanel, CARD_PARAMS_FULL);

        paramsCardLayout.show(paramsCardContainer, CARD_PARAMS_EMPTY);
        add(paramsCardContainer, c);
    }


    /**
     * Crea el panel para la operación "Update" con sus tabs de Filter y Update.
     *
     * @return JPanel con los campos y configuraciones para la operación Update
     */
    private JPanel createUpdateOperationPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_SIDEBAR);

        JPanel tabsPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        tabsPanel.setBackground(BG_SIDEBAR);

        filterTabBtn = createTabButton("Filter", true);
        updateTabBtn = createTabButton("Update", false);

        filterTabBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!filterTabSelected) {
                    selectTab(true);
                    subOperationCardLayout.show(subOperationContentPanel, CARD_SUB_FILTER);
                    reloadParamsForCurrentSubTab();
                    fireActionIfNeeded();
                }
            }
        });

        updateTabBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (filterTabSelected) {
                    selectTab(false);
                    subOperationCardLayout.show(subOperationContentPanel, CARD_SUB_UPDATE);
                    reloadParamsForCurrentSubTab();
                    fireActionIfNeeded();
                }
            }
        });

        tabsPanel.add(filterTabBtn);
        tabsPanel.add(updateTabBtn);
        panel.add(tabsPanel, BorderLayout.NORTH);

        subOperationCardLayout = new CardLayout();
        subOperationContentPanel = new JPanel(subOperationCardLayout);
        subOperationContentPanel.setBackground(BG_SIDEBAR);
        subOperationContentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        filterQueryArea = new AtiResizableTextArea("Filter Mongo Query", new JTextArea(), true);
        filterQueryArea.setMinimumSize(new Dimension(100, 80));
        filterQueryArea.setPreferredSize(new Dimension(100, 100));
        subOperationContentPanel.add(filterQueryArea, CARD_SUB_FILTER);

        updateMongoQueryArea = new AtiResizableTextArea("Update Mongo Query", new JTextArea(), true);
        updateMongoQueryArea.setMinimumSize(new Dimension(100, 80));
        updateMongoQueryArea.setPreferredSize(new Dimension(100, 100));
        subOperationContentPanel.add(updateMongoQueryArea, CARD_SUB_UPDATE);

        subOperationCardLayout.show(subOperationContentPanel, CARD_SUB_FILTER);
        panel.add(subOperationContentPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea un panel para las operaciones "Insert" o "Delete".
     *
     * @param operationType Tipo de operación ("Insert" o "Delete")
     * @return JPanel con el campo de query para la operación especificada
     */
    private JPanel createSingleOperationPanel(String operationType) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_SIDEBAR);

        JPanel tabsPanel = new JPanel(new BorderLayout());
        tabsPanel.setBackground(BG_SIDEBAR);
        tabsPanel.add(createFullWidthTabButton(operationType, true), BorderLayout.CENTER);
        panel.add(tabsPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_SIDEBAR);
        contentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        AtiResizableTextArea queryArea = new AtiResizableTextArea(operationType + " Mongo Query", new JTextArea(), true);
        queryArea.setMinimumSize(new Dimension(100, 80));
        queryArea.setPreferredSize(new Dimension(100, 100));

        if ("Insert".equals(operationType)) {
            insertQueryArea = queryArea;
        } else {
            deleteQueryArea = queryArea;
        }

        contentPanel.add(queryArea, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea un botón de tab personalizado.
     *
     * @param text Texto del tab
     * @param selected Si está seleccionado
     * @return JPanel con el estilo de tab
     */
    private JPanel createTabButton(String text, boolean selected) {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(selected ? Color.WHITE : BG_SIDEBAR);
        tab.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, selected ? 2 : 0, 0, BBVA_BLUE),
            new EmptyBorder(8, 20, 8, 20)
        ));
        tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(selected ? BBVA_BLUE : TEXT_GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        tab.add(label, BorderLayout.CENTER);

        return tab;
    }

    /**
     * Crea un botón de tab que ocupa todo el ancho.
     *
     * @param text Texto del tab
     * @param selected Si está seleccionado
     * @return JPanel con el estilo de tab de ancho completo
     */
    private JPanel createFullWidthTabButton(String text, boolean selected) {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(selected ? Color.WHITE : BG_SIDEBAR);
        tab.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, selected ? 2 : 0, 0, BBVA_BLUE),
            new EmptyBorder(8, 0, 8, 0)
        ));
        tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(selected ? BBVA_BLUE : TEXT_GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        tab.add(label, BorderLayout.CENTER);

        return tab;
    }

    /**
     * Selecciona un tab y actualiza los estilos.
     *
     * @param filterSelected true si se selecciona Filter
     */
    private void selectTab(boolean filterSelected) {
        this.filterTabSelected = filterSelected;
        updateTabStyle(filterTabBtn, filterSelected);
        updateTabStyle(updateTabBtn, !filterSelected);
    }

    /**
     * Actualiza el estilo visual de un tab.
     *
     * @param tab Panel del tab
     * @param selected Si está seleccionado
     */
    private void updateTabStyle(JPanel tab, boolean selected) {
        tab.setBackground(selected ? Color.WHITE : BG_SIDEBAR);
        tab.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, selected ? 2 : 0, 0, BBVA_BLUE),
            new EmptyBorder(8, 20, 8, 20)
        ));

        for (Component c : tab.getComponents()) {
            if (c instanceof JLabel) {
                ((JLabel) c).setForeground(selected ? BBVA_BLUE : TEXT_GRAY);
            }
        }
    }

    /**
     * Dispara un evento de acción al listener padre.
     */
    private void fireActionIfNeeded() {
        if (parentActionListener != null && !isInternal) {
            parentActionListener.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "SubOpChanged")
            );
        }
    }

    /**
     * Recarga la tabla de parámetros según el sub-tab seleccionado.
     */
    private void reloadParamsForCurrentSubTab() {
        if (currentSelection == null) return;

        String paramKey = getParamKeyForOperation("Update");
        JsonArray params = currentSelection.has(paramKey)
            ? currentSelection.getAsJsonArray(paramKey)
            : new JsonArray();

        if (!currentSelection.has(paramKey)) {
            currentSelection.add(paramKey, params);
        }

        reloadParamsTable(params);

        if (!params.isEmpty()) {
            paramsTable.setRowSelectionInterval(0, 0);
        } else {
            selectedParamRow = -1;
            loadSelectedParam();
        }
    }

    /**
     * Maneja el cambio de operación.
     */
    private void handleOperationChange() {
        String selected = (String) operationCombo.getSelectedItem();

        if (selected != null && selected.equals(currentOperation)) {
            showOperationCard(selected);
            return;
        }

        if (!isInternal && currentSelection != null && currentOperation != null) {
            saveCurrentOperationToCache(currentOperation);
        }

        if (!isInternal && currentSelection != null && selected != null) {
            loadOperationFromCache(selected);
        }

        currentOperation = selected;
        showOperationCard(selected);

        if (!isInternal && currentSelection != null) {
            currentSelection.addProperty("operation", selected);
        }

        if (parentActionListener != null && !isInternal) {
            parentActionListener.actionPerformed(
                new ActionEvent(operationCombo, ActionEvent.ACTION_PERFORMED, "OperationChanged")
            );
        }
    }

    /**
     * Muestra el panel correspondiente a la operación seleccionada.
     *
     * @param operation Operación seleccionada
     */
    private void showOperationCard(String operation) {
        switch (operation) {
            case "Insert":
                operationCardLayout.show(operationDynamicPanel, CARD_OP_INSERT);
                break;
            case "Delete":
                operationCardLayout.show(operationDynamicPanel, CARD_OP_DELETE);
                break;
            default:
                operationCardLayout.show(operationDynamicPanel, CARD_OP_UPDATE);
                break;
        }
    }

    /**
     * Guarda el estado actual en cache.
     *
     * @param operation Operación de la cual guardar
     */
    private void saveCurrentOperationToCache(String operation) {
        OperationCache cache = operationDataCache.computeIfAbsent(operation, k -> new OperationCache());

        switch (operation) {
            case "Update":
                cache.filterQuery = filterQueryArea.getTextArea().getText();
                cache.updateMongoQuery = updateMongoQueryArea.getTextArea().getText();
                break;
            case "Insert":
                cache.insertQuery = insertQueryArea.getTextArea().getText();
                break;
            case "Delete":
                cache.deleteQuery = deleteQueryArea.getTextArea().getText();
                break;
        }

        String paramKey = getParamKeyForOperation(operation);
        if (currentSelection != null && currentSelection.has(paramKey)) {
            cache.parameters = currentSelection.getAsJsonArray(paramKey).deepCopy();
        } else {
            cache.parameters = new JsonArray();
        }
    }

    /**
     * Carga el estado desde cache.
     *
     * @param operation Operación de la cual cargar
     */
    private void loadOperationFromCache(String operation) {
        OperationCache cache = operationDataCache.get(operation);
        String paramKey = getParamKeyForOperation(operation);

        if (cache == null) {
            clearOperationFields(operation);
            if (currentSelection != null) {
                currentSelection.add(paramKey, new JsonArray());
                reloadParamsTable(new JsonArray());
            }
            return;
        }

        switch (operation) {
            case "Update":
                filterQueryArea.getTextArea().setText(cache.filterQuery);
                updateMongoQueryArea.getTextArea().setText(cache.updateMongoQuery);
                break;
            case "Insert":
                insertQueryArea.getTextArea().setText(cache.insertQuery);
                break;
            case "Delete":
                deleteQueryArea.getTextArea().setText(cache.deleteQuery);
                break;
        }

        if (currentSelection != null) {
            JsonArray paramsCopy = cache.parameters.deepCopy();
            currentSelection.add(paramKey, paramsCopy);
            reloadParamsTable(paramsCopy);

            if (!paramsCopy.isEmpty()) {
                paramsTable.setRowSelectionInterval(0, 0);
            } else {
                selectedParamRow = -1;
                loadSelectedParam();
            }
        }
    }

    /**
     * Limpia los campos de la operación.
     *
     * @param operation Operación para la cual limpiar
     */
    private void clearOperationFields(String operation) {
        switch (operation) {
            case "Update":
                filterQueryArea.getTextArea().setText("");
                updateMongoQueryArea.getTextArea().setText("");
                break;
            case "Insert":
                insertQueryArea.getTextArea().setText("");
                break;
            case "Delete":
                deleteQueryArea.getTextArea().setText("");
                break;
        }
    }

    /**
     * Crea el panel que se muestra cuando no hay parámetros.
     *
     * @return JPanel con el diseño para el estado vacío
     */
    private JPanel createParamsEmptyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SIDEBAR);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel title = new JLabel("Parameters (0)");
        title.setFont(FONT_SUBTITLE);
        title.setForeground(TEXT_GRAY);

        AtiCircularIconButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> addNewParam());

        header.add(title, BorderLayout.WEST);
        header.add(btn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        return panel;
    }

    /**
     * Crea el panel que contiene la tabla de parámetros.
     *
     * @return JPanel con la tabla
     */
    private JPanel createParamsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setBackground(Color.WHITE);
        headerContent.setBorder(new EmptyBorder(5, 10, 5, 10));

        paramsCountLabel = new JLabel("Parameters (0)");
        paramsCountLabel.setForeground(TEXT_GRAY);

        AtiCircularIconButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> addNewParam());

        headerContent.add(paramsCountLabel, BorderLayout.WEST);
        headerContent.add(btn, BorderLayout.EAST);
        header.add(headerContent, BorderLayout.CENTER);

        paramsModel = new DefaultTableModel(new String[]{"#", "Param Name", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        paramsTable = new JBTable(paramsModel);
        paramsTable.setRowHeight(25);
        paramsTable.setShowGrid(false);
        paramsTable.setSelectionBackground(BG_TABLE_SELECTION);
        paramsTable.setSelectionForeground(Color.BLACK);
        paramsTable.getTableHeader().setBackground(BG_TABLE_HEADER);
        paramsTable.getTableHeader().setFont(FONT_HEADER_TABLE);

        paramsTable.getColumnModel().getColumn(0).setMaxWidth(40);
        paramsTable.getColumnModel().getColumn(0).setCellRenderer(new CenterGrayRenderer());
        paramsTable.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setCellRenderer(new MoreActionsRenderer());

        paramsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedParamRow = paramsTable.getSelectedRow();
                loadSelectedParam();
            }
        });

        addActionsListener(paramsTable);

        JScrollPane sp = new JBScrollPane(paramsTable);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(0, 120));

        panel.add(header, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de detalles para el parámetro seleccionado.
     *
     * @return JPanel con los campos de detalle
     */
    private JPanel createParamDetailsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_SIDEBAR);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0; c.insets = new Insets(0, 0, 5, 10);
        c.gridx = 0; c.weightx = 0.5;
        p.add(createLabel("Parameter Name"), c);
        c.gridx = 1; c.insets = new Insets(0, 10, 5, 0);
        p.add(createLabel("Param Type"), c);

        c.gridy = 1; c.insets = new Insets(0, 0, 10, 10);
        c.gridx = 0;
        paramNameField = new AtiTextField();
        paramNameField.setPreferredSize(new Dimension(0, 30));
        p.add(paramNameField, c);

        c.gridx = 1; c.insets = new Insets(0, 10, 10, 0);
        paramTypeCombo = new AtiComboBox(new String[]{"String", "Integer", "Long", "Double", "Date"});
        paramTypeCombo.setPreferredSize(new Dimension(0, 30));
        p.add(paramTypeCombo, c);

        c.gridy = 2; c.gridx = 0; c.gridwidth = 2;
        c.weightx = 1.0; c.insets = new Insets(0, 0, 5, 0);
        p.add(createLabel("Query Param"), c);

        c.gridy = 3; c.insets = new Insets(0, 0, 0, 0);
        queryParamField = new AtiTextField();
        queryParamField.setPreferredSize(new Dimension(0, 30));
        p.add(queryParamField, c);

        return p;
    }

    /**
     * Agrega un nuevo parámetro.
     */
    private void addNewParam() {
        if (currentSelection == null) return;

        String operation = (String) operationCombo.getSelectedItem();
        String paramKey = getParamKeyForOperation(operation);

        JsonArray params = currentSelection.has(paramKey)
            ? currentSelection.getAsJsonArray(paramKey)
            : new JsonArray();

        if (!currentSelection.has(paramKey)) {
            currentSelection.add(paramKey, params);
        }

        JsonObject newParam = new JsonObject();
        newParam.addProperty("fieldName", "newParam");
        newParam.addProperty("paramType", "STRING");
        newParam.addProperty("queryParam", "");
        params.add(newParam);

        reloadParamsTable(params);
        paramsTable.setRowSelectionInterval(paramsModel.getRowCount() - 1, paramsModel.getRowCount() - 1);

        notifyParentAction(null);
    }

    /**
     * Obtiene la clave del array de parámetros según la operación.
     *
     * @param operation Operación
     * @return Clave del array de parámetros
     */
    private String getParamKeyForOperation(String operation) {
        if (operation == null) return "updateParameters";
        switch (operation) {
            case "Insert": return "insertParameters";
            case "Delete": return "deleteParameters";
            case "Update":
                return filterTabSelected ? "filterParameters" : "updateParameters";
            default: return "updateParameters";
        }
    }

    /**
     * Elimina un parámetro del formulario.
     *
     * @param row Índice del parámetro a eliminar
     */
    @Override
    public void deleteParam(int row) {
        if (currentSelection == null) return;

        String operation = (String) operationCombo.getSelectedItem();
        String paramKey = getParamKeyForOperation(operation);

        if (!currentSelection.has(paramKey)) return;

        JsonArray params = currentSelection.getAsJsonArray(paramKey);
        if (row >= 0 && row < params.size()) {
            params.remove(row);
            reloadParamsTable(params);

            if (!params.isEmpty()) {
                int s = Math.max(0, row - 1);
                paramsTable.setRowSelectionInterval(s, s);
            } else {
                selectedParamRow = -1;
                loadSelectedParam();
            }
        }
    }

    /**
     * Recarga la tabla de parámetros.
     *
     * @param params JsonArray con los parámetros
     */
    private void reloadParamsTable(JsonArray params) {
        if (paramsModel == null) return;

        paramsModel.setRowCount(0);
        paramsCountLabel.setText("Parameters (" + params.size() + ")");

        for (int i = 0; i < params.size(); i++) {
            JsonObject param = params.get(i).getAsJsonObject();
            String name = param.has("fieldName") ? param.get("fieldName").getAsString() : "";
            paramsModel.addRow(new Object[]{String.format("%02d", i + 1), name, ""});
        }

        paramsCardLayout.show(paramsCardContainer,
            params.isEmpty() ? CARD_PARAMS_EMPTY : CARD_PARAMS_FULL);
    }

    /**
     * Carga los datos del parámetro seleccionado.
     */
    private void loadSelectedParam() {
        isInternal = true;
        try {
            String operation = (String) operationCombo.getSelectedItem();
            String paramKey = getParamKeyForOperation(operation);

            if (selectedParamRow != -1 && currentSelection != null && currentSelection.has(paramKey)) {
                JsonArray params = currentSelection.getAsJsonArray(paramKey);
                if (selectedParamRow < params.size()) {
                    JsonObject p = params.get(selectedParamRow).getAsJsonObject();
                    paramNameField.setText(getStringOrEmpty(p, "fieldName"));
                    String paramTypeFromJson = getStringOrDefault(p, "paramType", "STRING");
                    String formattedParamType = formatParamTypeForCombo(paramTypeFromJson);
                    paramTypeCombo.setSelectedItem(formattedParamType);
                    queryParamField.setText(getStringOrEmpty(p, "queryParam"));
                    setParamFieldsEnabled(true);
                    return;
                }
            }
            clearParamFields();
            setParamFieldsEnabled(false);
        } finally {
            isInternal = false;
        }
    }

    /**
     * Convierte el formato del paramType.
     *
     * @param paramType Tipo del JSON
     * @return Tipo formateado para el combo
     */
    private String formatParamTypeForCombo(String paramType) {
        if (paramType == null || paramType.isEmpty()) return "String";
        return paramType.substring(0, 1).toUpperCase() + paramType.substring(1).toLowerCase();
    }

    /**
     * Limpia los campos de parámetro.
     */
    private void clearParamFields() {
        paramNameField.setText("");
        paramTypeCombo.setSelectedIndex(-1);
        queryParamField.setText("");
    }

    /**
     * Habilita o deshabilita los campos de parámetro.
     *
     * @param enabled Si habilitar
     */
    private void setParamFieldsEnabled(boolean enabled) {
        paramNameField.setEnabled(enabled);
        paramTypeCombo.setEnabled(enabled);
        queryParamField.setEnabled(enabled);
    }

    /**
     * Guarda los cambios realizados en el parámetro seleccionado.
     */
    @Override
    protected void saveSelectedParam() {
        if (isInternal || selectedParamRow == -1 || currentSelection == null) return;

        String operation = (String) operationCombo.getSelectedItem();
        String paramKey = getParamKeyForOperation(operation);

        if (!currentSelection.has(paramKey)) return;

        JsonArray params = currentSelection.getAsJsonArray(paramKey);
        if (selectedParamRow < params.size()) {
            JsonObject p = params.get(selectedParamRow).getAsJsonObject();
            String newName = paramNameField.getText();
            p.addProperty("fieldName", newName);
            String paramType = (String) paramTypeCombo.getSelectedItem();
            p.addProperty("paramType", paramType != null ? paramType.toUpperCase() : "STRING");
            p.addProperty("queryParam", queryParamField.getText());
            paramsModel.setValueAt(newName, selectedParamRow, 1);
        }
    }

    /**
     * Carga los datos de una query en los campos del formulario.
     *
     * @param q JsonObject con los datos de la query a cargar
     */
    @Override
    public void loadData(JsonObject q) {
        isInternal = true;
        try {
            operationDataCache.clear();
            queryCodeField.setText(getStringOrEmpty(q, "queryCode"));
            dbSourceCombo.setSelectedItem(getStringOrEmpty(q, "dbSource"));
            collectionField.setText(getStringOrEmpty(q, "collectionName"));
            scriptArea.getTextArea().setText(getStringOrEmpty(q, "script"));
            optionsField.setText(getStringOrEmpty(q, "options"));

            String operation = determineOperation(q);
            initializeOperationCacheFromJson(q);

            currentOperation = operation;
            operationCombo.setSelectedItem(operation);
            showOperationCard(operation);
            loadOperationFieldsFromJson(q, operation);

            String paramKey = getParamKeyForOperation(operation);
            JsonArray p = q.has(paramKey) ? q.getAsJsonArray(paramKey) : new JsonArray();
            reloadParamsTable(p);

            if (!p.isEmpty()) {
                paramsTable.setRowSelectionInterval(0, 0);
            } else {
                selectedParamRow = -1;
                loadSelectedParam();
            }
        } finally {
            isInternal = false;
        }
    }

    /**
     * Determina la operación basándose en los campos del JSON.
     *
     * @param jsonQuery JsonObject de la query
     * @return Nombre de la operación
     */
    private String determineOperation(JsonObject jsonQuery) {
        if (jsonQuery.has("insert") && !getStringOrEmpty(jsonQuery, "insert").isEmpty()) {
            return "Insert";
        } else if (jsonQuery.has("delete") && !getStringOrEmpty(jsonQuery, "delete").isEmpty()) {
            return "Delete";
        }
        return "Update";
    }

    /**
     * Inicializa el cache desde el JSON.
     *
     * @param q JsonObject de la query
     */
    private void initializeOperationCacheFromJson(JsonObject q) {
        String currentOp = determineOperation(q);

        OperationCache updateCache = new OperationCache();
        updateCache.filterQuery = getStringOrEmpty(q, "filter");
        updateCache.updateMongoQuery = getStringOrEmpty(q, "update");
        if ("Update".equals(currentOp) && q.has("updateParameters")) {
            updateCache.parameters = q.getAsJsonArray("updateParameters").deepCopy();
        }
        operationDataCache.put("Update", updateCache);

        OperationCache insertCache = new OperationCache();
        insertCache.insertQuery = getStringOrEmpty(q, "insert");
        if ("Insert".equals(currentOp) && q.has("insertParameters")) {
            insertCache.parameters = q.getAsJsonArray("insertParameters").deepCopy();
        }
        operationDataCache.put("Insert", insertCache);

        OperationCache deleteCache = new OperationCache();
        deleteCache.deleteQuery = getStringOrEmpty(q, "delete");
        if ("Delete".equals(currentOp) && q.has("deleteParameters")) {
            deleteCache.parameters = q.getAsJsonArray("deleteParameters").deepCopy();
        }
        operationDataCache.put("Delete", deleteCache);
    }

    /**
     * Carga los campos de operación desde el JSON.
     *
     * @param q JsonObject de la query
     * @param operation Operación
     */
    private void loadOperationFieldsFromJson(JsonObject q, String operation) {
        switch (operation) {
            case "Update":
                filterQueryArea.getTextArea().setText(getStringOrEmpty(q, "filter"));
                updateMongoQueryArea.getTextArea().setText(getStringOrEmpty(q, "update"));
                break;
            case "Insert":
                insertQueryArea.getTextArea().setText(getStringOrEmpty(q, "insert"));
                break;
            case "Delete":
                deleteQueryArea.getTextArea().setText(getStringOrEmpty(q, "delete"));
                break;
        }
    }

    /**
     * Guarda los datos del formulario en el objeto query.
     *
     * @param q JsonObject donde guardar los datos del formulario
     */
    @Override
    public void saveData(JsonObject q) {
        q.addProperty("queryCode", queryCodeField.getText());
        q.addProperty("dbSource", (String) dbSourceCombo.getSelectedItem());
        q.addProperty("collectionName", collectionField.getText());

        String script = scriptArea.getTextArea().getText();
        if (!script.isEmpty()) {
            q.addProperty("script", script);
        }

        q.addProperty("options", optionsField.getText());

        String operation = (String) operationCombo.getSelectedItem();

        q.remove("filter");
        q.remove("update");
        q.remove("insert");
        q.remove("delete");

        if (!q.has("deleteParameters")) q.add("deleteParameters", new JsonArray());
        if (!q.has("filterParameters")) q.add("filterParameters", new JsonArray());
        if (!q.has("updateParameters")) q.add("updateParameters", new JsonArray());
        if (!q.has("insertParameters")) q.add("insertParameters", new JsonArray());

        switch (operation) {
            case "Update":
                q.addProperty("filter", filterQueryArea.getTextArea().getText());
                q.addProperty("update", updateMongoQueryArea.getTextArea().getText());
                break;
            case "Insert":
                q.addProperty("insert", insertQueryArea.getTextArea().getText());
                break;
            case "Delete":
                q.addProperty("delete", deleteQueryArea.getTextArea().getText());
                break;
        }

        saveSelectedParam();
    }

    /**
     * Configura los listeners para sincronización automática.
     *
     * @param al Listener para acciones (combos, botones)
     * @param dl Listener para cambios de texto
     */
    @Override
    public void setListeners(ActionListener al, DocumentListener dl) {
        if (al == null || dl == null) return;
        this.parentActionListener = al;

        // Listener para queryCodeField que sincroniza con currentSelection
        DocumentListener syncQueryCodeDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    String name = queryCodeField.getText();
                    currentSelection.addProperty("queryCode", name);
                    if (queryCodeUpdater != null) {
                        queryCodeUpdater.accept(name);
                    }
                }
            }
        };
        queryCodeField.getDocument().addDocumentListener(syncQueryCodeDl);

        // Listener para dbSourceCombo que sincroniza con currentSelection
        dbSourceCombo.addActionListener(e -> {
            if (!isInternal && currentSelection != null) {
                currentSelection.addProperty("dbSource", (String) dbSourceCombo.getSelectedItem());
            }
            al.actionPerformed(e);
        });

        // Listener para collectionField que sincroniza con currentSelection
        DocumentListener syncCollectionDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("collectionName", collectionField.getText());
                }
            }
        };
        collectionField.getDocument().addDocumentListener(syncCollectionDl);

        // Listener para scriptArea que sincroniza con currentSelection
        DocumentListener syncScriptDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("script", scriptArea.getTextArea().getText());
                }
            }
        };
        scriptArea.getTextArea().getDocument().addDocumentListener(syncScriptDl);

        // Listener para optionsField que sincroniza con currentSelection
        DocumentListener syncOptionsDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("options", optionsField.getText());
                }
            }
        };
        optionsField.getDocument().addDocumentListener(syncOptionsDl);

        // Listener para operationCombo que sincroniza con currentSelection
        operationCombo.addActionListener(e -> {
            if (!isInternal && currentSelection != null) {
                currentSelection.addProperty("operation", (String) operationCombo.getSelectedItem());
            }
            al.actionPerformed(e);
        });

        // Listener para updateMongoQueryArea que sincroniza con currentSelection
        DocumentListener syncUpdateMongoQueryDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("update", updateMongoQueryArea.getTextArea().getText());
                }
            }
        };
        updateMongoQueryArea.getTextArea().getDocument().addDocumentListener(syncUpdateMongoQueryDl);

        // Listener para filterQueryArea que sincroniza con currentSelection
        DocumentListener syncFilterQueryDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("filter", filterQueryArea.getTextArea().getText());
                }
            }
        };
        filterQueryArea.getTextArea().getDocument().addDocumentListener(syncFilterQueryDl);

        // Listener para insertQueryArea que sincroniza con currentSelection
        DocumentListener syncInsertQueryDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("insert", insertQueryArea.getTextArea().getText());
                }
            }
        };
        insertQueryArea.getTextArea().getDocument().addDocumentListener(syncInsertQueryDl);

        // Listener para deleteQueryArea que sincroniza con currentSelection
        DocumentListener syncDeleteQueryDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("delete", deleteQueryArea.getTextArea().getText());
                }
            }
        };
        deleteQueryArea.getTextArea().getDocument().addDocumentListener(syncDeleteQueryDl);

        // Listeners para parámetros
        DocumentListener paramSyncDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { saveSelectedParam(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { saveSelectedParam(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { saveSelectedParam(); dl.changedUpdate(e); }
        };

        paramNameField.getDocument().addDocumentListener(paramSyncDl);
        paramTypeCombo.addActionListener(e -> { saveSelectedParam(); al.actionPerformed(e); });
        queryParamField.getDocument().addDocumentListener(paramSyncDl);
    }

    /**
     * Cache de datos por operación para mantener estado independiente.
     */
    private static class OperationCache {
        String filterQuery = "";
        String updateMongoQuery = "";
        String insertQuery = "";
        String deleteQuery = "";
        JsonArray parameters = new JsonArray();
    }
}

