package com.bbva.gkxj.atiframework.filetype.component.utils;

/**
 * Constantes estáticas para los tipos, subtipos y estrategias de los componentes.
 * <p>
 * Centraliza las cadenas de texto utilizadas en la lógica de negocio, en los desplegables (JComboBox)
 * y en el guardado del JSON. Esto evita errores tipográficos (magic strings) y facilita
 * el mantenimiento si los nombres cambian en el futuro.
 * </p>
 */
public final class ComponentConstants {

    private ComponentConstants() {
        // Constructor privado para evitar instanciación de clase estática
    }

    // --- TIPOS DE COMPONENTE PRINCIPALES ---
    public static final String TYPE_INPUT_ADAPTER = "Input Adapter";
    public static final String TYPE_OUTPUT_ADAPTER = "Output Adapter";
    public static final String TYPE_FILTER = "Filter";
    public static final String TYPE_ENRICHER = "Enricher";
    public static final String TYPE_ROUTER = "Router";
    public static final String TYPE_SPLITTER = "Splitter";
    public static final String TYPE_AGGREGATOR = "Aggregator";
    public static final String[] EXTRACTION_TYPE_COMBO= {"XPath", "JSONPath", "Fixed Value"};
    public static final String[] FIELD_TYPE_LIST= {"STRING", "INTEGER", "LONG", "DOUBLE", "BOOLEAN", "DATE", "JSON_OBJECT",
            "A_STRING", "A_INTEGER", "A_LONG", "A_DOUBLE", "A_BOOLEAN", "A_DATE", "A_JSON_OBJECT"};
    public static final String[] MESSAGE_TYPE_LIST={"", "XML", "JSON", "CSV"};
    /** Array con todos los tipos principales para inicializar el JComboBox general. */
    public static final String[] ALL_TYPES = {
            TYPE_INPUT_ADAPTER, TYPE_OUTPUT_ADAPTER, TYPE_FILTER,
            TYPE_ENRICHER, TYPE_ROUTER, TYPE_SPLITTER, TYPE_AGGREGATOR
    };

    // --- SUBTIPOS (Adapter, Enricher, Splitter) ---
    public static final String SUBTYPE_JMS = "JMS";
    public static final String SUBTYPE_ASYNC_API = "Async API";
    public static final String SUBTYPE_DATABASE = "Database";
    public static final String SUBTYPE_WORKSTATEMENT = "WorkStatement";
    public static final String SUBTYPE_JAVA_CLASS = "Java Class";
    public static final String SUBTYPE_JAVASCRIPT = "Javascript";
    public static final String SUBTYPE_BY_ROOT_ELEMENT = "By Root Element";

    // --- ESTRATEGIAS (Específicas del Aggregator) ---

    // Correlación
    public static final String STRATEGY_HEADER = "Header";
    public static final String STRATEGY_PAYLOAD_PATH = "Payload path";

    // Agregación y Liberación (Release)
    public static final String STRATEGY_MSG_GROUP_SIZE = "Message group size";
    public static final String STRATEGY_TIMEOUT = "Timeout";

    /** Estrategias disponibles para la correlación de mensajes en el Aggregator. */
    public static final String[] CORRELATION_STRATEGIES = {
            STRATEGY_HEADER, STRATEGY_PAYLOAD_PATH, SUBTYPE_JAVASCRIPT
    };

    /** Estrategias disponibles para la agregación de mensajes en el Aggregator. */
    public static final String[] AGGREGATION_STRATEGIES = {
            SUBTYPE_JAVASCRIPT, SUBTYPE_JAVA_CLASS
    };

    /** Estrategias disponibles para la liberación (release) de mensajes en el Aggregator. */
    public static final String[] RELEASE_STRATEGIES = {
            STRATEGY_MSG_GROUP_SIZE, STRATEGY_TIMEOUT, SUBTYPE_JAVASCRIPT, SUBTYPE_JAVA_CLASS
    };
}