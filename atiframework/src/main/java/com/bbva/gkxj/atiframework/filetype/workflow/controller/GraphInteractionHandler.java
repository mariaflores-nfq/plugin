package com.bbva.gkxj.atiframework.filetype.workflow.controller;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Gestor encargado de controlar las interacciones físicas del usuario con el lienzo del grafo.
 * <p>
 * Centraliza la lógica de las diferentes "herramientas" seleccionables en la paleta:
 * <ul>
 * <li><b>NODE (Selección):</b> Permite seleccionar y mover nodos libremente usando el cursor estándar.</li>
 * <li><b>HAND (Paneo):</b> Permite arrastrar el lienzo para desplazarse por el grafo sin mover los nodos.</li>
 * <li><b>DIRECT / QUEUE (Conexión):</b> Cambia el cursor a modo trazado para forzar la creación de conexiones.</li>
 * </ul>
 * También gestiona eventos globales como el Zoom mediante la rueda del ratón.
 */
public class GraphInteractionHandler {

    /** Componente visual sobre el que interactúa el usuario. */
    private final mxGraphComponent graphComponent;

    /** Controlador del menú contextual flotante para iniciar conexiones. */
    private final WorkflowContextPadController contextPadController;

    /** Herramienta de selección múltiple tipo "lazo" o rectángulo elástico. */
    private final mxRubberband rubberband;

    /** Identificador de la herramienta actualmente activa (ej. "NODE", "HAND"). */
    private String activeTool = "NODE";

    /** Almacena el último punto registrado en la pantalla durante una operación de paneo (arrastre). */
    private Point lastPanPointOnScreen = null;

    /**
     * Constructor del gestor de interacciones.
     * Inicializa los listeners de ratón para las herramientas y el zoom.
     *
     * @param graphComponent       El componente visual de mxGraph.
     * @param contextPadController El controlador del Context Pad para la delegación de conexiones.
     * @param rubberband           El manejador de la selección elástica de mxGraph.
     */
    public GraphInteractionHandler(mxGraphComponent graphComponent,
                                   WorkflowContextPadController contextPadController,
                                   mxRubberband rubberband) {
        this.graphComponent = graphComponent;
        this.contextPadController = contextPadController;
        this.rubberband = rubberband;

        setupToolInteractions();
        setupZoom();
    }

    /**
     * Cambia la herramienta activa del editor y ajusta el comportamiento del lienzo en consecuencia.
     * Activa o desactiva la capacidad de mover nodos, la selección múltiple y cambia el cursor del ratón.
     *
     * @param toolId El identificador de la herramienta a activar ("HAND", "DIRECT", "QUEUE", "NODE").
     */
    public void changeToolMode(String toolId) {
        this.activeTool = toolId;

        // Si cambiamos a una herramienta que no sea de conexión, cancelamos cualquier conexión en curso.
        if (contextPadController != null && !toolId.equals("DIRECT") && !toolId.equals("QUEUE")) {
            contextPadController.cancelConnectionMode();
        }

        switch (toolId) {
            case "HAND":
                // Bloqueamos el movimiento de celdas y la selección elástica para poder arrastrar la vista.
                graphComponent.setConnectable(false);
                graphComponent.getGraphHandler().setEnabled(true);
                graphComponent.getGraphHandler().setMoveEnabled(false);
                if (rubberband != null) rubberband.setEnabled(false);
                graphComponent.getGraphControl().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                break;

            case "DIRECT":
            case "QUEUE":
                // Mantenemos la selección habilitada para elegir el origen, pero bloqueamos el movimiento.
                graphComponent.setConnectable(false);
                graphComponent.getGraphHandler().setEnabled(true);
                graphComponent.getGraphHandler().setMoveEnabled(false);
                if (rubberband != null) rubberband.setEnabled(false);
                graphComponent.getGraphControl().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;

            case "NODE":
            default:
                // Restauramos el comportamiento normal: los nodos se pueden seleccionar y mover.
                graphComponent.setConnectable(false);
                graphComponent.getGraphHandler().setEnabled(true);
                graphComponent.getGraphHandler().setMoveEnabled(true);
                if (rubberband != null) rubberband.setEnabled(true);
                graphComponent.getGraphControl().setCursor(Cursor.getDefaultCursor());
                break;
        }
    }

    /**
     * Configura los listeners de ratón necesarios para ejecutar el comportamiento
     * de las herramientas especiales, como el paneo ("HAND") o el inicio de conexiones.
     */
    private void setupToolInteractions() {
        // Listener para clics del ratón
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ("HAND".equals(activeTool)) {
                    // Guardamos la posición inicial al pulsar para iniciar el arrastre de la vista
                    lastPanPointOnScreen = e.getLocationOnScreen();
                } else if ("DIRECT".equals(activeTool) || "QUEUE".equals(activeTool)) {
                    // Si hacemos clic sobre un nodo con una herramienta de flecha activa, iniciamos la conexión
                    Object cell = graphComponent.getCellAt(e.getX(), e.getY());
                    if (cell instanceof mxCell && ((mxCell) cell).isVertex()) {
                        if (contextPadController != null) {
                            contextPadController.startConnectionFrom((mxCell) cell, activeTool);
                        }
                    }
                }
            }
        });

        // Listener para el arrastre del ratón
        graphComponent.getGraphControl().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if ("HAND".equals(activeTool) && lastPanPointOnScreen != null) {
                    // Calculamos la diferencia de posición (Delta) para mover el JViewport
                    Point currentPointOnScreen = e.getLocationOnScreen();
                    int dx = currentPointOnScreen.x - lastPanPointOnScreen.x;
                    int dy = currentPointOnScreen.y - lastPanPointOnScreen.y;

                    JViewport viewport = graphComponent.getViewport();
                    Point viewPos = viewport.getViewPosition();

                    // Aplicamos la nueva posición de la vista, asegurando que no salimos de los márgenes (>= 0)
                    viewport.setViewPosition(new Point(Math.max(0, viewPos.x - dx), Math.max(0, viewPos.y - dy)));
                    lastPanPointOnScreen = currentPointOnScreen;
                }
            }
        });
    }

    /**
     * Configura el listener de la rueda del ratón para permitir realizar acercamientos (Zoom In)
     * y alejamientos (Zoom Out) en el lienzo cuando se mantiene pulsada la tecla Control (Ctrl).
     */
    private void setupZoom() {
        graphComponent.getGraphControl().addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                if (e.getWheelRotation() < 0) {
                    graphComponent.zoomIn();
                } else {
                    graphComponent.zoomOut();
                }
                graphComponent.revalidate();
            }
        });
    }
}