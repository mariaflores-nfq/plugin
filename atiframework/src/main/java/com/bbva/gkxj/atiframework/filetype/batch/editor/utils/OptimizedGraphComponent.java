package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Componente de grafo optimizado para renderizado de alto rendimiento.
 *
 * Esta clase extiende mxGraphComponent y aplica varias optimizaciones:
 *
 * 1. DETECCIÓN DE INTERACCIÓN:
 *    - Detecta automáticamente scroll, zoom y arrastre
 *    - Activa modo de renderizado simplificado durante estas operaciones
 *
 * 2. RENDERING HINTS DINÁMICOS:
 *    - Desactiva anti-aliasing durante scroll/zoom (gran mejora de rendimiento)
 *    - Lo reactiva cuando el usuario deja de interactuar
 *
 * 3. THROTTLING DE REPINTADOS:
 *    - Limita la frecuencia de repintados a ~60fps máximo
 *    - Evita sobrecarga cuando hay muchos eventos de scroll
 *
 * 4. OCULTACIÓN DE LABELS:
 *    - Durante interacción, no renderiza etiquetas de texto
 *    - El texto es el elemento más costoso de renderizar
 *
 * USO:
 * Reemplazar `new mxGraphComponent(graph)` por `new OptimizedGraphComponent(graph)`
 */
