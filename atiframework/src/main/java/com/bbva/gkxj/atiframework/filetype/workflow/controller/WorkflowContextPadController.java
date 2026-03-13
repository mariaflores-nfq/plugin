package com.bbva.gkxj.atiframework.filetype.workflow.controller;

import com.bbva.gkxj.atiframework.filetype.workflow.editor.WorkflowContextPadView;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.intellij.icons.AllIcons;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxConnectPreview;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxCellState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Controlador de la interfaz interactiva o "Context Pad" que aparece al seleccionar un nodo.
 * <p>
 * Gestiona la visualización de botones flotantes cerca del nodo seleccionado y maneja la lógica
 * para iniciar conexiones manuales y la inserción rápida de nodos.
 * Funciona como intermediario entre la vista del pad ({@link WorkflowContextPadView}),
 * el lienzo del grafo ({@link mxGraphComponent}) y el gestor de datos ({@link WorkflowDataManager}).
 * <p>
 * Para la edición de nodos, delega la acción a un {@link Consumer} inyectado, desacoplando
 * así la vista gráfica de la lógica de apertura de archivos del IDE.
 * </p>
 */
public class WorkflowContextPadController implements WorkflowContextPadView.PadListener {

    /** Componente visual de Swing que contiene el grafo interactivo. */
    private final mxGraphComponent graphComponent;

    /** La vista visual (el panel flotante) del context pad. */
    private final WorkflowContextPadView view;

    /** Gestor encargado de las operaciones de modificación del modelo del grafo. */
    private final WorkflowDataManager dataManager;

    /** Acción a ejecutar al pulsar el botón de configuración. Recibe la celda (nodo) a editar. */
    private final Consumer<mxCell> onConfigurationAction;

    /** Celda (nodo) actualmente seleccionada en el lienzo. */
    private mxCell currentCell = null;

    /** Indica si el usuario está actualmente arrastrando para crear una conexión manual. */
    private boolean isConnectingMode = false;

    /** Tipo de canal o arista que se creará al finalizar la conexión (ej. "DIRECT", "DISCARD"). */
    private String pendingEdgeType = null;

    /** Utilidad de mxGraph para renderizar una línea temporal mientras se arrastra una conexión. */
    private mxConnectPreview connectionPreview;

    /** Indica si la previsualización de la conexión (línea dinámica) está activa. */
    private boolean isPreviewActive = false;

    /** Cursor mostrado cuando se intenta conectar a un nodo no válido. */
    private final Cursor prohibitedCursor;

    /** Cursor mostrado cuando el usuario está en modo de conexión activa. */
    private final Cursor connectionCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

    /**
     * Constructor del controlador del Context Pad.
     *
     * @param graphComponent        El lienzo interactivo del grafo.
     * @param dataManager           El gestor encargado de insertar nodos y conexiones reales en el modelo.
     * @param onConfigurationAction Función delegada que se ejecutará al solicitar la edición de un nodo.
     */
    public WorkflowContextPadController(mxGraphComponent graphComponent, WorkflowDataManager dataManager, Consumer<mxCell> onConfigurationAction) {
        this.graphComponent = graphComponent;
        this.dataManager = dataManager;
        this.onConfigurationAction = onConfigurationAction;

        this.view = new WorkflowContextPadView(this, graphComponent.getGraphControl());
        this.graphComponent.getGraphControl().add(view, 0);

        this.connectionPreview = new mxConnectPreview(graphComponent);
        this.prohibitedCursor = createProhibitedCursor();

        initListeners();
    }

