package com.bbva.gkxj.atiframework.filetype.step.editor.panels;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.ExceptionData;
import com.bbva.gkxj.atiframework.filetype.step.editor.utils.IssueTreatmentData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class StepIssueTreatmentPanel extends JPanel {

    private AtiTableSplitterPanel<IssueTreatmentData> issuePanel;
    private AtiTableSplitterPanel<ExceptionData> exceptionPanel;

    private AtiTextField exceptionClassTextField = new AtiTextField();
    private AtiTextField exceptionMethodTextField = new AtiTextField();
    private AtiJSpinner skipLimitField = new AtiJSpinner(new SpinnerNumberModel(0,Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private CustomToggleSwitch skippableSwitch = new CustomToggleSwitch();
    private AtiTextField exceptionCodeMethodTextField = new AtiTextField();
    private AtiTextField issueCodeTextField = new AtiTextField();
    private AtiTextField technicalCodeTextField = new AtiTextField();

    private AtiTextField errorCodeExTextField = new AtiTextField();
    private AtiTextField issueCodeExTextField = new AtiTextField();
    private AtiTextField technicalCodeExTextField = new AtiTextField();
    private AtiJSpinner skipLimitExField = new AtiJSpinner(new SpinnerNumberModel(0,Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    private CustomToggleSwitch skippableExSwitch = new CustomToggleSwitch();

    private boolean isUpdatingUI = false;
    private boolean syncListenersAttached = false;

    private DocumentListener parentTextListener;
    private ChangeListener parentChangeListener;
    private ActionListener parentActionListener;

    public StepIssueTreatmentPanel() {
        createUIComponents();
    }

    private void createUIComponents() {
        setLayout(new BorderLayout());
        setBackground(SchedulerTheme.BG_CARD);

        JPanel exceptionForm = createExceptionForm();
        exceptionPanel = new AtiTableSplitterPanel<>(
                "Exceptions",
                "Error Code",
                () -> {
                    ExceptionData ex = new ExceptionData(String.format("%02d", exceptionPanel.getDataList().size() + 1), "");
                    ex.setSkippable(false);
                    return ex;
                },
                ExceptionData::getId,
                ExceptionData::getErrorCode,
                exceptionForm
        );

        exceptionPanel.setPreferredSize(new Dimension(0, 300));
        exceptionPanel.setMinimumSize(new Dimension(0, 250));

        exceptionPanel.setSelectionListener(this::loadExceptionIntoForm);
        exceptionPanel.setDeselectionListener(this::clearExceptionForm);

        JPanel issueForm = createIssueFormPanel();

        issuePanel = new AtiTableSplitterPanel<IssueTreatmentData>(
                "Nº of elements",
                "Issue Treatment",
                () -> {
                    IssueTreatmentData issue = new IssueTreatmentData(String.format("%02d", issuePanel.getDataList().size() + 1));
                    issue.setSkippable(false);
                    return issue;
                },
                IssueTreatmentData::getId,
                IssueTreatmentData::getExceptionMethod,
                issueForm
        );

        issuePanel.setSelectionListener(issue -> {
            loadIssueIntoForm(issue);
            exceptionPanel.reloadData(issue.getExceptions());
        });
        issuePanel.setDeselectionListener(() -> {
            clearIssueForm();
            exceptionPanel.reloadData(new ArrayList<>());
        });
        issuePanel.setChangeCallback(() -> notifyParentAction("ISSUE_CHANGED"));

        // Ahora que issuePanel está inicializado, agregar el callback de cambios a exceptionPanel
        exceptionPanel.setChangeCallback(() -> {
            IssueTreatmentData currentIssue = issuePanel.getCurrentSelection();
            if (currentIssue != null) {
                currentIssue.setExceptions(new ArrayList<>(exceptionPanel.getDataList()));
            }
            notifyParentAction("EXCEPTION_CHANGED");
        });

        add(issuePanel, BorderLayout.CENTER);
    }

    public void addFieldListeners(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        this.parentTextListener = textListener;
        this.parentChangeListener = changeListener;
        this.parentActionListener = actionListener;

        attachParentListenerToIssueTreatmentSpecifics(textListener, actionListener, changeListener);
        attachParentListenerToExceptionSpecifics(textListener, actionListener, changeListener);

        // Solo agregar los listeners de sincronización una sola vez
        if (!syncListenersAttached) {
            attachSyncListenersToIssueTreatmentFields();
            attachSyncListenersToExceptionFields();
            syncListenersAttached = true;
        }
    }

    private void attachParentListenerToIssueTreatmentSpecifics(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener) {
        if(exceptionClassTextField != null) exceptionClassTextField.getDocument().addDocumentListener(textListener);
        if(exceptionMethodTextField != null) exceptionMethodTextField.getDocument().addDocumentListener(textListener);
        if(skipLimitField != null) skipLimitField.addChangeListener(changeListener);
        if(skippableSwitch != null) skippableSwitch.addChangeListener(changeListener);
        if(exceptionCodeMethodTextField != null) exceptionCodeMethodTextField.getDocument().addDocumentListener(textListener);
        if(issueCodeTextField != null) issueCodeTextField.getDocument().addDocumentListener(textListener);
        if(technicalCodeTextField != null) technicalCodeTextField.getDocument().addDocumentListener(textListener);
    }

    private void attachParentListenerToExceptionSpecifics(DocumentListener textListener, ActionListener actionListener, ChangeListener changeListener){
        if(errorCodeExTextField != null) errorCodeExTextField.getDocument().addDocumentListener(textListener);
        if(issueCodeExTextField != null) issueCodeExTextField.getDocument().addDocumentListener(textListener);
        if(technicalCodeExTextField != null) technicalCodeExTextField.getDocument().addDocumentListener(textListener);
        if(skipLimitExField != null) skipLimitExField.addChangeListener(changeListener);
        if(skippableExSwitch != null) skippableExSwitch.addChangeListener(changeListener);
    }

    private void attachSyncListenersToIssueTreatmentFields() {
        if (exceptionClassTextField != null) {
            exceptionClassTextField.getDocument().addDocumentListener(createSyncDocumentListener(() -> {
                syncCurrentSelectionIssueFromUI();
            }));
        }
        if (exceptionMethodTextField != null) {
            exceptionMethodTextField.getDocument().addDocumentListener(createSyncDocumentListener(() -> {
                syncCurrentSelectionIssueFromUI();
                issuePanel.updateSelectedRowName(exceptionMethodTextField.getText());
            }));
        }
        if (skipLimitField != null) {
            skipLimitField.addChangeListener(e -> {
                if (!isUpdatingUI) {
                    syncCurrentSelectionIssueFromUI();
                }
            });
        }
        if (skippableSwitch != null) {
            skippableSwitch.addChangeListener(e -> {
                if (!isUpdatingUI) {
                    syncCurrentSelectionIssueFromUI();
                }
            });
        }
        if (exceptionCodeMethodTextField != null) {
            exceptionCodeMethodTextField.getDocument().addDocumentListener(createSyncDocumentListener(() -> {
                syncCurrentSelectionIssueFromUI();
            }));
        }
        if (issueCodeTextField != null) {
            issueCodeTextField.getDocument().addDocumentListener(createSyncDocumentListener(() -> {
                syncCurrentSelectionIssueFromUI();
            }));
        }
        if (technicalCodeTextField != null) {
            technicalCodeTextField.getDocument().addDocumentListener(createSyncDocumentListener(() -> {
                syncCurrentSelectionIssueFromUI();
            }));
        }
    }

    private void attachSyncListenersToExceptionFields() {
        if (errorCodeExTextField != null) {
            errorCodeExTextField.getDocument().addDocumentListener(createSyncDocumentListener(() -> {
                syncCurrentSelectionExceptionFromUI();
                exceptionPanel.updateSelectedRowName(errorCodeExTextField.getText());
            }));
        }
        if (issueCodeExTextField != null) {
            issueCodeExTextField.getDocument().addDocumentListener(createSyncDocumentListener(() -> {
                syncCurrentSelectionExceptionFromUI();
            }));
        }
        if (technicalCodeExTextField != null) {
            technicalCodeExTextField.getDocument().addDocumentListener(createSyncDocumentListener(() -> {
                syncCurrentSelectionExceptionFromUI();
            }));
        }
        if (skipLimitExField != null) {
            skipLimitExField.addChangeListener(e -> {
                if (!isUpdatingUI) {
                    syncCurrentSelectionExceptionFromUI();
                }
            });
        }
        if (skippableExSwitch != null) {
            skippableExSwitch.addChangeListener(e -> {
                if (!isUpdatingUI) {
                    syncCurrentSelectionExceptionFromUI();
                }
            });
        }
    }

    private javax.swing.event.DocumentListener createSyncDocumentListener(Runnable syncAction) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            void update() { if (!isUpdatingUI) syncAction.run(); }
        };
    }

    public void updateDocument(JsonObject jsonObject) {
        if (jsonObject == null) return;

        if (!isUpdatingUI) {
            syncCurrentSelectionIssueFromUI();
            syncCurrentSelectionExceptionFromUI();
        }

        JsonArray issuesArray = new JsonArray();

        for (IssueTreatmentData d : issuePanel.getDataList()) {
            JsonObject o = new JsonObject();
            o.addProperty("exceptionClass", d.getExceptionClass());
            o.addProperty("exceptionMethod", d.getExceptionMethod());
            o.addProperty("exceptionCodeMethod", d.getExceptionCodeMethod());
            o.addProperty("skippable", d.getSkippable());
            o.addProperty("skipLimit", d.getSkipLimit());
            o.addProperty("issueCode", d.getIssueCode());
            o.addProperty("technicalCode", d.getTechnicalCode());

            JsonArray exArray = new JsonArray();
            for (ExceptionData ed : d.getExceptions()) {
                JsonObject exObj = new JsonObject();
                exObj.addProperty("errorCode", ed.getErrorCode());
                exObj.addProperty("issueCode", ed.getIssueCode());
                exObj.addProperty("technicalCode", ed.getTechnicalCode());
                exObj.addProperty("skipLimit", ed.getSkipLimit());
                exObj.addProperty("skippable", ed.getSkippable());
                exArray.add(exObj);
            }
            o.add("exceptionCodes", exArray);
            issuesArray.add(o);
        }

        jsonObject.add("IssueTreatments", issuesArray);
    }

    public void updateForm(JsonObject jsonObject) {
        if (jsonObject == null) return;
        isUpdatingUI = true;

        String rootKey = "IssueTreatments";
        try {
            // Keep the panel visible even when the node does not exist, so the tab always renders.
            issuePanel.setVisible(true);

            if (!jsonObject.has(rootKey) || !jsonObject.get(rootKey).isJsonArray()) {
                issuePanel.reloadData(new ArrayList<>());
                exceptionPanel.reloadData(new ArrayList<>());
                clearIssueForm();
                clearExceptionForm();
                return;
            }

            JsonArray issuesArr = jsonObject.getAsJsonArray(rootKey);
            List<IssueTreatmentData> parsedIssues = new ArrayList<>();

            for (int i = 0; i < issuesArr.size(); i++) {
                parsedIssues.add(parseIssueTreatmentData(issuesArr.get(i).getAsJsonObject(), i));
            }

            issuePanel.reloadData(parsedIssues);

        } finally {
            isUpdatingUI = false;
            revalidate();
            repaint();
        }
    }

    private JPanel createIssueFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SchedulerTheme.BG_MAIN);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0; c.gridx = 0; c.gridwidth = 3;
        addStack(p, c, 0, 0, createLabel("Exception Class"), exceptionClassTextField);

        c.gridwidth = 1;
        addStack(p, c, 0, 2, createLabel("Exception Method"), exceptionMethodTextField);

        addStack(p, c, 1, 2, createLabel("Skip Limit"), skipLimitField);

        JPanel appendFileRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        appendFileRow.add(skippableSwitch);
        appendFileRow.add(createLabel("Skippable"));
        c.gridx = 2; c.gridy = 3;
        p.add(appendFileRow, c);

        addStack(p, c, 0, 4, createLabel("Exception Code Method"), exceptionCodeMethodTextField);

        addStack(p, c, 1, 4, createLabel("Issue Code"), issueCodeTextField);

        addStack(p, c, 2, 4, createLabel("Technical Code"), technicalCodeTextField);

        c.gridx = 0; c.gridy = 6; c.gridwidth = 3;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        p.add(exceptionPanel, c);

        return p;
    }

    private JPanel createExceptionForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SchedulerTheme.BG_MAIN);
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;

        addStack(form, c, 0, 0, createLabel("Error Code"), errorCodeExTextField);

        addStack(form, c, 1, 0, createLabel("Issue Code"), issueCodeExTextField);

        addStack(form, c, 0, 2, createLabel("Technical Code"), technicalCodeExTextField);

        addStack(form, c, 1, 2, createLabel("Skip Limit"), skipLimitExField);

        JPanel skRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        skRow.setOpaque(false);
        skRow.add(skippableExSwitch);
        skRow.add(createLabel("Skippable"));

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        form.add(skRow, c);

        JPanel filler = new JPanel(); filler.setOpaque(false);
        c.gridy = 9999; c.weighty = 1.0; c.fill = GridBagConstraints.BOTH;
        form.add(filler, c);

        return form;
    }


    private void loadIssueIntoForm(IssueTreatmentData data) {
        if (data == null) return;
        isUpdatingUI = true;
        try {
            exceptionClassTextField.setText(data.getExceptionClass() != null ? data.getExceptionClass() : "");
            exceptionMethodTextField.setText(data.getExceptionMethod() != null ? data.getExceptionMethod() : "");
            skipLimitField.setValue(data.getSkipLimit() != null ? data.getSkipLimit() : "");

            Boolean skippable = data.getSkippable();
            skippableSwitch.setSelected(skippable != null && skippable);

            exceptionCodeMethodTextField.setText(data.getExceptionCodeMethod() != null ? data.getExceptionCodeMethod() : "");
            issueCodeTextField.setText(data.getIssueCode() != null ? data.getIssueCode() : "");
            technicalCodeTextField.setText(data.getTechnicalCode() != null ? data.getTechnicalCode() : "");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isUpdatingUI = false;
        }
    }

    private void clearIssueForm() {
        isUpdatingUI = true;
        exceptionClassTextField.setText("");
        exceptionMethodTextField.setText("");
        skipLimitField.setValue(0);
        skippableSwitch.setSelected(false);
        exceptionCodeMethodTextField.setText("");
        issueCodeTextField.setText("");
        technicalCodeTextField.setText("");
        isUpdatingUI = false;
    }

    private void loadExceptionIntoForm(ExceptionData data) {
        if (data == null) return;
        isUpdatingUI = true;
        try {
            errorCodeExTextField.setText(data.getErrorCode() != null ? data.getErrorCode() : "");
            issueCodeExTextField.setText(data.getIssueCode() != null ? data.getIssueCode() : "");
            technicalCodeExTextField.setText(data.getTechnicalCode() != null ? data.getTechnicalCode() : "");
            skipLimitExField.setValue(data.getSkipLimit() != null ? data.getSkipLimit() : "");
            skippableExSwitch.setSelected(data.getSkippable() != null ? data.getSkippable() : false);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isUpdatingUI = false;
        }
    }

    private void clearExceptionForm() {
        isUpdatingUI = true;
        errorCodeExTextField.setText("");
        issueCodeExTextField.setText("");
        technicalCodeExTextField.setText("");
        skipLimitExField.setValue(0);
        skippableExSwitch.setSelected(false);
        isUpdatingUI = false;
    }

    private void syncCurrentSelectionIssueFromUI() {
        IssueTreatmentData current = issuePanel.getCurrentSelection();
        if (current == null) return;

        current.setExceptionClass(exceptionClassTextField.getText());
        current.setExceptionMethod(exceptionMethodTextField.getText());
        current.setSkipLimit((Integer) skipLimitField.getValue());
        current.setSkippable(skippableSwitch.isSelected());
        current.setExceptionCodeMethod(exceptionCodeMethodTextField.getText());
        current.setIssueCode(issueCodeTextField.getText());
        current.setTechnicalCode(technicalCodeTextField.getText());
    }

    private void syncCurrentSelectionExceptionFromUI() {
        ExceptionData current = exceptionPanel.getCurrentSelection();
        if (current == null) return;

        current.setErrorCode(errorCodeExTextField.getText());
        current.setIssueCode(issueCodeExTextField.getText());
        current.setTechnicalCode(technicalCodeExTextField.getText());
        current.setSkipLimit((Integer) skipLimitExField.getValue());
        current.setSkippable(skippableExSwitch.isSelected());
    }

    private IssueTreatmentData parseIssueTreatmentData(JsonObject jsonObject, int index) {
        IssueTreatmentData issueTreatmentData = new IssueTreatmentData(String.format("%02d", index + 1));
        if (jsonObject.has("exceptionClass")) issueTreatmentData.setExceptionClass(jsonObject.get("exceptionClass").getAsString());
        if (jsonObject.has("exceptionMethod")) issueTreatmentData.setExceptionMethod(jsonObject.get("exceptionMethod").getAsString());
        if (jsonObject.has("skipLimit")) issueTreatmentData.setSkipLimit(jsonObject.get("skipLimit").getAsInt());
        if (jsonObject.has("skippable")) issueTreatmentData.setSkippable(jsonObject.get("skippable").getAsBoolean());
        if (jsonObject.has("exceptionCodeMethod")) issueTreatmentData.setExceptionCodeMethod(jsonObject.get("exceptionCodeMethod").getAsString());
        if (jsonObject.has("issueCode")) issueTreatmentData.setIssueCode(jsonObject.get("issueCode").getAsString());
        if (jsonObject.has("technicalCode")) issueTreatmentData.setTechnicalCode(jsonObject.get("technicalCode").getAsString());

        if (jsonObject.has("exceptionCodes") && jsonObject.get("exceptionCodes").isJsonArray()) {
            JsonArray ex = jsonObject.getAsJsonArray("exceptionCodes");
            for (int i = 0; i < ex.size(); i++) {
                String id = String.format("%02d", i + 1);
                if (ex.get(i).isJsonPrimitive()) {
                    issueTreatmentData.getExceptions().add(new ExceptionData(id, ex.get(i).getAsString()));
                } else if (ex.get(i).isJsonObject()) {
                    JsonObject exObj = ex.get(i).getAsJsonObject();
                    ExceptionData ed = new ExceptionData(id, "");
                    if (exObj.has("exceptionCode")) ed.setErrorCode(exObj.get("exceptionCode").getAsString());
                    if (exObj.has("issueCode")) ed.setIssueCode(exObj.get("issueCode").getAsString());
                    if (exObj.has("technicalCode")) ed.setTechnicalCode(exObj.get("technicalCode").getAsString());
                    if (exObj.has("skipLimit")) ed.setSkipLimit(exObj.get("skipLimit").getAsInt());
                    if (exObj.has("skippable")) ed.setSkippable(exObj.get("skippable").getAsBoolean());
                    issueTreatmentData.getExceptions().add(ed);
                }
            }
        }
        return issueTreatmentData;
    }


    private void notifyParentAction(String command) {
        if (parentActionListener != null) {
            parentActionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command));
        }
    }


    private void addToGrid(JPanel panel, GridBagConstraints c, JComponent comp, int x, int y) {
        c.gridx = x; c.gridy = y; panel.add(comp, c);
    }

    private void addStack(JPanel panel, GridBagConstraints c, int x, int yStart, JComponent topLabel, JComponent bottomComp) {
        addToGrid(panel, c, topLabel, x, yStart);
        addToGrid(panel, c, bottomComp, x, yStart + 1);
    }

    private JLabel createLabel(String text) {
        return new AtiJLabel(text);
    }
}

