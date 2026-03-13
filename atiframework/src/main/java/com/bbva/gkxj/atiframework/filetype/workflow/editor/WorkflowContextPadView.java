package com.bbva.gkxj.atiframework.filetype.workflow.editor;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.intellij.ui.JBColor;
import icons.AtiIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Vista del panel de herramientas contextuales (Context Pad) para el editor de flujos.
 * <p>
 * Este componente proporciona acceso rápido a acciones comunes cuando un nodo está seleccionado,
 * tales como:
 * <ul>
 * <li>Creación de conexiones (Directa, Cola, Descarte).</li>
 * <li>Inserción rápida de nuevos nodos a continuación del actual.</li>
 * <li>Edición de las propiedades del nodo seleccionado.</li>
 * <li>Eliminación del nodo seleccionado.</li>
 * </ul>
 * El panel se adapta dinámicamente según el tipo de nodo seleccionado y la disponibilidad de canales.
 * Posee un fondo semi-transparente y sin bordes para una mejor integración visual con el lienzo del grafo.
 */
public class WorkflowContextPadView extends JPanel {

    /** Escuchador para delegar las acciones del usuario al controlador del grafo. */
    private final PadListener listener;

    /** Referencia al componente del grafo donde se renderiza este panel. */
    private final JComponent graphControl;

    /**
     * Interfaz que define las acciones que el Context Pad puede solicitar al controlador.
     */
    public interface PadListener {
        /**
         * Inicia el modo de creación de una arista de un tipo específico.
         * @param channelType El tipo de canal de la conexión (ej. "DIRECT", "QUEUE").
         */
        void onConnectionModeStarted(String channelType);

        /**
         * Solicita la inserción de un nuevo nodo y su conexión automática.
         * @param type El tipo de nodo a insertar.
         */
        void onQuickNodeInsert(WorkFlowStyles.WfNodeType type);

        /** Solicita la eliminación del nodo actual. */
        void onDeleteRequested();

        /**
         * Verifica si un tipo de conexión es válido para el contexto y nodo actual.
         * @param channelType El tipo de canal a verificar.
         * @return true si el canal está disponible para ser usado.
         */
        boolean isChannelAvailable(String channelType);

        /** Solicita la edición del nodo actual, habitualmente a través de un modal de configuración. */
        void onEditRequested();
    }

