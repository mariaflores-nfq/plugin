package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.reader;

import com.bbva.gkxj.atiframework.components.AtiJSpinner;
import com.bbva.gkxj.atiframework.components.AtiResizableTextArea;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.components.CustomToggleSwitch;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsGeneralPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsPanelMode;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.PanelType;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepType;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel de configuración para el paso Reader de tipo Fixed File.
 * Permite configurar los parámetros necesarios para leer archivos de longitud fija en un proceso ETL.
 * Incluye campos para hilos, commit, grid, cabeceras, encoding, validación de archivo vacío, áreas de texto para cabecera y posiciones, y panel de campos fijos.
 */
public class StepReaderFixedPanel extends JPanel implements StepReaderSubPanel {

    /**
     * Proyecto de IntelliJ asociado al panel.
     */
    private final Project myProject;

    /**
     * Archivo virtual asociado al panel.
     */
    private final VirtualFile myFile;

    /**
     * Campo para el número de hilos.
     */
    private JSpinner threadsField;

    /**
     * Campo para el intervalo de commit.
     */
    private JSpinner commitIntervalField;

    /**
     * Campo para el tamaño del grid.
     */
    private JSpinner gridSizeField;

    /**
     * Campo para el tamaño mínimo de archivo en el grid.
     */
    private JSpinner gridMinFileSizeField;

    /**
     * Campo para el número de cabeceras.
     */
    private JSpinner headersField;

    /**
     * Campo para el encoding del archivo.
     */
    private JTextField encodingField;

    /**
     * Interruptor para validar si el archivo está vacío.
     */
    private CustomToggleSwitch checkEmptyFileToggle;

    /**
     * Área de texto para la cabecera del archivo.
     */
    private JTextArea headerFileArea;

    /**
     * Área de texto para las posiciones de los campos.
     */
    private JTextArea positionFieldsArea;

    /**
     * Panel para la lista de campos fijos.
     */
    private FieldsGeneralPanel fieldsGeneralPanel;

    /**
     * Constructor del panel StepReaderFixedPanel.
     * Inicializa los paneles de campos y componentes de la interfaz.
     * @param myProject Proyecto de IntelliJ asociado.
     * @param myFile Archivo virtual asociado.
     */
    public StepReaderFixedPanel(Project myProject, VirtualFile myFile) {
        this.myProject = myProject;
        this.myFile = myFile;
        initUIComponents();
    }

    /**
     * Inicializa y organiza los componentes de la interfaz gráfica.
     */
    private void initUIComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Reader Configuration");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 18f));
        this.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(8);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        threadsField = new AtiJSpinner();
        commitIntervalField = new AtiJSpinner();
        gridSizeField = new AtiJSpinner();
        gridMinFileSizeField = new AtiJSpinner();
        headersField = new AtiJSpinner();
        encodingField = new AtiTextField();
        checkEmptyFileToggle = new CustomToggleSwitch();

        Color textAreaBg = new JBColor(new Color(0xF4F4F4), new Color(0x3C3F41));

        headerFileArea = new JTextArea(3, 20);
        headerFileArea.setBackground(textAreaBg);

        positionFieldsArea = new JTextArea(3, 20);
        positionFieldsArea.setBackground(textAreaBg);

