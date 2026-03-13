package com.bbva.gkxj.atiframework.filetype.step.editor.panels.queriestasklet;

import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import icons.AtiIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Clase abstracta base para los paneles de queries del Queries Tasklet (SQL y Mongo).
 * Contiene toda la lógica común de gestión de Output Parameters: tabla, detalles,
 * añadir/eliminar parámetros, sincronización con JSON y renderers de tabla.
 * <p>
 * Las subclases solo deben implementar la construcción de su parte específica
 * (SQL Query area o Mongo Query con operaciones) y los métodos de carga/guardado
 * de datos propios de cada tipo.
 */
public abstract class AbstractQueriesTaskletPanel extends JPanel {

    protected static final Color BG_SIDEBAR = StepConstants.BG_SIDEBAR;
    protected static final Color BBVA_BLUE = StepConstants.BBVA_BLUE;
    protected static final Color TEXT_GRAY = StepConstants.TEXT_GRAY;
    protected static final Color BORDER_COLOR = StepConstants.BORDER_COLOR;
    protected static final Color BG_TABLE_HEADER = StepConstants.BG_TABLE_HEADER;
    protected static final Color BG_TABLE_SELECTION = StepConstants.BG_TABLE_SELECTION;
    protected static final Font FONT_LABEL = StepConstants.FONT_LABEL;
    protected static final Font FONT_SUBTITLE = StepConstants.FONT_SUBTITLE;
    protected static final Font FONT_HEADER_TABLE = StepConstants.FONT_HEADER_TABLE;

    private static final int ACTION_COLUMN_INDEX = 2;
    private static final String CARD_PARAMS_EMPTY = "paramsEmpty";
    private static final String CARD_PARAMS_FULL = "paramsFull";

    private JTable paramsTable;
    private DefaultTableModel paramsModel;
    private JLabel paramsCountLabel;
    private JPanel paramsCardContainer;
    private CardLayout paramsCardLayout;
    private AtiTextField fieldNameField;
    private AtiTextField paramNameField;
    private AtiComboBox paramTypeCombo;

    private int selectedParamRow = -1;
    protected boolean isInternal = false;
    protected JsonObject currentSelection;
    protected ActionListener parentActionListener;

    /**
     * Constructor base. Configura el layout y fondo del panel.
     */
    protected AbstractQueriesTaskletPanel() {
        setLayout(new GridBagLayout());
        setBackground(BG_SIDEBAR);
    }

    /**
     * Establece el objeto JSON actual que representa la configuración de la query.
     * @param selection El objeto JsonObject que contiene los datos de la query actualmente editada.
     */
    public void setCurrentSelection(JsonObject selection) {
        this.currentSelection = selection;
    }

    /**
     * Establece el ActionListener del panel padre para notificar cambios.
     * @param listener El ActionListener que se llamará cuando haya cambios que requieran actualización en el panel padre.
     */
    public void setParentActionListener(ActionListener listener) {
        this.parentActionListener = listener;
    }

    /**
     * Carga los datos de la query en los campos del panel.
     * @param q El objeto JsonObject con los datos a cargar.
     */
    public abstract void loadData(JsonObject q);

    /**
     * Guarda los datos del panel en el objeto query.
     * @param q El objeto JsonObject donde guardar los datos.
     */
    public abstract void saveData(JsonObject q);

    /**
     * Registra listeners para sincronización automática con el JSON.
     * @param al ActionListener para componentes de tipo combo/botón.
     * @param dl DocumentListener para campos de texto.
     */
    public abstract void setupListeners(ActionListener al, DocumentListener dl);

    /**
     * Crea la sección completa de Output Parameters (título + cards vacía/llena).
     * Las subclases deben añadir este panel a su layout en la posición deseada.
     * @return JPanel con la sección de Output Parameters completa.
     */
    protected JPanel createOutputParametersSection() {
        JPanel paramsSection = new JPanel(new BorderLayout(0, 5));
        paramsSection.setBackground(BG_SIDEBAR);

        JLabel outputTitle = new JLabel("Output Parameters");
        outputTitle.setFont(outputTitle.getFont().deriveFont(Font.BOLD, 16f));
        outputTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        paramsSection.add(outputTitle, BorderLayout.NORTH);

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
        paramsSection.add(paramsCardContainer, BorderLayout.CENTER);

        return paramsSection;
    }

    /**
     * Carga los parámetros de salida del JSON y los muestra en la tabla.
     * Selecciona el primer parámetro si existe.
     * @param q El objeto JsonObject que contiene los enricherOutputFields.
     */
    protected void loadOutputParameters(JsonObject q) {
        JsonArray p = q.has("enricherOutputFields") ? q.getAsJsonArray("enricherOutputFields") : new JsonArray();
        reloadParamsTable(p);
        if (!p.isEmpty()) {
            paramsTable.setRowSelectionInterval(0, 0);
        } else {
            selectedParamRow = -1;
            loadSelectedParam();
        }
    }

