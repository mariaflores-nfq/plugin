package com.bbva.gkxj.atiframework.filetype.workflow.utils;

import com.bbva.gkxj.atiframework.filetype.workflow.controller.GraphController;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.intellij.ui.JBColor;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxCellState;
import icons.AtiIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Gestor del "Context Pad" para el editor de flujos de trabajo.
 * <p>
 * Esta clase se encarga de mostrar un menú flotante al lado del nodo seleccionado,
 * permitiendo realizar acciones rápidas como:
 * <ul>
 * <li>Crear y conectar nuevos nodos de diferentes tipos.</li>
 * <li>Iniciar el modo de conexión manual hacia nodos existentes.</li>
 * <li>Eliminar el nodo seleccionado.</li>
 * <li>Seleccionar el tipo de canal (DIRECT, QUEUE, DISCARD) para las conexiones.</li>
 * </ul>
 * Maneja una interfaz de dos pasos: selección de canal y selección de nodo destino.
 */
public class WorkflowContextPadHandler {

    /** Tamaño estándar para los botones de la interfaz flotante. */
    private static final int BUTTON_SIZE = 32;

    /** Caché para almacenar iconos escalados y evitar redundancia en el procesado. */
    private static final Map<Icon, Icon> scaledCache = new HashMap<>();

    /** Componente principal del grafo donde se renderiza el pad. */
    private final mxGraphComponent graphComponent;

    /** Controlador principal del grafo para delegar acciones de alto nivel. */
    private final GraphController graphController;

    /** Panel de Swing que contiene los elementos visuales del Context Pad. */
    private final JPanel contextPad;

    /** Indica si el sistema está esperando que el usuario seleccione un nodo destino para una conexión. */
    private boolean isConnectingMode = false;

    /** Almacena el tipo de canal seleccionado (DIRECT, QUEUE, DISCARD) durante el flujo de creación. */
    private String selectedChannelType = null;

    /** Referencia a la celda (nodo) que ha originado la apertura del pad. */
    private mxCell currentCell = null;

    /**
     * Constructor del gestor del Context Pad.
     * Inicializa el panel visual y configura los listeners básicos de selección y repintado.
     *
     * @param graphComponent El componente visual de mxGraph.
     * @param controller     El controlador principal del editor.
     */
    public WorkflowContextPadHandler(mxGraphComponent graphComponent, GraphController controller) {
        this.graphComponent = graphComponent;
        this.graphController = controller;

        this.contextPad = new JPanel();
        this.contextPad.setLayout(new BoxLayout(contextPad, BoxLayout.Y_AXIS));
        this.contextPad.setBackground(JBColor.PanelBackground);
        this.contextPad.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        this.contextPad.setVisible(false);

        // Bloqueador para evitar que los clics en el pad se propaguen al lienzo del grafo
        MouseAdapter blocker = new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { e.consume(); } };
        this.contextPad.addMouseListener(blocker);

        this.graphComponent.getGraphControl().add(contextPad, 0);

