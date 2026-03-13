package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.CacheInfo;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WorkStatementData;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WsOutputParameter;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WsQueryPanel extends JPanel {

    private final AtiTextField queryCodeField;
    private final AtiComboBox queryTypeCombo;
    private final AtiComboBox dbSourceCombo;

    // Contenedores dinámicos
    private final JPanel mongoContainer;
    private final JPanel sqlContainer;

    // Elementos Mongo
    private final AtiTextField collectionField;
    private  AtiTableSplitterPanel<PipelineWrapper> aggregatePanel;
    private final AtiScriptPanel pipelineTextArea;

    // Elementos SQL
    private final AtiScriptPanel sqlQueryArea;


    // Cache Info
    private final AtiTextField cacheNameField;
    private final AtiResizableTextArea cacheTtlField;
    private final AtiTextField cacheSizeField;

    private final Runnable onChange;
    private boolean isPopulating = false;

    private AtiTableSplitterPanel<WsOutputParameter> outputParamsPanel;
    private WsOutputParameterDetailView outputDetailView;

    public WsQueryPanel(Runnable onChange) {
        this.onChange = onChange;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // ==========================================
        // 1. FILA SUPERIOR (Común)
        // ==========================================
        JPanel topRow = new JPanel(new GridBagLayout());
        topRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 0; gbc.weightx = 0.33;

        queryCodeField = createTextField();
        queryTypeCombo = new AtiComboBox(new String[]{"Mongo Query", "SQL Query"});
        dbSourceCombo = new AtiComboBox(new String[]{"MongoDb", "SQLServer"});

        WorkflowThemeUtils.applyWorkflowTheme(queryTypeCombo);
        WorkflowThemeUtils.applyWorkflowTheme(dbSourceCombo);

        queryTypeCombo.addActionListener(e -> { switchQueryType(); notifyChange(); });
        dbSourceCombo.addActionListener(e -> notifyChange());

        gbc.gridy = 0;
        gbc.gridx = 0; gbc.insets = new Insets(5, 0, 10, 15);
        topRow.add(new AtiLabeledComponent("Query Code", queryCodeField), gbc);

        gbc.gridx = 1;
        topRow.add(new AtiLabeledComponent("Query Type", queryTypeCombo), gbc);

        gbc.gridx = 2; gbc.insets = new Insets(5, 0, 10, 0);
        topRow.add(new AtiLabeledComponent("Db Source", dbSourceCombo), gbc);
        add(topRow);

        // ==========================================
        // 2. CONTENEDOR MONGO
        // ==========================================
        mongoContainer = new JPanel();
        mongoContainer.setLayout(new BoxLayout(mongoContainer, BoxLayout.Y_AXIS));
        mongoContainer.setOpaque(false);

        collectionField = createTextField();
        mongoContainer.add(new AtiLabeledComponent("Collection", collectionField));
        mongoContainer.add(Box.createVerticalStrut(15));

        // --- CREAMOS EL FORMULARIO INLINE PARA EL PIPELINE ---
        pipelineTextArea = new AtiScriptPanel();
        JPanel pipelineForm = new JPanel(new BorderLayout());
        pipelineForm.setOpaque(false);
        pipelineForm.setBorder(JBUI.Borders.empty(15, 20));
        pipelineForm.add(new AtiLabeledComponent("Pipeline Content (JSON)", pipelineTextArea), BorderLayout.CENTER);

        // --- CREAMOS EL SPLITTER PANEL ---
        aggregatePanel = new AtiTableSplitterPanel<>(
                "Pipelines",
                "Pipeline Content",
                PipelineWrapper::new,
                item -> String.format("%02d", (aggregatePanel.getDataList().indexOf(item) + 1)),
                item -> (item.pipeline != null && !item.pipeline.isEmpty()) ? item.pipeline : "{New Pipeline}",
                pipelineForm // Pasamos el formulario inline que acabamos de crear
        );
        aggregatePanel.setPreferredSize(new Dimension(0, 220));

        // --- LISTENERS PARA SINCRONIZAR TABLA Y TEXTO ---
        aggregatePanel.setChangeCallback(this::notifyChange);

        // Al seleccionar un elemento en la tabla, lo mostramos en el texto
        aggregatePanel.setSelectionListener(item -> {
            isPopulating = true;
            pipelineTextArea.setText(item.pipeline != null ? item.pipeline : "");
            isPopulating = false;
        });

        // Al escribir en el texto, actualizamos el elemento y la tabla
        pipelineTextArea.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) {
                if (isPopulating) return;
                PipelineWrapper current = aggregatePanel.getCurrentSelection();
                if (current != null) {
                    current.pipeline = pipelineTextArea.getText();
                    aggregatePanel.updateSelectedRowName(current.pipeline);
                    notifyChange();
                }
            }
        });

        mongoContainer.add(new AtiLabeledComponent("Tab Aggregate (Pipelines)", aggregatePanel));

        add(mongoContainer);

