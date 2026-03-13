package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.writer;

import com.bbva.gkxj.atiframework.filetype.step.editor.panels.forms.SqlQueryDetailsForm;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;

/**
 * Panel para configurar queries SQL en el Writer de archivos .step.
 *
 * Este panel extiende {@link AbstractStepWriterQueryPanel}
 */
public class StepWriterSqlPanel extends AbstractStepWriterQueryPanel {


    /**
     * Formulario de detalles específico para queries SQL.
     */
    private SqlQueryDetailsForm detailsForm;

    /**
     * Constructor del panel SQL Writer.
     *
     * @param project Proyecto de IntelliJ IDEA activo
     * @param file    Archivo virtual .step siendo editado
     */
    public StepWriterSqlPanel(Project project, VirtualFile file) {
        super(project, file);
    }

    /**
     * Crea una instancia de {@link SqlQueryDetailsForm} con los campos
     * específicos para queries SQL.
     */
    @Override
    protected JPanel createDetailsForm() {
        detailsForm = new SqlQueryDetailsForm(this::updateQueryCodeInTable);
        return detailsForm;
    }

    /**
     * Actualiza el queryCode en la tabla de queries cuando cambia en el formulario.
     *
     * @param queryCode Nuevo valor del queryCode
     */
    private void updateQueryCodeInTable(String queryCode) {
        int row = queriesTable.getSelectedRow();
        if (row != -1) {
            tableModel.setValueAt(queryCode, row, 1);
        }
    }

    /**
     * Crea un nuevo objeto query con valores por defecto.
     *
     * @return JsonObject con la estructura de una nueva query
     */
    @Override
    protected JsonObject createNewQueryObject() {
        JsonObject newQ = new JsonObject();
        newQ.addProperty("queryCode", "NewQuery");
        newQ.addProperty("type", StepConstants.TYPE_SQL);
        newQ.add("parameters", new JsonArray());
        return newQ;
    }

    /**
     * Obtiene el array de queries desde el writer según el tipo.
     *
     * @param writer Objeto writer del JSON
     * @return JsonArray con las queries
     */
    @Override
    protected JsonArray getQueryArrayFromWriter(JsonObject writer) {
        return writer.has("sqlQueryList") ? writer.getAsJsonArray("sqlQueryList") : null;
    }

    /**
     * Agrega el array de queries al writer y limpia el tipo contrario.
     *
     * @param writer    Objeto writer del JSON
     * @param queryList Array de queries a agregar
     */
    @Override
    protected void addQueryArrayToWriter(JsonObject writer, JsonArray queryList) {
        writer.add("sqlQueryList", queryList);
        writer.remove("mongoQueryList");
    }

    /**
     * Carga los datos en el formulario de detalles.
     * Los datos vienen directamente en formato JSON del archivo.
     *
     * @param queryData Datos de la query a cargar (formato JSON)
     */
    @Override
    protected void loadDataToForm(JsonObject queryData) {
        detailsForm.setCurrentSelection(queryData);
        detailsForm.loadData(queryData);
    }

    /**
     * Guarda los datos del formulario en el objeto query.
     *
     * @param queryData Objeto query donde guardar los datos
     */
    @Override
    protected void saveDataFromForm(JsonObject queryData) {
        detailsForm.saveData(queryData);
    }

    /**
     * Configura los listeners del formulario.
     */
    @Override
    protected void setupFormListeners(ActionListener al, DocumentListener dl) {
        detailsForm.setParentActionListener(al);
        detailsForm.setListeners(al, dl);
    }

    /**
     * Verifica si el formulario de detalles está visible.
     * @return {@code true} si el formulario de detalles está visible, {@code false} en caso contrario
     */
    @Override
    protected boolean isFormVisible() {
        return detailsForm.isVisible();
    }

    /**
     * Elimina un parámetro del formulario.
     *
     * @param rowIndex Índice del parámetro a eliminar
     */
    @Override
    protected void deleteParam(int rowIndex) {
        detailsForm.deleteParam(rowIndex);
    }
}
