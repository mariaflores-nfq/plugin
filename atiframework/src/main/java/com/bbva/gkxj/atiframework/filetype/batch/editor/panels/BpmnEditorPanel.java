package com.bbva.gkxj.atiframework.filetype.batch.editor.panels;

import com.bbva.gkxj.atiframework.filetype.batch.editor.utils.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.ui.JBColor;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Clase que representa el panel de edicion principal de ficheros batch.
 * Gestiona el ciclo de vida del grafo visual, la interaccion del usuario y la sincronizacion
 * de los elementos visuales con la estructura de datos en formato JSON.
 */
public class BpmnEditorPanel extends JPanel {

    /**
     * Componente que recoge la funcionalidad estructural base para la edicion de grafos,
     * incluyendo el lienzo y los paneles laterales.
     */
    private BasicGraphEditor editor;

    /**
     * Referencia al modelo logico y visual del diagrama BPMN.
     */
    private mxGraph graph;

    /**
     * Bandera de control para evitar disparos recursivos de eventos de cambio
     * mientras se esta reconstruyendo el grafo desde un origen de datos externo.
     */
    private boolean isRendering = false;

    /**
     * Consumidor de eventos personalizado que se ejecuta al solicitar la apertura de un subproceso.
     * Recibe el identificador del nodo que representa el subproceso a abrir.
     */
    private Consumer<String> onOpenSubProcess;

    /**
     * Constructor del panel de edicion.
     * Establece el diseño base e inicializa todos los componentes de la interfaz de usuario.
     */
    public BpmnEditorPanel() {
        super(new BorderLayout());
        initUIComponents();
    }

    /**
     * Permite registrar un manejador externo para los eventos de apertura de subprocesos.
     *
     * @param onOpenSubProcess Accion a ejecutar que recibe el identificador del nodo a abrir.
     */
    public void setOnOpenSubProcess(Consumer<String> onOpenSubProcess) {
        this.onOpenSubProcess = onOpenSubProcess;
    }

    /**
     * Dispara el evento de apertura de subproceso con el identificador del nodo proporcionado.
     *
     * @param nodeId Identificador del nodo que representa el subproceso a abrir.
     */
    private void fireOpenSubProcess(String nodeId) {
        if (onOpenSubProcess != null) {
            onOpenSubProcess.accept(nodeId);
        }
    }

    /**
     * Genera un identificador unico para una celda del grafo basandose en su tipo y estilo.
     * Si la celda es un evento de inicio, aplica una regla estatica especifica.
     *
     * @param cell La celda para la cual se generara el identificador.
     * @return Una cadena de texto que representa el identificador unico de la celda.
     */
    private String generateBpmnId(mxCell cell) {
        if (cell == null) return null;

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 7).toLowerCase();

        if (cell.isEdge()) {
            return "_" + suffix;
        }

        String style = cell.getStyle();
        if (style == null) return null;

        BpmnConstants.BpmnPaletteItem item = detectItemFromStyle(style);

