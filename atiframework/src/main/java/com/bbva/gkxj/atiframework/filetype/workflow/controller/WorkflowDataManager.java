package com.bbva.gkxj.atiframework.filetype.workflow.controller;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestor central para la manipulación y persistencia de datos del flujo de trabajo (Workflow).
 * <p>
 * Esta clase actúa como controlador entre la representación visual del grafo (mxGraph)
 * y el modelo de datos subyacente ({@link WorkflowJsonData}).
 * </p>
 * <p>
 * Gestiona la inserción de nodos, conexiones, y es el responsable de serializar (exportar)
 * y deserializar (importar) el lienzo gráfico, incluyendo las posiciones manuales de los elementos
 * y la limpieza de datos anidados para mantener un JSON óptimo.
 * </p>
 */
public class WorkflowDataManager {

    /** Componente visual de Swing que contiene el lienzo interactivo. */
    private final mxGraphComponent graphComponent;

    /** Modelo lógico del grafo. */
    private final mxGraph graph;

    /** Objeto raíz que contiene la configuración global y las listas de nodos/aristas. */
    private WorkflowJsonData globalWorkflowData;

    /**
     * Constructor que inicializa el gestor con un componente de grafo específico.
     * @param graphComponent El componente Swing que contiene la visualización del grafo.
     */
    public WorkflowDataManager(mxGraphComponent graphComponent) {
        this.graphComponent = graphComponent;
        this.graph = graphComponent.getGraph();
        this.globalWorkflowData = new WorkflowJsonData();
    }

    /**
     * Obtiene los datos globales actuales del workflow (sin sincronizar los últimos cambios del lienzo).
     * @return El objeto de datos global que representa el estado actual del workflow.
     */
    public WorkflowJsonData getGlobalData() {
        return globalWorkflowData;
    }

