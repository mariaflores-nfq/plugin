package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.reader;

import com.bbva.gkxj.atiframework.components.AtiJSpinner;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.components.AtiUploadCsvButton;
import com.bbva.gkxj.atiframework.components.CustomToggleSwitch;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsGeneralPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsPanelMode;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.PanelType;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepType;
import com.google.gson.JsonObject;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel de configuración para el paso Reader de tipo CSV File.
 * Permite configurar los parámetros necesarios para leer archivos CSV en un proceso ETL.
 */
public class StepReaderCsvPanel extends JPanel implements StepReaderSubPanel {

    /**
     * Campos de configuración para el Reader CSV, incluyendo número de hilos, commit interval, configuración de grid,
     */
    private AtiJSpinner threadsSpinner;

    /**
     * Campo para el intervalo de commit, que define cada cuántos registros se realiza un commit durante la lectura del CSV.
     */
    private AtiJSpinner commitIntervalSpinner;

    /**
     * Campo para el tamaño del grid, que determina cuántos registros se procesan en cada bloque durante la lectura del CSV.
     */
    private AtiJSpinner gridSizeSpinner;

    /**
     * Campo para el tamaño mínimo de archivo en el grid, que determina el tamaño mínimo de los archivos CSV.
     */
    private AtiJSpinner gridMinFileSizeSpinner;

    /**
     * Campo para el número de encabezados en el archivo CSV.
     */
    private AtiJSpinner numHeadersSpinner;

    /**
     * Campos para la configuración específica del CSV, como el separador de campos.
     */
    private AtiTextField separatorTextField;

    /**
     * Campo para la codificación del archivo CSV, que permite especificar la codificación de caracteres utilizada en el archivo.
     */
    private AtiTextField encodingTextField;

    /**
     * Campo para activar o desactivar la opción de verificar si el archivo CSV está vacío antes de procesarlo.
     */
    private CustomToggleSwitch checkEmptyFileSwitch;

    /**
     * Campo para especificar un archivo de encabezado (header) separado.
     */
    private AtiTextField headerFileTextField;

    /**
     * Botón para cargar un archivo CSV de ejemplo, que permite al usuario seleccionar un archivo CSV.
     */
    private final AtiUploadCsvButton uploadCSVFileBtn = new AtiUploadCsvButton();

    /**
     * Panel para la configuración de campos específicos del CSV, que permite al usuario definir los campos a leer del
     * archivo CSV y sus propiedades.
     */
    private FieldsGeneralPanel fieldsGeneralPanel;

    /**
     * Crea un nuevo StepReaderCsvPanel.
     */
    public StepReaderCsvPanel() {
        initUIComponents();
    }

    /**
     * Inicializa y configura todos los componentes visuales del panel.
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

        threadsSpinner = new AtiJSpinner();
        commitIntervalSpinner = new AtiJSpinner();
        gridSizeSpinner = new AtiJSpinner();
        gridMinFileSizeSpinner = new AtiJSpinner();
        numHeadersSpinner = new AtiJSpinner();
        separatorTextField = new AtiTextField();
        encodingTextField = new AtiTextField();
        checkEmptyFileSwitch = new CustomToggleSwitch();
        headerFileTextField = new AtiTextField();

        gbc.gridy = 0;
        gbc.gridx = 0;
        formPanel.add(createLabeledField("# of Threads", threadsSpinner), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabeledField("Commit Interval", commitIntervalSpinner), gbc);
        gbc.gridx = 2;
        formPanel.add(createLabeledField("Grid Size", gridSizeSpinner), gbc);
        gbc.gridx = 3;
        formPanel.add(createLabeledField("Grid Min File Size", gridMinFileSizeSpinner), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(createLabeledField("# of Headers", numHeadersSpinner), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabeledField("Separator", separatorTextField), gbc);
        gbc.gridx = 2;
        formPanel.add(createLabeledField("Encoding", encodingTextField), gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        switchPanel.setBorder(JBUI.Borders.emptyTop(25));
        switchPanel.add(checkEmptyFileSwitch);
        JLabel switchLabel = new JLabel("Check empty file");
        switchLabel.setFont(switchLabel.getFont().deriveFont(Font.PLAIN, 13f));
        switchPanel.add(switchLabel);
        formPanel.add(switchPanel, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        JPanel uploadPanel = new JPanel(new BorderLayout());
        uploadPanel.add(uploadCSVFileBtn, BorderLayout.WEST);
        formPanel.add(uploadPanel, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        formPanel.add(createLabeledField("Header File", headerFileTextField), gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        fieldsGeneralPanel = new FieldsGeneralPanel(StepType.READER, PanelType.CSV, FieldsPanelMode.CSV);
        formPanel.add(fieldsGeneralPanel, gbc);

        this.add(formPanel, BorderLayout.CENTER);
    }

    /**
     * Crea un panel con etiqueta y componente de entrada.
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
     * Actualiza los valores del formulario a partir de un objeto JSON completo.
     *
     * @param jsonObject objeto JSON completo con los datos a cargar en el formulario.
     */
    @Override
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (jsonObject.has("threads") && !jsonObject.get("threads").isJsonNull()) {
            threadsSpinner.setValue(jsonObject.get("threads").getAsInt());
        }
        if (jsonObject.has("commitInterval") && !jsonObject.get("commitInterval").isJsonNull()) {
            commitIntervalSpinner.setValue(jsonObject.get("commitInterval").getAsInt());
        }
        if (jsonObject.has("grid") && jsonObject.get("grid").isJsonObject()) {
            JsonObject grid = jsonObject.getAsJsonObject("grid");
            if (grid.has("size") && !grid.get("size").isJsonNull()) {
                gridSizeSpinner.setValue(grid.get("size").getAsInt());
            }
            if (grid.has("minFileSize") && !grid.get("minFileSize").isJsonNull()) {
                gridMinFileSizeSpinner.setValue(grid.get("minFileSize").getAsInt());
            }
        }

