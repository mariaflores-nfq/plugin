package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor centralizado del estado de interacción del usuario con el editor de grafos.
 *
 * Esta clase implementa el patrón de "reducción de complejidad visual durante interacciones",
 * detectando cuándo el usuario está realizando scroll, zoom o arrastre, y notificando
 * a los componentes suscritos para que reduzcan temporalmente la calidad de renderizado.
 *
 * FUNCIONAMIENTO:
 * 1. Cuando el usuario inicia una interacción (scroll/zoom), se llama a startInteraction()
 * 2. Esto activa el modo de "renderizado simplificado" (isInteracting = true)
 * 3. Se inicia un timer que espera 150ms de inactividad
 * 4. Si pasan 150ms sin nueva interacción, se restaura la calidad completa
 *
 * BENEFICIOS:
 * - Durante scroll/zoom rápido, el renderizado es mucho más fluido
 * - El usuario no percibe la reducción de calidad porque ocurre durante el movimiento
 * - La calidad completa se restaura automáticamente al detenerse
 */
public class InteractionManager {

    /**
     * Niveles de detalle para el renderizado adaptativo.
     * A menor nivel de zoom, menos detalle necesitamos mostrar.
     */
    public enum DetailLevel {
        /** Zoom > 80%: Todo visible con máxima calidad */
        FULL,
        /** Zoom 50-80%: Sin iconos internos en nodos */
        MEDIUM,
        /** Zoom 20-50%: Solo formas básicas y colores */
        LOW,
        /** Zoom < 20%: Rectángulos simples de color */
        MINIMAL
    }

    // ========== SINGLETON ==========

    private static volatile InteractionManager instance;

    public static InteractionManager getInstance() {
        if (instance == null) {
            synchronized (InteractionManager.class) {
                if (instance == null) {
                    instance = new InteractionManager();
                }
            }
        }
        return instance;
    }

    // ========== ESTADO ==========

    /**
     * Flag que indica si el usuario está actualmente interactuando (scroll/zoom/drag).
     * Cuando es true, los componentes deben usar renderizado simplificado.
     */
    private volatile boolean isInteracting = false;

    /**
     * Timer que controla el tiempo de espera antes de restaurar la calidad completa.
     * Se reinicia con cada nueva interacción.
     */
    private Timer interactionTimer;

    /**
     * Tiempo en milisegundos a esperar después de la última interacción
     * antes de restaurar la calidad completa.
     * 100ms es un buen balance: suficiente para scroll continuo sin parpadeos,
     * pero no tanto como para que el usuario note el retraso en restaurar calidad.
     */
    private static final int INTERACTION_TIMEOUT_MS = 100;

    /**
     * Escala actual del zoom (1.0 = 100%).
     * Se usa para calcular el nivel de detalle apropiado.
     */
    private double currentScale = 1.0;

    /**
     * Lista de listeners que serán notificados cuando cambie el estado de interacción.
     */
    private final List<InteractionListener> listeners = new ArrayList<>();

    // ========== INTERFAZ DE LISTENER ==========

    /**
     * Interfaz para componentes que quieran ser notificados de cambios en el estado de interacción.
     */
    public interface InteractionListener {
        /**
         * Llamado cuando el estado de interacción cambia.
         * @param isInteracting true si el usuario está interactuando, false si terminó
         * @param detailLevel nivel de detalle recomendado según el zoom actual
         */
        void onInteractionStateChanged(boolean isInteracting, DetailLevel detailLevel);
    }

    // ========== CONSTRUCTOR ==========

    private InteractionManager() {
        // Crear el timer que restaurará la calidad después del timeout
        interactionTimer = new Timer(INTERACTION_TIMEOUT_MS, e -> {
            endInteraction();
        });
        interactionTimer.setRepeats(false); // Solo ejecutar una vez
    }

    // ========== MÉTODOS PÚBLICOS ==========

