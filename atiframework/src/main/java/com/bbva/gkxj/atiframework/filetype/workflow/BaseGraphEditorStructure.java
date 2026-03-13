package com.bbva.gkxj.atiframework.filetype.workflow;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.workflow.controller.GraphController;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.ImageUtil;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.view.mxGraph;
import com.intellij.ui.components.JBTabbedPane;
import icons.AtiIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Clase base abstracta que define la estructura visual y lógica principal para los editores basados en grafos (mxGraph).
 * <p>
 * Proporciona un diseño (layout) estandarizado que incluye:
 * <ul>
 * <li>Un panel izquierdo (Paleta/Librería) para arrastrar elementos.</li>
 * <li>Un lienzo central para el grafo interactivo.</li>
 * <li>Un minimapa superpuesto y colapsable en la esquina superior derecha.</li>
 * <li>Un panel derecho para visualizar y editar propiedades.</li>
 * </ul>
 * <p>
 * Las clases que hereden de esta deben implementar los métodos abstractos para definir las herramientas
 * específicas, el modelo del grafo y las propiedades del editor.
 */
public abstract class BaseGraphEditorStructure extends JPanel {

    // Bloque estático para inicializar los estilos globales una única vez.
    static {
        setupGlobalGraphStyles();
    }

    /**
     * Configura las constantes estáticas globales de mxGraph relacionadas con los estilos de selección.
     * Define el color corporativo, el grosor de las líneas de selección y los colores de los tiradores.
     */
    private static void setupGlobalGraphStyles() {
        Color primaryBlue = new Color(0, 120, 215);

        mxSwingConstants.VERTEX_SELECTION_COLOR = primaryBlue;
        mxSwingConstants.EDGE_SELECTION_COLOR = primaryBlue;

        mxSwingConstants.VERTEX_SELECTION_STROKE = new BasicStroke(3.0f);
        mxSwingConstants.EDGE_SELECTION_STROKE = new BasicStroke(3.0f);

        mxSwingConstants.HANDLE_BORDERCOLOR = primaryBlue;
        mxSwingConstants.HANDLE_FILLCOLOR = Color.WHITE;
    }

    /** Modelo principal del grafo. */
    protected mxGraph graph;

    /** Componente visual de Swing que renderiza el grafo. */
    protected mxGraphComponent graphComponent;

    /** Componente visual que representa el minimapa (vista general) del grafo. */
    protected mxGraphOutline graphOutline;

    /** Gestor del historial para deshacer/rehacer acciones en el grafo. */
    protected mxUndoManager undoManager;

    /** Controlador principal que gestiona la interacción, herramientas y datos del grafo. */
    protected GraphController controller;

    /** Panel de pestañas izquierdo que contiene la paleta de componentes. */
    protected JTabbedPane libraryPane;

    /** Panel derecho encargado de mostrar las propiedades del elemento seleccionado. */
    protected JPanel propertiesPanel;

    /** Divisor principal de la interfaz que separa la paleta, el lienzo y las propiedades. */
    protected JSplitPane mainSplitter;

    /** Contexto del proyecto de IntelliJ actualmente activo. */
    protected final Project project;

    /** Archivo virtual que representa el workflow abierto. */
    protected final VirtualFile virtualFile;

    /**
     * Constructor base. Inicializa los componentes principales del grafo, el controlador,
     * el gestor de historial y ensambla la estructura visual completa del editor.
     *
     * @param project     El proyecto actual de IntelliJ.
     * @param virtualFile El archivo virtual sobre el que trabaja este editor.
     */
    public BaseGraphEditorStructure(Project project, VirtualFile virtualFile) {
        super(new BorderLayout());

        // Asignamos las variables del contexto de IntelliJ
        this.project = project;
        this.virtualFile = virtualFile;

        fixClassLoader();

        this.graph = createGraph();
        this.graphComponent = createGraphComponent(graph);
        this.undoManager = new mxUndoManager();
        this.controller = new GraphController(graphComponent, undoManager, project, virtualFile);

        setupVisualStructure();
    }

    /**
     * Soluciona problemas de carga de clases (Classloader) específicos de entornos de plugins (IntelliJ),
     * asegurando que las transferencias de objetos de mxGraph funcionen correctamente durante el arrastrar y soltar.
     */
    private void fixClassLoader() {
        Thread.currentThread().setContextClassLoader(com.mxgraph.swing.util.mxGraphTransferable.class.getClassLoader());
    }

    /**
     * Crea y configura el componente de renderizado interactivo para el grafo proporcionado.
     * Sobrescribe el lienzo nativo para interceptar la carga de imágenes y permitir el uso
     * de iconos vectoriales propios del entorno de IntelliJ (AllIcons/AtiIcons).
     *
     * @param graph El objeto mxGraph que será renderizado por el componente.
     * @return El componente visual de Swing (mxGraphComponent) configurado.
     */
    protected mxGraphComponent createGraphComponent(mxGraph graph) {
        mxGraphComponent component = new mxGraphComponent(graph) {
            @Override
            public mxInteractiveCanvas createCanvas() {
                return new mxInteractiveCanvas() {
                    @Override
                    public BufferedImage loadImage(String imagePath) {
                        if (imagePath != null) {
                            for (WorkFlowStyles.WfNodeType type : WorkFlowStyles.WfNodeType.values()) {
                                if (imagePath.equals(type.name())) {
                                    Icon scaledIcon = AtiIcons.getScaledIcon(type.getGraphIcon(), 32);
                                    Image awtImage = IconUtil.toImage(scaledIcon);
                                    return ImageUtil.toBufferedImage(awtImage);
                                }
                            }
                        }
                        return super.loadImage(imagePath);
                    }
                };
            }
        };

        component.getViewport().setOpaque(true);
        component.getViewport().setBackground(Color.WHITE);
        return component;
    }

