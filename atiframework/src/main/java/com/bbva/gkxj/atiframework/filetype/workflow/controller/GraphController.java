package com.bbva.gkxj.atiframework.filetype.workflow.controller;

import com.bbva.gkxj.atiframework.filetype.batch.editor.utils.EditorKeyboardHandler;
import com.bbva.gkxj.atiframework.filetype.workflow.editor.PropertiesPanel;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.view.mxGraph;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mxgraph.model.mxCell;

import javax.swing.*;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Controlador principal y fachada (Facade) del editor de grafos.
 * <p>
 * Esta clase actúa como el orquestador central que inicializa y coordina los diferentes
 * sub-controladores especializados (Manejo de Datos, Interacción, Selección y Drag & Drop).
 * Expone métodos de alto nivel para interactuar con el grafo sin exponer la complejidad interna
 * de la librería JGraphX (mxGraph).
 * </p>
 */
public class GraphController {

    /** Componente visual principal de Swing que renderiza el lienzo del grafo. */
    private final mxGraphComponent graphComponent;

    /** Modelo de datos principal del grafo subyacente. */
    private final mxGraph graph;

    /** Componente visual que representa el minimapa (vista general y navegación rápida). */
    protected mxGraphOutline graphOutline;

    /** Gestor del historial de acciones para permitir Deshacer (Undo) y Rehacer (Redo). */
    private final mxUndoManager undoManager;

    /** Contexto del proyecto de IntelliJ actualmente activo. */
    private final Project project;

    /** Archivo virtual del IDE que representa el flujo de trabajo (workflow) actualmente abierto. */
    private final VirtualFile virtualFile;

    /** Bandera que indica si el grafo ha sufrido modificaciones no guardadas. */
    private boolean setModified;

    /** Tipo de canal o arista activa por defecto al crear nuevas conexiones manuales (ej. "DIRECT"). */
    private String currentEdgeType = "DIRECT";

    // --- Sub-Controladores especializados ---

    /** Gestor encargado de la persistencia, exportación/importación de JSON y creación de nodos. */
    private final WorkflowDataManager dataManager;

    /** Gestor encargado de las herramientas del ratón (paneo, selección, conexión) y el zoom. */
    private final GraphInteractionHandler interactionHandler;

    /** Gestor encargado de escuchar los eventos de selección en el lienzo y actualizar el panel de propiedades. */
    private final GraphSelectionHandler selectionHandler;

    /** Gestor encargado de manejar los eventos de arrastrar y soltar (Drag and Drop) desde la paleta de componentes. */
    private final WorkflowTransferHandler transferHandler;

    /** Controlador de la interfaz flotante (Context Pad) para conexiones y creación rápida adyacente a los nodos. */
    private final WorkflowContextPadController contextPadController;

    /**
     * Interfaz listener para escuchar y reaccionar a los cambios producidos en el modelo del grafo.
     */
    public interface GraphChangeListener {
        /**
         * Método invocado cuando se detecta un cambio estructural o visual en el grafo
         * (movimiento, adición, eliminación de nodos o aristas, etc.).
         */
        void onGraphChanged();
    }

    /** Listener registrado actualmente para recibir las notificaciones de cambios en el grafo. */
    private GraphChangeListener graphChangeListener;

    /**
     * Establece el oyente (listener) que escuchará los cambios realizados sobre el grafo.
     *
     * @param listener El oyente a registrar.
     */
    public void setGraphChangeListener(GraphChangeListener listener) {
        this.graphChangeListener = listener;
    }

