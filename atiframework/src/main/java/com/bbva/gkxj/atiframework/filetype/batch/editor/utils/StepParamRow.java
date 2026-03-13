package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.bbva.gkxj.atiframework.components.AtiScriptPanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
/**
 * Representa una fila visual de configuración para los parámetros de un "Step" (paso ETL)
 * dentro del panel de propiedades del editor Batch.
 * Esta clase gestiona un componente expandible que permite al usuario definir la relación
 * entre los parámetros de un nodo específico y las variables del batch, valores fijos o scripts.
 */
public class StepParamRow {

    /** Panel raíz que organiza la cabecera y el panel de contenido mediante un BoxLayout vertical. */
    public JPanel rootPanel;

    /** Panel que contiene los campos de entrada de datos; su visibilidad se alterna al expandir/colapsar. */
    private JPanel contentPanel;

    /** Etiqueta de la cabecera que muestra el índice y el nombre dinámico del parámetro. */
    private JLabel headerTitleLabel;

    /** Botón que muestra el estado de expansión actual (flecha derecha o abajo). */
    private JButton expandBtn;

    /** Posición ordinal de esta fila en la lista de parámetros del paso seleccionado. */
    private int currentIndex;

    /** Campo de texto para ingresar el nombre identificativo del parámetro en el Step. */
    public JBTextField paramNameField = new JBTextField();

    /** Campo de texto para referenciar el nombre de un parámetro de Batch existente. */
    public JBTextField batchParamNameField = new JBTextField();

    /** Campo de texto para asignar un valor estático constante al parámetro. */
    public JBTextField fixedValueField = new JBTextField();

    /** Panel de script reutilizable (RSyntaxTextArea + toolbar + resizable). */
    public AtiScriptPanel scriptArea = new AtiScriptPanel(3);

    /** Colección de componentes que deben alternar su estado de edición según el modo del editor. */
    private final List<JComponent> rowFields = new ArrayList<>();

    private static final Color TEXT_COLOR_LABEL = new JBColor(new Color(0x6C707E), new Color(0x8C8C8C));
    private static final Color TEXT_COLOR_HEADER_BLUE = new JBColor(new Color(0x0059B3), new Color(0x589DF6));
    private static final Color FIELD_BG = JBColor.WHITE;
    private static final Color BORDER_COLOR = new JBColor(new Color(0xD1D1D1), new Color(0x555555));
    private static final Color SEPARATOR_COLOR = new JBColor(0xE0E0E0, 0x5E6060);


    /**
     * Construye una nueva fila de configuración para parámetros de paso ETL
     *
     * @param parent   Panel contenedor donde se alojará esta fila.
     * @param index    Índice numérico para identificar la posición de la fila.
     * @param onDelete Callback invocado cuando el usuario solicita la eliminación de este parámetro.
     */
    public StepParamRow(JPanel parent, int index, Consumer<StepParamRow> onDelete) {
        this.currentIndex = index;

        rootPanel = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        rootPanel.setOpaque(false);
        rootPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.setBorder(new MatteBorder(0, 0, 1, 0, SEPARATOR_COLOR));

        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);
        h.setBorder(JBUI.Borders.empty(5, 0));
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        h.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftContainer.setOpaque(false);

        expandBtn = new JButton(AllIcons.General.ArrowRight);
        expandBtn.setBorder(null);
        expandBtn.setContentAreaFilled(false);
        expandBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        headerTitleLabel = new JLabel();
        headerTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headerTitleLabel.setForeground(TEXT_COLOR_HEADER_BLUE);
        headerTitleLabel.setBorder(JBUI.Borders.emptyLeft(5));

        leftContainer.add(expandBtn);
        leftContainer.add(headerTitleLabel);

        h.add(leftContainer, BorderLayout.CENTER);

        JButton deleteBtn = new JButton(AllIcons.General.Remove);
        deleteBtn.setBorder(JBUI.Borders.empty(0, 5));
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.setToolTipText("Eliminar parámetro");
        deleteBtn.addActionListener(e -> onDelete.accept(this));

        h.add(deleteBtn, BorderLayout.EAST);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(JBUI.Borders.empty(5, 15, 15, 5));
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.setVisible(false);

