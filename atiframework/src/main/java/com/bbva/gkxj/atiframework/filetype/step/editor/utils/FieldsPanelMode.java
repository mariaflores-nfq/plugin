package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

/**
 * Enum que representa los diferentes modos de visualización de campos en el panel de edición.
 * Cada modo corresponde a un tipo específico de campos (fijos, XML, header, JSON) y define
 * tanto la clave del array JSON que contiene la lista de campos como el nombre de la tabla
 * que se muestra en la UI.
 * Este enum facilita la gestión de los diferentes tipos de campos y su representación en la interfaz,
 * permitiendo una implementación más limpia y mantenible del código relacionado con la edición de pasos.
 */
public enum FieldsPanelMode {
    FIXED("fixedFieldList", "Fixed",false, false),
    XML("xmlFieldList", "XML",true, true),
    HEADER("headerFieldList", "Header",true,true),
    JSON("jsonFieldList", "JSON",true,true),
    CSV("csvFieldList", "CSV",false,false);

    private final String listName;
    private final String tableName;
    private final Boolean supportComplexTypes;
    private final Boolean usesPathExpressions;

    public Boolean usesPathExpressions() {
        return usesPathExpressions;
    }

    public Boolean supportsComplexTypes() {
        return supportComplexTypes;
    }

    FieldsPanelMode(String listName, String tableName, Boolean supportComplexTypes, Boolean usesPathExpressions) {
        this.listName = listName;
        this.tableName = tableName;
        this.supportComplexTypes = supportComplexTypes;
        this.usesPathExpressions = usesPathExpressions;
    }

    /**
     * Clave del array JSON que contiene la lista de campos.
     */
    public String fieldListKey() {
        return listName;
    }

    /**
     * Nombre de la tabla para mostrar en la UI.
     *
     * @return Nombre de la tabla según el modo.
     */
    public String fieldTableName() {
        return tableName;
    }
}
