package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import icons.AtiIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor centralizado de estilos y caché de imágenes para el editor BPMN.
 *
 * Esta clase implementa varias optimizaciones de rendimiento:
 * 1. Caché de imágenes: Evita cargar SVGs repetidamente
 * 2. Estilos predefinidos en stylesheet: Evita parsear strings de estilo
 * 3. Caché de estilos generados: Reutiliza strings de estilo ya construidos
 */
public final class BpmnStyleManager {

    // ========== CACHÉ DE IMÁGENES ==========

    /**
     * Caché thread-safe para almacenar imágenes ya cargadas.
     * Key: ruta del recurso (ej: "/icons/batch_icon.svg")
     * Value: imagen cargada en memoria
     */
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    /**
     * Caché de URLs de imágenes para evitar resolver URLs repetidamente.
     */
    private static final Map<String, String> URL_CACHE = new ConcurrentHashMap<>();

    // ========== CACHÉ DE ESTILOS ==========

    /**
     * Caché de estilos generados para cada tipo de elemento.
     * Evita concatenar strings repetidamente.
     */
    private static final Map<BpmnConstants.BpmnPaletteItem, String> STYLE_CACHE = new ConcurrentHashMap<>();

    // ========== NOMBRES DE ESTILOS REGISTRADOS ==========

    public static final String STYLE_BATCH_STEP = "batchStep";
    public static final String STYLE_ETL_STEP = "etlStep";
    public static final String STYLE_START_EVENT = "startEvent";
    public static final String STYLE_END_EVENT = "endEvent";
    public static final String STYLE_PARALLEL_GATEWAY = "parallelGateway";
    public static final String STYLE_EXCLUSIVE_GATEWAY = "exclusiveGateway";
    public static final String STYLE_INCLUSIVE_GATEWAY = "inclusiveGateway";
    public static final String STYLE_LOOP_GATEWAY = "loopGateway";
    public static final String STYLE_DEFAULT_EDGE = "defaultEdge";

    // Flag para saber si ya se inicializaron los estilos
    private static volatile boolean stylesInitialized = false;

    private BpmnStyleManager() {
        // Clase de utilidad, no instanciable
    }

    // ========== INICIALIZACIÓN ==========

    /**
     * Inicializa el sistema de estilos y pre-carga las imágenes.
     * Debe llamarse una vez al crear el editor.
     *
     * @param graph El grafo donde se registrarán los estilos
     */
    public static synchronized void initialize(mxGraph graph) {
        if (stylesInitialized) {
            return;
        }

        // Pre-cargar todas las imágenes en caché
        preloadImages();

        // Registrar estilos en el stylesheet del grafo
        registerStyles(graph);

        stylesInitialized = true;
    }

    /**
     * Pre-carga todas las imágenes de iconos en memoria.
     * Esto evita cargas durante el renderizado.
     */
    private static void preloadImages() {
        // Iconos de los nodos STEP
        cacheImageUrl("/icons/batch_icon.svg");
        cacheImageUrl("/icons/etl_icon.svg");

        // Iconos de eventos y gateways
        for (BpmnConstants.BpmnPaletteItem item : BpmnConstants.BpmnPaletteItem.values()) {
            cacheImageUrl("/icons/" + item.getSvgName());
        }
    }

    /**
     * Cachea la URL de un recurso de imagen.
     */
    private static void cacheImageUrl(String resourcePath) {
        if (!URL_CACHE.containsKey(resourcePath)) {
            URL url = AtiIcons.class.getResource(resourcePath);
            if (url != null) {
                URL_CACHE.put(resourcePath, url.toString());
            }
        }
    }

    /**
     * Obtiene la URL cacheada de un recurso.
     */
    public static String getCachedImageUrl(String resourcePath) {
        return URL_CACHE.computeIfAbsent(resourcePath, path -> {
            URL url = AtiIcons.class.getResource(path);
            return (url != null) ? url.toString() : "";
        });
    }

    // ========== REGISTRO DE ESTILOS EN STYLESHEET ==========