    /**
     * Registra los listeners de sincronización para los campos de parámetros.
     * @param al ActionListener para el combo de tipo de parámetro.
     * @param dl DocumentListener base para propagar los cambios.
     */
    protected void setupParamListeners(ActionListener al, DocumentListener dl) {
        DocumentListener paramSyncDl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { saveSelectedParam(); dl.insertUpdate(e); }
            @Override
            public void removeUpdate(DocumentEvent e) { saveSelectedParam(); dl.removeUpdate(e); }
            @Override
            public void changedUpdate(DocumentEvent e) { saveSelectedParam(); dl.changedUpdate(e); }
        };

        if (fieldNameField != null) fieldNameField.getDocument().addDocumentListener(paramSyncDl);
        if (paramNameField != null) paramNameField.getDocument().addDocumentListener(paramSyncDl);
        if (paramTypeCombo != null) paramTypeCombo.addActionListener(e -> {
            saveSelectedParam();
            al.actionPerformed(e);
        });
    }

    /**
     * Notifica al listener padre que hubo un cambio que requiere actualización.
     */
    protected void notifyParent() {
        if (parentActionListener != null) {
            parentActionListener.actionPerformed(null);
        }
    }

    /**
     * Crea un panel con un JLabel en la parte superior y un componente de entrada debajo.
     * @param labelText      El texto de la etiqueta.
     * @param inputComponent El componente de entrada que se colocará debajo del JLabel.
     * @return JPanel con la etiqueta y el componente organizados verticalmente.
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
     * Crea un JLabel con estilo de fuente en negrita.
     * @param text El texto a mostrar.
     * @return JLabel configurado con fuente en negrita y color de texto principal.
     */
    protected JLabel createBoldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(SchedulerTheme.TEXT_MAIN);
        return l;
    }

    /**
     * Obtiene un String de un JsonObject, devolviendo cadena vacía si la clave no existe o es nula.
     * @param obj JsonObject del que obtener el valor.
     * @param key Clave del valor a obtener.
     * @return Valor asociado a la clave o cadena vacía.
     */
    protected String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    /**
     * Obtiene un String de un JsonObject, devolviendo un valor por defecto si la clave no existe o es nula.
     * @param obj JsonObject del que obtener el valor.
     * @param key Clave del valor a obtener.
     * @param def Valor por defecto.
     * @return Valor asociado a la clave o el valor por defecto.
     */
    protected String getStrDefault(JsonObject obj, String key, String def) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : def;
    }

    /**
     * Elimina el parámetro en la fila indicada del JSON y recarga la tabla.
     * @param row Índice de la fila del parámetro a eliminar.
     */
    public void deleteParam(int row) {
        if (currentSelection == null || !currentSelection.has("enricherOutputFields")) return;
        JsonArray params = currentSelection.getAsJsonArray("enricherOutputFields");
        if (row >= 0 && row < params.size()) {
            params.remove(row);
            reloadParamsTable(params);
            if (!params.isEmpty()) {
                paramsTable.setRowSelectionInterval(Math.max(0, row - 1), Math.max(0, row - 1));
            } else {
                selectedParamRow = -1;
                loadSelectedParam();
            }
        }
    }

    /**
     * Guarda los cambios del parámetro seleccionado de vuelta al JSON y actualiza la tabla.
     */
    public void saveSelectedParam() {
        if (isInternal || selectedParamRow == -1 || currentSelection == null
                || !currentSelection.has("enricherOutputFields")) return;

        JsonArray params = currentSelection.getAsJsonArray("enricherOutputFields");
        if (selectedParamRow < params.size()) {
            JsonObject p = params.get(selectedParamRow).getAsJsonObject();
            String newName = fieldNameField.getText();
            p.addProperty("fieldName", newName);
            p.addProperty("paramName", paramNameField.getText());
            p.addProperty("paramType", (String) paramTypeCombo.getSelectedItem());
            paramsModel.setValueAt(newName, selectedParamRow, 1);
        }
    }

    /**
     * Crea el panel que se muestra cuando no hay parámetros definidos.
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
        title.setFont(FONT_SUBTITLE.deriveFont(Font.BOLD));
        title.setForeground(TEXT_GRAY);

        AtiCircularIconButton btn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        btn.addActionListener(e -> addNewParam());

        header.add(title, BorderLayout.WEST);
        header.add(btn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    /**
     * Crea el panel que contiene la tabla de parámetros con cabecera y scroll.
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

        paramsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = paramsTable.rowAtPoint(e.getPoint());
                int c = paramsTable.columnAtPoint(e.getPoint());
                if (r != -1 && c == ACTION_COLUMN_INDEX) {
                    showActionsPopup(r, e.getPoint());
                }
            }
        });

        JScrollPane sp = new JBScrollPane(paramsTable);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(0, 120));

        panel.add(header, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de detalles para el parámetro seleccionado (Field Name, Param Name, Param Type).
     */
    private JPanel createParamDetailsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_SIDEBAR);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 10);
        c.gridx = 0;
        c.weightx = 0.5;
        p.add(createBoldLabel("Field Name"), c);

        c.gridx = 1;
        c.insets = new Insets(0, 10, 5, 0);
        p.add(createBoldLabel("Param Name"), c);

        c.gridy = 1;
        c.insets = new Insets(0, 0, 10, 10);
        c.gridx = 0;
        fieldNameField = new AtiTextField();
        fieldNameField.setPreferredSize(new Dimension(0, 30));
        p.add(fieldNameField, c);

        c.gridx = 1;
        c.insets = new Insets(0, 10, 10, 0);
        paramNameField = new AtiTextField();
        paramNameField.setPreferredSize(new Dimension(0, 30));
        p.add(paramNameField, c);

        c.gridy = 2;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 5, 10);
        p.add(createBoldLabel("Param Type"), c);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 10);
        paramTypeCombo = new AtiComboBox(new String[]{"String", "Integer", "Long", "Double", "Date", "Boolean"});
        paramTypeCombo.setPreferredSize(new Dimension(0, 30));
        p.add(paramTypeCombo, c);

        return p;
    }

    /**
     * Agrega un nuevo parámetro con valores por defecto al JSON y recarga la tabla.
     */
    private void addNewParam() {
        if (currentSelection == null) return;

        JsonArray params = currentSelection.has("enricherOutputFields")
                ? currentSelection.getAsJsonArray("enricherOutputFields") : new JsonArray();
        if (!currentSelection.has("enricherOutputFields")) {
            currentSelection.add("enricherOutputFields", params);
        }

        JsonObject newParam = new JsonObject();
        newParam.addProperty("fieldName", "New Param");
        newParam.addProperty("paramName", "");
        newParam.addProperty("paramType", "String");
        params.add(newParam);

        reloadParamsTable(params);
        if (paramsModel.getRowCount() > 0) {
            paramsTable.setRowSelectionInterval(paramsModel.getRowCount() - 1, paramsModel.getRowCount() - 1);
        }
        notifyParent();
    }

    /**
     * Recarga la tabla de parámetros a partir del array JSON.
     * @param params JsonArray con los parámetros a mostrar.
     */
    private void reloadParamsTable(JsonArray params) {
        if (paramsModel == null) return;
        paramsModel.setRowCount(0);
        if (paramsCountLabel != null) {
            paramsCountLabel.setText("Parameters (" + params.size() + ")");
        }
        for (int i = 0; i < params.size(); i++) {
            JsonObject param = params.get(i).getAsJsonObject();
            String name = getStr(param, "fieldName");
            paramsModel.addRow(new Object[]{String.format("%02d", i + 1), name, ""});
        }
        paramsCardLayout.show(paramsCardContainer, params.isEmpty() ? CARD_PARAMS_EMPTY : CARD_PARAMS_FULL);
    }

    /**
     * Carga los datos del parámetro seleccionado en los campos de detalle.
     */
    private void loadSelectedParam() {
        isInternal = true;
        try {
            if (selectedParamRow != -1 && currentSelection != null && currentSelection.has("enricherOutputFields")) {
                JsonArray params = currentSelection.getAsJsonArray("enricherOutputFields");
                if (selectedParamRow < params.size()) {
                    JsonObject p = params.get(selectedParamRow).getAsJsonObject();
                    fieldNameField.setText(getStr(p, "fieldName"));
                    paramNameField.setText(getStr(p, "paramName"));
                    paramTypeCombo.setSelectedItem(getStrDefault(p, "paramType", "String"));
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
     * Limpia los campos de detalle del parámetro.
     */
    private void clearParamFields() {
        if (fieldNameField != null) fieldNameField.setText("");
        if (paramNameField != null) paramNameField.setText("");
        if (paramTypeCombo != null) paramTypeCombo.setSelectedIndex(-1);
    }

    /**
     * Habilita o deshabilita los campos de detalle del parámetro.
     * @param enabled true para habilitar, false para deshabilitar.
     */
    private void setParamFieldsEnabled(boolean enabled) {
        if (fieldNameField != null) fieldNameField.setEnabled(enabled);
        if (paramNameField != null) paramNameField.setEnabled(enabled);
        if (paramTypeCombo != null) paramTypeCombo.setEnabled(enabled);
    }

    /**
     * Muestra un menú contextual con la acción de eliminar para el parámetro indicado.
     * @param rowIndex Índice de la fila.
     * @param p        Punto donde mostrar el menú.
     */
    private void showActionsPopup(int rowIndex, Point p) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete", AtiIcons.TRASH_ICON);
        deleteItem.addActionListener(e -> {
            deleteParam(rowIndex);
            notifyParent();
        });
        popup.add(deleteItem);
        popup.show(paramsTable, p.x, p.y);
    }

    /**
     * Renderer para celdas centradas con texto gris (columna de índice).
     */
    protected static class CenterGrayRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setHorizontalAlignment(CENTER);
            setForeground(StepConstants.TEXT_GRAY);
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

