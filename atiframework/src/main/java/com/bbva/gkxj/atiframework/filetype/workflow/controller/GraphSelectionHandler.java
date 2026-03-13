package com.bbva.gkxj.atiframework.filetype.workflow.controller;

import com.bbva.gkxj.atiframework.filetype.workflow.editor.PropertiesPanel;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;

/**
 * Gestor encargado de escuchar y procesar los eventos de selección dentro del lienzo del grafo.
 * <p>
 * Actúa como un puente entre el modelo del grafo y la interfaz de usuario (panel derecho).
 * Su principal responsabilidad es detectar qué elemento ha seleccionado el usuario (un nodo,
 * una flecha, o nada) y solicitar al {@link PropertiesPanel} que muestre los datos correspondientes.
 */
public class GraphSelectionHandler {

    /** Referencia al modelo principal del grafo. */
    private final mxGraph graph;

    /** Gestor de datos global para acceder a la información del workflow. */
    private final WorkflowDataManager dataManager;

    /** Panel visual de la interfaz donde se muestran y editan los atributos del elemento seleccionado. */
    private PropertiesPanel propertiesPanel;

    /**
     * Constructor del gestor de selecciones.
     * Inicializa las referencias y registra el listener de selección en el modelo del grafo.
     *
     * @param graph       El grafo sobre el cual se escucharán los eventos de clic/selección.
     * @param dataManager El gestor de datos para extraer la configuración global cuando sea necesario.
     */
    public GraphSelectionHandler(mxGraph graph, WorkflowDataManager dataManager) {
        this.graph = graph;
        this.dataManager = dataManager;
        setupSelectionListener();
    }

    /**
     * Vincula el panel lateral de propiedades con este gestor.
     * Si el panel no es nulo, también le inyecta la referencia del grafo para que pueda interactuar con él.
     *
     * @param panel El componente {@link PropertiesPanel} de la interfaz.
     */
    public void setPropertiesPanel(PropertiesPanel panel) {
        this.propertiesPanel = panel;
        if (this.propertiesPanel != null) {
            this.propertiesPanel.setGraph(this.graph);
        }
    }

    /**
     * Fuerza al panel de propiedades a mostrar la configuración global del workflow
     * (por ejemplo, cuando no hay ningún elemento seleccionado en el lienzo).
     * <p>
     * Crea una celda virtual (mock) que envuelve los datos globales para mantener
     * la compatibilidad con el método de pintado del panel.
     */
    public void showGlobalProperties() {
        if (propertiesPanel == null) return;

        // Creamos una celda "ficticia" para pasar los datos globales al panel
        mxCell globalCell = new mxCell(dataManager.getGlobalData());
        propertiesPanel.showGlobalProperties(globalCell);
    }

    /**
     * Configura el listener central que se dispara cada vez que cambia el modelo de selección de mxGraph.
     * <p>
     * Lógica de evaluación:
     * <ul>
     * <li><b>1 Nodo (Vertex):</b> Muestra las propiedades del nodo e inyecta la lista de ejecutores globales.</li>
     * <li><b>1 Flecha (Edge):</b> Muestra las propiedades de la conexión e identifica el tipo de nodo de origen.</li>
     * <li><b>Nada o Selección múltiple:</b> Muestra las propiedades globales del Workflow.</li>
     * </ul>
     */
    private void setupSelectionListener() {
        graph.getSelectionModel().addListener(mxEvent.CHANGE, (sender, evt) -> {
            // Si la UI aún no ha cargado el panel, ignoramos el evento
            if (propertiesPanel == null) return;

            // Usamos invokeLater para asegurar que la actualización de UI se hace en el hilo de eventos (EDT)
            SwingUtilities.invokeLater(() -> {

                // Solo evaluamos propiedades individuales si hay exactamente UN elemento seleccionado
                if (graph.getSelectionCount() == 1) {
                    Object cell = graph.getSelectionCell();

                    // CASO 1: Se ha seleccionado una caja (Nodo)
                    if (cell instanceof mxCell && ((mxCell) cell).isVertex()) {
                        WorkflowJsonData nodeData = (WorkflowJsonData) ((mxCell) cell).getValue();

                        // Inyectamos los ejecutores globales en el nodo para que los combos del panel los vean
                        nodeData.setTaskExecutors(dataManager.getGlobalData().getTaskExecutors());
                        propertiesPanel.showNodeProperties((mxCell) cell);

                        // CASO 2: Se ha seleccionado una conexión (Flecha)
                    } else if (cell instanceof mxCell && ((mxCell) cell).isEdge()) {
                        Object val = ((mxCell) cell).getValue();

                        if (val instanceof WorkflowJsonData.EdgeData) {
                            mxCell edgeCell = (mxCell) cell;

                            // Determinamos de qué tipo de nodo proviene esta flecha para validaciones
                            if (edgeCell.getSource() != null && edgeCell.getSource().getValue() instanceof WorkflowJsonData) {
                                WorkflowJsonData sourceNode = (WorkflowJsonData) edgeCell.getSource().getValue();
                                ((WorkflowJsonData.EdgeData) val).setSourceType(sourceNode.getType());
                            }
                            propertiesPanel.showNodeProperties(edgeCell);
                        } else {
                            // Si la flecha no tiene nuestros datos personalizados (raro), mostramos global
                            showGlobalProperties();
                        }

                        // CASO 3: Es otro tipo de celda no contemplada
                    } else {
                        showGlobalProperties();
                    }

                    // CASO 4: Ningún elemento seleccionado o selección múltiple (>1)
                } else {
                    showGlobalProperties();
                }
            });
        });
    }
}