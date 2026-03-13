package com.bbva.gkxj.atiframework.filetype.component.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentJsonData {

    public String getAsyncApiClassName() {
        return asyncApiClassName;
    }

    public void setAsyncApiClassName(String asyncApiClassName) {
        this.asyncApiClassName = asyncApiClassName;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public List<QueryData> getDataSet() {
        return dataSet;
    }

    public void setDataSet(List<QueryData> dataSet) {
        this.dataSet = dataSet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FieldData> getFieldDataList() {
        return fieldDataList;
    }

    public void setFieldDataList(List<FieldData> fieldDataList) {
        this.fieldDataList = fieldDataList;
    }

    public List<FilterMapData> getFilterScriptMap() {
        return filterScriptMap;
    }

    public void setFilterScriptMap(List<FilterMapData> filterScriptMap) {
        this.filterScriptMap = filterScriptMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInputAdapterType() {
        return inputAdapterType;
    }

    public void setInputAdapterType(String inputAdapterType) {
        this.inputAdapterType = inputAdapterType;
    }

    public String getJmsConnector() {
        return jmsConnector;
    }

    public void setJmsConnector(String jmsConnector) {
        this.jmsConnector = jmsConnector;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getOutputAdapterType() {
        return outputAdapterType;
    }

    public void setOutputAdapterType(String outputAdapterType) {
        this.outputAdapterType = outputAdapterType;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getRecordVersion() {
        return recordVersion;
    }

    public void setRecordVersion(String recordVersion) {
        this.recordVersion = recordVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUuaa() {
        return uuaa;
    }

    public void setUuaa(String uuaa) {
        this.uuaa = uuaa;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<WorkStatementData> getWorkStatementList() {
        return workStatementList;
    }

    public void setWorkStatementList(List<WorkStatementData> workStatementList) {
        this.workStatementList = workStatementList;
    }

    // --- Metadatos Básicos ---
    @JsonProperty("_id")
    private String id;
    private String componentCode;
    private String description;
    private String status;
    private String uuaa;
    private String version;
    private String recordVersion;

    // --- Configuración de Adaptadores ---
    private String nodeType;
    private String inputAdapterType;
    private String jmsConnector;
    private String queueName;
    @JsonProperty("isCritical")
    private boolean critical;
    private String messageType;
    private String asyncApiClassName;
    private String outputAdapterType;

    // --- Listas de Configuración ---
    private List<QueryData> dataSet;
    private List<FilterMapData> filterScriptMap;
    private List<FieldData> fieldDataList;
    private List<WorkStatementData> workStatementList;



    // =================================================================================
    // CLASES BASE Y OBJETOS DE VALOR (Reutilizables)
    // =================================================================================

    /** Clase base para unificar campos comunes */
    public abstract static class BaseField {
        public String fieldName;
        public String description;

        public String getPayloadPath() {
            return payloadPath;
        }

        public void setPayloadPath(String payloadPath) {
            this.payloadPath = payloadPath;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String payloadPath;
    }

    /** Objeto de valor para unificar configuraciones de formato */
    public static class FormattingConfig {
        public String fieldRegex;

        public String getFieldFormat() {
            return fieldFormat;
        }

        public void setFieldFormat(String fieldFormat) {
            this.fieldFormat = fieldFormat;
        }

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getGroupingDelimiter() {
            return groupingDelimiter;
        }

        public void setGroupingDelimiter(String groupingDelimiter) {
            this.groupingDelimiter = groupingDelimiter;
        }

        public String getFieldRegex() {
            return fieldRegex;
        }

        public void setFieldRegex(String fieldRegex) {
            this.fieldRegex = fieldRegex;
        }

        public Integer getFieldLength() {
            return fieldLength;
        }

        public void setFieldLength(Integer fieldLength) {
            this.fieldLength = fieldLength;
        }

        public String getDecimalDelimiter() {
            return decimalDelimiter;
        }

        public void setDecimalDelimiter(String decimalDelimiter) {
            this.decimalDelimiter = decimalDelimiter;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String fieldFormat;
        public String decimalDelimiter;
        public String groupingDelimiter;
        public Integer fieldLength;
        public String country;
        public String language;
        public String timeZone;
    }

    // =================================================================================
    // 1. ESTRUCTURA DE CONSULTAS
    // =================================================================================
    public static class QueryData {
        public Integer priority;
        public String type;
        public String operation;
        public String dbSource;
        public String collectionName;
        public String queryCode;
        public String shouldBeExecuted;
        public String options;
        public String filter;
        public String insert;
        public String update;
        public String sqlQuery;

        public List<QueryParameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<QueryParameter> parameters) {
            this.parameters = parameters;
        }

        public String getCollectionName() {
            return collectionName;
        }

        public void setCollectionName(String collectionName) {
            this.collectionName = collectionName;
        }

        public String getDbSource() {
            return dbSource;
        }

        public void setDbSource(String dbSource) {
            this.dbSource = dbSource;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public String getInsert() {
            return insert;
        }

        public void setInsert(String insert) {
            this.insert = insert;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getOptions() {
            return options;
        }

        public void setOptions(String options) {
            this.options = options;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public String getQueryCode() {
            return queryCode;
        }

        public void setQueryCode(String queryCode) {
            this.queryCode = queryCode;
        }

        public String getShouldBeExecuted() {
            return shouldBeExecuted;
        }

        public void setShouldBeExecuted(String shouldBeExecuted) {
            this.shouldBeExecuted = shouldBeExecuted;
        }

        public String getSqlQuery() {
            return sqlQuery;
        }

        public void setSqlQuery(String sqlQuery) {
            this.sqlQuery = sqlQuery;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUpdate() {
            return update;
        }

        public void setUpdate(String update) {
            this.update = update;
        }

        public List<QueryParameter> parameters;
    }

    public static class QueryParameter {
        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getParamType() {
            return paramType;
        }

        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        public String getQueryParam() {
            return queryParam;
        }

        public void setQueryParam(String queryParam) {
            this.queryParam = queryParam;
        }

        public String paramType;
        public String fieldName;
        public String queryParam;
    }

    // =================================================================================
    // 2. ESTRUCTURA DE FILTROS
    // =================================================================================
    public static class FilterMapData {
        public String filterCode;
        public String script;
    }

    // =================================================================================
    // 3. ESTRUCTURA DE CAMPOS (Refactorizada)
    // =================================================================================
    public static class FieldData extends BaseField {
        public String productFpml;
        // fieldName, description y payloadPath vienen heredados de BaseField
        public String xpath;
        public String jsonPath;
        public String csvColumn;
        public Integer priority;
        public String type;
        public String shouldBeExecuted;
        public String outputMessagePath;
        public String outputMessageFixedValue;

        // @JsonUnwrapped "desempaqueta" este objeto en el JSON final.
        // Así el JSON sigue siendo plano, pero tu Java está ordenado.
        @JsonUnwrapped
        public FormattingConfig formattingConfig;
    }

    // =================================================================================
    // 4. ESTRUCTURA DE ENRICHER (Refactorizada)
    // =================================================================================
    public static class WorkStatementData {
        public String wsCode;
        public String description;
        public String queryCode;
        public List<String> wsDependencyList;
        public String shouldBeExecuted;
        public String dbSource;
        public String collectionName;
        public String sqlQuery;
        public List<String> mongoAggregatePipelines;
        public CacheInfo cacheInfo;

        public List<WsInputParameter> enrichInputParameters;
        public List<WsScriptData> enrichScriptList;
        public List<WsOutputParameter> enricherOutputFields;
    }

    public static class WsInputParameter extends BaseField {
        // fieldName, description y payloadPath vienen heredados
        public String type;
        public String enviromentField;
        public String fixedValue;
        @JsonProperty("isMandatory")
        public boolean mandatory;

        // Aquí no usamos @JsonUnwrapped porque en el Excel original
        // fieldExtraConfig SÍ era un objeto anidado.
        @JsonProperty("fieldExtraConfig")
        public FormattingConfig fieldExtraConfig;
    }

    public static class WsOutputParameter extends BaseField {
        // No necesita campos extra, hereda todo lo necesario de BaseField
    }

    public static class WsScriptData extends BaseField {
        public String script;
    }

    public static class CacheInfo {
        public String name;
        public Integer size;
        public Integer ttl;
    }
}