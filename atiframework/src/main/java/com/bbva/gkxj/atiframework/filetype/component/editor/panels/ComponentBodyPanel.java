package com.bbva.gkxj.atiframework.filetype.component.editor.panels;

import com.bbva.gkxj.atiframework.filetype.component.controller.ComponentDetailsController;
import com.bbva.gkxj.atiframework.filetype.component.controller.AdapterPropertiesController;
import com.bbva.gkxj.atiframework.filetype.component.controller.FilterPropertiesController;
import com.bbva.gkxj.atiframework.filetype.component.controller.WorkStatementPropertiesController;
import com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs.AdapterTabView;
import com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs.FilterTabView;
import com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs.WorkStatementTabView;
import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static com.bbva.gkxj.atiframework.filetype.component.utils.ComponentConstants.*;

/**
 * Panel orquestador principal del editor visual de componentes (.comp).
 * <p>
 * Ensambla la vista global dividida en:
 * 1. Cabecera fija: Detalles generales del componente (ID, Tipo, Subtipo).
 * 2. Cuerpo dinámico: Pestañas de configuración que aparecen/desaparecen según la matriz de tipos.
 * </p>
 */
public class ComponentBodyPanel extends JPanel {

    private final ComponentDetailsController detailsController;
    private final Runnable parentOnFormChanged;
    private JBTabbedPane tabbedPane;
    private JPanel behaviourWrapperPanel;
    private JsonObject lastLoadedJson;

    /**
     * Constructor del panel principal.
     *
     * @param project       Proyecto activo.
     * @param virtualFile   Archivo .comp en edición.
     * @param onFormChanged Callback para notificar cambios de estado al IDE.
     */
    public ComponentBodyPanel(Project project, VirtualFile virtualFile, Runnable onFormChanged) {
        this.parentOnFormChanged = onFormChanged;

        final ComponentDetailsController[] controllerRef = new ComponentDetailsController[1];
        this.detailsController = new ComponentDetailsController(project, virtualFile, () -> {
            if (controllerRef[0] != null && !controllerRef[0].isPopulating()) {
                refreshTabs();
                if (this.parentOnFormChanged != null) this.parentOnFormChanged.run();
            }
        });
        controllerRef[0] = this.detailsController;

        createUIComponents();
    }

