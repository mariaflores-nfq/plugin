package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.reader;

import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.components.AtiScriptPanel;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import icons.AtiIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

/**
 * Clase extraída de StepReaderQueriesPanel para desacoplar el formulario de detalles
 */
public class QueryDetailsForm extends JPanel {
    private AtiTextField queryCodeField;
    private AtiComboBox typeCombo;
    private JPanel cardsPanel;
    private CardLayout cardLayout;

    private AtiComboBox dbSourceCombo;
    private AtiTextField collectionField;
    private JLabel collectionLabel;

    private AtiScriptPanel sqlQueryArea;
    private AtiScriptPanel mongoQueryArea;

    private JTable filtersTable;
    private DefaultTableModel filtersModel;
    private JLabel filtersCountLabel;

    private boolean isInternal = false;
    private String currentType = StepConstants.TYPE_SQL;

    private JTable parentQueriesTable;
    private DefaultTableModel parentTableModel;
    private JsonObject currentSelection;
    private ActionListener parentActionListener;

    /**
     * Constructor por defecto del formulario de detalles de query.
     */
    public QueryDetailsForm() {
        setLayout(new BorderLayout());
        setBackground(StepConstants.BBVA_BLUE);
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initForm();
    }

    /**
     * Constructor del formulario de detalles de query con referencia a la tabla padre para sincronización.
     *
     * @param parentTable JTable que contiene la lista de queries, para sincronizar cambios en el nombre
     * @param parentModel DefaultTableModel asociado a la tabla padre, para actualizar datos directamente
     */
    public QueryDetailsForm(JTable parentTable, DefaultTableModel parentModel) {
        this.parentQueriesTable = parentTable;
        this.parentTableModel = parentModel;
        setLayout(new BorderLayout());
        setBackground(new Color(248, 249, 250));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initForm();
    }

    /**
     * Inicializa y configura todos los componentes visuales del formulario.
     */
    private void initForm() {
        // Crear panel interno con GridBagLayout para los componentes
        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0; c.insets = new Insets(0, 0, 5, 10);
        c.gridx = 0; c.weightx = 0.5;  c.gridwidth = 1;
        queryCodeField = new AtiTextField();
        queryCodeField.setPreferredSize(new Dimension(0, 30));
        JPanel sqlWrapper = createLabeledField("Query Code",queryCodeField);
        innerPanel.add(sqlWrapper, c);

        c.gridx = 1; c.insets = new Insets(0, 10, 5, 0);
        typeCombo = new AtiComboBox(new String[]{StepConstants.TYPE_SQL, StepConstants.TYPE_MONGO});
        typeCombo.setPreferredSize(new Dimension(0, 30));
        typeCombo.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED && !isInternal) switchView((String)e.getItem());
        });
        JPanel typeWrapper = createLabeledField("Type",typeCombo);
        innerPanel.add(typeWrapper, c);

        c.gridy = 2; c.insets = new Insets(0, 0, 5, 10);
        c.gridx = 0; c.weightx = 0.5;
        dbSourceCombo = new AtiComboBox(new String[]{"Oracle_Dev", "Mongo_Prod"});
        dbSourceCombo.setPreferredSize(new Dimension(0, 30));
        JPanel dbWrapper = createLabeledField("Db Source",dbSourceCombo);
        innerPanel.add(dbWrapper, c);

        c.gridx = 1; c.insets = new Insets(0, 10, 5, 0);
        collectionField = new AtiTextField();
        collectionField.setPreferredSize(new Dimension(0, 30));
        JPanel collectionWrapper = createLabeledField("collectionName",collectionField);
        // Inicializar collectionLabel obteniendo la etiqueta del wrapper
        collectionLabel = (JLabel) collectionWrapper.getComponent(0);
        innerPanel.add(collectionWrapper, c);

        c.gridy = 4; c.gridx = 0; c.gridwidth = 2;
        c.weightx = 1.0; c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(new Color(248, 249, 250));

        cardsPanel.add(createSqlPanel(), StepConstants.TYPE_SQL);
        cardsPanel.add(createMongoPanel(), StepConstants.TYPE_MONGO);

        innerPanel.add(cardsPanel, c);
        collectionLabel.setVisible(false);
        collectionField.setVisible(false);

        // Agregar el panel interno al BorderLayout externo
        add(innerPanel, BorderLayout.CENTER);
    }

    /**
     * Cambia la vista del formulario según el tipo seleccionado (SQL o Mongo), mostrando los campos correspondientes.
     *
     * @param type El tipo de query seleccionado, que determina qué campos se muestran
     */
    private void switchView(String type) {
        currentType = type;
        cardLayout.show(cardsPanel, type);
        boolean isMongo = StepConstants.TYPE_MONGO.equals(type);
        collectionLabel.setVisible(isMongo);
        collectionField.setVisible(isMongo);
        if(parentActionListener != null) parentActionListener.actionPerformed(null);
    }

    /**
     * Crea el panel específico para consultas SQL, con un área de texto para la query y un espacio para futuros campos relacionados.
     *
     * @return JPanel configurado para mostrar los detalles de una consulta SQL
     */
    private JPanel createSqlPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(248, 249, 250));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 0, 0);
        sqlQueryArea = new AtiScriptPanel();
        JPanel scriptWrapper = createLabeledField("Script", sqlQueryArea);
        scriptWrapper.setBackground(StepConstants.BG_SIDEBAR);
        p.add(scriptWrapper, c);

        c.gridy = 1;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        p.add(Box.createVerticalGlue(), c);
        return p;
    }

    /**
     * Crea el panel específico para consultas MongoDB, con un área de texto para la query y una tabla para gestionar filtros agregados.
     *
     * @return JPanel configurado para mostrar los detalles de una consulta MongoDB, incluyendo gestión de filtros
     */
    private JPanel createMongoPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(248, 249, 250));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 0, 0);
        // Editor para consultas Mongo (igual funcionalidad que SQL)
        mongoQueryArea = new AtiScriptPanel();
