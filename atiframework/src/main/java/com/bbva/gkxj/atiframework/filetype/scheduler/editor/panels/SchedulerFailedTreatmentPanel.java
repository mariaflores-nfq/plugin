package com.bbva.gkxj.atiframework.filetype.scheduler.editor.panels;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.ISSUE_LEVELS;
import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.applyBlueFocusBorder;

/**
 * Panel de edición para la configuración de issues.
 *
 * Esta clase gestiona la interfaz que permite al usuario establecer parámetros para las issues en caso de fallo,
 * conectando bidireccionalmente los componentes de la UI con el archivo de configuración de extensión .sch
 * relativo a la planificación.
 */
public class SchedulerFailedTreatmentPanel extends JPanel {

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
     * Crea un nuevo SchedulerFailedTreatmentPanel asociado a un proyecto y un fichero.
     *
     * @param project proyecto de IntelliJ donde se está editando.
     * @param file    fichero virtual cuyo contenido JSON se edita en este panel.
     */
    public SchedulerFailedTreatmentPanel(@NotNull Project project, @NotNull VirtualFile file) {
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

        addStack(c, 0, 0, createLabel("Issue Level"), issueLevelCombo);
        addStack(c, 1, 0, createLabel("Issue Code"), issueCode);
        addStack(c, 2, 0, createLabel("Technical Code"), technicalCode);

        addVerticalFiller(c, 2, 3);
    }

    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     *
     * @param textListener   listener para cambios en campos de texto.
     * @param actionListener listener para acciones (combos, botones, etc.).
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener) {
        issueCode.getDocument().addDocumentListener(textListener);
        technicalCode.getDocument().addDocumentListener(textListener);
        issueLevelCombo.addActionListener(actionListener);
    }

    /**
     * Actualiza el JsonObject con los datos actuales del formulario.
     *
     * @param jsonObject objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null || isUpdatingUI) return;

        JsonObject failedTreatment;
        if (jsonObject.has("failedTreatment")) {
            failedTreatment = jsonObject.getAsJsonObject("failedTreatment");
        } else {
            failedTreatment = new JsonObject();
        }

        String selectedLevel = (String) issueLevelCombo.getSelectedItem();
        if (selectedLevel != null && !selectedLevel.equals("No Level")) {
            failedTreatment.addProperty("level", selectedLevel);
        } else {
            if (failedTreatment.has("level")) {
                failedTreatment.remove("level");
            }
        }

        failedTreatment.addProperty("issueCode", issueCode.getText());
        failedTreatment.addProperty("technicalCode", technicalCode.getText());
        jsonObject.add("failedTreatment", failedTreatment);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(jsonObject));
        });
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
            if (jsonObject.has("failedTreatment") && !jsonObject.get("failedTreatment").isJsonNull()) {
                JsonObject ft = jsonObject.getAsJsonObject("failedTreatment");

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

    /**
     * Añade un panel invisible en la parte inferior para mandar los componentes arriba
     *
     * @param c     El objeto GridBagConstraints actual (se modificará para este componente).
     * @param row   La fila (gridy) donde se debe colocar el relleno (debe ser la última).
     * @param width El ancho (gridwidth) que debe ocupar (número total de columnas del formulario).
     */
    private void addVerticalFiller(GridBagConstraints c, int row, int width) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0.0;
        c.weighty = 1.0;
        c.gridwidth = width;
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.NORTH;

        JPanel filler = new JPanel();
        filler.setOpaque(false);
        add(filler, c);
    }


    /**
     * Añadir un componente al panel en una posición específica de la rejilla.
     *
     * @param c    El objeto GridBagConstraints a modificar.
     * @param comp El componente visual a añadir.
     * @param x    La columna de la rejilla.
     * @param y    La fila de la rejilla.
     */
    private void addToGrid(GridBagConstraints c, JComponent comp, int x, int y) {
        c.gridx = x;
        c.gridy = y;
        add(comp, c);
    }

    /**
     * Apilar verticalmente una etiqueta y un componente en la misma columna. Utilizado para crear pares etiqueta/input
     * de forma vertical.
     *
     * @param c          El objeto GridBagConstraints a modificar.
     * @param x          La columna de la rejilla donde se colocarán ambos elementos.
     * @param yStart     La fila inicial para la etiqueta superior.
     * @param topLabel   La etiqueta que irá arriba.
     * @param bottomComp El componente principal que irá debajo.
     */
    private void addStack(GridBagConstraints c, int x, int yStart, JComponent topLabel, JComponent bottomComp) {
        addToGrid(c, topLabel, x, yStart);
        addToGrid(c, bottomComp, x, yStart + 1);
    }

    /**
     * Crea una etiqueta con el estilo estándar.
     *
     * @param text El texto de la etiqueta.
     * @return JLabel configurado.
     */
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Lato", Font.BOLD, 14));
        l.setForeground(SchedulerTheme.TEXT_MAIN);
        return l;
    }
}
