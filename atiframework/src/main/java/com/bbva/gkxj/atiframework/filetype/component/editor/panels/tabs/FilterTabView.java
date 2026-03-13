package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FilterData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

/**
 * Pestaña principal unificada para la configuración del nodo Filter.
 * <p>
 * Esta vista agrupa toda la interfaz necesaria para definir las reglas de filtrado de mensajes.
 * Contiene tanto la lista de scripts (gestionada mediante un {@link AtiTableSplitterPanel} en la parte izquierda)
 * como el formulario de detalle (en la parte derecha) donde se editan el código del filtro y su script asociado.
 * </p>
 */
public class FilterTabView extends JPanel {

    /** Panel divisor que contiene la lista de filtros a la izquierda y el detalle a la derecha. */
    private AtiTableSplitterPanel<FilterData> splitterPanel;

    // --- Campos del Formulario de Detalle ---

    /** Campo de texto para introducir el código identificador único del filtro. */
    private AtiTextField filterCodeField;

    /** Panel de editor avanzado para escribir la lógica en JavaScript del filtro. */
    private AtiScriptPanel scriptField;

    /** Callback que se ejecuta cuando el usuario realiza algún cambio en el formulario. */
    private Runnable onChange;

    /** Bandera para evitar disparar eventos de cambio mientras se cargan datos programáticamente. */
    private boolean isPopulating = false;

    /**
     * Construye la vista de la pestaña Filter.
     * Configura el diseño base, los márgenes, el título principal y llama a la inicialización de componentes.
     */
    public FilterTabView() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(JBUI.Borders.empty(20));

        // Título de la vista
        AtiJLabel titleLabel = new AtiJLabel("Filter List");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setBorder(JBUI.Borders.empty(10, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        initComponents();
    }

    /**
     * Inicializa y ensambla todos los componentes visuales de la interfaz.
     * Construye el panel de detalle (derecha) y lo inyecta dentro del componente {@code AtiTableSplitterPanel}.
     */
    private void initComponents() {
        // 1. Construimos el panel de detalle (Parte derecha)
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

        // 2. Construimos el Splitter inyectándole nuestro panel de detalle recién creado
        splitterPanel = new AtiTableSplitterPanel<>(
                "Scripts",
                "Fields",
                FilterData::new,
                item -> String.format("%02d", (splitterPanel.getDataList().indexOf(item) + 1)),
                item -> item.filterCode != null && !item.filterCode.isEmpty() ? item.filterCode : "New Filter",
                detailPanel
        );

        add(splitterPanel, BorderLayout.CENTER);
    }

    /**
     * Establece la acción que se ejecutará cada vez que se detecte un cambio en los campos del formulario.
     *
     * @param onChange Implementación del {@link Runnable} a ejecutar.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Dispara el callback de notificación de cambios, siempre y cuando
     * no se estén cargando datos desde el modelo en ese instante.
     */
    private void notifyChange() {
        if (!isPopulating && onChange != null) onChange.run();
    }

    /**
     * Carga un elemento seleccionado de la tabla (modelo) en los campos visuales del formulario derecho.
     * Bloquea temporalmente los eventos de cambio durante la carga.
     *
     * @param data El objeto {@link FilterData} que contiene los datos a mostrar.
     */
    public void loadData(FilterData data) {
        this.isPopulating = true;
        try {
            filterCodeField.setText(data.filterCode != null ? data.filterCode : "");
            scriptField.setText(data.script != null ? data.script : "");
        } finally {
            this.isPopulating = false;
        }
    }

    /**
     * Extrae los valores actuales del formulario visual y los guarda en el elemento en memoria (modelo).
     *
     * @param data El objeto {@link FilterData} donde se guardarán los cambios.
     */
    public void saveData(FilterData data) {
        data.filterCode = filterCodeField.getText().trim().isEmpty() ? null : filterCodeField.getText().trim();
        data.script = scriptField.getText().trim().isEmpty() ? null : scriptField.getText();
    }

    /**
     * Devuelve el componente contenedor del divisor y la tabla.
     * Útil para que el controlador pueda suscribirse a los eventos de la tabla (selección, adición, borrado).
     *
     * @return La instancia de {@link AtiTableSplitterPanel} configurada para los filtros.
     */
    public AtiTableSplitterPanel<FilterData> getSplitterPanel() {
        return splitterPanel;
    }
}