    /**
     * Constructor del controlador principal.
     * Inicializa las configuraciones básicas del lienzo, aplica restricciones de movimiento
     * y levanta todos los sub-controladores inyectando las dependencias requeridas.
     *
     * @param graphComponent El componente visual que contiene el grafo.
     * @param undoManager    El gestor del historial de acciones (deshacer/rehacer).
     * @param project        El proyecto de IntelliJ (necesario para abrir modales o nuevos archivos).
     * @param virtualFile    El archivo virtual sobre el que se está trabajando.
     */
    public GraphController(mxGraphComponent graphComponent, mxUndoManager undoManager, Project project, VirtualFile virtualFile) {
        this.graphComponent = graphComponent;
        this.graph = graphComponent.getGraph();
        this.undoManager = undoManager;
        this.project = project;
        this.virtualFile = virtualFile;
        this.graphOutline = new mxGraphOutline(graphComponent);

        // Configuraciones base del lienzo y el grafo
        graphComponent.setAntiAlias(true);
        this.graph.setCellsEditable(false);
        this.graph.setHtmlLabels(true);
        this.graph.setLabelsVisible(true);
        this.graph.setAllowDanglingEdges(false);
        this.graph.setAllowNegativeCoordinates(false);
        this.graph.setBorder(80);

        // 1. Inicializar Gestor de Datos
        this.dataManager = new WorkflowDataManager(graphComponent);
        mxRubberband rubberband = new mxRubberband(graphComponent);
        new EditorKeyboardHandler(graphComponent);

        // 2. Inicializar Context Pad inyectando la lógica de apertura/creación de archivos
        this.contextPadController = new WorkflowContextPadController(graphComponent, dataManager, (mxCell cellToEdit) -> {
            WorkflowJsonData nodeData = null;
            if (cellToEdit.getValue() instanceof WorkflowJsonData) {
                nodeData = (WorkflowJsonData) cellToEdit.getValue();
            }

            if (nodeData == null || nodeData.getId() == null) return;

            VirtualFile parentDir = this.virtualFile.getParent();

            // Usamos ".wf" para sub-workflows, y ".comp" para el resto de nodos
            String extension = "Subworkflow".equalsIgnoreCase(nodeData.getType()) ? ".wf" : ".comp";
            String fileName = nodeData.getId() + extension;

            VirtualFile existingFile = parentDir.findChild(fileName);

            if (existingFile != null && existingFile.isValid()) {
                FileEditorManager.getInstance(this.project).openFile(existingFile, true);
            } else {
                createAndOpenNodeFile(parentDir, fileName, nodeData);
            }
        });

        // 3. Configuración de comportamiento del lienzo
        this.graphComponent.getConnectionHandler().setEnabled(true);
        this.graphComponent.setDragEnabled(false);
        this.graphComponent.setPageVisible(false);
        this.graphComponent.setAutoExtend(true);
        this.graphComponent.setAutoScroll(true);

        // 4. Inicializar Handlers interactivos
        this.interactionHandler = new GraphInteractionHandler(graphComponent, contextPadController, rubberband);
        this.selectionHandler = new GraphSelectionHandler(graph, dataManager);
        this.transferHandler = new WorkflowTransferHandler(graphComponent, dataManager);

        setupBaseListeners();
        changeToolMode("NODE");
    }

    /**
     * Crea un nuevo archivo virtual con una estructura JSON inicial y lo abre en el editor de IntelliJ.
     * Esta operación se realiza de forma segura a través de una transacción del IDE (WriteCommandAction).
     *
     * @param parentDir El directorio padre donde se creará el archivo.
     * @param fileName  El nombre que tendrá el archivo (incluyendo extensión).
     * @param nodeData  Los datos del nodo que se utilizarán para poblar la estructura base del archivo.
     */
    private void createAndOpenNodeFile(VirtualFile parentDir, String fileName, WorkflowJsonData nodeData) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                VirtualFile newFile = parentDir.createChildData(this, fileName);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // 1. CLONAR EL NODO para no afectar la instancia visual del grafo principal
                String originalJson = gson.toJson(nodeData);
                WorkflowJsonData fileData = gson.fromJson(originalJson, WorkflowJsonData.class);

                // 2. LIMPIAR DATOS DE INTERFAZ
                fileData.setX(null);
                fileData.setY(null);

                // --- NUEVO: LIMPIAR LISTAS GLOBALES PARA EL NUEVO ARCHIVO ---
                // Esto evita que el sub-componente herede la configuración del padre
                fileData.setTaskExecutors(null);
                fileData.setEnvironmentVariables(null);

                // 3. SOBRESCRIBIR EL WORKFLOW CODE CON EL COMPONENT CODE
                String newCode = fileData.getComponentCode();
                if (newCode == null || newCode.trim().isEmpty()) {
                    newCode = fileData.getId();
                }
                fileData.setWorkflowCode(newCode);

                // 4. PREPARAR ESTRUCTURA SEGÚN TIPO
                if ("Subworkflow".equalsIgnoreCase(fileData.getType())) {
                    if (fileData.getNodeList() == null) {
                        fileData.setNodeList(new ArrayList<>());
                    }
                    if (fileData.getEdgeList() == null) {
                        fileData.setEdgeList(new ArrayList<>());
                    }
                }

                // 5. GENERAR JSON FINAL Y GUARDAR
                String finalJsonContent = gson.toJson(fileData);
                VfsUtil.saveText(newFile, finalJsonContent);

