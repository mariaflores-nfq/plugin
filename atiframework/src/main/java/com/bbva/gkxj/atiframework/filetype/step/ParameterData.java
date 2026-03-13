package com.bbva.gkxj.atiframework.filetype.step;

public class ParameterData {

    String paramName;
    String batchName;

    public ParameterData(String paramName, String filterValue) {
        this.paramName = paramName;
        this.batchName = filterValue;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
}