    /**
     * Construye una nueva vista de Pad contextual con fondo semi-transparente.
     *
     * @param listener     Implementación de las acciones del pad.
     * @param graphControl Componente padre (lienzo del grafo) para gestionar el foco y eventos.
     */
    public WorkflowContextPadView(PadListener listener, JComponent graphControl) {
        this.listener = listener;
        this.graphControl = graphControl;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Configuramos un fondo
        setBackground(WorkFlowStyles.UI_PANEL_BG);

        // Establecemos únicamente márgenes internos para separar los iconos de los bordes lógicos
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setVisible(true);

        // Evita que los clics en el pad se propaguen al lienzo del grafo inferior
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { e.consume(); }
        });
    }

    /**
     * Sobrescribe el método de pintado de Swing para renderizar el fondo semi-transparente
     * correctamente y sin causar artefactos visuales.
     *
     * @param g El contexto gráfico.
     */
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    /**
     * Reconstruye dinámicamente la interfaz del pad basándose en el tipo de nodo seleccionado.
     *
     * @param type       El tipo de nodo seleccionado (ej. "FILTER", "ROUTER").
     * @param canConnect Indica si el nodo actual permite crear nuevas conexiones de salida.
     */
    public void updateButtonsForType(String type, boolean canConnect) {
        removeAll();
        String nodeType = type.toUpperCase();

        // Grid superior: Acciones de conexión, configuración y borrado
        JPanel topGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        topGrid.setOpaque(false);

        if (canConnect) {
            // Canales estándar
            if (listener.isChannelAvailable("DIRECT")) {
                topGrid.add(createDraggableChannelButton("DIRECT", AtiIcons.GLOBAL_CONNECT_TOOL_ICON, "Direct Connection"));
                topGrid.add(createDraggableChannelButton("QUEUE", AtiIcons.QUEUE_EDGE_ICON, "Queue Connection"));
            }

            // Canal especial de descarte para filtros
            if (nodeType.contains("FILTER") && listener.isChannelAvailable("DISCARD")) {
                topGrid.add(createDraggableChannelButton("DISCARD", AtiIcons.DISCARD_EDGE_ICON, "Discard Connection"));
            }
        }

        // Botón de edición de propiedades
        JLabel wrenchBtn = new JLabel(AtiIcons.getScaledIcon(AtiIcons.WRENCH_CONFIGURATION_ICON, 14));
        wrenchBtn.setToolTipText("Navigate to component");
        wrenchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wrenchBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { listener.onEditRequested(); }
        });
        topGrid.add(wrenchBtn);

        // Botón de eliminación del nodo
        JLabel trashBtn = new JLabel(AtiIcons.getScaledIcon(AtiIcons.REMOVE_ICON, 14));
        trashBtn.setToolTipText("Delete Node");
        trashBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        trashBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { listener.onDeleteRequested(); }
        });
        topGrid.add(trashBtn);

        add(topGrid);

        // Grid inferior: Inserción rápida de nodos (solo si el nodo actual es conectable)
        if (canConnect) {
            add(Box.createRigidArea(new Dimension(0, 5)));
            add(new JSeparator(SwingConstants.HORIZONTAL));
            add(Box.createRigidArea(new Dimension(0, 5)));

            JPanel nodeGrid = new JPanel(new GridLayout(0, 4, 4, 4));
            nodeGrid.setOpaque(false);
            for (WorkFlowStyles.WfNodeType node : getAllowedNodes()) {
                nodeGrid.add(createQuickNodeButton(node));
            }
            add(nodeGrid);
        }

        revalidate();
        repaint();
    }

    /**
     * Crea un botón visual (etiqueta) para iniciar el modo de arrastre y conexión de aristas.
     *
     * @param type El tipo de canal lógico ("DIRECT", "DISCARD", etc.).
     * @param icon El icono a mostrar en el botón.
     * @param tip  El texto del tooltip.
     * @return El componente visual configurado.
     */
    private JComponent createDraggableChannelButton(String type, Icon icon, String tip) {
        JLabel btn = new JLabel(AtiIcons.getScaledIcon(icon, 15));
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setToolTipText(tip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { listener.onConnectionModeStarted(type); }
        });
        return btn;
    }

    /**
     * Crea un botón visual (etiqueta) para la inserción rápida de un tipo de nodo específico.
     *
     * @param type El tipo de nodo del flujo de trabajo a insertar.
     * @return El componente visual configurado.
     */
    private JComponent createQuickNodeButton(WorkFlowStyles.WfNodeType type) {
        Icon originalIcon = type.getIcon();
        Icon iconNitido;
        if (type == WorkFlowStyles.WfNodeType.INPUT || type == WorkFlowStyles.WfNodeType.OUTPUT) {
            iconNitido = AtiIcons.getScaledIcon(originalIcon, 10);
        } else {
            iconNitido = AtiIcons.getScaledIcon(originalIcon, 20);
        }
        JLabel btn = new JLabel(iconNitido);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setToolTipText("Insert " + type.getLabel());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { listener.onQuickNodeInsert(type); }
        });
        return btn;
    }

    /**
     * Define y devuelve la lista de tipos de nodos permitidos para la creación rápida desde este pad.
     *
     * @return Una lista inmutable de tipos de nodo {@link WorkFlowStyles.WfNodeType}.
     */
    private List<WorkFlowStyles.WfNodeType> getAllowedNodes() {
        return Arrays.asList(
                WorkFlowStyles.WfNodeType.FILTER, WorkFlowStyles.WfNodeType.ROUTER,
                WorkFlowStyles.WfNodeType.ENRICHER, WorkFlowStyles.WfNodeType.OUTPUT,
                WorkFlowStyles.WfNodeType.SPLITTER, WorkFlowStyles.WfNodeType.AGGREGATOR,
                WorkFlowStyles.WfNodeType.SUBWORKFLOW
        );
    }
}