package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

/**
 * Clase que representa los datos de un filtro utilizado en consultas y condiciones.
 */
public class FilterData {
    String id;
    String filterValue;

    public FilterData(String id, String filterValue) {
        this.id = id;
        this.filterValue = filterValue;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}