    /**
     * Ensambla y organiza todos los componentes visuales de la interfaz (Paleta, Lienzo, Minimapa, Panel de Propiedades).
     * Configura el comportamiento responsivo del minimapa superpuesto y los divisores (JSplitPane).
     */
    private void setupVisualStructure() {
        libraryPane = new JBTabbedPane();
        setupPalette(libraryPane);

        graphOutline = new mxGraphOutline(graphComponent);
        graphOutline.setBackground(SchedulerTheme.BBVA_NAVY);

        JPanel minimapContainer = new JPanel(new BorderLayout());
        minimapContainer.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));

        JPanel minimapHeader = new JPanel(new BorderLayout());
        minimapHeader.setBackground(JBColor.PanelBackground);
        minimapHeader.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        minimapHeader.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel("Minimap");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel toggleLabel = new JLabel(" \u25BC ");

        minimapHeader.add(titleLabel, BorderLayout.WEST);
        minimapHeader.add(toggleLabel, BorderLayout.EAST);

        minimapContainer.add(minimapHeader, BorderLayout.NORTH);
        minimapContainer.add(graphOutline, BorderLayout.CENTER);

        JLayeredPane layeredGraphArea = new JLayeredPane();
        layeredGraphArea.add(graphComponent, JLayeredPane.DEFAULT_LAYER);
        layeredGraphArea.add(minimapContainer, JLayeredPane.PALETTE_LAYER);

        minimapHeader.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                boolean isVisible = graphOutline.isVisible();
                graphOutline.setVisible(!isVisible);
                toggleLabel.setText(isVisible ? " \u25C0 " : " \u25BC ");

                layeredGraphArea.dispatchEvent(new java.awt.event.ComponentEvent(layeredGraphArea, java.awt.event.ComponentEvent.COMPONENT_RESIZED));
            }
        });

        int minimapWidth = 200;
        int minimapHeight = 150;

        controller.setupMinimapListeners(layeredGraphArea, minimapWidth, minimapHeight);

        layeredGraphArea.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = layeredGraphArea.getWidth();
                int h = layeredGraphArea.getHeight();
                graphComponent.setBounds(0, 0, w, h);
                int headerHeight = minimapHeader.getPreferredSize().height;
                if (graphOutline.isVisible()) {
                    minimapContainer.setBounds(w - minimapWidth - 20, 20, minimapWidth, minimapHeight + headerHeight);
                } else {
                    minimapContainer.setBounds(w - minimapWidth - 20, 20, minimapWidth, headerHeight);
                }
            }
        });

        propertiesPanel = new JPanel(new BorderLayout());
        propertiesPanel.setBackground(SchedulerTheme.BBVA_NAVY);
        propertiesPanel.setPreferredSize(new Dimension(300, 0));
        setupPropertiesPanel(propertiesPanel);

        JSplitPane centerRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, layeredGraphArea, propertiesPanel);
        centerRightSplit.setResizeWeight(1.0);
        centerRightSplit.setDividerSize(8);
        centerRightSplit.setOneTouchExpandable(true);

        mainSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, libraryPane, centerRightSplit);
        mainSplitter.setDividerLocation(110);
        mainSplitter.setDividerSize(3);

        graphComponent.setImportEnabled(true);
        add(mainSplitter, BorderLayout.CENTER);

        JComponent toolbar = createToolbar();
        if (toolbar != null) add(toolbar, BorderLayout.NORTH);
    }

    /**
     * Define el panel de propiedades específico para este editor.
     * Las clases hijas deben rellenar el contenedor con los campos necesarios.
     *
     * @param propertiesContainer El contenedor principal del panel lateral derecho.
     */
    protected abstract void setupPropertiesPanel(@NotNull JPanel propertiesContainer);

    /**
     * Define las herramientas (Stencils/Iconos) que tendrá disponibles este editor en la paleta izquierda.
     *
     * @param paletteContainer El contenedor de pestañas donde se registrarán las paletas de herramientas.
     */
    protected abstract void setupPalette(@NotNull JTabbedPane paletteContainer);

    /**
     * Define y personaliza el objeto {@code mxGraph} (estilos, reglas de conexión, comportamiento del lienzo).
     * Es instanciado al inicio del ciclo de vida de la clase.
     *
     * @return El objeto mxGraph configurado.
     */
    protected abstract mxGraph createGraph();

    /**
     * Define la barra de herramientas superior personalizada del editor (botones de guardar, exportar, etc.).
     *
     * @return El componente de la barra de herramientas, o {@code null} si no se desea mostrar ninguna.
     */
    protected abstract @Nullable JComponent createToolbar();

    /**
     * Devuelve el controlador principal asociado a este editor.
     *
     * @return La instancia de {@link GraphController} que orquesta las acciones del grafo.
     */
    public GraphController getController() {
        return this.controller;
    }
}