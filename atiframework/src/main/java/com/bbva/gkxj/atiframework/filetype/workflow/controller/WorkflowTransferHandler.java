package com.bbva.gkxj.atiframework.filetype.workflow.controller;

import com.mxgraph.swing.mxGraphComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;

/**
 * Gestor encargado de manejar las operaciones de arrastrar y soltar (Drag and Drop)
 * desde la paleta de componentes hacia el lienzo del grafo.
 * <p>
 * Esta clase escucha los eventos de soltado (drop) en el componente del grafo,
 * extrae el tipo de nodo transportado y calcula las coordenadas reales en el lienzo
 * teniendo en cuenta el factor de escala (zoom) y el desplazamiento (scroll) actual.
 * Finalmente, delega la creación del nodo al {@link WorkflowDataManager}.
 */
public class WorkflowTransferHandler {

    /** Componente visual del grafo que actúa como receptor del drop. */
    private final mxGraphComponent graphComponent;

    /** Gestor de datos al que se le solicita la inserción física del nuevo nodo. */
    private final WorkflowDataManager dataManager;

    /**
     * Constructor del gestor de transferencias.
     * Inicializa el objetivo de soltado (DropTarget) sobre el control del grafo.
     *
     * @param graphComponent El componente visual de mxGraph.
     * @param dataManager    El gestor encargado de procesar la lógica de inserción de datos.
     */
    public WorkflowTransferHandler(mxGraphComponent graphComponent, WorkflowDataManager dataManager) {
        this.graphComponent = graphComponent;
        this.dataManager = dataManager;
        setupDragAndDrop();
    }

    /**
     * Configura el componente del grafo para que sea capaz de aceptar elementos
     * arrastrados desde otros componentes de la interfaz.
     */
    private void setupDragAndDrop() {
        // Registramos un nuevo DropTarget en el control del grafo (donde se dibujan los nodos)
        new DropTarget(graphComponent.getGraphControl(), new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                handleDropEvent(dtde);
            }
        });
    }

    /**
     * Procesa el evento de soltado del ratón.
     * <p>
     * El proceso sigue estos pasos:
     * <ol>
     * <li>Verifica si los datos arrastrados son de tipo texto (String).</li>
     * <li>Acepta la transferencia de datos.</li>
     * <li>Traduce la posición del ratón de coordenadas de pantalla a coordenadas del grafo.</li>
     * <li>Solicita al dataManager la creación del nodo en la posición calculada.</li>
     * </ol>
     *
     * @param dtde El evento de soltado que contiene la información de la transferencia y la ubicación.
     */
    private void handleDropEvent(DropTargetDropEvent dtde) {
        try {
            Transferable tr = dtde.getTransferable();

            // Comprobamos si el formato de datos es compatible (String)
            if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                // Extraemos el tipo de componente (ej: "FILTER", "ROUTER")
                String type = (String) tr.getTransferData(DataFlavor.stringFlavor);

                // --- CÁLCULO DE COORDENADAS RELATIVAS ---
                Point p = dtde.getLocation();
                double scale = graphComponent.getGraph().getView().getScale();
                Point translate = graphComponent.getGraphControl().getLocation();

                // Ajustamos la X e Y restando el desplazamiento y dividiendo por la escala (zoom)
                // para que el nodo aparezca exactamente bajo el puntero del ratón.
                int finalX = (int) ((p.getX() / scale) - (translate.getX() / scale));
                int finalY = (int) ((p.getY() / scale) - (translate.getY() / scale));

                // Insertamos el nodo en el hilo de despacho de eventos de Swing (EDT)
                SwingUtilities.invokeLater(() -> dataManager.insertNode(type, finalX, finalY));

                dtde.dropComplete(true);
            } else {
                dtde.rejectDrop();
            }
        } catch (Exception e) {
            e.printStackTrace();
            dtde.rejectDrop();
        }
    }
}