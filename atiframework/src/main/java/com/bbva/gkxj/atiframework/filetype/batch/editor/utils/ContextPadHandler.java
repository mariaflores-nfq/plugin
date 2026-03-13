package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import com.intellij.ui.JBColor;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import icons.AtiIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.bbva.gkxj.atiframework.filetype.batch.editor.utils.BpmnConstants.getAllowedSuccessors;

/**
 * Clase encargada de la lógica relacionada al menú contextual de herramientas.
 * Decide de forma dinámica que acciones pueden realizarse desde un elemento cualquiera del grafo.
 * Se apoya en {@link BpmnValidationGraph} y {@link BpmnConstants} para validar según cardinalidad y tipo de nodo.
 */
public class ContextPadHandler {

    /**
     * Tamaño de icono del menú contextual
     */
    private static final int TARGET_ICON_SIZE = 20;

    /**
     * Grafo desde el que se lanza la petición del menú contextual
     */
    private final mxGraphComponent graphComponent;
    /**
     * Acción a ejecutar al pulsar el botón de configuración (solo para pasos).
     * Recibe el ID del nodo a configurar.
     */
    private final Consumer<String> onConfigurationAction;
    /**
     * Panel del menú contextual
     */
    private JPanel contextPad;

    /**
     * Constructor parametrizado del menú contextual
     *
     * @param graphComponent Grafo que lanza la petición
     */
    public ContextPadHandler(mxGraphComponent graphComponent, Consumer<String> onConfigurationAction) {
        this.graphComponent = graphComponent;
        this.onConfigurationAction = onConfigurationAction;
        initListeners();
    }

    /**
     * Inicializar listeners
     */
    private void initListeners() {
        graphComponent.getGraph().getSelectionModel().addListener(mxEvent.CHANGE, (sender, evt) -> updateContextPad());
        graphComponent.getGraph().getView().addListener(mxEvent.SCALE, (sender, evt) -> updateContextPad());
        graphComponent.getGraph().getView().addListener(mxEvent.TRANSLATE, (sender, evt) -> updateContextPad());
        graphComponent.getGraphControl().addMouseWheelListener(e -> hideContextPad());
        graphComponent.getGraphControl().addMouseWheelListener(e -> {
            if (e.isControlDown()) graphComponent.getGraph().clearSelection();
        });
    }

    /**
     * Actualiza el estado y visibilidad del Context Pad en función a la selección actual
     */
    private void updateContextPad() {
        Object cell = graphComponent.getGraph().getSelectionCell();
        if (cell == null || graphComponent.getGraph().getModel().isEdge(cell)) {
            hideContextPad();
            return;
        }
        showContextPad((mxCell) cell);
    }

    /**
     * Mostrar el menú contextual
     *
     * @param cell Componente desde el que se lanza el menú contextual
     */
    private void showContextPad(mxCell cell) {
        hideContextPad();

        mxCellState state = graphComponent.getGraph().getView().getState(cell);
        if (state == null) return;

        BpmnValidationGraph graph = (BpmnValidationGraph) graphComponent.getGraph();
        String cardinalityError = graph.validateSourceCardinality(cell);
        boolean canCreateConnections = (cardinalityError == null);
        BpmnConstants.NODETYPE nodeType = graph.getNodeType(cell);

        contextPad = new JPanel();
        contextPad.setLayout(new BoxLayout(contextPad, BoxLayout.Y_AXIS));
        contextPad.setOpaque(true);
        contextPad.setBorder(null);
        contextPad.setBackground(JBColor.PanelBackground);
        contextPad.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        blockGraphInteraction(contextPad);

        if (canCreateConnections) {
            List<BpmnConstants.BpmnPaletteItem> allowedItems = getAllowedSuccessors(nodeType);

            if (!allowedItems.isEmpty()) {
                JPanel shapesGrid = new JPanel(new GridLayout(0, 3, 4, 4));
                shapesGrid.setOpaque(false);

                for (BpmnConstants.BpmnPaletteItem item : allowedItems) {
                    shapesGrid.add(createPadButtonFromEnum(item));
                }
                contextPad.add(shapesGrid);
                contextPad.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        JPanel toolsPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        toolsPanel.setOpaque(false);
        toolsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        if (canCreateConnections) {
            JComponent btnConnect = createPadButton(AtiIcons.GLOBAL_CONNECT_TOOL_ICON, "Manual Connect", "ARROW");
            btnConnect.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    evt.consume();
                    hideContextPad();
                    initiateManualConnection(cell, evt);
                }
            });
            addToolRow(toolsPanel, btnConnect);
        }

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomRow.setOpaque(false);

