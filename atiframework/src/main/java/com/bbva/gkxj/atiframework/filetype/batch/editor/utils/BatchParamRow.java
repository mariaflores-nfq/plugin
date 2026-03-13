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
import java.util.List;
import java.util.function.Consumer;

/**
 * Representa una fila visual de configuración para un parámetro de Batch dentro del panel de propiedades.
 *
 * Esta clase gestiona un panel expandible (card) que contiene múltiples campos de entrada
 * (nombre, tipo, IO, descripción, scripts) para definir las propiedades de un parámetro.
 * Implementa una interfaz de usuario dinámica con capacidad de expansión/colapso y estados de edición.
 */
public class BatchParamRow {
    /** Panel principal que contiene tanto la cabecera como el contenido expandible. */
    public JPanel rootPanel;

    /** Panel que contiene los campos de configuración, visible solo cuando la fila está expandida. */
    private JPanel contentPanel;

    /** Etiqueta que muestra el índice y el nombre del parámetro en la cabecera. */
    private JLabel headerTitleLabel;

    /** Botón para expandir o colapsar el panel de contenido. */
    private JButton expandBtn;

    /** Índice ordinal de la fila dentro de la lista global de parámetros. */
    private int currentIndex;

    /** Campo de texto para el nombre lógico del parámetro. */
    public JBTextField paramNameField = new JBTextField();

    /** Campo de texto para el nombre del parámetro tal como se define en el planificador (Scheduler). */
    public JBTextField schedulerNameField = new JBTextField();

    /** Selector para el tipo de dato del parámetro (ej. String, Date). */
    public JComboBox<String> typeCombo = new JComboBox<>(new String[]{"String", "Date"});

    /** Selector para la dirección del flujo de datos (Input, Output, Both). */
    public JComboBox<String> ioTypeCombo = new JComboBox<>(new String[]{"Input", "Output", "Both"});

    /** Interruptor para marcar si el parámetro es de carácter obligatorio. */
    public ToggleSwitch mandatorySwitch = new ToggleSwitch();

    /** Área de texto para una descripción funcional del parámetro. */
    public JTextArea descriptionArea = new JTextArea(2, 20);

    /** Panel de script reutilizable (RSyntaxTextArea + toolbar + resizable). */
    public AtiScriptPanel scriptArea = new AtiScriptPanel(3);

    /** Lista interna de componentes que responden al cambio de estado de edición (habilitado/deshabilitado). */
    private final List<JComponent> rowFields = new ArrayList<>();

    private static final Color TEXT_COLOR_LABEL = new JBColor(new Color(0x6C707E), new Color(0x8C8C8C));
    private static final Color TEXT_COLOR_HEADER_BLUE = new JBColor(new Color(0x0059B3), new Color(0x589DF6));
    private static final Color FIELD_BG = JBColor.WHITE;
    private static final Color BORDER_COLOR = new JBColor(new Color(0xD1D1D1), new Color(0x555555));
    private static final Color SEPARATOR_COLOR = new JBColor(0xE0E0E0, 0x5E6060);

    /**
     * Construye una nueva fila de parámetro de Batch.
     *
     * @param parent   El panel contenedor donde se añadirá esta fila.
     * @param index    El número de orden visual para esta fila (ej. "01").
     * @param onDelete Callback que se ejecutará cuando el usuario pulse el botón de eliminar.
     */
    public BatchParamRow(JPanel parent, int index, Consumer<BatchParamRow> onDelete) {
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
        addStyledField("Scheduler Parameter Name", schedulerNameField);
        addStyledComboBox("Type of parameter", typeCombo);
        addStyledComboBox("Input/Output Type", ioTypeCombo);
        addSwitchField("isMandatory", mandatorySwitch);
        addStyledTextArea("Description", descriptionArea);
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
     * Actualiza el texto de la cabecera con el índice y el nombre actual del parámetro.
     */
    public void updateHeader() {
        String txt = paramNameField.getText().isEmpty() ? "New Parameter" : paramNameField.getText();
        headerTitleLabel.setText(String.format("%02d %s", currentIndex, txt));
    }

    /**
     * Obtiene el panel raíz de esta fila.
     * @return El JPanel contenedor.
     */
    public JPanel getRootPanel() { return rootPanel; }

    /**
     * Establece el índice visual de la fila y actualiza la cabecera.
     * @param i Nuevo índice.
     */
    public void setIndex(int i) { this.currentIndex = i; updateHeader(); }

    /**
     * @return true si el panel de contenido está visible.
     */
    public boolean isExpanded() { return contentPanel.isVisible(); }

    /**
     * Expande o contrae el panel de contenido y actualiza el icono de la flecha.
     * @param b true para expandir, false para contraer.
     */
    public void setExpanded(boolean b) {
        contentPanel.setVisible(b);
        expandBtn.setIcon(b ? AllIcons.General.ArrowDown : AllIcons.General.ArrowRight);
        rootPanel.revalidate();
    }

    /**
     * Actualiza el estado de edición (lectura/escritura) de todos los campos de la fila.
     * Cambia el color de fondo para indicar visualmente el estado.
     *
     * @param editable true para habilitar la edición.
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
     * Registra listeners externos para detectar cambios en los datos de la fila.
     *
     * @param dl Listener para cambios en documentos de texto.
     * @param al Listener para acciones en combos o interruptores.
     */
    public void addListeners(DocumentListener dl, ActionListener al) {
        paramNameField.getDocument().addDocumentListener(dl);
        schedulerNameField.getDocument().addDocumentListener(dl);
        descriptionArea.getDocument().addDocumentListener(dl);
        scriptArea.getDocument().addDocumentListener(dl);
        typeCombo.addActionListener(al);
        ioTypeCombo.addActionListener(al);
        mandatorySwitch.setOnStateChanged(b -> al.actionPerformed(null));
    }

    /**
     * Añade un campo de texto estándar con etiqueta y bordes estilizados.
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
     * Añade un JComboBox con etiqueta y estilo consistente con los campos de texto.
     * @param label Texto de la etiqueta que describe el propósito del combo box.
     * @param combo El JComboBox a añadir, que se estilizará con fondo blanco y bordes personalizados.
     */
    private void addStyledComboBox(String label, JComboBox<?> combo) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_COLOR_LABEL);
        p.add(l, BorderLayout.NORTH);

