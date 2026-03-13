package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.writer;

import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
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

/**
 * Clase base abstracta que proporciona la funcionalidad común para los paneles
 * de escritura de queries (SQL y Mongo).
 */
public abstract class AbstractStepWriterQueryPanel extends JPanel {

    /** Fuente para títulos principales */
    protected static final Font FONT_TITLE = new Font("Lato", Font.BOLD, 16);

    /** Fuente para subtítulos y textos secundarios */
    protected static final Font FONT_SUBTITLE = new Font("Lato", Font.PLAIN, 13);

    /** Fuente para labels de formularios */
    protected static final Font FONT_LABEL = new Font("Lato", Font.BOLD, 14);

    /** Fuente para headers de tablas */
    protected static final Font FONT_HEADER_TABLE = new Font("Lato", Font.BOLD, 12);

    /** Fuente para títulos de secciones */
    protected static final Font FONT_SECTION_TITLE = new Font("Lato", Font.PLAIN, 18);

    /** Color de fondo del sidebar */
    protected static final Color BG_SIDEBAR = new Color(248, 249, 250);

    /** Color de fondo del header de tabla */
    protected static final Color BG_TABLE_HEADER = new Color(245, 245, 245);

    /** Color de fondo de fila seleccionada */
    protected static final Color BG_TABLE_SELECTION = new Color(232, 244, 253);

    /** Color de texto gris secundario */
    protected static final Color TEXT_GRAY = new Color(100, 100, 100);

    /** Color de bordes */
    protected static final Color BORDER_COLOR = new Color(200, 200, 200);

    /** Color principal BBVA para botones */
    protected static final Color BBVA_BLUE = new Color(0, 68, 129);

    /** Índice de la columna de acciones en la tabla */
    protected static final int ACTION_COLUMN_INDEX = 2;

    /** Identificador del card vacío */
    protected static final String CARD_EMPTY = "empty";

    /** Identificador del card con splitter */
    protected static final String CARD_SPLITTER = "splitter";

    /** Proyecto actual de IntelliJ */
    protected final Project project;

    /** Archivo virtual siendo editado */
    protected final VirtualFile file;

    /** Tabla que muestra la lista de queries */
    protected JTable queriesTable;

    /** Modelo de datos de la tabla de queries */
    protected DefaultTableModel tableModel;

    /** Label que muestra el contador de queries */
    protected JLabel queriesCounterLabel;

    /** Splitter que divide sidebar y formulario */
    protected JBSplitter splitter;

    /** Panel vacío para el estado sin selección */
    protected JPanel emptyPanel;

    /** ScrollPane para el formulario de detalles */
    protected JScrollPane formScrollPane;

    /** Contenedor central con CardLayout */
    protected JPanel centerContainer;

    /** CardLayout del contenedor central */
    protected CardLayout centerCardLayout;

    /** Lista de datos de queries en formato interno */
    protected List<JsonObject> queriesData = new ArrayList<>();

    /** Query actualmente seleccionada */
    protected JsonObject currentSelection = null;

    /** Flag para evitar eventos recursivos durante actualización de UI */
    protected boolean isUpdatingUI = false;

    /** Listener de texto del panel padre */
    protected DocumentListener parentTextListener;

    /** Listener de acciones del panel padre */
    protected ActionListener parentActionListener;

    /**
     * Construye el panel base con el proyecto y archivo dados.
     *
     * @param project Proyecto de IntelliJ
     * @param file    Archivo virtual siendo editado
     */
    protected AbstractStepWriterQueryPanel(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initComponents();
    }

    /**
     * Inicializa todos los componentes del panel.
     */
    private void initComponents() {
        add(createGlobalConfigPanel(), BorderLayout.NORTH);

        JPanel sidebarPanel = createSidebar();

        emptyPanel = createEmptyStatePanel();
        JPanel fullEmptyPanel = createFullEmptyStatePanel();

        formScrollPane = new JScrollPane(createDetailsForm());
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
        centerContainer.add(fullEmptyPanel, CARD_EMPTY);
        centerContainer.add(splitter, CARD_SPLITTER);

        centerCardLayout.show(centerContainer, CARD_EMPTY);

        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * Crea el panel de configuración global (header).
     * Las subclases pueden sobrescribir este método para personalizar o eliminar el header.
     */
    protected JPanel createGlobalConfigPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(JBUI.Borders.empty(15, 20, 10, 20));

        JLabel title = new JLabel("Writer Configuration");
        title.setFont(FONT_SECTION_TITLE);
        title.setForeground(SchedulerTheme.TEXT_MAIN);
        p.add(title, BorderLayout.WEST);

        return p;
    }

