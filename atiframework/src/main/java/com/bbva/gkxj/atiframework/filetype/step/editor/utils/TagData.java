package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

/**
 * Modelo de datos para una etiqueta (tag) de Epsilon.
 * Contiene el identificador, el nombre de clave y el tipo de valor (Fixed Value, Step Parameter o Script)
 * junto con su contenido correspondiente.
 */
public class TagData {

    /** Identificador único del tag. */
    private String id;


    /** Tipo de valor seleccionado: "", "Fixed Value", "Step Parameter" o "Script". */
    private String type;

    /** Valor fijo del tag (usado cuando type = "Fixed Value"). */
    private String fixedValue;

    /** Nombre del parámetro de step (usado cuando type = "Step Parameter"). */
    private String stepParameter;

    /** Script del tag (usado cuando type = "Script"). */
    private String script;

    /**
     * Constructor de la clase TagData.
     *
     * @param id Identificador único del tag.
     */
    public TagData(String id) {
        this.id = id;
        this.type = "";
        this.fixedValue = "";
        this.stepParameter = "";
        this.script = "";
    }

    /**
     * Obtiene el identificador del tag.
     *
     * @return Identificador del tag.
     */
    public String getId() { return id; }

    /**
     * Establece el identificador del tag.
     *
     * @param id Nuevo identificador del tag.
     */
    public void setId(String id) { this.id = id; }


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
     * Obtiene el valor fijo del tag.
     *
     * @return Valor fijo.
     */
    public String getFixedValue() { return fixedValue; }

    /**
     * Establece el valor fijo del tag.
     *
     * @param fixedValue Valor fijo.
     */
    public void setFixedValue(String fixedValue) { this.fixedValue = fixedValue; }

    /**
     * Obtiene el nombre del parámetro de step del tag.
     *
     * @return Nombre del parámetro de step.
     */
    public String getStepParameter() { return stepParameter; }

    /**
     * Establece el nombre del parámetro de step del tag.
     *
     * @param stepParameter Nombre del parámetro de step.
     */
    public void setStepParameter(String stepParameter) { this.stepParameter = stepParameter; }

    /**
     * Obtiene el script del tag.
     *
     * @return Script.
     */
    public String getScript() { return script; }

    /**
     * Establece el script del tag.
     *
     * @param script Script.
     */
    public void setScript(String script) { this.script = script; }

    /**
     * Devuelve el valor visible en la columna de la tabla según el tipo seleccionado.
     * Tags no tienen campo "key" en el JSON, solo tienen el valor del tipo.
     *
     * @return Valor a mostrar en la tabla.
     */
    public String getKey() {
        return switch (type) {
            case "Fixed Value" -> fixedValue != null ? fixedValue : "";
            case "Step Parameter" -> stepParameter != null ? stepParameter : "";
            case "Script" -> script != null ? script : "";
            default -> "";
        };
    }
}
