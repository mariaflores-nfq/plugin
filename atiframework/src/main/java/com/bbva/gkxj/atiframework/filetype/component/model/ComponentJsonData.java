package com.bbva.gkxj.atiframework.filetype.component.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de datos exclusivo para la serialización y deserialización de archivos de tipo Component (.comp).
 * <p>
 * Representa la estructura JSON de un único nodo, almacenando sus propiedades técnicas,
 * metadatos y la configuración específica de sus pestañas (como Aggregators, JMS, Async API o Filters).
 * </p>
 */
public class ComponentJsonData {

    // =================================================================================
    // PROPIEDADES DEL MODELO
    // =================================================================================

    // --- Metadatos Básicos ---

    /** Identificador único interno del componente. */
    private String id;

    /** Código de negocio o nombre técnico asignado al componente. */
    private String componentCode;

    /** Tipo principal del nodo (ej. Input Adapter, Output Adapter, Filter, Aggregator). */
    private String type;

    /** Subtipo específico del nodo (ej. JMS, Async API, Workstatement). */
    private String subtype;

    /** Versión actual del componente. */
    private String version;

    /** Estado del componente (ej. Draft, Final). */
    private String status;

    /** Descripción funcional o técnica del propósito del componente. */
    private String description;

    // --- Configuración para Aggregators ---

    /** Estrategia utilizada para correlacionar los mensajes en un Aggregator. */
    private String correlationType;

    /** Estrategia utilizada para agregar el contenido de los mensajes. */
    private String aggregationType;

    /** Estrategia que define cuándo se liberan los mensajes agregados. */
    private String releaseType;

    // --- Configuración de Pestañas Dinámicas ---

    /** Configuración exclusiva cuando el tipo de componente es Filter. */
    private FilterConfig filterConfig;

    /** Configuración exclusiva cuando el tipo es Adapter y el subtipo es JMS. */
    private AdapterConfig jmsConfig;

    /** Configuración exclusiva cuando el tipo es Adapter y el subtipo es Async API. */
    private AdapterConfig asyncApiConfig;

    /** Configuración para el tab Enricher */
    private WorkStatementConfig workStatementConfig;
    // =================================================================================
    // GETTERS Y SETTERS PRINCIPALES
    // =================================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getComponentCode() { return componentCode; }
    public void setComponentCode(String componentCode) { this.componentCode = componentCode; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubtype() { return subtype; }
    public void setSubtype(String subtype) { this.subtype = subtype; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AdapterConfig getJmsConfig() { return jmsConfig; }
    public void setJmsConfig(AdapterConfig jmsConfig) { this.jmsConfig = jmsConfig; }

    public AdapterConfig getAsyncApiConfig() { return asyncApiConfig; }
    public void setAsyncApiConfig(AdapterConfig asyncApiConfig) { this.asyncApiConfig = asyncApiConfig; }

    public FilterConfig getFilterConfig() { return filterConfig; }
    public void setFilterConfig(FilterConfig filterConfig) { this.filterConfig = filterConfig; }

    // =================================================================================
    // CLASES INTERNAS (ESTRUCTURAS DE DATOS JSON)
    // =================================================================================
    public static class WorkStatementConfig {
        public List<WorkStatementData> workStatements = new ArrayList<>();
    }

    public static class WorkStatementData {

        public String wsCode;
        public String description;
        public String queryCode;
        public String shouldBeExecuted;
        public List<String> wsDependencyList = new ArrayList<>();
        public String dbSource;
        public String collectionName;
        public String sqlQuery;
        public List<String> mongoAggregatePipelines ;
        public List<WsInputParameter> enricherInputParameters = new ArrayList<>();
        public String wsType;
        public String queryType;
        public List<WsOutputParameter> enricherOutputFields;
        public CacheInfo cacheInfo;
        public List<WsScriptData> enrichScriptList;
    }

    public static class WsInputParameter extends AdapterFieldData {
        public String inputValueType; // PAYLOAD_PATH, FIXED_VALUE
        public String description;
    }

    public static class WsOutputParameter {
        public String fieldName;
        public String description;
        public String payloadPath;
    }

    public static class WsScriptData {
        public String fieldName;
        public String description;
        public String payloadPath;
        public String script;
    }

    public static class CacheInfo {
        public String name;
        public Long ttl;
        public Integer size;
    }
    /**
     * Estructura interna genérica para la configuración de adaptadores (JMS o Async API).
     * Agrupa propiedades de conexión y listas de campos organizadas por formato de mensaje.
     */
    public static class AdapterConfig {
        // --- Campos de JMS ---
        /** Nombre de la conexión JMS (ej. Operation, Admin). */
        public String jmsConnection;

        /** Nombre de la cola de mensajería (Queue). */
        public String queueName;

        // --- Campo de Async API ---
        /** Nombre de la clase Java que implementa el publisher/subscriber en Async API. */
        public String javaClassName;

        // --- Campos compartidos ---
        /** Formato del mensaje procesado (XML, JSON, CSV). */
        public String messageType;

        /** Indica si la ejecución de este adaptador es crítica para el flujo. */
        public boolean isCritical;

        // --- Buffers de campos por formato de mensaje ---
        /** Lista de campos configurados para procesar mensajes en formato XML. */
        public List<AdapterFieldData> xmlFields = new ArrayList<>();

        /** Lista de campos configurados para procesar mensajes en formato JSON. */
        public List<AdapterFieldData> jsonFields = new ArrayList<>();

        /** Lista de campos configurados para procesar mensajes en formato CSV. */
        public List<AdapterFieldData> csvFields = new ArrayList<>();
    }

    /**
     * Representa la definición detallada de un único campo (Field) para las operaciones
     * de extracción o generación de payloads en los adaptadores.
     */
    public static class AdapterFieldData {
        // --- Datos Básicos y de Extracción ---
        public String fieldName;
        public String payloadPath;
        public Integer priority;
        public String fieldType;
        public String extractionType;
        public String extractionValue;

        // --- Configuración Específica de Formato (Numéricos, Fechas, etc.) ---
        public String fieldFormat;
        public String fieldLength;
        public String regularExpression;
        public String decimalDelimiter;
        public String groupingDelimiter;
        public String language;
        public String country;
        public String timeZone;

        // --- Scripts Personalizados ---
        public String script;
    }

    /**
     * Contenedor para la configuración del nodo Filter.
     * Agrupa todos los scripts de filtrado que se ejecutarán en este componente.
     */
    public static class FilterConfig {
        /** Lista de filtros (scripts) configurados. */
        public List<FilterData> filters = new ArrayList<>();
    }

    /**
     * Representa un script de filtrado individual dentro de un componente Filter.
     */
    public static class FilterData {
        /** Código identificador único del filtro. */
        public String filterCode;

        /** Código JavaScript que evalúa si el mensaje debe ser filtrado. */
        public String script;
    }
}