        initListeners();
    }

    /**
     * Inicializa los listeners encargados de sincronizar el pad con las acciones del usuario,
     * como la selección de nodos, el scroll y el dibujo de conexiones manuales.
     */
    private void initListeners() {
        // Reposicionar el pad cuando el grafo se repinta (zoom, scroll, etc.)
        graphComponent.getGraph().getView().addListener(mxEvent.REPAINT, (sender, evt) -> repositionContextPad());

        // Actualizar el contenido del pad cuando cambia la selección
        graphComponent.getGraph().getSelectionModel().addListener(mxEvent.CHANGE, (sender, evt) -> {
            selectedChannelType = null;
            updateContextPad();
        });

        // Ocultar al usar la rueda del ratón
        graphComponent.getGraphControl().addMouseWheelListener(e -> hideContextPad());

        // Gestionar el final del modo de conexión manual (suelta del ratón)
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isConnectingMode) {
                    Object targetCell = graphComponent.getCellAt(e.getX(), e.getY());

                    if (targetCell instanceof mxCell && ((mxCell) targetCell).isVertex() && targetCell != currentCell) {
                        createEdgeToExisting((mxCell) targetCell, selectedChannelType);
                    }

                    isConnectingMode = false;
                    selectedChannelType = null;
                    graphComponent.getGraphControl().setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    /**
     * Algoritmo de búsqueda de espacio libre para evitar colisiones entre nodos.
     * <p>
     * Busca iterativamente hacia abajo en el eje Y una posición donde el nuevo nodo
     * no se superponga con los existentes, manteniendo un margen de seguridad.
     *
     * @param graph  Instancia del grafo.
     * @param startX Coordenada X ideal inicial.
     * @param startY Coordenada Y ideal inicial.
     * @param width  Ancho del nuevo nodo.
     * @param height Alto del nuevo nodo.
     * @return Punto con las coordenadas (X, Y) seguras calculadas.
     */
    private com.mxgraph.util.mxPoint findFreeSpace(com.mxgraph.view.mxGraph graph, double startX, double startY, double width, double height) {
        double currentX = startX;
        double currentY = startY;
        boolean collision = true;

        Object parent = graph.getDefaultParent();
        Object[] cells = graph.getChildVertices(parent);

        int maxAttempts = 50;
        int attempts = 0;

        while (collision && attempts < maxAttempts) {
            collision = false;
            com.mxgraph.util.mxRectangle proposedBounds = new com.mxgraph.util.mxRectangle(currentX, currentY, width, height);

            for (Object cellObj : cells) {
                com.mxgraph.model.mxCell cell = (com.mxgraph.model.mxCell) cellObj;
                if (cell.isVertex()) {
                    com.mxgraph.model.mxGeometry geo = graph.getModel().getGeometry(cell);
                    if (geo != null) {
                        // Margen de seguridad de 20 píxeles
                        com.mxgraph.util.mxRectangle existingBounds = new com.mxgraph.util.mxRectangle(
                                geo.getX() - 20, geo.getY() - 20, geo.getWidth() + 40, geo.getHeight() + 40
                        );
                        if (proposedBounds.getRectangle().intersects(existingBounds.getRectangle())) {
                            collision = true;
                            currentY += height + 30; // Desplazar hacia abajo
                            break;
                        }
                    }
                }
            }
            attempts++;
        }
        return new com.mxgraph.util.mxPoint(currentX, currentY);
    }

    /**
     * Determina si debe mostrarse el pad y actualiza su visibilidad basándose en la selección actual.
     */
    private void updateContextPad() {
        com.mxgraph.view.mxGraph currentGraph = graphComponent.getGraph();
        if (currentGraph.getSelectionCount() != 1) {
            hideContextPad();
            return;
        }
        Object cell = currentGraph.getSelectionCell();
        if (cell instanceof mxCell && ((mxCell) cell).isVertex()) {
            currentCell = (mxCell) cell;
            rebuildUI();
            contextPad.setVisible(true);
            repositionContextPad();
        } else {
            hideContextPad();
        }
    }

    /**
     * Reconstruye dinámicamente la interfaz de usuario del pad.
     * Limpia los componentes anteriores y renderiza el "Paso 1" o "Paso 2" según el estado.
     */
    private void rebuildUI() {
        Rectangle oldBounds = contextPad.getBounds();
        contextPad.removeAll();

        if (selectedChannelType == null) {
            renderStep1();
        } else {
            renderStep2();
        }

        contextPad.revalidate();
        contextPad.setSize(contextPad.getPreferredSize());
        repositionContextPad();

        if (graphComponent.getGraphControl() != null && oldBounds.width > 0) {
            oldBounds.grow(10, 10);
            graphComponent.getGraphControl().repaint(oldBounds);
        }
    }

    /**
     * Renderiza el estado inicial del pad: botones de canal (Direct, Queue, Discard),
     * botón de borrado y rejilla de nodos de inserción rápida.
     */
    private void renderStep1() {
        JPanel topGrid = new JPanel(new GridLayout(1, 4, 4, 0));
        topGrid.setOpaque(false);

        topGrid.add(createChannelButton("DIRECT", AtiIcons.GLOBAL_CONNECT_TOOL_ICON, "Direct"));
        topGrid.add(createChannelButton("QUEUE", AtiIcons.QUEUE_EDGE_ICON, "Queue"));
        topGrid.add(createChannelButton("DISCARD", AtiIcons.DISCARD_EDGE_ICON, "Discard (/)"));
        topGrid.add(createActionButton(AtiIcons.TRASH_ICON, "Delete Node", e -> {
            graphComponent.getGraph().removeCells(new Object[]{currentCell});
            hideContextPad();
        }));

        contextPad.add(topGrid);
        contextPad.add(Box.createRigidArea(new Dimension(0, 4)));
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(JBColor.border());
        contextPad.add(separator);
        contextPad.add(Box.createRigidArea(new Dimension(0, 4)));

        JPanel nodeGrid = new JPanel(new GridLayout(0, 3, 2, 2));
        nodeGrid.setOpaque(false);

        for (WorkFlowStyles.WfNodeType type : getAllowedNodes()) {
            nodeGrid.add(createQuickNodeButton(type));
        }

        contextPad.add(nodeGrid);
    }

    /**
     * Renderiza el segundo estado del pad tras seleccionar un canal.
     * Muestra el botón de retroceso, el canal activo y las opciones para insertar
     * un nodo nuevo o conectar con uno existente.
     */
    private void renderStep2() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel backBtn = new JLabel(" \u25C0 ");
        backBtn.setToolTipText("Back to connections");
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setForeground(JBColor.GRAY);
        backBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                e.consume();
                selectedChannelType = null;
                rebuildUI();
            }
        });

        JLabel label = new JLabel(" " + selectedChannelType + " TO:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));

        header.add(backBtn, BorderLayout.WEST);
        header.add(label, BorderLayout.CENTER);

        contextPad.add(header);
        contextPad.add(Box.createRigidArea(new Dimension(0, 4)));
        JPanel nodeGrid = new JPanel(new GridLayout(0, 3, 2, 2));
        nodeGrid.setOpaque(false);

        for (WorkFlowStyles.WfNodeType type : getAllowedNodes()) {
            nodeGrid.add(createQuickNodeButton(type));
        }

        if ("DISCARD".equals(selectedChannelType)) {
            nodeGrid.add(createDiscardNodeButton());
        }

        contextPad.add(nodeGrid);
        contextPad.add(Box.createRigidArea(new Dimension(0, 4)));
        JSeparator separator2 = new JSeparator(SwingConstants.HORIZONTAL);
        separator2.setForeground(JBColor.border());
        contextPad.add(separator2);
        contextPad.add(createConnectExistingButton());
    }

    /**
     * Actualiza la ubicación física del pad basándose en la posición actual del nodo seleccionado.
     */
    private void repositionContextPad() {
        if (currentCell == null || !contextPad.isVisible()) return;
        mxCellState state = graphComponent.getGraph().getView().getState(currentCell);
        if (state == null) return;

        int x = (int) (state.getX() + state.getWidth() + 10);
        int y = (int) state.getY();
        contextPad.setLocation(x, Math.max(y, 0));
    }

    /**
     * Realiza la inserción de un nuevo nodo y su conexión inmediata con el nodo origen.
     *
     * @param type          Tipo de nodo a insertar.
     * @param channelType   Tipo de canal de conexión.
     * @param isDiscardNode Si es true, ignora el parámetro 'type' y crea un nodo de tipo DISCARD.
     */
    private void insertAndConnect(WorkFlowStyles.WfNodeType type, String channelType, boolean isDiscardNode) {
        com.mxgraph.view.mxGraph graph = graphComponent.getGraph();
        graph.getModel().beginUpdate();
        try {
            mxCellState sourceState = graph.getView().getState(currentCell);
            int w = isDiscardNode ? 120 : type.getWidth();
            int h = isDiscardNode ? 80 : type.getHeight();

            double idealX = sourceState.getX() + sourceState.getWidth() + 100;
            double idealY = sourceState.getY();
            com.mxgraph.util.mxPoint safePos = findFreeSpace(graph, idealX, idealY, w, h);

            WorkflowJsonData newNode = new WorkflowJsonData();
            newNode.setId(UUID.randomUUID().toString());
            newNode.setType(isDiscardNode ? "DISCARD" : type.getLabel());
            newNode.setComponentCode((isDiscardNode ? "discard_" : "node_") + System.currentTimeMillis() % 1000);

            String nodeStyle = isDiscardNode ? WorkFlowStyles.getStyleForType("DISCARD") : WorkFlowStyles.getStyleForType(type.getLabel());

            Object targetCell = graph.insertVertex(graph.getDefaultParent(), null, newNode,
                    safePos.getX(), safePos.getY(), w, h, nodeStyle);

            WorkflowJsonData.EdgeData edge = new WorkflowJsonData.EdgeData();
            edge.setChannelType(channelType);
            edge.setSourceId(((WorkflowJsonData)currentCell.getValue()).getId());
            edge.setTargetId(newNode.getId());

            graph.insertEdge(graph.getDefaultParent(), null, edge, currentCell, targetCell,
                    WorkFlowStyles.getStyleForEdge(channelType));

            graph.setSelectionCell(targetCell);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Crea el botón visual para insertar un nodo de tipo DISCARD (especial).
     */
    private JComponent createDiscardNodeButton() {
        JLabel btn = new JLabel("D");
        btn.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        btn.setToolTipText("Discard Node");
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.RED);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                insertAndConnect(null, "DISCARD", true);
                hideContextPad();
            }
        });
        return btn;
    }

    /**
     * Crea un botón de selección de canal de conexión.
     */
    private JComponent createChannelButton(String type, Icon icon, String tip) {
        Icon scaledIcon = AtiIcons.getScaledIcon(icon, 16);
        JLabel btn = new JLabel(scaledIcon);
        btn.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        btn.setToolTipText(tip);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                e.consume();
                selectedChannelType = type;
                rebuildUI();
            }
        });
        return btn;
    }

    /**
     * Crea un botón para la inserción rápida de un tipo de nodo específico.
     */
    private JComponent createQuickNodeButton(WorkFlowStyles.WfNodeType type) {

        Icon originalIcon = type.getIcon();
        Icon iconNitido;
        iconNitido = AtiIcons.getScaledIcon(originalIcon, 24);
        JLabel btn = new JLabel(iconNitido);
        btn.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        btn.setToolTipText(type.getLabel());
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                String channel = (selectedChannelType != null) ? selectedChannelType : "DIRECT";
                insertAndConnect(type, channel, false);
                hideContextPad();
            }
        });
        return btn;
    }

    /**
     * Crea un botón genérico de acción.
     */
    private JComponent createActionButton(Icon icon, String tip, java.util.function.Consumer<MouseEvent> action) {
        Icon scaledIcon = AtiIcons.getScaledIcon(icon, 10);
        JLabel btn = new JLabel(scaledIcon);
        btn.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        btn.setToolTipText(tip);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                e.consume();
                action.accept(e);
            }
        });
        return btn;
    }

    /** Oculta el Context Pad y limpia la referencia a la celda actual. */
    private void hideContextPad() {
        contextPad.setVisible(false);
        currentCell = null;
    }

    /** Activa el modo de conexión manual (cursor en cruz). */
    private void enableConnectionMode() {
        isConnectingMode = true;
        hideContextPad();
        graphComponent.getGraphControl().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Crea una arista (conexión) entre el nodo actual y un nodo existente seleccionado.
     *
     * @param targetNode  Nodo destino de la conexión.
     * @param channelType Tipo de canal de la conexión.
     */
    private void createEdgeToExisting(mxCell targetNode, String channelType) {
        com.mxgraph.view.mxGraph graph = graphComponent.getGraph();
        graph.getModel().beginUpdate();
        try {
            WorkflowJsonData.EdgeData edge = new WorkflowJsonData.EdgeData();
            edge.setChannelType(channelType);
            edge.setSourceId(((WorkflowJsonData) currentCell.getValue()).getId());
            edge.setTargetId(((WorkflowJsonData) targetNode.getValue()).getId());

            graph.insertEdge(graph.getDefaultParent(), null, edge, currentCell, targetNode,
                    WorkFlowStyles.getStyleForEdge(channelType));

            graph.setSelectionCell(targetNode);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Crea el componente visual para iniciar el modo de "Conectar a existente".
     */
    private JComponent createConnectExistingButton() {
        JLabel btn = new JLabel("\uD83D\uDD17 Conectar a existente");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setToolTipText("Haz clic aquí y luego selecciona una caja en el lienzo");
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(JBColor.blue);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                enableConnectionMode();
            }
        });
        return btn;
    }

    /**
     * Retorna la lista de tipos de nodos permitidos para la inserción rápida desde el pad.
     */
    private List<WorkFlowStyles.WfNodeType> getAllowedNodes() {
        return Arrays.asList(
                WorkFlowStyles.WfNodeType.FILTER,
                WorkFlowStyles.WfNodeType.ROUTER,
                WorkFlowStyles.WfNodeType.ENRICHER,
                WorkFlowStyles.WfNodeType.OUTPUT,
                WorkFlowStyles.WfNodeType.SPLITTER,
                WorkFlowStyles.WfNodeType.AGGREGATOR,
                WorkFlowStyles.WfNodeType.SUBWORKFLOW
        );
    }
}