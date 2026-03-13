package com.bbva.gkxj.atiframework.components;

import com.intellij.ui.JBColor;
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
import java.util.function.Consumer; // NUEVO
import java.util.function.Supplier;

/**
 * Tabla simplificada para listas anidadas (como Input Parameters y Scripts).
 * Soporta eventos de selección para sincronizarse con formularios de detalle.
 */
public class AtiSimpleTablePanel<T> extends JPanel {

    private final List<T> dataList = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JBTable table;
    private final JLabel countLabel;

    private final String titlePrefix;
    private final Supplier<T> itemFactory;
    private final ItemNameExtractor<T> nameExtractor;
    private Runnable onChange;

    // NUEVO: Listener para cuando el usuario hace clic en una fila
    private Consumer<T> onItemSelected;
    private boolean isRebuilding = false; // Bandera para no disparar eventos mientras dibujamos la tabla

    @FunctionalInterface
    public interface ItemNameExtractor<T> {
        String getName(T item);
    }

    public AtiSimpleTablePanel(String title, String columnName, Supplier<T> itemFactory, ItemNameExtractor<T> nameExtractor) {
        this.titlePrefix = title;
        this.itemFactory = itemFactory;
        this.nameExtractor = nameExtractor;

        setLayout(new BorderLayout(0, 5));
        setOpaque(false);

        // --- 1. CABECERA (Título + Botón Añadir) ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);

        countLabel = new JLabel(title + " (0)");
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        AtiCircularIconButton addBtn = new AtiCircularIconButton(AtiIcons.ADD_ICON);
        addBtn.setToolTipText("Add new element");
        addBtn.addActionListener(e -> addItem());

        header.add(countLabel);
        header.add(addBtn);
        add(header, BorderLayout.NORTH);

        // --- 2. TABLA ---
        tableModel = new DefaultTableModel(new String[]{"#", columnName, "Actions"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JBTable(tableModel);
        setupTableAppearance();

        JBScrollPane scroll = new JBScrollPane(table);
        scroll.setBorder(JBUI.Borders.customLine(JBColor.border(), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    private void setupTableAppearance() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(30);

        TableColumn colIndex = table.getColumnModel().getColumn(0);
        colIndex.setMaxWidth(40);
        colIndex.setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                setForeground(new Color(0, 102, 204));
                setHorizontalAlignment(CENTER);
                return comp;
            }
        });

        TableColumn colActions = table.getColumnModel().getColumn(2);
        colActions.setMaxWidth(60);
        colActions.setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                setIcon(AtiIcons.TRASH_ICON);
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 2) {
                    deleteItem(row);
                }
            }
        });

        // NUEVO: Escuchar selección de filas para avisar a la vista principal
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isRebuilding) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < dataList.size()) {
                    if (onItemSelected != null) onItemSelected.accept(dataList.get(row));
                } else {
                    if (onItemSelected != null) onItemSelected.accept(null);
                }
            }
        });
    }

    // NUEVO: Añade y autoselecciona
    public void addItem() {
        T item = itemFactory.get();
        dataList.add(item);
        refreshUI();
        int newRowIndex = dataList.size() - 1;
        table.setRowSelectionInterval(newRowIndex, newRowIndex);
        notifyChange();
    }

    private void deleteItem(int index) {
        dataList.remove(index);
        refreshUI();
        notifyChange();
    }

    public void reloadData(List<T> items) {
        dataList.clear();
        if (items != null) dataList.addAll(items);
        refreshUI();
    }

    // ACTUALIZADO: Mantiene la selección o selecciona el primero por defecto
    private void refreshUI() {
        isRebuilding = true;
        int selectedRow = table.getSelectedRow();

        tableModel.setRowCount(0);
        for (int i = 0; i < dataList.size(); i++) {
            T item = dataList.get(i);
            tableModel.addRow(new Object[]{
                    String.format("%02d", i + 1),
                    nameExtractor.getName(item),
                    ""
            });
        }
        countLabel.setText(titlePrefix + " (" + dataList.size() + ")");
        isRebuilding = false;

        // Recuperar selección si es posible
        if (selectedRow >= 0 && selectedRow < dataList.size()) {
            table.setRowSelectionInterval(selectedRow, selectedRow);
        } else if (!dataList.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        } else {
            if (onItemSelected != null) onItemSelected.accept(null);
        }
    }

    // NUEVO: Para uso desde fuera
    public void setSelectionListener(Consumer<T> listener) { this.onItemSelected = listener; }

    // NUEVO: Obtener qué objeto está seleccionado
    public T getCurrentSelection() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < dataList.size()) return dataList.get(row);
        return null;
    }

    // NUEVO: Actualizar el texto en la tabla en tiempo real si el usuario lo cambia en el formulario
    public void refreshSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < dataList.size()) {
            T item = dataList.get(row);
            tableModel.setValueAt(nameExtractor.getName(item), row, 1);
        }
    }

    public List<T> getDataList() { return new ArrayList<>(dataList); }
    public void setOnChange(Runnable onChange) { this.onChange = onChange; }
    private void notifyChange() { if (onChange != null) onChange.run(); }
}