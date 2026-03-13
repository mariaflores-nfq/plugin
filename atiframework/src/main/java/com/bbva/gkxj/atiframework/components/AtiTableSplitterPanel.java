package com.bbva.gkxj.atiframework.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import icons.AtiIcons;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Componente reutilizable que proporciona un panel con tabla y formulario.
 * Permite mostrar una lista de elementos y un panel de edición a la derecha.
 *
 * @param <T> Tipo de dato que representa cada fila de la tabla.
 */
public class AtiTableSplitterPanel<T> extends JPanel {

    /**
     * Lista interna de elementos de tipo T.
     */
    private final List<T> dataList = new ArrayList<>();

    /**
     * Instancia de JBTable utilizada en el panel.
     */
    private JBTable table;

    /**
     * Modelo de la tabla.
     */
    private DefaultTableModel tableModel;

    /**
     * Panel derecho con CardLayout.
     */
    private JPanel rightPane;

    /**
     * CardLayout del panel derecho.
     */
    private CardLayout rightPaneCardLayout;

    /**
     * Etiqueta de contador de elementos.
     */
    private JLabel countLabel;

    /**
     * CardLayout de la tabla.
     */
    private CardLayout tableCardLayout;

    /**
     * Panel de la tabla con CardLayout.
     */
    private JPanel tableCardPanel;

    /**
     * Elemento actualmente seleccionado.
     */
    private T currentSelection = null;

    /**
     * Callback para cambios en los datos.
     */
    private Runnable changeCallback;

    /**
     * Prefijo para la etiqueta de contador.
     */
    private String countLabelPrefix;

    /**
     * Nombre de la segunda columna.
     */
    private final String secondColumnName;

    /**
     * Proveedor de nuevos elementos.
     */
    private final Supplier<T> itemFactory;

    /**
     * Extractor del ID del elemento.
     */
    private final ItemIdExtractor<T> idExtractor;

    /**
     * Extractor del nombre del elemento.
     */
    private final ItemNameExtractor<T> nameExtractor;

    /**
     * Listener de selección de fila.
     */
    private SelectionListener<T> selectionListener;

    /**
     * Listener de deselección.
     */
    private DeselectionListener deselectionListener;

    /**
     * Constante para mostrar solo la cabecera.
     */
    private static final String CARD_HEADER = "HEADER";

    /**
     * Constante para mostrar la tabla.
     */
    private static final String CARD_TABLE = "TABLE";

    /**
     * Constante para mostrar el panel vacío.
     */
    private static final String CARD_EMPTY = "EMPTY";

    /**
     * Constante para mostrar el formulario.
     */
    private static final String CARD_FORM = "FORM";

    /**
     * Interfaz funcional para extraer el ID (ej. "01") de un elemento.
     */
    @FunctionalInterface
    public interface ItemIdExtractor<T> {
        /**
         * Extrae el ID de un elemento.
         * @param item Elemento del que extraer el ID.
         * @return Cadena que representa el ID del elemento.
         */
        String getId(T item);
    }

    /**
     * Interfaz funcional para extraer el nombre visible en la tabla de un elemento.
     */
    @FunctionalInterface
    public interface ItemNameExtractor<T> {
        /**
         * Extrae el nombre de un elemento.
         * @param item Elemento del que extraer el nombre.
         * @return Cadena que representa el nombre del elemento.
         */
        String getName(T item);
    }

    /**
     * Listener invocado cuando se selecciona una fila.
     */
    @FunctionalInterface
    public interface SelectionListener<T> {
        /**
         * Acción al seleccionar un elemento.
         * @param item Elemento seleccionado.
         */
        void onSelected(T item);
    }

    /**
     * Listener invocado cuando se deselecciona (no hay fila seleccionada).
     */
    @FunctionalInterface
    public interface DeselectionListener {
        /**
         * Acción al deseleccionar.
         */
        void onDeselected();
    }

    /**
     * Construye el panel de tabla con splitter.
     *
     * @param countLabelPrefix Prefijo para el label del contador (ej. "Fixed Fields", "Queries").
     * @param secondColumnName Nombre de la segunda columna de la tabla (ej. "Fields", "Query Code").
     * @param itemFactory      Proveedor de nuevas instancias de T al pulsar el botón "+".
     * @param idExtractor      Extractor del ID de un elemento para la columna #.
     * @param nameExtractor    Extractor del nombre de un elemento para la segunda columna.
     * @param formPanel        Panel que se muestra a la derecha al seleccionar una fila.
     */
    public AtiTableSplitterPanel(
            String countLabelPrefix,
            String secondColumnName,
            Supplier<T> itemFactory,
            ItemIdExtractor<T> idExtractor,
            ItemNameExtractor<T> nameExtractor,
            JPanel formPanel
    ) {
        this.countLabelPrefix = countLabelPrefix;
        this.secondColumnName = secondColumnName;
        this.itemFactory = itemFactory;
        this.idExtractor = idExtractor;
        this.nameExtractor = nameExtractor;

        initUI(formPanel);
        setupTableSelectionListener();
    }