        combo.setBackground(FIELD_BG);
        rowFields.add(combo);
        p.add(combo, BorderLayout.CENTER);
        contentPanel.add(p);
        contentPanel.add(Box.createVerticalStrut(10));
    }


    /**
     * Añade un JTextArea con etiqueta y un mecanismo de redimensionado manual mediante un icono en la esquina inferior derecha.
     * @param label Texto de la etiqueta que describe el propósito del área de texto.
     * @param area El JTextArea a añadir, que se configurará para permitir salto de línea y tendrá un fondo blanco con bordes personalizados.
     */
    private void addStyledTextArea(String label, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_COLOR_LABEL);
        p.add(l, BorderLayout.NORTH);

        JPanel resizableContainer = new JPanel(new BorderLayout());
        resizableContainer.setOpaque(false);
        resizableContainer.setPreferredSize(new Dimension(100, 80));

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setOpaque(true);
        layeredPane.setBackground(FIELD_BG);

        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(FIELD_BG);
        area.setBorder(new CompoundBorder(
                JBUI.Borders.customLine(BORDER_COLOR),
                JBUI.Borders.empty(5, 8)
        ));

        JLabel resizeHandle = new JLabel(AllIcons.General.ArrowDown);
        resizeHandle.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));

        ResizeHandleListener resizeListener = new ResizeHandleListener(resizableContainer, rootPanel);
        resizeHandle.addMouseListener(resizeListener);
        resizeHandle.addMouseMotionListener(resizeListener);

        layeredPane.add(area, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(resizeHandle, JLayeredPane.PALETTE_LAYER);

        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Rectangle bounds = layeredPane.getBounds();
                area.setBounds(0, 0, bounds.width, bounds.height);
                int iconW = resizeHandle.getPreferredSize().width;
                int iconH = resizeHandle.getPreferredSize().height;
                resizeHandle.setBounds(bounds.width - iconW - 4, bounds.height - iconH - 4, iconW, iconH);
            }
        });

        resizableContainer.add(layeredPane, BorderLayout.CENTER);

        rowFields.add(area);
        p.add(resizableContainer, BorderLayout.CENTER);
        contentPanel.add(p);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    /**
     * Crea un área de script usando AtiScriptPanel.
     * @param label Texto descriptivo superior del área de script.
     * @param panel AtiScriptPanel a configurar y añadir al panel.
     */
    private void addScriptField(String label, AtiScriptPanel panel) {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_COLOR_LABEL);
        l.setBorder(JBUI.Borders.emptyBottom(5));
        p.add(l, BorderLayout.NORTH);

        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(JBUI.Borders.customLine(BORDER_COLOR));
        editorPanel.setBackground(new Color(0xECECEC));
        editorPanel.setPreferredSize(new Dimension(100, 100));

        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowFields.add(panel);
        p.add(panel, BorderLayout.CENTER);

        contentPanel.add(p);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    /**
     * Añade un campo compuesto por un interruptor (ToggleSwitch) y una etiqueta descriptiva, alineados horizontalmente.
     * @param text Texto de la etiqueta que describe la función del interruptor.
     * @param s El ToggleSwitch a añadir, que se colocará a la izquierda de la etiqueta y se estilizará para integrarse visualmente con el diseño general de la fila.
     */
    private void addSwitchField(String text, ToggleSwitch s) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        rowFields.add(s);
        p.add(s);

        JLabel l = new JLabel("  " + text);
        l.setForeground(TEXT_COLOR_LABEL);
        p.add(l);

        contentPanel.add(p);
        contentPanel.add(Box.createVerticalStrut(10));
    }

    /**
     * Clase interna para manejar eventos de ratón y permitir el redimensionado
     * vertical de los componentes JTextArea.
     */
    private static class ResizeHandleListener extends MouseAdapter {
        private final JComponent targetComponent;
        private final JComponent parentToRevalidate;
        private Point startPoint;
        private Dimension startSize;

        public ResizeHandleListener(JComponent targetComponent, JComponent parentToRevalidate) {
            this.targetComponent = targetComponent;
            this.parentToRevalidate = parentToRevalidate;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            startPoint = e.getLocationOnScreen();
            startSize = targetComponent.getSize();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (startPoint == null || startSize == null) return;
            Point currentPoint = e.getLocationOnScreen();
            int deltaY = currentPoint.y - startPoint.y;
            int newHeight = Math.max(60, startSize.height + deltaY);

            Dimension newDim = new Dimension(startSize.width, newHeight);
            targetComponent.setPreferredSize(newDim);
            targetComponent.setSize(newDim);

            parentToRevalidate.revalidate();
            parentToRevalidate.repaint();
        }
    }
}

