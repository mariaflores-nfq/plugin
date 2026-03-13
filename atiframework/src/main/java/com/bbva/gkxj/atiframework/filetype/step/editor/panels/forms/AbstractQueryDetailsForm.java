package com.bbva.gkxj.atiframework.filetype.step.editor.panels.forms;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import icons.AtiIcons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Clase base abstracta para formularios de detalles de queries.
 */
public abstract class AbstractQueryDetailsForm extends JPanel implements IQueryDetailsForm {


    /** Fuente para subtítulos y textos secundarios */
    protected static final Font FONT_SUBTITLE = new Font("Lato", Font.PLAIN, 13);

    /** Fuente para labels de formularios */
    protected static final Font FONT_LABEL = new Font("Lato", Font.BOLD, 14);

    /** Fuente para headers de tablas */
    protected static final Font FONT_HEADER_TABLE = new Font("Lato", Font.BOLD, 12);

    /** Fuente para títulos de secciones */
    protected static final Font FONT_SECTION_TITLE = new Font("Lato", Font.PLAIN, 18);

    /** Color de fondo del sidebar/formulario */
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

    /** Índice de la columna de acciones en la tabla de parámetros */
    protected static final int ACTION_COLUMN_INDEX = 2;

    /** Identificador del card para estado vacío de parámetros */
    protected static final String CARD_PARAMS_EMPTY = "paramsEmpty";

    /** Identificador del card para estado con parámetros */
    protected static final String CARD_PARAMS_FULL = "paramsFull";

    /** Flag para evitar eventos recursivos durante actualización de UI */
    protected boolean isInternal = false;

    /** Query actualmente seleccionada (referencia externa) */
    protected JsonObject currentSelection = null;

    /** Listener de acciones del panel padre */
    protected ActionListener parentActionListener;

    /** Callback para actualizar la tabla de queries en el panel padre */
    protected final Consumer<String> queryCodeUpdater;

    /** Índice del parámetro seleccionado */
    protected int selectedParamRow = -1;

    /**
     * Constructor base del formulario.
     *
     * @param queryCodeUpdater Callback para actualizar el queryCode en la tabla padre
     */
    protected AbstractQueryDetailsForm(Consumer<String> queryCodeUpdater) {
        this.queryCodeUpdater = queryCodeUpdater;
        setLayout(new GridBagLayout());
        setBackground(StepConstants.BG_SIDEBAR);
        setBorder(new EmptyBorder(15, 20, 15, 20));
    }

    /**
     * Establece la referencia a la query actualmente seleccionada.
     *
     * @param currentSelection JsonObject de la query seleccionada
     */
    public void setCurrentSelection(JsonObject currentSelection) {
        this.currentSelection = currentSelection;
    }

    /**
     * Establece el listener de acciones del panel padre.
     *
     * @param listener ActionListener del panel padre
     */
    public void setParentActionListener(ActionListener listener) {
        this.parentActionListener = listener;
    }

    /**
     * Crea un label con el estilo estándar del framework.
     *
     * @param text Texto del label
     * @return JLabel configurado con fuente y color estándar
     */
    protected JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
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
     * Agrega listener de acciones a la tabla de parámetros.
     *
     * Detecta clics en la columna de acciones y muestra el popup correspondiente.
     *
     * @param table Tabla a la que agregar el listener
     */
    protected void addActionsListener(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r != -1 && c == StepConstants.ACTION_COLUMN_INDEX) {
                    showActionsPopup(table, r, e.getPoint());
                }
            }
        });
    }

    /**
     * Muestra el popup de acciones para un parámetro.
     *
     * @param table Tabla donde mostrar el popup
     * @param rowIndex Índice de la fila seleccionada
     * @param p Punto donde mostrar el popup
     */
    protected void showActionsPopup(JTable table, int rowIndex, Point p) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete", AtiIcons.TRASH_ICON);
        deleteItem.addActionListener(e -> {
            deleteParam(rowIndex);
            notifyParentAction(null);
        });
        popup.add(deleteItem);
        popup.show(table, p.x, p.y);
    }

    /**
     * Notifica al listener padre que hubo un cambio.
     *
     * @param event Evento de acción (puede ser null)
     */
    protected void notifyParentAction(java.awt.event.ActionEvent event) {
        if (parentActionListener != null) {
            parentActionListener.actionPerformed(event);
        }
    }

    /**
     * Obtiene un String de un JsonObject.
     *
     * @param obj JsonObject
     * @param key Clave
     * @return Valor o cadena vacía
     */
    protected String getStringOrEmpty(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    /**
     * Obtiene un String de un JsonObject con valor por defecto.
     *
     * @param obj JsonObject
     * @param key Clave
     * @param defaultValue Valor por defecto
     * @return Valor o valor por defecto
     */
    protected String getStringOrDefault(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : defaultValue;
    }


    /**
     * Renderer para celdas centradas con texto gris.
     */
    public static class CenterGrayRenderer extends DefaultTableCellRenderer {
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
    public static class MoreActionsRenderer extends DefaultTableCellRenderer {
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


    /**
     * Inicializa los componentes específicos del formulario.
     */
    protected abstract void initForm();

    /**
     * Guarda el parámetro actualmente seleccionado.
     */
    protected abstract void saveSelectedParam();
}

