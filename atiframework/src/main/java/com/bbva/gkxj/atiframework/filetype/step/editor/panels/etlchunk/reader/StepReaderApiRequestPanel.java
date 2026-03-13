package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.reader;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsGeneralPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsPanelMode;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.PanelType;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepType;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * Panel de configuración para el paso Reader de tipo Api Request.
 * Permite configurar los parámetros necesarios para realizar peticiones API como Reader en un proceso ETL.
 * Incluye campos para hilos, commit, URL, método, nombre de servicio, versión, timeout, tamaño de página, root element, rutas de guardado, script de procesado y listas de campos JSON y Header.
 */
public class StepReaderApiRequestPanel extends JPanel implements StepReaderSubPanel {

    /**
     * Métodos HTTP que permiten cuerpo en la petición.
     */
    private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");
    /**
     * Proyecto de IntelliJ asociado al panel.
     */
    private final Project myProject;
    /**
     * Archivo virtual asociado al panel.
     */
    private final VirtualFile myFile;
    /**
     * Panel para la lista de campos JSON.
     */
    private final FieldsGeneralPanel jsonFieldsPanel;
    /**
     * Panel para la lista de campos Header.
     */
    private final FieldsGeneralPanel headerFieldsPanel;
    /**
     * Campo para el número de hilos.
     */
    private AtiJSpinner threadsField;
    /**
     * Campo para el intervalo de commit.
     */
    private AtiJSpinner commitIntervalField;
    /**
     * Área de texto para la plantilla de URL.
     */
    private AtiResizableTextArea urlTemplate;
    /**
     * ComboBox para seleccionar el método HTTP.
     */
    private AtiComboBox method;
    /**
     * Campo de texto para el nombre del servicio.
     */
    private AtiTextField serviceName;
    /**
     * Campo de texto para la versión de la API.
     */
    private AtiTextField apiVersion;
    /**
     * Spinner para el timeout de la petición.
     */
    private AtiJSpinner timeout;
    /**
     * Spinner para el tamaño de página.
     */
    private AtiJSpinner pageSizeField;
    /**
     * Campo de texto para el elemento raíz.
     */
    private AtiTextField rootElement;
    /**
     * Área de texto para la ruta de guardado del archivo.
     */
    private AtiResizableTextArea saveFilePathArea;
    /**
     * RadioButton para seleccionar nombre de archivo fijo.
     */
    private JRadioButton fileNameRadio;
    /**
     * RadioButton para seleccionar nombre de archivo por consulta.
     */
    private JRadioButton fileNameQueryRadio;
    /**
     * Área de texto para la consulta del nombre de archivo.
     */
    private AtiResizableTextArea saveFileNameQueryArea;
    /**
     * Panel para el script de procesado.
     */
    private AtiScriptPanel script;
    /**
     * Área de texto para la plantilla del cuerpo de la petición.
     */
    private AtiResizableTextArea bodyTemplate;
    /**
     * Panel contenedor para el cuerpo de la petición.
     */
    private JPanel bodyWrapper;