//        mongoQueryArea.setMinimumSize(new Dimension(100, 200));
//        mongoQueryArea.setPreferredSize(new Dimension(100, 200));
//        mongoQueryArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        JPanel scriptWrapper = createLabeledField("Script", mongoQueryArea);
        scriptWrapper.setBackground(StepConstants.BG_SIDEBAR);
        p.add(scriptWrapper, c);

        c.gridy = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(15, 0, 5, 0);
        JLabel aggLabel = new JLabel("Aggregate Query");
        aggLabel.setFont(new Font("Lato", Font.PLAIN, 18));
        aggLabel.setForeground(SchedulerTheme.TEXT_MAIN);
        p.add(aggLabel, c);

        c.gridy = 2;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        p.add(createFiltersSubTable(), c);

        return p;
    }

    /**
     * Crea el subpanel que contiene la tabla de filtros para consultas MongoDB, junto con un botón para agregar nuevos filtros.
     *
     * @return JPanel configurado con una tabla para gestionar los filtros de una consulta MongoDB y un botón para agregar nuevos filtros
     */
    private JPanel createFiltersSubTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createLineBorder(StepConstants.BBVA_BLUE));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(248, 249, 250));
        header.setBorder(new EmptyBorder(5, 10, 5, 10));

        filtersCountLabel = new JLabel("Filters(0)");
        filtersCountLabel.setFont(new Font("Lato", Font.PLAIN, 12));
        filtersCountLabel.setForeground(Color.GRAY);

        JButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> {
            if (currentSelection == null) return;
            JsonArray filters;
            if (currentSelection.has("filters") && currentSelection.get("filters").isJsonArray()) {
                filters = currentSelection.getAsJsonArray("filters");
            } else {
                filters = new JsonArray();
                currentSelection.add("filters", filters);
            }

            filters.add("New Filter");
            reloadFiltersTable(filters);

            if (parentActionListener != null) parentActionListener.actionPerformed(e);
        });

        header.add(filtersCountLabel, BorderLayout.WEST);
        header.add(btn, BorderLayout.EAST);

        filtersModel = new DefaultTableModel(new String[]{"#", "Filter", "Actions"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 1; }
        };
        filtersTable = new JTable(filtersModel);
        filtersTable.setRowHeight(25);
        filtersTable.setShowGrid(false);

        filtersTable.getDefaultEditor(Object.class).addCellEditorListener(new CellEditorListener() {
            public void editingStopped(ChangeEvent e) {
                if (currentSelection == null || !currentSelection.has("filters")) return;
                // Verificar que filters sea un JsonArray antes de intentar convertir
                if (!currentSelection.get("filters").isJsonArray()) return;

                int row = filtersTable.getSelectedRow();
                JsonArray filters = currentSelection.getAsJsonArray("filters");
                if (row != -1 && row < filters.size()) {
                    String newVal = (String) filtersTable.getValueAt(row, 1);
                    filters.set(row, new com.google.gson.JsonPrimitive(newVal));
                    if (parentActionListener != null) parentActionListener.actionPerformed(null);
                }
            }
            public void editingCanceled(ChangeEvent e) {}
        });

        JScrollPane sp = new JScrollPane(filtersTable);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(0, 200));

        panel.add(header, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Elimina un filtro de la tabla de filtros de MongoDB en función de la fila seleccionada, actualizando el JSON y la vista.
     *
     * @param row La fila del filtro a eliminar, que corresponde al índice en el array de filtros del JSON
     */
    public void deleteFilter(int row) {
        if(currentSelection != null && currentSelection.has("filters") && currentSelection.get("filters").isJsonArray()) {
            JsonArray filters = currentSelection.getAsJsonArray("filters");
            if (row >= 0 && row < filters.size()) {
                filters.remove(row);
                reloadFiltersTable(filters);
            }
        }
    }

    /**
     * Recarga la tabla de filtros con los datos actuales del array de filtros, actualizando el contador y los valores mostrados.
     *
     * @param filters JsonArray que contiene los filtros actuales de la consulta MongoDB, utilizado para actualizar la tabla y el contador
     */
    private void reloadFiltersTable(JsonArray filters) {
        if(filtersModel == null || filters == null) return;
        filtersModel.setRowCount(0);
        filtersCountLabel.setText("Filters(" + filters.size() + ")");
        for(int i=0; i<filters.size(); i++) {
            if (filters.get(i).isJsonPrimitive()) {
                filtersModel.addRow(new Object[]{String.format("%02d", i+1), filters.get(i).getAsString(), ""});
            } else {
                filtersModel.addRow(new Object[]{String.format("%02d", i+1), filters.get(i).toString(), ""});
            }
        }
    }


    /** Carga los datos de una query en los campos del formulario, actualizando la vista según el tipo de query y llenando los campos correspondientes.
     *
     * @param q JsonObject que contiene los datos de la query a cargar, incluyendo tipo, código, fuente de datos, colección y contenido de la query
     */
    public void loadData(JsonObject q) {
        this.currentSelection = q;
        isInternal = true;
        try {
            String type = q.has("type") ? q.get("type").getAsString() : StepConstants.TYPE_SQL;
            typeCombo.setSelectedItem(type);
            switchView(type);

            queryCodeField.setText(q.has("queryCode") ? q.get("queryCode").getAsString() : "");
            dbSourceCombo.setSelectedItem(q.has("dbSource") ? q.get("dbSource").getAsString() : "");
            collectionField.setText(q.has("collectionName") ? q.get("collectionName").getAsString() : "");

            String content = q.has("sqlQuery") ? q.get("sqlQuery").getAsString() : "";

            if (StepConstants.TYPE_SQL.equals(type)) {
                sqlQueryArea.getTextArea().setText(content);
            } else {
                mongoQueryArea.getTextArea().setText(content);
                JsonArray f;
                if (q.has("filters") && q.get("filters").isJsonArray()) {
                    f = q.getAsJsonArray("filters");
                } else {
                    f = new JsonArray();
                }
                reloadFiltersTable(f);
            }
        } finally {
            isInternal = false;
        }
    }

    /**
     * Resetea el formulario a su estado vacío, limpiando todos los campos.
     * Se utiliza cuando se cambia de tipo de reader o se vacía la lista de queries.
     */
    public void resetForm() {
        isInternal = true;
        try {
            queryCodeField.setText("");
            typeCombo.setSelectedItem(StepConstants.TYPE_SQL);
            switchView(StepConstants.TYPE_SQL);
            dbSourceCombo.setSelectedItem("Oracle_Dev");
            collectionField.setText("");
            sqlQueryArea.getTextArea().setText("");
            mongoQueryArea.getTextArea().setText("");
            if (filtersModel != null) {
                filtersModel.setRowCount(0);
            }
            if (filtersCountLabel != null) {
                filtersCountLabel.setText("Filters(0)");
            }
            currentSelection = null;
        } finally {
            isInternal = false;
        }
    }


    public void setListeners(ActionListener al, DocumentListener dl) {
        this.parentActionListener = al;
        if(al == null || dl == null) return;

        // Listener para queryCodeField que sincroniza con currentSelection y la tabla
        DocumentListener syncQueryCodeDl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }
            void sync() {
                if (!isInternal && currentSelection != null) {
                    String name = queryCodeField.getText();
                    currentSelection.addProperty("queryCode", name);
                    if (parentQueriesTable != null && parentTableModel != null) {
                        int row = parentQueriesTable.getSelectedRow();
                        if(row != -1) parentTableModel.setValueAt(name, row, 1);
                    }
                }
            }
        };
        queryCodeField.getDocument().addDocumentListener(syncQueryCodeDl);

        dbSourceCombo.addActionListener(e -> {
            if (!isInternal && currentSelection != null) {
                currentSelection.addProperty("dbSource", (String) dbSourceCombo.getSelectedItem());
            }
            al.actionPerformed(e);
        });

        DocumentListener syncCollectionDl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }
            void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("collectionName", collectionField.getText());
                }
            }
        };
        collectionField.getDocument().addDocumentListener(syncCollectionDl);

        DocumentListener syncSqlDl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }
            void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("sqlQuery", sqlQueryArea.getTextArea().getText());
                }
            }
        };
        sqlQueryArea.getTextArea().getDocument().addDocumentListener(syncSqlDl);

        // Listener para mongoQueryArea que sincroniza con currentSelection
        DocumentListener syncMongoDl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }
            void sync() {
                if (!isInternal && currentSelection != null) {
                    currentSelection.addProperty("sqlQuery", mongoQueryArea.getTextArea().getText());
                }
            }
        };
        mongoQueryArea.getTextArea().getDocument().addDocumentListener(syncMongoDl);

        typeCombo.addActionListener(e -> {
            if (!isInternal && currentSelection != null) {
                currentSelection.addProperty("type", (String) typeCombo.getSelectedItem());
            }
        });
    }

    /**
     * Crea un panel con etiqueta y componente de entrada.
     * @param labelText Texto de la etiqueta.
     * @param inputComponent Componente de entrada asociado.
     * @return Panel con etiqueta y campo.
     */
    protected JPanel createLabeledField(String labelText, JComponent inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, BorderLayout.NORTH);
        panel.add(inputComponent, BorderLayout.CENTER);
        return panel;
    }

}
