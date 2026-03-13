package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

public enum PanelType {
    FIXED("fixedFile"),
    XML("xmlFile"),
    API_REQUEST("apiRequest"),
    CSV("csvFile");

    private final String value;

    PanelType(String value) {
        this.value = value;
    }

    /**
     * Clave del array JSON que contiene la configuración del panel.
     * @return Clave del array JSON según el tipo de panel.
     */
    public String getValue() {
        return value;
    }
}