    /**
     * Crea el sidebar con la lista de queries.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new EmptyBorder(0, 20, 10, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_SIDEBAR);
        header.setBorder(new EmptyBorder(10, 5, 10, 10));

        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Queries");
        title.setFont(FONT_TITLE);
        title.setForeground(SchedulerTheme.TEXT_MAIN);
        titleBox.add(title, BorderLayout.NORTH);

        queriesCounterLabel = new JLabel("Queries (0)");
        queriesCounterLabel.setFont(FONT_SUBTITLE);
        queriesCounterLabel.setForeground(TEXT_GRAY);
        titleBox.add(queriesCounterLabel, BorderLayout.SOUTH);

        header.add(titleBox, BorderLayout.WEST);

        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnContainer.setOpaque(false);
        AtiCircularIconButton addBtn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        addBtn.addActionListener(e -> {
            addNewQuery();
            if (parentActionListener != null) parentActionListener.actionPerformed(e);
        });
        btnContainer.add(addBtn);
        header.add(btnContainer, BorderLayout.EAST);

        sidebar.add(header, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"#", "Query Code", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        queriesTable = new JBTable(tableModel);
        queriesTable.setRowHeight(40);
        queriesTable.setShowGrid(false);
        queriesTable.setIntercellSpacing(new Dimension(0, 0));
        queriesTable.setSelectionBackground(BG_TABLE_SELECTION);
        queriesTable.setSelectionForeground(Color.BLACK);

        queriesTable.getTableHeader().setBackground(BG_TABLE_HEADER);
        queriesTable.getTableHeader().setFont(FONT_HEADER_TABLE);
        queriesTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
        queriesTable.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        queriesTable.getColumnModel().getColumn(0).setMaxWidth(40);
        queriesTable.getColumnModel().getColumn(0).setCellRenderer(new CenterGrayRenderer());
        queriesTable.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setCellRenderer(new MoreActionsRenderer());

        queriesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isUpdatingUI) {
                if (currentSelection != null && isFormVisible()) {
                    saveDataFromForm(currentSelection);
                }

                int row = queriesTable.getSelectedRow();
                if (row != -1 && row < queriesData.size()) {
                    currentSelection = queriesData.get(row);
                    showFormPanel(true);
                    isUpdatingUI = true;
                    try {
                        loadDataToForm(currentSelection);
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
        scroll.setBorder(new MatteBorder(1, 1, 1, 1, BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        sidebar.add(scroll, BorderLayout.CENTER);

        return sidebar;
    }

    /**
     * Muestra u oculta el panel del formulario.
     */
    protected void showFormPanel(boolean show) {
        JComponent right = show ? formScrollPane : emptyPanel;
        if (splitter.getSecondComponent() != right) {
            splitter.setSecondComponent(right);
            splitter.revalidate();
            splitter.repaint();
        }
    }

    /**
     * Crea el panel de estado vacío para cuando no hay selección.
     */
    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setBackground(Color.WHITE);
        contentBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLabel = new JLabel(AllIcons.General.Information);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentBox.add(iconLabel);

        contentBox.add(Box.createVerticalStrut(15));

