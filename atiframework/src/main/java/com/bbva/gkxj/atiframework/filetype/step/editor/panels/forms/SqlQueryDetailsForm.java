package com.bbva.gkxj.atiframework.filetype.step.editor.panels.forms;

import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.bbva.gkxj.atiframework.components.AtiComboBox;
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
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Clase que encapsula todos los componentes visuales y la lógica
 * de interacción para configurar una query SQL individual.
 *
 * Extiende {@link AbstractQueryDetailsForm} para heredar funcionalidad
 * común como creación de labels, gestión de acciones en tablas, etc.
 */
public class SqlQueryDetailsForm extends AbstractQueryDetailsForm {

    private AtiTextField queryCodeField;
    private AtiComboBox dbSourceCombo;
    private AtiScriptPanel sqlQueryArea;
    private AtiScriptPanel scriptArea;

    private JTable paramsTable;
    private DefaultTableModel paramsModel;
    private JLabel paramsCountLabel;
    private JPanel paramsCardContainer;
    private CardLayout paramsCardLayout;

    private AtiTextField paramNameField;
    private AtiComboBox paramTypeCombo;
    private AtiTextField queryParamField;

    /**
     * Constructor del formulario de detalles SQL.
     *
     * @param queryCodeUpdater Callback para actualizar el queryCode en la tabla padre
     */
    public SqlQueryDetailsForm(Consumer<String> queryCodeUpdater) {
        super(queryCodeUpdater);
        initForm();
    }


