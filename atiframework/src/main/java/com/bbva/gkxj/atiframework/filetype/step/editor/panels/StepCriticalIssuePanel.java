package com.bbva.gkxj.atiframework.filetype.step.editor.panels;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

import com.intellij.util.ui.JBUI;
import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.ISSUE_LEVELS;
import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.applyBlueFocusBorder;


public class StepCriticalIssuePanel extends JPanel {
    /**
     * Proyecto de IntelliJ asociado al panel.
     */
    private final Project myProject;

    /**
     * Documento (texto) asociado al fichero que se edita.
     */
    private final Document myDocument;

    /**
     * Desplegable para seleccionar el nivel de issue (No level/Info/Warning/Critical).
     */
    private JComboBox<String> issueLevelCombo;

    /**
     * Campo de texto para el código de issue.
     */
    private JTextField issueCode;

    /**
     * Campo de texto para el código técnico.
     */
    private JTextField technicalCode;

    private boolean isUpdatingUI = false;

    /**
     * Crea un nuevo StepIssueTreatmentPanel asociado a un proyecto y un fichero.
     *
     * @param project proyecto de IntelliJ donde se está editando.
     * @param file    fichero virtual cuyo contenido JSON se edita en este panel.
     */
    public StepCriticalIssuePanel(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        createUIComponents();
    }
    /**
     * Inicializa y configura todos los componentes visuales del panel.
     */
    private void createUIComponents() {
        setLayout(new GridBagLayout());
        setBackground(SchedulerTheme.BG_CARD);

        issueLevelCombo = new JComboBox<>(ISSUE_LEVELS);
        issueLevelCombo.setBackground(SchedulerTheme.BG_CARD);
        issueLevelCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyBlueFocusBorder(issueLevelCombo);

        issueCode = new JTextField();
        applyBlueFocusBorder(issueCode);

        technicalCode = new JTextField();
        applyBlueFocusBorder(technicalCode);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = JBUI.insets(2, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;


        addStack(c, 0, 0, createLabel("Level"), issueLevelCombo);
        addStack(c, 1, 0, createLabel("Issue Code"), issueCode);
        addStack(c, 2, 0, createLabel("Technical Code"), technicalCode);

        addVerticalFiller(c, 3, 1);
    }


    private void addStack(GridBagConstraints c, int x, int yStart, JComponent topLabel, JComponent bottomComp) {
        c.gridx = x; c.gridy = yStart;
        add(topLabel, c);
        c.gridy = yStart + 1;
        add(bottomComp, c);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Lato", Font.BOLD, 14));
        l.setForeground(SchedulerTheme.TEXT_MAIN);
        return l;
    }

    private void addVerticalFiller(GridBagConstraints c, int row, int width) {
        c.gridy = row; c.weighty = 1.0; c.gridwidth = width;
        add(new JPanel() {{ setOpaque(false); }}, c);
    }

    public void updateDocument(JsonObject jsonObject){
        if (jsonObject == null || isUpdatingUI) return;

        JsonObject criticalIssue;
        if (jsonObject.has("criticalIssue")) {
            criticalIssue = jsonObject.getAsJsonObject("criticalIssue");
        } else {
            criticalIssue = new JsonObject();
        }

        String selectedLevel = (String) issueLevelCombo.getSelectedItem();
        if (selectedLevel != null && !selectedLevel.equals("No Level")) {
            criticalIssue.addProperty("level", selectedLevel);
        } else {
            if (criticalIssue.has("level")) {
                criticalIssue.remove("level");
            }
        }

        criticalIssue.addProperty("issueCode", issueCode.getText());
        criticalIssue.addProperty("technicalCode", technicalCode.getText());
        jsonObject.add("criticalIssue", criticalIssue);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(jsonObject));
        });
    }

    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        isUpdatingUI = true;
        try {
            if (jsonObject.has("criticalIssue") && !jsonObject.get("criticalIssue").isJsonNull()) {
                JsonObject ft = jsonObject.getAsJsonObject("criticalIssue");

                if (ft.has("level") && !ft.get("level").isJsonNull()) {
                    setComboSelection(issueLevelCombo, ft.get("level").getAsString());
                } else {
                    issueLevelCombo.setSelectedIndex(0);
                }

                if (ft.has("issueCode") && !ft.get("issueCode").isJsonNull()) {
                    issueCode.setText(ft.get("issueCode").getAsString());
                } else {
                    issueCode.setText("");
                }

                if (ft.has("technicalCode") && !ft.get("technicalCode").isJsonNull()) {
                    technicalCode.setText(ft.get("technicalCode").getAsString());
                } else {
                    technicalCode.setText("");
                }
            } else {
                issueLevelCombo.setSelectedIndex(0);
                issueCode.setText("");
                technicalCode.setText("");
            }
        } finally {
            isUpdatingUI = false;
        }

    }
    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     *
     * @param textListener   listener para cambios en campos de texto.
     * @param actionListener listener para acciones (combos, botones, etc.).
     * @param changeListener
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        issueCode.getDocument().addDocumentListener(textListener);
        technicalCode.getDocument().addDocumentListener(textListener);
        issueLevelCombo.addActionListener(actionListener);
    }

    /*
     * Selecciona un elemento en el JComboBox buscando una coincidencia de texto que ignore mayúsculas y minúsculas.
     * Recorre los elementos del combo y selecciona el primero que coincida con el valor proporcionado.
     *
     * @param combo El JComboBox donde se realizará la selección.
     * @param value El valor de texto a buscar y seleccionar.
     */
    private void setComboSelection(JComboBox<String> combo, String value) {
        if (value == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).equalsIgnoreCase(value)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
}
