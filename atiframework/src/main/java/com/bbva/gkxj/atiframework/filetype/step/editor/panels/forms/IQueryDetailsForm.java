package com.bbva.gkxj.atiframework.filetype.step.editor.panels.forms;

import com.google.gson.JsonObject;

import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;

/**
 * Interfaz que define el contrato común para los formularios de detalles de queries.
 */
public interface IQueryDetailsForm {

    /**
     * Carga los datos de una query en los campos del formulario.
     *
     * @param queryData JsonObject con los datos de la query a cargar
     */
    void loadData(JsonObject queryData);

    /**
     * Guarda los datos del formulario en el objeto query.
     *
     * @param queryData JsonObject donde guardar los datos del formulario
     */
    void saveData(JsonObject queryData);

    /**
     * Elimina un parámetro de la query actual.
     *
     * @param rowIndex Índice del parámetro a eliminar.
     */
    void deleteParam(int rowIndex);

    /**
     * Configura los listeners para sincronización automática.
     *
     * @param actionListener Listener para acciones (combos, botones)
     * @param documentListener Listener para cambios de texto
     */
    void setListeners(ActionListener actionListener, DocumentListener documentListener);

    /**
     * Verifica si el formulario está visible.
     *
     * @return {@code true} si el formulario está visible
     */
    boolean isVisible();
}

