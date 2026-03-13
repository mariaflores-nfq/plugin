package com.bbva.gkxj.atiframework.filetype.workflow.editor;

import com.bbva.gkxj.atiframework.filetype.workflow.BaseGraphEditorStructure;
import com.bbva.gkxj.atiframework.filetype.workflow.controller.GraphController;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.mxgraph.view.mxGraph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Panel principal del editor de Workflows.
 * <p>
 * Extiende de {@link BaseGraphEditorStructure} para implementar la arquitectura base del editor
 * y gestiona la interacción entre la paleta de componentes, el lienzo de dibujo (mxGraph)
 * y el panel de propiedades.
 */
public class WfEditorPanel extends BaseGraphEditorStructure implements EditorPalette.PaletteToolListener {

    /** Paleta de herramientas y componentes disponibles para el diseño del workflow. */
    private EditorPalette palette;

    /**
     * Constructor del editor de Workflow. Inicializa la estructura base del editor
     * pasando el contexto de IntelliJ a la clase padre.
     * * @param project     Contexto del proyecto de IntelliJ actualmente activo.
     * @param virtualFile Archivo virtual que representa el workflow abierto.
     */
    public WfEditorPanel(Project project, VirtualFile virtualFile) {
        super(project, virtualFile); // Pasamos los datos directamente al padre
    }

    /**
     * Configura e inicializa el panel de propiedades.
     * @param propertiesContainer Contenedor Swing donde se alojará el panel de propiedades.
     */
    @Override
    protected void setupPropertiesPanel(@NotNull JPanel propertiesContainer) {
        PropertiesPanel propertiesPanel = new PropertiesPanel();
        if (controller != null) {
            controller.setPropertiesPanel(propertiesPanel);
        }
        propertiesContainer.setLayout(new BorderLayout());
        propertiesContainer.add(propertiesPanel, BorderLayout.CENTER);

        propertiesContainer.revalidate();
        propertiesContainer.repaint();
        propertiesPanel.clearPanel();
    }

    /**
     * Ejecuta un callback cuando el controlador del grafo está listo para ser utilizado.
     * @param callback Consumidor que recibirá el {@link GraphController} una vez inicializado.
     */
    public void onControllerReady(java.util.function.Consumer<GraphController> callback) {
        if (this.getController() != null) {
            callback.accept(this.getController());
        } else {
            SwingUtilities.invokeLater(() -> {
                if (this.getController() != null) {
                    callback.accept(this.getController());
                }
            });
        }
    }

    /**
     * Configura la paleta de componentes laterales dentro de un contenedor con pestañas.
     * @param paletteContainer Contenedor de pestañas (JTabbedPane) para la paleta.
     */
    @Override
    protected void setupPalette(@NotNull JTabbedPane paletteContainer) {
        this.palette = new EditorPalette(this);
        palette.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(palette);
        palette.addTemplate();

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(null);
        paletteContainer.addTab("Workflow", scrollPane);
    }

    /**
     * Notifica al controlador cuando se ha seleccionado una herramienta de la paleta.
     * @param toolId Identificador de la herramienta seleccionada.
     */
    @Override
    public void onToolSelected(String toolId) {
        if (controller != null) {
            controller.changeToolMode(toolId);
        }
    }

    /**
     * Gestiona la lógica visual y de estado del cursor cuando se selecciona un tipo de nodo.
     * Desactiva el desplazamiento (panning) y la conexión automática para facilitar la inserción.
     * @param nodeType Tipo de nodo de workflow seleccionado.
     */
    @Override
    public void onNodeSelected(WorkFlowStyles.WfNodeType nodeType) {
        if (controller == null) return;

        graphComponent.setPanning(false);
        graphComponent.setConnectable(false);
        graphComponent.getGraphControl().setCursor(Cursor.getDefaultCursor());
        controller.changeToolMode("NODE");
    }

    /**
     * Crea y configura la instancia de {@link mxGraph} personalizada para el workflow.
     * Define restricciones como la imposibilidad de colapsar nodos o cambiar su tamaño.
     * @return Una instancia de mxGraph configurada.
     */
    @Override
    protected mxGraph createGraph() {
        mxGraph graph = new mxGraph() {
            @Override
            public boolean isCellFoldable(Object cell, boolean collapse) {
                return false;
            }
        };
        graph.setCellsMovable(true);
        graph.setCellsLocked(false);
        graph.setCellsSelectable(true);
        graph.setCellsResizable(false);

        return graph;
    }

    /**
     * Carga los datos de un workflow desde un objeto JSON al grafo visual.
     * @param jsonData Datos del workflow en formato {@link WorkflowJsonData}.
     */
    public void loadWorkflowData(WorkflowJsonData jsonData) {
        if (jsonData == null) return;
        if (this.controller != null) {
            this.controller.loadGraphFromData(jsonData);
        }
    }

    /**
     * Crea la barra de herramientas superior.
     * @return null en esta implementación (el workflow no requiere barra de herramientas por defecto).
     */
    @Override
    protected @Nullable JComponent createToolbar() {
        return null;
    }
}