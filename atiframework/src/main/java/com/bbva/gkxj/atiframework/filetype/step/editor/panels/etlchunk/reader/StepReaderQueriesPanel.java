package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.reader;

import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.bbva.gkxj.atiframework.components.AtiJSpinner;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import icons.AtiIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.*;

/**
 * Panel específico para configurar queries SQL en el Reader de un .step.
 * Este panel permite al usuario añadir, eliminar y configurar múltiples queries SQL que el Reader ejecutará.
 * Cada query tiene su propio formulario de detalles para configurar su código SQL, tipo y filtros asociados.
 */
public class StepReaderQueriesPanel extends JPanel implements StepReaderSubPanel{


    private static final int ACTION_COLUMN_INDEX = 2;
    private static final int FILTER_ACTION_COLUMN_INDEX = 2;
    private static final Dimension FIELD_DIMENSION = new Dimension(220, 30);

    private final Project project;
    private final VirtualFile file;

    private AtiJSpinner threadsField;
    private AtiJSpinner commitIntervalField;
    private JTable queriesTable;
    private DefaultTableModel tableModel;
    private JLabel queriesCounterLabel;

    private JBSplitter splitter;
    private JPanel sidebarPanel;
    private JPanel emptyPanel;
    private JPanel fullEmptyPanel;
    private JScrollPane formScrollPane;
    private QueryDetailsForm detailsForm;
    private JPanel centerContainer;
    private CardLayout centerCardLayout;

    private List<JsonObject> queriesData = new ArrayList<>();
    private JsonObject currentSelection = null;
    private boolean isUpdatingUI = false;

    private ActionListener parentActionListener;

    /**
     * Constructor del panel de configuración de queries SQL para el Reader.
     *
     * @param project Proyecto de IntelliJ IDEA activo
     * @param file    Archivo virtual .step siendo editado
     */
    public StepReaderQueriesPanel(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initComponents();
    }

