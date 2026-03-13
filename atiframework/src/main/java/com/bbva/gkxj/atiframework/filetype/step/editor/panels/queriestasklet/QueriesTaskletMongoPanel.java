package com.bbva.gkxj.atiframework.filetype.step.editor.panels.queriestasklet;

import com.bbva.gkxj.atiframework.components.AtiComboBox;
import com.bbva.gkxj.atiframework.components.AtiResizableTextArea;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel de contenido para queries de tipo Mongo en el Queries Task.
 * Hereda de {@link AbstractQueriesTaskletPanel} la gestión completa de Output Parameters
 * y solo implementa la parte específica de la configuración Mongo (collectionName, options,
 * operaciones Update/Insert/Delete).
 */
public class QueriesTaskletMongoPanel extends AbstractQueriesTaskletPanel {

    // ─── Constantes de cards de operación ────────────────────────────────
    private static final String CARD_OP_UPDATE = "opUpdate";
    private static final String CARD_OP_INSERT = "opInsert";
    private static final String CARD_OP_DELETE = "opDelete";
    private static final String CARD_SUB_FILTER = "subFilter";
    private static final String CARD_SUB_UPDATE = "subUpdate";

    // ─── Componentes específicos de Mongo ────────────────────────────────
    private AtiTextField collectionField;
    private AtiTextField optionsField;
    private AtiComboBox operationCombo;

    private JPanel operationDynamicPanel;
    private CardLayout operationCardLayout;
    private AtiResizableTextArea updateMongoQueryArea;
    private AtiResizableTextArea filterQueryArea;
    private AtiResizableTextArea insertQueryArea;
    private AtiResizableTextArea deleteQueryArea;

    private JPanel filterTabBtn;
    private JPanel updateTabBtn;
    private JPanel subOperationContentPanel;
    private CardLayout subOperationCardLayout;
    private boolean filterTabSelected = true;

    // ─── Caché de operaciones ────────────────────────────────────────────
    private final Map<String, OperationCache> operationDataCache = new HashMap<>();
    private String currentOperation = "Update";

    /**
     * Constructor del panel Mongo Query en Queries Task.
     * Inicializa los componentes y el layout del panel.
     */
    public QueriesTaskletMongoPanel() {
        super();
        initComponents();
    }

    /**
     * Inicializa los componentes UI del panel: campos Mongo y sección de Output Parameters.
     */
    private void initComponents() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Collection (full width)
        c.gridy = 0; c.gridx = 0; c.gridwidth = 2;
        c.weightx = 1.0; c.insets = new Insets(0, 0, 5, 0);
        collectionField = new AtiTextField();
        collectionField.setPreferredSize(new Dimension(0, 30));
        add(createLabeledField("Collection", collectionField), c);

        // Options y Operation (2 columnas)
        c.gridy = 1; c.gridwidth = 1; c.weightx = 0.5;
        c.insets = new Insets(10, 0, 5, 10);
        optionsField = new AtiTextField();
        optionsField.setPreferredSize(new Dimension(0, 30));
        add(createLabeledField("Options", optionsField), c);

