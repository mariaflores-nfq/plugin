package com.bbva.gkxj.atiframework.filetype.step.editor.panels.filemanteinance;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.MetadataData;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.TagData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants.FILE_TASK_OPERATION_TYPES;

/**
 * Panel principal para la configuración de tareas de mantenimiento de ficheros.
 * Incluye campos comunes (Operation Type, File Mask, Source Path, Target Path, Days Ago, Move Files)
 * y un CardLayout para mostrar subpaneles específicos según el tipo de operación seleccionado:
 * vacío (normal), Nova Transfer o Epsilon.
 */
public class StepFileTaskPanel extends JPanel {

    /**
     * Constantes para identificar las tarjetas del CardLayout de tipos de operación y contenido dinámico.
     */
    private static final String CARD_EMPTY = "empty";

    /**
     * Constantes para identificar las tarjetas del CardLayout de tipos de operación y contenido dinámico.
     */
    private static final String CARD_NOVA = "novaTransfer";

    /**
     * Constantes para identificar las tarjetas del CardLayout de tipos de operación y contenido dinámico.
     */
    private static final String CARD_EPSILON = "epsilon";

    /**
     * Constantes para identificar las tarjetas del CardLayout de contenido dinámico en los formularios de Tags y Metadata.
     */
    private static final String CARD_CONTENT_EMPTY = "contentEmpty";

    /**
     * Constantes para identificar las tarjetas del CardLayout de contenido dinámico en los formularios de Tags y Metadata.
     */
    private static final String CARD_CONTENT_FIXED = "contentFixed";

    /**
     * Constantes para identificar las tarjetas del CardLayout de contenido dinámico en los formularios de Tags y Metadata.
     */
    private static final String CARD_CONTENT_STEP_PARAM = "contentStepParam";

    /**
     * Constantes para identificar las tarjetas del CardLayout de contenido dinámico en los formularios de Tags y Metadata.
     */
    private static final String CARD_CONTENT_SCRIPT = "contentScript";

    /**
     * Lista de tipos de campo para Tags y Metadata (valor fijo, step parameter o script).
     */
    private static final String[] FIELD_TYPES = {"", "Fixed Value", "Step Parameter", "Script"};

    /**
     * Lista de nombres de parámetros del step disponibles.
     */
    private final List<String> stepParameterNames = new ArrayList<>();

    /**
     * Combo para seleccionar el tipo de operación.
     */
    private AtiComboBox operationTypeCombo;

    /**
     * Área de texto para la máscara de fichero.
     */
    private AtiResizableTextArea fileMaskField;

    /**
     * Área de texto para la ruta de origen.
     */
    private AtiResizableTextArea sourcePathField;

    /**
     * Área de texto para la ruta de destino.
     */
    private AtiResizableTextArea targetPathField;

    /**
     * Spinner para los días atrás.
     */
    private AtiJSpinner daysAgoField;

    /**
     * Toggle para mover ficheros.
     */
    private CustomToggleSwitch moveFilesCheck;

    /**
     * Contenedor con CardLayout para los subpaneles específicos.
     */
    private JPanel operationTypeCards;

    /**
     * CardLayout del contenedor de tarjetas.
     */
    private CardLayout cardLayout;

    /**
     * Campo de texto para el nombre de Nova Transfer.
     */
    private AtiTextField novaTransferName;

    /**
     * Campo de texto para el UUAA de Nova Transfer.
     */
    private AtiTextField novaTransferUUAA;

    /**
     * Campo de texto para el Bucket ID de Epsilon.
     */
    private AtiTextField epsilonBucketId;

    /**
     * Campo de texto para el usuario de Epsilon.
     */
    private AtiTextField epsilonUser;

    /**
     * Área de texto para la descripción de Epsilon.
     */
    private AtiResizableTextArea epsilonDescription;

    /**
     * Pestañas de Epsilon (Tags y Metadata).
     */
    private JBTabbedPane epsilonTabs;

    /**
     * Splitter de tabla para los tags de Epsilon.
     */
    private AtiTableSplitterPanel<TagData> tagsTableSplitter;

    /**
     * Splitter de tabla para los metadatos de Epsilon.
     */
    private AtiTableSplitterPanel<MetadataData> metadataTableSplitter;

    /**
     * Contador para IDs de tags.
     */
    private int tagCounter = 0;

    /**
     * Contador para IDs de metadatos.
     */
    private int metadataCounter = 0;

    /**
     * Tag actualmente seleccionado para editar.
     */
    private TagData currentTagSelection = null;