    /**
     * Establece el callback que se invocará cuando el usuario modifique datos (añadir/eliminar).
     * @param callback Runnable a ejecutar en el cambio de datos.
     */
    public void setChangeCallback(Runnable callback) {
        this.changeCallback = callback;
    }

    /**
     * Establece el listener de selección de fila.
     * @param listener Listener a invocar al seleccionar una fila.
     */
    public void setSelectionListener(SelectionListener<T> listener) {
        this.selectionListener = listener;
    }

    /**
     * Establece el listener de deselección.
     * @param listener Listener a invocar cuando no hay fila seleccionada.
     */
    public void setDeselectionListener(DeselectionListener listener) {
        this.deselectionListener = listener;
    }

    /**
     * Devuelve la lista interna de datos.
     * @return Lista de elementos de tipo T.
     */
    public List<T> getDataList() {
        return dataList;
    }

    /**
     * Devuelve el elemento actualmente seleccionado, o null si no hay selección.
     * @return Elemento seleccionado de tipo T, o null.
     */
    public T getCurrentSelection() {
        return currentSelection;
    }

    /**
     * Devuelve el índice de la fila seleccionada en la tabla (-1 si no hay selección).
     * @return Índice de la fila seleccionada, o -1.
     */
    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    /**
     * Devuelve la tabla JBTable interna.
     * @return Instancia de JBTable utilizada en el panel.
     */
    public JBTable getTable() {
        return table;
    }

    /**
     * Devuelve el modelo de la tabla.
     * @return Instancia de DefaultTableModel utilizada en el panel.
     */
    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    /**
     * Actualiza el valor de la segunda columna (nombre) de la fila seleccionada.
     * @param name Nuevo nombre a mostrar.
     */
    public void updateSelectedRowName(String name) {
        int row = table.getSelectedRow();
        if (row != -1) {
            tableModel.setValueAt(name, row, 1);
        }
    }

    /**
     * Muestra el panel del formulario en el lado derecho del splitter.
     */
    public void showFormPanel() {
        rightPaneCardLayout.show(rightPane, CARD_FORM);
    }

    /**
     * Muestra el panel vacío en el lado derecho del splitter.
     */
    public void showEmptyPanel() {
        rightPaneCardLayout.show(rightPane, CARD_EMPTY);
    }

    /**
     * Añade un elemento nuevo a la tabla usando el itemFactory.
     */
    public void addItem() {
        T item = itemFactory.get();
        dataList.add(item);
        tableModel.addRow(new Object[]{idExtractor.getId(item), nameExtractor.getName(item), ""});
        updateCountLabel();
        updateTableCard();
        notifyChange();
    }

    /**
     * Elimina un elemento de la tabla por su índice.
     * @param rowIndex Índice de la fila a eliminar.
     */
    public void deleteItem(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= dataList.size()) return;

        dataList.remove(rowIndex);
        tableModel.removeRow(rowIndex);

        for (int i = 0; i < dataList.size(); i++) {
            tableModel.setValueAt(idExtractor.getId(dataList.get(i)), i, 0);
        }

        updateCountLabel();
        updateTableCard();

