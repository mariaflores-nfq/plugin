package com.bbva.gkxj.atiframework.filetype.workflow.model;

import java.util.List;

/**
 * Representa el modelo de datos unificado para la persistencia de Workflows en formato JSON.
 * <p>
 * Esta clase se utiliza con un doble propósito:
 * <ul>
 * <li><b>Como objeto raíz:</b> Contiene la configuración global del workflow, variables de entorno,
 * lista de ejecutores y las colecciones de nodos y aristas.</li>
 * <li><b>Como valor de nodo:</b> Actúa como el objeto "User Object" dentro de las celdas de mxGraph,
 * almacenando las propiedades específicas de cada componente (tipo, código, clase Java, etc.)
 * y su posición física en el lienzo (X, Y).</li>
 * </ul>
 * Incluye clases internas estáticas para definir la estructura de las conexiones (EdgeData),
 * ejecutores de tareas (TaskExecutorData) y variables de entorno (EnvironmentVariableData).
 */
public class WorkflowJsonData {

    // --- Metadatos Globales del Workflow ---
    private String workflowCode;
    private Integer recordVersion;
    private String branchId;
    private String branchCode;
    private String uuaa;
    private String status;
    private String startNodeCode;

    // --- Propiedades del Nodo (Componente) ---
    /** Identificador único interno (UUID) utilizado para vincular nodos y aristas en el grafo. */
    private String id;
    /** Código identificador de negocio del componente (ej: "FIL001"). */
    private String componentCode;
    /** Tipo de componente (ej: "FILTER", "ROUTER", "OUTPUT"). */
    private String type;
    private String subtype;
    private String description;
    /** Referencia al código del Task Executor que ejecutará este componente (NO LA LISTA, SOLO EL ID/CODE). */
    private String taskExecutor;
    private String correlationType;
    private String aggregationType;
    private String releaseType;
    private String javaClassName;
    private String rootElement;
    private String queueName;
    private String messageType;

    // --- Geometría del Nodo en el Editor ---
    /** Coordenada X del nodo en el lienzo visual (mxGraph). */
    private Double x;
    /** Coordenada Y del nodo en el lienzo visual (mxGraph). */
    private Double y;

    // --- Estructura del Grafo (Solo relevantes en el objeto RAÍZ) ---
    /** Lista de nodos que componen el flujo de trabajo. */
    private List<WorkflowJsonData> nodeList;
    /** Lista de conexiones entre los nodos. */
    private List<EdgeData> edgeList;

    private List<String> middleInstanceIdList;

    /** Colección de ejecutores de tareas configurados globalmente en el workflow. */
    private List<TaskExecutorData> taskExecutors;

    /** Colección de variables de entorno configuradas globalmente. */
    private List<EnvironmentVariableData> environmentVariables;

    // --- Getters y Setters ---

    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public List<String> getMiddleInstanceIdList() { return middleInstanceIdList; }
    public void setMiddleInstanceIdList(List<String> middleInstanceIdList) { this.middleInstanceIdList = middleInstanceIdList; }

    public String getWorkflowCode() { return workflowCode; }
    public void setWorkflowCode(String workflowCode) { this.workflowCode = workflowCode; }

