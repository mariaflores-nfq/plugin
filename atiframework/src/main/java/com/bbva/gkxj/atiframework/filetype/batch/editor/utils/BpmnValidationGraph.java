
package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import com.mxgraph.view.mxGraph;
import java.util.Objects;
import static com.bbva.gkxj.atiframework.filetype.batch.editor.utils.BpmnConstants.NODETYPE.*;

public class BpmnValidationGraph extends mxGraph {

    public BpmnValidationGraph() {
        super();
        this.setAllowDanglingEdges(false);
        this.setConnectableEdges(true);
        this.setMultigraph(false);
    }

    /**
     * Valida si el grafo permite crear una conexión.
     * JGraphX llama a esto automáticamente al mover/conectar flechas.
     */
    @Override
    public String getEdgeValidationError(Object edge, Object source, Object target) {
        String basic = super.getEdgeValidationError(edge, source, target);
        if (basic != null) return basic;

        if (source == null || target == null) return "Conexión inválida";

        if (!getModel().isVertex(source) || !getModel().isVertex(target)) {
            return "Solo se permiten conexiones entre nodos";
        }

        if (Objects.equals(source, target)) {
            return "No se permite conectar un nodo consigo mismo";
        }

        if (hasDirectEdge(target, source, edge)) {
            return "No se permite conexión recíproca inmediata (A->B y B->A)";
        }

        BpmnConstants.NODETYPE sourceType = getNodeType(source);
        BpmnConstants.NODETYPE targetType = getNodeType(target);

        if (sourceType == NULL || targetType == NULL) {
            return "Elemento no válido para conexión";
        }

        if (STEP.equals(sourceType) && STEP.equals(targetType)) {
            if (hasDirectEdge(target, source, edge)) return "No se pueden crear bucles inmediatos";
        }

        int outSource = countOutgoingExcludingEdge(source, edge);
        int inTarget = countIncomingExcludingEdge(target, edge);

        if (FINAL.equals(sourceType)) return "Un nodo FINAL no puede tener salidas";
        if (INITIAL.equals(sourceType) && outSource >= 1) return "El nodo INICIAL solo puede tener 1 salida";
        if (STEP.equals(sourceType) && outSource >= 1) return "Un STEP solo puede tener 1 salida";

        if (INITIAL.equals(targetType)) return "Un nodo INICIAL no puede tener entradas";
        if (FINAL.equals(targetType) && inTarget >= 1) return "Un nodo FINAL solo puede tener 1 entrada";
        if (STEP.equals(targetType) && inTarget >= 1) return "Un STEP solo puede tener 1 entrada";

        return null;
    }

    /**
     * Verifica 'a priori' si el nodo puede aceptar una NUEVA conexión saliente.
     * @return null si permite salida, mensaje de error si está lleno.
     */
    public String validateSourceCardinality(Object source) {
        if (source == null) return "Origen nulo";

        BpmnConstants.NODETYPE sourceType = getNodeType(source);
        int currentOutgoing = countOutgoingExcludingEdge(source, null);

        if (FINAL.equals(sourceType)) return "Fin de flujo";
        if ((INITIAL.equals(sourceType) || STEP.equals(sourceType)) && currentOutgoing >= 1) {
            return "Salida ocupada";
        }
        return null;
    }

    public int countIncomingExcludingEdge(Object vertex, Object edgeToExclude) {
        Object[] incoming = getIncomingEdges(vertex);
        if (incoming == null) return 0;
        int count = 0;
        for (Object e : incoming) {
            if (e != null && e != edgeToExclude) count++;
        }
        return count;
    }

    public int countOutgoingExcludingEdge(Object vertex, Object edgeToExclude) {
        Object[] outgoing = getOutgoingEdges(vertex);
        if (outgoing == null) return 0;
        int count = 0;
        for (Object e : outgoing) {
            if (e != null && e != edgeToExclude) count++;
        }
        return count;
    }

    public BpmnConstants.NODETYPE getNodeType(Object cell) {
        if (cell == null) return NULL;
        Object v = getCellStyle(cell).get("nodeType");
        if (v != null) {
            try {
                return BpmnConstants.NODETYPE.valueOf(String.valueOf(v));
            } catch (IllegalArgumentException ignored) {}
        }
        String style = getModel().getStyle(cell);
        if (style != null) {
            if (style.contains("nodeType=INITIAL") || style.contains("START")) return INITIAL;
            if (style.contains("nodeType=FINAL") || style.contains("END")) return FINAL;
            if (style.contains("nodeType=STEP")) return STEP;
            if (style.contains("nodeType=GATEWAY")) return GATEWAY;
        }
        return NULL;
    }

    private boolean hasDirectEdge(Object from, Object to, Object edgeToExclude) {
        Object[] outgoing = getOutgoingEdges(from);
        if (outgoing == null) return false;
        for (Object e : outgoing) {
            if (e != null && e != edgeToExclude) {
                Object target = getModel().getTerminal(e, false);
                if (target == to) return true;
            }
        }
        return false;
    }
}