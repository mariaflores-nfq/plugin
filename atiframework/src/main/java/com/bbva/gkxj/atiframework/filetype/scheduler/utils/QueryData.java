package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

import java.util.ArrayList;
import java.util.List;

public class QueryData {
    private String id;
    private String queryCode = "NewQuery";
    private String type = "Mongo Query"; // Default
    private String collection = "";
    private String dbSource = "Config";
    private String sqlQuery = "";
    private List<FilterData> filters = new ArrayList<>();

    public QueryData(String id) {
        this.id = id;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getQueryCode() { return queryCode; }
    public void setQueryCode(String queryCode) { this.queryCode = queryCode; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }
    public String getDbSource() { return dbSource; }
    public void setDbSource(String dbSource) { this.dbSource = dbSource; }
    public String getSqlQuery() { return sqlQuery; }
    public void setSqlQuery(String sqlQuery) { this.sqlQuery = sqlQuery; }
    public List<FilterData> getFilters() { return filters; }
    public void setFilters(List<FilterData> filters) { this.filters = filters; }
}