    /**
     * Inicializa los campos específicos del formulario SQL:
     * Query Code, DB Source, SQL Query, Script y Parameters.
     */
    @Override
    protected void initForm() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0; c.insets = new Insets(0, 0, 15, 10);
        c.gridx = 0; c.weightx = 0.5; c.gridwidth = 1;
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
        c.weightx = 1.0; c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 15, 0);
        sqlQueryArea = new AtiScriptPanel(3);
        JPanel sqlWrapper = createLabeledField("SQL Query", sqlQueryArea);
        sqlWrapper.setBackground(BG_SIDEBAR);
        add(sqlWrapper, c);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 15, 0);
        scriptArea = new AtiScriptPanel(3);
        JPanel scriptWrapper = createLabeledField("Script", scriptArea);
        scriptWrapper.setBackground(BG_SIDEBAR);
        add(scriptWrapper, c);

        c.gridy = 4; c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        JLabel paramTitle = new JLabel("Query Parameter List");
        paramTitle.setFont(paramTitle.getFont().deriveFont(Font.BOLD, 16f));
        paramTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        add(paramTitle, c);

        c.gridy = 5; c.fill = GridBagConstraints.BOTH; c.weighty = 0.1;
        c.insets = new Insets(0, 0, 0, 0);

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
     * Guarda los cambios del parámetro seleccionado en el JSON de la query.
     */
    @Override
    protected void saveSelectedParam() {
        if (isInternal || selectedParamRow == -1 || currentSelection == null
            || !currentSelection.has("parameters")) return;

        JsonArray params = currentSelection.getAsJsonArray("parameters");
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
     * Crea el panel que se muestra cuando no hay parámetros configurados.
     *
     * @return JPanel con el diseño para el estado vacío de parámetros
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
        title.setFont(title.getFont().deriveFont(Font.BOLD));

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
     * @return JPanel con la tabla de parámetros y su encabezado
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
        paramsCountLabel.setFont(paramsCountLabel.getFont().deriveFont(Font.BOLD));

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
     * @return JPanel con los campos de detalles del parámetro
     */
    private JPanel createParamDetailsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_SIDEBAR);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0; c.insets = new Insets(0, 0, 5, 10);
        c.gridx = 0; c.weightx = 0.5;
        JLabel lblParamName = createLabel("Parameter Name");
        lblParamName.setFont(lblParamName.getFont().deriveFont(Font.BOLD));
        p.add(lblParamName, c);
        c.gridx = 1; c.insets = new Insets(0, 10, 5, 0);
        JLabel lblQueryParam = createLabel("Query Param");
        lblQueryParam.setFont(lblQueryParam.getFont().deriveFont(Font.BOLD));
        p.add(lblQueryParam, c);

        c.gridy = 1; c.insets = new Insets(0, 0, 10, 10);
        c.gridx = 0;
        paramNameField = new AtiTextField();
        paramNameField.setPreferredSize(new Dimension(0, 30));
        p.add(paramNameField, c);

        c.gridx = 1; c.insets = new Insets(0, 10, 10, 0);
        queryParamField = new AtiTextField();
        queryParamField.setPreferredSize(new Dimension(0, 30));
        p.add(queryParamField, c);

        c.gridy = 2; c.gridx = 0;
        c.insets = new Insets(0, 0, 5, 10);
        JLabel lblParamType = createLabel("Param Type");
        lblParamType.setFont(lblParamType.getFont().deriveFont(Font.BOLD));
        p.add(lblParamType, c);

        c.gridy = 3; c.insets = new Insets(0, 0, 0, 10);
        paramTypeCombo = new AtiComboBox(new String[]{"String", "Integer", "Long", "Double", "Date"});
        paramTypeCombo.setPreferredSize(new Dimension(0, 30));
        p.add(paramTypeCombo, c);

        return p;
    }

    /**
     * Agrega un nuevo parámetro a la query actual con valores por defecto.
     */
    private void addNewParam() {
        if (currentSelection == null) return;

        JsonArray params = currentSelection.has("parameters")
            ? currentSelection.getAsJsonArray("parameters")
            : new JsonArray();

        if (!currentSelection.has("parameters")) {
            currentSelection.add("parameters", params);
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
     * Elimina un parámetro del formulario.
     *
     * @param row Índice del parámetro a eliminar
     */
    @Override
    public void deleteParam(int row) {
        if (currentSelection == null || !currentSelection.has("parameters")) return;

        JsonArray params = currentSelection.getAsJsonArray("parameters");
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
     * Recarga la tabla de parámetros con los datos proporcionados.
     *
     * @param params JsonArray con la lista de parámetros
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
     * Carga los datos del parámetro seleccionado en los campos de edición.
     */
    private void loadSelectedParam() {
        isInternal = true;
        try {
            if (selectedParamRow != -1 && currentSelection != null && currentSelection.has("parameters")) {
                JsonArray params = currentSelection.getAsJsonArray("parameters");
                if (selectedParamRow < params.size()) {
                    JsonObject p = params.get(selectedParamRow).getAsJsonObject();
                    paramNameField.setText(getStringOrEmpty(p, "fieldName"));
                    String paramType = getStringOrDefault(p, "paramType", "STRING");
                    paramTypeCombo.setSelectedItem(formatParamTypeForCombo(paramType));
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
     * Convierte el formato del paramType del JSON al formato del combo.
     */
    private String formatParamTypeForCombo(String paramType) {
        if (paramType == null || paramType.isEmpty()) return "String";
        return paramType.substring(0, 1).toUpperCase() + paramType.substring(1).toLowerCase();
    }

    /**
     * Limpia los campos de edición del parámetro.
     */
    private void clearParamFields() {
        paramNameField.setText("");
        paramTypeCombo.setSelectedIndex(-1);
        queryParamField.setText("");
    }

    /**
     * Habilita o deshabilita los campos de edición del parámetro.
     *
     * @param enabled true para habilitar, false para deshabilitar
     */
    private void setParamFieldsEnabled(boolean enabled) {
        paramNameField.setEnabled(enabled);
        paramTypeCombo.setEnabled(enabled);
        queryParamField.setEnabled(enabled);
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
            queryCodeField.setText(getStringOrEmpty(q, "queryCode"));
            dbSourceCombo.setSelectedItem(getStringOrEmpty(q, "dbSource"));
            sqlQueryArea.getTextArea().setText(getStringOrEmpty(q, "sqlQuery"));
            scriptArea.getTextArea().setText(getStringOrEmpty(q, "script"));

            JsonArray p = q.has("parameters") ? q.getAsJsonArray("parameters") : new JsonArray();
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
     * Guarda los datos del formulario en el objeto query.
     *
     * @param q JsonObject donde guardar los datos del formulario
     */
    @Override
    public void saveData(JsonObject q) {
        q.addProperty("queryCode", queryCodeField.getText());
        q.addProperty("sqlQuery", sqlQueryArea.getTextArea().getText());
        q.addProperty("dbSource", (String) dbSourceCombo.getSelectedItem());

        String script = scriptArea.getTextArea().getText();
        if (!script.isEmpty()) {
            q.addProperty("script", script);
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

        // Listener para sqlQueryArea que sincroniza con currentSelection
        DocumentListener syncSqlDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("sqlQuery", sqlQueryArea.getTextArea().getText());
                }
            }
        };
        sqlQueryArea.getTextArea().getDocument().addDocumentListener(syncSqlDl);

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

}