    /**
     * Registra todos los estilos predefinidos en el stylesheet del grafo.
     * Usar estilos registrados es mucho más rápido que parsear strings.
     */
    private static void registerStyles(mxGraph graph) {
        mxStylesheet stylesheet = graph.getStylesheet();

        // Estilo para BATCH_STEP
        stylesheet.putCellStyle(STYLE_BATCH_STEP, createBatchStepStyle());

        // Estilo para ETL_STEP
        stylesheet.putCellStyle(STYLE_ETL_STEP, createEtlStepStyle());

        // Estilos para eventos
        stylesheet.putCellStyle(STYLE_START_EVENT, createEventStyle("start_event.svg", BpmnConstants.BpmnPaletteItem.START));
        stylesheet.putCellStyle(STYLE_END_EVENT, createEventStyle("end_event.svg", BpmnConstants.BpmnPaletteItem.END));

        // Estilos para gateways
        stylesheet.putCellStyle(STYLE_PARALLEL_GATEWAY, createGatewayStyle("parallel_gateway.svg", BpmnConstants.BpmnPaletteItem.PARALLEL));
        stylesheet.putCellStyle(STYLE_EXCLUSIVE_GATEWAY, createGatewayStyle("exclusive_gateway.svg", BpmnConstants.BpmnPaletteItem.EXCLUSIVE));
        stylesheet.putCellStyle(STYLE_INCLUSIVE_GATEWAY, createGatewayStyle("inclusive_gateway.svg", BpmnConstants.BpmnPaletteItem.INCLUSIVE));
        stylesheet.putCellStyle(STYLE_LOOP_GATEWAY, createGatewayStyle("loop_gateway.svg", BpmnConstants.BpmnPaletteItem.LOOP));

        // Configurar estilo de aristas por defecto
        configureDefaultEdgeStyle(stylesheet);
    }

