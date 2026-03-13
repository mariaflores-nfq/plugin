package com.bbva.gkxj.atiframework.filetype.step.editor.panels.jasperreport;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.JasperReportData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import com.intellij.openapi.editor.Document;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class StepJasperReportPanel extends JPanel {

    /**
     * Proyecto de IntelliJ asociado al panel.
     */
    private final Project myProject;

    /**
     * Referencia al archivo virtual que se está editando.
     */
    private final VirtualFile myFile;

    private final Document myDocument;


    private boolean isUpdatingUI = false;

    private JasperReportData currentSelection = null;

    private AtiResizableTextArea reportNameTextArea;
    private AtiTextField sheetNameTextField;
    private AtiResizableTextArea templateTextArea;
    private AtiScriptPanel sqlQueryPanel;
    private AtiTableSplitterPanel<JasperReportData> tableSplitter;
    private int lblCount = 0;


    public StepJasperReportPanel(Project myProject, VirtualFile myFile) {
        this.myProject = myProject;
        this.myFile = myFile;
        this.myDocument = FileDocumentManager.getInstance().getDocument(myFile);
        initUIComponents();
    }

    private void initUIComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setOpaque(false);

        reportNameTextArea = new AtiResizableTextArea("Path", new JTextArea(2, 80), true);

        add(reportNameTextArea, BorderLayout.NORTH);

        JPanel formPanel = buildFormPanel();

        tableSplitter = new AtiTableSplitterPanel<>(
                "Sheets",
                "Jasper Sheet",
                () -> {
                    lblCount++;
                    return new JasperReportData(String.format("%02d", lblCount));
                },
                JasperReportData::getId,
                JasperReportData::getSheetName,
                formPanel
        );
        tableSplitter.setSelectionListener(item -> {
            if (currentSelection != null) {
                syncCurrentSelectionFromUI();
            }
            currentSelection = item;
            loadSelectionIntoUI(item);
        });

        tableSplitter.setDeselectionListener(() -> currentSelection = null);

        add(tableSplitter, BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        sheetNameTextField = new AtiTextField();
        templateTextArea = new AtiResizableTextArea("Template", new JTextArea(3, 80), true);
        sqlQueryPanel = new AtiScriptPanel();

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(6, 0,6,10);

        form.add(new AtiLabeledComponent("Sheet Name", sheetNameTextField), gbc);

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.25;
        form.add(templateTextArea, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.75;

        JPanel sqlWrapper = new JPanel(new BorderLayout(0, 5));
        sqlWrapper.setOpaque(false);
        JLabel sqlLabel = new AtiJLabel("SQL Query");
        sqlWrapper.add(sqlLabel, BorderLayout.NORTH);
        sqlWrapper.add(sqlQueryPanel, BorderLayout.CENTER);

        form.add(sqlWrapper, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        form.add(Box.createVerticalGlue(), gbc);

        return form;
    }


    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado.
     *
     * @param jsonObject objeto JSON con los datos a cargar en el formulario.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        isUpdatingUI = true;
        try {
            String reportName = "";
            List<JasperReportData> items = new ArrayList<>();

            if (jsonObject.has("jasperReport") && jsonObject.get("jasperReport").isJsonObject()) {
                JsonObject root = jsonObject.getAsJsonObject("jasperReport");

                reportName = getSafeString(root, "reportName");

                if (root.has("jasperSheetList") && root.get("jasperSheetList").isJsonArray()) {
                    JsonArray arr = root.getAsJsonArray("jasperSheetList");
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject o = arr.get(i).getAsJsonObject();
                        JasperReportData d = new JasperReportData(String.format("%02d", i + 1));
                        d.setSheetName(getSafeString(o, "sheetName"));
                        d.setTemplate(getSafeString(o, "template"));
                        d.setSqlQuery(getSafeString(o, "sqlQuery"));
                        items.add(d);
                    }
                }
            }

            reportNameTextArea.setText(reportName);

            lblCount = items.size();
            tableSplitter.reloadData(items);
            currentSelection = tableSplitter.getCurrentSelection();
            loadSelectionIntoUI(currentSelection);

        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Lee del JSON el valor de una clave de forma segura (sin NullPointerException).
     * Si no existe o es nulo, devuelve cadena vacía "".
     */
    private String getSafeString(JsonObject obj, String key) {
        if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "";
    }


    /**
     * Actualiza el JsonObject con los datos actuales del formulario.
     *
     * @param jsonObject objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;

        syncCurrentSelectionFromUI();

        if (!jsonObject.has("jasperReport") || !jsonObject.get("jasperReport").isJsonObject()) {
            jsonObject.add("jasperReport", new JsonObject());
        }
        JsonObject root = jsonObject.getAsJsonObject("jasperReport");

        root.addProperty("reportName", reportNameTextArea.getText());



        List<JasperReportData> items = tableSplitter.getDataList();
        JsonArray arr = new JsonArray();

        for (JasperReportData d : items) {
            JsonObject o = new JsonObject();
            o.addProperty("sheetName", d.getSheetName());
            o.addProperty("template", d.getTemplate());
            o.addProperty("sqlQuery", d.getSqlQuery());
            arr.add(o);
        }

        root.add("jasperSheetList", arr);

        jsonObject.add("jasperReport",root);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(jsonObject));
        });
    }

    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     *
     * @param textListener   listener para cambios en campos de texto.
     * @param actionListener listener para acciones (combos, botones, etc.).
     * @param dateChangeListener listener para cambios en componentes.
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener dateChangeListener) {


        reportNameTextArea.getDocument().addDocumentListener(textListener);
        sheetNameTextField.getDocument().addDocumentListener(textListener);
        templateTextArea.getDocument().addDocumentListener(textListener);
        sqlQueryPanel.getTextArea().getDocument().addDocumentListener(textListener);

        tableSplitter.setChangeCallback(() -> {
            if (dateChangeListener != null) {
                dateChangeListener.stateChanged(new javax.swing.event.ChangeEvent(this));
            }
        });
    }

    private void loadSelectionIntoUI(JasperReportData data) {
        if (data == null) return;
        isUpdatingUI = true;
        try {
            sheetNameTextField.setText(data.getSheetName());
            templateTextArea.setText(data.getTemplate());
            sqlQueryPanel.getTextArea().setText(data.getSqlQuery());
        } finally {
            isUpdatingUI = false;
        }
    }

    private void syncCurrentSelectionFromUI() {
        if (currentSelection == null) return;
        currentSelection.setSheetName(sheetNameTextField.getText());
        currentSelection.setTemplate(templateTextArea.getText());
        currentSelection.setSqlQuery(sqlQueryPanel.getTextArea().getText());
    }




}