        gbc.gridy = 0;
        gbc.gridx = 0;
        formPanel.add(createLabeledField("# of Threads", threadsField), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabeledField("Commit Interval", commitIntervalField), gbc);
        gbc.gridx = 2;
        formPanel.add(createLabeledField("Grid Size", gridSizeField), gbc);
        gbc.gridx = 3;
        formPanel.add(createLabeledField("Grid Min File Size", gridMinFileSizeField), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(createLabeledField("# of Headers", headersField), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabeledField("Encoding", encodingField), gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        switchPanel.setBorder(JBUI.Borders.emptyTop(25));
        switchPanel.add(checkEmptyFileToggle);

        JLabel switchLabel = new JLabel("Check empty file");
        switchLabel.setFont(switchLabel.getFont().deriveFont(Font.PLAIN, 13f));
        switchPanel.add(switchLabel);

        formPanel.add(switchPanel, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;
        formPanel.add(new AtiResizableTextArea("Header File", headerFileArea, true), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;
        formPanel.add(new AtiResizableTextArea("Position Fields", positionFieldsArea, true), gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.6;

        fieldsGeneralPanel = new FieldsGeneralPanel(StepType.READER, PanelType.FIXED, FieldsPanelMode.FIXED);
        formPanel.add(fieldsGeneralPanel, gbc);

        this.add(formPanel, BorderLayout.CENTER);
    }

    /**
     * Crea un panel con etiqueta y componente de entrada.
     * @param labelText Texto de la etiqueta.
     * @param inputComponent Componente de entrada asociado.
     * @return Panel con etiqueta y campo.
     */
    private JPanel createLabeledField(String labelText, JComponent inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, BorderLayout.NORTH);
        panel.add(inputComponent, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Actualiza los valores del formulario a partir de un objeto JSON.
     * @param jsonObject Objeto JSON con los datos a cargar.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (jsonObject.has("threads") && !jsonObject.get("threads").isJsonNull()) {
            threadsField.setValue(jsonObject.get("threads").getAsInt());
        }
        if (jsonObject.has("commitInterval") && !jsonObject.get("commitInterval").isJsonNull()) {
            commitIntervalField.setValue(jsonObject.get("commitInterval").getAsInt());
        }

        if (jsonObject.has("grid") && jsonObject.get("grid").isJsonObject()) {
            JsonObject grid = jsonObject.getAsJsonObject("grid");

            if (grid.has("size") && !grid.get("size").isJsonNull()) {
                gridSizeField.setValue(grid.get("size").getAsInt());
            }
            if (grid.has("minFileSize") && !grid.get("minFileSize").isJsonNull()) {
                gridMinFileSizeField.setValue(grid.get("minFileSize").getAsInt());
            }
        }

        if (jsonObject.has("reader") && jsonObject.get("reader").isJsonObject()) {
            JsonObject reader = jsonObject.getAsJsonObject("reader");

            if (reader.has("fixedFile") && reader.get("fixedFile").isJsonObject()) {
                JsonObject fixedFile = reader.getAsJsonObject("fixedFile");

                if (fixedFile.has("numHeader") && !fixedFile.get("numHeader").isJsonNull()) {
                    try {
                        headersField.setValue(Integer.parseInt(fixedFile.get("numHeader").getAsString()));
                    } catch (NumberFormatException e) {
                        headersField.setValue(0);
                    }
                }
                if (fixedFile.has("header") && !fixedFile.get("header").isJsonNull()) {
                    headerFileArea.setText(fixedFile.get("header").getAsString());
                }
                if (fixedFile.has("checkEmptyFile") && !fixedFile.get("checkEmptyFile").isJsonNull()) {
                    checkEmptyFileToggle.setSelected(fixedFile.get("checkEmptyFile").getAsBoolean());
                }
                if (fixedFile.has("encoding") && !fixedFile.get("encoding").isJsonNull()) {
                    encodingField.setText(fixedFile.get("encoding").getAsString());
                }
                if (fixedFile.has("positionFields") && !fixedFile.get("positionFields").isJsonNull()) {
                    positionFieldsArea.setText(fixedFile.get("positionFields").getAsString());
                }
            }
        }
        this.fieldsGeneralPanel.updateForm(jsonObject);
    }

    /**
     * Actualiza el objeto JSON con los valores actuales del formulario.
     * @param jsonObject Objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null) return;

        jsonObject.addProperty("threads", (Integer) threadsField.getValue());
        jsonObject.addProperty("commitInterval", (Integer) commitIntervalField.getValue());

        JsonObject grid = jsonObject.has("grid") ? jsonObject.getAsJsonObject("grid") : new JsonObject();
        grid.addProperty("size", (Integer) gridSizeField.getValue());
        grid.addProperty("minFileSize", (Integer) gridMinFileSizeField.getValue());
        jsonObject.add("grid", grid);

        JsonObject reader = jsonObject.has("reader") ? jsonObject.getAsJsonObject("reader") : new JsonObject();
        JsonObject fixedFile = reader.has("fixedFile") ? reader.getAsJsonObject("fixedFile") : new JsonObject();

        fixedFile.addProperty("numHeader", String.valueOf(headersField.getValue()));
        fixedFile.addProperty("header", headerFileArea.getText());

        fixedFile.addProperty("checkEmptyFile", checkEmptyFileToggle.isSelected());

        fixedFile.addProperty("encoding", encodingField.getText());
        fixedFile.addProperty("positionFields", positionFieldsArea.getText());

        reader.add("fixedFile", fixedFile);
        jsonObject.add("reader", reader);
        this.fieldsGeneralPanel.updateDocument(jsonObject);
    }

    /**
     * Añade listeners a los campos del formulario para detectar cambios.
     * @param textListener Listener para cambios en campos de texto.
     * @param actionListener Listener para acciones de botones y combos.
     * @param changeListener Listener para cambios en spinners y paneles.
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        threadsField.addChangeListener(changeListener);
        commitIntervalField.addChangeListener(changeListener);
        gridSizeField.addChangeListener(changeListener);
        gridMinFileSizeField.addChangeListener(changeListener);
        headersField.addChangeListener(changeListener);
        checkEmptyFileToggle.addChangeListener(changeListener);
        encodingField.getDocument().addDocumentListener(textListener);
        headerFileArea.getDocument().addDocumentListener(textListener);
        positionFieldsArea.getDocument().addDocumentListener(textListener);

        // Wire FixedFieldsPanel changes to the parent editor chain
        fieldsGeneralPanel.setChangeCallback(() -> {
            if (changeListener != null) {
                changeListener.stateChanged(new javax.swing.event.ChangeEvent(fieldsGeneralPanel));
            }
        });
    }

    /**
     * Devuelve el componente principal del panel.
     * @return Panel principal.
     */
    @Override
    public JPanel getComponent() {
        return this;
    }

}

