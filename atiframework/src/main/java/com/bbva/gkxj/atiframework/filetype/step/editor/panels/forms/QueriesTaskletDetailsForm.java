package com.bbva.gkxj.atiframework.filetype.step.editor.panels.forms;

import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.step.editor.panels.queriestasklet.QueriesTaskletMongoPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.panels.queriestasklet.QueriesTaskletSqlPanel;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

/**
 * Formulario orquestador para Database Query en Queries Task.
 * {@link QueriesTaskletSqlPanel} — vista SQL Query + Output Parameters
 * {@link QueriesTaskletMongoPanel} — vista Mongo con Collection, Operations y Output Parameters
 */
public class QueriesTaskletDetailsForm extends AbstractQueryDetailsForm {

    private static final String CARD_SQL = "SQL Query";
    private static final String CARD_MONGO = "Mongo Query";

    private AtiTextField queryCodeField;
    private AtiComboBox queryTypeCombo;
    private AtiComboBox dbSourceCombo;

    private JPanel typeCardsPanel;
    private CardLayout typeCardLayout;

    private QueriesTaskletSqlPanel sqlPanel;
    private QueriesTaskletMongoPanel mongoPanel;

    private String currentQueryType = CARD_SQL;

    public QueriesTaskletDetailsForm(Consumer<String> queryCodeUpdater) {
        super(queryCodeUpdater);
        initForm();
    }

    @Override
    protected void initForm() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // --- Fila 0: Labels ---
        c.gridy = 0;
        c.gridx = 0; c.weightx = 0.33; c.insets = new Insets(0, 0, 5, 10);
        add(createLabel("Query Code"), c);

        c.gridx = 1; c.insets = new Insets(0, 10, 5, 10);
        add(createLabel("Query Type"), c);

        c.gridx = 2; c.insets = new Insets(0, 10, 5, 0);
        add(createLabel("Db Source"), c);

        // --- Fila 1: Campos ---
        c.gridy = 1;
        c.gridx = 0; c.insets = new Insets(0, 0, 15, 10);
        queryCodeField = new AtiTextField();
        queryCodeField.setPreferredSize(new Dimension(0, 30));
        add(queryCodeField, c);

        c.gridx = 1; c.insets = new Insets(0, 10, 15, 10);
        queryTypeCombo = new AtiComboBox(new String[]{CARD_SQL, CARD_MONGO});
        queryTypeCombo.setPreferredSize(new Dimension(0, 30));
        queryTypeCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && !isInternal) {
                switchQueryType((String) e.getItem());
            }
        });
        add(queryTypeCombo, c);

        c.gridx = 2; c.insets = new Insets(0, 10, 15, 0);
        dbSourceCombo = new AtiComboBox(new String[]{"Config", "Oracle", "Mongo"});
        dbSourceCombo.setPreferredSize(new Dimension(0, 30));
        add(dbSourceCombo, c);

        // --- Fila 2: Card panel (SQL / Mongo) ---
        c.gridy = 2; c.gridx = 0; c.gridwidth = 3;
        c.weightx = 1.0; c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);

        typeCardLayout = new CardLayout();
        typeCardsPanel = new JPanel(typeCardLayout);
        typeCardsPanel.setBackground(BG_SIDEBAR);

        sqlPanel = new QueriesTaskletSqlPanel();
        mongoPanel = new QueriesTaskletMongoPanel();

        typeCardsPanel.add(sqlPanel, CARD_SQL);
        typeCardsPanel.add(mongoPanel, CARD_MONGO);

        typeCardLayout.show(typeCardsPanel, CARD_SQL);
        add(typeCardsPanel, c);
    }

    private void switchQueryType(String type) {
        currentQueryType = type;
        typeCardLayout.show(typeCardsPanel, type);

        if (currentSelection != null) {
            if (CARD_SQL.equals(type)) {
                sqlPanel.loadData(currentSelection);
            } else {
                mongoPanel.loadData(currentSelection);
            }
        }

        revalidate();
        repaint();

        if (parentActionListener != null && !isInternal) {
            parentActionListener.actionPerformed(
                    new ActionEvent(queryTypeCombo, ActionEvent.ACTION_PERFORMED, "QueryTypeChanged")
            );
        }
    }

    @Override
    public void deleteParam(int row) {
        if (CARD_MONGO.equals(currentQueryType)) {
            mongoPanel.deleteParam(row);
        } else {
            sqlPanel.deleteParam(row);
        }
    }

    @Override
    protected void saveSelectedParam() {
        if (CARD_MONGO.equals(currentQueryType)) {
            mongoPanel.saveSelectedParam();
        } else {
            sqlPanel.saveSelectedParam();
        }
    }

    @Override
    public void loadData(JsonObject q) {
        isInternal = true;
        try {
            queryCodeField.setText(getStringOrEmpty(q, "queryCode"));
            String queryType = q.has("collectionName") ? CARD_MONGO : CARD_SQL;
            currentQueryType = queryType;
            queryTypeCombo.setSelectedItem(queryType);
            typeCardLayout.show(typeCardsPanel, queryType);

            dbSourceCombo.setSelectedItem(getStringOrDefault(q, "dbSource", "Oracle"));
            sqlPanel.setCurrentSelection(q);
            mongoPanel.setCurrentSelection(q);

            if (CARD_SQL.equals(queryType)) {
                sqlPanel.loadData(q);
            } else {
                mongoPanel.loadData(q);
            }
        } finally {
            isInternal = false;
        }
    }

    @Override
    public void saveData(JsonObject q) {
        q.addProperty("queryCode", queryCodeField.getText());
        q.addProperty("dbSource", (String) dbSourceCombo.getSelectedItem());

        if (CARD_SQL.equals(currentQueryType)) {
            sqlPanel.saveData(q);
        } else {
            mongoPanel.saveData(q);
        }
    }

    @Override
    public void setListeners(ActionListener al, DocumentListener dl) {
        if (al == null || dl == null) return;
        parentActionListener = al;

        sqlPanel.setParentActionListener(al);
        mongoPanel.setParentActionListener(al);

        DocumentListener queryCodeSyncDl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); dl.insertUpdate(e); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); dl.removeUpdate(e); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); dl.changedUpdate(e); }

            private void sync() {
                if (!isInternal && currentSelection != null) {
                    String name = queryCodeField.getText();
                    currentSelection.addProperty("queryCode", name);
                    if (queryCodeUpdater != null) queryCodeUpdater.accept(name);
                }
            }
        };

        queryCodeField.getDocument().addDocumentListener(queryCodeSyncDl);
        queryTypeCombo.addActionListener(e -> {
            al.actionPerformed(e);
        });
        dbSourceCombo.addActionListener(e -> {
            if (!isInternal && currentSelection != null) {
                currentSelection.addProperty("dbSource", (String) dbSourceCombo.getSelectedItem());
            }
            al.actionPerformed(e);
        });
        sqlPanel.setupListeners(al, dl);
        mongoPanel.setupListeners(al, dl);
    }

    @Override
    public void setCurrentSelection(JsonObject currentSelection) {
        super.setCurrentSelection(currentSelection);
        sqlPanel.setCurrentSelection(currentSelection);
        mongoPanel.setCurrentSelection(currentSelection);
        if (currentSelection != null) {
            sqlPanel.loadData(currentSelection);
            mongoPanel.loadData(currentSelection);
        }
    }
}
