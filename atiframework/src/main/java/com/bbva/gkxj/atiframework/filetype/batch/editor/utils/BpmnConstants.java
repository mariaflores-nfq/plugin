package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import icons.AtiIcons;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BpmnConstants {


    /**
     * Convierte un string del JSON (backend) al Enum de la paleta.
     * Maneja compatibilidad con nombres antiguos.
     */
    public static BpmnPaletteItem fromJsonType(String jsonType) {
        if (jsonType == null) return null;
        try {
            return BpmnPaletteItem.valueOf(jsonType);
        } catch (IllegalArgumentException e) {
            return switch (jsonType) {
                case "INITIAL" -> BpmnPaletteItem.START;
                case "FINAL" -> BpmnPaletteItem.END;
                case "PARALLEL" -> BpmnPaletteItem.PARALLEL;
                case "LOOP" -> BpmnPaletteItem.LOOP;
                case "EXCLUSIVE" -> BpmnPaletteItem.EXCLUSIVE;
                case "INCLUSIVE" -> BpmnPaletteItem.INCLUSIVE;
                case "ETL" -> BpmnPaletteItem.ETL_STEP;
                case "BATCH" -> BpmnPaletteItem.BATCH_STEP;
                default -> null;
            };
        }
    }

    /**
     * Convierte el Item de la paleta al String que espera el Backend en el JSON.
     */
    public static String toBackendType(BpmnPaletteItem item) {
        if (item == null) return "ETL";
        return switch (item) {
            case START -> "INITIAL";
            case END -> "FINAL";
            case PARALLEL -> "PARALLEL";
            case EXCLUSIVE -> "EXCLUSIVE";
            case INCLUSIVE -> "INCLUSIVE";
            case LOOP -> "LOOP";
            case BATCH_STEP -> "BATCH";
            case ETL_STEP -> "ETL";
        };
    }

    public enum BpmnPaletteItem {

        // Tools
        //HAND_TOOL("Hand Tool", AtiIcons.HAND_TOOL_ICON, "hand_tool.svg", NODETYPE.NULL),
        //LASSO_TOOL("Lasso Tool", AtiIcons.LASSO_TOOL_ICON, "lasso_tool.svg", NODETYPE.NULL),
        //SPACE_TOOL("Space Tool", AtiIcons.CREATE_REMOVE_SPACE_TOOL_ICON, "create_remove_space_tool.svg", NODETYPE.NULL),
        //CONNECT_TOOL("Connect Tool", AtiIcons.GLOBAL_CONNECT_TOOL_ICON, "global_connect_tool.svg", NODETYPE.NULL),

        // Events
        START("Start Event", AtiIcons.START_EVENT_ICON, "start_event.svg", NODETYPE.INITIAL),
        END("End Event", AtiIcons.END_EVENT_ICON, "end_event.svg", NODETYPE.FINAL),

        // Steps
        BATCH_STEP("Batch Step", AtiIcons.CREATE_BATCH_STEP_ICON, "batch_step.svg", NODETYPE.STEP),
        ETL_STEP("ETL Step", AtiIcons.CREATE_ETL_STEP_ICON, "etl_step.svg", NODETYPE.STEP),

        // Gateways
        PARALLEL("Parallel Gateway", AtiIcons.CREATE_PARALLEL_GATEWAY_ICON, "parallel_gateway.svg", NODETYPE.GATEWAY),
        EXCLUSIVE("Exclusive Gateway", AtiIcons.CREATE_EXCLUSIVE_GATEWAY_ICON, "exclusive_gateway.svg", NODETYPE.GATEWAY),
        INCLUSIVE("Inclusive Gateway", AtiIcons.CREATE_INCLUSIVE_GATEWAY_ICON, "inclusive_gateway.svg", NODETYPE.GATEWAY),
        LOOP("Loop Gateway", AtiIcons.CREATE_LOOP_GATEWAY_ICON, "loop_gateway.svg", NODETYPE.GATEWAY);

        private final String label;
        private final Icon icon;
        private final String svgName;
        private final NODETYPE nodeType;

        BpmnPaletteItem(String label, Icon icon, String svgName, NODETYPE nodeType) {
            this.label = label;
            this.icon = icon;
            this.svgName = svgName;
            this.nodeType = nodeType;
        }

        public String getLabel() { return label; }
        public Icon getIcon() { return icon; }
        public String getSvgName() { return svgName; }
        public NODETYPE getNodeType() { return nodeType; }

        public int getSize() {
            return nodeType.getSize();
        }
    }

    public enum NODETYPE {
        INITIAL("INITIAL", 50),
        STEP("STEP", 100),
        GATEWAY("GATEWAY", 40),
        FINAL("FINAL", 50),
        NULL("NULL", 0);

        private final String value;
        private final int size;

        NODETYPE(String value, int size) {
            this.value = value;
            this.size = size;
        }

        public String getValue() {
            return value;
        }

        public int getSize() {
            return size;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Define qué nodos se pueden crear a continuación según el tipo del origen.
     *
     * @param sourceType nodo de salida
     * @return Posibles elementos a los que conectarse
     */
    public static List<BpmnPaletteItem> getAllowedSuccessors(BpmnConstants.NODETYPE sourceType) {
        return switch (sourceType) {
            case INITIAL, STEP, GATEWAY -> Arrays.asList(
                    BpmnConstants.BpmnPaletteItem.END,
                    BpmnConstants.BpmnPaletteItem.BATCH_STEP,
                    BpmnConstants.BpmnPaletteItem.ETL_STEP,
                    BpmnConstants.BpmnPaletteItem.PARALLEL,
                    BpmnConstants.BpmnPaletteItem.EXCLUSIVE,
                    BpmnConstants.BpmnPaletteItem.INCLUSIVE,
                    BpmnConstants.BpmnPaletteItem.LOOP
            );
            default -> Collections.emptyList();
        };
    }

}