    /**
     * Indica que el usuario ha iniciado o continúa una interacción (scroll, zoom, drag).
     *
     * Este método debe llamarse cada vez que se detecte un evento de interacción.
     * Es seguro llamarlo múltiples veces seguidas - simplemente reinicia el timer.
     *
     * EFECTO:
     * - Activa el modo de renderizado simplificado
     * - Reinicia el timer de timeout
     * - Notifica a los listeners si el estado cambió
     */
    public void startInteraction() {
        // Reiniciar el timer con cada interacción
        interactionTimer.restart();

        // Si ya estábamos interactuando, no notificar de nuevo
        if (!isInteracting) {
            isInteracting = true;
            notifyListeners();
        }
    }

    /**
     * Finaliza el modo de interacción y restaura la calidad completa de renderizado.
     *
     * Este método es llamado automáticamente por el timer después del timeout,
     * pero también puede llamarse manualmente si se sabe que la interacción terminó.
     */
    public void endInteraction() {
        if (isInteracting) {
            isInteracting = false;
            interactionTimer.stop();
            notifyListeners();
        }
    }

    /**
     * Actualiza la escala de zoom actual.
     * Esto afecta el nivel de detalle calculado.
     *
     * @param scale escala de zoom (1.0 = 100%, 0.5 = 50%, etc.)
     */
    public void setCurrentScale(double scale) {
        this.currentScale = scale;
    }

    /**
     * @return true si el usuario está actualmente interactuando con el grafo
     */
    public boolean isInteracting() {
        return isInteracting;
    }

    /**
     * Calcula el nivel de detalle apropiado según la escala de zoom actual.
     *
     * A menor zoom, menos detalle es visible para el usuario, por lo que
     * podemos ahorrar recursos renderizando menos elementos.
     *
     * @return nivel de detalle recomendado
     */
    public DetailLevel getDetailLevel() {
        if (currentScale > 0.8) return DetailLevel.FULL;
        if (currentScale > 0.5) return DetailLevel.MEDIUM;
        if (currentScale > 0.2) return DetailLevel.LOW;
        return DetailLevel.MINIMAL;
    }

    /**
     * Calcula el nivel de detalle considerando también el estado de interacción.
     * Durante una interacción, siempre se usa un nivel reducido.
     *
     * @return nivel de detalle efectivo considerando interacción y zoom
     */
    public DetailLevel getEffectiveDetailLevel() {
        if (isInteracting) {
            // Durante interacción, reducir un nivel respecto al zoom
            DetailLevel zoomLevel = getDetailLevel();
            return switch (zoomLevel) {
                case FULL -> DetailLevel.MEDIUM;
                case MEDIUM -> DetailLevel.LOW;
                case LOW, MINIMAL -> DetailLevel.MINIMAL;
            };
        }
        return getDetailLevel();
    }

    // ========== GESTIÓN DE LISTENERS ==========

    /**
     * Registra un listener para recibir notificaciones de cambio de estado.
     */
    public void addListener(InteractionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Elimina un listener previamente registrado.
     */
    public void removeListener(InteractionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifica a todos los listeners del cambio de estado.
     */
    private void notifyListeners() {
        DetailLevel level = getEffectiveDetailLevel();
        for (InteractionListener listener : listeners) {
            listener.onInteractionStateChanged(isInteracting, level);
        }
    }

    // ========== UTILIDADES ==========

    /**
     * Determina si se deben mostrar las etiquetas según el estado actual.
     * Durante interacción o zoom muy alejado, las etiquetas se ocultan.
     *
     * @return true si las etiquetas deben mostrarse
     */
    public boolean shouldShowLabels() {
        if (isInteracting) return false;
        return currentScale > 0.3; // No mostrar labels si zoom < 30%
    }

    /**
     * Determina si se debe usar anti-aliasing según el estado actual.
     * Durante interacción, se desactiva para mejorar rendimiento.
     *
     * @return true si se debe usar anti-aliasing
     */
    public boolean shouldUseAntiAliasing() {
        return !isInteracting;
    }

    /**
     * Determina si se deben mostrar las imágenes/iconos de los nodos.
     * A zoom muy alejado o durante interacción, se ocultan.
     *
     * @return true si se deben mostrar imágenes
     */
    public boolean shouldShowImages() {
        if (isInteracting) return false;
        return currentScale > 0.4;
    }
}