        return switch (Objects.requireNonNull(item).getNodeType()) {
            case STEP -> "Activity_" + suffix;
            case GATEWAY -> "Gateway_" + suffix;
            case FINAL -> "Event_" + suffix;
            case INITIAL -> {
                if (((mxGraphModel) graph.getModel()).getCell("StartEvent_1") != null) {
                    yield "StartEvent_" + suffix;
                }
                yield "StartEvent_1";
            }
            default -> "Node_" + suffix;
        };
    }

    /**
     * Inicializacion de la interfaz y configuracion central del motor del grafo.
     * Configura el modelo de identificadores, los estilos visuales de las conexiones,
     * el manejador de seleccion con bordes personalizados, los eventos de raton para hacer zoom
     * y ensambla la paleta lateral de herramientas.
     */
    private void initUIComponents() {
        ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            graph = new BpmnValidationGraph();
            graph.setModel(new mxGraphModel() {
                @Override
                public String createId(Object cell) {
                    if (cell instanceof mxCell) {
                        String customId = generateBpmnId((mxCell) cell);
                        if (customId != null) return customId;
                    }
                    return super.createId(cell);
                }
            });

            // Inicializar el gestor de estilos (pre-carga imágenes y registra estilos)
            BpmnStyleManager.initialize(graph);

            Map<String, Object> edgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
            edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
            edgeStyle.put(mxConstants.STYLE_ROUNDED, true);
            edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#555555");
            edgeStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
            graph.setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
            configureBpmnRules(graph);

            // ===== COMPONENTE DE GRAFO OPTIMIZADO =====
            // Usamos OptimizedGraphComponent que implementa:
            // - Detección automática de scroll/zoom/drag
            // - Desactivación de anti-aliasing durante interacciones
            // - Throttling de repintados (máx 60fps durante interacción)
            // - Ocultación de labels durante scroll/zoom
            final int SELECTION_MARGIN = 4;
            final OptimizedGraphComponent graphComponent = new OptimizedGraphComponent(graph) {
                @Override
                public com.mxgraph.swing.handler.mxCellHandler createHandler(com.mxgraph.view.mxCellState state) {
                    com.mxgraph.view.mxGraph g = getGraph();
                    // Obtener referencia al InteractionManager para optimizar handlers
                    InteractionManager interactionMgr = InteractionManager.getInstance();

                    if (g.getModel().isVertex(state.getCell())) {
                        return new com.mxgraph.swing.handler.mxVertexHandler(this, state) {
                            @Override
                            public void paint(Graphics gr) {
                                // Durante interacción, usar renderizado simplificado del handler
                                if (state != null && gr instanceof Graphics2D) {
                                    Rectangle r = state.getRectangle();
                                    if (r != null) {
                                        Rectangle rect = new Rectangle(r);
                                        rect.grow(SELECTION_MARGIN, SELECTION_MARGIN);
                                        Graphics2D g2 = (Graphics2D) gr;

                                        // Optimización: solo usar anti-aliasing si no estamos interactuando
                                        if (interactionMgr.shouldUseAntiAliasing()) {
                                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                        } else {
                                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                                        }

                                        g2.setColor(new JBColor(new Color(0x389FD6), new Color(0x389FD6)));
                                        g2.setStroke(new BasicStroke(2.0f));
                                        int arc = 16;
                                        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, arc, arc);
                                    }
                                }
                            }
                        };
                    }
                    else if (g.getModel().isEdge(state.getCell())) {
                        return new com.mxgraph.swing.handler.mxEdgeHandler(this, state) {
                            @Override
                            public Color getSelectionColor() {
                                return new JBColor(new Color(0x389FD6), new Color(0x389FD6));
                            }

                            @Override
                            public Stroke getSelectionStroke() {
                                return new BasicStroke(2.0f);
                            }
                        };
                    }
                    return super.createHandler(state);
                }
            };

            graphComponent.getConnectionHandler().addListener(mxEvent.CONNECT, (sender, evt) -> {
                graphComponent.setConnectable(false);
                graphComponent.getConnectionHandler().setEnabled(false);
                graphComponent.getGraphControl().setCursor(Cursor.getDefaultCursor());
            });

            // Listener de zoom con Ctrl+Rueda - también notifica al InteractionManager
            graphComponent.getGraphControl().addMouseWheelListener(e -> {
                if (e.isControlDown()) {
                    // Notificar interacción para activar renderizado simplificado
                    InteractionManager.getInstance().startInteraction();
                    InteractionManager.getInstance().setCurrentScale(graph.getView().getScale());

                    if (e.getWheelRotation() < 0) {
                        graphComponent.zoomIn();
                    } else {
                        graphComponent.zoomOut();
                    }
                    e.consume();
                }
            });

            // Configuración básica del componente
            graphComponent.setCenterZoom(true);
            graphComponent.setDragEnabled(false);
            graphComponent.setImportEnabled(true);
            graphComponent.setTransferHandler(new mxGraphTransferHandler());
            graphComponent.setPanning(true);
            graphComponent.setConnectable(false);
            graphComponent.getConnectionHandler().setEnabled(false);
            graphComponent.getConnectionHandler().setCreateTarget(false);
            graphComponent.setGridVisible(false);
            graphComponent.setToolTips(true);

            // NOTA: Las siguientes optimizaciones ya están configuradas en OptimizedGraphComponent:
            // - Double/Triple buffering
            // - Anti-aliasing (dinámico según estado de interacción)
            // - Throttling de repintados durante scroll/zoom

            Color white = new JBColor(Color.WHITE, Color.WHITE);
            graphComponent.getViewport().setBackground(white);
            graphComponent.setBackground(white);
            new ContextPadHandler(graphComponent, this::fireOpenSubProcess);
            this.editor = new BasicGraphEditor("Editor BPMN", graphComponent);
            EditorPalette palette = editor.insertPalette("ATI Tools");
            setupPalette(palette);
            add(editor, BorderLayout.CENTER);

        } finally {
            Thread.currentThread().setContextClassLoader(originalLoader);
        }
    }

    /**
     * Configura la paleta con los elementos iterando sobre la enumeracion de items disponibles.
     *
     * @param palette Paleta lateral en la que se inyectaran los elementos de dibujo.
     */
    private void setupPalette(EditorPalette palette) {
        for (BpmnConstants.BpmnPaletteItem item : BpmnConstants.BpmnPaletteItem.values()) {
            addStencilFromEnum(palette, item);
        }
    }

    /**
     * Genera una plantilla especifica a partir de un elemento de la enumeracion y la añade a la paleta.
     * Determina las dimensiones base, los estilos visuales y el texto predeterminado segun el tipo de nodo.
     *
     * @param palette Paleta lateral de destino.
     * @param item Elemento constante que define las caracteristicas del nodo a crear.
     */
    private void addStencilFromEnum(EditorPalette palette, BpmnConstants.BpmnPaletteItem item) {
        String style = generateStyleForItem(item);
        int width = item.getSize();
        int height = (item.getNodeType() == BpmnConstants.NODETYPE.STEP) ? item.getSize() / 2 : item.getSize();
        String labelText = (item.getNodeType() == BpmnConstants.NODETYPE.STEP) ? item.getLabel() : "";
        palette.addTemplate(
                "",
                toImageIcon(item.getIcon()),
                style,
                width,
                height,
                labelText
        );
    }

    /**
     * Convierte un icono generico en un objeto ImageIcon asegurando la compatibilidad de pintado.
     * Delega al BpmnStyleManager para aprovechar el caché de imágenes.
     *
     * @param icon Icono de origen, tipicamente proveniente del sistema de iconos de IntelliJ.
     * @return Una instancia de ImageIcon lista para ser usada en componentes de Swing.
     */
    private ImageIcon toImageIcon(Icon icon) {
        return BpmnStyleManager.toImageIcon(icon);
    }


    /**
     * Configura de manera global las reglas de interaccion permitidas en el grafo para todos los elementos.
     * Bloquea la edicion directa, el redimensionado, y ajusta comportamientos como el auto-tamano y las etiquetas HTML.
     *
     * @param graph Instancia del grafo a configurar.
     */
    private void configureBpmnRules(mxGraph graph) {
        graph.setCellsEditable(false);
        graph.setCellsResizable(false);
        graph.setCellsMovable(true);
        graph.setCellsDisconnectable(true);
        graph.setConnectableEdges(false);
        graph.setAllowDanglingEdges(false);
        graph.setHtmlLabels(true);
        graph.setAutoSizeCells(false);
        graph.setAutoOrigin(true);
        graph.setConnectableEdges(true);
        graph.setSplitEnabled(false);
    }

    /**
     * Registra un escuchador para capturar los eventos de seleccion de celdas en el diagrama.
     *
     * @param listener Accion a ejecutar que recibe la celda seleccionada, o nulo si se deshizo la seleccion.
     */
    public void addSelectionListener(Consumer<mxCell> listener) {
        if (graph != null) {
            graph.getSelectionModel().addListener(mxEvent.CHANGE, (sender, evt) -> {
                Object cell = graph.getSelectionCell();
                listener.accept(cell instanceof mxCell ? (mxCell) cell : null);
            });
        }
    }

    /**
     * Registra un escuchador para capturar cualquier cambio estructural que ocurra en el grafo,
     * ignorando aquellos que suceden durante procesos de renderizado interno.
     *
     * @param listener Tarea a ejecutar cuando el grafo sea modificado por el usuario.
     */
    public void addGraphChangeListener(Runnable listener) {
        if (graph != null) {
            graph.getModel().addListener(mxEvent.CHANGE, (sender, evt) -> {
                if (!isRendering) listener.run();
            });
        }
    }

    /**
     * Destruye el contenido actual del lienzo y reconstruye el diagrama visual completo
     * a partir de la informacion de nodos y conexiones suministrada en un objeto JSON.
     *
     * @param json Objeto JSON estructurado que contiene las listas de nodos y enlaces a dibujar.
     */
    public void renderGraphFromJSON(JsonObject json) {
        if (json == null || !json.has("workflowNodeList")) return;
        isRendering = true;
        String edgeStyle = "edgeStyle=orthogonalEdgeStyle;" +
                "rounded=1;jettySize=auto;orthogonalLoop=1;" +
                "strokeColor=#555555;strokeWidth=2;endArrow=block;endFill=1;";

        graph.getModel().beginUpdate();
        try {
            graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
            Object parent = graph.getDefaultParent();
            JsonArray nodes = json.getAsJsonArray("workflowNodeList");
            Map<String, Object> vertexMap = new HashMap<>();
            for (JsonElement element : nodes) {
                if (!element.isJsonObject()) continue;
                JsonObject node = element.getAsJsonObject();
                String id = node.has("workflowNodeCode") ? node.get("workflowNodeCode").getAsString() : "";
                if(id.isEmpty()) continue;

                String jsonType = node.has("type") && !node.get("type").isJsonNull()
                        ? node.get("type").getAsString() : "ETL";

                BpmnConstants.BpmnPaletteItem item = BpmnConstants.fromJsonType(jsonType);
                int x = node.has("xPosition") ? node.get("xPosition").getAsInt() : 0;
                int y = node.has("yPosition") ? node.get("yPosition").getAsInt() : 0;

                String label = "";
                if ("BATCH".equalsIgnoreCase(jsonType)) {
                    if (node.has("batchCode") && !node.get("batchCode").isJsonNull()) label = node.get("batchCode").getAsString();
                } else if ("ETL".equalsIgnoreCase(jsonType)) {
                    if (node.has("stepCode") && !node.get("stepCode").isJsonNull()) label = node.get("stepCode").getAsString();
                }
                if (label.isEmpty() && node.has("stepCode") && !node.get("stepCode").isJsonNull()) label = node.get("stepCode").getAsString();
                if (label.isEmpty() && node.has("batchCode") && !node.get("batchCode").isJsonNull()) label = node.get("batchCode").getAsString();

                int width = item.getSize();
                int height = item.getNodeType() == BpmnConstants.NODETYPE.STEP ? item.getSize() / 2 : item.getSize();
                Object v = graph.insertVertex(parent, id, label, x, y, width, height, generateStyleForItem(item));
                vertexMap.put(id, v);
            }
            java.util.List<Object> edges = new ArrayList<>();
            for (JsonElement element : nodes) {
                JsonObject node = element.getAsJsonObject();
                if (!node.has("workflowNodeCode")) continue;
                String sourceId = node.get("workflowNodeCode").getAsString();
                Object sourceVertex = vertexMap.get(sourceId);
                if (sourceVertex != null && node.has("nextWorkflowNodeList")) {
                    for (JsonElement next : node.getAsJsonArray("nextWorkflowNodeList")) {
                        JsonObject nextObj = next.getAsJsonObject();
                        if (!nextObj.has("workflowNodeCode")) continue;
                        String targetId = nextObj.get("workflowNodeCode").getAsString();
                        Object targetVertex = vertexMap.get(targetId);
                        if (targetVertex != null) {
                            Object edge = graph.insertEdge(parent, null, "", sourceVertex, targetVertex, edgeStyle);
                            edges.add(edge);
                        }
                    }
                }
            }
            graph.orderCells(true, edges.toArray());
        } finally {
            graph.getModel().endUpdate();
            isRendering = false;
            graph.refresh();
        }
    }

    /**
     * Modifica el texto mostrado en el interior de un nodo especifico sin alterar el resto de sus propiedades.
     * Utiliza una transaccion segura para garantizar que el cambio se refleje visualmente de forma inmediata.
     *
     * @param nodeId Identificador unico del nodo que sera modificado.
     * @param newLabel Nuevo texto que reemplazara al actual en el nodo.
     */
    public void updateNodeLabel(String nodeId, String newLabel) {
        if (graph == null || nodeId == null) return;
        mxCell cell = findCellById(nodeId);
        if (cell != null) {
            graph.getModel().beginUpdate();
            try {
                graph.getModel().setValue(cell, newLabel);
            } finally {
                graph.getModel().endUpdate();
            }
        }
    }

    /**
     * Genera la cadena de estilos de JGraphX adecuada basandose en el tipo de elemento del enumerado proporcionado.
     * Utiliza el caché de estilos del BpmnStyleManager para evitar concatenaciones repetidas.
     *
     * @param item Elemento que describe el tipo de nodo a renderizar.
     * @return Cadena de configuracion de estilos que JGraphX puede interpretar (cacheada).
     */
    private String generateStyleForItem(BpmnConstants.BpmnPaletteItem item) {
        return BpmnStyleManager.getStyleString(item);
    }

    /**
     * Resalta visualmente un nodo dentro del diagrama pasandole el foco de seleccion.
     *
     * @param nodeId Identificador de la celda a seleccionar.
     */
    public void selectNodeById(String nodeId) {
        if (graph == null || nodeId == null) return;
        Object parent = graph.getDefaultParent();
        Object[] cells = graph.getChildCells(parent, true, true);
        for (Object c : cells) {
            mxCell cell = (mxCell) c;
            if (nodeId.equals(cell.getId())) {
                graph.setSelectionCell(cell);
                return;
            }
        }
    }

    /**
     * Dado el identificador de una conexion, recupera el identificador del nodo desde el cual se origina.
     *
     * @param edgeId Identificador de la arista.
     * @return Identificador del nodo origen, o nulo si no se puede determinar.
     */
    public String getSourceNodeId(String edgeId) {
        mxCell cell = findCellById(edgeId);
        if (cell != null && cell.isEdge()) {
            mxCell source = (mxCell) cell.getSource();
            return (source != null) ? source.getId() : null;
        }
        return null;
    }

    /**
     * Dado el identificador de una conexion, recupera el identificador del nodo al cual se dirige.
     *
     * @param edgeId Identificador de la arista.
     * @return Identificador del nodo destino, o nulo si no se puede determinar.
     */
    public String getTargetNodeId(String edgeId) {
        mxCell cell = findCellById(edgeId);
        if (cell != null && cell.isEdge()) {
            mxCell target = (mxCell) cell.getTarget();
            return (target != null) ? target.getId() : null;
        }
        return null;
    }

    /**
     * Recorre todo el modelo visual del grafo y extrae sus coordenadas, conexiones y propiedades incrustadas,
     * volcando esta informacion actualizada dentro del objeto JSON proporcionado.
     *
     * @param json Estructura de destino que sera sobreescrita o complementada con la nueva informacion del diagrama.
     */
    public void updateJsonWithGraphData(JsonObject json) {
        Object parent = graph.getDefaultParent();
        Object[] cells = graph.getChildCells(parent, true, true);
        JsonArray nodeList = new JsonArray();
        Map<String, JsonObject> existingNodes = new HashMap<>();
        Map<String, String> scriptConditionBackup = new HashMap<>();
        if (json.has("workflowNodeList")) {
            for (JsonElement el : json.getAsJsonArray("workflowNodeList")) {
                if (el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    if (obj.has("workflowNodeCode")) {
                        String id = obj.get("workflowNodeCode").getAsString();
                        existingNodes.put(id, obj);
                        if (obj.has("nextWorkflowNodeList")) {
                            for(JsonElement next : obj.getAsJsonArray("nextWorkflowNodeList")) {
                                JsonObject nextObj = next.getAsJsonObject();
                                String targetId = nextObj.get("workflowNodeCode").getAsString();
                                if(nextObj.has("scriptCondition")) scriptConditionBackup.put(id + "_" + targetId, nextObj.get("scriptCondition").getAsString());
                            }
                        }
                    }
                }
            }
        }
        for (Object c : cells) {
            mxCell cell = (mxCell) c;
            if (cell.isVertex()) {
                String id = cell.getId();
                JsonObject node = existingNodes.getOrDefault(id, new JsonObject());
                mxGeometry geo = cell.getGeometry();
                if (geo != null) {
                    node.addProperty("xPosition", (int) geo.getX());
                    node.addProperty("yPosition", (int) geo.getY());
                }
                node.addProperty("workflowNodeCode", id);
                String type;
                if (node.has("type")) {
                    type = node.get("type").getAsString();
                } else {
                    BpmnConstants.BpmnPaletteItem item = detectItemFromStyle(cell.getStyle());
                    type = mapItemToLegacyJsonType(item);
                    node.addProperty("type", type);
                }
                if ("LOOP".equals(type)) {
                    if (!node.has("initializationScript")) node.addProperty("initializationScript", "");
                    if (!node.has("exitScript")) node.addProperty("exitScript", "");
                    if (!node.has("incrementScript")) node.addProperty("incrementScript", "");
                    if (!node.has("isEndLoop")) node.addProperty("isEndLoop", false);
                }
                node.add("nextWorkflowNodeList", new JsonArray());
                node.add("joinWorkflowNodeList", new JsonArray());
                nodeList.add(node);
            }
        }
        for (Object c : cells) {
            mxCell cell = (mxCell) c;
            if (cell.isEdge()) {
                mxCell source = (mxCell) cell.getSource();
                mxCell target = (mxCell) cell.getTarget();
                if (source != null && target != null) {
                    for (JsonElement el : nodeList) {
                        JsonObject node = el.getAsJsonObject();
                        String nodeId = node.get("workflowNodeCode").getAsString();
                        if (nodeId.equals(source.getId())) {
                            JsonArray nextList = node.getAsJsonArray("nextWorkflowNodeList");
                            String condition = scriptConditionBackup.getOrDefault(source.getId() + "_" + target.getId(), "");
                            JsonObject nextObj = new JsonObject();
                            nextObj.addProperty("workflowNodeCode", target.getId());
                            nextObj.addProperty("scriptCondition", condition);
                            nextList.add(nextObj);
                        }
                        if (nodeId.equals(target.getId())) {
                            JsonArray joinList = node.getAsJsonArray("joinWorkflowNodeList");
                            boolean exists = false;
                            for(JsonElement j : joinList) {
                                if(j.getAsString().equals(source.getId())) {
                                    exists = true; break;
                                }
                            }
                            if(!exists) joinList.add(source.getId());
                        }
                    }
                }
            }
        }
        json.add("workflowNodeList", nodeList);
    }

    /**
     * Extrae y analiza la cadena de estilos de una celda para deducir a que elemento de la paleta corresponde.
     * Busca especificamente la marca asignada mediante la clave bpmnType.
     *
     * @param style Cadena de configuracion visual de la celda de JGraphX.
     * @return El item de la paleta correspondiente, o un valor de fallback predeterminado si no se encuentra coincidencia.
     */
    private BpmnConstants.BpmnPaletteItem detectItemFromStyle(String style) {
        if (style == null || style.isEmpty()) return BpmnConstants.BpmnPaletteItem.ETL_STEP;
        String key = "bpmnType=";
        int index = style.indexOf(key);
        if (index != -1) {
            int start = index + key.length();
            int end = style.indexOf(";", start);
            String typeName = (end == -1) ? style.substring(start) : style.substring(start, end);
            try {
                return BpmnConstants.BpmnPaletteItem.valueOf(typeName);
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (BpmnConstants.BpmnPaletteItem item : BpmnConstants.BpmnPaletteItem.values()) {
            if (style.contains(item.getSvgName())) return item;
        }
        return null;
    }

    /**
     * Mapea un tipo enumerado propio de la interfaz visual al nombre de tipo compatible
     * esperado por el formato de contrato JSON tradicional del backend.
     *
     * @param item Elemento de la paleta a procesar.
     * @return Cadena que representa el tipo en formato compatible para persistencia.
     */
    private String mapItemToLegacyJsonType(BpmnConstants.BpmnPaletteItem item) {
        if (item.getNodeType() == BpmnConstants.NODETYPE.INITIAL) return "INITIAL";
        if (item.getNodeType() == BpmnConstants.NODETYPE.FINAL) return "FINAL";
        if (item.getNodeType() == BpmnConstants.NODETYPE.GATEWAY) {
            if (item == BpmnConstants.BpmnPaletteItem.PARALLEL) return "PARALLEL";
            if (item == BpmnConstants.BpmnPaletteItem.EXCLUSIVE) return "EXCLUSIVE";
            if (item == BpmnConstants.BpmnPaletteItem.INCLUSIVE) return "INCLUSIVE";
            if (item == BpmnConstants.BpmnPaletteItem.LOOP) return "LOOP";
        }
        if (item == BpmnConstants.BpmnPaletteItem.BATCH_STEP) return "BATCH";
        return "ETL";
    }

    /**
     * Busca y recupera la instancia de una celda interna del motor del grafo a partir de su identificador textual.
     *
     * @param id Cadena unica que identifica la celda buscada.
     * @return La celda coincidente, o nulo si no existe en el nivel superior del grafo.
     */
    private mxCell findCellById(String id) {
        if (graph == null || id == null) return null;
        Object parent = graph.getDefaultParent();
        Object[] cells = graph.getChildCells(parent, true, true);
        for (Object c : cells) {
            if (id.equals(((mxCell) c).getId())) return (mxCell) c;
        }
        return null;
    }
}