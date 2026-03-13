package com.bbva.gkxj.atiframework.filetype.component.controller;

import com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs.AdapterTabView;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.AdapterConfig;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.AdapterFieldData;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.DocumentEvent;
import java.util.ArrayList;

/**
 * Controlador encargado de gestionar la lógica de la pestaña de configuración de Adaptadores (JMS y Async API).
 * <p>
 * Conecta la vista ({@link AdapterTabView}) con el modelo de datos ({@link AdapterConfig}),
 * gestionando eventos de la interfaz de usuario de forma condicional (protegido contra nulos) y manteniendo
 * buffers independientes para las configuraciones de campos según el tipo de mensaje (XML, JSON, CSV).
 * </p>
 */
public class AdapterPropertiesController {

    /** Instancia de la vista del adaptador que este controlador gestiona. */
    private final AdapterTabView view;

    /** Acción a ejecutar (callback) cuando se detecta un cambio válido en el formulario, para marcar el archivo como modificado. */
    private final Runnable onFormChanged;

    /**
     * Bandera utilizada para evitar disparar eventos de cambio (y evitar bucles infinitos)
     * mientras se actualiza la interfaz de usuario programáticamente desde el modelo.
     */
    private boolean isPopulating = false;

    /** Objeto que almacena el estado actual de la configuración del adaptador. */
    private ComponentJsonData.AdapterConfig currentConfig = new ComponentJsonData.AdapterConfig();

    /** Rastrea el último formato de mensaje seleccionado para gestionar correctamente los cambios de buffer. */
    private String lastSelectedType = "";

    /**
     * Construye un nuevo controlador para las propiedades del Adaptador.
     *
     * @param view          La vista asociada (pestaña genérica de adaptador).
     * @param onFormChanged Callback que se ejecuta cuando hay cambios en el formulario
     * para notificar al editor principal que el archivo ha sido modificado.
     */
    public AdapterPropertiesController(AdapterTabView view, Runnable onFormChanged) {
        this.view = view;
        this.onFormChanged = onFormChanged;
        initListeners();
    }

    /**
     * Inicializa y registra todos los listeners necesarios para reaccionar
     * a las interacciones del usuario en la vista.
     * <p>
     * Dado que la vista es dinámica y algunos componentes pueden no existir (dependiendo de si es JMS o Async API),
     * se verifica la nulidad de cada componente antes de añadirle un listener.
     * </p>
     */
    private void initListeners() {
        // Listener de cambios en el Splitter (Tabla de campos)
        if (view.getSplitterPanel() != null) {
            view.getSplitterPanel().setChangeCallback(this::notifyChange);

            view.getSplitterPanel().setSelectionListener(item -> {
                this.isPopulating = true;
                try {
                    String type = view.getMessageTypeCombo() != null ? (String) view.getMessageTypeCombo().getSelectedItem() : "";
                    if (view.getDetailView() != null) {
                        view.getDetailView().updateExtractionLabel(type);
                        view.getDetailView().loadData(item);
                    }
                } finally {
                    this.isPopulating = false;
                }
            });
        }

        // Listener de cambios en el Formulario (Vista de Detalle de Campo)
        if (view.getDetailView() != null) {
            view.getDetailView().setOnChange(() -> {
                if (isPopulating) return;
                AdapterFieldData current = view.getSplitterPanel() != null ? view.getSplitterPanel().getCurrentSelection() : null;
                if (current != null) {
                    view.getDetailView().saveData(current);
                    view.getSplitterPanel().updateSelectedRowName(current.fieldName);
                    notifyChange();
                }
            });
        }

        // Listener para el cambio de TIPO DE MENSAJE (Gestión de buffers dinámicos)
        if (view.getMessageTypeCombo() != null) {
            view.getMessageTypeCombo().addActionListener(e -> {
                if (isPopulating) return;

                String newType = (String) view.getMessageTypeCombo().getSelectedItem();
                if (newType == null) newType = "";

                // 1. Guardar la lista actual en su buffer correspondiente antes de cambiar la vista
                saveCurrentListToBuffer(lastSelectedType);

                // 2. Actualizar la UI (Etiquetas y visibilidad de columnas o campos)
                view.updateFieldsContext(newType);

                // 3. Cargar la nueva lista en la tabla según el tipo seleccionado
                loadBufferToTable(newType);

                lastSelectedType = newType;
                notifyChange();
            });
        }

        // --- Listeners de Cabecera Condicionales ---

        // Campos de JMS
        if (view.getJmsConnectionCombo() != null) {
            view.getJmsConnectionCombo().addActionListener(e -> notifyChange());
        }

        if (view.getQueueNameField() != null) {
            view.getQueueNameField().getDocument().addDocumentListener(new DocumentAdapter() {
                @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
            });
        }

        // Campos de Async API
        if (view.getJavaClassNameField() != null) {
            view.getJavaClassNameField().getDocument().addDocumentListener(new DocumentAdapter() {
                @Override protected void textChanged(@NotNull DocumentEvent e) { notifyChange(); }
            });
        }

        // Switch de configuración crítica
        if (view.getIsCriticalSwitch() != null) {
            view.getIsCriticalSwitch().addActionListener(e -> notifyChange());
        }
    }

