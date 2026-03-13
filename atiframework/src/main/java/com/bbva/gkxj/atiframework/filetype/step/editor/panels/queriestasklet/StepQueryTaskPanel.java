package com.bbva.gkxj.atiframework.filetype.step.editor.panels.queriestasklet;

import com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.writer.AbstractStepWriterQueryPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.panels.forms.QueriesTaskletDetailsForm;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel principal para configurar queries cuando el tipo de step es "Queries Task".
 */
public class StepQueryTaskPanel extends AbstractStepWriterQueryPanel {

    /** Nombre del array de queries directamente en el raíz */
    private static final String JSON_QUERY_LIST = "queryList";

    /** Formulario de detalles de la query seleccionada */
    private QueriesTaskletDetailsForm detailsForm;
 
    /**
     * Constructor del panel Query Task.
     *
     * @param project Proyecto de IntelliJ IDEA activo
     * @param file    Archivo virtual .step siendo editado
     */
    public StepQueryTaskPanel(Project project, VirtualFile file) {
        super(project, file);
    }

    /**
     * Esta vista NO tiene el header "Writer Configuration" en la parte superior.
     * Devolvemos un panel vacío para que el splitter ocupe todo.
     */
    @Override
    protected JPanel createGlobalConfigPanel() {
        JPanel emptyHeader = new JPanel();
        emptyHeader.setPreferredSize(new Dimension(0, 0));
        return emptyHeader;
    }

    /**
     * Instancia el formulario visual de detalles de query.
     */
    @Override
    protected JPanel createDetailsForm() {
        detailsForm = new QueriesTaskletDetailsForm(this::updateQueryCodeInTable);
        return detailsForm;
    }

    /**
     * Actualiza el queryCode en el sidebar cuando se modifica en el formulario.
     *
     * @param queryCode Nuevo código de query
     */
    private void updateQueryCodeInTable(String queryCode) {
        int row = queriesTable.getSelectedRow();
        if (row != -1) {
            tableModel.setValueAt(queryCode, row, 1);
        }
    }

    /**
     * Crea una nueva query vacía con valores por defecto.
     *
     * @return JsonObject con la estructura de una nueva query
     */
    @Override
    protected JsonObject createNewQueryObject() {
        JsonObject newQ = new JsonObject();
        newQ.addProperty("queryCode", "New query");
        newQ.addProperty("dbSource", "Oracle");
        newQ.addProperty("sqlQuery", "");
        newQ.add("enricherOutputFields", new JsonArray());
        return newQ;
    }

    /**
     * Obtiene la lista de queries desde el JSON principal.
     *
     * @param jsonObject Objeto JSON principal
     * @return JsonArray con las queries, o null si no existe
     */
    @Override
    protected JsonArray getQueryArrayFromWriter(JsonObject jsonObject) {
        return jsonObject.has(JSON_QUERY_LIST) ? jsonObject.getAsJsonArray(JSON_QUERY_LIST) : null;
    }

    /**
     * Guarda la lista de queries directamente en el JSON principal.
     *
     * @param jsonObject Objeto JSON principal
     * @param queryList Array de queries a guardar
     */
    @Override
    protected void addQueryArrayToWriter(JsonObject jsonObject, JsonArray queryList) {
        jsonObject.add(JSON_QUERY_LIST, queryList);
    }

    /**
     * Actualiza el formulario con los datos del JSON.
     *
     * @param jsonObject JSON completo del archivo .step
     */
    @Override
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;
        if (currentSelection != null && isFormVisible() && !isUpdatingUI) {
            saveDataFromForm(currentSelection);
        }
        isUpdatingUI = true;
        try {
            int previousSelectedRow = queriesTable.getSelectedRow();
            String previousQueryCode = null;
            if (previousSelectedRow >= 0 && previousSelectedRow < queriesData.size()) {
                JsonObject prevQuery = queriesData.get(previousSelectedRow);
                if (prevQuery.has("queryCode")) {
                    previousQueryCode = prevQuery.get("queryCode").getAsString();
                }
            }

            queriesData.clear();
            tableModel.setRowCount(0);

            JsonArray queryArray = getQueryArrayFromWriter(jsonObject);
            if (queryArray != null) {
                for (int i = 0; i < queryArray.size(); i++) {
                    JsonObject q = queryArray.get(i).getAsJsonObject();
                    queriesData.add(q);
                    String code = q.has("queryCode") ? q.get("queryCode").getAsString() : "";
                    tableModel.addRow(new Object[]{String.format("%02d", i + 1), code, ""});
                }
            }
            queriesCounterLabel.setText("Queries (" + queriesData.size() + ")");

            if (!queriesData.isEmpty()) {
                centerCardLayout.show(centerContainer, CARD_SPLITTER);
                int rowToSelect = 0;
                if (previousQueryCode != null) {
                    for (int i = 0; i < queriesData.size(); i++) {
                        JsonObject q = queriesData.get(i);
                        if (q.has("queryCode") && previousQueryCode.equals(q.get("queryCode").getAsString())) {
                            rowToSelect = i;
                            break;
                        }
                    }
                } else if (previousSelectedRow >= 0 && previousSelectedRow < queriesData.size()) {
                    rowToSelect = previousSelectedRow;
                }

                queriesTable.setRowSelectionInterval(rowToSelect, rowToSelect);
            } else {
                currentSelection = null;
                showFormPanel(false);
                centerCardLayout.show(centerContainer, CARD_EMPTY);
            }
        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Actualiza el documento JSON con los datos del formulario.
     *
     * @param jsonObject JSON completo del archivo .step
     */
    @Override
    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;

        if (currentSelection != null && isFormVisible()) {
            saveDataFromForm(currentSelection);
        }
        for (int i = 0; i < queriesData.size(); i++) {
            JsonObject q = queriesData.get(i);
            String code = q.has("queryCode") ? q.get("queryCode").getAsString() : "";
            if (i < tableModel.getRowCount()) {
                tableModel.setValueAt(code, i, 1);
            }
        }
        JsonArray queryList = new JsonArray();
        for (JsonObject q : queriesData) {
            queryList.add(q);
        }
        addQueryArrayToWriter(jsonObject, queryList);

        Document myDocument = FileDocumentManager.getInstance().getDocument(file);
        if (myDocument != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                myDocument.setText(gson.toJson(jsonObject));
            });
        }
    }

    /**
     * Carga los datos de una query en el formulario de detalles.
     *
     * @param queryData Datos de la query a cargar
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
     * Configura los listeners del formulario para detectar cambios.
     *
     * @param al Listener de acciones (para combos, botones)
     * @param dl Listener de documentos (para campos de texto)
     */
    @Override
    protected void setupFormListeners(ActionListener al, DocumentListener dl) {
        detailsForm.setParentActionListener(al);
        detailsForm.setListeners(al, dl);
    }

    /**
     * Verifica si el formulario de detalles está visible.
     *
     * @return true si el formulario está visible
     */
    @Override
    protected boolean isFormVisible() {
        return detailsForm.isVisible();
    }

    /**
     * Elimina un parámetro de output de la query seleccionada.
     *
     * @param rowIndex Índice del parámetro a eliminar
     */
    @Override
    protected void deleteParam(int rowIndex) {
        detailsForm.deleteParam(rowIndex);
    }
}