    /**
     * Crea un cursor personalizado utilizando un icono de advertencia de IntelliJ para
     * indicar visualmente que una conexión arrastrada hacia un destino no está permitida.
     *
     * @return El objeto Cursor modificado con el icono de advertencia.
     */
    private Cursor createProhibitedCursor() {
        Icon icon = AllIcons.General.BalloonWarning;
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(new JPanel(), g2, 8, 8);
        g2.dispose();
        return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(16, 16), "ProhibitedCursor");
    }

    /**
     * Inicializa todos los oyentes de eventos necesarios (ratón, scroll, selección del grafo)
     * para reaccionar a la interacción del usuario.
     */
    private void initListeners() {
        setupViewSyncListeners();
        setupConnectionMotionListener();
        setupConnectionClickListener();
    }

    /**
     * Configura los oyentes que mantienen el context pad sincronizado visualmente con el grafo
     * (ocultándose al hacer scroll o actualizando su posición al hacer paneo).
     */
    private void setupViewSyncListeners() {
        graphComponent.getGraph().getView().addListener(mxEvent.REPAINT, (s, e) -> repositionView());
        graphComponent.getGraph().getSelectionModel().addListener(mxEvent.CHANGE, (s, e) -> updateSelection());
        graphComponent.getGraphControl().addMouseWheelListener(e -> hideView());
    }

    /**
     * Configura el evento de ratón que captura el momento en que el usuario suelta el clic (MouseReleased)
     * después de haber arrastrado una conexión, confirmando e insertando la arista si es válida.
     */
    private void setupConnectionClickListener() {
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isConnectingMode) return;
                String typeToCreate = pendingEdgeType;

                if (isPreviewActive) {
                    connectionPreview.stop(false);
                    isPreviewActive = false;
                }

                Object targetCell = graphComponent.getCellAt(e.getX(), e.getY());
                if (targetCell instanceof mxCell && ((mxCell) targetCell).isVertex()) {
                    mxCell target = (mxCell) targetCell;
                    if (canConnect(currentCell, target)) {
                        dataManager.connectNodes(currentCell, target, typeToCreate);
                    }
                }

                resetConnectingState();
                e.consume();
                graphComponent.refresh();
            }
        });
    }

    /**
     * Responde a la acción de edición solicitada desde la vista del Context Pad.
     * Delega la lógica de negocio (como abrir el archivo de configuración nativo en el IDE)
     * al consumidor inyectado en el constructor.
     */
    @Override
    public void onEditRequested() {
        if (currentCell == null) return;

        // Ocultamos el pad de botones para que no moleste visualmente
        hideView();

        // Delegamos la acción de abrir el archivo a la clase que instanció este controlador
        if (onConfigurationAction != null) {
            onConfigurationAction.accept(currentCell);
        }

        // Restauramos el pad al finalizar si la celda sigue seleccionada
        updateSelection();
    }

    /**
     * Registra eventos de movimiento y arrastre del ratón para actualizar en tiempo real
     * la previsualización de la línea de conexión (flecha dinámica).
     */
    private void setupConnectionMotionListener() {
        MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { handlePreview(e); }
            @Override
            public void mouseDragged(MouseEvent e) { handlePreview(e); }
        };
        graphComponent.getGraphControl().addMouseMotionListener(motionAdapter);
    }

    /**
     * Evalúa la posición del ratón e inicia o actualiza la previsualización de la arista.
     *
     * @param e Evento de ratón que contiene las coordenadas actuales.
     */
    private void handlePreview(MouseEvent e) {
        if (!isConnectingMode || currentCell == null) return;
        if (!isPreviewActive) startConnectionPreview(e);
        if (isPreviewActive) updateConnectionPreview(e);
    }

    /**
     * Activa el renderizado temporal de la flecha de conexión utilizando la API interna de mxGraph.
     *
     * @param e Evento de ratón.
     */
    private void startConnectionPreview(MouseEvent e) {
        mxCellState state = graphComponent.getGraph().getView().getState(currentCell);
        if (state != null) {
            connectionPreview.start(e, state, WorkFlowStyles.getStyleForEdge(pendingEdgeType));
            isPreviewActive = true;
        }
    }

    /**
     * Actualiza el final de la flecha dinámica de conexión según la posición del cursor,
     * y cambia el icono del cursor si sobrevuela un objetivo no válido.
     *
     * @param e Evento de ratón continuo (drag/move).
     */
    private void updateConnectionPreview(MouseEvent e) {
        Object target = graphComponent.getCellAt(e.getX(), e.getY());
        boolean valid = true;
        mxCellState targetState = null;

        if (target instanceof mxCell && ((mxCell) target).isVertex()) {
            if (target != currentCell) {
                valid = canConnect(currentCell, (mxCell) target);
                if (valid) {
                    targetState = graphComponent.getGraph().getView().getState(target);
                }
            } else {
                valid = false;
            }
        }

        graphComponent.getGraphControl().setCursor(valid ? connectionCursor : prohibitedCursor);
        connectionPreview.update(e, targetState, e.getX(), e.getY());
    }

    /**
     * Lógica de negocio estructural que determina si dos nodos pueden conectarse entre sí.
     * Aplica reglas de validación como topología de filtros (solo 2 salidas admitidas),
     * nodos de entrada (no pueden recibir conexiones), etc.
     *
     * @param sourceNode Nodo de origen.
     * @param targetNode Nodo de destino (puede ser nulo si solo se está verificando disponibilidad genérica).
     * @return {@code true} si la conexión cumple todas las reglas de negocio; de lo contrario, {@code false}.
     */
    private boolean canConnect(mxCell sourceNode, mxCell targetNode) {
        if (sourceNode == null) return false;
        if (sourceNode == targetNode) return false;

        String sourceType = getComponentType(sourceNode);
        int outCount = countRealEdges(graphComponent.getGraph().getOutgoingEdges(sourceNode));

        if (sourceType.contains("OUTPUT") || sourceType.contains("DISCARD")) return false;

        if (!sourceType.contains("ROUTER")) {
            if (sourceType.contains("FILTER")) {
                if (outCount >= 2) return false;
            } else {
                if (outCount >= 1) return false;
            }
        }

        if (targetNode != null) {
            String targetType = getComponentType(targetNode);
            if (targetType.contains("INPUT")) return false;

            if (sourceType.contains("FILTER")) {
                if (isChannelAlreadyUsed(sourceNode, pendingEdgeType)) return false;
                if (("DIRECT".equals(pendingEdgeType) || "QUEUE".equals(pendingEdgeType)) &&
                        (isChannelAlreadyUsed(sourceNode, "DIRECT") || isChannelAlreadyUsed(sourceNode, "QUEUE"))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Evalúa si un determinado tipo de canal (ej. DIRECT, DISCARD) está todavía
     * disponible para ser utilizado desde la celda actual.
     *
     * @param channelType Tipo del canal lógico a verificar.
     * @return {@code true} si la celda permite una conexión extra de dicho tipo.
     */
    @Override
    public boolean isChannelAvailable(String channelType) {
        if (currentCell == null) return false;
        String type = getComponentType(currentCell);

        if (!canConnect(currentCell, null)) return false;

        if (type.contains("FILTER")) {
            boolean hasData = isChannelAlreadyUsed(currentCell, "DIRECT") || isChannelAlreadyUsed(currentCell, "QUEUE");
            boolean hasDiscard = isChannelAlreadyUsed(currentCell, "DISCARD");

            if ("DISCARD".equals(channelType)) return !hasDiscard;
            if ("DIRECT".equals(channelType) || "QUEUE".equals(channelType)) return !hasData;
        }
        return true;
    }

    /**
     * Utilitario que cuenta cuántas aristas (edges) de datos reales salen de un nodo,
     * ignorando líneas temporales o artefactos gráficos.
     *
     * @param edges Matriz de objetos que mxGraph reporta como conexiones salientes.
     * @return El número de aristas válidas vinculadas al modelo {@link WorkflowJsonData.EdgeData}.
     */
    private int countRealEdges(Object[] edges) {
        if (edges == null) return 0;
        int count = 0;
        for (Object edge : edges) {
            if (edge instanceof mxCell && ((mxCell) edge).isEdge()) {
                if (((mxCell) edge).getValue() instanceof WorkflowJsonData.EdgeData) count++;
            }
        }
        return count;
    }

    /**
     * Verifica si el nodo ya tiene una conexión saliente usando un tipo de canal específico.
     * Útil para asegurar que un 'Filter' no tenga dos conexiones 'DISCARD', por ejemplo.
     *
     * @param source      El nodo origen a evaluar.
     * @param channelType El tipo de conexión (ej. "DIRECT").
     * @return {@code true} si ya existe una salida con ese canal asignado.
     */
    private boolean isChannelAlreadyUsed(mxCell source, String channelType) {
        if (channelType == null) return false;
        Object[] outEdges = graphComponent.getGraph().getOutgoingEdges(source);
        if (outEdges == null) return false;

        for (Object edge : outEdges) {
            Object val = ((mxCell) edge).getValue();
            if (val instanceof WorkflowJsonData.EdgeData) {
                if (channelType.equals(((WorkflowJsonData.EdgeData) val).getChannelType())) return true;
            }
        }
        return false;
    }

    /**
     * Reinicia todas las banderas de estado internas que marcan el modo de conexión activa
     * y restaura el cursor predeterminado del sistema.
     */
    private void resetConnectingState() {
        isConnectingMode = false;
        pendingEdgeType = null;
        graphComponent.getGraphControl().setCursor(Cursor.getDefaultCursor());
        updateSelection();
    }

    /**
     * Obtiene y normaliza el tipo de componente definido en el objeto de negocio de un nodo.
     *
     * @param cell El nodo visual a consultar.
     * @return El tipo de componente en mayúsculas (ej. "FILTER"), o cadena vacía si no existe.
     */
    private String getComponentType(mxCell cell) {
        if (cell != null && cell.getValue() instanceof WorkflowJsonData) {
            String type = ((WorkflowJsonData) cell.getValue()).getType();
            return (type != null) ? type.toUpperCase().trim() : "";
        }
        return "";
    }

    /**
     * Evalúa qué celda está actualmente seleccionada en el grafo y muestra u oculta
     * el pad interactivo en consecuencia, configurando qué botones están habilitados.
     */
    private void updateSelection() {
        if (isConnectingMode) return;

        Object cell = graphComponent.getGraph().getSelectionCell();
        if (cell instanceof mxCell && ((mxCell) cell).isVertex()) {
            currentCell = (mxCell) cell;
            String type = getComponentType(currentCell);
            boolean canConnectMore = canConnect(currentCell, null);
            view.updateButtonsForType(type, canConnectMore);
            view.setVisible(true);
            repositionView();
        } else {
            hideView();
        }
    }

    /**
     * Maneja el evento disparado desde la vista cuando el usuario presiona un botón
     * de "Conectar" en el pad. Establece el estado para que inicie el modo de dragado.
     *
     * @param channelType El tipo de la conexión a previsualizar y crear.
     */
    @Override
    public void onConnectionModeStarted(String channelType) {
        if (currentCell == null) return;
        this.pendingEdgeType = channelType;
        this.isConnectingMode = true;
        hideView();
        graphComponent.getGraphControl().setCursor(connectionCursor);
    }

    /**
     * Maneja el evento disparado desde la vista cuando el usuario presiona un botón
     * de "Inserción Rápida". Delega al {@code dataManager} para crear y enlazar un nodo nuevo.
     *
     * @param type El estilo y naturaleza del nuevo nodo a crear.
     */
    @Override
    public void onQuickNodeInsert(WorkFlowStyles.WfNodeType type) {
        if (currentCell != null && canConnect(currentCell, null)) {
            String channelToUse = "DIRECT";

            if (getComponentType(currentCell).contains("FILTER")) {
                boolean hasData = isChannelAlreadyUsed(currentCell, "DIRECT") || isChannelAlreadyUsed(currentCell, "QUEUE");
                if (hasData) channelToUse = "DISCARD";
            }

            dataManager.insertNodeAndConnect(currentCell, type, channelToUse, false);
            hideView();
        }
    }

    /**
     * Ejecuta el borrado inmediato del nodo seleccionado actualmente y retira el pad de la vista.
     */
    @Override
    public void onDeleteRequested() {
        if (currentCell != null) {
            graphComponent.getGraph().removeCells(new Object[]{currentCell});
            hideView();
            currentCell = null;
        }
    }

    /**
     * Recalcula la posición del Context Pad para que siempre flote en la esquina
     * superior derecha (con un ligero offset) respecto al nodo actualmente seleccionado.
     */
    private void repositionView() {
        if (currentCell == null || !view.isVisible()) return;
        mxCellState state = graphComponent.getGraph().getView().getState(currentCell);
        if (state == null) return;
        view.setLocation((int) (state.getX() + state.getWidth() + 10), (int) Math.max(state.getY(), 0));
        view.setSize(view.getPreferredSize());
    }

    /**
     * Oculta de manera segura el Context Pad de la vista del usuario.
     */
    private void hideView() { view.setVisible(false); }

    /**
     * Expone una API pública para poder iniciar de forma programática el modo
     * de conexión manual desde un origen dado, sin depender de un clic en el pad.
     *
     * @param sourceNode  El nodo raíz desde donde arranca la flecha.
     * @param channelType El canal que define la naturaleza de la conexión.
     */
    public void startConnectionFrom(mxCell sourceNode, String channelType) {
        if (sourceNode != null && sourceNode.isVertex()) {
            this.currentCell = sourceNode;
            if (isChannelAvailable(channelType)) {
                onConnectionModeStarted(channelType);
            }
        }
    }

    /**
     * Fuerza la cancelación programática del estado de conexión activa,
     * limpiando cursores y previsualizaciones residuales en el lienzo.
     */
    public void cancelConnectionMode() {
        if (isConnectingMode) {
            resetConnectingState();
            if (isPreviewActive) {
                connectionPreview.stop(false);
                isPreviewActive = false;
            }
        }
    }
}