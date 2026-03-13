package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.AdapterFieldData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

import static com.bbva.gkxj.atiframework.filetype.component.utils.ComponentConstants.*;

/**
 * Vista unificada para adaptadores que soporta protocolos JMS y Async API.
 * <p>
 * Esta clase gestiona una interfaz dinámica que adapta sus componentes de cabecera
 * según el subtipo de adaptador proporcionado. Utiliza un {@link AtiTableSplitterPanel}
 * para la gestión de listas de campos inmutables y permite la edición detallada
 * a través de {@link FieldDetailView}.
 * </p>
 * <p>
 * Implementa una lógica de "descubrimiento" de componentes mediante recursividad para
 * actualizar etiquetas internas que son inaccesibles (final) en la librería base.
 * </p>
 */
public class AdapterTabView extends JPanel {

    // --- Campos Cabecera JMS ---
    /** Combo para seleccionar la conexión JMS. */
    private AtiComboBox jmsConnectionCombo;
    /** Campo de texto para el nombre de la cola (Queue). */
    private AtiTextField queueNameField;
    /** Switch personalizado para marcar la criticidad del adaptador. */
    private JToggleButton isCriticalSwitch;

    // --- Campos Cabecera Async API ---
    /** Campo de texto para la clase Java que implementa el contrato Async API. */
    private AtiTextField javaClassNameField;

    // --- Campos Comunes ---
    /** Combo para seleccionar el tipo de mensaje (XML, JSON, CSV). */
    private AtiComboBox messageTypeCombo;

    // --- Paneles de Campos ---
    /** Panel divisor que contiene la tabla de campos a la izquierda y el detalle a la derecha. */
    private AtiTableSplitterPanel<AdapterFieldData> splitterPanel;
    /** Vista de detalle para la edición de campos individuales (Panel derecho del Splitter). */
    private FieldDetailView detailView;
    /** Contenedor que permite ocultar o mostrar la sección de campos dinámicamente. */
    private JPanel fieldsSectionContainer;

    /** Dirección del componente: Input o Output. */
    private final String componentType;
    /** Protocolo del adaptador: JMS o Async API. */
    private final String subtype;

    /**
     * Constructor del Tab unificado de Adaptadores.
     * * @param componentType El tipo de componente (ej. {@code TYPE_INPUT_ADAPTER}).
     * @param subtype El subtipo de comunicación (ej. {@code SUBTYPE_JMS} o {@code SUBTYPE_ASYNC_API}).
     */
    public AdapterTabView(String componentType, String subtype) {
        this.componentType = componentType;
        this.subtype = subtype;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initComponents();
    }

    /**
     * Inicializa los componentes de la interfaz de usuario.
     * <p>
     * Divide la construcción en dos bloques: la cabecera (dinámica según el protocolo)
     * y la sección de campos (común para adaptadores con carga de datos).
     * </p>
     */
    private void initComponents() {

        // --- 1. CONFIGURACIÓN DE CABECERA (DINÁMICA) ---
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setOpaque(false);
        gridPanel.setBorder(JBUI.Borders.empty(20, 20, 0, 20)); // Márgenes para la cabecera
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 20, 20);

        if (SUBTYPE_ASYNC_API.equals(subtype)) {
            javaClassNameField = WorkflowThemeUtils.createThemedTextField();

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
            gridPanel.add(new AtiLabeledComponent("Java Class Name", javaClassNameField), gbc);

            if (TYPE_INPUT_ADAPTER.equals(componentType)) {
                messageTypeCombo = new AtiComboBox(new Object[]{"", "XML", "JSON"});
                WorkflowThemeUtils.applyWorkflowTheme(messageTypeCombo);
                gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
                gridPanel.add(new AtiLabeledComponent("Message Type", messageTypeCombo), gbc);
            }

        } else {
            jmsConnectionCombo = new AtiComboBox(new Object[]{"", "Operation", "Admin"});
            WorkflowThemeUtils.applyWorkflowTheme(jmsConnectionCombo);
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
            gridPanel.add(new AtiLabeledComponent("JMS Connection", jmsConnectionCombo), gbc);

            queueNameField = WorkflowThemeUtils.createThemedTextField();

            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
            gridPanel.add(new AtiLabeledComponent("Queue Name", queueNameField), gbc);

            messageTypeCombo = new AtiComboBox(MESSAGE_TYPE_LIST);
            WorkflowThemeUtils.applyWorkflowTheme(messageTypeCombo);
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
            gridPanel.add(new AtiLabeledComponent("Message Type", messageTypeCombo), gbc);

            if (TYPE_INPUT_ADAPTER.equals(componentType)) {
                isCriticalSwitch = createCustomSwitch();
                JPanel swPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                swPanel.setOpaque(false);
                swPanel.add(isCriticalSwitch);
                swPanel.add(new JLabel("Is Critical"));
                gbc.gridx = 1; gbc.gridy = 1;
                gridPanel.add(swPanel, gbc);
            }
        }

        // AÑADIMOS LA CABECERA AL NORTE (Se queda pegada arriba y no crece hacia abajo)
        add(gridPanel, BorderLayout.NORTH);

