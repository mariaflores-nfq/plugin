package com.bbva.gkxj.atiframework.filetype.step.editor.panels;

import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.step.ParameterData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import icons.AtiIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
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

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.applyBlueFocusBorder;

/**
 * Panel de edición para los parámetros de un Step de tipo ETL.
 * Gestiona una lista de parámetros mediante un sidebar con tabla y un formulario de edición lateral.
 */
public class StepParametersPanel extends JPanel {

    private static final Color BG_SIDEBAR = new Color(248, 249, 250);
    private static final Color BG_TABLE_HEADER = new Color(245, 245, 245);
    private static final Color BG_TABLE_SELECTION = new Color(232, 244, 253);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color TEXT_GRAY = new Color(100, 100, 100);

    private static final Font FONT_TITLE = new Font("Lato", Font.BOLD, 16);
    private static final Font FONT_SUBTITLE = new Font("Lato", Font.PLAIN, 13);
    private static final Font FONT_HEADER_TABLE = new Font("Lato", Font.BOLD, 12);

    private static final String CARD_EMPTY = "empty";
    private static final String CARD_SPLITTER = "splitter";
    private static final int ACTION_COLUMN_INDEX = 2;

    private final Project myProject;

    /** Documento de texto que contiene el JSON del archivo. */
    private final Document myDocument;

    /** Parámetro actualmente seleccionado en la interfaz. */
    private ParameterData currentSelection = null;

    /** Lista de parámetros cargados en memoria. */
    private final List<ParameterData> stepParameterList = new ArrayList<>();

    /** Panel que se muestra cuando no hay ninguna selección en la tabla. */
    private final JPanel emptyPanel;

    /** Tabla que muestra el listado de parámetros. */
    private JTable parameterTable;

    /** Modelo de datos para la tabla de parámetros. */
    private DefaultTableModel tableModel;

    /** Campo de texto para el nombre del parámetro (mapeado a 'name' en JSON). */
    private JTextField paramName;

    /** Campo de texto para el nombre batch (mapeado a 'batchParamName' en JSON). */
    private JTextField batchName;

    /** Etiqueta que muestra el conteo total de parámetros. */
    private JLabel parameterCountLabel;

    /** Divisor principal que separa el sidebar del formulario. */
    private JSplitPane splitPane;

    /** Flag para evitar disparar eventos de actualización durante la carga de datos. */
    private boolean isUpdatingUI = false;

    /** Contenedor con scroll que aloja el formulario de edición. */
    private final JScrollPane formScrollPane;

    // Listeners
    private DocumentListener parentTextListener;
    private ActionListener parentActionListener;

    private JPanel fullEmptyPanel;
    private JBSplitter splitter;
    private CardLayout centerCardLayout;
    private JPanel sidebarPanel;
    private JPanel centerContainer;

    /**
     * Constructor del panel. Inicializa los componentes de la interfaz y configura el layout.
     * @param project Proyecto actual de IntelliJ.
     * @param file Fichero virtual (.json) que se está editando.
     */
    public StepParametersPanel(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        this.emptyPanel = new JPanel();
        this.emptyPanel.setBackground(SchedulerTheme.BG_MAIN);
        this.formScrollPane = new JScrollPane(createForm());
        this.formScrollPane.setBorder(null);
        createStepParameterPage();
    }

