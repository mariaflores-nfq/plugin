package com.bbva.gkxj.atiframework.filetype.step.editor.panels.etlchunk.writer;

import com.bbva.gkxj.atiframework.components.AtiJLabel;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.*;
// Se asume que existe esta constante en tu clase StepConstants para los tipos de Writer
import static com.bbva.gkxj.atiframework.filetype.step.editor.utils.StepConstants.ETL_STEP_WRITER_TYPES;

/**
 * Panel de configuración para el componente Writer siguiendo el estilo de StepDetailsPanel.
 */
public class StepWriterPanel extends JPanel {

    private final Project myProject;
    private final Document myDocument;
    private final VirtualFile myFile;
    private JComboBox<String> writerTypeField;
    private boolean isUpdatingUI = false;

    private JPanel dynamicContentPanel;

    // Paneles específicos comentados para futura implementación
    // private StepWriterFixedPanel stepWriterFixedPanel;
    // private StepWriterXmlFile stepWriterXmlFile;

    // El panel de queries del Writer que definimos basándonos en tu imagen
    private StepWriterMongoPanel stepWriterMongoPanel;
    private StepWriterSqlPanel stepWriterSqlPanel;
    private StepWriterApiRequestPanel stepWriterApiRequestPanel;

    private DocumentListener savedTextListener;
    private ActionListener savedActionListener;
    private ChangeListener savedChangeListener;

    public StepWriterPanel(@NotNull Project project, @NotNull VirtualFile file) {
        this.myProject = project;
        this.myFile = file;
        this.myDocument = FileDocumentManager.getInstance().getDocument(file);
        createUIComponents();
        setupInternalListeners();
    }