        addStyledField("Parameter Name", paramNameField);
        addStyledField("Batch Parameter Name", batchParamNameField);
        addStyledField("Fixed Value", fixedValueField);

        addScriptField("Script Value", scriptArea);

        updateHeader();

        paramNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateHeader(); }
            public void removeUpdate(DocumentEvent e) { updateHeader(); }
            public void changedUpdate(DocumentEvent e) { updateHeader(); }
        });

        MouseAdapter toggle = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                setExpanded(!contentPanel.isVisible());
            }
        };
        h.addMouseListener(toggle);
        expandBtn.addActionListener(e -> toggle.mouseClicked(null));

        rootPanel.add(h);
        rootPanel.add(contentPanel);
        parent.add(rootPanel);
    }

    /**
     * Sincroniza el texto visible de la cabecera con el valor actual del nombre del parámetro.
     */
    public void updateHeader() {
        String txt = paramNameField.getText().isEmpty() ? "New Step Param" : paramNameField.getText();
        headerTitleLabel.setText(String.format("%02d %s", currentIndex, txt));
    }

    /**
     * Actualiza el número de índice de la fila y refresca la interfaz.
     * @param i El nuevo índice de la fila.
     */
    public void setIndex(int i) { this.currentIndex = i; updateHeader(); }

    /**
     * Determina si el panel con los detalles del parámetro es visible actualmente.
     * @return {@code true} si está expandido.
     */
    public boolean isExpanded() { return contentPanel.isVisible(); }

    /**
     * Define el estado de expansión de la fila.
     * @param b true para mostrar los detalles, false para ocultarlos.
     */
    public void setExpanded(boolean b) {
        contentPanel.setVisible(b);
        expandBtn.setIcon(b ? AllIcons.General.ArrowDown : AllIcons.General.ArrowRight);
        rootPanel.revalidate();
    }

    /**
     * Modifica la capacidad de edición de todos los componentes de entrada de la fila.
     * Ajusta visualmente el fondo y el color del texto para indicar el estado de bloqueo.
     * @param editable  true para permitir cambios, false para modo lectura.
     */
    public void updateEditableState(boolean editable) {
        for (JComponent c : rowFields) {
            if (c instanceof JTextComponent tc) {
                tc.setEditable(editable);
                tc.setBackground(editable ? FIELD_BG : new Color(0xF9F9F9));
                tc.setForeground(editable ? JBColor.BLACK : JBColor.GRAY);
            } else { c.setEnabled(editable); }
        }
    }

    /**
     * Asocia manejadores de eventos externos a los campos de la fila para sincronización de datos.
     *
     * @param dl Manejador para detectar cambios en el contenido de texto.
     * @param al Manejador para detectar acciones específicas (no utilizado en campos de texto puro).
     */
    public void addListeners(DocumentListener dl, ActionListener al) {
        paramNameField.getDocument().addDocumentListener(dl);
        batchParamNameField.getDocument().addDocumentListener(dl);
        fixedValueField.getDocument().addDocumentListener(dl);
        scriptArea.getDocument().addDocumentListener(dl);
    }

    /**
     * Genera y añade un campo de texto con etiqueta aplicando el esquema visual corporativo.
     * @param label Texto descriptivo superior.
     * @param field Componente de entrada a estilizar.
     */
    private void addStyledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_COLOR_LABEL);
        p.add(l, BorderLayout.NORTH);

        field.setBackground(FIELD_BG);
        field.setBorder(new CompoundBorder(JBUI.Borders.customLine(BORDER_COLOR), JBUI.Borders.empty(2, 6)));

        rowFields.add(field);
        p.add(field, BorderLayout.CENTER);
        contentPanel.add(p);
        contentPanel.add(Box.createVerticalStrut(10));
    }


    /**
     * Crea un área de script usando AtiScriptPanel.
     * @param label Texto descriptivo superior del área de script.
     * @param panel AtiScriptPanel a configurar y añadir al panel.
     */
    private void addScriptField(String label, AtiScriptPanel panel) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_COLOR_LABEL);
        l.setBorder(JBUI.Borders.emptyBottom(5));
        p.add(l, BorderLayout.NORTH);

        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rowFields.add(panel);
        p.add(panel, BorderLayout.CENTER);
        contentPanel.add(p);
        contentPanel.add(Box.createVerticalStrut(10));
    }

}