package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.components.AtiLabeledComponent;
import com.bbva.gkxj.atiframework.components.AtiScriptPanel;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData.EdgeData;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Editor de propiedades para las conexiones (Edges) entre nodos del workflow.
 * <p>
 * Este editor implementa {@link ComponentEditorStrategy} para gestionar la configuración de las aristas.
 * Utiliza el sistema de componentes Ati ({@link AtiTextField}, {@link AtiLabeledComponent}, etc.)
 * para mantener la coherencia visual con el resto del plugin.
 * </p>
 * <p>
 * Actualmente, su funcionalidad principal se activa cuando el nodo de origen es un <b>Router</b>,
 * permitiendo definir la lógica de encaminamiento (routing) mediante un panel de scripts.
 * </p>
 */
public class EdgePropertiesEditor implements ComponentEditorStrategy {

    /** Panel contenedor principal del editor. */
    private JPanel contentPanel;

    /** Callback para notificar cambios en la UI hacia el modelo del grafo o controlador principal. */
    private Runnable onChangeCallback;

    /** Referencia a los datos de la conexión (arista) actual que se está editando. */
    private EdgeData currentData;

    /** Campo para mostrar el tipo de canal (Directo, Cola, etc.). Generalmente de solo lectura. */
    private AtiTextField channelTypeField;

    /** Área de texto avanzada para definir el script de routing cuando la conexión nace de un Router. */
    private AtiScriptPanel scriptRoutingField;

    /**
     * Inicializa el contenedor básico del editor de la conexión.
     * * @param onChangeCallback Acción a ejecutar cuando se detecten cambios en los campos.
     * @return El panel principal ({@link JPanel}) configurado con layout vertical.
     */
    @Override
    public JPanel buildUI(Runnable onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.setBackground(JBColor.PanelBackground);
        return contentPanel;
    }

    /**
     * Carga y renderiza dinámicamente la interfaz de usuario basada en los datos de la arista.
     * <p>
     * Limpia el panel y, si el nodo de origen es un Router, construye la interfaz
     * para editar el script de ruteo. En otros casos, el panel permanece vacío.
     * </p>
     * * @param value Objeto de datos, se espera una instancia de {@link EdgeData}.
     */
    @Override
    public void loadData(Object value) {
        contentPanel.removeAll();

        if (!(value instanceof EdgeData)) return;
        this.currentData = (EdgeData) value;

        if ("Router".equalsIgnoreCase(currentData.getSourceType())) {
            buildRouterUI();
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Construye los componentes visuales específicos para conexiones de salida de un nodo Router.
     * Incluye la visualización del tipo de canal y el editor de scripts de enrutamiento.
     */
    private void buildRouterUI() {
        // --- 1. Título de Sección ---
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titleLabel = new JLabel("Flow Details");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.setBorder(JBUI.Borders.empty(10, 0, 15, 0));
        contentPanel.add(titlePanel);

        // --- 2. Campo: Tipo de Canal (Solo lectura) ---
        String cType = currentData.getChannelType() != null ? currentData.getChannelType() : "DIRECT";
        channelTypeField = new AtiTextField();
        channelTypeField.setText(cType);
        channelTypeField.setEditable(false);
        // Estilo de solo lectura manual para este campo específico adaptado al tema del IDE
        channelTypeField.setBackground(new JBColor(new Color(245, 245, 245), new Color(60, 63, 65)));
        channelTypeField.setForeground(JBColor.GRAY);

        // Envolvemos con AtiLabeledComponent y lo añadimos al flujo
        addFieldToPanel("Channel Type", channelTypeField);

        // --- 3. Campo: Script de Routing ---
        scriptRoutingField = new AtiScriptPanel();
        if (currentData.getScriptRouting() != null) {
            scriptRoutingField.setText(currentData.getScriptRouting());
        }

        // Listener para detectar cambios en el script de ruteo
        scriptRoutingField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { notifyChange(); }
            @Override public void removeUpdate(DocumentEvent e) { notifyChange(); }
            @Override public void changedUpdate(DocumentEvent e) { notifyChange(); }
        });

        // Al AtiScriptPanel le damos un tamaño mínimo preferido antes de envolverlo
        scriptRoutingField.setPreferredSize(new Dimension(0, 150));
        addFieldToPanel("Script Routing", scriptRoutingField);
    }

    /**
     * Persiste los cambios realizados en la interfaz hacia el objeto de datos de la arista.
     * <p>
     * Solo extrae la información si la conexión actual proviene de un nodo tipo Router.
     * </p>
     * * @param value Objeto de datos ({@link EdgeData}) donde se guardará la información.
     */
    @Override
    public void saveData(Object value) {
        if (value instanceof EdgeData && currentData != null && "Router".equalsIgnoreCase(currentData.getSourceType())) {
            EdgeData data = (EdgeData) value;
            if (scriptRoutingField != null) {
                data.setScriptRouting(scriptRoutingField.getText().trim());
            }
        }
    }

    /**
     * Ejecuta el callback de notificación de cambios para disparar la sincronización
     * con el modelo general del workflow.
     */
    private void notifyChange() {
        if (onChangeCallback != null) onChangeCallback.run();
    }

    /**
     * Utilidad local para empaquetar un componente de entrada dentro de un {@link AtiLabeledComponent}.
     * Añade el componente resultante al panel principal respetando los márgenes y espaciados verticales.
     * * @param label Texto descriptivo que aparecerá encima del campo.
     * @param field Componente de entrada visual (ej. {@link AtiTextField}, {@link AtiScriptPanel}).
     */
    private void addFieldToPanel(String label, JComponent field) {
        AtiLabeledComponent labeledComponent = new AtiLabeledComponent(label, field);
        labeledComponent.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Contenedor extra para asegurar el margen inferior (espaciado entre campos)
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(labeledComponent);
        wrapper.add(Box.createRigidArea(new Dimension(0, 15))); // 15px de separación vertical

        contentPanel.add(wrapper);
    }
}