//        // ==========================================
//        // 2. CONTENEDOR MONGO
//        // ==========================================
//        mongoContainer = new JPanel();
//        mongoContainer.setLayout(new BoxLayout(mongoContainer, BoxLayout.Y_AXIS));
//        mongoContainer.setOpaque(false);
//
//        collectionField = createTextField();
//        mongoContainer.add(new AtiLabeledComponent("Collection", collectionField));
//        mongoContainer.add(Box.createVerticalStrut(15));
//
//        aggregateTable = new AtiSimpleTablePanel<>(
//                "Nº", "Pipeline", PipelineWrapper::new, item -> item.pipeline != null ? item.pipeline : "{New Pipeline}"
//        );
//        aggregateTable.setPreferredSize(new Dimension(0, 150));
//        aggregateTable.setOnChange(this::notifyChange);
//        mongoContainer.add(new AtiLabeledComponent("Tab Aggregate (Pipelines)", aggregateTable));
//
//        add(mongoContainer);

        // ==========================================
        // 3. CONTENEDOR SQL
        // ==========================================
        sqlContainer = new JPanel(new BorderLayout());
        sqlContainer.setOpaque(false);
        sqlContainer.setBorder(JBUI.Borders.emptyTop(5));

        sqlQueryArea = new AtiScriptPanel();
        sqlQueryArea.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        sqlContainer.add(new AtiLabeledComponent("SQL Query", sqlQueryArea), BorderLayout.CENTER);

        add(sqlContainer);

        // ==========================================
        // 4. OUTPUT PARAMETERS (Splitter Panel)
        // ==========================================
        add(Box.createVerticalStrut(20));

        outputDetailView = new WsOutputParameterDetailView(() -> {});
        outputDetailView.setMinimumSize(new Dimension(250, 0));

        outputParamsPanel = new AtiTableSplitterPanel<>(
                "Output Parameters",
                "Field Name",
                WsOutputParameter::new,
                item -> String.format("%02d", (outputParamsPanel.getDataList().indexOf(item) + 1)),
                item -> item.fieldName != null && !item.fieldName.isEmpty() ? item.fieldName : "New Output",
                outputDetailView
        );
        outputParamsPanel.setPreferredSize(new Dimension(0, 220));

        // Listeners de la tabla y formulario
        outputParamsPanel.setChangeCallback(this::notifyChange);

        outputParamsPanel.setSelectionListener(item -> {
            outputDetailView.loadData(item);
        });

        outputDetailView.setOnChange(() -> {
            WsOutputParameter current = outputParamsPanel.getCurrentSelection();
            if (current != null) {
                outputDetailView.saveData(current);
                outputParamsPanel.updateSelectedRowName(current.fieldName);
                notifyChange();
            }
        });

        add(outputParamsPanel);

        // ==========================================
        // 5. CACHE INFO (Común)
        // ==========================================
        JPanel cacheRow = new JPanel(new BorderLayout());
        cacheRow.setOpaque(false);
        cacheRow.setBorder(JBUI.Borders.emptyTop(10));

        // Título de la sección
        AtiJLabel cacheTitle = new AtiJLabel("Cache Info");
        cacheTitle.setBorder(JBUI.Borders.emptyBottom(10));
        cacheRow.add(cacheTitle, BorderLayout.NORTH);

        cacheNameField = createTextField();
        cacheTtlField = createResizableTextArea();
        cacheSizeField = createTextField();

        // Usamos GridLayout(1 fila, 2 columnas) para obligar a un ancho exacto de 50/50.
        // El 20 es la separación en píxeles entre la columna izquierda y derecha.
        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        columnsPanel.setOpaque(false);

        // --- COLUMNA IZQUIERDA (Name y Size) ---
        JPanel leftCol = new JPanel(new GridBagLayout());
        leftCol.setOpaque(false);

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        gbcLeft.anchor = GridBagConstraints.NORTH;
        gbcLeft.weightx = 1.0;

        // Añadimos Name
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.insets = JBUI.insetsBottom(15);
        leftCol.add(new AtiLabeledComponent("Name", cacheNameField), gbcLeft);

        // Añadimos Size
        gbcLeft.gridy = 1;
        gbcLeft.insets = JBUI.insetsBottom(0);
        leftCol.add(new AtiLabeledComponent("Size", cacheSizeField), gbcLeft);

        // Muelle para absorber la altura y que Name y Size NO se muevan
        gbcLeft.gridy = 2;
        gbcLeft.weighty = 1.0;
        gbcLeft.fill = GridBagConstraints.BOTH;
        leftCol.add(Box.createGlue(), gbcLeft);


        // --- COLUMNA DERECHA (Time to live) ---
        // Usamos BorderLayout para aislar la altura del TextArea. Al ponerlo en NORTH,
        // respeta la altura que el usuario decida al arrastrar sin empujar a los lados.
        JPanel rightCol = new JPanel(new BorderLayout());
        rightCol.setOpaque(false);
        rightCol.add(new AtiLabeledComponent("Time to live", cacheTtlField), BorderLayout.NORTH);

        // Añadimos ambas columnas al panel que fuerza el 50/50
        columnsPanel.add(leftCol);
        columnsPanel.add(rightCol);

        // Añadimos las columnas al contenedor principal de Caché
        cacheRow.add(columnsPanel, BorderLayout.CENTER);
        add(cacheRow);

        switchQueryType();
    }

    private void switchQueryType() {
        boolean isMongo = "Mongo Query".equals(queryTypeCombo.getSelectedItem());
        mongoContainer.setVisible(isMongo);
        sqlContainer.setVisible(!isMongo);
        revalidate();
        repaint();
    }

    public void loadData(WorkStatementData data) {
        this.isPopulating = true;
        try {
            queryCodeField.setText(data.queryCode != null ? data.queryCode : "");

            // Inferencia de tipo y fuente
            String dbSource = data.dbSource != null ? data.dbSource : "MongoDb";
            dbSourceCombo.setSelectedItem(dbSource);

            if (data.sqlQuery != null && !data.sqlQuery.isEmpty()) {
                queryTypeCombo.setSelectedItem("SQL Query");
            } else {
                queryTypeCombo.setSelectedItem("Mongo Query");
            }

            // Datos Mongo
            collectionField.setText(data.collectionName != null ? data.collectionName : "");
            List<PipelineWrapper> pipelines = new ArrayList<>();
            if (data.mongoAggregatePipelines != null) {
                pipelines = data.mongoAggregatePipelines.stream().map(PipelineWrapper::new).collect(Collectors.toList());
            }
            aggregatePanel.reloadData(pipelines);

            // Datos SQL
            sqlQueryArea.setText(data.sqlQuery != null ? data.sqlQuery : "");

            // Outputs & Cache
            outputParamsPanel.reloadData(data.enricherOutputFields != null ? data.enricherOutputFields : new ArrayList<>());

            if (data.cacheInfo != null) {
                cacheNameField.setText(data.cacheInfo.name != null ? data.cacheInfo.name : "");
                cacheTtlField.setText(data.cacheInfo.ttl != null ? String.valueOf(data.cacheInfo.ttl) : "");
                cacheSizeField.setText(data.cacheInfo.size != null ? String.valueOf(data.cacheInfo.size) : "");
            } else {
                cacheNameField.setText(""); cacheTtlField.setText(""); cacheSizeField.setText("");
            }

            switchQueryType();
        } finally {
            this.isPopulating = false;
        }
    }

    public void saveData(WorkStatementData data) {
        data.queryCode = getNullIfEmpty(queryCodeField.getText());
        data.dbSource = (String) dbSourceCombo.getSelectedItem();

        boolean isMongo = "Mongo Query".equals(queryTypeCombo.getSelectedItem());

        if (isMongo) {
            data.collectionName = getNullIfEmpty(collectionField.getText());
            data.mongoAggregatePipelines = aggregatePanel.getDataList().stream() // <-- Usar aggregatePanel
                    .map(w -> w.pipeline).collect(Collectors.toList());
            data.sqlQuery = null;
        } else {
            data.sqlQuery = getNullIfEmpty(sqlQueryArea.getText());
            data.collectionName = null;
            data.mongoAggregatePipelines = new ArrayList<>();
        }

        data.enricherOutputFields = new ArrayList<>(outputParamsPanel.getDataList());

        String cName = getNullIfEmpty(cacheNameField.getText());
        String cTtl = getNullIfEmpty(cacheTtlField.getText());
        String cSize = getNullIfEmpty(cacheSizeField.getText());

        if (cName != null || cTtl != null || cSize != null) {
            data.cacheInfo = new CacheInfo();
            data.cacheInfo.name = cName;
            try { data.cacheInfo.ttl = cTtl != null ? Integer.parseInt(cTtl) : null; } catch (NumberFormatException ignored) {}
            try { data.cacheInfo.size = cSize != null ? Integer.parseInt(cSize) : null; } catch (NumberFormatException ignored) {}
        } else {
            data.cacheInfo = null;
        }
    }

    private AtiTextField createTextField() {
        AtiTextField field = WorkflowThemeUtils.createThemedTextField();
        field.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        return field;
    }

    private AtiResizableTextArea createResizableTextArea() {
        AtiResizableTextArea area = WorkflowThemeUtils.createThemedResizableTextArea();
        area.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        return area;
    }

    private String getNullIfEmpty(String text) {
        return (text == null || text.trim().isEmpty()) ? null : text;
    }

    private void notifyChange() {
        if (!isPopulating && onChange != null) onChange.run();
    }

    public static class PipelineWrapper {
        public String pipeline;
        public PipelineWrapper() {}
        public PipelineWrapper(String pipeline) { this.pipeline = pipeline; }
    }
}



