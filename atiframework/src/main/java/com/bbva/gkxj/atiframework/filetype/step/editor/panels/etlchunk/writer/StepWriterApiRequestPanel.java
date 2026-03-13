package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.writer;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsGeneralPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsPanelMode;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.PanelType;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepType;
import com.google.gson.JsonObject;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * Panel de configuración para el paso Writer de tipo Api Request.
 * Permite configurar los parámetros necesarios para realizar peticiones API como Writer en un proceso ETL.
 * Incluye campos para URL, método, nombre de servicio, versión, timeout, tamaño de página, root element, rutas de guardado, script de procesado y listas de campos JSON y Header.
 */
public class StepWriterApiRequestPanel extends JPanel {

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
     * Panel para la lista de campos JSON.
     */
    private final FieldsGeneralPanel jsonFieldsPanel;

    /**
     * Panel para la lista de campos Header.
     */
    private final FieldsGeneralPanel headerFieldsPanel;

    /**
     * Métodos HTTP que permiten cuerpo en la petición.
     */
    private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");

    /**
     * Constructor del panel StepWriterApiRequestPanel.
     * Inicializa los paneles de campos y componentes de la interfaz.
     */
    public StepWriterApiRequestPanel() {
        this.jsonFieldsPanel = new FieldsGeneralPanel(StepType.WRITER, PanelType.API_REQUEST, FieldsPanelMode.JSON);
        this.headerFieldsPanel = new FieldsGeneralPanel(StepType.WRITER, PanelType.API_REQUEST, FieldsPanelMode.HEADER);
        initUIComponents();
    }

    /**
     * Inicializa y organiza los componentes de la interfaz gráfica.
     */
    private void initUIComponents() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Writer Configuration");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 18f));
        this.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(8);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;


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

        saveFileNameQueryArea = new AtiResizableTextArea("Save File Name Query", new JTextArea(3, 20), true);

        script = new AtiScriptPanel();

        bodyTemplate = new AtiResizableTextArea("Body Template", new JTextArea(3, 80), true);

        updateBodyTemplateVisibility();

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 3.0;
        formPanel.add(createLabeledField("URL", urlTemplate), gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        formPanel.add(createLabeledField("Method", method), gbc);

        gbc.gridy = 1;
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


        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        formPanel.add(createLabeledField("Root Element", rootElement), gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        formPanel.add(saveFilePathArea, gbc);

        gbc.gridy = 4;
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

        gbc.gridy = 5;
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

        gbc.gridy = 6;
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

        gbc.gridy = 7;
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
     * @param labelText Texto de la etiqueta.
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
     * @param jsonObject Objeto JSON con los datos a cargar.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (jsonObject.has("writer") && jsonObject.get("writer").isJsonObject()) {
            JsonObject writer = jsonObject.getAsJsonObject("writer");

            if (writer.has("apiRequest") && writer.get("apiRequest").isJsonObject()) {
                JsonObject api = writer.getAsJsonObject("apiRequest");

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
                if (api.has("pageSize") && !api.get("pageSize").isJsonNull()) {
                    pageSizeField.setValue(api.get("pageSize").getAsInt());
                }
                if (api.has("rootElement") && !api.get("rootElement").isJsonNull()) {
                    rootElement.setText(api.get("rootElement").getAsString());
                }
                if (api.has("bodyTemplate") && !api.get("bodyTemplate").isJsonNull()) {
                    bodyTemplate.setText(api.get("bodyTemplate").getAsString());
                }
                if (api.has("processorScript") && !api.get("processorScript").isJsonNull()) {
                    script.getTextArea().setText(api.get("processorScript").getAsString());
                }

                if (api.has("saveToFile") && api.get("saveToFile").isJsonObject()) {
                    JsonObject saveToFile = api.getAsJsonObject("saveToFile");
                    if (saveToFile.has("targetPath") && !saveToFile.get("targetPath").isJsonNull()) {
                        saveFilePathArea.setText(saveToFile.get("targetPath").getAsString());
                    }

                    String fileName = "";
                    String fileNameQuery = "";

                    if (saveToFile.has("fileName") && !saveToFile.get("fileName").isJsonNull()) {
                        fileName = saveToFile.get("fileName").getAsString();
                    }
                    if (saveToFile.has("fileNameQuery") && !saveToFile.get("fileNameQuery").isJsonNull()) {
                        fileNameQuery = saveToFile.get("fileNameQuery").getAsString();
                    }

                    if (fileName != null && !fileName.isEmpty()) {
                        fileNameRadio.setSelected(true);
                        saveFileNameQueryArea.setText(fileName);
                    } else if (fileNameQuery != null && !fileNameQuery.isEmpty()) {
                        fileNameQueryRadio.setSelected(true);
                        saveFileNameQueryArea.setText(fileNameQuery);
                    }
                }
            }
        }
        this.jsonFieldsPanel.updateForm(jsonObject);
        this.headerFieldsPanel.updateForm(jsonObject);
    }

    /**
     * Actualiza el objeto JSON con los valores actuales del formulario.
     * @param jsonObject Objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null) return;

        JsonObject writer = jsonObject.has("writer") ? jsonObject.getAsJsonObject("writer") : new JsonObject();
        JsonObject api = writer.has("apiRequest") ? writer.getAsJsonObject("apiRequest") : new JsonObject();

        api.addProperty("urlTemplate", urlTemplate.getText());
        api.addProperty("method", (String) method.getSelectedItem());
        api.addProperty("serviceName", serviceName.getText());
        api.addProperty("apiVersion", apiVersion.getText());
        api.addProperty("timeout", (Integer) timeout.getValue());
        api.addProperty("pageSize", (Integer) pageSizeField.getValue());
        api.addProperty("rootElement", rootElement.getText());

        String selectedMethod = (String) method.getSelectedItem();
        if (selectedMethod != null && METHODS_WITH_BODY.contains(selectedMethod)) {
            api.addProperty("bodyTemplate", bodyTemplate.getText());
        } else {
            api.remove("bodyTemplate");
        }
        api.addProperty("processorScript", script.getTextArea().getText());

        JsonObject saveToFile = new JsonObject();
        saveToFile.addProperty("targetPath", saveFilePathArea.getText());

        if (fileNameRadio.isSelected()) {
            saveToFile.addProperty("fileName", saveFileNameQueryArea.getText());
            saveToFile.addProperty("fileNameQuery", "");
        } else if (fileNameQueryRadio.isSelected()) {
            saveToFile.addProperty("fileName", "");
            saveToFile.addProperty("fileNameQuery", saveFileNameQueryArea.getText());
        }
        api.add("saveToFile", saveToFile);

        writer.add("apiRequest", api);
        jsonObject.add("writer", writer);

        this.jsonFieldsPanel.updateDocument(jsonObject);
        this.headerFieldsPanel.updateDocument(jsonObject);
    }

    /**
     * Añade listeners a los campos del formulario para detectar cambios.
     * @param textListener Listener para cambios en campos de texto.
     * @param actionListener Listener para acciones de botones y combos.
     * @param changeListener Listener para cambios en spinners y paneles.
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
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
     * @return Panel principal.
     */
    public JPanel getComponent() {
        return this;
    }
}