    public Integer getRecordVersion() { return recordVersion; }
    public void setRecordVersion(Integer recordVersion) { this.recordVersion = recordVersion; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getUuaa() { return uuaa; }
    public void setUuaa(String uuaa) { this.uuaa = uuaa; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartNodeCode() { return startNodeCode; }
    public void setStartNodeCode(String startNodeCode) { this.startNodeCode = startNodeCode; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getComponentCode() { return componentCode; }
    public void setComponentCode(String componentCode) { this.componentCode = componentCode; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubtype() { return subtype; }
    public void setSubtype(String subtype) { this.subtype = subtype; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTaskExecutor() { return taskExecutor; }
    public void setTaskExecutor(String taskExecutor) { this.taskExecutor = taskExecutor; }

    public String getCorrelationType() { return correlationType; }
    public void setCorrelationType(String correlationType) { this.correlationType = correlationType; }

    public String getAggregationType() { return aggregationType; }
    public void setAggregationType(String aggregationType) { this.aggregationType = aggregationType; }

    public String getReleaseType() { return releaseType; }
    public void setReleaseType(String releaseType) { this.releaseType = releaseType; }

    public String getJavaClassName() { return javaClassName; }
    public void setJavaClassName(String javaClassName) { this.javaClassName = javaClassName; }

    public String getRootElement() { return rootElement; }
    public void setRootElement(String rootElement) { this.rootElement = rootElement; }

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public List<WorkflowJsonData> getNodeList() { return nodeList; }
    public void setNodeList(List<WorkflowJsonData> nodeList) { this.nodeList = nodeList; }

    public List<EdgeData> getEdgeList() { return edgeList; }
    public void setEdgeList(List<EdgeData> edgeList) { this.edgeList = edgeList; }

    public List<TaskExecutorData> getTaskExecutors() { return taskExecutors; }
    public void setTaskExecutors(List<TaskExecutorData> taskExecutors) { this.taskExecutors = taskExecutors; }

    public List<EnvironmentVariableData> getEnvironmentVariables() { return environmentVariables; }
    public void setEnvironmentVariables(List<EnvironmentVariableData> environmentVariables) { this.environmentVariables = environmentVariables; }

    /**
     * Limpia las listas globales de la instancia actual.
     * Este método DEBE llamarse antes de meter un nodo en la lista `nodeList` del objeto raíz
     * para evitar que el JSON contenga listas duplicadas de ejecutores, nodos y variables dentro de cada hijo.
     */
    public void clearGlobalListsForNode() {
        this.taskExecutors = null;
        this.environmentVariables = null;
        this.nodeList = null;
        this.edgeList = null;
        this.middleInstanceIdList = null;
    }

    /**
     * Extrae únicamente los códigos de los ejecutores de tareas configurados.
     * Útil para poblar componentes visuales como ComboBoxes.
     * @return Lista de strings con los códigos de los task executors.
     */
    public List<String> getGlobalTaskExecutorCodes() {
        if (taskExecutors == null) return java.util.Collections.emptyList();
        return taskExecutors.stream()
                .map(te -> te.code)
                .filter(code -> code != null && !code.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Estructura de datos para definir variables de entorno en el workflow.
     */
    public static class EnvironmentVariableData {
        public String variableName = "";
        public String type = "String";
        public String regularExpression = "";
        public String fieldLength = "";
        public String description = "";
        public String fixedValue = "";
        public String scriptValue = "";
    }

    /**
     * Estructura de datos para definir un pool de hilos ejecutor (Task Executor).
     */
    public static class TaskExecutorData {
        public String code = "";
        public String namePrefix = "";
        public String description = "";
        public String corePoolSize = "3";
        public String maxPoolSize = "10";
        public String queueCapacity = "50";
    }

    /**
     * Devuelve la representación textual del nodo.
     * mxGraph utiliza este método para renderizar la etiqueta (label) en el lienzo si no hay un Labeller configurado.
     * @return El código del componente o el tipo si el código es nulo.
     */
    @Override
    public String toString() {
        if (componentCode != null && !componentCode.trim().isEmpty()) {
            return componentCode;
        }
        return type != null ? type : "Nodo";
    }

    /**
     * Estructura de datos para guardar las coordenadas de los codos (puntos de control).
     */
    public static class PointData {
        public Double x;
        public Double y;

        public PointData() {}
        public PointData(Double x, Double y) { this.x = x; this.y = y; }
    }

    /**
     * Representa la conexión entre dos nodos del workflow (Arista).
     */
    public static class EdgeData {
        /** ID del nodo origen (coincide con {@link WorkflowJsonData#getId()}). */
        private String sourceId;
        /** ID del nodo destino (coincide con {@link WorkflowJsonData#getId()}). */
        private String targetId;
        /** Tipo de canal (ej: "DIRECT", "QUEUE", "DISCARD"). */
        private String channelType;
        /** Script de enrutamiento opcional si el canal lo requiere. */
        private String scriptRouting;
        /** Tipo del componente origen (usado para validaciones de conexión). */
        private String sourceType;

        // Lista para guardar los codos de la flecha ---
        private List<PointData> points;

        public List<PointData> getPoints() { return points; }
        public void setPoints(List<PointData> points) { this.points = points; }
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }

        public String getSourceId() { return sourceId; }
        public void setSourceId(String sourceId) { this.sourceId = sourceId; }

        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }

        public String getChannelType() { return channelType; }
        public void setChannelType(String channelType) { this.channelType = channelType; }

        public String getScriptRouting() { return scriptRouting; }
        public void setScriptRouting(String scriptRouting) { this.scriptRouting = scriptRouting; }

        /**
         * Representación visual de la arista en el grafo.
         * Si el canal es de descarte, se muestra un símbolo de barra diagonal.
         * @return Símbolo visual de la arista o cadena vacía.
         */
        @Override
        public String toString() {
            if ("DISCARD".equalsIgnoreCase(channelType)) {
                return "/";
            }
            return "";
        }
    }
}