    /**
     * Constructor del panel StepReaderApiRequestPanel.
     * Inicializa los paneles de campos y componentes de la interfaz.
     *
     * @param myProject Proyecto de IntelliJ asociado.
     * @param myFile    Archivo virtual asociado.
     */
    public StepReaderApiRequestPanel(Project myProject, VirtualFile myFile) {
        this.myProject = myProject;
        this.myFile = myFile;
        this.jsonFieldsPanel = new FieldsGeneralPanel(StepType.READER, PanelType.API_REQUEST, FieldsPanelMode.JSON);
        this.headerFieldsPanel = new FieldsGeneralPanel(StepType.READER, PanelType.API_REQUEST, FieldsPanelMode.HEADER);
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

        urlTemplate = new AtiResizableTextArea();
        method = new AtiComboBox(new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"});
        method.addActionListener(e -> {
            updateBodyTemplateVisibility();
            formPanel.revalidate();
            formPanel.repaint();
        });

        serviceName = new AtiTextField();
        apiVersion = new AtiTextField();
        timeout = new AtiJSpinner();
        pageSizeField = new AtiJSpinner();

        rootElement = new AtiTextField();

        saveFilePathArea = new AtiResizableTextArea("Save File Path", new JTextArea(3, 20), true);

        fileNameRadio = new JRadioButton("File Name");
        fileNameQueryRadio = new JRadioButton("File Name Query");
        fileNameRadio.setSelected(true);
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(fileNameRadio);
        radioGroup.add(fileNameQueryRadio);

        saveFileNameQueryArea = new AtiResizableTextArea("Save File Name", new JTextArea(3, 20), true);

        script = new AtiScriptPanel();

        bodyTemplate = new AtiResizableTextArea("Body Template", new JTextArea(3, 80), true);

        updateBodyTemplateVisibility();

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        formPanel.add(createLabeledField("# of Threads", threadsField), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabeledField("Commit Interval", commitIntervalField), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 3.0;
        formPanel.add(createLabeledField("URL", urlTemplate), gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        formPanel.add(createLabeledField("Method", method), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        formPanel.add(createLabeledField("Service Name", serviceName), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabeledField("Api Version", apiVersion), gbc);
        gbc.gridx = 2;
        formPanel.add(createLabeledField("Timeout", timeout), gbc);
        gbc.gridx = 3;
        formPanel.add(createLabeledField("Page Size", pageSizeField), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        formPanel.add(createLabeledField("Root Element", rootElement), gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        formPanel.add(saveFilePathArea, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.setOpaque(false);
        radioPanel.add(fileNameRadio);
        radioPanel.add(fileNameQueryRadio);
        formPanel.add(radioPanel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        formPanel.add(saveFileNameQueryArea, gbc);

        ActionListener radioListener = e -> {
            String title = fileNameQueryRadio.isSelected() ? "Save File Name Query" : "Save File Name";
            saveFileNameQueryArea.setTitle(title);
        };
        fileNameRadio.addActionListener(radioListener);
        fileNameQueryRadio.addActionListener(radioListener);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.4;

        JPanel scriptWrapper = new JPanel(new BorderLayout(0, 5));
        scriptWrapper.setOpaque(false);
        JLabel scriptLabel = new JLabel("Processor Script");
        scriptLabel.setFont(scriptLabel.getFont().deriveFont(Font.BOLD));
        scriptWrapper.add(scriptLabel, BorderLayout.NORTH);
        scriptWrapper.add(script, BorderLayout.CENTER);
        formPanel.add(scriptWrapper, gbc);

        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.4;

        JPanel bodyWrapper = new JPanel(new BorderLayout(0, 5));
        this.bodyWrapper = bodyWrapper;
        bodyWrapper.setOpaque(false);
        bodyWrapper.add(bodyTemplate, BorderLayout.CENTER);
        formPanel.add(bodyWrapper, gbc);

        JPanel tabbedPaneWrapper = new JPanel(new BorderLayout());
        JLabel fieldListLabel = new JLabel("Field List");
        fieldListLabel.setFont(fieldListLabel.getFont().deriveFont(Font.BOLD, 16f));
        fieldListLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        tabbedPaneWrapper.add(fieldListLabel, BorderLayout.NORTH);

        JBTabbedPane tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("JSON Field List", jsonFieldsPanel);
        tabbedPane.addTab("Header Field List", headerFieldsPanel);
        tabbedPaneWrapper.add(tabbedPane, BorderLayout.CENTER);

        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(tabbedPaneWrapper, gbc);
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        this.add(formPanel, BorderLayout.CENTER);
        updateBodyTemplateVisibility();
    }

    /**
     * Actualiza la visibilidad del área de plantilla de cuerpo según el método seleccionado.
     */
    private void updateBodyTemplateVisibility() {
        if (bodyWrapper == null) return;
        String selectedMethod = (String) method.getSelectedItem();
        boolean show = selectedMethod != null && METHODS_WITH_BODY.contains(selectedMethod);
        bodyWrapper.setVisible(show);
    }

    /**
     * Crea un panel con etiqueta y componente de entrada.
     *
     * @param labelText      Texto de la etiqueta.
     * @param inputComponent Componente de entrada asociado.
     * @return Panel con etiqueta y campo.
     */
    private JPanel createLabeledField(String labelText, JComponent inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, BorderLayout.NORTH);
        panel.add(inputComponent, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Actualiza los valores del formulario a partir de un objeto JSON.
     *
     * @param jsonObject Objeto JSON con los datos a cargar.
     */
    @Override
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (jsonObject.has("threads") && !jsonObject.get("threads").isJsonNull()) {
            threadsField.setValue(jsonObject.get("threads").getAsInt());
        }
        if (jsonObject.has("commitInterval") && !jsonObject.get("commitInterval").isJsonNull()) {
            commitIntervalField.setValue(jsonObject.get("commitInterval").getAsInt());
        }
        if (jsonObject.has("pageSize") && !jsonObject.get("pageSize").isJsonNull()) {
            pageSizeField.setValue(jsonObject.get("pageSize").getAsInt());
        }
        if (jsonObject.has("saveFilePath") && !jsonObject.get("saveFilePath").isJsonNull()) {
            saveFilePathArea.setText(jsonObject.get("saveFilePath").getAsString());
        }


        if (jsonObject.has("reader") && jsonObject.get("reader").isJsonObject()) {
            JsonObject reader = jsonObject.getAsJsonObject("reader");

            if (reader.has("apiRequest") && reader.get("apiRequest").isJsonObject()) {
                JsonObject api = reader.getAsJsonObject("apiRequest");

                if (api.has("script") && !api.get("script").isJsonNull()) {
                    script.setText(api.get("script").getAsString());
                }

                if (api.has("urlTemplate") && !api.get("urlTemplate").isJsonNull()) {
                    urlTemplate.setText(api.get("urlTemplate").getAsString());
                }
                if (api.has("method") && !api.get("method").isJsonNull()) {
                    method.setSelectedItem(api.get("method").getAsString());
                    updateBodyTemplateVisibility();
                }
                if (api.has("serviceName") && !api.get("serviceName").isJsonNull()) {
                    serviceName.setText(api.get("serviceName").getAsString());
                }
                if (api.has("apiVersion") && !api.get("apiVersion").isJsonNull()) {
                    apiVersion.setText(api.get("apiVersion").getAsString());
                }
                if (api.has("timeout") && !api.get("timeout").isJsonNull()) {
                    timeout.setValue(api.get("timeout").getAsInt());
                }
                if (api.has("rootElement") && !api.get("rootElement").isJsonNull()) {
                    rootElement.setText(api.get("rootElement").getAsString());
                }
                if (api.has("bodyTemplate") && !api.get("bodyTemplate").isJsonNull()) {
                    bodyTemplate.setText(api.get("bodyTemplate").getAsString());
                }

                if (api.has("saveToFile") && api.get("saveToFile").isJsonObject()) {
                    JsonObject saveToFile = api.getAsJsonObject("saveToFile");
                    if (saveToFile.has("targetPath") && !saveToFile.get("targetPath").isJsonNull()) {
                        saveFilePathArea.setText(saveToFile.get("targetPath").getAsString());
                    }
                    if (saveToFile.has("fileNameQuery") && !saveToFile.get("fileNameQuery").isJsonNull()) {
                        saveFileNameQueryArea.setText(saveToFile.get("fileNameQuery").getAsString());
                    }
                    if (saveToFile.has("fileName") && !saveToFile.get("fileName").isJsonNull()) {
                        String fileName = saveToFile.get("fileName").getAsString();
                        if (!fileName.isEmpty()) {
                            fileNameRadio.setSelected(true);
                        } else if (!saveFileNameQueryArea.getText().isEmpty()) {
                            fileNameQueryRadio.setSelected(true);
                        }
                    }
                }
            }
        }
        this.jsonFieldsPanel.updateForm(jsonObject);
        this.headerFieldsPanel.updateForm(jsonObject);
    }

    /**
     * Actualiza el objeto JSON con los valores actuales del formulario.
     *
     * @param jsonObject Objeto JSON a actualizar.
     */
    @Override
    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null) return;

        jsonObject.addProperty("threads", (Integer) threadsField.getValue());
        jsonObject.addProperty("commitInterval", (Integer) commitIntervalField.getValue());
        jsonObject.addProperty("pageSize", (Integer) pageSizeField.getValue());
        jsonObject.addProperty("saveFilePath", saveFilePathArea.getText());

        JsonObject reader = jsonObject.has("reader") ? jsonObject.getAsJsonObject("reader") : new JsonObject();
        JsonObject api = reader.has("apiRequest") ? reader.getAsJsonObject("apiRequest") : new JsonObject();

        api.addProperty("script", script.getTextArea().getText());
        api.addProperty("urlTemplate", urlTemplate.getText());
        api.addProperty("method", (String) method.getSelectedItem());
        String selectedMethod = (String) method.getSelectedItem();
        if (selectedMethod != null && METHODS_WITH_BODY.contains(selectedMethod)) {
            api.addProperty("bodyTemplate", bodyTemplate.getText());
        } else {
            api.remove("bodyTemplate");
        }
        api.addProperty("serviceName", serviceName.getText());
        api.addProperty("apiVersion", apiVersion.getText());
        api.addProperty("timeout", (Integer) timeout.getValue());
        api.addProperty("rootElement", rootElement.getText());

        JsonObject saveToFile = new JsonObject();
        saveToFile.addProperty("targetPath", saveFilePathArea.getText());
        saveToFile.addProperty("fileNameQuery", saveFileNameQueryArea.getText());

        if (fileNameRadio.isSelected()) {
            saveToFile.addProperty("fileName", saveFileNameQueryArea.getText());
            saveToFile.addProperty("fileNameQuery", "");
        } else if (fileNameQueryRadio.isSelected()) {
            saveToFile.addProperty("fileName", "");
            saveToFile.addProperty("fileNameQuery", saveFileNameQueryArea.getText());
        }
        api.add("saveToFile", saveToFile);

        reader.add("apiRequest", api);
        jsonObject.add("reader", reader);

        this.headerFieldsPanel.updateDocument(jsonObject);
        this.jsonFieldsPanel.updateDocument(jsonObject);
    }

    /**
     * Añade listeners a los campos del formulario para detectar cambios.
     *
     * @param textListener   Listener para cambios en campos de texto.
     * @param actionListener Listener para acciones de botones y combos.
     * @param changeListener Listener para cambios en spinners y paneles.
     */
    @Override
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        threadsField.addChangeListener(changeListener);
        commitIntervalField.addChangeListener(changeListener);
        urlTemplate.getDocument().addDocumentListener(textListener);
        method.addActionListener(actionListener);
        serviceName.getDocument().addDocumentListener(textListener);
        apiVersion.getDocument().addDocumentListener(textListener);
        timeout.addChangeListener(changeListener);
        pageSizeField.addChangeListener(changeListener);
        rootElement.getDocument().addDocumentListener(textListener);
        saveFilePathArea.getDocument().addDocumentListener(textListener);
        saveFileNameQueryArea.getDocument().addDocumentListener(textListener);
        script.getTextArea().getDocument().addDocumentListener(textListener);
        fileNameRadio.addActionListener(actionListener);
        fileNameQueryRadio.addActionListener(actionListener);
        bodyTemplate.getDocument().addDocumentListener(textListener);

        jsonFieldsPanel.setChangeCallback(() -> {
            if (changeListener != null) {
                changeListener.stateChanged(new javax.swing.event.ChangeEvent(jsonFieldsPanel));
            }
        });
        headerFieldsPanel.setChangeCallback(() -> {
            if (changeListener != null) {
                changeListener.stateChanged(new javax.swing.event.ChangeEvent(headerFieldsPanel));
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