    private void createUIComponents() {
        setLayout(new BorderLayout());
        setBackground(SchedulerTheme.BBVA_NAVY);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));

        // 1. Cabecera de Detalles
        JPanel detailsView = detailsController.getView();
        detailsView.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(detailsView);

        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Contenedor de Pestañas
        behaviourWrapperPanel = createBehaviourPanel();
        behaviourWrapperPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(behaviourWrapperPanel);

        JBScrollPane scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(SchedulerTheme.BBVA_NAVY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createBehaviourPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 1));

        tabbedPane = new JBTabbedPane();
        tabbedPane.setBackground(Color.WHITE);

        JPanel tabContainer = new JPanel(new BorderLayout());
        tabContainer.setBackground(Color.WHITE);
        tabContainer.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        tabContainer.add(tabbedPane, BorderLayout.CENTER);

        wrapper.add(tabContainer, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Regenera las pestañas basándose en el Type y Subtype seleccionados en la cabecera.
     * Limpia controladores antiguos para evitar fugas de memoria o guardados erróneos.
     */
    public void refreshTabs() {
        SwingUtilities.invokeLater(() -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String selectedTitle = selectedIndex >= 0 ? tabbedPane.getTitleAt(selectedIndex) : null;

            tabbedPane.removeAll();

            // Reset de controladores delegados
            detailsController.setAdapterController(null);
            detailsController.setFilterController(null);
            detailsController.setWorkStatementController(null);

            String type = detailsController.getCurrentType();
            String subtype = detailsController.getCurrentSubtype();

            if (type != null && !type.isEmpty()) {

                // Adaptadores (JMS / Async API)
                if ((TYPE_INPUT_ADAPTER.equals(type) || TYPE_OUTPUT_ADAPTER.equals(type))) {
                    if (SUBTYPE_JMS.equals(subtype)) {
                        addAdapterTab(type, subtype, "JMS", "jmsConfig");
                    } else if (SUBTYPE_ASYNC_API.equals(subtype)) {
                        addAdapterTab(type, subtype, "Async API", "asyncApiConfig");
                    }
                }

                // Database
                if (TYPE_OUTPUT_ADAPTER.equals(type) && SUBTYPE_DATABASE.equals(subtype)) {
                    tabbedPane.addTab("Database", createPlaceholderPanel("SQL/MongoDB Queries"));
                }

                // Filter
                if (TYPE_FILTER.equals(type)) {
                    addFilterTab();
                }

                // Enricher (WorkStatement)
                if (TYPE_ENRICHER.equals(type) && SUBTYPE_WORKSTATEMENT.equals(subtype)) {
                    addWorkStatementTab();
                }

                // Otros (Placeholders)
                if (TYPE_SPLITTER.equals(type)) {
                    tabbedPane.addTab("Splitter", createPlaceholderPanel("Split Logic (Java/JS/Root)"));
                }
                if (TYPE_AGGREGATOR.equals(type)) {
                    tabbedPane.addTab("Aggregator", createPlaceholderPanel("Aggregator Global Config"));
                }
            }

            behaviourWrapperPanel.setVisible(tabbedPane.getTabCount() > 0);
            restoreSelection(selectedTitle);
            behaviourWrapperPanel.revalidate();
            behaviourWrapperPanel.repaint();
        });
    }

    private void addAdapterTab(String type, String subtype, String tabTitle, String jsonKey) {
        AdapterTabView adapterView = new AdapterTabView(type, subtype);
        AdapterPropertiesController adapterController = new AdapterPropertiesController(adapterView, parentOnFormChanged);
        detailsController.setAdapterController(adapterController);

        if (lastLoadedJson != null && lastLoadedJson.has(jsonKey)) {
            detailsController.loadAdapterDataFromJson(lastLoadedJson.getAsJsonObject(jsonKey));
        }
        tabbedPane.addTab(tabTitle, adapterView);
    }

    private void addFilterTab() {
        FilterTabView filterView = new FilterTabView();
        FilterPropertiesController filterController = new FilterPropertiesController(filterView, parentOnFormChanged);
        detailsController.setFilterController(filterController);

        if (lastLoadedJson != null && lastLoadedJson.has("filterConfig")) {
            detailsController.loadFilterDataFromJson(lastLoadedJson.getAsJsonObject("filterConfig"));
        }
        tabbedPane.addTab("Filter", filterView);
    }

    /**
     * Añade la pestaña de WorkStatement (Enricher).
     * Utiliza la vista de pestaña (TabView) que contiene el Splitter maestro.
     */
    private void addWorkStatementTab() {
        WorkStatementTabView wsView = new WorkStatementTabView();
        WorkStatementPropertiesController wsController = new WorkStatementPropertiesController(wsView, parentOnFormChanged);
        detailsController.setWorkStatementController(wsController);

        if (lastLoadedJson != null && lastLoadedJson.has("workStatementConfig")) {
            detailsController.loadWorkStatementDataFromJson(lastLoadedJson.getAsJsonObject("workStatementConfig"));
        }
        tabbedPane.addTab("WorkStatement", wsView);
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(0, 300));
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.ITALIC, 14));
        label.setForeground(Color.GRAY);
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private void restoreSelection(String selectedTitle) {
        if (selectedTitle != null) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getTitleAt(i).equals(selectedTitle)) {
                    tabbedPane.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public void updateForm(JsonObject jsonObject) {
        this.lastLoadedJson = jsonObject;
        this.detailsController.updateForm(jsonObject);
        refreshTabs();
    }

    public void updateDocument(JsonObject jsonObject) {
        this.detailsController.updateDocument(jsonObject);
    }

    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.detailsController.getPreferredFocusedComponent();
    }
}