//package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;
//
//import com.bbva.gkxj.atiframework.components.AtiComboBox;
//import com.bbva.gkxj.atiframework.components.AtiLabeledComponent;
//import com.bbva.gkxj.atiframework.components.AtiTextField;
//import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WorkStatementData;
//import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
//import com.intellij.ui.DocumentAdapter;
//import org.jetbrains.annotations.NotNull;
//
//import javax.swing.*;
//import javax.swing.event.DocumentEvent;
//import java.awt.*;
//
//public class WsQueryPanel extends JPanel {
//
//    private final AtiTextField queryCodeField;
//    private final AtiComboBox queryTypeCombo;
//    private final AtiComboBox dbSourceCombo;
//    private final AtiTextField collectionField;
//
//    public WsQueryPanel(Runnable onChange) {
//        setLayout(new GridBagLayout());
//        setOpaque(false);
//
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.anchor = GridBagConstraints.NORTHWEST;
//        gbc.weighty = 0;
//
//        queryCodeField = createTextField(onChange);
//        queryTypeCombo = new AtiComboBox(new String[]{"Mongo Query", "SQL Query"});
//        dbSourceCombo = new AtiComboBox(new String[]{"MongoDb", "SQLServer"});
//
//        WorkflowThemeUtils.applyWorkflowTheme(queryTypeCombo);
//        WorkflowThemeUtils.applyWorkflowTheme(dbSourceCombo);
//        queryTypeCombo.addActionListener(e -> onChange.run());
//        dbSourceCombo.addActionListener(e -> onChange.run());
//
//        // --- FILA 0 ---
//        gbc.gridy = 0; gbc.weightx = 0.33;
//        gbc.gridx = 0; gbc.insets = new Insets(5, 0, 10, 15);
//        add(new AtiLabeledComponent("Query Code", queryCodeField), gbc);
//        gbc.gridx = 1;
//        add(new AtiLabeledComponent("Query Type", queryTypeCombo), gbc);
//        gbc.gridx = 2; gbc.insets = new Insets(5, 0, 10, 0);
//        add(new AtiLabeledComponent("Db Source", dbSourceCombo), gbc);
//
//        // --- FILA 1 ---
//        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 3; gbc.weightx = 1.0;
//        gbc.insets = new Insets(5, 0, 10, 0);
//        collectionField = createTextField(onChange);
//        JPanel mongoSpecificPanel = new JPanel(new BorderLayout());
//        mongoSpecificPanel.setOpaque(false);
//        mongoSpecificPanel.add(new AtiLabeledComponent("Collection", collectionField), BorderLayout.CENTER);
//        add(mongoSpecificPanel, gbc);
//
//        // --- FILA 2 (GLUE) ---
//        gbc.gridy = 2; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
//        add(Box.createGlue(), gbc);
//    }
//
//    public void loadData(WorkStatementData data) {
//        queryCodeField.setText(data.queryCode != null ? data.queryCode : "");
//        // Asumiendo que determinamos el queryType base al dbSource si no viene
//        String qType = "MongoDb".equals(data.dbSource) ? "Mongo Query" : "SQL Query";
//        queryTypeCombo.setSelectedItem(qType);
//        dbSourceCombo.setSelectedItem(data.dbSource != null ? data.dbSource : "MongoDb");
//        collectionField.setText(data.collectionName != null ? data.collectionName : "");
//    }
//
//    public void saveData(WorkStatementData data) {
//        data.queryCode = getNullIfEmpty(queryCodeField.getText());
//        data.dbSource = (String) dbSourceCombo.getSelectedItem();
//        data.collectionName = getNullIfEmpty(collectionField.getText());
//    }
//
//    private AtiTextField createTextField(Runnable onChange) {
//        AtiTextField field = WorkflowThemeUtils.createThemedTextField();
//        field.getDocument().addDocumentListener(new DocumentAdapter() {
//            @Override protected void textChanged(@NotNull DocumentEvent e) { onChange.run(); }
//        });
//        return field;
//    }
//
//    private String getNullIfEmpty(String text) {
//        return (text == null || text.trim().isEmpty()) ? null : text;
//    }
//}