    /**
     * Flag para evitar actualizaciones en cascada al cargar datos en el form de tags.
     */
    private boolean isUpdatingTagForm = false;

    /**
     * Combo de tipo de valor para tags.
     */
    private AtiComboBox tagTypeCombo;

    /**
     * CardLayout del contenido dinámico del form de tags.
     */
    private CardLayout tagContentCardLayout;

    /**
     * Contenedor del contenido dinámico del form de tags.
     */
    private JPanel tagContentCards;

    /**
     * Campo de valor fijo para tags.
     */
    private AtiTextField tagFixedValueField;

    /**
     * Combo de step parameter para tags.
     */
    private AtiComboBox tagStepParamCombo;

    /**
     * Panel de script para tags.
     */
    private AtiScriptPanel tagScriptPanel;

    /**
     * Metadato actualmente seleccionado para editar.
     */
    private MetadataData currentMetaSelection = null;

    /**
     * Flag para evitar actualizaciones en cascada al cargar datos en el form de metadata.
     */
    private boolean isUpdatingMetaForm = false;

    /**
     * Combo de tipo de valor para metadata.
     */
    private AtiComboBox metaTypeCombo;

    /**
     * CardLayout del contenido dinámico del form de metadata.
     */
    private CardLayout metaContentCardLayout;

    /**
     * Contenedor del contenido dinámico del form de metadata.
     */
    private JPanel metaContentCards;

    /**
     * Campo de valor fijo para metadata.
     */
    private AtiTextField metaFixedValueField;

    /**
     * Campo para el nombre de la clave del metadato (campo "key" en el JSON).
     */
    private AtiTextField metaKeyNameField;

    /**
     * Combo de step parameter para metadata.
     */
    private AtiComboBox metaStepParamCombo;

    /**
     * Panel de script para metadata.
     */
    private AtiScriptPanel metaScriptPanel;

    /**
     * Construye el panel de tareas de fichero.
     */
    public StepFileTaskPanel() {
        initUIComponents();
    }

