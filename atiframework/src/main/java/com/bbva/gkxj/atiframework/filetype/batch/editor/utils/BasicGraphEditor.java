package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.util.*;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * Panel principal que encapsula la logica y los componentes visuales de un editor de grafos basico.
 * Integra un lienzo de dibujo (mxGraphComponent), una vista miniatura (mxGraphOutline),
 * un panel de biblioteca (JTabbedPane) para paletas de herramientas, y gestiona
 * el historial de acciones (Deshacer/Rehacer).
 */
public class BasicGraphEditor extends JPanel {

    /**
     * Componente principal de la interfaz grafica que actua como lienzo interactivo para visualizar y editar el grafo.
     */
    protected mxGraphComponent graphComponent;

    /**
     * Vista en miniatura que proporciona una panoramica global del grafo contenido en el componente principal.
     */
    protected mxGraphOutline graphOutline;

    /**
     * Panel con pestañas diseñado para alojar y organizar las diferentes paletas de componentes o plantillas disponibles para el usuario.
     */
    protected JTabbedPane libraryPane;

    /**
     * Gestor encargado de registrar el historial de transacciones y cambios del modelo para permitir operaciones de deshacer y rehacer.
     */
    protected mxUndoManager undoManager;

    /**
     * Titulo base de la aplicacion que se utiliza para conformar el texto a mostrar en la barra de la ventana principal.
     */
    protected String appTitle;

    /**
     * Archivo fisico en el sistema local que se encuentra actualmente abierto y vinculado al editor.
     */
    protected File currentFile;

    /**
     * Indicador de estado que determina si el modelo del grafo actual contiene cambios que aun no han sido guardados en el archivo.
     */
    protected boolean modified = false;

    /**
     * Manejador visual que permite a los usuarios seleccionar multiples celdas en el lienzo arrastrando un area rectangular.
     */
    protected mxRubberband rubberband;

    /**
     * Manejador responsable de interceptar y procesar los atajos y eventos de teclado dirigidos al componente del grafo.
     */
    protected mxKeyboardHandler keyboardHandler;

    static {
        try {
            mxResources.add("com/bbva/gkxj/atiframework/filetype/batch/editor/core/resources/editor");
        } catch (Exception ignored) {
        }
    }

    protected mxIEventListener undoHandler = new mxIEventListener() {
        public void invoke(Object source, mxEventObject evt) {
            undoManager.undoableEditHappened((mxUndoableEdit) evt
                    .getProperty("edit"));
        }
    };

    protected mxIEventListener changeTracker = new mxIEventListener() {
        public void invoke(Object source, mxEventObject evt) {
            setModified(true);
        }
    };

    /**
     * Construye un nuevo editor de grafos basico inicializando sus componentes estructurales.
     * Configura el lienzo principal, el administrador de historial, la vista en miniatura
     * y el panel lateral para la biblioteca de plantillas, ensamblandolos usando divisores (JSplitPane).
     * Tambien registra los manejadores de teclado y eventos necesarios.
     *
     * @param appTitle Titulo base de la aplicacion que se mostrara en la ventana contenedora.
     * @param component El componente visual del grafo (mxGraphComponent) que actuara como lienzo central.
     */
    public BasicGraphEditor(String appTitle, mxGraphComponent component) {
        this.appTitle = appTitle;

        graphComponent = component;
        final mxGraph graph = graphComponent.getGraph();
        undoManager = createUndoManager();

        graph.setResetViewOnRootChange(false);
        graph.getModel().addListener(mxEvent.CHANGE, changeTracker);
        graph.getModel().addListener(mxEvent.UNDO, undoHandler);
        graph.getView().addListener(mxEvent.UNDO, undoHandler);

        mxIEventListener undoHandler = (source, evt) -> {
            List<mxUndoableChange> changes = ((mxUndoableEdit) evt
                    .getProperty("edit")).getChanges();
            graph.setSelectionCells(graph
                    .getSelectionCellsForChanges(changes));
        };

        undoManager.addListener(mxEvent.UNDO, undoHandler);
        undoManager.addListener(mxEvent.REDO, undoHandler);

        graphOutline = new mxGraphOutline(graphComponent);
        Color mainColor = new Color(245, 245, 245);
        graphOutline.setBackground(mainColor);
        graphOutline.setTripleBuffered(true);  // Habilitar triple buffering para mejor rendimiento

        libraryPane = new JTabbedPane();
        libraryPane.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                libraryPane, graphOutline);
        inner.setDividerLocation(740);
        inner.setResizeWeight(1);
        inner.setDividerSize(3);
        inner.setBorder(null);

        JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inner,
                graphComponent);
        outer.setOneTouchExpandable(false);
        outer.setDividerLocation(80);
        outer.setDividerSize(1);
        outer.setBorder(null);

        setLayout(new BorderLayout());
        add(outer, BorderLayout.CENTER);

        installHandlers();
        installListeners();
        updateTitle();
    }

    /**
     * Instancia y devuelve un nuevo gestor de deshacer/rehacer para rastrear los cambios del modelo.
     *
     * @return Una nueva instancia de mxUndoManager lista para registrar ediciones.
     */
    protected mxUndoManager createUndoManager() {
        return new mxUndoManager();
    }

    /**
     * Inicializa e instala los manejadores de interaccion por defecto en el componente del grafo.
     * Configura el seleccionador elastico (rubberband) para multiples celdas y el soporte basico de teclado.
     */
    protected void installHandlers() {
        rubberband = new mxRubberband(graphComponent);
        keyboardHandler = new EditorKeyboardHandler(graphComponent);
    }

    /**
     * Crea una nueva paleta de herramientas, la envuelve en un panel con desplazamiento y la añade
     * como una nueva pestaña en el panel de la biblioteca lateral.
     *
     * @param title El titulo que se mostrara en la pestaña de la nueva paleta.
     * @return La instancia recien creada de EditorPalette para añadirle plantillas posteriormente.
     */
    public EditorPalette insertPalette(String title) {
        final EditorPalette palette = new EditorPalette();
        final JScrollPane scrollPane = new JScrollPane(palette);
        Color mainColor = new Color(245, 245, 245);
        palette.setBackground(mainColor);
        palette.setOpaque(true);
        scrollPane.getViewport().setBackground(mainColor);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        libraryPane.add(title, scrollPane);

        libraryPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = scrollPane.getWidth()
                        - scrollPane.getVerticalScrollBar().getWidth();
                palette.setPreferredWidth(w);
            }

        });

        return palette;
    }

    /**
     * Procesa los eventos de movimiento de la rueda del raton para aplicar acercamiento (zoom in)
     * o alejamiento (zoom out) sobre el lienzo principal.
     *
     * @param e Evento de raton que contiene la rotacion de la rueda.
     */
    protected void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            graphComponent.zoomIn();
        } else {
            graphComponent.zoomOut();
        }
    }

    /**
     * Muestra un menu emergente de opciones de visualizacion al hacer clic derecho sobre
     * la vista en miniatura (graphOutline). Permite alternar la cuadricula, etiquetas y el almacenamiento en buffer.
     *
     * @param e Evento de raton que desencadeno la apertura del menu contextual.
     */
    protected void showOutlinePopupMenu(MouseEvent e) {
        Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
                graphComponent);
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(
                mxResources.get("magnifyPage"));
        item.setSelected(graphOutline.isFitPage());

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphOutline.setFitPage(!graphOutline.isFitPage());
                graphOutline.repaint();
            }
        });

        JCheckBoxMenuItem item2 = new JCheckBoxMenuItem(
                mxResources.get("showLabels"));
        item2.setSelected(graphOutline.isDrawLabels());

        item2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphOutline.setDrawLabels(!graphOutline.isDrawLabels());
                graphOutline.repaint();
            }
        });

        JCheckBoxMenuItem item3 = new JCheckBoxMenuItem(
                mxResources.get("buffering"));
        item3.setSelected(graphOutline.isTripleBuffered());

        item3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphOutline.setTripleBuffered(!graphOutline.isTripleBuffered());
                graphOutline.repaint();
            }
        });

        JPopupMenu menu = new JPopupMenu();
        menu.add(item);
        menu.add(item2);
        menu.add(item3);
        menu.show(graphComponent, pt.x, pt.y);

        e.consume();
    }

    /**
     * Configura y registra los escuchadores globales de interaccion del raton tanto en el
     * lienzo principal como en la vista en miniatura, gestionando atajos como el zoom con la tecla Control.
     */
    protected void installListeners() {
        MouseWheelListener wheelTracker = new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getSource() instanceof mxGraphOutline
                        || e.isControlDown()) {
                    BasicGraphEditor.this.mouseWheelMoved(e);
                }
            }

        };

        graphOutline.addMouseWheelListener(wheelTracker);
        graphComponent.addMouseWheelListener(wheelTracker);
        graphOutline.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                mouseReleased(e);
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showOutlinePopupMenu(e);
                }
            }

        });

    }

    /**
     * Establece el archivo fisico que actualmente esta siendo editado en el editor.
     * Notifica el cambio de propiedad y actualiza el titulo de la ventana si es necesario.
     *
     * @param file El objeto File que representa el documento actual, o null si es uno nuevo.
     */
    public void setCurrentFile(File file) {
        File oldValue = currentFile;
        currentFile = file;

        firePropertyChange("currentFile", oldValue, file);

        if (oldValue != file) {
            updateTitle();
        }
    }

    /**
     * Verifica el estado actual de edicion del documento en el editor.
     *
     * @return Verdadero si el grafo ha sufrido modificaciones no guardadas, falso en caso contrario.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Actualiza el indicador de modificaciones del documento actual.
     * Si el estado cambia respecto al anterior, dispara una notificacion de propiedad y actualiza el titulo de la ventana.
     *
     * @param modified Verdadero para marcar el editor con cambios pendientes de guardar, falso si esta sincronizado.
     */
    public void setModified(boolean modified) {
        boolean oldValue = this.modified;
        this.modified = modified;

        firePropertyChange("modified", oldValue, modified);

        if (oldValue != modified) {
            updateTitle();
        }
    }

    /**
     * Obtiene el componente visual que actua como lienzo donde se esta renderizando el grafo.
     *
     * @return El mxGraphComponent instanciado para este editor.
     */
    public mxGraphComponent getGraphComponent() {
        return graphComponent;
    }

    /**
     * Obtiene el administrador encargado de guardar y gestionar el historial de deshacer/rehacer.
     *
     * @return La instancia de mxUndoManager vinculada al modelo del grafo actual.
     */
    public mxUndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * Envuelve una accion abstracta enlazandola con el componente del grafo principal
     * para asegurar que se ejecute en el contexto adecuado.
     *
     * @param name Nombre o identificador en formato texto para referirse a la accion.
     * @param action Accion base que se desea envolver y enlazar.
     * @return Una nueva accion abstracta lista para ser ejecutada sobre el grafo.
     */
    public Action bind(String name, final Action action) {
        return bind(name, action, null);
    }

    /**
     * Envuelve una accion abstracta enlazandola con el componente del grafo principal y le asigna
     * un icono visual si se proporciona una ruta valida.
     *
     * @param name Nombre o identificador en formato texto de la accion.
     * @param action Accion base que se desea envolver y enlazar al contexto actual.
     * @param iconUrl Ruta relativa para cargar una imagen que se utilizara como icono de la accion.
     * @return Una nueva accion abstracta enriquecida visualmente y vinculada al lienzo.
     */
    @SuppressWarnings("serial")
    public Action bind(String name, final Action action, String iconUrl) {
        AbstractAction newAction = new AbstractAction(name, (iconUrl != null) ? new ImageIcon(
                BasicGraphEditor.class.getResource(iconUrl)) : null) {
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(new ActionEvent(getGraphComponent(), e
                        .getID(), e.getActionCommand()));
            }
        };

        newAction.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.SHORT_DESCRIPTION));

        return newAction;
    }

    /**
     * Sincroniza la barra de titulo del contenedor principal (JFrame) para reflejar
     * el nombre del archivo actual, la aplicacion y un asterisco (*) si hay modificaciones pendientes.
     */
    public void updateTitle() {
        JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

        if (frame != null) {
            String title = (currentFile != null) ? currentFile
                    .getAbsolutePath() : mxResources.get("newDiagram");

            if (modified) {
                title += "*";
            }

            frame.setTitle(title + " - " + appTitle);
        }
    }

}