        if (jsonObject.has("reader") && jsonObject.get("reader").isJsonObject()) {
            JsonObject reader = jsonObject.getAsJsonObject("reader");
            if (reader.has("csvFile") && reader.get("csvFile").isJsonObject()) {
                JsonObject csvFile = reader.getAsJsonObject("csvFile");

                if (csvFile.has("numHeader") && !csvFile.get("numHeader").isJsonNull()) {
                    try {
                        numHeadersSpinner.setValue(Integer.parseInt(csvFile.get("numHeader").getAsString()));
                    } catch (NumberFormatException e) {
                        numHeadersSpinner.setValue(0);
                    }
                }
                if (csvFile.has("header") && !csvFile.get("header").isJsonNull()) {
                    headerFileTextField.setText(csvFile.get("header").getAsString());
                }
                if (csvFile.has("separator") && !csvFile.get("separator").isJsonNull()) {
                    separatorTextField.setText(csvFile.get("separator").getAsString());
                }
                if (csvFile.has("checkEmptyFile") && !csvFile.get("checkEmptyFile").isJsonNull()) {
                    checkEmptyFileSwitch.setSelected(csvFile.get("checkEmptyFile").getAsBoolean());
                }
                if (csvFile.has("encoding") && !csvFile.get("encoding").isJsonNull()) {
                    encodingTextField.setText(csvFile.get("encoding").getAsString());
                }
            }
        }

        fieldsGeneralPanel.updateForm(jsonObject);
    }

    /**
     * Actualiza el objeto JSON con los valores actuales del formulario.
     *
     * @param jsonObject objeto JSON a actualizar.
     */
    @Override
    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null) return;

        jsonObject.addProperty("threads", (Integer) threadsSpinner.getValue());
        jsonObject.addProperty("commitInterval", (Integer) commitIntervalSpinner.getValue());

        JsonObject grid = jsonObject.has("grid") ? jsonObject.getAsJsonObject("grid") : new JsonObject();
        grid.addProperty("size", (Integer) gridSizeSpinner.getValue());
        grid.addProperty("minFileSize", (Integer) gridMinFileSizeSpinner.getValue());
        jsonObject.add("grid", grid);

        JsonObject reader = jsonObject.has("reader") ? jsonObject.getAsJsonObject("reader") : new JsonObject();
        JsonObject csvFile = reader.has("csvFile") ? reader.getAsJsonObject("csvFile") : new JsonObject();

        csvFile.addProperty("numHeader", String.valueOf(numHeadersSpinner.getValue()));
        csvFile.addProperty("header", headerFileTextField.getText());
        csvFile.addProperty("separator", separatorTextField.getText());
        csvFile.addProperty("checkEmptyFile", checkEmptyFileSwitch.isSelected());
        csvFile.addProperty("encoding", encodingTextField.getText());

        reader.add("csvFile", csvFile);
        jsonObject.add("reader", reader);

        fieldsGeneralPanel.updateDocument(jsonObject);
    }

    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     *
     * @param textListener   listener para cambios en campos de texto.
     * @param actionListener listener para acciones (combos, botones, etc.).
     * @param changeListener listener para cambios en spinners, toggles, etc.
     */
    @Override
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        threadsSpinner.addChangeListener(changeListener);
        commitIntervalSpinner.addChangeListener(changeListener);
        gridSizeSpinner.addChangeListener(changeListener);
        gridMinFileSizeSpinner.addChangeListener(changeListener);
        numHeadersSpinner.addChangeListener(changeListener);
        checkEmptyFileSwitch.addChangeListener(changeListener);
        separatorTextField.getDocument().addDocumentListener(textListener);
        encodingTextField.getDocument().addDocumentListener(textListener);
        headerFileTextField.getDocument().addDocumentListener(textListener);

        fieldsGeneralPanel.setChangeCallback(() -> {
            if (changeListener != null) {
                changeListener.stateChanged(new javax.swing.event.ChangeEvent(fieldsGeneralPanel));
            }
        });
    }

    /**
     * Devuelve el componente principal del panel.
     *
     * @return Panel principal.
     */
    @Override
    public JPanel getComponent() {
        return this;
    }

}