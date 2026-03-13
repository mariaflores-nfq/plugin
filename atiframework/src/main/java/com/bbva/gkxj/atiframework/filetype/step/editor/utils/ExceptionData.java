package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

public class ExceptionData {
    String id;
    String errorCode;
    String issueCode;
    String technicalCode;
    Integer skipLimit;
    Boolean skippable;

    public ExceptionData(String id, String exceptionValue) {
        this.id = id;
        this.errorCode = "New Code";
        this.issueCode = "";
        this.technicalCode = "";
        this.skipLimit = 0;
    }


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) {this.errorCode = errorCode;}
    public String getIssueCode() { return issueCode; }
    public void setIssueCode(String issueCode) {this.issueCode = issueCode;}
    public String getTechnicalCode() { return technicalCode; }
    public void  setTechnicalCode(String technicalCode) { this.technicalCode = technicalCode;}
    public Integer getSkipLimit() { return skipLimit; }
    public void setSkipLimit(Integer skipLimit) { this.skipLimit = skipLimit; }
    public Boolean getSkippable() { return skippable; }
    public void setSkippable(Boolean skippable) { this.skippable = skippable; }
}
