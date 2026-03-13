package com.bbva.gkxj.atiframework.filetype.step.editor.panels.queriestasklet;

import com.bbva.gkxj.atiframework.components.AtiScriptPanel;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel de contenido para queries de tipo SQL en el Queries Task.
 * Hereda de {@link AbstractQueriesTaskletPanel} la gestión completa de Output Parameters
 * y solo implementa la parte específica del SQL Query area.
 */
public class QueriesTaskletSqlPanel extends AbstractQueriesTaskletPanel {

    private AtiScriptPanel sqlQueryPanel;

    /**
     * Constructor del panel de configuración para SQL Query en Queries Task.
     * Inicializa los componentes y el layout del panel.
     */
    public QueriesTaskletSqlPanel() {
        super();
        initComponents();
    }

    /**
     * Inicializa los componentes UI del panel: área de SQL Query y sección de Output Parameters.
     */
    private void initComponents() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.weightx = 1.0;

        // SQL Query area
        c.gridy = 0;
        c.weighty = 0.4;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 15, 0);

        JPanel sqlWrapper = new JPanel(new BorderLayout(0, 5));
        sqlWrapper.setOpaque(false);
        JLabel sqlLabel = new JLabel("SQL Query");
        sqlLabel.setFont(sqlLabel.getFont().deriveFont(Font.BOLD));
        sqlWrapper.add(sqlLabel, BorderLayout.NORTH);

        sqlQueryPanel = new AtiScriptPanel();
        sqlWrapper.add(sqlQueryPanel, BorderLayout.CENTER);
        add(sqlWrapper, c);

        // Output Parameters section (delegada a la clase abstracta)
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.6;
        c.insets = new Insets(0, 0, 0, 0);
        add(createOutputParametersSection(), c);
    }

    /**
     * Carga los datos SQL de la query en los campos del panel.
     * @param q El objeto JsonObject con los datos a cargar.
     */
    @Override
    public void loadData(JsonObject q) {
        isInternal = true;
        try {
            if (sqlQueryPanel != null && sqlQueryPanel.getTextArea() != null) {
                sqlQueryPanel.getTextArea().setText(getStr(q, "sqlQuery"));
            }
            loadOutputParameters(q);
        } finally {
            isInternal = false;
        }
    }

    /**
     * Guarda los datos SQL del panel en el objeto query.
     * @param q El objeto JsonObject donde guardar los datos.
     */
    @Override
    public void saveData(JsonObject q) {
        if (sqlQueryPanel != null && sqlQueryPanel.getTextArea() != null) {
            q.addProperty("sqlQuery", sqlQueryPanel.getTextArea().getText());
        }
        // Limpiar campos Mongo
        q.remove("collectionName");
        q.remove("options");
        q.remove("operation");
        q.remove("filter");
        q.remove("update");
        q.remove("insert");
        q.remove("delete");

        saveSelectedParam();
    }

    /**
     * Registra listeners para sincronización automática con el JSON.
     * @param al ActionListener para componentes de tipo combo/botón.
     * @param dl DocumentListener para campos de texto.
     */
    @Override
    public void setupListeners(ActionListener al, DocumentListener dl) {
        // SQL Query sync
        DocumentListener sqlSyncDl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { syncSqlQuery(); dl.insertUpdate(e); }
            @Override
            public void removeUpdate(DocumentEvent e) { syncSqlQuery(); dl.removeUpdate(e); }
            @Override
            public void changedUpdate(DocumentEvent e) { syncSqlQuery(); dl.changedUpdate(e); }
        };

        if (sqlQueryPanel != null && sqlQueryPanel.getTextArea() != null) {
            sqlQueryPanel.getTextArea().getDocument().addDocumentListener(sqlSyncDl);
        }

        // Param sync (delegado a la clase abstracta)
        setupParamListeners(al, dl);
    }

    /**
     * Sincroniza el contenido del área de SQL Query con el JSON actual.
     */
    private void syncSqlQuery() {
        if (!isInternal && currentSelection != null && sqlQueryPanel != null && sqlQueryPanel.getTextArea() != null) {
            currentSelection.addProperty("sqlQuery", sqlQueryPanel.getTextArea().getText());
        }
    }
}