        c.gridx = 1; c.insets = new Insets(10, 10, 5, 0);
        operationCombo = new AtiComboBox(new String[]{"Update", "Insert", "Delete"});
        operationCombo.setPreferredSize(new Dimension(0, 30));
        operationCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                handleOperationChange();
            }
        });
        add(createLabeledField("Operation", operationCombo), c);

        // Operation dynamic panel
        c.gridy = 2; c.gridx = 0; c.gridwidth = 2;
        c.weightx = 1.0; c.insets = new Insets(0, 0, 15, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;

        operationCardLayout = new CardLayout();
        operationDynamicPanel = new JPanel(operationCardLayout);
        operationDynamicPanel.setBackground(BG_SIDEBAR);

        operationDynamicPanel.add(createUpdateOperationPanel(), CARD_OP_UPDATE);
        operationDynamicPanel.add(createSingleOperationPanel("Insert"), CARD_OP_INSERT);
        operationDynamicPanel.add(createSingleOperationPanel("Delete"), CARD_OP_DELETE);

        operationCardLayout.show(operationDynamicPanel, CARD_OP_UPDATE);
        add(operationDynamicPanel, c);

        // Output Parameters section (delegada a la clase abstracta)
        c.gridy = 3; c.fill = GridBagConstraints.BOTH; c.weighty = 1.0;
        c.insets = new Insets(0, 0, 0, 0);
        add(createOutputParametersSection(), c);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Paneles de operaciones Mongo
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Crea el panel específico para la operación de Update, con tabs para Filter y Update.
     */
    private JPanel createUpdateOperationPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_SIDEBAR);

        JPanel tabsPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        tabsPanel.setBackground(BG_SIDEBAR);

        filterTabBtn = createTabButton("Filter", true);
        updateTabBtn = createTabButton("Update", false);

        filterTabBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!filterTabSelected) {
                    selectTab(true);
                    subOperationCardLayout.show(subOperationContentPanel, CARD_SUB_FILTER);
                    notifyParent();
                }
            }
        });

        updateTabBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (filterTabSelected) {
                    selectTab(false);
                    subOperationCardLayout.show(subOperationContentPanel, CARD_SUB_UPDATE);
                    notifyParent();
                }
            }
        });

        tabsPanel.add(filterTabBtn);
        tabsPanel.add(updateTabBtn);
        panel.add(tabsPanel, BorderLayout.NORTH);

        subOperationCardLayout = new CardLayout();
        subOperationContentPanel = new JPanel(subOperationCardLayout);
        subOperationContentPanel.setBackground(BG_SIDEBAR);
        subOperationContentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        filterQueryArea = new AtiResizableTextArea("Filter Mongo Query", new JTextArea(), true);
        filterQueryArea.setMinimumSize(new Dimension(100, 80));
        filterQueryArea.setPreferredSize(new Dimension(100, 100));
        subOperationContentPanel.add(filterQueryArea, CARD_SUB_FILTER);

        updateMongoQueryArea = new AtiResizableTextArea("Update Mongo Query", new JTextArea(), true);
        updateMongoQueryArea.setMinimumSize(new Dimension(100, 80));
        updateMongoQueryArea.setPreferredSize(new Dimension(100, 100));
        subOperationContentPanel.add(updateMongoQueryArea, CARD_SUB_UPDATE);

        subOperationCardLayout.show(subOperationContentPanel, CARD_SUB_FILTER);
        panel.add(subOperationContentPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea un panel para las operaciones de Insert y Delete (un solo campo de Mongo Query).
     * @param operationType El tipo de operación ("Insert" o "Delete").
     */
    private JPanel createSingleOperationPanel(String operationType) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_SIDEBAR);

        JPanel tabsPanel = new JPanel(new BorderLayout());
        tabsPanel.setBackground(BG_SIDEBAR);
        tabsPanel.add(createFullWidthTabButton(operationType), BorderLayout.CENTER);
        panel.add(tabsPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_SIDEBAR);
        contentPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        AtiResizableTextArea queryArea = new AtiResizableTextArea(operationType + " Mongo Query", new JTextArea(), true);
        queryArea.setMinimumSize(new Dimension(100, 80));
        queryArea.setPreferredSize(new Dimension(100, 100));

        if ("Insert".equals(operationType)) {
            insertQueryArea = queryArea;
        } else {
            deleteQueryArea = queryArea;
        }

        contentPanel.add(queryArea, BorderLayout.CENTER);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Tabs de operaciones
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Crea un botón de tab personalizado con estilo para indicar selección.
     */
    private JPanel createTabButton(String text, boolean selected) {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(selected ? Color.WHITE : BG_SIDEBAR);
        tab.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, selected ? 2 : 0, 0, BBVA_BLUE),
                new EmptyBorder(8, 20, 8, 20)
        ));
        tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(selected ? BBVA_BLUE : TEXT_GRAY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        tab.add(label, BorderLayout.CENTER);

        return tab;
    }

    /**
     * Crea un botón de tab de ancho completo para operaciones sin subtabs (Insert, Delete).
     */
    private JPanel createFullWidthTabButton(String text) {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(Color.WHITE);
        tab.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, BBVA_BLUE),
                new EmptyBorder(8, 0, 8, 0)
        ));
        tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(BBVA_BLUE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        tab.add(label, BorderLayout.CENTER);

        return tab;
    }

    /**
     * Cambia la selección entre los tabs de Filter y Update.
     */
    private void selectTab(boolean filterSelected) {
        this.filterTabSelected = filterSelected;
        updateTabStyle(filterTabBtn, filterSelected);
        updateTabStyle(updateTabBtn, !filterSelected);
    }

    /**
     * Actualiza el estilo visual de un tab.
     */
    private void updateTabStyle(JPanel tab, boolean selected) {
        tab.setBackground(selected ? Color.WHITE : BG_SIDEBAR);
        tab.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, selected ? 2 : 0, 0, BBVA_BLUE),
                new EmptyBorder(8, 20, 8, 20)
        ));
        for (Component comp : tab.getComponents()) {
            if (comp instanceof JLabel label) {
                label.setForeground(selected ? BBVA_BLUE : TEXT_GRAY);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Gestión de operaciones y caché
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Maneja el cambio de operación (Update, Insert, Delete), guardando datos en caché.
     */
    private void handleOperationChange() {
        String selected = (String) operationCombo.getSelectedItem();
        if (selected == null || selected.equals(currentOperation)) {
            if (selected != null) showOperationCard(selected);
            return;
        }

        if (!isInternal && currentSelection != null && currentOperation != null) {
            saveCurrentOperationToCache(currentOperation);
        }
        if (!isInternal && currentSelection != null) {
            loadOperationFromCache(selected);
        }

        currentOperation = selected;
        showOperationCard(selected);

        if (!isInternal && currentSelection != null) {
            currentSelection.addProperty("operation", selected);
        }
        notifyParent();
    }

    /**
     * Muestra el panel correspondiente a la operación seleccionada.
     */
    private void showOperationCard(String operation) {
        switch (operation) {
            case "Insert" -> operationCardLayout.show(operationDynamicPanel, CARD_OP_INSERT);
            case "Delete" -> operationCardLayout.show(operationDynamicPanel, CARD_OP_DELETE);
            default -> operationCardLayout.show(operationDynamicPanel, CARD_OP_UPDATE);
        }
    }

    /**
     * Guarda los datos de la operación actual en la caché.
     */
    private void saveCurrentOperationToCache(String operation) {
        OperationCache cache = operationDataCache.computeIfAbsent(operation, k -> new OperationCache());
        switch (operation) {
            case "Update" -> {
                cache.filterQuery = filterQueryArea.getTextArea().getText();
                cache.updateMongoQuery = updateMongoQueryArea.getTextArea().getText();
            }
            case "Insert" -> cache.insertQuery = insertQueryArea.getTextArea().getText();
            case "Delete" -> cache.deleteQuery = deleteQueryArea.getTextArea().getText();
        }
    }

    /**
     * Carga los datos de la operación desde la caché.
     */
    private void loadOperationFromCache(String operation) {
        OperationCache cache = operationDataCache.get(operation);
        if (cache == null) {
            clearOperationFields(operation);
            return;
        }
        switch (operation) {
            case "Update" -> {
                filterQueryArea.getTextArea().setText(cache.filterQuery);
                updateMongoQueryArea.getTextArea().setText(cache.updateMongoQuery);
            }
            case "Insert" -> insertQueryArea.getTextArea().setText(cache.insertQuery);
            case "Delete" -> deleteQueryArea.getTextArea().setText(cache.deleteQuery);
        }
    }

    /**
     * Limpia los campos de la operación cuando no hay datos en caché.
     */
    private void clearOperationFields(String operation) {
        switch (operation) {
            case "Update" -> {
                filterQueryArea.getTextArea().setText("");
                updateMongoQueryArea.getTextArea().setText("");
            }
            case "Insert" -> insertQueryArea.getTextArea().setText("");
            case "Delete" -> deleteQueryArea.getTextArea().setText("");
        }
    }

    private String determineOperation(JsonObject q) {
        if (q.has("operation") && !q.get("operation").isJsonNull()) {
            return q.get("operation").getAsString();
        }
        if (q.has("insert") && !getStr(q, "insert").isEmpty()) return "Insert";
        if (q.has("delete") && !getStr(q, "delete").isEmpty()) return "Delete";
        return "Update";
    }

    private void initializeOperationCacheFromJson(JsonObject q) {
        OperationCache updateCache = new OperationCache();
        updateCache.filterQuery = getStr(q, "filter");
        updateCache.updateMongoQuery = getStr(q, "update");
        operationDataCache.put("Update", updateCache);

        OperationCache insertCache = new OperationCache();
        insertCache.insertQuery = getStr(q, "insert");
        operationDataCache.put("Insert", insertCache);

        OperationCache deleteCache = new OperationCache();
        deleteCache.deleteQuery = getStr(q, "delete");
        operationDataCache.put("Delete", deleteCache);
    }

    private void loadOperationFieldsFromJson(JsonObject q, String operation) {
        switch (operation) {
            case "Update" -> {
                filterQueryArea.getTextArea().setText(getStr(q, "filter"));
                updateMongoQueryArea.getTextArea().setText(getStr(q, "update"));
            }
            case "Insert" -> insertQueryArea.getTextArea().setText(getStr(q, "insert"));
            case "Delete" -> deleteQueryArea.getTextArea().setText(getStr(q, "delete"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Implementación de métodos abstractos
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Carga los datos Mongo de la query en los campos del panel.
     * @param q El objeto JsonObject con los datos a cargar.
     */
    @Override
    public void loadData(JsonObject q) {
        isInternal = true;
        try {
            collectionField.setText(getStr(q, "collectionName"));
            optionsField.setText(getStr(q, "options"));

            operationDataCache.clear();
            String operation = determineOperation(q);
            initializeOperationCacheFromJson(q);

            currentOperation = operation;
            operationCombo.setSelectedItem(operation);
            showOperationCard(operation);
            loadOperationFieldsFromJson(q, operation);

            loadOutputParameters(q);
        } finally {
            isInternal = false;
        }
    }

    /**
     * Guarda los datos Mongo del panel en el objeto query.
     * @param q El objeto JsonObject donde guardar los datos.
     */
    @Override
    public void saveData(JsonObject q) {
        q.addProperty("collectionName", collectionField.getText());
        q.addProperty("options", optionsField.getText());
        q.remove("sqlQuery");

        String operation = (String) operationCombo.getSelectedItem();
        if (operation != null) {
            q.addProperty("operation", operation);
        }

        q.remove("filter");
        q.remove("update");
        q.remove("insert");
        q.remove("delete");

        if (operation != null) {
            switch (operation) {
                case "Update" -> {
                    q.addProperty("filter", filterQueryArea.getTextArea().getText());
                    q.addProperty("update", updateMongoQueryArea.getTextArea().getText());
                }
                case "Insert" -> q.addProperty("insert", insertQueryArea.getTextArea().getText());
                case "Delete" -> q.addProperty("delete", deleteQueryArea.getTextArea().getText());
            }
        }

        saveSelectedParam();
    }

    /**
     * Registra listeners para sincronización automática con el JSON.
     * @param al ActionListener para componentes de tipo combo/botón.
     * @param dl DocumentListener para campos de texto.
     */
    @Override
    public void setupListeners(ActionListener al, DocumentListener dl) {
        if (collectionField != null) collectionField.getDocument().addDocumentListener(dl);
        if (optionsField != null) optionsField.getDocument().addDocumentListener(dl);
        if (operationCombo != null) operationCombo.addActionListener(al);

        if (updateMongoQueryArea != null) updateMongoQueryArea.getTextArea().getDocument().addDocumentListener(dl);
        if (filterQueryArea != null) filterQueryArea.getTextArea().getDocument().addDocumentListener(dl);
        if (insertQueryArea != null) insertQueryArea.getTextArea().getDocument().addDocumentListener(dl);
        if (deleteQueryArea != null) deleteQueryArea.getTextArea().getDocument().addDocumentListener(dl);

        // Param sync (delegado a la clase abstracta)
        setupParamListeners(al, dl);
    }

    /**
     * Sobreescribe la notificación al padre para incluir el comando "MongoChanged".
     */
    @Override
    protected void notifyParent() {
        if (parentActionListener != null) {
            parentActionListener.actionPerformed(
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "MongoChanged")
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Caché interna de operaciones
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Almacena temporalmente los datos de cada operación (Update, Insert, Delete)
     * mientras el usuario navega entre ellas.
     */
    private static class OperationCache {
        String filterQuery = "";
        String updateMongoQuery = "";
        String insertQuery = "";
        String deleteQuery = "";
    }
}