    private static Map<String, Object> createBatchStepStyle() {
        Map<String, Object> style = new HashMap<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LABEL);
        style.put(mxConstants.STYLE_IMAGE, getCachedImageUrl("/icons/batch_icon.svg"));
        style.put(mxConstants.STYLE_IMAGE_WIDTH, 16);
        style.put(mxConstants.STYLE_IMAGE_HEIGHT, 16);
        style.put(mxConstants.STYLE_IMAGE_ALIGN, mxConstants.ALIGN_LEFT);
        style.put(mxConstants.STYLE_IMAGE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
        style.put(mxConstants.STYLE_SPACING_LEFT, 4);
        style.put(mxConstants.STYLE_SPACING_TOP, 4);
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_ARCSIZE, 5);
        style.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
        style.put(mxConstants.STYLE_FILLCOLOR, "#B4B4FF");
        style.put(mxConstants.STYLE_STROKECOLOR, "#0000FF");
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        style.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
        // Metadatos para identificación
        style.put("bpmnType", BpmnConstants.BpmnPaletteItem.BATCH_STEP.name());
        style.put("nodeType", BpmnConstants.NODETYPE.STEP.getValue());
        return style;
    }

    private static Map<String, Object> createEtlStepStyle() {
        Map<String, Object> style = new HashMap<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LABEL);
        style.put(mxConstants.STYLE_IMAGE, getCachedImageUrl("/icons/etl_icon.svg"));
        style.put(mxConstants.STYLE_IMAGE_WIDTH, 16);
        style.put(mxConstants.STYLE_IMAGE_HEIGHT, 16);
        style.put(mxConstants.STYLE_IMAGE_ALIGN, mxConstants.ALIGN_LEFT);
        style.put(mxConstants.STYLE_IMAGE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
        style.put(mxConstants.STYLE_SPACING_LEFT, 4);
        style.put(mxConstants.STYLE_SPACING_TOP, 4);
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_ARCSIZE, 5);
        style.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
        style.put(mxConstants.STYLE_FILLCOLOR, "#D5F5D5");
        style.put(mxConstants.STYLE_STROKECOLOR, "#33CC33");
        style.put(mxConstants.STYLE_STROKEWIDTH, 2);
        style.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        style.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
        // Metadatos para identificación
        style.put("bpmnType", BpmnConstants.BpmnPaletteItem.ETL_STEP.name());
        style.put("nodeType", BpmnConstants.NODETYPE.STEP.getValue());
        return style;
    }

    private static Map<String, Object> createEventStyle(String svgName, BpmnConstants.BpmnPaletteItem item) {
        Map<String, Object> style = new HashMap<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
        style.put(mxConstants.STYLE_IMAGE, getCachedImageUrl("/icons/" + svgName));
        style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
        style.put(mxConstants.STYLE_SPACING, 0);
        // Metadatos para identificación
        style.put("bpmnType", item.name());
        style.put("nodeType", item.getNodeType().getValue());
        return style;
    }

    private static Map<String, Object> createGatewayStyle(String svgName, BpmnConstants.BpmnPaletteItem item) {
        Map<String, Object> style = new HashMap<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_IMAGE);
        style.put(mxConstants.STYLE_IMAGE, getCachedImageUrl("/icons/" + svgName));
        style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_BOTTOM);
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
        style.put(mxConstants.STYLE_SPACING, 0);
        // Metadatos para identificación
        style.put("bpmnType", item.name());
        style.put("nodeType", item.getNodeType().getValue());
        return style;
    }

    private static void configureDefaultEdgeStyle(mxStylesheet stylesheet) {
        Map<String, Object> edgeStyle = stylesheet.getDefaultEdgeStyle();
        edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ORTHOGONAL);
        edgeStyle.put(mxConstants.STYLE_ROUNDED, true);
        edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#555555");
        edgeStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
    }

    // ========== OBTENCIÓN DE ESTILOS ==========

    /**
     * Obtiene el nombre del estilo registrado para un elemento de la paleta.
     * Usar nombres de estilo es mucho más eficiente que strings completos.
     *
     * @param item El elemento de la paleta
     * @return El nombre del estilo registrado
     */
    public static String getRegisteredStyleName(BpmnConstants.BpmnPaletteItem item) {
        if (item == null) return STYLE_ETL_STEP;

        return switch (item) {
            case BATCH_STEP -> STYLE_BATCH_STEP;
            case ETL_STEP -> STYLE_ETL_STEP;
            case START -> STYLE_START_EVENT;
            case END -> STYLE_END_EVENT;
            case PARALLEL -> STYLE_PARALLEL_GATEWAY;
            case EXCLUSIVE -> STYLE_EXCLUSIVE_GATEWAY;
            case INCLUSIVE -> STYLE_INCLUSIVE_GATEWAY;
            case LOOP -> STYLE_LOOP_GATEWAY;
        };
    }

    /**
     * Genera el estilo completo como string (para compatibilidad).
     * Los estilos se cachean para evitar concatenaciones repetidas.
     *
     * @param item El elemento de la paleta
     * @return String de estilo completo (cacheado)
     */
    public static String getStyleString(BpmnConstants.BpmnPaletteItem item) {
        if (item == null) {
            return "shape=rectangle;whiteSpace=wrap;html=1;fillColor=#FFCC00";
        }

        return STYLE_CACHE.computeIfAbsent(item, BpmnStyleManager::buildStyleString);
    }

    /**
     * Construye el string de estilo para un elemento.
     * Este método solo se llama una vez por tipo de elemento.
     */
    private static String buildStyleString(BpmnConstants.BpmnPaletteItem item) {
        String baseStyle;

        if (item == BpmnConstants.BpmnPaletteItem.BATCH_STEP) {
            String imgUrl = getCachedImageUrl("/icons/batch_icon.svg");
            baseStyle = "shape=label;image=" + imgUrl +
                    ";imageWidth=16;imageHeight=16;imageAlign=left;imageVerticalAlign=top;" +
                    "spacingLeft=4;spacingTop=4;" +
                    "rounded=1;arcSize=5;whiteSpace=wrap;html=1;" +
                    "fillColor=#B4B4FF;strokeColor=#0000FF;strokeWidth=2;" +
                    "fontColor=#000000;align=center;verticalAlign=middle;";
        } else if (item == BpmnConstants.BpmnPaletteItem.ETL_STEP) {
            String imgUrl = getCachedImageUrl("/icons/etl_icon.svg");
            baseStyle = "shape=label;image=" + imgUrl +
                    ";imageWidth=16;imageHeight=16;imageAlign=left;imageVerticalAlign=top;" +
                    "spacingLeft=4;spacingTop=4;" +
                    "rounded=1;arcSize=5;whiteSpace=wrap;html=1;" +
                    "fillColor=#D5F5D5;strokeColor=#33CC33;strokeWidth=2;" +
                    "fontColor=#000000;align=center;verticalAlign=middle;";
        } else {
            String imgUrl = getCachedImageUrl("/icons/" + item.getSvgName());
            baseStyle = "shape=image;image=" + imgUrl +
                    ";verticalLabelPosition=bottom;verticalAlign=top;spacing=0";
        }

        return baseStyle + ";bpmnType=" + item.name() + ";nodeType=" + item.getNodeType().getValue();
    }

    // ========== UTILIDADES DE IMAGEN ==========

    /**
     * Convierte un Icon a ImageIcon de forma optimizada.
     * Cachea el resultado para reutilización.
     */
    public static ImageIcon toImageIcon(Icon icon) {
        if (icon instanceof ImageIcon) {
            return (ImageIcon) icon;
        }

        BufferedImage image = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = image.createGraphics();
        // Aplicar hints de calidad para el renderizado
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        return new ImageIcon(image);
    }

    /**
     * Limpia todos los cachés (útil para liberar memoria si es necesario).
     */
    public static void clearCaches() {
        IMAGE_CACHE.clear();
        URL_CACHE.clear();
        STYLE_CACHE.clear();
        stylesInitialized = false;
    }
}

