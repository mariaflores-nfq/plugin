package com.bbva.gkxj.atiframework.filetype.workflow.model;

import icons.AtiIcons;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase de utilidad encargada de centralizar todos los estilos visuales del editor de Workflows.
 * <p>
 * Proporciona:
 * <ul>
 * <li>Constantes de color compatibles con el tema de IntelliJ (JBColor).</li>
 * <li>Definiciones de estilos para el motor de renderizado de mxGraph (vértices y aristas).</li>
 * <li>Enumerado de tipos de nodos con sus dimensiones y metadatos visuales.</li>
 * </ul>
 */
public class WorkFlowStyles {

    // --- Colores de la Interfaz de Usuario (Swing) ---
    public static final JBColor UI_PANEL_BG = JBColor.WHITE;
    public static final JBColor UI_BORDER_BG = new JBColor(new Color(100, 150, 200),new Color(0x6592200));
    public static final JBColor UI_HEADER_FOCUS = new JBColor(new Color(200, 200, 200), new Color(0x3C3F41));
    public static final JBColor UI_TEXT_MAIN = new JBColor(new Color(0x333333), new Color(0xCCCCCC));
    public static final JBColor UI_TEXT_SUB = new JBColor(new Color(0x666666), new Color(0x999999));
    public static final JBColor UI_BORDER = new JBColor(new Color(0xD1D1D1), new Color(0x555555));
    public static final JBColor UI_READONLY_BG = new JBColor(new Color(0xF9F9F9), new Color(0x2B2D30));
    public static final JBColor UI_READONLY_TEXT = new JBColor(new Color(0x999999), new Color(0x777777));
    public static final JBColor UI_READONLY_BORDER = new JBColor(new Color(0xEEEEEE), new Color(0x444444));
    public static final JBColor UI_ICON_ACTION = new JBColor(new Color(0x999999), new Color(0xAAAAAA));

    /** Caché interna para iconos procesados. */
    private static final Map<String, ImageIcon> cache = new HashMap<>();

    // --- Definiciones de Estilos de mxGraph (Strings de Configuración) ---

    /** Estilo base para componentes estándar (Filtros, Routers, etc.) con fondo verde suave. */
    private static final String COMPONENT_BASE_STYLE = "shape=label;imageWidth=50;imageHeight=50;imageAlign=left;imageVerticalAlign=top;" +
            "spacingLeft=4;spacingTop=4;" +
            "rounded=1;arcSize=5;whiteSpace=wrap;html=1;" +
            "fillColor=#DDF5DD;strokeColor=#A4DA8B;strokeWidth=2;" +
            "fontColor=#000000;align=center;verticalAlign=middle;";

    /** Estilo específico para sub-flujos de trabajo con fondo amarillento. */
    private static final String WORKFLOW_BASE_STYLE = "shape=label;imageWidth=16;imageHeight=16;" +
            "imageAlign=center;imageVerticalAlign=bottom;" +
            "spacingBottom=10;" +
            "rounded=1;arcSize=5;whiteSpace=wrap;html=1;" +
            "fillColor=#fff3bf;strokeColor=#FFD83C;strokeWidth=2;" +
            "fontColor=#000000;align=center;verticalAlign=middle;";

    /** Estilo minimalista basado únicamente en imagen para adaptadores de entrada y salida. */
    private static final String BASE_STYLE = "shape=image;" +
            "fillColor=none;" +
            "strokeColor=none;" +
            "imageAspect=0;" +
            "verticalLabelPosition=bottom;verticalAlign=top;spacing=0;";

    // --- Estilos de Aristas (Edges) ---
    private static final String EDGE_BASE = "strokeWidth=2;endArrow=block;endFill=1;edgeStyle=orthogonalEdgeStyle;rounded=1;";
    private static final String STYLE_DIRECT = EDGE_BASE + "strokeColor=#000000;";
    private static final String STYLE_QUEUE = EDGE_BASE + "strokeColor=#000000;dashed=1;dashPattern=5 5;";
    private static final String STYLE_DISCARD = EDGE_BASE + "strokeColor=#000000;fontColor=#000000;fontSize=25;fontStyle=1;";
    private static final String STYLE_PREVIOUS = EDGE_BASE + "strokeColor=#000000;dashed=1;endArrow=none;";

    /**
     * Devuelve la cadena de estilo de mxGraph correspondiente según el tipo de canal de conexión.
     * * @param type Tipo de canal (DIRECT, QUEUE, DISCARD, PREVIOUS).
     * @return String de configuración de estilo para la arista.
     */
    public static String getStyleForEdge(String type) {
        if (type == null) return STYLE_DIRECT;

        return switch (type.toUpperCase()) {
            case "DIRECT"  -> STYLE_DIRECT;
            case "QUEUE"   -> STYLE_QUEUE;
            case "DISCARD" -> STYLE_DISCARD;
            case "PREV", "PREVIOUS" -> STYLE_PREVIOUS;
            default        -> STYLE_DIRECT;
        };
    }