        // --- 2. SECCIÓN DINÁMICA DE CAMPOS (SPLITTER + DETAIL) ---
        fieldsSectionContainer = new JPanel(new BorderLayout());
        fieldsSectionContainer.setOpaque(false);
        fieldsSectionContainer.setBorder(JBUI.Borders.empty(0, 20, 20, 20)); // Márgenes para el cuerpo
        fieldsSectionContainer.setVisible(false);

        detailView = new FieldDetailView(this.componentType, () -> {});

        Dimension hackSize = new Dimension(100, 500);
        detailView.setPreferredSize(hackSize);
        detailView.setMinimumSize(hackSize);

        splitterPanel = new AtiTableSplitterPanel<>(
                "Fields",
                "Fields",
                AdapterFieldData::new,
                item -> String.format("%02d", (splitterPanel.getDataList().indexOf(item) + 1)),
                item -> item.fieldName != null ? item.fieldName : "New Field",
                detailView
        );

        splitterPanel.setMinimumSize(new Dimension(250, 0));

        fieldsSectionContainer.add(splitterPanel, BorderLayout.CENTER);

        add(fieldsSectionContainer, BorderLayout.CENTER);
    }

    /**
     * Refresca la visibilidad de la sección de campos y actualiza el contexto de extracción.
     * <p>
     * Se encarga de ocultar la sección si el tipo de mensaje no está definido o si
     * se trata de un Output Adapter para Async API (según definición de negocio).
     * </p>
     * * @param type El tipo de mensaje seleccionado (XML, JSON, CSV).
     */
    public void updateFieldsContext(String type) {
        // En Async API Output Adapter la zona de campos NUNCA se muestra.
        if (SUBTYPE_ASYNC_API.equals(subtype) && TYPE_OUTPUT_ADAPTER.equals(componentType)) {
            fieldsSectionContainer.setVisible(false);
            return;
        }

        // Para el resto (JMS Input/Output o Async API Input), validamos si hay tipo seleccionado
        boolean hasType = type != null && !type.trim().isEmpty();
        fieldsSectionContainer.setVisible(hasType);

        if (hasType) {
            // 1. Etiqueta del formulario derecho (XPath vs JSONPath)
            detailView.updateExtractionLabel(type);

            // 2. Actualizar el título del componente custom
            try {
                splitterPanel.setTitle(type + " Fields");
            } catch (Exception e) {
                // Fallback a lógica de descubrimiento si setTitle falla
                updateInternalSplitterLabel(type + " Fields");
            }
        }

        revalidate();
        repaint();
    }

    /**
     * Accede internamente a las etiquetas del SplitterPanel mediante descubrimiento recursivo.
     * * @param newText El nuevo texto base para el prefijo de la etiqueta.
     */
    private void updateInternalSplitterLabel(String newText) {
        findAndSetLabel(splitterPanel, newText);
    }

    /**
     * Busca recursivamente un JLabel que contenga paréntesis dentro del contenedor y actualiza su texto.
     * * @param container Contenedor donde iniciar la búsqueda.
     * @param text Texto base a asignar antes del paréntesis.
     */
    private void findAndSetLabel(Container container, String text) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel && ((JLabel) comp).getText().contains("(")) {
                String currentText = ((JLabel) comp).getText();
                int startIndex = currentText.indexOf("(");
                if (startIndex != -1) {
                    ((JLabel) comp).setText(text + " " + currentText.substring(startIndex));
                }
                return;
            } else if (comp instanceof Container) {
                findAndSetLabel((Container) comp, text);
            }
        }
    }

    // --- GETTERS ---

    /** @return Desplegable de conexiones JMS. */
    public AtiComboBox getJmsConnectionCombo() { return jmsConnectionCombo; }
    /** @return Campo del nombre de cola JMS. */
    public AtiTextField getQueueNameField() { return queueNameField; }
    /** @return Desplegable del tipo de mensaje. */
    public AtiComboBox getMessageTypeCombo() { return messageTypeCombo; }
    /** @return Botón tipo switch para criticidad. */
    public JToggleButton getIsCriticalSwitch() { return isCriticalSwitch; }
    /** @return Campo para el nombre de la clase Java (Async API). */
    public AtiTextField getJavaClassNameField() { return javaClassNameField; }
    /** @return Panel de tabla y divisor de campos. */
    public AtiTableSplitterPanel<AdapterFieldData> getSplitterPanel() { return splitterPanel; }
    /** @return Vista de detalle del campo seleccionado. */
    public FieldDetailView getDetailView() { return detailView; }

    /**
     * Crea un componente {@link JToggleButton} con apariencia de switch Material Design.
     * * @return El switch configurado con renderizado personalizado.
     */
    private JToggleButton createCustomSwitch() {
        JToggleButton sw = new JToggleButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? new Color(255, 64, 129) : Color.LIGHT_GRAY);
                g2.fillRoundRect(0, 8, 34, 16, 16, 16);
                g2.setColor(Color.WHITE);
                g2.fillOval(isSelected() ? 18 : 2, 6, 20, 20);
                g2.dispose();
            }
        };
        sw.setPreferredSize(new Dimension(40, 30));
        sw.setBorder(null);
        sw.setContentAreaFilled(false);
        return sw;
    }
}