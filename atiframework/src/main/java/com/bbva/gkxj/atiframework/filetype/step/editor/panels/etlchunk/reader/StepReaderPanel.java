package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.reader;

import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.components.AtiJLabel;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.PanelType;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.ReaderType;
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
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.*;
import static com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants.ETL_STEP_READER_TYPES;

/**
 * Panel principal de configuración del reader en el editor de steps.
 * Contiene un combo para seleccionar el tipo de reader
 */
public class StepReaderPanel extends JPanel {

    /**
     * Clave del CardLayout usada cuando el tipo seleccionado no tiene subpanel propio.
     */
    private static final String CARD_EMPTY = "__EMPTY__";

    /**
     * Proyecto de IntelliJ asociado al panel.
     */
    private final Project myProject;

    /**
     * Referencia al Document asociado al archivo virtual que se está editando.
     */
    private final Document myDocument;

    /**
     * Referencia al archivo virtual que se está editando.
     */
    private final VirtualFile myFile;

    /**
     * ComboBox para seleccionar el tipo de reader.
     */
    private AtiComboBox readerTypeField;

    /**
     * Panel con CardLayout: contiene todas las cartas pre-inicializadas.
     */
    private final JPanel cardPanel;

    /**
     * CardLayout para gestionar las cartas del cardPanel.
     */
    private final CardLayout cardLayout;

    /**
     * Mapa tipo subpanel. Solo contiene los tipos que tienen implementación real.
     */
    private final Map<ReaderType, StepReaderSubPanel> subPanelMap = new EnumMap<>(ReaderType.class);

    /**
     * Flag para controlar el estado de edición.
     */
    private boolean isUpdatingUI = false;

    /**
     * Constructor del panel principal de configuración del reader.
     * @param project proyecto de IntelliJ asociado al editor.
     * @param file archivo virtual que se está editando, usado para obtener el Document y pasar a subpaneles si es necesario.
     */
    public StepReaderPanel(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myFile = file;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        this.readerTypeField = buildReaderTypeCombo();
        this.cardLayout = new CardLayout();
        this.cardPanel = buildCardPanel();
        initLayout();
        setupInternalListeners();
    }

    /**
     * Construye el combo de selección de tipo de reader, llenándolo con los nombres de los tipos definidos
     * en StepConstants.ReaderType.
     * @return AtiComboBox configurado con los tipos de reader y estilo visual acorde al tema.
     */
    private AtiComboBox buildReaderTypeCombo() {
        String[] types = Arrays.stream(ReaderType.values())
                .map(ReaderType::getDisplayName)
                .toArray(String[]::new);
        AtiComboBox combo = new AtiComboBox(types);
        combo.setBackground(SchedulerTheme.BG_CARD);
        applyBlueFocusBorder(combo);
        return combo;
    }

    /**
     * Construye el panel con CardLayout, pre-inicializando todas las cartas para cada tipo de reader.
     * @return JPanel con CardLayout listo para mostrar las cartas según el tipo seleccionado.
     */
    private JPanel buildCardPanel() {
        JPanel panel = new JPanel(cardLayout);
        panel.setBackground(SchedulerTheme.BG_CARD);

        JPanel emptyCard = new JPanel();
        emptyCard.setOpaque(false);
        panel.add(emptyCard, CARD_EMPTY);

        for (ReaderType type : ReaderType.values()) {
            StepReaderSubPanel subPanel = createSubPanel(type);
            if (subPanel != null) {
                subPanelMap.put(type, subPanel);
                panel.add(subPanel.getComponent(), type.name());
            }
        }
        cardLayout.show(panel, CARD_EMPTY);
        return panel;
    }

