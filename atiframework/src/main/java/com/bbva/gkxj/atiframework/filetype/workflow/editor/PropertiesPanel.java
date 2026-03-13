package com.bbva.gkxj.atiframework.filetype.workflow.editor;

import com.bbva.gkxj.atiframework.filetype.workflow.controller.WorkflowDataManager;
import com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor.ComponentEditorStrategy;
import com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor.EditorFactory;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.TaskExecutorCombo;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel de UI encargado de mostrar y gestionar las propiedades de los nodos del workflow.
 * <p>
 * Actúa como contenedor dinámico que, mediante el uso de {@link EditorFactory},
 * renderiza el formulario específico según el tipo de celda seleccionada en el grafo.
 */
public class PropertiesPanel extends JPanel {

    /** Panel contenedor donde se inyectan los editores de componentes. */
    private final JPanel contentPanel;

    /** Referencia al grafo de mxGraph para gestionar las actualizaciones del modelo. */
    private mxGraph graph;

    /** Referencia al DataManager para obtener información global del workflow. */
    private WorkflowDataManager dataManager;

    /** Celda (nodo o arista) que se está editando actualmente. */
    private mxCell currentCell;

    /** Estrategia de edición activa para la celda seleccionada. */
    private ComponentEditorStrategy currentEditor;

    /**
     * Constructor por defecto. Inicializa la estructura visual del panel,
     * incluyendo el encabezado y el área de scroll para el contenido dinámico.
     */
    public PropertiesPanel() {
        setLayout(new BorderLayout());
        setBackground(JBColor.WHITE);

        // Configuración del encabezado
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(new Color(200, 200, 200));
        JLabel titleLabel = new JLabel("Properties panel");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(titleLabel);
        add(header, BorderLayout.NORTH);

        // Configuración del panel de contenido
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(JBUI.Borders.empty(10, 5, 10, 10));
        contentPanel.setBackground(JBColor.WHITE);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Establece la instancia del grafo sobre la que operará el panel.
     * @param graph Instancia de {@link mxGraph}.
     */
    public void setGraph(mxGraph graph) {
        this.graph = graph;
    }

    /**
     * Establece el gestor de datos para tener acceso a metadatos globales (como los ejecutores).
     * @param dataManager Instancia de {@link WorkflowDataManager}.
     */
    public void setDataManager(WorkflowDataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Muestra las propiedades globales del workflow asociadas al nodo principal.
     * @param mainNodeCell Celda que contiene la configuración global del JSON.
     */
    public void showGlobalProperties(mxCell mainNodeCell) {
        renderEditorFor(mainNodeCell, true);
    }

    /**
     * Muestra las propiedades específicas de un nodo individual del grafo.
     * @param cell Celda (nodo) seleccionada por el usuario.
     */
    public void showNodeProperties(mxCell cell) {
        renderEditorFor(cell, false);
    }

    /**
     * Limpia el contenido del panel de propiedades y resetea las referencias
     * al editor y celda actuales.
     */
    public void clearPanel() {
        this.currentCell = null;
        this.currentEditor = null;
        contentPanel.removeAll();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Lógica interna para determinar qué editor renderizar basado en el valor de la celda.
     * @param cell     La celda de mxGraph a editar.
     * @param isGlobal Indica si se deben cargar propiedades de nivel superior (globales).
     */
    private void renderEditorFor(mxCell cell, boolean isGlobal) {
        if (cell == null || cell.getValue() == null) {
            clearPanel();
            return;
        }

        this.currentCell = cell;
        Object value = cell.getValue();

        // Obtener la estrategia adecuada según el tipo de objeto (Node, Workflow, etc.)
        currentEditor = EditorFactory.getEditor(value, isGlobal);

        if (currentEditor != null) {
            contentPanel.removeAll();

            // Se construye la UI pasando un callback (saveChangesToModel) para persistencia
            JPanel editorUI = currentEditor.buildUI(this::saveChangesToModel);
            contentPanel.add(editorUI, BorderLayout.NORTH);

            // --- INYECCIÓN DE DEPENDENCIA ---
            // Si el editor requiere la lista de Task Executors, se la pasamos antes de cargar los datos
            if (currentEditor instanceof TaskExecutorCombo && dataManager != null) {
                List<String> globalExecutors = dataManager.getGlobalData().getGlobalTaskExecutorCodes();
                ((TaskExecutorCombo) currentEditor).setGlobalExecutors(globalExecutors);
            }

            // Cargar los datos del modelo en los campos del editor
            currentEditor.loadData(value);
        } else {
            clearPanel();
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Sincroniza los cambios realizados en la UI del editor de vuelta al modelo de mxGraph.
     * <p>
     * Utiliza {@code graph.getModel().beginUpdate()} para asegurar que el cambio
     * sea atómico y soporte la funcionalidad de Undo/Redo.
     */
    private void saveChangesToModel() {
        if (currentEditor == null || currentCell == null || graph == null) return;

        Object value = currentCell.getValue();

        graph.getModel().beginUpdate();
        try {
            // Persistir cambios desde la UI al objeto de negocio (value)
            currentEditor.saveData(value);

            // Notificar al grafo que el valor de la celda ha cambiado
            graph.getModel().setValue(currentCell, value);
        } finally {
            graph.getModel().endUpdate();
        }
    }
}