    /**
     * Extrae la lista actual de campos de la tabla visual y la guarda
     * en el buffer interno correspondiente (XML, JSON o CSV).
     *
     * @param type El tipo de mensaje cuyo buffer se desea sobreescribir con los datos actuales.
     */
    private void saveCurrentListToBuffer(String type) {
        if (type == null || view.getSplitterPanel() == null) return;
        switch (type) {
            case "XML" -> currentConfig.xmlFields = new ArrayList<>(view.getSplitterPanel().getDataList());
            case "JSON" -> currentConfig.jsonFields = new ArrayList<>(view.getSplitterPanel().getDataList());
            case "CSV" -> currentConfig.csvFields = new ArrayList<>(view.getSplitterPanel().getDataList());
        }
    }

    /**
     * Vuelca el contenido del buffer interno correspondiente en la tabla visual.
     *
     * @param type El tipo de mensaje (XML, JSON o CSV) cuyos campos deben mostrarse.
     */
    private void loadBufferToTable(String type) {
        if (view.getSplitterPanel() == null) return;
        this.isPopulating = true;
        try {
            switch (type) {
                case "XML" -> view.getSplitterPanel().reloadData(currentConfig.xmlFields);
                case "JSON" -> view.getSplitterPanel().reloadData(currentConfig.jsonFields);
                case "CSV" -> view.getSplitterPanel().reloadData(currentConfig.csvFields);
                default -> view.getSplitterPanel().reloadData(new ArrayList<>());
            }
        } finally {
            this.isPopulating = false;
        }
    }

    /**
     * Carga la configuración completa del componente desde el modelo hacia la interfaz gráfica.
     * Evalúa dinámicamente qué componentes existen en la vista antes de poblarlos para evitar
     * errores de puntero nulo (NullPointerException) en las vistas condicionales.
     *
     * @param config El objeto {@link AdapterConfig} con los datos a cargar.
     */
    public void loadDataFromConfig(ComponentJsonData.AdapterConfig config) {
        if (config == null) return;
        this.currentConfig = config;
        this.isPopulating = true;
        try {
            // JMS
            if (view.getJmsConnectionCombo() != null) view.getJmsConnectionCombo().setSelectedItem(config.jmsConnection);
            if (view.getQueueNameField() != null) view.getQueueNameField().setText(config.queueName);

            // Async API
            if (view.getJavaClassNameField() != null) view.getJavaClassNameField().setText(config.javaClassName);

            // Comunes
            if (view.getMessageTypeCombo() != null) {
                view.getMessageTypeCombo().setSelectedItem(config.messageType);
                this.lastSelectedType = config.messageType != null ? config.messageType : "";
            } else {
                this.lastSelectedType = "";
            }

            if (view.getIsCriticalSwitch() != null) {
                view.getIsCriticalSwitch().setSelected(config.isCritical);
            }

            // Forzamos la carga inicial de las etiquetas correctas y la tabla correspondiente
            view.updateFieldsContext(lastSelectedType);
            loadBufferToTable(lastSelectedType);

        } finally {
            this.isPopulating = false;
        }
    }

    /**
     * Extrae todos los datos introducidos por el usuario en la interfaz visual,
     * actualiza el buffer activo y devuelve la configuración consolidada.
     *
     * @return El objeto {@link AdapterConfig} con la información actual del formulario y de todos los buffers.
     */
    public ComponentJsonData.AdapterConfig getDataFromUI() {
        // Aseguramos guardar los últimos cambios de la tabla activa antes de extraer
        saveCurrentListToBuffer(lastSelectedType);

        // JMS
        if (view.getJmsConnectionCombo() != null) {
            currentConfig.jmsConnection = (String) view.getJmsConnectionCombo().getSelectedItem();
        }
        if (view.getQueueNameField() != null) {
            currentConfig.queueName = view.getQueueNameField().getText();
        }

        // Async API
        if (view.getJavaClassNameField() != null) {
            currentConfig.javaClassName = view.getJavaClassNameField().getText();
        }

        // Comunes
        if (view.getMessageTypeCombo() != null) {
            currentConfig.messageType = (String) view.getMessageTypeCombo().getSelectedItem();
        }
        if (view.getIsCriticalSwitch() != null) {
            currentConfig.isCritical = view.getIsCriticalSwitch().isSelected();
        }

        return currentConfig;
    }

    /**
     * Dispara el callback de notificación de cambios, indicando al editor padre
     * que la información contenida en esta pestaña ha sido alterada por el usuario.
     */
    private void notifyChange() {
        if (!isPopulating && onFormChanged != null) {
            onFormChanged.run();
        }
    }
}