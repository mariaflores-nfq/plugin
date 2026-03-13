package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FilterMapData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.List;

public class FilterTabView extends JPanel {

    private AtiTableSplitterPanel<FilterMapData> splitterPanel;
    private AtiTextField filterCodeField;
    private AtiScriptPanel scriptField;

    private Runnable onFormChangedCallback;

    public FilterTabView() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(JBUI.Borders.empty(20));

        AtiJLabel titleLabel = new AtiJLabel("Filter List");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setBorder(JBUI.Borders.empty(10, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        initComponents();
    }

    private void initComponents() {
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setOpaque(false);

        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setOpaque(false);
        formContent.setBorder(JBUI.Borders.empty(15));

        // --- Filter Code ---
        JPanel topGrid = new JPanel(new BorderLayout());
        topGrid.setOpaque(false);
        filterCodeField = WorkflowThemeUtils.createThemedTextField();
        WorkflowThemeUtils.applyWorkflowTheme(filterCodeField);
        filterCodeField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });
        topGrid.add(new AtiLabeledComponent("Filter Code", filterCodeField), BorderLayout.CENTER);
        formContent.add(topGrid);

        formContent.add(Box.createVerticalStrut(20));

        // --- Script ---
        JPanel scriptHeader = new JPanel(new BorderLayout());
        scriptHeader.setOpaque(false);
        JLabel scriptLabel = new JLabel("Script");
        scriptLabel.setFont(scriptLabel.getFont().deriveFont(Font.BOLD));
        scriptHeader.add(scriptLabel, BorderLayout.WEST);
        formContent.add(scriptHeader);

        formContent.add(Box.createVerticalStrut(10));

        scriptField = new AtiScriptPanel();
        scriptField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
        });

        JPanel scriptWrapper = new JPanel(new BorderLayout());
        scriptWrapper.setOpaque(false);
        scriptWrapper.add(scriptField, BorderLayout.CENTER);

        formContent.add(scriptWrapper);
        detailPanel.add(formContent, BorderLayout.CENTER);

        // --- Splitter ---
        // Usamos el nuevo nombre FilterMapData
        splitterPanel = new AtiTableSplitterPanel<>(
                "Scripts", "Fields",
                FilterMapData::new,
                (AtiTableSplitterPanel.ItemIdExtractor<FilterMapData>) item -> String.format("%02d", (splitterPanel.getDataList().indexOf(item) + 1)),
                (AtiTableSplitterPanel.ItemNameExtractor<FilterMapData>) item -> item.filterCode != null && !item.filterCode.isEmpty() ? item.filterCode : "New Filter",
                detailPanel
        );

        add(splitterPanel, BorderLayout.CENTER);
    }

    private void notifyChange() {
        if (onFormChangedCallback != null) onFormChangedCallback.run();
    }

    // --- API PÚBLICA PARA EL CONTROLADOR ---

    public String getFilterCode() { return filterCodeField.getText().trim(); }
    public void setFilterCode(String code) { filterCodeField.setText(code != null ? code : ""); }

    public String getScript() { return scriptField.getText().trim(); }
    public void setScript(String script) { scriptField.setText(script != null ? script : ""); }

    public void setOnFormChanged(Runnable callback) { this.onFormChangedCallback = callback; }

    public List<FilterMapData> getTableData() { return splitterPanel.getDataList(); }
    public void setTableData(List<FilterMapData> data) { splitterPanel.reloadData(data); }

    public FilterMapData getSelectedTableItem() { return splitterPanel.getCurrentSelection(); }
    public void updateSelectedRowName(String name) { splitterPanel.updateSelectedRowName(name); }

    public void setOnTableChangeCallback(Runnable callback) { splitterPanel.setChangeCallback(callback); }
    public void setOnTableSelectionListener(AtiTableSplitterPanel.SelectionListener<FilterMapData> listener) {
        splitterPanel.setSelectionListener(listener);
    }
}