    /**
     * Genera dinámicamente el estilo de un vértice (nodo) basándose en su tipo de componente.
     * Mapea el tipo de negocio a una configuración visual que incluye imagen, dimensiones y colores.
     * * @param type El tipo de componente (ej: "FILTER", "INPUT ADAPTER").
     * @return String de configuración de estilo para el vértice.
     */
    public static String getStyleForType(String type) {
        if (type == null) {
            return "shape=rectangle;whiteSpace=wrap;html=1;fillColor=#FFCC00";
        }

        WfNodeType nodeType = WfNodeType.fromLabel(type);
        int iconWidth = 50;
        int iconHeight = 50;

        String dimensionStyle = "imageWidth=" + iconWidth + ";imageHeight=" + iconHeight + ";";
        String imageStyle = "image=" + nodeType.name() + ";";

        String normalizedType = type.toUpperCase().trim();

        switch (normalizedType) {
            case "INPUT":
            case "INPUT ADAPTER":
            case "OUTPUT":
            case "OUTPUT ADAPTER":
                return BASE_STYLE  + imageStyle;
            case "SUBWORKFLOW":
            case "SUB WORKFLOW":
                return WORKFLOW_BASE_STYLE  + imageStyle;
            case "DISCARD":
                return COMPONENT_BASE_STYLE;
            default:
                return COMPONENT_BASE_STYLE + dimensionStyle + imageStyle;
        }
    }

    /**
     * Enumeración de los tipos de nodos disponibles en el flujo de trabajo.
     * Cada tipo define su etiqueta de negocio, iconos asociados y dimensiones por defecto en el lienzo.
     */
    public enum WfNodeType {
        OUTPUT("Output", AtiIcons.OUTPUT_ICON, AtiIcons.OUTPUT_ICON, 50, 94),
        INPUT("Input", AtiIcons.INPUT_ICON, AtiIcons.INPUT_ICON, 50, 94),
        SUBWORKFLOW("Subworkflow", AtiIcons.SUBWORKFLOW_ICON, AtiIcons.SUBWORKFLOW_GRAPH_ICON, 120, 80),
        FILTER("Filter", AtiIcons.FILTER_ICON, AtiIcons.FILTER_GRAPH_ICON, 120, 80),
        ENRICHER("Enricher", AtiIcons.ENRICHER_ICON, AtiIcons.ENRICHER_GRAPH_ICON, 120, 80),
        ROUTER("Router", AtiIcons.ROUTER_ICON, AtiIcons.ROUTER_GRAPH_ICON, 120, 80),
        SPLITTER("Splitter", AtiIcons.SPLITTER_ICON, AtiIcons.SPLITTER_GRAPH_ICON, 120, 80),
        AGGREGATOR("Aggregator", AtiIcons.AGGREGATOR_ICON, AtiIcons.AGGREGATOR_GRAPH_ICON, 120, 80);

        private final String label;
        private final Icon icon;
        private final Icon graphIcon;
        private final int width;
        private final int height;

        WfNodeType(String label, Icon icon, Icon graphIcon, int width, int height) {
            this.label = label;
            this.icon = icon;
            this.graphIcon = graphIcon;
            this.width = width;
            this.height = height;
        }

        public String getLabel() { return label; }
        public Icon getIcon() { return icon; }
        public Icon getGraphIcon() { return graphIcon; }
        public int getHeight() { return height; }
        public int getWidth() { return width; }

        /**
         * Mapea un texto (label) proveniente del modelo o del JSON al tipo de nodo correspondiente.
         * Realiza normalizaciones para manejar alias comunes como "INPUT ADAPTER".
         * * @param text Etiqueta descriptiva del tipo de nodo.
         * @return La instancia de WfNodeType correspondiente (FILTER por defecto).
         */
        public static WfNodeType fromLabel(String text) {
            if (text == null) return FILTER;

            String normalized = text.toUpperCase().trim();

            if (normalized.equals("INPUT ADAPTER")) return INPUT;
            if (normalized.equals("OUTPUT ADAPTER")) return OUTPUT;
            if (normalized.equals("SUB WORKFLOW")) return SUBWORKFLOW;

            for (WfNodeType b : WfNodeType.values()) {
                if (b.label.toUpperCase().equals(normalized)) {
                    return b;
                }
            }
            return FILTER;
        }
    }
}