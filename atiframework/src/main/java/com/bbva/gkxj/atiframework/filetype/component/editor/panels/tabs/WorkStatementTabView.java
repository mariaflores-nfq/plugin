package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.AtiTableSplitterPanel;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.WorkStatementData;
import javax.swing.*;
import java.awt.*;

/**
 * Pestaña orquestadora para WorkStatements.
 * Contiene el SplitterPanel (Lista a la izquierda) y el DetailView (Formulario a la derecha).
 */
public class WorkStatementTabView extends JPanel {

    private AtiTableSplitterPanel<WorkStatementData> splitterPanel;
    private final WorkStatementDetailView detailView;

    public WorkStatementTabView() {
        setLayout(new BorderLayout());
        detailView = new WorkStatementDetailView(() -> {}); // El controlador asignará el callback real

        detailView.setMinimumSize(new Dimension(300, 0));
        splitterPanel = new AtiTableSplitterPanel<>(
                "WorkStatements",
                "Fields",
                WorkStatementData::new,
                (AtiTableSplitterPanel.ItemIdExtractor<WorkStatementData>) item ->  String.format("%02d", (splitterPanel.getDataList().indexOf(item) + 1)),
                (AtiTableSplitterPanel.ItemNameExtractor<WorkStatementData>) item -> item.wsCode != null && !item.wsCode.isEmpty() ? item.wsCode : "New WorkStatement",
                detailView
        );
        splitterPanel.setMinimumSize(new Dimension(250, 0));
        add(splitterPanel, BorderLayout.CENTER);
    }

    public AtiTableSplitterPanel<WorkStatementData> getSplitterPanel() {
        return splitterPanel;
    }

    public WorkStatementDetailView getDetailView() {
        return detailView;
    }
}