    /**
     * Inserta un nuevo nodo en el grafo en una posición específica (X, Y).
     * @param type El tipo de nodo (ej. "Filter", "Router") definido en {@link WorkFlowStyles}.
     * @param x Coordenada X donde se desea centrar el nodo.
     * @param y Coordenada Y donde se desea centrar el nodo.
     */
    public void insertNode(String type, int x, int y) {
        graph.getModel().beginUpdate();
        try {
            WorkFlowStyles.WfNodeType nodeStyleInfo = WorkFlowStyles.WfNodeType.fromLabel(type);
            String style = WorkFlowStyles.getStyleForType(type);

            WorkflowJsonData newNodeData = new WorkflowJsonData();
            newNodeData.setId(UUID.randomUUID().toString());
            newNodeData.setType(type);
            newNodeData.setComponentCode("new_" + type.toLowerCase() + "_" + System.currentTimeMillis() % 1000);

            graph.insertVertex(graph.getDefaultParent(), null, newNodeData,
                    x - (nodeStyleInfo.getWidth() / 2.0), y - (nodeStyleInfo.getHeight() / 2.0),
                    nodeStyleInfo.getWidth(), nodeStyleInfo.getHeight(), style);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Carga un grafo completo a partir de un objeto de datos JSON.
     * Lee las posiciones manuales guardadas en los nodos (si existen) o, en su defecto,
     * aplica un diseño jerárquico automático de ordenación.
     * @param jsonData El objeto raíz que contiene la lista de nodos y aristas a cargar.
     * @param onComplete Acción opcional a ejecutar tras finalizar la carga y el renderizado visual.
     */
    public void loadGraphFromData(WorkflowJsonData jsonData, Runnable onComplete) {
        if (jsonData == null) return;

        this.globalWorkflowData = jsonData;
        Object parent = graph.getDefaultParent();

        boolean needsAutoLayout = false;

        graph.getModel().beginUpdate();
        try {
            graph.removeCells(graph.getChildVertices(parent));
            Map<String, Object> vertexMap = new HashMap<>();

            // 1. Reconstrucción de Vértices (Nodos) y lectura de coordenadas espaciales
            if (jsonData.getNodeList() != null) {
                for (WorkflowJsonData node : jsonData.getNodeList()) {
                    if (node.getId() == null || node.getId().isEmpty()) {
                        node.setId(UUID.randomUUID().toString());
                    }
                    String type = node.getType() != null ? node.getType() : "Filter";
                    WorkFlowStyles.WfNodeType nodeStyleInfo = WorkFlowStyles.WfNodeType.fromLabel(type);
                    String style = WorkFlowStyles.getStyleForType(type);

                    double x = (node.getX() != null) ? node.getX() : 0.0;
                    double y = (node.getY() != null) ? node.getY() : 0.0;

                    if (x == 0.0 && y == 0.0) {
                        needsAutoLayout = true;
                    }

                    Object vertex = graph.insertVertex(parent, null, node,
                            x, y, nodeStyleInfo.getWidth(), nodeStyleInfo.getHeight(), style);
                    vertexMap.put(node.getId(), vertex);
                }
            }

            // 2. Reconstrucción de Edges (Conexiones) y lectura de codos (puntos)
            if (jsonData.getEdgeList() != null) {
                for (WorkflowJsonData.EdgeData edge : jsonData.getEdgeList()) {
                    Object sourceVertex = vertexMap.get(edge.getSourceId());
                    Object targetVertex = vertexMap.get(edge.getTargetId());

                    if (sourceVertex != null && targetVertex != null) {
                        String edgeStyle = WorkFlowStyles.getStyleForEdge(edge.getChannelType());

                        Object edgeCell = graph.insertEdge(parent, null, edge, sourceVertex, targetVertex, edgeStyle);

                        // Obtenemos la geometría que acaba de crear mxGraph
                        com.mxgraph.model.mxGeometry edgeGeo = graph.getModel().getGeometry(edgeCell);

                        // --- Ajuste de posición si es DISCARD ---
                        if ("DISCARD".equalsIgnoreCase(edge.getChannelType())) {
                            edgeGeo.setRelative(true);
                            edgeGeo.setX(-0.8);

                            if (edgeGeo.getOffset() == null) {
                                edgeGeo.setOffset(new com.mxgraph.util.mxPoint(0, 0));
                            } else {
                                edgeGeo.getOffset().setY(0);
                            }
                        }

                        // Restaurar los codos (puntos intermedios) si el JSON los tiene
                        if (edge.getPoints() != null && !edge.getPoints().isEmpty()) {
                            java.util.List<com.mxgraph.util.mxPoint> mxPoints = new ArrayList<>();
                            for (WorkflowJsonData.PointData pt : edge.getPoints()) {
                                mxPoints.add(new com.mxgraph.util.mxPoint(pt.x, pt.y));
                            }
                            edgeGeo.setPoints(mxPoints);
                        }

                        // Guardamos la geometría actualizada (con la nueva posición y los puntos)
                        graph.getModel().setGeometry(edgeCell, edgeGeo);
                    }
                }
            }

            // 3. Aplicar Layout automático SÓLO si es necesario
            if (needsAutoLayout) {
                com.mxgraph.layout.hierarchical.mxHierarchicalLayout layout =
                        new com.mxgraph.layout.hierarchical.mxHierarchicalLayout(graph);
                layout.setOrientation(SwingConstants.WEST);
                layout.execute(parent);
            }

        } finally {
            graph.getModel().endUpdate();

            final boolean finalNeedsAutoLayout = needsAutoLayout;

            SwingUtilities.invokeLater(() -> {
                if (finalNeedsAutoLayout) {
                    centerGraph();
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        }
    }

    /**
     * Sincroniza el estado visual del grafo actual con la estructura de datos para su exportación.
     * Captura las coordenadas espaciales (X, Y) de los nodos, los puntos de las flechas, y
     * <b>limpia las colecciones globales en los nodos hijos</b> para evitar duplicados en el JSON.
     * @return El objeto raíz {@link WorkflowJsonData} actualizado listo para ser guardado.
     */
    /**
     * Sincroniza el estado visual del grafo actual con la estructura de datos para su exportación.
     * Captura las coordenadas espaciales (X, Y) de los nodos, los puntos de las flechas, y
     * <b>limpia las colecciones globales en los nodos hijos</b> para evitar duplicados en el JSON.
     * @return El objeto raíz {@link WorkflowJsonData} actualizado listo para ser guardado.
     */
    public WorkflowJsonData exportGraphToJsonData() {
        WorkflowJsonData exportData = this.globalWorkflowData;
        if (exportData == null) {
            exportData = new WorkflowJsonData();
        }

        java.util.List<WorkflowJsonData> nodes = new ArrayList<>();
        java.util.List<WorkflowJsonData.EdgeData> edges = new ArrayList<>();

        Object parent = graph.getDefaultParent();
        Object[] allCells = graph.getChildCells(parent);

        for (Object cellObj : allCells) {
            if (cellObj instanceof mxCell) {
                mxCell cell = (mxCell) cellObj;

                // --- 1. Recopilación de Nodos y su Geometría ---
                if (cell.isVertex() && cell.getValue() instanceof WorkflowJsonData) {
                    WorkflowJsonData nodeData = (WorkflowJsonData) cell.getValue();
                    if (nodeData.getId() == null || nodeData.getId().isEmpty()) {
                        nodeData.setId(UUID.randomUUID().toString());
                    }

                    com.mxgraph.model.mxGeometry geo = cell.getGeometry();
                    if (geo != null) {
                        nodeData.setX(geo.getX());
                        nodeData.setY(geo.getY());
                    }

                    // Limpieza para evitar JSON cíclico o duplicado
                    nodeData.clearGlobalListsForNode();
                    nodes.add(nodeData);

                    // --- 2. Recopilación de Conexiones (Edges) ---
                } else if (cell.isEdge()) {
                    mxCell source = (mxCell) cell.getSource();
                    mxCell target = (mxCell) cell.getTarget();

                    if (source != null && target != null &&
                            source.getValue() instanceof WorkflowJsonData &&
                            target.getValue() instanceof WorkflowJsonData) {

                        WorkflowJsonData sourceNode = (WorkflowJsonData) source.getValue();
                        WorkflowJsonData targetNode = (WorkflowJsonData) target.getValue();

                        WorkflowJsonData.EdgeData edgeData;
                        if (cell.getValue() instanceof WorkflowJsonData.EdgeData) {
                            edgeData = (WorkflowJsonData.EdgeData) cell.getValue();
                        } else {
                            edgeData = new WorkflowJsonData.EdgeData();
                            edgeData.setChannelType("DIRECT");
                        }

                        edgeData.setSourceId(sourceNode.getId());
                        edgeData.setTargetId(targetNode.getId());
                        edgeData.setSourceType(sourceNode.getType());

                        java.util.List<WorkflowJsonData.PointData> pointDataList = new ArrayList<>();

                        // SIEMPRE leemos el estado visual (lo que el usuario ve en la pantalla)
                        com.mxgraph.view.mxCellState state = graph.getView().getState(cell);

                        if (state != null && state.getAbsolutePoints() != null) {
                            java.util.List<com.mxgraph.util.mxPoint> absolutePoints = state.getAbsolutePoints();

                            // Si hay más de 2 puntos, hay codos (ya sean manuales o calculados por el motor)
                            if (absolutePoints.size() > 2) {
                                double scale = graph.getView().getScale();
                                com.mxgraph.util.mxPoint trans = graph.getView().getTranslate();

                                // Ignoramos el índice 0 (salida) y el último (entrada)
                                for (int i = 1; i < absolutePoints.size() - 1; i++) {
                                    com.mxgraph.util.mxPoint absPt = absolutePoints.get(i);

                                    // Des-escalamos para obtener la coordenada real del lienzo
                                    double realX = (absPt.getX() / scale) - trans.getX();
                                    double realY = (absPt.getY() / scale) - trans.getY();

                                    // REDONDEO CRÍTICO: Evita el "baile" de las flechas por decimales infinitos al hacer zoom
                                    realX = Math.round(realX * 2.0) / 2.0; // Redondea a .0 o .5
                                    realY = Math.round(realY * 2.0) / 2.0;

                                    pointDataList.add(new WorkflowJsonData.PointData(realX, realY));
                                }
                            }
                        }

                        edgeData.setPoints(pointDataList.isEmpty() ? null : pointDataList);
                        edges.add(edgeData);
                    }
                }
            }
        }

        exportData.setNodeList(nodes.isEmpty() ? null : nodes);
        exportData.setEdgeList(edges.isEmpty() ? null : edges);
        return exportData;
    }
    /**
     * Conecta de forma lógica y visual dos nodos existentes en el grafo.
     */
    public void connectNodes(mxCell source, mxCell target, String channelType) {
        graph.getModel().beginUpdate();
        try {
            WorkflowJsonData.EdgeData edge = new WorkflowJsonData.EdgeData();
            edge.setChannelType(channelType);

            WorkflowJsonData sourceData = (WorkflowJsonData) source.getValue();
            WorkflowJsonData targetData = (WorkflowJsonData) target.getValue();

            if (targetData.getId() == null) targetData.setId(UUID.randomUUID().toString());

            edge.setSourceId(sourceData.getId());
            edge.setTargetId(targetData.getId());

            String style = WorkFlowStyles.getStyleForEdge(channelType);
            if (style == null) style = "defaultEdge;";

            // GUARDAMOS LA CELDA EN UNA VARIABLE
            Object edgeCell = graph.insertEdge(graph.getDefaultParent(), null, edge, source, target, style);

            // Ajuste de posición de la etiqueta para DISCARD
            if ("DISCARD".equalsIgnoreCase(channelType)) {
                com.mxgraph.model.mxGeometry geo = graph.getModel().getGeometry(edgeCell);
                geo = (com.mxgraph.model.mxGeometry) geo.clone();

                // 1. OBLIGATORIO para flechas: indicar que la geometría es relativa al recorrido
                geo.setRelative(true);

                // 2. Mover la etiqueta al principio de la flecha
                geo.setX(-0.8);
                geo.setY(0);

                // 3. OBLIGATORIO: Inicializar el Offset para evitar que mxGraph aborte el pintado
                if (geo.getOffset() == null) {
                    geo.setOffset(new com.mxgraph.util.mxPoint(0, 0));
                } else {
                    geo.getOffset().setY(0);
                }

                graph.getModel().setGeometry(edgeCell, geo);
            }

        } finally {
            graph.getModel().endUpdate();
        }
        graphComponent.refresh();
    }

    /**
     * Crea un nuevo nodo en el lienzo y lo conecta automáticamente a un nodo de origen existente.
     * Utiliza un algoritmo simple de detección de colisiones para posicionar el nuevo nodo en un área libre.
     * @param sourceCell El nodo desde el cual partirá la conexión.
     * @param type Definición del tipo de nodo a crear.
     * @param channelType El tipo de conexión para la arista (ej. "DIRECT").
     * @param isDiscardNode Si es true, fuerza la creación de un nodo de descarte con dimensiones predeterminadas.
     */
    public void insertNodeAndConnect(mxCell sourceCell, WorkFlowStyles.WfNodeType type, String channelType, boolean isDiscardNode) {
        graph.getModel().beginUpdate();
        try {
            com.mxgraph.view.mxCellState sourceState = graph.getView().getState(sourceCell);
            int w = isDiscardNode ? 120 : type.getWidth();
            int h = isDiscardNode ? 80 : type.getHeight();

            com.mxgraph.util.mxPoint safePos = findFreeSpace(sourceState.getX() + sourceState.getWidth() + 100, sourceState.getY(), w, h);

            WorkflowJsonData newNode = new WorkflowJsonData();
            newNode.setId(UUID.randomUUID().toString());
            newNode.setType(isDiscardNode ? "DISCARD" : type.getLabel());
            newNode.setComponentCode((isDiscardNode ? "discard_" : "node_") + System.currentTimeMillis() % 1000);

            Object targetCell = graph.insertVertex(graph.getDefaultParent(), null, newNode,
                    safePos.getX(), safePos.getY(), w, h, WorkFlowStyles.getStyleForType(newNode.getType()));

            connectNodes(sourceCell, (mxCell) targetCell, channelType);

            graph.setSelectionCell(targetCell);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Busca un espacio libre en el lienzo para evitar el solapamiento visual de nodos
     * cuando se ejecutan las funciones de auto-conectar e inserción rápida.
     * @param startX Posición X inicial sugerida.
     * @param startY Posición Y inicial sugerida.
     * @param width Ancho del nodo a posicionar.
     * @param height Alto del nodo a posicionar.
     * @return Un punto {@link com.mxgraph.util.mxPoint} con las coordenadas finales calculadas y seguras.
     */
    private com.mxgraph.util.mxPoint findFreeSpace(double startX, double startY, double width, double height) {
        double currentX = startX, currentY = startY;
        boolean collision = true;
        int attempts = 0;

        while (collision && attempts < 50) {
            collision = false;
            Rectangle proposed = new Rectangle((int)currentX, (int)currentY, (int)width, (int)height);

            for (Object c : graph.getChildVertices(graph.getDefaultParent())) {
                com.mxgraph.model.mxGeometry geo = graph.getModel().getGeometry(c);
                if (proposed.intersects(new Rectangle((int)geo.getX()-20, (int)geo.getY()-20, (int)geo.getWidth()+40, (int)geo.getHeight()+40))) {
                    collision = true;
                    currentY += height + 30;

                    if (attempts % 3 == 2) {
                        currentY = startY;
                        currentX += width + 50;
                    }
                    break;
                }
            }
            attempts++;
        }
        return new com.mxgraph.util.mxPoint(currentX, currentY);
    }

    /**
     * Centra visualmente todos los elementos del grafo dentro del área visible del componente.
     */
    public void centerGraph() {
        Object parent = graph.getDefaultParent();
        Object[] cells = graph.getChildCells(parent);
        if (cells.length == 0) return;

        mxRectangle bounds = graph.getGraphBounds();
        Dimension viewportSize = graphComponent.getViewport().getSize();

        double viewWidth = viewportSize.width > 0 ? viewportSize.width : 1200;
        double viewHeight = viewportSize.height > 0 ? viewportSize.height : 800;

        double dx = (viewWidth - bounds.getWidth()) / 2.0 - bounds.getX();
        double dy = (viewHeight - bounds.getHeight()) / 2.0 - bounds.getY();

        final int MARGIN = 80;

        if (bounds.getWidth() > viewWidth || dx < 0) {
            dx = MARGIN - bounds.getX();
        }

        if (bounds.getHeight() > viewHeight || dy < 0) {
            dy = MARGIN - bounds.getY();
        }

        graph.getModel().beginUpdate();
        try {
            graph.moveCells(cells, dx, dy);
        } finally {
            graph.getModel().endUpdate();
        }
    }
}