        JLabel titleLabel = new JLabel("No queries available");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(SchedulerTheme.TEXT_MAIN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentBox.add(titleLabel);

        contentBox.add(Box.createVerticalStrut(8));

        JLabel descLabel = new JLabel("Click the + button to add a new query");
        descLabel.setFont(FONT_SUBTITLE);
        descLabel.setForeground(TEXT_GRAY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentBox.add(descLabel);

        panel.add(contentBox);
        return panel;
    }

    /**
     * Crea el panel de estado vacío completo (cuando no hay queries en la lista).
     */
    private JPanel createFullEmptyStatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SIDEBAR);
        panel.setBorder(new EmptyBorder(0, 20, 10, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_SIDEBAR);
        header.setBorder(new EmptyBorder(10, 5, 10, 10));

        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Queries");
        title.setFont(FONT_TITLE);
        title.setForeground(SchedulerTheme.TEXT_MAIN);
        titleBox.add(title, BorderLayout.NORTH);

        JLabel counterLabel = new JLabel("Queries (0)");
        counterLabel.setFont(FONT_SUBTITLE);
        counterLabel.setForeground(TEXT_GRAY);
        titleBox.add(counterLabel, BorderLayout.SOUTH);

        header.add(titleBox, BorderLayout.WEST);

        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnContainer.setOpaque(false);
        AtiCircularIconButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> {
            addNewQuery();
            if (parentActionListener != null) parentActionListener.actionPerformed(e);
        });
        btnContainer.add(btn);
        header.add(btnContainer, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(BG_SIDEBAR);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Agrega una nueva query a la lista.
     */
    protected void addNewQuery() {
        if (currentSelection != null && isFormVisible()) {
            saveDataFromForm(currentSelection);
        }

        boolean wasEmpty = queriesData.isEmpty();

        int n = queriesData.size() + 1;
        JsonObject newQ = createNewQueryObject();

        queriesData.add(newQ);
        String queryCode = newQ.has("queryCode") ? newQ.get("queryCode").getAsString() : "NewQuery";
        tableModel.addRow(new Object[]{String.format("%02d", n), queryCode, ""});
        queriesCounterLabel.setText("Queries (" + queriesData.size() + ")");

        if (wasEmpty) {
            centerCardLayout.show(centerContainer, CARD_SPLITTER);
        }
        currentSelection = newQ;
        int lastRow = tableModel.getRowCount() - 1;
        isUpdatingUI = true;
        try {
            queriesTable.setRowSelectionInterval(lastRow, lastRow);
            showFormPanel(true);
            loadDataToForm(newQ);
        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Elimina una query de la lista.
     *
     * @param row Índice de la fila a eliminar
     */
    protected void deleteQuery(int row) {
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
            centerCardLayout.show(centerContainer, CARD_EMPTY);
        } else {
            int s = Math.max(0, row - 1);
            currentSelection = queriesData.get(s);
            isUpdatingUI = true;
            try {
                queriesTable.setRowSelectionInterval(s, s);
                showFormPanel(true);
                loadDataToForm(currentSelection);
            } finally {
                isUpdatingUI = false;
            }
        }

        if (parentActionListener != null) {
            parentActionListener.actionPerformed(
                new ActionEvent(queriesTable, ActionEvent.ACTION_PERFORMED, "QueryDeleted")
            );
        }
    }

    /**
     * Agrega listener de acciones a una tabla.
     */
    protected void addActionsListener(JTable table, boolean isParam) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r != -1 && c == ACTION_COLUMN_INDEX) {
                    showActionsPopup(table, r, isParam, e.getPoint());
                }
            }
        });
    }

    /**
     * Muestra el popup de acciones.
     */
    protected void showActionsPopup(JTable table, int rowIndex, boolean isParam, Point p) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete", AtiIcons.TRASH_ICON);
        deleteItem.addActionListener(e -> {
            if (isParam) {
                deleteParam(rowIndex);
                if (parentActionListener != null) {
                    parentActionListener.actionPerformed(
                        new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "ParamDeleted")
                    );
                }
            } else {
                deleteQuery(rowIndex);
            }
        });
        popup.add(deleteItem);
        popup.show(table, p.x, p.y);
    }

    /**
     * Crea un label con el estilo estándar.
     */
    protected JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(SchedulerTheme.TEXT_MAIN);
        return l;
    }

    /**
     * Actualiza el formulario con los datos del JSON.
     * Trabaja directamente con el formato JSON del archivo (sin conversión intermedia).
     *
     * @param jsonObject JSON completo del archivo
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("writer")) return;

        isUpdatingUI = true;
        try {
            JsonObject writer = jsonObject.getAsJsonObject("writer");

            queriesData.clear();
            tableModel.setRowCount(0);
            currentSelection = null;

            JsonArray queryArray = getQueryArrayFromWriter(writer);
            if (queryArray != null) {
                for (int i = 0; i < queryArray.size(); i++) {
                    JsonObject q = queryArray.get(i).getAsJsonObject();
                    queriesData.add(q.deepCopy());
                    String code = q.has("queryCode") ? q.get("queryCode").getAsString() : "";
                    tableModel.addRow(new Object[]{String.format("%02d", i + 1), code, ""});
                }
            }
            queriesCounterLabel.setText("Queries (" + queriesData.size() + ")");

            if (!queriesData.isEmpty()) {
                centerCardLayout.show(centerContainer, CARD_SPLITTER);
                currentSelection = queriesData.get(0);
                queriesTable.setRowSelectionInterval(0, 0);
                showFormPanel(true);
                loadDataToForm(currentSelection);
            } else {
                currentSelection = null;
                showFormPanel(false);
                centerCardLayout.show(centerContainer, CARD_EMPTY);
            }
        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Actualiza el documento JSON con los datos del formulario.
     * Trabaja directamente con el formato JSON del archivo (sin conversión intermedia).
     *
     * @param jsonObject JSON completo del archivo
     */
    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;

        JsonObject writer;
        if (jsonObject.has("writer")) {
            writer = jsonObject.getAsJsonObject("writer");
        } else {
            writer = new JsonObject();
            jsonObject.add("writer", writer);
        }

        if (currentSelection != null && isFormVisible()) {
            saveDataFromForm(currentSelection);
        }

        JsonArray queryList = new JsonArray();
        for (JsonObject q : queriesData) {
            JsonObject copy = q.deepCopy();
            queryList.add(copy);
        }
        addQueryArrayToWriter(writer, queryList);
    }

    /**
     * Registra los listeners del panel padre.
     */
    public void addFieldListeners(DocumentListener dl, ActionListener al, javax.swing.event.ChangeListener cl) {
        this.parentTextListener = dl;
        this.parentActionListener = al;
        setupFormListeners(al, dl);
    }

    /**
     * Crea el panel de formulario de detalles específico.
     *
     * @return Panel de detalles
     */
    protected abstract JPanel createDetailsForm();

    /**
     * Crea un nuevo objeto query con valores por defecto.
     *
     * @return JsonObject con la estructura de una nueva query
     */
    protected abstract JsonObject createNewQueryObject();

    /**
     * Obtiene el array de queries desde el writer según el tipo.
     *
     * @param writer Objeto writer del JSON
     * @return JsonArray con las queries
     */
    protected abstract JsonArray getQueryArrayFromWriter(JsonObject writer);

    /**
     * Agrega el array de queries al writer y limpia el tipo contrario.
     *
     * @param writer    Objeto writer del JSON
     * @param queryList Array de queries a agregar
     */
    protected abstract void addQueryArrayToWriter(JsonObject writer, JsonArray queryList);

    /**
     * Carga los datos en el formulario de detalles.
     * Los datos vienen directamente en formato JSON del archivo.
     *
     * @param queryData Datos de la query a cargar (formato JSON)
     */
    protected abstract void loadDataToForm(JsonObject queryData);

    /**
     * Guarda los datos del formulario en el objeto query.
     *
     * @param queryData Objeto query donde guardar los datos
     */
    protected abstract void saveDataFromForm(JsonObject queryData);

    /**
     * Configura los listeners del formulario.
     */
    protected abstract void setupFormListeners(ActionListener al, DocumentListener dl);

    /**
     * Verifica si el formulario de detalles está visible.
     * @return {@code true} si el formulario de detalles está visible, {@code false} en caso contrario
     */
    protected abstract boolean isFormVisible();

    /**
     * Elimina un parámetro del formulario.
     *
     * @param rowIndex Índice del parámetro a eliminar
     */
    protected abstract void deleteParam(int rowIndex);

    /**
     * Renderer para celdas centradas con texto gris.
     */
    protected static class CenterGrayRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setHorizontalAlignment(CENTER);
            setForeground(TEXT_GRAY);
            return this;
        }
    }

    /**
     * Renderer para la columna de acciones con icono "More".
     */
    protected static class MoreActionsRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setText("");
            setHorizontalAlignment(CENTER);
            setIcon(AllIcons.Actions.More);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }
    }
}