                // 6. ABRIR EL ARCHIVO EN EL EDITOR
                ApplicationManager.getApplication().invokeLater(() -> {
                    FileEditorManager.getInstance(project).openFile(newFile, true);
                });

            } catch (IOException e) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    Messages.showErrorDialog(project,
                            "No se pudo crear el archivo del nodo: " + e.getMessage(),
                            "Error de Creación");
                });
            }
        });
    }
    /**
     * Configura los listeners base del controlador.
     * Rastrea modificaciones en el modelo para disparar el evento de cambio y
     * maneja la rueda del ratón (scroll) para aplicar acercamiento o alejamiento (zoom).
     */
    private void setupBaseListeners() {
        mxIEventListener changeTracker = (source, evt) -> {
            setModified = true;
            if (graphChangeListener != null) graphChangeListener.onGraphChanged();
        };
        graph.getModel().addListener(mxEvent.CHANGE, changeTracker);

        MouseWheelListener wheelTracker = e -> {
            if (e.getSource() instanceof com.mxgraph.swing.mxGraphOutline || e.isControlDown()) {
                if (e.getWheelRotation() < 0) graphComponent.zoomIn();
                else graphComponent.zoomOut();
            }
        };
        graphOutline.addMouseWheelListener(wheelTracker);
        graphComponent.addMouseWheelListener(wheelTracker);
    }

    /**
     * Configura los oyentes que mantienen sincronizado el minimapa (Outline) con
     * los repintados, rehaceres/deshaceres y cambios estructurales del grafo principal.
     *
     * @param layeredContainer El contenedor UI de capas donde se encuentra renderizado el minimapa.
     * @param outlineWidth     El ancho asignado al minimapa.
     * @param outlineHeight    La altura asignada al minimapa.
     */
    public void setupMinimapListeners(JLayeredPane layeredContainer, int outlineWidth, int outlineHeight) {
        mxIEventListener refreshAction = (sender, evt) -> SwingUtilities.invokeLater(() -> {
            if (graphOutline != null) {
                layeredContainer.revalidate();
                graph.repaint();
                layeredContainer.repaint();
            }
        });
        graph.getModel().addListener(mxEvent.CHANGE, refreshAction);
        graph.getView().addListener(mxEvent.UNDO, refreshAction);
        graph.getView().addListener(mxEvent.REPAINT, refreshAction);
    }

    /**
     * Vincula el panel de propiedades global/lateral al gestor de selecciones.
     * Le inyecta el grafo actual y el DataManager para que pueda reflejar la información
     * correcta al seleccionar un nodo o el propio lienzo.
     *
     * @param panel El panel de propiedades a actualizar.
     */
    public void setPropertiesPanel(PropertiesPanel panel) {
        panel.setGraph(this.graph);
        panel.setDataManager(this.dataManager);
        selectionHandler.setPropertiesPanel(panel);
    }

    /**
     * Cambia el modo de herramienta activa del editor (por ejemplo: "HAND" para paneo de vista,
     * "NODE" para modo de selección regular).
     * Delega esta responsabilidad visual al {@link GraphInteractionHandler}.
     *
     * @param toolId El identificador de la herramienta a activar.
     */
    public void changeToolMode(String toolId) {
        interactionHandler.changeToolMode(toolId);
    }

    /**
     * Deserializa y carga un modelo de datos en el lienzo, generando los vértices y aristas visuales.
     * Delega la lógica de construcción al {@link WorkflowDataManager}. Al finalizar,
     * enfoca el panel en las propiedades globales.
     *
     * @param jsonData El objeto con los datos del workflow a renderizar.
     */
    public void loadGraphFromData(WorkflowJsonData jsonData) {
        dataManager.loadGraphFromData(jsonData, () -> selectionHandler.showGlobalProperties());
    }

    /**
     * Recorre la topología actual del grafo visual y la serializa en un modelo de datos estructurado.
     * Delega la extracción al {@link WorkflowDataManager}.
     *
     * @return El objeto {@link WorkflowJsonData} actualizado con las coordenadas, nodos y conexiones actuales.
     */
    public WorkflowJsonData exportGraphToJsonData() {
        return dataManager.exportGraphToJsonData();
    }

    /**
     * Define el tipo de canal o arista que se creará de manera predeterminada
     * cuando el usuario trace una nueva línea de conexión manual.
     *
     * @param type El tipo de conexión (ej: "DIRECT", "DISCARD", "QUEUE").
     */
    public void setActiveEdgeStyle(String type) {
        this.currentEdgeType = type;
    }
}