        JComponent btnRemove = createPadButton(AtiIcons.REMOVE_ICON, "Delete Node", "TRASH");
        btnRemove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
                graphComponent.getGraph().removeCells(new Object[]{cell});
                hideContextPad();
            }
        });
        bottomRow.add(btnRemove);

        if (nodeType == BpmnConstants.NODETYPE.STEP) {
            bottomRow.add(Box.createRigidArea(new Dimension(4, 0)));

            JComponent btnWrench = createPadButton(AtiIcons.WRENCH_CONFIGURATION_ICON, "Configuration", "CONF");
            btnWrench.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    e.consume();
                    String nodeId = cell.getId();
                    graph.setSelectionCell(null);
                    hideContextPad();
                    if (onConfigurationAction != null) {
                        onConfigurationAction.accept(nodeId);
                    }
                }
            });
            bottomRow.add(btnWrench);
        }

        toolsPanel.add(bottomRow);
        contextPad.add(toolsPanel);

        contextPad.setSize(contextPad.getPreferredSize());
        int x = (int) (state.getX() + state.getWidth() + 12);
        int y = (int) state.getY();
        if (y < 0) y = 0;

        contextPad.setLocation(x, y);
        graphComponent.getGraphControl().add(contextPad);
        graphComponent.getGraphControl().revalidate();
        graphComponent.getGraphControl().repaint();
    }

    /**
     * Envuelve el botón en un panel con FlowLayout(LEFT).
     * Esto evita que el botón se estire al ancho del menú ("hueco para nada")
     * y asegura que mantenga su tamaño preferido (cuadrado 1x1).
     */
    private void addToolRow(JPanel parent, JComponent toolButton) {
        JPanel rowWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        rowWrapper.setOpaque(false);
        rowWrapper.add(toolButton);
        parent.add(rowWrapper);
    }


    /**
     * Lógica principal para evaluar la conexión de un elemento con otro mediante la connection tool
     *
     * @param sourceCell Elemento de salida
     * @param startEvt   Evento de mouse
     */
    private void initiateManualConnection(mxCell sourceCell, MouseEvent startEvt) {
        final mxGraphComponent.mxGraphControl graphControl = graphComponent.getGraphControl();
        final com.mxgraph.swing.handler.mxConnectionHandler handler = graphComponent.getConnectionHandler();
        final mxCellState sourceState = graphComponent.getGraph().getView().getState(sourceCell);
        if (sourceState == null) return;
        handler.getConnectPreview().start(startEvt, sourceState, "");

        MouseAdapter connectionListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Object targetCell = graphComponent.getCellAt(e.getX(), e.getY());
                mxCellState targetState = graphComponent.getGraph().getView().getState(targetCell);
                handler.getConnectPreview().update(e, targetState, e.getX(), e.getY());

                if (targetCell != null) {
                    String errorMsg = graphComponent.getGraph().getEdgeValidationError(null, sourceCell, targetCell);
                    if (errorMsg == null) {
                        graphControl.setCursor(DragSource.DefaultLinkDrop);
                    } else {
                        graphControl.setCursor(DragSource.DefaultCopyNoDrop);
                    }
                } else {
                    graphControl.setCursor(DragSource.DefaultCopyNoDrop);
                }
                e.consume();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Object targetCell = graphComponent.getCellAt(e.getX(), e.getY());
                mxGraph graph = graphComponent.getGraph();

                String errorMsg = graph.getEdgeValidationError(null, sourceCell, targetCell);

                if (targetCell != null && errorMsg == null) {
                    graph.getModel().beginUpdate();
                    try {
                        String style = "edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;strokeColor=#000000;strokeWidth=2;";
                        graph.insertEdge(graph.getDefaultParent(), null, "", sourceCell, targetCell, style);
                    } finally {
                        graph.getModel().endUpdate();
                    }
                    stop(e);
                } else if (targetCell != null) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    stop(e);
                } else {
                    stop(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) stop(e);
            }

            private void stop(MouseEvent e) {
                handler.getConnectPreview().stop(true, e);
                graphControl.setCursor(Cursor.getDefaultCursor());
                graphControl.removeMouseListener(this);
                graphControl.removeMouseMotionListener(this);
                e.consume();
            }
        };

        graphControl.addMouseListener(connectionListener);
        graphControl.addMouseMotionListener(connectionListener);
        graphControl.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Busca una posición libre.
     * Lógica: Intenta ponerlo a la derecha. Si está ocupado, baja 60px y vuelve a probar.
     * Nunca subirá, siempre mantendrá la X y bajará la Y si es necesario.
     */
    private Point getFreePosition(mxGraph graph, Object parent, double startX, double startY, double width, double height) {
        double currentY = startY;
        for (int i = 0; i < 100; i++) {
            if (!isRectOccupied(graph, parent, startX, currentY, width, height)) {
                return new Point((int) startX, (int) currentY);
            }
            currentY += (height + 20);
        }
        return new Point((int) startX, (int) startY);
    }

    /**
     * Verifica si un rectángulo imaginario choca con algún nodo existente.
     */
    private boolean isRectOccupied(mxGraph graph, Object parent, double x, double y, double width, double height) {
        Rectangle proposed = new Rectangle((int) x, (int) y, (int) width, (int) height);
        Object[] cells = graph.getChildVertices(parent);

        for (Object cell : cells) {
            com.mxgraph.model.mxGeometry geo = graph.getModel().getGeometry(cell);
            if (geo != null) {
                Rectangle existing = new Rectangle(
                        (int) geo.getX() - 10,
                        (int) geo.getY() - 10,
                        (int) geo.getWidth() + 20,
                        (int) geo.getHeight() + 20
                );
                if (proposed.intersects(existing)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Crear un nuevo elemento desde la paleta, dicho elemento quedará conectado al elemento que lanzó el evento
     *
     * @param item item de la paleta lanzado
     */
    private void createNewConnectedNode(BpmnConstants.BpmnPaletteItem item) {
        mxGraph graph = graphComponent.getGraph();
        Object sourceCell = graph.getSelectionCell();
        if (sourceCell == null) return;
        if (((BpmnValidationGraph) graph).validateSourceCardinality(sourceCell) != null)
            return;
        Object parent = graph.getDefaultParent();
        Object newVertex = null;

        graph.getModel().beginUpdate();
        try {
            com.mxgraph.model.mxGeometry sourceGeo = graph.getModel().getGeometry(sourceCell);

            double width = item.getSize();
            double height = (item.getNodeType() == BpmnConstants.NODETYPE.STEP) ? item.getSize() / 2 : item.getSize();

            double idealX = sourceGeo.getX() + sourceGeo.getWidth() + 80;
            double idealY = sourceGeo.getY();

            Point finalPoint = getFreePosition(graph, parent, idealX, idealY, width, height);

            String style;

            if (item == BpmnConstants.BpmnPaletteItem.BATCH_STEP) {
                String imgUrl = String.valueOf(Objects.requireNonNull(AtiIcons.class.getResource("/icons/batch_icon.svg")));
                style = "shape=label;image=" + imgUrl +
                        ";imageWidth=16;imageHeight=16;imageAlign=left;imageVerticalAlign=top;" +
                        "spacingLeft=4;spacingTop=4;" +
                        "rounded=1;arcSize=5;whiteSpace=wrap;html=1;" +
                        "fillColor=#B4B4FF;strokeColor=#0000FF;strokeWidth=2;" +
                        "fontColor=#000000;align=center;verticalAlign=middle;" +
                        "bpmnType=" + item.name() + ";nodeType=" + item.getNodeType().getValue();
            } else if (item == BpmnConstants.BpmnPaletteItem.ETL_STEP) {
                String imgUrl = String.valueOf(Objects.requireNonNull(AtiIcons.class.getResource("/icons/etl_icon.svg")));
                style = "shape=label;image=" + imgUrl +
                        ";imageWidth=16;imageHeight=16;imageAlign=left;imageVerticalAlign=top;" +
                        "spacingLeft=4;spacingTop=4;" +
                        "rounded=1;arcSize=5;whiteSpace=wrap;html=1;" +
                        "fillColor=#D5F5D5;strokeColor=#33CC33;strokeWidth=2;" +
                        "fontColor=#000000;align=center;verticalAlign=middle;" +
                        "bpmnType=" + item.name() + ";nodeType=" + item.getNodeType().getValue();
            } else if (item.getNodeType() == BpmnConstants.NODETYPE.STEP) {
                style = "shape=rectangle;rounded=1;arcSize=5;whiteSpace=wrap;html=1;" +
                        "fillColor=#F5F5F5;strokeColor=#666666;strokeWidth=2;" +
                        "fontColor=#333333;align=center;verticalAlign=middle;" +
                        "bpmnType=" + item.name() + ";nodeType=" + item.getNodeType().getValue();
            } else {
                String iconUrl = Objects.requireNonNull(AtiIcons.class.getResource("/icons/" + item.getSvgName())).toString();
                style = "shape=image;image=" + iconUrl + ";imageWidth=" + (int) width + ";imageHeight=" + (int) height +
                        ";verticalLabelPosition=bottom;verticalAlign=top;spacing=0;"
                        + "bpmnType=" + item.name() + ";nodeType=" + item.getNodeType().getValue();
            }

            String labelText = (item.getNodeType() == BpmnConstants.NODETYPE.STEP) ? item.getLabel() : "";

            newVertex = graph.insertVertex(parent, null, labelText, finalPoint.getX(), finalPoint.getY(), width, height, style);
            String edgeStyle = "edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;strokeColor=#000000;";
            graph.insertEdge(parent, null, "", sourceCell, newVertex, edgeStyle);

            graph.setSelectionCell(newVertex);
        } finally {
            graph.getModel().endUpdate();
        }
        if (newVertex != null) {
            graph.setSelectionCell(newVertex);
        }
    }

    /**
     * Crear botón del menú contextual en base a su tipo
     *
     * @param item botón de la paleta
     * @return botón del menú contextual
     */
    private JComponent createPadButtonFromEnum(BpmnConstants.BpmnPaletteItem item) {
        JComponent btn = createPadButton(item.getIcon(), item.getLabel(), null);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
                createNewConnectedNode(item);
            }
        });
        return btn;
    }

    /**
     * Crear Botón del menú contextual
     *
     * @param originalIcon icono de la opción
     * @param tooltip      descripción
     * @param fallbackText texto en caso de fallo en el icono
     * @return botón del menú contextual
     */
    private JComponent createPadButton(Icon originalIcon, String tooltip, String fallbackText) {
        JLabel btn = new JLabel();
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (originalIcon != null) {
            btn.setIcon(new ResizedIcon(originalIcon, TARGET_ICON_SIZE, TARGET_ICON_SIZE));
        } else {
            btn.setText(fallbackText != null ? fallbackText : "?");
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }
        });
        return btn;
    }

    /**
     * Ocultar el menú contextual
     */
    private void hideContextPad() {
        if (contextPad != null) {
            graphComponent.getGraphControl().remove(contextPad);
            graphComponent.getGraphControl().repaint();
            contextPad = null;
        }
    }

    /**
     * Evitar que el menú contextual mande eventos al grafo principal
     *
     * @param component Componente del que se bloquean los eventos
     */
    private void blockGraphInteraction(JComponent component) {
        MouseAdapter blocker = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }
        };
        component.addMouseListener(blocker);
        component.addMouseMotionListener(blocker);
    }

    private record ResizedIcon(Icon delegate, int w, int h) implements Icon {
        @Override
        public int getIconWidth() {
            return w;
        }

        @Override
        public int getIconHeight() {
            return h;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.translate(x, y);
            g2.scale((double) w / delegate.getIconWidth(), (double) h / delegate.getIconHeight());
            delegate.paintIcon(c, g2, 0, 0);
            g2.dispose();
        }
    }
}