    /**
     * Configura el layout principal del panel y monta el JSplitPane.
     */
    private void createStepParameterPage() {
        setLayout(new BorderLayout());
        setBackground(SchedulerTheme.BG_CARD);

        sidebarPanel = createSidebar();
        fullEmptyPanel = createFullEmptyStatePanel();

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
     * Crea el panel del formulario con los campos de edición.
     * * @return JPanel con los componentes del formulario.
     */
    private JPanel createForm() {
        JPanel parameterForm = new JPanel(new GridBagLayout());
        parameterForm.setBackground(SchedulerTheme.BG_MAIN);
        parameterForm.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 5, 0);

        paramName = new JTextField();
        paramName.setPreferredSize(new Dimension(220, 30));
        paramName.setFont(new Font("Lato", Font.PLAIN, 13));
        paramName.setForeground(SchedulerTheme.TEXT_MAIN);
        applyBlueFocusBorder(paramName);

        batchName = new JTextField();
        batchName.setPreferredSize(new Dimension(220, 30));
        applyBlueFocusBorder(batchName);

        gbc.gridy = 0;
        parameterForm.add(createLabel("Param Name"), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        parameterForm.add(paramName, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 5, 0);
        parameterForm.add(createLabel("Batch Param Name"), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        parameterForm.add(batchName, gbc);

        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        parameterForm.add(new JPanel() {{ setOpaque(false); }}, gbc);

        return parameterForm;
    }

    /**
     * Crea el sidebar que contiene el listado de parámetros en una tabla.
     * * @return JPanel configurado como barra lateral.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(new EmptyBorder(0, 20, 10, 0));

        parameterCountLabel = new JLabel("Parameters (0)");
        parameterCountLabel.setFont(FONT_SUBTITLE);
        parameterCountLabel.setForeground(TEXT_GRAY);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_SIDEBAR);
        header.setBorder(new EmptyBorder(10, 5, 10, 10));
        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Parameter List");
        title.setFont(FONT_TITLE);
        title.setForeground(SchedulerTheme.TEXT_MAIN);
        titleBox.add(title, BorderLayout.NORTH);
        titleBox.add(parameterCountLabel, BorderLayout.SOUTH);

        header.add(titleBox, BorderLayout.WEST);

        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnContainer.setOpaque(false);
        JButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> {
            addNewParameter();
            if (parentActionListener != null) parentActionListener.actionPerformed(e);
        });
        btnContainer.add(btn);
        header.add(btnContainer, BorderLayout.EAST);

        sidebar.add(header, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"#", "StepParameterName", "Actions"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        parameterTable = new JBTable(tableModel);
        parameterTable.setRowHeight(40);
        parameterTable.setShowGrid(false);
        parameterTable.setIntercellSpacing(new Dimension(0, 0));
        parameterTable.setSelectionBackground(BG_TABLE_SELECTION);
        parameterTable.setSelectionForeground(Color.BLACK);

        parameterTable.getTableHeader().setBackground(BG_TABLE_HEADER);
        parameterTable.getTableHeader().setFont(FONT_HEADER_TABLE);
        parameterTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
        parameterTable.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));

        parameterTable.getColumnModel().getColumn(0).setMaxWidth(40);
        parameterTable.getColumnModel().getColumn(0).setCellRenderer(new CenterGrayRenderer());
        parameterTable.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setCellRenderer(new MoreActionsRenderer());

        parameterTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });

        addActionsListener(parameterTable);

        JBScrollPane scroll = new JBScrollPane(parameterTable);
        scroll.setBorder(new MatteBorder(1, 1, 1, 1, BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        sidebar.add(scroll, BorderLayout.CENTER);
        return sidebar;
    }

    /**
     * Agrega un MouseListener a la tabla para detectar clicks en la columna de acciones y mostrar un menú contextual.
     * @param table JTable al que se le agregará el listener.
     */
    private void addActionsListener(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r != -1 && c == ACTION_COLUMN_INDEX) {
                    showActionsPopup(table, r, e.getPoint());
                }
            }
        });
    }

    /**
     * Muestra un menú contextual con opciones para el parámetro seleccionado en la fila.
     * @param table JTable donde se hizo click.
     * @param rowIndex Índice de la fila seleccionada.
     * @param p Punto donde se hizo click para posicionar el menú.
     */
    private void showActionsPopup(JTable table, int rowIndex, Point p) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete", AtiIcons.TRASH_ICON);
        deleteItem.addActionListener(e -> deleteParameter(rowIndex));
        popup.add(deleteItem);
        popup.show(table, p.x, p.y);
    }

    /**
     * Agrega un nuevo parámetro a la lista con valores por defecto, actualiza la tabla y selecciona el nuevo elemento.
     */
    private void addNewParameter() {
        boolean wasEmpty = stepParameterList.isEmpty();
        int n = stepParameterList.size() + 1;
        ParameterData newParam = new ParameterData("NewParam", "NewBatchParam");
        stepParameterList.add(newParam);
        tableModel.addRow(new Object[]{String.format("%02d", n), newParam.getParamName(), ""});
        parameterCountLabel.setText("Parameters (" + stepParameterList.size() + ")");

        if (wasEmpty && centerCardLayout != null && centerContainer != null) {
            centerCardLayout.show(centerContainer, CARD_SPLITTER);
        }

        if (parameterTable != null && tableModel != null) {
            int lastRow = tableModel.getRowCount() - 1;
            if (lastRow >= 0) parameterTable.setRowSelectionInterval(lastRow, lastRow);
        }
    }

    /**
     * Elimina un parámetro de la lista y actualiza la tabla y el estado del formulario. Si la lista queda vacía, muestra el panel vacío.
     * @param row Índice de la fila a eliminar.
     */
    private void deleteParameter(int row) {
        if (row < 0 || row >= stepParameterList.size()) return;
        stepParameterList.remove(row);
        tableModel.removeRow(row);

        for (int i = 0; i < stepParameterList.size(); i++) {
            tableModel.setValueAt(String.format("%02d", i + 1), i, 0);
        }
        parameterCountLabel.setText("Parameters (" + stepParameterList.size() + ")");

        if (stepParameterList.isEmpty()) {
            showFormPanel(false);
            currentSelection = null;
            if (centerCardLayout != null && centerContainer != null) {
                centerCardLayout.show(centerContainer, CARD_EMPTY);
            }
        } else{
            int s = Math.max(0, row - 1);
            parameterTable.setRowSelectionInterval(s, s);
        }

        if(parentActionListener != null && parameterTable != null)
            parentActionListener.actionPerformed(new ActionEvent(parameterTable, ActionEvent.ACTION_PERFORMED, "StepParameterDeleted"));
    }

    /**
     * Renderiza el texto de la primera columna centrado y con un color gris para diferenciarlo del nombre del parámetro.
     */
    private static class CenterGrayRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setHorizontalAlignment(CENTER); setForeground(TEXT_GRAY); return this;
        }
    }

    /**
     * Renderiza el icono de "más acciones" en la columna de acciones. Configura el cursor para indicar que es interactivo.
     */
    private class MoreActionsRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t,v,s,f,r,c);
            setText(""); setHorizontalAlignment(CENTER); setIcon(AllIcons.Actions.More);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return this;
        }
    }

    /**
     * Crea el panel que se muestra cuando no hay ningún parámetro en la lista. Incluye un botón para agregar el primer parámetro.
     * @return JPanel con el estado vacío completo.
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
        JLabel title = new JLabel("Parameter List");
        title.setFont(FONT_TITLE);
        title.setForeground(SchedulerTheme.TEXT_MAIN);
        titleBox.add(title, BorderLayout.NORTH);

        JLabel counterLabel = new JLabel("Parameters (0)");
        counterLabel.setFont(FONT_SUBTITLE);
        counterLabel.setForeground(TEXT_GRAY);
        titleBox.add(counterLabel, BorderLayout.SOUTH);

        header.add(titleBox, BorderLayout.WEST);

        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnContainer.setOpaque(false);
        JButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> {
            addNewParameter();
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
     * Maneja la selección de filas en la tabla. Carga los datos del parámetro seleccionado en el formulario o
     * muestra el panel vacío si no hay selección.
     */
    private void handleTableSelection() {
        int selectedRow = parameterTable != null ? parameterTable.getSelectedRow() : -1;
        if (selectedRow != -1 && selectedRow < stepParameterList.size()) {
            currentSelection = stepParameterList.get(selectedRow);
            loadDataIntoForm(currentSelection);
            showFormPanel(true);
        } else {
            currentSelection = null;
            showFormPanel(false);
        }
    }

    /**
     * Actualiza la interfaz del panel a partir de un objeto JSON.
     * * @param jsonObject Objeto raíz que contiene 'stepParameterList'.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("stepParameterList") || !jsonObject.get("stepParameterList").isJsonArray()) {
            stepParameterList.clear();
            if (tableModel != null) tableModel.setRowCount(0);
            parameterCountLabel.setText("Parameters (" + stepParameterList.size() + ")");
            if (centerCardLayout != null && centerContainer != null) {
                centerCardLayout.show(centerContainer, CARD_EMPTY);
            }
            return;
        }

        isUpdatingUI = true;
        try {
            stepParameterList.clear();
            if (tableModel != null) tableModel.setRowCount(0);

            JsonArray arr = jsonObject.getAsJsonArray("stepParameterList");
            for (int i = 0; i < arr.size(); i++) {
                JsonElement el = arr.get(i);
                if (el.isJsonObject()) {
                    JsonObject item = el.getAsJsonObject();
                    ParameterData data = parseParameterData(item);
                    stepParameterList.add(data);
                    if (tableModel != null) {
                        tableModel.addRow(new Object[]{String.format("%02d", i + 1), data.getParamName()});
                    }
                }
            }
            parameterCountLabel.setText("Parameters (" + stepParameterList.size() + ")");
            if (!stepParameterList.isEmpty()) {
                if (centerCardLayout != null && centerContainer != null) {
                    centerCardLayout.show(centerContainer, CARD_SPLITTER);
                }
                if(parameterTable != null && parameterTable.getSelectedRow() == -1) {
                    parameterTable.setRowSelectionInterval(0, 0);
                }
            } else {
                if (centerCardLayout != null && centerContainer != null) {
                    centerCardLayout.show(centerContainer, CARD_EMPTY);
                }
                showFormPanel(false);
            }

        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Sincroniza los cambios de la UI al JSON y los escribe en el documento.
     * * @param jsonObject Objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;

        syncCurrentSelectionFromUI();
        JsonArray parameterListArray = new JsonArray();

        for (ParameterData data : stepParameterList) {
            JsonObject item = new JsonObject();
            item.addProperty("paramName", data.getParamName());
            item.addProperty("batchParamName", data.getBatchName());
            parameterListArray.add(item);
        }

        jsonObject.add("stepParameterList", parameterListArray);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(jsonObject));
        });
    }

    /**
     * Vuelca los valores de los JTextFields al objeto {@link ParameterData} seleccionado.
     */
    private void syncCurrentSelectionFromUI() {
        if (currentSelection == null) return;
        currentSelection.setParamName(paramName.getText());
        currentSelection.setBatchName(batchName.getText());
    }

    /**
     * Carga los datos de un parámetro en los campos del formulario.
     * * @param data Objeto con los datos del parámetro.
     */
    private void loadDataIntoForm(ParameterData data) {
        if (data == null) return;
        isUpdatingUI = true;
        try {
            paramName.setText(data.getParamName());
            batchName.setText(data.getBatchName());
            paramName.setEnabled(true);
            batchName.setEnabled(true);
        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Intercambia el componente derecho del split pane entre el formulario y el panel vacío.
     * * @param show true para mostrar el formulario, false para el panel vacío.
     */
    private void showFormPanel(boolean show) {
        JComponent right = show ? formScrollPane : emptyPanel;
        if (splitter != null && splitter.getSecondComponent() != right) {
            splitter.setSecondComponent(right);
            splitter.revalidate();
            splitter.repaint();
        }
    }

    /**
     * Parsea un JsonObject individual para convertirlo en un objeto ParameterData.
     * * @param item Objeto JSON con las propiedades name y batchParamName.
     * @return Instancia de {@link ParameterData}.
     */
    private ParameterData parseParameterData(JsonObject item) {
        ParameterData data = new ParameterData("", "");
        if (item.has("paramName")) {
            data.setParamName(item.get("paramName").getAsString());
        }
        if (item.has("batchParamName")) {
            data.setBatchName(item.get("batchParamName").getAsString());
        }
        return data;
    }


    /**
     * Crea una etiqueta formateada con el estilo del tema.
     * * @param text Texto de la etiqueta.
     * @return JLabel configurado.
     */
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Lato", Font.BOLD, 14));
        l.setForeground(SchedulerTheme.TEXT_MAIN);
        return l;
    }

    /**
     * Registra un listener de cambios en el documento para los campos de texto.
     *@param textListener Listener que reacciona a cambios de texto.
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener) {
        this.parentTextListener = textListener;
        this.parentActionListener = actionListener;

        if (paramName != null) {
            paramName.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { updateTable(); if(textListener!=null) textListener.insertUpdate(e); }
                @Override
                public void removeUpdate(DocumentEvent e) { updateTable(); if(textListener!=null) textListener.removeUpdate(e); }
                @Override
                public void changedUpdate(DocumentEvent e) { updateTable(); if(textListener!=null) textListener.changedUpdate(e); }

                private void updateTable() {
                    if (isUpdatingUI) return;
                    int selectedRow = parameterTable != null ? parameterTable.getSelectedRow() : -1;
                    if (selectedRow != -1 && tableModel != null) {
                        tableModel.setValueAt(paramName.getText(), selectedRow, 1);
                    }
                }
            });
        }

        if (batchName != null && textListener != null) {
            batchName.getDocument().addDocumentListener(textListener);
        }
    }
}