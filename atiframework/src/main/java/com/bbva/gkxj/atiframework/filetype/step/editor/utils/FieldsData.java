package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

/**
 * Modelo de datos para los campos de configuración de un paso ETL.
 * Incluye atributos comunes como nombre, tipo, formato y delimitadores.
 * Permite almacenar y manipular la información de los campos configurados por el usuario en la interfaz de edición de pasos ETL.
 * Es aplicable a cualquier tipo de panel (fijo, XML, API Request, etc.).
 */
public class FieldsData {

    /**
     * Identificador único del campo.
     */
    private String id;

    /**
     * Nombre del campo.
     */
    private String name;

    /**
     * Tipo general del campo.
     */
    private String type;

    /**
     * Nombre específico del campo en el contexto del paso.
     */
    private String fieldName;

    /**
     * Nombre interno del campo en el CSV.
     */
    private String csvInternalName;

    /**
     * Tipo de dato del campo.
     */
    private String fieldType;

    /**
     * Expresión regular asociada al campo.
     */
    private String fieldRegex;

    /**
     * Formato del campo.
     */
    private String fieldFormat;

    /**
     * Delimitador decimal utilizado en el campo.
     */
    private String decimalDelimiter;

    /**
     * Delimitador de agrupación utilizado en el campo.
     */
    private String groupingDelimiter;

    /**
     * Longitud máxima del campo.
     */
    private String fieldLength;

    /**
     * País asociado al campo.
     */
    private String country;

    /**
     * Idioma asociado al campo.
     */
    private String language;

    /**
     * Expresión XPath para campos XML.
     */
    private String xpath;

    /**
     * Ruta JSON para campos de tipo JSON.
     */
    private String jsonPath;

    /**
     * Constructor de la clase FieldsData.
     * @param id Identificador único del campo.
     */
    public FieldsData(String id) {
        this.id = id;
    }

    /**
     * Obtiene el identificador del campo.
     * @return Identificador del campo.
     */
    public String getId() {
        return id;
    }

    /**
     * Establece el identificador del campo.
     * @param id Identificador del campo.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre del campo.
     * @return Nombre del campo.
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del campo.
     * @param name Nombre del campo.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtiene el tipo de dato del campo.
     * @return Tipo de dato del campo.
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * Establece el tipo de dato del campo.
     * @param fieldType Tipo de dato del campo.
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Obtiene el nombre específico del campo.
     * @return Nombre específico del campo.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Establece el nombre específico del campo.
     * @param fieldName Nombre específico del campo.
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Obtiene el tipo general del campo.
     * @return Tipo general del campo.
     */
    public String getType() {
        return type;
    }

    /**
     * Establece el tipo general del campo.
     * @param type Tipo general del campo.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Obtiene el nombre interno del campo en el CSV.
     * @return Nombre interno del campo en el CSV.
     */
    public String getCsvInternalName() {
        return csvInternalName;
    }

    /**
     * Establece el nombre interno del campo en el CSV.
     * @param csvInternalName Nombre interno del campo en el CSV.
     */
    public void setCsvInternalName(String csvInternalName) {
        this.csvInternalName = csvInternalName;
    }

    /**
     * Obtiene la expresión regular del campo.
     * @return Expresión regular del campo.
     */
    public String getFieldRegex() {
        return fieldRegex;
    }

    /**
     * Establece la expresión regular del campo.
     * @param fieldRegex Expresión regular del campo.
     */
    public void setFieldRegex(String fieldRegex) {
        this.fieldRegex = fieldRegex;
    }

    /**
     * Obtiene el formato del campo.
     * @return Formato del campo.
     */
    public String getFieldFormat() {
        return fieldFormat;
    }

    /**
     * Establece el formato del campo.
     * @param fieldFormat Formato del campo.
     */
    public void setFieldFormat(String fieldFormat) {
        this.fieldFormat = fieldFormat;
    }

    /**
     * Obtiene el delimitador decimal del campo.
     * @return Delimitador decimal del campo.
     */
    public String getDecimalDelimiter() {
        return decimalDelimiter;
    }

    /**
     * Establece el delimitador decimal del campo.
     * @param decimalDelimiter Delimitador decimal del campo.
     */
    public void setDecimalDelimiter(String decimalDelimiter) {
        this.decimalDelimiter = decimalDelimiter;
    }

    /**
     * Obtiene la longitud máxima del campo.
     * @return Longitud máxima del campo.
     */
    public String getFieldLength() {
        return fieldLength;
    }

    /**
     * Establece la longitud máxima del campo.
     * @param fieldLength Longitud máxima del campo.
     */
    public void setFieldLength(String fieldLength) {
        this.fieldLength = fieldLength;
    }

    /**
     * Obtiene el país asociado al campo.
     * @return País asociado al campo.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Establece el país asociado al campo.
     * @param country País asociado al campo.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Obtiene el delimitador de agrupación del campo.
     * @return Delimitador de agrupación del campo.
     */
    public String getGroupingDelimiter() {
        return groupingDelimiter;
    }

    /**
     * Establece el delimitador de agrupación del campo.
     * @param groupingDelimiter Delimitador de agrupación del campo.
     */
    public void setGroupingDelimiter(String groupingDelimiter) {
        this.groupingDelimiter = groupingDelimiter;
    }

    /**
     * Obtiene el idioma asociado al campo.
     * @return Idioma asociado al campo.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Establece el idioma asociado al campo.
     * @param language Idioma asociado al campo.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Obtiene la expresión XPath del campo.
     * @return Expresión XPath del campo.
     */
    public String getXpath() {
        return xpath;
    }

    /**
     * Establece la expresión XPath del campo.
     * @param xpath Expresión XPath del campo.
     */
    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    /**
     * Obtiene la ruta JSON del campo.
     * @return Ruta JSON del campo.
     */
    public String getJsonPath() {
        return jsonPath;
    }

    /**
     * Establece la ruta JSON del campo.
     * @param jsonPath Ruta JSON del campo.
     */
    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }
}
