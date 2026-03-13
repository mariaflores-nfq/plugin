package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

import com.bbva.gkxj.atiframework.filetype.scheduler.editor.panels.SchedulerConditionsPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa los datos de una condición en el editor de condiciones.
 * Utilizada principalmente en {@link SchedulerConditionsPanel}.
 */
public class ConditionData {
    String id;
    String name;
    String type;

    int checkEveryValue;
    String checkEveryUnit;
    boolean forcedAtEnd;
    String script;

    // Campos específicos File/Nova
    String filePath;
    String filePattern;
    String paramName;
    String novaTransferName;

    // Campos Query
    String collection;
    String dbSource;
    String sqlQuery;

    List<FilterData> filtersList;

    public ConditionData(String id) {
        this.id = id;
        this.name = "NewCondition";
        this.type = "File Watcher";
        this.checkEveryValue = 1;
        this.checkEveryUnit = "seconds";
        this.forcedAtEnd = false;
        this.script = "";
        this.sqlQuery = "";
        this.filePath = "";
        this.filePattern = "";
        this.paramName = "";
        this.novaTransferName = "";
        this.collection = "";
        this.dbSource = "";
        this.filtersList = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCheckEveryValue() {
        return checkEveryValue;
    }

    public void setCheckEveryValue(int checkEveryValue) {
        this.checkEveryValue = checkEveryValue;
    }

    public String getCheckEveryUnit() {
        return checkEveryUnit;
    }

    public void setCheckEveryUnit(String checkEveryUnit) {
        this.checkEveryUnit = checkEveryUnit;
    }

    public boolean isForcedAtEnd() {
        return forcedAtEnd;
    }

    public void setForcedAtEnd(boolean forcedAtEnd) {
        this.forcedAtEnd = forcedAtEnd;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getNovaTransferName() {
        return novaTransferName;
    }

    public void setNovaTransferName(String novaTransferName) {
        this.novaTransferName = novaTransferName;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getDbSource() {
        return dbSource;
    }

    public void setDbSource(String dbSource) {
        this.dbSource = dbSource;
    }

    public List<FilterData> getFiltersList() {
        return filtersList;
    }

    public void setFiltersList(List<FilterData> filtersList) {
        this.filtersList = filtersList;
    }
}