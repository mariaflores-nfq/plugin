package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

public class JasperReportData {

    private String id;
    private String sheetName;
    private String template;
    private String sqlQuery;

    public JasperReportData(String id){
        this.id = id;
        this.sheetName= "NewSheet";
        this.template = "";
        this.sqlQuery = "";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    public String getSqlQuery() { return sqlQuery; }
    public void setSqlQuery(String sqlQuery) { this.sqlQuery = sqlQuery; }

}