    /**
     * Inicializa y dispone todos los componentes de la interfaz gráfica.
     */
    private void initUIComponents() {
        operationTypeCombo = new AtiComboBox(FILE_TASK_OPERATION_TYPES);
        fileMaskField = new AtiResizableTextArea("File Mask", new JTextArea(3, 80), true);
        sourcePathField = new AtiResizableTextArea("Source Path", new JTextArea(3, 80), true);
        targetPathField = new AtiResizableTextArea("Target Path", new JTextArea(3, 80), true);
        daysAgoField = new AtiJSpinner(new javax.swing.SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
        moveFilesCheck = new CustomToggleSwitch();

        cardLayout = new CardLayout();
        operationTypeCards = new JPanel(cardLayout);

        operationTypeCards.add(createEmptyPanel(), CARD_EMPTY);
        operationTypeCards.add(createNovaTransferPanel(), CARD_NOVA);
        operationTypeCards.add(createEpsilonPanel(), CARD_EPSILON);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(8);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 0;

        formPanel.add(new AtiLabeledComponent("Operation Type", operationTypeCombo), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        formPanel.add(fileMaskField, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        formPanel.add(sourcePathField, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;

        gbc.gridx = 0;
        gbc.weightx = 0.70;
        formPanel.add(targetPathField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.15;
        formPanel.add(new AtiLabeledComponent("Days Ago", daysAgoField), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.15;
        formPanel.add(new AtiLabeledComponent("Move Files", moveFilesCheck), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = JBUI.emptyInsets();
        formPanel.add(operationTypeCards, gbc);

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);

        operationTypeCombo.addActionListener(e -> {
            String selected = (String) operationTypeCombo.getSelectedItem();
            if (selected == null) {
                cardLayout.show(operationTypeCards, CARD_EMPTY);
                return;
            }
            switch (selected) {
                case "NOVA TRANSFER" -> cardLayout.show(operationTypeCards, CARD_NOVA);
                case "EPSILON UPLOAD", "EPSILON DOWNLOAD", "EPSILON DELETE" ->
                        cardLayout.show(operationTypeCards, CARD_EPSILON);
                default -> cardLayout.show(operationTypeCards, CARD_EMPTY);
            }
        });
    }

    /**
     * Crea el panel vacío (sin parámetros adicionales).
     *
     * @return Panel vacío transparente.
     */
    private JPanel createEmptyPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Crea el panel de parámetros específicos de Nova Transfer.
     *
     * @return Panel con los campos Name y UUAA para Nova Transfer.
     */
    private JPanel createNovaTransferPanel() {
        novaTransferName = new AtiTextField();
        novaTransferUUAA = new AtiTextField();
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = JBUI.insets(8);
        panel.add(new AtiLabeledComponent("Nova Transfer. Name", novaTransferName), gbc);
        gbc.gridx = 1;
        panel.add(new AtiLabeledComponent("Nova Transfer. UUAA", novaTransferUUAA), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(new JPanel(), gbc);
        return panel;
    }

    /**
     * Crea el panel de parámetros específicos de Epsilon (Upload, Download, Delete).
     * Incluye Bucket ID, User, Description y dos pestañas con tablas para Tags y Metadata.
     *
     * @return Panel con todos los campos y pestañas de Epsilon.
     */
    private JPanel createEpsilonPanel() {
        epsilonBucketId = new AtiTextField();
        epsilonUser = new AtiTextField();
        epsilonDescription = new AtiResizableTextArea("Description", new JTextArea(3, 80), true);

        tagsTableSplitter = new AtiTableSplitterPanel<>(
                "Tags", "Value",
                () -> new TagData(String.format("%02d", ++tagCounter)),
                TagData::getId, TagData::getKey,
                buildTagFormPanel()
        );
        tagsTableSplitter.setSelectionListener(item -> {
            if (currentTagSelection != null) syncTagFormToData();
            currentTagSelection = item;
            loadTagDataIntoForm(item);
        });
        tagsTableSplitter.setDeselectionListener(() -> currentTagSelection = null);

        metadataTableSplitter = new AtiTableSplitterPanel<>(
                "Metadata", "Value",
                () -> new MetadataData(String.format("%02d", ++metadataCounter)),
                MetadataData::getId, MetadataData::getKey,
                buildMetadataFormPanel()
        );
        metadataTableSplitter.setSelectionListener(item -> {
            if (currentMetaSelection != null) syncMetaFormToData();
            currentMetaSelection = item;
            loadMetaDataIntoForm(item);
        });
        metadataTableSplitter.setDeselectionListener(() -> currentMetaSelection = null);

        epsilonTabs = new JBTabbedPane();
        epsilonTabs.addTab("Tags", tagsTableSplitter);
        epsilonTabs.addTab("Metadata", metadataTableSplitter);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new AtiLabeledComponent("Bucket Id", epsilonBucketId), gbc);
        gbc.gridx = 1;
        panel.add(new AtiLabeledComponent("User", epsilonUser), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(epsilonDescription, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(epsilonTabs, gbc);

        return panel;
    }

    /**
     * Construye el panel de formulario dinámico para la edición de un Tag.
     * Contiene un combo de tipo (Fixed Value, Step Parameter, Script) y un CardLayout
     * que muestra el campo correspondiente según la selección.
     *
     * @return JPanel con el formulario de tags.
     */
    private JPanel buildTagFormPanel() {
        tagTypeCombo = new AtiComboBox(FIELD_TYPES);
        tagFixedValueField = new AtiTextField();
        tagStepParamCombo = new AtiComboBox(stepParameterNames.toArray(new String[0]));
        tagScriptPanel = new AtiScriptPanel();

        tagContentCardLayout = new CardLayout();
        tagContentCards = new JPanel(tagContentCardLayout);
        tagContentCards.setOpaque(false);

        JPanel fixedPanel = new JPanel(new GridBagLayout());
        fixedPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        fixedPanel.add(new AtiLabeledComponent("Fixed Value", tagFixedValueField), gbc);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        fixedPanel.add(new JPanel(), gbc);
        tagContentCards.add(fixedPanel, CARD_CONTENT_FIXED);

        JPanel stepPanel = new JPanel(new GridBagLayout());
        stepPanel.setOpaque(false);
        gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        stepPanel.add(new AtiLabeledComponent("Step Parameter", tagStepParamCombo), gbc);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        stepPanel.add(new JPanel(), gbc);
        tagContentCards.add(stepPanel, CARD_CONTENT_STEP_PARAM);

        JPanel scriptPanel = new JPanel(new GridBagLayout());
        scriptPanel.setOpaque(false);
        gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        scriptPanel.add(new AtiLabeledComponent("Script", tagScriptPanel), gbc);
        tagContentCards.add(scriptPanel, CARD_CONTENT_SCRIPT);
        tagContentCards.add(new JPanel(), CARD_CONTENT_EMPTY);

        JPanel form = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new AtiLabeledComponent("Value type", tagTypeCombo), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(new JPanel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = JBUI.insets(0, 8);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        form.add(tagContentCards, gbc);

        tagTypeCombo.addActionListener(e -> {
            if (isUpdatingTagForm) return;
            String selected = (String) tagTypeCombo.getSelectedItem();
            showTagContentCard(selected);
            if (currentTagSelection != null) {
                syncTagFormToData();
                tagsTableSplitter.updateSelectedRowName(currentTagSelection.getKey());
            }
        });
        tagStepParamCombo.addActionListener(e -> {
            if (isUpdatingTagForm || currentTagSelection == null) return;
            String selected = (String) tagStepParamCombo.getSelectedItem();
            currentTagSelection.setStepParameter(selected != null ? selected : "");
            tagsTableSplitter.updateSelectedRowName(currentTagSelection.getKey());
        });

        return form;
    }

    /**
     * Construye el panel de formulario dinámico para la edición de un Metadato.
     * Contiene un combo de tipo (Fixed Value, Step Parameter, Script) y un CardLayout
     * que muestra el campo correspondiente según la selección.
     *
     * @return JPanel con el formulario de metadata.
     */
    private JPanel buildMetadataFormPanel() {
        metaKeyNameField = new AtiTextField();
        metaTypeCombo = new AtiComboBox(FIELD_TYPES);
        metaFixedValueField = new AtiTextField();
        metaStepParamCombo = new AtiComboBox(stepParameterNames.toArray(new String[0]));
        metaScriptPanel = new AtiScriptPanel();

        metaContentCardLayout = new CardLayout();
        metaContentCards = new JPanel(metaContentCardLayout);
        metaContentCards.setOpaque(false);

        JPanel fixedPanel = new JPanel(new GridBagLayout());
        fixedPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        fixedPanel.add(new AtiLabeledComponent("Fixed Value", metaFixedValueField), gbc);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        fixedPanel.add(new JPanel(), gbc);
        metaContentCards.add(fixedPanel, CARD_CONTENT_FIXED);

        JPanel stepPanel = new JPanel(new GridBagLayout());
        stepPanel.setOpaque(false);
        gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        stepPanel.add(new AtiLabeledComponent("Step Parameter", metaStepParamCombo), gbc);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        stepPanel.add(new JPanel(), gbc);
        metaContentCards.add(stepPanel, CARD_CONTENT_STEP_PARAM);

        JPanel scriptPanel = new JPanel(new GridBagLayout());
        scriptPanel.setOpaque(false);
        gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        scriptPanel.add(new AtiLabeledComponent("Script", metaScriptPanel), gbc);
        metaContentCards.add(scriptPanel, CARD_CONTENT_SCRIPT);
        metaContentCards.add(new JPanel(), CARD_CONTENT_EMPTY);

        JPanel form = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        form.add(new AtiLabeledComponent("Key", metaKeyNameField), gbc);
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new AtiLabeledComponent("Value type", metaTypeCombo), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(new JPanel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        form.add(metaContentCards, gbc);

        metaTypeCombo.addActionListener(e -> {
            if (isUpdatingMetaForm) return;
            String selected = (String) metaTypeCombo.getSelectedItem();
            showMetaContentCard(selected);
            if (currentMetaSelection != null) {
                syncMetaFormToData();
                metadataTableSplitter.updateSelectedRowName(currentMetaSelection.getKey());
            }
        });
        metaStepParamCombo.addActionListener(e -> {
            if (isUpdatingMetaForm || currentMetaSelection == null) return;
            String selected = (String) metaStepParamCombo.getSelectedItem();
            currentMetaSelection.setStepParameter(selected != null ? selected : "");
            metadataTableSplitter.updateSelectedRowName(currentMetaSelection.getKey());
        });

        return form;
    }

    /**
     * Muestra la tarjeta de contenido correspondiente al tipo seleccionado en el form de Tags.
     *
     * @param type Tipo seleccionado ("Fixed Value", "Step Parameter", "Script" o vacío).
     */
    private void showTagContentCard(String type) {
        if (type == null || type.isEmpty()) {
            tagContentCardLayout.show(tagContentCards, CARD_CONTENT_EMPTY);
        } else {
            switch (type) {
                case "Fixed Value" -> tagContentCardLayout.show(tagContentCards, CARD_CONTENT_FIXED);
                case "Step Parameter" -> tagContentCardLayout.show(tagContentCards, CARD_CONTENT_STEP_PARAM);
                case "Script" -> tagContentCardLayout.show(tagContentCards, CARD_CONTENT_SCRIPT);
                default -> tagContentCardLayout.show(tagContentCards, CARD_CONTENT_EMPTY);
            }
        }
    }

    /**
     * Muestra la tarjeta de contenido correspondiente al tipo seleccionado en el form de Metadata.
     *
     * @param type Tipo seleccionado ("Fixed Value", "Step Parameter", "Script" o vacío).
     */
    private void showMetaContentCard(String type) {
        if (type == null || type.isEmpty()) {
            metaContentCardLayout.show(metaContentCards, CARD_CONTENT_EMPTY);
        } else {
            switch (type) {
                case "Fixed Value" -> metaContentCardLayout.show(metaContentCards, CARD_CONTENT_FIXED);
                case "Step Parameter" -> metaContentCardLayout.show(metaContentCards, CARD_CONTENT_STEP_PARAM);
                case "Script" -> metaContentCardLayout.show(metaContentCards, CARD_CONTENT_SCRIPT);
                default -> metaContentCardLayout.show(metaContentCards, CARD_CONTENT_EMPTY);
            }
        }
    }

    /**
     * Carga los datos de un TagData en los campos del formulario de Tags.
     *
     * @param tag TagData a cargar.
     */
    private void loadTagDataIntoForm(TagData tag) {
        isUpdatingTagForm = true;
        try {
            tagFixedValueField.setText(tag.getFixedValue() != null ? tag.getFixedValue() : "");
            tagScriptPanel.getTextArea().setText(tag.getScript() != null ? tag.getScript() : "");
            refreshStepParamCombo(tagStepParamCombo, tag.getStepParameter() != null ? tag.getStepParameter() : "");
            tagTypeCombo.setSelectedItem(tag.getType() != null ? tag.getType() : "");
            showTagContentCard(tag.getType());
        } finally {
            isUpdatingTagForm = false;
        }
    }

    /**
     * Carga los datos de un MetadataData en los campos del formulario de Metadata.
     *
     * @param meta MetadataData a cargar.
     */
    private void loadMetaDataIntoForm(MetadataData meta) {
        isUpdatingMetaForm = true;
        try {
            metaKeyNameField.setText(meta.getKeyName() != null ? meta.getKeyName() : "");
            metaFixedValueField.setText(meta.getFixedValue() != null ? meta.getFixedValue() : "");
            metaScriptPanel.getTextArea().setText(meta.getScript() != null ? meta.getScript() : "");
            refreshStepParamCombo(metaStepParamCombo, meta.getStepParameter() != null ? meta.getStepParameter() : "");
            metaTypeCombo.setSelectedItem(meta.getType() != null ? meta.getType() : "");
            showMetaContentCard(meta.getType());
        } finally {
            isUpdatingMetaForm = false;
        }
    }

    /**
     * Actualiza el combo de Step Parameter con la selección correcta.
     *
     * @param combo    Combo a actualizar.
     * @param selected Valor a seleccionar.
     */
    private void refreshStepParamCombo(AtiComboBox combo, String selected) {
        combo.setSelectedItem(selected);
    }

    /**
     * Sincroniza los valores del formulario de Tags al objeto TagData actualmente seleccionado.
     */
    private void syncTagFormToData() {
        if (currentTagSelection == null) return;
        String type = (String) tagTypeCombo.getSelectedItem();
        currentTagSelection.setType(type != null ? type : "");
        currentTagSelection.setFixedValue(tagFixedValueField.getText());
        currentTagSelection.setStepParameter(getSelectedStepParam(tagStepParamCombo));
        currentTagSelection.setScript(tagScriptPanel.getTextArea().getText());
    }

    /**
     * Sincroniza los valores del formulario de Metadata al objeto MetadataData actualmente seleccionado.
     */
    private void syncMetaFormToData() {
        if (currentMetaSelection == null) return;
        currentMetaSelection.setKeyName(metaKeyNameField.getText());
        String type = (String) metaTypeCombo.getSelectedItem();
        currentMetaSelection.setType(type != null ? type : "");
        currentMetaSelection.setFixedValue(metaFixedValueField.getText());
        currentMetaSelection.setStepParameter(getSelectedStepParam(metaStepParamCombo));
        currentMetaSelection.setScript(metaScriptPanel.getTextArea().getText());
    }

    /**
     * Devuelve el ítem seleccionado en el combo de step parameter, o cadena vacía si es null.
     *
     * @param combo Combo del que obtener la selección.
     * @return Ítem seleccionado o cadena vacía.
     */
    private String getSelectedStepParam(AtiComboBox combo) {
        String val = (String) combo.getSelectedItem();
        return val != null ? val : "";
    }

    /**
     * Actualiza la lista de parámetros de step disponibles para los combos de "Step Parameter".
     * Debe llamarse desde el editor padre al cargar o actualizar los parámetros del step.
     *
     * @param paramNames Lista de nombres de parámetros del step.
     */
    public void updateStepParameters(List<String> paramNames) {
        stepParameterNames.clear();
        stepParameterNames.add("fileName");
        stepParameterNames.add("executionCode");
        stepParameterNames.addAll(paramNames);

        String[] items = stepParameterNames.toArray(new String[0]);
        if (tagStepParamCombo != null) {
            String prevTag = (String) tagStepParamCombo.getSelectedItem();
            tagStepParamCombo.removeAllItems();
            for (String name : items) tagStepParamCombo.addItem(name);
            tagStepParamCombo.setSelectedItem(prevTag);
        }
        if (metaStepParamCombo != null) {
            String prevMeta = (String) metaStepParamCombo.getSelectedItem();
            metaStepParamCombo.removeAllItems();
            for (String name : items) metaStepParamCombo.addItem(name);
            metaStepParamCombo.setSelectedItem(prevMeta);
        }
    }

    /**
     * Carga los datos de un JsonObject en los campos del formulario, actualizando la interfaz según el tipo de operación
     * @param jsonObject JsonObject con los datos de la tarea de mantenimiento de ficheros a cargar en el formulario.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;

        JsonObject fileTask = null;
        if (jsonObject.has("fileTask") && jsonObject.get("fileTask").isJsonObject()) {
            fileTask = jsonObject.getAsJsonObject("fileTask");
        }

        operationTypeCombo.setSelectedItem(
                fileTask != null && fileTask.has("fileOper") && !fileTask.get("fileOper").isJsonNull()
                        ? fileTask.get("fileOper").getAsString()
                        : ""
        );
        fileMaskField.setText(
                fileTask != null && fileTask.has("fileMask") && !fileTask.get("fileMask").isJsonNull()
                        ? fileTask.get("fileMask").getAsString()
                        : ""
        );
        sourcePathField.setText(
                fileTask != null && fileTask.has("baseDir") && !fileTask.get("baseDir").isJsonNull()
                        ? fileTask.get("baseDir").getAsString()
                        : ""
        );
        targetPathField.setText(
                fileTask != null && fileTask.has("toDir") && !fileTask.get("toDir").isJsonNull()
                        ? fileTask.get("toDir").getAsString()
                        : ""
        );
        daysAgoField.setValue(
                fileTask != null && fileTask.has("daysAgo") && !fileTask.get("daysAgo").isJsonNull()
                        ? fileTask.get("daysAgo").getAsInt()
                        : 0
        );
        moveFilesCheck.setSelected(
                fileTask != null && fileTask.has("move") && !fileTask.get("move").isJsonNull()
                        && fileTask.get("move").getAsBoolean()
        );

        novaTransferUUAA.setText(
                fileTask != null && fileTask.has("uuaa") && !fileTask.get("uuaa").isJsonNull()
                        ? fileTask.get("uuaa").getAsString()
                        : ""
        );
        novaTransferName.setText(
                fileTask != null && fileTask.has("transferName") && !fileTask.get("transferName").isJsonNull()
                        ? fileTask.get("transferName").getAsString()
                        : ""
        );

        JsonObject epsilon = null;
        if (fileTask != null && fileTask.has("epsilon") && fileTask.get("epsilon").isJsonObject()) {
            epsilon = fileTask.getAsJsonObject("epsilon");
        }

        epsilonBucketId.setText(
                epsilon != null && epsilon.has("bucketId") && !epsilon.get("bucketId").isJsonNull()
                        ? epsilon.get("bucketId").getAsString()
                        : ""
        );
        epsilonUser.setText(
                epsilon != null && epsilon.has("ivUser") && !epsilon.get("ivUser").isJsonNull()
                        ? epsilon.get("ivUser").getAsString()
                        : ""
        );
        epsilonDescription.setText(
                epsilon != null && epsilon.has("description") && !epsilon.get("description").isJsonNull()
                        ? epsilon.get("description").getAsString()
                        : ""
        );

        List<TagData> tags = new ArrayList<>();
        int tagIdx = 0;
        if (epsilon != null && epsilon.has("tagList") && epsilon.get("tagList").isJsonArray()) {
            for (JsonElement el : epsilon.getAsJsonArray("tagList")) {
                if (!el.isJsonObject()) continue;
                JsonObject tagObj = el.getAsJsonObject();
                tagIdx++;
                TagData tag = new TagData(String.format("%02d", tagIdx));

                if (tagObj.has("fixedValue") && !tagObj.get("fixedValue").isJsonNull()) {
                    tag.setType("Fixed Value");
                    tag.setFixedValue(tagObj.get("fixedValue").getAsString());
                } else if (tagObj.has("stepParameter") && !tagObj.get("stepParameter").isJsonNull()) {
                    tag.setType("Step Parameter");
                    tag.setStepParameter(tagObj.get("stepParameter").getAsString());
                } else if (tagObj.has("script") && !tagObj.get("script").isJsonNull()) {
                    tag.setType("Script");
                    tag.setScript(tagObj.get("script").getAsString());
                } else {
                    tag.setType("");
                }
                tags.add(tag);
            }
        }
        tagCounter = tagIdx;
        currentTagSelection = null;
        tagsTableSplitter.reloadData(tags);

        List<MetadataData> metas = new ArrayList<>();
        int metaIdx = 0;
        if (epsilon != null && epsilon.has("metadataList") && epsilon.get("metadataList").isJsonArray()) {
            for (JsonElement el : epsilon.getAsJsonArray("metadataList")) {
                if (!el.isJsonObject()) continue;
                JsonObject metaObj = el.getAsJsonObject();
                metaIdx++;

                MetadataData meta = new MetadataData(String.format("%02d", metaIdx));
                if (metaObj.has("key") && !metaObj.get("key").isJsonNull()) {
                    meta.setKeyName(metaObj.get("key").getAsString());
                }

                JsonObject valueObj = null;
                if (metaObj.has("value") && metaObj.get("value").isJsonObject()) {
                    valueObj = metaObj.getAsJsonObject("value");
                }

                if (valueObj != null && valueObj.has("fixedValue") && !valueObj.get("fixedValue").isJsonNull()) {
                    meta.setType("Fixed Value");
                    meta.setFixedValue(valueObj.get("fixedValue").getAsString());
                } else if (valueObj != null && valueObj.has("stepParameter") && !valueObj.get("stepParameter").isJsonNull()) {
                    meta.setType("Step Parameter");
                    meta.setStepParameter(valueObj.get("stepParameter").getAsString());
                } else if (valueObj != null && valueObj.has("script") && !valueObj.get("script").isJsonNull()) {
                    meta.setType("Script");
                    meta.setScript(valueObj.get("script").getAsString());
                } else {
                    meta.setType("");
                }

                metas.add(meta);
            }
        }
        metadataCounter = metaIdx;
        currentMetaSelection = null;
        metadataTableSplitter.reloadData(metas);
    }

    /**
     * Actualiza el JsonObject con los datos actuales del formulario.
     * Escribe dentro del objeto "fileTask" respetando la estructura del JSON.
     *
     * @param jsonObject Objeto JSON raíz del step a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null) return;

        syncTagFormToData();
        syncMetaFormToData();

        JsonObject fileTask = (jsonObject.has("fileTask") && jsonObject.get("fileTask").isJsonObject())
                ? jsonObject.getAsJsonObject("fileTask")
                : new JsonObject();

        fileTask.addProperty("fileOper", (String) operationTypeCombo.getSelectedItem());
        fileTask.addProperty("fileMask", fileMaskField.getText());
        fileTask.addProperty("baseDir", sourcePathField.getText());
        fileTask.addProperty("toDir", targetPathField.getText());
        fileTask.addProperty("daysAgo", ((Number) daysAgoField.getValue()).intValue());
        fileTask.addProperty("move", moveFilesCheck.isSelected());


        if(operationTypeCombo.getSelectedItem() != null && operationTypeCombo.getSelectedItem().toString().equals("NOVA TRANSFER")) {
            fileTask.addProperty("transferName", novaTransferName.getText());
            fileTask.addProperty("uuaa", novaTransferUUAA.getText());
        } else {
            fileTask.remove("transferName");
            fileTask.remove("uuaa");
        }

        String selectedOper = (String) operationTypeCombo.getSelectedItem();
        if (selectedOper != null && selectedOper.startsWith("EPSILON")) {
            JsonObject epsilon = (fileTask.has("epsilon") && fileTask.get("epsilon").isJsonObject())
                    ? fileTask.getAsJsonObject("epsilon")
                    : new JsonObject();

            epsilon.addProperty("bucketId", epsilonBucketId.getText());
            epsilon.addProperty("ivUser", epsilonUser.getText());
            epsilon.addProperty("description", epsilonDescription.getText());

            JsonArray tagArray = new JsonArray();
            for (TagData tag : tagsTableSplitter.getDataList()) {
                JsonObject tagObj = new JsonObject();
                String type = tag.getType();
                if ("Fixed Value".equals(type) && tag.getFixedValue() != null && !tag.getFixedValue().isEmpty()) {
                    tagObj.addProperty("fixedValue", tag.getFixedValue());
                } else if ("Step Parameter".equals(type) && tag.getStepParameter() != null && !tag.getStepParameter().isEmpty()) {
                    tagObj.addProperty("stepParameter", tag.getStepParameter());
                } else if ("Script".equals(type) && tag.getScript() != null && !tag.getScript().isEmpty()) {
                    tagObj.addProperty("script", tag.getScript());
                }
                tagArray.add(tagObj);
            }
            epsilon.add("tagList", tagArray);

            JsonArray metaArray = new JsonArray();
            for (MetadataData meta : metadataTableSplitter.getDataList()) {
                JsonObject metaObj = new JsonObject();
                if (meta.getKeyName() != null && !meta.getKeyName().isEmpty()) {
                    metaObj.addProperty("key", meta.getKeyName());
                }

                JsonObject valueObj = new JsonObject();
                String type = meta.getType();
                if ("Fixed Value".equals(type) && meta.getFixedValue() != null && !meta.getFixedValue().isEmpty()) {
                    valueObj.addProperty("fixedValue", meta.getFixedValue());
                } else if ("Step Parameter".equals(type) && meta.getStepParameter() != null && !meta.getStepParameter().isEmpty()) {
                    valueObj.addProperty("stepParameter", meta.getStepParameter());
                } else if ("Script".equals(type) && meta.getScript() != null && !meta.getScript().isEmpty()) {
                    valueObj.addProperty("script", meta.getScript());
                }
                metaObj.add("value", valueObj);

                if (metaObj.has("key")) {
                    metaArray.add(metaObj);
                }
            }
            epsilon.add("metadataList", metaArray);

            fileTask.add("epsilon", epsilon);
        } else {
            fileTask.remove("epsilon");
        }

        jsonObject.add("fileTask", fileTask);
    }

    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     *
     * @param textListener   Listener para campos de texto.
     * @param actionListener Listener para acciones (combos, botones, etc.).
     * @param changeListener Listener para spinners y toggles.
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        operationTypeCombo.addActionListener(actionListener);
        moveFilesCheck.addChangeListener(changeListener);
        fileMaskField.getTextArea().getDocument().addDocumentListener(textListener);
        sourcePathField.getTextArea().getDocument().addDocumentListener(textListener);
        targetPathField.getTextArea().getDocument().addDocumentListener(textListener);
        epsilonBucketId.getDocument().addDocumentListener(textListener);
        epsilonUser.getDocument().addDocumentListener(textListener);
        epsilonDescription.getTextArea().getDocument().addDocumentListener(textListener);
        novaTransferName.getDocument().addDocumentListener(textListener);
        novaTransferUUAA.getDocument().addDocumentListener(textListener);
        daysAgoField.addChangeListener(changeListener);

        tagsTableSplitter.setChangeCallback(() -> actionListener.actionPerformed(null));
        metadataTableSplitter.setChangeCallback(() -> actionListener.actionPerformed(null));

        javax.swing.event.DocumentListener tagGuardedListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { if (!isUpdatingTagForm) textListener.insertUpdate(e); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { if (!isUpdatingTagForm) textListener.removeUpdate(e); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { if (!isUpdatingTagForm) textListener.changedUpdate(e); }
        };
        ActionListener tagGuardedActionListener = e -> { if (!isUpdatingTagForm) actionListener.actionPerformed(e); };
        tagFixedValueField.getDocument().addDocumentListener(tagGuardedListener);
        tagTypeCombo.addActionListener(tagGuardedActionListener);
        tagStepParamCombo.addActionListener(tagGuardedActionListener);
        tagScriptPanel.getTextArea().getDocument().addDocumentListener(tagGuardedListener);

        javax.swing.event.DocumentListener metaGuardedListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { if (!isUpdatingMetaForm) textListener.insertUpdate(e); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { if (!isUpdatingMetaForm) textListener.removeUpdate(e); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { if (!isUpdatingMetaForm) textListener.changedUpdate(e); }
        };
        ActionListener metaGuardedActionListener = e -> { if (!isUpdatingMetaForm) actionListener.actionPerformed(e); };
        metaKeyNameField.getDocument().addDocumentListener(metaGuardedListener);
        metaFixedValueField.getDocument().addDocumentListener(metaGuardedListener);
        metaTypeCombo.addActionListener(metaGuardedActionListener);
        metaStepParamCombo.addActionListener(metaGuardedActionListener);
        metaScriptPanel.getTextArea().getDocument().addDocumentListener(metaGuardedListener);
    }
}