    private void createUIComponents() {
        setLayout(new BorderLayout());
        setBackground(SchedulerTheme.BG_CARD);

        dynamicContentPanel = new JPanel(new BorderLayout());
        dynamicContentPanel.setBackground(SchedulerTheme.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SchedulerTheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.25;

        c.gridx = 0;
        c.gridy = 0;
        form.add(new AtiJLabel("Writer Type"), c);

        c.gridy = 1;
        // Usamos la constante de tipos de Writer
        writerTypeField = new JComboBox<>(ETL_STEP_WRITER_TYPES);
        writerTypeField.setBackground(SchedulerTheme.BG_CARD);

        applyBlueFocusBorder(writerTypeField);
        form.add(writerTypeField, c);

        c.gridy = 2;
        c.weighty = 1.0;
        form.add(new JPanel() {{ setOpaque(false); }}, c);

        add(form, BorderLayout.NORTH);
        add(dynamicContentPanel, BorderLayout.CENTER);
    }

    private void setupInternalListeners() {
        writerTypeField.addActionListener(e -> {
            if (isUpdatingUI) return;
            String selectedType = (String) writerTypeField.getSelectedItem();
            updateDynamicPanelVisibility(selectedType);
        });
    }

    private void updateDynamicPanelVisibility(String type) {

        if (dynamicContentPanel == null) return;

        dynamicContentPanel.removeAll();

        switch (type) {

            case "Api Request":
                if (stepWriterApiRequestPanel == null) {
                    stepWriterApiRequestPanel = new StepWriterApiRequestPanel();
                    if (savedActionListener != null) {
                        stepWriterApiRequestPanel.addFieldListeners(savedTextListener, savedActionListener, savedChangeListener);
                    }
                }
                dynamicContentPanel.add(stepWriterApiRequestPanel, BorderLayout.CENTER);
                break;

            case "CSV File":
                break;

            case "Fixed File":
//                if (stepWriterFixedPanel == null) {
//                    stepWriterFixedPanel = new StepWriterFixedPanel(myProject, myFile);
//                    if (savedActionListener != null) {
//                        stepWriterFixedPanel.addFieldListeners(savedTextListener, savedActionListener, savedChangeListener);
//                    }
//                }
//                dynamicContentPanel.add(stepWriterFixedPanel, BorderLayout.CENTER);
//                break;

            case "XML File":
//                if (stepWriterXmlFile == null) {
//                    stepWriterXmlFile = new StepWriterXmlFile(myProject, myFile);
//                    if (savedActionListener != null) {
//                        stepWriterXmlFile.addFieldListeners(savedTextListener, savedActionListener, savedChangeListener);
//                    }
//                }
//                dynamicContentPanel.add(stepWriterXmlFile, BorderLayout.CENTER);
//                break;

                // En tu imagen el tipo seleccionado es "Mongo Query", puedes ajustar el string según tu constante
            case "Mongo Query":
                if (stepWriterMongoPanel == null) {
                    stepWriterMongoPanel = new StepWriterMongoPanel(myProject, myFile);
                    if (savedActionListener != null) {
                        stepWriterMongoPanel.addFieldListeners(savedTextListener, savedActionListener, savedChangeListener);
                    }
                }
                dynamicContentPanel.add(stepWriterMongoPanel, BorderLayout.CENTER);
                break;
            case "SQL Query":
                if (stepWriterSqlPanel == null) {
                    stepWriterSqlPanel = new StepWriterSqlPanel(myProject, myFile);
                    if (savedActionListener != null) {
                        stepWriterSqlPanel.addFieldListeners(savedTextListener, savedActionListener, savedChangeListener);
                    }
                }
                dynamicContentPanel.add(stepWriterSqlPanel, BorderLayout.CENTER);
                break;

            default:
                break;
        }

        dynamicContentPanel.revalidate();
        dynamicContentPanel.repaint();
    }

    public void updateForm(JsonObject jsonObject) {
        // Buscamos el nodo "writer" en lugar de "reader"
        if (jsonObject == null || !jsonObject.has("writer")) return;

        isUpdatingUI = true;
        try {
            JsonObject writerObj = jsonObject.getAsJsonObject("writer");

            // Detectar el tipo basándose en qué lista existe
            String type = null;
            if (writerObj.has("type") && !writerObj.get("type").isJsonNull()) {
                // Si existe el campo type (compatibilidad hacia atrás), usarlo
                type = writerObj.get("type").getAsString();
            } else if (writerObj.has("sqlQueryList") && !writerObj.has("mongoQueryList")) {
                // Si solo existe sqlQueryList, es SQL Query
                type = "SQL Query";
            } else if (writerObj.has("mongoQueryList") && !writerObj.has("sqlQueryList")) {
                // Si solo existe mongoQueryList, es Mongo Query
                type = "Mongo Query";
            } else if (writerObj.has("sqlQueryList") && writerObj.has("mongoQueryList")) {
                // Si existen ambas, priorizar según cuál tiene elementos
                JsonArray sqlList = writerObj.getAsJsonArray("sqlQueryList");
                JsonArray mongoList = writerObj.getAsJsonArray("mongoQueryList");
                if (sqlList.size() > 0 && mongoList.size() == 0) {
                    type = "SQL Query";
                } else if (mongoList.size() > 0 && sqlList.size() == 0) {
                    type = "Mongo Query";
                } else {
                    // Por defecto SQL Query
                    type = "SQL Query";
                }
            }

            if (type != null) {
                writerTypeField.setSelectedItem(type);
                updateDynamicPanelVisibility(type);
            }

            String selectedType = String.valueOf(writerTypeField.getSelectedItem());
            switch (selectedType) {

                case "Api Request":
                    if (stepWriterApiRequestPanel != null) {
                        stepWriterApiRequestPanel.updateForm(jsonObject);
                    }
                    break;

                case "CSV File":
                    break;

//                case "Fixed File":
//                    if (stepWriterFixedPanel != null) {
//                        stepWriterFixedPanel.updateForm(jsonObject);
//                    }
//                    break;
//
//                case "XML File":
//                    if (stepWriterXmlFile != null) {
//                        stepWriterXmlFile.updateForm(jsonObject);
//                    }
//                    break;

                case "Mongo Query":
                    if (stepWriterMongoPanel != null) {
                        stepWriterMongoPanel.updateForm(jsonObject);
                    }
                    break;
                case "SQL Query":
                    if (stepWriterSqlPanel != null) {
                        stepWriterSqlPanel.updateForm(jsonObject);
                    }
                    break;

                default:
                    break;
            }
        } finally {
            isUpdatingUI = false;
        }
    }

    public void updateDocument(JsonObject jsonObject) {
        if (isUpdatingUI || jsonObject == null) return;

        JsonObject writerObj;
        // Operamos sobre el nodo "writer"
        if (jsonObject.has("writer") && jsonObject.get("writer").isJsonObject()) {
            writerObj = jsonObject.getAsJsonObject("writer");
        } else {
            writerObj = new JsonObject();
            jsonObject.add("writer", writerObj);
        }

        // Eliminar la propiedad type si existe
        writerObj.remove("type");

        String selectedType = String.valueOf(writerTypeField.getSelectedItem());
        switch (selectedType) {

            case "Api Request":
                if (stepWriterApiRequestPanel != null) {
                    stepWriterApiRequestPanel.updateDocument(jsonObject);
                }
                break;

            case "CSV File":
                break;

            case "Fixed File":
//                if (stepWriterFixedPanel != null) {
//                    stepWriterFixedPanel.updateDocument(jsonObject);
//                }
                break;

            case "XML File":
//                if (stepWriterXmlFile != null) {
//                    stepWriterXmlFile.updateDocument(jsonObject);
//                }
                break;

            case "Mongo Query":
                if (stepWriterMongoPanel != null) {
                    // Actualizamos pasando el jsonObject completo para que el panel gestione el nodo "writer"
                    stepWriterMongoPanel.updateDocument(jsonObject);
                }
                break;
            case "SQL Query":
                if (stepWriterSqlPanel != null) {
                    // Actualizamos pasando el jsonObject completo para que el panel gestione el nodo "writer"
                    stepWriterSqlPanel.updateDocument(jsonObject);
                }
                break;

            default:
                break;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            this.myDocument.setText(gson.toJson(jsonObject));
        });
    }

    /**
     * Registra listeners proporcionados por el editor padre para reaccionar a cambios
     * en los campos del formulario y propagar la actualización del JSON.
     *
     * @param textListener   listener para cambios en campos de texto.
     * @param actionListener listener para acciones (combos, botones, etc.).
     * @param changeListener listener para cambios en componentes.
     */
    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        this.savedActionListener = actionListener;
        this.savedChangeListener = changeListener;
        this.savedTextListener = textListener;
        writerTypeField.addActionListener(actionListener);

        if (stepWriterApiRequestPanel != null) {
            stepWriterApiRequestPanel.addFieldListeners(textListener, actionListener, changeListener);
        }
//        if (stepWriterFixedPanel != null) {
//            stepWriterFixedPanel.addFieldListeners(textListener, actionListener, changeListener);
//        }
        if (stepWriterMongoPanel != null) {
            stepWriterMongoPanel.addFieldListeners(textListener, actionListener, changeListener);
        }
        if (stepWriterSqlPanel != null) {
            stepWriterSqlPanel.addFieldListeners(textListener, actionListener, changeListener);
        }
    }

}