        currentSelection = null;
        table.clearSelection();
        showEmptyPanel();
        notifyChange();
    }

    /**
     * Cambia el titulo de la etiqueta contador.
     * @param newTitle nuevo nombre a mostrar.
     */
    public void setTitle(String newTitle) {
        this.countLabelPrefix = newTitle;
        updateCountLabel(); // Actualiza el texto de arriba ("JSON Fields (X)")
    }
    /**
     * Recarga la tabla con una nueva lista de datos.
     * Preserva la selección si es posible.
     * @param items Lista de elementos a mostrar.
     */
    public void reloadData(List<T> items) {
        int selectedRow = table.getSelectedRow();
        dataList.clear();
        tableModel.setRowCount(0);

        for (T item : items) {
            dataList.add(item);
            tableModel.addRow(new Object[]{idExtractor.getId(item), nameExtractor.getName(item), ""});
        }

        updateCountLabel();
        updateTableCard();

        if (selectedRow != -1 && selectedRow < dataList.size()) {
            // setRowSelectionInterval fires the ListSelectionListener which calls selectionListener
            table.setRowSelectionInterval(selectedRow, selectedRow);
        } else {
            currentSelection = null;
            showEmptyPanel();
        }
    }

    /**
     * Selecciona una fila de la tabla programáticamente.
     * @param row Índice de la fila a seleccionar.
     */
    public void selectRow(int row) {
        if (row >= 0 && row < dataList.size()) {
            table.setRowSelectionInterval(row, row);
        }
    }

    /**
     * Limpia la selección actual.
     */
    public void clearSelection() {
        currentSelection = null;
        table.clearSelection();
        showEmptyPanel();
    }

    private void initUI(JPanel formPanel) {
        this.setLayout(new BorderLayout(0, 10));

        // Header con contador + botón añadir
        JPanel headerPanel = new JPanel(new BorderLayout());
        AtiCircularIconButton newBtn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        newBtn.addActionListener(e -> addItem());

        JPanel titlePanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 2, true, false));
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        countLabel = new JLabel("");
        updateCountLabel();
        titleRow.add(countLabel);
        titleRow.add(newBtn);
        titlePanel.add(titleRow);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        this.add(headerPanel, BorderLayout.NORTH);

        // Splitter
        JBSplitter splitter = new JBSplitter(false, 0.35f);
        splitter.setDividerWidth(1);
        splitter.getDivider().setBackground(JBColor.border());

        // Tabla
        initTable();

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        JTable headerTable = new JBTable(new DefaultTableModel(new String[]{"#", secondColumnName, "Actions"}, 0));
        headerTable.setTableHeader(table.getTableHeader());
        headerTable.setPreferredScrollableViewportSize(
                new Dimension(table.getPreferredSize().width, table.getTableHeader().getPreferredSize().height));
        tableHeaderPanel.add(headerTable.getTableHeader(), BorderLayout.NORTH);
        tableHeaderPanel.setPreferredSize(
                new Dimension(table.getPreferredSize().width, table.getTableHeader().getPreferredSize().height));

        JBScrollPane tableScroll = new JBScrollPane(table);
        tableScroll.setBorder(JBUI.Borders.customLine(JBColor.border(), 1, 1, 1, 0));

        tableCardLayout = new CardLayout();
        tableCardPanel = new JPanel(tableCardLayout);
        tableCardPanel.add(tableHeaderPanel, CARD_HEADER);
        tableCardPanel.add(tableScroll, CARD_TABLE);
        splitter.setFirstComponent(tableCardPanel);

        // Panel derecho con CardLayout (vacío / formulario)
        rightPaneCardLayout = new CardLayout();
        rightPane = new JPanel(rightPaneCardLayout);
        rightPane.setBorder(JBUI.Borders.emptyLeft(15));
        rightPane.setBackground(new JBColor(new Color(245, 245, 245), new Color(60, 63, 65)));

        JPanel emptyPanel = new JPanel();
        rightPane.add(emptyPanel, CARD_EMPTY);
        rightPane.add(formPanel, CARD_FORM);

        splitter.setSecondComponent(rightPane);
        this.add(splitter, BorderLayout.CENTER);
        updateTableCard();
    }

    private void initTable() {
        tableModel = new DefaultTableModel(new String[]{"#", secondColumnName, "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JBTable(tableModel) ;
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);

        // Columna #
        TableColumn firstColumn = table.getColumnModel().getColumn(0);
        firstColumn.setMinWidth(30);
        firstColumn.setMaxWidth(30);
        firstColumn.setPreferredWidth(30);

        // Columna Actions
        TableColumn actionCol = table.getColumnModel().getColumn(2);
        actionCol.setMaxWidth(80);
        actionCol.setMinWidth(80);
        actionCol.setPreferredWidth(80);

        actionCol.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setIcon(AllIcons.Actions.More);
                setText("");
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleRowClick(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleRowClick(e);
            }

            private void handleRowClick(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;

                if (e.isPopupTrigger()) {
                    table.setRowSelectionInterval(row, row);
                    showActionsPopup(e.getPoint(), row);
                } else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    int col = table.columnAtPoint(e.getPoint());
                    if (col == 2) {
                        table.setRowSelectionInterval(row, row);
                        showActionsPopup(e.getPoint(), row);
                    }
                }
            }
        });
    }

    /**
     * Configura el listener de selección de la tabla para actualizar la selección actual y mostrar el formulario.
     */
    private void setupTableSelectionListener() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1 && row < dataList.size()) {
                    currentSelection = dataList.get(row);
                    if (selectionListener != null) selectionListener.onSelected(currentSelection);
                    showFormPanel();
                } else {
                    currentSelection = null;
                    if (deselectionListener != null) deselectionListener.onDeselected();
                }
            }
        });
    }

    /**
     * Muestra un popup de acciones (actualmente solo "Delete") al hacer click derecho o en el icono de acciones.
     * @param p Punto donde mostrar el popup.
     * @param rowIndex Índice de la fila para la que se muestran las acciones.
     */
    private void showActionsPopup(Point p, int rowIndex) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete", AtiIcons.TRASH_ICON);
        deleteItem.addActionListener(e -> deleteItem(rowIndex));
        popup.add(deleteItem);
        popup.show(table, p.x, p.y);
    }

    /**
     * Actualiza la vista de la tabla para mostrar solo la cabecera si no hay datos, o la tabla completa si hay datos.
     */
    private void updateTableCard() {
        if (tableModel.getRowCount() == 0) {
            tableCardLayout.show(tableCardPanel, CARD_HEADER);
        } else {
            tableCardLayout.show(tableCardPanel, CARD_TABLE);
        }
    }

    /**
     * Actualiza el texto del label de contador con el número actual de elementos.
     */
    private void updateCountLabel() {
        countLabel.setText(countLabelPrefix + " (" + dataList.size() + ")");
    }

    /**
     * Notifica al callback de cambio que los datos han sido modificados.
     */
    private void notifyChange() {
        if (changeCallback != null) {
            changeCallback.run();
        }
    }
}

