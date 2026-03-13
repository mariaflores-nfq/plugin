package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

/**
 * Modelo de datos para un elemento de metadatos de Epsilon.
 * Contiene el identificador y el tipo de valor (Fixed Value, Step Parameter o Script)
 * junto con su contenido correspondiente.
 */
public class MetadataData {

    /** Identificador único del metadato. */
    private String id;

    /** Nombre de la clave del metadato (campo "key" en el JSON). */
    private String keyName;

    /** Tipo de valor seleccionado: "", "Fixed Value", "Step Parameter" o "Script". */
    private String type;

    /** Valor fijo del metadato (usado cuando type = "Fixed Value"). */
    private String fixedValue;

    /** Nombre del parámetro de step (usado cuando type = "Step Parameter"). */
    private String stepParameter;

    /** Script del metadato (usado cuando type = "Script"). */
    private String script;

    /**
     * Constructor de la clase MetadataData.
     *
     * @param id Identificador único del metadato.
     */
    public MetadataData(String id) {
        this.id = id;
        this.keyName = "";
        this.type = "";
        this.fixedValue = "";
        this.stepParameter = "";
        this.script = "";
    }

    /**
     * Obtiene el identificador del metadato.
     *
     * @return Identificador del metadato.
     */
    public String getId() { return id; }

    /**
     * Establece el identificador del metadato.
     *
     * @param id Nuevo identificador del metadato.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Obtiene el nombre de la clave del metadato.
     *
     * @return Nombre de la clave.
     */
    public String getKeyName() { return keyName; }

    /**
     * Establece el nombre de la clave del metadato.
     *
     * @param keyName Nombre de la clave.
     */
    public void setKeyName(String keyName) { this.keyName = keyName; }

    /**
     * Obtiene el tipo de valor seleccionado.
     *
     * @return Tipo de valor.
     */
    public String getType() { return type; }

    /**
     * Establece el tipo de valor seleccionado.
     *
     * @param type Tipo de valor.
     */
    public void setType(String type) { this.type = type; }

    /**
     * Obtiene el valor fijo del metadato.
     *
     * @return Valor fijo.
     */
    public String getFixedValue() { return fixedValue; }

    /**
     * Establece el valor fijo del metadato.
     *
     * @param fixedValue Valor fijo.
     */
    public void setFixedValue(String fixedValue) { this.fixedValue = fixedValue; }

    /**
     * Obtiene el nombre del parámetro de step del metadato.
     *
     * @return Nombre del parámetro de step.
     */
    public String getStepParameter() { return stepParameter; }

    /**
     * Establece el nombre del parámetro de step del metadato.
     *
     * @param stepParameter Nombre del parámetro de step.
     */
    public void setStepParameter(String stepParameter) { this.stepParameter = stepParameter; }

    /**
     * Obtiene el script del metadato.
     *
     * @return Script.
     */
    public String getScript() { return script; }

    /**
     * Establece el script del metadato.
     *
     * @param script Script.
     */
    public void setScript(String script) { this.script = script; }

    /**
     * Devuelve el valor visible en la columna de la tabla según el tipo activo.
     *
     * @return Valor a mostrar en la tabla.
     */
    public String getKey() {
        return keyName.isEmpty() ? switch (type) {
            case "Fixed Value" -> fixedValue;
            case "Step Parameter" -> stepParameter;
            case "Script" -> script;
            default -> "";
        } : keyName;
    }
}