public class OptimizedGraphComponent extends mxGraphComponent
        implements InteractionManager.InteractionListener {

    // ========== CONFIGURACIÓN DE THROTTLING ==========

    /**
     * Tiempo mínimo entre repintados en milisegundos.
     * 16ms ≈ 60fps, que es suficiente para una experiencia fluida.
     */
    private static final long MIN_PAINT_INTERVAL_MS = 15;

    /**
     * Timestamp del último repintado completado.
     */
    private long lastPaintTime = 0;

    // ========== ESTADO ==========

    /**
     * Referencia al gestor de interacción.
     */
    private final InteractionManager interactionManager;

    /**
     * Flag para saber si estamos en modo de renderizado simplificado.
     */
    private boolean simplifiedRendering = false;

    /**
     * Flag para controlar si mostramos labels.
     */
    private boolean showLabels = true;

    /**
     * Timer para programar repintados pendientes (evita crear muchos timers).
     */
    private Timer pendingRepaintTimer = null;

    /**
     * Flag para saber si hay un repintado pendiente.
     */
    private volatile boolean repaintPending = false;

    // ========== CONSTRUCTOR ==========

    /**
     * Crea un nuevo componente de grafo optimizado.
     *
     * @param graph el grafo a visualizar
     */
    public OptimizedGraphComponent(mxGraph graph) {
        super(graph);

        // Obtener el gestor de interacción y suscribirse
        this.interactionManager = InteractionManager.getInstance();
        this.interactionManager.addListener(this);

        // Configurar optimizaciones base
        configureOptimizations();

        // Instalar listeners para detectar interacciones
        installInteractionListeners();
    }

    // ========== CONFIGURACIÓN INICIAL ==========

    /**
     * Configura las optimizaciones base del componente.
     */
    private void configureOptimizations() {
        // Habilitar doble y triple buffering
        setDoubleBuffered(true);
        setTripleBuffered(true);

        // Habilitar anti-aliasing por defecto (se desactivará durante interacción)
        setAntiAlias(true);
        setTextAntiAlias(true);

        // Optimizar el control del grafo
        getGraphControl().setDoubleBuffered(true);

        // Optimizar el viewport
        getViewport().setDoubleBuffered(true);

        // Deshabilitar validación automática durante operaciones (mejora rendimiento)
        getGraph().setAutoSizeCells(false);
    }

    /**
     * Instala los listeners para detectar interacciones del usuario.
     */
    private void installInteractionListeners() {
        // Listener para la rueda del ratón (zoom)
        // Este se añade al control del grafo para capturar todos los eventos
        getGraphControl().addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Notificar inicio de interacción
                interactionManager.startInteraction();

                // Actualizar la escala actual
                SwingUtilities.invokeLater(() -> {
                    double scale = getGraph().getView().getScale();
                    interactionManager.setCurrentScale(scale);
                });
            }
        });

        // Listener para scroll mediante arrastre del viewport
        getViewport().addChangeListener(e -> {
            // El viewport cambió (scroll)
            interactionManager.startInteraction();
        });

        // Listener para arrastre de celdas
        getGraphControl().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // El usuario está arrastrando algo
                interactionManager.startInteraction();
            }
        });

        // Listener para eventos de modelo (cuando se mueven celdas)
        getGraph().getModel().addListener(mxEvent.CHANGE, (sender, evt) -> {
            // Hubo un cambio en el modelo
            interactionManager.startInteraction();
        });
    }

    // ========== CALLBACK DEL INTERACTION MANAGER ==========

    /**
     * Callback llamado cuando cambia el estado de interacción.
     * Aquí es donde activamos/desactivamos las optimizaciones.
     */
    @Override
    public void onInteractionStateChanged(boolean isInteracting,
            InteractionManager.DetailLevel detailLevel) {

        boolean wasSimplified = this.simplifiedRendering;
        this.simplifiedRendering = isInteracting;

        // IMPORTANTE: Siempre mantener las labels visibles para mejor UX
        // Solo cambiamos la calidad del renderizado, no la visibilidad
        this.showLabels = true;
        getGraph().setLabelsVisible(true);

        // Solo forzar repintado si cambiamos de modo
        if (wasSimplified != isInteracting) {
            // Forzar repintado para aplicar los cambios
            // Usamos invokeLater para evitar problemas de concurrencia
            SwingUtilities.invokeLater(() -> {
                // Invalidar la vista para forzar recálculo
                getGraph().getView().invalidate();
                repaint();
            });
        }
    }

    // ========== OVERRIDE DE MÉTODOS DE RENDERIZADO ==========

    /**
     * Override del método paint para aplicar optimizaciones dinámicas.
     *
     * Este es el punto clave donde controlamos:
     * - Throttling de repintados (solo programa futuros, nunca salta el actual)
     * - Rendering hints según estado de interacción
     */
    @Override
    public void paint(Graphics g) {
        // ===== RENDERING HINTS =====
        // Siempre aplicar los hints ANTES de pintar
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;

            if (simplifiedRendering) {
                // Modo simplificado: menor calidad pero más rápido
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                    RenderingHints.VALUE_RENDER_SPEED);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                    RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                    RenderingHints.VALUE_COLOR_RENDER_SPEED);
            } else {
                // Modo completo: máxima calidad
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                    RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
        }

        // Siempre pintar - nunca saltamos un frame
        super.paint(g);

        // ===== THROTTLING =====
        // Solo limitamos la programación de repintados adicionales
        // Esto evita acumulación de eventos pero NUNCA deja la pantalla vacía
        lastPaintTime = System.currentTimeMillis();
    }

    /**
     * Override de repaint para implementar throttling inteligente.
     * Este método controla la frecuencia de repintados sin dejar frames vacíos.
     */
    @Override
    public void repaint() {
        if (simplifiedRendering) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastPaintTime;

            if (elapsed < MIN_PAINT_INTERVAL_MS) {
                // Programar un repintado futuro si no hay uno pendiente
                if (!repaintPending) {
                    repaintPending = true;
                    if (pendingRepaintTimer == null) {
                        pendingRepaintTimer = new Timer((int) MIN_PAINT_INTERVAL_MS, e -> {
                            repaintPending = false;
                            OptimizedGraphComponent.super.repaint();
                        });
                        pendingRepaintTimer.setRepeats(false);
                    }
                    pendingRepaintTimer.restart();
                }
                return;
            }
        }

        // Ejecutar repaint normalmente
        super.repaint();
    }


    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * @return true si actualmente estamos en modo de renderizado simplificado
     */
    public boolean isSimplifiedRendering() {
        return simplifiedRendering;
    }

    /**
     * @return true si actualmente se muestran las etiquetas
     */
    public boolean isShowingLabels() {
        return showLabels;
    }

    /**
     * Permite forzar el modo de renderizado simplificado manualmente.
     * Útil para operaciones batch que modifiquen muchas celdas.
     *
     * @param simplified true para activar modo simplificado
     */
    public void setSimplifiedRendering(boolean simplified) {
        this.simplifiedRendering = simplified;
        if (simplified) {
            interactionManager.startInteraction();
        } else {
            interactionManager.endInteraction();
        }
    }

    // ========== CLEANUP ==========

    /**
     * Limpia los recursos cuando el componente ya no se usa.
     */
    public void dispose() {
        interactionManager.removeListener(this);
        if (pendingRepaintTimer != null) {
            pendingRepaintTimer.stop();
            pendingRepaintTimer = null;
        }
    }
}

