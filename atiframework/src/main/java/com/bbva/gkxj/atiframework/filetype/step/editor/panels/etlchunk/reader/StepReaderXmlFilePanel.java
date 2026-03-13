package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.reader;

import com.bbva.gkxj.atiframework.components.AtiJSpinner;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsGeneralPanel;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.FieldsPanelMode;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.PanelType;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepType;
import com.google.gson.JsonObject;
import com.intellij.util.ui.JBUI;

import com.intellij.ui.components.JBTabbedPane;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel de configuración para el paso Reader de tipo XML File.
 * Permite configurar los parámetros necesarios para leer archivos XML en un proceso ETL.
 * Incluye campos para hilos, commit, grid, root element y listas de campos XML y Header.
 */
public class StepReaderXmlFilePanel extends JPanel implements StepReaderSubPanel {

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
     * Campo para el elemento raíz del XML.
     */
    private JTextField rootElement;

    /**
     * Panel para la lista de campos XML.
     */
    private final FieldsGeneralPanel xmlFieldsPanel;

    /**
     * Panel para la lista de campos Header.
     */
    private final FieldsGeneralPanel headerFieldsPanel;

    /**
     * Constructor del panel StepReaderXmlFilePanel.
     * Inicializa los paneles de campos y componentes de la interfaz.
     */
    public StepReaderXmlFilePanel() {
        this.xmlFieldsPanel = new FieldsGeneralPanel(StepType.READER, PanelType.XML,FieldsPanelMode.XML);
        this.headerFieldsPanel = new FieldsGeneralPanel(StepType.READER, PanelType.XML,FieldsPanelMode.HEADER);
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
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        rootElement = new AtiTextField();

        // Primera fila: todos los spinner fields
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        formPanel.add(createLabeledField("# of Threads", threadsField), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabeledField("Commit Interval", commitIntervalField), gbc);
        gbc.gridx = 2;
        formPanel.add(createLabeledField("Grid Size", gridSizeField), gbc);
        gbc.gridx = 3;
        formPanel.add(createLabeledField("Grid Min File Size", gridMinFileSizeField), gbc);

        // Segunda fila: rootElement field, ocupando 40% width
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0.4;
        formPanel.add(createLabeledField("Root Element", rootElement), gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;

        // Add tabbed field panels below
        JBTabbedPane tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("XML Field List", xmlFieldsPanel);
        tabbedPane.addTab("Header Field List", headerFieldsPanel);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(tabbedPane, gbc);
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

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

            if (reader.has("xmlFile") && reader.get("xmlFile").isJsonObject()) {
                JsonObject fixedFile = reader.getAsJsonObject("xmlFile");

                if (fixedFile.has("rootElement") && !fixedFile.get("rootElement").isJsonNull()) {
                    rootElement.setText(fixedFile.get("rootElement").getAsString());
                }

            }
        }
        this.xmlFieldsPanel.updateForm(jsonObject);
        this.headerFieldsPanel.updateForm(jsonObject);
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

        fixedFile.addProperty("rootElement", rootElement.getText());

        reader.add("xmlFile", fixedFile);
        jsonObject.add("reader", reader);
        this.xmlFieldsPanel.updateDocument(jsonObject);
        this.headerFieldsPanel.updateDocument(jsonObject);
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
        rootElement.getDocument().addDocumentListener(textListener);

        xmlFieldsPanel.setChangeCallback(() -> {
            if (changeListener != null) {
                changeListener.stateChanged(new javax.swing.event.ChangeEvent(xmlFieldsPanel));
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
    @Override
    public JPanel getComponent() {
        return this;
    }

}