    /**
     * Inicializa y configura todos los componentes visuales del panel.
     * Crea la estructura principal con un sidebar para la lista de queries y un área de detalles para configurar cada query.
     * También establece el estado vacío inicial cuando no hay queries configurados.
     */
    private void initComponents() {
        add(createGlobalConfigPanel(), BorderLayout.NORTH);

        sidebarPanel = createSidebar();
        detailsForm = new QueryDetailsForm(queriesTable, tableModel);

        emptyPanel = new JPanel();
        emptyPanel.setBackground(Color.WHITE);
        fullEmptyPanel = createFullEmptyStatePanel();

        formScrollPane = new JScrollPane(detailsForm);
        formScrollPane.setBorder(null);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        splitter = new JBSplitter(false, 0.35f);
        splitter.setFirstComponent(sidebarPanel);
        splitter.setSecondComponent(emptyPanel);
        splitter.setDividerWidth(2);

        centerCardLayout = new CardLayout();
        centerContainer = new JPanel(centerCardLayout);
        centerContainer.setBackground(Color.WHITE);
        centerContainer.add(fullEmptyPanel, StepConstants.CARD_EMPTY);
        centerContainer.add(splitter, StepConstants.CARD_SPLITTER);

        centerCardLayout.show(centerContainer, StepConstants.CARD_EMPTY);

        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * Crea el panel de configuración global del Reader, que incluye campos para el número de threads y el intervalo de commit.
     * Estos parámetros afectan a la ejecución general del Reader y no a queries individuales.
     *
     * @return JPanel con los campos de configuración global
     */
    private JPanel createGlobalConfigPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(JBUI.Borders.empty(15, 20, 10, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;

        c.gridy = 0; c.gridx = 0; c.gridwidth = 3;
        c.insets = new Insets(0, 0, 15, 0);
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Reader Configuration");
        title.setFont(StepConstants.FONT_SECTION_TITLE);
        title.setForeground(SchedulerTheme.TEXT_MAIN);
        p.add(title, c);

        c.gridwidth = 1;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 5, 20);

        c.gridy = 1;
        c.gridx = 0; p.add(createLabel("# of Threads"), c);
        c.gridx = 1; p.add(createLabel("Commit Interval"), c);

        c.gridy = 2;
        c.gridx = 0;
        threadsField = new AtiJSpinner();
        threadsField.setPreferredSize(FIELD_DIMENSION);
        applyBlueFocusBorder(threadsField);
        p.add(threadsField, c);

        c.gridx = 1;
        commitIntervalField = new AtiJSpinner();
        commitIntervalField.setPreferredSize(FIELD_DIMENSION);
        applyBlueFocusBorder(commitIntervalField);
        p.add(commitIntervalField, c);

        c.gridx = 2; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        p.add(Box.createHorizontalGlue(), c);

        return p;
    }

    /**
     * Crea el sidebar que contiene la lista de queries SQL configurados.
     * Permite añadir nuevas queries con el botón "+" y muestra un contador del número total de queries.
     * Cada query se muestra en una tabla con su código y un botón de acciones para eliminarla.
     *
     * @return JPanel con el sidebar de queries
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(StepConstants.BG_SIDEBAR);
        sidebar.setBorder(new EmptyBorder(0, 20, 10, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(StepConstants.BG_SIDEBAR);
        header.setBorder(new EmptyBorder(10, 5, 10, 10));

        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Query List");
        title.setFont(StepConstants.FONT_TITLE);
        title.setForeground(SchedulerTheme.TEXT_MAIN);
        titleBox.add(title, BorderLayout.NORTH);

        queriesCounterLabel = new JLabel("Queries (0)");
        queriesCounterLabel.setFont(StepConstants.FONT_SUBTITLE);
        queriesCounterLabel.setForeground(StepConstants.TEXT_GRAY);
        titleBox.add(queriesCounterLabel, BorderLayout.SOUTH);

        header.add(titleBox, BorderLayout.WEST);

        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnContainer.setOpaque(false);
        JButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> {
            addNewQuery();
            if (parentActionListener != null) parentActionListener.actionPerformed(e);
        });
        btnContainer.add(btn);
        header.add(btnContainer, BorderLayout.EAST);

        sidebar.add(header, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"#", "Query Code", "Actions"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        queriesTable = new JBTable(tableModel);
        queriesTable.setRowHeight(40);
        queriesTable.setShowGrid(false);
        queriesTable.setIntercellSpacing(new Dimension(0, 0));
        queriesTable.setSelectionBackground(StepConstants.BG_TABLE_SELECTION);
        queriesTable.setSelectionForeground(Color.BLACK);

        queriesTable.getTableHeader().setBackground(StepConstants.BG_TABLE_HEADER);
        queriesTable.getTableHeader().setFont(StepConstants.FONT_HEADER_TABLE);
        queriesTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
        queriesTable.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, StepConstants.BORDER_COLOR));

        queriesTable.getColumnModel().getColumn(0).setMaxWidth(40);
        queriesTable.getColumnModel().getColumn(0).setCellRenderer(new CenterGrayRenderer());
        queriesTable.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setCellRenderer(new MoreActionsRenderer());

        queriesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = queriesTable.getSelectedRow();
                if (row != -1 && row < queriesData.size()) {
                    currentSelection = queriesData.get(row);
                    showFormPanel(true);
                    isUpdatingUI = true;
                    try {
                        detailsForm.loadData(currentSelection);
                    } finally {
                        isUpdatingUI = false;
                    }
                } else {
                    currentSelection = null;
                    showFormPanel(false);
                }
            }
        });

        addActionsListener(queriesTable, false);

        JBScrollPane scroll = new JBScrollPane(queriesTable);
        scroll.setBorder(new MatteBorder(1, 1, 1, 1, StepConstants.BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        sidebar.add(scroll, BorderLayout.CENTER);

        return sidebar;
    }

    /**
     * Muestra u oculta el panel de detalles del formulario según si hay una query seleccionada.
     * Si show es true, muestra el formScrollPane con los detalles de la query; si es false, muestra un panel vacío.
     *
     * @param show boolean que indica si se debe mostrar el formulario de detalles
     */
    private void showFormPanel(boolean show) {
        JComponent right = show ? formScrollPane : emptyPanel;
        if (splitter.getSecondComponent() != right) {
            splitter.setSecondComponent(right);
            splitter.revalidate();
            splitter.repaint();
        }
    }

    /**
     * Panel vacío que ocupa toda la ventana cuando no hay queries.
     * Muestra el estado vacío con el título "Query List" y el botón +.
     */
    private JPanel createFullEmptyStatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StepConstants.BG_SIDEBAR);
        panel.setBorder(new EmptyBorder(0, 20, 10, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(StepConstants.BG_SIDEBAR);
        header.setBorder(new EmptyBorder(10, 5, 10, 10));

        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Query List");
        title.setFont(StepConstants.FONT_TITLE);
        title.setForeground(SchedulerTheme.TEXT_MAIN);
        titleBox.add(title, BorderLayout.NORTH);

        JLabel counterLabel = new JLabel("Queries (0)");
        counterLabel.setFont(StepConstants.FONT_SUBTITLE);
        counterLabel.setForeground(StepConstants.TEXT_GRAY);
        titleBox.add(counterLabel, BorderLayout.SOUTH);

        header.add(titleBox, BorderLayout.WEST);

        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnContainer.setOpaque(false);
        JButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> {
            addNewQuery();
            if (parentActionListener != null) parentActionListener.actionPerformed(e);
        });
        btnContainer.add(btn);
        header.add(btnContainer, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(StepConstants.BG_SIDEBAR);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Agrega una nueva query SQL a la lista con valores por defecto.
     * Actualiza la tabla y el contador de queries, y selecciona automáticamente la nueva query para mostrar su formulario de detalles.
     */
    private void addNewQuery() {
        boolean wasEmpty = queriesData.isEmpty();
        int n = queriesData.size() + 1;
        JsonObject newQ = new JsonObject();
        newQ.addProperty("queryCode", "NewQuery");
        newQ.addProperty("type", StepConstants.TYPE_SQL);
        newQ.addProperty("sqlQuery", "");
        newQ.addProperty("dbSource", "Oracle_Dev");
        newQ.add("filters", new JsonArray());

        queriesData.add(newQ);
        tableModel.addRow(new Object[]{String.format("%02d", n), "NewQuery", ""});
        queriesCounterLabel.setText("Queries (" + queriesData.size() + ")");
        if (wasEmpty) {
            centerCardLayout.show(centerContainer, StepConstants.CARD_SPLITTER);
        }
        int lastRow = tableModel.getRowCount() - 1;
        queriesTable.setRowSelectionInterval(lastRow, lastRow);
    }

    private void deleteQuery(int row) {
        if (row < 0 || row >= queriesData.size()) return;
        queriesData.remove(row);
        tableModel.removeRow(row);
        for (int i = 0; i < queriesData.size(); i++) {
            tableModel.setValueAt(String.format("%02d", i + 1), i, 0);
        }
        queriesCounterLabel.setText("Queries (" + queriesData.size() + ")");

        if (queriesData.isEmpty()) {
            showFormPanel(false);
            currentSelection = null;
            centerCardLayout.show(centerContainer, StepConstants.CARD_EMPTY);
        } else {
            int s = Math.max(0, row - 1);
            queriesTable.setRowSelectionInterval(s, s);
        }
        if(parentActionListener != null)
            parentActionListener.actionPerformed(new ActionEvent(queriesTable, ActionEvent.ACTION_PERFORMED, "QueryDeleted"));
    }

    /**
     * Parsea un elemento JSON de la lista de queries y lo convierte en un JsonObject interno.
     * Solo soporta el nuevo formato (aggregateMongoQuery/sqlQuery).
     *
     * @param item  objeto JSON que representa una query.
     * @param index índice de la query dentro de la lista, usado para generar el id.
     * @return JsonObject con la representación de la query en el esquema interno.
     */
    private JsonObject parseConditionData(JsonObject item, int index) {
        JsonObject condition = new JsonObject();
        condition.addProperty("id", String.format("%02d", index + 1));

        if (item.has("queryCode")) {
            condition.addProperty("queryCode", item.get("queryCode").getAsString());
        }

        if (item.has("sqlQuery")) {
            condition.addProperty("sqlContent", item.get("sqlQuery").getAsString());
        }
        if (item.has("dbSource")) {
            condition.addProperty("dbSource", item.get("dbSource").getAsString());
        }

        // Normalizar filters a un JsonArray de strings (si no existe, devolver array vacío)
        JsonArray filtersArr = new JsonArray();

        if (item.has("aggregateMongoQuery") && item.get("aggregateMongoQuery").isJsonObject()) {
            condition.addProperty("type", StepConstants.TYPE_MONGO);
            JsonObject aggregateMongoQuery = item.getAsJsonObject("aggregateMongoQuery");

            if (aggregateMongoQuery.has("collectionName")) {
                condition.addProperty("collectionName", aggregateMongoQuery.get("collectionName").getAsString());
            }

            if (aggregateMongoQuery.has("filter") && aggregateMongoQuery.get("filter").isJsonArray()) {
                JsonArray srcFilters = aggregateMongoQuery.getAsJsonArray("filter");
                for (int j = 0; j < srcFilters.size(); j++) {
                    if (srcFilters.get(j).isJsonPrimitive()) {
                        filtersArr.add(srcFilters.get(j).getAsString());
                    } else {
                        filtersArr.add(srcFilters.get(j).toString());
                    }
                }
            }
        }
        else {
            condition.addProperty("type", StepConstants.TYPE_SQL);
        }

        condition.add("filters", filtersArr);

        return condition;
    }

    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado.
     * Carga la configuración global (número de threads, intervalo de commit) desde el nivel raíz y la lista de queries.
     *
     * @param jsonObject JsonObject con la configuración del Reader y sus queries
     */
    @Override
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("reader")) return;
        isUpdatingUI = true;
        try {
            JsonObject reader = jsonObject.getAsJsonObject("reader");

            if (jsonObject.has("numThreads") && jsonObject.get("numThreads").isJsonPrimitive())
                threadsField.setValue(jsonObject.get("numThreads").getAsInt());
            else
                threadsField.setValue(0);

            if (jsonObject.has("commitInterval") && jsonObject.get("commitInterval").isJsonPrimitive())
                commitIntervalField.setValue(jsonObject.get("commitInterval").getAsInt());
            else
                commitIntervalField.setValue(0);

            queriesData.clear();
            tableModel.setRowCount(0);

            if (reader.has("queries") && reader.get("queries").isJsonArray()) {
                JsonArray arr = reader.getAsJsonArray("queries");
                for (int i = 0; i < arr.size(); i++) {
                    JsonObject q = arr.get(i).getAsJsonObject();
                    JsonObject p = parseConditionData(q, i);
                    queriesData.add(p);
                    String code = p.has("queryCode") ? p.get("queryCode").getAsString() : "";
                    tableModel.addRow(new Object[]{String.format("%02d", i + 1), code, ""});
                }
            }
            queriesCounterLabel.setText("Queries (" + queriesData.size() + ")");

            if (!queriesData.isEmpty()) {
                centerCardLayout.show(centerContainer, StepConstants.CARD_SPLITTER);
                queriesTable.setRowSelectionInterval(0, 0);
            } else {
                currentSelection = null;
                showFormPanel(false);
                centerCardLayout.show(centerContainer, StepConstants.CARD_EMPTY);
            }
        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Actualiza el JsonObject con los datos actuales del formulario.
     * Guarda la configuración global del Reader y la lista de queries SQL configuradas.
     *
     * @param jsonObject JsonObject a actualizar con los datos del formulario
     */
    @Override
    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;
        JsonObject reader;
        if (jsonObject.has("reader")) {
            reader = jsonObject.getAsJsonObject("reader");
        }
        else {
            reader = new JsonObject();
            jsonObject.add("reader", reader);
        }

        int numThreads = (Integer) threadsField.getValue();
        int commitInterval = (Integer) commitIntervalField.getValue();

        if (numThreads > 0) {
            jsonObject.addProperty("numThreads", numThreads);
        } else {
            jsonObject.remove("numThreads");
        }
        if (commitInterval > 0) {
            jsonObject.addProperty("commitInterval", commitInterval);
        } else {
            jsonObject.remove("commitInterval");
        }

        JsonArray arr = new JsonArray();
        for (JsonObject q : queriesData) {
            JsonObject queryOutput = new JsonObject();
            String queryCode = q.has("queryCode") ? q.get("queryCode").getAsString() : "";
            String type = q.has("type") ? q.get("type").getAsString() : StepConstants.TYPE_SQL;

            queryOutput.addProperty("queryCode", queryCode);

            if (StepConstants.TYPE_MONGO.equals(type)) {
                JsonObject aggregateMongoQuery = new JsonObject();
                if (q.has("collectionName")) {
                    aggregateMongoQuery.addProperty("collectionName", q.get("collectionName").getAsString());
                }
                if (q.has("filters") && q.get("filters").isJsonArray()) {
                    aggregateMongoQuery.add("filter", q.getAsJsonArray("filters"));
                } else {
                    aggregateMongoQuery.add("filter", new JsonArray());
                }
                queryOutput.add("aggregateMongoQuery", aggregateMongoQuery);
            }

            if (q.has("sqlQuery")) {
                queryOutput.addProperty("sqlQuery", q.get("sqlQuery").getAsString());
            }

            String dbSource = q.has("dbSource") ? q.get("dbSource").getAsString() : "";
            queryOutput.addProperty("dbSource", dbSource);
            arr.add(queryOutput);
        }
        reader.remove("type");
        reader.add("queries", arr);
    }

    @Override
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        try {
            if (textListener != null) {
                if (threadsField != null && threadsField.getValue() != null) {
                    threadsField.addChangeListener(changeListener);
                }
                if (commitIntervalField != null && commitIntervalField.getValue() != null) {
                    commitIntervalField.addChangeListener(changeListener);
                }
            }
            if (actionListener != null) {
                this.parentActionListener = actionListener;
            }
            if (detailsForm != null) {
                detailsForm.setListeners(actionListener, textListener);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public JPanel getComponent() {
        return this;
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(StepConstants.FONT_LABEL);
        l.setForeground(SchedulerTheme.TEXT_MAIN);
        return l;
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

    /**
     * Agrega un MouseListener a la tabla para detectar clicks en la columna de acciones y mostrar un menú contextual.
     *
     * @param table    JTable al que agregar el listener
     * @param isFilter boolean que indica si la tabla es de filtros (para usar el índice de columna correcto)
     */
    private void addActionsListener(JTable table, boolean isFilter) {
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                int actionCol = isFilter ? FILTER_ACTION_COLUMN_INDEX : ACTION_COLUMN_INDEX;
                if (r != -1 && c == actionCol) showActionsPopup(table, r, isFilter, e.getPoint());
            }
        });
    }

    /**
     * Muestra un menú contextual con acciones disponibles para cada query o filtro (actualmente solo "Delete").
     * Permite eliminar la query o filtro seleccionado y actualiza la tabla y el formulario en consecuencia.
     *
     * @param table    JTable donde se hizo click
     * @param rowIndex índice de la fila seleccionada
     * @param isFilter boolean que indica si se trata de una tabla de filtros
     * @param p        Point con la posición del click para mostrar el menú
     */
    private void showActionsPopup(JTable table, int rowIndex, boolean isFilter, Point p) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete", AtiIcons.TRASH_ICON);
        deleteItem.addActionListener(e -> {
            if (isFilter) {
                detailsForm.deleteFilter(rowIndex);
                if (parentActionListener != null) parentActionListener.actionPerformed(new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "FilterDeleted"));
            } else {
                deleteQuery(rowIndex);
            }
        });
        popup.add(deleteItem);
        popup.show(table, p.x, p.y);
    }

    /**
     * Renderer personalizado para centrar el texto y aplicar un color gris a la primera columna de la tabla de queries.
     */
    private static class CenterGrayRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setHorizontalAlignment(CENTER); setForeground(StepConstants.TEXT_GRAY); return this;
        }
    }

    /**
     * Renderer personalizado para mostrar un icono de "más acciones" en la columna de acciones de la tabla de queries.
     */
    private static class MoreActionsRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t,v,s,f,r,c);
            setText(""); setHorizontalAlignment(CENTER); setIcon(AllIcons.Actions.More);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return this;
        }
    }
}
