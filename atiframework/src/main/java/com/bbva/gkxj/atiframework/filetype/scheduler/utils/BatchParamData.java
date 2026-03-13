package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

public class BatchParamData {
    private String id;
    private String paramName = "NewParam";
    private String queryParam = "";
    private String fixedValue = "";
    private String script = "";

    public BatchParamData(String id) {
        this.id = id;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getParamName() { return paramName; }
    public void setParamName(String paramName) { this.paramName = paramName; }
    public String getQueryParam() { return queryParam; }
    public void setQueryParam(String queryParam) { this.queryParam = queryParam; }
    public String getFixedValue() { return fixedValue; }
    public void setFixedValue(String fixedValue) { this.fixedValue = fixedValue; }
    public String getScript() { return script; }
    public void setScript(String script) { this.script = script; }
}