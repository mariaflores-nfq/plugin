package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

import java.util.ArrayList;
import java.util.List;

public class IssueTreatmentData {
    private String id;
    private String exceptionClass;
    private String exceptionMethod;
    private Integer skipLimit;
    private boolean skippable;
    private String exceptionCodeMethod;
    private String issueCode;
    private String technicalCode;
    private List<ExceptionData> exceptions;

    public IssueTreatmentData(String id){
        this.id = id;
        this.exceptionClass = "";
        this.exceptionMethod = "NewIssue";
        this.skipLimit = 0;
        this.skippable = false;
        this.exceptionCodeMethod = "";
        this.issueCode = "";
        this.technicalCode = "";
        this.exceptions = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getExceptionClass() { return exceptionClass; }
    public void setExceptionClass(String exceptionClass) { this.exceptionClass = exceptionClass; }
    public String getExceptionMethod() { return exceptionMethod; }
    public void setExceptionMethod(String exceptionMethod) { this.exceptionMethod = exceptionMethod; }
    public Integer getSkipLimit() { return skipLimit; }
    public void setSkipLimit(Integer skipLimit) { this.skipLimit = skipLimit; }
    public boolean getSkippable() { return skippable; }
    public void setSkippable(boolean skippable) { this.skippable = skippable; }
    public String getExceptionCodeMethod() { return exceptionCodeMethod; }
    public void setExceptionCodeMethod(String exceptionCodeMethod) { this.exceptionCodeMethod = exceptionCodeMethod; }
    public String getIssueCode() { return issueCode; }
    public void setIssueCode(String issueCode) { this.issueCode = issueCode; }
    public String getTechnicalCode() { return technicalCode; }
    public void setTechnicalCode(String technicalCode) { this.technicalCode = technicalCode; }
    public List<ExceptionData> getExceptions() { return exceptions; }
    public void setExceptions(List<ExceptionData> exceptions) { this.exceptions = exceptions; }
}