    /**
     * Inicializa el layout del panel principal, organizando el formulario en la parte superior
     * y el CardLayout en el centro. Se utiliza GridBagLayout para el formulario para permitir una fácil
     * expansión y alineación de los campos.
     */
    private void initLayout() {
        setLayout(new BorderLayout());
        setBackground(SchedulerTheme.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SchedulerTheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = JBUI.insets(6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.25;

        c.gridx = 0; c.gridy = 0;
        form.add(new AtiJLabel("Reader Type"), c);

        c.gridy = 1;
        readerTypeField = new AtiComboBox(ETL_STEP_READER_TYPES);
        readerTypeField.setBackground(SchedulerTheme.BG_CARD);
        applyBlueFocusBorder(readerTypeField);
        form.add(readerTypeField, c);

        c.gridy = 2;
        c.weighty = 1.0;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        form.add(spacer, c);

        add(form, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
    }

    /**
     * Configura los listeners internos del panel, principalmente el ActionListener del combo de tipo de reader
     * para mostrar la carta correspondiente.
     */
    private void setupInternalListeners() {
        readerTypeField.addActionListener(e -> {
            if (isUpdatingUI) return;
            ReaderType selectedType = getSelectedReaderType();
            showCardForType(selectedType);

            // Limpiar los datos del JSON cuando cambias de tipo de Reader para evitar que los campos
            // del tipo anterior persistan en el nuevo tipo de reader
            cleanReaderDataInDocument(selectedType);

            // Actualizar el formulario del subpanel con los datos limpios
            StepReaderSubPanel subPanel = subPanelMap.get(selectedType);
            if (subPanel != null && myDocument != null) {
                try {
                    String currentText = myDocument.getText();
                    Gson gson = new GsonBuilder().create();
                    JsonObject jsonObject = gson.fromJson(currentText, JsonObject.class);
                    if (jsonObject != null) {
                        subPanel.updateForm(jsonObject);
                    }
                } catch (Exception ex) {
                    // Ignorar errores al actualizar el formulario
                }
            }
        });
    }

    /**
     * Limpia los datos del reader en el documento, manteniendo solo el tipo seleccionado.
     * Esto evita que campos de un reader anterior persistan en el nuevo tipo.
     * El tipo se infiere automáticamente de la presencia de la clave JSON correspondiente.
     * @param selectedType tipo de reader seleccionado
     */
    private void cleanReaderDataInDocument(ReaderType selectedType) {
        if (myDocument == null) return;

        try {
            String currentText = myDocument.getText();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject = gson.fromJson(currentText, JsonObject.class);

            if (jsonObject != null && selectedType != null) {
                JsonObject readerObj = jsonObject.has("reader") && jsonObject.get("reader").isJsonObject()
                        ? jsonObject.getAsJsonObject("reader")
                        : new JsonObject();

                // Limpiar todos los campos específicos del reader excepto el que corresponde al tipo seleccionado
                String selectedKey = getJsonKeyForReaderType(selectedType);
                JsonObject cleanedReader = new JsonObject();

                // Preservar la estructura del tipo seleccionado pero vacía
                if (selectedKey != null) {
                    if (readerObj.has(selectedKey) && readerObj.get(selectedKey).isJsonObject()) {
                        cleanedReader.add(selectedKey, readerObj.get(selectedKey));
                    } else {
                        cleanedReader.add(selectedKey, new JsonObject());
                    }
                }

                jsonObject.add("reader", cleanedReader);

                // Limpiar campos de nivel raíz que corresponden a tipos específicos
                // (pageSize, saveFilePath son campos que corresponden a API_REQUEST)
                jsonObject.remove("pageSize");
                jsonObject.remove("saveFilePath");
                jsonObject.remove("threads");
                jsonObject.remove("commitInterval");

                String json = gson.toJson(jsonObject);
                WriteCommandAction.runWriteCommandAction(myProject, () -> myDocument.setText(json));
            }
        } catch (Exception e) {
            // Ignorar errores al limpiar el documento
        }
    }

    /**
     * Obtiene el tipo de reader seleccionado actualmente en el combo.
     * @return StepConstants.ReaderType correspondiente al item seleccionado, o null si no se puede mapear.
     */
    private ReaderType getSelectedReaderType() {
        return ReaderType.fromDisplayName((String) readerTypeField.getSelectedItem());
    }

    /**
     * Mapea un ReaderType a su clave JSON correspondiente en el PanelType.
     * @param readerType tipo de reader
     * @return clave JSON correcta (ej: "csvFile", "apiRequest", etc)
     */
    private String getJsonKeyForReaderType(ReaderType readerType) {
        if (readerType == null) return null;
        return switch (readerType) {
            case API_REQUEST -> PanelType.API_REQUEST.getValue();
            case CSV_FILE -> PanelType.CSV.getValue();
            case FIXED_FILE -> PanelType.FIXED.getValue();
            case XML_FILE -> PanelType.XML.getValue();
            case QUERIES -> "queries"; // QUERIES no tiene un tipo de panel JSON específico
        };
    }

    /**
     * Infiere el tipo de reader basándose en qué clave está presente en el objeto reader del JSON.
     * El tipo se determina por la presencia de una clave conocida (queries, csvFile, fixedFile, xmlFile, apiRequest).
     * @param readerObj objeto JSON del reader
     * @return ReaderType inferido, o null si no se puede determinar
     */
    private ReaderType inferReaderTypeFromJson(JsonObject readerObj) {
        if (readerObj == null) return null;

        if (readerObj.has("queries")) return ReaderType.QUERIES;
        if (readerObj.has("csvFile")) return ReaderType.CSV_FILE;
        if (readerObj.has("fixedFile")) return ReaderType.FIXED_FILE;
        if (readerObj.has("xmlFile")) return ReaderType.XML_FILE;
        if (readerObj.has("apiRequest")) return ReaderType.API_REQUEST;

        return null;
    }

    /**
     * Cambia la carta visible en el CardLayout.
     * No hay removeAll, no hay add, no hay revalidate/repaint manual:
     * CardLayout gestiona todo internamente con coste O(1).
     */
    private void showCardForType(ReaderType type) {
        if (type != null && subPanelMap.containsKey(type)) {
            cardLayout.show(cardPanel, type.name());
        } else {
            cardLayout.show(cardPanel, CARD_EMPTY);
        }
    }

    /**
     * Factory method: único lugar donde se decide qué clase instanciar por tipo.
     */
    private StepReaderSubPanel createSubPanel(ReaderType type) {
        return switch (type) {
            case API_REQUEST -> new StepReaderApiRequestPanel(myProject, myFile);
            case CSV_FILE -> new StepReaderCsvPanel();
            case QUERIES -> new StepReaderQueriesPanel(myProject, myFile);
            case FIXED_FILE -> new StepReaderFixedPanel(myProject, myFile);
            case XML_FILE -> new StepReaderXmlFilePanel();
        };
    }

    /**
     * Actualiza el formulario con los datos del JsonObject proporcionado. Solo actualiza los campos del formulario
     * Infiere el tipo de reader detectando qué clave está presente en el JSON.
     * @param jsonObject objeto JSON con los datos a cargar en el formulario.
     */
    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("reader")) return;

        isUpdatingUI = true;
        try {
            JsonObject readerObj = jsonObject.getAsJsonObject("reader");
            ReaderType type = inferReaderTypeFromJson(readerObj);
            if (type != null) {
                readerTypeField.setSelectedItem(type.getDisplayName());
                showCardForType(type);

                StepReaderSubPanel subPanel = subPanelMap.get(type);
                if (subPanel != null) {
                    subPanel.updateForm(jsonObject);
                }
            }
        } finally {
            isUpdatingUI = false;
        }
    }

    /**
     * Actualiza el JsonObject con los datos actuales del formulario.
     * No guarda un atributo "type" explícito; el tipo se infiere de la presencia de la clave correspondiente.
     * @param jsonObject objeto JSON a actualizar.
     */
    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;

        JsonObject readerObj = jsonObject.has("reader") && jsonObject.get("reader").isJsonObject()
                ? jsonObject.getAsJsonObject("reader")
                : new JsonObject();

        if (!jsonObject.has("reader")) {
            jsonObject.add("reader", readerObj);
        }

        ReaderType selectedType = getSelectedReaderType();
        if (selectedType != null) {
            String selectedKey = getJsonKeyForReaderType(selectedType);
            readerObj.entrySet().removeIf(entry -> {
                String key = entry.getKey();
                return !key.equals(selectedKey);
            });

            StepReaderSubPanel subPanel = subPanelMap.get(selectedType);
            if (subPanel != null) {
                subPanel.updateDocument(jsonObject);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonObject);
        WriteCommandAction.runWriteCommandAction(myProject, () -> myDocument.setText(json));
    }

    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     * @param textListener listener para cambios en campos de texto.
     * @param actionListener listener para acciones.
     * @param changeListener listener para cambios en componentes.
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        readerTypeField.addActionListener(actionListener);
        subPanelMap.values().forEach(p ->
                p.addFieldListeners(textListener, actionListener, changeListener));
    }

}